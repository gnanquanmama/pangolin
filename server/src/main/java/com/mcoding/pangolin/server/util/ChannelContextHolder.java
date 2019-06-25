package com.mcoding.pangolin.server.util;

import com.google.common.collect.Maps;
import io.netty.channel.Channel;

import java.util.Map;
import java.util.Objects;

/**
 * @author wzt on 2019/6/25.
 * @version 1.0
 */
public class ChannelContextHolder {

    private static Map<String, Channel> proxyServerChannel = Maps.newConcurrentMap();
    private static Map<String, Channel> userServerChannel = Maps.newConcurrentMap();


    public static void addProxyServerChannel(String userId, Channel channel) {
        proxyServerChannel.put(userId, channel);
    }

    public static void addUserServerChannel(String userId, Channel channel) {
        userServerChannel.put(userId, channel);
    }

    public static void closeProxyServerChannel(String userId) {
        Channel channel = proxyServerChannel.get(userId);
        if (Objects.nonNull(channel)) {
            channel.close();
            proxyServerChannel.remove(userId);
        }
    }

    public static void closeUserServerChannel(String userId) {
        Channel channel = userServerChannel.get(userId);
        if (Objects.nonNull(channel)) {
            channel.close();
            userServerChannel.remove(userId);
        }
    }

    public static Channel getProxyServerChannel(String userId) {
        return proxyServerChannel.get(userId);
    }

    public static Channel getUserServerChannel(String userId) {
        return userServerChannel.get(userId);
    }



}
