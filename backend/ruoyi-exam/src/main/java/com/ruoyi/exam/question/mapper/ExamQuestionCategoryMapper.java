package com.ruoyi.exam.question.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.exam.question.domain.ExamQuestionCategory;

/**
 * 试题分类 数据层
 * 
 * @author ruoyi
 */
public interface ExamQuestionCategoryMapper
{
    /**
     * 查询试题分类列表
     * 
     * @param category 试题分类信息
     * @return 试题分类集合
     */
    public List<ExamQuestionCategory> selectCategoryList(ExamQuestionCategory category);

    /**
     * 查询所有试题分类
     * 
     * @return 试题分类列表
     */
    public List<ExamQuestionCategory> selectCategoryAll();

    /**
     * 根据分类ID查询试题分类信息
     * 
     * @param id 分类ID
     * @return 试题分类信息
     */
    public ExamQuestionCategory selectCategoryById(Long id);

    /**
     * 根据父分类ID查询子分类数量
     * 
     * @param parentId 父分类ID
     * @return 子分类数量
     */
    public int selectChildrenCategoryCount(Long parentId);

    /**
     * 检查分类名称是否唯一
     * 
     * @param categoryName 分类名称
     * @param parentId 父分类ID
     * @return 分类信息
     */
    public ExamQuestionCategory checkCategoryNameUnique(@Param("categoryName") String categoryName, @Param("parentId") Long parentId);

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
     * 删除试题分类
     * 
     * @param id 分类ID
     * @return 结果
     */
    public int deleteCategoryById(Long id);

    /**
     * 批量删除试题分类
     * 
     * @param ids 需要删除的分类ID
     * @return 结果
     */
    public int deleteCategoryByIds(Long[] ids);

    /**
     * 查询分类下试题数量
     * 
     * @param categoryId 分类ID
     * @return 试题数量
     */
    public int selectQuestionCountByCategoryId(Long categoryId);

    /**
     * 根据分类名称查询分类信息
     * 
     * @param categoryName 分类名称
     * @return 分类信息
     */
    public ExamQuestionCategory selectCategoryByName(@Param("categoryName") String categoryName);
} 