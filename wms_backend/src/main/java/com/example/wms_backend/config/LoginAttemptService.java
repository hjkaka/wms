package com.example.wms_backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 登录防爆破（s1-5）：
 * 基于"用户名|IP"维度的滑动窗口失败计数器，纯内存存储。
 * 规则：15 分钟内同一 用户名|IP 连续失败达到 5 次 → 锁定，直到最早的失败记录滑出窗口。
 * 缓存自动惰性清理（过期即剔除），避免内存泄漏；登录成功清零。
 */
@Component
public class LoginAttemptService {

    private final int maxAttempts;
    private final long windowMs;

    // key = username|ip，value = 最近失败时刻的队列（升序，队首最旧）
    private final ConcurrentHashMap<String, Deque<Long>> failureCache = new ConcurrentHashMap<>();

    public LoginAttemptService(
            @Value("${security.login.max-attempts:5}") int maxAttempts,
            @Value("${security.login.window-minutes:15}") long windowMinutes) {
        this.maxAttempts = maxAttempts;
        this.windowMs = windowMinutes * 60 * 1000L;
    }

    private String key(String username, String ip) {
        return (username == null ? "" : username) + "|" + (ip == null ? "" : ip);
    }

    /** 是否已锁定（窗口内失败次数达到上限） */
    public boolean isBlocked(String username, String ip) {
        Deque<Long> deque = failureCache.get(key(username, ip));
        if (deque == null) {
            return false; // 无失败记录
        }
        synchronized (deque) {
            purgeExpired(deque);
            if (deque.isEmpty()) {
                failureCache.remove(key(username, ip)); // 全过期，直接清掉防泄漏
                return false;
            }
            return deque.size() >= maxAttempts;
        }
    }

    /** 记录一次失败（幂等加入计数） */
    public void recordFailure(String username, String ip) {
        failureCache.compute(key(username, ip), (k, deque) -> {
            if (deque == null) {
                deque = new ArrayDeque<>();
            }
            synchronized (deque) {
                deque.addLast(System.currentTimeMillis());
                purgeExpired(deque);
            }
            return deque;
        });
    }

    /** 登录成功，清除该 用户名|IP 的全部失败记录 */
    public void recordSuccess(String username, String ip) {
        failureCache.remove(key(username, ip));
    }

    /** 滑动窗口剔除窗口之外的旧失败记录 */
    private void purgeExpired(Deque<Long> deque) {
        long threshold = System.currentTimeMillis() - windowMs;
        while (!deque.isEmpty() && deque.peekFirst() < threshold) {
            deque.pollFirst();
        }
    }
}