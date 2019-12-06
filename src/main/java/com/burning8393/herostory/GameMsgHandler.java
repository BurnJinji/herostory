package com.burning8393.herostory;

import com.burning8393.herostory.msg.GameMsgProtocol;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * 自定义的消息处理器
 */
public class GameMsgHandler extends SimpleChannelInboundHandler {
    /**
     * 客户端信道数组，一定要使用 static，否则无法实现群发
     */
    private static final ChannelGroup _channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    /**
     * 用户字典
     */
    private static final Map<Integer, User> userMap = new HashMap<>();

    private static final Logger LOGGER = LoggerFactory.getLogger(GameMsgHandler.class);

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        _channelGroup.add(ctx.channel());
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        super.handlerRemoved(ctx);
        _channelGroup.remove(ctx.channel());

        // 先拿到用户id
        Integer userId = (Integer) ctx.channel().attr(AttributeKey.valueOf("userId")).get();

        if (userId == null) {
            return;
        }

        userMap.remove(userId);

        GameMsgProtocol.UserQuitResult.Builder resultBuilder = GameMsgProtocol.UserQuitResult.newBuilder();
        resultBuilder.setQuitUserId(userId);

        GameMsgProtocol.UserQuitResult newResult = resultBuilder.build();
        _channelGroup.writeAndFlush(newResult);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        LOGGER.debug("收到客户端消息， msgClazz = " + msg.getClass().getName() + ", msg = " + msg);
        if (msg instanceof GameMsgProtocol.UserEntryCmd) {
            // 冲指令对象中获取用户id和英雄形象
            GameMsgProtocol.UserEntryCmd cmd = (GameMsgProtocol.UserEntryCmd) msg;
            int userId = cmd.getUserId();
            String heroAvatar = cmd.getHeroAvatar();

            GameMsgProtocol.UserEntryResult.Builder newBuilder = GameMsgProtocol.UserEntryResult.newBuilder();
            newBuilder.setUserId(userId);
            newBuilder.setHeroAvatar(heroAvatar);

            // 将用户加入字典
            User newUser = new User();
            newUser.userId = userId;
            newUser.heroAvatar = heroAvatar;
            userMap.put(newUser.userId, newUser);
            // 将用户id附着在channel上
            ctx.channel().attr(AttributeKey.valueOf("userId")).set(userId);

            // 构建结果并发送出去
            GameMsgProtocol.UserEntryResult result = newBuilder.build();
            _channelGroup.writeAndFlush(result);
        } else if (msg instanceof GameMsgProtocol.WhoElseIsHereCmd) {
            GameMsgProtocol.WhoElseIsHereResult.Builder newBuilder = GameMsgProtocol.WhoElseIsHereResult.newBuilder();

            for (User currUser : userMap.values()) {
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
        } else if (msg instanceof GameMsgProtocol.UserMoveToCmd) {
            Integer userId = (Integer) ctx.channel().attr(AttributeKey.valueOf("userId")).get();
            if (userId == null) {
                return;
            }
            GameMsgProtocol.UserMoveToCmd cmd = (GameMsgProtocol.UserMoveToCmd) msg;

            GameMsgProtocol.UserMoveToResult.Builder newBuilder = GameMsgProtocol.UserMoveToResult.newBuilder();
            newBuilder.setUserId(userId);
            newBuilder.setMoveToPosX(cmd.getMoveToPosX());
            newBuilder.setMoveToPosY(cmd.getMoveToPosY());

            GameMsgProtocol.UserMoveToResult newResult = newBuilder.build();
            _channelGroup.writeAndFlush(newResult);
        }
    }
}
