package com.ruoyi.exam.question.domain;

import com.ruoyi.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 试题媒体附件表 exam_question_media
 *
 * @author ruoyi
 */
public class ExamQuestionMedia extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 媒体ID */
    private Long id;

    /** 试题ID */
    private Long questionId;

    /** 媒体类型 image/video/audio/file */
    private String mediaType;

    /** 文件访问地址 */
    private String fileUrl;

    /** 缩略图地址 */
    private String thumbnailUrl;

    /** 文件名称 */
    private String fileName;

    /** 文件大小 */
    private Long fileSize;

    /** 时长（秒） */
    private Integer duration;

    /** 排序 */
    private Integer sortOrder;

    /** 展示位置 stem/analysis */
    private String displayPosition;

    /** 状态（0正常 1停用） */
    private String status;

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public Long getQuestionId()
    {
        return questionId;
    }

    public void setQuestionId(Long questionId)
    {
        this.questionId = questionId;
    }

    public String getMediaType()
    {
        return mediaType;
    }

    public void setMediaType(String mediaType)
    {
        this.mediaType = mediaType;
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

    public String getFileName()
    {
        return fileName;
    }

    public void setFileName(String fileName)
    {
        this.fileName = fileName;
    }

    public Long getFileSize()
    {
        return fileSize;
    }

    public void setFileSize(Long fileSize)
    {
        this.fileSize = fileSize;
    }

    public Integer getDuration()
    {
        return duration;
    }

    public void setDuration(Integer duration)
    {
        this.duration = duration;
    }

    public Integer getSortOrder()
    {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder)
    {
        this.sortOrder = sortOrder;
    }

    public String getDisplayPosition()
    {
        return displayPosition;
    }

    public void setDisplayPosition(String displayPosition)
    {
        this.displayPosition = displayPosition;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("questionId", getQuestionId())
            .append("mediaType", getMediaType())
            .append("fileUrl", getFileUrl())
            .append("thumbnailUrl", getThumbnailUrl())
            .append("fileName", getFileName())
            .append("fileSize", getFileSize())
            .append("duration", getDuration())
            .append("sortOrder", getSortOrder())
            .append("displayPosition", getDisplayPosition())
            .append("status", getStatus())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
