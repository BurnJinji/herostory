package com.burning8393.herostory.cmdhandler;

import com.burning8393.herostory.model.MoveState;
import com.burning8393.herostory.model.User;
import com.burning8393.herostory.model.UserManager;
import com.burning8393.herostory.msg.GameMsgProtocol;
import io.netty.channel.ChannelHandlerContext;

/**
 * 谁在场指令处理器
 */
public class WhoElseHereCmdHandler implements ICmdHandler<GameMsgProtocol.WhoElseIsHereCmd> {
    @Override
    public void handle(ChannelHandlerContext ctx, GameMsgProtocol.WhoElseIsHereCmd cmd) {
        if (null == ctx
                || null == cmd) {
            return;
        }

        GameMsgProtocol.WhoElseIsHereResult.Builder resultBuilder = GameMsgProtocol.WhoElseIsHereResult.newBuilder();

        for (User currUser : UserManager.listUser()) {
            if (currUser == null) {
                continue;
            }

            // 在这里构建每一个用户的信息
            GameMsgProtocol.WhoElseIsHereResult.UserInfo.Builder userInfoBuiler = GameMsgProtocol.WhoElseIsHereResult.UserInfo.newBuilder();
            userInfoBuiler.setUserId(currUser.userId);
            userInfoBuiler.setHeroAvatar(currUser.heroAvatar);

            // 构建移动状态消息
            MoveState moveState = currUser.moveState;
            GameMsgProtocol.WhoElseIsHereResult.UserInfo.MoveState.Builder
                    moveStateBuilder = GameMsgProtocol.WhoElseIsHereResult.UserInfo.MoveState.newBuilder();
            moveStateBuilder.setFromPosX(moveState.fromPosX);
            moveStateBuilder.setFromPosY(moveState.fromPosY);
            moveStateBuilder.setToPosX(moveState.toPosX);
            moveStateBuilder.setToPosY(moveState.toPosY);
            moveStateBuilder.setStartTime(moveState.startTime);

            // 将移动状态设置给用户信息
            userInfoBuiler.setMoveState(moveStateBuilder);
            // 将用户消息添加到结果消息
            resultBuilder.addUserInfo(userInfoBuiler);
        }

        GameMsgProtocol.WhoElseIsHereResult newResult = resultBuilder.build();
        ctx.writeAndFlush(newResult);
    }
}
