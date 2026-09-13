package com.ruoyi.web.controller.exam;

import static com.ruoyi.exam.constant.ExamStatusConstants.USER_EXAM_GRADED;
import static com.ruoyi.exam.constant.ExamStatusConstants.USER_EXAM_SUBMITTED;

import java.math.BigDecimal;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.exam.examination.domain.ExamExamination;
import com.ruoyi.exam.examination.domain.ExamUser;
import com.ruoyi.exam.examination.domain.ExamAnswerRecord;
import com.ruoyi.exam.examination.domain.ExportScoreDetailsDTO;
import com.ruoyi.exam.examination.service.IExamExaminationService;
import com.ruoyi.exam.examination.service.IExamAnswerRecordService;
import com.ruoyi.exam.examination.service.IExamUserService;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.system.service.ISysUserService;
import com.ruoyi.common.core.domain.entity.SysUser;

/**
 * 考试发布Controller
 *
 * @author ruoyi
 * @date 2024-06-10
 */
@RestController
@RequestMapping("/exam/examination")
public class ExamExaminationController extends BaseController {
    private static final Logger logger = LoggerFactory.getLogger(ExamExaminationController.class);

    @Value("${exam.h5.public-url:}")
    private String h5PublicUrl;

    @Value("${exam.h5.allowed-base-urls:}")
    private String h5AllowedBaseUrls;

    @Autowired
    private IExamExaminationService examExaminationService;

    @Autowired
    private IExamAnswerRecordService examAnswerRecordService;

    @Autowired
    private IExamUserService examUserService;

    @Autowired
    private ISysUserService userService;

    /**
     * 查询考试发布列表
     */
    @PreAuthorize("@ss.hasPermi('exam:examination:list')")
    @GetMapping("/list")
    public TableDataInfo list(ExamExamination examExamination) {
        startPage();
        List<ExamExamination> list = examExaminationService.selectExamExaminationList(examExamination);
        return getDataTable(list);
    }

    /**
     * 导出考试发布列表
     */
    @PreAuthorize("@ss.hasPermi('exam:examination:export')")
    @Log(title = "考试发布", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, ExamExamination examExamination) {
        List<ExamExamination> list = examExaminationService.selectExamExaminationList(examExamination);
        ExcelUtil<ExamExamination> util = new ExcelUtil<ExamExamination>(ExamExamination.class);
        util.exportExcel(response, list, "考试发布数据");
    }

    /**
     * 获取考试发布详细信息
     */
    @PreAuthorize("@ss.hasPermi('exam:examination:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        ExamExamination examination = examExaminationService.selectExamExaminationById(id);
        if (examination == null) {
            return error("考试不存在");
        }
        // 获取关联的用户ID列表
        List<Long> userIds = examExaminationService.getExamUserIds(id);
        examination.setExamUserIds(userIds);
        examination.setExamDeptIds(examExaminationService.getExamDeptIds(id));
        return success(examination);
    }

    /**
     * 获取考试发布详细信息（包含统计数据）
     */
    @PreAuthorize("@ss.hasPermi('exam:examination:query')")
    @GetMapping(value = "/details/{id}")
    public AjaxResult getDetails(@PathVariable("id") Long id) {
        return success(examExaminationService.selectExamExaminationWithDetails(id));
    }

    /**
     * 新增考试发布
     */
    @PreAuthorize("@ss.hasPermi('exam:examination:add')")
    @Log(title = "考试发布", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody ExamExamination examExamination) {
        if ("1".equals(examExaminationService.checkExamNameUnique(examExamination))) {
            return error("新增考试'" + examExamination.getExamName() + "'失败，考试名称已存在");
        }

        int result = examExaminationService.insertExamExamination(examExamination);
        if (result > 0) {
            // 返回成功信息，并包含考试ID
            return success("新增成功").put("data", examExamination.getId());
        } else {
            return error("新增失败");
        }
    }

    /**
     * 修改考试发布
     */
    @PreAuthorize("@ss.hasPermi('exam:examination:edit')")
    @Log(title = "考试发布", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody ExamExamination examExamination) {
        if ("1".equals(examExaminationService.checkExamNameUnique(examExamination))) {
            return error("修改考试'" + examExamination.getExamName() + "'失败，考试名称已存在");
        }
        return toAjax(examExaminationService.updateExamExamination(examExamination));
    }

