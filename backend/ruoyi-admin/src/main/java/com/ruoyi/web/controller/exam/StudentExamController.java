package com.ruoyi.web.controller.exam;

import static com.ruoyi.exam.constant.ExamStatusConstants.REGISTRATION_REGISTERED;
import static com.ruoyi.exam.constant.ExamStatusConstants.USER_EXAM_GRADED;
import static com.ruoyi.exam.constant.ExamStatusConstants.USER_EXAM_IN_PROGRESS;
import static com.ruoyi.exam.constant.ExamStatusConstants.USER_EXAM_NOT_STARTED;
import static com.ruoyi.exam.constant.ExamStatusConstants.USER_EXAM_SUBMITTED;
import static com.ruoyi.exam.constant.ExamStatusConstants.normalizeExaminationStatusForQuery;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.exam.examination.domain.ExamExamination;
import com.ruoyi.exam.examination.domain.ExamUser;
import com.ruoyi.exam.examination.domain.ExamAnswerRecord;
import com.ruoyi.exam.examination.service.IExamExaminationService;
import com.ruoyi.exam.examination.service.IExamUserService;
import com.ruoyi.exam.examination.service.IExamAnswerRecordService;
import com.ruoyi.exam.paper.domain.ExamPaper;
import com.ruoyi.exam.paper.domain.ExamPaperQuestion;
import com.ruoyi.exam.paper.service.IExamPaperService;
import com.ruoyi.exam.paper.service.IExamPaperQuestionService;
import com.ruoyi.exam.question.domain.ExamQuestion;
import com.ruoyi.exam.question.service.IExamQuestionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 学员端考试Controller
 * 
 * @author ruoyi
 */
@RestController
@RequestMapping("/student/exam")
public class StudentExamController extends BaseController
{
    private static final Logger logger = LoggerFactory.getLogger(StudentExamController.class);

    @Autowired
    private IExamExaminationService examExaminationService;
    
    @Autowired
    private IExamUserService examUserService;
    
    @Autowired
    private IExamAnswerRecordService examAnswerRecordService;
    
    @Autowired
    private IExamPaperService examPaperService;
    
    @Autowired
    private IExamPaperQuestionService examPaperQuestionService;
    
    @Autowired
    private IExamQuestionService examQuestionService;

    /**
     * 查询学员可参加的考试列表
     */
    @PreAuthorize("@ss.hasPermi('student:exam:list')")
    @GetMapping("/available")
    public TableDataInfo getAvailableExaminations()
    {
        Long userId = SecurityUtils.getUserId();
        startPage();
        List<ExamExamination> list = examExaminationService.selectAvailableExaminations(userId);
        
        // 为每个考试查询用户报名信息并设置到examUsers中
        for (ExamExamination exam : list) {
            ExamUser examUser = examUserService.selectExamUserByExamIdAndUserId(exam.getId(), userId);
            if (examUser != null) {
                // 修正：设置正确的考试次数（只统计已开始的考试）
                int actualAttempts = examUserService.countUserAttempts(exam.getId(), userId);
                examUser.setAttemptCount(actualAttempts);
                
                List<ExamUser> examUsers = new ArrayList<>();
                examUsers.add(examUser);
                exam.setExamUsers(examUsers);
            }
        }
        
        return getDataTable(list);
    }

    /**
     * 查询学员的所有考试列表（包括未开始、进行中、已结束）
     */
    @PreAuthorize("@ss.hasPermi('student:exam:list')")
    @GetMapping("/list")
    public TableDataInfo getAllExaminations(ExamExamination examination)
    {
        Long userId = SecurityUtils.getUserId();
        
        // 处理前端传递的status查询条件
        if (StringUtils.isNotEmpty(examination.getStatus())) {
            examination.setStatus(normalizeExaminationStatusForQuery(examination.getStatus()));
        }
        
        // 设置过滤条件：排除草稿状态
        examination.setParams(new HashMap<>());
        examination.getParams().put("excludeDraft", "true");
        examination.getParams().put("userId", userId);
        
        startPage();
        
        // 查询考试列表（在SQL层面进行过滤和排序）
        List<ExamExamination> allExams = examExaminationService.selectStudentExamList(examination);
        
        // 为每个考试查询用户报名信息并设置到examUsers中
        for (ExamExamination exam : allExams) {
            ExamUser examUser = examUserService.selectExamUserByExamIdAndUserId(exam.getId(), userId);
            if (examUser != null) {
                // 修正：设置正确的考试次数（只统计已开始的考试）
                int actualAttempts = examUserService.countUserAttempts(exam.getId(), userId);
                examUser.setAttemptCount(actualAttempts);
                
                List<ExamUser> examUsers = new ArrayList<>();
                examUsers.add(examUser);
                exam.setExamUsers(examUsers);
            }
        }
        
        return getDataTable(allExams);
    }

