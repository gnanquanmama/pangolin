package com.mcoding.pangolin.server.monitor.func;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Maps;
import com.mcoding.pangolin.common.model.AddressInfo;
import com.mcoding.pangolin.common.util.ChannelAddressUtils;
import com.mcoding.pangolin.server.context.ChannelHolderContext;

import java.util.List;
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

        List<AddressInfo> allPublicNetworkAddressInfoList = ChannelHolderContext.getAllPublicNetworkChannel()
                .stream()
                .map(ChannelAddressUtils::buildAddressInfo)
                .collect(Collectors.toList());

        List<AddressInfo> allIntranetProxyServerChannel = ChannelHolderContext.getAllIntranetProxyChannel()
                .stream()
                .map(ChannelAddressUtils::buildAddressInfo)
                .collect(Collectors.toList());

        Map<String, List<AddressInfo>> resultMap = Maps.newHashMap();
        resultMap.put("allIntranetProxyServerChannel", allIntranetProxyServerChannel);
        resultMap.put("allPublicServerChannel", allPublicNetworkAddressInfoList);

        return JSON.toJSONString(resultMap);
    }







}
