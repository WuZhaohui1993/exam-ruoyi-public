package com.ruoyi.exam.training.domain;

import java.math.BigDecimal;

/**
 * 培训扩展统计摘要。
 */
public class TrainingSummary
{
    private Long courseCount;
    private Long resourceCount;
    private Long qaCount;
    private Long certificateCount;
    private Long attachmentCount;
    private Long practiceCount;
    private Long publishedCount;
    private Long completedCount;
    private Long openQaCount;
    private Long mediaReadyCount;
    private Long mediaSkippedCount;
    private Long mediaFailedCount;
    private BigDecimal totalCredit;
    private BigDecimal averageScore;

    public Long getCourseCount()
    {
        return courseCount;
    }

    public void setCourseCount(Long courseCount)
    {
        this.courseCount = courseCount;
    }

    public Long getResourceCount()
    {
        return resourceCount;
    }

    public void setResourceCount(Long resourceCount)
    {
        this.resourceCount = resourceCount;
    }

    public Long getQaCount()
    {
        return qaCount;
    }

    public void setQaCount(Long qaCount)
    {
        this.qaCount = qaCount;
    }

    public Long getCertificateCount()
    {
        return certificateCount;
    }

    public void setCertificateCount(Long certificateCount)
    {
        this.certificateCount = certificateCount;
    }

    public Long getAttachmentCount()
    {
        return attachmentCount;
    }

    public void setAttachmentCount(Long attachmentCount)
    {
        this.attachmentCount = attachmentCount;
    }

    public Long getPracticeCount()
    {
        return practiceCount;
    }

    public void setPracticeCount(Long practiceCount)
    {
        this.practiceCount = practiceCount;
    }

    public Long getPublishedCount()
    {
        return publishedCount;
    }

    public void setPublishedCount(Long publishedCount)
    {
        this.publishedCount = publishedCount;
    }

    public Long getCompletedCount()
    {
        return completedCount;
    }

    public void setCompletedCount(Long completedCount)
    {
        this.completedCount = completedCount;
    }

    public Long getOpenQaCount()
    {
        return openQaCount;
    }

    public void setOpenQaCount(Long openQaCount)
    {
        this.openQaCount = openQaCount;
    }

    public Long getMediaReadyCount()
    {
        return mediaReadyCount;
    }

    public void setMediaReadyCount(Long mediaReadyCount)
    {
        this.mediaReadyCount = mediaReadyCount;
    }

    public Long getMediaSkippedCount()
    {
        return mediaSkippedCount;
    }

    public void setMediaSkippedCount(Long mediaSkippedCount)
    {
        this.mediaSkippedCount = mediaSkippedCount;
    }

    public Long getMediaFailedCount()
    {
        return mediaFailedCount;
    }

    public void setMediaFailedCount(Long mediaFailedCount)
    {
        this.mediaFailedCount = mediaFailedCount;
    }

    public BigDecimal getTotalCredit()
    {
        return totalCredit;
    }

    public void setTotalCredit(BigDecimal totalCredit)
    {
        this.totalCredit = totalCredit;
    }

    public BigDecimal getAverageScore()
    {
        return averageScore;
    }

    public void setAverageScore(BigDecimal averageScore)
    {
        this.averageScore = averageScore;
    }
}
