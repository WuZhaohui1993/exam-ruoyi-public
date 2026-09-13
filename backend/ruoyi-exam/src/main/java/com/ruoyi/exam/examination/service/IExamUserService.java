package com.ruoyi.exam.examination.service;

import java.util.List;
import com.ruoyi.exam.examination.domain.ExamUser;

/**
 * 考试用户关联Service接口
 * 
 * @author ruoyi
 */
public interface IExamUserService 
{
    /**
     * 查询考试用户关联
     * 
     * @param id 考试用户关联主键
     * @return 考试用户关联
     */
    public ExamUser selectExamUserById(Long id);

    /**
     * 查询考试用户关联列表
     * 
     * @param examUser 考试用户关联
     * @return 考试用户关联集合
     */
    public List<ExamUser> selectExamUserList(ExamUser examUser);

    /**
     * 根据用户ID查询考试记录
     * 
     * @param userId 用户ID
     * @return 考试记录列表
     */
    public List<ExamUser> selectExamUserListByUserId(Long userId);

    /**
     * 根据考试ID和用户ID查询考试用户关联
     * 
     * @param examId 考试ID
     * @param userId 用户ID
     * @return 考试用户关联
     */
    public ExamUser selectExamUserByExamIdAndUserId(Long examId, Long userId);

    /**
     * 根据考试ID和用户ID查询所有考试记录（支持多次考试）
     * 
     * @param examId 考试ID
     * @param userId 用户ID
     * @return 考试用户关联列表
     */
    public List<ExamUser> selectExamUserListByExamIdAndUserId(Long examId, Long userId);

    /**
     * 根据考试ID查询考试用户列表
     * 
     * @param examId 考试ID
     * @return 考试用户列表
     */
    public List<ExamUser> selectExamUserListByExamId(Long examId);

    /**
     * 新增考试用户关联
     * 
     * @param examUser 考试用户关联
     * @return 结果
     */
    public int insertExamUser(ExamUser examUser);

    /**
     * 修改考试用户关联
     * 
     * @param examUser 考试用户关联
     * @return 结果
     */
    public int updateExamUser(ExamUser examUser);

    /**
     * 批量删除考试用户关联
     * 
     * @param ids 需要删除的考试用户关联主键集合
     * @return 结果
     */
    public int deleteExamUserByIds(Long[] ids);

    /**
     * 删除考试用户关联信息
     * 
     * @param id 考试用户关联主键
     * @return 结果
     */
    public int deleteExamUserById(Long id);

    /**
     * 根据考试ID删除考试用户关联
     * 
     * @param examId 考试ID
     * @return 结果
     */
    public int deleteExamUserByExamId(Long examId);

    /**
     * 审核用户报名
     * 
     * @param examUserId 考试用户关联ID
     * @param status 审核状态
     * @return 结果
     */
    public int approveExamUser(Long examUserId, String status);

    /**
     * 批量审核用户报名
     * 
     * @param examUserIds 考试用户关联ID数组
     * @param status 审核状态
     * @return 结果
     */
    public int batchApproveExamUsers(Long[] examUserIds, String status);

    /**
     * 统计用户考试次数
     * 
     * @param examId 考试ID
     * @param userId 用户ID
     * @return 考试次数
     */
    public int countUserAttempts(Long examId, Long userId);

    /**
     * 学员开始考试或复用当前未完成考试记录。
     *
     * @param examId 考试ID
     * @param userId 学员ID
     * @param deptId 学员部门ID
     * @return 本次考试记录
     */
    public ExamUser startStudentExam(Long examId, Long userId, Long deptId);

    /**
     * 统计用户总数
     * 
     * @return 用户总数
     */
    public int countTotalUsers();
}
