package com.ruoyi.exam.question.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.exam.enums.QuestionTypeEnum;
import com.ruoyi.exam.question.domain.QuestionOption;
import com.ruoyi.exam.question.domain.ExamQuestion;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 试题类型处理工具类
 * 
 * @author ruoyi
 */
public class QuestionTypeUtils
{
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 根据题型初始化标准选项
     * 
     * @param questionType 题型
     * @return 选项列表
     */
    public static List<QuestionOption> initStandardOptions(String questionType)
    {
        List<QuestionOption> options = new ArrayList<>();
        
        switch (questionType)
        {
            case "single":
            case "multiple":
                // 单选题和多选题默认4个选项
                options.add(new QuestionOption("A", ""));
                options.add(new QuestionOption("B", ""));
                options.add(new QuestionOption("C", ""));
                options.add(new QuestionOption("D", ""));
                break;
                
            case "judge":
                // 判断题固定2个选项
                options.add(new QuestionOption("true", "正确"));
                options.add(new QuestionOption("false", "错误"));
                break;
                
            case "fill":
            case "essay":
                // 填空题和简答题不需要选项
                break;
                
            default:
                break;
        }
        
        return options;
    }

    /**
     * 验证题型选项的合理性
     * 
     * @param questionType 题型
     * @param options 选项列表
     * @return 验证结果
     */
    public static boolean validateOptions(String questionType, List<QuestionOption> options)
    {
        if (StringUtils.isEmpty(questionType))
        {
            return false;
        }

        switch (questionType)
        {
            case "single":
                // 单选题必须有选项，且只能有一个正确答案
                if (CollectionUtils.isEmpty(options) || options.size() < 2)
                {
                    return false;
                }
                long correctCount = options.stream().filter(opt -> Boolean.TRUE.equals(opt.getIsCorrect())).count();
                return correctCount == 1;
                
            case "multiple":
                // 多选题必须有选项，且至少有一个正确答案
                if (CollectionUtils.isEmpty(options) || options.size() < 2)
                {
                    return false;
                }
                long multiCorrectCount = options.stream().filter(opt -> Boolean.TRUE.equals(opt.getIsCorrect())).count();
                return multiCorrectCount >= 1;
                
            case "judge":
                // 判断题必须有2个选项，且只能有一个正确答案
                if (CollectionUtils.isEmpty(options) || options.size() != 2)
                {
                    return false;
                }
                long judgeCorrectCount = options.stream().filter(opt -> Boolean.TRUE.equals(opt.getIsCorrect())).count();
                return judgeCorrectCount == 1;
                
            case "fill":
            case "essay":
                // 填空题和简答题不需要选项验证
                return true;
                
            default:
                return false;
        }
    }

    /**
     * 生成标准正确答案格式
     * 
     * @param questionType 题型
     * @param options 选项列表
     * @param textAnswer 文本答案（用于填空题和简答题）
     * @return 格式化的正确答案
     */
    public static String generateCorrectAnswer(String questionType, List<QuestionOption> options, String textAnswer)
    {
        if (StringUtils.isEmpty(questionType))
        {
            return "";
        }

        switch (questionType)
        {
            case "single":
            case "judge":
                // 单选题和判断题返回单个正确选项
                if (!CollectionUtils.isEmpty(options))
                {
                    return options.stream()
                            .filter(opt -> Boolean.TRUE.equals(opt.getIsCorrect()))
                            .map(QuestionOption::getKey)
                            .findFirst()
                            .orElse("");
                }
                break;
                
            case "multiple":
                // 多选题返回多个正确选项，用逗号分隔
                if (!CollectionUtils.isEmpty(options))
                {
                    return options.stream()
                            .filter(opt -> Boolean.TRUE.equals(opt.getIsCorrect()))
                            .map(QuestionOption::getKey)
                            .collect(Collectors.joining(","));
                }
                break;
                
            case "fill":
            case "essay":
                // 填空题和简答题返回文本答案
                return StringUtils.isEmpty(textAnswer) ? "" : textAnswer.trim();
                
            default:
                break;
        }
        
        return "";
    }

    /**
     * 选项列表转JSON字符串
     * 
     * @param options 选项列表
     * @return JSON字符串
     */
    public static String optionsToJson(List<QuestionOption> options)
    {
        if (CollectionUtils.isEmpty(options))
        {
            return "[]";
        }
        
        try
        {
            return objectMapper.writeValueAsString(options);
        }
        catch (JsonProcessingException e)
        {
            return "[]";
        }
    }

    /**
     * JSON字符串转选项列表
     * 
     * @param jsonString JSON字符串
     * @return 选项列表
     */
    public static List<QuestionOption> jsonToOptions(String jsonString)
    {
        if (StringUtils.isEmpty(jsonString) || "[]".equals(jsonString))
        {
            return new ArrayList<>();
        }
        
        try
        {
            return objectMapper.readValue(jsonString, new TypeReference<List<QuestionOption>>(){});
        }
        catch (JsonProcessingException e)
        {
            // 兼容旧格式，尝试解析旧的选项格式
            return parseOldFormatOptions(jsonString);
        }
    }

