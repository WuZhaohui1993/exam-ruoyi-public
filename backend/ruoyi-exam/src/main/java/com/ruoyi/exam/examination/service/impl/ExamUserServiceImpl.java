package com.ruoyi.exam.examination.service.impl;

import static com.ruoyi.exam.constant.ExamStatusConstants.EXAM_IN_PROGRESS;
import static com.ruoyi.exam.constant.ExamStatusConstants.REGISTRATION_APPROVED;
import static com.ruoyi.exam.constant.ExamStatusConstants.REGISTRATION_REGISTERED;
import static com.ruoyi.exam.constant.ExamStatusConstants.REGISTRATION_REJECTED;
import static com.ruoyi.exam.constant.ExamStatusConstants.REUSABLE_ATTEMPT_STATUS;
import static com.ruoyi.exam.constant.ExamStatusConstants.USER_EXAM_IN_PROGRESS;

import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.exam.examination.domain.ExamExamination;
import com.ruoyi.exam.examination.domain.ExamUser;
import com.ruoyi.exam.examination.mapper.ExamExaminationMapper;
import com.ruoyi.exam.examination.mapper.ExamUserMapper;
import com.ruoyi.exam.examination.service.IExamUserService;

/**
 * 考试用户关联Service业务层处理
 * 
 * @author ruoyi
 */
@Service
public class ExamUserServiceImpl implements IExamUserService 
{
    @Autowired
    private ExamUserMapper examUserMapper;

    @Autowired
    private ExamExaminationMapper examExaminationMapper;

    /**
     * 查询考试用户关联
     * 
     * @param id 考试用户关联主键
     * @return 考试用户关联
     */
    @Override
    public ExamUser selectExamUserById(Long id)
    {
        return examUserMapper.selectExamUserById(id);
    }

    /**
     * 查询考试用户关联列表
     * 
     * @param examUser 考试用户关联
     * @return 考试用户关联
     */
    @Override
    public List<ExamUser> selectExamUserList(ExamUser examUser)
    {
        return examUserMapper.selectExamUserList(examUser);
    }

    /**
     * 根据用户ID查询考试记录
     * 
     * @param userId 用户ID
     * @return 考试记录列表
     */
    @Override
    public List<ExamUser> selectExamUserListByUserId(Long userId)
    {
        return examUserMapper.selectExamUserListByUserId(userId);
    }

    /**
     * 根据考试ID和用户ID查询考试用户关联
     * 
     * @param examId 考试ID
     * @param userId 用户ID
     * @return 考试用户关联
     */
    @Override
    public ExamUser selectExamUserByExamIdAndUserId(Long examId, Long userId)
    {
        return examUserMapper.selectExamUserByExamIdAndUserId(examId, userId);
    }

    /**
     * 根据考试ID和用户ID查询所有考试记录（支持多次考试）
     * 
     * @param examId 考试ID
     * @param userId 用户ID
     * @return 考试用户关联列表
     */
    @Override
    public List<ExamUser> selectExamUserListByExamIdAndUserId(Long examId, Long userId)
    {
        ExamUser queryParam = new ExamUser();
        queryParam.setExamId(examId);
        queryParam.setUserId(userId);
        return examUserMapper.selectExamUserList(queryParam);
    }

    /**
     * 根据考试ID查询考试用户列表
     * 
     * @param examId 考试ID
     * @return 考试用户列表
     */
    @Override
    public List<ExamUser> selectExamUserListByExamId(Long examId)
    {
        return examUserMapper.selectExamUserListByExamId(examId);
    }

    /**
     * 新增考试用户关联
     * 
     * @param examUser 考试用户关联
     * @return 结果
     */
    @Override
    public int insertExamUser(ExamUser examUser)
    {
        return examUserMapper.insertExamUser(examUser);
    }

    /**
     * 修改考试用户关联
     * 
     * @param examUser 考试用户关联
     * @return 结果
     */
    @Override
    public int updateExamUser(ExamUser examUser)
    {
        return examUserMapper.updateExamUser(examUser);
    }

    /**
     * 批量删除考试用户关联
     * 
     * @param ids 需要删除的考试用户关联主键
     * @return 结果
     */
    @Override
    public int deleteExamUserByIds(Long[] ids)
    {
        return examUserMapper.deleteExamUserByIds(ids);
    }

    /**
     * 删除考试用户关联信息
     * 
     * @param id 考试用户关联主键
     * @return 结果
     */
    @Override
    public int deleteExamUserById(Long id)
    {
        return examUserMapper.deleteExamUserById(id);
    }

    /**
     * 根据考试ID删除考试用户关联
     * 
     * @param examId 考试ID
     * @return 结果
     */
    @Override
    public int deleteExamUserByExamId(Long examId)
    {
        return examUserMapper.deleteExamUserByExamId(examId);
    }

    /**
     * 审核用户报名
     * 
     * @param examUserId 考试用户关联ID
     * @param status 审核状态
     * @return 结果
     */
    @Override
    public int approveExamUser(Long examUserId, String status)
    {
        return examUserMapper.approveExamUser(examUserId, status);
    }

