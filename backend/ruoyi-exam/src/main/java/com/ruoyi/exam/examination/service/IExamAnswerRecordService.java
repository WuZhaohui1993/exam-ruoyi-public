package com.ruoyi.exam.examination.service;

import java.math.BigDecimal;
import java.util.List;
import com.ruoyi.exam.examination.domain.ExamAnswerRecord;

/**
 * 答题记录Service接口
 * 
 * @author ruoyi
 */
public interface IExamAnswerRecordService 
{
    /**
     * 查询答题记录
     * 
     * @param id 答题记录主键
     * @return 答题记录
     */
    public ExamAnswerRecord selectExamAnswerRecordById(Long id);

    /**
     * 查询答题记录列表
     * 
     * @param examAnswerRecord 答题记录
     * @return 答题记录集合
     */
    public List<ExamAnswerRecord> selectExamAnswerRecordList(ExamAnswerRecord examAnswerRecord);

    /**
     * 根据考试用户ID查询答题记录
     * 
     * @param examUserId 考试用户关联ID
     * @return 答题记录列表
     */
    public List<ExamAnswerRecord> selectAnswerRecordsByExamUserId(Long examUserId);

    /**
     * 根据考试用户ID查询答题记录（包含题目详情）
     * 
     * @param examUserId 考试用户关联ID
     * @return 答题记录列表
     */
    public List<ExamAnswerRecord> selectAnswerRecordsWithQuestionsByExamUserId(Long examUserId);

    /**
     * 根据考试用户ID和试题ID查询答题记录
     * 
     * @param examUserId 考试用户关联ID
     * @param questionId 试题ID
     * @return 答题记录
     */
    public ExamAnswerRecord selectAnswerRecordByExamUserIdAndQuestionId(Long examUserId, Long questionId);

    /**
     * 根据考试ID查询答题记录
     * 
     * @param examId 考试ID
     * @return 答题记录列表
     */
    public List<ExamAnswerRecord> selectAnswerRecordsByExamId(Long examId);

    /**
     * 根据用户ID查询答题记录
     * 
     * @param userId 用户ID
     * @return 答题记录列表
     */
    public List<ExamAnswerRecord> selectAnswerRecordsByUserId(Long userId);

    /**
     * 新增答题记录
     * 
     * @param examAnswerRecord 答题记录
     * @return 结果
     */
    public int insertExamAnswerRecord(ExamAnswerRecord examAnswerRecord);

    /**
     * 并发安全的插入或更新答题记录
     * 
     * @param examAnswerRecord 答题记录
     * @return 结果
     */
    public int insertOrUpdateExamAnswerRecord(ExamAnswerRecord examAnswerRecord);

    /**
     * 修改答题记录
     * 
     * @param examAnswerRecord 答题记录
     * @return 结果
     */
    public int updateExamAnswerRecord(ExamAnswerRecord examAnswerRecord);

    /**
     * 批量删除答题记录
     * 
     * @param ids 需要删除的答题记录主键集合
     * @return 结果
     */
    public int deleteExamAnswerRecordByIds(Long[] ids);

    /**
     * 删除答题记录信息
     * 
     * @param id 答题记录主键
     * @return 结果
     */
    public int deleteExamAnswerRecordById(Long id);

    /**
     * 根据考试ID删除答题记录
     * 
     * @param examId 考试ID
     * @return 结果
     */
    public int deleteAnswerRecordsByExamId(Long examId);

    /**
     * 根据考试用户ID删除答题记录
     * 
     * @param examUserId 考试用户关联ID
     * @return 结果
     */
    public int deleteAnswerRecordsByExamUserId(Long examUserId);

    /**
     * 自动评分
     * 
     * @param examUserId 考试用户关联ID
     * @return 结果
     */
    public int autoGrade(Long examUserId);

    /**
     * 人工评分
     * 
     * @param examAnswerRecord 答题记录
     * @return 结果
     */
    public int manualGrade(ExamAnswerRecord examAnswerRecord);

    /**
     * 重新计算并更新考试用户总分
     * 
     * @param examUserId 考试用户关联ID
     * @return 结果
     */
    public int calculateAndUpdateScore(Long examUserId);

    /**
     * 填空题自动评分
     *
     * @param userAnswer 用户答案
     * @param correctAnswer 正确答案
     * @param totalScore 题目总分
     * @return 用户得分
     */
    public BigDecimal gradeFillInTheBlank(String userAnswer, String correctAnswer, BigDecimal totalScore);
    
    /**
     * 重新评分填空题
     * 专门针对填空题的重新评分，解决历史数据评分不准确的问题
     * 
     * @param examUserId 考试用户关联ID
     * @return 重新评分的填空题数量
     */
    public int regradeFillBlankQuestions(Long examUserId);
}
