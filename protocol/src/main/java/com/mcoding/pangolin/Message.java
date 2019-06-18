package com.mcoding.pangolin;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * @author wzt on 2019/6/17.
 * @version 1.0
 */
@Data
public class Message implements Serializable {

    public static final Integer CONNECTING = 1;
    public static final Integer TRANSFER = 2;

    private String key;
    private Integer proxyPort;
    private Integer type;
    private byte[] data;

}