    /**
     * 发布考试
     */
    @PreAuthorize("@ss.hasPermi('exam:examination:publish')")
    @Log(title = "考试发布", businessType = BusinessType.UPDATE)
    @PutMapping("/publish/{id}")
    public AjaxResult publish(@PathVariable("id") Long id) {
        return toAjax(examExaminationService.publishExamination(id));
    }

    /**
     * 取消考试
     */
    @PreAuthorize("@ss.hasPermi('exam:examination:cancel')")
    @Log(title = "考试取消", businessType = BusinessType.UPDATE)
    @PutMapping("/cancel/{id}")
    public AjaxResult cancel(@PathVariable("id") Long id) {
        return toAjax(examExaminationService.cancelExamination(id));
    }

    /**
     * 恢复为草稿
     */
    @PreAuthorize("@ss.hasPermi('exam:examination:edit')")
    @Log(title = "恢复草稿", businessType = BusinessType.UPDATE)
    @PutMapping("/restoreDraft/{id}")
    public AjaxResult restoreDraft(@PathVariable("id") Long id) {
        return toAjax(examExaminationService.restoreDraftExamination(id));
    }

    /**
     * 开始考试
     */
    @PreAuthorize("@ss.hasAnyPermi('exam:examination:start,exam:examination:manage')")
    @Log(title = "开始考试", businessType = BusinessType.UPDATE)
    @PutMapping("/start/{id}")
    public AjaxResult start(@PathVariable("id") Long id) {
        return toAjax(examExaminationService.startExamination(id));
    }

    /**
     * 结束考试
     */
    @PreAuthorize("@ss.hasAnyPermi('exam:examination:end,exam:examination:manage')")
    @Log(title = "结束考试", businessType = BusinessType.UPDATE)
    @PutMapping("/end/{id}")
    public AjaxResult end(@PathVariable("id") Long id) {
        return toAjax(examExaminationService.endExamination(id));
    }

    /**
     * 发布考试成绩
     */
    @PreAuthorize("@ss.hasPermi('exam:examination:publishResults')")
    @Log(title = "发布考试成绩", businessType = BusinessType.UPDATE)
    @PutMapping("/publishResults/{id}")
    public AjaxResult publishResults(@PathVariable("id") Long id) {
        return toAjax(examExaminationService.publishExamResults(id));
    }

    /**
     * 重新评分（修复判断题评分问题）
     */
    @PreAuthorize("@ss.hasPermi('exam:examination:grade')")
    @Log(title = "重新评分", businessType = BusinessType.UPDATE)
    @PutMapping("/regrade/{examId}")
    public AjaxResult regradeExam(@PathVariable("examId") Long examId) {
        try {
            // 获取该考试的所有已提交的考试用户
            List<ExamUser> examUsers = examUserService.selectExamUserListByExamId(examId);
            int regradeCount = 0;

            for (ExamUser examUser : examUsers) {
                // 只对已提交或已评分的用户重新评分
                if (USER_EXAM_SUBMITTED.equals(examUser.getExamStatus())
                        || USER_EXAM_GRADED.equals(examUser.getExamStatus())) {
                    // 执行自动评分
                    int gradedCount = examAnswerRecordService.autoGrade(examUser.getId());

                    // 重新计算总分
                    if (gradedCount >= 0) {
                        examAnswerRecordService.calculateAndUpdateScore(examUser.getId());
                        regradeCount++;
                    }
                }
            }

            return success("重新评分完成，共处理 " + regradeCount + " 个用户的答题记录");

        } catch (Exception e) {
            logger.error("重新评分失败", e);
            return error("重新评分失败：" + e.getMessage());
        }
    }

    /**
     * 关联证书模板
     */
    @PreAuthorize("@ss.hasPermi('exam:examination:edit')")
    @Log(title = "关联证书模板", businessType = BusinessType.UPDATE)
    @PutMapping("/associateCertificate/{examId}/{certificateTemplateId}")
    public AjaxResult associateCertificate(@PathVariable("examId") Long examId,
            @PathVariable("certificateTemplateId") Long certificateTemplateId) {
        return toAjax(examExaminationService.associateCertificate(examId, certificateTemplateId));
    }

    /**
     * 获取考试统计信息
     */
    @PreAuthorize("@ss.hasPermi('exam:examination:query')")
    @GetMapping("/statistics/{id}")
    public AjaxResult getStatistics(@PathVariable("id") Long id) {
        return success(examExaminationService.getExamStatistics(id));
    }

