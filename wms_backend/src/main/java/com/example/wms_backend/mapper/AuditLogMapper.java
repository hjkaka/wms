package com.example.wms_backend.mapper;

import com.example.wms_backend.entity.AuditLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AuditLogMapper {
    /** 新增一条审计日志（返回受影响行数） */
    int insert(@Param("log") AuditLog log);
}