    /**
     * 查询学员的考试记录
     */
    @PreAuthorize("@ss.hasPermi('student:exam:records')")
    @GetMapping("/records")
    public TableDataInfo getExamRecords()
    {
        Long userId = SecurityUtils.getUserId();
        logger.info("查询用户{}的考试记录", userId);
        
        startPage();
        List<ExamUser> list = examUserService.selectExamUserListByUserId(userId);
        
        // 数据验证和日志记录
        if (list != null && !list.isEmpty()) {
            logger.info("查询到{}条考试记录", list.size());
            
            // 检查并记录数据异常
            long invalidRecords = list.stream()
                .filter(record -> record.getId() == null)
                .count();
                
            if (invalidRecords > 0) {
                logger.warn("发现{}条考试记录缺少ID字段", invalidRecords);
                
                // 过滤掉无效记录
                list = list.stream()
                    .filter(record -> record.getId() != null)
                    .collect(java.util.stream.Collectors.toList());
                    
                logger.info("过滤后剩余{}条有效考试记录", list.size());
            }
            
            // 记录第一条记录的详细信息用于调试
            if (!list.isEmpty()) {
                ExamUser firstRecord = list.get(0);
                logger.debug("第一条考试记录详情: ID={}, ExamId={}, UserId={}, ExamStatus={}", 
                    firstRecord.getId(), firstRecord.getExamId(), firstRecord.getUserId(), firstRecord.getExamStatus());
            }
        } else {
            logger.info("用户{}没有考试记录", userId);
        }
        
        return getDataTable(list);
    }

    /**
     * 获取考试详情
     */
    @PreAuthorize("@ss.hasAnyPermi('student:exam:list,student:exam:detail')")
    @GetMapping("/{examId}")
    public AjaxResult getExamDetail(@PathVariable Long examId)
    {
        Long userId = SecurityUtils.getUserId();
        
        // 获取考试信息
        ExamExamination examination = examExaminationService.selectExamExaminationById(examId);
        if (examination == null)
        {
            return error("考试不存在");
        }
        if (!examExaminationService.canUserAccessExam(examId, userId, SecurityUtils.getDeptId()))
        {
            return error("您不在本次考试授权范围内");
        }
        
        // 检查用户是否有权限参加此考试
        ExamUser examUser = examUserService.selectExamUserByExamIdAndUserId(examId, userId);
        if (examUser != null) {
            // 修正：设置正确的考试次数（只统计已开始的考试）
            int actualAttempts = examUserService.countUserAttempts(examId, userId);
            examUser.setAttemptCount(actualAttempts);
        }
        
        // 获取试卷详细信息
        ExamPaper paper = null;
        List<ExamPaperQuestion> paperQuestions = new ArrayList<>();
        Map<String, Object> paperStatistics = new HashMap<>();
        
        if (examination.getPaperId() != null) {
            paper = examPaperService.selectPaperById(examination.getPaperId());
            if (paper != null) {
                // 获取试卷题目列表
                paperQuestions = examPaperQuestionService.selectQuestionsWithDetailsByPaperId(paper.getId());
                
                // 统计题型分布
                paperStatistics = calculatePaperStatistics(paperQuestions);
            }
        }
        
        // 获取考试历史记录（如果允许多次考试）
        List<ExamUser> historyRecords = new ArrayList<>();
        if (examination.getMaxAttempts() > 1 && examUser != null) {
            // 查询该用户的所有考试记录
            historyRecords = examUserService.selectExamUserListByUserId(userId);
            // 只保留当前考试的记录
            historyRecords = historyRecords.stream()
                .filter(record -> record.getExamId().equals(examId))
                .collect(Collectors.toList());
        }
        
        // 获取考试统计信息（报名人数等）
        ExamExamination examStatistics = examExaminationService.getExamStatistics(examId);
        
        AjaxResult ajax = AjaxResult.success();
        ajax.put("examination", examination);
        ajax.put("examUser", examUser);
        ajax.put("paper", paper);
        ajax.put("paperQuestions", paperQuestions);
        ajax.put("paperStatistics", paperStatistics);
        ajax.put("historyRecords", historyRecords);
        ajax.put("examStatistics", examStatistics);
        return ajax;
    }
    
