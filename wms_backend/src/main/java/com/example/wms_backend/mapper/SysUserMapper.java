package com.example.wms_backend.mapper;

import com.example.wms_backend.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SysUserMapper {
 /**
  * 根据用户名查询用户
  * 登录的时候用：用户输入用户名，我们去数据库查有没有这个人
  *
  * @Param("username") 对应 XML 里的 #{username}
  */
    SysUser findByUsername(@Param("username") String username);

    SysUser findById(@Param("id") Long id);
}
