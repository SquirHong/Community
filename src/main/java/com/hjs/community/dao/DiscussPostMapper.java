package com.hjs.community.dao;

import com.hjs.community.entity.DiscussPost;
import com.hjs.community.entity.User;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author hong
 * @create 2022-12-30 17:29
 */
@Repository
public interface DiscussPostMapper {

    /**
     *
     * @param userId
     * @param offset 起始行号
     * @param limit
     * @return
     */
    List<DiscussPost> selectDiscussPosts(int userId,int offset,int limit);

    int selectDiscussPostRows(@Param("userId") int userId);

    int insertDiscussPost(DiscussPost discussPost);
    //帖子自己有自己的id
    DiscussPost selectDiscussPostById(int id);

    int updateDiscussPostRows(int id,int commentCount);

}
