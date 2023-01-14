package com.hjs.community.service;

import com.hjs.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

/**
 * @author hong
 * @create 2023-01-12 16:21
 */
@Service
public class LikeService {

    @Autowired
    private RedisTemplate redisTemplate;

    //点赞按钮
    //将userId加到set集合中
    public void like(int userId, int entityType, int entityId, int entityUserId) {
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
                String userLikeKey = RedisKeyUtil.getUserLikeKey(entityUserId);
                Boolean b = operations.opsForSet().isMember(entityLikeKey, userId);
                //开启redis事务后，所有的crud操作都会被放到队列里，等commit时才会去队列里一个个取
                operations.multi();
                if (b) {
                    //取消点赞
                    operations.opsForSet().remove(entityLikeKey, userId);
                    //被点赞的用户赞-1
                    operations.opsForValue().decrement(userLikeKey);
                } else {
                    operations.opsForSet().add(entityLikeKey, userId);
                    operations.opsForValue().increment(userLikeKey);
                }
                return operations.exec();
            }
        });

    }

    //返回已经点赞的数量 针对某个post或评论
    public long findEntityLikeCount(int entityType, int entityId) {
        String LikeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
        return redisTemplate.opsForSet().size(LikeKey);
    }

    //查询某个user点赞某个letter的状态
    //返回1表示已赞 0表示未赞  -1表示踩
    public int findEntityLikeStatus(int userId, int entityType, int entityId) {
        String LikeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
        return redisTemplate.opsForSet().isMember(LikeKey, userId) ? 1 : 0;
    }

    //查询用户收到的赞
    public int findUserLikeCount(int userId) {
        String userLikeKey = RedisKeyUtil.getUserLikeKey(userId);
        Integer count = (Integer) redisTemplate.opsForValue().get(userLikeKey);
        return count == null ? 0 : count.intValue();
    }

}
