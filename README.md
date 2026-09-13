# 考试培训系统

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE) [![Vue](https://img.shields.io/badge/Vue-2.x-42b883.svg)](https://vuejs.org/) [![Java](https://img.shields.io/badge/Java-8%2B-ed8b00.svg)](https://www.java.com/)

基于 RuoYi 的考试培训系统，连接题库、组卷、考试、成绩查询和培训学习流程，提供管理端、学员端和移动端接口。

> 本项目提供通用考试业务能力，不包含真实题库、学员资料、身份证信息、生产数据库、上传文件或部署配置。题库口径、权限和考试规则需要在目标环境单独验收。

## 功能

- **题库管理**：题目分类、题型、难度、分值、解析和批量维护。
- **组卷与考试**：固定/随机组卷、考试发布、作答、交卷、成绩和考试状态管理。
- **培训学习**：课程、课件、学习记录和学员侧入口。
- **管理平台**：用户、角色、菜单、字典、操作日志和定时任务。
- **移动端接口**：为 H5 或其他客户端提供登录、考试和成绩查询接口。

## 技术栈

| 层 | 技术 |
| --- | --- |
| 前端 | Vue 2、Vue CLI、Element UI、Axios |
| 后端 | Java、Spring Boot、Spring Security、MyBatis、JWT |
| 平台 | RuoYi、MySQL、Redis、Maven |

## 快速开始

### 环境要求

Git、JDK 8+、Maven、Node.js、npm、MySQL 8+ 和 Redis 6+。

### 配置与启动

后端配置位于 `backend/ruoyi-admin/src/main/resources/application.yml`，数据库、Redis 和 Token 密钥通过环境变量提供。不要把真实凭据写入配置文件。

```bash
mvn -f backend/pom.xml -DskipTests package
cd frontend
npm install
npm run dev
```

本公开副本不提供数据库初始化脚本；请在本地准备专用数据库，并按目标环境补充结构和虚构演示数据。

## 测试

```bash
mvn -f backend/pom.xml -DskipTests package
npm --prefix frontend run build:prod
```

完整登录、考试和培训链路需要专用测试数据库及测试账号，不能用生产数据代替。

## 项目结构

```text
├── backend/       # Spring Boot 多模块后端
├── frontend/      # 管理端、学员端和移动端页面
├── scripts/       # 通用维护脚本
├── LICENSE
└── THIRD_PARTY_NOTICES.md
```

## 安全边界

数据库密码、JWT 密钥、初始管理员密码、身份证和学员资料只能通过本地环境注入。生产环境还需配置 HTTPS、备份恢复、权限审计、上传目录隔离和漏洞扫描。

## 参与贡献

欢迎提交 Issue 和 Pull Request。请不要提交真实题库、个人信息、凭据、生产配置、上传文件或构建产物；提交 PR 时说明实际运行的检查和未覆盖范围。

## 许可证

本项目及其自有代码采用 MIT 许可证。RuoYi 和其他依赖的版权与许可证见 [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md) 及各自目录中的原始文件。
