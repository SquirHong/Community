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

}
