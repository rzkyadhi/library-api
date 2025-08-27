package com.itsec.technical_test.security;

import java.util.concurrent.TimeUnit;

import lombok.extern.log4j.Log4j2;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Log4j2
public class TokenService {
    private final StringRedisTemplate redisTemplate;
    private static final String ACCESS_PREFIX = "access:";
    private static final String REFRESH_PREFIX = "refresh:";
    private static final String USER_REFRESH_PREFIX = "user_refresh:";

    public void storeAccessToken(String token, long expirationMillis) {
        log.info("Storing access token {}", token);
        redisTemplate.opsForValue()
                .set(ACCESS_PREFIX + token, "", expirationMillis, TimeUnit.MILLISECONDS);
    }

    public void storeRefreshToken(String username, String token, long expirationMillis) {
        ValueOperations<String, String> ops = redisTemplate.opsForValue();
        String oldToken = ops.get(USER_REFRESH_PREFIX + username);
        if (oldToken != null) {
            redisTemplate.delete(REFRESH_PREFIX + oldToken);
        }
        ops.set(REFRESH_PREFIX + token, username, expirationMillis, TimeUnit.MILLISECONDS);
        ops.set(USER_REFRESH_PREFIX + username, token, expirationMillis, TimeUnit.MILLISECONDS);
    }

    public boolean isAccessTokenValid(String token) {
        return redisTemplate.hasKey(ACCESS_PREFIX + token);
    }

    public String getUsernameFromRefreshToken(String token) {
        return redisTemplate.opsForValue().get(REFRESH_PREFIX + token);
    }

    public void deleteRefreshToken(String username) {
        ValueOperations<String, String> ops = redisTemplate.opsForValue();
        String token = ops.get(USER_REFRESH_PREFIX + username);
        if (token != null) {
            redisTemplate.delete(REFRESH_PREFIX + token);
            redisTemplate.delete(USER_REFRESH_PREFIX + username);
        }
    }
}
