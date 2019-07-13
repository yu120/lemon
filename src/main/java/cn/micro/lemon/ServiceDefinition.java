package cn.micro.lemon;

import lombok.Data;
import lombok.ToString;


/**
 * Service Definition
 *
 * @author lry
 */
@Data
@ToString
public class ServiceDefinition extends ServiceMapping {

    private String method;
    private String[] paramTypes;
    private Object[] paramValues;

}
