package com.medops.adapter.out.persistence.redis.repository;

import com.medops.application.dto.InvitationCodeDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Repository
@RequiredArgsConstructor
public class VerificationCodeRedisCacheRepository {

    private final RedisTemplate<String, String> redisTemplate;

    public void save(String email, String code, String hospitalId){
        String key = email + ":invitationCode";
        Map<String, String> details = new HashMap<>();
        details.put("code", code);
        details.put("hospitalId", hospitalId);
        redisTemplate.opsForHash().putAll(key, details);
        redisTemplate.expire(key, 1, TimeUnit.HOURS);
    }

    public InvitationCodeDto getInvitationCode(String email){
        String key = email + ":invitationCode";
        var entries = redisTemplate.opsForHash().entries(key);
        if (entries.isEmpty()){
            throw new IllegalArgumentException("검증 코드 만료되었습나디. 다시 요청하세요");
        }
        return new InvitationCodeDto(
            entries.get("code").toString(),
            entries.get("hospitalId").toString()
        );
    }

    public void remove(String email){
        String key = email + ":invitationCode";
        redisTemplate.delete(key);
    }
}
