package com.his.service;

import com.his.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(
        name = "his.refresh-token.cleanup-enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class RefreshTokenCleanupService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Scheduled(
            initialDelayString = "${his.refresh-token.cleanup-initial-delay:600000}",
            fixedDelayString = "${his.refresh-token.cleanup-fixed-delay:3600000}"
    )
    @Transactional
    public void cleanupExpiredOrRevokedTokens() {
        long deletedCount = refreshTokenRepository.deleteByRevokedTrueOrExpiresAtBefore(LocalDateTime.now());
        if (deletedCount > 0) {
            log.info("{} adet süresi dolmuş veya iptal edilmiş refresh token temizlendi.", deletedCount);
        }
    }
}
