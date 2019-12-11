package com.burning8393.herostory.cmdhandler;

import com.burning8393.herostory.util.PackageUtil;
import com.google.protobuf.GeneratedMessageV3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 指令处理器工厂
 */
public final class CmdHandlerFactory {
    /**
     * 日志对象
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(CmdHandlerFactory.class);
    /**
     * 处理器字典
     */
    private static final Map<Class<?>, ICmdHandler<? extends GeneratedMessageV3>> _handlerMap = new HashMap<>();

    /**
     * 私有化类默认构造器
     */
    private CmdHandlerFactory() {

    }

    /**
     * 初始化
     */
    public static void init() {
        LOGGER.info("========= 完成Cmd 和 Handler 的关联 ============");

        // 获取包名称
        String packageName = CmdHandlerFactory.class.getPackage().getName();

        // 获取所有的 ICmdHandler 的子类
        Set<Class<?>> clazzSet = PackageUtil.listSubClazz(
            packageName,
            true,
            ICmdHandler.class
        );
        for (Class<?> clazz : clazzSet) {
            if ((clazz.getModifiers() & Modifier.ABSTRACT) != 0) {
                // 如果是抽象类
                continue;
            }

            // 获取方法数组
            Method[] methodArray = clazz.getDeclaredMethods();
            // 消息类型
            Class<?> msgType = null;

            for (Method currMethod : methodArray) {
                if (!currMethod.getName().equals("handle")) {
                    // 如果不是handle 方法
                    continue;
                }

                // 获取函数参数类型
                Class<?>[] parameterArray = currMethod.getParameterTypes();

                if (parameterArray.length < 2 ||
                    parameterArray[1] == GeneratedMessageV3.class ||
                    !GeneratedMessageV3.class.isAssignableFrom(parameterArray[1])) {
                    continue;
                }

                msgType = parameterArray[1];
                break;

            }

            if (null == msgType) {
                continue;
            }

            try {
                // 创建指令处理器
                ICmdHandler<?> newHandler = (ICmdHandler<?>) clazz.newInstance();

                LOGGER.info(
                        "关联 {} <==> {}",
                        msgType.getName(),
                        clazz.getName()
                );
                _handlerMap.put(msgType, newHandler);
            } catch (Exception ex) {
                LOGGER.error(ex.getMessage(), ex);
            }

        }
//        _handlerMap.put(GameMsgProtocol.UserEntryCmd.class, new UserEntryCmdHandler());
//        _handlerMap.put(GameMsgProtocol.WhoElseIsHereCmd.class, new WhoElseHereCmdHandler());
//        _handlerMap.put(GameMsgProtocol.UserMoveToCmd.class, new UserMoveToCmdHandler());
    }

    /**
     * 创建指令处理器工厂
     * @param clazz 消息类
     * @return
     */
    public static ICmdHandler<? extends GeneratedMessageV3> create(Class<?> clazz) {
        if (null == clazz) {
            return null;
        }
        return _handlerMap.get(clazz);
    }
}
