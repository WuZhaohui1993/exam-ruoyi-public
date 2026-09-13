package com.ruoyi.exam.examination.service.impl;

import static com.ruoyi.exam.constant.ExamStatusConstants.USER_EXAM_GRADED;

import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.ArrayList;
import java.math.BigDecimal;
import java.util.Date;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.exam.examination.mapper.ExamAnswerRecordMapper;
import com.ruoyi.exam.examination.domain.ExamAnswerRecord;
import com.ruoyi.exam.examination.domain.ExamUser;
import com.ruoyi.exam.examination.domain.ExamExamination;
import com.ruoyi.exam.examination.service.IExamAnswerRecordService;
import com.ruoyi.exam.examination.service.IExamUserService;
import com.ruoyi.exam.examination.service.IExamExaminationService;
import com.ruoyi.exam.question.domain.ExamQuestion;
import com.ruoyi.exam.question.domain.ExamQuestionMedia;
import com.ruoyi.exam.question.mapper.ExamQuestionMapper;
import com.ruoyi.exam.question.service.IExamQuestionMediaService;
import com.ruoyi.exam.paper.service.IExamPaperQuestionService;
import java.util.Map;
import java.util.stream.Collectors;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.html.HtmlSanitizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 答题记录Service业务层处理
 * 
 * @author ruoyi
 */
@Service
public class ExamAnswerRecordServiceImpl implements IExamAnswerRecordService 
{
    private static final Logger logger = LoggerFactory.getLogger(ExamAnswerRecordServiceImpl.class);

    @Autowired
    private ExamAnswerRecordMapper examAnswerRecordMapper;

    @Autowired
    private ExamQuestionMapper examQuestionMapper;

    @Autowired
    private IExamUserService examUserService;

    @Autowired
    private IExamExaminationService examExaminationService;

    @Autowired
    private IExamPaperQuestionService examPaperQuestionService;

    @Autowired
    private IExamQuestionMediaService questionMediaService;

    /**
     * 查询答题记录
     * 
     * @param id 答题记录主键
     * @return 答题记录
     */
    @Override
    public ExamAnswerRecord selectExamAnswerRecordById(Long id)
    {
        return examAnswerRecordMapper.selectExamAnswerRecordById(id);
    }

    /**
     * 查询答题记录列表
     * 
     * @param examAnswerRecord 答题记录
     * @return 答题记录
     */
    @Override
    public List<ExamAnswerRecord> selectExamAnswerRecordList(ExamAnswerRecord examAnswerRecord)
    {
        return examAnswerRecordMapper.selectExamAnswerRecordList(examAnswerRecord);
    }

    /**
     * 根据考试用户ID查询答题记录
     * 
     * @param examUserId 考试用户关联ID
     * @return 答题记录列表
     */
    @Override
    public List<ExamAnswerRecord> selectAnswerRecordsByExamUserId(Long examUserId)
    {
        return examAnswerRecordMapper.selectAnswerRecordsByExamUserId(examUserId);
    }

    /**
     * 根据考试用户ID查询答题记录（包含题目详情）
     * 
     * @param examUserId 考试用户关联ID
     * @return 答题记录列表
     */
    @Override
    public List<ExamAnswerRecord> selectAnswerRecordsWithQuestionsByExamUserId(Long examUserId)
    {
        List<ExamAnswerRecord> records = examAnswerRecordMapper.selectAnswerRecordsWithQuestionsByExamUserId(examUserId);
        fillQuestionMedia(records);
        return records;
    }

    private void fillQuestionMedia(List<ExamAnswerRecord> records)
    {
        if (StringUtils.isEmpty(records))
        {
            return;
        }

        List<Long> questionIds = new ArrayList<>();
        for (ExamAnswerRecord record : records)
        {
            if (record != null && record.getQuestionId() != null)
            {
                questionIds.add(record.getQuestionId());
            }
        }
        if (questionIds.isEmpty())
        {
            return;
        }

        Map<Long, List<ExamQuestionMedia>> mediaMap = questionMediaService.selectMediaMapByQuestionIds(questionIds);
        for (ExamAnswerRecord record : records)
        {
            if (record != null && record.getQuestion() != null)
            {
                record.getQuestion().setMediaList(mediaMap.get(record.getQuestionId()));
            }
        }
    }

