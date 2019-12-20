package com.burning8393.herostory.cmdhandler;

import com.burning8393.herostory.BroadCaster;
import com.burning8393.herostory.model.User;
import com.burning8393.herostory.model.UserManager;
import com.burning8393.herostory.mq.MQProducer;
import com.burning8393.herostory.mq.VictorMsg;
import com.burning8393.herostory.msg.GameMsgProtocol;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;

/**
 * 攻击消息处理器
 */
public class UserAttkCmdHandler implements ICmdHandler<GameMsgProtocol.UserAttkCmd> {
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

        // 获取被攻击者
        User targetUser = UserManager.getUser(targetUserId);
        if (null == targetUser) {
            return;
        }

        // 减血消息，可以根据自己的喜好写。。。
        // 例如加上装备加成， 暴击等等
        // 这些都属于游戏的业务逻辑了
        int subtractHp = 10;
        targetUser.currHp = targetUser.currHp - subtractHp;

        // 广播减血消息
        broadcastSubtractHp(targetUserId, subtractHp);

        if (targetUser.currHp <= 0) {
            // 广播死亡消息
            broadcastDie(targetUserId);

            if (!targetUser.died) {
                // 设置死亡标志
                targetUser.died = true;

                // 发送消息到MQ
                VictorMsg mqMsq = new VictorMsg();
                mqMsq.winnerId = attkUserId;
                mqMsq.loserId = targetUserId;
                MQProducer.sendMsg("Victor", mqMsq);
            }
        }
    }

    /**
     * 广播减血消息
     *
     * @param targetUserId 被攻击者 id
     * @param targetUserHp 减血量
     */
    private void broadcastSubtractHp(int targetUserId, int targetUserHp) {
        if (targetUserId <= 0 ||
                targetUserHp < 0) {
            return;
        }

        GameMsgProtocol.UserSubtractHpResult.Builder subtractResultBuilder = GameMsgProtocol.UserSubtractHpResult.newBuilder();
        subtractResultBuilder.setTargetUserId(targetUserId);
        subtractResultBuilder.setSubtractHp(targetUserHp);

        GameMsgProtocol.UserSubtractHpResult subtractHpResult = subtractResultBuilder.build();
        BroadCaster.broadcast(subtractHpResult);
    }

    /**
     * 广播死亡消息
     *
     * @param targetUserId 被攻击者 id
     */
    private void broadcastDie(int targetUserId) {
        if (targetUserId <= 0) {
            return;
        }

        GameMsgProtocol.UserDieResult.Builder resultBuilder = GameMsgProtocol.UserDieResult.newBuilder();
        resultBuilder.setTargetUserId(targetUserId);

        GameMsgProtocol.UserDieResult newResult = resultBuilder.build();
        BroadCaster.broadcast(newResult);
    }
}
