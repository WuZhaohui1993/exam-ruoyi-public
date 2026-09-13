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
import com.ruoyi.exam.paper.domain.ExamPaper;

/**
 * 考试表（优化版） exam_examination
 * 
 * @author ruoyi
 */
public class ExamExaminationOptimized extends BaseEntity
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

    // ==================== 覆盖字段（为NULL时使用试卷默认配置） ====================
    
    /** 考试时长覆盖（分钟，NULL时使用试卷配置） */
    @Excel(name = "考试时长覆盖", type = Type.ALL, cellType = ColumnType.NUMERIC)
    private Integer durationOverride;

    /** 及格分数覆盖（NULL时使用试卷配置） */
    @Excel(name = "及格分数覆盖", type = Type.ALL, cellType = ColumnType.NUMERIC)
    private BigDecimal passScoreOverride;

    /** 打乱题目覆盖（NULL时使用试卷配置） */
    @Excel(name = "打乱题目覆盖", readConverterExp = "0=否,1=是", type = Type.ALL)
    private String shuffleQuestionsOverride;

    /** 打乱选项覆盖（NULL时使用试卷配置） */
    @Excel(name = "打乱选项覆盖", readConverterExp = "0=否,1=是", type = Type.ALL)
    private String shuffleOptionsOverride;

    // ==================== 考试特有配置 ====================

    /** 最大考试次数 */
    @Excel(name = "最大考试次数", type = Type.ALL, cellType = ColumnType.NUMERIC)
    private Integer maxAttempts;

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

    // ==================== 实际生效的配置（通过业务逻辑计算） ====================

    /** 实际生效的考试时长 */
    @Excel(name = "考试时长", type = Type.EXPORT, cellType = ColumnType.NUMERIC)
    private Integer effectiveDuration;

    /** 实际生效的总分 */
    @Excel(name = "总分", type = Type.EXPORT, cellType = ColumnType.NUMERIC)
    private BigDecimal effectiveTotalScore;

    /** 实际生效的及格分数 */
    @Excel(name = "及格分数", type = Type.EXPORT, cellType = ColumnType.NUMERIC)
    private BigDecimal effectivePassScore;

    /** 实际生效的打乱题目设置 */
    @Excel(name = "打乱题目", readConverterExp = "0=否,1=是", type = Type.EXPORT)
    private String effectiveShuffleQuestions;

    /** 实际生效的打乱选项设置 */
    @Excel(name = "打乱选项", readConverterExp = "0=否,1=是", type = Type.EXPORT)
    private String effectiveShuffleOptions;

    // ==================== 统计字段 ====================

    /** 考试人员列表 */
    private List<ExamUser> examUsers;

    /** 报名人数 */
    @Excel(name = "报名人数", type = Type.EXPORT, cellType = ColumnType.NUMERIC)
    private Integer registeredCount;

    /** 已提交人数 */
    @Excel(name = "已提交人数", type = Type.EXPORT, cellType = ColumnType.NUMERIC)
    private Integer submittedCount;

    /** 通过人数 */
    @Excel(name = "通过人数", type = Type.EXPORT, cellType = ColumnType.NUMERIC)
    private Integer passedCount;

    // ==================== Getter and Setter ====================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @NotBlank(message = "考试名称不能为空")
    @Size(min = 0, max = 100, message = "考试名称长度不能超过100个字符")
    public String getExamName() {
        return examName;
    }

    public void setExamName(String examName) {
        this.examName = examName;
    }

    @NotNull(message = "考试分类不能为空")
    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    @NotNull(message = "试卷不能为空")
    public Long getPaperId() {
        return paperId;
    }

    public void setPaperId(Long paperId) {
        this.paperId = paperId;
    }

    public String getPaperName() {
        return paperName;
    }

    public void setPaperName(String paperName) {
        this.paperName = paperName;
    }

    public String getExamDescription() {
        return examDescription;
    }

    public void setExamDescription(String examDescription) {
        this.examDescription = examDescription;
    }

    @NotNull(message = "开始时间不能为空")
    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    @NotNull(message = "结束时间不能为空")
    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    // ==================== Override字段的getter/setter ====================

    public Integer getDurationOverride() {
        return durationOverride;
    }

    public void setDurationOverride(Integer durationOverride) {
        this.durationOverride = durationOverride;
    }

    public BigDecimal getPassScoreOverride() {
        return passScoreOverride;
    }

    public void setPassScoreOverride(BigDecimal passScoreOverride) {
        this.passScoreOverride = passScoreOverride;
    }

    public String getShuffleQuestionsOverride() {
        return shuffleQuestionsOverride;
    }

    public void setShuffleQuestionsOverride(String shuffleQuestionsOverride) {
        this.shuffleQuestionsOverride = shuffleQuestionsOverride;
    }

    public String getShuffleOptionsOverride() {
        return shuffleOptionsOverride;
    }

    public void setShuffleOptionsOverride(String shuffleOptionsOverride) {
        this.shuffleOptionsOverride = shuffleOptionsOverride;
    }

    // ==================== 考试特有字段的getter/setter ====================

    public Integer getMaxAttempts() {
        return maxAttempts;
    }

    public void setMaxAttempts(Integer maxAttempts) {
        this.maxAttempts = maxAttempts;
    }

    public String getExamType() {
        return examType;
    }

    public void setExamType(String examType) {
        this.examType = examType;
    }

    public String getExamMode() {
        return examMode;
    }

    public void setExamMode(String examMode) {
        this.examMode = examMode;
    }

    public String getRegistrationRequired() {
        return registrationRequired;
    }

    public void setRegistrationRequired(String registrationRequired) {
        this.registrationRequired = registrationRequired;
    }

    public Date getRegistrationStartTime() {
        return registrationStartTime;
    }

    public void setRegistrationStartTime(Date registrationStartTime) {
        this.registrationStartTime = registrationStartTime;
    }

    public Date getRegistrationEndTime() {
        return registrationEndTime;
    }

    public void setRegistrationEndTime(Date registrationEndTime) {
        this.registrationEndTime = registrationEndTime;
    }

    public String getAutoGrade() {
        return autoGrade;
    }

    public void setAutoGrade(String autoGrade) {
        this.autoGrade = autoGrade;
    }

    public String getResultPublishType() {
        return resultPublishType;
    }

    public void setResultPublishType(String resultPublishType) {
        this.resultPublishType = resultPublishType;
    }

    public Date getResultPublishTime() {
        return resultPublishTime;
    }

    public void setResultPublishTime(Date resultPublishTime) {
        this.resultPublishTime = resultPublishTime;
    }

    public String getAllowReview() {
        return allowReview;
    }

    public void setAllowReview(String allowReview) {
        this.allowReview = allowReview;
    }

    public String getMonitorMode() {
        return monitorMode;
    }

    public void setMonitorMode(String monitorMode) {
        this.monitorMode = monitorMode;
    }

    public String getAntiCheat() {
        return antiCheat;
    }

    public void setAntiCheat(String antiCheat) {
        this.antiCheat = antiCheat;
    }

    public Long getCertificateTemplateId() {
        return certificateTemplateId;
    }

    public void setCertificateTemplateId(Long certificateTemplateId) {
        this.certificateTemplateId = certificateTemplateId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(Long creatorId) {
        this.creatorId = creatorId;
    }

    public String getCreatorName() {
        return creatorName;
    }

    public void setCreatorName(String creatorName) {
        this.creatorName = creatorName;
    }

    // ==================== 实际生效配置的getter/setter ====================

    public Integer getEffectiveDuration() {
        return effectiveDuration;
    }

    public void setEffectiveDuration(Integer effectiveDuration) {
        this.effectiveDuration = effectiveDuration;
    }

    public BigDecimal getEffectiveTotalScore() {
        return effectiveTotalScore;
    }

    public void setEffectiveTotalScore(BigDecimal effectiveTotalScore) {
        this.effectiveTotalScore = effectiveTotalScore;
    }

    public BigDecimal getEffectivePassScore() {
        return effectivePassScore;
    }

    public void setEffectivePassScore(BigDecimal effectivePassScore) {
        this.effectivePassScore = effectivePassScore;
    }

    public String getEffectiveShuffleQuestions() {
        return effectiveShuffleQuestions;
    }

    public void setEffectiveShuffleQuestions(String effectiveShuffleQuestions) {
        this.effectiveShuffleQuestions = effectiveShuffleQuestions;
    }

    public String getEffectiveShuffleOptions() {
        return effectiveShuffleOptions;
    }

    public void setEffectiveShuffleOptions(String effectiveShuffleOptions) {
        this.effectiveShuffleOptions = effectiveShuffleOptions;
    }

    // ==================== 统计字段的getter/setter ====================

    public List<ExamUser> getExamUsers() {
        return examUsers;
    }

    public void setExamUsers(List<ExamUser> examUsers) {
        this.examUsers = examUsers;
    }

    public Integer getRegisteredCount() {
        return registeredCount;
    }

    public void setRegisteredCount(Integer registeredCount) {
        this.registeredCount = registeredCount;
    }

    public Integer getSubmittedCount() {
        return submittedCount;
    }

    public void setSubmittedCount(Integer submittedCount) {
        this.submittedCount = submittedCount;
    }

    public Integer getPassedCount() {
        return passedCount;
    }

    public void setPassedCount(Integer passedCount) {
        this.passedCount = passedCount;
    }

    // ==================== 业务方法 ====================

    /**
     * 计算实际生效的配置
     * 需要传入试卷信息来计算实际生效的配置
     */
    public void calculateEffectiveConfig(ExamPaper paper) {
        if (paper == null) {
            return;
        }
        
        // 计算实际生效的考试时长
        this.effectiveDuration = (this.durationOverride != null) ? this.durationOverride : paper.getDuration();
        
        // 总分直接使用试卷的总分
        this.effectiveTotalScore = paper.getTotalScore();
        
        // 计算实际生效的及格分数
        this.effectivePassScore = (this.passScoreOverride != null) ? this.passScoreOverride : paper.getPassScore();
        
        // 计算实际生效的打乱题目设置
        this.effectiveShuffleQuestions = (this.shuffleQuestionsOverride != null) ? this.shuffleQuestionsOverride : paper.getShuffleQuestions();
        
        // 计算实际生效的打乱选项设置
        this.effectiveShuffleOptions = (this.shuffleOptionsOverride != null) ? this.shuffleOptionsOverride : paper.getShuffleOptions();
    }

    /**
     * 检查是否覆盖了试卷配置
     */
    public boolean hasOverrideConfig() {
        return durationOverride != null || passScoreOverride != null || 
               shuffleQuestionsOverride != null || shuffleOptionsOverride != null;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("examName", getExamName())
            .append("categoryId", getCategoryId())
            .append("paperId", getPaperId())
            .append("examDescription", getExamDescription())
            .append("startTime", getStartTime())
            .append("endTime", getEndTime())
            .append("durationOverride", getDurationOverride())
            .append("passScoreOverride", getPassScoreOverride())
            .append("shuffleQuestionsOverride", getShuffleQuestionsOverride())
            .append("shuffleOptionsOverride", getShuffleOptionsOverride())
            .append("maxAttempts", getMaxAttempts())
            .append("examType", getExamType())
            .append("examMode", getExamMode())
            .append("registrationRequired", getRegistrationRequired())
            .append("registrationStartTime", getRegistrationStartTime())
            .append("registrationEndTime", getRegistrationEndTime())
            .append("autoGrade", getAutoGrade())
            .append("resultPublishType", getResultPublishType())
            .append("resultPublishTime", getResultPublishTime())
            .append("allowReview", getAllowReview())
            .append("monitorMode", getMonitorMode())
            .append("antiCheat", getAntiCheat())
            .append("certificateTemplateId", getCertificateTemplateId())
            .append("status", getStatus())
            .append("creatorId", getCreatorId())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
} 