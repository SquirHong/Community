package com.hjs.community.controller;

import com.hjs.community.annotation.LoginRequired;
import com.hjs.community.entity.Comment;
import com.hjs.community.entity.Event;
import com.hjs.community.event.EventProducer;
import com.hjs.community.service.CommentService;
import com.hjs.community.service.DiscussPostService;
import com.hjs.community.util.CommunityConstant;
import com.hjs.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Date;

/**
 * @author hong
 * @create 2023-01-09 17:57
 */
@Controller
@RequestMapping("/comment")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private EventProducer eventProducer;

    @Autowired
    private DiscussPostService discussPostService;


    @LoginRequired
    @PostMapping("/add/{discussPostId}")
    public String addComment(@PathVariable("discussPostId") int id, Comment comment) {
        comment.setUserId(hostHolder.getUser().getId());
        comment.setStatus(0);
        comment.setCreateTime(new Date());

        commentService.addComment(comment);
        //做系统通知
        Event event = new Event()
                .setUserId(hostHolder.getUser().getId())
                .setTopic(CommunityConstant.TOPIC_COMMENT)
                .setEntityType(comment.getEntityType())
                .setEntityId(comment.getEntityId())
                .setData("postId", id);
        if (comment.getEntityType() == CommunityConstant.ENTITY_TYPE_POST) {
            event.setEntityUserId(discussPostService.findDiscussPostById(id).getUserId());
//            event.setEntityUserId(discussPostService.findDiscussPostById(comment.getEntityId()).getUserId());
        } else if (comment.getEntityType() == CommunityConstant.ENTITY_TYPE_COMMENT) {
            event.setEntityUserId(commentService.findCommentById(comment.getEntityId()).getUserId());
        }
        eventProducer.fireEvent(event);
        return "redirect:/discuss/detail/" + id;
    }


}
