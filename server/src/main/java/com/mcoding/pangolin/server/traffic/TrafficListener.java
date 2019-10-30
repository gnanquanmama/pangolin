package com.mcoding.pangolin.server.traffic;

import com.google.common.eventbus.Subscribe;
import com.mcoding.pangolin.server.context.TrafficTable;

public class TrafficListener {

    @Subscribe
    public void listen(TrafficEvent trafficEvent) {

        String userPrivateKey = trafficEvent.getUserPrivateKye();
        long inFlow = trafficEvent.getInFlow();
        long outFlow = trafficEvent.getOutFlow();

        TrafficTable.record(userPrivateKey, inFlow, outFlow);
    }
}
