package com.example.wms_backend.exception;

/**
 * 登录被锁定异常（s1-5 登录防爆破）
 * 当同一 用户名|IP 在滑动窗口内失败次数达到上限时抛出，
 * 由 GlobalExceptionHandler 捕获并返回 HTTP 429（Too Many Requests）。
 */
public class LoginLockedException extends RuntimeException {
    public LoginLockedException(String message) {
        super(message);
    }
}