# WMS 仓储管理系统 - AI Agent 指南

## 1. 项目概述

### 1.1 项目简介
WMS（Warehouse Management System）是一个面向中小型企业的仓储管理系统，提供商品管理、出入库管理、库存管理等核心功能。

### 1.2 技术栈
| 分类 | 技术 | 版本 |
|------|------|------|
| 语言 | Java | 17 |
| 框架 | Spring Boot | 2.7.18 |
| ORM | MyBatis | 3.5.13 |
| 数据库 | MySQL | 8.0 |
| 缓存 | Redis | 6.0+ |
| 认证 | Spring Security + JWT | 0.11.5 |
| 工具 | Lombok | 1.18.30 |
| 构建 | Maven | 3.9+ |

### 1.3 核心功能
- **用户认证**：JWT Token 认证，支持登录、权限控制
- **仓库管理**：仓库信息的增删改查
- **商品管理**：商品分类、商品信息管理
- **出入库管理**：入库单、出库单、审核流程
- **库存管理**：库存查询、预警、流水记录

### 1.4 三种用户角色
| 角色 | 权限 |
|------|------|
| ADMIN（管理员） | 全部权限，用户管理、数据查看 |
| MANAGER（仓库主管） | 商品管理、出入库审核 |
| STAFF（仓库员工） | 出入库录入、库存查询 |

---

## 2. 项目结构

### 2.1 目录结构
```
wms_backend/
├── src/main/java/com/example/wms_backend/
│   ├── WmsBackendApplication.java    # 启动类
│   ├── common/                       # 公共类
│   │   └── Result.java               # 统一返回格式
│   ├── config/                       # 配置类
│   │   └── SecurityConfig.java       # Security 配置
│   ├── controller/                   # 接口层
│   │   ├── AuthController.java       # 认证接口
│   │   ├── HelloController.java      # 测试接口
│   │   └── TestController.java       # JWT 测试接口
│   ├── dto/                          # 数据传输对象
│   │   ├── LoginDTO.java             # 登录请求
│   │   └── LoginVO.java              # 登录响应
│   ├── entity/                       # 实体类
│   │   └── SysUser.java              # 用户实体
│   ├── exception/                    # 异常处理
│   │   └── GlobalExceptionHandler.java
│   ├── mapper/                       # 数据访问层
│   │   └── SysUserMapper.java
│   ├── security/                     # 安全相关
│   │   └── JwtFilter.java            # JWT 过滤器
│   ├── service/                      # 业务层
│   │   ├── AuthService.java
│   │   ├── HelloService.java
│   │   └── impl/                     # 业务实现
│   │       ├── AuthServiceImpl.java
│   │       └── HelloServiceImpl.java
│   └── util/                         # 工具类
│       └── JwtUtil.java              # JWT 工具
├── src/main/resources/
│   ├── application.properties        # 应用配置
│   └── mapper/                       # MyBatis XML
│       └── SysUserMapper.xml
└── pom.xml
```

### 2.2 分层架构说明
| 层 | 包 | 职责 |
|----|----|------|
| Controller | `controller` | 接收 HTTP 请求，参数校验，调用 Service |
| Service | `service` / `service.impl` | 业务逻辑处理 |
| Mapper | `mapper` + XML | 数据库操作（CRUD） |
| Entity | `entity` | 与数据库表对应的 Java 类 |
| DTO | `dto` | 接收前端请求参数 |
| VO | `dto` | 返回给前端的数据 |

---

## 3. 开发规范

### 3.1 命名规范
| 类型 | 格式 | 示例 |
|------|------|------|
| 实体类 | 名词，与表名对应 | `Warehouse`, `SysUser` |
| DTO/VO | 功能 + DTO/VO | `LoginDTO`, `LoginVO` |
| Service 接口 | 功能 + Service | `WarehouseService` |
| Service 实现 | 功能 + ServiceImpl | `WarehouseServiceImpl` |
| Mapper 接口 | 实体名 + Mapper | `WarehouseMapper` |
| Controller | 功能 + Controller | `WarehouseController` |
| 数据库表 | 下划线，带前缀 | `warehouse`, `sys_user` |
| Java 属性 | 小驼峰 | `warehouseName`, `managerId` |

### 3.2 代码风格
1. **统一返回格式**：所有接口返回 `Result<T>`
```java
public class Result<T> {
    private Integer code;    // 状态码：200成功，400错误
    private String message;  // 提示信息
    private T data;          // 返回数据
}
```

2. **异常处理**：业务异常使用 `RuntimeException`，全局捕获处理
```java
throw new RuntimeException("错误信息");
```

3. **权限控制**：通过 `@PreAuthorize` 注解或 Security 配置控制权限
```java
@PreAuthorize("hasRole('ADMIN')")
```

4. **日志规范**：关键操作记录日志
```java
log.info("用户登录成功：{}", username);
log.error("操作失败：{}", e.getMessage());
```

### 3.3 Git 规范
- 分支命名：`feature/功能名`、`fix/问题描述`
- 提交信息：`feat: 新增仓库管理接口`、`fix: 修复登录bug`

### 3.4 新增功能流程
开发新功能时，按以下顺序：
1. 创建 Entity（对应数据库表）
2. 创建 Mapper 接口 + XML
3. 创建 Service 接口
4. 创建 Service 实现
5. 创建 DTO/VO（请求参数/响应数据）
6. 创建 Controller

---

## 4. 数据库设计

