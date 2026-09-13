package com.ruoyi.web.controller.mobile;

import static com.ruoyi.exam.constant.ExamStatusConstants.EXAM_CANCELLED;
import static com.ruoyi.exam.constant.ExamStatusConstants.EXAM_DRAFT;
import static com.ruoyi.exam.constant.ExamStatusConstants.EXAM_IN_PROGRESS;
import static com.ruoyi.exam.constant.ExamStatusConstants.REGISTRATION_REGISTERED;
import static com.ruoyi.exam.constant.ExamStatusConstants.REGISTRATION_REJECTED;
import static com.ruoyi.exam.constant.ExamStatusConstants.USER_EXAM_GRADED;
import static com.ruoyi.exam.constant.ExamStatusConstants.USER_EXAM_IN_PROGRESS;
import static com.ruoyi.exam.constant.ExamStatusConstants.USER_EXAM_SUBMITTED;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.exception.ServiceException;
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

/**
 * 移动端考试Controller
 * 为H5移动端提供简化的考试接口
 * 
 * @author ruoyi
 */
@RestController
@RequestMapping("/mobile/exam")
public class MobileExamController extends BaseController {
    private static final Logger logger = LoggerFactory.getLogger(MobileExamController.class);

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
     * 获取考试信息（H5扫码后首页）
     */
    @PreAuthorize("@ss.hasPermi('student:exam:list')")
    @GetMapping("/{examId}/info")
    public AjaxResult getExamInfo(@PathVariable Long examId) {
        Long userId = SecurityUtils.getUserId();

        // 获取考试信息
        ExamExamination examination = examExaminationService.selectExamExaminationById(examId);
        if (examination == null) {
            return error("考试不存在");
        }

        // 检查考试状态
        if (EXAM_DRAFT.equals(examination.getStatus())) {
            return error("考试尚未发布");
        }
        if (EXAM_CANCELLED.equals(examination.getStatus())) {
            return error("考试已取消");
        }
        if (!examExaminationService.canUserAccessExam(examId, userId, SecurityUtils.getDeptId())) {
            return error("您不在本次考试授权范围内");
        }

        // 查询用户考试记录
        ExamUser examUser = examUserService.selectExamUserByExamIdAndUserId(examId, userId);
        int attemptCount = 0;
        if (examUser != null) {
            attemptCount = examUserService.countUserAttempts(examId, userId);
        }

        AjaxResult ajax = AjaxResult.success();
        ajax.put("examination", examination);
        ajax.put("examUser", examUser);
        ajax.put("attemptCount", attemptCount);
        ajax.put("canStart", canStartExam(examination, examUser, attemptCount));
        return ajax;
    }

    /**
     * 判断是否可以开始考试
     */
    private boolean canStartExam(ExamExamination examination, ExamUser examUser, int attemptCount) {
        Date now = new Date();

        // 检查考试时间
        if (now.before(examination.getStartTime())) {
            return false; // 考试未开始
        }
        if (now.after(examination.getEndTime())) {
            return false; // 考试已结束
        }

        // 检查考试状态
        if (!EXAM_IN_PROGRESS.equals(examination.getStatus())) {
            return false;
        }

        // 检查是否正在进行中
        if (examUser != null && USER_EXAM_IN_PROGRESS.equals(examUser.getExamStatus())) {
            return true; // 可以继续考试
        }

        if (examUser != null && (REGISTRATION_REGISTERED.equals(examUser.getRegistrationStatus())
                || REGISTRATION_REJECTED.equals(examUser.getRegistrationStatus()))) {
            return false;
        }

        if (examUser != null && USER_EXAM_SUBMITTED.equals(examUser.getExamStatus())) {
            return false;
        }

        Integer maxAttempts = examination.getMaxAttempts() == null ? 1 : examination.getMaxAttempts();
        return attemptCount < maxAttempts;
    }