    /**
     * 计算试卷统计信息
     */
    private Map<String, Object> calculatePaperStatistics(List<ExamPaperQuestion> paperQuestions) {
        Map<String, Object> statistics = new HashMap<>();
        Map<String, Integer> typeCount = new HashMap<>();
        Map<String, BigDecimal> typeScore = new HashMap<>();
        
        BigDecimal totalScore = BigDecimal.ZERO;
        int totalCount = paperQuestions.size();
        
        for (ExamPaperQuestion pq : paperQuestions) {
            if (pq.getQuestion() != null) {
                String questionType = pq.getQuestion().getQuestionType();
                String typeName = getQuestionTypeName(questionType);
                
                // 统计题目数量
                typeCount.put(typeName, typeCount.getOrDefault(typeName, 0) + 1);
                
                // 统计分值
                BigDecimal score = pq.getScore() != null ? pq.getScore() : BigDecimal.ZERO;
                typeScore.put(typeName, typeScore.getOrDefault(typeName, BigDecimal.ZERO).add(score));
                totalScore = totalScore.add(score);
            }
        }
        
        statistics.put("totalCount", totalCount);
        statistics.put("totalScore", totalScore);
        statistics.put("typeCount", typeCount);
        statistics.put("typeScore", typeScore);
        
        return statistics;
    }
    
    /**
     * 获取题型中文名称
     */
    private String getQuestionTypeName(String questionType) {
        switch (questionType) {
            case "single_choice": return "单选题";
            case "multiple_choice": return "多选题";
            case "true_false": return "判断题";
            case "fill_blank": return "填空题";
            case "essay": return "简答题";
            default: return "其他";
        }
    }

    /**
     * 获取学员端统计数据
     */
    @PreAuthorize("@ss.hasPermi('student:exam:list')")
    @GetMapping("/statistics")
    public AjaxResult getDashboardStatistics()
    {
        try {
            // 统计考试场数（已发布和进行中的考试）
            int examCount = examExaminationService.countPublishedExaminations();
            
            // 统计试题总数
            int questionCount = examQuestionService.countTotalQuestions();
            
            // 统计用户总数
            int userCount = examUserService.countTotalUsers();
            
            Map<String, Object> statistics = new HashMap<>();
            statistics.put("examCount", examCount);
            statistics.put("questionCount", questionCount);
            statistics.put("userCount", userCount);
            
            return AjaxResult.success(statistics);
        } catch (Exception e) {
            logger.error("获取学员端统计数据失败", e);
            return AjaxResult.error("获取统计数据失败");
        }
    }

    /**
     * 报名参加考试
     */
    @PreAuthorize("@ss.hasPermi('student:exam:register')")
    @Log(title = "考试报名", businessType = BusinessType.INSERT)
    @PostMapping("/register/{examId}")
    public AjaxResult registerExam(@PathVariable Long examId)
    {
        Long userId = SecurityUtils.getUserId();
        
        // 检查考试是否存在
        ExamExamination examination = examExaminationService.selectExamExaminationById(examId);
        if (examination == null)
        {
            return error("考试不存在");
        }
        if (!examExaminationService.canUserAccessExam(examId, userId, SecurityUtils.getDeptId()))
        {
            return error("您不在本次考试授权范围内");
        }
        
        // 检查是否需要报名
        if (!"1".equals(examination.getRegistrationRequired()))
        {
            return error("此考试无需报名");
        }

        // 检查报名时间
        Date now = new Date();
        if (examination.getRegistrationStartTime() != null && now.before(examination.getRegistrationStartTime()))
        {
            return error("报名尚未开始");
        }
        if (examination.getRegistrationEndTime() != null && now.after(examination.getRegistrationEndTime()))
        {
            return error("报名已结束");
        }
        
        // 检查是否已经报名
        ExamUser existingExamUser = examUserService.selectExamUserByExamIdAndUserId(examId, userId);
        if (existingExamUser != null)
        {
            return error("您已报名此考试");
        }
        
        // 创建考试用户关联记录
        ExamUser examUser = new ExamUser();
        examUser.setExamId(examId);
        examUser.setUserId(userId);
        examUser.setDeptId(SecurityUtils.getDeptId());
        examUser.setRegistrationTime(now);
        examUser.setRegistrationStatus(REGISTRATION_REGISTERED);
        examUser.setExamStatus(USER_EXAM_NOT_STARTED);
        examUser.setAttemptCount(0);
        examUser.setAttemptNumber(1); // 设置默认尝试次数为1
        
        try {
            return toAjax(examUserService.insertExamUser(examUser));
        } catch (Exception e) {
            return error("报名失败，您可能已经报名了此考试");
        }
    }

    /**
     * 开始考试
     */
    @PreAuthorize("@ss.hasPermi('student:exam:start')")
    @Log(title = "开始考试", businessType = BusinessType.UPDATE)
    @PostMapping("/start/{examId}")
    public AjaxResult startExam(@PathVariable Long examId)
    {
        ExamUser beforeStart = examUserService.selectExamUserByExamIdAndUserId(examId, SecurityUtils.getUserId());
        ExamUser examUser = examUserService.startStudentExam(examId, SecurityUtils.getUserId(), SecurityUtils.getDeptId());
        AjaxResult result = success();
        result.put("examUserId", examUser.getId());
        result.put("isNewAttempt", beforeStart == null || !examUser.getId().equals(beforeStart.getId()));
        return result;
    }

