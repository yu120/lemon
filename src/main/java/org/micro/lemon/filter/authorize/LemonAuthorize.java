package org.micro.lemon.filter.authorize;

import org.micro.neural.config.store.RedisStore;

/**
 * Lemon Authorize
 *
 * @author lry
 */
public enum LemonAuthorize {

    // ===

    INSTANCE;

    private RedisStore redisStore = RedisStore.INSTANCE;

    /**
     * 上架服务(对外暴露服务)
     */
    public void upperShelf() {
        // redisStore.putAllMap();
    }

}