    /**
     * 开始考试（自动报名）
     */
    @PreAuthorize("@ss.hasPermi('student:exam:start')")
    @Log(title = "移动端开始考试", businessType = BusinessType.UPDATE)
    @PostMapping("/{examId}/start")
    public AjaxResult startExam(@PathVariable Long examId) {
        ExamUser examUser;
        try {
            examUser = examUserService.startStudentExam(examId, SecurityUtils.getUserId(), SecurityUtils.getDeptId());
        } catch (ServiceException e) {
            return error(e.getMessage());
        }

        AjaxResult result = success(USER_EXAM_IN_PROGRESS.equals(examUser.getExamStatus()) ? "考试已开始" : "操作成功");
        result.put("examUserId", examUser.getId());
        return result;
    }

    /**
     * 获取考试试卷
     */
    @PreAuthorize("@ss.hasPermi('student:exam:start')")
    @GetMapping("/{examId}/paper")
    public AjaxResult getExamPaper(@PathVariable Long examId, Long examUserId) {
        Long userId = SecurityUtils.getUserId();

        ExamUser examUser = null;

        // 如果传递了examUserId，直接使用该ID查询
        if (examUserId != null) {
            examUser = examUserService.selectExamUserById(examUserId);
            if (examUser == null || !examUser.getUserId().equals(userId) || !examUser.getExamId().equals(examId)) {
                return error("无效的考试用户ID");
            }
        } else {
            // 如果没有传递examUserId，使用原来的查询方式
            examUser = examUserService.selectExamUserByExamIdAndUserId(examId, userId);
        }

        if (examUser == null || !USER_EXAM_IN_PROGRESS.equals(examUser.getExamStatus())) {
            return error("您当前不在考试状态");
        }

        // 获取考试信息
        ExamExamination examination = examExaminationService.selectExamExaminationById(examId);
        if (examination == null) {
            return error("考试不存在");
        }

        // 获取试卷信息
        ExamPaper paper = examPaperService.selectPaperById(examination.getPaperId());
        if (paper == null) {
            return error("试卷不存在");
        }

        // 获取试题列表（带试题详细信息）
        List<ExamPaperQuestion> questions = examPaperQuestionService
                .selectQuestionsWithDetailsByPaperId(examination.getPaperId());

        // 获取已答题记录
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
     * 保存答案
     */
    @PreAuthorize("@ss.hasPermi('student:exam:submit')")
    @Log(title = "移动端保存答案", businessType = BusinessType.UPDATE)
    @PostMapping("/answer")
    public AjaxResult saveAnswer(@RequestBody Map<String, Object> params) {
        Long userId = SecurityUtils.getUserId();
        Long examId = Long.valueOf(params.get("examId").toString());
        Long questionId = Long.valueOf(params.get("questionId").toString());
        String userAnswer = params.get("userAnswer").toString();

        ExamUser examUser = null;

        // 如果传递了examUserId，直接使用该ID查询
        if (params.containsKey("examUserId") && params.get("examUserId") != null) {
            Long examUserId = Long.valueOf(params.get("examUserId").toString());
            examUser = examUserService.selectExamUserById(examUserId);
            if (examUser == null || !examUser.getUserId().equals(userId) || !examUser.getExamId().equals(examId)) {
                return error("无效的考试用户ID");
            }
        } else {
            examUser = examUserService.selectExamUserByExamIdAndUserId(examId, userId);
        }

        if (examUser == null || !USER_EXAM_IN_PROGRESS.equals(examUser.getExamStatus())) {
            return error("您当前不在考试状态");
        }

        // 获取考试信息
        ExamExamination examination = examExaminationService.selectExamExaminationById(examId);
        if (examination == null) {
            return error("考试不存在");
        }

        // 获取试题信息
        ExamQuestion question = examQuestionService.selectQuestionById(questionId);
        if (question == null) {
            return error("试题不存在");
        }

        // 获取试卷中该题目的分值
        List<ExamPaperQuestion> paperQuestions = examPaperQuestionService
                .selectQuestionsWithDetailsByPaperId(examination.getPaperId());
        ExamPaperQuestion paperQuestion = paperQuestions.stream()
                .filter(pq -> pq.getQuestionId().equals(questionId))
                .findFirst()
                .orElse(null);

        if (paperQuestion == null) {
            return error("该题目不在当前试卷中");
        }

        try {
            ExamAnswerRecord answerRecord = new ExamAnswerRecord();
            answerRecord.setExamId(examId);
            answerRecord.setExamUserId(examUser.getId());
            answerRecord.setUserId(userId);
            answerRecord.setPaperId(examination.getPaperId());
            answerRecord.setQuestionId(questionId);
            answerRecord.setUserAnswer(userAnswer);
            answerRecord.setAnswerTime(new Date());
            answerRecord.setQuestionScore(paperQuestion.getScore());
            answerRecord.setCorrectAnswer(question.getCorrectAnswer());
            answerRecord.setVersion(0);

            examAnswerRecordService.insertOrUpdateExamAnswerRecord(answerRecord);

            return success();
        } catch (Exception e) {
            logger.error("保存答案失败", e);
            return error("保存答案失败，请重试");
        }
    }

    /**
     * 提交考试
     */
    @PreAuthorize("@ss.hasPermi('student:exam:submit')")
    @Log(title = "移动端提交考试", businessType = BusinessType.UPDATE)
    @PostMapping("/{examId}/submit")
    @Transactional
    public AjaxResult submitExam(@PathVariable Long examId, @RequestBody(required = false) Map<String, Object> params) {
        Long userId = SecurityUtils.getUserId();

        ExamUser examUser = null;

        // 如果传递了examUserId，直接使用该ID查询
        if (params != null && params.containsKey("examUserId") && params.get("examUserId") != null) {
            Long examUserId = Long.valueOf(params.get("examUserId").toString());
            examUser = examUserService.selectExamUserById(examUserId);
            if (examUser == null || !examUser.getUserId().equals(userId) || !examUser.getExamId().equals(examId)) {
                return error("无效的考试用户ID");
            }
        } else {
            examUser = examUserService.selectExamUserByExamIdAndUserId(examId, userId);
        }

        if (examUser == null || !USER_EXAM_IN_PROGRESS.equals(examUser.getExamStatus())) {
            return error("您当前不在考试状态");
        }

        // 获取考试信息
        ExamExamination examination = examExaminationService.selectExamExaminationById(examId);
        if (examination == null) {
            return error("考试不存在");
        }

        // 更新考试状态为已提交
        examUser.setExamStatus(USER_EXAM_SUBMITTED);
        examUser.setSubmitTime(new Date());
        examUserService.updateExamUser(examUser);

        // 获取试卷中的题目信息（包含分值）
        List<ExamPaperQuestion> paperQuestions = examPaperQuestionService
                .selectQuestionsWithDetailsByPaperId(examination.getPaperId());
        Map<Long, ExamPaperQuestion> paperQuestionMap = paperQuestions.stream()
                .collect(Collectors.toMap(ExamPaperQuestion::getQuestionId, pq -> pq));

        // 获取所有答题记录
        List<ExamAnswerRecord> answerRecords = examAnswerRecordService
                .selectAnswerRecordsByExamUserId(examUser.getId());

        // 自动评分
        BigDecimal totalScore = BigDecimal.ZERO;
        for (ExamAnswerRecord record : answerRecords) {
            ExamQuestion question = examQuestionService.selectQuestionById(record.getQuestionId());
            if (question == null)
                continue;

            ExamPaperQuestion paperQuestion = paperQuestionMap.get(record.getQuestionId());
            if (paperQuestion == null)
                continue;

            record.setCorrectAnswer(question.getCorrectAnswer());
            record.setQuestionScore(paperQuestion.getScore());

            // 根据题目类型进行评分
            String questionType = question.getQuestionType();
            String userAnswer = record.getUserAnswer();
            String correctAnswer = question.getCorrectAnswer();

            if (StringUtils.isEmpty(userAnswer)) {
                record.setUserScore(BigDecimal.ZERO);
                record.setIsCorrect("0");
            } else if ("single".equals(questionType) || "single_choice".equals(questionType) ||
                    "judge".equals(questionType) || "true_false".equals(questionType)) {
                // 单选题和判断题
                String trimmedUserAnswer = userAnswer.trim();
                String trimmedCorrectAnswer = correctAnswer.trim();

                if ("judge".equals(questionType) && ("1".equals(trimmedUserAnswer) || "0".equals(trimmedUserAnswer))) {
                    trimmedUserAnswer = convertJudgeAnswerFormat(question, trimmedUserAnswer);
                }

                if (trimmedUserAnswer.equals(trimmedCorrectAnswer)) {
                    record.setUserScore(paperQuestion.getScore());
                    record.setIsCorrect("1");
                } else {
                    record.setUserScore(BigDecimal.ZERO);
                    record.setIsCorrect("0");
                }
            } else if ("multiple".equals(questionType) || "multiple_choice".equals(questionType)) {
                Set<String> userAnswerSet = new HashSet<>();
                for (String answer : userAnswer.split(",")) {
                    String trimmed = answer.trim();
                    if (!trimmed.isEmpty()) {
                        userAnswerSet.add(trimmed);
                    }
                }

                Set<String> correctAnswerSet = new HashSet<>();
                for (String answer : correctAnswer.split(",")) {
                    String trimmed = answer.trim();
                    if (!trimmed.isEmpty()) {
                        correctAnswerSet.add(trimmed);
                    }
                }

                Set<String> intersection = new HashSet<>(userAnswerSet);
                intersection.retainAll(correctAnswerSet);
                int correctCount = intersection.size();

                Set<String> wrongAnswers = new HashSet<>(userAnswerSet);
                wrongAnswers.removeAll(correctAnswerSet);
                int wrongCount = wrongAnswers.size();
                int totalCorrect = correctAnswerSet.size();

                if (correctCount == totalCorrect && wrongCount == 0) {
                    record.setUserScore(paperQuestion.getScore());
                    record.setIsCorrect("1");
                } else if (correctCount > 0 && wrongCount == 0 && totalCorrect > 0) {
                    BigDecimal partialScore = paperQuestion.getScore()
                            .multiply(BigDecimal.valueOf(correctCount))
                            .divide(BigDecimal.valueOf(totalCorrect), 2, BigDecimal.ROUND_HALF_UP);
                    record.setUserScore(partialScore);
                    record.setIsCorrect("2");
                } else {
                    record.setUserScore(BigDecimal.ZERO);
                    record.setIsCorrect("0");
                }
            } else if ("fill".equals(questionType) || "fill_blank".equals(questionType)) {
                BigDecimal fillScore = examAnswerRecordService.gradeFillInTheBlank(userAnswer, correctAnswer, paperQuestion.getScore());
                record.setUserScore(fillScore);
                if (fillScore.compareTo(paperQuestion.getScore()) == 0) {
                    record.setIsCorrect("1");
                } else if (fillScore.compareTo(BigDecimal.ZERO) > 0) {
                    record.setIsCorrect("2");
                } else {
                    record.setIsCorrect("0");
                }
                record.setIsGraded("1");
                record.setGradeTime(new Date());
                record.setGradeBy("system");
            } else {
                // 简答题需要人工评分
                record.setUserScore(BigDecimal.ZERO);
                record.setIsCorrect("0");
                record.setIsGraded("0");
            }

            if (!"essay".equals(questionType)) {
                record.setIsGraded("1");
                record.setGradeTime(new Date());
                record.setGradeBy("system");
            }

            totalScore = totalScore.add(record.getUserScore() != null ? record.getUserScore() : BigDecimal.ZERO);
            examAnswerRecordService.updateExamAnswerRecord(record);
        }

        // 更新用户总分
        examUser.setTotalScore(totalScore);
        examUser.setPassScore(examination.getPassScore());
        examUser.setIsPassed(totalScore.compareTo(examination.getPassScore()) >= 0 ? "1" : "0");
        examUser.setExamStatus(USER_EXAM_GRADED);
        examUser.setGradeTime(new Date());
        examUserService.updateExamUser(examUser);

        AjaxResult result = success("提交成功");
        result.put("totalScore", totalScore);
        result.put("passScore", examination.getPassScore());
        result.put("isPassed", examUser.getIsPassed());
        return result;
    }

    /**
     * 获取考试结果
     */
    @PreAuthorize("@ss.hasPermi('student:exam:result')")
    @GetMapping("/{examId}/result")
    public AjaxResult getExamResult(@PathVariable Long examId, Long examUserId) {
        Long userId = SecurityUtils.getUserId();

        ExamUser examUser = null;

        if (examUserId != null) {
            examUser = examUserService.selectExamUserById(examUserId);
            if (examUser == null || !examUser.getUserId().equals(userId) || !examUser.getExamId().equals(examId)) {
                return error("无效的考试用户ID");
            }
        } else {
            examUser = examUserService.selectExamUserByExamIdAndUserId(examId, userId);
        }

        if (examUser == null) {
            return error("未找到考试记录");
        }
        if (!USER_EXAM_SUBMITTED.equals(examUser.getExamStatus()) && !USER_EXAM_GRADED.equals(examUser.getExamStatus())) {
            return error("当前考试记录尚未提交，不能查看成绩");
        }

        // 获取考试信息
        ExamExamination examination = examExaminationService.selectExamExaminationById(examId);

        // 获取试卷题目，用于移动端按整张试卷统计未答题数
        List<ExamPaperQuestion> paperQuestions = examPaperQuestionService
                .selectQuestionsWithDetailsByPaperId(examination.getPaperId());

        // 获取答题记录
        List<ExamAnswerRecord> answerRecords = examAnswerRecordService
                .selectAnswerRecordsByExamUserId(examUser.getId());

        AjaxResult ajax = AjaxResult.success();
        ajax.put("examination", examination);
        ajax.put("examUser", examUser);
        ajax.put("paperQuestions", paperQuestions);
        ajax.put("answerRecords", answerRecords);
        return ajax;
    }

    /**
     * 判断题答案格式转换
     * 将前端的"1"/"0"格式转换为试题选项中的正确/错误选项。
     */
    private String convertJudgeAnswerFormat(ExamQuestion question, String userAnswer) {
        try {
            String optionsJson = question.getQuestionOptions();
            if (StringUtils.isEmpty(optionsJson)) {
                return "1".equals(userAnswer) ? "A" : "B";
            }

            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            com.fasterxml.jackson.databind.JsonNode optionsNode = mapper.readTree(optionsJson);

            String trueOptionKey = null;
            String falseOptionKey = null;
            if (optionsNode.isArray()) {
                for (com.fasterxml.jackson.databind.JsonNode option : optionsNode) {
                    String key = option.has("key") ? option.get("key").asText() : null;
                    String value = option.has("value") ? option.get("value").asText() : "";
                    if (key == null && option.has("label")) {
                        key = option.get("label").asText();
                    }
                    if (key == null) {
                        continue;
                    }
                    if ("正确".equals(value) || "对".equals(value) || "true".equalsIgnoreCase(value)) {
                        trueOptionKey = key;
                    } else if ("错误".equals(value) || "错".equals(value) || "false".equalsIgnoreCase(value)) {
                        falseOptionKey = key;
                    }
                }
            }

            if ("1".equals(userAnswer) && trueOptionKey != null) {
                return trueOptionKey;
            }
            if ("0".equals(userAnswer) && falseOptionKey != null) {
                return falseOptionKey;
            }
        } catch (Exception e) {
            logger.warn("移动端判断题答案格式转换失败，使用默认规则。questionId={}, error={}", question.getId(), e.getMessage());
        }
        return "1".equals(userAnswer) ? "A" : "B";
    }
}
