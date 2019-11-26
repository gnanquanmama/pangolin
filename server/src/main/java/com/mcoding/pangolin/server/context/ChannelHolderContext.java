package com.mcoding.pangolin.server.context;

import com.google.common.collect.Maps;
import com.mcoding.pangolin.common.constant.Constants;
import io.netty.channel.Channel;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;

/**
 * 通道管理上下文
 *
 * @author wzt on 2019/6/25.
 * @version 1.0
 */
public class ChannelHolderContext {

    private static Map<String, Channel> intranetProxyChannelMap = Maps.newConcurrentMap();
    private static Map<String, Channel> publicNetworkChannelMap = Maps.newConcurrentMap();


    public static void markAsLogin(Channel channel) {
        String privateKey = channel.attr(Constants.PRIVATE_KEY).get();
        intranetProxyChannelMap.put(privateKey, channel);
    }

    public static boolean hasLogin(Channel channel) {
        String privateKey = channel.attr(Constants.PRIVATE_KEY).get();
        return StringUtils.isBlank(privateKey);
    }

    /**
     * 绑定公网通道
     *
     * @param sessionId
     * @param channel
     */
    public static void bindPublicNetworkChannel(String sessionId, Channel channel) {
        publicNetworkChannelMap.put(sessionId, channel);
    }

    public static void unBindIntranetProxyChannel(String privateKey) {
        Channel channel = intranetProxyChannelMap.get(privateKey);
        if (Objects.nonNull(channel)) {
            channel.close();
            intranetProxyChannelMap.remove(privateKey);
        }
    }

    public static void unBindPublicNetworkChannel(String sessionId) {
        Channel channel = publicNetworkChannelMap.get(sessionId);
        if (Objects.nonNull(channel)) {
            channel.close();
            publicNetworkChannelMap.remove(sessionId);
        }
    }

    /**
     * 获取内网代理通道
     *
     * @param privateKey
     * @return
     */
    public static Channel getIntranetProxyServerChannel(String privateKey) {
        return intranetProxyChannelMap.get(privateKey);
    }

    public static Channel getPublicNetworkChannel(String sessionId) {
        return publicNetworkChannelMap.get(sessionId);
    }

    public static Collection<Channel> getAllIntranetProxyChannel() {
        return intranetProxyChannelMap.values();
    }

    public static Collection<Channel> getAllPublicNetworkChannel() {
        return publicNetworkChannelMap.values();
    }


}