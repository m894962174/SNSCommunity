package com.community.service.impl;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.community.mapper.UserMapper;
import com.community.service.IUserService;
import com.community.util.CommonUtil;
import com.community.util.MailClient;
import com.community.util.UserThreadLocal;
import com.community.vo.LoginTicket;
import com.community.vo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import com.community.util.CommonStatus;


@Service
public class UserService extends ServiceImpl<UserMapper, User> implements IUserService {

    @Autowired
    private LoginTicketService loginTicketService;

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


    @Override
    public User selectUserByUserName(String userName) {
        User user = new User();
        user.setUsername(userName);
        return this.selectOne(new EntityWrapper<User>().eq("username", userName));
    }

    /**
     * 注册新用户
     * BUG:邮件地址不存在怎么办？ && jsr303校验
     *
     * @param user
     * @return
     */
    @Override
    @Transactional
    public Map<String, Object> register(User user) {
        Map<String, Object> map = new HashMap<>();
        //唯一性校验
        User u = this.selectOne(new EntityWrapper<User>().eq("username", user.getUsername()));
        if (u != null) {
            map.put("usernameMsg", "该账号已存在！");
            return map;
        }
        u = this.selectOne(new EntityWrapper<User>().eq("email", user.getEmail()));
        if (u != null) {
            map.put("emailMsg", "该邮箱已被注册！");
            return map;
        }
        //添加新用户
        user.setSalt(CommonUtil.generateUUID().substring(0, 5));
        user.setPassword(CommonUtil.md5(user.getPassword() + user.getSalt()));
        user.setType(0);
        user.setStatus(0);
        user.setActivationCode(CommonUtil.generateUUID());
        user.setHeaderUrl(String.format("http://images.nowcoder.com/head/%dt.png", new Random().nextInt(1000)));
        user.setCreateTime(new Date());
        this.baseMapper.insert(user);
        //激活邮件
        Context context = new Context();
        context.setVariable("email", user.getEmail());
        String url = domain + contextPath + "/activation/" + user.getId() + "/" + user.getActivationCode();
        context.setVariable("url", url);
        //模板解析后的页面当作邮件正文
        mailClient.sendMail(user.getEmail(), "请激活您的账号", templateEngine.process("/mail/activation", context));
        return map;
    }

    /**
     * 激活账号
     *
     * @param userId
     * @param activtionCode
     * @return
     */
    @Override
    public int activtion(int userId, String activtionCode) {
        User user = this.selectUserById(userId);
        if (user == null) {
            return CommonStatus.USER_NOTEXIST;
        } else {
            if (user.getStatus() == 1) {
                return CommonStatus.ACTIVATION_REPEAT;
            } else if (user.getActivationCode().equals(activtionCode)) {
                user.setStatus(1);
                this.updateById(user);
                return CommonStatus.ACTIVATION_SUCCESS;
            } else {
                return CommonStatus.ACTIVATION_FAILURE;
            }
        }
    }

    /**
     * 根据ticket获取User
     *
     * @param param
     * @return
     */
    @Override
    public User selectUserByParam(String param) {
        LoginTicket loginTicket = loginTicketService.selectLoginTicketByTicket(param);
        return this.baseMapper.selectById(loginTicket.getUserId());
    }

    /**
     * 修改HeaderUrl
     *
     * @param user
     */
    @Override
    public void updateUserHeaderUrl(User user) {
        this.updateById(user);
    }

    /**
     * 修改密码
     *
     * @param OldPassWord
     * @param passWord
     */
    @Override
    public Map<String, Object> updatePassWord(String OldPassWord, String passWord, String checkPassWord) {
        Map<String, Object> map = new HashMap<>();
        if (OldPassWord == null || OldPassWord.equals("") || passWord == null || passWord.equals("")) {
            map.put("errorMsg", "密码不能为空！");
            return map;
        }
        User user = UserThreadLocal.getUser();
        String opwd = CommonUtil.md5(OldPassWord + user.getSalt());
        if (!opwd.equals(user.getPassword())) {
            map.put("errorMsg", "旧密码错误！");
            return map;
        }
        if (!checkPassWord.equals(passWord)) {
            map.put("checkMsg", "前后俩次输入不一致");
            return map;
        }
        user.setPassword(CommonUtil.md5(passWord + user.getSalt()));
        this.updateById(user);
        return null;
    }


}
