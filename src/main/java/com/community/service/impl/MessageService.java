package com.community.service.impl;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.community.mapper.MessageMapper;
import com.community.service.IMessageService;
import com.community.vo.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 *
 * @Auther: majhp
 * @Date: 2020/01/20/13:57
 * @Description:
 */
@Service
public class MessageService extends ServiceImpl<MessageMapper, Message> implements IMessageService {

    @Autowired
    MessageMapper messageMapper;

    /**
     * 查询当前用户下的所有会话
     * @param userId
     * @param offset
     * @param limit
     * @return
     */
    public List<Message> selectConversations(int userId, int offset, int limit){
        return messageMapper.selectConversations(userId, offset, limit);
    }

    /**
     * 查询当前用户的会话总数
     * @param userId
     * @return
     */
    public int selectConversationsCount(int userId){
        return messageMapper.selectConversationCount(userId);
    }

    /**
     * 查询当前会话下的私信列表
     * @param conversationId
     * @param offset
     * @param limit
     * @return
     */
    public List<Message> selectConversationLetters(String conversationId, int offset, int limit){
        return messageMapper.selectLetters(conversationId, offset, limit);
    }

    /**
     * 查询某个会话所包含的私信数量
     * @param conversationId
     * @return
     */
    @Override
    public int selectLetterCount(String conversationId) {
        return this.selectCount(new EntityWrapper<Message>().eq("conversation_id",conversationId).ne("status",2).ne("from_id",1));
    }

    /**
     * 查询未读会话数
     * @param userId
     * @param conversationId 带此参数查询某会话的未读数，不带则查询某用户下总未读
     * @return
     */
    @Override
    public int selectLetterUnreadCount(int userId, String conversationId) {
        if(conversationId!=null) {
            return this.selectCount(new EntityWrapper<Message>().eq("to_id",userId).eq("conversation_id",conversationId).eq("status",0).ne("from_id",1));
        }
        return this.selectCount(new EntityWrapper<Message>().eq("to_id",userId).eq("status",0).ne("from_id",1));
    }
}
