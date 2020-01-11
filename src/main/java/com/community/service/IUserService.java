package com.community.service;

import com.baomidou.mybatisplus.service.IService;
import com.community.vo.User;

public interface IUserService extends IService<User> {

    /**
     * 根据id查询User
     * @param id
     * @return
     */
    User selectUserById(int id);
}
