package com.ruoyi.exam.question.utils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.ruoyi.exam.question.domain.ExamQuestion;
import com.ruoyi.exam.question.domain.ExamQuestionImportTemplate.*;
import com.ruoyi.exam.question.domain.QuestionOption;
import com.ruoyi.exam.question.service.IExamQuestionCategoryService;

/**
 * 题目导入模板转换工具类
 * 
 * @author ruoyi
 */
public class ImportTemplateConverter {

    /**
     * 将单选题模板转换为ExamQuestion
     */
    public static ExamQuestion convertSingleChoice(SingleChoiceTemplate template, IExamQuestionCategoryService categoryService) {
        ExamQuestion question = new ExamQuestion();
        question.setQuestionType("single");
        
        // 设置基本信息
        setBasicInfo(question, template.getCategoryName(), template.getQuestionTitle(), 
                    template.getQuestionContent(), template.getQuestionAnalysis(), 
                    template.getDifficultyLevel(), template.getScore(), 
                    template.getTags(), categoryService);
        
        // 设置选项
        List<QuestionOption> options = new ArrayList<>();
        if (StringUtils.isNotEmpty(template.getOptionA())) {
            options.add(new QuestionOption("A", template.getOptionA(), 
                template.getCorrectAnswer().equals("A")));
        }
        if (StringUtils.isNotEmpty(template.getOptionB())) {
            options.add(new QuestionOption("B", template.getOptionB(), 
                template.getCorrectAnswer().equals("B")));
        }
        if (StringUtils.isNotEmpty(template.getOptionC())) {
            options.add(new QuestionOption("C", template.getOptionC(), 
                template.getCorrectAnswer().equals("C")));
        }
        if (StringUtils.isNotEmpty(template.getOptionD())) {
            options.add(new QuestionOption("D", template.getOptionD(), 
                template.getCorrectAnswer().equals("D")));
        }
        
        question.setOptionList(options);
        question.setCorrectAnswer(template.getCorrectAnswer());
        
        return question;
    }

    /**
     * 将多选题模板转换为ExamQuestion
     */
    public static ExamQuestion convertMultipleChoice(MultipleChoiceTemplate template, IExamQuestionCategoryService categoryService) {
        ExamQuestion question = new ExamQuestion();
        question.setQuestionType("multiple");
        
        // 设置基本信息
        setBasicInfo(question, template.getCategoryName(), template.getQuestionTitle(), 
                    template.getQuestionContent(), template.getQuestionAnalysis(), 
                    template.getDifficultyLevel(), template.getScore(), 
                    template.getTags(), categoryService);
        
        // 设置选项
        List<QuestionOption> options = new ArrayList<>();
        String[] correctAnswers = template.getCorrectAnswer().split(",");
        List<String> correctList = new ArrayList<>();
        for (String answer : correctAnswers) {
            correctList.add(answer.trim());
        }
        
        if (StringUtils.isNotEmpty(template.getOptionA())) {
            options.add(new QuestionOption("A", template.getOptionA(), 
                correctList.contains("A")));
        }
        if (StringUtils.isNotEmpty(template.getOptionB())) {
            options.add(new QuestionOption("B", template.getOptionB(), 
                correctList.contains("B")));
        }
        if (StringUtils.isNotEmpty(template.getOptionC())) {
            options.add(new QuestionOption("C", template.getOptionC(), 
                correctList.contains("C")));
        }
        if (StringUtils.isNotEmpty(template.getOptionD())) {
            options.add(new QuestionOption("D", template.getOptionD(), 
                correctList.contains("D")));
        }
        if (StringUtils.isNotEmpty(template.getOptionE())) {
            options.add(new QuestionOption("E", template.getOptionE(), 
                correctList.contains("E")));
        }
        if (StringUtils.isNotEmpty(template.getOptionF())) {
            options.add(new QuestionOption("F", template.getOptionF(), 
                correctList.contains("F")));
        }
        
        question.setOptionList(options);
        question.setCorrectAnswer(template.getCorrectAnswer());
        
        return question;
    }

