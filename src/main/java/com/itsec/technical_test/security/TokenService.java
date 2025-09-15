package com.itsec.technical_test.security;

import java.util.concurrent.TimeUnit;

import lombok.extern.log4j.Log4j2;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Log4j2
public class TokenService {
    private final RedisTemplate<String, Object> redis;
    private static final String ACCESS_PREFIX = "access:";
    private static final String REFRESH_PREFIX = "refresh:";
    private static final String USER_REFRESH_PREFIX = "user_refresh:";

    public void storeAccessToken(String token, long expirationMillis) {
        log.info("Storing access token {}", token);
        redis.opsForHash().put(ACCESS_PREFIX + token, token, expirationMillis);
    }

    public void storeRefreshToken(String username, String token, long expirationMillis) {
        String oldToken = (String) redis.opsForValue().get(USER_REFRESH_PREFIX + username);
        if (oldToken != null) {
            redis.delete(REFRESH_PREFIX + oldToken);
        }
        redis.opsForHash().put(REFRESH_PREFIX + token, username, expirationMillis);
        redis.opsForHash().put(USER_REFRESH_PREFIX + username, token, expirationMillis);
    }

    public boolean isAccessTokenValid(String token) {
        return redis.hasKey(ACCESS_PREFIX + token);
    }

    public String getUsernameFromRefreshToken(String token) {
        return (String) redis.opsForValue().get(REFRESH_PREFIX + token);
    }

    public void deleteRefreshToken(String username) {
        String token = (String) redis.opsForValue().get(USER_REFRESH_PREFIX + username);
        if (token != null) {
            redis.delete(REFRESH_PREFIX + token);
            redis.delete(USER_REFRESH_PREFIX + username);
        }
    }
}
