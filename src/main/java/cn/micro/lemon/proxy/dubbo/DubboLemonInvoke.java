package cn.micro.lemon.proxy.dubbo;

import cn.micro.lemon.common.LemonInvoke;
import cn.micro.lemon.common.LemonStatusCode;
import cn.micro.lemon.common.LemonConfig;
import cn.micro.lemon.common.ServiceDefinition;
import cn.micro.lemon.proxy.dubbo.metadata.MetadataCollectorFactory;
import cn.micro.lemon.server.LemonContext;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.common.constants.CommonConstants;
import org.apache.dubbo.common.utils.StringUtils;
import org.apache.dubbo.config.ApplicationConfig;
import org.apache.dubbo.config.ReferenceConfig;
import org.apache.dubbo.config.RegistryConfig;
import org.apache.dubbo.config.utils.ReferenceConfigCache;
import org.apache.dubbo.rpc.RpcContext;
import org.apache.dubbo.rpc.RpcException;
import org.apache.dubbo.rpc.service.GenericService;
import org.micro.neural.extension.Extension;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentMap;

/**
 * Dubbo Lemon Invoke
 *
 * @author lry
 */
@Slf4j
@Extension("dubbo")
public class DubboLemonInvoke implements LemonInvoke {

    private LemonConfig lemonConfig;
    private RegistryConfig registry;
    private MetadataCollectorFactory metadataCollectorFactory;
    private RegistryServiceSubscribe registryServiceSubscribe;

    @Override
    public void initialize(LemonConfig lemonConfig) {
        this.lemonConfig = lemonConfig;
        DubboConfig dubboConfig = lemonConfig.getDubbo();
        RegistryConfig registryConfig = new RegistryConfig();
        registryConfig.setAddress(dubboConfig.getRegistryAddress());
        this.registry = registryConfig;

        this.metadataCollectorFactory = MetadataCollectorFactory.INSTANCE;
        metadataCollectorFactory.initialize(lemonConfig);
        this.registryServiceSubscribe = new RegistryServiceSubscribe();
        registryServiceSubscribe.initialize();
    }

    @Override
    public Object invoke(LemonContext context) {
        context.setSendTime(System.currentTimeMillis());
        // setter request header list
        for (Map.Entry<String, String> entry : context.getHeaders().entrySet()) {
            // originalReqHeaders contains or starts with 'X-'
            if (lemonConfig.getOriginalReqHeaders().contains(entry.getKey())
                    || entry.getKey().startsWith(LemonContext.HEADER_PREFIX)) {
                RpcContext.getContext().setAttachment(entry.getKey(), entry.getValue());
            }
        }

        // call original remote
        ServiceDefinition serviceDefinition = buildServiceDefinition(context);
        GenericService genericService = buildGenericService(context, serviceDefinition);
        Object result = genericService.$invoke(serviceDefinition.getMethod(),
                serviceDefinition.getParamTypes(), serviceDefinition.getParamValues());

        // setter response header list
        for (Map.Entry<String, String> entry : RpcContext.getContext().getAttachments().entrySet()) {
            // originalResHeaders contains or starts with 'X-'
            if (lemonConfig.getOriginalResHeaders().contains(entry.getKey())
                    || entry.getKey().startsWith(LemonContext.HEADER_PREFIX)) {
                context.getResHeaders().put(entry.getKey(), entry.getValue());
            }
        }

        return result;
    }

    @SuppressWarnings("unchecked")
    @Override
    public CompletableFuture<Object> invokeAsync(LemonContext context) {
        Object object = invoke(context);
        if (object instanceof CompletableFuture) {
            return (CompletableFuture<Object>) object;
        }

        return CompletableFuture.completedFuture(object);
    }