    /**
     * 获取考试试卷
     */
    @PreAuthorize("@ss.hasPermi('student:exam:start')")
    @GetMapping("/paper/{examId}")
    public AjaxResult getExamPaper(@PathVariable Long examId, Long examUserId)
    {
        Long userId = SecurityUtils.getUserId();
        
        ExamUser examUser = null;
        
        // 如果传递了examUserId，直接使用该ID查询
        if (examUserId != null)
        {
            examUser = examUserService.selectExamUserById(examUserId);
            if (examUser == null || !examUser.getUserId().equals(userId) || !examUser.getExamId().equals(examId))
            {
                return error("无效的考试用户ID");
            }
        }
        else
        {
            // 如果没有传递examUserId，使用原来的查询方式（向后兼容）
            examUser = examUserService.selectExamUserByExamIdAndUserId(examId, userId);
        }
        
        if (examUser == null || !USER_EXAM_IN_PROGRESS.equals(examUser.getExamStatus()))
        {
            return error("您当前不在考试状态");
        }
        
        // 如果开始时间为空，设置为当前时间（兼容性处理）
        if (examUser.getStartTime() == null) {
            examUser.setStartTime(new Date());
            examUserService.updateExamUser(examUser);
        }
        
        // 获取考试信息
        ExamExamination examination = examExaminationService.selectExamExaminationById(examId);
        if (examination == null)
        {
            return error("考试不存在");
        }
        
        // 获取试卷信息
        ExamPaper paper = examPaperService.selectPaperById(examination.getPaperId());
        if (paper == null)
        {
            return error("试卷不存在");
        }
        
        // 获取试题列表（带试题详细信息）
        List<ExamPaperQuestion> questions = examPaperQuestionService.selectQuestionsWithDetailsByPaperId(examination.getPaperId());
        
        // 获取已答题记录（基于当前的examUserId）
        List<ExamAnswerRecord> answers = examAnswerRecordService.selectAnswerRecordsByExamUserId(examUser.getId());
        
        AjaxResult ajax = AjaxResult.success();
        ajax.put("examination", examination);
        ajax.put("paper", paper);
        ajax.put("questions", questions);
        ajax.put("answers", answers);
        ajax.put("examUser", examUser);
        return ajax;
    }

    /**
     * 保存答案（并发安全版本）
     */
    @PreAuthorize("@ss.hasPermi('student:exam:submit')")
    @Log(title = "保存答案", businessType = BusinessType.UPDATE)
    @PostMapping("/answer")
    public AjaxResult saveAnswer(@RequestBody Map<String, Object> params)
    {
        Long userId = SecurityUtils.getUserId();
        Long examId = Long.valueOf(params.get("examId").toString());
        Long questionId = Long.valueOf(params.get("questionId").toString());
        String userAnswer = params.get("userAnswer").toString();
        
        ExamUser examUser = null;
        
        // 如果传递了examUserId，直接使用该ID查询
        if (params.containsKey("examUserId") && params.get("examUserId") != null)
        {
            Long examUserId = Long.valueOf(params.get("examUserId").toString());
            examUser = examUserService.selectExamUserById(examUserId);
            if (examUser == null || !examUser.getUserId().equals(userId) || !examUser.getExamId().equals(examId))
            {
                return error("无效的考试用户ID");
            }
        }
        else
        {
            // 如果没有传递examUserId，使用原来的查询方式（向后兼容）
            examUser = examUserService.selectExamUserByExamIdAndUserId(examId, userId);
        }
        
        if (examUser == null || !USER_EXAM_IN_PROGRESS.equals(examUser.getExamStatus()))
        {
            return error("您当前不在考试状态");
        }
        
        // 获取考试信息获得paperId
        ExamExamination examination = examExaminationService.selectExamExaminationById(examId);
        if (examination == null)
        {
            return error("考试不存在");
        }
        
        // 获取试题信息以获取正确答案
        ExamQuestion question = examQuestionService.selectQuestionById(questionId);
        if (question == null)
        {
            return error("试题不存在");
        }
        
        // 获取试卷中该题目的分值（从exam_paper_question表）
        List<ExamPaperQuestion> paperQuestions = examPaperQuestionService.selectQuestionsWithDetailsByPaperId(examination.getPaperId());
        ExamPaperQuestion paperQuestion = paperQuestions.stream()
            .filter(pq -> pq.getQuestionId().equals(questionId))
            .findFirst()
            .orElse(null);
        
        if (paperQuestion == null)
        {
            return error("该题目不在当前试卷中");
        }
        
        try {
            // 使用并发安全的插入或更新方法
            ExamAnswerRecord answerRecord = new ExamAnswerRecord();
            answerRecord.setExamId(examId);
            answerRecord.setExamUserId(examUser.getId());
            answerRecord.setUserId(userId);
            answerRecord.setPaperId(examination.getPaperId());
            answerRecord.setQuestionId(questionId);
            answerRecord.setUserAnswer(userAnswer);
            answerRecord.setAnswerTime(new Date());
            answerRecord.setQuestionScore(paperQuestion.getScore()); // 使用试卷中配置的分值
            answerRecord.setCorrectAnswer(question.getCorrectAnswer());
            answerRecord.setVersion(0); // 初始版本号
            
            examAnswerRecordService.insertOrUpdateExamAnswerRecord(answerRecord);
            
            return success();
        } catch (Exception e) {
            // 记录日志并返回错误信息
            logger.error("保存答案失败: examId={}, userId={}, questionId={}, error={}", 
                         examId, userId, questionId, e.getMessage());
            return error("保存答案失败，请重试");
        }
    }

