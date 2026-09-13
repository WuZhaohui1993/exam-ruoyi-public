package com.ruoyi.web.controller.exam;

import java.util.List;
import javax.servlet.http.HttpServletResponse;
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
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.exam.training.domain.TrainingAttachment;
import com.ruoyi.exam.training.domain.TrainingItem;
import com.ruoyi.exam.training.service.ITrainingAttachmentService;
import com.ruoyi.exam.training.service.ITrainingItemService;

/**
 * 培训运营管理。
 */
@RestController
@RequestMapping("/training/manage")
public class TrainingManageController extends BaseController
{
    @Autowired
    private ITrainingItemService trainingItemService;

    @Autowired
    private ITrainingAttachmentService trainingAttachmentService;

    @PreAuthorize("@ss.hasPermi('training:item:list')")
    @GetMapping("/list")
    public TableDataInfo list(TrainingItem item)
    {
        startPage();
        List<TrainingItem> list = trainingItemService.selectTrainingItemList(item);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('training:item:list')")
    @GetMapping("/learning-ledger")
    public TableDataInfo learningLedger(TrainingItem item)
    {
        startPage();
        List<TrainingItem> list = trainingItemService.selectLearningLedgerList(item);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('training:item:query')")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable Long id)
    {
        return success(trainingItemService.selectTrainingItemById(id));
    }

    @PreAuthorize("@ss.hasPermi('training:item:add')")
    @Log(title = "培训运营", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody TrainingItem item)
    {
        item.setCreateBy(getUsername());
        return toAjax(trainingItemService.insertTrainingItems(item));
    }

    @PreAuthorize("@ss.hasPermi('training:item:edit')")
    @Log(title = "培训运营", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody TrainingItem item)
    {
        item.setUpdateBy(getUsername());
        return toAjax(trainingItemService.updateTrainingItem(item));
    }

    @PreAuthorize("@ss.hasPermi('training:item:remove')")
    @Log(title = "培训运营", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(trainingItemService.deleteTrainingItemByIds(ids));
    }

    @PreAuthorize("@ss.hasPermi('training:item:export')")
    @Log(title = "培训运营", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, TrainingItem item)
    {
        List<TrainingItem> list = trainingItemService.selectTrainingItemList(item);
        ExcelUtil<TrainingItem> util = new ExcelUtil<TrainingItem>(TrainingItem.class);
        util.exportExcel(response, list, "培训运营数据");
    }

    @PreAuthorize("@ss.hasPermi('training:item:statistics')")
    @GetMapping("/summary")
    public AjaxResult summary()
    {
        return success(trainingItemService.selectAdminSummary());
    }

    @PreAuthorize("@ss.hasPermi('training:item:list')")
    @GetMapping("/options/courses")
    public AjaxResult courseOptions(String keyword)
    {
        return success(trainingItemService.selectCourseOptions(keyword));
    }

    @PreAuthorize("@ss.hasPermi('training:item:list')")
    @GetMapping("/options/exams")
    public AjaxResult examOptions(String keyword)
    {
        return success(trainingItemService.selectExamOptions(keyword));
    }

    @PreAuthorize("@ss.hasPermi('training:item:list')")
    @GetMapping("/options/students")
    public AjaxResult studentOptions(String keyword)
    {
        return success(trainingItemService.selectStudentOptions(keyword));
    }

    @PreAuthorize("@ss.hasPermi('training:item:edit')")
    @Log(title = "培训问答回复", businessType = BusinessType.UPDATE)
    @PutMapping("/qa/{id}/reply")
    public AjaxResult replyQuestion(@PathVariable Long id, @RequestBody TrainingItem item)
    {
        if (StringUtils.isEmpty(item.getAnswerContent()))
        {
            return error("回复内容不能为空");
        }
        return toAjax(trainingItemService.replyQuestion(id, item.getAnswerContent(), getUsername()));
    }

    @PreAuthorize("@ss.hasPermi('training:item:add')")
    @Log(title = "发放证书", businessType = BusinessType.INSERT)
    @PostMapping("/certificate")
    public AjaxResult issueCertificate(@Validated @RequestBody TrainingItem item)
    {
        if (item.getUserId() == null && (item.getUserIds() == null || item.getUserIds().length == 0))
        {
            return error("发证学员不能为空");
        }
        int rows = trainingItemService.issueCertificates(item, getUsername());
        return success("证书已发放 " + rows + " 人");
    }

    @PreAuthorize("@ss.hasPermi('training:item:edit')")
    @Log(title = "处理媒体资源", businessType = BusinessType.UPDATE)
    @PostMapping("/media/{id}/process")
    public AjaxResult processMedia(@PathVariable Long id)
    {
        return toAjax(trainingItemService.processMediaResource(id, getUsername()));
    }

    @PreAuthorize("@ss.hasPermi('training:attachment:list')")
    @GetMapping("/attachment/list")
    public TableDataInfo attachmentList(TrainingAttachment attachment)
    {
        startPage();
        List<TrainingAttachment> list = trainingAttachmentService.selectTrainingAttachmentList(attachment);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('training:attachment:remove')")
    @Log(title = "作答附件", businessType = BusinessType.DELETE)
    @DeleteMapping("/attachment/{ids}")
    public AjaxResult removeAttachment(@PathVariable Long[] ids)
    {
        return toAjax(trainingAttachmentService.deleteTrainingAttachmentByIds(ids));
    }
}
