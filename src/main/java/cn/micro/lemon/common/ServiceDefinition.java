package cn.micro.lemon.common;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;


/**
 * Service Definition
 *
 * @author lry
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class ServiceDefinition extends ServiceMapping {

    /**
     * The method name
     */
    private String method;
    /**
     * The parameter type list
     */
    private String[] paramTypes;
    /**
     * The parameter value list
     */
    private Object[] paramValues;

}
