package com.mcoding.pangolin.context;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import io.netty.channel.Channel;

/**
 * @author wzt on 2019/6/18.
 * @version 1.0
 */
public class TunnelContext {


    private static Table<String, Integer, Channel> tunnelTable = HashBasedTable.create();

    public static void put(String endPointPath, Integer proxyPort, Channel channel) {
        tunnelTable.put(endPointPath, proxyPort, channel);
    }

    public static Channel get(String endPointPath, Integer proxyPort) {
        return tunnelTable.get(endPointPath, proxyPort);
    }

    public static void printTunnelTable() {
        System.out.println(tunnelTable.toString());
    }
}
