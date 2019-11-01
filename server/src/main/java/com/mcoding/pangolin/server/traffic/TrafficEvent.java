package com.mcoding.pangolin.server.traffic;

import lombok.Data;

/**
 * @author wzt
 */
@Data
public class TrafficEvent implements Cloneable {

    private String userPrivateKye;
    private long inFlow = 0;
    private long outFlow = 0;

    @Override
    public TrafficEvent clone() {
        try {
            return (TrafficEvent) super.clone();
        } catch (Exception e) {
            e.printStackTrace();
            return new TrafficEvent();
        }
    }
}
