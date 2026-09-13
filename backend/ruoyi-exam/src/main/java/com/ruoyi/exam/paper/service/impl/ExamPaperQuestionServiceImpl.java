package com.ruoyi.exam.paper.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.exam.paper.domain.ExamPaperQuestion;
import com.ruoyi.exam.paper.mapper.ExamPaperQuestionMapper;
import com.ruoyi.exam.paper.service.IExamPaperQuestionService;
import com.ruoyi.exam.question.domain.ExamQuestionMedia;
import com.ruoyi.exam.question.service.IExamQuestionMediaService;

/**
 * 试卷试题关联Service业务层处理
 *
 * @author ruoyi
 * @date 2025-01-24
 */
@Service
public class ExamPaperQuestionServiceImpl implements IExamPaperQuestionService
{
    @Autowired
    private ExamPaperQuestionMapper examPaperQuestionMapper;

    @Autowired
    private IExamQuestionMediaService questionMediaService;

    /**
     * 查询试卷试题关联
     *
     * @param id 试卷试题关联主键
     * @return 试卷试题关联
     */
    @Override
    public ExamPaperQuestion selectExamPaperQuestionById(Long id)
    {
        return examPaperQuestionMapper.selectExamPaperQuestionById(id);
    }

    /**
     * 查询试卷试题关联列表
     *
     * @param examPaperQuestion 试卷试题关联
     * @return 试卷试题关联
     */
    @Override
    public List<ExamPaperQuestion> selectExamPaperQuestionList(ExamPaperQuestion examPaperQuestion)
    {
        return examPaperQuestionMapper.selectExamPaperQuestionList(examPaperQuestion);
    }

    /**
     * 根据试卷ID查询试题列表
     *
     * @param paperId 试卷ID
     * @return 试题列表
     */
    @Override
    public List<ExamPaperQuestion> selectQuestionsByPaperId(Long paperId)
    {
        return examPaperQuestionMapper.selectQuestionsByPaperId(paperId);
    }

    /**
     * 根据试卷ID查询试题列表（带试题详细信息）
     *
     * @param paperId 试卷ID
     * @return 试题列表
     */
    @Override
    public List<ExamPaperQuestion> selectQuestionsWithDetailsByPaperId(Long paperId)
    {
        List<ExamPaperQuestion> questions = examPaperQuestionMapper.selectQuestionsWithDetailsByPaperId(paperId);
        fillQuestionMedia(questions);
        return questions;
    }

    private void fillQuestionMedia(List<ExamPaperQuestion> paperQuestions)
    {
        if (StringUtils.isEmpty(paperQuestions))
        {
            return;
        }

        List<Long> questionIds = new ArrayList<>();
        for (ExamPaperQuestion paperQuestion : paperQuestions)
        {
            if (paperQuestion != null && paperQuestion.getQuestionId() != null)
            {
                questionIds.add(paperQuestion.getQuestionId());
            }
        }
        if (questionIds.isEmpty())
        {
            return;
        }

        Map<Long, List<ExamQuestionMedia>> mediaMap = questionMediaService.selectMediaMapByQuestionIds(questionIds);
        for (ExamPaperQuestion paperQuestion : paperQuestions)
        {
            if (paperQuestion != null && paperQuestion.getQuestion() != null)
            {
                paperQuestion.getQuestion().setMediaList(mediaMap.get(paperQuestion.getQuestionId()));
            }
        }
    }

    /**
     * 根据试卷ID统计试题数量
     *
     * @param paperId 试卷ID
     * @return 试题数量
     */
    @Override
    public int countQuestionsByPaperId(Long paperId)
    {
        return examPaperQuestionMapper.countQuestionsByPaperId(paperId);
    }

    /**
     * 新增试卷试题关联
     *
     * @param examPaperQuestion 试卷试题关联
     * @return 结果
     */
    @Override
    public int insertExamPaperQuestion(ExamPaperQuestion examPaperQuestion)
    {
        return examPaperQuestionMapper.insertExamPaperQuestion(examPaperQuestion);
    }

    /**
     * 修改试卷试题关联
     *
     * @param examPaperQuestion 试卷试题关联
     * @return 结果
     */
    @Override
    public int updateExamPaperQuestion(ExamPaperQuestion examPaperQuestion)
    {
        return examPaperQuestionMapper.updateExamPaperQuestion(examPaperQuestion);
    }

    /**
     * 批量删除试卷试题关联
     *
     * @param ids 需要删除的试卷试题关联主键
     * @return 结果
     */
    @Override
    public int deleteExamPaperQuestionByIds(Long[] ids)
    {
        return examPaperQuestionMapper.deleteExamPaperQuestionByIds(ids);
    }

    /**
     * 删除试卷试题关联信息
     *
     * @param id 试卷试题关联主键
     * @return 结果
     */
    @Override
    public int deleteExamPaperQuestionById(Long id)
    {
        return examPaperQuestionMapper.deleteExamPaperQuestionById(id);
    }

    /**
     * 根据试卷ID删除试卷试题关联
     *
     * @param paperId 试卷ID
     * @return 结果
     */
    @Override
    public int deleteExamPaperQuestionByPaperId(Long paperId)
    {
        return examPaperQuestionMapper.deleteExamPaperQuestionByPaperId(paperId);
    }

    /**
     * 根据试卷ID删除关联的所有试题
     *
     * @param paperId 试卷ID
     * @return 结果
     */
    @Override
    public int deleteByPaperId(Long paperId)
    {
        return examPaperQuestionMapper.deleteByPaperId(paperId);
    }

    /**
     * 批量新增试卷试题关联
     *
     * @param examPaperQuestions 试卷试题关联列表
     * @return 结果
     */
    @Override
    public int batchInsert(List<ExamPaperQuestion> examPaperQuestions)
    {
        return examPaperQuestionMapper.batchInsert(examPaperQuestions);
    }

    /**
     * 批量添加试题到试卷
     *
     * @param paperId 试卷ID
     * @param questionIds 试题ID数组
     * @return 结果
     */
    @Override
    public int batchInsertPaperQuestions(Long paperId, Long[] questionIds)
    {
        return examPaperQuestionMapper.batchInsertPaperQuestions(paperId, questionIds);
    }

    /**
     * 更新试题顺序
     *
     * @param examPaperQuestions 试卷试题关联列表
     * @return 结果
     */
    @Override
    public int updateQuestionOrder(List<ExamPaperQuestion> examPaperQuestions)
    {
        return examPaperQuestionMapper.updateQuestionOrder(examPaperQuestions);
    }
}
