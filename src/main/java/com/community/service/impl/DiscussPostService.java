package com.community.service.impl;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.community.mapper.DiscussPostMapper;
import com.community.service.IDiscussPostService;
import com.community.vo.DiscussPost;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.util.List;


@Service
public class DiscussPostService extends ServiceImpl<DiscussPostMapper, DiscussPost> implements IDiscussPostService {

    private static final Logger log= LoggerFactory.getLogger(DiscussPostService.class);

    /**
     * 社区首页帖子展示
     * @param userId
     * @param offset
     * @param limit
     * @return
     */
    @Override
    public List<DiscussPost> listDiscussPosts(int userId, int offset, int limit) {
        return this.baseMapper.selectDiscussPosts(userId,offset,limit);
    }


    /**
     * 获取未被拉黑的帖子数
     * @param userId
     * @return
     */
    public int getCount(int userId){
        if(userId==0){
            return this.baseMapper.selectCount(new EntityWrapper<DiscussPost>().ne("status",2));
        }else{
            return this.baseMapper.selectCount(new EntityWrapper<DiscussPost>().eq("user_id",userId).ne("status",2));
        }
    }
}
