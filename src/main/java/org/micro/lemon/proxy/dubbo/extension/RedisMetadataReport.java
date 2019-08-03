package org.micro.lemon.proxy.dubbo.extension;

import lombok.ToString;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.apache.dubbo.common.URL;
import org.apache.dubbo.common.logger.Logger;
import org.apache.dubbo.common.logger.LoggerFactory;
import org.apache.dubbo.metadata.identifier.MetadataIdentifier;
import org.apache.dubbo.metadata.support.AbstractMetadataReport;
import org.apache.dubbo.rpc.RpcException;
import redis.clients.jedis.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.apache.dubbo.common.constants.CommonConstants.*;
import static org.apache.dubbo.metadata.identifier.MetadataIdentifier.META_DATA_STORE_TAG;

/**
 * Redis Metadata Report
 *
 * @author lry
 */
@ToString
public class RedisMetadataReport extends AbstractMetadataReport {

    private final static Logger logger = LoggerFactory.getLogger(RedisMetadataReport.class);

    private JedisPool pool;
    private Set<HostAndPort> jedisClusterNodes;
    private int timeout;
    private String password;

    public RedisMetadataReport(URL url) {
        super(url);
        timeout = url.getParameter(TIMEOUT_KEY, DEFAULT_TIMEOUT);
        if (url.getParameter(CLUSTER_KEY, false)) {
            jedisClusterNodes = new HashSet<>();
            List<URL> urls = url.getBackupUrls();
            for (URL tmpUrl : urls) {
                jedisClusterNodes.add(new HostAndPort(tmpUrl.getHost(), tmpUrl.getPort()));
            }
        } else {
            pool = new JedisPool(new JedisPoolConfig(), url.getHost(), url.getPort(), timeout, url.getPassword());
        }
    }

    @Override
    protected void doStoreProviderMetadata(MetadataIdentifier providerMetadataIdentifier, String serviceDefinitions) {
        this.storeMetadata(providerMetadataIdentifier, serviceDefinitions);
    }

    @Override
    protected void doStoreConsumerMetadata(MetadataIdentifier consumerMetadataIdentifier, String value) {
        this.storeMetadata(consumerMetadataIdentifier, value);
    }

    private void storeMetadata(MetadataIdentifier metadataIdentifier, String v) {
        if (pool != null) {
            storeMetadataStandalone(metadataIdentifier, v);
        } else {
            storeMetadataInCluster(metadataIdentifier, v);
        }
    }

    private void storeMetadataInCluster(MetadataIdentifier metadataIdentifier, String v) {
        try (JedisCluster jedisCluster = new JedisCluster(jedisClusterNodes, timeout, timeout, 2, password, new GenericObjectPoolConfig())) {
            jedisCluster.set(metadataIdentifier.getIdentifierKey() + META_DATA_STORE_TAG, v);
        } catch (Throwable e) {
            logger.error("Failed to put " + metadataIdentifier + " to redis cluster " + v + ", cause: " + e.getMessage(), e);
            throw new RpcException("Failed to put " + metadataIdentifier + " to redis cluster " + v + ", cause: " + e.getMessage(), e);
        }
    }

    private void storeMetadataStandalone(MetadataIdentifier metadataIdentifier, String v) {
        try (Jedis jedis = pool.getResource()) {
            jedis.set(metadataIdentifier.getUniqueKey(MetadataIdentifier.KeyTypeEnum.UNIQUE_KEY), v);
        } catch (Throwable e) {
            logger.error("Failed to put " + metadataIdentifier + " to redis " + v + ", cause: " + e.getMessage(), e);
            throw new RpcException("Failed to put " + metadataIdentifier + " to redis " + v + ", cause: " + e.getMessage(), e);
        }
    }

}