    /**
     * 根据考试用户ID和试题ID查询答题记录
     * 
     * @param examUserId 考试用户关联ID
     * @param questionId 试题ID
     * @return 答题记录
     */
    @Override
    public ExamAnswerRecord selectAnswerRecordByExamUserIdAndQuestionId(Long examUserId, Long questionId)
    {
        return examAnswerRecordMapper.selectAnswerRecordByExamUserIdAndQuestionId(examUserId, questionId);
    }

    /**
     * 根据考试ID查询答题记录
     * 
     * @param examId 考试ID
     * @return 答题记录列表
     */
    @Override
    public List<ExamAnswerRecord> selectAnswerRecordsByExamId(Long examId)
    {
        return examAnswerRecordMapper.selectAnswerRecordsByExamId(examId);
    }

    /**
     * 根据用户ID查询答题记录
     * 
     * @param userId 用户ID
     * @return 答题记录列表
     */
    @Override
    public List<ExamAnswerRecord> selectAnswerRecordsByUserId(Long userId)
    {
        return examAnswerRecordMapper.selectAnswerRecordsByUserId(userId);
    }

    /**
     * 新增答题记录
     * 
     * @param examAnswerRecord 答题记录
     * @return 结果
     */
    @Override
    public int insertExamAnswerRecord(ExamAnswerRecord examAnswerRecord)
    {
        sanitizeAnswerRecord(examAnswerRecord);
        return examAnswerRecordMapper.insertExamAnswerRecord(examAnswerRecord);
    }

    /**
     * 并发安全的插入或更新答题记录
     * 
     * @param examAnswerRecord 答题记录
     * @return 结果
     */
    @Override
    public int insertOrUpdateExamAnswerRecord(ExamAnswerRecord examAnswerRecord)
    {
        sanitizeAnswerRecord(examAnswerRecord);
        return examAnswerRecordMapper.insertOrUpdateExamAnswerRecord(examAnswerRecord);
    }

    /**
     * 修改答题记录
     * 
     * @param examAnswerRecord 答题记录
     * @return 结果
     */
    @Override
    public int updateExamAnswerRecord(ExamAnswerRecord examAnswerRecord)
    {
        sanitizeAnswerRecord(examAnswerRecord);
        return examAnswerRecordMapper.updateExamAnswerRecord(examAnswerRecord);
    }

    private void sanitizeAnswerRecord(ExamAnswerRecord examAnswerRecord)
    {
        if (examAnswerRecord == null)
        {
            return;
        }
        examAnswerRecord.setUserAnswer(HtmlSanitizer.cleanText(examAnswerRecord.getUserAnswer()));
        examAnswerRecord.setCorrectAnswer(HtmlSanitizer.cleanText(examAnswerRecord.getCorrectAnswer()));
        examAnswerRecord.setGradeComment(HtmlSanitizer.cleanText(examAnswerRecord.getGradeComment()));
    }

    /**
     * 批量删除答题记录
     * 
     * @param ids 需要删除的答题记录主键
     * @return 结果
     */
    @Override
    public int deleteExamAnswerRecordByIds(Long[] ids)
    {
        return examAnswerRecordMapper.deleteExamAnswerRecordByIds(ids);
    }

    /**
     * 删除答题记录信息
     * 
     * @param id 答题记录主键
     * @return 结果
     */
    @Override
    public int deleteExamAnswerRecordById(Long id)
    {
        return examAnswerRecordMapper.deleteExamAnswerRecordById(id);
    }

    /**
     * 根据考试ID删除答题记录
     * 
     * @param examId 考试ID
     * @return 结果
     */
    @Override
    public int deleteAnswerRecordsByExamId(Long examId)
    {
        return examAnswerRecordMapper.deleteAnswerRecordsByExamId(examId);
    }

    /**
     * 根据考试用户ID删除答题记录
     * 
     * @param examUserId 考试用户关联ID
     * @return 结果
     */
    @Override
    public int deleteAnswerRecordsByExamUserId(Long examUserId)
    {
        return examAnswerRecordMapper.deleteAnswerRecordsByExamUserId(examUserId);
    }

