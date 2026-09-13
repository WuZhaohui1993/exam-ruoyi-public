package com.ruoyi.web.controller.exam;

import java.util.List;
import java.util.Map;
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
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.exam.paper.domain.ExamPaper;
import com.ruoyi.exam.paper.domain.ExamPaperQuestion;
import com.ruoyi.exam.paper.service.IExamPaperService;
import com.ruoyi.exam.question.domain.ExamQuestion;
import java.util.ArrayList;
import org.springframework.util.StringUtils;
import java.util.HashMap;

/**
 * 试卷信息操作处理
 * 
 * @author ruoyi
 */
@RestController
@RequestMapping("/exam/paper")
public class ExamPaperController extends BaseController
{
    @Autowired
    private IExamPaperService paperService;

    /**
     * 获取试卷列表
     */
    @PreAuthorize("@ss.hasPermi('exam:paper:list')")
    @GetMapping("/list")
    public TableDataInfo list(ExamPaper paper)
    {
        startPage();
        List<ExamPaper> list = paperService.selectPaperList(paper);
        return getDataTable(list);
    }

    /**
     * 导出试卷列表
     */
    @Log(title = "试卷管理", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('exam:paper:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, ExamPaper paper)
    {
        List<ExamPaper> list = paperService.selectPaperList(paper);
        ExcelUtil<ExamPaper> util = new ExcelUtil<ExamPaper>(ExamPaper.class);
        util.exportExcel(response, list, "试卷数据");
    }

    /**
     * 根据试卷编号获取详细信息
     */
    @PreAuthorize("@ss.hasPermi('exam:paper:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable Long id)
    {
        return success(paperService.selectPaperById(id));
    }

    /**
     * 预览试卷
     */
    @PreAuthorize("@ss.hasPermi('exam:paper:query')")
    @GetMapping(value = "/preview/{id}")
    public AjaxResult preview(@PathVariable Long id)
    {
        ExamPaper paper = paperService.previewPaper(id);
        return success(paper);
    }

    /**
     * 新增试卷
     */
    @PreAuthorize("@ss.hasPermi('exam:paper:add')")
    @Log(title = "试卷管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody ExamPaper paper)
    {
        if (!paperService.checkPaperNameUnique(paper))
        {
            return error("新增试卷'" + paper.getPaperName() + "'失败，试卷名称已存在");
        }
        paper.setCreateBy(getUsername());
        return toAjax(paperService.insertPaper(paper));
    }

    /**
     * 创建固定试卷
     */
    @PreAuthorize("@ss.hasPermi('exam:paper:add')")
    @Log(title = "试卷管理", businessType = BusinessType.INSERT)
    @PostMapping("/fixed")
    public AjaxResult createFixedPaper(@RequestBody Map<String, Object> params)
    {
        try
        {
            ExamPaper paper = parseExamPaper(params);
            
            // 正确处理试题列表的转换
            List<ExamQuestion> questions = new ArrayList<>();
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> questionMaps = (List<Map<String, Object>>) params.get("questions");
            
            if (questionMaps != null) {
                for (Map<String, Object> questionMap : questionMaps) {
                    ExamQuestion question = new ExamQuestion();
                    if (questionMap.get("questionId") != null) {
                        question.setId(Long.valueOf(questionMap.get("questionId").toString()));
                    }
                    if (questionMap.get("questionScore") != null) {
                        question.setScore(new java.math.BigDecimal(questionMap.get("questionScore").toString()));
                    }
                    questions.add(question);
                }
            }
            
            if (!paperService.checkPaperNameUnique(paper))
            {
                return error("创建试卷'" + paper.getPaperName() + "'失败，试卷名称已存在");
            }
            
            paper.setCreateBy(getUsername());
            int result = paperService.createFixedPaper(paper, questions);
            
            if (result > 0) {
                // 返回成功信息，并包含试卷ID
                return success("创建成功").put("data", paper.getId());
            } else {
                return error("创建失败");
            }
        }
        catch (Exception e)
        {
            return error("创建固定试卷失败：" + e.getMessage());
        }
    }

    /**
     * 创建随机试卷
     */
    @PreAuthorize("@ss.hasPermi('exam:paper:add')")
    @Log(title = "试卷管理", businessType = BusinessType.INSERT)
    @PostMapping("/random")
    public AjaxResult createRandomPaper(@RequestBody Map<String, Object> params)
    {
        try
        {
            ExamPaper paper = parseExamPaper(params);
            // 修复参数名称：前端发送的是randomConfig，不是questionConfig
            String questionConfig = (String) params.get("randomConfig");
            
            if (!paperService.checkPaperNameUnique(paper))
            {
                return error("创建试卷'" + paper.getPaperName() + "'失败，试卷名称已存在");
            }
            
            paper.setCreateBy(getUsername());
            int result = paperService.createRandomPaper(paper, questionConfig);
            
            if (result > 0) {
                // 返回成功信息，并包含试卷ID
                return success("创建成功").put("data", paper.getId());
            } else {
                return error("创建失败");
            }
        }
        catch (Exception e)
        {
            return error("创建随机试卷失败：" + e.getMessage());
        }
    }

    /**
     * 创建混合试卷
     */
    @PreAuthorize("@ss.hasPermi('exam:paper:add')")
    @Log(title = "试卷管理", businessType = BusinessType.INSERT)
    @PostMapping("/mixed")
    public AjaxResult createMixedPaper(@RequestBody Map<String, Object> params)
    {
        try
        {
            ExamPaper paper = parseExamPaper(params);
            
            // 正确处理固定试题列表的转换
            List<ExamQuestion> fixedQuestions = new ArrayList<>();
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> questionMaps = (List<Map<String, Object>>) params.get("questions");
            
            if (questionMaps != null) {
                for (Map<String, Object> questionMap : questionMaps) {
                    ExamQuestion question = new ExamQuestion();
                    if (questionMap.get("questionId") != null) {
                        question.setId(Long.valueOf(questionMap.get("questionId").toString()));
                    }
                    if (questionMap.get("questionScore") != null) {
                        question.setScore(new java.math.BigDecimal(questionMap.get("questionScore").toString()));
                    }
                    fixedQuestions.add(question);
                }
            }
            
            String randomConfig = (String) params.get("randomConfig");
            
            if (!paperService.checkPaperNameUnique(paper))
            {
                return error("创建试卷'" + paper.getPaperName() + "'失败，试卷名称已存在");
            }
            
            paper.setCreateBy(getUsername());
            int result = paperService.createMixedPaper(paper, fixedQuestions, randomConfig);
            
            if (result > 0) {
                // 返回成功信息，并包含试卷ID
                return success("创建成功").put("data", paper.getId());
            } else {
                return error("创建失败");
            }
        }
        catch (Exception e)
        {
            return error("创建混合试卷失败：" + e.getMessage());
        }
    }

    /**
     * 修改试卷
     */
    @PreAuthorize("@ss.hasPermi('exam:paper:edit')")
    @Log(title = "试卷管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody ExamPaper paper)
    {
        if (!paperService.checkPaperNameUnique(paper))
        {
            return error("修改试卷'" + paper.getPaperName() + "'失败，试卷名称已存在");
        }
        paper.setUpdateBy(getUsername());
        return toAjax(paperService.updatePaper(paper));
    }

    /**
     * 修改试卷（包含题目）
     */
    @PreAuthorize("@ss.hasPermi('exam:paper:edit')")
    @Log(title = "试卷管理", businessType = BusinessType.UPDATE)
    @PutMapping("/full")
    public AjaxResult editFull(@RequestBody Map<String, Object> params)
    {
        try
        {
            ExamPaper paper = parseExamPaper(params);
            Long paperId = Long.valueOf(params.get("id").toString());
            paper.setId(paperId);
            
            if (!paperService.checkPaperNameUnique(paper))
            {
                return error("修改试卷'" + paper.getPaperName() + "'失败，试卷名称已存在");
            }
            
            paper.setUpdateBy(getUsername());
            
            String paperType = paper.getPaperType();
            
            if ("fixed".equals(paperType)) {
                // 处理固定试卷题目
                List<ExamQuestion> questions = new ArrayList<>();
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> questionMaps = (List<Map<String, Object>>) params.get("questions");
                
                if (questionMaps != null) {
                    for (Map<String, Object> questionMap : questionMaps) {
                        ExamQuestion question = new ExamQuestion();
                        if (questionMap.get("questionId") != null) {
                            question.setId(Long.valueOf(questionMap.get("questionId").toString()));
                        }
                        if (questionMap.get("questionScore") != null) {
                            question.setScore(new java.math.BigDecimal(questionMap.get("questionScore").toString()));
                        }
                        questions.add(question);
                    }
                }
                
                return toAjax(paperService.updateFixedPaper(paper, questions));
            } else if ("random".equals(paperType)) {
                // 处理随机试卷
                String randomConfig = (String) params.get("randomConfig");
                paper.setQuestionConfig(randomConfig);
                return toAjax(paperService.updatePaper(paper));
            } else if ("mixed".equals(paperType)) {
                // 处理混合试卷
                List<ExamQuestion> fixedQuestions = new ArrayList<>();
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> questionMaps = (List<Map<String, Object>>) params.get("questions");
                
                if (questionMaps != null) {
                    for (Map<String, Object> questionMap : questionMaps) {
                        ExamQuestion question = new ExamQuestion();
                        if (questionMap.get("questionId") != null) {
                            question.setId(Long.valueOf(questionMap.get("questionId").toString()));
                        }
                        if (questionMap.get("questionScore") != null) {
                            question.setScore(new java.math.BigDecimal(questionMap.get("questionScore").toString()));
                        }
                        fixedQuestions.add(question);
                    }
                }
                
                String randomConfig = (String) params.get("randomConfig");
                return toAjax(paperService.updateMixedPaper(paper, fixedQuestions, randomConfig));
            } else {
                return toAjax(paperService.updatePaper(paper));
            }
        }
        catch (Exception e)
        {
            return error("修改试卷失败：" + e.getMessage());
        }
    }

    /**
     * 删除试卷
     */
    @PreAuthorize("@ss.hasPermi('exam:paper:remove')")
    @Log(title = "试卷管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(paperService.deletePaperByIds(ids));
    }

    /**
     * 复制试卷
     */
    @PreAuthorize("@ss.hasPermi('exam:paper:add')")
    @Log(title = "试卷管理", businessType = BusinessType.INSERT)
    @PostMapping("/copy/{id}")
    public AjaxResult copy(@PathVariable Long id)
    {
        return toAjax(paperService.copyPaper(id));
    }



    /**
     * 根据分类ID获取试卷列表
     */
    @PreAuthorize("@ss.hasAnyPermi('exam:paper:list,exam:paper:query')")
    @GetMapping("/list/category/{categoryId}")
    public AjaxResult getPapersByCategory(@PathVariable Long categoryId)
    {
        List<ExamPaper> papers = paperService.selectPapersByCategoryId(categoryId);
        return success(papers);
    }

    /**
     * 根据试卷类型获取试卷列表
     */
    @PreAuthorize("@ss.hasAnyPermi('exam:paper:list,exam:paper:query')")
    @GetMapping("/type/{paperType}")
    public AjaxResult getPapersByType(@PathVariable String paperType)
    {
        List<ExamPaper> papers = paperService.selectPapersByType(paperType);
        return success(papers);
    }

    /**
     * 获取试卷关联的试题列表
     */
    @PreAuthorize("@ss.hasAnyPermi('exam:paper:query,exam:paper:preview')")
    @GetMapping("/{paperId}/questions")
    public AjaxResult getPaperQuestions(@PathVariable Long paperId)
    {
        try {
            // 获取试卷信息
            ExamPaper paper = paperService.selectPaperById(paperId);
            if (paper == null) {
                return error("试卷不存在");
            }
            
            List<ExamPaperQuestion> paperQuestions = paperService.selectPaperQuestions(paperId);
            
            // 如果设置了选项乱序，需要处理每个题目的选项
            if ("1".equals(paper.getShuffleOptions())) {
                for (ExamPaperQuestion pq : paperQuestions) {
                    if (pq.getQuestion() != null) {
                        com.ruoyi.exam.question.utils.QuestionTypeUtils.shuffleQuestionOptions(pq.getQuestion());
                    }
                }
            }
            
            return success(paperQuestions);
        } catch (Exception e) {
            return error("获取试卷题目失败：" + e.getMessage());
        }
    }

    /**
     * 生成试卷试题（用于随机试卷预览）
     */
    @PreAuthorize("@ss.hasAnyPermi('exam:paper:query,exam:paper:preview')")
    @GetMapping("/{paperId}/generate")
    public AjaxResult generatePaperQuestions(@PathVariable Long paperId)
    {
        try {
            // 添加调试信息
            ExamPaper paper = paperService.selectPaperById(paperId);
            if (paper == null) {
                return error("试卷不存在，ID: " + paperId);
            }
            
            if (StringUtils.isEmpty(paper.getQuestionConfig())) {
                return error("试卷配置为空，paperType: " + paper.getPaperType() + ", questionConfig: " + paper.getQuestionConfig());
            }
            
            List<ExamQuestion> questions = paperService.generatePaperQuestions(paperId);
            return success(questions);
        } catch (Exception e) {
            return error("生成试卷题目失败：" + e.getMessage());
        }
    }

    /**
     * 调试接口：检查试卷配置和题目数据
     */
    @PreAuthorize("@ss.hasPermi('exam:paper:query')")
    @GetMapping("/{paperId}/debug")
    public AjaxResult debugPaper(@PathVariable Long paperId)
    {
        try {
            ExamPaper paper = paperService.selectPaperById(paperId);
            if (paper == null) {
                return error("试卷不存在，ID: " + paperId);
            }
            
            Map<String, Object> debugInfo = new HashMap<>();
            debugInfo.put("paper", paper);
            debugInfo.put("paperType", paper.getPaperType());
            debugInfo.put("questionConfig", paper.getQuestionConfig());
            debugInfo.put("questionConfigLength", paper.getQuestionConfig() != null ? paper.getQuestionConfig().length() : 0);
            
            return success(debugInfo);
        } catch (Exception e) {
            return error("调试失败：" + e.getMessage());
        }
    }

    /**
     * 重新生成随机试卷题目
     */
    @PreAuthorize("@ss.hasPermi('exam:paper:edit')")
    @PostMapping("/{paperId}/regenerate")
    public AjaxResult regenerateRandomPaper(@PathVariable Long paperId)
    {
        try {
            int result = paperService.regenerateRandomPaperQuestions(paperId);
            if (result > 0) {
                return success("重新生成成功");
            } else {
                return error("重新生成失败");
            }
        } catch (Exception e) {
            return error("重新生成失败：" + e.getMessage());
        }
    }

    /**
     * 解析请求参数为试卷对象
     */
    private ExamPaper parseExamPaper(Map<String, Object> params)
    {
        ExamPaper paper = new ExamPaper();
        
        if (params.get("paperName") != null)
        {
            paper.setPaperName((String) params.get("paperName"));
        }
        if (params.get("categoryId") != null)
        {
            paper.setCategoryId(Long.valueOf(params.get("categoryId").toString()));
        }
        if (params.get("paperDescription") != null)
        {
            paper.setPaperDescription((String) params.get("paperDescription"));
        }
        if (params.get("passScore") != null)
        {
            paper.setPassScore(new java.math.BigDecimal(params.get("passScore").toString()));
        }
        if (params.get("duration") != null)
        {
            paper.setDuration(Integer.valueOf(params.get("duration").toString()));
        }
        if (params.get("difficultyLevel") != null)
        {
            paper.setDifficultyLevel(Integer.valueOf(params.get("difficultyLevel").toString()));
        }
        if (params.get("shuffleQuestions") != null)
        {
            paper.setShuffleQuestions((String) params.get("shuffleQuestions"));
        }
        if (params.get("shuffleOptions") != null)
        {
            paper.setShuffleOptions((String) params.get("shuffleOptions"));
        }
        if (params.get("remark") != null)
        {
            paper.setRemark((String) params.get("remark"));
        }
        
        return paper;
    }
}
