package com.ruoyi.exam.examination.domain;

import java.math.BigDecimal;
import java.util.Date;
import javax.validation.constraints.NotNull;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.annotation.Excel.ColumnType;
import com.ruoyi.common.annotation.Excel.Type;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 答题记录表 exam_answer_record
 * 
 * @author ruoyi
 */
public class ExamAnswerRecord extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 记录ID */
    private Long id;

    /** 考试ID */
    @Excel(name = "考试ID", type = Type.IMPORT, cellType = ColumnType.NUMERIC)
    private Long examId;

    /** 考试用户关联ID */
    @Excel(name = "考试用户关联ID", type = Type.IMPORT, cellType = ColumnType.NUMERIC)
    private Long examUserId;

    /** 用户ID */
    @Excel(name = "用户ID", type = Type.IMPORT, cellType = ColumnType.NUMERIC)
    private Long userId;

    /** 试卷ID */
    @Excel(name = "试卷ID", type = Type.IMPORT, cellType = ColumnType.NUMERIC)
    private Long paperId;

    /** 试题ID */
    @Excel(name = "试题ID", type = Type.IMPORT, cellType = ColumnType.NUMERIC)
    private Long questionId;

    /** 用户答案 */
    @Excel(name = "用户答案", type = Type.ALL)
    private String userAnswer;

    /** 正确答案 */
    @Excel(name = "正确答案", type = Type.ALL)
    private String correctAnswer;

    /** 题目分值 */
    @Excel(name = "题目分值", type = Type.ALL, cellType = ColumnType.NUMERIC)
    private BigDecimal questionScore;

    /** 得分 */
    @Excel(name = "得分", type = Type.ALL, cellType = ColumnType.NUMERIC)
    private BigDecimal userScore;

    /** 是否正确（0否 1是 2部分正确） */
    @Excel(name = "是否正确", readConverterExp = "0=否,1=是,2=部分正确", type = Type.ALL)
    private String isCorrect;

    /** 是否已评分（0否 1是） */
    @Excel(name = "是否已评分", readConverterExp = "0=否,1=是", type = Type.ALL)
    private String isGraded;

    /** 阅卷人ID */
    @Excel(name = "阅卷人ID", cellType = ColumnType.NUMERIC)
    private Long graderId;

    /** 评分时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "评分时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss", type = Type.ALL)
    private Date gradeTime;

    /** 评分备注 */
    @Excel(name = "评分备注", type = Type.ALL)
    private String gradeComment;

    /** 评分人员 */
    @Excel(name = "评分人员", type = Type.ALL)
    private String gradeBy;

    /** 答题时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "答题时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss", type = Type.ALL)
    private Date answerTime;

    /** 用时（秒） */
    @Excel(name = "用时", type = Type.ALL, cellType = ColumnType.NUMERIC)
    private Integer timeSpent;

    /** 版本号，用于乐观锁控制 */
    private Integer version;

    /** 试题详情（关联查询时使用） */
    private com.ruoyi.exam.question.domain.ExamQuestion question;

    /** 题目内容（用于导出） */
    @Excel(name = "题目内容", type = Type.EXPORT)
    private String questionContent;

    /** 题目类型（用于导出） */
    @Excel(name = "题目类型", readConverterExp = "single=单选题,multiple=多选题,judge=判断题,fill=填空题,essay=简答题", type = Type.EXPORT)
    private String questionType;

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    @NotNull(message = "考试ID不能为空")
    public Long getExamId()
    {
        return examId;
    }

    public void setExamId(Long examId)
    {
        this.examId = examId;
    }

    @NotNull(message = "考试用户关联ID不能为空")
    public Long getExamUserId()
    {
        return examUserId;
    }

    public void setExamUserId(Long examUserId)
    {
        this.examUserId = examUserId;
    }

    @NotNull(message = "用户ID不能为空")
    public Long getUserId()
    {
        return userId;
    }

    public void setUserId(Long userId)
    {
        this.userId = userId;
    }

    @NotNull(message = "试卷ID不能为空")
    public Long getPaperId()
    {
        return paperId;
    }

    public void setPaperId(Long paperId)
    {
        this.paperId = paperId;
    }

    @NotNull(message = "试题ID不能为空")
    public Long getQuestionId()
    {
        return questionId;
    }

    public void setQuestionId(Long questionId)
    {
        this.questionId = questionId;
    }

    public String getUserAnswer()
    {
        return userAnswer;
    }

    public void setUserAnswer(String userAnswer)
    {
        this.userAnswer = userAnswer;
    }

    public String getCorrectAnswer()
    {
        return correctAnswer;
    }

    public void setCorrectAnswer(String correctAnswer)
    {
        this.correctAnswer = correctAnswer;
    }

    public BigDecimal getQuestionScore()
    {
        return questionScore;
    }

    public void setQuestionScore(BigDecimal questionScore)
    {
        this.questionScore = questionScore;
    }

    public BigDecimal getUserScore()
    {
        return userScore;
    }

    public void setUserScore(BigDecimal userScore)
    {
        this.userScore = userScore;
    }

    public String getIsCorrect()
    {
        return isCorrect;
    }

    public void setIsCorrect(String isCorrect)
    {
        this.isCorrect = isCorrect;
    }

    public String getIsGraded()
    {
        return isGraded;
    }

    public void setIsGraded(String isGraded)
    {
        this.isGraded = isGraded;
    }

    public Long getGraderId()
    {
        return graderId;
    }

    public void setGraderId(Long graderId)
    {
        this.graderId = graderId;
    }

    public Date getGradeTime()
    {
        return gradeTime;
    }

    public void setGradeTime(Date gradeTime)
    {
        this.gradeTime = gradeTime;
    }

    public String getGradeComment()
    {
        return gradeComment;
    }

    public void setGradeComment(String gradeComment)
    {
        this.gradeComment = gradeComment;
    }

    public String getGradeBy()
    {
        return gradeBy;
    }

    public void setGradeBy(String gradeBy)
    {
        this.gradeBy = gradeBy;
    }

    public Date getAnswerTime()
    {
        return answerTime;
    }

    public void setAnswerTime(Date answerTime)
    {
        this.answerTime = answerTime;
    }

    public Integer getTimeSpent()
    {
        return timeSpent;
    }

    public void setTimeSpent(Integer timeSpent)
    {
        this.timeSpent = timeSpent;
    }

    public Integer getVersion()
    {
        return version;
    }

    public void setVersion(Integer version)
    {
        this.version = version;
    }

    public com.ruoyi.exam.question.domain.ExamQuestion getQuestion()
    {
        return question;
    }

    public void setQuestion(com.ruoyi.exam.question.domain.ExamQuestion question)
    {
        this.question = question;
    }

    public String getQuestionContent()
    {
        return questionContent;
    }

    public void setQuestionContent(String questionContent)
    {
        this.questionContent = questionContent;
    }

    public String getQuestionType()
    {
        return questionType;
    }

    public void setQuestionType(String questionType)
    {
        this.questionType = questionType;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("examId", getExamId())
            .append("examUserId", getExamUserId())
            .append("userId", getUserId())
            .append("paperId", getPaperId())
            .append("questionId", getQuestionId())
            .append("userAnswer", getUserAnswer())
            .append("correctAnswer", getCorrectAnswer())
            .append("questionScore", getQuestionScore())
            .append("userScore", getUserScore())
            .append("isCorrect", getIsCorrect())
            .append("isGraded", getIsGraded())
            .append("graderId", getGraderId())
            .append("gradeTime", getGradeTime())
            .append("gradeComment", getGradeComment())
            .append("gradeBy", getGradeBy())
            .append("answerTime", getAnswerTime())
            .append("timeSpent", getTimeSpent())
            .append("createTime", getCreateTime())
            .append("updateTime", getUpdateTime())
            .toString();
    }
} 