    /**
     * 获取业务概览统计信息（今日、本周、本月）
     */
    @PreAuthorize("@ss.hasAnyPermi('exam:examination:query,exam:examination:statistics')")
    @GetMapping("/statistics/business")
    public AjaxResult getBusinessStatistics() {
        return success(examExaminationService.selectBusinessStatistics());
    }

    /**
     * 导出成绩统计
     */
    @PreAuthorize("@ss.hasPermi('exam:examination:export')")
    @Log(title = "导出成绩统计", businessType = BusinessType.EXPORT)
    @PostMapping("/exportScoreStatistics/{examId}")
    public void exportScoreStatistics(HttpServletResponse response,
            @PathVariable("examId") Long examId,
            @RequestBody(required = false) ExportScoreDetailsDTO params) {
        ExamUser examUser = buildScoreExportFilter(params);
        List<ExamUser> list = examExaminationService.exportScoreStatistics(examId, examUser);
        ExcelUtil<ExamUser> util = new ExcelUtil<ExamUser>(ExamUser.class);
        util.exportExcel(response, list, "成绩统计数据");
    }

    /**
     * 导出详细成绩
     */
    @PreAuthorize("@ss.hasPermi('exam:examination:export')")
    @Log(title = "导出详细成绩", businessType = BusinessType.EXPORT)
    @PostMapping("/exportScoreDetails/{examId}")
    public void exportScoreDetails(HttpServletResponse response,
            @PathVariable("examId") Long examId,
            @RequestBody(required = false) ExportScoreDetailsDTO params) {
        ExamUser examUser = buildScoreExportFilter(params);

        // 使用扁平化导出方法
        List<com.ruoyi.exam.examination.domain.ExamScoreDetailExportDTO> list = examExaminationService
                .exportScoreDetailsFlat(examId, examUser);

        ExcelUtil<com.ruoyi.exam.examination.domain.ExamScoreDetailExportDTO> util = new ExcelUtil<com.ruoyi.exam.examination.domain.ExamScoreDetailExportDTO>(
                com.ruoyi.exam.examination.domain.ExamScoreDetailExportDTO.class);
        util.exportExcel(response, list, "详细成绩数据");
    }

    private ExamUser buildScoreExportFilter(ExportScoreDetailsDTO params) {
        ExamUser examUser = new ExamUser();
        if (params == null) {
            return examUser;
        }
        examUser.setIncludeAnswerDetails(params.getIncludeAnswerDetails());
        examUser.setUserId(params.getUserId());
        examUser.setUserIds(params.getUserIds());
        examUser.setDeptId(params.getDeptId());
        examUser.setSearchName(params.getSearchName());
        examUser.setIsPassed(params.getPassStatus());
        applyScoreRange(examUser, params.getScoreRange());
        return examUser;
    }

    private void applyScoreRange(ExamUser examUser, String scoreRange) {
        if (scoreRange == null || scoreRange.trim().isEmpty()) {
            return;
        }
        String[] range = scoreRange.split("-");
        if (range.length != 2) {
            return;
        }
        try {
            examUser.setMinScore(new BigDecimal(range[0].trim()));
            examUser.setMaxScore(new BigDecimal(range[1].trim()));
        } catch (NumberFormatException ignored) {
            // 非法筛选值直接忽略，避免导出接口因前端参数异常中断。
        }
    }

    /**
     * 查询用户可参与的考试列表
     */
    @PreAuthorize("@ss.hasAnyPermi('exam:examination:list,exam:examination:query,student:exam:list')")
    @GetMapping("/available")
    public TableDataInfo getAvailableExaminations() {
        Long userId = getUserId();
        List<ExamExamination> list = examExaminationService.selectAvailableExaminations(userId);
        return getDataTable(list);
    }

    /**
     * 删除考试发布
     */
    @PreAuthorize("@ss.hasPermi('exam:examination:remove')")
    @Log(title = "考试发布", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(examExaminationService.deleteExamExaminationByIds(ids));
    }

    // ==================== 考试人员管理 ====================

    /**
     * 添加考试人员
     */
    @PreAuthorize("@ss.hasPermi('exam:examination:addUsers')")
    @Log(title = "添加考试人员", businessType = BusinessType.INSERT)
    @PostMapping("/addUsers/{examId}")
    public AjaxResult addUsers(@PathVariable("examId") Long examId, @RequestBody Long[] userIds) {
        int count = examExaminationService.addExamUsers(examId, userIds);
        return success("成功添加 " + count + " 名考试人员");
    }

