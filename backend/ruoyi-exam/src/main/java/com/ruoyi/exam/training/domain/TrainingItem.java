package com.ruoyi.exam.training.domain;

import java.math.BigDecimal;
import java.util.Date;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 培训扩展条目 exam_training_item
 */
public class TrainingItem extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long id;

    @Excel(name = "类型")
    private String itemType;

    @Excel(name = "标题")
    private String title;

    private String summary;

    private String content;

    private Long parentId;

    private Long categoryId;

    private String coverUrl;

    private String fileUrl;

    private String thumbnailUrl;

    private String resourceType;

    private String processingStatus;

    private String processingMessage;

    private Long relatedExamId;

    private Long relatedPaperId;

    private Long userId;

    private Long[] userIds;

    private String userName;

    private String answerContent;

    private String status;

    private Integer sortOrder;

    private Integer durationMinutes;

    private BigDecimal credit;

    private BigDecimal score;

    private Date startTime;

    private Date finishTime;

    private Long learnerCount;

    private Long completedCount;

    private Long resourceCount;

    private String courseTitle;

    private String userNickName;

    private Integer targetDurationMinutes;

    private BigDecimal progressPercent;

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public String getItemType()
    {
        return itemType;
    }

    public void setItemType(String itemType)
    {
        this.itemType = itemType;
    }

    @NotBlank(message = "标题不能为空")
    @Size(max = 120, message = "标题长度不能超过120个字符")
    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public String getSummary()
    {
        return summary;
    }

    public void setSummary(String summary)
    {
        this.summary = summary;
    }

    public String getContent()
    {
        return content;
    }

    public void setContent(String content)
    {
        this.content = content;
    }

    public Long getParentId()
    {
        return parentId;
    }

    public void setParentId(Long parentId)
    {
        this.parentId = parentId;
    }

    public Long getCategoryId()
    {
        return categoryId;
    }

    public void setCategoryId(Long categoryId)
    {
        this.categoryId = categoryId;
    }

    public String getCoverUrl()
    {
        return coverUrl;
    }

    public void setCoverUrl(String coverUrl)
    {
        this.coverUrl = coverUrl;
    }

    public String getFileUrl()
    {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl)
    {
        this.fileUrl = fileUrl;
    }

    public String getThumbnailUrl()
    {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl)
    {
        this.thumbnailUrl = thumbnailUrl;
    }

    public String getResourceType()
    {
        return resourceType;
    }

    public void setResourceType(String resourceType)
    {
        this.resourceType = resourceType;
    }

    public String getProcessingStatus()
    {
        return processingStatus;
    }

    public void setProcessingStatus(String processingStatus)
    {
        this.processingStatus = processingStatus;
    }

    public String getProcessingMessage()
    {
        return processingMessage;
    }

    public void setProcessingMessage(String processingMessage)
    {
        this.processingMessage = processingMessage;
    }

    public Long getRelatedExamId()
    {
        return relatedExamId;
    }

    public void setRelatedExamId(Long relatedExamId)
    {
        this.relatedExamId = relatedExamId;
    }

    public Long getRelatedPaperId()
    {
        return relatedPaperId;
    }

    public void setRelatedPaperId(Long relatedPaperId)
    {
        this.relatedPaperId = relatedPaperId;
    }

    public Long getUserId()
    {
        return userId;
    }

    public void setUserId(Long userId)
    {
        this.userId = userId;
    }

    public Long[] getUserIds()
    {
        return userIds;
    }

    public void setUserIds(Long[] userIds)
    {
        this.userIds = userIds;
    }

    public String getUserName()
    {
        return userName;
    }

    public void setUserName(String userName)
    {
        this.userName = userName;
    }

    public String getAnswerContent()
    {
        return answerContent;
    }

    public void setAnswerContent(String answerContent)
    {
        this.answerContent = answerContent;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public Integer getSortOrder()
    {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder)
    {
        this.sortOrder = sortOrder;
    }

    public Integer getDurationMinutes()
    {
        return durationMinutes;
    }

    public void setDurationMinutes(Integer durationMinutes)
    {
        this.durationMinutes = durationMinutes;
    }

    public BigDecimal getCredit()
    {
        return credit;
    }

    public void setCredit(BigDecimal credit)
    {
        this.credit = credit;
    }

    public BigDecimal getScore()
    {
        return score;
    }

    public void setScore(BigDecimal score)
    {
        this.score = score;
    }

    public Date getStartTime()
    {
        return startTime;
    }

    public void setStartTime(Date startTime)
    {
        this.startTime = startTime;
    }

    public Date getFinishTime()
    {
        return finishTime;
    }

    public void setFinishTime(Date finishTime)
    {
        this.finishTime = finishTime;
    }

    public Long getLearnerCount()
    {
        return learnerCount;
    }

    public void setLearnerCount(Long learnerCount)
    {
        this.learnerCount = learnerCount;
    }

    public Long getCompletedCount()
    {
        return completedCount;
    }

    public void setCompletedCount(Long completedCount)
    {
        this.completedCount = completedCount;
    }

    public Long getResourceCount()
    {
        return resourceCount;
    }

    public void setResourceCount(Long resourceCount)
    {
        this.resourceCount = resourceCount;
    }

    public String getCourseTitle()
    {
        return courseTitle;
    }

    public void setCourseTitle(String courseTitle)
    {
        this.courseTitle = courseTitle;
    }

    public String getUserNickName()
    {
        return userNickName;
    }

    public void setUserNickName(String userNickName)
    {
        this.userNickName = userNickName;
    }

    public Integer getTargetDurationMinutes()
    {
        return targetDurationMinutes;
    }

    public void setTargetDurationMinutes(Integer targetDurationMinutes)
    {
        this.targetDurationMinutes = targetDurationMinutes;
    }

    public BigDecimal getProgressPercent()
    {
        return progressPercent;
    }

    public void setProgressPercent(BigDecimal progressPercent)
    {
        this.progressPercent = progressPercent;
    }
}
