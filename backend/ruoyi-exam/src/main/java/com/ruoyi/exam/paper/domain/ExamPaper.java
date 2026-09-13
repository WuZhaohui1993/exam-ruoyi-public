package com.ruoyi.exam.paper.domain;

import java.math.BigDecimal;
import java.util.Date;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.annotation.Excel.ColumnType;
import com.ruoyi.common.annotation.Excel.Type;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 试卷表 exam_paper
 * 
 * @author ruoyi
 */
public class ExamPaper extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 试卷ID */
    @Excel(name = "试卷序号", type = Type.EXPORT, cellType = ColumnType.NUMERIC, prompt = "试卷编号")
    private Long id;

    /** 试卷名称 */
    @Excel(name = "试卷名称", type = Type.ALL)
    private String paperName;

    /** 试卷分类ID */
    @Excel(name = "分类编号", type = Type.IMPORT, cellType = ColumnType.NUMERIC)
    private Long categoryId;

    /** 分类名称 */
    @Excel(name = "分类名称", type = Type.EXPORT)
    private String categoryName;

    /** 试卷类型（fixed:固定试卷 random:随机试卷 mixed:混合试卷） */
    @Excel(name = "试卷类型", readConverterExp = "fixed=固定试卷,random=随机试卷,mixed=混合试卷", type = Type.ALL)
    private String paperType;

    /** 试卷描述 */
    @Excel(name = "试卷描述", type = Type.ALL)
    private String paperDescription;

    /** 总分值 */
    @Excel(name = "总分值", type = Type.ALL, cellType = ColumnType.NUMERIC)
    private BigDecimal totalScore;

    /** 及格分数 */
    @Excel(name = "及格分数", type = Type.ALL, cellType = ColumnType.NUMERIC)
    private BigDecimal passScore;

    /** 考试时长（分钟） */
    @Excel(name = "考试时长", type = Type.ALL, cellType = ColumnType.NUMERIC)
    private Integer duration;

    /** 题目数量 */
    @Excel(name = "题目数量", type = Type.EXPORT, cellType = ColumnType.NUMERIC)
    private Integer questionCount;

    /** 难度等级（1简单 2中等 3困难） */
    @Excel(name = "难度等级", readConverterExp = "1=简单,2=中等,3=困难", type = Type.ALL, cellType = ColumnType.NUMERIC)
    private Integer difficultyLevel;

    /** 状态（0正常 1停用） */
    @Excel(name = "状态", readConverterExp = "0=正常,1=停用", type = Type.ALL)
    private String status;

    /** 是否打乱题目顺序（0否 1是） */
    @Excel(name = "是否打乱题目顺序", readConverterExp = "0=否,1=是", type = Type.ALL)
    private String shuffleQuestions;

    /** 是否打乱选项顺序（0否 1是） */
    @Excel(name = "是否打乱选项顺序", readConverterExp = "0=否,1=是", type = Type.ALL)
    private String shuffleOptions;

    /** 题目配置（JSON格式，用于随机和混合试卷） */
    private String questionConfig;

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    @NotBlank(message = "试卷名称不能为空")
    @Size(min = 0, max = 100, message = "试卷名称长度不能超过100个字符")
    public String getPaperName()
    {
        return paperName;
    }

    public void setPaperName(String paperName)
    {
        this.paperName = paperName;
    }

    @NotNull(message = "分类ID不能为空")
    public Long getCategoryId()
    {
        return categoryId;
    }

    public void setCategoryId(Long categoryId)
    {
        this.categoryId = categoryId;
    }

    public String getCategoryName()
    {
        return categoryName;
    }

    public void setCategoryName(String categoryName)
    {
        this.categoryName = categoryName;
    }

    @NotBlank(message = "试卷类型不能为空")
    public String getPaperType()
    {
        return paperType;
    }

    public void setPaperType(String paperType)
    {
        this.paperType = paperType;
    }

    public String getPaperDescription()
    {
        return paperDescription;
    }

    public void setPaperDescription(String paperDescription)
    {
        this.paperDescription = paperDescription;
    }

    public BigDecimal getTotalScore()
    {
        return totalScore;
    }

    public void setTotalScore(BigDecimal totalScore)
    {
        this.totalScore = totalScore;
    }

    public BigDecimal getPassScore()
    {
        return passScore;
    }

    public void setPassScore(BigDecimal passScore)
    {
        this.passScore = passScore;
    }

    public Integer getDuration()
    {
        return duration;
    }

    public void setDuration(Integer duration)
    {
        this.duration = duration;
    }

    public Integer getQuestionCount()
    {
        return questionCount;
    }

    public void setQuestionCount(Integer questionCount)
    {
        this.questionCount = questionCount;
    }

    public Integer getDifficultyLevel()
    {
        return difficultyLevel;
    }

    public void setDifficultyLevel(Integer difficultyLevel)
    {
        this.difficultyLevel = difficultyLevel;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getShuffleQuestions()
    {
        return shuffleQuestions;
    }

    public void setShuffleQuestions(String shuffleQuestions)
    {
        this.shuffleQuestions = shuffleQuestions;
    }

    public String getShuffleOptions()
    {
        return shuffleOptions;
    }

    public void setShuffleOptions(String shuffleOptions)
    {
        this.shuffleOptions = shuffleOptions;
    }

    public String getQuestionConfig()
    {
        return questionConfig;
    }

    public void setQuestionConfig(String questionConfig)
    {
        this.questionConfig = questionConfig;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("paperName", getPaperName())
            .append("categoryId", getCategoryId())
            .append("categoryName", getCategoryName())
            .append("paperType", getPaperType())
            .append("paperDescription", getPaperDescription())
            .append("totalScore", getTotalScore())
            .append("passScore", getPassScore())
            .append("duration", getDuration())
            .append("questionCount", getQuestionCount())
            .append("difficultyLevel", getDifficultyLevel())
            .append("status", getStatus())
            .append("shuffleQuestions", getShuffleQuestions())
            .append("shuffleOptions", getShuffleOptions())
            .append("questionConfig", getQuestionConfig())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
} 