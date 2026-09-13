package com.ruoyi.exam.examination.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.annotation.Excel.ColumnType;
import com.ruoyi.common.annotation.Excel.Type;

/**
 * 成绩详情导出DTO
 * 
 * @author ruoyi
 */
public class ExamScoreDetailExportDTO {

    /** 用户名 */
    @Excel(name = "用户名", type = Type.ALL)
    private String userName;

    /** 用户昵称 */
    @Excel(name = "姓名", type = Type.ALL)
    private String nickName;

    /** 部门名称 */
    @Excel(name = "部门", type = Type.ALL)
    private String deptName;

    /** 总得分 */
    @Excel(name = "总得分", type = Type.ALL, cellType = ColumnType.NUMERIC)
    private BigDecimal totalScore;

    /** 考试状态 */
    @Excel(name = "考试状态", readConverterExp = "not_started=未开始,in_progress=进行中,submitted=已提交,graded=已评分", type = Type.ALL)
    private String examStatus;

    /** 提交时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "提交时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss", type = Type.ALL)
    private Date submitTime;

    /** 题目序号 */
    @Excel(name = "题目序号", type = Type.ALL, cellType = ColumnType.NUMERIC)
    private Integer questionNumber;

    /** 题目类型 */
    @Excel(name = "题目类型", readConverterExp = "single=单选题,multiple=多选题,judge=判断题,fill=填空题,essay=简答题", type = Type.ALL)
    private String questionType;

    /** 题目内容 */
    @Excel(name = "题目内容", type = Type.ALL)
    private String questionContent;

    /** 正确答案 */
    @Excel(name = "正确答案", type = Type.ALL)
    private String correctAnswer;

    /** 用户答案 */
    @Excel(name = "用户答案", type = Type.ALL)
    private String userAnswer;

    /** 题目分值 */
    @Excel(name = "题目分值", type = Type.ALL, cellType = ColumnType.NUMERIC)
    private BigDecimal questionScore;

    /** 得分 */
    @Excel(name = "得分", type = Type.ALL, cellType = ColumnType.NUMERIC)
    private BigDecimal userScore;

    /** 是否正确 */
    @Excel(name = "是否正确", readConverterExp = "0=错误,1=正确,2=部分正确", type = Type.ALL)
    private String isCorrect;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getDeptName() {
        return deptName;
    }

    public void setDeptName(String deptName) {
        this.deptName = deptName;
    }

    public BigDecimal getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(BigDecimal totalScore) {
        this.totalScore = totalScore;
    }

    public String getExamStatus() {
        return examStatus;
    }

    public void setExamStatus(String examStatus) {
        this.examStatus = examStatus;
    }

    public Date getSubmitTime() {
        return submitTime;
    }

    public void setSubmitTime(Date submitTime) {
        this.submitTime = submitTime;
    }

    public Integer getQuestionNumber() {
        return questionNumber;
    }

    public void setQuestionNumber(Integer questionNumber) {
        this.questionNumber = questionNumber;
    }

    public String getQuestionType() {
        return questionType;
    }

    public void setQuestionType(String questionType) {
        this.questionType = questionType;
    }

    public String getQuestionContent() {
        return questionContent;
    }

    public void setQuestionContent(String questionContent) {
        this.questionContent = questionContent;
    }

    public String getCorrectAnswer() {
        return correctAnswer;
    }

    public void setCorrectAnswer(String correctAnswer) {
        this.correctAnswer = correctAnswer;
    }

    public String getUserAnswer() {
        return userAnswer;
    }

    public void setUserAnswer(String userAnswer) {
        this.userAnswer = userAnswer;
    }

    public BigDecimal getQuestionScore() {
        return questionScore;
    }

    public void setQuestionScore(BigDecimal questionScore) {
        this.questionScore = questionScore;
    }

    public BigDecimal getUserScore() {
        return userScore;
    }

    public void setUserScore(BigDecimal userScore) {
        this.userScore = userScore;
    }

    public String getIsCorrect() {
        return isCorrect;
    }

    public void setIsCorrect(String isCorrect) {
        this.isCorrect = isCorrect;
    }
}