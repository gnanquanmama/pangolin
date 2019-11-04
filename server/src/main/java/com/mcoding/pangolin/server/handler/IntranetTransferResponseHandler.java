package com.mcoding.pangolin.server.handler;

import com.mcoding.pangolin.common.codec.TransferPacket;
import com.mcoding.pangolin.common.constant.Constants;
import com.mcoding.pangolin.server.context.PangolinChannelContext;
import com.mcoding.pangolin.server.context.TrafficEventBus;
import com.mcoding.pangolin.server.traffic.TrafficEvent;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * 数据传输处理器
 *
 * @author wzt on 2019/10/31.
 * @version 1.0
 */
@Slf4j
@ChannelHandler.Sharable
public class IntranetTransferResponseHandler extends SimpleChannelInboundHandler<TransferPacket> {

    public static final IntranetTransferResponseHandler INSTANCE = new IntranetTransferResponseHandler();

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TransferPacket packet) {
        Channel userChannel = PangolinChannelContext.getPublicNetworkChannel(packet.getSessionId());
        userChannel.writeAndFlush(Unpooled.wrappedBuffer(packet.getData()));

        ctx.channel().eventLoop().execute(() -> {
            // 记录流入流量字节数量
            TrafficEvent trafficEvent = TrafficEvent.INSTANCE.clone();
            String userPrivateKey = ctx.channel().attr(Constants.PRIVATE_KEY).get();
            trafficEvent.setUserPrivateKye(userPrivateKey);
            trafficEvent.setInFlow(0);
            trafficEvent.setOutFlow(packet.getData().length);
            TrafficEventBus.getInstance().post(trafficEvent);
        });
    }

}
