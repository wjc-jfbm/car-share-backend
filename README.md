# 🚗 拼车协作平台

面向粉丝团购场景的拼车协作与可信结算系统。

## 技术栈

| 层级 | 技术 |
|------|------|
| 后端框架 | Spring Boot 3.2、MyBatis-Plus 3.5.5、RuoYi |
| 数据库 | MySQL、Redis、Druid |
| 前端 | 微信小程序（原生）|
| 管理后台 | Vue 3 + Element Plus |
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

### 1. 数据库

```sql
SOURCE init_complete.sql;
```

### 2. 配置

```bash
cp src/main/resources/application-sample.yml src/main/resources/application.yml
# 修改 application.yml 中的数据库连接和 Redis 配置
```

### 3. 启动后端

```bash
cd car-share-backend
mvn spring-boot:run
```

### 4. 微信小程序

用微信开发者工具打开 `wechatproject` 目录，将 `utils/request.js` 中的 `baseUrl` 改为你的后端地址。

## 项目结构

```
car-share-backend/    # Java 后端 + RuoYi 管理后台
  ├── src/main/java/com/carshare/
  │   ├── controller/    # API 控制器
  │   ├── service/       # 业务逻辑层
  │   ├── mapper/        # 数据访问层
  │   └── entity/        # 实体类
  └── admin-ui/          # 管理后台前端

wechatproject/          # 微信小程序
  ├── pages/            # 页面
  └── utils/            # 工具类
```
