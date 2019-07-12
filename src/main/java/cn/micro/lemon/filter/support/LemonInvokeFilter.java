package cn.micro.lemon.filter.support;

import cn.micro.lemon.LemonInvoke;
import cn.micro.lemon.LemonStatusCode;
import cn.micro.lemon.LemonConfig;
import cn.micro.lemon.dubbo.DubboLemonInvoke;
import cn.micro.lemon.filter.AbstractFilter;
import cn.micro.lemon.filter.LemonChain;
import cn.micro.lemon.filter.LemonContext;
import lombok.extern.slf4j.Slf4j;
import org.micro.neural.extension.Extension;

import java.util.concurrent.CompletableFuture;

/**
 * Lemon Invoke Filter
 *
 * @author lry
 */
@Slf4j
@Extension(order = 100, category = LemonChain.ROUTER)
public class LemonInvokeFilter extends AbstractFilter {

    private LemonInvoke lemonInvoke;

    @Override
    public void initialize(LemonConfig lemonConfig) {
        this.lemonInvoke = new DubboLemonInvoke();
        lemonInvoke.initialize(lemonConfig);
    }

    @Override
    public void preFilter(LemonChain chain, LemonContext context) throws Throwable {
        CompletableFuture<Object> future = lemonInvoke.invokeAsync(context);
        if (future == null) {
            log.error("The completable future is null by context:{}", context);
            return;
        }

        future.whenComplete((result, throwable) -> {
            if (throwable != null) {
                log.error(throwable.getMessage(), throwable);
                LemonStatusCode statusCode = lemonInvoke.failure(context, throwable);
                context.writeAndFlush(statusCode);
                return;
            } else {
                context.setResult(result);
            }

            try {
                super.preFilter(chain, context);
            } catch (Throwable t) {
                log.error(t.getMessage(), t);
            } finally {
                context.writeAndFlush(LemonStatusCode.SUCCESS);
            }
        });
    }

    @Override
    public void destroy() {
        if (lemonInvoke != null) {
            lemonInvoke.destroy();
        }
    }

}
