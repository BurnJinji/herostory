package com.burning8393.herostory.model;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 用户管理器
 */
public final class UserManager {
    /**
     * 用户字典
     */
    private static final Map<Integer, User> _userMap = new ConcurrentHashMap<>();

    /**
     * 私有化类默认构造器
     */
    private UserManager() {
    }

    /**
     * 添加用户
     * @param user
     */
    public static void addUser(User user) {
        if (null != user) {
            _userMap.put(user.userId, user);
        }
    }

    /**
     * 根据用户 id 移除用户
     * @param id
     */
    public static void removeUserById(int id) {
        _userMap.remove(id);
    }

    /**
     * 列表用户
     * @return
     */
    public static Collection<User> listUser() {
        return _userMap.values();
    }

    /**
     * 根据用户id获取用户
     * @param userId 用户id
     * @return
     */
    public static User getUser(int userId) {
        return _userMap.get(userId);
    }
}
