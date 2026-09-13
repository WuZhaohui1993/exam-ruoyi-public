package com.ruoyi.system.domain;

import java.util.Date;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 通知公告阅读状态表 sys_notice_read
 */
public class SysNoticeRead extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 阅读ID */
    private Long readId;

    /** 公告ID */
    private Long noticeId;

    /** 用户ID */
    private Long userId;

    /** 阅读时间 */
    private Date readTime;

    public Long getReadId() {
        return readId;
    }

    public void setReadId(Long readId) {
        this.readId = readId;
    }

    public Long getNoticeId() {
        return noticeId;
    }

    public void setNoticeId(Long noticeId) {
        this.noticeId = noticeId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Date getReadTime() {
        return readTime;
    }

    public void setReadTime(Date readTime) {
        this.readTime = readTime;
    }
} 