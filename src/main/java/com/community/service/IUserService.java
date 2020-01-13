package com.community.service;

import com.baomidou.mybatisplus.service.IService;
import com.community.vo.User;

import java.util.Map;

public interface IUserService extends IService<User> {

    /**
     * 根据id查询User
     *
     * @param id
     * @return
     */
    User selectUserById(int id);

    /**
     * 注册新用户
     *
     * @param user
     * @return
     */
    Map<String, Object> register(User user);

    /**
     * 激活账号
     * @param userId
     * @param activtionCode
     * @return
     */
    int activtion(int userId, String activtionCode);
}
