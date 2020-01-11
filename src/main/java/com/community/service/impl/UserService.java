package com.community.service.impl;

import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.community.mapper.UserMapper;
import com.community.service.IUserService;
import com.community.vo.User;
import org.springframework.stereotype.Service;

@Service
public class UserService extends ServiceImpl<UserMapper, User> implements IUserService {

    @Override
    public User selectUserById(int id) {
        return this.baseMapper.selectById(id);
    }
}
