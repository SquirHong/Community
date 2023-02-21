package com.hjs.community.controller;

import com.hjs.community.annotation.LoginRequired;
import com.hjs.community.entity.Comment;
import com.hjs.community.entity.DiscussPost;
import com.hjs.community.entity.Page;
import com.hjs.community.entity.User;
import com.hjs.community.service.CommentService;
import com.hjs.community.service.DiscussPostService;
import com.hjs.community.service.LikeService;
import com.hjs.community.service.UserService;
import com.hjs.community.util.CommunityConstant;
import com.hjs.community.util.CommunityUtil;
import com.hjs.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 与帖子相关的处理器
 *
 * @author hong
 * @create 2023-01-08 13:21
 */
@Controller
@RequestMapping("/discuss")
public class DiscussPostController {

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private UserService userService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private LikeService likeService;

    // TODO: 2023/1/8  这里可以优化一下 因为前端在显示index.html的时候 已经隐藏了发布帖子按钮
    @LoginRequired
    @PostMapping("/add")
    @ResponseBody
    public String addDiscussPost(String title, String content) {
        DiscussPost discussPost = new DiscussPost();
        discussPost.setUserId(hostHolder.getUser().getId());
        discussPost.setTitle(title);
        discussPost.setContent(content);
        discussPost.setCreateTime(new Date());
        discussPostService.addDiscussPost(discussPost);
        // TODO: 2023/1/8 这里应该设置code值为200
        return CommunityUtil.getJsonString(0, "贴子发布成功!");
    }

    //一般通过id查数据，请求路径最好带上id号
    @LoginRequired
    @GetMapping("/detail/{discussPostId}")
    public String getDiscussPost(@PathVariable("discussPostId") int id, Model model, Page page) {
        //只要是实体类型并且在controller的参数上， springmvc调用完controller方法后，会自动将其add到model中
        // 帖子
        DiscussPost post = discussPostService.findDiscussPostById(id);
        model.addAttribute("post", post);
// TODO: 2023/1/8 前端要显示帖子的发布人的名字 第一种方法 建一个含有冗余name字段的扩充discussPost的类，第二种方法 再用userservice根据id查一次
        // 作者
        User user = userService.findUserById(post.getUserId());
        model.addAttribute("user", user);
        //点赞数量
        long likeCount = likeService.findEntityLikeCount(CommunityConstant.ENTITY_TYPE_POST, id);
        model.addAttribute("likeCount", likeCount);
        //点赞状态
        User holderUser = hostHolder.getUser();
        int likeStatus = holderUser != null ?
                likeService.findEntityLikeStatus(holderUser.getId(), CommunityConstant.ENTITY_TYPE_POST, id) : 0;
        model.addAttribute("likeStatus", likeStatus);
        //处理评论分页
        page.setLimit(5);
        page.setPath("/discuss/detail/" + id);
        page.setRows(post.getCommentCount());

        // 评论: 给帖子的评论
        // 回复: 给评论的评论
        // 评论列表
        List<Comment> commentList = commentService.findCommentByEntity(
                1, post.getId(), page.getOffset(), page.getLimit());
        // TODO: 2023/1/8 同本controller一样，需构造一个中间类VO来承载数据
        // 评论VO列表
        List<Map<String, Object>> commentVoList = null;
        if (commentList != null) {
            commentVoList = new ArrayList<>();
            for (Comment comment : commentList) {
                // 评论VO
                Map<String, Object> commentVo = new HashMap<>();
                commentVo.put("comment", comment);
                commentVo.put("user", userService.findUserById(comment.getUserId()));
                //点赞数量
                likeCount = likeService.findEntityLikeCount(CommunityConstant.ENTITY_TYPE_COMMENT, comment.getId());
                commentVo.put("likeCount", likeCount);
                likeStatus = holderUser != null ?
                        likeService.findEntityLikeStatus(holderUser.getId(), CommunityConstant.ENTITY_TYPE_COMMENT, comment.getId()) : 0;
                commentVo.put("likeStatus", likeStatus);
                //回复列表
                List<Comment> replyList = commentService.findCommentByEntity(
                        CommunityConstant.ENTITY_TYPE_COMMENT, comment.getId(), 0, Integer.MAX_VALUE);
                //回复Vo列表
                List<Map<String, Object>> replyVoList = null;
                if (replyList != null) {
                    replyVoList = new ArrayList<>();
                    for (Comment reply : replyList) {
                        Map<String, Object> replyVo = new HashMap<>();
                        replyVo.put("reply", reply);
                        replyVo.put("user", userService.findUserById(reply.getUserId()));
                        //回复目标
                        User target = reply.getTargetId() == 0 ? null : userService.findUserById(reply.getTargetId());
                        replyVo.put("target", target);

                        //点赞数量
                        likeCount = likeService.findEntityLikeCount(CommunityConstant.ENTITY_TYPE_COMMENT, reply.getId());
                        replyVo.put("likeCount", likeCount);
                        likeStatus = holderUser != null ?
                                likeService.findEntityLikeStatus(holderUser.getId(), CommunityConstant.ENTITY_TYPE_COMMENT, reply.getId()) : 0;
                        replyVo.put("likeStatus", likeStatus);

                        replyVoList.add(replyVo);
                    }
                }
                commentVo.put("replyVoList", replyVoList);
                int replyCount = commentService.getCommentCount(CommunityConstant.ENTITY_TYPE_COMMENT, comment.getId());
                commentVo.put("replyCount", replyCount);
                commentVoList.add(commentVo);
            }
        }
        model.addAttribute("comments", commentVoList);
        return "/site/discuss-detail";
    }

}
