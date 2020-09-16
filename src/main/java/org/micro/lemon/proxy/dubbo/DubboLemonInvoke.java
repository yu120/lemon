package org.micro.lemon.proxy.dubbo;

import org.micro.lemon.common.LemonConfig;
import org.micro.lemon.common.LemonInvoke;
import org.micro.lemon.common.LemonStatusCode;
import org.micro.lemon.common.ServiceMapping;
import org.micro.lemon.common.config.DubboConfig;
import org.micro.lemon.common.config.OriginalConfig;
import org.micro.lemon.proxy.dubbo.metadata.MetadataCollectorFactory;
import org.micro.lemon.server.LemonContext;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.ApplicationConfig;
import org.apache.dubbo.config.ReferenceConfig;
import org.apache.dubbo.config.RegistryConfig;
import org.apache.dubbo.config.utils.ReferenceConfigCache;
import org.apache.dubbo.rpc.RpcContext;
import org.apache.dubbo.rpc.RpcException;
import org.apache.dubbo.rpc.service.GenericService;
import org.micro.lemon.extension.Extension;

import java.util.ArrayList;
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

    private LemonConfig lemonConfig;
    private RegistryConfig registry;
    private MetadataCollectorFactory metadataCollectorFactory;

    @Override
    public void initialize(LemonConfig lemonConfig) {
        this.lemonConfig = lemonConfig;
        DubboConfig dubboConfig = lemonConfig.getDubbo();
        RegistryConfig registryConfig = new RegistryConfig();
        registryConfig.setAddress(dubboConfig.getRegistryAddress());
        this.registry = registryConfig;

        this.metadataCollectorFactory = MetadataCollectorFactory.INSTANCE;
        metadataCollectorFactory.initialize(lemonConfig);
    }

    @Override
    public Object invoke(LemonContext context) {
        OriginalConfig originalConfig = lemonConfig.getOriginal();

        context.setSendTime(System.currentTimeMillis());
        // setter request header list
        for (Map.Entry<String, String> entry : context.getHeaders().entrySet()) {
            // originalReqHeaders contains or starts with 'X-'
            if (originalConfig.getReqHeaders().contains(entry.getKey())
                    || entry.getKey().startsWith(LemonContext.HEADER_PREFIX)) {
                RpcContext.getContext().setAttachment(entry.getKey(), entry.getValue());
            }
        }

        // call original remote
        ServiceMapping serviceMapping = context.getServiceMapping();
        wrapperServiceDefinition(context);
        GenericService genericService = buildGenericService(context, serviceMapping);
        Object result = genericService.$invoke(serviceMapping.getMethod(),
                serviceMapping.getParamTypes(), serviceMapping.getParamValues());

        // setter response header list
        for (Map.Entry<String, String> entry : RpcContext.getContext().getAttachments().entrySet()) {
            // originalResHeaders contains or starts with 'X-'
            if (originalConfig.getResHeaders().contains(entry.getKey())
                    || entry.getKey().startsWith(LemonContext.HEADER_PREFIX)) {
                context.getResHeaders().put(entry.getKey(), entry.getValue());
            }
        }

        return result;
    }

    @Override
    @SuppressWarnings("unchecked")
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

    /**
     * The wrapper ServiceDefinition by {@link LemonContext}
     *
     * @param context {@link LemonContext}
     */
    private void wrapperServiceDefinition(LemonContext context) {
        List<Object> paramValues = new ArrayList<>();
        if (JSON.isValid(context.getContent())) {
            paramValues.add(JSON.parse(context.getContent()));
        } else {
            paramValues.add(context.getContent());
        }

        context.getServiceMapping().setParamValues(paramValues.toArray(new Object[0]));
    }

    /**
     * The build {@link GenericService} by {@link ServiceMapping}
     *
     * @param context        {@link LemonContext}
     * @param serviceMapping {@link ServiceMapping}
     * @return {@link GenericService}
     */
    private GenericService buildGenericService(LemonContext context, ServiceMapping serviceMapping) {
        ReferenceConfig<GenericService> referenceConfig = new ReferenceConfig<>();
        referenceConfig.setApplication(new ApplicationConfig(serviceMapping.getApplication()));
        referenceConfig.setGroup(serviceMapping.getGroup());
        referenceConfig.setVersion(serviceMapping.getVersion());
        referenceConfig.setRegistry(registry);
        referenceConfig.setInterface(serviceMapping.getServiceName());
        referenceConfig.setGeneric(true);

        if (serviceMapping.getParamTypes() == null) {
            metadataCollectorFactory.wrapperTypesFromMetadata(context, serviceMapping);
        }

        return ReferenceConfigCache.getCache().get(referenceConfig);
    }

}