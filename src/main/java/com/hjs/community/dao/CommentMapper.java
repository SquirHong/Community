package com.hjs.community.dao;

import com.hjs.community.entity.Comment;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author hong
 * @create 2023-01-08 17:15
 */
@Repository
public interface CommentMapper {

    List<Comment> selectCommentByEntity(int entityType,int entityId,int offset,int limit);

    int selectCountByEntity(int entityType,int entityId);

    int insertComment(Comment comment);

    Comment selectCommentById(int id);

    /**
     * 查询某用户对帖子回复的评论，只对帖子，不对帖子评论
     * @param userId
     * @param offset
     * @param limit
     * @return
     */
    List<Comment> selectCommentsByUser(int userId, int offset, int limit);

    /**
     * 查询某用户对帖子回复的评论的数量，只对帖子，不对帖子评论
     * @param userId
     * @return
     */
    int selectCountByUser(int userId);

}
