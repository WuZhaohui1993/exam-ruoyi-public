package com.ruoyi.system.service.impl;

import java.util.List;
import java.util.ArrayList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.system.domain.SysNotice;
import com.ruoyi.system.domain.SysNoticeRead;
import com.ruoyi.system.domain.SysNoticeScope;
import com.ruoyi.system.mapper.SysNoticeMapper;
import com.ruoyi.system.mapper.SysNoticeScopeMapper;
import com.ruoyi.system.mapper.SysNoticeReadMapper;
import com.ruoyi.system.service.ISysNoticeService;
import com.ruoyi.common.utils.html.HtmlSanitizer;

/**
 * 公告 服务层实现
 * 
 * @author ruoyi
 */
@Service
public class SysNoticeServiceImpl implements ISysNoticeService
{
    @Autowired
    private SysNoticeMapper noticeMapper;

    @Autowired
    private SysNoticeScopeMapper noticeScopeMapper;

    @Autowired
    private SysNoticeReadMapper noticeReadMapper;

    /**
     * 查询公告信息
     * 
     * @param noticeId 公告ID
     * @return 公告信息
     */
    @Override
    public SysNotice selectNoticeById(Long noticeId)
    {
        SysNotice notice = noticeMapper.selectNoticeById(noticeId);
        if (notice != null)
        {
            notice.setScopeList(noticeScopeMapper.selectScopeByNoticeId(noticeId));
        }
        return notice;
    }

    /**
     * 查询公告列表
     * 
     * @param notice 公告信息
     * @return 公告集合
     */
    @Override
    public List<SysNotice> selectNoticeList(SysNotice notice)
    {
        return noticeMapper.selectNoticeList(notice);
    }

    /**
     * 查询用户可见的公告列表
     * 
     * @param notice 公告信息
     * @param userId 用户ID
     * @return 公告集合
     */
    @Override
    public List<SysNotice> selectUserNoticeList(SysNotice notice, Long userId)
    {
        List<Long> noticeIds = noticeScopeMapper.selectNoticeIdsByUserId(userId);
        if (noticeIds != null && !noticeIds.isEmpty())
        {
            notice.setNoticeIds(noticeIds);
            // 使用带阅读状态的查询方法
            return noticeMapper.selectUserNoticeListWithReadStatus(notice, userId);
        }
        return new ArrayList<>();
    }

    /**
     * 新增公告
     * 
     * @param notice 公告信息
     * @return 结果
     */
    @Override
    @Transactional
    public int insertNotice(SysNotice notice)
    {
        sanitizeNotice(notice);
        int rows = noticeMapper.insertNotice(notice);
        if (notice.getScopeList() != null && !notice.getScopeList().isEmpty())
        {
            // 设置生成的noticeId到每个scope对象中
            for (SysNoticeScope scope : notice.getScopeList())
            {
                scope.setNoticeId(notice.getNoticeId());
            }
            noticeScopeMapper.batchInsertScope(notice.getScopeList());
        }
        return rows;
    }

    /**
     * 修改公告
     * 
     * @param notice 公告信息
     * @return 结果
     */
    @Override
    @Transactional
    public int updateNotice(SysNotice notice)
    {
        sanitizeNotice(notice);
        noticeScopeMapper.deleteScopeByNoticeId(notice.getNoticeId());
        if (notice.getScopeList() != null && !notice.getScopeList().isEmpty())
        {
            // 确保每个scope对象都有正确的noticeId
            for (SysNoticeScope scope : notice.getScopeList())
            {
                scope.setNoticeId(notice.getNoticeId());
            }
            noticeScopeMapper.batchInsertScope(notice.getScopeList());
        }
        return noticeMapper.updateNotice(notice);
    }

    private void sanitizeNotice(SysNotice notice)
    {
        if (notice == null)
        {
            return;
        }
        notice.setNoticeTitle(HtmlSanitizer.cleanText(notice.getNoticeTitle()));
        notice.setNoticeContent(HtmlSanitizer.cleanRichText(notice.getNoticeContent()));
    }

    /**
     * 删除公告对象
     * 
     * @param noticeId 公告ID
     * @return 结果
     */
    @Override
    @Transactional
    public int deleteNoticeById(Long noticeId)
    {
        noticeScopeMapper.deleteScopeByNoticeId(noticeId);
        return noticeMapper.deleteNoticeById(noticeId);
    }

    /**
     * 批量删除公告信息
     * 
     * @param noticeIds 需要删除的公告ID
     * @return 结果
     */
    @Override
    @Transactional
    public int deleteNoticeByIds(Long[] noticeIds)
    {
        for (Long noticeId : noticeIds)
        {
            noticeScopeMapper.deleteScopeByNoticeId(noticeId);
        }
        return noticeMapper.deleteNoticeByIds(noticeIds);
    }

    /**
     * 查询用户未读公告数量
     * 
     * @param userId 用户ID
     * @return 未读数量
     */
    @Override
    public int selectUnreadCount(Long userId)
    {
        return noticeReadMapper.selectUnreadCount(userId);
    }

    /**
     * 标记公告为已读
     * 
     * @param noticeId 公告ID
     * @param userId 用户ID
     * @return 结果
     */
    @Override
    public int markAsRead(Long noticeId, Long userId)
    {
        try {
            SysNoticeRead noticeRead = noticeReadMapper.selectNoticeRead(userId, noticeId);
            if (noticeRead == null)
            {
                noticeRead = new SysNoticeRead();
                noticeRead.setNoticeId(noticeId);
                noticeRead.setUserId(userId);
                return noticeReadMapper.insertNoticeRead(noticeRead);
            }
            // 如果已经存在阅读记录，返回1表示操作成功（幂等性）
            return 1;
        } catch (Exception e) {
            // 处理数据库唯一约束异常（可能是并发插入）
            if (e.getMessage() != null && e.getMessage().contains("uk_notice_user")) {
                // 唯一约束冲突，说明已经存在记录，返回成功
                return 1;
            }
            throw e;
        }
    }
}
