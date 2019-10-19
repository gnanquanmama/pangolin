package com.mcoding.pangolin.server.flow;

import com.google.common.eventbus.Subscribe;

public class FlowListener {

    @Subscribe
    public void listen(FlowEvent flowEvent) {

        String userPrivateKey = flowEvent.getUserPrivateKye();
        long inFlow = flowEvent.getInFlow();
        long outFlow = flowEvent.getOutFlow();

        FlowTable.record(userPrivateKey, inFlow, outFlow);
    }
}
