package com.burning8393.herostory.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 * redis 工具类
 */
public final class RedisUtil {
    /**
     * 日志对象
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(RedisUtil.class);

    /**
     * redis 连接池
     */
    private static JedisPool jedisPool = null;

    /**
     * 私有化类默认构造器
     */
    private RedisUtil() {

    }

    /**
     * 初始化
     */
    public static void init() {
        try {
            jedisPool = new JedisPool("192.168.56.99", 6379);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    /**
     * 获取 redis 实例
     * @return redis 实例
     */
    public static Jedis getJedis() {
        if (null == jedisPool) {
            throw new RuntimeException("jedisPool 尚未初始化");
        }

        Jedis result = jedisPool.getResource();

        return result;
    }
}
