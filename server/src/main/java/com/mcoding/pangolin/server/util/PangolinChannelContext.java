package com.mcoding.pangolin.server.util;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.netty.channel.Channel;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;

/**
 * @author wzt on 2019/6/25.
 * @version 1.0
 */
public class PangolinChannelContext {

    private static Map<String, Channel> proxyServerChannel = Maps.newConcurrentMap();
    private static Map<String, Channel> userServerChannel = Maps.newConcurrentMap();


    public static void addProxyServerChannel(String privateKey, Channel channel) {
        proxyServerChannel.put(privateKey, channel);
    }

    public static void addUserServerChannel(String sessionId, Channel channel) {
        userServerChannel.put(sessionId, channel);
    }

    public static void closeProxyServerChannel(String privateKey) {
        Channel channel = proxyServerChannel.get(privateKey);
        if (Objects.nonNull(channel)) {
            channel.close();
            proxyServerChannel.remove(privateKey);
        }
    }

    public static void closeUserServerChannel(String sessionId) {
        System.out.println(sessionId);
        Channel channel = userServerChannel.get(sessionId);
        if (Objects.nonNull(channel)) {
            channel.close();
            userServerChannel.remove(sessionId);
        }
    }

    public static Channel getProxyServerChannel(String privateKey) {
        return proxyServerChannel.get(privateKey);
    }

    public static Channel getUserServerChannel(String sessionId) {
        return userServerChannel.get(sessionId);
    }

    public static Collection<Channel> getAllProxyServerChannel() {
        return proxyServerChannel.values();
    }

    public static Collection<Channel> getAllUserServerChannel() {
        return userServerChannel.values();
    }



}