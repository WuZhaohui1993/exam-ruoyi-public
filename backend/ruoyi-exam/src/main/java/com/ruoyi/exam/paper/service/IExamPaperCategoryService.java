package com.ruoyi.exam.paper.service;

import java.util.List;
import com.ruoyi.exam.paper.domain.ExamPaperCategory;

/**
 * 试卷分类 服务层
 * 
 * @author ruoyi
 */
public interface IExamPaperCategoryService
{
    /**
     * 查询试卷分类列表
     * 
     * @param category 试卷分类信息
     * @return 试卷分类集合
     */
    public List<ExamPaperCategory> selectCategoryList(ExamPaperCategory category);

    /**
     * 构建前端所需要的树结构
     * 
     * @param categories 试卷分类列表
     * @return 树结构列表
     */
    public List<ExamPaperCategory> buildCategoryTree(List<ExamPaperCategory> categories);

    /**
     * 构建前端所需要下拉树结构
     * 
     * @param categories 试卷分类列表
     * @return 下拉树结构列表
     */
    public List<ExamPaperCategory> buildCategoryTreeSelect(List<ExamPaperCategory> categories);

    /**
     * 根据分类ID查询信息
     * 
     * @param id 分类ID
     * @return 试卷分类信息
     */
    public ExamPaperCategory selectCategoryById(Long id);

    /**
     * 校验分类名称是否唯一
     * 
     * @param category 试卷分类信息
     * @return 结果
     */
    public boolean checkCategoryNameUnique(ExamPaperCategory category);

    /**
     * 新增试卷分类
     * 
     * @param category 试卷分类信息
     * @return 结果
     */
    public int insertCategory(ExamPaperCategory category);

    /**
     * 修改试卷分类
     * 
     * @param category 试卷分类信息
     * @return 结果
     */
    public int updateCategory(ExamPaperCategory category);

    /**
     * 批量删除试卷分类
     * 
     * @param ids 需要删除的试卷分类ID
     * @return 结果
     */
    public int deleteCategoryByIds(Long[] ids);

    /**
     * 删除试卷分类信息
     * 
     * @param id 试卷分类ID
     * @return 结果
     */
    public int deleteCategoryById(Long id);
} 