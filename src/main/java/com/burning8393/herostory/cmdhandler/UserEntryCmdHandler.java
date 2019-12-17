package com.burning8393.herostory.cmdhandler;

import com.burning8393.herostory.BroadCaster;
import com.burning8393.herostory.model.User;
import com.burning8393.herostory.model.UserManager;
import com.burning8393.herostory.msg.GameMsgProtocol;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 用户入场指令处理器
 */
public class UserEntryCmdHandler implements ICmdHandler<GameMsgProtocol.UserEntryCmd> {

    /**
     * 日志对象
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(UserEntryCmdHandler.class);

    @Override
    public void handle(ChannelHandlerContext ctx, GameMsgProtocol.UserEntryCmd msg) {
        if (null == ctx
                || null == msg) {
            return;
        }

        // 获取用户id
        Integer userId = (Integer) ctx.channel().attr(AttributeKey.valueOf("userId")).get();
        if (null == userId) {
            return;
        }

        // 获取已有用户
        User existUser = UserManager.getUser(userId);
        if (null == existUser) {
            LOGGER.error("用户不存在， userId = {}", userId);
            return;
        }

        // 获取英雄形象
        String heroAvatar = existUser.heroAvatar;

        GameMsgProtocol.UserEntryResult.Builder newBuilder = GameMsgProtocol.UserEntryResult.newBuilder();
        newBuilder.setUserId(userId);
        newBuilder.setHeroAvatar(heroAvatar);

        // 构建结果并发送出去
        GameMsgProtocol.UserEntryResult result = newBuilder.build();
        BroadCaster.broadcast(result);
    }
}
