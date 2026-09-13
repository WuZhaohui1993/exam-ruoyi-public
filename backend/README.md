# 考试培训系统后端说明

> 后端基于 RuoYi-Vue、Spring Boot 2.5.15、Spring Security、MyBatis、MySQL、Redis。
> 当前状态：构建通过，P0/P1 和整改阶段代码侧任务已收口；结构化题目媒体、培训扩展基础闭环、健康检查和生产配置模板已完成本地验证。真实生产配置落值、生产目标库约束、正式域名和真机媒体/附件回归仍需上线前完成。

## 目录说明

- `ruoyi-admin/`：启动模块、Web Controller、应用配置。
- `ruoyi-exam/`：考试业务模块，包含试题、题目媒体、试卷、考试、学员考试记录、培训扩展和学员附件等核心逻辑。
- `ruoyi-framework/`：安全配置、权限、过滤器、框架能力。
- `ruoyi-system/`：若依系统管理模块。
- `ruoyi-common/`：通用工具。
- `sql/`：数据库初始化、菜单、角色和业务表脚本。

## 本地运行

首次克隆先准备本地连接配置：

```bash
cp backend/ruoyi-admin/src/main/resources/application-druid.example.yml backend/ruoyi-admin/src/main/resources/application-druid.yml
```

上面的复制命令在仓库根目录执行。按实际环境填写复制后的文件，或通过 `MYSQL_PASSWORD` 等环境变量注入密码。真实 `application-druid.yml` 已纳入忽略规则；已有工作区文件保留，本次归档不改变本地连接。生产继续使用外置配置和 `application-prod.yml`，不要上传本地连接文件。

```bash
cd backend
mvn clean package -Dmaven.test.skip=true
java -jar ruoyi-admin/target/ruoyi-admin.jar
```

本地默认数据库为 `ry-vue-exam-local`，连接信息以 `ruoyi-admin/src/main/resources/application-druid.yml` 为准。该配置仅用于开发调试，不得直接用于生产。

2026-05-23 已在父工程补齐 Spring Boot Maven 插件版本管理。日常联调仍优先使用构建后 jar 启动；需要调试时可在 `ruoyi-admin` 模块执行 `mvn spring-boot:run`。

本地调试账号：
- 管理员：`admin / 123456`
- 学员：`student001 / 123456`

## 构建

```bash
cd backend
mvn clean package -Dmaven.test.skip=true
```

2026-05-23 P1 推进后该命令复测通过，产物为 `ruoyi-admin/target/ruoyi-admin.jar`。

项目 `pom.xml` 目标 Java 版本为 8，仓库根目录 `.java-version` 固定为 `1.8`。生产构建建议使用 Java 8，或使用经完整回归验证的 Java 11/17，不建议直接用未验证的新版本 JDK。

## 上线前必须整改

- 生产配置模板已新增 `ruoyi-admin/src/main/resources/application-prod.yml`，真实生产值必须通过环境变量、配置中心或服务器标准路径落地。
- 不使用本地 MySQL、Redis、上传目录和默认 JWT secret。
- 生产模板默认关闭 Swagger、Druid；代码生成器访问由 `security.expose-dev-tools` 控制，正式公网不得开放。
- `spring.devtools.restart.enabled=false`。
- 日志级别不使用 `debug`。
- Druid 必须设置强密码、访问白名单或内网/VPN 限制。
- H5 二维码公开域名支持 `exam.h5.public-url` 固定公开域名和 `exam.h5.allowed-base-urls` 白名单；生产应配置公网访问地址。
- `exam_examination.status`、`exam_user.registration_status`、`exam_user.exam_status` 必须完成状态统一。
- `exam_user` 多次考试唯一约束必须为 `(exam_id, user_id, attempt_number)`。
- 重新发布考试只允许重置未开始的报名记录，不能清空已开始、已提交、已评分历史轮次的状态、分数和 `attempt_number`。
- 管理端重置考试只允许按 `examUserId` 重置单次考试记录，不能按 `examId + userId` 清空该考生所有历史轮次。
- 管理端 Controller 方法继续按上线审查补齐 `@PreAuthorize`。
- 学员端菜单权限、按钮权限和动态路由权限已统一为 `student:exam:start/result`；旧权限只允许出现在迁移 SQL 中。
- `/exam/*`、`/student/*` 富文本输入输出链路需要明确 XSS 策略。
- 题目媒体表 `exam_question_media` 已用于题干图片和主流 Web 视频，生产上线前需要真机验证图片展示、视频播放、上传大小和带宽策略。
- 培训扩展表 `exam_training_item`、`exam_training_attachment` 已用于课程、课件、资源、问答、证书、学分、练习、课后考试和学员附件基础闭环。
- 培训媒体资源可调用服务器 `ffmpeg` 生成封面；未安装或文件缺失时记录 `skipped/failed`，不阻断资源发布。
- `/health` 已提供 database、redis、version、status 健康检查，发布前应接入网关或监控平台。
- 发布前执行 `scripts/deploy/release_check.sh` 和 `sql/20260523_release_readiness_checks.sql`，确认本地运行文件、状态、索引、菜单权限、题目媒体和培训扩展表。

## 关键状态约定

`exam_examination.status`：
- `0` 草稿
- `1` 已发布
- `2` 进行中
- `3` 已结束
- `4` 已取消

`exam_user.registration_status`：
- `registered`
- `approved`
- `rejected`

`exam_user.exam_status`：
- `registered`
- `not_started`
- `in_progress`
- `submitted`
- `graded`

不允许继续混用 `draft/published/cancelled` 或学员考试数字状态。

## 培训扩展接口

管理端菜单入口：后台侧边栏 `培训运维 -> 培训运营`，真实路由 `/training/manage`。

管理端：
- `/training/manage/list`
- `/training/manage/summary`
- `/training/manage/qa/{id}/reply`
- `/training/manage/certificate`
- `/training/manage/media/{id}/process`
- `/training/manage/attachment/list`

学员端：
- `/student/training/summary`
- `/student/training/courses`
- `/student/training/resources`
- `/student/training/course/{courseId}/progress`
- `/student/training/course/{courseId}/exam/start`
- `/student/training/practice`
- `/student/training/qa`
- `/student/training/attachment`

## 参考文档

- `../readme/上线全量代码审查报告.md`
- `../readme/全功能模块测试报告.md`
- `../readme/project-progress.md`
- `sql/README_student_setup.md`