    /**
     * 自动评分
     * 
     * @param examUserId 考试用户关联ID
     * @return 结果
     */
    @Override
    @Transactional
    public int autoGrade(Long examUserId)
    {
        List<ExamAnswerRecord> answerRecords = examAnswerRecordMapper.selectAnswerRecordsByExamUserId(examUserId);
        if (answerRecords.isEmpty()) {
            return 0;
        }
        
        // 获取试卷信息（从第一条答题记录中获取paperId）
        Long paperId = answerRecords.get(0).getPaperId();
        if (paperId == null) {
            logger.error("无法获取试卷ID，examUserId: {}", examUserId);
            return 0;
        }
        
        // 获取试卷中的题目信息（包含分值）
        List<com.ruoyi.exam.paper.domain.ExamPaperQuestion> paperQuestions = 
            examPaperQuestionService.selectQuestionsWithDetailsByPaperId(paperId);
        Map<Long, com.ruoyi.exam.paper.domain.ExamPaperQuestion> paperQuestionMap = 
            paperQuestions.stream().collect(java.util.stream.Collectors.toMap(
                com.ruoyi.exam.paper.domain.ExamPaperQuestion::getQuestionId, pq -> pq));
        
        int gradedCount = 0;
        for (ExamAnswerRecord record : answerRecords)
        {
            // 获取试题信息
            ExamQuestion question = examQuestionMapper.selectQuestionById(record.getQuestionId());
            if (question == null) continue;
            
            // 获取试卷中该题目的分值
            com.ruoyi.exam.paper.domain.ExamPaperQuestion paperQuestion = paperQuestionMap.get(record.getQuestionId());
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
                }
                
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
                // 多选题：计算部分分数（优化答案比较逻辑）
                String[] userAnswers = userAnswer.split(",");
                String[] correctAnswers = correctAnswer.split(",");
                
                // 去除空格并转换为Set进行比较
                Set<String> userAnswerSet = new HashSet<>();
                for (String ua : userAnswers) {
                    String trimmed = ua.trim();
                    if (!trimmed.isEmpty()) {
                        userAnswerSet.add(trimmed);
                    }
                }
                
                Set<String> correctAnswerSet = new HashSet<>();
                for (String ca : correctAnswers) {
                    String trimmed = ca.trim();
                    if (!trimmed.isEmpty()) {
                        correctAnswerSet.add(trimmed);
                    }
                }
                
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
                        .divide(BigDecimal.valueOf(totalCorrect), 2, java.math.RoundingMode.HALF_UP);
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
                // 填空题：自动评分
                BigDecimal fillScore = gradeFillInTheBlank(userAnswer, correctAnswer, paperQuestion.getScore());
                record.setUserScore(fillScore);
                
                if (fillScore.compareTo(paperQuestion.getScore()) == 0) {
                    record.setIsCorrect("1"); // 完全正确
                } else if (fillScore.compareTo(BigDecimal.ZERO) > 0) {
                    record.setIsCorrect("2"); // 部分正确
                } else {
                    record.setIsCorrect("0"); // 错误
                }
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
            
            if (examAnswerRecordMapper.updateExamAnswerRecord(record) > 0)
            {
                gradedCount++;
            }
        }
        
