package com.mcoding.pangolin.server;

import com.google.common.collect.Lists;
import com.google.common.eventbus.AsyncEventBus;
import com.mcoding.pangolin.common.LifeCycle;
import com.mcoding.pangolin.common.PangolinEngine;
import com.mcoding.pangolin.common.util.PropertyUtils;
import com.mcoding.pangolin.server.container.IntranetServerBootstrapContainer;
import com.mcoding.pangolin.server.container.DashboardServerBootstrapContainer;
import com.mcoding.pangolin.server.container.MonitorServerBootstrapContainer;
import com.mcoding.pangolin.server.container.PublicNetServerBootstrapContainer;
import com.mcoding.pangolin.server.context.TrafficEventBus;
import com.mcoding.pangolin.server.monitor.traffic.TrafficListener;

import java.util.List;
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

        List<LifeCycle> containerList = Lists.newLinkedList();
        containerList.add(PublicNetServerBootstrapContainer.INSTANCE);
        containerList.add(new IntranetServerBootstrapContainer(defaultServerPort));
        containerList.add(MonitorServerBootstrapContainer.INSTANCE);
        containerList.add(DashboardServerBootstrapContainer.INSTANCE);
        PangolinEngine.start(containerList);

        // 流量监控总线
        AsyncEventBus eventBus = TrafficEventBus.getInstance();
        eventBus.register(new TrafficListener());
    }

}
