package com.hjs.community.controller;

import com.hjs.community.annotation.LoginRequired;
import com.hjs.community.entity.Event;
import com.hjs.community.entity.Page;
import com.hjs.community.entity.User;
import com.hjs.community.event.EventProducer;
import com.hjs.community.service.FollowService;
import com.hjs.community.service.UserService;
import com.hjs.community.util.CommunityConstant;
import com.hjs.community.util.CommunityUtil;
import com.hjs.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

/**
 * @author hong
 * @create 2023-01-20 16:48
 */
@Controller
public class FollowController {

    @Autowired
    private FollowService followService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private UserService userService;
    @Autowired
    private EventProducer eventProducer;


    @PostMapping("/follow")
    @ResponseBody
    @LoginRequired
    public String follow(int entityId, int entityType) {
        User user = hostHolder.getUser();
        followService.follow(user.getId(), entityType, entityId);

        //关注用户 站内通知被关注的用户
        Event event = new Event()
                .setUserId(user.getId())
                .setTopic(CommunityConstant.TOPIC_FOLLOW)
                .setEntityType(entityType)
                .setEntityId(entityId)
                .setEntityUserId(entityId);
        eventProducer.fireEvent(event);

        return CommunityUtil.getJsonString(0, "已关注!");
    }

    @PostMapping("/unfollow")
    @ResponseBody
    public String unfollow(int entityId, int entityType) {
        User user = hostHolder.getUser();
        followService.unfollow(user.getId(), entityType, entityId);
        return CommunityUtil.getJsonString(0, "已取消关注!");
    }

    @GetMapping("/followees/{userId}")
    public String getFollowees(@PathVariable("userId") int userId, Model model, Page page) {
        User user = userService.findUserById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        model.addAttribute("user", user);

        page.setLimit(5);
        page.setPath("/followees/" + userId);
        page.setRows((int) followService.findFolloweeCount(userId, CommunityConstant.ENTITY_TYPE_USER));

        List<Map<String, Object>> followees = followService.findFollowees(userId, page.getOffset(), page.getLimit());
        if (followees != null) {
            for (Map<String, Object> map : followees) {
                User u = (User) map.get("user");
                map.put("hasFollowed",
                        followService.hasFollowed(hostHolder.getUser().getId(), CommunityConstant.ENTITY_TYPE_USER, u.getId()));
            }
        }
        model.addAttribute("userList",followees);

        return "/site/followee";
    }

    @GetMapping("/followers/{userId}")
    public String getFollowers(@PathVariable("userId") int userId, Model model, Page page) {
        User user = userService.findUserById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        model.addAttribute("user", user);

        page.setLimit(5);
        page.setPath("/followers/" + userId);
        page.setRows((int) followService.findFollowerCount(userId,CommunityConstant.ENTITY_TYPE_USER));

        List<Map<String, Object>> followers = followService.findFollowers(userId, page.getOffset(), page.getLimit());
        if (followers != null) {
            for (Map<String, Object> map : followers) {
                User u = (User) map.get("user");
                map.put("hasFollowed",
                        followService.hasFollowed(hostHolder.getUser().getId(), CommunityConstant.ENTITY_TYPE_USER, u.getId()));
            }
        }
        model.addAttribute("userList",followers);

        return "/site/follower";
    }



}