    /**
     * 移除考试人员
     */
    @PreAuthorize("@ss.hasPermi('exam:examination:removeUsers')")
    @Log(title = "移除考试人员", businessType = BusinessType.DELETE)
    @DeleteMapping("/removeUsers/{examId}")
    public AjaxResult removeUsers(@PathVariable("examId") Long examId, @RequestBody Long[] userIds) {
        return toAjax(examExaminationService.removeExamUsers(examId, userIds));
    }

    /**
     * 同步更新考试人员
     */
    @PreAuthorize("@ss.hasPermi('exam:examination:addUsers')")
    @Log(title = "同步更新考试人员", businessType = BusinessType.UPDATE)
    @PutMapping("/updateUsers/{examId}")
    public AjaxResult updateUsers(@PathVariable("examId") Long examId, @RequestBody Long[] userIds) {
        int count = examExaminationService.updateExamUsers(examId, userIds);
        return success("成功更新考试人员，当前共 " + count + " 名参考人员");
    }

    /**
     * 按部门添加考试人员
     */
    @PreAuthorize("@ss.hasPermi('exam:examination:addUsers')")
    @Log(title = "按部门添加考试人员", businessType = BusinessType.INSERT)
    @PostMapping("/addUsersByDept/{examId}")
    public AjaxResult addUsersByDept(@PathVariable("examId") Long examId, @RequestBody Long[] deptIds) {
        int count = examExaminationService.addExamUsersByDept(examId, deptIds);
        return success("成功按部门添加 " + count + " 名考试人员");
    }

    /**
     * 按部门批量添加考试人员（包含子部门）
     */
    @PreAuthorize("@ss.hasPermi('exam:examination:addUsers')")
    @Log(title = "按部门批量添加考试人员", businessType = BusinessType.INSERT)
    @PostMapping("/addUsersByDeptWithChildren/{examId}")
    public AjaxResult addUsersByDeptWithChildren(@PathVariable("examId") Long examId, @RequestBody Long[] deptIds) {
        int count = examExaminationService.addExamUsersByDeptWithChildren(examId, deptIds);
        return success("成功按部门（含子部门）添加 " + count + " 名考试人员");
    }

    /**
     * 审核用户报名
     */
    @PreAuthorize("@ss.hasPermi('exam:examination:approve')")
    @Log(title = "审核用户报名", businessType = BusinessType.UPDATE)
    @PutMapping("/approveUser/{examUserId}/{status}")
    public AjaxResult approveUser(@PathVariable("examUserId") Long examUserId,
            @PathVariable("status") String status) {
        return toAjax(examExaminationService.approveUserRegistration(examUserId, status));
    }

    /**
     * 批量审核用户报名
     */
    @PreAuthorize("@ss.hasPermi('exam:examination:approve')")
    @Log(title = "批量审核用户报名", businessType = BusinessType.UPDATE)
    @PutMapping("/batchApproveUsers/{status}")
    public AjaxResult batchApproveUsers(@RequestBody Long[] examUserIds,
            @PathVariable("status") String status) {
        int count = examExaminationService.batchApproveUserRegistration(examUserIds, status);
        return success("成功审核 " + count + " 名用户报名");
    }

    /**
     * 获取用户考试成绩详情（管理端查看）
     */
    @PreAuthorize("@ss.hasPermi('exam:examination:query')")
    @GetMapping("/examResult/{examId}/{userId}")
    public AjaxResult getExamResult(@PathVariable("examId") Long examId, @PathVariable("userId") Long userId) {
        // 获取考试信息
        ExamExamination examination = examExaminationService.selectExamExaminationById(examId);
        if (examination == null) {
            return error("考试不存在");
        }

        // 获取所有考试记录（支持多次考试）
        List<ExamUser> examUsers = examUserService.selectExamUserListByExamIdAndUserId(examId, userId);
        if (examUsers == null || examUsers.isEmpty()) {
            return error("未找到考试记录");
        }

        // 为每个考试记录获取详细的答题记录（包含题目详情）
        for (ExamUser examUser : examUsers) {
            List<ExamAnswerRecord> answerRecords = examAnswerRecordService
                    .selectAnswerRecordsWithQuestionsByExamUserId(examUser.getId());
            examUser.setAnswerRecords(answerRecords);
        }

        AjaxResult result = AjaxResult.success();
        result.put("examination", examination);
        result.put("examUsers", examUsers);
        return result;
    }

