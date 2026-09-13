package com.ruoyi.system.mapper;

import java.util.List;
import com.ruoyi.system.domain.SysNoticeRead;

/**
 * 通知公告阅读状态 数据层
 */
public interface SysNoticeReadMapper
{
    /**
     * 新增阅读记录
     * 
     * @param noticeRead 阅读记录
     * @return 结果
     */
    public int insertNoticeRead(SysNoticeRead noticeRead);

    /**
     * 查询用户阅读状态
     * 
     * @param userId 用户ID
     * @param noticeId 公告ID
     * @return 阅读记录
     */
    public SysNoticeRead selectNoticeRead(@org.apache.ibatis.annotations.Param("userId") Long userId, @org.apache.ibatis.annotations.Param("noticeId") Long noticeId);

    /**
     * 查询用户未读公告数量
     * 
     * @param userId 用户ID
     * @return 未读数量
     */
    public int selectUnreadCount(Long userId);

    /**
     * 查询用户的阅读记录
     * 
     * @param userId 用户ID
     * @return 阅读记录列表
     */
    public List<SysNoticeRead> selectReadListByUserId(Long userId);
} 