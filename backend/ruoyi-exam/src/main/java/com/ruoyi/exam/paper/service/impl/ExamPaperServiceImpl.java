package com.ruoyi.exam.paper.service.impl;

import java.math.BigDecimal;
import java.util.List;
import java.util.ArrayList;
import java.util.Date;
import java.util.Collections;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.common.constant.UserConstants;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.exam.paper.domain.ExamPaper;
import com.ruoyi.exam.paper.domain.ExamPaperQuestion;
import com.ruoyi.exam.paper.mapper.ExamPaperMapper;
import com.ruoyi.exam.paper.service.IExamPaperService;
import com.ruoyi.exam.question.domain.ExamQuestion;
import com.ruoyi.exam.question.domain.ExamQuestionMedia;
import com.ruoyi.exam.question.mapper.ExamQuestionMapper;
import com.ruoyi.exam.question.service.IExamQuestionMediaService;
import com.ruoyi.exam.question.utils.QuestionTypeUtils;

/**
 * 试卷 服务层处理
 *
 * @author ruoyi
 */
@Service
public class ExamPaperServiceImpl implements IExamPaperService
{
    @Autowired
    private ExamPaperMapper paperMapper;

    @Autowired
    private ExamQuestionMapper questionMapper;

    @Autowired
    private IExamQuestionMediaService questionMediaService;

    /**
     * 查询试卷列表
     *
     * @param paper 试卷信息
     * @return 试卷集合
     */
    @Override
    public List<ExamPaper> selectPaperList(ExamPaper paper)
    {
        return paperMapper.selectPaperList(paper);
    }

    /**
     * 根据试卷ID查询试卷信息
     *
     * @param id 试卷ID
     * @return 试卷信息
     */
    @Override
    public ExamPaper selectPaperById(Long id)
    {
        return paperMapper.selectPaperById(id);
    }

    /**
     * 根据分类ID查询试卷列表
     *
     * @param categoryId 分类ID
     * @return 试卷列表
     */
    @Override
    public List<ExamPaper> selectPapersByCategoryId(Long categoryId)
    {
        return paperMapper.selectPapersByCategoryId(categoryId);
    }

    /**
     * 根据试卷类型查询试卷列表
     *
     * @param paperType 试卷类型
     * @return 试卷列表
     */
    @Override
    public List<ExamPaper> selectPapersByType(String paperType)
    {
        return paperMapper.selectPapersByType(paperType);
    }

    /**
     * 校验试卷名称是否唯一
     *
     * @param paper 试卷信息
     * @return 结果
     */
    @Override
    public boolean checkPaperNameUnique(ExamPaper paper)
    {
        Long paperId = StringUtils.isNull(paper.getId()) ? -1L : paper.getId();
        ExamPaper info = paperMapper.checkPaperNameUnique(paper.getPaperName(), paper.getCategoryId());
        if (StringUtils.isNotNull(info) && info.getId().longValue() != paperId.longValue())
        {
            return UserConstants.NOT_UNIQUE;
        }
        return UserConstants.UNIQUE;
    }

    /**
     * 新增试卷
     *
     * @param paper 试卷信息
     * @return 结果
     */
    @Override
    @Transactional
    public int insertPaper(ExamPaper paper)
    {
        // 设置默认值
        if (paper.getDifficultyLevel() == null)
        {
            paper.setDifficultyLevel(1);
        }
        if (StringUtils.isEmpty(paper.getStatus()))
        {
            paper.setStatus("0");
        }
        if (StringUtils.isEmpty(paper.getShuffleQuestions()))
        {
            paper.setShuffleQuestions("0");
        }
        if (StringUtils.isEmpty(paper.getShuffleOptions()))
        {
            paper.setShuffleOptions("0");
        }

        paper.setCreateBy(SecurityUtils.getUsername());
        paper.setCreateTime(new Date());

        return paperMapper.insertPaper(paper);
    }

