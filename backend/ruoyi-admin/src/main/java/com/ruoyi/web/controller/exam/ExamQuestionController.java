package com.ruoyi.web.controller.exam;

import java.util.List;
import javax.servlet.http.HttpServletRequest;
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
import org.springframework.web.multipart.MultipartFile;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.utils.ServletUtils;
import com.ruoyi.exam.question.domain.ExamQuestion;
import com.ruoyi.exam.question.domain.ExamQuestionCategory;
import com.ruoyi.exam.question.domain.QuestionImportResult;
import com.ruoyi.exam.question.service.IExamQuestionService;
import com.ruoyi.exam.question.service.IExamQuestionCategoryService;

/**
 * 试题信息操作处理
 * 
 * @author ruoyi
 */
@RestController
@RequestMapping("/exam/question")
public class ExamQuestionController extends BaseController {
    @Autowired
    private IExamQuestionService questionService;

    @Autowired
    private IExamQuestionCategoryService categoryService;

    /**
     * 获取试题列表
     */
    @PreAuthorize("@ss.hasPermi('exam:question:list')")
    @GetMapping("/list")
    public TableDataInfo list(ExamQuestion question) {
        startPage();
        List<ExamQuestion> list = questionService.selectQuestionList(question);
        return getDataTable(list);
    }

