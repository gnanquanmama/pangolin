package com.mcoding.pangolin.server.context;

import com.google.common.eventbus.AsyncEventBus;

import java.util.concurrent.Executors;

public class FlowEventBusSingleton {


    private static AsyncEventBus eventBus = new AsyncEventBus(Executors.newFixedThreadPool(2));

    public static AsyncEventBus getInstance() {
        return eventBus;
    }


}
