package com.ruoyi.exam.examination.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.exam.examination.domain.ExamAnswerRecord;

/**
 * 答题记录Mapper接口
 * 
 * @author ruoyi
 */
public interface ExamAnswerRecordMapper 
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
    public ExamAnswerRecord selectAnswerRecordByExamUserIdAndQuestionId(@Param("examUserId") Long examUserId, @Param("questionId") Long questionId);

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
     * 删除答题记录
     * 
     * @param id 答题记录主键
     * @return 结果
     */
    public int deleteExamAnswerRecordById(Long id);

    /**
     * 批量删除答题记录
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteExamAnswerRecordByIds(Long[] ids);

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
     * 统计考试答题记录数
     * 
     * @param examId 考试ID
     * @return 答题记录数
     */
    public int countAnswerRecordsByExamId(Long examId);

    /**
     * 统计用户答题数
     * 
     * @param examUserId 考试用户关联ID
     * @return 答题数
     */
    public int countAnswerRecordsByExamUserId(Long examUserId);

    /**
     * 根据考试ID和用户ID查询答题记录
     * 
     * @param examId 考试ID
     * @param userId 用户ID
     * @return 答题记录列表
     */
    public List<ExamAnswerRecord> selectAnswerRecordsByExamIdAndUserId(@Param("examId") Long examId, @Param("userId") Long userId);

    /**
     * 统计考试已答题人数
     * 
     * @param examId 考试ID
     * @return 已答题人数
     */
    public int countAnsweredUsersByExamId(Long examId);

    /**
     * 查询考试最高分答题记录
     * 
     * @param examId 考试ID
     * @return 答题记录
     */
    public ExamAnswerRecord selectHighestScoreRecordByExamId(Long examId);

    /**
     * 查询考试平均分
     * 
     * @param examId 考试ID
     * @return 平均分
     */
    public Double selectAverageScoreByExamId(Long examId);

    /**
     * 批量新增答题记录
     * 
     * @param answerRecords 答题记录列表
     * @return 结果
     */
    public int batchInsertExamAnswerRecord(List<ExamAnswerRecord> answerRecords);

    /**
     * 批量更新答题记录
     * 
     * @param answerRecords 答题记录列表
     * @return 结果
     */
    public int batchUpdateExamAnswerRecord(List<ExamAnswerRecord> answerRecords);
} 