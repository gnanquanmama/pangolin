package com.mcoding.pangolin.server;

import com.mcoding.pangolin.common.PangolinEngine;
import com.mcoding.pangolin.common.util.PropertyUtils;
import com.mcoding.pangolin.server.container.ChannelManagerContainer;
import com.mcoding.pangolin.server.container.BaseChannelServerContainer;

import java.util.Objects;

/**
 * @author wzt on 2019/6/17.
 * @version 1.0
 */
public class ServerMain {

    public static void main(String[] args) {
        int defaultServerPort = PropertyUtils.getInt("default_server_port");
        if (Objects.nonNull(args) && args.length > 0) {
            defaultServerPort = Integer.valueOf(args[0]);
        }

        PangolinEngine.start(new ChannelManagerContainer(), new BaseChannelServerContainer(defaultServerPort));
    }

}
