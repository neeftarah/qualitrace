package com.qualitrace.backend.infrastructure.security;

import com.qualitrace.backend.domain.repository.UserRepository;
import com.qualitrace.backend.domain.type.UserStatus;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class LoginAttemptService {
    private static final int MAX_ATTEMPTS = 3;

    private final StringRedisTemplate redisTemplate;
    private final UserRepository userRepository;

    public LoginAttemptService(StringRedisTemplate redisTemplate, UserRepository userRepository) {
        this.redisTemplate = redisTemplate;
        this.userRepository = userRepository;
    }

    /** Check rapide, un seul GET Redis — aucun appel BDD. */
    public boolean isLocked(String login) {
        String value = redisTemplate.opsForValue().get(key(login));
        return value != null && Long.parseLong(value) >= MAX_ATTEMPTS;
    }

    /** Déjà verrouillé côté Redis : on continue de compter, mais aucun accès BDD/BCrypt. */
    public void recordAttemptWhileLocked(String login) {
        redisTemplate.opsForValue().increment(key(login));
    }

    /** Échec réel (après authentification tentée) : incrémente, et verrouille en BDD une seule fois, au franchissement du seuil. */
    public void recordFailure(String login) {
        Long attempts = redisTemplate.opsForValue().increment(key(login));

        if (attempts != null && attempts == MAX_ATTEMPTS) {
            lockUser(login);
        }
    }

    public void recordSuccess(String login) {
        reset(login);
    }

    public void resetOnUnlock(String login) {
        reset(login);
    }

    private void reset(String login) {
        redisTemplate.delete(key(login));
    }

    private void lockUser(String login) {
        userRepository.findByLogin(login).ifPresent(user -> {
            if (user.status() == UserStatus.ACTIVE) {
                userRepository.save(user.lock());
            }
        });
    }

    private String key(String login) {
        return "login:attempts:" + login;
    }
}