package com.mcoding.pangolin.server;

import com.google.common.eventbus.AsyncEventBus;
import com.mcoding.pangolin.common.PangolinEngine;
import com.mcoding.pangolin.common.util.PropertyUtils;
import com.mcoding.pangolin.server.container.ChannelManagerContainer;
import com.mcoding.pangolin.server.container.BaseChannelServerContainer;
import com.mcoding.pangolin.server.context.TrafficEventBus;
import com.mcoding.pangolin.server.traffic.TrafficListener;

import java.util.Objects;

/**
 * @author wzt on 2019/6/17.
 * @version 1.0
 */
public class ServerBootMain {

    public static void main(String[] args) {
        int defaultServerPort = PropertyUtils.getInt("default_server_port");
        if (Objects.nonNull(args) && args.length > 0) {
            defaultServerPort = Integer.valueOf(args[0]);
        }

        PangolinEngine.start(new ChannelManagerContainer(), new BaseChannelServerContainer(defaultServerPort));


        // 流量监控总线
        AsyncEventBus eventBus = TrafficEventBus.getInstance();
        eventBus.register(new TrafficListener());
    }

}
