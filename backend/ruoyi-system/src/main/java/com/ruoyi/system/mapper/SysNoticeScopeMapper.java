package com.ruoyi.system.mapper;

import java.util.List;
import com.ruoyi.system.domain.SysNoticeScope;

/**
 * 通知公告接收范围 数据层
 */
public interface SysNoticeScopeMapper
{
    /**
     * 批量新增公告接收范围
     * 
     * @param scopeList 公告接收范围列表
     * @return 结果
     */
    public int batchInsertScope(List<SysNoticeScope> scopeList);

    /**
     * 删除公告接收范围
     * 
     * @param noticeId 公告ID
     * @return 结果
     */
    public int deleteScopeByNoticeId(Long noticeId);

    /**
     * 查询公告接收范围
     * 
     * @param noticeId 公告ID
     * @return 接收范围列表
     */
    public List<SysNoticeScope> selectScopeByNoticeId(Long noticeId);

    /**
     * 查询用户可见的公告ID列表
     * 
     * @param userId 用户ID
     * @return 公告ID列表
     */
    public List<Long> selectNoticeIdsByUserId(Long userId);
} 