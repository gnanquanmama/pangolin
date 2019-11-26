package com.mcoding.pangolin.server.handler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.google.common.collect.Lists;
import com.mcoding.pangolin.common.codec.ChainTracePacket;
import com.mcoding.pangolin.common.model.AddressInfo;
import com.mcoding.pangolin.common.util.ChannelAddressUtils;
import com.mcoding.pangolin.server.context.ChannelHolderContext;
import com.mcoding.pangolin.server.context.NetworkChainTraceTable;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.internal.StringUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * 链路追踪处理器
 *
 * @author wzt on 2019/10/31.
 * @version 1.0
 */
@Slf4j
@ChannelHandler.Sharable
public class ChainTracingResponseHandler extends SimpleChannelInboundHandler<ChainTracePacket> {

    public static final ChainTracingResponseHandler INSTANCE = new ChainTracingResponseHandler();

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ChainTracePacket packet) {
        String data = new String(packet.getData());
        log.info("EVENT=RECEIVE TRACING MESSAGE|MSG={}", data);

        List<AddressInfo> clientAddressList = JSON.parseObject(data, new TypeReference<List<AddressInfo>>() {
        });

        AddressInfo clientProxyAddress = null;
        AddressInfo clientTargetAddress = null;

        for (AddressInfo addressInfo : clientAddressList) {
            if (StringUtil.isNullOrEmpty(addressInfo.getSessionId())) {
                clientProxyAddress = addressInfo;
            } else {
                clientTargetAddress = addressInfo;
            }
        }

        String privateKey = clientProxyAddress.getPrivateKey();
        String sessionId = clientTargetAddress.getSessionId();

        Channel intranetProxyChannel = ChannelHolderContext.getIntranetProxyServerChannel(privateKey);
        Channel publicNetworkChannel = ChannelHolderContext.getPublicNetworkChannel(sessionId);

        AddressInfo serverIntranetProxyChannel = ChannelAddressUtils.buildAddressInfo(intranetProxyChannel);
        AddressInfo serverPublicNetworkChannel = ChannelAddressUtils.buildAddressInfo(publicNetworkChannel);

        List<AddressInfo> requestAddressList = Lists.newArrayList();
        requestAddressList.add(serverPublicNetworkChannel);
        requestAddressList.add(serverIntranetProxyChannel);
        requestAddressList.add(clientProxyAddress);
        requestAddressList.add(clientTargetAddress);

        NetworkChainTraceTable.add(sessionId, requestAddressList);
    }

}
