package cn.micro.lemon.dubbo;

import org.apache.dubbo.metadata.identifier.MetadataIdentifier;
import org.micro.neural.common.URL;
import org.micro.neural.extension.SPI;

/**
 * Metadata Collector
 *
 * @author lry
 */
@SPI("zookeeper")
public interface MetadataCollector {

    void initialize(URL url);

    String pullMetaData(MetadataIdentifier metadataIdentifier);

}