    /**
     * 导出试题列表
     */
    @Log(title = "试题管理", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('exam:question:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, ExamQuestion question) {
        List<ExamQuestion> list = questionService.exportQuestion(question);
        ExcelUtil<ExamQuestion> util = new ExcelUtil<ExamQuestion>(ExamQuestion.class);
        util.exportExcel(response, list, "试题数据");
    }

    /**
     * 根据试题编号获取详细信息
     */
    @PreAuthorize("@ss.hasPermi('exam:question:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable Long id) {
        return success(questionService.selectQuestionById(id));
    }

    /**
     * 预览试题
     */
    @PreAuthorize("@ss.hasPermi('exam:question:query')")
    @GetMapping(value = "/preview/{id}")
    public AjaxResult preview(@PathVariable Long id) {
        ExamQuestion question = questionService.previewQuestion(id);
        return success(question);
    }

    /**
     * 新增试题
     */
    @PreAuthorize("@ss.hasPermi('exam:question:add')")
    @Log(title = "试题管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody ExamQuestion question) {
        if (!questionService.checkQuestionTitleUnique(question)) {
            return error("新增试题'" + question.getQuestionTitle() + "'失败，试题标题已存在");
        }
        question.setCreateBy(getUsername());
        return toAjax(questionService.insertQuestion(question));
    }

    /**
     * 修改试题
     */
    @PreAuthorize("@ss.hasPermi('exam:question:edit')")
    @Log(title = "试题管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody ExamQuestion question) {
        if (!questionService.checkQuestionTitleUnique(question)) {
            return error("修改试题'" + question.getQuestionTitle() + "'失败，试题标题已存在");
        }
        question.setUpdateBy(getUsername());
        return toAjax(questionService.updateQuestion(question));
    }

    /**
     * 删除试题
     */
    @PreAuthorize("@ss.hasPermi('exam:question:remove')")
    @Log(title = "试题管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(questionService.deleteQuestionByIds(ids));
    }

    /**
     * 获取试题分类下拉框数据
     */
    @PreAuthorize("@ss.hasAnyPermi('exam:question:list,exam:question:query')")
    @GetMapping("/categoryList")
    public AjaxResult categoryList() {
        List<ExamQuestionCategory> categories = categoryService.selectCategoryList(new ExamQuestionCategory());
        return success(categoryService.buildCategoryTreeSelect(categories));
    }

    /**
     * 获取试题分类树形列表
     */
    @PreAuthorize("@ss.hasAnyPermi('exam:question:list,exam:question:query')")
    @GetMapping("/categoryTree")
    public AjaxResult categoryTree() {
        List<ExamQuestionCategory> categories = categoryService.selectCategoryList(new ExamQuestionCategory());
        return success(categoryService.buildCategoryTree(categories));
    }

    /**
     * 根据分类ID获取试题列表
     */
    @PreAuthorize("@ss.hasPermi('exam:question:list')")
    @GetMapping("/category/{categoryId}")
    public AjaxResult getQuestionsByCategory(@PathVariable Long categoryId) {
        List<ExamQuestion> questions = questionService.selectQuestionsByCategoryId(categoryId);
        return success(questions);
    }

    /**
     * 根据题型获取试题列表
     */
    @PreAuthorize("@ss.hasPermi('exam:question:list')")
    @GetMapping("/type/{questionType}")
    public AjaxResult getQuestionsByType(@PathVariable String questionType) {
        List<ExamQuestion> questions = questionService.selectQuestionsByType(questionType);
        return success(questions);
    }

    /**
     * 根据难度等级获取试题列表
     */
    @PreAuthorize("@ss.hasPermi('exam:question:list')")
    @GetMapping("/difficulty/{difficultyLevel}")
    public AjaxResult getQuestionsByDifficulty(@PathVariable Integer difficultyLevel) {
        List<ExamQuestion> questions = questionService.selectQuestionsByDifficulty(difficultyLevel);
        return success(questions);
    }

    /**
     * 根据标签获取试题列表
     */
    @PreAuthorize("@ss.hasPermi('exam:question:list')")
    @GetMapping("/tags/{tags}")
    public AjaxResult getQuestionsByTags(@PathVariable String tags) {
        List<ExamQuestion> questions = questionService.selectQuestionsByTags(tags);
        return success(questions);
    }

    /**
     * 导入试题数据
     */
    @Log(title = "试题管理", businessType = BusinessType.IMPORT)
    @PreAuthorize("@ss.hasPermi('exam:question:import')")
    @PostMapping("/importData")
    public AjaxResult importData(MultipartFile file, boolean updateSupport) throws Exception {
        String operName = getUsername();
        QuestionImportResult result = questionService.importQuestionWithErrorFile(file, updateSupport, operName);

        // 构建返回结果
        AjaxResult ajaxResult = success(result.getMessage());

        // 如果有错误文件，添加到返回结果中
        if (result.isHasErrorFile()) {
            ajaxResult.put("hasErrorFile", true);
            ajaxResult.put("errorFileName", result.getErrorFileName());
            ajaxResult.put("successCount", result.getSuccessCount());
            ajaxResult.put("failureCount", result.getFailureCount());

            // 将错误文件数据临时存储在session中，供下载使用
            ServletUtils.getRequest().getSession().setAttribute("errorFileData", result.getErrorFileData());
            ServletUtils.getRequest().getSession().setAttribute("errorFileName", result.getErrorFileName());
        }

        return ajaxResult;
    }

    /**
     * 下载错误文件
     */
    @PreAuthorize("@ss.hasPermi('exam:question:import')")
    @PostMapping("/downloadErrorFile")
    public void downloadErrorFile(HttpServletResponse response) throws Exception {
        byte[] errorFileData = (byte[]) ServletUtils.getRequest().getSession().getAttribute("errorFileData");
        String errorFileName = (String) ServletUtils.getRequest().getSession().getAttribute("errorFileName");

        if (errorFileData != null && errorFileName != null) {
            response.setHeader("Content-Disposition", "attachment; filename=" +
                    java.net.URLEncoder.encode(errorFileName, "UTF-8"));
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setContentLength(errorFileData.length);

            response.getOutputStream().write(errorFileData);
            response.getOutputStream().flush();

            // 清除session中的数据
            ServletUtils.getRequest().getSession().removeAttribute("errorFileData");
            ServletUtils.getRequest().getSession().removeAttribute("errorFileName");
        }
    }

    /**
     * 下载试题导入模板（按题型分sheet）
     */
    @PreAuthorize("@ss.hasPermi('exam:question:import')")
    @PostMapping("/importTemplate")
    public void importTemplate(HttpServletResponse response) {
        questionService.downloadImportTemplate(response);
    }

    /**
     * 下载旧版导入模板（兼容性保留）
     */
    @PreAuthorize("@ss.hasPermi('exam:question:import')")
    @PostMapping("/importTemplateOld")
    public void importTemplateOld(HttpServletResponse response) {
        ExcelUtil<ExamQuestion> util = new ExcelUtil<ExamQuestion>(ExamQuestion.class);
        util.importTemplateExcel(response, "试题数据");
    }

    /**
     * 统计各题型试题数量
     */
    @PreAuthorize("@ss.hasPermi('exam:question:list')")
    @GetMapping("/statistics/type")
    public AjaxResult getQuestionTypeStatistics() {
        return success(questionService.selectQuestionTypeStatistics());
    }

    /**
     * 统计试题数量
     */
    @PreAuthorize("@ss.hasPermi('exam:question:list')")
    @GetMapping("/count")
    public AjaxResult countQuestions(ExamQuestion question) {
        int count = questionService.countQuestions(question);
        return success(count);
    }
}
