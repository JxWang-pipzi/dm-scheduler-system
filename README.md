
# 🎭 剧本杀门店 DM 调度管理系统

剧本杀门店 DM（主持人）智能调度管理系统，基于 Spring Boot 2.7 + Vue 2 + WebSocket 构建，支持 DM 智能推荐、实时通知推送、场次排班管理等功能。

## ✨ 功能特性

- **用户管理** — 注册 / 登录，JWT 身份认证，角色权限控制
- **DM 管理** — DM 信息维护、技能标签、擅长剧本绑定
- **DM 排班** — 排班日历视图、智能推荐可用 DM、冲突检测
- **剧本管理** — 剧本信息录入、类型/人数/时长配置
- **场次管理** — 场次创建与排期、DM 分配、状态流转
- **订单管理** — 玩家下单、支付状态跟踪、订单关联场次
- **预约管理** — 玩家预约场次、预约确认与取消
- **评价系统** — 玩家对 DM 的评分与评价
- **实时通知** — WebSocket 推送排班变更、新订单等消息
- **数据统计** — ECharts 可视化仪表盘，营收/场次/DM 评分统计
- **操作日志** — 关键操作留痕，支持查询与审计
- **门店管理** — 多门店信息配置
- **系统配置** — 运行参数动态调整

## 🛠 技术栈

### 后端

| 技术 | 版本 | 说明 |
|------|------|------|
| Spring Boot | 2.7.15 | 核心框架 |
| MyBatis | 2.3.1 | ORM 框架 |
| MySQL | 8.0 | 数据库 |
| WebSocket | — | 实时通信 |
| JWT (jjwt) | 0.9.1 | 身份认证 |
| SpringDoc OpenAPI | 1.7.0 | API 文档 |
| Lombok | — | 代码简化 |
| Java | 1.8 | 运行环境 |

### 前端

| 技术 | 版本 | 说明 |
|------|------|------|
| Vue | 2.6.14 | 前端框架 |
| Vue Router | 3.5.1 | 路由管理 |
| Element UI | 2.15.13 | UI 组件库 |
| Axios | 0.27.2 | HTTP 请求 |
| ECharts | 5.5.0 | 数据可视化 |
| Sass | — | CSS 预处理器 |

### 测试

| 技术 | 说明 |
|------|------|
| Jest | 单元测试 |
| Playwright | E2E 测试 |
| Cypress | E2E 测试（备选） |

## 📁 项目结构

```
dm-scheduler-system/
├── dm-scheduler/                  # 后端 (Spring Boot)
│   ├── src/main/java/com/example/dm/
│   │   ├── config/                # 配置类
│   │   ├── controller/            # 控制器层
│   │   │   ├── DmController.java
│   │   │   ├── DmScheduleController.java
│   │   │   ├── DmScheduleManageController.java
│   │   │   ├── EvaluationController.java
│   │   │   ├── NotificationController.java
│   │   │   ├── OperationLogController.java
│   │   │   ├── OrderController.java
│   │   │   ├── ReservationController.java
│   │   │   ├── ScriptController.java
│   │   │   ├── SessionController.java
│   │   │   └── StatisticsController.java
│   │   ├── entity/                # 实体类
│   │   ├── mapper/                # MyBatis Mapper 接口
│   │   ├── service/               # 业务逻辑层
│   │   │   └── impl/              # 业务实现
│   │   ├── util/                  # 工具类
│   │   ├── vo/                    # 视图对象
│   │   └── websocket/             # WebSocket 处理
│   ├── src/main/resources/
│   │   ├── application.yml        # 应用配置
│   │   └── mapper/                # MyBatis XML 映射
│   ├── scripts/                   # 数据库脚本
│   └── pom.xml
│
├── dm-scheduler-web/              # 前端 (Vue 2)
│   ├── src/
│   │   ├── api/                   # API 请求模块
│   │   ├── components/            # 公共组件
│   │   ├── directives/            # 自定义指令
│   │   ├── router/                # 路由配置
│   │   ├── utils/                 # 工具函数
│   │   ├── views/                 # 页面视图
│   │   │   ├── login.vue          # 登录
│   │   │   ├── register.vue       # 注册
│   │   │   ├── dashboard.vue      # 仪表盘
│   │   │   ├── dm.vue             # DM 管理
│   │   │   ├── dm-schedule.vue    # DM 排班
│   │   │   ├── script.vue         # 剧本管理
│   │   │   ├── session.vue        # 场次管理
│   │   │   ├── order.vue          # 订单管理
│   │   │   ├── reservation.vue    # 预约管理
│   │   │   ├── evaluation.vue     # 评价管理
│   │   │   ├── notification.vue   # 通知中心
│   │   │   ├── statistics.vue     # 数据统计
│   │   │   ├── operation-log.vue  # 操作日志
│   │   │   ├── store.vue          # 门店管理
│   │   │   └── profile.vue        # 个人中心
│   │   ├── App.vue
│   │   └── main.js
│   └── package.json
│
└── .gitignore
```

## 🚀 快速开始

### 环境要求

- JDK 1.8+
- Node.js 14+
- MySQL 8.0+
- Maven 3.6+

### 1. 初始化数据库

创建 MySQL 数据库：

```sql
CREATE DATABASE dm_scheduler DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
```

执行初始化脚本（如有）：

```bash
mysql -u root -p dm_scheduler < dm-scheduler/scripts/reset-real-test-data.sql
```

### 2. 启动后端

```bash
cd dm-scheduler

# 修改数据库配置（如需）
# 编辑 src/main/resources/application.yml 中的数据库连接信息

# 构建并运行
mvn spring-boot:run
```

后端服务默认运行在 `http://localhost:8081`

数据库连接信息通过环境变量配置：
- `DB_USERNAME`：数据库用户名（默认 root）
- `DB_PASSWORD`：数据库密码（默认空）

### 3. 启动前端

```bash
cd dm-scheduler-web

# 安装依赖
npm install

# 启动开发服务器
npm run serve
```

前端开发服务器默认运行在 `http://localhost:8080`

## 📡 API 文档

启动后端后，访问 SpringDoc OpenAPI 文档：

```
http://localhost:8081/swagger-ui.html
```

## 🧪 测试

```bash
cd dm-scheduler-web

# 单元测试
npm run test:unit

# E2E 测试 (Playwright)
npm run test:e2e

# E2E 测试 (Cypress)
npm run test:e2e:cypress
```

## 📦 构建部署

### 前端构建

```bash
cd dm-scheduler-web
npm run build
```

构建产物输出至 `dist/` 目录，可部署至 Nginx 等 Web 服务器。

### 后端构建

```bash
cd dm-scheduler
mvn clean package -DskipTests
```

生成可执行 JAR 包，通过 `java -jar` 运行。

## 📄 License

本项目仅供学习交流使用。
