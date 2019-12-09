package com.burning8393.herostory.cmdhandler;

import com.burning8393.herostory.msg.GameMsgProtocol;
import com.google.protobuf.GeneratedMessageV3;

import java.util.HashMap;
import java.util.Map;

/**
 * 指令处理器工厂
 */
public final class CmdHandlerFactory {
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
        _handlerMap.put(GameMsgProtocol.UserEntryCmd.class, new UserEntryCmdHandler());
        _handlerMap.put(GameMsgProtocol.WhoElseIsHereCmd.class, new WhoElseHereCmdHandler());
        _handlerMap.put(GameMsgProtocol.UserMoveToCmd.class, new UserMoveToCmdHandler());
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
