package com.hjs.community.service;

import com.hjs.community.entity.User;
import com.hjs.community.util.CommunityConstant;
import com.hjs.community.util.RedisKeyUtil;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @author hong
 * @create 2023-01-15 14:34
 */
@Service
public class FollowService {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private UserService userService;

    public void follow(int userId, int entityType, int entityId) {
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                //关注的目标
                String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
                //目标的粉丝
                String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);
                operations.multi();
                redisTemplate.opsForZSet().add(followeeKey, entityId, new Date().getTime());
                redisTemplate.opsForZSet().add(followerKey, userId, new Date().getTime());
                return operations.exec();
            }
        });
    }

    public void unfollow(int userId, int entityType, int entityId) {
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                //关注的目标
                String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
                //目标的粉丝
                String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);
                operations.multi();
                redisTemplate.opsForZSet().remove(followeeKey, entityId);
                redisTemplate.opsForZSet().remove(followerKey, userId);
                return operations.exec();
            }
        });
    }

    //查询关注的某个具体实体的数量
    public long findFolloweeCount(int userId, int entityType) {
        String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
        return redisTemplate.opsForZSet().zCard(followeeKey);
    }

    //查询某个实体的“粉丝”数量
    public long findFollowerCount(int entityId, int entityType) {
        String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);
        return redisTemplate.opsForZSet().zCard(followerKey);
    }

    //查询某用户是否已经关注某个实体
    public boolean hasFollowed(int userId, int entityType, int entityId) {
        String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);
        return redisTemplate.opsForZSet().score(followerKey, userId) != null;
    }

    //查询某用户关注的人       带分页
    public List<Map<String, Object>> findFollowees(int userId, int offset, int limit) {
        String followeeKey = RedisKeyUtil.getFolloweeKey(userId, CommunityConstant.ENTITY_TYPE_USER);
        Set<Integer> set = redisTemplate.opsForZSet().reverseRange(followeeKey, offset, offset + limit - 1);
        if (set == null) {
            return null;
        }
        List<Map<String, Object>> list = new ArrayList<>();

        for (Integer id : set) {
            Map<String, Object> map = new HashMap<>();
            User user = userService.findUserById(id);
            map.put("user", user);
            Double score = redisTemplate.opsForZSet().score(followeeKey, id);
            map.put("followTime", new Date(score.longValue()));
            list.add(map);
        }
        return list;
    }

    //查询某用户的粉丝
    public List<Map<String, Object>> findFollowers(int userId, int offset, int limit) {
        String followerKey = RedisKeyUtil.getFollowerKey(CommunityConstant.ENTITY_TYPE_USER, userId);

        Set<Integer> set = redisTemplate.opsForZSet().reverseRange(followerKey, offset, offset + limit - 1);
        if (set == null) {
            return null;
        }
        List<Map<String, Object>> list = new ArrayList<>();
        for (Integer id : set) {
            Map<String, Object> map = new HashMap<>();
            User user = userService.findUserById(id);
            map.put("user", user);
            Double score = redisTemplate.opsForZSet().score(followerKey, id);
            map.put("followTime", new Date(score.longValue()));
            list.add(map);
        }
        return list;
    }

}
