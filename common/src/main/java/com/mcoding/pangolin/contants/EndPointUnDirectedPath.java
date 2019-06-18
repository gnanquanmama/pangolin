package com.mcoding.pangolin.contants;

/**
 * 终端无向路径标识常量
 *
 * @author wzt on 2019/6/18.
 * @version 1.0
 */
public interface EndPointUnDirectedPath {

    /**
     * 应用端 -> 代理服务端
     */
    String APP_TO_PROXY_SERVER = "APP_TO_PROXY_SERVER";

    /**
     * 代理服务端 -> 基础服务端
     */
    String PROXY_SERVER_TO_BASE_SERVER = "PROXY_SERVER_TO_BASE_SERVER";

    /**
     * 基础服务端 -> 基础客户端
     */
    String BASE_SERVER_TO_BASE_CLIENT = "BASE_SERVER_TO_BASE_CLIENT";
    /**
     * 基础客户端 -> 真实服务端
     */
    String BASE_CLIENT_TO_REAL_SERVER = "BASE_CLIENT_TO_REAL_SERVER";

}
