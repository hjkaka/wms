package com.example.wms_backend.mapper;

import com.example.wms_backend.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

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

    /**
     * 分页条件查询用户（用户名模糊 + 角色 + 状态）
     */
    List<SysUser> findByCondition(
            @Param("username") String username,
            @Param("role") String role,
            @Param("status") Integer status,
            @Param("offset") Integer offset,
            @Param("pageSize") Integer pageSize);

    /**
     * 统计符合条件的用户数量（分页总数用）
     */
    long countByCondition(
            @Param("username") String username,
            @Param("role") String role,
            @Param("status") Integer status);

    /**
     * 新增用户，返回影响行数；自增主键通过 useGeneratedKeys 写回 user.id
     */
    int insert(@Param("user") SysUser user);

    /**
     * 修改用户基本信息（real_name/phone/role，密码可留空不改），返回影响行数
     */
    int update(@Param("user") SysUser user);

    /**
     * 修改用户状态（启用/停用），返回影响行数
     */
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);
}
