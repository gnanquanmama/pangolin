package com.mcoding.pangolin.client.entity;

import lombok.Data;

/**
 * @author wzt on 2019/6/26.
 * @version 1.0
 */
@Data
public class AddressBridgeInfo {

    private String privateKey;

    private String targetServerHost;
    private Integer targetServerPort;

    private String intranetProxyServerHost;
    private Integer intranetProxyServerPort;

}
