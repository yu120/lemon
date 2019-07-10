package cn.micro.lemon.dubbo;

import cn.micro.lemon.ServiceMapping;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

/**
 * Service Mapping
 *
 * @author lry
 */
@Data
@ToString
public class MicroConfig implements Serializable {

    private String registryAddress;
    private String metadataAddress;
    private List<ServiceMapping> services;

}
