package com.ruoyi.exam.question.domain;

import java.math.BigDecimal;
import java.util.List;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.annotation.Excel.ColumnType;
import com.ruoyi.common.annotation.Excel.Type;
import com.ruoyi.common.core.domain.BaseEntity;
import com.ruoyi.exam.question.utils.QuestionTypeUtils;

/**
 * 试题表 exam_question
 *
 * @author ruoyi
 */
public class ExamQuestion extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 试题ID */
    @Excel(name = "试题序号", type = Type.EXPORT, cellType = ColumnType.NUMERIC, prompt = "试题编号")
    private Long id;

    /** 分类ID */
    @Excel(name = "分类编号", type = Type.IMPORT, cellType = ColumnType.NUMERIC)
    private Long categoryId;

    /** 分类名称 */
    @Excel(name = "分类名称", type = Type.EXPORT)
    private String categoryName;

    /** 题型（single:单选题 multiple:多选题 judge:判断题 fill:填空题 essay:简答题） */
    @Excel(name = "题型", readConverterExp = "single=单选题,multiple=多选题,judge=判断题,fill=填空题,essay=简答题", type = Type.ALL)
    private String questionType;

    /** 题目标题 */
    @Excel(name = "题目标题", type = Type.ALL)
    private String questionTitle;

    /** 题目内容（富文本） */
    @Excel(name = "题目内容", type = Type.ALL)
    private String questionContent;

    /** 选择题选项（JSON格式） */
    @Excel(name = "选择题选项", type = Type.ALL)
    private String questionOptions;

    /** 正确答案 */
    @Excel(name = "正确答案", type = Type.ALL)
    private String correctAnswer;

    /** 题目解析 */
    @Excel(name = "题目解析", type = Type.ALL)
    private String questionAnalysis;

    /** 难度等级（1简单 2中等 3困难） */
    @Excel(name = "难度等级", readConverterExp = "1=简单,2=中等,3=困难", type = Type.ALL, cellType = ColumnType.NUMERIC)
    private Integer difficultyLevel;

    /** 分值 */
    @Excel(name = "分值", type = Type.ALL, cellType = ColumnType.NUMERIC)
    private BigDecimal score;

    /** 标签（多个用逗号分隔） */
    @Excel(name = "标签", type = Type.ALL)
    private String tags;

    /** 状态（0正常 1停用） */
    @Excel(name = "状态", readConverterExp = "0=正常,1=停用", type = Type.ALL)
    private String status;

    /** 选项列表（用于前端展示，不存储到数据库） */
    private List<QuestionOption> optionList;

    /** 题目媒体附件（图片、视频等，不存储到试题主表） */
    private List<ExamQuestionMedia> mediaList;

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
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

    @NotBlank(message = "题型不能为空")
    public String getQuestionType()
    {
        return questionType;
    }

    public void setQuestionType(String questionType)
    {
        this.questionType = questionType;
    }

    @NotBlank(message = "题目标题不能为空")
    @Size(min = 0, max = 200, message = "题目标题长度不能超过200个字符")
    public String getQuestionTitle()
    {
        return questionTitle;
    }

    public void setQuestionTitle(String questionTitle)
    {
        this.questionTitle = questionTitle;
    }

    public String getQuestionContent()
    {
        return questionContent;
    }

    public void setQuestionContent(String questionContent)
    {
        this.questionContent = questionContent;
    }

    public String getQuestionOptions()
    {
        return questionOptions;
    }

    public void setQuestionOptions(String questionOptions)
    {
        this.questionOptions = questionOptions;
    }

    public String getCorrectAnswer()
    {
        return correctAnswer;
    }

    public void setCorrectAnswer(String correctAnswer)
    {
        this.correctAnswer = correctAnswer;
    }

    public String getQuestionAnalysis()
    {
        return questionAnalysis;
    }

    public void setQuestionAnalysis(String questionAnalysis)
    {
        this.questionAnalysis = questionAnalysis;
    }

    public Integer getDifficultyLevel()
    {
        return difficultyLevel;
    }

    public void setDifficultyLevel(Integer difficultyLevel)
    {
        this.difficultyLevel = difficultyLevel;
    }

    public BigDecimal getScore()
    {
        return score;
    }

    public void setScore(BigDecimal score)
    {
        this.score = score;
    }

    public String getTags()
    {
        return tags;
    }

    public void setTags(String tags)
    {
        this.tags = tags;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public List<QuestionOption> getOptionList()
    {
        if (optionList == null)
        {
            optionList = QuestionTypeUtils.jsonToOptions(this.questionOptions);
        }
        return optionList;
    }

    public void setOptionList(List<QuestionOption> optionList)
    {
        this.optionList = optionList;
        if (optionList != null)
        {
            this.questionOptions = QuestionTypeUtils.optionsToJson(optionList);
        }
    }

    public List<ExamQuestionMedia> getMediaList()
    {
        return mediaList;
    }

    public void setMediaList(List<ExamQuestionMedia> mediaList)
    {
        this.mediaList = mediaList;
    }

    /**
     * 验证试题选项的合理性
     *
     * @return 验证结果
     */
    public boolean validateOptions()
    {
        return QuestionTypeUtils.validateOptions(this.questionType, this.getOptionList());
    }

    /**
     * 根据题型和选项生成标准答案
     *
     * @param textAnswer 文本答案（用于填空题和简答题）
     */
    public void generateStandardAnswer(String textAnswer)
    {
        this.correctAnswer = QuestionTypeUtils.generateCorrectAnswer(this.questionType, this.getOptionList(), textAnswer);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("categoryId", getCategoryId())
            .append("categoryName", getCategoryName())
            .append("questionType", getQuestionType())
            .append("questionTitle", getQuestionTitle())
            .append("questionContent", getQuestionContent())
            .append("questionOptions", getQuestionOptions())
            .append("correctAnswer", getCorrectAnswer())
            .append("questionAnalysis", getQuestionAnalysis())
            .append("difficultyLevel", getDifficultyLevel())
            .append("score", getScore())
            .append("tags", getTags())
            .append("status", getStatus())
            .append("mediaList", getMediaList())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
