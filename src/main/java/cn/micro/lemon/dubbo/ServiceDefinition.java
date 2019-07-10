package cn.micro.lemon.dubbo;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

@Data
@ToString
public class ServiceDefinition implements Serializable {

    private String application;
    private String service;
    private String method;

    private String group;
    private String version;

    private String[] paramTypes;
    private Object[] paramValues;

}
