package com.mcoding.pangolin.server.manager.func;

import com.mcoding.pangolin.server.util.PangolinChannelContext;
import io.netty.channel.Channel;

import java.util.function.Function;

/**
 * @author wzt on 2019/7/16.
 * @version 1.0
 */
public class CloseInactiveChannelFunc implements Function<Void, String> {

    @Override
    public String apply(Void aVoid) {
        PangolinChannelContext.getAllUserServerChannel().stream()
                .filter(channel -> !channel.isActive())
                .forEach(Channel::close);

        PangolinChannelContext.getAllProxyServerChannel().stream()
                .filter(channel -> !channel.isActive())
                .forEach(Channel::close);

        return "close success";
    }
}
