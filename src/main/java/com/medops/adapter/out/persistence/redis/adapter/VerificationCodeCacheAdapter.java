package com.medops.adapter.out.persistence.redis.adapter;

import com.medops.adapter.out.persistence.redis.repository.VerificationCodeRedisRepository;
import com.medops.application.port.out.VerificationCodePort;
import com.medops.application.dto.InvitationCodeDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Random;

@Component
@RequiredArgsConstructor
public class VerificationCodeCacheAdapter implements VerificationCodePort {

    private final VerificationCodeRedisRepository verificationCodeRedisRepository;

    @Override
    public String generateVerificationCode() {
        Random random = new Random();
        int randomNumber = random.nextInt(1_000_000);
        return String.format("%06d", randomNumber);
    }

    @Override
    public void saveVerificationCode(String email, String code, String hospitalId) {
        verificationCodeRedisRepository.save(email, code, hospitalId);
    }

    @Override
    public InvitationCodeDto getInvitationCode(String email) {
        return verificationCodeRedisRepository.getInvitationCode(email);
    }

    @Override
    public void removeVerificationCode(String email){
        verificationCodeRedisRepository.remove(email);
    }
}
