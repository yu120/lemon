package cn.micro.lemon.dubbo;

import cn.micro.lemon.LemonInvoke;
import cn.micro.lemon.MicroConfig;
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
import org.apache.dubbo.rpc.RpcException;
import org.apache.dubbo.rpc.service.GenericService;
import org.micro.neural.extension.Extension;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Extension("dubbo")
public class DubboLemonInvoke implements LemonInvoke {

    private ApplicationConfig application;
    private RegistryConfig registry;
    private MetadataCollectorFactory metadataCollectorFactory;

    @Override
    public void initialize(MicroConfig microConfig) {
        ApplicationConfig applicationConfig = new ApplicationConfig();
        applicationConfig.setName(microConfig.getApplication());
        this.application = applicationConfig;

        RegistryConfig registryConfig = new RegistryConfig();
        registryConfig.setAddress(microConfig.getDubbo().getRegistryAddress());
        this.registry = registryConfig;

        this.metadataCollectorFactory = new MetadataCollectorFactory();
        metadataCollectorFactory.initialize(microConfig.getDubbo().getMetadataAddress());
    }

    @Override
    public Object invoke(LemonContext context) {
        ServiceDefinition serviceDefinition = buildServiceDefinition(context);

        try {
            GenericService genericService = buildGenericService(serviceDefinition);
            log.debug("Dubbo generic invoke is starting, service definition is {}, generic service is {}.", serviceDefinition, genericService);
            Object result = genericService.$invoke(serviceDefinition.getMethod(),
                    serviceDefinition.getParamTypes(), serviceDefinition.getParamValues());
            log.debug("Dubbo generic invoke is end, service definition is {}, return result is {}.", serviceDefinition, result);
            return result;
        } catch (Exception e) {
            log.error("Dubbo generic invoke failed", e);
            if (e instanceof RpcException) {
                RpcException e1 = (RpcException) e;
                if (e1.isTimeout()) {
                    return ResultCode.TIMEOUT;
                }
                if (e1.isBiz()) {
                    return ResultCode.BIZ_ERROR;
                }
                if (e1.isNetwork()) {
                    return ResultCode.NETWORK_ERROR;
                }
                if (e1.isSerialization()) {
                    return ResultCode.SERIALIZATION;
                }
            }

            throw e;
        }
    }

    @Override
    public CompletableFuture<Object> invokeAsync(LemonContext context) {
        ServiceDefinition serviceDefinition = buildServiceDefinition(context);
        GenericService genericService = buildGenericService(serviceDefinition);
        return genericService.$invokeAsync(serviceDefinition.getMethod(),
                serviceDefinition.getParamTypes(), serviceDefinition.getParamValues());
    }

    @Override
    public void destroy() {

    }

    private GenericService buildGenericService(ServiceDefinition serviceDefinition) {
        ReferenceConfig<GenericService> referenceConfig = new ReferenceConfig<>();
        referenceConfig.setApplication(application);
        referenceConfig.setRegistry(registry);
        referenceConfig.setInterface(serviceDefinition.getService());
        referenceConfig.setGeneric(true);

        if (serviceDefinition.getParamTypes() == null) {
            metadataCollectorFactory.wrapperTypesFromMetadata(serviceDefinition);
        }

        return ReferenceConfigCache.getCache().get(referenceConfig);
    }

    private ServiceDefinition buildServiceDefinition(LemonContext context) {
        String uri = context.getPath();
        if (uri.startsWith("/")) {
            uri = uri.substring(1);
        }
        if (uri.endsWith("/")) {
            uri = uri.substring(0, uri.length() - 1);
        }

        String[] pathArray = uri.split("/");
        if (pathArray.length != 3) {
            throw new IllegalArgumentException("Illegal Request");
        }

        ServiceDefinition serviceDefinition = new ServiceDefinition();
        serviceDefinition.setApplication(pathArray[0]);
        serviceDefinition.setService(pathArray[1]);
        serviceDefinition.setMethod(pathArray[2]);

        Map<String, String> parameters = context.getParameters();
        if (parameters.containsKey(CommonConstants.GROUP_KEY)) {
            serviceDefinition.setGroup(parameters.get(CommonConstants.GROUP_KEY));
        }
        if (parameters.containsKey(CommonConstants.VERSION_KEY)) {
            serviceDefinition.setVersion(parameters.get(CommonConstants.VERSION_KEY));
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