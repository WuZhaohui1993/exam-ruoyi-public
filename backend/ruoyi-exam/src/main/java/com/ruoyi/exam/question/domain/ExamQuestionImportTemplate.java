package com.ruoyi.exam.question.domain;

import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.annotation.Excel.ColumnType;
import com.ruoyi.common.annotation.Excel.Type;

/**
 * 题目导入模板工具类
 * 
 * @author ruoyi
 */
public class ExamQuestionImportTemplate {

    /**
     * 单选题导入模板
     */
    public static class SingleChoiceTemplate {
        @Excel(name = "*分类名称", type = Type.ALL)
        private String categoryName;

        @Excel(name = "*题目标题", type = Type.ALL)
        private String questionTitle;

        @Excel(name = "*题目内容", type = Type.ALL)
        private String questionContent;

        @Excel(name = "选项A", type = Type.ALL)
        private String optionA;

        @Excel(name = "选项B", type = Type.ALL)
        private String optionB;

        @Excel(name = "选项C", type = Type.ALL)
        private String optionC;

        @Excel(name = "选项D", type = Type.ALL)
        private String optionD;

        @Excel(name = "*正确答案", type = Type.ALL)
        private String correctAnswer;

        @Excel(name = "题目解析", type = Type.ALL)
        private String questionAnalysis;

        @Excel(name = "*难度等级", readConverterExp = "1=简单,2=中等,3=困难", type = Type.ALL, cellType = ColumnType.NUMERIC)
        private Integer difficultyLevel;

        @Excel(name = "*分值", type = Type.ALL, cellType = ColumnType.NUMERIC)
        private String score;

        @Excel(name = "标签", type = Type.ALL)
        private String tags;

        // Getters and Setters
        public String getCategoryName() { return categoryName; }
        public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
        public String getQuestionTitle() { return questionTitle; }
        public void setQuestionTitle(String questionTitle) { this.questionTitle = questionTitle; }
        public String getQuestionContent() { return questionContent; }
        public void setQuestionContent(String questionContent) { this.questionContent = questionContent; }
        public String getOptionA() { return optionA; }
        public void setOptionA(String optionA) { this.optionA = optionA; }
        public String getOptionB() { return optionB; }
        public void setOptionB(String optionB) { this.optionB = optionB; }
        public String getOptionC() { return optionC; }
        public void setOptionC(String optionC) { this.optionC = optionC; }
        public String getOptionD() { return optionD; }
        public void setOptionD(String optionD) { this.optionD = optionD; }
        public String getCorrectAnswer() { return correctAnswer; }
        public void setCorrectAnswer(String correctAnswer) { this.correctAnswer = correctAnswer; }
        public String getQuestionAnalysis() { return questionAnalysis; }
        public void setQuestionAnalysis(String questionAnalysis) { this.questionAnalysis = questionAnalysis; }
        public Integer getDifficultyLevel() { return difficultyLevel; }
        public void setDifficultyLevel(Integer difficultyLevel) { this.difficultyLevel = difficultyLevel; }
        public String getScore() { return score; }
        public void setScore(String score) { this.score = score; }
        public String getTags() { return tags; }
        public void setTags(String tags) { this.tags = tags; }
    }

    /**
     * 多选题导入模板
     */
    public static class MultipleChoiceTemplate {
        @Excel(name = "*分类名称", type = Type.ALL)
        private String categoryName;

        @Excel(name = "*题目标题", type = Type.ALL)
        private String questionTitle;

        @Excel(name = "*题目内容", type = Type.ALL)
        private String questionContent;

        @Excel(name = "选项A", type = Type.ALL)
        private String optionA;

        @Excel(name = "选项B", type = Type.ALL)
        private String optionB;

        @Excel(name = "选项C", type = Type.ALL)
        private String optionC;

        @Excel(name = "选项D", type = Type.ALL)
        private String optionD;

        @Excel(name = "选项E", type = Type.ALL)
        private String optionE;

        @Excel(name = "选项F", type = Type.ALL)
        private String optionF;

        @Excel(name = "*正确答案", type = Type.ALL)
        private String correctAnswer;

