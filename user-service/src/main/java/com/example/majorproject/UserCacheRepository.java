package com.example.majorproject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;

@Repository
public class UserCacheRepository {

    @Autowired
    RedisTemplate<String,Object> redisTemplate;

    private static final String USER_KEY_PREFIX = "usr::";

    public User getUser(String userId){
        return (User)redisTemplate.opsForValue().get(USER_KEY_PREFIX + userId);
    }

    public void setUser(User user){
         redisTemplate.opsForValue().set(USER_KEY_PREFIX+user.getUserId(),user, Duration.ofMinutes(30));
    }


}
