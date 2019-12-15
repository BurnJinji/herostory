package com.burning8393.herostory.model;

/**
 * 用户
 */
public class User {
    /**
     * 用户id
     */
    public int userId;

    /**
     * 英雄形象
     */
    public String heroAvatar;

    /**
     * 当前用户血量
     */
    public int currHp;

    /**
     * 移动状态
     */
    public final MoveState moveState = new MoveState();

}
