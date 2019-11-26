package com.mcoding.pangolin.client;

import com.google.common.collect.Lists;
import com.mcoding.pangolin.client.container.ClientBootstrapContainer;
import com.mcoding.pangolin.client.model.AddressBridge;
import com.mcoding.pangolin.common.PangolinEngine;
import com.mcoding.pangolin.common.util.PropertyUtils;
import org.apache.commons.cli.*;
import org.apache.commons.lang3.StringUtils;

/**
 * @author wzt on 2019/6/17.
 * @version 1.0
 */
public class ClientBootMain {


    public static void main(String[] args) throws ParseException {
        AddressBridge addressBridge = buildProxyInfo(args);
        PangolinEngine.start(Lists.newArrayList(new ClientBootstrapContainer(addressBridge)));
    }


    /**
     * 根据命令行操作构建代理机器连接信息
     *
     * @param args
     * @return
     * @throws ParseException
     */
    private static AddressBridge buildProxyInfo(String[] args) throws ParseException {

        // 读取配置文件信息，初始化 管道的桥接信息
        AddressBridge addressBridge = new AddressBridge();
        addressBridge.setPrivateKey(PropertyUtils.get("private_key"));
        addressBridge.setTargetServerHost(PropertyUtils.get("target_server_host"));
        addressBridge.setTargetServerPort(PropertyUtils.getInt("target_Server_port"));
        addressBridge.setIntranetProxyServerHost(PropertyUtils.get("intranet_proxy_server_host"));
        addressBridge.setIntranetProxyServerPort(PropertyUtils.getInt("intranet_proxy_server_port"));


        Options argOptions = buildOption();
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(argOptions, args);

        String privateKey = cmd.getOptionValue("p_key");
        if (StringUtils.isNoneBlank(privateKey)) {
            addressBridge.setPrivateKey(privateKey);
        }

        String intranetProxyServerHost = cmd.getOptionValue("i_host");
        if (StringUtils.isNoneBlank(intranetProxyServerHost)) {
            addressBridge.setIntranetProxyServerHost(intranetProxyServerHost);
        }
        String intranetProxyServerPort = cmd.getOptionValue("i_port");
        if (StringUtils.isNoneBlank(intranetProxyServerPort)) {
            addressBridge.setIntranetProxyServerPort(Integer.valueOf(intranetProxyServerPort));
        }
        String targetServerHost = cmd.getOptionValue("t_host");
        if (StringUtils.isNoneBlank(targetServerHost)) {
            addressBridge.setTargetServerHost(targetServerHost);
        }
        String targetServerPort = cmd.getOptionValue("t_port");
        if (StringUtils.isNoneBlank(targetServerPort)) {
            addressBridge.setTargetServerPort(Integer.valueOf(targetServerPort));
        }

        return addressBridge;
    }

    private static Options buildOption() {
        Options options = new Options();
        options.addOption("p_key", true, "");
        options.addOption("i_host", true, "");
        options.addOption("i_port", true, "");
        options.addOption("t_host", true, "");
        options.addOption("t_port", true, "");

        return options;
    }
}
