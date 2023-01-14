package com.hjs.community.controller;

import com.hjs.community.entity.User;
import com.hjs.community.service.LikeService;
import com.hjs.community.util.CommunityUtil;
import com.hjs.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

/**
 * @author hong
 * @create 2023-01-12 17:09
 */
@Controller
public class LikeController {

    @Autowired
    private LikeService likeService;

    @Autowired
    private HostHolder hostHolder;

    @ResponseBody
    @PostMapping("/like")
    public String like(int entityType, int entityId,int entityUserId) {
        User user = hostHolder.getUser();
        likeService.like(user.getId(), entityType, entityId,entityUserId);
        long count = likeService.findEntityLikeCount(entityType, entityId);
        int likeStatus = likeService.findEntityLikeStatus(user.getId(), entityType, entityId);
        Map<String, Object> map = new HashMap<>();
        map.put("likeCount", count);
        map.put("likeStatus", likeStatus);
        return CommunityUtil.getJsonString(0, likeStatus == 1 ? "点赞" : "取消点赞", map);
    }

}