    /**
     * 分页查询考试人员
     */
    @PreAuthorize("@ss.hasPermi('exam:examination:query')")
    @GetMapping("/users/{examId}")
    public TableDataInfo getExamUsers(@PathVariable("examId") Long examId, ExamUser examUser) {
        startPage();
        examUser.setExamId(examId);
        List<ExamUser> list = examUserService.selectExamUserList(examUser);
        return getDataTable(list);
    }

    /**
     * 导出考试人员
     */
    @PreAuthorize("@ss.hasPermi('exam:examination:export')")
    @Log(title = "导出考试人员", businessType = BusinessType.EXPORT)
    @PostMapping("/exportUsers/{examId}")
    public void exportUsers(HttpServletResponse response, @PathVariable("examId") Long examId) {
        List<ExamUser> list = examExaminationService.exportExamUsers(examId);
        ExcelUtil<ExamUser> util = new ExcelUtil<ExamUser>(ExamUser.class);
        util.exportExcel(response, list, "考试人员数据");
    }

    /**
     * 人工阅卷
     */
    @PreAuthorize("@ss.hasPermi('exam:examination:grade')")
    @Log(title = "人工阅卷", businessType = BusinessType.UPDATE)
    @PutMapping("/manualGrade/{examId}/{userId}")
    public AjaxResult manualGrade(@PathVariable("examId") Long examId,
            @PathVariable("userId") Long userId) {
        return toAjax(examExaminationService.manualGrading(examId, userId));
    }

    /**
     * 重置单次考试记录（保留同一考生的其他历史轮次）
     */
    @PreAuthorize("@ss.hasPermi('exam:examination:reset')")
    @Log(title = "重置考试", businessType = BusinessType.DELETE)
    @DeleteMapping("/reset/{examUserId}")
    public AjaxResult resetExam(@PathVariable("examUserId") Long examUserId) {
        try {
            ExamUser examUser = examUserService.selectExamUserById(examUserId);
            if (examUser == null) {
                return error("考试记录不存在");
            }

            // 获取用户信息用于日志
            SysUser user = userService.selectUserById(examUser.getUserId());
            String userName = user != null ? user.getUserName() : "ID:" + examUser.getUserId();

            // 执行重置操作
            int result = examExaminationService.resetUserExam(examUserId);

            if (result > 0) {
                return success("成功重置考生【" + userName + "】的本次考试记录，共清理 " + result + " 条记录");
            } else {
                return error("该考生没有考试记录，无需重置");
            }
        } catch (Exception e) {
            logger.error("重置考试失败: examUserId={}, error={}", examUserId, e.getMessage());
            return error("重置考试失败：" + e.getMessage());
        }
    }

    /**
     * 导入参考人员模板下载
     */
    @PreAuthorize("@ss.hasPermi('exam:examination:addUsers')")
    @PostMapping("/importTemplate/{examId}")
    public void importTemplate(@PathVariable("examId") Long examId, HttpServletResponse response) throws IOException {
        ExcelUtil<ExamUser> util = new ExcelUtil<ExamUser>(ExamUser.class);
        util.importTemplateExcel(response, "参考人员数据");
    }

    /**
     * 批量导入参考人员
     */
    @PreAuthorize("@ss.hasPermi('exam:examination:addUsers')")
    @Log(title = "批量导入参考人员", businessType = BusinessType.IMPORT)
    @PostMapping("/importUsers/{examId}")
    public AjaxResult importUsers(@PathVariable("examId") Long examId, MultipartFile file, boolean updateSupport)
            throws Exception {
        ExcelUtil<ExamUser> util = new ExcelUtil<ExamUser>(ExamUser.class);
        List<ExamUser> examUserList = util.importExcel(file.getInputStream());
        String operName = SecurityUtils.getUsername();
        String message = examExaminationService.importExamUsers(examId, examUserList, updateSupport, operName);
        return success(message);
    }

