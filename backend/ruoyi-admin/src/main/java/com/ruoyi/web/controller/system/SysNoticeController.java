package com.ruoyi.web.controller.system;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.system.domain.SysNotice;
import com.ruoyi.system.service.ISysNoticeService;
import com.ruoyi.system.mapper.SysNoticeScopeMapper;

/**
 * 公告 信息操作处理
 * 
 * @author ruoyi
 */
@RestController
@RequestMapping("/system/notice")
public class SysNoticeController extends BaseController
{
    @Autowired
    private ISysNoticeService noticeService;

    @Autowired
    private SysNoticeScopeMapper noticeScopeMapper;

    /**
     * 获取通知公告列表
     */
    @PreAuthorize("@ss.hasPermi('system:notice:list')")
    @GetMapping("/list")
    public TableDataInfo list(SysNotice notice)
    {
        startPage();
        List<SysNotice> list = noticeService.selectNoticeList(notice);
        return getDataTable(list);
    }

    /**
     * 获取用户可见的通知公告列表
     */
    @GetMapping("/user/list")
    public TableDataInfo userList(SysNotice notice)
    {
        startPage();
        List<SysNotice> list = noticeService.selectUserNoticeList(notice, SecurityUtils.getUserId());
        return getDataTable(list);
    }

    /**
     * 根据通知公告编号获取详细信息
     */
    @PreAuthorize("@ss.hasPermi('system:notice:query')")
    @GetMapping(value = "/{noticeId}")
    public AjaxResult getInfo(@PathVariable Long noticeId)
    {
        return success(noticeService.selectNoticeById(noticeId));
    }

    /**
     * 学员端获取公告详情（带权限验证）
     */
    @GetMapping("/user/{noticeId}")
    public AjaxResult getUserNoticeInfo(@PathVariable Long noticeId)
    {
        Long userId = SecurityUtils.getUserId();
        
        // 获取用户可见的公告ID列表
        List<Long> visibleNoticeIds = noticeScopeMapper.selectNoticeIdsByUserId(userId);
        
        // 验证用户是否有权查看此公告
        if (!visibleNoticeIds.contains(noticeId)) {
            return error("无权查看此公告");
        }
        
        // 用户有权限，返回公告详情
        SysNotice notice = noticeService.selectNoticeById(noticeId);
        if (notice == null) {
            return error("公告不存在");
        }
        
        return success(notice);
    }

    /**
     * 新增通知公告
     */
    @PreAuthorize("@ss.hasPermi('system:notice:add')")
    @Log(title = "通知公告", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody SysNotice notice)
    {
        notice.setCreateBy(SecurityUtils.getUsername());
        return toAjax(noticeService.insertNotice(notice));
    }

    /**
     * 修改通知公告
     */
    @PreAuthorize("@ss.hasPermi('system:notice:edit')")
    @Log(title = "通知公告", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody SysNotice notice)
    {
        notice.setUpdateBy(SecurityUtils.getUsername());
        return toAjax(noticeService.updateNotice(notice));
    }

    /**
     * 删除通知公告
     */
    @PreAuthorize("@ss.hasPermi('system:notice:remove')")
    @Log(title = "通知公告", businessType = BusinessType.DELETE)
    @DeleteMapping("/{noticeIds}")
    public AjaxResult remove(@PathVariable Long[] noticeIds)
    {
        return toAjax(noticeService.deleteNoticeByIds(noticeIds));
    }

    /**
     * 标记公告为已读
     */
    @PostMapping("/read/{noticeId}")
    public AjaxResult markAsRead(@PathVariable Long noticeId)
    {
        return toAjax(noticeService.markAsRead(noticeId, SecurityUtils.getUserId()));
    }

    /**
     * 获取未读公告数量
     */
    @GetMapping("/unread/count")
    public AjaxResult getUnreadCount()
    {
        return success(noticeService.selectUnreadCount(SecurityUtils.getUserId()));
    }
}
