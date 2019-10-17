package com.mcoding.pangolin.common.entity;

import lombok.Data;

/**
 * @author wzt on 2019/10/17.
 * @version 1.0
 */
@Data
public class AddressInfo {
    String sessionId;
    String privateKey;

    String localIp;
    int localPort;

    String remoteIp;
    int remotePort;
}