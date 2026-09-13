package com.ruoyi.exam.paper.service;

import java.util.List;
import com.ruoyi.exam.paper.domain.ExamPaper;
import com.ruoyi.exam.paper.domain.ExamPaperQuestion;
import com.ruoyi.exam.question.domain.ExamQuestion;

/**
 * 试卷 服务层
 * 
 * @author ruoyi
 */
public interface IExamPaperService
{
    /**
     * 查询试卷列表
     * 
     * @param paper 试卷信息
     * @return 试卷集合
     */
    public List<ExamPaper> selectPaperList(ExamPaper paper);

    /**
     * 根据试卷ID查询试卷信息
     * 
     * @param id 试卷ID
     * @return 试卷信息
     */
    public ExamPaper selectPaperById(Long id);

    /**
     * 根据分类ID查询试卷列表
     * 
     * @param categoryId 分类ID
     * @return 试卷列表
     */
    public List<ExamPaper> selectPapersByCategoryId(Long categoryId);

    /**
     * 根据试卷类型查询试卷列表
     * 
     * @param paperType 试卷类型
     * @return 试卷列表
     */
    public List<ExamPaper> selectPapersByType(String paperType);

    /**
     * 校验试卷名称是否唯一
     * 
     * @param paper 试卷信息
     * @return 结果
     */
    public boolean checkPaperNameUnique(ExamPaper paper);

    /**
     * 新增试卷
     * 
     * @param paper 试卷信息
     * @return 结果
     */
    public int insertPaper(ExamPaper paper);

    /**
     * 修改试卷
     * 
     * @param paper 试卷信息
     * @return 结果
     */
    public int updatePaper(ExamPaper paper);

    /**
     * 批量删除试卷
     * 
     * @param ids 需要删除的试卷ID
     * @return 结果
     */
    public int deletePaperByIds(Long[] ids);

    /**
     * 删除试卷信息
     * 
     * @param id 试卷ID
     * @return 结果
     */
    public int deletePaperById(Long id);

    /**
     * 统计试卷数量
     * 
     * @param paper 查询条件
     * @return 试卷数量
     */
    public int countPapers(ExamPaper paper);

    /**
     * 预览试卷
     * 
     * @param id 试卷ID
     * @return 试卷详细信息（包含试题）
     */
    public ExamPaper previewPaper(Long id);

    /**
     * 复制试卷
     * 
     * @param id 试卷ID
     * @return 结果
     */
    public int copyPaper(Long id);

    /**
     * 创建固定试卷
     * 
     * @param paper 试卷信息
     * @param questions 选中的试题列表
     * @return 结果
     */
    public int createFixedPaper(ExamPaper paper, List<ExamQuestion> questions);

    /**
     * 创建随机试卷
     * 
     * @param paper 试卷信息
     * @param questionConfig 随机组卷配置（JSON格式）
     * @return 结果
     */
    public int createRandomPaper(ExamPaper paper, String questionConfig);

    /**
     * 创建混合试卷
     * 
     * @param paper 试卷信息
     * @param fixedQuestions 固定试题列表
     * @param randomConfig 随机试题配置
     * @return 结果
     */
    public int createMixedPaper(ExamPaper paper, List<ExamQuestion> fixedQuestions, String randomConfig);

    /**
     * 查询试卷关联的试题列表
     * 
     * @param paperId 试卷ID
     * @return 试题列表
     */
    public List<ExamPaperQuestion> selectPaperQuestions(Long paperId);

    /**
     * 根据试卷配置生成实际试题（用于随机试卷）
     * 
     * @param paperId 试卷ID
     * @return 生成的试题列表
     */
    public List<ExamQuestion> generatePaperQuestions(Long paperId);

    /**
     * 更新试卷题目数量和总分
     * 
     * @param paperId 试卷ID
     */
    public void updatePaperStatistics(Long paperId);

    /**
     * 更新固定试卷
     * 
     * @param paper 试卷信息
     * @param questions 选中的试题列表
     * @return 结果
     */
    public int updateFixedPaper(ExamPaper paper, List<ExamQuestion> questions);

    /**
     * 更新混合试卷
     * 
     * @param paper 试卷信息
     * @param fixedQuestions 固定试题列表
     * @param randomConfig 随机试题配置
     * @return 结果
     */
    public int updateMixedPaper(ExamPaper paper, List<ExamQuestion> fixedQuestions, String randomConfig);

    /**
     * 重新生成随机试卷题目
     * 
     * @param paperId 试卷ID
     * @return 结果
     */
    public int regenerateRandomPaperQuestions(Long paperId);
} 