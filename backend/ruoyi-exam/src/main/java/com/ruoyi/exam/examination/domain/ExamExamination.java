package com.ruoyi.exam.examination.domain;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
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
 * 考试表 exam_examination
 * 
 * @author ruoyi
 */
public class ExamExamination extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 考试ID */
    @Excel(name = "考试序号", type = Type.EXPORT, cellType = ColumnType.NUMERIC, prompt = "考试编号")
    private Long id;

    /** 考试名称 */
    @Excel(name = "考试名称", type = Type.ALL)
    private String examName;

    /** 考试分类ID */
    @Excel(name = "分类编号", type = Type.IMPORT, cellType = ColumnType.NUMERIC)
    private Long categoryId;

    /** 分类名称 */
    @Excel(name = "分类名称", type = Type.EXPORT)
    private String categoryName;

    /** 试卷ID */
    @Excel(name = "试卷编号", type = Type.IMPORT, cellType = ColumnType.NUMERIC)
    private Long paperId;

    /** 试卷名称 */
    @Excel(name = "试卷名称", type = Type.EXPORT)
    private String paperName;

    /** 考试描述 */
    @Excel(name = "考试描述", type = Type.ALL)
    private String examDescription;

    /** 开始时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "开始时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss", type = Type.ALL)
    private Date startTime;

    /** 结束时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "结束时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss", type = Type.ALL)
    private Date endTime;

    /** 考试时长（分钟） */
    @Excel(name = "考试时长", type = Type.ALL, cellType = ColumnType.NUMERIC)
    private Integer duration;

    /** 最大考试次数 */
    @Excel(name = "最大考试次数", type = Type.ALL, cellType = ColumnType.NUMERIC)
    private Integer maxAttempts;

    /** 及格分数 */
    @Excel(name = "及格分数", type = Type.ALL, cellType = ColumnType.NUMERIC)
    private BigDecimal passScore;

    /** 总分 */
    @Excel(name = "总分", type = Type.ALL, cellType = ColumnType.NUMERIC)
    private BigDecimal totalScore;

    /** 关联试卷总分 */
    private BigDecimal paperTotalScore;

    /** 考试类型（normal:普通考试 practice:练习考试） */
    @Excel(name = "考试类型", readConverterExp = "normal=普通考试,practice=练习考试", type = Type.ALL)
    private String examType;

    /** 考试模式（online:在线考试 offline:线下考试） */
    @Excel(name = "考试模式", readConverterExp = "online=在线考试,offline=线下考试", type = Type.ALL)
    private String examMode;

    /** 是否需要报名（0否 1是） */
    @Excel(name = "是否需要报名", readConverterExp = "0=否,1=是", type = Type.ALL)
    private String registrationRequired;

    /** 报名开始时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "报名开始时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss", type = Type.ALL)
    private Date registrationStartTime;

    /** 报名结束时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "报名结束时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss", type = Type.ALL)
    private Date registrationEndTime;

    /** 是否自动评分（0否 1是） */
    @Excel(name = "是否自动评分", readConverterExp = "0=否,1=是", type = Type.ALL)
    private String autoGrade;

    /** 成绩发布方式（auto:自动发布 manual:手动发布） */
    @Excel(name = "成绩发布方式", readConverterExp = "auto=自动发布,manual=手动发布", type = Type.ALL)
    private String resultPublishType;

    /** 成绩发布时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "成绩发布时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss", type = Type.ALL)
    private Date resultPublishTime;

    /** 是否允许查看试卷（0否 1是） */
    @Excel(name = "是否允许查看试卷", readConverterExp = "0=否,1=是", type = Type.ALL)
    private String allowReview;

    /** 是否打乱题目顺序（0否 1是） */
    @Excel(name = "是否打乱题目顺序", readConverterExp = "0=否,1=是", type = Type.ALL)
    private String shuffleQuestions;

    /** 是否打乱选项顺序（0否 1是） */
    @Excel(name = "是否打乱选项顺序", readConverterExp = "0=否,1=是", type = Type.ALL)
    private String shuffleOptions;

    /** 监控模式（normal:普通 strict:严格） */
    @Excel(name = "监控模式", readConverterExp = "normal=普通,strict=严格", type = Type.ALL)
    private String monitorMode;

    /** 是否开启防作弊（0否 1是） */
    @Excel(name = "是否开启防作弊", readConverterExp = "0=否,1=是", type = Type.ALL)
    private String antiCheat;

    /** 证书模板ID */
    @Excel(name = "证书模板ID", cellType = ColumnType.NUMERIC)
    private Long certificateTemplateId;

    /** 考试状态（0:草稿 1:已发布 2:进行中 3:已结束 4:已取消） */
    @Excel(name = "考试状态", readConverterExp = "0=草稿,1=已发布,2=进行中,3=已结束,4=已取消", type = Type.ALL)
    private String status;

    /** 创建人ID */
    @Excel(name = "创建人ID", cellType = ColumnType.NUMERIC)
    private Long creatorId;

    /** 创建人姓名 */
    @Excel(name = "创建人", type = Type.EXPORT)
    private String creatorName;

    /** 考试人员列表 */
    private List<ExamUser> examUsers;

    /** 考试人员ID列表 */
    private List<Long> examUserIds;

    /** 考试授权部门ID列表 */
    private List<Long> examDeptIds;

    /** 授权部门是否包含子部门（0否 1是） */
    private String includeChildDept;

    /** 报名人数 */
    @Excel(name = "报名人数", type = Type.EXPORT, cellType = ColumnType.NUMERIC)
    private Integer registeredCount;

    /** 已提交人数 */
    @Excel(name = "已提交人数", type = Type.EXPORT, cellType = ColumnType.NUMERIC)
    private Integer submittedCount;

    /** 通过人数 */
    @Excel(name = "通过人数", type = Type.EXPORT, cellType = ColumnType.NUMERIC)
    private Integer passedCount;

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    @NotBlank(message = "考试名称不能为空")
    @Size(min = 0, max = 100, message = "考试名称长度不能超过100个字符")
    public String getExamName()
    {
        return examName;
    }

    public void setExamName(String examName)
    {
        this.examName = examName;
    }

    @NotNull(message = "考试分类不能为空")
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

    @NotNull(message = "试卷不能为空")
    public Long getPaperId()
    {
        return paperId;
    }

    public void setPaperId(Long paperId)
    {
        this.paperId = paperId;
    }

    public String getPaperName()
    {
        return paperName;
    }

    public void setPaperName(String paperName)
    {
        this.paperName = paperName;
    }

    public String getExamDescription()
    {
        return examDescription;
    }

    public void setExamDescription(String examDescription)
    {
        this.examDescription = examDescription;
    }

    @NotNull(message = "开始时间不能为空")
    public Date getStartTime()
    {
        return startTime;
    }

    public void setStartTime(Date startTime)
    {
        this.startTime = startTime;
    }

    @NotNull(message = "结束时间不能为空")
    public Date getEndTime()
    {
        return endTime;
    }

    public void setEndTime(Date endTime)
    {
        this.endTime = endTime;
    }

    public Integer getDuration()
    {
        return duration;
    }

    public void setDuration(Integer duration)
    {
        this.duration = duration;
    }

    public Integer getMaxAttempts()
    {
        return maxAttempts;
    }

    public void setMaxAttempts(Integer maxAttempts)
    {
        this.maxAttempts = maxAttempts;
    }

    public BigDecimal getPassScore()
    {
        return passScore;
    }

    public void setPassScore(BigDecimal passScore)
    {
        this.passScore = passScore;
    }

    public BigDecimal getTotalScore()
    {
        return totalScore;
    }

    public void setTotalScore(BigDecimal totalScore)
    {
        this.totalScore = totalScore;
    }

    public BigDecimal getPaperTotalScore()
    {
        return paperTotalScore;
    }

    public void setPaperTotalScore(BigDecimal paperTotalScore)
    {
        this.paperTotalScore = paperTotalScore;
    }

    public String getExamType()
    {
        return examType;
    }

    public void setExamType(String examType)
    {
        this.examType = examType;
    }

    public String getExamMode()
    {
        return examMode;
    }

    public void setExamMode(String examMode)
    {
        this.examMode = examMode;
    }

    public String getRegistrationRequired()
    {
        return registrationRequired;
    }

    public void setRegistrationRequired(String registrationRequired)
    {
        this.registrationRequired = registrationRequired;
    }

    public Date getRegistrationStartTime()
    {
        return registrationStartTime;
    }

    public void setRegistrationStartTime(Date registrationStartTime)
    {
        this.registrationStartTime = registrationStartTime;
    }

    public Date getRegistrationEndTime()
    {
        return registrationEndTime;
    }

    public void setRegistrationEndTime(Date registrationEndTime)
    {
        this.registrationEndTime = registrationEndTime;
    }

    public String getAutoGrade()
    {
        return autoGrade;
    }

    public void setAutoGrade(String autoGrade)
    {
        this.autoGrade = autoGrade;
    }

    public String getResultPublishType()
    {
        return resultPublishType;
    }

    public void setResultPublishType(String resultPublishType)
    {
        this.resultPublishType = resultPublishType;
    }

    public Date getResultPublishTime()
    {
        return resultPublishTime;
    }

    public void setResultPublishTime(Date resultPublishTime)
    {
        this.resultPublishTime = resultPublishTime;
    }

    public String getAllowReview()
    {
        return allowReview;
    }

    public void setAllowReview(String allowReview)
    {
        this.allowReview = allowReview;
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

    public String getMonitorMode()
    {
        return monitorMode;
    }

    public void setMonitorMode(String monitorMode)
    {
        this.monitorMode = monitorMode;
    }

    public String getAntiCheat()
    {
        return antiCheat;
    }

    public void setAntiCheat(String antiCheat)
    {
        this.antiCheat = antiCheat;
    }

    public Long getCertificateTemplateId()
    {
        return certificateTemplateId;
    }

    public void setCertificateTemplateId(Long certificateTemplateId)
    {
        this.certificateTemplateId = certificateTemplateId;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public Long getCreatorId()
    {
        return creatorId;
    }

    public void setCreatorId(Long creatorId)
    {
        this.creatorId = creatorId;
    }

    public String getCreatorName()
    {
        return creatorName;
    }

    public void setCreatorName(String creatorName)
    {
        this.creatorName = creatorName;
    }

    public List<ExamUser> getExamUsers()
    {
        return examUsers;
    }

    public void setExamUsers(List<ExamUser> examUsers)
    {
        this.examUsers = examUsers;
    }

    public List<Long> getExamUserIds()
    {
        return examUserIds;
    }

    public void setExamUserIds(List<Long> examUserIds)
    {
        this.examUserIds = examUserIds;
    }

    public List<Long> getExamDeptIds()
    {
        return examDeptIds;
    }

    public void setExamDeptIds(List<Long> examDeptIds)
    {
        this.examDeptIds = examDeptIds;
    }

    public String getIncludeChildDept()
    {
        return includeChildDept;
    }

    public void setIncludeChildDept(String includeChildDept)
    {
        this.includeChildDept = includeChildDept;
    }

    public Integer getRegisteredCount()
    {
        return registeredCount;
    }

    public void setRegisteredCount(Integer registeredCount)
    {
        this.registeredCount = registeredCount;
    }

    public Integer getSubmittedCount()
    {
        return submittedCount;
    }

    public void setSubmittedCount(Integer submittedCount)
    {
        this.submittedCount = submittedCount;
    }

    public Integer getPassedCount()
    {
        return passedCount;
    }

    public void setPassedCount(Integer passedCount)
    {
        this.passedCount = passedCount;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("examName", getExamName())
            .append("categoryId", getCategoryId())
            .append("paperId", getPaperId())
            .append("examDescription", getExamDescription())
            .append("startTime", getStartTime())
            .append("endTime", getEndTime())
            .append("duration", getDuration())
            .append("maxAttempts", getMaxAttempts())
            .append("passScore", getPassScore())
            .append("totalScore", getTotalScore())
            .append("paperTotalScore", getPaperTotalScore())
            .append("examType", getExamType())
            .append("examMode", getExamMode())
            .append("registrationRequired", getRegistrationRequired())
            .append("registrationStartTime", getRegistrationStartTime())
            .append("registrationEndTime", getRegistrationEndTime())
            .append("autoGrade", getAutoGrade())
            .append("resultPublishType", getResultPublishType())
            .append("resultPublishTime", getResultPublishTime())
            .append("allowReview", getAllowReview())
            .append("shuffleQuestions", getShuffleQuestions())
            .append("shuffleOptions", getShuffleOptions())
            .append("monitorMode", getMonitorMode())
            .append("antiCheat", getAntiCheat())
            .append("certificateTemplateId", getCertificateTemplateId())
            .append("status", getStatus())
            .append("creatorId", getCreatorId())
            .append("examDeptIds", getExamDeptIds())
            .append("includeChildDept", getIncludeChildDept())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
