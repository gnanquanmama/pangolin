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

    private String privateKey;
    private String sessionId;
    private byte type;
    private byte[] data;

}