    /**
     * 批量审核用户报名
     * 
     * @param examUserIds 考试用户关联ID数组
     * @param status 审核状态
     * @return 结果
     */
    @Override
    public int batchApproveExamUsers(Long[] examUserIds, String status)
    {
        return examUserMapper.batchApproveExamUsers(examUserIds, status);
    }

    /**
     * 统计用户考试次数
     * 
     * @param examId 考试ID
     * @param userId 用户ID
     * @return 考试次数
     */
    @Override
    public int countUserAttempts(Long examId, Long userId)
    {
        return examUserMapper.countUserAttempts(examId, userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ExamUser startStudentExam(Long examId, Long userId, Long deptId)
    {
        ExamExamination examination = examExaminationMapper.selectExamExaminationById(examId);
        if (examination == null)
        {
            throw new ServiceException("考试不存在");
        }
        if (!EXAM_IN_PROGRESS.equals(examination.getStatus()))
        {
            throw new ServiceException("考试尚未开始或已结束");
        }

        Date now = new Date();
        if (examination.getStartTime() != null && now.before(examination.getStartTime()))
        {
            throw new ServiceException("考试尚未开始");
        }
        if (examination.getEndTime() != null && now.after(examination.getEndTime()))
        {
            throw new ServiceException("考试已结束");
        }
        if (examExaminationMapper.checkUserExamAccess(examId, userId, deptId) == 0)
        {
            throw new ServiceException("您不在本次考试授权范围内");
        }

        ExamUser existingExamUser = examUserMapper.selectExamUserByExamIdAndUserId(examId, userId);
        int userAttempts = examUserMapper.countUserAttempts(examId, userId);
        int maxAttemptNumber = examUserMapper.selectMaxAttemptNumber(examId, userId);
        Integer maxAttempts = examination.getMaxAttempts() == null ? 1 : examination.getMaxAttempts();

        if (existingExamUser == null)
        {
            if ("1".equals(examination.getRegistrationRequired()))
            {
                throw new ServiceException("您未报名此考试");
            }

            ExamUser examUser = buildInProgressExamUser(examId, userId, deptId, now, nextAttemptNumber(userAttempts, maxAttemptNumber));
            examUserMapper.insertExamUser(examUser);
            return examUser;
        }

        if (REGISTRATION_REGISTERED.equals(existingExamUser.getRegistrationStatus()))
        {
            throw new ServiceException("您的报名尚未审核通过");
        }
        if (REGISTRATION_REJECTED.equals(existingExamUser.getRegistrationStatus()))
        {
            throw new ServiceException("您的报名已被拒绝");
        }

        if (USER_EXAM_IN_PROGRESS.equals(existingExamUser.getExamStatus()))
        {
            if (existingExamUser.getStartTime() == null)
            {
                existingExamUser.setStartTime(now);
                examUserMapper.updateExamUser(existingExamUser);
            }
            return existingExamUser;
        }

        if (REUSABLE_ATTEMPT_STATUS.contains(existingExamUser.getExamStatus()))
        {
            existingExamUser.setExamStatus(USER_EXAM_IN_PROGRESS);
            existingExamUser.setStartTime(existingExamUser.getStartTime() == null ? now : existingExamUser.getStartTime());
            existingExamUser.setAttemptCount(1);
            if (existingExamUser.getAttemptNumber() == null)
            {
                existingExamUser.setAttemptNumber(nextAttemptNumber(userAttempts, maxAttemptNumber));
            }
            examUserMapper.updateExamUser(existingExamUser);
            return existingExamUser;
        }

        if (userAttempts >= maxAttempts)
        {
            throw new ServiceException("您已超过最大考试次数");
        }

        ExamUser examUser = buildInProgressExamUser(examId, userId, deptId, now, nextAttemptNumber(userAttempts, maxAttemptNumber));
        examUserMapper.insertExamUser(examUser);
        return examUser;
    }

    private Integer nextAttemptNumber(int userAttempts, int maxAttemptNumber)
    {
        return Math.max(userAttempts, maxAttemptNumber) + 1;
    }

    private ExamUser buildInProgressExamUser(Long examId, Long userId, Long deptId, Date now, Integer attemptNumber)
    {
        ExamUser examUser = new ExamUser();
        examUser.setExamId(examId);
        examUser.setUserId(userId);
        examUser.setDeptId(deptId);
        examUser.setRegistrationTime(now);
        examUser.setRegistrationStatus(REGISTRATION_APPROVED);
        examUser.setExamStatus(USER_EXAM_IN_PROGRESS);
        examUser.setStartTime(now);
        examUser.setAttemptCount(1);
        examUser.setAttemptNumber(attemptNumber);
        return examUser;
    }

    /**
     * 统计用户总数
     * 
     * @return 用户总数
     */
    @Override
    public int countTotalUsers()
    {
        return examUserMapper.countTotalUsers();
    }
}
