package com.hjs.community.controller;

import com.hjs.community.annotation.LoginRequired;
import com.hjs.community.entity.Comment;
import com.hjs.community.entity.DiscussPost;
import com.hjs.community.entity.Page;
import com.hjs.community.entity.User;
import com.hjs.community.entity.vo.post.DiscussPostQueryVo;
import com.hjs.community.service.*;
import com.hjs.community.util.CommunityConstant;
import com.hjs.community.util.CommunityUtil;
import com.hjs.community.util.HostHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author hong
 * @create 2023-01-04 21:42
 */
@Controller
@RequestMapping("/user")
public class UserController {

    @Value("${community.path.upload}")
    private String upload;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private LikeService likeService;

    @Autowired
    private FollowService followService;

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private CommentService commentService;

    public static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @LoginRequired
    @GetMapping("/setting")
    public String getSettingPage() {
        return "/site/setting";
    }

    @LoginRequired
    @PostMapping("/upload")
    public String uploadHeader(MultipartFile headerImage, Model model) {
        if (headerImage == null) {
            model.addAttribute("error", "您还没有上传文件");
            return "/site/setting";
        }

        String filename = headerImage.getOriginalFilename();
        String uuid = CommunityUtil.generateUUID();
        filename = uuid + filename;

//        String timeUrl = new DateTime().toString("yyyy-MM/dd");
//        filename = timeUrl + "/" +filename;

        File dest = new File(upload + "/" + filename);

        try {
            headerImage.transferTo(dest);
        } catch (IOException e) {
            logger.error("上传文件失败：" + e.getMessage());
            throw new RuntimeException("上传文件失败", e);
        }
        User user = hostHolder.getUser();
        String url = domain + contextPath + "/user/header/" + filename;
        userService.updateHeader(user.getId(), url);
        return "redirect:/index";
    }

    @GetMapping("/header/{fileName}")
    public void getHeader(@PathVariable("fileName") String filename, HttpServletResponse response) {
        filename = upload + "/" + filename;
        String suffix = filename.substring(filename.lastIndexOf(".") + 1);
        response.setContentType("image/" + suffix);
        try (FileInputStream fileInputStream = new FileInputStream(filename); ServletOutputStream outputStream = response.getOutputStream()) {
            byte[] buffer = new byte[1024];
            int b = 0;
            while ((b = fileInputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, b);
            }
        } catch (IOException e) {
            logger.error("读取头像失败" + e.getMessage());
        }
    }

    @PostMapping("/updatePassword")
    public String updatePassword(Model model, String oldPassword, String newPassword) {
        User user = hostHolder.getUser();
        Map<String, Object> map = userService.updatePassword(user, oldPassword, newPassword);
        if (map.containsKey("oldPasswordMsg")) {
            model.addAttribute("oldPasswordMsg", map.get("oldPasswordMsg"));
            return "/site/setting";
        }
        if (map.containsKey("newPasswordMsg")) {
            model.addAttribute("newPasswordMsg", map.get("newPasswordMsg"));
            return "/site/setting";
        }
        // TODO: 2023/1/5 考虑这里要不要修改host holder值

        return "redirect:/logout";
    }

    //个人主页
    @LoginRequired
    @GetMapping("/profile/{id}")
    public String getProfilePage(@PathVariable("id") int id, Model model) {
        User user = userService.findUserById(id);
        if (user == null) {
            throw new IllegalArgumentException("该用户不存在");
        }
        //用户基本信息
        model.addAttribute("user", user);
        int likeCount = likeService.findUserLikeCount(id);
        model.addAttribute("likeCount", likeCount);

        //关注数量
        long followeeCount = followService.findFolloweeCount(id, CommunityConstant.ENTITY_TYPE_USER);
        model.addAttribute("followeeCount", followeeCount);
        //粉丝数量
        long followerCount = followService.findFollowerCount(id, CommunityConstant.ENTITY_TYPE_USER);
        model.addAttribute("followerCount", followerCount);
        //是否已经关注
        boolean hasFollowed = followService.hasFollowed(hostHolder.getUser().getId(), CommunityConstant.ENTITY_TYPE_USER, id);
        model.addAttribute("hasFollowed", hasFollowed);

        return "/site/profile";
    }

    //查询某用户曾经发布的帖子
    @GetMapping("/mypost/{userId}")
    public String getPostsByUserId(@PathVariable("userId") int userId, Page page, Model model) {
        User user = userService.findUserById(userId);
        if (user == null) {
            throw new RuntimeException("该用户不存在!");
        }
        model.addAttribute("user", user);

        page.setPath("/user/mypost/" + userId);
        page.setLimit(5);
        page.setRows(discussPostService.findDiscussPostRows(userId));

        List<DiscussPost> discussPosts = discussPostService.findDiscussPosts(userId, page.getOffset(), page.getLimit());
        List<Map<String, Object>> postList = new ArrayList<>();
        if (discussPosts != null) {
            for (DiscussPost discussPost : discussPosts) {
                Map<String, Object> map = new HashMap<>();
                map.put("discussPost", discussPost);
                map.put("likeCount", likeService.findEntityLikeCount(CommunityConstant.ENTITY_TYPE_POST, discussPost.getId()));
                postList.add(map);
            }
        }
        model.addAttribute("postList", postList);

        return "/site/my-post";
    }

    @GetMapping("/myreply/{userId}")
    public String getMyreply(@PathVariable("userId") int userId, Model model, Page page) {
        User user = userService.findUserById(userId);
        if (user == null) {
            throw new RuntimeException("该用户不存在");
        }
        model.addAttribute("user", user);
        // TODO: 2023/1/24 这里考虑一下是否可以连表查   Comment带上post的title
        // 分页信息
        page.setPath("/user/myreply/" + userId);
        page.setRows(commentService.findCountByUser(userId));
        page.setLimit(5);
        List<Comment> userComments = commentService.findUserComments(userId, page.getOffset(), page.getLimit());
        List<Map<String, Object>> comments = new ArrayList<>();
        if (userComments != null) {
            for (Comment comment : userComments) {
                Map<String, Object> map = new HashMap<>();
                DiscussPost post = discussPostService.findDiscussPostById(comment.getEntityId());
                DiscussPostQueryVo discussPostQueryVo = new DiscussPostQueryVo();
                BeanUtils.copyProperties(post, discussPostQueryVo);
                map.put("comment", comment);
                map.put("discussPost", discussPostQueryVo);
                comments.add(map);
            }
        }
        model.addAttribute("comments", comments);
        return "/site/my-reply";
    }

}