    /**
     * 测试填空题评分修复
     * 用于验证Bug修复是否有效
     */
    @PreAuthorize("@ss.hasPermi('exam:examination:query')")
    @GetMapping("/testFillBlankGrading")
    public AjaxResult testFillBlankGrading() {
        try {
            // 调用Service层的测试方法
            if (examAnswerRecordService instanceof com.ruoyi.exam.examination.service.impl.ExamAnswerRecordServiceImpl) {
                com.ruoyi.exam.examination.service.impl.ExamAnswerRecordServiceImpl serviceImpl = (com.ruoyi.exam.examination.service.impl.ExamAnswerRecordServiceImpl) examAnswerRecordService;
                serviceImpl.testFillBlankGradingFix();
                return success("填空题评分修复验证测试已完成，请查看后端日志获取详细结果");
            } else {
                return error("无法执行测试：Service实现类型不匹配");
            }
        } catch (Exception e) {
            return error("测试执行失败: " + e.getMessage());
        }
    }

    /**
     * 重新评分填空题（修复填空题评分问题）
     * 专门针对填空题的重新评分接口，解决历史数据评分不准确的问题
     */
    @PreAuthorize("@ss.hasPermi('exam:examination:grade')")
    @Log(title = "重新评分填空题", businessType = BusinessType.UPDATE)
    @PutMapping("/regradeFillBlank/{examId}")
    public AjaxResult regradeFillBlankQuestions(@PathVariable("examId") Long examId) {
        try {
            // 获取该考试的所有已提交的考试用户
            List<ExamUser> examUsers = examUserService.selectExamUserListByExamId(examId);
            int regradeCount = 0;
            int totalFillBlankRecords = 0;

            for (ExamUser examUser : examUsers) {
                // 只对已提交或已评分的用户重新评分
                if (USER_EXAM_SUBMITTED.equals(examUser.getExamStatus())
                        || USER_EXAM_GRADED.equals(examUser.getExamStatus())) {
                    // 调用Service层的填空题重新评分方法
                    if (examAnswerRecordService instanceof com.ruoyi.exam.examination.service.impl.ExamAnswerRecordServiceImpl) {
                        com.ruoyi.exam.examination.service.impl.ExamAnswerRecordServiceImpl serviceImpl = (com.ruoyi.exam.examination.service.impl.ExamAnswerRecordServiceImpl) examAnswerRecordService;
                        int fillBlankCount = serviceImpl.regradeFillBlankQuestions(examUser.getId());

                        if (fillBlankCount > 0) {
                            // 重新计算总分
                            examAnswerRecordService.calculateAndUpdateScore(examUser.getId());
                            regradeCount++;
                            totalFillBlankRecords += fillBlankCount;
                        }
                    }
                }
            }

            return success(String.format("填空题重新评分完成，共处理 %d 个用户的 %d 道填空题",
                    regradeCount, totalFillBlankRecords));

        } catch (Exception e) {
            logger.error("填空题重新评分失败", e);
            return error("填空题重新评分失败：" + e.getMessage());
        }
    }

    /**
     * 生成H5考试二维码
     * 返回Base64格式的二维码图片
     * 二维码内容为完整的H5登录页面URL，支持微信扫码直接访问
     *
     * @param examId  考试ID
     * @param baseUrl 前端传来的同源基础URL（用于本地开发端口区分）
     */
    @PreAuthorize("@ss.hasPermi('exam:examination:query')")
    @GetMapping("/qrcode/{examId}")
    public AjaxResult generateQrCode(@PathVariable("examId") Long examId,
            @RequestParam(value = "baseUrl", required = false) String baseUrl,
            HttpServletRequest request) {
        try {
            // 检查考试是否存在
            ExamExamination examination = examExaminationService.selectExamExaminationById(examId);
            if (examination == null) {
                return error("考试不存在");
            }

            String requestBaseUrl = buildRequestBaseUrl(request);
            String configuredPublicUrl = normalizeBaseUrl(h5PublicUrl);
            String finalBaseUrl = hasText(configuredPublicUrl) ? configuredPublicUrl : requestBaseUrl;

            if (hasText(baseUrl)) {
                if (!isTrustedQrBaseUrl(baseUrl, requestBaseUrl, request)) {
                    return error("二维码域名不可信，请使用当前访问域名生成");
                }
                if (!hasText(configuredPublicUrl)) {
                    finalBaseUrl = normalizeBaseUrl(baseUrl);
                }
            }

            // 构建完整的H5登录URL（带examId参数）
            String h5LoginUrl = finalBaseUrl + "/h5/login?examId=" + examId;

            // 生成二维码Base64图片（使用完整URL）
            String qrCodeBase64 = com.ruoyi.common.utils.QrCodeUtil.generateQrCodeBase64(h5LoginUrl, 300, 300);

            AjaxResult result = AjaxResult.success();
            result.put("qrCodeBase64", qrCodeBase64);
            result.put("h5Url", h5LoginUrl);
            result.put("examName", examination.getExamName());
            return result;
        } catch (Exception e) {
            logger.error("生成二维码失败", e);
            return error("生成二维码失败：" + e.getMessage());
        }
    }

