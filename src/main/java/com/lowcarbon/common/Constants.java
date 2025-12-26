package com.lowcarbon.common;

public class Constants {
    // 用户角色
    public static final String ROLE_USER = "USER";
    public static final String ROLE_ADMIN = "ADMIN";

    // 用户状态
    public static final Integer STATUS_NORMAL = 1;
    public static final Integer STATUS_DISABLED = 0;

    // JWT Token前缀
    public static final String TOKEN_PREFIX = "Bearer ";

    // 积分规则：每减排0.1kg获得1积分
    public static final double POINTS_PER_REDUCTION = 10.0; // 每kg减排量 = 10积分
}

