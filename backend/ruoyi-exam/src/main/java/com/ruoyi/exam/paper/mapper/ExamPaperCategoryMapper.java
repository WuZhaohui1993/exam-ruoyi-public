package com.ruoyi.exam.paper.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.exam.paper.domain.ExamPaperCategory;

/**
 * 试卷分类 数据层
 * 
 * @author ruoyi
 */
public interface ExamPaperCategoryMapper
{
    /**
     * 查询试卷分类列表
     * 
     * @param category 试卷分类信息
     * @return 试卷分类集合
     */
    public List<ExamPaperCategory> selectCategoryList(ExamPaperCategory category);

    /**
     * 查询所有试卷分类
     * 
     * @return 试卷分类列表
     */
    public List<ExamPaperCategory> selectCategoryAll();

    /**
     * 根据分类ID查询试卷分类信息
     * 
     * @param id 分类ID
     * @return 试卷分类信息
     */
    public ExamPaperCategory selectCategoryById(Long id);

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
    public ExamPaperCategory checkCategoryNameUnique(@Param("categoryName") String categoryName, @Param("parentId") Long parentId);

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
     * 删除试卷分类
     * 
     * @param id 分类ID
     * @return 结果
     */
    public int deleteCategoryById(Long id);

    /**
     * 批量删除试卷分类
     * 
     * @param ids 需要删除的分类ID
     * @return 结果
     */
    public int deleteCategoryByIds(Long[] ids);

    /**
     * 批量修改子元素关系
     * 
     * @param ids 需要修改的数据主键集合
     * @return 结果
     */
    public int updateCategoryStatusNormal(Long[] ids);

    /**
     * 统计该分类下的试卷数量
     * 
     * @param categoryId 分类ID
     * @return 试卷数量
     */
    public int selectPaperCountByCategoryId(Long categoryId);
} 