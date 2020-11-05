package org.micro.lemon.proxy.dubbo.extension;

import lombok.ToString;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.apache.dubbo.common.URL;
import org.apache.dubbo.common.logger.Logger;
import org.apache.dubbo.common.logger.LoggerFactory;
import org.apache.dubbo.metadata.MetadataConstants;
import org.apache.dubbo.metadata.report.identifier.KeyTypeEnum;
import org.apache.dubbo.metadata.report.identifier.MetadataIdentifier;
import org.apache.dubbo.metadata.report.identifier.ServiceMetadataIdentifier;
import org.apache.dubbo.metadata.report.identifier.SubscriberMetadataIdentifier;
import org.apache.dubbo.rpc.RpcException;
import redis.clients.jedis.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.apache.dubbo.common.constants.CommonConstants.*;

/**
 * Redis Metadata Report
 *
 * @author lry
 */
@ToString
public class RedisMetadataReport extends AbstractExtensionMetadataReport {

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

    @Override
    protected void doSaveMetadata(ServiceMetadataIdentifier metadataIdentifier, URL url) {

    }

    @Override
    protected void doRemoveMetadata(ServiceMetadataIdentifier metadataIdentifier) {

    }

    @Override
    protected List<String> doGetExportedURLs(ServiceMetadataIdentifier metadataIdentifier) {
        return null;
    }

    @Override
    protected void doSaveSubscriberData(SubscriberMetadataIdentifier subscriberMetadataIdentifier, String urlListStr) {

    }

    @Override
    protected String doGetSubscribedURLs(SubscriberMetadataIdentifier subscriberMetadataIdentifier) {
        return null;
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
            jedisCluster.set(metadataIdentifier.getIdentifierKey() + MetadataConstants.META_DATA_STORE_TAG, v);
        } catch (Throwable e) {
            logger.error("Failed to put " + metadataIdentifier + " to redis cluster " + v + ", cause: " + e.getMessage(), e);
            throw new RpcException("Failed to put " + metadataIdentifier + " to redis cluster " + v + ", cause: " + e.getMessage(), e);
        }
    }

    private void storeMetadataStandalone(MetadataIdentifier metadataIdentifier, String v) {
        try (Jedis jedis = pool.getResource()) {
            jedis.set(metadataIdentifier.getUniqueKey(KeyTypeEnum.UNIQUE_KEY), v);
        } catch (Throwable e) {
            logger.error("Failed to put " + metadataIdentifier + " to redis " + v + ", cause: " + e.getMessage(), e);
            throw new RpcException("Failed to put " + metadataIdentifier + " to redis " + v + ", cause: " + e.getMessage(), e);
        }
    }

    @Override
    public String getServiceDefinition(MetadataIdentifier metadataIdentifier) {
        return null;
    }
}
