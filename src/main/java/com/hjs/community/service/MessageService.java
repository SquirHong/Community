package com.hjs.community.service;

import com.hjs.community.dao.MessageMapper;
import com.hjs.community.entity.Message;
import com.hjs.community.util.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

/**
 * @author hong
 * @create 2023-01-10 15:58
 */
@Service
public class MessageService {

    @Autowired
    private MessageMapper messageMapper;

    @Autowired
    private SensitiveFilter sensitiveFilter;

    public List<Message> findConversations(int userId,int offset,int limit){
        return messageMapper.selectConversations(userId,offset,limit);
    }

    public int findConversationCount(int userId){
        return messageMapper.selectConversationCount(userId);
    }

    public List<Message> findLetters(String conversationId,int offset,int limit){
        return messageMapper.selectLetters(conversationId,offset,limit);
    }

    public int findLetterCount(String conversationId){
        return messageMapper.selectLetterCount(conversationId);
    }

    public List<Integer> findUnreadLetterList(int userId, String conversationId){
        return messageMapper.selectUnreadLetterlist(userId,conversationId);
    }

    public int findUnreadLetterCount(int userId, String conversationId){
        return messageMapper.selectUnreadLetterCount(userId,conversationId);
    }

    public int addMessage(Message message){
        message.setContent(HtmlUtils.htmlEscape(sensitiveFilter.filter(message.getContent())));
        return messageMapper.insertMessage(message);
    }

    public int readMessage(List<Integer> ids){
        return messageMapper.updateStatus(ids,1);
    }

}
