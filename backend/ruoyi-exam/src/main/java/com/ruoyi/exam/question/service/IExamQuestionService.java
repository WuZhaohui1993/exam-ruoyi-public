package com.ruoyi.exam.question.service;

import java.util.List;
import javax.servlet.http.HttpServletResponse;
import com.ruoyi.exam.question.domain.ExamQuestion;
import com.ruoyi.exam.question.domain.QuestionImportResult;
import org.springframework.web.multipart.MultipartFile;

/**
 * 试题 服务层
 * 
 * @author ruoyi
 */
public interface IExamQuestionService {
    /**
     * 查询试题列表
     * 
     * @param question 试题信息
     * @return 试题集合
     */
    public List<ExamQuestion> selectQuestionList(ExamQuestion question);

    /**
     * 根据试题ID查询试题信息
     * 
     * @param id 试题ID
     * @return 试题信息
     */
    public ExamQuestion selectQuestionById(Long id);

    /**
     * 根据分类ID查询试题列表
     * 
     * @param categoryId 分类ID
     * @return 试题列表
     */
    public List<ExamQuestion> selectQuestionsByCategoryId(Long categoryId);

    /**
     * 根据题型查询试题列表
     * 
     * @param questionType 题型
     * @return 试题列表
     */
    public List<ExamQuestion> selectQuestionsByType(String questionType);

    /**
     * 根据难度等级查询试题列表
     * 
     * @param difficultyLevel 难度等级
     * @return 试题列表
     */
    public List<ExamQuestion> selectQuestionsByDifficulty(Integer difficultyLevel);

    /**
     * 校验试题标题是否唯一
     * 
     * @param question 试题信息
     * @return 结果
     */
    public boolean checkQuestionTitleUnique(ExamQuestion question);

    /**
     * 新增试题
     * 
     * @param question 试题信息
     * @return 结果
     */
    public int insertQuestion(ExamQuestion question);

    /**
     * 修改试题
     * 
     * @param question 试题信息
     * @return 结果
     */
    public int updateQuestion(ExamQuestion question);

    /**
     * 批量删除试题
     * 
     * @param ids 需要删除的试题ID
     * @return 结果
     */
    public int deleteQuestionByIds(Long[] ids);

    /**
     * 删除试题信息
     * 
     * @param id 试题ID
     * @return 结果
     */
    public int deleteQuestionById(Long id);

    /**
     * 批量导入试题
     * 
     * @param file          Excel文件
     * @param updateSupport 是否更新支持，如果已存在，则进行更新数据
     * @param operName      操作用户
     * @return 结果
     */
    public QuestionImportResult importQuestionWithErrorFile(MultipartFile file, Boolean updateSupport, String operName)
            throws Exception;

    /**
     * 批量导入试题（兼容旧版本）
     * 
     * @param file          Excel文件
     * @param updateSupport 是否更新支持，如果已存在，则进行更新数据
     * @param operName      操作用户
     * @return 结果
     */
    public String importQuestion(MultipartFile file, Boolean updateSupport, String operName) throws Exception;

    /**
     * 导出试题数据
     * 
     * @param question 试题信息
     * @return 试题列表
     */
    public List<ExamQuestion> exportQuestion(ExamQuestion question);

    /**
     * 根据标签查询试题
     * 
     * @param tags 标签
     * @return 试题列表
     */
    public List<ExamQuestion> selectQuestionsByTags(String tags);

    /**
     * 统计试题数量
     * 
     * @param question 查询条件
     * @return 试题数量
     */
    public int countQuestions(ExamQuestion question);

    /**
     * 预览试题
     * 
     * @param id 试题ID
     * @return 试题详细信息
     */
    public ExamQuestion previewQuestion(Long id);

    /**
     * 统计试题总数
     * 
     * @return 试题总数
     */
    public int countTotalQuestions();

    /**
     * 统计各题型试题数量
     * 
     * @return 统计结果
     */
    public java.util.Map<String, Integer> selectQuestionTypeStatistics();

    /**
     * 下载按题型分sheet的导入模板
     * 
     * @param response HTTP响应
     */
    public void downloadImportTemplate(HttpServletResponse response);
}