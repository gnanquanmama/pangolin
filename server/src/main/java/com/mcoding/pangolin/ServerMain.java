package com.mcoding.pangolin;

import com.mcoding.pangolin.task.BaseChannelServerTask;

/**
 * @author wzt on 2019/6/17.
 * @version 1.0
 */
public class ServerMain {

    public static void main(String[] args) {
        new BaseChannelServerTask(8007, false).run();
    }

}
