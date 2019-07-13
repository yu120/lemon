package cn.micro.lemon.http;

import cn.micro.lemon.LemonConfig;
import cn.micro.lemon.LemonInvoke;
import cn.micro.lemon.LemonStatusCode;
import cn.micro.lemon.filter.LemonContext;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.micro.neural.extension.Extension;

import java.util.concurrent.CompletableFuture;

/**
 * Jsoup Lemon Invoke
 *
 * @author lry
 */
@Slf4j
@Extension("jsoup")
public class JsoupLemonInvoke implements LemonInvoke {

    @Override
    public void initialize(LemonConfig lemonConfig) {

    }

    @Override
    public Object invoke(LemonContext context) {
        Connection connection = Jsoup.connect("");
        Connection.Request request = connection.request();

        return null;
    }

    @Override
    public CompletableFuture<Object> invokeAsync(LemonContext context) {
        return null;
    }

    @Override
    public LemonStatusCode failure(LemonContext context, Throwable throwable) {
        return LemonStatusCode.CALL_ORIGINAL_UNKNOWN;
    }

    @Override
    public void destroy() {

    }

}