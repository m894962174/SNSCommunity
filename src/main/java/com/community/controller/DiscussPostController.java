package com.community.controller;
import com.alibaba.fastjson.JSONObject;
import com.community.annotation.CheckLogin;
import com.community.service.IDiscussPostService;
import com.community.service.IUserService;
import com.community.util.CommonUtil;
import com.community.util.UserThreadLocal;
import com.community.vo.DiscussPost;
import com.community.vo.Page;
import com.community.vo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;


@Controller
public class DiscussPostController {

    @Autowired
    IDiscussPostService discussPostService;

    @Autowired
    IUserService userService;


    @RequestMapping(path = "/index", method = RequestMethod.GET)
    public String getIndexPage(Model model, Page page) {
        // 方法调用栈,SpringMVC会自动实例化Model和Page,并将Page注入Model.
        // 所以,在thymeleaf中可以直接访问Page对象中的数据.
        page.setRows(discussPostService.getCount(0));
        page.setPath("/index");
        List<DiscussPost> list = discussPostService.listDiscussPosts(0, page.getOffset(), page.getLimit());
        List<Map<String, Object>> discussPosts = new ArrayList<>();
        if (list != null) {
            for (DiscussPost post : list) {
                Map<String, Object> map = new HashMap<>();
                map.put("post", post);
                User user = userService.selectUserById(post.getUserId());
                map.put("user", user);
                discussPosts.add(map);
            }
        }
        model.addAttribute("discussPosts", discussPosts);
        return "/index";
    }

    /**
     * 发布帖子
     * @param title
     * @param content
     * @return
     */
    @CheckLogin
    @RequestMapping(value = "/addDiscussPost",method = RequestMethod.POST)
    @ResponseBody
    public String addDiscussPost(String title, String content){
        User user= UserThreadLocal.getUser();
        if (user == null) {
            return CommonUtil.getJSONString(403, "你还没有登录哦!");
        }
        DiscussPost post = new DiscussPost();
        post.setUserId(user.getId());
        post.setTitle(title);
        post.setContent(content);
        post.setCreateTime(new Date());
        post.setStatus(0);
        post.setType(0);
        post.setCommentCount(0);
        discussPostService.add(post);
        return CommonUtil.getJSONString(0, "发布成功!");
    }

}
