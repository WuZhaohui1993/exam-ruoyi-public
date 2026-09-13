package com.ruoyi.exam.examination.service.impl;

import static com.ruoyi.exam.constant.ExamStatusConstants.EXAM_CANCELLED;
import static com.ruoyi.exam.constant.ExamStatusConstants.EXAM_DRAFT;
import static com.ruoyi.exam.constant.ExamStatusConstants.EXAM_FINISHED;
import static com.ruoyi.exam.constant.ExamStatusConstants.EXAM_IN_PROGRESS;
import static com.ruoyi.exam.constant.ExamStatusConstants.EXAM_PUBLISHED;
import static com.ruoyi.exam.constant.ExamStatusConstants.REGISTRATION_APPROVED;
import static com.ruoyi.exam.constant.ExamStatusConstants.USER_EXAM_GRADED;
import static com.ruoyi.exam.constant.ExamStatusConstants.USER_EXAM_IN_PROGRESS;
import static com.ruoyi.exam.constant.ExamStatusConstants.USER_EXAM_NOT_STARTED;
import static com.ruoyi.exam.constant.ExamStatusConstants.USER_EXAM_SUBMITTED;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.html.HtmlSanitizer;
import com.ruoyi.exam.examination.domain.ExamExamination;
import com.ruoyi.exam.examination.domain.ExamUser;
import com.ruoyi.exam.examination.mapper.ExamExaminationMapper;
import com.ruoyi.exam.examination.mapper.ExamUserMapper;
import com.ruoyi.exam.examination.service.IExamExaminationService;
import com.ruoyi.exam.examination.service.IExamAnswerRecordService;
import com.ruoyi.exam.examination.domain.ExamAnswerRecord;
import com.ruoyi.exam.paper.domain.ExamPaper;
import com.ruoyi.exam.paper.domain.ExamPaperQuestion;
import com.ruoyi.exam.paper.service.IExamPaperService;
import com.ruoyi.exam.paper.service.IExamPaperQuestionService;
import com.ruoyi.system.service.ISysUserService;
import com.ruoyi.common.core.domain.entity.SysUser;

/**
 * 考试发布Service业务层处理
 * 
 * @author ruoyi
 * @date 2024-06-10
 */
@Service
public class ExamExaminationServiceImpl implements IExamExaminationService {
    @Autowired
    private ExamExaminationMapper examExaminationMapper;

    @Autowired
    private ExamUserMapper examUserMapper;

    @Autowired
    private ISysUserService userService;

    @Autowired
    private IExamAnswerRecordService examAnswerRecordService;

    @Autowired
    private IExamPaperService examPaperService;

    @Autowired
    private IExamPaperQuestionService examPaperQuestionService;

    /**
     * 查询考试发布
     * 
     * @param id 考试发布主键
     * @return 考试发布
     */
    @Override
    public ExamExamination selectExamExaminationById(Long id) {
        ExamExamination examination = examExaminationMapper.selectExamExaminationById(id);
        fillExamDeptScope(examination);
        return examination;
    }

    /**
     * 查询考试发布详细信息（包含统计数据）
     * 
     * @param id 考试发布主键
     * @return 考试发布
     */
    @Override
    public ExamExamination selectExamExaminationWithDetails(Long id) {
        ExamExamination examination = examExaminationMapper.selectExamExaminationWithDetails(id);
        if (examination != null) {
            fillExamDeptScope(examination);
            // 获取考试关联的用户详细信息
            List<ExamUser> examUsers = examUserMapper.selectExamUserByExamId(id);
            examination.setExamUsers(examUsers);

            // 获取用户ID列表（用于编辑时回显）
            List<Long> userIds = examUsers.stream()
                    .map(ExamUser::getUserId)
                    .collect(java.util.stream.Collectors.toList());
            examination.setExamUserIds(userIds);
        }
        return examination;
    }

    /**
     * 查询考试发布列表
     * 
     * @param examExamination 考试发布
     * @return 考试发布
     */
    @Override
    public List<ExamExamination> selectExamExaminationList(ExamExamination examExamination) {
        return examExaminationMapper.selectExamExaminationList(examExamination);
    }

    /**
     * 根据状态查询考试列表
     * 
     * @param status 考试状态
     * @return 考试列表
     */
    public List<ExamExamination> selectExamExaminationListByStatus(String status) {
        return examExaminationMapper.selectExamExaminationListByStatus(status);
    }

    /**
     * 根据分类ID查询考试列表
     * 
     * @param categoryId 分类ID
     * @return 考试列表
     */
    public List<ExamExamination> selectExamExaminationListByCategoryId(Long categoryId) {
        return examExaminationMapper.selectExamExaminationListByCategoryId(categoryId);
    }

    /**
     * 根据创建者ID查询考试列表
     * 
     * @param creatorId 创建者ID
     * @return 考试列表
     */
    public List<ExamExamination> selectExamExaminationListByCreatorId(Long creatorId) {
        return examExaminationMapper.selectExamExaminationListByCreatorId(creatorId);
    }

    /**
     * 查询用户可参与的考试列表
     * 
     * @param userId 用户ID
     * @return 考试列表
     */
    @Override
    public List<ExamExamination> selectAvailableExaminations(Long userId) {
        SysUser user = userService.selectUserById(userId);
        ExamExamination query = new ExamExamination();
        query.setParams(new HashMap<>());
        query.getParams().put("userId", userId);
        if (user != null) {
            query.getParams().put("deptId", user.getDeptId());
        }
        return examExaminationMapper.selectAvailableExaminations(query);
    }

    /**
     * 查询学员端考试列表（包含智能排序和过滤）
     * 
     * @param examination 考试查询条件
     * @return 考试列表
     */
    @Override
    public List<ExamExamination> selectStudentExamList(ExamExamination examination) {
        if (examination != null && examination.getParams() != null
                && examination.getParams().get("userId") != null
                && examination.getParams().get("deptId") == null) {
            Long userId = Long.valueOf(String.valueOf(examination.getParams().get("userId")));
            SysUser user = userService.selectUserById(userId);
            if (user != null) {
                examination.getParams().put("deptId", user.getDeptId());
            }
        }
        return examExaminationMapper.selectStudentExamList(examination);
    }

    /**
     * 获取考试关联的用户ID列表
     * 
     * @param examId 考试ID
     * @return 用户ID列表
     */
    @Override
    public List<Long> getExamUserIds(Long examId) {
        return examUserMapper.selectUserIdsByExamId(examId);
    }

    /**
     * 获取考试授权部门ID列表
     *
     * @param examId 考试ID
     * @return 部门ID列表
     */
    @Override
    public List<Long> getExamDeptIds(Long examId) {
        return examExaminationMapper.selectDeptIdsByExamId(examId);
    }

