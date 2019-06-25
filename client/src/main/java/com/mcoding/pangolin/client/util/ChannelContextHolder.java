package com.mcoding.pangolin.client.util;

import com.google.common.collect.Maps;
import io.netty.channel.Channel;

import java.util.Map;

/**
 * @author wzt on 2019/6/25.
 * @version 1.0
 */
public class ChannelContextHolder {

    private static String PROXY_CHANNEL = "proxy_channel";
    private static String USER_CHANNEL = "user_channel";

    private static Map<String, Channel> channelMap = Maps.newConcurrentMap();

    public static void addProxyChannel(Channel channel) {
        channelMap.put(PROXY_CHANNEL, channel);
    }

    public static Channel getProxyChannel() {
        return channelMap.get(PROXY_CHANNEL);
    }

    public static void addUserChannel(Channel channel) {
        channelMap.put(USER_CHANNEL, channel);
    }

    public static Channel getUserChannel() {
        return channelMap.get(USER_CHANNEL);
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
