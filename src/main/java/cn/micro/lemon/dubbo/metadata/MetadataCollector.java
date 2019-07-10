package cn.micro.lemon.dubbo.metadata;

import org.apache.dubbo.common.URL;
import org.apache.dubbo.common.extension.SPI;
import org.apache.dubbo.metadata.identifier.MetadataIdentifier;

@SPI("zookeeper")
public interface MetadataCollector {

    void initialize(URL url);

    String getProviderMetaData(MetadataIdentifier identifier);

}