        @Excel(name = "题目解析", type = Type.ALL)
        private String questionAnalysis;

        @Excel(name = "*难度等级", readConverterExp = "1=简单,2=中等,3=困难", type = Type.ALL, cellType = ColumnType.NUMERIC)
        private Integer difficultyLevel;

        @Excel(name = "*分值", type = Type.ALL, cellType = ColumnType.NUMERIC)
        private String score;

        @Excel(name = "标签", type = Type.ALL)
        private String tags;

        // Getters and Setters
        public String getCategoryName() { return categoryName; }
        public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
        public String getQuestionTitle() { return questionTitle; }
        public void setQuestionTitle(String questionTitle) { this.questionTitle = questionTitle; }
        public String getQuestionContent() { return questionContent; }
        public void setQuestionContent(String questionContent) { this.questionContent = questionContent; }
        public String getOptionA() { return optionA; }
        public void setOptionA(String optionA) { this.optionA = optionA; }
        public String getOptionB() { return optionB; }
        public void setOptionB(String optionB) { this.optionB = optionB; }
        public String getOptionC() { return optionC; }
        public void setOptionC(String optionC) { this.optionC = optionC; }
        public String getOptionD() { return optionD; }
        public void setOptionD(String optionD) { this.optionD = optionD; }
        public String getOptionE() { return optionE; }
        public void setOptionE(String optionE) { this.optionE = optionE; }
        public String getOptionF() { return optionF; }
        public void setOptionF(String optionF) { this.optionF = optionF; }
        public String getCorrectAnswer() { return correctAnswer; }
        public void setCorrectAnswer(String correctAnswer) { this.correctAnswer = correctAnswer; }
        public String getQuestionAnalysis() { return questionAnalysis; }
        public void setQuestionAnalysis(String questionAnalysis) { this.questionAnalysis = questionAnalysis; }
        public Integer getDifficultyLevel() { return difficultyLevel; }
        public void setDifficultyLevel(Integer difficultyLevel) { this.difficultyLevel = difficultyLevel; }
        public String getScore() { return score; }
        public void setScore(String score) { this.score = score; }
        public String getTags() { return tags; }
        public void setTags(String tags) { this.tags = tags; }
    }

    /**
     * 判断题导入模板
     */
    public static class JudgeTemplate {
        @Excel(name = "*分类名称", type = Type.ALL)
        private String categoryName;

        @Excel(name = "*题目标题", type = Type.ALL)
        private String questionTitle;

        @Excel(name = "*题目内容", type = Type.ALL)
        private String questionContent;

        @Excel(name = "*正确答案", type = Type.ALL)
        private String correctAnswer;

        @Excel(name = "题目解析", type = Type.ALL)
        private String questionAnalysis;

        @Excel(name = "*难度等级", readConverterExp = "1=简单,2=中等,3=困难", type = Type.ALL, cellType = ColumnType.NUMERIC)
        private Integer difficultyLevel;

        @Excel(name = "*分值", type = Type.ALL, cellType = ColumnType.NUMERIC)
        private String score;

        @Excel(name = "标签", type = Type.ALL)
        private String tags;

        // Getters and Setters
        public String getCategoryName() { return categoryName; }
        public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
        public String getQuestionTitle() { return questionTitle; }
        public void setQuestionTitle(String questionTitle) { this.questionTitle = questionTitle; }
        public String getQuestionContent() { return questionContent; }
        public void setQuestionContent(String questionContent) { this.questionContent = questionContent; }
        public String getCorrectAnswer() { return correctAnswer; }
        public void setCorrectAnswer(String correctAnswer) { this.correctAnswer = correctAnswer; }
        public String getQuestionAnalysis() { return questionAnalysis; }
        public void setQuestionAnalysis(String questionAnalysis) { this.questionAnalysis = questionAnalysis; }
        public Integer getDifficultyLevel() { return difficultyLevel; }
        public void setDifficultyLevel(Integer difficultyLevel) { this.difficultyLevel = difficultyLevel; }
        public String getScore() { return score; }
        public void setScore(String score) { this.score = score; }
        public String getTags() { return tags; }
        public void setTags(String tags) { this.tags = tags; }
    }

