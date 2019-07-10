package cn.micro.lemon.dubbo;

import cn.micro.lemon.LemonInvoke;
import cn.micro.lemon.MicroConfig;
import cn.micro.lemon.filter.IFilter;
import cn.micro.lemon.filter.LemonChain;
import cn.micro.lemon.filter.LemonContext;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import org.apache.dubbo.common.constants.CommonConstants;
import org.micro.neural.extension.Extension;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Extension(order = 20)
public class DubboInvokeFilter implements IFilter {

    private LemonInvoke lemonInvoke;

    @Override
    public void initialize(MicroConfig microConfig) {
        this.lemonInvoke = new DubboLemonInvoke();
        lemonInvoke.initialize(microConfig);
    }

    @Override
    public void doFilter(LemonChain chain, LemonContext context) throws Throwable {
        ServiceDefinition serviceDefinition = buildServiceDefinition(context);
        CompletableFuture<Object> future = lemonInvoke.invokeAsync(serviceDefinition);
        future.whenComplete((result, t) -> context.writeAndFlush(result));
    }

    @Override
    public void destroy() {

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
            return null;
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