    /**
     * 解析旧格式选项（兼容性处理）
     * 
     * @param jsonString 旧格式JSON字符串
     * @return 选项列表
     */
    private static List<QuestionOption> parseOldFormatOptions(String jsonString)
    {
        List<QuestionOption> options = new ArrayList<>();
        
        try
        {
            // 尝试解析旧格式：[{"key": "A", "value": "选项内容"}]
            List<Object> oldOptions = objectMapper.readValue(jsonString, new TypeReference<List<Object>>(){});
            
            for (Object option : oldOptions)
            {
                if (option instanceof java.util.Map)
                {
                    @SuppressWarnings("unchecked")
                    java.util.Map<String, Object> optMap = (java.util.Map<String, Object>) option;
                    
                    String key = String.valueOf(optMap.get("key"));
                    String value = String.valueOf(optMap.get("value"));
                    
                    options.add(new QuestionOption(key, value, false));
                }
            }
        }
        catch (Exception e)
        {
            // 如果解析失败，返回空列表
            return new ArrayList<>();
        }
        
        return options;
    }

    /**
     * 检查答案是否正确
     * 
     * @param questionType 题型
     * @param correctAnswer 正确答案
     * @param userAnswer 用户答案
     * @return 是否正确
     */
    public static boolean checkAnswer(String questionType, String correctAnswer, String userAnswer)
    {
        if (StringUtils.isEmpty(correctAnswer) || StringUtils.isEmpty(userAnswer))
        {
            return false;
        }

        switch (questionType)
        {
            case "single":
            case "judge":
                // 单选题和判断题精确匹配
                return correctAnswer.equals(userAnswer);
                
            case "multiple":
                // 多选题按选项排序后比较
                String[] correctOptions = correctAnswer.split(",");
                String[] userOptions = userAnswer.split(",");
                
                Arrays.sort(correctOptions);
                Arrays.sort(userOptions);
                
                return Arrays.equals(correctOptions, userOptions);
                
            case "fill":
                // 填空题去除首尾空格后比较（可以扩展为更智能的匹配）
                return correctAnswer.trim().equals(userAnswer.trim());
                
            case "essay":
                // 简答题需要人工评阅，这里只做基本匹配
                return correctAnswer.trim().equals(userAnswer.trim());
                
            default:
                return false;
        }
    }

    /**
     * 对试题选项进行乱序处理
     * 
     * @param question 试题对象
     * @return 处理后的试题对象
     */
    public static ExamQuestion shuffleQuestionOptions(ExamQuestion question)
    {
        if (question == null || StringUtils.isEmpty(question.getQuestionType()))
        {
            return question;
        }
        
        String questionType = question.getQuestionType();
        
        // 只对选择题进行选项乱序
        if (!"single".equals(questionType) && !"multiple".equals(questionType) && !"judge".equals(questionType))
        {
            return question;
        }
        
        List<QuestionOption> options = jsonToOptions(question.getQuestionOptions());
        if (CollectionUtils.isEmpty(options) || options.size() <= 1)
        {
            return question;
        }
        
        // 创建选项副本并乱序
        List<QuestionOption> shuffledOptions = new ArrayList<>(options);
        Collections.shuffle(shuffledOptions);
        
        // 重新分配选项key并更新正确答案
        Map<String, String> keyMapping = new HashMap<>();
        
        if ("judge".equals(questionType))
        {
            // 判断题特殊处理：保持选项的语义对应关系，不进行key重新分配
            // 因为判断题的key("true"/"false")与语义("正确"/"错误")是固定对应的
            for (QuestionOption option : shuffledOptions)
            {
                String oldKey = option.getKey();
                keyMapping.put(oldKey, oldKey); // 保持原有的key不变
            }
        }
        else
        {
            // 单选题、多选题：重新分配A、B、C、D等
            String[] newKeys = {"A", "B", "C", "D", "E", "F", "G", "H"};
            for (int i = 0; i < shuffledOptions.size() && i < newKeys.length; i++)
            {
                QuestionOption option = shuffledOptions.get(i);
                String oldKey = option.getKey();
                String newKey = newKeys[i];
                option.setKey(newKey);
                keyMapping.put(oldKey, newKey);
            }
        }
        
        // 更新正确答案
        String newCorrectAnswer = updateCorrectAnswerAfterShuffle(question.getCorrectAnswer(), keyMapping);
        question.setCorrectAnswer(newCorrectAnswer);
        
        // 更新选项
        question.setQuestionOptions(optionsToJson(shuffledOptions));
        question.setOptionList(shuffledOptions);
        
        return question;
    }
    
    /**
     * 根据key映射更新正确答案
     * 
     * @param correctAnswer 原正确答案
     * @param keyMapping key映射关系
     * @return 更新后的正确答案
     */
    private static String updateCorrectAnswerAfterShuffle(String correctAnswer, Map<String, String> keyMapping)
    {
        if (StringUtils.isEmpty(correctAnswer) || keyMapping.isEmpty())
        {
            return correctAnswer;
        }
        
        // 处理多选题的逗号分隔答案
        if (correctAnswer.contains(","))
        {
            String[] answers = correctAnswer.split(",");
            List<String> newAnswers = new ArrayList<>();
            
            for (String answer : answers)
            {
                String trimmedAnswer = answer.trim();
                String newAnswer = keyMapping.getOrDefault(trimmedAnswer, trimmedAnswer);
                newAnswers.add(newAnswer);
            }
            
            // 排序确保答案的一致性
            Collections.sort(newAnswers);
            return String.join(",", newAnswers);
        }
        else
        {
            // 单选题或判断题
            return keyMapping.getOrDefault(correctAnswer, correctAnswer);
        }
    }
} 