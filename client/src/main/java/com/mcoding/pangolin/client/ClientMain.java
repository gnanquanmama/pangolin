package com.mcoding.pangolin.client;

import com.mcoding.pangolin.client.container.ClientContainer;
import com.mcoding.pangolin.client.entity.ProxyInfo;
import io.netty.util.internal.StringUtil;
import org.apache.commons.cli.*;

/**
 * @author wzt on 2019/6/17.
 * @version 1.0
 */
public class ClientMain {


    public static void main(String[] args) throws ParseException{

        Options argOptions = buildOption();
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(argOptions, args);
        String proxyServerHost = cmd.getOptionValue("proxy_server_host");
        if (StringUtil.isNullOrEmpty(proxyServerHost)) {
            proxyServerHost = "127.0.0.1";
        }
        String proxyServerPort = cmd.getOptionValue("proxy_server_port");
        if (StringUtil.isNullOrEmpty(proxyServerPort)) {
            proxyServerPort = "7979";
        }
        String realServerHost = cmd.getOptionValue("real_server_host");
        if (StringUtil.isNullOrEmpty(realServerHost)) {
            realServerHost = "127.0.0.1";
        }
        String realServerPort = cmd.getOptionValue("real_server_port");
        if (StringUtil.isNullOrEmpty(realServerPort)) {
            realServerPort = "9999";
        }
        String userId = cmd.getOptionValue("user_id");
        if (StringUtil.isNullOrEmpty(userId)) {
            userId = "1";
        }

        ProxyInfo proxyInfo = new ProxyInfo();
        proxyInfo.setUserId(userId);
        proxyInfo.setRealServerHost(realServerHost);
        proxyInfo.setRealServerPort(Integer.valueOf(realServerPort));
        proxyInfo.setProxyServerHost(proxyServerHost);
        proxyInfo.setProxyServerPort(Integer.valueOf(proxyServerPort));

        new ClientContainer(proxyInfo).start();
    }

    private static Options buildOption() {
        Options options = new Options();
        options.addOption("proxy_server_host", true, "");
        options.addOption("proxy_server_port", true, "");
        options.addOption("real_server_host", true, "");
        options.addOption("real_server_port", true, "");
        options.addOption("user_id", true, "");

        return options;
    }
}
