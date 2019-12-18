package com.burning8393.herostory.async;

import com.burning8393.herostory.MainThreadProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 异步操作处理器
 */
public final class AsyncOperationProcessor {

    /**
     * 日志对象
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(AsyncOperationProcessor.class);

    /**
     * 单例对象
     */
    private static final AsyncOperationProcessor INSTANCE = new AsyncOperationProcessor();

    /**
     * 线程池数组
     */
    private final ExecutorService[] _esArray = new ExecutorService[8];

    /**
     * 私有化类默认构造器
     */
    private AsyncOperationProcessor() {
        for (int i = 0; i < _esArray.length; i++) {
            // 创建线程名称
            String executorServiceName = "AsyncOperationProcessor_" + i;

            // 创建单线程服务
            _esArray[i] = Executors.newSingleThreadExecutor((newRunnable) -> {
                Thread newThread = new Thread(newRunnable);
                newThread.setName(executorServiceName);
                return newThread;
            });
        }
    }

    /**
     * 获取单例对象
     *
     * @return 单例对象
     */
    public static AsyncOperationProcessor getINSTANCE() {
        return INSTANCE;
    }

    /**
     * 处理器异步操作
     *
     * @param operation 异步操纵
     */
    public void process(IAsyncOperation operation) {
        if (operation == null) {
            return;
        }

        // 根据绑定id 获取线程索引
        int bindId = operation.getBindId();
        int esIndex = bindId % _esArray.length;

        _esArray[esIndex].submit(() -> {
            try {
                // 执行异步操作
                operation.doAsync();

                // 回到主线程处理器执行完成逻辑
                MainThreadProcessor.getInstance().process(operation::doFinish);
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
        });

    }


}
