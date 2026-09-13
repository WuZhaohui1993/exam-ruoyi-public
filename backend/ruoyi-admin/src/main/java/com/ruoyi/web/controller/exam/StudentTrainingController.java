package com.ruoyi.web.controller.exam;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.exam.training.domain.TrainingAttachment;
import com.ruoyi.exam.training.domain.TrainingItem;
import com.ruoyi.exam.training.service.ITrainingAttachmentService;
import com.ruoyi.exam.training.service.ITrainingItemService;
import com.ruoyi.exam.examination.domain.ExamUser;
import com.ruoyi.exam.examination.service.IExamUserService;

/**
 * 学员培训学习中心。
 */
@RestController
@RequestMapping("/student/training")
public class StudentTrainingController extends BaseController
{
    @Autowired
    private ITrainingItemService trainingItemService;

    @Autowired
    private ITrainingAttachmentService trainingAttachmentService;

    @Autowired
    private IExamUserService examUserService;

    @PreAuthorize("@ss.hasPermi('student:training:list')")
    @GetMapping("/summary")
    public AjaxResult summary()
    {
        return success(trainingItemService.selectStudentSummary(SecurityUtils.getUserId()));
    }

    @PreAuthorize("@ss.hasPermi('student:training:list')")
    @GetMapping("/courses")
    public AjaxResult courses()
    {
        return success(trainingItemService.selectPublishedLearningList("course"));
    }

    @PreAuthorize("@ss.hasPermi('student:training:list')")
    @GetMapping("/course/{courseId}/items")
    public AjaxResult courseItems(@PathVariable Long courseId)
    {
        TrainingItem course = trainingItemService.selectTrainingItemById(courseId);
        if (course == null || !"course".equals(course.getItemType()) || !"published".equals(course.getStatus()))
        {
            return error("课程不存在或未发布");
        }
        TrainingItem query = new TrainingItem();
        query.setParentId(courseId);
        query.setStatus("published");
        return success(trainingItemService.selectTrainingItemList(query));
    }

    @PreAuthorize("@ss.hasPermi('student:training:list')")
    @GetMapping("/course/{courseId}/progress")
    public AjaxResult courseProgress(@PathVariable Long courseId)
    {
        TrainingItem query = new TrainingItem();
        query.setItemType("progress");
        query.setParentId(courseId);
        query.setUserId(SecurityUtils.getUserId());
        List<TrainingItem> list = trainingItemService.selectTrainingItemList(query);
        return success(list.isEmpty() ? null : list.get(0));
    }

    @PreAuthorize("@ss.hasPermi('student:training:list')")
    @GetMapping("/resources")
    public AjaxResult resources(TrainingItem item)
    {
        item.setStatus("published");
        if (item.getItemType() == null)
        {
            item.setItemType("resource");
        }
        return success(trainingItemService.selectTrainingItemList(item));
    }

    @PreAuthorize("@ss.hasPermi('student:training:list')")
    @GetMapping("/certificates")
    public AjaxResult certificates()
    {
        TrainingItem query = new TrainingItem();
        query.setItemType("certificate");
        query.setUserId(SecurityUtils.getUserId());
        return success(trainingItemService.selectTrainingItemList(query));
    }

    @PreAuthorize("@ss.hasPermi('student:training:list')")
    @GetMapping("/qa")
    public TableDataInfo qaList()
    {
        TrainingItem query = new TrainingItem();
        query.setItemType("qa");
        query.setUserId(SecurityUtils.getUserId());
        startPage();
        List<TrainingItem> list = trainingItemService.selectTrainingItemList(query);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('student:training:learn')")
    @Log(title = "课程学习进度", businessType = BusinessType.UPDATE)
    @PostMapping("/course/{courseId}/progress")
    public AjaxResult markProgress(@PathVariable Long courseId, @RequestBody TrainingItem item)
    {
        return success(trainingItemService.markCourseProgress(courseId, SecurityUtils.getUserId(), getUsername(),
                item.getStatus(), item.getDurationMinutes()));
    }

    @PreAuthorize("@ss.hasPermi('student:training:learn') and @ss.hasPermi('student:exam:start')")
    @Log(title = "课后考试", businessType = BusinessType.UPDATE)
    @PostMapping("/course/{courseId}/exam/start")
    public AjaxResult startCourseExam(@PathVariable Long courseId)
    {
        TrainingItem course = trainingItemService.selectTrainingItemById(courseId);
        if (course == null || !"course".equals(course.getItemType()) || course.getRelatedExamId() == null)
        {
            return error("课程未绑定课后考试");
        }
        ExamUser examUser = examUserService.startStudentExam(course.getRelatedExamId(), SecurityUtils.getUserId(), SecurityUtils.getDeptId());
        AjaxResult ajax = success(examUser);
        ajax.put("examId", course.getRelatedExamId());
        ajax.put("examUserId", examUser.getId());
        return ajax;
    }

    @PreAuthorize("@ss.hasPermi('student:training:practice')")
    @Log(title = "自主练习", businessType = BusinessType.INSERT)
    @PostMapping("/practice")
    public AjaxResult recordPractice(@RequestBody TrainingItem item)
    {
        return success(trainingItemService.recordPractice(SecurityUtils.getUserId(), getUsername(), item));
    }

    @PreAuthorize("@ss.hasPermi('student:training:practice')")
    @GetMapping("/practice/list")
    public TableDataInfo practiceList()
    {
        TrainingItem query = new TrainingItem();
        query.setItemType("practice");
        query.setUserId(SecurityUtils.getUserId());
        startPage();
        List<TrainingItem> list = trainingItemService.selectTrainingItemList(query);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('student:training:qa')")
    @Log(title = "互动问答", businessType = BusinessType.INSERT)
    @PostMapping("/qa")
    public AjaxResult askQuestion(@Validated @RequestBody TrainingItem item)
    {
        return success(trainingItemService.askQuestion(SecurityUtils.getUserId(), getUsername(), item));
    }

    @PreAuthorize("@ss.hasPermi('student:training:attachment')")
    @GetMapping("/attachment/list")
    public TableDataInfo attachmentList(TrainingAttachment attachment)
    {
        attachment.setUserId(SecurityUtils.getUserId());
        startPage();
        List<TrainingAttachment> list = trainingAttachmentService.selectTrainingAttachmentList(attachment);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('student:training:attachment')")
    @Log(title = "学员作答附件", businessType = BusinessType.INSERT)
    @PostMapping("/attachment")
    public AjaxResult addAttachment(@Validated @RequestBody TrainingAttachment attachment)
    {
        attachment.setUserId(SecurityUtils.getUserId());
        attachment.setCreateBy(getUsername());
        return toAjax(trainingAttachmentService.insertTrainingAttachment(attachment));
    }
}
