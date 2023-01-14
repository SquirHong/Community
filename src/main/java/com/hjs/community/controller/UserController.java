package com.hjs.community.controller;

import com.hjs.community.annotation.LoginRequired;
import com.hjs.community.entity.User;
import com.hjs.community.service.LikeService;
import com.hjs.community.service.UserService;
import com.hjs.community.util.CommunityUtil;
import com.hjs.community.util.HostHolder;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
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

    public static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @LoginRequired
    @GetMapping("/setting")
    public String getSettingPage(){
        return "/site/setting";
    }

    @LoginRequired
    @PostMapping("/upload")
    public String uploadHeader(MultipartFile headerImage,Model model){
        if (headerImage == null){
            model.addAttribute("error","您还没有上传文件");
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
            logger.error("上传文件失败："+e.getMessage());
            throw  new RuntimeException("上传文件失败",e);
        }
        User user = hostHolder.getUser();
        String url = domain + contextPath + "/user/header/" + filename;
        userService.updateHeader(user.getId(),url);
        return "redirect:/index";
    }

    @GetMapping("/header/{fileName}")
    public void getHeader(@PathVariable("fileName")String filename, HttpServletResponse response){
        filename = upload +"/"+ filename;
        String suffix = filename.substring(filename.lastIndexOf(".")+1);
        response.setContentType("image/"+suffix);
        try(
                FileInputStream fileInputStream = new FileInputStream(filename);
                ServletOutputStream outputStream = response.getOutputStream()
        ) {
            byte[] buffer = new byte[1024];
            int b=0;
            while ( (b=fileInputStream.read(buffer)) != -1){
                outputStream.write(buffer,0,b);
            }
        } catch (IOException e) {
            logger.error("读取头像失败"+e.getMessage());
        }
    }

    @PostMapping("/updatePassword")
    public String updatePassword(Model model,String oldPassword,String newPassword){
        User user = hostHolder.getUser();
        Map<String,Object> map = userService.updatePassword(user,oldPassword,newPassword);
        if (map.containsKey("oldPasswordMsg")){
            model.addAttribute("oldPasswordMsg",map.get("oldPasswordMsg"));
            return "/site/setting";
        }
        if (map.containsKey("newPasswordMsg")){
            model.addAttribute("newPasswordMsg",map.get("newPasswordMsg"));
            return "/site/setting";
        }
        // TODO: 2023/1/5 考虑这里要不要修改host holder值

        return "redirect:/logout";
    }

    //个人主页
    @GetMapping("/profile/{id}")
    public String getProfilePage(@PathVariable("id")int id,Model model){
        User user = userService.findUserById(id);
        if (user == null){
            throw new IllegalArgumentException("该用户不存在");
        }
        //用户基本信息
        model.addAttribute("user",user);
        int likeCount = likeService.findUserLikeCount(id);
        model.addAttribute("likeCount",likeCount);
        return "/site/profile";
    }


}
