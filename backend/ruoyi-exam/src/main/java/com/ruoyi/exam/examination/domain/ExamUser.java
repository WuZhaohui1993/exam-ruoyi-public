package com.ruoyi.exam.examination.domain;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import javax.validation.constraints.NotNull;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.annotation.Excel.ColumnType;
import com.ruoyi.common.annotation.Excel.Type;
import com.ruoyi.common.core.domain.BaseEntity;
import com.ruoyi.exam.examination.domain.ExamAnswerRecord;

/**
 * 考试用户关联表 exam_user
 * 
 * @author ruoyi
 */
public class ExamUser extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 关联ID */
    private Long id;

    /** 考试ID */
    @Excel(name = "考试ID", type = Type.IMPORT, cellType = ColumnType.NUMERIC)
    private Long examId;

    /** 考试名称 */
    @Excel(name = "考试名称", type = Type.EXPORT)
    private String examName;

    /** 用户ID */
    @Excel(name = "用户ID", type = Type.IMPORT, cellType = ColumnType.NUMERIC)
    private Long userId;

    /** 用户名 */
    @Excel(name = "用户名", type = Type.EXPORT)
    private String userName;

    /** 用户昵称 */
    @Excel(name = "用户昵称", type = Type.EXPORT)
    private String nickName;

    /** 部门ID */
    @Excel(name = "部门ID", type = Type.IMPORT, cellType = ColumnType.NUMERIC)
    private Long deptId;

    /** 部门名称 */
    @Excel(name = "部门名称", type = Type.EXPORT)
    private String deptName;

    /** 报名时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "报名时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss", type = Type.ALL)
    private Date registrationTime;

    /** 报名状态（registered:已报名 approved:审核通过 rejected:审核拒绝） */
    @Excel(name = "报名状态", readConverterExp = "registered=已报名,approved=审核通过,rejected=审核拒绝", type = Type.ALL)
    private String registrationStatus;

    /** 考试状态（not_started:未开始 in_progress:进行中 submitted:已提交 graded:已评分） */
    @Excel(name = "考试状态", readConverterExp = "not_started=未开始,in_progress=进行中,submitted=已提交,graded=已评分", type = Type.ALL)
    private String examStatus;

    /** 开始考试时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "开始考试时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss", type = Type.ALL)
    private Date startTime;

    /** 提交时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "提交时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss", type = Type.ALL)
    private Date submitTime;

    /** 考试次数 */
    @Excel(name = "考试次数", type = Type.ALL, cellType = ColumnType.NUMERIC)
    private Integer attemptCount;

    /** 考试尝试次数 */
    @Excel(name = "考试尝试次数", type = Type.ALL, cellType = ColumnType.NUMERIC)
    private Integer attemptNumber;

    /** 总得分 */
    @Excel(name = "总得分", type = Type.ALL, cellType = ColumnType.NUMERIC)
    private BigDecimal totalScore;

    /** 及格分数 */
    @Excel(name = "及格分数", type = Type.ALL, cellType = ColumnType.NUMERIC)
    private BigDecimal passScore;

    /** 是否通过（0否 1是） */
    @Excel(name = "是否通过", readConverterExp = "0=否,1=是", type = Type.ALL)
    private String isPassed;

    /** 评分时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "评分时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss", type = Type.ALL)
    private Date gradeTime;

    /** 评分人 */
    @Excel(name = "评分人", type = Type.ALL)
    private String gradeBy;

    /** 答题记录列表 */
    private List<ExamAnswerRecord> answerRecords;

    /** 手机号码 */
    @Excel(name = "手机号码", type = Type.EXPORT)
    private String phonenumber;

    /** 邮箱 */
    @Excel(name = "邮箱", type = Type.EXPORT)
    private String email;

    /** 是否包含答题详情（用于导出） */
    private Boolean includeAnswerDetails;

    /** 用户名/昵称搜索关键字（用于成绩导出筛选） */
    private String searchName;

    /** 用户ID列表（用于成绩导出选中用户） */
    private Long[] userIds;

    /** 最低分（用于成绩导出筛选） */
    private BigDecimal minScore;

    /** 最高分（用于成绩导出筛选） */
    private BigDecimal maxScore;

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

    public String getExamName()
    {
        return examName;
    }

    public void setExamName(String examName)
    {
        this.examName = examName;
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

    public String getUserName()
    {
        return userName;
    }

    public void setUserName(String userName)
    {
        this.userName = userName;
    }

    public String getNickName()
    {
        return nickName;
    }

    public void setNickName(String nickName)
    {
        this.nickName = nickName;
    }

    public Long getDeptId()
    {
        return deptId;
    }

    public void setDeptId(Long deptId)
    {
        this.deptId = deptId;
    }

    public String getDeptName()
    {
        return deptName;
    }

    public void setDeptName(String deptName)
    {
        this.deptName = deptName;
    }

    public Date getRegistrationTime()
    {
        return registrationTime;
    }

    public void setRegistrationTime(Date registrationTime)
    {
        this.registrationTime = registrationTime;
    }

    public String getRegistrationStatus()
    {
        return registrationStatus;
    }

    public void setRegistrationStatus(String registrationStatus)
    {
        this.registrationStatus = registrationStatus;
    }

    public String getExamStatus()
    {
        return examStatus;
    }

    public void setExamStatus(String examStatus)
    {
        this.examStatus = examStatus;
    }

    public Date getStartTime()
    {
        return startTime;
    }

    public void setStartTime(Date startTime)
    {
        this.startTime = startTime;
    }

    public Date getSubmitTime()
    {
        return submitTime;
    }

    public void setSubmitTime(Date submitTime)
    {
        this.submitTime = submitTime;
    }

    public Integer getAttemptCount()
    {
        return attemptCount;
    }

    public void setAttemptCount(Integer attemptCount)
    {
        this.attemptCount = attemptCount;
    }

    public Integer getAttemptNumber()
    {
        return attemptNumber;
    }

    public void setAttemptNumber(Integer attemptNumber)
    {
        this.attemptNumber = attemptNumber;
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

    public String getIsPassed()
    {
        return isPassed;
    }

    public void setIsPassed(String isPassed)
    {
        this.isPassed = isPassed;
    }

    public Date getGradeTime()
    {
        return gradeTime;
    }

    public void setGradeTime(Date gradeTime)
    {
        this.gradeTime = gradeTime;
    }

    public String getGradeBy()
    {
        return gradeBy;
    }

    public void setGradeBy(String gradeBy)
    {
        this.gradeBy = gradeBy;
    }

    public List<ExamAnswerRecord> getAnswerRecords()
    {
        return answerRecords;
    }

    public void setAnswerRecords(List<ExamAnswerRecord> answerRecords)
    {
        this.answerRecords = answerRecords;
    }

    public String getPhonenumber()
    {
        return phonenumber;
    }

    public void setPhonenumber(String phonenumber)
    {
        this.phonenumber = phonenumber;
    }

    public String getEmail()
    {
        return email;
    }

    public void setEmail(String email)
    {
        this.email = email;
    }

    public Boolean getIncludeAnswerDetails()
    {
        return includeAnswerDetails;
    }

    public void setIncludeAnswerDetails(Boolean includeAnswerDetails)
    {
        this.includeAnswerDetails = includeAnswerDetails;
    }

    public String getSearchName()
    {
        return searchName;
    }

    public void setSearchName(String searchName)
    {
        this.searchName = searchName;
    }

    public Long[] getUserIds()
    {
        return userIds;
    }

    public void setUserIds(Long[] userIds)
    {
        this.userIds = userIds;
    }

    public BigDecimal getMinScore()
    {
        return minScore;
    }

    public void setMinScore(BigDecimal minScore)
    {
        this.minScore = minScore;
    }

    public BigDecimal getMaxScore()
    {
        return maxScore;
    }

    public void setMaxScore(BigDecimal maxScore)
    {
        this.maxScore = maxScore;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("examId", getExamId())
            .append("userId", getUserId())
            .append("deptId", getDeptId())
            .append("registrationTime", getRegistrationTime())
            .append("registrationStatus", getRegistrationStatus())
            .append("examStatus", getExamStatus())
            .append("startTime", getStartTime())
            .append("submitTime", getSubmitTime())
            .append("attemptCount", getAttemptCount())
            .append("attemptNumber", getAttemptNumber())
            .append("totalScore", getTotalScore())
            .append("passScore", getPassScore())
            .append("isPassed", getIsPassed())
            .append("gradeTime", getGradeTime())
            .append("gradeBy", getGradeBy())
            .append("searchName", getSearchName())
            .append("minScore", getMinScore())
            .append("maxScore", getMaxScore())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
