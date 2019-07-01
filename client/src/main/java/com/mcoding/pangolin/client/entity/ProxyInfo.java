package com.mcoding.pangolin.client.entity;

import lombok.Data;

/**
 * @author wzt on 2019/6/26.
 * @version 1.0
 */
@Data
public class ProxyInfo {

    private String realServerHost;
    private Integer realServerPort;

    private String privateKey;
    private String proxyServerHost;
    private Integer proxyServerPort;

}