    /**
     * 用户是否在考试授权范围内。
     *
     * 有显式参考人员时按人员放行；有授权部门时按部门放行；二者都没有时兼容旧考试为全员可见。
     */
    @Override
    public boolean canUserAccessExam(Long examId, Long userId, Long deptId) {
        if (examId == null || userId == null) {
            return false;
        }
        if (deptId == null) {
            SysUser user = userService.selectUserById(userId);
            deptId = user == null ? null : user.getDeptId();
        }
        return examExaminationMapper.checkUserExamAccess(examId, userId, deptId) > 0;
    }

    /**
     * 校验考试名称是否唯一
     * 
     * @param examExamination 考试信息
     * @return 结果
     */
    @Override
    public String checkExamNameUnique(ExamExamination examExamination) {
        Long examId = StringUtils.isNull(examExamination.getId()) ? -1L : examExamination.getId();
        ExamExamination exam = examExaminationMapper.checkExamNameUnique(examExamination.getExamName());
        if (StringUtils.isNotNull(exam) && exam.getId().longValue() != examId.longValue()) {
            return "1";
        }
        return "0";
    }

    /**
     * 统计考试数量
     * 
     * @param examExamination 考试条件
     * @return 考试数量
     */
    public int countExaminations(ExamExamination examExamination) {
        return examExaminationMapper.countExaminations(examExamination);
    }

    /**
     * 新增考试发布
     * 
     * @param examExamination 考试发布
     * @return 结果
     */
    @Override
    @Transactional
    public int insertExamExamination(ExamExamination examExamination) {
        sanitizeExamExamination(examExamination);

        // 设置创建者信息
        examExamination.setCreatorId(SecurityUtils.getUserId());
        examExamination.setCreateBy(SecurityUtils.getUsername());

        if (StringUtils.isEmpty(examExamination.getStatus())) {
            examExamination.setStatus(EXAM_DRAFT);
        } else if (!EXAM_DRAFT.equals(examExamination.getStatus())) {
            throw new ServiceException("新增考试只能保存为草稿，请通过发布操作切换考试状态");
        }

        // 设置默认值
        if (examExamination.getMaxAttempts() == null) {
            examExamination.setMaxAttempts(1);
        }

        int result = examExaminationMapper.insertExamExamination(examExamination);
        saveExamDeptScope(examExamination);
        return result;
    }

    /**
     * 修改考试发布
     * 
     * @param examExamination 考试发布
     * @return 结果
     */
    @Override
    @Transactional
    public int updateExamExamination(ExamExamination examExamination) {
        sanitizeExamExamination(examExamination);

        // 检查考试状态是否允许修改
        ExamExamination existingExam = examExaminationMapper.selectExamExaminationById(examExamination.getId());
        if (existingExam == null) {
            throw new ServiceException("考试不存在");
        }

        if (StringUtils.isNotEmpty(examExamination.getStatus())
                && !Objects.equals(examExamination.getStatus(), existingExam.getStatus())) {
            throw new ServiceException("考试状态请通过发布、开始、结束、取消或恢复草稿等专用操作调整");
        }

        // 检查考试状态限制
        if (EXAM_FINISHED.equals(existingExam.getStatus()) || EXAM_CANCELLED.equals(existingExam.getStatus())) {
            throw new ServiceException("考试已结束或已取消，不允许修改");
        }

        // 考试进行中的特殊限制检查
        if (EXAM_IN_PROGRESS.equals(existingExam.getStatus())) {
            validateExamInProgressUpdate(examExamination, existingExam);
        }

        examExamination.setUpdateBy(SecurityUtils.getUsername());
        int result = examExaminationMapper.updateExamExamination(examExamination);
        saveExamDeptScope(examExamination);
        return result;
    }

    private void fillExamDeptScope(ExamExamination examination) {
        if (examination == null || examination.getId() == null) {
            return;
        }
        List<Long> deptIds = examExaminationMapper.selectDeptIdsByExamId(examination.getId());
        examination.setExamDeptIds(deptIds);
        String includeChildDept = examExaminationMapper.selectIncludeChildDeptByExamId(examination.getId());
        examination.setIncludeChildDept(StringUtils.isEmpty(includeChildDept) ? "1" : includeChildDept);
    }

    private void saveExamDeptScope(ExamExamination examExamination) {
        if (examExamination == null || examExamination.getId() == null || examExamination.getExamDeptIds() == null) {
            return;
        }

        examExaminationMapper.deleteExamDeptByExamId(examExamination.getId());
        List<Long> deptIds = examExamination.getExamDeptIds().stream()
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (!deptIds.isEmpty()) {
            String includeChildDept = "0".equals(examExamination.getIncludeChildDept()) ? "0" : "1";
            examExaminationMapper.batchInsertExamDept(examExamination.getId(), deptIds, includeChildDept,
                    SecurityUtils.getUsername());
        }
    }

    private void sanitizeExamExamination(ExamExamination examExamination) {
        if (examExamination == null) {
            return;
        }
        examExamination.setExamName(HtmlSanitizer.cleanText(examExamination.getExamName()));
        examExamination.setExamDescription(HtmlSanitizer.cleanRichText(examExamination.getExamDescription()));
        examExamination.setExamType(HtmlSanitizer.cleanText(examExamination.getExamType()));
        examExamination.setExamMode(HtmlSanitizer.cleanText(examExamination.getExamMode()));
        examExamination.setMonitorMode(HtmlSanitizer.cleanText(examExamination.getMonitorMode()));
        examExamination.setRemark(HtmlSanitizer.cleanText(examExamination.getRemark()));
    }

    /**
     * 更新考试状态
     * 
     * @param id     考试ID
     * @param status 新状态
     * @return 结果
     */
    @Transactional
    public int updateExamStatus(Long id, String status) {
        return examExaminationMapper.updateExamStatus(id, status);
    }

    /**
     * 发布考试
     * 
     * @param id 考试ID
     * @return 结果
     */
    @Override
    @Transactional
    public int publishExamination(Long id) {
        ExamExamination exam = examExaminationMapper.selectExamExaminationById(id);
        if (exam == null) {
            throw new ServiceException("考试不存在");
        }

        // 验证当前状态 - 允许草稿、已取消、已结束状态的考试发布/重新发布
        if (!EXAM_DRAFT.equals(exam.getStatus()) && !EXAM_FINISHED.equals(exam.getStatus())
                && !EXAM_CANCELLED.equals(exam.getStatus())) {
            throw new ServiceException("只有草稿、已结束或已取消状态的考试才能发布");
        }

        // 验证考试信息完整性
        validateExamForPublish(exam);

        // 验证是否配置了参考人员或部门授权范围
        List<ExamUser> examUsers = examUserMapper.selectExamUserListByExamId(id);
        int deptScopeCount = examExaminationMapper.countExamDeptByExamId(id);
        if ((examUsers == null || examUsers.isEmpty()) && deptScopeCount == 0) {
            throw new ServiceException("请先添加参考人员或配置授权部门后再发布考试");
        }

        // 如果是重新发布，需要重置相关数据
        if (EXAM_FINISHED.equals(exam.getStatus()) || EXAM_CANCELLED.equals(exam.getStatus())) {
            // 重置考试用户状态为已报名
            examUserMapper.resetExamUsersStatus(id);
            createNextAttemptsForRepublish(exam);
        }

        return examExaminationMapper.updateExamStatus(id, EXAM_PUBLISHED);
    }

