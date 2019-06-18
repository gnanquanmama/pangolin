package com.mcoding.pangolin.listener;

import com.google.common.eventbus.Subscribe;
import com.mcoding.pangolin.context.UserProxyServerContext;
import com.mcoding.pangolin.listener.event.CloseProxyPortServerEvent;
import com.mcoding.pangolin.listener.event.CreateNewProxyPortEvent;
import com.mcoding.pangolin.task.UserProxyServerTask;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author wzt on 2019/6/18.
 * @version 1.0
 */
public class NewProxyPortListener {

    private static ExecutorService execService = Executors.newCachedThreadPool();

    @Subscribe
    public void listen(CreateNewProxyPortEvent createNewProxyPortEvent) {
        UserProxyServerTask proxyServerTask = new UserProxyServerTask(createNewProxyPortEvent.getProxyPort(), false);
        execService.submit(proxyServerTask);

        UserProxyServerContext.put(createNewProxyPortEvent.getProxyPort(), proxyServerTask);
    }

    @Subscribe
    public void close(CloseProxyPortServerEvent closeProxyPortServerEvent) {
        try {
            UserProxyServerContext.get(closeProxyPortServerEvent.getProxyPort()).close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
