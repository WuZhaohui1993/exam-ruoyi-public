package com.ruoyi.exam.examination.service;

import java.util.List;
import com.ruoyi.exam.examination.domain.ExamExamination;
import com.ruoyi.exam.examination.domain.ExamUser;

/**
 * 考试Service接口
 * 
 * @author ruoyi
 */
public interface IExamExaminationService {
    /**
     * 查询考试
     * 
     * @param id 考试主键
     * @return 考试
     */
    public ExamExamination selectExamExaminationById(Long id);

    /**
     * 查询考试列表
     * 
     * @param examExamination 考试
     * @return 考试集合
     */
    public List<ExamExamination> selectExamExaminationList(ExamExamination examExamination);

    /**
     * 查询考试详细信息（包含关联信息）
     * 
     * @param id 考试主键
     * @return 考试
     */
    public ExamExamination selectExamExaminationWithDetails(Long id);

    /**
     * 查询用户可参加的考试列表
     * 
     * @param userId 用户ID
     * @return 考试列表
     */
    public List<ExamExamination> selectAvailableExaminations(Long userId);

    /**
     * 查询学员端考试列表（包含智能排序和过滤）
     * 
     * @param examination 考试查询条件
     * @return 考试列表
     */
    public List<ExamExamination> selectStudentExamList(ExamExamination examination);

    /**
     * 获取考试关联的用户ID列表
     * 
     * @param examId 考试ID
     * @return 用户ID列表
     */
    public List<Long> getExamUserIds(Long examId);

    /**
     * 获取考试授权部门ID列表
     *
     * @param examId 考试ID
     * @return 部门ID列表
     */
    public List<Long> getExamDeptIds(Long examId);

    /**
     * 用户是否有权限访问/参加该考试
     *
     * @param examId 考试ID
     * @param userId 用户ID
     * @param deptId 部门ID
     * @return true 有权限
     */
    public boolean canUserAccessExam(Long examId, Long userId, Long deptId);

    /**
     * 校验考试名称是否唯一
     * 
     * @param examExamination 考试信息
     * @return 结果
     */
    public String checkExamNameUnique(ExamExamination examExamination);

    /**
     * 新增考试
     * 
     * @param examExamination 考试
     * @return 结果
     */
    public int insertExamExamination(ExamExamination examExamination);

    /**
     * 修改考试
     * 
     * @param examExamination 考试
     * @return 结果
     */
    public int updateExamExamination(ExamExamination examExamination);

    /**
     * 发布考试
     * 
     * @param id 考试ID
     * @return 结果
     */
    public int publishExamination(Long id);

    /**
     * 取消考试
     * 
     * @param id 考试ID
     * @return 结果
     */
    public int cancelExamination(Long id);

    /**
     * 将已取消且没有考试作答记录的考试恢复为草稿
     *
     * @param id 考试ID
     * @return 结果
     */
    public int restoreDraftExamination(Long id);

    /**
     * 开始考试（自动更新状态）
     * 
     * @param id 考试ID
     * @return 结果
     */
    public int startExamination(Long id);

    /**
     * 结束考试（自动更新状态）
     * 
     * @param id 考试ID
     * @return 结果
     */
    public int endExamination(Long id);

    /**
     * 添加考试人员
     * 
     * @param examId  考试ID
     * @param userIds 用户ID数组
     * @return 结果
     */
    public int addExamUsers(Long examId, Long[] userIds);

    /**
     * 移除考试人员
     * 
     * @param examId  考试ID
     * @param userIds 用户ID数组
     * @return 结果
     */
    public int removeExamUsers(Long examId, Long[] userIds);

    /**
     * 同步更新考试人员（先删除所有关联，再添加新的用户列表）
     * 
     * @param examId  考试ID
     * @param userIds 用户ID数组
     * @return 结果
     */
    public int updateExamUsers(Long examId, Long[] userIds);

    /**
     * 批量添加考试人员（按部门）
     * 
     * @param examId  考试ID
     * @param deptIds 部门ID数组
     * @return 结果
     */
    public int addExamUsersByDept(Long examId, Long[] deptIds);

