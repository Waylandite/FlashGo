# 本机独立开发环境

本项目连接机器上已有的 MySQL91 服务，Redis 和 RabbitMQ 通过 Windows 原生进程运行。Redis、RabbitMQ 的数据及本地密钥在 `.local/`。开发脚本不启动或停止系统 MySQL 服务。

## 工具与连接

| 组件 | 本次选择 | 地址 |
| --- | --- | --- |
| Java | 本机 JDK 17 | 自动使用现有 Java |
| Maven | Wrapper 固定 3.9.9 | `mvnw.cmd`，首次联网下载 |
| MySQL | 本机 MySQL91 服务（9.1.0） | 127.0.0.1:3306 / flashgo |
| Redis 协议服务 | Memurai Developer 4.1.2 | 127.0.0.1:16379 |
| RabbitMQ | 4.4.0 / Erlang OTP 27.3.4.19 | AMQP 15672；管理界面 15673 |
| FlashGo | Spring Boot 3.3.0 | 127.0.0.1:8080 |

MySQL 使用本机账号 `root`，密码保存在 `.local/env.json` 的 `FLASHGO_DB_PASSWORD`，用户名在 `FLASHGO_DB_USER`。RabbitMQ 使用 `flashgo`，vhost 为 `/flashgo`；Redis、RabbitMQ 密码与 JWT 密钥也保存在该文件。不要提交或分享此文件。

`dev-env.ps1` 会根据此文件生成被 Git 忽略的 `.local/application-local.yml`。本地 profile 自动导入它，使 IDE 与脚本启动使用相同配置。修改密钥后重新执行一次 `./scripts/dev.ps1 Status` 以刷新配置。其他机器首次使用前，需要在 `.local/env.json` 中填写其已有 MySQL 的密码。

项目原设计基于 MySQL 8；当前复用机器上的 9.1 二进制，需以迁移与集成测试结果判断本机可用性，不能据此替代未来 CI/部署对 MySQL 8 的验证。

Memurai 是 Windows 的 Redis 兼容实现，开发版存在连续运行期限限制，适合本机开发。上线前需要在目标 Redis 版本上验证。下载及安装方式参考 [Memurai 官方安装文档](https://docs.memurai.com/en/installation)。RabbitMQ 与 Erlang 的组合参考 [官方兼容矩阵](https://www.rabbitmq.com/docs/which-erlang)。构建入口参考 [Maven Wrapper 文档](https://maven.apache.org/tools/wrapper/)。

## 首次准备

在项目根目录 PowerShell 执行：

```powershell
./scripts/setup-native.ps1
```

需要联网。脚本将下载固定版本并解压到 `.local/tools`；Memurai 使用 MSI administrative extraction，不注册系统服务。MySQL 默认二进制目录为 `D:/DevelopTools/mysql/bin`，其他机器通过 `-MysqlBin` 指定；后续运行设置 `FLASHGO_MYSQL_BIN`。支持路径应避免空格。

## 日常开发

```powershell
./scripts/dev.ps1 Start
./scripts/dev.ps1 Status
./scripts/dev.ps1 Test
./scripts/dev.ps1 Run
```

`Run` 在前台启动应用，Ctrl+C 结束。独立打包用 `./scripts/dev.ps1 Build`。当前测试连接本机 3306 的 flashgo，验证 V3 迁移、Redis 和 HTTP 访问；后续增加写入型业务测试前需建立专用测试库。

后台运行打包后的应用：`./scripts/dev.ps1 StartApp`；停止本脚本启动的后台应用：`./scripts/dev.ps1 StopApp`。修改 Java 源码后需重新 Build 或使用前台 Run。

停止中间件：

```powershell
./scripts/dev.ps1 StopApp
./scripts/dev.ps1 Stop
```

系统 MySQL 保持运行，数据持久保留。旧的 `.local/mysql/data` 是之前 13306 实例的数据，不再作为当前开发数据库，仍保留在本机。

## IDE 与接口

将 `pom.xml` 作为 Maven 项目导入，选择 JDK 17，启用 Lombok 注解处理。先执行上述 Start 命令启动 Redis 和 RabbitMQ。IDE 工作目录设为项目根目录，默认 local profile 自动读取 `.local/application-local.yml`，无需手工复制密码。也可以通过环境变量覆盖配置。

- 健康接口：<http://127.0.0.1:8080/api/v1/health>
- 接口文档：<http://127.0.0.1:8080/swagger-ui/index.html>
- RabbitMQ 管理：<http://127.0.0.1:15673>

数据库迁移只有结构。首次应用启动完成迁移后，在另一个 PowerShell 中执行 `./scripts/seed-local.ps1`，再执行 `./scripts/smoke.ps1` 验证四端登录、身份查询、退出和旧令牌拒绝。

执行种子脚本后，测试用户名：APP_USER 为 `13800000001`，MERCHANT 为 `13800000002`，RIDER 为 `13800000003`，PLATFORM 为 `local_admin`。共用的随机本地密码位于 `.local/env.json` 的 `FLASHGO_DEV_PASSWORD`。种子脚本已适配 V3 的 username 字段，不覆盖已有账号。

## 排查

- 日志位于 `.local/logs`。端口被占用时先确认进程归属，不要直接结束无关进程。
- Wrapper 第一次下载失败时可重试；后续直接使用缓存。项目没有要求修改全局 PATH。
- 启动失败先检查三个中间件状态和环境变量。缺少 JWT 密钥、空前缀和无效 TTL 会在启动时失败。
- Flyway 校验失败不要修改已执行迁移文件，通过新迁移修复。

## 本次搭建验证记录（2026-10-29）

- Maven Wrapper 打包成功，`verify` 成功：2 项测试，0 失败、0 错误。
- Flyway V1/V2 从空库执行成功，生成 11 张业务表及迁移历史表。
- MySQL 专用账号连接、Redis 密码认证和 PING 通过。
- 健康接口、OpenAPI 文档可访问；未登录访问身份接口返回 401。
- 四端本地账号的登录、身份查询、退出及退出后令牌拒绝均通过。
- RabbitMQ 项目账号鉴权、持久队列创建、消息发布和消费通过；验证队列已删除。
- 应用及三个中间件停止、重启后重新通过完整 smoke 检查；setup 脚本重复执行成功。
- 应用标准输出和错误日志分别在 `.local/logs/app.out.log`、`.local/logs/app.err.log`；当前状态使用 Status 命令查询。

上述验证覆盖开发环境和现有认证骨架，不代表尚未实现的订单、支付、配送、结算功能已通过测试。此前报告发现的会话与权限业务风险仍需后续开发处理。

## 本机配置切换验证（2026-10-30）

- 默认连接切换为 `127.0.0.1:3306/flashgo`，账号为 `root`；密码仅在忽略的本地文件中保存。
- 启停脚本复用系统 MySQL，不再启动或关闭旧的 13306 独立实例。
- Maven verify 通过：2 项测试，0 失败、0 错误；检查当前 V3 数据库结构。
- 清除 FLASHGO 环境变量后直接运行打包应用，local profile 成功导入本地配置，连接 3306，校验全部 3 个迁移，健康接口返回 UP。
- 验证完成后关闭应用、Redis、RabbitMQ 和辅助进程，系统 MySQL 保持运行。
