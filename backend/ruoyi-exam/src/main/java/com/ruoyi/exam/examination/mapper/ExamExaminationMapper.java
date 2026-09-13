package com.ruoyi.exam.examination.mapper;

import java.util.Date;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.exam.examination.domain.ExamExamination;

/**
 * 考试Mapper接口
 * 
 * @author ruoyi
 */
public interface ExamExaminationMapper {
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
     * 根据状态查询考试列表
     * 
     * @param status 考试状态
     * @return 考试列表
     */
    public List<ExamExamination> selectExamExaminationListByStatus(String status);

    /**
     * 根据分类ID查询考试列表
     * 
     * @param categoryId 分类ID
     * @return 考试列表
     */
    public List<ExamExamination> selectExamExaminationListByCategoryId(Long categoryId);

    /**
     * 根据创建人查询考试列表
     * 
     * @param creatorId 创建人ID
     * @return 考试列表
     */
    public List<ExamExamination> selectExamExaminationListByCreatorId(Long creatorId);

    /**
     * 查询用户可参加的考试列表
     * 
     * @param examExamination 考试查询条件
     * @return 考试列表
     */
    public List<ExamExamination> selectAvailableExaminations(ExamExamination examExamination);

    /**
     * 查询学员端考试列表（包含智能排序和过滤）
     * 
     * @param examination 考试查询条件
     * @return 考试列表
     */
    public List<ExamExamination> selectStudentExamList(ExamExamination examination);

    /**
     * 查询考试授权部门ID列表
     *
     * @param examId 考试ID
     * @return 部门ID列表
     */
    public List<Long> selectDeptIdsByExamId(Long examId);

    /**
     * 查询考试授权部门是否包含子部门
     *
     * @param examId 考试ID
     * @return 0否 1是
     */
    public String selectIncludeChildDeptByExamId(Long examId);

    /**
     * 统计考试授权部门数量
     *
     * @param examId 考试ID
     * @return 授权部门数量
     */
    public int countExamDeptByExamId(Long examId);

    /**
     * 删除考试授权部门
     *
     * @param examId 考试ID
     * @return 结果
     */
    public int deleteExamDeptByExamId(Long examId);

    /**
     * 批量新增考试授权部门
     *
     * @param examId 考试ID
     * @param deptIds 部门ID集合
     * @param includeChildDept 是否包含子部门
     * @param createBy 创建者
     * @return 结果
     */
    public int batchInsertExamDept(@Param("examId") Long examId, @Param("deptIds") List<Long> deptIds,
            @Param("includeChildDept") String includeChildDept, @Param("createBy") String createBy);

    /**
     * 校验用户是否在考试授权范围内
     *
     * @param examId 考试ID
     * @param userId 用户ID
     * @param deptId 部门ID
     * @return 命中数量
     */
    public int checkUserExamAccess(@Param("examId") Long examId, @Param("userId") Long userId,
            @Param("deptId") Long deptId);

    /**
     * 校验考试名称是否唯一
     * 
     * @param examName 考试名称
     * @return 结果
     */
    public ExamExamination checkExamNameUnique(String examName);

    /**
     * 统计考试数量
     * 
     * @param examExamination 查询条件
     * @return 考试数量
     */
    public int countExaminations(ExamExamination examExamination);

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
     * 更新考试状态
     * 
     * @param id     考试ID
     * @param status 新状态
     * @return 结果
     */
    public int updateExamStatus(@Param("id") Long id, @Param("status") String status);

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
     * 删除考试
     * 
     * @param id 考试主键
     * @return 结果
     */
    public int deleteExamExaminationById(Long id);

    /**
     * 批量删除考试
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteExamExaminationByIds(Long[] ids);

    /**
     * 根据状态和时间查询考试列表（用于自动开始）
     * 
     * @param status      考试状态
     * @param currentTime 当前时间
     * @return 考试列表
     */
    public List<ExamExamination> selectExaminationsByStatusAndTime(@Param("status") String status,
            @Param("currentTime") Date currentTime);

    /**
     * 根据状态和结束时间查询考试列表（用于自动结束）
     * 
     * @param status      考试状态
     * @param currentTime 当前时间
     * @return 考试列表
     */
    public List<ExamExamination> selectExaminationsByStatusAndEndTime(@Param("status") String status,
            @Param("currentTime") Date currentTime);

    /**
     * 根据状态统计考试数量
     * 
     * @param status 考试状态
     * @return 考试数量
     */
    public int countByStatus(String status);

    /**
     * 统计已发布的考试数量
     * 
     * @return 考试数量
     */
    public int countPublishedExaminations();

    /**
     * 统计业务数据（今日、本周、本月考试数量）
     * 
     * @return 统计结果
     */
    public java.util.Map<String, Object> selectBusinessStatistics();
}
