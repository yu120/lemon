package cn.micro.lemon.dubbo;

import cn.micro.lemon.LemonInvoke;
import cn.micro.lemon.LemonStatusCode;
import cn.micro.lemon.LemonConfig;
import cn.micro.lemon.ServiceDefinition;
import cn.micro.lemon.dubbo.metadata.MetadataCollectorFactory;
import cn.micro.lemon.filter.LemonContext;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.common.constants.CommonConstants;
import org.apache.dubbo.config.ApplicationConfig;
import org.apache.dubbo.config.ReferenceConfig;
import org.apache.dubbo.config.RegistryConfig;
import org.apache.dubbo.config.utils.ReferenceConfigCache;
import org.apache.dubbo.rpc.RpcContext;
import org.apache.dubbo.rpc.RpcException;
import org.apache.dubbo.rpc.service.GenericService;
import org.micro.neural.extension.Extension;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Dubbo Lemon Invoke
 *
 * @author lry
 */
@Slf4j
@Extension("dubbo")
public class DubboLemonInvoke implements LemonInvoke {

    private RegistryConfig registry;
    private MetadataCollectorFactory metadataCollectorFactory;

    @Override
    public void initialize(LemonConfig lemonConfig) {
        RegistryConfig registryConfig = new RegistryConfig();
        registryConfig.setAddress(lemonConfig.getDubbo().getRegistryAddress());
        this.registry = registryConfig;

        this.metadataCollectorFactory = new MetadataCollectorFactory();
        metadataCollectorFactory.initialize(lemonConfig.getDubbo().getMetadataAddress());
    }

    @Override
    public Object invoke(LemonContext context) {
        Map<String, String> attachment = new LinkedHashMap<>();
        attachment.put(LemonContext.LEMON_ID, context.getId());
        RpcContext.getContext().setAttachments(attachment);

        context.setSendTime(System.currentTimeMillis());

        ServiceDefinition serviceDefinition = buildServiceDefinition(context);
        GenericService genericService = buildGenericService(serviceDefinition);
        return genericService.$invoke(serviceDefinition.getMethod(),
                serviceDefinition.getParamTypes(), serviceDefinition.getParamValues());
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

    }

    private GenericService buildGenericService(ServiceDefinition serviceDefinition) {
        ReferenceConfig<GenericService> referenceConfig = new ReferenceConfig<>();
        referenceConfig.setApplication(new ApplicationConfig(serviceDefinition.getApplication()));
        referenceConfig.setGroup(serviceDefinition.getGroup());
        referenceConfig.setVersion(serviceDefinition.getVersion());
        referenceConfig.setRegistry(registry);
        referenceConfig.setInterface(serviceDefinition.getService());
        referenceConfig.setGeneric(true);

        if (serviceDefinition.getParamTypes() == null) {
            metadataCollectorFactory.wrapperTypesFromMetadata(serviceDefinition);
        }

        return ReferenceConfigCache.getCache().get(referenceConfig);
    }

    private ServiceDefinition buildServiceDefinition(LemonContext context) {
        List<String> paths = context.getPaths();
        if (paths.size() != 4) {
            throw new IllegalArgumentException("Illegal Request");
        }

        ServiceDefinition serviceDefinition = new ServiceDefinition();
        serviceDefinition.setApplication(paths.get(1));
        serviceDefinition.setService(paths.get(2));
        serviceDefinition.setMethod(paths.get(3));

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
            Object object = JSON.parse(context.getContent());
            if (object instanceof JSONArray) {
                paramValues.addAll(((JSONArray) object).toJavaList(Map.class));
            } else {
                paramValues.add(object);
            }
        } else {
            paramValues.add(context.getContent());
        }

        serviceDefinition.setParamValues(paramValues.toArray(new Object[0]));
        return serviceDefinition;
    }

}