package com.burning8393.herostory;

import com.burning8393.herostory.cmdhandler.CmdHandlerFactory;
import com.burning8393.herostory.cmdhandler.ICmdHandler;
import com.google.protobuf.GeneratedMessageV3;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 主线程处理器
 */
public final class MainThreadProcessor {

    /**
     * 日志对象
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(MainThreadProcessor.class);

    /**
     * 单例对象
     */
    private static final MainThreadProcessor INSTANCE = new MainThreadProcessor();

    /**
     * 创建一个单线程
     */
    private final ExecutorService _es = Executors.newSingleThreadExecutor(r -> {
        Thread newThread = new Thread(r);
        newThread.setName("MainThreadProcessor");
        return newThread;
    });


    /**
     * 私有化类默认构造器
     */
    private MainThreadProcessor() {
    }

    /**
     * 获取单例对象
     * @return 单例对象
     */
    public static MainThreadProcessor getInstance() {
        return INSTANCE;
    }

    public void process(ChannelHandlerContext ctx, GeneratedMessageV3 msg) {
        if (null == ctx || null == msg) {
            return;
        }

        // 获取消息类
        Class<?> msgClazz = msg.getClass();

        LOGGER.info(
            "受到客户端消息， msgClazz = {}",
            msgClazz.getName()
        );

        _es.submit(() -> {
            // 获取指令处理器
            ICmdHandler<? extends GeneratedMessageV3> cmdHandler
                    = CmdHandlerFactory.create(msg.getClass());

            if (null == cmdHandler) {
                LOGGER.error(
                    "未找到相对应的消息处理器，msgClazz = {}",
                    msgClazz.getName()
                );
                return;
            }

            try {
                // 处理指令
                cmdHandler.handle(ctx, cast(msg));
            } catch (Exception e) {
                // 注意： 这里一定要套在 try... catch... 块里，
                // 避免handler报错导致线程终止
                LOGGER.error(e.getMessage(), e);
            }
        });

    }

    /**
     * 转型消息对象
     * @param msg 消息对象
     * @param <TCmd> 指令类型
     * @return 指令对象
     */
    private static <TCmd extends GeneratedMessageV3> TCmd cast(Object msg) {
        if (null == msg) {
            return null;
        } else {
            return (TCmd) msg;
        }
    }
}
