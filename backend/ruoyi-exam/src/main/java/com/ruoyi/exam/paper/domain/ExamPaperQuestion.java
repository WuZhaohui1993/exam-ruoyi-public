package com.ruoyi.exam.paper.domain;

import java.math.BigDecimal;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.annotation.Excel.ColumnType;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 试卷试题关联表 exam_paper_question
 * 
 * @author ruoyi
 */
public class ExamPaperQuestion extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 关联ID */
    private Long id;

    /** 试卷ID */
    @Excel(name = "试卷ID", cellType = ColumnType.NUMERIC)
    private Long paperId;

    /** 试题ID */
    @Excel(name = "试题ID", cellType = ColumnType.NUMERIC)
    private Long questionId;

    /** 题目序号 */
    @Excel(name = "题目序号", cellType = ColumnType.NUMERIC)
    private Integer questionOrder;

    /** 题目分值 */
    @Excel(name = "题目分值", cellType = ColumnType.NUMERIC)
    private BigDecimal score;

    /** 试题信息（关联查询） */
    private com.ruoyi.exam.question.domain.ExamQuestion question;

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public Long getPaperId()
    {
        return paperId;
    }

    public void setPaperId(Long paperId)
    {
        this.paperId = paperId;
    }

    public Long getQuestionId()
    {
        return questionId;
    }

    public void setQuestionId(Long questionId)
    {
        this.questionId = questionId;
    }

    public Integer getQuestionOrder()
    {
        return questionOrder;
    }

    public void setQuestionOrder(Integer questionOrder)
    {
        this.questionOrder = questionOrder;
    }

    public BigDecimal getScore()
    {
        return score;
    }

    public void setScore(BigDecimal score)
    {
        this.score = score;
    }

    public com.ruoyi.exam.question.domain.ExamQuestion getQuestion()
    {
        return question;
    }

    public void setQuestion(com.ruoyi.exam.question.domain.ExamQuestion question)
    {
        this.question = question;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("paperId", getPaperId())
            .append("questionId", getQuestionId())
            .append("questionOrder", getQuestionOrder())
            .append("score", getScore())
            .append("createTime", getCreateTime())
            .toString();
    }
} 