        return gradedCount;
    }

    /**
     * 人工评分
     * 
     * @param examAnswerRecord 答题记录
     * @return 结果
     */
    @Override
    public int manualGrade(ExamAnswerRecord examAnswerRecord)
    {
        examAnswerRecord.setIsGraded("1");
        examAnswerRecord.setGradeTime(new Date());
        return examAnswerRecordMapper.updateExamAnswerRecord(examAnswerRecord);
    }

    /**
     * 计算总分并更新考试用户状态
     * 
     * @param examUserId 考试用户关联ID
     * @return 结果
     */
    @Override
    @Transactional
    public int calculateAndUpdateScore(Long examUserId)
    {
        // 获取所有答题记录
        List<ExamAnswerRecord> answerRecords = examAnswerRecordMapper.selectAnswerRecordsByExamUserId(examUserId);
        
        // 计算总分
        BigDecimal totalScore = BigDecimal.ZERO;
        for (ExamAnswerRecord record : answerRecords) {
            if (record.getUserScore() != null) {
                totalScore = totalScore.add(record.getUserScore());
            }
        }
        
        // 获取考试用户信息
        ExamUser examUser = examUserService.selectExamUserById(examUserId);
        if (examUser == null) {
            return 0;
        }
        
        // 获取考试信息以确定及格分数
        ExamExamination examination = examExaminationService.selectExamExaminationById(examUser.getExamId());
        if (examination == null) {
            return 0;
        }
        
        // 更新考试用户成绩和状态
        examUser.setTotalScore(totalScore);
        examUser.setPassScore(examination.getPassScore());
        examUser.setIsPassed(totalScore.compareTo(examination.getPassScore()) >= 0 ? "1" : "0");
        examUser.setExamStatus(USER_EXAM_GRADED);
        examUser.setGradeTime(new Date());
        examUser.setGradeBy("system");
        
        return examUserService.updateExamUser(examUser);
    }
    
    /**
     * 转换判断题答案格式
     * 将前端的"1"/"0"格式转换为选项格式（如"A"/"B"）
     * 
     * @param question 题目信息
     * @param userAnswer 用户答案（"1"表示正确，"0"表示错误）
     * @return 转换后的选项格式答案
     */
    private String convertJudgeAnswerFormat(ExamQuestion question, String userAnswer)
    {
        // 添加调试日志
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
    
    /**
     * 填空题自动评分
     * 
     * @param userAnswer 用户答案（JSON数组格式或单个答案）
     * @param correctAnswer 正确答案（JSON数组格式或单个答案）
     * @param totalScore 题目总分
     * @return 用户得分
     */
    @Override
    public BigDecimal gradeFillInTheBlank(String userAnswer, String correctAnswer, BigDecimal totalScore)
    {
        try {
            // 添加详细的调试日志
            logger.info("填空题评分开始 - 用户答案: {}, 正确答案: {}, 总分: {}", userAnswer, correctAnswer, totalScore);
            
            // 解析用户答案和正确答案
            List<String> userAnswers = parseAnswerArray(userAnswer);
            List<String> correctAnswers = parseAnswerArray(correctAnswer);
            
            logger.info("解析后 - 用户答案: {}, 正确答案: {}", userAnswers, correctAnswers);
            
            // 如果答案数量不匹配，返回0分
            if (userAnswers.size() != correctAnswers.size()) {
                logger.warn("填空题答案数量不匹配 - 用户答案数: {}, 正确答案数: {}", 
                           userAnswers.size(), correctAnswers.size());
                return BigDecimal.ZERO;
            }
            
            int totalBlanks = correctAnswers.size();
            int correctCount = 0;
            
            // 逐个比较每个空的答案
            for (int i = 0; i < totalBlanks; i++) {
                String userAns = userAnswers.get(i);
                String correctAns = correctAnswers.get(i);
                
                boolean isCorrect = isAnswerCorrect(userAns, correctAns);
                logger.info("第{}空比较 - 用户: '{}', 正确: '{}', 结果: {}", 
                           i + 1, userAns, correctAns, isCorrect ? "正确" : "错误");
                
                if (isCorrect) {
                    correctCount++;
                }
            }
            
            // 计算得分（按比例给分）
            if (correctCount == 0) {
                logger.info("所有空都答错，得分为0");
                return BigDecimal.ZERO;
            }
            
            BigDecimal score = totalScore
                .multiply(BigDecimal.valueOf(correctCount))
                .divide(BigDecimal.valueOf(totalBlanks), 2, java.math.RoundingMode.HALF_UP);
                
            logger.info("填空题评分结果 - 总空数: {}, 正确数: {}, 得分: {}/{}", 
                       totalBlanks, correctCount, score, totalScore);
                       
            return score;
            
        } catch (Exception e) {
            logger.error("填空题自动评分失败: {}", e.getMessage(), e);
            return BigDecimal.ZERO;
        }
    }
    
    /**
     * 解析答案数组（支持JSON数组格式和单个答案）
     * 支持两种格式：
     * 1. 简单数组格式（学员端）：["答案1", "答案2"]
     * 2. 嵌套数组格式（管理端）：[["答案1", "答案1备选"], ["答案2"]]
     * 
     * @param answer 答案字符串
     * @return 答案列表
     */
    private List<String> parseAnswerArray(String answer)
    {
        List<String> answers = new ArrayList<>();
        
        if (StringUtils.isEmpty(answer)) {
            return answers;
        }
        
        try {
            // 尝试解析为JSON数组
            if (answer.trim().startsWith("[") && answer.trim().endsWith("]")) {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                com.fasterxml.jackson.databind.JsonNode jsonNode = mapper.readTree(answer);
                
                if (jsonNode.isArray()) {
                    for (com.fasterxml.jackson.databind.JsonNode node : jsonNode) {
                        if (node.isArray()) {
                            // 嵌套数组格式（管理端）：将子数组中的所有答案用分号连接
                            List<String> subAnswers = new ArrayList<>();
                            for (com.fasterxml.jackson.databind.JsonNode subNode : node) {
                                String subAnswer = subNode.asText().trim();
                                if (!subAnswer.isEmpty()) {
                                    subAnswers.add(subAnswer);
                                }
                            }
                            answers.add(String.join(";", subAnswers));
                        } else {
                            // 简单数组格式（学员端）：直接添加答案
                            answers.add(node.asText().trim());
                        }
                    }
                } else {
                    answers.add(answer.trim());
                }
            } else {
                // 单个答案，直接添加
                answers.add(answer.trim());
            }
        } catch (Exception e) {
            // JSON解析失败，当作单个答案处理
            logger.warn("填空题答案解析失败，当作单个答案处理: {}", answer, e);
            answers.add(answer.trim());
        }
        
        return answers;
    }
    
    /**
     * 判断单个答案是否正确
     * 
     * @param userAnswer 用户答案
     * @param correctAnswer 正确答案（支持多个答案用分号分隔）
     * @return 是否正确
     */
    private boolean isAnswerCorrect(String userAnswer, String correctAnswer)
    {
        if (StringUtils.isEmpty(userAnswer) || StringUtils.isEmpty(correctAnswer)) {
            return false;
        }
        
        // 标准化答案（去除首尾空格，转换为小写）
        String normalizedUserAnswer = userAnswer.trim().toLowerCase();
        
        // 支持多个正确答案（用分号分隔）
        String[] correctAnswers = correctAnswer.split(";");
        
        for (String correct : correctAnswers) {
            String normalizedCorrect = correct.trim().toLowerCase();
            if (normalizedUserAnswer.equals(normalizedCorrect)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * 重新评分填空题
     * 专门针对填空题的重新评分方法，解决历史数据评分不准确的问题
     * 
     * @param examUserId 考试用户关联ID
     * @return 重新评分的填空题数量
     */
    /**
     * 测试填空题评分修复效果
     * 用于验证填空题评分问题是否已修复
     */
    public void testFillBlankGradingFix()
    {
        logger.info("=== 开始填空题评分修复测试 ===");
        
        // 测试案例1：问题案例 - 数据库中的实际数据
        String userAnswer1 = "[\"20美元\",\"12个月\"]";
        String correctAnswer1 = "[[\"20美元\"],[\"12个月\"]]";
        BigDecimal totalScore1 = new BigDecimal("10.00");
        
        logger.info("测试案例1 - 实际问题数据");
        BigDecimal score1 = gradeFillInTheBlank(userAnswer1, correctAnswer1, totalScore1);
        logger.info("测试案例1结果 - 得分: {}, 预期: {}", score1, totalScore1);
        
        // 测试案例2：简单数组对简单数组
        String userAnswer2 = "[\"答案1\",\"答案2\"]";
        String correctAnswer2 = "[\"答案1\",\"答案2\"]";
        BigDecimal totalScore2 = new BigDecimal("5.00");
        
        logger.info("测试案例2 - 简单数组对比");
        BigDecimal score2 = gradeFillInTheBlank(userAnswer2, correctAnswer2, totalScore2);
        logger.info("测试案例2结果 - 得分: {}, 预期: {}", score2, totalScore2);
        
        // 测试案例3：简单数组对嵌套数组（多个备选答案）
        String userAnswer3 = "[\"北京\",\"上海\"]";
        String correctAnswer3 = "[[\"北京\",\"beijing\"],[\"上海\",\"shanghai\"]]";
        BigDecimal totalScore3 = new BigDecimal("8.00");
        
        logger.info("测试案例3 - 简单数组对嵌套数组");
        BigDecimal score3 = gradeFillInTheBlank(userAnswer3, correctAnswer3, totalScore3);
        logger.info("测试案例3结果 - 得分: {}, 预期: {}", score3, totalScore3);
        
        // 测试案例4：部分正确
        String userAnswer4 = "[\"正确答案\",\"错误答案\"]";
        String correctAnswer4 = "[[\"正确答案\"],[\"正确答案2\"]]";
        BigDecimal totalScore4 = new BigDecimal("6.00");
        BigDecimal expectedScore4 = new BigDecimal("3.00"); // 50%正确
        
        logger.info("测试案例4 - 部分正确");
        BigDecimal score4 = gradeFillInTheBlank(userAnswer4, correctAnswer4, totalScore4);
        logger.info("测试案例4结果 - 得分: {}, 预期: {}", score4, expectedScore4);
        
        logger.info("=== 填空题评分修复测试完成 ===");
        
        // 总结测试结果
        boolean test1Pass = score1.compareTo(totalScore1) == 0;
        boolean test2Pass = score2.compareTo(totalScore2) == 0;
        boolean test3Pass = score3.compareTo(totalScore3) == 0;
        boolean test4Pass = score4.compareTo(expectedScore4) == 0;
        
        logger.info("测试结果总结:");
        logger.info("案例1（实际问题数据）: {}", test1Pass ? "通过" : "失败");
        logger.info("案例2（简单数组对比）: {}", test2Pass ? "通过" : "失败");
        logger.info("案例3（多备选答案）: {}", test3Pass ? "通过" : "失败");
        logger.info("案例4（部分正确）: {}", test4Pass ? "通过" : "失败");
        
        if (test1Pass && test2Pass && test3Pass && test4Pass) {
            logger.info("✅ 所有测试通过，填空题评分修复成功！");
        } else {
            logger.warn("❌ 部分测试失败，需要进一步修复");
        }
    }
    
    /**
     * 重新评分填空题（修复填空题评分问题）
     * 专门针对填空题的重新评分，解决历史数据评分不准确的问题
     * 
     * @param examUserId 考试用户关联ID
     * @return 重新评分的填空题数量
     */
    @Override
    @Transactional
    public int regradeFillBlankQuestions(Long examUserId)
    {
        List<ExamAnswerRecord> answerRecords = examAnswerRecordMapper.selectAnswerRecordsByExamUserId(examUserId);
        if (answerRecords.isEmpty()) {
            return 0;
        }
        
        // 获取试卷信息（从第一条答题记录中获取paperId）
        Long paperId = answerRecords.get(0).getPaperId();
        if (paperId == null) {
            logger.error("无法获取试卷ID，examUserId: {}", examUserId);
            return 0;
        }
        
        // 获取试卷中的题目信息（包含分值）
        List<com.ruoyi.exam.paper.domain.ExamPaperQuestion> paperQuestions = 
            examPaperQuestionService.selectQuestionsWithDetailsByPaperId(paperId);
        Map<Long, com.ruoyi.exam.paper.domain.ExamPaperQuestion> paperQuestionMap = 
            paperQuestions.stream().collect(Collectors.toMap(
                com.ruoyi.exam.paper.domain.ExamPaperQuestion::getQuestionId, pq -> pq));
        
        int regradedCount = 0;
        for (ExamAnswerRecord record : answerRecords)
        {
            // 获取试题信息
            ExamQuestion question = examQuestionMapper.selectQuestionById(record.getQuestionId());
            if (question == null) continue;
            
            // 获取试卷中该题目的分值
            com.ruoyi.exam.paper.domain.ExamPaperQuestion paperQuestion = paperQuestionMap.get(record.getQuestionId());
            if (paperQuestion == null) continue;
            
            // 只处理填空题
            if (!"fill".equals(question.getQuestionType())) {
                continue;
            }
            
            String userAnswer = record.getUserAnswer();
            String correctAnswer = question.getCorrectAnswer();
            
            if (StringUtils.isEmpty(userAnswer))
            {
                // 未答题
                record.setUserScore(BigDecimal.ZERO);
                record.setIsCorrect("0");
            }
            else
            {
                // 填空题：使用修复后的评分逻辑（使用试卷中配置的分值）
                BigDecimal fillScore = gradeFillInTheBlank(userAnswer, correctAnswer, paperQuestion.getScore());
                record.setUserScore(fillScore);
                
                if (fillScore.compareTo(paperQuestion.getScore()) == 0) {
                    record.setIsCorrect("1"); // 完全正确
                } else if (fillScore.compareTo(BigDecimal.ZERO) > 0) {
                    record.setIsCorrect("2"); // 部分正确
                } else {
                    record.setIsCorrect("0"); // 错误
                }
            }
            
            // 更新评分信息
            record.setIsGraded("1");
            record.setGradeTime(new Date());
            record.setGradeBy("system-fillblank-regrade");
            
            if (examAnswerRecordMapper.updateExamAnswerRecord(record) > 0)
             {
                 regradedCount++;
                 logger.info("填空题重新评分 - 用户ID: {}, 题目ID: {}, 新得分: {}", 
                            examUserId, record.getQuestionId(), record.getUserScore());
             }
        }
        
        logger.info("填空题重新评分完成 - 用户ID: {}, 重新评分题目数: {}", examUserId, regradedCount);
        return regradedCount;
    }
}
