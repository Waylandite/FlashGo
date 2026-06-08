# FlashGo

<div align="center">

### 即时配送 / 本地生活履约 / 同城闪送交易平台后端

一个围绕 **用户、商户、骑手、平台** 四方协同构建的 Spring Boot 开源后端项目。  
聚焦订单主链路、角色权限、履约状态机、结算能力与平台化管理。

[![Java](https://img.shields.io/badge/Java-17-ff6b35?style=for-the-badge&logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-6db33f?style=for-the-badge&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![MySQL](https://img.shields.io/badge/MySQL-8.x-4479a1?style=for-the-badge&logo=mysql&logoColor=white)](https://www.mysql.com/)
[![Redis](https://img.shields.io/badge/Redis-7.x-dc382d?style=for-the-badge&logo=redis&logoColor=white)](https://redis.io/)
[![RabbitMQ](https://img.shields.io/badge/RabbitMQ-3.x-ff6600?style=for-the-badge&logo=rabbitmq&logoColor=white)](https://www.rabbitmq.com/)
[![Flyway](https://img.shields.io/badge/Flyway-DB%20Migration-cc0200?style=for-the-badge&logo=flyway&logoColor=white)](https://flywaydb.org/)

</div>

---

## 项目背景

FlashGo 面向典型的同城即时配送与本地生活履约场景，目标不是只做一个下单系统，而是构建一套完整的：

- 交易下单能力
- 履约流转能力
- 骑手配送能力
- 平台监管与结算能力

项目围绕四类核心角色展开：

- **用户**：浏览店铺、选择商品、下单、支付、追踪配送进度
- **商户**：维护店铺与商品、处理订单、接单或拒单
- **骑手**：抢单、取货、配送、送达
- **平台**：统一管理用户、商户、骑手、订单、结算和运营数据

---

## 核心能力

### 1. 订单主链路

FlashGo 当前 V1 围绕最关键的一条履约主链路设计：

1. 用户创建订单
2. 用户支付订单
3. 商户接单
4. 骑手抢单
5. 骑手取货
6. 骑手配送
7. 平台结算

### 2. 订单状态机

已定义一套清晰的订单状态流转规则，核心状态包括：

- `PENDING_PAYMENT`
- `PENDING_MERCHANT_ACCEPT`
- `PENDING_RIDER_GRAB`
- `RIDER_ASSIGNED`
- `PICKED_UP`
- `DELIVERING`
- `DELIVERED`
- `SETTLED`
- `CANCELLED`
- `MERCHANT_REJECTED`

### 3. 四端权限与认证

项目当前采用统一登录入口，支持：

- `APP_USER`
- `MERCHANT`
- `RIDER`
- `PLATFORM`

结合 `Spring Security + JWT + Redis` 实现：

- 统一登录
- 统一在线态校验
- 动态权限装配
- 角色级接口隔离
- 资源归属校验

### 4. 平台化数据设计

当前已完成第一版核心数据库模型设计，覆盖：

- 用户
- 商户
- 店铺
- 骑手
- 商品
- 订单
- 订单明细
- 订单状态日志
- 订单结算

---

## 技术方案

### 后端框架

- **Spring Boot 3**：作为主应用框架
- **Spring MVC**：提供 RESTful API
- **Spring Security**：实现登录鉴权与角色控制
- **JWT**：承载轻量身份信息

### 数据与持久化

- **MySQL 8**：核心业务数据存储
- **MyBatis-Plus**：持久层开发
- **Flyway**：数据库结构迁移管理

### 缓存与中间件

- **Redis**：在线状态、权限池、缓存、并发控制
- **RabbitMQ**：异步消息与流程解耦

### 工程与辅助能力

- **Springdoc OpenAPI**：接口文档
- **Lombok**：减少样板代码
- **MapStruct**：对象转换
- **JUnit 5**：测试基础

---

## 中间件矩阵

| 中间件 / 组件 | 用途 |
| --- | --- |
| MySQL | 订单、主体、商品、结算等核心业务数据存储 |
| Redis | 登录在线态、权限池、缓存、幂等辅助、并发控制 |
| RabbitMQ | 异步通知、状态解耦、后续结算和超时任务扩展 |
| Flyway | 数据库版本迁移与结构演进 |

---

## 当前已实现内容

### 工程骨架

- Spring Boot 单体项目初始化
- 基础配置分环境管理
- 全局异常处理
- 统一返回结构
- 健康检查接口

### 数据库

- V1 初始建表迁移
- V2 鉴权结构迁移

### 鉴权模块

已实现首批认证接口：

- `POST /api/v1/auth/login`
- `GET /api/v1/auth/me`
- `POST /api/v1/auth/logout`

已接入的鉴权基础设施：

- JWT 签发与解析
- Redis 在线状态
- Redis 公共权限池
- `SecurityContextHolder` 登录上下文接入

---

## 项目结构

```text
src/main/java/com/flashgo
├── auth                # 登录鉴权模块
├── common              # 公共响应、异常、上下文
├── config              # Spring 配置
├── infrastructure      # JWT、Redis 等底层支撑
├── merchant            # 商户端模块（待扩展）
├── order               # 订单模块（待扩展）
├── platform            # 平台端模块（待扩展）
├── product             # 商品模块（待扩展）
├── rider               # 骑手端模块（待扩展）
├── settlement          # 结算模块（待扩展）
├── shop                # 店铺模块（待扩展）
└── user                # 用户端模块（待扩展）
```

---

## 快速开始

### 1. 准备环境

- Java 17+
- MySQL 8+
- Redis 7+
- RabbitMQ 3+

### 2. 创建数据库

根据当前环境创建数据库，例如本地环境：

```sql
CREATE DATABASE flashgo
DEFAULT CHARACTER SET utf8mb4
DEFAULT COLLATE utf8mb4_unicode_ci;
```

### 3. 配置环境

当前项目采用多环境 YAML：

- `application.yml`
- `application-local.yml`
- `application-dev.yml`
- `application-pro.yml`

### 4. 启动项目

启动 Spring Boot 后，Flyway 会自动执行数据库迁移。

默认健康检查接口：

- `GET /api/v1/health`

---

## 文档说明

仓库首页 README 保持开源展示风格。  
完整的项目原始业务说明已归档到：

- [docs/project-overview.md](./docs/project-overview.md)

---

## Roadmap

- [x] 项目业务建模
- [x] 订单状态机设计
- [x] V1 数据库结构设计
- [x] Spring Boot 工程初始化
- [x] Spring Security + JWT 鉴权骨架
- [x] 首批认证接口实现
- [ ] 用户下单接口
- [ ] 商户接单 / 拒单接口
- [ ] 骑手抢单 / 配送接口
- [ ] 平台结算接口
- [ ] 初始化测试数据

---

## Star History

[![Star History Chart](https://api.star-history.com/svg?repos=Waylandite/FlashGo&type=Date)](https://www.star-history.com/#Waylandite/FlashGo&Date)

---

## 致谢

如果这个项目对你有帮助，欢迎：

- 点一个 Star
- 提交 Issue
- 提交 PR 一起完善

FlashGo 仍处于持续演进中，当前版本重点是把即时配送平台的核心主链路打扎实。
