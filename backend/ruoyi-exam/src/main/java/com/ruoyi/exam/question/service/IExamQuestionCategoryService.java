package com.ruoyi.exam.question.service;

import java.util.List;
import com.ruoyi.common.core.domain.TreeSelect;
import com.ruoyi.exam.question.domain.ExamQuestionCategory;

/**
 * 试题分类 服务层
 * 
 * @author ruoyi
 */
public interface IExamQuestionCategoryService
{
    /**
     * 查询试题分类列表
     * 
     * @param category 试题分类信息
     * @return 试题分类集合
     */
    public List<ExamQuestionCategory> selectCategoryList(ExamQuestionCategory category);

    /**
     * 构建前端所需要的树结构
     * 
     * @param categories 试题分类列表
     * @return 树结构列表
     */
    public List<ExamQuestionCategory> buildCategoryTree(List<ExamQuestionCategory> categories);

    /**
     * 构建前端所需要下拉树结构
     * 
     * @param categories 试题分类列表
     * @return 下拉树结构列表
     */
    public List<TreeSelect> buildCategoryTreeSelect(List<ExamQuestionCategory> categories);

    /**
     * 根据分类ID查询信息
     * 
     * @param id 分类ID
     * @return 试题分类信息
     */
    public ExamQuestionCategory selectCategoryById(Long id);

    /**
     * 校验分类名称是否唯一
     * 
     * @param category 试题分类信息
     * @return 结果
     */
    public boolean checkCategoryNameUnique(ExamQuestionCategory category);

    /**
     * 新增试题分类
     * 
     * @param category 试题分类信息
     * @return 结果
     */
    public int insertCategory(ExamQuestionCategory category);

    /**
     * 修改试题分类
     * 
     * @param category 试题分类信息
     * @return 结果
     */
    public int updateCategory(ExamQuestionCategory category);

    /**
     * 批量删除试题分类
     * 
     * @param ids 需要删除的试题分类ID
     * @return 结果
     */
    public int deleteCategoryByIds(Long[] ids);

    /**
     * 删除试题分类信息
     * 
     * @param id 试题分类ID
     * @return 结果
     */
    public int deleteCategoryById(Long id);

    /**
     * 是否存在试题分类子节点
     * 
     * @param id 试题分类ID
     * @return 结果 true 存在 false 不存在
     */
    public boolean hasChildByCategoryId(Long id);

    /**
     * 查询分类是否存在试题
     * 
     * @param id 分类ID
     * @return 结果 true 存在 false 不存在
     */
    public boolean checkCategoryExistQuestion(Long id);

    /**
     * 根据分类名称查找分类ID
     * 
     * @param categoryName 分类名称
     * @return 分类ID
     */
    public Long findCategoryIdByName(String categoryName);
} 