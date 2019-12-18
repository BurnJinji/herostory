package com.burning8393.herostory.login;

import com.burning8393.herostory.MySqlSessionFactory;
import com.burning8393.herostory.login.db.IUserDao;
import com.burning8393.herostory.login.db.UserEntity;
import org.apache.ibatis.session.SqlSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
     * @return 用户实体
     */
    public UserEntity userLogin(String userName, String password) {
        if (null == userName
                || null == password) {
            return null;
        }

        try (SqlSession sqlSession = MySqlSessionFactory.openSession()) {
            // 获取dao对象
            IUserDao dao = sqlSession.getMapper(IUserDao.class);

            LOGGER.info("当前线程 = {}", Thread.currentThread().getName());

            // 根据用户名称获取用户实体
            UserEntity userEntity = dao.getUserByName(userName);

            if (null != userEntity) {
                // 判断用户密码
                if (!password.equals(userEntity.password)) {
                    LOGGER.error(
                            "用户密码错误， userId = {}, userName = {}",
                            userEntity.userId,
                            userName
                    );
                }
                throw new RuntimeException("用户密码错误");
            } else {
                // 如果用户实体为空，则新建用户
                userEntity = new UserEntity();
                userEntity.userName = userName;
                userEntity.password = password;
                userEntity.heroAvatar = "Hero_Shaman";

                // 将用户实体添加到数据库
                dao.insertInto(userEntity);
            }
            return userEntity;
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
            return null;
        }
    }
}