    /**
     * 修改试卷
     *
     * @param paper 试卷信息
     * @return 结果
     */
    @Override
    @Transactional
    public int updatePaper(ExamPaper paper)
    {
        paper.setUpdateBy(SecurityUtils.getUsername());
        paper.setUpdateTime(new Date());
        return paperMapper.updatePaper(paper);
    }

    /**
     * 批量删除试卷
     *
     * @param ids 需要删除的试卷ID
     * @return 结果
     */
    @Override
    @Transactional
    public int deletePaperByIds(Long[] ids)
    {
        // 检查试卷是否被考试引用
        for (Long id : ids)
        {
            if (isPaperReferencedByExamination(id))
            {
                ExamPaper paper = paperMapper.selectPaperById(id);
                String paperName = paper != null ? paper.getPaperName() : "ID:" + id;
                throw new ServiceException(String.format("试卷【%s】正在被考试引用，不允许删除", paperName));
            }
        }

        // 删除试卷时同时删除关联的试题
        for (Long id : ids)
        {
            paperMapper.deletePaperQuestionByPaperId(id);
        }
        return paperMapper.deletePaperByIds(ids);
    }

    /**
     * 删除试卷信息
     *
     * @param id 试卷ID
     * @return 结果
     */
    @Override
    @Transactional
    public int deletePaperById(Long id)
    {
        // 检查试卷是否被考试引用
        if (isPaperReferencedByExamination(id))
        {
            ExamPaper paper = paperMapper.selectPaperById(id);
            String paperName = paper != null ? paper.getPaperName() : "ID:" + id;
            throw new ServiceException(String.format("试卷【%s】正在被考试引用，不允许删除", paperName));
        }

        // 删除试卷时同时删除关联的试题
        paperMapper.deletePaperQuestionByPaperId(id);
        return paperMapper.deletePaperById(id);
    }

    /**
     * 检查试卷是否被考试引用
     *
     * @param paperId 试卷ID
     * @return 是否被引用
     */
    private boolean isPaperReferencedByExamination(Long paperId)
    {
        return paperMapper.countExaminationsByPaperId(paperId) > 0;
    }

    /**
     * 统计试卷数量
     *
     * @param paper 查询条件
     * @return 试卷数量
     */
    @Override
    public int countPapers(ExamPaper paper)
    {
        return paperMapper.countPapers(paper);
    }

    /**
     * 预览试卷
     *
     * @param id 试卷ID
     * @return 试卷详细信息（包含试题）
     */
    @Override
    public ExamPaper previewPaper(Long id)
    {
        ExamPaper paper = paperMapper.selectPaperById(id);
        if (paper != null)
        {
            // 根据试卷类型获取试题
            List<ExamQuestion> questions = new ArrayList<>();

            if ("fixed".equals(paper.getPaperType()))
            {
                // 固定试卷：直接获取关联的试题
                List<ExamPaperQuestion> paperQuestions = paperMapper.selectPaperQuestions(id);
                for (ExamPaperQuestion pq : paperQuestions)
                {
                    ExamQuestion question = questionMapper.selectQuestionById(pq.getQuestionId());
                    if (question != null)
                    {
                        // 设置该题在试卷中的分值
                        question.setScore(pq.getScore());
                        questions.add(question);
                    }
                }
            }
            else if ("random".equals(paper.getPaperType()) || "mixed".equals(paper.getPaperType()))
            {
                // 随机试卷或混合试卷：根据配置生成试题
                questions = generatePaperQuestions(id);
            }

            // 应用试卷设置：题目乱序
            if ("1".equals(paper.getShuffleQuestions()))
            {
                Collections.shuffle(questions);
            }

            // 应用试卷设置：选项乱序
            if ("1".equals(paper.getShuffleOptions()))
            {
                for (ExamQuestion question : questions)
                {
                    QuestionTypeUtils.shuffleQuestionOptions(question);
                }
            }

            // 将试题添加到试卷对象中（这里需要在ExamPaper中添加questions属性）
            // paper.setQuestions(questions);
        }
        return paper;
    }

