package com.burning8393.herostory.login;

import com.burning8393.herostory.MySqlSessionFactory;
import com.burning8393.herostory.async.AsyncOperationProcessor;
import com.burning8393.herostory.async.IAsyncOperation;
import com.burning8393.herostory.login.db.IUserDao;
import com.burning8393.herostory.login.db.UserEntity;
import org.apache.ibatis.session.SqlSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Function;

/**
 * 登录服务
 */
public class LoginService {
    /**
     * 日志对象
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(LoginService.class);

    /**
     * 单例对象
     */
    private static final LoginService INSTANCE = new LoginService();

    /**
     * 私有化类默认构造器
     */
    private LoginService() {
    }

    /**
     * 获取单例对象
     *
     * @return 单例对象
     */
    public static LoginService getINSTANCE() {
        return INSTANCE;
    }

    /**
     * 用户登录
     * @param userName 用户名称
     * @param password 用户密码
     * @param callback 回调函数
     * @return 用户实体
     */
    public void userLogin(String userName, String password, Function<UserEntity, Void> callback) {
        if (null == userName
                || null == password) {
            return;
        }

        // 创建异步操纵
        AysncGetUserByName asyncOp = new AysncGetUserByName(userName, password) {
            @Override
            public void doFinish() {
                if (callback != null) {
                    // 执行回调函数
                    callback.apply(this.getUserEntity());
                }
            }
        };

        // 执行异步操纵
        AsyncOperationProcessor.getINSTANCE().process(asyncOp);

    }

    /**
     * 异步方式获取用户
     */
    private class AysncGetUserByName implements IAsyncOperation {

        /**
         * 用户名称
         */
        private final String userName;

        /**
         * 密码
         */
        private final String password;

        /**
         * 用户实体
         */
        private UserEntity _userEntity = null;

        /**
         * 类参数构造器
         *
         * @param userName 用户名称
         * @param password 密码
         */
        public AysncGetUserByName(String userName, String password) {
            if (null == userName || null == password) {
                throw new IllegalArgumentException();
            }
            this.userName = userName;
            this.password = password;
        }

        /**
         * 获取用户实体
         *
         * @return 用户实体
         */
        public UserEntity getUserEntity() {
            return _userEntity;
        }

        @Override
        public int getBindId() {
            return userName.charAt(userName.length() - 1);
        }

        @Override
        public void doAsync() {
            try (SqlSession sqlSession = MySqlSessionFactory.openSession()) {
                // 获取dao对象
                IUserDao dao = sqlSession.getMapper(IUserDao.class);

                LOGGER.info("当前线程 = {}", Thread.currentThread().getName());

                // 根据用户名称获取用户实体
                UserEntity userEntity = dao.getUserByName(userName);

                if (null != userEntity) {
                    // 判断用户密码
                    if (!password.equals(userEntity.password)) {
                        // 用户密码错误
                        LOGGER.error(
                                "用户密码错误， userId = {}, userName = {}",
                                userEntity.userId,
                                userName
                        );
                        return;
                    }
                } else {
                    // 如果用户实体为空，则新建用户
                    userEntity = new UserEntity();
                    userEntity.userName = userName;
                    userEntity.password = password;
                    userEntity.heroAvatar = "Hero_Shaman";

                    // 将用户实体添加到数据库
                    dao.insertInto(userEntity);
                }

                this._userEntity = userEntity;
            } catch (Exception ex) {
                LOGGER.error(ex.getMessage(), ex);
            }
        }
    }
}
