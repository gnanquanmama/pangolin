package com.mcoding.pangolin.server.dashboard.model;

import lombok.Data;


/**
 * @author wzt on 2019/11/27.
 * @version 1.0
 */
@Data
public class BindTempProxyPortModel {

    private String privateKey;

    private String proxyPort;

    private String authCode;

}