    /**
     * 批量添加考试人员（按部门，包含子部门）
     * 
     * @param examId  考试ID
     * @param deptIds 部门ID数组
     * @return 结果
     */
    public int addExamUsersByDeptWithChildren(Long examId, Long[] deptIds);

    /**
     * 审核用户报名
     * 
     * @param examUserId         考试用户关联ID
     * @param registrationStatus 审核状态
     * @return 结果
     */
    public int approveUserRegistration(Long examUserId, String registrationStatus);

    /**
     * 批量审核用户报名
     * 
     * @param examUserIds        考试用户关联ID数组
     * @param registrationStatus 审核状态
     * @return 结果
     */
    public int batchApproveUserRegistration(Long[] examUserIds, String registrationStatus);

    /**
     * 发布考试成绩
     * 
     * @param examId 考试ID
     * @return 结果
     */
    public int publishExamResults(Long examId);

    /**
     * 手动阅卷
     * 
     * @param examId 考试ID
     * @param userId 用户ID
     * @return 结果
     */
    public int manualGrading(Long examId, Long userId);

    /**
     * 统计考试数据
     * 
     * @param examId 考试ID
     * @return 统计结果
     */
    public ExamExamination getExamStatistics(Long examId);

    /**
     * 更新考试统计数据
     * 
     * @param examId 考试ID
     */
    public void updateExamStatistics(Long examId);

    /**
     * 导出考试报名信息
     * 
     * @param examId 考试ID
     * @return 导出数据
     */
    public List<ExamUser> exportExamUsers(Long examId);

    /**
     * 批量删除考试
     * 
     * @param ids 需要删除的考试主键集合
     * @return 结果
     */
    public int deleteExamExaminationByIds(Long[] ids);

    /**
     * 删除考试信息
     * 
     * @param id 考试主键
     * @return 结果
     */
    public int deleteExamExaminationById(Long id);

    /**
     * 关联证书模板
     * 
     * @param examId                考试ID
     * @param certificateTemplateId 证书模板ID
     * @return 结果
     */
    public int associateCertificate(Long examId, Long certificateTemplateId);

    /**
     * 审核考试用户
     * 
     * @param examUserId 考试用户ID
     * @param status     审核状态
     * @return 结果
     */
    public int auditExamUser(Long examUserId, String status);

    /**
     * 发布成绩
     * 
     * @param examId 考试ID
     * @return 结果
     */
    public int publishResults(Long examId);

    /**
     * 批量导入考试用户
     * 
     * @param examId        考试ID
     * @param examUserList  考试用户列表
     * @param updateSupport 是否更新支持
     * @param operName      操作用户
     * @return 结果消息
     */
    public String importExamUsers(Long examId, List<ExamUser> examUserList, Boolean updateSupport, String operName);

    /**
     * 重置单次考试记录（删除该考试记录的答题数据后重建未开始记录）
     *
     * @param examUserId 考试用户关联ID
     * @return 结果（处理的记录数）
     */
    public int resetUserExam(Long examUserId);

    /**
     * 统计已发布的考试数量
     * 
     * @return 考试数量
     */
    public int countPublishedExaminations();

    /**
     * 导出成绩统计
     * 
     * @param examId   考试ID
     * @param examUser 查询条件
     * @return 成绩统计数据
     */
    public List<ExamUser> exportScoreStatistics(Long examId, ExamUser examUser);

    /**
     * 导出详细成绩
     * 
     * @param examId   考试ID
     * @param examUser 查询条件
     * @return 详细成绩数据
     */
    public List<ExamUser> exportScoreDetails(Long examId, ExamUser examUser);

    /**
     * 导出详细成绩（扁平化结构）
     * 
     * @param examId   考试ID
     * @param examUser 查询条件
     * @return 详细成绩数据（扁平化）
     */
    public List<com.ruoyi.exam.examination.domain.ExamScoreDetailExportDTO> exportScoreDetailsFlat(Long examId,
            ExamUser examUser);

    /**
     * 统计业务概览数据（今日、本周、本月考试数量）
     * 
     * @return 统计结果
     */
    public java.util.Map<String, Object> selectBusinessStatistics();
}
