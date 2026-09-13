package com.ruoyi.exam.question.service;

import java.util.List;
import java.util.Map;
import com.ruoyi.exam.question.domain.ExamQuestionMedia;

/**
 * 试题媒体附件 服务层
 *
 * @author ruoyi
 */
public interface IExamQuestionMediaService
{
    public List<ExamQuestionMedia> selectMediaByQuestionId(Long questionId);

    public Map<Long, List<ExamQuestionMedia>> selectMediaMapByQuestionIds(List<Long> questionIds);

    public void replaceQuestionMedia(Long questionId, List<ExamQuestionMedia> mediaList, String operName);

    public int deleteMediaByQuestionId(Long questionId);

    public int deleteMediaByQuestionIds(Long[] questionIds);
}
