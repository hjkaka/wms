package com.example.wms_backend.service.impl;

import com.example.wms_backend.dto.UserCreateDTO;
import com.example.wms_backend.dto.UserQueryDTO;
import com.example.wms_backend.dto.UserUpdateDTO;
import com.example.wms_backend.entity.SysUser;
import com.example.wms_backend.mapper.SysUserMapper;
import com.example.wms_backend.service.UserManageService;
import com.example.wms_backend.vo.UserVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 用户管理实现（仅 ADMIN）。
 * 关键点：
 *  - 密码一律 BCrypt 加密后入库（禁止明文，对应 s1-1 规范）。
 *  - 只做启停，不做物理删除（保留历史归属，对应审计原则）。
 *  - 保护：禁止停用 admin、禁止停用当前登录账号。
 */
@Service
public class UserManageServiceImpl implements UserManageService {

    /** 允许的角色集合 */
    private static final Set<String> VALID_ROLES = Set.of("ADMIN", "MANAGER", "STAFF");

    @Autowired
    private SysUserMapper sysUserMapper;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public UserVO createUser(UserCreateDTO dto) {
        if (dto.getUsername() == null || dto.getUsername().isBlank()) {
            throw new RuntimeException("用户名不能为空");
        }
        if (dto.getPassword() == null || dto.getPassword().isBlank()) {
            throw new RuntimeException("密码不能为空");
        }
        String role = normalizeRole(dto.getRole());
        if (role == null) {
            throw new RuntimeException("角色不合法，只能是 ADMIN/MANAGER/STAFF");
        }
        // 用户名唯一校验（数据库有唯一键，先给友好提示）
        if (sysUserMapper.findByUsername(dto.getUsername()) != null) {
            throw new RuntimeException("用户名已存在：" + dto.getUsername());
        }

        SysUser user = new SysUser();
        user.setUsername(dto.getUsername().trim());
        user.setPassword(passwordEncoder.encode(dto.getPassword())); // BCrypt 编码
        user.setRealName(dto.getRealName());
        user.setPhone(dto.getPhone());
        user.setRole(role);
        user.setStatus(1); // 默认启用

        sysUserMapper.insert(user);
        return toVO(user);
    }

    @Override
    public UserVO updateUser(Long id, UserUpdateDTO dto) {
        SysUser existing = sysUserMapper.findById(id);
        if (existing == null) {
            throw new RuntimeException("用户不存在，ID：" + id);
        }
        if (dto.getRole() != null && !dto.getRole().isBlank()
                && !VALID_ROLES.contains(dto.getRole().toUpperCase())) {
            throw new RuntimeException("角色不合法，只能是 ADMIN/MANAGER/STAFF");
        }

        SysUser user = new SysUser();
        user.setId(id);
        user.setRealName(dto.getRealName());
        user.setPhone(dto.getPhone());
        if (dto.getRole() != null && !dto.getRole().isBlank()) {
            user.setRole(dto.getRole().toUpperCase());
        }
        // 密码留空不改，填了才重新 BCrypt 编码
        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        sysUserMapper.update(user);
        return toVO(sysUserMapper.findById(id));
    }

    @Override
    public void changeStatus(Long id, Integer status) {
        if (status == null || (status != 0 && status != 1)) {
            throw new RuntimeException("状态参数不合法，只能是 0(停用)/1(启用)");
        }
        SysUser target = sysUserMapper.findById(id);
        if (target == null) {
            throw new RuntimeException("用户不存在，ID：" + id);
        }
        if (status == 0) { // 停用才需要保护
            if ("ADMIN".equals(target.getRole())) {
                throw new RuntimeException("不能停用管理员账号");
            }
            Long currentId = currentOperatorId();
            if (currentId != null && currentId.equals(id)) {
                throw new RuntimeException("不能停用当前登录账号");
            }
        }
        sysUserMapper.updateStatus(id, status);
    }

    @Override
    public Map<String, Object> pageUsers(UserQueryDTO query) {
        List<SysUser> list = sysUserMapper.findByCondition(
                query.getUsername(), query.getRole(), query.getStatus(),
                query.getOffset(), query.getPageSize());
        long total = sysUserMapper.countByCondition(
                query.getUsername(), query.getRole(), query.getStatus());

        List<UserVO> voList = list.stream().map(this::toVO).collect(Collectors.toList());

        Map<String, Object> page = new HashMap<>();
        page.put("list", voList);
        page.put("total", total);
        page.put("pageNum", query.getPageNum());
        page.put("pageSize", query.getPageSize());
        return page;
    }

    // ---------- 小工具 ----------

    private String normalizeRole(String role) {
        if (role == null || role.isBlank()) {
            return null;
        }
        String upper = role.trim().toUpperCase();
        return VALID_ROLES.contains(upper) ? upper : null;
    }

    /** 从 SecurityContext 取当前登录用户 ID（JwtFilter 把 userId 设为 principal） */
    private Long currentOperatorId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof Long) {
            return (Long) auth.getPrincipal();
        }
        return null;
    }

    /** 实体转 VO（刻意不带 password） */
    private UserVO toVO(SysUser user) {
        if (user == null) {
            return null;
        }
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setRealName(user.getRealName());
        vo.setPhone(user.getPhone());
        vo.setRole(user.getRole());
        vo.setStatus(user.getStatus());
        vo.setCreateTime(user.getCreateTime());
        return vo;
    }
}