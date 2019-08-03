package org.micro.lemon.proxy.dubbo.extension;

import org.apache.dubbo.common.URL;
import org.apache.dubbo.metadata.identifier.MetadataIdentifier;
import org.apache.dubbo.metadata.support.AbstractMetadataReport;

/**
 * Abstract Extension Metadata Report
 *
 * @author lry
 */
public abstract class AbstractExtensionMetadataReport extends AbstractMetadataReport {

    public AbstractExtensionMetadataReport(URL reportServerURL) {
        super(reportServerURL);
    }

    protected String wrapperStoreProviderMetadata(MetadataIdentifier providerMetadataIdentifier, String serviceDefinitions) {
        return serviceDefinitions;
    }

    protected String wrapperStoreConsumerMetadata(MetadataIdentifier consumerMetadataIdentifier, String serviceParameterString) {
        return serviceParameterString;
    }

}
