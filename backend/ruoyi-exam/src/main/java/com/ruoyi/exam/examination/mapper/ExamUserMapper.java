package com.ruoyi.exam.examination.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.exam.examination.domain.ExamUser;

/**
 * 考试用户关联Mapper接口
 * 
 * @author ruoyi
 */
public interface ExamUserMapper 
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
     * 根据考试ID查询考试用户列表
     * 
     * @param examId 考试ID
     * @return 考试用户关联集合
     */
    public List<ExamUser> selectExamUserByExamId(Long examId);

    /**
     * 根据考试ID查询用户ID列表
     * 
     * @param examId 考试ID
     * @return 用户ID列表
     */
    public List<Long> selectUserIdsByExamId(Long examId);

    /**
     * 根据用户ID查询考试用户列表
     * 
     * @param userId 用户ID
     * @return 考试用户关联集合
     */
    public List<ExamUser> selectExamUserByUserId(Long userId);

    /**
     * 根据用户ID查询考试记录列表
     * 
     * @param userId 用户ID
     * @return 考试用户关联集合
     */
    public List<ExamUser> selectExamUserListByUserId(Long userId);

    /**
     * 根据考试ID和用户ID查询考试用户关联
     * 
     * @param examId 考试ID
     * @param userId 用户ID
     * @return 考试用户关联
     */
    public ExamUser selectExamUserByExamIdAndUserId(@Param("examId") Long examId, @Param("userId") Long userId);

    /**
     * 查询用户当前考试次数
     * 
     * @param examId 考试ID
     * @param userId 用户ID
     * @return 当前考试记录
     */
    public ExamUser selectCurrentExamUser(@Param("examId") Long examId, @Param("userId") Long userId);

    /**
     * 统计考试报名人数
     * 
     * @param examId 考试ID
     * @return 报名人数
     */
    public int countRegisteredUsers(Long examId);

    /**
     * 统计考试已提交人数
     * 
     * @param examId 考试ID
     * @return 已提交人数
     */
    public int countSubmittedUsers(Long examId);

    /**
     * 统计考试通过人数
     * 
     * @param examId 考试ID
     * @return 通过人数
     */
    public int countPassedUsers(Long examId);

    /**
     * 统计用户考试次数
     * 
     * @param examId 考试ID
     * @param userId 用户ID
     * @return 考试次数
     */
    public int countUserAttempts(@Param("examId") Long examId, @Param("userId") Long userId);

    /**
     * 查询用户在某场考试下已占用的最大考试轮次号
     *
     * @param examId 考试ID
     * @param userId 用户ID
     * @return 最大轮次号
     */
    public int selectMaxAttemptNumber(@Param("examId") Long examId, @Param("userId") Long userId);

    /**
     * 查询某场考试已占用的最大考试轮次号
     *
     * @param examId 考试ID
     * @return 最大轮次号
     */
    public int selectMaxAttemptNumberByExamId(Long examId);

    /**
     * 统计用户总数
     * 
     * @return 用户总数
     */
    public int countTotalUsers();

    /**
     * 检查用户是否已报名
     * 
     * @param examId 考试ID
     * @param userId 用户ID
     * @return 结果
     */
    public int checkUserRegistered(@Param("examId") Long examId, @Param("userId") Long userId);

    /**
     * 检查用户是否可以参加考试
     * 
     * @param examId 考试ID
     * @param userId 用户ID
     * @return 结果
     */
    public int checkUserCanTakeExam(@Param("examId") Long examId, @Param("userId") Long userId);

    /**
     * 新增考试用户关联
     * 
     * @param examUser 考试用户关联
     * @return 结果
     */
    public int insertExamUser(ExamUser examUser);

    /**
     * 批量新增考试用户关联
     * 
     * @param examUsers 考试用户关联列表
     * @return 结果
     */
    public int batchInsertExamUser(List<ExamUser> examUsers);

    /**
     * 修改考试用户关联
     * 
     * @param examUser 考试用户关联
     * @return 结果
     */
    public int updateExamUser(ExamUser examUser);

    /**
     * 更新考试用户状态
     * 
     * @param id 考试用户关联ID
     * @param examStatus 考试状态
     * @return 结果
     */
    public int updateExamUserStatus(@Param("id") Long id, @Param("examStatus") String examStatus);

    /**
     * 更新用户报名状态
     * 
     * @param id 考试用户关联ID
     * @param registrationStatus 报名状态
     * @return 结果
     */
    public int updateRegistrationStatus(@Param("id") Long id, @Param("registrationStatus") String registrationStatus);

    /**
     * 开始考试
     * 
     * @param examUser 考试用户关联
     * @return 结果
     */
    public int startExam(ExamUser examUser);

    /**
     * 提交考试
     * 
     * @param examUser 考试用户关联
     * @return 结果
     */
    public int submitExam(ExamUser examUser);

    /**
     * 删除考试用户关联
     * 
     * @param id 考试用户关联主键
     * @return 结果
     */
    public int deleteExamUserById(Long id);

    /**
     * 批量删除考试用户关联
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteExamUserByIds(Long[] ids);

    /**
     * 根据考试ID删除考试用户关联
     * 
     * @param examId 考试ID
     * @return 结果
     */
    public int deleteExamUserByExamId(Long examId);

    /**
     * 根据考试ID和用户ID删除考试用户关联
     * 
     * @param examId 考试ID
     * @param userIds 用户ID数组
     * @return 结果
     */
    public int deleteExamUserByExamIdAndUserIds(@Param("examId") Long examId, @Param("userIds") Long[] userIds);

    /**
     * 根据考试ID查询考试用户列表（包含用户信息）
     * 
     * @param examId 考试ID
     * @return 考试用户关联集合
     */
    public List<ExamUser> selectExamUserListByExamId(Long examId);

    /**
     * 统计正在考试的用户数量
     * 
     * @param examId 考试ID
     * @return 正在考试的用户数量
     */
    public int countInProgressUsers(Long examId);

    /**
     * 强制提交所有未完成的考试
     * 
     * @param examId 考试ID
     * @return 影响的记录数
     */
    public int forceSubmitUnfinishedExams(Long examId);

    /**
     * 重置考试用户状态（用于重新发布考试）
     * 
     * @param examId 考试ID
     * @return 影响的记录数
     */
    public int resetExamUsersStatus(Long examId);

    /**
     * 查询已完成作答且仍可继续参加下一轮考试的用户记录
     *
     * @param examId 考试ID
     * @param maxAttempts 最大考试次数
     * @return 考试用户记录
     */
    public List<ExamUser> selectUsersNeedNextAttempt(@Param("examId") Long examId, @Param("maxAttempts") Integer maxAttempts);

    /**
     * 根据部门ID查询用户ID列表
     * 
     * @param deptId 部门ID
     * @return 用户ID列表
     */
    public List<Long> selectUserIdsByDeptId(Long deptId);

    /**
     * 递归查询部门及其所有子部门ID
     * 
     * @param deptId 部门ID
     * @return 部门ID列表（包含自身）
     */
    public List<Long> selectAllSubDeptIds(Long deptId);

    /**
     * 根据用户名查询用户ID
     * 
     * @param userName 用户名
     * @return 用户ID
     */
    public Long selectUserIdByUserName(String userName);

    /**
     * 审核考试用户
     * 
     * @param id 考试用户关联ID
     * @param status 审核状态
     * @return 结果
     */
    public int approveExamUser(@Param("id") Long id, @Param("status") String status);

    /**
     * 批量审核考试用户
     * 
     * @param ids 考试用户关联ID数组
     * @param status 审核状态
     * @return 结果
     */
    public int batchApproveExamUsers(@Param("ids") Long[] ids, @Param("status") String status);
}
