package cn.micro.lemon;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

/**
 * Service Mapping
 *
 * @author lry
 */
@Data
@ToString
public class ServiceMapping implements Serializable {

    private String name;
    private String serviceId;
    private String group;
    private String version;

}