    /**
     * 复制试卷
     *
     * @param id 试卷ID
     * @return 结果
     */
    @Override
    @Transactional
    public int copyPaper(Long id)
    {
        ExamPaper originalPaper = paperMapper.selectPaperById(id);
        if (originalPaper == null)
        {
            throw new ServiceException("原试卷不存在");
        }

        // 创建新的试卷对象
        ExamPaper newPaper = new ExamPaper();
        newPaper.setPaperName(originalPaper.getPaperName() + "_副本");
        newPaper.setCategoryId(originalPaper.getCategoryId());
        newPaper.setPaperType(originalPaper.getPaperType());
        newPaper.setPaperDescription(originalPaper.getPaperDescription());
        newPaper.setTotalScore(originalPaper.getTotalScore());
        newPaper.setPassScore(originalPaper.getPassScore());
        newPaper.setDuration(originalPaper.getDuration());
        newPaper.setQuestionCount(originalPaper.getQuestionCount());
        newPaper.setDifficultyLevel(originalPaper.getDifficultyLevel());
        newPaper.setStatus(originalPaper.getStatus());
        newPaper.setShuffleQuestions(originalPaper.getShuffleQuestions());
        newPaper.setShuffleOptions(originalPaper.getShuffleOptions());
        newPaper.setQuestionConfig(originalPaper.getQuestionConfig());
        newPaper.setCreateBy(SecurityUtils.getUsername());
        newPaper.setCreateTime(new Date());

        int result = paperMapper.insertPaper(newPaper);

        // 如果是固定试卷，复制试题关联
        if ("fixed".equals(originalPaper.getPaperType()) && result > 0)
        {
            List<ExamPaperQuestion> originalQuestions = paperMapper.selectPaperQuestions(id);
            List<ExamPaperQuestion> newQuestions = new ArrayList<>();

            for (ExamPaperQuestion pq : originalQuestions)
            {
                ExamPaperQuestion newPq = new ExamPaperQuestion();
                newPq.setPaperId(newPaper.getId());
                newPq.setQuestionId(pq.getQuestionId());
                newPq.setQuestionOrder(pq.getQuestionOrder());
                newPq.setScore(pq.getScore());
                newQuestions.add(newPq);
            }

            if (!newQuestions.isEmpty())
            {
                paperMapper.batchInsertPaperQuestion(newQuestions);
            }
        }

        return result;
    }

    /**
     * 创建固定试卷
     *
     * @param paper 试卷信息
     * @param questions 选中的试题列表
     * @return 结果
     */
    @Override
    @Transactional
    public int createFixedPaper(ExamPaper paper, List<ExamQuestion> questions)
    {
        paper.setPaperType("fixed");

        // 计算试卷统计信息
        BigDecimal totalScore = BigDecimal.ZERO;
        for (ExamQuestion question : questions)
        {
            totalScore = totalScore.add(question.getScore());
        }
        paper.setTotalScore(totalScore);
        paper.setQuestionCount(questions.size());

        int result = insertPaper(paper);

        if (result > 0)
        {
            // 添加试卷试题关联
            List<ExamPaperQuestion> paperQuestions = new ArrayList<>();
            for (int i = 0; i < questions.size(); i++)
            {
                ExamQuestion question = questions.get(i);
                ExamPaperQuestion pq = new ExamPaperQuestion();
                pq.setPaperId(paper.getId());
                pq.setQuestionId(question.getId());
                pq.setQuestionOrder(i + 1);
                pq.setScore(question.getScore());
                paperQuestions.add(pq);
            }

            paperMapper.batchInsertPaperQuestion(paperQuestions);
        }

        return result;
    }

