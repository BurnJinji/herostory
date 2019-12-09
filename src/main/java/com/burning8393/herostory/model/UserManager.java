package com.burning8393.herostory.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * 用户管理器
 */
public class UserManager {
    /**
     * 用户字典
     */
    private static final Map<Integer, User> _userMap = new HashMap<>();

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
    public static void removeUserById(Integer id) {
        _userMap.remove(id);
    }

    /**
     * 列表用户
     * @return
     */
    public static Collection<User> listUser() {
        return _userMap.values();
    }
}
