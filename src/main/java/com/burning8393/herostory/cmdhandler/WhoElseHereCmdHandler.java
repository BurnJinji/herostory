package com.burning8393.herostory.cmdhandler;

import com.burning8393.herostory.model.User;
import com.burning8393.herostory.model.UserManager;
import com.burning8393.herostory.msg.GameMsgProtocol;
import io.netty.channel.ChannelHandlerContext;

/**
 * 谁在场指令处理器
 */
public class WhoElseHereCmdHandler implements ICmdHandler<GameMsgProtocol.WhoElseIsHereCmd> {
    @Override
    public void handle(ChannelHandlerContext ctx, GameMsgProtocol.WhoElseIsHereCmd msg) {
        GameMsgProtocol.WhoElseIsHereResult.Builder newBuilder = GameMsgProtocol.WhoElseIsHereResult.newBuilder();

        for (User currUser : UserManager.listUser()) {
            if (currUser == null) {
                return;
            }
            GameMsgProtocol.WhoElseIsHereResult.UserInfo.Builder userInfoBuiler = GameMsgProtocol.WhoElseIsHereResult.UserInfo.newBuilder();
            userInfoBuiler.setUserId(currUser.userId);
            userInfoBuiler.setHeroAvatar(currUser.heroAvatar);
            GameMsgProtocol.WhoElseIsHereResult.UserInfo userInfoProto = userInfoBuiler.build();
            newBuilder.addUserInfo(userInfoProto);
        }

        GameMsgProtocol.WhoElseIsHereResult newResult = newBuilder.build();
        ctx.writeAndFlush(newResult);
    }
}
