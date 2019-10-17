package com.mcoding.pangolin.server.manager.func;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Maps;
import com.mcoding.pangolin.server.util.PangolinChannelContext;
import io.netty.channel.Channel;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author wzt on 2019/7/16.
 * @version 1.0
 */
public class GetOnlineChannelInfoFunc implements Function<Void, String> {

    @Override
    public String apply(Void aVoid) {
        String allProxyServerChannel = PangolinChannelContext.getAllIntranetProxyChannel()
                .stream()
                .map(Channel::toString)
                .collect(Collectors.joining(","));

        String allUserServerChannel = PangolinChannelContext.getAllPublicNetworkChannel()
                .stream()
                .map(Channel::toString)
                .collect(Collectors.joining(","));

        Map<String, String> resultMap = Maps.newHashMap();
        resultMap.put("proxyChannel", allProxyServerChannel);
        resultMap.put("userChannel", allUserServerChannel);
        return JSON.toJSONString(resultMap);
    }
}