    private void createNextAttemptsForRepublish(ExamExamination exam) {
        Integer maxAttempts = exam.getMaxAttempts() == null ? 1 : exam.getMaxAttempts();
        List<ExamUser> usersNeedNextAttempt = examUserMapper.selectUsersNeedNextAttempt(exam.getId(), maxAttempts);
        if (usersNeedNextAttempt == null || usersNeedNextAttempt.isEmpty()) {
            return;
        }

        Date now = new Date();
        for (ExamUser sourceUser : usersNeedNextAttempt) {
            if (sourceUser == null || sourceUser.getUserId() == null) {
                continue;
            }

            int nextAttemptNumber = examUserMapper.selectMaxAttemptNumber(exam.getId(), sourceUser.getUserId()) + 1;
            if (nextAttemptNumber > maxAttempts) {
                continue;
            }

            SysUser user = userService.selectUserById(sourceUser.getUserId());
            ExamUser nextAttempt = new ExamUser();
            nextAttempt.setExamId(exam.getId());
            nextAttempt.setUserId(sourceUser.getUserId());
            nextAttempt.setDeptId(user != null ? user.getDeptId() : sourceUser.getDeptId());
            nextAttempt.setRegistrationTime(now);
            nextAttempt.setRegistrationStatus(REGISTRATION_APPROVED);
            nextAttempt.setExamStatus(USER_EXAM_NOT_STARTED);
            nextAttempt.setAttemptCount(0);
            nextAttempt.setAttemptNumber(nextAttemptNumber);
            nextAttempt.setCreateBy(SecurityUtils.getUsername());
            examUserMapper.insertExamUser(nextAttempt);
        }
    }

    /**
     * 取消考试
     * 
     * @param id 考试ID
     * @return 结果
     */
    @Override
    @Transactional
    public int cancelExamination(Long id) {
        ExamExamination exam = examExaminationMapper.selectExamExaminationById(id);
        if (exam == null) {
            throw new ServiceException("考试不存在");
        }

        // 验证当前状态 - 只有已发布或进行中的考试可以取消
        if (!EXAM_PUBLISHED.equals(exam.getStatus()) && !EXAM_IN_PROGRESS.equals(exam.getStatus())) {
            throw new ServiceException("只有已发布或进行中的考试才能取消");
        }

        // 如果考试正在进行中，需要特殊处理
        if (EXAM_IN_PROGRESS.equals(exam.getStatus())) {
            // 检查是否有人正在考试
            int inProgressCount = examUserMapper.countInProgressUsers(id);
            if (inProgressCount > 0) {
                throw new ServiceException("考试正在进行中，有 " + inProgressCount + " 人正在答题，无法取消");
            }
        }

        return examExaminationMapper.updateExamStatus(id, EXAM_CANCELLED);
    }

