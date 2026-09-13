package com.ruoyi.exam.paper.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.exam.paper.domain.ExamPaper;
import com.ruoyi.exam.paper.domain.ExamPaperQuestion;
import com.ruoyi.exam.question.domain.ExamQuestion;

/**
 * 试卷 数据层
 * 
 * @author ruoyi
 */
public interface ExamPaperMapper
{
    /**
     * 查询试卷列表
     * 
     * @param paper 试卷信息
     * @return 试卷集合
     */
    public List<ExamPaper> selectPaperList(ExamPaper paper);

    /**
     * 根据试卷ID查询试卷信息
     * 
     * @param id 试卷ID
     * @return 试卷信息
     */
    public ExamPaper selectPaperById(Long id);

    /**
     * 根据分类ID查询试卷列表
     * 
     * @param categoryId 分类ID
     * @return 试卷列表
     */
    public List<ExamPaper> selectPapersByCategoryId(Long categoryId);

    /**
     * 根据试卷类型查询试卷列表
     * 
     * @param paperType 试卷类型
     * @return 试卷列表
     */
    public List<ExamPaper> selectPapersByType(String paperType);

    /**
     * 检查试卷名称是否唯一
     * 
     * @param paperName 试卷名称
     * @param categoryId 分类ID
     * @return 试卷信息
     */
    public ExamPaper checkPaperNameUnique(@Param("paperName") String paperName, @Param("categoryId") Long categoryId);

    /**
     * 新增试卷
     * 
     * @param paper 试卷信息
     * @return 结果
     */
    public int insertPaper(ExamPaper paper);

    /**
     * 修改试卷
     * 
     * @param paper 试卷信息
     * @return 结果
     */
    public int updatePaper(ExamPaper paper);

    /**
     * 删除试卷
     * 
     * @param id 试卷ID
     * @return 结果
     */
    public int deletePaperById(Long id);

    /**
     * 批量删除试卷
     * 
     * @param ids 需要删除的试卷ID
     * @return 结果
     */
    public int deletePaperByIds(Long[] ids);

    /**
     * 统计试卷数量
     * 
     * @param paper 查询条件
     * @return 试卷数量
     */
    public int countPapers(ExamPaper paper);

    /**
     * 查询试卷关联的试题列表
     * 
     * @param paperId 试卷ID
     * @return 试题列表
     */
    public List<ExamPaperQuestion> selectPaperQuestions(Long paperId);

    /**
     * 添加试卷试题关联
     * 
     * @param paperQuestion 试卷试题关联
     * @return 结果
     */
    public int insertPaperQuestion(ExamPaperQuestion paperQuestion);

    /**
     * 批量添加试卷试题关联
     * 
     * @param paperQuestions 试卷试题关联列表
     * @return 结果
     */
    public int batchInsertPaperQuestion(List<ExamPaperQuestion> paperQuestions);

    /**
     * 删除试卷试题关联
     * 
     * @param paperId 试卷ID
     * @return 结果
     */
    public int deletePaperQuestionByPaperId(Long paperId);

    /**
     * 根据条件随机获取试题
     * 
     * @param categoryId 分类ID
     * @param questionType 题型
     * @param difficultyLevel 难度
     * @param count 数量
     * @return 试题列表
     */
    public List<ExamQuestion> selectRandomQuestions(@Param("categoryId") Long categoryId, 
                                                   @Param("questionType") String questionType, 
                                                   @Param("difficultyLevel") Integer difficultyLevel,
                                                   @Param("count") Integer count);

    /**
     * 统计引用该试卷的考试数量
     * 
     * @param paperId 试卷ID
     * @return 考试数量
     */
    public int countExaminationsByPaperId(Long paperId);
} 