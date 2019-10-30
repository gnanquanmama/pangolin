package com.mcoding.pangolin.server.manager.context;

import com.google.common.collect.Maps;
import com.mcoding.pangolin.common.constant.Constants;
import io.netty.channel.Channel;

import java.util.Map;

/**
 * @author wzt on 2019/10/30.
 * @version 1.0
 */
public class ManagerChannelContext {


    private static Map<String, Channel> channelMap = Maps.newConcurrentMap();


    public static void markAsLogin(Channel channel) {
        String sessionId = channel.attr(Constants.SESSION_ID).get();
        channelMap.put(sessionId, channel);
    }

    public static void unLogin(String sessionId) {
        channelMap.remove(sessionId);
    }

}
