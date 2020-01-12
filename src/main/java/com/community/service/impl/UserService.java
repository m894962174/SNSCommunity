package com.community.service.impl;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.community.mapper.UserMapper;
import com.community.service.IUserService;
import com.community.util.CommonUtil;
import com.community.util.MailClient;
import com.community.vo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class UserService extends ServiceImpl<UserMapper, User> implements IUserService {

    @Autowired
    private MailClient mailClient;

    @Autowired
    private TemplateEngine templateEngine;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;


    /**
     * 查询User
     *
     * @param id
     * @return
     */
    @Override
    public User selectUserById(int id) {
        return this.baseMapper.selectById(id);
    }

    /**
     * 注册新用户
     * BUG:邮件地址不存在怎么办？ && jsr303校验
     * @param user
     * @return
     */
    @Override
    public Map<String, Object> register(User user) {
        Map<String,Object> map=new HashMap<>();
        //唯一性校验
        User u = this.selectOne(new EntityWrapper<User>().eq("username", user.getUsername()));
        if (u !=null) {
            map.put("usernameMsg","该账号已存在！");
            return map;
        }
        u=this.selectOne(new EntityWrapper<User>().eq("email", user.getEmail()));
        if(u!=null) {
            map.put("emailMsg","该邮箱已被注册！");
            return map;
        }
        //添加新用户
        user.setSalt(CommonUtil.generateUUID().substring(0,5));
        user.setPassword(CommonUtil.md5(user.getPassword()+user.getSalt()));
        user.setType(0);
        user.setStatus(0);
        user.setActivationCode(CommonUtil.generateUUID());
        user.setCreateTime(new Date());
        this.baseMapper.insert(user);
        //激活邮件
        Context context=new Context();
        context.setVariable("email",user.getEmail());
        String url=domain+contextPath+"/activation/"+user.getId()+"/"+user.getActivationCode();
        context.setVariable("url",url);
        //模板解析后的页面当作邮件正文
        mailClient.sendMail(user.getEmail(),"请激活您的账号",templateEngine.process("/mail/activation",context));
        return map;
    }
}
