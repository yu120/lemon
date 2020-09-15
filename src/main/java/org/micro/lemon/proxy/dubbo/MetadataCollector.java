package org.micro.lemon.proxy.dubbo;

import org.apache.dubbo.common.extension.SPI;
import org.apache.dubbo.metadata.identifier.MetadataIdentifier;
import org.micro.lemon.common.utils.URL;

/**
 * Metadata Collector
 *
 * @author lry
 */
@SPI("zookeeper")
public interface MetadataCollector {

    /**
     * The initialize
     *
     * @param url {@link URL}
     */
    void initialize(URL url);

    /**
     * The pull metaData
     *
     * @param metadataIdentifier {@link MetadataIdentifier}
     * @return meta data
     */
    String pullMetaData(MetadataIdentifier metadataIdentifier);

}
