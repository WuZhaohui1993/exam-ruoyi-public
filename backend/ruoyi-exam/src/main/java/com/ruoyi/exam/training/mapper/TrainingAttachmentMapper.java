package com.ruoyi.exam.training.mapper;

import java.util.List;
import com.ruoyi.exam.training.domain.TrainingAttachment;

public interface TrainingAttachmentMapper
{
    public List<TrainingAttachment> selectTrainingAttachmentList(TrainingAttachment attachment);

    public TrainingAttachment selectTrainingAttachmentById(Long id);

    public int insertTrainingAttachment(TrainingAttachment attachment);

    public int deleteTrainingAttachmentByIds(Long[] ids);

    public int countTrainingAttachment(TrainingAttachment attachment);
}
