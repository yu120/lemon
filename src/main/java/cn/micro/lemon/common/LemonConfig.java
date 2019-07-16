package cn.micro.lemon.common;

import cn.micro.lemon.dubbo.DubboConfig;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.ToString;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Lemon Config
 *
 * @author lry
 */
@Data
@ToString
public class LemonConfig implements Serializable {

    /**
     * The operation token
     */
    private String token = "lemon";
    /**
     * The lemon http application port
     */
    private int port = 8080;
    /**
     * The lemon http application path
     */
    private String application;
    /**
     * The registry address
     */
    private String lemonRegistry;

    /**
     * The IO thread number
     */
    private int ioThread = 0;
    /**
     * The work thread number
     */
    private int workThread = 0;
    /**
     * The default value: 64MB
     */
    private int maxContentLength = 1024 * 1024 * 64;
    /**
     * The max server conn (all clients conn)
     **/
    private int maxChannel = 100000;
    /**
     * The server model: true
     */
    private boolean server = true;
    /**
     * The biz core thread number
     */
    private int bizCoreThread = 20;
    /**
     * The biz max thread number
     */
    private int bizMaxThread = 200;
    /**
     * The biz queue capacity
     */
    private int bizQueueCapacity = 800;
    /**
     * The biz keep alive time(ms)
     */
    private long bizKeepAliveTime = 60000L;
    /**
     * The biz thread rejected strategy
     */
    private RejectedStrategy rejectedStrategy = RejectedStrategy.ABORT_POLICY;

    /**
     * The configure fixed response header list
     */
    private Map<String, Object> resHeaders = new LinkedHashMap<>();

    /**
     * The global call original server timeout(ms)
     */
    private long originalTimeout = 30000L;
    /**
     * The call original request header list key
     */
    private Set<String> originalReqHeaders = new LinkedHashSet<>();
    /**
     * The call original response header list key
     */
    private Set<String> originalResHeaders = new LinkedHashSet<>();

    /**
     * The dubbo config
     */
    private DubboConfig dubbo;
    /**
     * The direct connection service mapping list
     */
    private List<ServiceMapping> services = new ArrayList<>();

    /**
     * Rejected Strategy
     *
     * @author lry
     */
    @Getter
    @AllArgsConstructor
    public enum RejectedStrategy {

        // ===

        ABORT_POLICY(new ThreadPoolExecutor.AbortPolicy()),
        CALLER_RUNS_POLICY(new ThreadPoolExecutor.CallerRunsPolicy()),
        DISCARD_OLDEST_POLICY(new ThreadPoolExecutor.DiscardOldestPolicy()),
        DISCARD_POLICY(new ThreadPoolExecutor.DiscardPolicy());

        private RejectedExecutionHandler handler;

    }

}
