package com.burning8393.herostory.cmdhandler;

import com.burning8393.herostory.BroadCaster;
import com.burning8393.herostory.msg.GameMsgProtocol;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;

/**
 * 攻击消息处理器
 */
public class UserAttkHandler implements ICmdHandler<GameMsgProtocol.UserAttkCmd> {
    @Override
    public void handle(ChannelHandlerContext ctx, GameMsgProtocol.UserAttkCmd cmd) {
        if (null == ctx || null == cmd) {
            return;
        }

        //获取攻击者id
        Integer attkUserId = (Integer) ctx.channel().attr(AttributeKey.valueOf("userId")).get();
        if (null == attkUserId) {
            return;
        }

        // 获取被攻击者id
        int targetUserId = cmd.getTargetUserId();

        GameMsgProtocol.UserAttkResult.Builder resultBuilder = GameMsgProtocol.UserAttkResult.newBuilder();
        resultBuilder.setAttkUserId(attkUserId);
        resultBuilder.setTargetUserId(targetUserId);

        GameMsgProtocol.UserAttkResult attkResult = resultBuilder.build();
        BroadCaster.broadcast(attkResult);

        // 减血消息，可以根据自己的喜好写。。。
        // 例如加上装备加成， 暴击等等
        // 这些都属于游戏的业务逻辑了
        GameMsgProtocol.UserSubtractHpResult.Builder subtractResultBuilder = GameMsgProtocol.UserSubtractHpResult.newBuilder();
        subtractResultBuilder.setTargetUserId(targetUserId);
        subtractResultBuilder.setSubtractHp(10);

        GameMsgProtocol.UserSubtractHpResult subtractHpResult = subtractResultBuilder.build();
        BroadCaster.broadcast(subtractHpResult);

    }
}
