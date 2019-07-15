package cn.micro.lemon;

import cn.micro.lemon.provider.DemoService;
import cn.micro.lemon.provider.DemoServiceImpl;
import org.apache.dubbo.config.*;

public class ProviderDemo {

    public static void main(String[] args) throws Exception {
        // Implementation
        DemoService demoService = new DemoServiceImpl();

        // Application Info
        ApplicationConfig application = new ApplicationConfig();
        application.setName("micro-dubbo-provider");

        // Registry Info
        RegistryConfig registry = new RegistryConfig();
        registry.setAddress("zookeeper://127.0.0.1:2181");

        // Metadata Report Info
        MetadataReportConfig metadataReportConfig = new MetadataReportConfig();
        metadataReportConfig.setAddress("zookeeper://127.0.0.1:2181");

        // Protocol
        ProtocolConfig protocol = new ProtocolConfig();
        protocol.setName("dubbo");
        protocol.setPort(12345);
        protocol.setThreads(200);

        // Exporting: In case of memory leak, please cache.
        ServiceConfig<DemoService> service = new ServiceConfig<>();
        service.setApplication(application);
        // Use setRegistries() for multi-registry case
        service.setRegistry(registry);
        // Use setProtocols() for multi-protocol case
        service.setProtocol(protocol);
        service.setMetadataReportConfig(metadataReportConfig);
        service.setInterface(DemoService.class);
        service.setRef(demoService);

        // Local export and register
        service.export();
        System.in.read();
    }

}
