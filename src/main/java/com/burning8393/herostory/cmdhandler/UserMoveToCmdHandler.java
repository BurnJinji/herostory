package com.burning8393.herostory.cmdhandler;

import com.burning8393.herostory.BroadCaster;
import com.burning8393.herostory.model.MoveState;
import com.burning8393.herostory.model.User;
import com.burning8393.herostory.model.UserManager;
import com.burning8393.herostory.msg.GameMsgProtocol;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 用户移动指令处理器
 */
public class UserMoveToCmdHandler implements ICmdHandler<GameMsgProtocol.UserMoveToCmd> {
    /**
     * 日志对象
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(UserMoveToCmdHandler.class);

    @Override
    public void handle(ChannelHandlerContext ctx, GameMsgProtocol.UserMoveToCmd cmd) {
        if (null == ctx || null == cmd) {
            return;
        }
        Integer userId = (Integer) ctx.channel().attr(AttributeKey.valueOf("userId")).get();
        if (userId == null) {
            return;
        }

        // 获取移动用户
        User moveUser = UserManager.getUser(userId);
        if (moveUser == null) {
            LOGGER.info("未找到用户， userId = {}", userId);
            return;
        }

        // 获取移动状态
        MoveState moveState = moveUser.moveState;
        // 设置位置和开始时间
        moveState.fromPosX = cmd.getMoveFromPosX();
        moveState.fromPosY = cmd.getMoveFromPosY();
        moveState.toPosX = cmd.getMoveToPosX();
        moveState.toPosY = cmd.getMoveToPosY();
        moveState.startTime = System.currentTimeMillis();

        GameMsgProtocol.UserMoveToResult.Builder newBuilder = GameMsgProtocol.UserMoveToResult.newBuilder();
        newBuilder.setMoveUserId(userId);
        newBuilder.setMoveFromPosX(moveState.fromPosX);
        newBuilder.setMoveFromPosY(moveState.fromPosY);
        newBuilder.setMoveToPosX(moveState.toPosX);
        newBuilder.setMoveToPosY(moveState.toPosY);
        newBuilder.setMoveStartTime(moveState.startTime);

        GameMsgProtocol.UserMoveToResult newResult = newBuilder.build();
        BroadCaster.broadcast(newResult);
    }
}
