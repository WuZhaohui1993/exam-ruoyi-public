package com.ruoyi.system.domain;

import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 通知公告接收范围表 sys_notice_scope
 */
public class SysNoticeScope extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 范围ID */
    private Long scopeId;

    /** 公告ID */
    private Long noticeId;

    /** 范围类型（1部门 2用户组 3个人） */
    private String scopeType;

    /** 目标ID */
    private Long targetId;

    public Long getScopeId()
    {
        return scopeId;
    }

    public void setScopeId(Long scopeId)
    {
        this.scopeId = scopeId;
    }

    public Long getNoticeId()
    {
        return noticeId;
    }

    public void setNoticeId(Long noticeId)
    {
        this.noticeId = noticeId;
    }

    public String getScopeType()
    {
        return scopeType;
    }

    public void setScopeType(String scopeType)
    {
        this.scopeType = scopeType;
    }

    public Long getTargetId()
    {
        return targetId;
    }

    public void setTargetId(Long targetId)
    {
        this.targetId = targetId;
    }
} 