    private String buildRequestBaseUrl(HttpServletRequest request) {
        String scheme = request.getHeader("X-Forwarded-Proto");
        if (scheme == null || scheme.isEmpty()) {
            scheme = request.getScheme();
        }

        String host = request.getHeader("X-Forwarded-Host");
        if (host == null || host.isEmpty()) {
            host = request.getHeader("Host");
        }
        if (host == null || host.isEmpty()) {
            host = request.getServerName() + ":" + request.getServerPort();
        }
        return normalizeBaseUrl(scheme + "://" + host);
    }

    private String normalizeBaseUrl(String baseUrl) {
        if (baseUrl == null) {
            return "";
        }
        String normalized = baseUrl.trim();
        while (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private boolean isTrustedQrBaseUrl(String baseUrl, String requestBaseUrl, HttpServletRequest request) {
        try {
            URI candidate = new URI(normalizeBaseUrl(baseUrl));
            if (candidate.getScheme() == null || candidate.getHost() == null) {
                return false;
            }
            if (!"http".equalsIgnoreCase(candidate.getScheme()) && !"https".equalsIgnoreCase(candidate.getScheme())) {
                return false;
            }

            URI requestUri = new URI(requestBaseUrl);
            String candidateHost = candidate.getHost().toLowerCase(Locale.ROOT);
            String requestHost = requestUri.getHost() == null ? "" : requestUri.getHost().toLowerCase(Locale.ROOT);
            String serverHost = request.getServerName() == null ? "" : request.getServerName().toLowerCase(Locale.ROOT);

            if (candidateHost.equals(requestHost) || candidateHost.equals(serverHost)
                    || isConfiguredAllowedBaseUrl(candidate)) {
                return true;
            }

            return isLocalDevelopmentHost(candidateHost) && isLocalDevelopmentHost(requestHost);
        } catch (URISyntaxException e) {
            return false;
        }
    }

    private boolean isLocalDevelopmentHost(String host) {
        if (host == null) {
            return false;
        }
        return "localhost".equals(host)
                || "127.0.0.1".equals(host)
                || "0.0.0.0".equals(host)
                || "::1".equals(host)
                || host.startsWith("192.168.")
                || host.startsWith("10.")
                || host.matches("^172\\.(1[6-9]|2[0-9]|3[0-1])\\..*");
    }

    private boolean isConfiguredAllowedBaseUrl(URI candidate) {
        String candidateOrigin = toOrigin(candidate);
        if (!hasText(candidateOrigin)) {
            return false;
        }
        for (String allowedBaseUrl : getConfiguredAllowedBaseUrls()) {
            try {
                URI allowedUri = new URI(allowedBaseUrl);
                if (candidateOrigin.equalsIgnoreCase(toOrigin(allowedUri))) {
                    return true;
                }
            } catch (URISyntaxException e) {
                logger.warn("忽略无效的H5二维码白名单地址: {}", allowedBaseUrl);
            }
        }
        return false;
    }

    private Set<String> getConfiguredAllowedBaseUrls() {
        Set<String> allowedBaseUrls = new LinkedHashSet<>();
        if (hasText(h5PublicUrl)) {
            allowedBaseUrls.add(normalizeBaseUrl(h5PublicUrl));
        }
        if (hasText(h5AllowedBaseUrls)) {
            Arrays.stream(h5AllowedBaseUrls.split(","))
                    .map(this::normalizeBaseUrl)
                    .filter(this::hasText)
                    .forEach(allowedBaseUrls::add);
        }
        return allowedBaseUrls;
    }

    private String toOrigin(URI uri) {
        if (uri == null || uri.getScheme() == null || uri.getHost() == null) {
            return "";
        }
        StringBuilder origin = new StringBuilder();
        origin.append(uri.getScheme().toLowerCase(Locale.ROOT)).append("://").append(uri.getHost().toLowerCase(Locale.ROOT));
        if (uri.getPort() > -1) {
            origin.append(":").append(uri.getPort());
        }
        return origin.toString();
    }

}
