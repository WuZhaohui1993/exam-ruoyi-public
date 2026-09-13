package com.ruoyi.exam.question.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.exam.question.domain.ExamQuestionMedia;

/**
 * 试题媒体附件 数据层
 *
 * @author ruoyi
 */
public interface ExamQuestionMediaMapper
{
    public List<ExamQuestionMedia> selectQuestionMediaByQuestionId(Long questionId);

    public List<ExamQuestionMedia> selectQuestionMediaByQuestionIds(@Param("questionIds") List<Long> questionIds);

    public int insertQuestionMedia(ExamQuestionMedia media);

    public int batchInsertQuestionMedia(@Param("list") List<ExamQuestionMedia> mediaList);

    public int deleteQuestionMediaByQuestionId(Long questionId);

    public int deleteQuestionMediaByQuestionIds(Long[] questionIds);
}
