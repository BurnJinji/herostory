package com.burning8393.herostory.mq;

import com.alibaba.fastjson.JSONObject;
import com.burning8393.herostory.rank.RankService;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.message.MessageExt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * 消息队列消费者
 */
public final class MQConsumer {

    /**
     * 日志对象
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(MQConsumer.class);

    /**
     * 私有化类默认构造器
     */
    private MQConsumer() {

    }

    /**
     * 初始化
     */
    public static void init() {
        // 创建消息队列消费者
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer("herostory");
        // 设置 nameServer 地址
        consumer.setNamesrvAddr("192.168.56.99:9876");

        try {
            // 订阅
            consumer.subscribe("Victor", "");

            // 注册回调
            consumer.registerMessageListener(new MessageListenerConcurrently() {
                @Override
                public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgExtList, ConsumeConcurrentlyContext ctx) {
                    for (MessageExt msgExt : msgExtList) {
                        // 解析战斗结果消息
                        VictorMsg mqMsg = JSONObject.parseObject(
                            msgExt.getBody(),
                            VictorMsg.class
                        );

                        LOGGER.info("从消息队列中收到战斗结果, winnerId = {}, loserId = {}",
                                mqMsg.winnerId,
                                mqMsg.loserId);

                        // 刷新排行榜
                        RankService.getInstance().refreshRank(mqMsg.winnerId, mqMsg.loserId);
                    }

                    return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
                }
            });

            // 启动消费者
            consumer.start();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }

    }
}