    /**
     * 提交考试
     */
    @PreAuthorize("@ss.hasPermi('student:exam:submit')")
    @Log(title = "提交考试", businessType = BusinessType.UPDATE)
    @PostMapping("/submit/{examId}")
    @Transactional
    public AjaxResult submitExam(@PathVariable Long examId, @RequestBody(required = false) Map<String, Object> params)
    {
        Long userId = SecurityUtils.getUserId();

        ExamUser examUser = null;
        
        // 如果传递了examUserId，直接使用该ID查询
        if (params != null && params.containsKey("examUserId") && params.get("examUserId") != null)
        {
            Long examUserId = Long.valueOf(params.get("examUserId").toString());
            examUser = examUserService.selectExamUserById(examUserId);
            if (examUser == null || !examUser.getUserId().equals(userId) || !examUser.getExamId().equals(examId))
            {
                return error("无效的考试用户ID");
            }
        }
        else
        {
            // 如果没有传递examUserId，使用原来的查询方式（向后兼容）
            examUser = examUserService.selectExamUserByExamIdAndUserId(examId, userId);
        }

        if (examUser == null || !USER_EXAM_IN_PROGRESS.equals(examUser.getExamStatus()))
        {
            return error("您当前不在考试状态");
        }

        // 获取考试信息
        ExamExamination examination = examExaminationService.selectExamExaminationById(examId);
        if (examination == null)
        {
            return error("考试不存在");
        }

        // 更新考试状态为已提交
        examUser.setExamStatus(USER_EXAM_SUBMITTED);
        examUser.setSubmitTime(new Date());
        examUserService.updateExamUser(examUser);

        // 获取试卷中的题目信息（包含分值）
        List<ExamPaperQuestion> paperQuestions = examPaperQuestionService.selectQuestionsWithDetailsByPaperId(examination.getPaperId());
        Map<Long, ExamPaperQuestion> paperQuestionMap = paperQuestions.stream()
            .collect(Collectors.toMap(ExamPaperQuestion::getQuestionId, pq -> pq));

        // 获取所有答题记录
        List<ExamAnswerRecord> answerRecords = examAnswerRecordService.selectAnswerRecordsByExamUserId(examUser.getId());

        // 对每个答题记录进行评分
        BigDecimal totalScore = BigDecimal.ZERO;
        for (ExamAnswerRecord record : answerRecords)
        {
            // 获取试题信息
            ExamQuestion question = examQuestionService.selectQuestionById(record.getQuestionId());
            if (question == null) continue;
            
            // 获取试卷中该题目的分值
            ExamPaperQuestion paperQuestion = paperQuestionMap.get(record.getQuestionId());
            if (paperQuestion == null) continue;

            // 设置正确答案和题目分值（使用试卷中配置的分值）
            record.setCorrectAnswer(question.getCorrectAnswer());
            record.setQuestionScore(paperQuestion.getScore());

            // 根据题目类型进行评分
            String questionType = question.getQuestionType();
            String userAnswer = record.getUserAnswer();
            String correctAnswer = question.getCorrectAnswer();

            if (StringUtils.isEmpty(userAnswer))
            {
                // 未答题
                record.setUserScore(BigDecimal.ZERO);
                record.setIsCorrect("0");
            }
            else if ("single_choice".equals(questionType) || "true_false".equals(questionType) || 
                     "single".equals(questionType) || "judge".equals(questionType))
            {
                // 单选题和判断题：完全匹配（去除首尾空格后比较）
                String trimmedUserAnswer = userAnswer.trim();
                String trimmedCorrectAnswer = correctAnswer.trim();
                
                // 判断题特殊处理：前端使用"1"/"0"，需要转换为选项格式
                if ("judge".equals(questionType) && ("1".equals(trimmedUserAnswer) || "0".equals(trimmedUserAnswer)))
                {
                    trimmedUserAnswer = convertJudgeAnswerFormat(question, trimmedUserAnswer);
                    logger.info("判断题答案格式转换: 题目ID={}, 原答案={}, 转换后={}, 正确答案={}", 
                               question.getId(), userAnswer, trimmedUserAnswer, trimmedCorrectAnswer);
                }
                
                // 单选题和判断题：完全匹配则满分
                if (trimmedUserAnswer.equals(trimmedCorrectAnswer))
                {
                    record.setUserScore(paperQuestion.getScore());
                    record.setIsCorrect("1");
                }
                else
                {
                    record.setUserScore(BigDecimal.ZERO);
                    record.setIsCorrect("0");
                }
            }
            else if ("multiple_choice".equals(questionType) || "multiple".equals(questionType))
            {
                // 多选题：按正确选项比例给分
                String[] userAnswers = userAnswer.split(",");
                String[] correctAnswers = correctAnswer.split(",");

                Set<String> userAnswerSet = new HashSet<>(Arrays.asList(userAnswers));
                Set<String> correctAnswerSet = new HashSet<>(Arrays.asList(correctAnswers));

                // 计算交集（用户答对的选项数）
                Set<String> intersection = new HashSet<>(userAnswerSet);
                intersection.retainAll(correctAnswerSet);
                int correctCount = intersection.size();

                // 计算用户多选的错误选项数
                Set<String> userWrongAnswers = new HashSet<>(userAnswerSet);
                userWrongAnswers.removeAll(correctAnswerSet);
                int wrongCount = userWrongAnswers.size();

                int totalCorrect = correctAnswerSet.size();

                if (correctCount == totalCorrect && wrongCount == 0)
                {
                    // 完全正确
                    record.setUserScore(paperQuestion.getScore());
                    record.setIsCorrect("1");
                }
                else if (correctCount > 0 && wrongCount == 0)
                {
                    // 部分正确（选对了一些，但没有选错的）
                    BigDecimal partialScore = paperQuestion.getScore()
                        .multiply(BigDecimal.valueOf(correctCount))
                        .divide(BigDecimal.valueOf(totalCorrect), 2, BigDecimal.ROUND_HALF_UP);
                    record.setUserScore(partialScore);
                    record.setIsCorrect("2");
                }
                else
                {
                    // 错误（选错了选项或完全没选对）
                    record.setUserScore(BigDecimal.ZERO);
                    record.setIsCorrect("0");
                }
            }
            else if ("fill".equals(questionType) || "fill_blank".equals(questionType))
            {
                // 填空题：暂时给0分，标记为待评分，后续通过专门的重新评分接口处理
                record.setUserScore(BigDecimal.ZERO);
                record.setIsCorrect("0");
                record.setIsGraded("0"); // 待评分
                
                logger.info("填空题暂时标记为待评分 - 题目ID: {}, 用户答案: {}, 正确答案: {}", 
                           record.getQuestionId(), userAnswer, correctAnswer);
                
                // 在考试提交完成后，会调用专门的填空题重新评分方法
            }
            else
            {
                // 简答题：需要人工评分，暂时给0分
                record.setUserScore(BigDecimal.ZERO);
                record.setIsCorrect("0");
                record.setIsGraded("0"); // 待评分
            }

            // 更新评分信息（除了简答题）
            if (!"essay".equals(questionType)) {
                record.setIsGraded("1");
                record.setGradeTime(new Date());
                record.setGradeBy("system");
            }

            // 更新答题记录
            examAnswerRecordService.updateExamAnswerRecord(record);

            // 累加得分
            if (record.getUserScore() != null) {
                totalScore = totalScore.add(record.getUserScore());
            }
        }

        // 更新考试用户的总分和状态
        examUser.setTotalScore(totalScore);
        examUser.setPassScore(examination.getPassScore());
        examUser.setIsPassed(totalScore.compareTo(examination.getPassScore()) >= 0 ? "1" : "0");
        examUser.setExamStatus(USER_EXAM_GRADED);
        examUser.setGradeTime(new Date());
        examUser.setGradeBy("system");

        // 保存考试用户更新
        examUserService.updateExamUser(examUser);
        
        // 对填空题进行重新评分
        try {
            int fillBlankCount = examAnswerRecordService.regradeFillBlankQuestions(examUser.getId());
            if (fillBlankCount > 0) {
                examAnswerRecordService.calculateAndUpdateScore(examUser.getId());
                ExamUser refreshedExamUser = examUserService.selectExamUserById(examUser.getId());
                if (refreshedExamUser != null) {
                    examUser = refreshedExamUser;
                    totalScore = examUser.getTotalScore() == null ? BigDecimal.ZERO : examUser.getTotalScore();
                }
            }
            logger.info("填空题重新评分完成 - 考试用户ID: {}", examUser.getId());
        } catch (Exception e) {
            logger.error("填空题重新评分失败 - 考试用户ID: {}, 错误: {}", examUser.getId(), e.getMessage(), e);
        }

        AjaxResult result = success();
        result.put("totalScore", totalScore);
        result.put("isPassed", examUser.getIsPassed());
        result.put("answerCount", answerRecords.size());
        return result;
    }