    @Override
    public LemonStatusCode failure(LemonContext context, Throwable throwable) {
        if (throwable instanceof RpcException) {
            RpcException e = (RpcException) throwable;
            if (e.isTimeout()) {
                return LemonStatusCode.CALL_ORIGINAL_TIMEOUT;
            } else if (e.isBiz()) {
                return LemonStatusCode.CALL_ORIGINAL_BIZ_ERROR;
            } else if (e.isNetwork()) {
                return LemonStatusCode.CALL_ORIGINAL_NETWORK_ERROR;
            } else if (e.isSerialization()) {
                return LemonStatusCode.CALL_ORIGINAL_SERIALIZATION;
            }
        }

        return LemonStatusCode.CALL_ORIGINAL_UNKNOWN;
    }

    @Override
    public void destroy() {
        if (registryServiceSubscribe != null) {
            registryServiceSubscribe.destroy();
        }
    }

    /**
     * The build {@link ServiceDefinition} by {@link LemonContext}
     *
     * @param context {@link LemonContext}
     * @return {@link ServiceDefinition}
     */
    private ServiceDefinition buildServiceDefinition(LemonContext context) {
        List<String> paths = context.getPaths();
        if (paths.size() != 4) {
            throw new IllegalArgumentException("Illegal Request");
        }

        ServiceDefinition serviceDefinition = new ServiceDefinition();
        serviceDefinition.setApplication(paths.get(1));
        serviceDefinition.setService(paths.get(2));
        serviceDefinition.setMethod(paths.get(3));

        // wrapper service name
        wrapperServiceName(serviceDefinition);

        Map<String, String> parameters = context.getParameters();
        if (parameters.containsKey(CommonConstants.GROUP_KEY)) {
            String group = parameters.get(CommonConstants.GROUP_KEY);
            if (group != null && group.length() > 0) {
                serviceDefinition.setGroup(group);
            }
        }
        if (parameters.containsKey(CommonConstants.VERSION_KEY)) {
            String version = parameters.get(CommonConstants.VERSION_KEY);
            if (version != null && version.length() > 0) {
                serviceDefinition.setVersion(version);
            }
        }

        List<Object> paramValues = new ArrayList<>();
        if (JSON.isValid(context.getContent())) {
            paramValues.add(JSON.parse(context.getContent()));
        } else {
            paramValues.add(context.getContent());
        }

        serviceDefinition.setParamValues(paramValues.toArray(new Object[0]));
        return serviceDefinition;
    }

    private void wrapperServiceName(ServiceDefinition serviceDefinition) {
        ConcurrentMap<String, String> serviceNames =
                registryServiceSubscribe.getServiceNames().get(serviceDefinition.getApplication());
        if (serviceNames == null || serviceNames.isEmpty()) {
            serviceDefinition.setServiceName(serviceDefinition.getService());
            return;
        }

        String serviceName = serviceNames.get(serviceDefinition.getService());
        if (StringUtils.isBlank(serviceName)) {
            serviceDefinition.setServiceName(serviceDefinition.getService());
            return;
        }

        serviceDefinition.setServiceName(serviceName);
    }

    /**
     * The build {@link GenericService} by {@link ServiceDefinition}
     *
     * @param context           {@link LemonContext}
     * @param serviceDefinition {@link ServiceDefinition}
     * @return {@link GenericService}
     */
    private GenericService buildGenericService(LemonContext context, ServiceDefinition serviceDefinition) {
        ReferenceConfig<GenericService> referenceConfig = new ReferenceConfig<>();
        referenceConfig.setApplication(new ApplicationConfig(serviceDefinition.getApplication()));
        referenceConfig.setGroup(serviceDefinition.getGroup());
        referenceConfig.setVersion(serviceDefinition.getVersion());
        referenceConfig.setRegistry(registry);
        referenceConfig.setInterface(serviceDefinition.getServiceName());
        referenceConfig.setGeneric(true);

        if (serviceDefinition.getParamTypes() == null) {
            metadataCollectorFactory.wrapperTypesFromMetadata(context, serviceDefinition);
        }

        return ReferenceConfigCache.getCache().get(referenceConfig);
    }

}