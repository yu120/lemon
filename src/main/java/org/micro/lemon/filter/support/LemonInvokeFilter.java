package org.micro.lemon.filter.support;

import org.micro.lemon.common.LemonInvoke;
import org.micro.lemon.common.LemonStatusCode;
import org.micro.lemon.common.LemonConfig;
import org.micro.lemon.common.ServiceMapping;
import org.micro.lemon.filter.AbstractFilter;
import org.micro.lemon.filter.LemonChain;
import org.micro.lemon.filter.LemonFactory;
import org.micro.lemon.server.LemonContext;
import lombok.extern.slf4j.Slf4j;
import org.micro.lemon.extension.Extension;
import org.micro.lemon.extension.ExtensionLoader;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Lemon Invoke Filter
 *
 * @author lry
 */
@Slf4j
@Extension(value = "invoke", order = 100, category = LemonFactory.ROUTER)
public class LemonInvokeFilter extends AbstractFilter {

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
        LemonInvoke lemonInvoke = lemonInvokes.get("jsoup");
        if (lemonInvoke == null) {
            context.onCallback(LemonStatusCode.NOT_FOUND);
            return;
        }

        CompletableFuture<LemonContext> future = lemonInvoke.invokeAsync(context);
        if (future == null) {
            log.error("The completable future is null by context:{}", context);
            return;
        }

        future.whenComplete((result, throwable) -> {
            if (throwable != null) {
                log.error(throwable.getMessage(), throwable);
                LemonStatusCode statusCode = lemonInvoke.failure(context, throwable);
                context.onCallback(statusCode);
                return;
            }

            try {
                super.preFilter(chain, context);
            } catch (Throwable t) {
                log.error(t.getMessage(), t);
            } finally {
                context.onCallback(LemonStatusCode.SUCCESS);
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
