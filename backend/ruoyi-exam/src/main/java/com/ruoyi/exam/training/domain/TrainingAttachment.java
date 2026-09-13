package com.ruoyi.exam.training.domain;

import javax.validation.constraints.NotBlank;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 学员作答附件 exam_training_attachment
 */
public class TrainingAttachment extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long id;

    private Long examId;

    private Long examUserId;

    private Long answerRecordId;

    private Long userId;

    private String fileName;

    private String fileUrl;

    private String fileType;

    private Long fileSize;

    private String uploadSource;

    private String status;

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public Long getExamId()
    {
        return examId;
    }

    public void setExamId(Long examId)
    {
        this.examId = examId;
    }

    public Long getExamUserId()
    {
        return examUserId;
    }

    public void setExamUserId(Long examUserId)
    {
        this.examUserId = examUserId;
    }

    public Long getAnswerRecordId()
    {
        return answerRecordId;
    }

    public void setAnswerRecordId(Long answerRecordId)
    {
        this.answerRecordId = answerRecordId;
    }

    public Long getUserId()
    {
        return userId;
    }

    public void setUserId(Long userId)
    {
        this.userId = userId;
    }

    @NotBlank(message = "文件名称不能为空")
    public String getFileName()
    {
        return fileName;
    }

    public void setFileName(String fileName)
    {
        this.fileName = fileName;
    }

    @NotBlank(message = "文件地址不能为空")
    public String getFileUrl()
    {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl)
    {
        this.fileUrl = fileUrl;
    }

    public String getFileType()
    {
        return fileType;
    }

    public void setFileType(String fileType)
    {
        this.fileType = fileType;
    }

    public Long getFileSize()
    {
        return fileSize;
    }

    public void setFileSize(Long fileSize)
    {
        this.fileSize = fileSize;
    }

    public String getUploadSource()
    {
        return uploadSource;
    }

    public void setUploadSource(String uploadSource)
    {
        this.uploadSource = uploadSource;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }
}
