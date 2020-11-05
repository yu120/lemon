package org.micro.lemon.filter.support;

import org.micro.lemon.common.LemonInvoke;
import org.micro.lemon.common.LemonStatusCode;
import org.micro.lemon.common.LemonConfig;
import org.micro.lemon.common.ServiceMapping;
import org.micro.lemon.filter.IFilter;
import org.micro.lemon.filter.LemonChain;
import org.micro.lemon.filter.LemonFactory;
import org.micro.lemon.server.LemonContext;
import lombok.extern.slf4j.Slf4j;
import org.micro.lemon.extension.Extension;
import org.micro.lemon.extension.ExtensionLoader;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * LemonInvokeFilter
 *
 * @author lry
 */
@Slf4j
@Extension(value = "invoke", order = 100, category = LemonFactory.ROUTER)
public class LemonInvokeFilter implements IFilter {

    private final ConcurrentMap<String, LemonInvoke> lemonInvokes = new ConcurrentHashMap<>();

    @Override
    public void initialize(LemonConfig lemonConfig) {
        // 计算配置文件中的协议
        Set<String> serviceProtocol = new HashSet<>();
        for (ServiceMapping serviceMapping : lemonConfig.getServices()) {
            serviceProtocol.add(serviceMapping.getProtocol());
        }

        List<LemonInvoke> lemonInvokeList = ExtensionLoader.getLoader(LemonInvoke.class).getExtensions();
        for (LemonInvoke lemonInvoke : lemonInvokeList) {
            Extension extension = lemonInvoke.getClass().getAnnotation(Extension.class);
            if (extension != null) {
                // 只启动配置文件中的配置
                if (serviceProtocol.contains(extension.value())) {
                    lemonInvokes.put(extension.value(), lemonInvoke);
                    lemonInvoke.initialize(lemonConfig);
                }
            }
        }
    }

    @Override
    public void preFilter(LemonChain chain, LemonContext context) throws Throwable {
        LemonInvoke lemonInvoke = lemonInvokes.get("dubbo");
        if (lemonInvoke == null) {
            context.callback(LemonStatusCode.NOT_FOUND);
            return;
        }

        context.setFuture(lemonInvoke.invokeAsync(context));
        if (context.getFuture() == null) {
            log.error("The completable future is null by context:{}", context);
            return;
        }

        context.getFuture().whenComplete((result, throwable) -> {
            if (throwable != null) {
                log.error("Invoke exception", throwable);
                context.callback(lemonInvoke.failure(context, throwable));
                return;
            }

            try {
                chain.doFilter(context);
                context.callback(LemonStatusCode.SUCCESS);
            } catch (Throwable t) {
                throw new RuntimeException(t);
            }
        });
    }

    @Override
    public void destroy() {
        for (ConcurrentMap.Entry<String, LemonInvoke> entry : lemonInvokes.entrySet()) {
            entry.getValue().destroy();
        }
    }

}
