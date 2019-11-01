package com.mcoding.pangolin.common.codec;

import lombok.Data;

/**
 * @author wzt on 2019/10/31.
 * @version 1.0
 */
@Data
public class Packet implements Cloneable {

    private String privateKey;
    private String sessionId;
    private byte[] data;
    private byte type;

}
