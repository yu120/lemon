package cn.micro.lemon.dubbo.metadata;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.apache.dubbo.common.URL;
import org.apache.dubbo.common.constants.CommonConstants;
import org.apache.dubbo.metadata.identifier.MetadataIdentifier;
import org.apache.dubbo.rpc.RpcException;
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
public class RedisMetadataCollector implements MetadataCollector {

    private JedisPool pool;
    private Set<HostAndPort> clusterNodes;
    private int timeout;
    private URL url;

    private final static String DEFAULT_ROOT = "dubbo";

    @Override
    public void initialize(URL url) {
        this.url = url;
        this.timeout = url.getParameter(CommonConstants.TIMEOUT_KEY, CommonConstants.DEFAULT_TIMEOUT);
        if (url.getParameter(CommonConstants.CLUSTER_KEY, false)) {
            this.clusterNodes = new HashSet<HostAndPort>();
            List<URL> urls = url.getBackupUrls();
            for (URL tmpUrl : urls) {
                clusterNodes.add(new HostAndPort(tmpUrl.getHost(), tmpUrl.getPort()));
            }
        } else {
            this.pool = new JedisPool(new JedisPoolConfig(), url.getHost(), url.getPort(), timeout, url.getPassword());
        }
    }

    @Override
    public String getProviderMetaData(MetadataIdentifier metadataIdentifier) {
        if (pool != null) {
            return getMetadataStandalone(metadataIdentifier);
        } else {
            return getMetadataInCluster(metadataIdentifier);
        }
    }

    private String getMetadataInCluster(MetadataIdentifier metadataIdentifier) {
        try (JedisCluster jedisCluster = new JedisCluster(clusterNodes, timeout,
                timeout, 2, url.getPassword(), new GenericObjectPoolConfig())) {
            return jedisCluster.get(metadataIdentifier.getIdentifierKey() + MetadataIdentifier.META_DATA_STORE_TAG);
        } catch (Throwable e) {
            throw new RpcException("Failed to get " + metadataIdentifier + " to redis cluster, cause: " + e.getMessage(), e);
        }
    }

    private String getMetadataStandalone(MetadataIdentifier metadataIdentifier) {
        try (Jedis jedis = pool.getResource()) {
            return jedis.get(metadataIdentifier.getUniqueKey(MetadataIdentifier.KeyTypeEnum.UNIQUE_KEY));
        } catch (Throwable e) {
            throw new RpcException("Failed to get " + metadataIdentifier + " to redis, cause: " + e.getMessage(), e);
        }
    }

}
