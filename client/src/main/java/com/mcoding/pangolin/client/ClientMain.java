package com.mcoding.pangolin.client;

import com.mcoding.pangolin.client.container.ClientContainer;
import com.mcoding.pangolin.client.entity.ProxyInfo;
import com.mcoding.pangolin.common.PangolinEngine;
import io.netty.util.internal.StringUtil;
import org.apache.commons.cli.*;

/**
 * @author wzt on 2019/6/17.
 * @version 1.0
 */
public class ClientMain {


    public static void main(String[] args) throws ParseException {
        ProxyInfo proxyInfo = buildProxyInfo(args);
        PangolinEngine.start(new ClientContainer(proxyInfo));
    }


    /**
     * 根据命令行操作构建代理机器连接信息
     *
     * @param args
     * @return
     * @throws ParseException
     */
    private static ProxyInfo buildProxyInfo(String[] args) throws ParseException {
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
            realServerPort = "8083";
        }
        String privateKey = cmd.getOptionValue("private_key");
        if (StringUtil.isNullOrEmpty(privateKey)) {
            privateKey = "qaz123";
        }

        ProxyInfo proxyInfo = new ProxyInfo();
        proxyInfo.setPrivateKey(privateKey);
        proxyInfo.setRealServerHost(realServerHost);
        proxyInfo.setRealServerPort(Integer.valueOf(realServerPort));
        proxyInfo.setProxyServerHost(proxyServerHost);
        proxyInfo.setProxyServerPort(Integer.valueOf(proxyServerPort));

        return proxyInfo;
    }

    private static Options buildOption() {
        Options options = new Options();
        options.addOption("proxy_server_host", true, "");
        options.addOption("proxy_server_port", true, "");
        options.addOption("real_server_host", true, "");
        options.addOption("real_server_port", true, "");
        options.addOption("private_key", true, "");

        return options;
    }
}
