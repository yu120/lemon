package cn.micro.lemon.common.config;

import cn.micro.lemon.common.support.RejectedStrategy;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

/**
 * Biz Task Config
 *
 * @author lry
 */
@Data
@ToString
public class BizTaskConfig implements Serializable {

    /**
     * The biz core thread number
     */
    private int coreThread = 20;
    /**
     * The biz max thread number
     */
    private int maxThread = 200;
    /**
     * The biz queue capacity
     */
    private int queueCapacity = 800;
    /**
     * The biz keep alive time(ms)
     */
    private long keepAliveTime = 60000L;
    /**
     * The biz thread rejected strategy
     */
    private RejectedStrategy rejectedStrategy = RejectedStrategy.ABORT_POLICY;

}
