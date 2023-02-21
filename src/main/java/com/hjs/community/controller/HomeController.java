package com.hjs.community.controller;

import com.hjs.community.entity.DiscussPost;
import com.hjs.community.entity.Page;
import com.hjs.community.entity.User;
import com.hjs.community.service.DiscussPostService;
import com.hjs.community.service.LikeService;
import com.hjs.community.service.UserService;
import com.hjs.community.util.CommunityConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author hong
 * @create 2022-12-30 19:51
 */
@Controller
public class HomeController {
    @Autowired
    private UserService userService;
    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private LikeService likeService;

    @GetMapping("/index")
    public String getIndexPage(Model model, Page page) {
        Integer[] myArray = {1, 2, 3};
        List<Integer> collect = Arrays.stream(myArray).collect(Collectors.toList());

        page.setRows(discussPostService.findDiscussPostRows(0));
        page.setPath("/index");
        List<DiscussPost> list = discussPostService.findDiscussPosts(0, page.getOffset(), page.getLimit());
        List<Map<String, Object>> discussPosts = new LinkedList<>();
        if (list != null) {
            for (DiscussPost dp : list) {
                Map<String, Object> map = new HashMap<>();
                map.put("post", dp);
                User user = userService.findUserById(dp.getUserId());
                map.put("user", user);
                long likeCount = likeService.findEntityLikeCount(CommunityConstant.ENTITY_TYPE_POST, dp.getId());
                map.put("likeCount", likeCount);
                discussPosts.add(map);
            }
        }
        //可以省略这一步
//        model.addAttribute("page",page);
        model.addAttribute("discussPosts", discussPosts);
        return "index";
    }

    @GetMapping("/error")
    public String getErrorPage() {
        return "/error/500";
    }


}
