package com.mcoding.pangolin.server.codec.packet;

import lombok.Data;

/**
 * @author wzt on 2019/10/31.
 * @version 1.0
 */
@Data
public class Packet {

    private String privateKey;
    private String  sessionId;
    private byte[] data;
    private byte type;

}