    /**
     * 恢复为草稿
     *
     * @param id 考试ID
     * @return 结果
     */
    @Override
    @Transactional
    public int restoreDraftExamination(Long id) {
        ExamExamination exam = examExaminationMapper.selectExamExaminationById(id);
        if (exam == null) {
            throw new ServiceException("考试不存在");
        }
        if (!EXAM_CANCELLED.equals(exam.getStatus())) {
            throw new ServiceException("只有已取消的考试才能恢复为草稿");
        }

        List<ExamUser> existingUsers = examUserMapper.selectExamUserListByExamId(id);
        Set<Long> allUserIds = existingUsers.stream()
                .map(ExamUser::getUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Set<Long> protectedUserIds = findProtectedUserIds(existingUsers, allUserIds);
        if (!protectedUserIds.isEmpty()) {
            throw new ServiceException("考试已有作答记录，不能恢复为草稿；请重新发布或新建考试");
        }

        examUserMapper.resetExamUsersStatus(id);
        return examExaminationMapper.updateExamStatus(id, EXAM_DRAFT);
    }

    /**
     * 添加考试人员
     * 
     * @param examId  考试ID
     * @param userIds 用户ID数组
     * @return 结果
     */
    @Override
    @Transactional
    public int addExamUsers(Long examId, Long[] userIds) {
        if (userIds == null || userIds.length == 0) {
            throw new ServiceException("请选择要添加的人员");
        }

        ExamExamination exam = examExaminationMapper.selectExamExaminationById(examId);
        if (exam == null) {
            throw new ServiceException("考试不存在");
        }

        Set<Long> existingUserIds = new HashSet<>(examUserMapper.selectUserIdsByExamId(examId));
        int successCount = 0;
        for (Long userId : userIds) {
            if (userId == null) {
                continue;
            }
            boolean existed = existingUserIds.contains(userId);
            // 获取用户信息来获取部门ID
            SysUser user = userService.selectUserById(userId);
            if (user == null) {
                continue; // 用户不存在，跳过
            }

            ExamUser examUser = new ExamUser();
            examUser.setExamId(examId);
            examUser.setUserId(userId);
            examUser.setDeptId(user.getDeptId()); // 设置用户的部门ID
            examUser.setRegistrationTime(new Date());
            examUser.setRegistrationStatus(REGISTRATION_APPROVED);
            examUser.setExamStatus(USER_EXAM_NOT_STARTED);
            examUser.setAttemptCount(0);
            examUser.setAttemptNumber(1); // 设置默认尝试次数为1

            try {
                if (examUserMapper.insertExamUser(examUser) > 0) {
                    if (!existed) {
                        successCount++;
                        existingUserIds.add(userId);
                    }
                }
            } catch (Exception e) {
                // 如果插入失败（可能是约束冲突），忽略错误继续处理
                // 这通常是因为用户已存在于考试中
            }
        }

        // 更新考试统计数据
        updateExamStatistics(examId);

        return successCount;
    }

    /**
     * 移除考试人员
     * 
     * @param examId  考试ID
     * @param userIds 用户ID数组
     * @return 结果
     */
    @Override
    @Transactional
    public int removeExamUsers(Long examId, Long[] userIds) {
        if (userIds == null || userIds.length == 0) {
            throw new ServiceException("请选择要移除的人员");
        }

        // 获取考试信息检查状态
        ExamExamination exam = examExaminationMapper.selectExamExaminationById(examId);
        if (exam == null) {
            throw new ServiceException("考试不存在");
        }

        List<ExamUser> existingUsers = examUserMapper.selectExamUserListByExamId(examId);
        Set<Long> blockedUserIds = findProtectedUserIds(existingUsers, new HashSet<Long>(java.util.Arrays.asList(userIds)));
        if (!blockedUserIds.isEmpty()) {
            throw new ServiceException("用户【" + buildUserNames(blockedUserIds) + "】已有考试记录，不允许从参考人员中删除");
        }

        int result = examUserMapper.deleteExamUserByExamIdAndUserIds(examId, userIds);

        // 更新考试统计数据
        updateExamStatistics(examId);

        return result;
    }

    /**
     * 同步更新考试人员（先删除所有关联，再添加新的用户列表）
     * 
     * @param examId  考试ID
     * @param userIds 用户ID数组
     * @return 结果
     */
    @Override
    @Transactional
    public int updateExamUsers(Long examId, Long[] userIds) {
        ExamExamination exam = examExaminationMapper.selectExamExaminationById(examId);
        if (exam == null) {
            throw new ServiceException("考试不存在");
        }

        Set<Long> targetUserIds = new HashSet<>();
        if (userIds != null) {
            for (Long userId : userIds) {
                if (userId != null) {
                    targetUserIds.add(userId);
                }
            }
        }

        List<ExamUser> existingUsers = examUserMapper.selectExamUserListByExamId(examId);
        Set<Long> existingUserIds = existingUsers.stream()
                .map(ExamUser::getUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Set<Long> removedUserIds = existingUserIds.stream()
                .filter(userId -> !targetUserIds.contains(userId))
                .collect(Collectors.toSet());
        Set<Long> protectedRemovedUserIds = findProtectedUserIds(existingUsers, removedUserIds);
        if (!protectedRemovedUserIds.isEmpty()) {
            throw new ServiceException("用户【" + buildUserNames(protectedRemovedUserIds) + "】已有考试记录，不允许从参考人员中删除");
        }
        if (!removedUserIds.isEmpty()) {
            examUserMapper.deleteExamUserByExamIdAndUserIds(examId, removedUserIds.toArray(new Long[0]));
        }

        Set<Long> addUserIds = targetUserIds.stream()
                .filter(userId -> !existingUserIds.contains(userId))
                .collect(Collectors.toSet());
        if (!addUserIds.isEmpty()) {
            addExamUsers(examId, addUserIds.toArray(new Long[0]));
        }

        updateExamStatistics(examId);
        return (int) examUserMapper.selectUserIdsByExamId(examId).stream().distinct().count();
    }

    /**
     * 审核考试人员
     * 
     * @param examUserId 考试用户ID
     * @param status     审核状态
     * @return 结果
     */
    @Override
    @Transactional
    public int auditExamUser(Long examUserId, String status) {
        return examUserMapper.updateRegistrationStatus(examUserId, status);
    }

    /**
     * 发布考试成绩
     * 
     * @param examId 考试ID
     * @return 结果
     */
    @Override
    @Transactional
    public int publishResults(Long examId) {
        ExamExamination exam = examExaminationMapper.selectExamExaminationById(examId);
        if (exam == null) {
            throw new ServiceException("考试不存在");
        }

        // 更新成绩发布状态
        ExamExamination updateExam = new ExamExamination();
        updateExam.setId(examId);
        updateExam.setResultPublishType("immediate");
        updateExam.setResultPublishTime(new Date());
        updateExam.setUpdateBy(SecurityUtils.getUsername());

        return examExaminationMapper.updateExamExamination(updateExam);
    }

    /**
     * 关联证书模板
     * 
     * @param examId                考试ID
     * @param certificateTemplateId 证书模板ID
     * @return 结果
     */
    @Override
    @Transactional
    public int associateCertificate(Long examId, Long certificateTemplateId) {
        ExamExamination exam = new ExamExamination();
        exam.setId(examId);
        exam.setCertificateTemplateId(certificateTemplateId);
        exam.setUpdateBy(SecurityUtils.getUsername());

        return examExaminationMapper.updateExamExamination(exam);
    }

    /**
     * 批量删除考试发布
     * 
     * @param ids 需要删除的考试发布主键
     * @return 结果
     */
    @Override
    @Transactional
    public int deleteExamExaminationByIds(Long[] ids) {
        // 删除前检查考试状态
        for (Long id : ids) {
            ExamExamination exam = examExaminationMapper.selectExamExaminationById(id);
            if (exam != null && !EXAM_DRAFT.equals(exam.getStatus())) {
                throw new ServiceException("只有草稿状态的考试允许删除，请先取消或归档后处理");
            }
        }

        // 删除相关的考试用户记录
        for (Long id : ids) {
            examUserMapper.deleteExamUserByExamId(id);
            examExaminationMapper.deleteExamDeptByExamId(id);
        }

        return examExaminationMapper.deleteExamExaminationByIds(ids);
    }

    /**
     * 删除考试发布信息
     * 
     * @param id 考试发布主键
     * @return 结果
     */
    @Override
    @Transactional
    public int deleteExamExaminationById(Long id) {
        // 删除前检查考试状态
        ExamExamination exam = examExaminationMapper.selectExamExaminationById(id);
        if (exam != null && !EXAM_DRAFT.equals(exam.getStatus())) {
            throw new ServiceException("只有草稿状态的考试允许删除，请先取消或归档后处理");
        }

        // 删除相关的考试用户记录
        examUserMapper.deleteExamUserByExamId(id);
        examExaminationMapper.deleteExamDeptByExamId(id);

        return examExaminationMapper.deleteExamExaminationById(id);
    }

    /**
     * 开始考试
     * 
     * @param id 考试ID
     * @return 结果
     */
    @Override
    @Transactional
    public int startExamination(Long id) {
        ExamExamination exam = examExaminationMapper.selectExamExaminationById(id);
        if (exam == null) {
            throw new ServiceException("考试不存在");
        }

        // 验证当前状态
        if (!EXAM_PUBLISHED.equals(exam.getStatus())) {
            throw new ServiceException("只有已发布的考试才能开始");
        }

        // 验证开始时间
        Date now = new Date();
        if (exam.getStartTime() != null && now.before(exam.getStartTime())) {
            throw new ServiceException("考试尚未到开始时间，不能手动开始");
        }

        return examExaminationMapper.updateExamStatus(id, EXAM_IN_PROGRESS);
    }

    /**
     * 结束考试
     * 
     * @param id 考试ID
     * @return 结果
     */
    @Override
    @Transactional
    public int endExamination(Long id) {
        ExamExamination exam = examExaminationMapper.selectExamExaminationById(id);
        if (exam == null) {
            throw new ServiceException("考试不存在");
        }

        // 验证当前状态
        if (!EXAM_IN_PROGRESS.equals(exam.getStatus())) {
            throw new ServiceException("只有进行中的考试才能结束");
        }

        // 强制提交所有未提交的答卷
        examUserMapper.forceSubmitUnfinishedExams(id);

        return examExaminationMapper.updateExamStatus(id, EXAM_FINISHED);
    }

    /**
     * 获取考试统计信息
     * 
     * @param examId 考试ID
     * @return 统计信息
     */
    @Override
    public ExamExamination getExamStatistics(Long examId) {
        ExamExamination examination = examExaminationMapper.selectExamExaminationWithDetails(examId);
        if (examination != null) {
            // 获取考试关联的用户详细信息
            List<ExamUser> examUsers = examUserMapper.selectExamUserByExamId(examId);
            examination.setExamUsers(examUsers);

            // 计算统计数据
            int registeredCount = 0; // 参考记录数
            int submittedCount = 0; // 已交卷人数
            int passedCount = 0; // 通过人数

            for (ExamUser examUser : examUsers) {
                registeredCount++; // 所有在列表中的用户都算报名

                // 判断是否已交卷（状态、提交时间或成绩任一存在，都作为已交卷记录）
                if (isSubmittedRecord(examUser)) {
                    submittedCount++;

                    // 判断是否通过（优先使用考试记录中的评分结果，避免前端和列表口径不一致）
                    if (isPassedRecord(examUser, examination.getPassScore())) {
                        passedCount++;
                    }
                }
            }

            // 设置统计数据
            examination.setRegisteredCount(registeredCount);
            examination.setSubmittedCount(submittedCount);
            examination.setPassedCount(passedCount);
        }
        return examination;
    }

    /**
     * 更新考试统计数据
     * 
     * @param examId 考试ID
     */
    @Override
    @Transactional
    public void updateExamStatistics(Long examId) {
        if (examId == null) {
            return;
        }

        // 获取考试关联的用户详细信息
        List<ExamUser> examUsers = examUserMapper.selectExamUserByExamId(examId);

        // 计算统计数据
        int registeredCount = 0; // 参考记录数
        int submittedCount = 0; // 已交卷人数
        int passedCount = 0; // 通过人数
        BigDecimal totalScoreSum = BigDecimal.ZERO; // 总分数和
        int gradedCount = 0; // 已评分人数（用于计算平均分）

        for (ExamUser examUser : examUsers) {
            registeredCount++; // 所有在列表中的用户都算报名

            // 判断是否已交卷（状态、提交时间或成绩任一存在，都作为已交卷记录）
            if (isSubmittedRecord(examUser)) {
                submittedCount++;

                // 如果有分数，计算平均分和通过情况
                if (examUser.getTotalScore() != null) {
                    gradedCount++;
                    totalScoreSum = totalScoreSum.add(examUser.getTotalScore());

                    // 判断是否通过
                    if (isPassedRecord(examUser, examUser.getPassScore())) {
                        passedCount++;
                    }
                }
            }
        }

        // 计算平均分
        BigDecimal averageScore = BigDecimal.ZERO;
        if (gradedCount > 0) {
            averageScore = totalScoreSum.divide(BigDecimal.valueOf(gradedCount), 2, RoundingMode.HALF_UP);
        }

        // 更新考试表中的统计数据
        ExamExamination updateExam = new ExamExamination();
        updateExam.setId(examId);
        updateExam.setRegisteredCount(registeredCount);
        updateExam.setSubmittedCount(submittedCount);
        updateExam.setPassedCount(passedCount);
        // 注意：数据库表中没有average_score字段，暂时不更新平均分
        // updateExam.setAverageScore(averageScore);
        updateExam.setUpdateBy(SecurityUtils.getUsername());
        updateExam.setUpdateTime(DateUtils.getNowDate());

        examExaminationMapper.updateExamExamination(updateExam);
    }

    /**
     * 发布考试成绩
     * 
     * @param examId 考试ID
     * @return 结果
     */
    @Override
    @Transactional
    public int publishExamResults(Long examId) {
        return publishResults(examId);
    }

    private Set<Long> findProtectedUserIds(List<ExamUser> existingUsers, Set<Long> targetUserIds) {
        Set<Long> protectedUserIds = new HashSet<>();
        if (existingUsers == null || targetUserIds == null || targetUserIds.isEmpty()) {
            return protectedUserIds;
        }
        for (ExamUser examUser : existingUsers) {
            if (examUser == null || examUser.getUserId() == null || !targetUserIds.contains(examUser.getUserId())) {
                continue;
            }
            if (isProtectedExamUserRecord(examUser)) {
                protectedUserIds.add(examUser.getUserId());
            }
        }
        return protectedUserIds;
    }

    private boolean isProtectedExamUserRecord(ExamUser examUser) {
        return USER_EXAM_IN_PROGRESS.equals(examUser.getExamStatus())
                || USER_EXAM_SUBMITTED.equals(examUser.getExamStatus())
                || USER_EXAM_GRADED.equals(examUser.getExamStatus())
                || examUser.getStartTime() != null
                || examUser.getSubmitTime() != null;
    }

    private String buildUserNames(Set<Long> userIds) {
        return userIds.stream()
                .map(userId -> {
                    SysUser user = userService.selectUserById(userId);
                    return user != null ? user.getUserName() : "ID:" + userId;
                })
                .collect(Collectors.joining("、"));
    }

    /**
     * 人工阅卷
     * 
     * @param examId 考试ID
     * @param userId 用户ID
     * @return 结果
     */
    @Override
    @Transactional
    public int manualGrading(Long examId, Long userId) {
        throw new ServiceException("人工阅卷功能暂未启用，请使用自动评分或专项阅卷接口处理");
    }

    /**
     * 批量添加考试人员（按部门）
     * 
     * @param examId  考试ID
     * @param deptIds 部门ID数组
     * @return 结果
     */
    @Override
    @Transactional
    public int addExamUsersByDept(Long examId, Long[] deptIds) {
        if (deptIds == null || deptIds.length == 0) {
            throw new ServiceException("请选择要添加的部门");
        }

        ExamExamination exam = examExaminationMapper.selectExamExaminationById(examId);
        if (exam == null) {
            throw new ServiceException("考试不存在");
        }

        // 获取部门下的所有用户
        List<Long> userIds = new ArrayList<>();
        for (Long deptId : deptIds) {
            List<Long> deptUserIds = examUserMapper.selectUserIdsByDeptId(deptId);
            userIds.addAll(deptUserIds);
        }

        // 去重
        userIds = userIds.stream().distinct().collect(Collectors.toList());

        // 批量添加用户
        return addExamUsers(examId, userIds.toArray(new Long[0]));
    }

    /**
     * 批量添加考试人员（按部门，包含子部门）
     * 
     * @param examId  考试ID
     * @param deptIds 部门ID数组
     * @return 结果
     */
    @Override
    @Transactional
    public int addExamUsersByDeptWithChildren(Long examId, Long[] deptIds) {
        if (deptIds == null || deptIds.length == 0) {
            throw new ServiceException("请选择要添加的部门");
        }

        ExamExamination exam = examExaminationMapper.selectExamExaminationById(examId);
        if (exam == null) {
            throw new ServiceException("考试不存在");
        }

        // 获取部门及其子部门下的所有用户
        List<Long> userIds = new ArrayList<>();
        for (Long deptId : deptIds) {
            List<Long> allDeptIds = examUserMapper.selectAllSubDeptIds(deptId);
            for (Long subDeptId : allDeptIds) {
                List<Long> deptUserIds = examUserMapper.selectUserIdsByDeptId(subDeptId);
                userIds.addAll(deptUserIds);
            }
        }

        // 去重
        userIds = userIds.stream().distinct().collect(Collectors.toList());

        // 批量添加用户
        return addExamUsers(examId, userIds.toArray(new Long[0]));
    }

    /**
     * 审核用户报名
     * 
     * @param examUserId 考试用户ID
     * @param status     审核状态
     * @return 结果
     */
    @Override
    @Transactional
    public int approveUserRegistration(Long examUserId, String status) {
        return auditExamUser(examUserId, status);
    }

    /**
     * 批量审核用户报名
     * 
     * @param examUserIds 考试用户ID数组
     * @param status      审核状态
     * @return 结果
     */
    @Override
    @Transactional
    public int batchApproveUserRegistration(Long[] examUserIds, String status) {
        int successCount = 0;
        for (Long examUserId : examUserIds) {
            if (auditExamUser(examUserId, status) > 0) {
                successCount++;
            }
        }
        return successCount;
    }

    /**
     * 导出考试人员
     * 
     * @param examId 考试ID
     * @return 导出数据
     */
    @Override
    public List<ExamUser> exportExamUsers(Long examId) {
        return examUserMapper.selectExamUserListByExamId(examId);
    }

    /**
     * 批量导入考试用户
     * 
     * @param examId        考试ID
     * @param examUserList  考试用户列表
     * @param updateSupport 是否更新支持
     * @param operName      操作用户
     * @return 结果消息
     */
    @Override
    @Transactional
    public String importExamUsers(Long examId, List<ExamUser> examUserList, Boolean updateSupport, String operName) {
        if (StringUtils.isNull(examUserList) || examUserList.size() == 0) {
            throw new ServiceException("导入考试用户数据不能为空！");
        }

        ExamExamination exam = examExaminationMapper.selectExamExaminationById(examId);
        if (exam == null) {
            throw new ServiceException("考试不存在");
        }

        int successNum = 0;
        int failureNum = 0;
        StringBuilder successMsg = new StringBuilder();
        StringBuilder failureMsg = new StringBuilder();

        for (ExamUser examUser : examUserList) {
            try {
                // 验证用户是否存在
                if (StringUtils.isEmpty(examUser.getUserName())) {
                    failureNum++;
                    failureMsg.append("<br/>第" + (successNum + failureNum) + "行：用户名不能为空");
                    continue;
                }

                // 根据用户名查询用户信息
                Long userId = examUserMapper.selectUserIdByUserName(examUser.getUserName());
                if (userId == null) {
                    failureNum++;
                    failureMsg.append("<br/>第" + (successNum + failureNum) + "行：用户 " + examUser.getUserName() + " 不存在");
                    continue;
                }

                // 获取用户完整信息包括部门ID
                SysUser user = userService.selectUserById(userId);
                if (user == null) {
                    failureNum++;
                    failureMsg.append(
                            "<br/>第" + (successNum + failureNum) + "行：用户 " + examUser.getUserName() + " 信息获取失败");
                    continue;
                }

                examUser.setExamId(examId);
                examUser.setUserId(userId);
                examUser.setDeptId(user.getDeptId()); // 设置用户的部门ID

                // 验证是否已存在
                int existCount = examUserMapper.checkUserRegistered(examId, userId);
                if (existCount > 0) {
                    if (updateSupport) {
                        // 仅刷新人员的基础归属信息，不允许导入覆盖历史考试状态和分数。
                        ExamUser existingUser = examUserMapper.selectExamUserByExamIdAndUserId(examId, userId);
                        if (existingUser != null) {
                            ExamUser updateUser = new ExamUser();
                            updateUser.setId(existingUser.getId());
                            updateUser.setDeptId(user.getDeptId());
                            updateUser.setUpdateBy(operName);
                            examUserMapper.updateExamUser(updateUser);
                            successNum++;
                            successMsg.append(
                                    "<br/>第" + (successNum + failureNum) + "行：用户 " + examUser.getUserName() + " 已存在，已刷新部门信息");
                        }
                    } else {
                        failureNum++;
                        failureMsg.append(
                                "<br/>第" + (successNum + failureNum) + "行：用户 " + examUser.getUserName() + " 已存在");
                    }
                } else {
                    // 新增记录
                    examUser.setRegistrationTime(new Date());
                    examUser.setRegistrationStatus(REGISTRATION_APPROVED);
                    examUser.setExamStatus(USER_EXAM_NOT_STARTED);
                    examUser.setAttemptCount(0);
                    examUser.setAttemptNumber(1); // 设置默认尝试次数为1
                    examUser.setCreateBy(operName);
                    try {
                        examUserMapper.insertExamUser(examUser);
                        successNum++;
                        successMsg.append(
                                "<br/>第" + (successNum + failureNum) + "行：用户 " + examUser.getUserName() + " 导入成功");
                    } catch (Exception insertException) {
                        // 如果插入失败（可能是约束冲突），记录为失败
                        failureNum++;
                        failureMsg.append("<br/>第" + (successNum + failureNum) + "行：用户 " + examUser.getUserName()
                                + " 导入失败，可能已存在");
                    }
                }
            } catch (Exception e) {
                failureNum++;
                String msg = "<br/>第" + (successNum + failureNum) + "行：导入失败：";
                failureMsg.append(msg + e.getMessage());
            }
        }

        if (failureNum > 0) {
            failureMsg.insert(0, "很抱歉，导入失败！共 " + failureNum + " 条数据格式不正确，错误如下：");
            throw new ServiceException(failureMsg.toString());
        } else {
            successMsg.insert(0, "恭喜您，数据已全部导入成功！共 " + successNum + " 条，数据如下：");
        }

        return successMsg.toString();
    }

    /**
     * 验证考试进行中的修改限制
     * 
     * @param newExam      新的考试信息
     * @param existingExam 现有的考试信息
     */
    private void validateExamInProgressUpdate(ExamExamination newExam, ExamExamination existingExam) {
        // 禁止修改核心业务字段
        if (!Objects.equals(newExam.getPaperId(), existingExam.getPaperId())) {
            throw new ServiceException("考试进行中不允许更换试卷");
        }

        if (!Objects.equals(newExam.getDuration(), existingExam.getDuration())) {
            throw new ServiceException("考试进行中不允许修改考试时长");
        }

        if (newExam.getPassScore() != null && existingExam.getPassScore() != null
                && newExam.getPassScore().compareTo(existingExam.getPassScore()) != 0) {
            throw new ServiceException("考试进行中不允许修改及格分数");
        }

        if (!Objects.equals(newExam.getMaxAttempts(), existingExam.getMaxAttempts())) {
            validateInProgressMaxAttempts(newExam);
        }

        if (!Objects.equals(newExam.getExamType(), existingExam.getExamType())) {
            throw new ServiceException("考试进行中不允许修改考试类型");
        }

        if (!Objects.equals(newExam.getExamMode(), existingExam.getExamMode())) {
            throw new ServiceException("考试进行中不允许修改考试模式");
        }

        if (!Objects.equals(newExam.getAutoGrade(), existingExam.getAutoGrade())) {
            throw new ServiceException("考试进行中不允许修改自动评分设置");
        }

        // 时间相关验证
        if (newExam.getStartTime() != null) {
            Date now = new Date();
            // 如果考试已经开始，不能将开始时间设置为未来
            if (newExam.getStartTime().after(now)) {
                throw new ServiceException("考试已开始，不能将开始时间设置为未来时间");
            }

            // 新的结束时间必须在开始时间之后
            if (newExam.getEndTime() != null && !newExam.getEndTime().after(newExam.getStartTime())) {
                throw new ServiceException("结束时间必须晚于开始时间");
            }
        }

        // 检查是否有人正在考试，如果有则更严格限制某些字段
        int inProgressCount = examUserMapper.countInProgressUsers(existingExam.getId());
        if (inProgressCount > 0) {
            // 有人正在考试时，不允许修改影响答题体验的设置
            if (!Objects.equals(newExam.getShuffleQuestions(), existingExam.getShuffleQuestions())) {
                throw new ServiceException("有考生正在答题，不允许修改题目顺序设置");
            }

            if (!Objects.equals(newExam.getShuffleOptions(), existingExam.getShuffleOptions())) {
                throw new ServiceException("有考生正在答题，不允许修改选项顺序设置");
            }
        }
    }

    /**
     * 进行中考试允许调整最大考试次数，但不能小于已经生成的最大考试轮次。
     */
    private void validateInProgressMaxAttempts(ExamExamination newExam) {
        if (newExam.getMaxAttempts() == null || newExam.getMaxAttempts() <= 0) {
            throw new ServiceException("最大考试次数必须大于0");
        }

        int usedMaxAttempt = examUserMapper.selectMaxAttemptNumberByExamId(newExam.getId());
        if (newExam.getMaxAttempts() < usedMaxAttempt) {
            throw new ServiceException("最大考试次数不能小于当前已存在的最大考试轮次：" + usedMaxAttempt);
        }
    }

    /**
     * 验证考试发布前的信息完整性
     */
    private void validateExamForPublish(ExamExamination exam) {
        if (StringUtils.isEmpty(exam.getExamName())) {
            throw new ServiceException("考试名称不能为空");
        }

        if (exam.getCategoryId() == null) {
            throw new ServiceException("请选择考试分类");
        }

        if (exam.getPaperId() == null) {
            throw new ServiceException("请选择试卷");
        }

        if (exam.getStartTime() == null || exam.getEndTime() == null) {
            throw new ServiceException("请设置考试开始和结束时间");
        }

        if (exam.getStartTime().after(exam.getEndTime())) {
            throw new ServiceException("考试开始时间不能晚于结束时间");
        }

        if (exam.getDuration() == null || exam.getDuration() <= 0) {
            throw new ServiceException("考试时长必须大于0");
        }

        if (exam.getPassScore() == null || exam.getPassScore().compareTo(BigDecimal.ZERO) < 0) {
            throw new ServiceException("及格分数不能为负数");
        }

        if (exam.getMaxAttempts() == null || exam.getMaxAttempts() <= 0) {
            throw new ServiceException("最大考试次数必须大于0");
        }

        ExamPaper paper = examPaperService.selectPaperById(exam.getPaperId());
        if (paper == null) {
            throw new ServiceException("关联试卷不存在");
        }
        if (!"0".equals(paper.getStatus())) {
            throw new ServiceException("关联试卷已停用，不能发布考试");
        }

        List<ExamPaperQuestion> paperQuestions = examPaperQuestionService.selectQuestionsWithDetailsByPaperId(exam.getPaperId());
        if (paperQuestions == null || paperQuestions.isEmpty()) {
            throw new ServiceException("关联试卷没有题目，不能发布考试");
        }
        if (paper.getQuestionCount() == null || paper.getQuestionCount() <= 0) {
            throw new ServiceException("关联试卷题量未配置，不能发布考试");
        }
        if (!paper.getQuestionCount().equals(paperQuestions.size())) {
            throw new ServiceException("关联试卷题量与实际题目数量不一致，请先同步试卷题量");
        }

        BigDecimal paperTotalScore = BigDecimal.ZERO;
        for (ExamPaperQuestion question : paperQuestions) {
            if (question.getScore() == null || question.getScore().compareTo(BigDecimal.ZERO) <= 0) {
                throw new ServiceException("关联试卷存在未配置分值的题目，不能发布考试");
            }
            paperTotalScore = paperTotalScore.add(question.getScore());
        }

        if (paperTotalScore.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ServiceException("关联试卷总分必须大于0");
        }
        if (paper.getTotalScore() == null || paper.getTotalScore().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ServiceException("关联试卷总分未配置，不能发布考试");
        }
        if (paper.getTotalScore().compareTo(paperTotalScore) != 0) {
            throw new ServiceException("关联试卷总分与题目分值合计不一致，请先同步试卷总分");
        }
        if (exam.getTotalScore() != null && exam.getTotalScore().compareTo(BigDecimal.ZERO) > 0
                && exam.getTotalScore().compareTo(paperTotalScore) != 0) {
            throw new ServiceException("考试总分与试卷题目分值合计不一致，请先同步试卷总分");
        }
        if (exam.getPassScore().compareTo(paperTotalScore) > 0) {
            throw new ServiceException("及格分数不能大于试卷总分");
        }
    }

    /**
     * 重置单次考试记录（删除该记录答题数据，然后保留同一 attempt_number 重建未开始记录）
     *
     * @param examUserId 考试用户关联ID
     * @return 结果（重置的记录数）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int resetUserExam(Long examUserId) {
        if (examUserId == null) {
            throw new ServiceException("考试记录ID不能为空");
        }

        ExamUser examUser = examUserMapper.selectExamUserById(examUserId);
        if (examUser == null) {
            throw new ServiceException("考试记录不存在");
        }

        if (USER_EXAM_IN_PROGRESS.equals(examUser.getExamStatus())) {
            throw new ServiceException("考生正在考试中，不允许重置");
        }

        ExamExamination exam = examExaminationMapper.selectExamExaminationById(examUser.getExamId());
        if (exam == null) {
            throw new ServiceException("考试不存在");
        }

        int deletedAnswerRecords = examAnswerRecordService.deleteAnswerRecordsByExamUserId(examUserId);
        int deletedExamUsers = examUserMapper.deleteExamUserById(examUserId);
        if (deletedExamUsers == 0) {
            throw new ServiceException("删除考试记录失败");
        }

        SysUser user = userService.selectUserById(examUser.getUserId());
        if (user == null) {
            throw new ServiceException("用户不存在");
        }

        ExamUser newExamUser = new ExamUser();
        newExamUser.setExamId(examUser.getExamId());
        newExamUser.setUserId(examUser.getUserId());
        newExamUser.setDeptId(user.getDeptId());
        newExamUser.setRegistrationTime(new Date());
        newExamUser.setRegistrationStatus(REGISTRATION_APPROVED);
        newExamUser.setExamStatus(USER_EXAM_NOT_STARTED);
        newExamUser.setAttemptCount(0);
        newExamUser.setAttemptNumber(examUser.getAttemptNumber() == null ? 1 : examUser.getAttemptNumber());

        int addedUsers = examUserMapper.insertExamUser(newExamUser);
        if (addedUsers == 0) {
            throw new ServiceException("重新添加考试记录失败");
        }

        updateExamStatistics(examUser.getExamId());

        return deletedAnswerRecords + deletedExamUsers + addedUsers;
    }

    /**
     * 统计已发布的考试数量
     * 
     * @return 考试数量
     */
    @Override
    public int countPublishedExaminations() {
        return examExaminationMapper.countPublishedExaminations();
    }

    /**
     * 导出成绩统计
     * 
     * @param examId   考试ID
     * @param examUser 查询条件
     * @return 成绩统计数据
     */
    @Override
    public List<ExamUser> exportScoreStatistics(Long examId, ExamUser examUser) {
        if (examUser == null) {
            examUser = new ExamUser();
        }
        examUser.setExamId(examId);

        // 获取考试用户列表（包含成绩信息）
        List<ExamUser> examUsers = examUserMapper.selectExamUserList(examUser);

        // 为每个用户设置部门信息和用户详细信息
        for (ExamUser user : examUsers) {
            if (user.getUserId() != null) {
                SysUser sysUser = userService.selectUserById(user.getUserId());
                if (sysUser != null) {
                    user.setUserName(sysUser.getUserName());
                    user.setNickName(sysUser.getNickName());
                    user.setDeptName(sysUser.getDept() != null ? sysUser.getDept().getDeptName() : "");
                    user.setPhonenumber(sysUser.getPhonenumber());
                    user.setEmail(sysUser.getEmail());
                }
            }
        }

        return examUsers;
    }

    /**
     * 导出详细成绩
     * 
     * @param examId   考试ID
     * @param examUser 查询条件
     * @return 详细成绩数据
     */
    @Override
    public List<ExamUser> exportScoreDetails(Long examId, ExamUser examUser) {
        if (examUser == null) {
            examUser = new ExamUser();
        }
        examUser.setExamId(examId);

        // 获取考试用户列表（包含成绩信息）
        List<ExamUser> examUsers = examUserMapper.selectExamUserList(examUser);

        // 为每个用户设置详细信息和答题记录
        for (ExamUser user : examUsers) {
            if (user.getUserId() != null) {
                SysUser sysUser = userService.selectUserById(user.getUserId());
                if (sysUser != null) {
                    user.setUserName(sysUser.getUserName());
                    user.setNickName(sysUser.getNickName());
                    user.setDeptName(sysUser.getDept() != null ? sysUser.getDept().getDeptName() : "");
                    user.setPhonenumber(sysUser.getPhonenumber());
                    user.setEmail(sysUser.getEmail());
                }

                // 获取详细的答题记录（如果需要包含答案详情）
                if (examUser.getIncludeAnswerDetails() != null && examUser.getIncludeAnswerDetails()) {
                    List<ExamAnswerRecord> answerRecords = examAnswerRecordService
                            .selectAnswerRecordsWithQuestionsByExamUserId(user.getId());

                    // 为每个答题记录填充题目内容和类型信息（用于Excel导出）
                    for (ExamAnswerRecord record : answerRecords) {
                        if (record.getQuestion() != null) {
                            record.setQuestionContent(record.getQuestion().getQuestionContent());
                            record.setQuestionType(record.getQuestion().getQuestionType());
                        }
                    }

                    user.setAnswerRecords(answerRecords);
                }
            }
        }

        return examUsers;
    }

    /**
     * 导出详细成绩（扁平化结构）
     */
    @Override
    public List<com.ruoyi.exam.examination.domain.ExamScoreDetailExportDTO> exportScoreDetailsFlat(Long examId,
            ExamUser examUser) {
        if (examUser == null) {
            examUser = new ExamUser();
        }
        examUser.setExamId(examId);
        examUser.setIncludeAnswerDetails(true); // 强制包含答题详情

        // 获取考试用户列表（包含成绩信息和答题记录）
        List<ExamUser> examUsers = this.exportScoreDetails(examId, examUser);

        // 转换为扁平化结构
        List<com.ruoyi.exam.examination.domain.ExamScoreDetailExportDTO> result = new java.util.ArrayList<>();

        for (ExamUser user : examUsers) {
            if (user.getAnswerRecords() != null && !user.getAnswerRecords().isEmpty()) {
                // 为每个答题记录创建一行导出数据
                int questionNumber = 1;
                for (ExamAnswerRecord record : user.getAnswerRecords()) {
                    com.ruoyi.exam.examination.domain.ExamScoreDetailExportDTO dto = new com.ruoyi.exam.examination.domain.ExamScoreDetailExportDTO();

                    // 设置用户信息
                    dto.setUserName(user.getUserName());
                    dto.setNickName(user.getNickName());
                    dto.setDeptName(user.getDeptName());
                    dto.setTotalScore(user.getTotalScore());
                    dto.setExamStatus(user.getExamStatus());
                    dto.setSubmitTime(user.getSubmitTime());

                    // 设置题目信息
                    dto.setQuestionNumber(questionNumber++);
                    if (record.getQuestion() != null) {
                        dto.setQuestionType(record.getQuestion().getQuestionType());
                        dto.setQuestionContent(record.getQuestion().getQuestionContent());
                    }
                    dto.setCorrectAnswer(record.getCorrectAnswer());
                    dto.setUserAnswer(record.getUserAnswer());
                    dto.setQuestionScore(record.getQuestionScore());
                    dto.setUserScore(record.getUserScore());
                    dto.setIsCorrect(record.getIsCorrect());

                    result.add(dto);
                }
            } else {
                // 如果没有答题记录，只显示用户基本信息
                com.ruoyi.exam.examination.domain.ExamScoreDetailExportDTO dto = new com.ruoyi.exam.examination.domain.ExamScoreDetailExportDTO();

                dto.setUserName(user.getUserName());
                dto.setNickName(user.getNickName());
                dto.setDeptName(user.getDeptName());
                dto.setTotalScore(user.getTotalScore());
                dto.setExamStatus(user.getExamStatus());
                dto.setSubmitTime(user.getSubmitTime());

                result.add(dto);
            }
        }

        return result;
    }

    /**
     * 统计业务概览数据（今日、本周、本月考试数量）
     * 
     * @return 统计结果
     */
    @Override
    public java.util.Map<String, Object> selectBusinessStatistics() {
        return examExaminationMapper.selectBusinessStatistics();
    }

    private boolean isSubmittedRecord(ExamUser examUser) {
        return USER_EXAM_SUBMITTED.equals(examUser.getExamStatus())
                || USER_EXAM_GRADED.equals(examUser.getExamStatus())
                || examUser.getSubmitTime() != null;
    }

    private boolean isPassedRecord(ExamUser examUser, BigDecimal passScore) {
        return "1".equals(examUser.getIsPassed())
                || (examUser.getIsPassed() == null
                && examUser.getTotalScore() != null
                && passScore != null
                && examUser.getTotalScore().compareTo(passScore) >= 0);
    }
}
