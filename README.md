# 🚗 拼车协作平台

面向粉丝团购场景的拼车协作与可信结算系统。

## 技术栈

| 层级 | 技术 |
|------|------|
| 后端框架 | Spring Boot 3.2、MyBatis-Plus 3.5.5、RuoYi |
| 数据库 | MySQL、Redis、Druid |
| 前端 | 微信小程序（原生）|
| 管理后台 | Vue 2 + Element UI (RuoYi-Vue) |
| 认证 | JWT |
| AI | 多维度加权推荐引擎 |

## 核心功能

- 发起/参与拼车、偏好认领、费用分摊
- AI 智能推荐（5 维匹配算法）
- 付款凭证上传与审核
- 结算确认与订单生成
- 物流追踪与签收确认
- 双向评价与信用评分体系
- 消息通知与数据统计
- 拼车模板快速创建

## 快速开始

### 前置条件

- JDK 17+、Maven 3.6+、Node.js 16+
- MySQL 8.0+、Redis 5+

### 1. 初始化数据库

```sql
CREATE DATABASE IF NOT EXISTS car_share_db CHARACTER SET utf8mb4;
USE car_share_db;
SOURCE car-share-backend/init_complete.sql;
```

### 2. 配置（可选）

后端配置文件：`car-share-backend/src/main/resources/application.yml`
- 数据库连接（默认 `localhost:3306`，用户名 `root`，密码 `202237`）
- Redis 连接（默认 `localhost:6379`）
- 微信小程序 AppID/Secret

小程序配置文件：`wechatproject/utils/config.js`
- 修改 `BASE_URL` 和 `SERVER_URL` 为后端实际地址

### 3. 一键启动 🚀

双击项目根目录下的 **`start.bat`**，然后选择：
- `[1]` 启动全部 — 自动启动后端 + 管理后台
- `[2]` 仅启动后端 — Spring Boot（端口 8081）
- `[3]` 仅启动管理后台 — Vue 管理端（端口 8082）

启动后访问：
| 服务 | 地址 |
|------|------|
| 后端 API | http://localhost:8081 |
| 管理后台 | http://localhost:8082 |
| 管理后台登录 | admin / admin123 |

### 4. 微信小程序

用**微信开发者工具**打开 `wechatproject` 目录即可运行。首次使用需在 `utils/config.js` 中配置后端地址。

## 项目结构

```
├── start.bat                 # 一键启动脚本
├── car-share-backend/        # Java 后端
│   ├── src/main/java/com/carshare/
│   │   ├── controller/       # 小程序 API 控制器
│   │   ├── service/          # 业务逻辑层
│   │   ├── mapper/           # 数据访问层
│   │   ├── entity/           # 实体类
│   │   └── common/enums/     # 枚举（含 CarStatus）
│   ├── admin-ui/             # Vue 管理后台
│   └── init_complete.sql     # 数据库初始化脚本
├── wechatproject/            # 微信小程序
│   ├── pages/                # 页面
│   └── utils/                # 工具（status.js, config.js）
└── jenkins/                  # CI/CD 配置
```
