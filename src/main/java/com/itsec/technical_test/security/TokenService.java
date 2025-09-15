package com.itsec.technical_test.security;

import com.itsec.technical_test.shared.Constant;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Log4j2
public class TokenService {
    private final RedisTemplate<String, Object> redis;

    public void storeAccessToken(String token, long expirationMillis) {
        redis.opsForHash().put(Constant.RedisKey.ACCESS_TOKEN_PREFIX + token, token, expirationMillis);
    }

    public void storeRefreshToken(String username, String token, long expirationMillis) {
        String oldToken = (String) redis.opsForValue().get(Constant.RedisKey.USER_REFRESH_PREFIX + username);
        if (oldToken != null) {
            redis.delete(Constant.RedisKey.REFRESH_TOKEN_PREFIX + oldToken);
        }
        redis.opsForHash().put(Constant.RedisKey.REFRESH_TOKEN_PREFIX + token, username, expirationMillis);
        redis.opsForHash().put(Constant.RedisKey.USER_REFRESH_PREFIX + username, token, expirationMillis);
    }

    public boolean isAccessTokenValid(String token) {
        return redis.hasKey(Constant.RedisKey.ACCESS_TOKEN_PREFIX + token);
    }

    public String getUsernameFromRefreshToken(String token) {
        return (String) redis.opsForValue().get(Constant.RedisKey.REFRESH_TOKEN_PREFIX + token);
    }

    public void deleteRefreshToken(String username) {
        String token = (String) redis.opsForValue().get(Constant.RedisKey.USER_REFRESH_PREFIX + username);
        if (token != null) {
            redis.delete(Constant.RedisKey.REFRESH_TOKEN_PREFIX + token);
            redis.delete(Constant.RedisKey.USER_REFRESH_PREFIX + username);
        }
    }
}
