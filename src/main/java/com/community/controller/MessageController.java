package com.community.controller;

import com.community.service.IUserService;
import com.community.service.impl.MessageService;
import com.community.util.UserThreadLocal;
import com.community.vo.Message;
import com.community.vo.Page;
import com.community.vo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 *
 * @Auther: majhp
 * @Date: 2020/01/20/14:25
 * @Description:
 */
@Controller
public class MessageController {

    //此处不用IMessageService接口接收，通常用接口接收是考虑到可能会有对于接口的多种实现，体现了程序的可扩展性，
    // 此工程可能不太会有这种情形，因此以后都采用Impl接收的方式，便于mybatis-plus2.0的使用
    @Autowired
    MessageService messageService;

    @Autowired
    IUserService userService;


    // 私信列表
    @RequestMapping(path = "/letter/list", method = RequestMethod.GET)
    public String getLetterList(Model model, Page page) {
        User user = UserThreadLocal.getUser();
        page.setPath("/letter/list");
        page.setLimit(10);
        page.setRows(messageService.selectConversationsCount(user.getId()));
        List<Message> conversationList = messageService.selectConversations(user.getId(), page.getOffset(), page.getLimit());
        List<Map<String,Object>> conversations = new ArrayList<>();
        if (conversationList != null) {
            for (Message message : conversationList) {
                Map<String, Object> map = new HashMap<>();
                map.put("conversation", message);
                map.put("letterCount", messageService.selectLetterCount(message.getConversationId()));
                map.put("unreadCount", messageService.selectLetterUnreadCount(user.getId(), message.getConversationId()));
                int targetId = user.getId() == message.getToId() ? message.getFromId() : message.getToId();
                map.put("target",userService.selectUserById(targetId));
                conversations.add(map);
            }
        }
        model.addAttribute("conversations", conversations);

        // 查询未读消息数量
        int letterUnreadCount = messageService.selectLetterUnreadCount(user.getId(),null);
        model.addAttribute("letterUnreadCount", letterUnreadCount);
        return "/site/letter";
    }
}
