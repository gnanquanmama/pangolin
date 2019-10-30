package com.mcoding.pangolin.server.manager.func;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.mcoding.pangolin.common.constant.Constants;
import com.mcoding.pangolin.server.context.PangolinChannelContext;
import io.netty.channel.Channel;

import java.util.List;
import java.util.function.Function;

/**
 * @author wzt on 2019/7/16.
 * @version 1.0
 */
public class MenuListFunc implements Function<Void, String> {

    @Override
    public String apply(Void aVoid) {

        List<String> instructionList = Lists.newArrayList();
        instructionList.add(">>> 0.菜单");
        instructionList.add(">>> 1.查询所有在线通道信息");
        instructionList.add(">>> 2.查询公网端口配置信息");
        instructionList.add(">>> 3.关闭已失活的通道");
        instructionList.add(">>> 4.查询请求链路信息");
        instructionList.add(">>> 5.查询用户流量信息");
        instructionList.add(">>> x.退出请输入exit");
        instructionList.add(">>> 请输入对应的数字..." + Constants.LINE_BREAK);

        return Joiner.on(Constants.LINE_BREAK).join(instructionList);
    }
}