### 4.1 数据库配置
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/wms?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=UTF-8
spring.datasource.username=root
spring.datasource.password=123456
```

### 4.2 已有数据表（10张）
| 表名 | 说明 | 状态 |
|------|------|------|
| `sys_user` | 用户表 | ✅ 已创建 |
| `warehouse` | 仓库表 | ✅ 已创建 |
| `category` | 商品分类表 | ✅ 已创建 |
| `product` | 商品表 | ✅ 已创建 |
| `stock` | 库存表 | ✅ 已创建 |
| `stock_in_order` | 入库单主表 | ✅ 已创建 |
| `stock_in_item` | 入库单明细表 | ✅ 已创建 |
| `stock_out_order` | 出库单主表 | ✅ 已创建 |
| `stock_out_item` | 出库单明细表 | ✅ 已创建 |
| `stock_log` | 库存流水表 | ✅ 已创建 |

### 4.3 表关系
```
sys_user ◄── warehouse (manager_id)
warehouse ◄── stock
category ◄── product (category_id)
product ◄── stock (product_id)
product ◄── stock_in_item / stock_out_item (product_id)
stock_in_order ◄── stock_in_item (order_id)
stock_out_order ◄── stock_out_item (order_id)
```

### 4.4 字段命名约定
- 数据库字段：下划线命名（`manager_id`, `create_time`）
- Java 属性：小驼峰（`managerId`, `createTime`）
- MyBatis 配置自动转换：`map-underscore-to-camel-case=true`

---

## 5. API 接口文档

### 5.1 通用规范
- 基础路径：`http://localhost:8888`
- 认证方式：Bearer Token
- 请求格式：`application/json`
- 响应格式：
```json
{
    "code": 200,
    "message": "success",
    "data": {}
}
```

### 5.2 认证接口

#### 用户登录
- **URL**: `POST /api/auth/login`
- **无需认证**：是
- **请求体**：
```json
{
    "username": "admin",
    "password": "123456"
}
```
- **成功响应**：
```json
{
    "code": 200,
    "data": {
        "token": "eyJhbGciOiJIUzI1NiJ9...",
        "userId": 1,
        "username": "admin",
        "realName": "管理员",
        "role": "ADMIN"
    }
}
```

### 5.3 测试接口

#### JWT 测试接口
- **URL**: `GET /api/test/info`
- **需要认证**：是
- **请求头**：`Authorization: Bearer <token>`
- **成功响应**：
```json
{
    "code": 200,
    "data": {
        "userId": 1,
        "role": "ROLE_ADMIN",
        "message": "你已经通过了 JWT 验证！"
    }
}
```

### 5.4 新增接口模板（待开发）
- 仓库管理：`/api/warehouse`
- 商品管理：`/api/product`
- 分类管理：`/api/category`
- 出入库：`/api/stock-in`, `/api/stock-out`

---

## 6. 安全认证

### 6.1 JWT 认证流程
```
1. 用户登录 → 返回 Token
2. 前端存储 Token（localStorage）
3. 后续请求在 Header 中携带：Authorization: Bearer <token>
4. JwtFilter 过滤器验证 Token
5. 解析 Token 获取用户信息，存入 SecurityContext
6. Controller 通过 SecurityContextHolder 获取当前用户
```

### 6.2 放行接口
| 路径 | 说明 |
|------|------|
| `/api/auth/login` | 登录接口 |
| `/api/hello` | 测试接口 |
| `/api/debug/**` | 调试接口 |

### 6.3 Token 配置
```properties
jwt.secret=MyWmsSecretKeyForJwtToken2026MustBeLongEnough
jwt.expiration=86400000  # 24小时
```

### 6.4 获取当前用户信息
```java
// 在 Controller 中获取当前用户ID
Authentication auth = SecurityContextHolder.getContext().getAuthentication();
Long userId = (Long) auth.getPrincipal();
String role = auth.getAuthorities().iterator().next().getAuthority();
```

---

## 7. 注意事项

### 7.1 版本兼容性
- JDK 必须使用 17
- Lombok 版本必须 >= 1.18.30（兼容 JDK 17）
- MySQL 连接 URL 使用 `characterEncoding=UTF-8`（不是 `utf8mb4`）

### 7.2 常见问题
| 问题 | 原因 | 解决方案 |
|------|------|----------|
| `TypeTag :: UNKNOWN` | Lombok 版本过低 | 升级到 1.18.30+ |
| `Unsupported character encoding` | URL 用了 utf8mb4 | 改为 UTF-8 |
| `401 Unauthorized` | Token 无效或过期 | 重新登录获取新 Token |
| `403 Forbidden` | 没有权限 | 检查用户角色权限 |
| `NoSuchBeanDefinitionException` | 包扫描问题 | 检查 `@ComponentScan` 配置 |

### 7.3 开发环境
- JDK 17
- Maven 3.9+
- IntelliJ IDEA（推荐）
- MySQL 8.0
- Redis 6.0+
- Postman / ApiPost（接口测试）

---

## 8. 后续开发计划

### 阶段一：基础管理
- [x] 用户登录认证
- [ ] 用户管理 CRUD
- [ ] 仓库管理 CRUD
- [ ] 商品分类管理

### 阶段二：商品与库存
- [ ] 商品管理
- [ ] 库存查询与预警
- [ ] 库存流水查询

### 阶段三：出入库
- [ ] 入库单创建
- [ ] 出库单创建
- [ ] 出入库审核
- [ ] 出入库流水

### 阶段四：加分项
- [ ] 数据统计报表
- [ ] 操作日志
- [ ] 数据导出 Excel

---

*文档创建时间：2026-08-10*
*项目状态：开发中*
