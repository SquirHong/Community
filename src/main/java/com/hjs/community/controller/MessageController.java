package com.hjs.community.controller;

import com.hjs.community.entity.Message;
import com.hjs.community.entity.Page;
import com.hjs.community.entity.User;
import com.hjs.community.service.MessageService;
import com.hjs.community.service.UserService;
import com.hjs.community.util.CommunityUtil;
import com.hjs.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.yaml.snakeyaml.events.Event;

import java.util.*;

/**
 * @author hong
 * @create 2023-01-10 16:03
 */
@Controller
public class MessageController {

    @Autowired
    private MessageService messageService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private UserService userService;

    @GetMapping("/letter/list")
    public String getLetterList(Model model, Page page){
        User user = hostHolder.getUser();
        page.setLimit(5);
        page.setPath("/letter/list");
        page.setRows(messageService.findConversationCount(user.getId()));

        List<Message> conversations = messageService.findConversations(user.getId(), page.getOffset(), page.getLimit());
        List<Map<String,Object>> conversationList = null;

        if (conversations != null){
            conversationList = new ArrayList<>();
            for (Message message : conversations){
                Map<String,Object> map = new HashMap<>();
                map.put("conversation",message);
                map.put("letterCount",messageService.findLetterCount(message.getConversationId()));
                map.put("unReadCount",messageService.findUnreadLetterCount(user.getId(), message.getConversationId()));
                //这里是为了显示头像
                int targetId = user.getId() == message.getFromId() ? message.getToId() : message.getFromId();
                map.put("target",userService.findUserById(targetId));
                conversationList.add(map);
            }
        }

        model.addAttribute("conversations",conversationList);
        model.addAttribute("conversationsUnreadCount",messageService.findUnreadLetterCount(user.getId(),null));
        return "/site/letter";
    }

    @GetMapping("/letter/detail/{conversationId}")
    public String getLetterDetail(@PathVariable("conversationId")String conversationId,Page page,Model model){
        page.setLimit(5);
        page.setPath("/letter/detail/"+conversationId);
        page.setRows(messageService.findLetterCount(conversationId));
        List<Message> letterList = messageService.findLetters(conversationId, page.getOffset(), page.getLimit());
        //显示 from_id 数据
        // TODO: 2023/1/10 做个冗余类 带上target用户的name和头像
        List<Map<String,Object>> letters = new ArrayList<>();
        List<Integer> ids = new ArrayList<>();
        if (letterList != null){
            for (Message message : letterList){
                Map<String,Object> map = new HashMap<>();
                map.put("letter",message);
                map.put("fromUser",userService.findUserById(message.getFromId()));
                letters.add(map);
                if (message.getToId() == hostHolder.getUser().getId() && message.getStatus() == 0){
                    ids.add(message.getId());
                }
            }
        }
        //处理未读信息 未读-> 已读
        if (!ids.isEmpty()){
            messageService.readMessage(ids);
        }
        model.addAttribute("letters",letters);
        model.addAttribute("target",getLetterTarget(conversationId));
        return "/site/letter-detail";
    }

    @ResponseBody
    @PostMapping("/letter/send")
    public String sendLetter(String toName,String content){
        User target = userService.getUserByName(toName);
        if (target == null){
            return CommunityUtil.getJsonString(1,"目标用户不存在");
        }
        Message message = new Message();
        message.setFromId(hostHolder.getUser().getId());
        message.setToId(target.getId());
        message.setContent(content);
        message.setStatus(0);
        message.setCreateTime(new Date());
        //小的id号在前
        message.setConversationId(message.getFromId() > message.getToId() ? message.getToId()+"_"+message.getFromId() : message.getFromId()+"_"+message.getToId());
        messageService.addMessage(message);
        return CommunityUtil.getJsonString(0);
    }


    private User getLetterTarget(String conversationId){
        String[] s = conversationId.split("_");
        int u1 = Integer.parseInt(s[0]);
        int u2 = Integer.parseInt(s[1]);
        return u1 == hostHolder.getUser().getId() ? userService.findUserById(u2) : userService.findUserById(u1);
    }

}
