package com.example.wms_backend.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 方法级操作审计标记
 * 标注在需要留痕的写操作方法上，由 AuditLogAspect 拦截并记录
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AuditLog {

    /** 所属模块：product / warehouse / stockin / stockout */
    String module();

    /** 动作：CREATE / UPDATE / DELETE */
    String action();
}