package com.mcoding.pangolin.server;

import com.mcoding.pangolin.server.container.ProxyServerContainer;

/**
 * @author wzt on 2019/6/17.
 * @version 1.0
 */
public class ServerMain {

    public static void main(String[] args) {
        new ProxyServerContainer( 7979).start();
    }

}
