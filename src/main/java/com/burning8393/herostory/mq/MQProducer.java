package com.burning8393.herostory.mq;

import com.alibaba.fastjson.JSONObject;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 消息队列生产者
 */
public final class MQProducer {
    /**
     * 日志对象
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(MQProducer.class);

    /**
     * 生产者对象
     */
    private static DefaultMQProducer _producer = null;

    /**
     * 私有化类默认构造器
     */
    private MQProducer() {
    }

    /**
     * 初始化
     */
    public static void init() {
        try {
            // 创建生产者
            DefaultMQProducer producer = new DefaultMQProducer("herostory");
            producer.setNamesrvAddr("192.168.56.99:9876");
            // 启动生产者
            producer.start();
            producer.setRetryTimesWhenSendAsyncFailed(3);

            _producer = producer;
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
    }

    /**
     * 发送消息
     *
     * @param topic 主题
     * @param msg 消息对象
     */
    public static void sendMsg(String topic, Object msg) {
        if (null == topic || null == msg) {
            return;
        }

        if (null == _producer) {
            throw new RuntimeException("Producer 尚未初始化");
        }

        Message mqMsg = new Message();
        mqMsg.setTopic(topic);
        mqMsg.setBody(JSONObject.toJSONBytes(msg));

        try {
            _producer.send(mqMsg);
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
    }
}
