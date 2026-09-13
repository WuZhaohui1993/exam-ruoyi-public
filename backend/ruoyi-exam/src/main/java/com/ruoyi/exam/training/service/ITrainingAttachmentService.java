package com.ruoyi.exam.training.service;

import java.util.List;
import com.ruoyi.exam.training.domain.TrainingAttachment;

public interface ITrainingAttachmentService
{
    public List<TrainingAttachment> selectTrainingAttachmentList(TrainingAttachment attachment);

    public TrainingAttachment selectTrainingAttachmentById(Long id);

    public int insertTrainingAttachment(TrainingAttachment attachment);

    public int deleteTrainingAttachmentByIds(Long[] ids);
}
