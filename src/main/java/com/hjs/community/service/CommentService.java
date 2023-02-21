package com.hjs.community.service;

import com.hjs.community.dao.CommentMapper;
import com.hjs.community.entity.Comment;
import com.hjs.community.util.CommunityConstant;
import com.hjs.community.util.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

/**
 * @author hong
 * @create 2023-01-08 19:15
 */
@Service
public class CommentService {

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private SensitiveFilter sensitiveFilter;

    @Autowired
    private DiscussPostService discussPostService;

    public List<Comment> findCommentByEntity(int entityType, int entityId, int offset, int limit) {
        return commentMapper.selectCommentByEntity(entityType, entityId, offset, limit);
    }

    public int getCommentCount(int entityType, int entityId) {
        return commentMapper.selectCountByEntity(entityType, entityId);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED)
    public int addComment(Comment comment) {
//这里做判断无意义 comment是springMvc生成的 并且是用来接受前端数据的
        //        if (comment == null){
//            throw new IllegalArgumentException("评论参数为空");
//        }
        comment.setContent(HtmlUtils.htmlEscape(sensitiveFilter.filter(comment.getContent())));
        int rows = commentMapper.insertComment(comment);
        //更新commentCount
        //这里 只给帖子评论 才会增加帖子的评论数量，而给帖子里的评论 评论 是不会增加帖子的评论数量
        if (comment.getEntityType() == CommunityConstant.ENTITY_TYPE_POST) {
            int count = commentMapper.selectCountByEntity(comment.getEntityType(), comment.getEntityId());
            discussPostService.updateCommentCount(comment.getEntityId(), count);
        }
        return rows;
    }

    public Comment findCommentById(int id) {
        return commentMapper.selectCommentById(id);
    }

    public List<Comment> findUserComments(int userId, int offset, int limit) {
        return commentMapper.selectCommentsByUser(userId, offset, limit);
    }

    public int findCountByUser(int userId) {
        return commentMapper.selectCountByUser(userId);
    }
}