    /**
     * 获取考试成绩
     */
    @PreAuthorize("@ss.hasPermi('student:exam:result')")
    @GetMapping("/result/{id}")
    public AjaxResult getExamResult(@PathVariable String id)
    {
        Long userId = SecurityUtils.getUserId();
        logger.info("用户{}查询考试成绩，参数ID: {}", userId, id);
        
        // 参数验证
        if (StringUtils.isEmpty(id) || "undefined".equals(id) || "null".equals(id)) {
            logger.warn("用户{}查询考试成绩时传递了无效的ID参数: {}", userId, id);
            return error("参数错误：考试记录ID不能为空");
        }
        
        Long recordId;
        try {
            recordId = Long.valueOf(id);
        } catch (NumberFormatException e) {
            logger.warn("用户{}查询考试成绩时传递了无法解析的ID参数: {}", userId, id);
            return error("参数错误：考试记录ID格式不正确");
        }
        
        ExamUser examUser = null;
        
        // 首先尝试按考试记录ID查询
        examUser = examUserService.selectExamUserById(recordId);
        
        // 如果找到记录，检查是否属于当前用户
        if (examUser != null && examUser.getUserId().equals(userId)) {
            logger.info("按考试记录ID{}找到用户{}的考试记录", recordId, userId);
        } else {
            // 如果没找到或不属于当前用户，尝试按examId查询（向后兼容）
            logger.info("按考试记录ID未找到，尝试按考试ID{}查询用户{}的最新记录", recordId, userId);
            examUser = examUserService.selectExamUserByExamIdAndUserId(recordId, userId);
            
            if (examUser != null) {
                logger.info("按考试ID{}找到用户{}的考试记录，记录ID: {}, 考试状态: {}", 
                    recordId, userId, examUser.getId(), examUser.getExamStatus());
            } else {
                logger.warn("按考试ID{}也未找到用户{}的考试记录", recordId, userId);
                
                // 进一步尝试查询该用户的所有考试记录进行调试
                List<ExamUser> allUserRecords = examUserService.selectExamUserListByUserId(userId);
                logger.info("用户{}的所有考试记录数量: {}", userId, allUserRecords.size());
                
                if (!allUserRecords.isEmpty()) {
                    // 查找指定考试ID的记录
                    List<ExamUser> examSpecificRecords = allUserRecords.stream()
                        .filter(record -> record.getExamId().equals(recordId))
                        .collect(java.util.stream.Collectors.toList());
                    
                    if (!examSpecificRecords.isEmpty()) {
                        logger.info("找到考试ID{}的记录{}条", recordId, examSpecificRecords.size());
                        // 选择最新的已评分记录
                        examUser = examSpecificRecords.stream()
                            .filter(record -> USER_EXAM_GRADED.equals(record.getExamStatus()))
                            .sorted((r1, r2) -> {
                                if (r1.getAttemptNumber() != null && r2.getAttemptNumber() != null) {
                                    return r2.getAttemptNumber().compareTo(r1.getAttemptNumber());
                                }
                                return r2.getCreateTime().compareTo(r1.getCreateTime());
                            })
                            .findFirst()
                            .orElse(examSpecificRecords.get(0));
                        
                        logger.info("选择记录ID: {}, 状态: {}, 考试次数: {}", 
                            examUser.getId(), examUser.getExamStatus(), examUser.getAttemptNumber());
                    } else {
                        logger.warn("用户{}没有考试ID{}的记录", userId, recordId);
                    }
                }
            }
        }
        
        if (examUser == null)
        {
            logger.warn("用户{}未找到ID为{}的考试记录", userId, recordId);
            return error("您未参加此考试");
        }
        
        // 检查成绩是否已发布
        if (!USER_EXAM_GRADED.equals(examUser.getExamStatus()))
        {
            logger.info("用户{}的考试记录ID{}成绩尚未发布，状态: {}", userId, examUser.getId(), examUser.getExamStatus());
            return error("成绩尚未发布");
        }
        
        logger.info("开始获取用户{}考试记录ID{}的详细成绩信息", userId, examUser.getId());
        
        // 获取考试信息
        ExamExamination examination = examExaminationService.selectExamExaminationById(examUser.getExamId());

        // 获取试卷信息
        ExamPaper paper = examPaperService.selectPaperById(examination.getPaperId());

        // 获取试卷的所有题目（用于统计总题数）
        List<ExamPaperQuestion> paperQuestions = examPaperQuestionService.selectQuestionsWithDetailsByPaperId(examination.getPaperId());

        // 获取答题记录（包含题目详情）
        List<ExamAnswerRecord> answers = examAnswerRecordService.selectAnswerRecordsWithQuestionsByExamUserId(examUser.getId());

        logger.info("成功获取用户{}的考试成绩，答题记录{}条", userId, answers.size());

        AjaxResult ajax = AjaxResult.success();
        ajax.put("examination", examination);
        ajax.put("paper", paper);
        ajax.put("paperQuestions", paperQuestions);
        ajax.put("examUser", examUser);
        ajax.put("answers", answers);
        return ajax;
    }

