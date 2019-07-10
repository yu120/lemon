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

    private String category;
    private String application;
    private String service;
    private String group;
    private String version;

}
