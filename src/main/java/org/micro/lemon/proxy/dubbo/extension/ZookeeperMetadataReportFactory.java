package org.micro.lemon.proxy.dubbo.extension;

import org.apache.dubbo.common.URL;
import org.apache.dubbo.metadata.report.MetadataReport;
import org.apache.dubbo.metadata.report.support.AbstractMetadataReportFactory;
import org.apache.dubbo.remoting.zookeeper.ZookeeperTransporter;

/**
 * Zookeeper Metadata Report Factory
 *
 * @author lry
 */
public class ZookeeperMetadataReportFactory extends AbstractMetadataReportFactory {

    private ZookeeperTransporter zookeeperTransporter;

    public void setZookeeperTransporter(ZookeeperTransporter zookeeperTransporter) {
        this.zookeeperTransporter = zookeeperTransporter;
    }

    @Override
    public MetadataReport createMetadataReport(URL url) {
        return new ZookeeperMetadataReport(url, zookeeperTransporter);
    }

}
