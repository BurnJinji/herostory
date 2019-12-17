package com.burning8393.herostory.cmdhandler;

import com.burning8393.herostory.BroadCaster;
import com.burning8393.herostory.login.LoginService;
import com.burning8393.herostory.login.db.UserEntity;
import com.burning8393.herostory.model.User;
import com.burning8393.herostory.model.UserManager;
import com.burning8393.herostory.msg.GameMsgProtocol;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 用户登录指令处理器
 */
public class UserLoginCmdHandler implements ICmdHandler<GameMsgProtocol.UserLoginCmd>{

    /**
     * 日志对象
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(UserLoginCmdHandler.class);

    @Override
    public void handle(ChannelHandlerContext ctx, GameMsgProtocol.UserLoginCmd cmd) {
        if (null == ctx || null == cmd) {
            return;
        }
        String userName = cmd.getUserName();
        String password = cmd.getPassword();

        LOGGER.info(
            "用户登录， userName = {}, password = {} ",
            userName,
            password
        );


        UserEntity userEntity = null;
        try {
            userEntity = LoginService.getINSTANCE().userLogin(userName, password);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            return;
        }

        if (null == userEntity) {
            LOGGER.error("用户登陆失败, userName = {}", cmd.getUserName());
            return;
        }

        LOGGER.info(
                "用户登陆成功, userId = {}, userName = {}",
                userEntity.userId,
                userEntity.userName
        );

        // 构建新用户
        User newUser = new User();
        newUser.userId = userEntity.userId;
        newUser.userName = userEntity.userName;
        newUser.heroAvatar = userEntity.heroAvatar;
        newUser.currHp = 100;

        // 将用户加入用户管理器
        UserManager.addUser(newUser);

        // 将用户id 附着在用户信道上
        ctx.channel().attr(AttributeKey.valueOf("userId")).set(newUser.userId);

        GameMsgProtocol.UserLoginResult.Builder resultBuilder = GameMsgProtocol.UserLoginResult.newBuilder();
        resultBuilder.setUserId(newUser.userId);
        resultBuilder.setUserName(newUser.userName);
        resultBuilder.setHeroAvatar(newUser.heroAvatar);

        // 构建结果并发送
        GameMsgProtocol.UserLoginResult newResult = resultBuilder.build();
        BroadCaster.broadcast(newResult);
    }
}
