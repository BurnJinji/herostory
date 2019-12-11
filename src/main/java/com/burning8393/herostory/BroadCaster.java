package com.burning8393.herostory;

import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;

/**
 * 广播员
 */
public final class BroadCaster {
    /**
     * 客户端信道数组，一定要使用 static，否则无法实现群发
     */
    private static final ChannelGroup _channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    /**
     * 私有化类默认构造器
     */
    private BroadCaster() {

    }

    /**
     * 添加信道
     *
     * @param channel
     */
    public static void addChannel(Channel channel) {
        _channelGroup.add(channel);
    }

    /**
     * 移除信道
     *
     * @param channel
     */
    public static void removeCHannel(Channel channel) {
        _channelGroup.remove(channel);
    }

    /**
     * 广播消息
     *
     * @param msg
     */
    public static void broadcast(Object msg) {
        if (null == msg) {
            return;
        }
        _channelGroup.writeAndFlush(msg);
    }
}