    /**
     * 创建随机试卷
     *
     * @param paper 试卷信息
     * @param questionConfig 随机试题配置
     * @return 结果
     */
    @Override
    @Transactional
    public int createRandomPaper(ExamPaper paper, String questionConfig)
    {
        paper.setPaperType("random");
        paper.setQuestionConfig(questionConfig);

        // 根据配置计算试卷统计信息
        calculatePaperStatistics(paper, questionConfig);

        // 先保存试卷基本信息
        int result = insertPaper(paper);

        if (result > 0) {
            // 根据随机配置生成题目
            try {
                JSONArray configArray = JSON.parseArray(questionConfig);
                List<ExamQuestion> generatedQuestions = generateRandomQuestionsFromArray(configArray);

                if (!generatedQuestions.isEmpty()) {
                    // 保存生成的题目到试卷题目关联表
                    List<ExamPaperQuestion> paperQuestions = new ArrayList<>();
                    for (int i = 0; i < generatedQuestions.size(); i++) {
                        ExamQuestion question = generatedQuestions.get(i);
                        ExamPaperQuestion pq = new ExamPaperQuestion();
                        pq.setPaperId(paper.getId());
                        pq.setQuestionId(question.getId());
                        pq.setQuestionOrder(i + 1);
                        pq.setScore(question.getScore());
                        paperQuestions.add(pq);
                    }

                    // 批量插入题目关联
                    paperMapper.batchInsertPaperQuestion(paperQuestions);

                    // 更新试卷的实际题目数量和总分
                    BigDecimal actualTotalScore = BigDecimal.ZERO;
                    for (ExamQuestion question : generatedQuestions) {
                        actualTotalScore = actualTotalScore.add(question.getScore());
                    }
                    paper.setQuestionCount(generatedQuestions.size());
                    paper.setTotalScore(actualTotalScore);
                    paperMapper.updatePaper(paper);
                } else {
                    throw new ServiceException("根据配置无法生成题目，请检查题库是否有足够的题目数据");
                }
            } catch (Exception e) {
                throw new ServiceException("生成随机试卷题目失败：" + e.getMessage());
            }
        }

        return result;
    }

    /**
     * 创建混合试卷
     *
     * @param paper 试卷信息
     * @param fixedQuestions 固定试题列表
     * @param randomConfig 随机试题配置
     * @return 结果
     */
    @Override
    @Transactional
    public int createMixedPaper(ExamPaper paper, List<ExamQuestion> fixedQuestions, String randomConfig)
    {
        paper.setPaperType("mixed");

        // 组合配置：固定试题 + 随机试题配置
        JSONObject config = new JSONObject();

        // 固定试题配置
        JSONArray fixedArray = new JSONArray();
        BigDecimal fixedTotalScore = BigDecimal.ZERO;
        for (ExamQuestion question : fixedQuestions)
        {
            JSONObject fixedQuestion = new JSONObject();
            fixedQuestion.put("questionId", question.getId());
            fixedQuestion.put("score", question.getScore());
            fixedArray.add(fixedQuestion);
            fixedTotalScore = fixedTotalScore.add(question.getScore());
        }
        config.put("fixedQuestions", fixedArray);

        // 随机试题配置
        if (randomConfig != null && !randomConfig.isEmpty())
        {
            config.put("randomConfig", JSON.parseArray(randomConfig));
        }

        paper.setQuestionConfig(config.toJSONString());

        // 准备所有题目列表（固定题目 + 随机生成的题目）
        List<ExamQuestion> allQuestions = new ArrayList<>(fixedQuestions);

        // 生成随机题目
        if (randomConfig != null && !randomConfig.isEmpty())
        {
            try
            {
                JSONArray randomConfigArray = JSON.parseArray(randomConfig);
                List<ExamQuestion> randomQuestions = generateRandomQuestionsFromArray(randomConfigArray);
                allQuestions.addAll(randomQuestions);
            }
            catch (Exception e)
            {
                throw new ServiceException("生成随机题目失败：" + e.getMessage());
            }
        }

        // 计算试卷统计信息
        BigDecimal actualTotalScore = BigDecimal.ZERO;
        for (ExamQuestion question : allQuestions) {
            actualTotalScore = actualTotalScore.add(question.getScore());
        }
        paper.setTotalScore(actualTotalScore);
        paper.setQuestionCount(allQuestions.size());

        int result = insertPaper(paper);

        if (result > 0)
        {
            // 删除原有试卷试题关联
            paperMapper.deletePaperQuestionByPaperId(paper.getId());

            // 添加新的试卷试题关联（包含固定题目 + 随机题目）
            List<ExamPaperQuestion> paperQuestions = new ArrayList<>();
            for (int i = 0; i < allQuestions.size(); i++)
            {
                ExamQuestion question = allQuestions.get(i);
                ExamPaperQuestion pq = new ExamPaperQuestion();
                pq.setPaperId(paper.getId());
                pq.setQuestionId(question.getId());
                pq.setQuestionOrder(i + 1);
                pq.setScore(question.getScore());
                paperQuestions.add(pq);
            }

            if (!paperQuestions.isEmpty())
            {
                paperMapper.batchInsertPaperQuestion(paperQuestions);
            }
        }

        return result;
    }

