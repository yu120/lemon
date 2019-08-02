package cn.micro.lemon.common.support;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

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
