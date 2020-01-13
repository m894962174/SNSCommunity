package com.community.util;

public interface ActivationStatus {

    /**
     * 激活成功
     */
    static int ACTIVATION_SUCCESS = 0;

    /**
     * 重复激活
     */
    static int ACTIVATION_REPEAT = 1;

    /**
     * 激活失败
     */
    static int ACTIVATION_FAILURE = 2;

    /**
     * 用户不存在
     */
    static int USER_NOTEXIST=-1;
}