    /**
     * 将判断题模板转换为ExamQuestion
     */
    public static ExamQuestion convertJudge(JudgeTemplate template, IExamQuestionCategoryService categoryService) {
        ExamQuestion question = new ExamQuestion();
        question.setQuestionType("judge");
        
        // 设置基本信息
        setBasicInfo(question, template.getCategoryName(), template.getQuestionTitle(), 
                    template.getQuestionContent(), template.getQuestionAnalysis(), 
                    template.getDifficultyLevel(), template.getScore(), 
                    template.getTags(), categoryService);
        
        // 判断题只有对/错两个选项
        List<QuestionOption> options = new ArrayList<>();
        String correctAnswer = template.getCorrectAnswer();
        
        // 标准化答案格式
        if ("对".equals(correctAnswer) || "true".equalsIgnoreCase(correctAnswer) || "T".equalsIgnoreCase(correctAnswer)) {
            correctAnswer = "A";
        } else if ("错".equals(correctAnswer) || "false".equalsIgnoreCase(correctAnswer) || "F".equalsIgnoreCase(correctAnswer)) {
            correctAnswer = "B";
        }
        
        options.add(new QuestionOption("A", "对", "A".equals(correctAnswer)));
        options.add(new QuestionOption("B", "错", "B".equals(correctAnswer)));
        
        question.setOptionList(options);
        question.setCorrectAnswer(correctAnswer);
        
        return question;
    }

    /**
     * 将填空题模板转换为ExamQuestion
     */
    public static ExamQuestion convertFill(FillTemplate template, IExamQuestionCategoryService categoryService) {
        ExamQuestion question = new ExamQuestion();
        question.setQuestionType("fill");
        
        // 设置基本信息
        setBasicInfo(question, template.getCategoryName(), template.getQuestionTitle(), 
                    template.getQuestionContent(), template.getQuestionAnalysis(), 
                    template.getDifficultyLevel(), template.getScore(), 
                    template.getTags(), categoryService);
        
        // 填空题不需要选项
        question.setCorrectAnswer(template.getCorrectAnswer());
        
        return question;
    }

    /**
     * 将简答题模板转换为ExamQuestion
     */
    public static ExamQuestion convertEssay(EssayTemplate template, IExamQuestionCategoryService categoryService) {
        ExamQuestion question = new ExamQuestion();
        question.setQuestionType("essay");
        
        // 设置基本信息
        setBasicInfo(question, template.getCategoryName(), template.getQuestionTitle(), 
                    template.getQuestionContent(), template.getQuestionAnalysis(), 
                    template.getDifficultyLevel(), template.getScore(), 
                    template.getTags(), categoryService);
        
        // 简答题不需要选项
        question.setCorrectAnswer(template.getCorrectAnswer());
        
        return question;
    }

    /**
     * 设置题目基本信息
     */
    private static void setBasicInfo(ExamQuestion question, String categoryName, String questionTitle,
                                   String questionContent, String questionAnalysis, Integer difficultyLevel,
                                   String score, String tags, IExamQuestionCategoryService categoryService) {
        
        // 根据分类名称查找分类ID
        if (StringUtils.isNotEmpty(categoryName)) {
            Long categoryId = categoryService.findCategoryIdByName(categoryName);
            if (categoryId != null) {
                question.setCategoryId(categoryId);
            } else {
                throw new RuntimeException("分类名称'" + categoryName + "'不存在，请先创建该分类");
            }
        }
        
        question.setQuestionTitle(questionTitle);
        question.setQuestionContent(questionContent);
        question.setQuestionAnalysis(questionAnalysis);
        question.setDifficultyLevel(difficultyLevel);
        
        if (StringUtils.isNotEmpty(score)) {
            try {
                question.setScore(new BigDecimal(score));
            } catch (NumberFormatException e) {
                question.setScore(new BigDecimal("5")); // 默认5分
            }
        } else {
            question.setScore(new BigDecimal("5")); // 默认5分
        }
        
        question.setTags(tags);
        question.setStatus("0"); // 默认启用状态
    }
}