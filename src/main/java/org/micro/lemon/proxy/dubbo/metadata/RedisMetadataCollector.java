package org.micro.lemon.proxy.dubbo.metadata;

import org.apache.dubbo.metadata.MetadataConstants;
import org.apache.dubbo.metadata.report.identifier.KeyTypeEnum;
import org.apache.dubbo.metadata.report.identifier.MetadataIdentifier;
import org.micro.lemon.common.utils.URL;
import org.micro.lemon.proxy.dubbo.MetadataCollector;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.apache.dubbo.common.constants.CommonConstants;
import org.apache.dubbo.rpc.RpcException;
import org.micro.lemon.extension.Extension;
import redis.clients.jedis.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Redis Metadata Collector
 *
 * @author lry
 */
@Slf4j
@Extension("redis")
public class RedisMetadataCollector implements MetadataCollector {

    private Set<HostAndPort> clusterNodes;
    private JedisPool pool;
    private int timeout;
    private URL url;

    @Override
    public void initialize(URL url) {
        this.url = url;
        this.timeout = url.getParameter(CommonConstants.TIMEOUT_KEY, CommonConstants.DEFAULT_TIMEOUT);
        if (url.getParameter(CommonConstants.CLUSTER_KEY, false)) {
            this.clusterNodes = new HashSet<>();
            List<URL> urls = url.getBackupUrls();
            for (URL tmpUrl : urls) {
                clusterNodes.add(new HostAndPort(tmpUrl.getHost(), tmpUrl.getPort()));
            }
        } else {
            this.pool = new JedisPool(new JedisPoolConfig(), url.getHost(), url.getPort(), timeout, url.getPassword());
        }
    }

    @Override
    public String pullMetaData(MetadataIdentifier metadataIdentifier) {
        if (pool != null) {
            return getMetadataInStandAlone(metadataIdentifier);
        } else {
            return getMetadataInCluster(metadataIdentifier);
        }
    }

    /**
     * The get metadata in StandAlone by {@link MetadataIdentifier}
     *
     * @param metadataIdentifier {@link MetadataIdentifier}
     * @return metadata json
     */
    private String getMetadataInStandAlone(MetadataIdentifier metadataIdentifier) {
        try (Jedis jedis = pool.getResource()) {
            return jedis.get(metadataIdentifier.getUniqueKey(KeyTypeEnum.UNIQUE_KEY));
        } catch (Throwable e) {
            throw new RpcException("Failed to get " + metadataIdentifier + " to redis, cause: " + e.getMessage(), e);
        }
    }

    /**
     * The get metadata in Cluster by {@link MetadataIdentifier}
     *
     * @param metadataIdentifier {@link MetadataIdentifier}
     * @return metadata json
     */
    private String getMetadataInCluster(MetadataIdentifier metadataIdentifier) {
        try (JedisCluster jedisCluster = new JedisCluster(clusterNodes, timeout,
                timeout, 2, url.getPassword(), new GenericObjectPoolConfig())) {
            return jedisCluster.get(metadataIdentifier.getIdentifierKey() + MetadataConstants.META_DATA_STORE_TAG);
        } catch (Throwable e) {
            throw new RpcException("Failed to get " + metadataIdentifier + " to redis cluster, cause: " + e.getMessage(), e);
        }
    }

}
