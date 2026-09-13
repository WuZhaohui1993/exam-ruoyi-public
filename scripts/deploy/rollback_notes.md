# 生产回滚和备份要点

1. 发布前备份数据库，至少覆盖 `exam_*`、`sys_menu`、`sys_role_menu`、`sys_config`。
2. 发布前保存当前 `ruoyi-admin.jar`、前端 `dist` 和 Nginx 配置。
3. 若新版本启动失败，先恢复旧 jar 和旧 dist，再执行数据库回滚脚本或从备份恢复。
4. 执行 `backend/sql/20260523_release_readiness_checks.sql` 确认状态、索引、菜单权限和培训扩展表。
5. 培训扩展涉及视频封面时，生产服务器需要安装 `ffmpeg`；未安装不会阻断系统运行，但媒体处理状态会显示 `skipped`。
