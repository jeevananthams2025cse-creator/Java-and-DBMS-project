package com.bloodbank.security;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LoginAttemptService {

    public static final int MAX_ATTEMPTS = 5;
    private static final long LOCKOUT_DURATION_SECONDS = 15 * 60; // 15 minutes

    private static class AttemptInfo {
        int attempts;
        Instant firstFailedAt;
        Instant lockedUntil;

        AttemptInfo(int attempts, Instant firstFailedAt, Instant lockedUntil) {
            this.attempts = attempts;
            this.firstFailedAt = firstFailedAt;
            this.lockedUntil = lockedUntil;
        }
    }

    private final Map<String, AttemptInfo> attemptsCache = new ConcurrentHashMap<>();

    public void loginSucceeded(String key) {
        if (key != null) {
            attemptsCache.remove(key.toLowerCase().trim());
        }
    }

    public void loginFailed(String key) {
        if (key == null) return;
        String normalizedKey = key.toLowerCase().trim();

        attemptsCache.compute(normalizedKey, (k, existing) -> {
            Instant now = Instant.now();
            if (existing == null) {
                return new AttemptInfo(1, now, null);
            }

            // If existing lockout expired, reset
            if (existing.lockedUntil != null && now.isAfter(existing.lockedUntil)) {
                return new AttemptInfo(1, now, null);
            }

            int newAttempts = existing.attempts + 1;
            Instant lockedUntil = existing.lockedUntil;

            if (newAttempts >= MAX_ATTEMPTS) {
                lockedUntil = now.plusSeconds(LOCKOUT_DURATION_SECONDS);
            }

            return new AttemptInfo(newAttempts, existing.firstFailedAt, lockedUntil);
        });
    }

    public boolean isBlocked(String key) {
        if (key == null) return false;
        String normalizedKey = key.toLowerCase().trim();
        AttemptInfo info = attemptsCache.get(normalizedKey);

        if (info == null || info.lockedUntil == null) {
            return false;
        }

        if (Instant.now().isAfter(info.lockedUntil)) {
            // Lockout expired
            attemptsCache.remove(normalizedKey);
            return false;
        }

        return true;
    }

    public int getRemainingAttempts(String key) {
        if (key == null) return MAX_ATTEMPTS;
        AttemptInfo info = attemptsCache.get(key.toLowerCase().trim());
        if (info == null) return MAX_ATTEMPTS;
        return Math.max(0, MAX_ATTEMPTS - info.attempts);
    }
}
