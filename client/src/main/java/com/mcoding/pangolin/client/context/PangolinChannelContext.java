package com.mcoding.pangolin.client.context;

import com.google.common.collect.Maps;
import io.netty.channel.Channel;

import java.util.Map;

/**
 * 管道上下文
 *
 * @author wzt on 2019/6/25.
 * @version 1.0
 */
public class PangolinChannelContext {

    private static Channel intranetProxyChannel = null;

    private static Map<String, Channel> targetServerChannelMap = Maps.newConcurrentMap();

    public static void bindIntranetProxyChannel(Channel channel) {
        intranetProxyChannel = channel;
    }

    public static Channel getIntranetProxyChannel() {
        return intranetProxyChannel;
    }

    public static void bindTargetServerChannel(String sessionId, Channel channel) {
        targetServerChannelMap.put(sessionId, channel);
    }

    public static Channel getTargetChannel(String sessionId) {
        return targetServerChannelMap.get(sessionId);
    }


    public static Map<String, Channel> getAllChannelList() {
        return targetServerChannelMap;
    }

    public static void unBindTargetServerChannel(String sessionId) {
        Channel channel = targetServerChannelMap.get(sessionId);
        if (channel != null) {
            channel.close();
        }

        targetServerChannelMap.remove(sessionId);
    }

    public static void unBindAll() {
        targetServerChannelMap.forEach((channelFlag, channel) -> {
            channel.close();
        });

        clearAll();
    }

    private static void clearAll() {
        targetServerChannelMap.clear();
    }

}