    /**
     * 获取考试剩余时间
     */
    @PreAuthorize("@ss.hasPermi('student:exam:start')")
    @GetMapping("/remainingTime/{examId}")
    public AjaxResult getRemainingTime(@PathVariable Long examId, Long examUserId)
    {
        Long userId = SecurityUtils.getUserId();
        
        // 获取考试用户记录
        ExamUser examUser = null;
        if (examUserId != null)
        {
            examUser = examUserService.selectExamUserById(examUserId);
            if (examUser == null || !examUser.getUserId().equals(userId) || !examUser.getExamId().equals(examId))
            {
                return error("无效的考试用户ID");
            }
        }
        else
        {
            examUser = examUserService.selectExamUserByExamIdAndUserId(examId, userId);
        }
        if (examUser == null || !USER_EXAM_IN_PROGRESS.equals(examUser.getExamStatus()))
        {
            return error("您当前不在考试状态");
        }
        
        // 获取考试信息
        ExamExamination examination = examExaminationService.selectExamExaminationById(examId);
        
        // 计算剩余时间
        Date now = new Date();
        Date startTime = examUser.getStartTime();
        
        // 如果开始时间为null，使用当前时间作为开始时间并更新记录
        if (startTime == null) {
            startTime = now;
            examUser.setStartTime(startTime);
            examUserService.updateExamUser(examUser);
        }
        
        long elapsedMinutes = (now.getTime() - startTime.getTime()) / (1000 * 60);
        long remainingMinutes = examination.getDuration() - elapsedMinutes;
        
        // 检查考试结束时间
        if (now.after(examination.getEndTime()))
        {
            remainingMinutes = 0;
        }
        
        AjaxResult ajax = AjaxResult.success();
        ajax.put("remainingMinutes", Math.max(0, remainingMinutes));
        ajax.put("isTimeout", remainingMinutes <= 0);
        return ajax;
    }

