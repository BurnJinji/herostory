package com.burning8393.herostory;

import com.burning8393.herostory.mq.MQConsumer;
import com.burning8393.herostory.util.RedisUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 排行榜应用程序
 */
public class RankApp {
    /**
     * 日志对象
     */
    static private final Logger LOGGER = LoggerFactory.getLogger(RankApp.class);

    /**
     * 应用主程序启动
     * @param args 启动参数
     */
    public static void main(String[] args) {
        RedisUtil.init();
        MQConsumer.init();

        LOGGER.info("排行榜应用程序启动成功");
    }
}
