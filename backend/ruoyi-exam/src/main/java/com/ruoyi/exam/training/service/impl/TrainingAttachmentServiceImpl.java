package com.ruoyi.exam.training.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.html.HtmlSanitizer;
import com.ruoyi.exam.training.domain.TrainingAttachment;
import com.ruoyi.exam.training.mapper.TrainingAttachmentMapper;
import com.ruoyi.exam.training.service.ITrainingAttachmentService;

@Service
public class TrainingAttachmentServiceImpl implements ITrainingAttachmentService
{
    @Autowired
    private TrainingAttachmentMapper trainingAttachmentMapper;

    @Override
    public List<TrainingAttachment> selectTrainingAttachmentList(TrainingAttachment attachment)
    {
        return trainingAttachmentMapper.selectTrainingAttachmentList(attachment);
    }

    @Override
    public TrainingAttachment selectTrainingAttachmentById(Long id)
    {
        return trainingAttachmentMapper.selectTrainingAttachmentById(id);
    }

    @Override
    public int insertTrainingAttachment(TrainingAttachment attachment)
    {
        if (StringUtils.isEmpty(attachment.getFileUrl()))
        {
            throw new ServiceException("附件地址不能为空");
        }
        attachment.setFileName(HtmlSanitizer.cleanText(attachment.getFileName()));
        attachment.setFileType(HtmlSanitizer.cleanText(attachment.getFileType()));
        attachment.setUploadSource(StringUtils.defaultIfEmpty(HtmlSanitizer.cleanText(attachment.getUploadSource()), "web"));
        attachment.setStatus(StringUtils.defaultIfEmpty(attachment.getStatus(), "0"));
        return trainingAttachmentMapper.insertTrainingAttachment(attachment);
    }

    @Override
    public int deleteTrainingAttachmentByIds(Long[] ids)
    {
        return trainingAttachmentMapper.deleteTrainingAttachmentByIds(ids);
    }
}
