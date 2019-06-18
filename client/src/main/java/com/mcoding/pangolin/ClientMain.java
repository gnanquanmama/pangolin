package com.mcoding.pangolin;

import com.mcoding.pangolin.task.ConnectBaseServerTask;

/**
 * @author wzt on 2019/6/17.
 * @version 1.0
 */
public class ClientMain {

    public static void main(String[] args)  throws Exception {

        String key = "key";
        String host = "127.0.0.1";
        int port = 8007;
        int proxyPort = 18888;

        String realServerHost =  "127.0.0.1";
        Integer realServerPort = 9999;

        new ConnectBaseServerTask(key, host, port, proxyPort, realServerHost, realServerPort).run();
    }

}
