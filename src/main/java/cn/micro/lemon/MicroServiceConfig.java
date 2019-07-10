package cn.micro.lemon;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

/**
 * Micro Service Config
 *
 * @author lry
 */
@Data
@ToString
public class MicroServiceConfig implements Serializable {

    private List<ServiceMapping> services;

}