    /**
     * 填空题导入模板
     */
    public static class FillTemplate {
        @Excel(name = "*分类名称", type = Type.ALL)
        private String categoryName;

        @Excel(name = "*题目标题", type = Type.ALL)
        private String questionTitle;

        @Excel(name = "*题目内容", type = Type.ALL)
        private String questionContent;

        @Excel(name = "*正确答案", type = Type.ALL)
        private String correctAnswer;

        @Excel(name = "题目解析", type = Type.ALL)
        private String questionAnalysis;

        @Excel(name = "*难度等级", readConverterExp = "1=简单,2=中等,3=困难", type = Type.ALL, cellType = ColumnType.NUMERIC)
        private Integer difficultyLevel;

        @Excel(name = "*分值", type = Type.ALL, cellType = ColumnType.NUMERIC)
        private String score;

        @Excel(name = "标签", type = Type.ALL)
        private String tags;

        // Getters and Setters
        public String getCategoryName() { return categoryName; }
        public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
        public String getQuestionTitle() { return questionTitle; }
        public void setQuestionTitle(String questionTitle) { this.questionTitle = questionTitle; }
        public String getQuestionContent() { return questionContent; }
        public void setQuestionContent(String questionContent) { this.questionContent = questionContent; }
        public String getCorrectAnswer() { return correctAnswer; }
        public void setCorrectAnswer(String correctAnswer) { this.correctAnswer = correctAnswer; }
        public String getQuestionAnalysis() { return questionAnalysis; }
        public void setQuestionAnalysis(String questionAnalysis) { this.questionAnalysis = questionAnalysis; }
        public Integer getDifficultyLevel() { return difficultyLevel; }
        public void setDifficultyLevel(Integer difficultyLevel) { this.difficultyLevel = difficultyLevel; }
        public String getScore() { return score; }
        public void setScore(String score) { this.score = score; }
        public String getTags() { return tags; }
        public void setTags(String tags) { this.tags = tags; }
    }

    /**
     * 简答题导入模板
     */
    public static class EssayTemplate {
        @Excel(name = "*分类名称", type = Type.ALL)
        private String categoryName;

        @Excel(name = "*题目标题", type = Type.ALL)
        private String questionTitle;

        @Excel(name = "*题目内容", type = Type.ALL)
        private String questionContent;

        @Excel(name = "*参考答案", type = Type.ALL)
        private String correctAnswer;

        @Excel(name = "题目解析", type = Type.ALL)
        private String questionAnalysis;

        @Excel(name = "*难度等级", readConverterExp = "1=简单,2=中等,3=困难", type = Type.ALL, cellType = ColumnType.NUMERIC)
        private Integer difficultyLevel;

        @Excel(name = "*分值", type = Type.ALL, cellType = ColumnType.NUMERIC)
        private String score;

        @Excel(name = "标签", type = Type.ALL)
        private String tags;

        // Getters and Setters
        public String getCategoryName() { return categoryName; }
        public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
        public String getQuestionTitle() { return questionTitle; }
        public void setQuestionTitle(String questionTitle) { this.questionTitle = questionTitle; }
        public String getQuestionContent() { return questionContent; }
        public void setQuestionContent(String questionContent) { this.questionContent = questionContent; }
        public String getCorrectAnswer() { return correctAnswer; }
        public void setCorrectAnswer(String correctAnswer) { this.correctAnswer = correctAnswer; }
        public String getQuestionAnalysis() { return questionAnalysis; }
        public void setQuestionAnalysis(String questionAnalysis) { this.questionAnalysis = questionAnalysis; }
        public Integer getDifficultyLevel() { return difficultyLevel; }
        public void setDifficultyLevel(Integer difficultyLevel) { this.difficultyLevel = difficultyLevel; }
        public String getScore() { return score; }
        public void setScore(String score) { this.score = score; }
        public String getTags() { return tags; }
        public void setTags(String tags) { this.tags = tags; }
    }
}