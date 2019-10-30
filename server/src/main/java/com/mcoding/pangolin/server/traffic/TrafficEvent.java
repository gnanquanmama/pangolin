package com.mcoding.pangolin.server.traffic;

import lombok.Data;

/**
 * @author wzt
 */
@Data
public class TrafficEvent {

    private String userPrivateKye;
    private long inFlow = 0;
    private long outFlow = 0;
}
