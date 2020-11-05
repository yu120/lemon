package org.micro.lemon.proxy.dubbo.extension;

import org.apache.dubbo.common.URL;
import org.apache.dubbo.metadata.report.MetadataReport;
import org.apache.dubbo.metadata.report.support.AbstractMetadataReportFactory;

/**
 * Redis Metadata Report Factory
 *
 * @author lry
 */
public class RedisMetadataReportFactory extends AbstractMetadataReportFactory {

    @Override
    public MetadataReport createMetadataReport(URL url) {
        return new RedisMetadataReport(url);
    }

}
