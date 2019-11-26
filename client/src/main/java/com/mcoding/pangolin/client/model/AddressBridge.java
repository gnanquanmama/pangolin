package com.mcoding.pangolin.client.model;

import lombok.Data;

/**
 * @author wzt on 2019/6/26.
 * @version 1.0
 */
@Data
public class AddressBridge {

    private String privateKey;

    private String targetServerHost;
    private Integer targetServerPort;

    private String intranetProxyServerHost;
    private Integer intranetProxyServerPort;

}
