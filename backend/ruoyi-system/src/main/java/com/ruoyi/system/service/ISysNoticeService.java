package com.ruoyi.system.service;

import java.util.List;
import com.ruoyi.system.domain.SysNotice;

/**
 * 公告 服务层
 * 
 * @author ruoyi
 */
public interface ISysNoticeService
{
    /**
     * 查询公告信息
     * 
     * @param noticeId 公告ID
     * @return 公告信息
     */
    public SysNotice selectNoticeById(Long noticeId);

    /**
     * 查询公告列表
     * 
     * @param notice 公告信息
     * @return 公告集合
     */
    public List<SysNotice> selectNoticeList(SysNotice notice);

    /**
     * 查询用户可见的公告列表
     * 
     * @param notice 公告信息
     * @param userId 用户ID
     * @return 公告集合
     */
    public List<SysNotice> selectUserNoticeList(SysNotice notice, Long userId);

    /**
     * 新增公告
     * 
     * @param notice 公告信息
     * @return 结果
     */
    public int insertNotice(SysNotice notice);

    /**
     * 修改公告
     * 
     * @param notice 公告信息
     * @return 结果
     */
    public int updateNotice(SysNotice notice);

    /**
     * 删除公告信息
     * 
     * @param noticeId 公告ID
     * @return 结果
     */
    public int deleteNoticeById(Long noticeId);
    
    /**
     * 批量删除公告信息
     * 
     * @param noticeIds 需要删除的公告ID
     * @return 结果
     */
    public int deleteNoticeByIds(Long[] noticeIds);

    /**
     * 查询用户未读公告数量
     * 
     * @param userId 用户ID
     * @return 未读数量
     */
    public int selectUnreadCount(Long userId);

    /**
     * 标记公告为已读
     * 
     * @param noticeId 公告ID
     * @param userId 用户ID
     * @return 结果
     */
    public int markAsRead(Long noticeId, Long userId);
}