    /**
     * 判断题答案格式转换
     * 将前端的"1"/"0"格式转换为选项格式（如"A"/"B"）
     */
    private String convertJudgeAnswerFormat(ExamQuestion question, String userAnswer) {
        logger.info("判断题答案格式转换 - 题目ID: {}, 用户答案: {}, 题目选项: {}", 
                   question.getId(), userAnswer, question.getQuestionOptions());
        
        try {
            // 获取判断题选项
            String optionsJson = question.getQuestionOptions();
            if (StringUtils.isEmpty(optionsJson)) {
                // 无选项信息，使用默认转换规则
                logger.warn("判断题选项为空，使用默认转换规则");
                return "1".equals(userAnswer) ? "A" : "B";
            }
            
            // 解析选项JSON
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            com.fasterxml.jackson.databind.JsonNode optionsNode = mapper.readTree(optionsJson);
            
            String correctOptionKey = null;
            String falseOptionKey = null;
            
            // 遍历选项，找到"正确"和"错误"对应的key
            if (optionsNode.isArray()) {
                for (com.fasterxml.jackson.databind.JsonNode option : optionsNode) {
                    if (option.has("key") && option.has("value")) {
                        String key = option.get("key").asText();
                        String value = option.get("value").asText();
                        
                        if ("正确".equals(value) || "true".equalsIgnoreCase(value)) {
                            correctOptionKey = key;
                        } else if ("错误".equals(value) || "false".equalsIgnoreCase(value)) {
                            falseOptionKey = key;
                        }
                    }
                }
            }
            
            // 转换答案格式
            String convertedAnswer;
            if ("1".equals(userAnswer)) {
                convertedAnswer = correctOptionKey != null ? correctOptionKey : "A"; // 默认A表示正确
            } else if ("0".equals(userAnswer)) {
                convertedAnswer = falseOptionKey != null ? falseOptionKey : "B"; // 默认B表示错误
            } else {
                convertedAnswer = userAnswer; // 已经是选项格式，不需要转换
            }
            
            logger.info("判断题答案格式转换结果 - 原答案: {}, 转换后: {}, 正确选项: {}, 错误选项: {}", 
                       userAnswer, convertedAnswer, correctOptionKey, falseOptionKey);
            
            return convertedAnswer;
            
        } catch (Exception e) {
            logger.error("判断题答案格式转换失败: {}", e.getMessage());
            // 解析失败，使用默认转换规则
            String defaultAnswer = "1".equals(userAnswer) ? "A" : "B";
            logger.warn("使用默认转换规则: {} -> {}", userAnswer, defaultAnswer);
            return defaultAnswer;
        }
    }
}
