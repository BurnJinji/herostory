package com.burning8393.herostory.cmdhandler;

import com.burning8393.herostory.login.LoginService;
import com.burning8393.herostory.model.User;
import com.burning8393.herostory.model.UserManager;
import com.burning8393.herostory.msg.GameMsgProtocol;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 用户登录指令处理器
 */
public class UserLoginCmdHandler implements ICmdHandler<GameMsgProtocol.UserLoginCmd>{

    /**
     * 日志对象
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(UserLoginCmdHandler.class);

    /**
     * 用户登录状态字典，防止用户连点登录按钮
     */
    private static final Map<String, Long> USER_LOGIN_STATE_MAP = new ConcurrentHashMap<>();

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

        // 实现清理超时的登录时间
        clearTimeoutLoginTime(USER_LOGIN_STATE_MAP);

        if (USER_LOGIN_STATE_MAP.containsKey(userName)) {
            return;
        }

        // 获取系统当前时间
        final long currTime = System.currentTimeMillis();
        // 设置用户登录时间
        USER_LOGIN_STATE_MAP.putIfAbsent(
            userName, currTime
        );

        LOGGER.info("当前执行线程 = {}", Thread.currentThread().getName());

        // 执行用户登录
        LoginService.getINSTANCE().userLogin(userName, password, (userEntity) -> {
            // 移除用户登录状态
            USER_LOGIN_STATE_MAP.remove(userName);

            if (null == userEntity) {
                LOGGER.error("用户登陆失败, userName = {}", cmd.getUserName());
                return null;
            }

            LOGGER.info(
                    "用户登陆成功, userId = {}, userName = {}",
                    userEntity.userId,
                    userEntity.userName
            );

            LOGGER.info("当前线程 = {}", Thread.currentThread().getName());
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
            ctx.writeAndFlush(newResult);

            return null;
        });


    }

    /**
     * 清理超时的用户登录时间
     * @param userLoginStateMap 用户登录时间字典
     */
    private void clearTimeoutLoginTime(Map<String, Long> userLoginStateMap) {
        if (null == userLoginStateMap ||
                userLoginStateMap.isEmpty()) {
            return;
        }
        // 获取系统时间
        final long currTime = System.currentTimeMillis();
        // 获取迭代器
        Iterator<String> it = userLoginStateMap.keySet().iterator();

        while (it.hasNext()) {
            // 根据用户名称获取登陆时间
            String userName = it.next();
            Long loginTime = userLoginStateMap.get(userName);

            if (null == loginTime
                    || (currTime - loginTime) > 5000) {

                // 如果已经超时
                it.remove();
            }
        }
    }
}
