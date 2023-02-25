package com.hjs.community.controller;

import com.alibaba.fastjson.JSONObject;
import com.hjs.community.annotation.LoginRequired;
import com.hjs.community.entity.Message;
import com.hjs.community.entity.Page;
import com.hjs.community.entity.User;
import com.hjs.community.service.MessageService;
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
import org.springframework.web.util.HtmlUtils;

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

    @LoginRequired
    @GetMapping("/letter/list")
    public String getLetterList(Model model, Page page) {
        User user = hostHolder.getUser();
        page.setLimit(5);
        page.setPath("/letter/list");
        page.setRows(messageService.findConversationCount(user.getId()));

        List<Message> conversations = messageService.findConversations(user.getId(), page.getOffset(), page.getLimit());
        List<Map<String, Object>> conversationList = null;

        if (conversations != null) {
            conversationList = new ArrayList<>();
            for (Message message : conversations) {
                Map<String, Object> map = new HashMap<>();
                map.put("conversation", message);
                map.put("letterCount", messageService.findLetterCount(message.getConversationId()));
                map.put("unReadCount", messageService.findUnreadLetterCount(user.getId(), message.getConversationId()));
                //这里是为了显示头像
                int targetId = user.getId() == message.getFromId() ? message.getToId() : message.getFromId();
                map.put("target", userService.findUserById(targetId));
                conversationList.add(map);
            }
        }
        model.addAttribute("conversations", conversationList);
        model.addAttribute("conversationsUnreadCount", messageService.findUnreadLetterCount(user.getId(), null));
        int noticeUnReadCount = messageService.findNoticeUnreadCountIf(user.getId());
        model.addAttribute("noticeUnReadCount", noticeUnReadCount);
        return "/site/letter";
    }

    @GetMapping("/letter/detail/{conversationId}")
    public String getLetterDetail(@PathVariable("conversationId") String conversationId, Page page, Model model) {
        page.setLimit(5);
        page.setPath("/letter/detail/" + conversationId);
        page.setRows(messageService.findLetterCount(conversationId));
        List<Message> letterList = messageService.findLetters(conversationId, page.getOffset(), page.getLimit());
        //显示 from_id 数据
        // TODO: 2023/1/10 做个冗余类 带上target用户的name和头像
        List<Map<String, Object>> letters = new ArrayList<>();
        List<Integer> ids = new ArrayList<>();
        if (letterList != null) {
            for (Message message : letterList) {
                Map<String, Object> map = new HashMap<>();
                map.put("letter", message);
                map.put("fromUser", userService.findUserById(message.getFromId()));
                letters.add(map);
                if (message.getToId() == hostHolder.getUser().getId() && message.getStatus() == 0) {
                    ids.add(message.getId());
                }
            }
        }
        //处理未读信息 未读-> 已读
        if (!ids.isEmpty()) {
            messageService.readMessage(ids);
        }
        model.addAttribute("letters", letters);
        model.addAttribute("target", getLetterTarget(conversationId));
        return "/site/letter-detail";
    }

    @ResponseBody
    @PostMapping("/letter/send")
    public String sendLetter(String toName, String content) {
        User target = userService.getUserByName(toName);
        if (target == null) {
            return CommunityUtil.getJsonString(1, "目标用户不存在");
        }
        Message message = new Message();
        message.setFromId(hostHolder.getUser().getId());
        message.setToId(target.getId());
        message.setContent(content);
        message.setStatus(0);
        message.setCreateTime(new Date());
        //小的id号在前
        message.setConversationId(message.getFromId() > message.getToId()
                ? message.getToId() + "_" + message.getFromId() : message.getFromId() + "_" + message.getToId());
        messageService.addMessage(message);
        return CommunityUtil.getJsonString(0);
    }


    private User getLetterTarget(String conversationId) {
        String[] s = conversationId.split("_");
        int u1 = Integer.parseInt(s[0]);
        int u2 = Integer.parseInt(s[1]);
        return u1 == hostHolder.getUser().getId() ? userService.findUserById(u2) : userService.findUserById(u1);
    }

    @ResponseBody
    @PostMapping("/letter/delete")
    public String deleteLetter(int id) {
        return messageService.deleteLetter(id) == 1
                ? CommunityUtil.getJsonString(0) : CommunityUtil.getJsonString(1, "删除私信错误");
    }

    @GetMapping("/notice/list")
    public String getNoticeListPage(Model model) {
        User user = hostHolder.getUser();
        //关于 评论
        Message message = messageService.findLatestNotice(user.getId(), CommunityConstant.TOPIC_COMMENT);

        int noticeUnreadCommentCount = 0;
        int noticeUnreadLikeCount = 0;
        int noticeUnreadFollowCount = 0;
        if (message != null) {
            Map<String, Object> messageVo = new HashMap<>();
            messageVo.put("message", message);
            String content = HtmlUtils.htmlUnescape(message.getContent());
            Map<String, Object> map = JSONObject.parseObject(content, HashMap.class);
            messageVo.put("user", userService.findUserById((Integer) map.get("userId")));
            messageVo.put("entityId", map.get("entityId"));
            messageVo.put("entityType", map.get("entityType"));
            noticeUnreadCommentCount = messageService.findNoticeUnreadCount(user.getId(), CommunityConstant.TOPIC_COMMENT);
            messageVo.put("unread", noticeUnreadCommentCount);
            int count = messageService.findNoticeCount(user.getId(), CommunityConstant.TOPIC_COMMENT);
            messageVo.put("count", count);
            model.addAttribute("commentNotice", messageVo);
        }

        //关于 点赞
        message = messageService.findLatestNotice(user.getId(), CommunityConstant.TOPIC_LIKE);

        if (message != null) {
            Map<String, Object> messageVo = new HashMap<>();
            messageVo.put("message", message);
            String content = HtmlUtils.htmlUnescape(message.getContent());
            Map<String, Object> map = JSONObject.parseObject(content, HashMap.class);
            messageVo.put("user", userService.findUserById((Integer) map.get("userId")));
            messageVo.put("entityId", map.get("entityId"));
            messageVo.put("entityType", map.get("entityType"));
            noticeUnreadLikeCount = messageService.findNoticeUnreadCount(user.getId(), CommunityConstant.TOPIC_LIKE);
            messageVo.put("unread", noticeUnreadLikeCount);
            int count = messageService.findNoticeCount(user.getId(), CommunityConstant.TOPIC_LIKE);
            messageVo.put("count", count);
            model.addAttribute("likeNotice", messageVo);
        }

        //关于 关注
        message = messageService.findLatestNotice(user.getId(), CommunityConstant.TOPIC_FOLLOW);
        if (message != null) {
            Map<String, Object> messageVo = new HashMap<>();
            messageVo.put("message", message);
            String content = HtmlUtils.htmlUnescape(message.getContent());
            Map<String, Object> map = JSONObject.parseObject(content, HashMap.class);
            messageVo.put("user", userService.findUserById((Integer) map.get("userId")));
            messageVo.put("entityId", map.get("entityId"));
            messageVo.put("entityType", map.get("entityType"));
            noticeUnreadFollowCount = messageService.findNoticeUnreadCount(user.getId(), CommunityConstant.TOPIC_FOLLOW);
            messageVo.put("unread", noticeUnreadFollowCount);
            int count = messageService.findNoticeCount(user.getId(), CommunityConstant.TOPIC_FOLLOW);
            messageVo.put("count", count);
            model.addAttribute("followNotice", messageVo);
        }

        // 查询未读消息数量
        int conversationsUnreadCount = messageService.findUnreadLetterCount(user.getId(), null);
        model.addAttribute("conversationsUnreadCount", conversationsUnreadCount);
        int noticeUnReadCount = noticeUnreadCommentCount + noticeUnreadLikeCount + noticeUnreadFollowCount;
        model.addAttribute("noticeUnReadCount", noticeUnReadCount);

        return "/site/notice";
    }

    /**
     * 前端需求 notices
     * fromUser notice User postId entityType
     * notices:
     *
     * @param topic
     * @param page
     * @param model
     * @return
     */
    @GetMapping("/notice/detail/{topic}")
    public String getNoticeDetail(@PathVariable("topic") String topic, Page page, Model model) {
        User user = hostHolder.getUser();
        page.setLimit(5);
        page.setPath("/notice/detail/" + topic);
        page.setRows(messageService.findNoticeCount(user.getId(), topic));
        List<Message> noticeList = messageService.findNotices(user.getId(), topic, page.getOffset(), page.getLimit());
        List<Map<String, Object>> noticeVoList = new ArrayList<>();
        if (noticeList != null) {
            for (Message notice : noticeList) {
                Map<String, Object> map = new HashMap<>();
                map.put("notice", notice);
                String content = HtmlUtils.htmlUnescape(notice.getContent());
                HashMap data = JSONObject.parseObject(content, HashMap.class);
                map.put("user", userService.findUserById((Integer) data.get("userId")));
                map.put("entityType", data.get("entityType"));
//                map.put("entityId", data.get("entityId"));
                map.put("postId", data.get("postId"));
                // 通知作者
//                项目目前的通知都是 系统内部发的通知 也就是kafka发出的消息，所有 fromUser都是 system系统用户，
                map.put("fromUser", userService.findUserById(notice.getFromId()));
                noticeVoList.add(map);
            }

        }
        model.addAttribute("notices", noticeVoList);
        List<Integer> ids = getLetterIds(noticeList);
        if (!ids.isEmpty()) {
            messageService.readMessage(ids);
        }
        return "/site/notice-detail";
    }

    public List<Integer> getLetterIds(List<Message> letterList) {
        List<Integer> ids = new ArrayList<>();
        if (letterList != null) {
            for (Message message : letterList) {
                if (hostHolder.getUser().getId() == message.getToId() && message.getStatus() == 0) {
                    ids.add(message.getId());
                }
            }
        }
        return ids;
    }

}