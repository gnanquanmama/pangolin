package com.mcoding.pangolin.common;

import java.util.List;

/**
 * @author wzt on 2019/6/26.
 * @version 1.0
 */
public class PangolinEngine {


    public static void start(List<LifeCycle> lifeCycles) {
        for (LifeCycle lifeCycle : lifeCycles) {
            lifeCycle.start();
        }
    }

}