    /**
     * 查询试卷关联的试题列表
     *
     * @param paperId 试卷ID
     * @return 试题列表
     */
    @Override
    public List<ExamPaperQuestion> selectPaperQuestions(Long paperId)
    {
        List<ExamPaperQuestion> paperQuestions = paperMapper.selectPaperQuestions(paperId);
        fillQuestionMedia(paperQuestions);
        return paperQuestions;
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

    private void fillGeneratedQuestionMedia(List<ExamQuestion> questions)
    {
        if (StringUtils.isEmpty(questions))
        {
            return;
        }

        List<Long> questionIds = new ArrayList<>();
        for (ExamQuestion question : questions)
        {
            if (question != null && question.getId() != null)
            {
                questionIds.add(question.getId());
            }
        }
        if (questionIds.isEmpty())
        {
            return;
        }

        Map<Long, List<ExamQuestionMedia>> mediaMap = questionMediaService.selectMediaMapByQuestionIds(questionIds);
        for (ExamQuestion question : questions)
        {
            if (question != null && question.getId() != null)
            {
                question.setMediaList(mediaMap.get(question.getId()));
            }
        }
    }

    /**
     * 根据试卷配置生成实际试题（用于随机试卷）
     *
     * @param paperId 试卷ID
     * @return 生成的试题列表
     */
    @Override
    public List<ExamQuestion> generatePaperQuestions(Long paperId)
    {
        ExamPaper paper = paperMapper.selectPaperById(paperId);
        if (paper == null || StringUtils.isEmpty(paper.getQuestionConfig()))
        {
            return new ArrayList<>();
        }

        List<ExamQuestion> questions = new ArrayList<>();

        try
        {
            if ("random".equals(paper.getPaperType()))
            {
                // 纯随机试卷 - 直接解析为数组
                JSONArray configArray = JSON.parseArray(paper.getQuestionConfig());
                questions = generateRandomQuestionsFromArray(configArray);
            }
            else if ("mixed".equals(paper.getPaperType()))
            {
                // 混合试卷：固定试题 + 随机试题 - 解析为对象
                JSONObject config = JSON.parseObject(paper.getQuestionConfig());

                // 1. 获取固定试题
                JSONArray fixedQuestions = config.getJSONArray("fixedQuestions");
                if (fixedQuestions != null)
                {
                    for (int i = 0; i < fixedQuestions.size(); i++)
                    {
                        JSONObject fixedQuestion = fixedQuestions.getJSONObject(i);
                        Long questionId = fixedQuestion.getLong("questionId");
                        ExamQuestion question = questionMapper.selectQuestionById(questionId);
                        if (question != null)
                        {
                            question.setScore(fixedQuestion.getBigDecimal("score"));
                            questions.add(question);
                        }
                    }
                }

                // 2. 生成随机试题
                JSONArray randomConfig = config.getJSONArray("randomConfig");
                if (randomConfig != null)
                {
                    List<ExamQuestion> randomQuestions = generateRandomQuestionsFromArray(randomConfig);
                    questions.addAll(randomQuestions);
                }
            }

            // 如果设置了打乱题目顺序
            if ("1".equals(paper.getShuffleQuestions()))
            {
                Collections.shuffle(questions);
            }

            // 如果设置了打乱选项顺序
            if ("1".equals(paper.getShuffleOptions()))
            {
                for (ExamQuestion question : questions)
                {
                    QuestionTypeUtils.shuffleQuestionOptions(question);
                }
            }
            fillGeneratedQuestionMedia(questions);

        }
        catch (Exception e)
        {
            throw new ServiceException("生成试卷题目失败：" + e.getMessage());
        }

        return questions;
    }

    /**
     * 更新试卷题目数量和总分
     *
     * @param paperId 试卷ID
     */
    @Override
    public void updatePaperStatistics(Long paperId)
    {
        ExamPaper paper = paperMapper.selectPaperById(paperId);
        if (paper != null && "fixed".equals(paper.getPaperType()))
        {
            List<ExamPaperQuestion> paperQuestions = paperMapper.selectPaperQuestions(paperId);

            int questionCount = paperQuestions.size();
            BigDecimal totalScore = BigDecimal.ZERO;

            for (ExamPaperQuestion pq : paperQuestions)
            {
                totalScore = totalScore.add(pq.getScore());
            }

            paper.setQuestionCount(questionCount);
            paper.setTotalScore(totalScore);
            paper.setUpdateBy(SecurityUtils.getUsername());
            paper.setUpdateTime(new Date());

            paperMapper.updatePaper(paper);
        }
    }

    /**
     * 根据配置计算试卷统计信息
     *
     * @param paper 试卷对象
     * @param questionConfig 试题配置
     */
    private void calculatePaperStatistics(ExamPaper paper, String questionConfig)
    {
        try
        {
            // 前端发送的是数组格式，不是对象格式
            JSONArray configArray = JSON.parseArray(questionConfig);
            int totalCount = 0;
            BigDecimal totalScore = BigDecimal.ZERO;

            // 遍历配置数组中的每种题型
            for (int i = 0; i < configArray.size(); i++)
            {
                JSONObject typeConfig = configArray.getJSONObject(i);
                if (typeConfig != null)
                {
                    Integer count = typeConfig.getInteger("questionCount");
                    BigDecimal score = typeConfig.getBigDecimal("questionScore");

                    if (count != null && score != null)
                    {
                        totalCount += count;
                        totalScore = totalScore.add(score.multiply(new BigDecimal(count)));
                    }
                }
            }

            paper.setQuestionCount(totalCount);
            paper.setTotalScore(totalScore);
        }
        catch (Exception e)
        {
            throw new ServiceException("解析试题配置失败：" + e.getMessage());
        }
    }

    /**
     * 根据配置生成随机试题
     *
     * @param config 随机试题配置
     * @return 试题列表
     */
    private List<ExamQuestion> generateRandomQuestions(JSONObject config)
    {
        List<ExamQuestion> questions = new ArrayList<>();

        for (String key : config.keySet())
        {
            JSONObject typeConfig = config.getJSONObject(key);
            if (typeConfig != null)
            {
                Long categoryId = typeConfig.getLong("categoryId");
                String questionType = typeConfig.getString("questionType");
                Integer difficultyLevel = typeConfig.getInteger("difficultyLevel");
                Integer count = typeConfig.getInteger("questionCount");
                BigDecimal score = typeConfig.getBigDecimal("questionScore");

                if (count != null && count > 0)
                {
                    List<ExamQuestion> randomQuestions = paperMapper.selectRandomQuestions(
                        categoryId, questionType, difficultyLevel, count);

                    // 设置题目分值
                    for (ExamQuestion question : randomQuestions)
                    {
                        question.setScore(score);
                    }

                    questions.addAll(randomQuestions);
                }
            }
        }

        return questions;
    }

    /**
     * 根据数组配置生成随机试题
     *
     * @param configArray 随机试题配置数组
     * @return 试题列表
     */
    private List<ExamQuestion> generateRandomQuestionsFromArray(JSONArray configArray)
    {
        List<ExamQuestion> questions = new ArrayList<>();

        if (configArray == null || configArray.size() == 0) {
            return questions;
        }

        for (int i = 0; i < configArray.size(); i++)
        {
            JSONObject typeConfig = configArray.getJSONObject(i);
            if (typeConfig != null)
            {
                // categoryId 可能为空，使用null表示不限制分类
                Long categoryId = typeConfig.getLong("categoryId");
                String questionType = typeConfig.getString("questionType");
                Integer difficultyLevel = typeConfig.getInteger("difficultyLevel");
                Integer count = typeConfig.getInteger("questionCount");
                BigDecimal score = typeConfig.getBigDecimal("questionScore");

                if (count != null && count > 0 && questionType != null)
                {
                    List<ExamQuestion> randomQuestions = paperMapper.selectRandomQuestions(
                        categoryId, questionType, difficultyLevel, count);

                    if (randomQuestions != null && !randomQuestions.isEmpty()) {
                        // 设置题目分值
                        for (ExamQuestion question : randomQuestions)
                        {
                            question.setScore(score);
                        }
                        questions.addAll(randomQuestions);
                    }
                }
            }
        }

        return questions;
    }

    /**
     * 更新固定试卷
     *
     * @param paper 试卷信息
     * @param questions 选中的试题列表
     * @return 结果
     */
    @Override
    @Transactional
    public int updateFixedPaper(ExamPaper paper, List<ExamQuestion> questions)
    {
        // 计算试卷统计信息
        BigDecimal totalScore = BigDecimal.ZERO;
        for (ExamQuestion question : questions)
        {
            totalScore = totalScore.add(question.getScore());
        }
        paper.setTotalScore(totalScore);
        paper.setQuestionCount(questions.size());

        int result = paperMapper.updatePaper(paper);

        if (result > 0)
        {
            // 删除原有试卷试题关联
            paperMapper.deletePaperQuestionByPaperId(paper.getId());

            // 添加新的试卷试题关联
            List<ExamPaperQuestion> paperQuestions = new ArrayList<>();
            for (int i = 0; i < questions.size(); i++)
            {
                ExamQuestion question = questions.get(i);
                ExamPaperQuestion pq = new ExamPaperQuestion();
                pq.setPaperId(paper.getId());
                pq.setQuestionId(question.getId());
                pq.setQuestionOrder(i + 1);
                pq.setScore(question.getScore());
                paperQuestions.add(pq);
            }

            if (!paperQuestions.isEmpty())
            {
                paperMapper.batchInsertPaperQuestion(paperQuestions);
            }
        }

        return result;
    }

    /**
     * 更新混合试卷
     *
     * @param paper 试卷信息
     * @param fixedQuestions 固定试题列表
     * @param randomConfig 随机试题配置
     * @return 结果
     */
    @Override
    @Transactional
    public int updateMixedPaper(ExamPaper paper, List<ExamQuestion> fixedQuestions, String randomConfig)
    {
        // 组合配置：固定试题 + 随机试题配置
        JSONObject config = new JSONObject();

        // 固定试题配置
        JSONArray fixedArray = new JSONArray();
        for (ExamQuestion question : fixedQuestions)
        {
            JSONObject fixedQuestion = new JSONObject();
            fixedQuestion.put("questionId", question.getId());
            fixedQuestion.put("score", question.getScore());
            fixedArray.add(fixedQuestion);
        }
        config.put("fixedQuestions", fixedArray);

        // 随机试题配置
        if (randomConfig != null && !randomConfig.isEmpty())
        {
            config.put("randomConfig", JSON.parseArray(randomConfig));
        }

        paper.setQuestionConfig(config.toJSONString());

        // 准备所有题目列表（固定题目 + 随机生成的题目）
        List<ExamQuestion> allQuestions = new ArrayList<>(fixedQuestions);

        // 生成随机题目
        if (randomConfig != null && !randomConfig.isEmpty())
        {
            try
            {
                JSONArray randomConfigArray = JSON.parseArray(randomConfig);
                List<ExamQuestion> randomQuestions = generateRandomQuestionsFromArray(randomConfigArray);
                allQuestions.addAll(randomQuestions);
            }
            catch (Exception e)
            {
                throw new ServiceException("生成随机题目失败：" + e.getMessage());
            }
        }

        // 计算试卷统计信息
        BigDecimal actualTotalScore = BigDecimal.ZERO;
        for (ExamQuestion question : allQuestions) {
            actualTotalScore = actualTotalScore.add(question.getScore());
        }
        paper.setTotalScore(actualTotalScore);
        paper.setQuestionCount(allQuestions.size());

        int result = paperMapper.updatePaper(paper);

        if (result > 0)
        {
            // 删除原有试卷试题关联
            paperMapper.deletePaperQuestionByPaperId(paper.getId());

            // 添加新的试卷试题关联（包含固定题目 + 随机题目）
            List<ExamPaperQuestion> paperQuestions = new ArrayList<>();
            for (int i = 0; i < allQuestions.size(); i++)
            {
                ExamQuestion question = allQuestions.get(i);
                ExamPaperQuestion pq = new ExamPaperQuestion();
                pq.setPaperId(paper.getId());
                pq.setQuestionId(question.getId());
                pq.setQuestionOrder(i + 1);
                pq.setScore(question.getScore());
                paperQuestions.add(pq);
            }

            if (!paperQuestions.isEmpty())
            {
                paperMapper.batchInsertPaperQuestion(paperQuestions);
            }
        }

        return result;
    }

    /**
     * 重新生成随机试卷题目
     *
     * @param paperId 试卷ID
     * @return 结果
     */
    @Override
    @Transactional
    public int regenerateRandomPaperQuestions(Long paperId)
    {
        ExamPaper paper = paperMapper.selectPaperById(paperId);
        if (paper == null || !"random".equals(paper.getPaperType())) {
            throw new ServiceException("只能重新生成随机试卷的题目");
        }

        if (StringUtils.isEmpty(paper.getQuestionConfig())) {
            throw new ServiceException("试卷配置为空，无法重新生成题目");
        }

        try {
            // 删除原有题目关联
            paperMapper.deletePaperQuestionByPaperId(paperId);

            // 根据配置重新生成题目
            JSONArray configArray = JSON.parseArray(paper.getQuestionConfig());
            List<ExamQuestion> generatedQuestions = generateRandomQuestionsFromArray(configArray);

            if (!generatedQuestions.isEmpty()) {
                // 保存新生成的题目到试卷题目关联表
                List<ExamPaperQuestion> paperQuestions = new ArrayList<>();
                for (int i = 0; i < generatedQuestions.size(); i++) {
                    ExamQuestion question = generatedQuestions.get(i);
                    ExamPaperQuestion pq = new ExamPaperQuestion();
                    pq.setPaperId(paperId);
                    pq.setQuestionId(question.getId());
                    pq.setQuestionOrder(i + 1);
                    pq.setScore(question.getScore());
                    paperQuestions.add(pq);
                }

                // 批量插入题目关联
                paperMapper.batchInsertPaperQuestion(paperQuestions);

                // 更新试卷的实际题目数量和总分
                BigDecimal actualTotalScore = BigDecimal.ZERO;
                for (ExamQuestion question : generatedQuestions) {
                    actualTotalScore = actualTotalScore.add(question.getScore());
                }
                paper.setQuestionCount(generatedQuestions.size());
                paper.setTotalScore(actualTotalScore);
                paper.setUpdateBy(SecurityUtils.getUsername());
                paper.setUpdateTime(new Date());
                paperMapper.updatePaper(paper);

                return 1;
            } else {
                throw new ServiceException("根据配置无法生成题目，请检查题库是否有足够的题目数据");
            }
        } catch (Exception e) {
            throw new ServiceException("重新生成随机试卷题目失败：" + e.getMessage());
        }
    }
}
