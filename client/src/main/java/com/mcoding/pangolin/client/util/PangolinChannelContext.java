package com.mcoding.pangolin.client.util;

import com.google.common.collect.Maps;
import io.netty.channel.Channel;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author wzt on 2019/6/25.
 * @version 1.0
 */
public class PangolinChannelContext {

    private static Channel proxyChannel = null;

    private static Map<String, Channel> channelMap = Maps.newConcurrentMap();

    public static void addProxyChannel(Channel channel) {
        proxyChannel = channel;
    }

    public static Channel getProxyChannel() {
        return proxyChannel;
    }

    public static void addUserChannel(String sessionId, Channel channel) {
        channelMap.put(sessionId, channel);
    }

    public static Channel getUserChannel(String sessionId) {
        return channelMap.get(sessionId);
    }


    public static Map<String, Channel> getAllChannelList() {
        return channelMap;
    }

    public static void closeUserChannel(String sessionId) {
        Channel channel = channelMap.get(sessionId);
        if (channel != null) {
            channel.close();
        }

        channelMap.remove(sessionId);
    }

    public static void closeAll() {
        channelMap.forEach((channelFlag, channel) -> {
            channel.close();
        });

        clearAll();
    }

    private static void clearAll() {
        channelMap.clear();
    }

}
