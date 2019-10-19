package com.mcoding.pangolin.server.flow;

import lombok.Data;

/**
 * @author wzt
 */
@Data
public class FlowEvent {

    private String userPrivateKye;
    private long inFlow = 0;
    private long outFlow = 0;
}
