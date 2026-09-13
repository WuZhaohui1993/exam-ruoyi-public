package com.ruoyi.exam.paper.service;

import java.util.List;
import com.ruoyi.exam.paper.domain.ExamPaperQuestion;

/**
 * 试卷试题关联Service接口
 * 
 * @author ruoyi
 */
public interface IExamPaperQuestionService 
{
    /**
     * 查询试卷试题关联
     * 
     * @param id 试卷试题关联主键
     * @return 试卷试题关联
     */
    public ExamPaperQuestion selectExamPaperQuestionById(Long id);

    /**
     * 查询试卷试题关联列表
     * 
     * @param examPaperQuestion 试卷试题关联
     * @return 试卷试题关联集合
     */
    public List<ExamPaperQuestion> selectExamPaperQuestionList(ExamPaperQuestion examPaperQuestion);

    /**
     * 根据试卷ID查询试题列表
     * 
     * @param paperId 试卷ID
     * @return 试题列表
     */
    public List<ExamPaperQuestion> selectQuestionsByPaperId(Long paperId);

    /**
     * 根据试卷ID查询试题列表（带试题详细信息）
     * 
     * @param paperId 试卷ID
     * @return 试题列表
     */
    public List<ExamPaperQuestion> selectQuestionsWithDetailsByPaperId(Long paperId);

    /**
     * 根据试卷ID统计试题数量
     * 
     * @param paperId 试卷ID
     * @return 试题数量
     */
    public int countQuestionsByPaperId(Long paperId);

    /**
     * 新增试卷试题关联
     * 
     * @param examPaperQuestion 试卷试题关联
     * @return 结果
     */
    public int insertExamPaperQuestion(ExamPaperQuestion examPaperQuestion);

    /**
     * 修改试卷试题关联
     * 
     * @param examPaperQuestion 试卷试题关联
     * @return 结果
     */
    public int updateExamPaperQuestion(ExamPaperQuestion examPaperQuestion);

    /**
     * 批量删除试卷试题关联
     * 
     * @param ids 需要删除的试卷试题关联主键集合
     * @return 结果
     */
    public int deleteExamPaperQuestionByIds(Long[] ids);

    /**
     * 删除试卷试题关联信息
     * 
     * @param id 试卷试题关联主键
     * @return 结果
     */
    public int deleteExamPaperQuestionById(Long id);

    /**
     * 根据试卷ID删除试卷试题关联
     * 
     * @param paperId 试卷ID
     * @return 结果
     */
    public int deleteExamPaperQuestionByPaperId(Long paperId);

    /**
     * 批量添加试题到试卷
     * 
     * @param paperId 试卷ID
     * @param questionIds 试题ID数组
     * @return 结果
     */
    public int batchInsertPaperQuestions(Long paperId, Long[] questionIds);

    /**
     * 更新试题顺序
     * 
     * @param paperQuestions 试卷试题列表
     * @return 结果
     */
    public int updateQuestionOrder(List<ExamPaperQuestion> paperQuestions);

    /**
     * 根据试卷ID删除关联的所有试题
     * 
     * @param paperId 试卷ID
     * @return 结果
     */
    public int deleteByPaperId(Long paperId);

    /**
     * 批量新增试卷试题关联
     * 
     * @param examPaperQuestions 试卷试题关联列表
     * @return 结果
     */
    public int batchInsert(List<ExamPaperQuestion> examPaperQuestions);
} 