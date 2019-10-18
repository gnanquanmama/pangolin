package com.mcoding.pangolin.client.util;

import java.util.concurrent.TimeUnit;

/**
 * @author wzt on 2019/10/18.
 * @version 1.0
 */
public class ThreadUtils {

    public static void sleep(int time, TimeUnit timeUnit) {
        try {
            timeUnit.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
