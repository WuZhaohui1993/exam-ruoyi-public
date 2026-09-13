package com.ruoyi.exam.question.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.exam.question.domain.ExamQuestion;

/**
 * 试题 数据层
 * 
 * @author ruoyi
 */
public interface ExamQuestionMapper {
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
     * 检查试题标题是否唯一
     * 
     * @param questionTitle 试题标题
     * @param categoryId    分类ID
     * @return 试题信息
     */
    public ExamQuestion checkQuestionTitleUnique(@Param("questionTitle") String questionTitle,
            @Param("categoryId") Long categoryId);

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
     * 删除试题
     * 
     * @param id 试题ID
     * @return 结果
     */
    public int deleteQuestionById(Long id);

    /**
     * 批量删除试题
     * 
     * @param ids 需要删除的试题ID
     * @return 结果
     */
    public int deleteQuestionByIds(Long[] ids);

    /**
     * 批量导入试题
     * 
     * @param questions 试题列表
     * @return 结果
     */
    public int batchInsertQuestions(List<ExamQuestion> questions);

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
     * 统计引用该试题的试卷数量
     * 
     * @param questionId 试题ID
     * @return 试卷数量
     */
    public int countPapersByQuestionId(Long questionId);

    /**
     * 统计各题型试题数量
     * 
     * @return 统计结果
     */
    public List<java.util.Map<String, Object>> selectQuestionTypeStatistics();

    /**
     * 统计试题总数
     * 
     * @return 试题总数
     */
    public int countTotalQuestions();
}