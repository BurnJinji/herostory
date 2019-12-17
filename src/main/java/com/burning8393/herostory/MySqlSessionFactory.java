package com.burning8393.herostory;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;


/**
 * MySql 会话工厂
 */
public final class MySqlSessionFactory {

    /**
     * Mybaits sql 会话工厂
     */
    private static SqlSessionFactory _sqlSessionFactory;

    /**
     * 私有化类默认构造器
     */
    private MySqlSessionFactory() {

    }

    /**
     * 初始化
     */
    public static void init() {
        try {
            _sqlSessionFactory = (new SqlSessionFactoryBuilder()).build(
                    Resources.getResourceAsStream("MybatisConfig.xml")
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 开启 MySql 会话
     * @return MySql 会话
     */
    public static SqlSession openSession() {
        if (null == _sqlSessionFactory) {
            throw new RuntimeException("_sqlSessionFactory 尚未初始化");
        }
        return _sqlSessionFactory.openSession(true);
    }
}