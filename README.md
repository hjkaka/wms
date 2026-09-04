# WMS 仓储管理系统

> Warehouse Management System —— 面向中小型企业的前后端分离仓储管理系统

## 📌 项目简介

本系统为 WMS（Warehouse Management System）仓储管理系统，覆盖**商品管理、出入库管理、库存管理与预警**等核心业务，采用前后端分离架构。

| 板块 | 位置 | 说明 |
|------|------|------|
| 前端 | `wms_front` | Vue 3 + Element Plus + Vite |
| 后端 | `wms_backend` | Spring Boot + MyBatis + MySQL + Redis |

## 🧩 技术栈

### 后端
| 分类 | 技术 | 版本 |
|------|------|------|
| 语言 | Java | 17 |
| 框架 | Spring Boot | 2.7.18 |
| ORM | MyBatis | 3.5.13 |
| 数据库 | MySQL | 8.0 |
| 缓存 / 实时推送 | Redis + WebSocket | 6.0+ |
| 认证 | Spring Security + JWT | 0.11.5 |
| 工具 | Lombok | 1.18.30 |

### 前端
| 分类 | 技术 | 版本 |
|------|------|------|
| 框架 | Vue 3 | 3.5+ |
| UI 组件库 | Element Plus | 2.8+ |
| 状态管理 | Pinia | 2.2+ |
| 路由 | Vue Router | 4.4+ |
| HTTP 请求 | Axios | 1.7+ |
| 构建工具 | Vite | 5.4+ |

## 🧭 功能模块

- **用户认证**：JWT Token 登录认证、权限控制
- **仓库管理**：仓库信息的增删改查
- **商品管理**：商品分类、商品信息与上下架
- **入库管理**：入库单创建、审核、库存流水
- **出库管理**：出库单创建、审核、库存流水
- **库存管理**：库存查询与低库存预警、库存流水记录
- **统计报表**：库存汇总、库存趋势

### 三种用户角色
| 角色 | 权限 |
|------|------|
| ADMIN（管理员） | 全部权限，用户管理、数据查看 |
| MANAGER（仓库主管） | 商品管理、出入库审核 |
| STAFF（仓库员工） | 出入库录入、库存查询 |

## 🚀 快速开始

### 0. 环境要求
- JDK 17
- Maven 3.9+
- MySQL 8.0
- Redis 6.0+
- Node.js 18+

### 1. 初始化数据库
创建数据库 `wms`，并导入建表 SQL（`sys_user`、`warehouse`、`category`、`product`、`stock`、`stock_in_order`、`stock_out_order`、`stock_log` 等 10 张表）。

### 2. 启动后端
```bash
cd wms_backend
# 修改 src/main/resources/application.properties 中的数据库密码
mvn spring-boot:run
```
- 服务地址：`http://localhost:8888`

### 3. 启动前端
```bash
cd wms_front
npm install
npm run dev
```
- 访问地址：`http://localhost:5173`（Vite 已配置 `/api` 代理到后端 8888 端口）

## 🔑 默认账号
| 用户名 | 密码 | 角色 |
|--------|------|------|
| admin | 123456 | ADMIN |

## 📁 项目结构

```
wms_project/
├── wms_backend/                 # 后端（Spring Boot）
│   └── src/main/java/com/example/wms_backend
│       ├── common/              # 统一返回格式 Result
│       ├── config/              # 安全配置等
│       ├── controller/          # 接口层（Auth/Product/Stock/入库/出库/仓库）
│       ├── dto/                 # 请求/响应 DTO
│       ├── entity/              # 实体类
│       ├── exception/           # 全局异常处理
│       ├── mapper/              # MyBatis Mapper 接口
│       ├── security/            # JWT 过滤器
│       ├── service/             # 业务层
│       ├── util/                # JWT 工具类
│       └── vo/                  # 视图对象
├── wms_front/                   # 前端（Vue 3 + Vite）
│   └── src/
│       ├── api/                 # 接口封装
│       ├── layout/              # 布局
│       ├── router/              # 路由
│       ├── store/               # Pinia 状态管理
│       ├── utils/               # 请求工具（axios 封装）
│       └── views/               # 页面（登录/仪表盘/商品/库存/出入库/仓库）
└── AGENTS.md                    # AI 开发指南
```

## 🔗 接口规范
- 基础路径：`http://localhost:8888`
- 认证方式：`Authorization: Bearer <token>`
- 请求/响应格式：`application/json`

```json
{
    "code": 200,
    "message": "success",
    "data": {}
}
```

## 📜 推送说明

本仓库通过以下方式完成上传：

1. **新建 GitHub 仓库**：`hjkaka/wms`（public）
2. **代码整理**：
   - 编写根级 `.gitignore`，排除 `node_modules/`、`target/`、`.idea/`、`dist/` 等构建产物与 IDE 配置
   - 移除 `wms_backend` 中残留的嵌套 git 仓库，将前后端统一纳入单一仓库管理
3. **提交**：使用规范的 Conventional Commits 消息（`feat:` / `docs:` 等）
4. **推送**：将 `master` 分支推送到 `origin`（`https://github.com/hjkaka/wms.git`）

日常更新流程：
```bash
git add .
git commit -m "feat: 说明本次改动"
git push origin master
```

## 📄 License

本项目仅用于学习与演示，请自行配置数据库密码等敏感信息，勿在生产环境使用默认凭据。