package org.example.service;

import org.example.repository.PasswordResetTokenRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TokenCleanupService {
    private final PasswordResetTokenRepository tokenRepository;

    public TokenCleanupService(PasswordResetTokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
    }

    @Scheduled(cron = "0 0 2 * * ?") // Каждый день в 2:00
    @Transactional
    public void cleanUpExpiredTokens() {
        tokenRepository.deleteExpiredTokens();
        System.out.println("🧹 Запущена очистка просроченных токенов");
    }
}