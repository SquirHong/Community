package com.hjs.community.dao;

import com.hjs.community.entity.Message;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author hong
 * @create 2023-01-10 10:24
 */
@Repository
public interface MessageMapper {

    //查询当前用户的会话列表,每个会话只返回最新的一条数据
    List<Message> selectConversations(int userId,int offset,int limit);

    //当前用户会话数量
    int selectConversationCount(int userId);

    //查询某个会话的列表
    List<Message> selectLetters(String conversationId,int offset,int limit);

    //查询某个会话的私信的数量
    int selectLetterCount(String conversationId);

    //查询未读私信集合
    List<Integer> selectUnreadLetterlist(int userId,String conversationId);

    //查询未读私信数量
    int selectUnreadLetterCount(int userId,String conversationId);

    //添加私信
    int insertMessage(Message message);

    //修改私信的状态 (未读，已读，删除)
    int updateStatus(List<Integer> ids,int status);

    //删除指定私信
    int deleteLetter(int id);

    //查找关于某人的最新的一条通知消息
    Message selectLatestNotice(int userId, String topic);

    //查找某人的所有通知消息
    int selectNoticeCount(int userId, String topic);

    int selectNoticeUnreadCount(int userId, String topic);

    //topic可以为空
    int selectNoticeUnreadCountIf(int userId);

    //查询某个通知主题所包含的所有消息
    List<Message> selectNotices(int userId, String topic, int offset, int limit);


}
