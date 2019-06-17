package com.mcoding.pangolin.context;

import io.netty.channel.Channel;

import java.util.HashMap;
import java.util.Map;

/**
 * @author wzt on 2019/6/17.
 * @version 1.0
 */
public class BaseChannelContext {

    private static Map<Integer, Channel> channelMap = new HashMap<>();


    public static void put(Integer key, Channel channel) {
        channelMap.put(key, channel);
    }

    public static Channel get(Integer key) {
        return channelMap.get(key);
    }

}
