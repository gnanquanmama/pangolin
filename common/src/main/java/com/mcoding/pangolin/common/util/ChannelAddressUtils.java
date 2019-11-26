package com.mcoding.pangolin.common.util;

import com.mcoding.pangolin.common.constant.Constants;
import com.mcoding.pangolin.common.model.AddressInfo;
import io.netty.channel.Channel;

import java.net.InetSocketAddress;

/**
 * @author wzt on 2019/10/17.
 * @version 1.0
 */
public class ChannelAddressUtils {


    public static AddressInfo buildAddressInfo(Channel channel) {
        InetSocketAddress localAddress = (InetSocketAddress) channel.localAddress();
        InetSocketAddress remoteAddress = (InetSocketAddress) channel.remoteAddress();

        AddressInfo addressInfo = new AddressInfo();
        addressInfo.setLocalIp(localAddress.getAddress().getHostAddress());
        addressInfo.setLocalPort(localAddress.getPort());

        addressInfo.setRemoteIp(remoteAddress.getAddress().getHostAddress());
        addressInfo.setRemotePort(remoteAddress.getPort());

        String sessionId = channel.attr(Constants.SESSION_ID).get();
        String privateKey = channel.attr(Constants.PRIVATE_KEY).get();
        addressInfo.setSessionId(sessionId);
        addressInfo.setPrivateKey(privateKey);

        return addressInfo;
    }

}
