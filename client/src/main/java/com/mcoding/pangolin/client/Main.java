package com.mcoding.pangolin.client;

import com.mcoding.pangolin.client.container.ClientContainer;

/**
 * @author wzt on 2019/6/17.
 * @version 1.0
 */
public class Main {

    private static String proxyServerHost = "127.0.0.1";
    private static Integer proxyServerPort = 7979;

    public static void main(String[] args) {
        new ClientContainer(proxyServerHost, proxyServerPort).start();
    }

}
