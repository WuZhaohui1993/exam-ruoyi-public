package com.ruoyi.exam.examination.service;

import java.util.List;
import com.ruoyi.exam.examination.domain.ExamExaminationCategory;

/**
 * 考试分类Service接口
 * 
 * @author ruoyi
 */
public interface IExamExaminationCategoryService 
{
    /**
     * 查询考试分类
     * 
     * @param id 考试分类主键
     * @return 考试分类
     */
    public ExamExaminationCategory selectExamExaminationCategoryById(Long id);

    /**
     * 查询考试分类列表
     * 
     * @param examExaminationCategory 考试分类
     * @return 考试分类集合
     */
    public List<ExamExaminationCategory> selectExamExaminationCategoryList(ExamExaminationCategory examExaminationCategory);

    /**
     * 构建前端所需要下拉树结构
     * 
     * @param examCategories 考试分类列表
     * @return 下拉树结构列表
     */
    public List<ExamExaminationCategory> buildCategoryTree(List<ExamExaminationCategory> examCategories);

    /**
     * 构建前端所需要树结构
     * 
     * @param examCategories 考试分类列表
     * @return 树结构列表
     */
    public List<ExamExaminationCategory> buildCategoryTreeSelect(List<ExamExaminationCategory> examCategories);

    /**
     * 根据角色ID查询考试分类
     * 
     * @param roleId 角色ID
     * @return 考试分类列表
     */
    public List<Long> selectExamExaminationCategoryListByRoleId(Long roleId);

    /**
     * 根据用户ID查询考试分类权限
     * 
     * @param userId 用户ID
     * @return 考试分类权限
     */
    public List<ExamExaminationCategory> selectExamExaminationCategorysByUserId(Long userId);

    /**
     * 校验考试分类名称是否唯一
     * 
     * @param examExaminationCategory 考试分类信息
     * @return 结果
     */
    public String checkCategoryNameUnique(ExamExaminationCategory examExaminationCategory);

    /**
     * 校验考试分类是否有数据权限
     * 
     * @param categoryId 考试分类id
     * @return 结果
     */
    public boolean checkCategoryDataScope(Long categoryId);

    /**
     * 查询考试分类是否存在用户
     * 
     * @param categoryId 考试分类ID
     * @return 结果 true 存在 false 不存在
     */
    public boolean checkCategoryExistUser(Long categoryId);

    /**
     * 查询考试分类是否存在子节点
     * 
     * @param categoryId 考试分类ID
     * @return 结果 true 存在 false 不存在
     */
    public boolean hasChildByCategoryId(Long categoryId);

    /**
     * 新增考试分类
     * 
     * @param examExaminationCategory 考试分类
     * @return 结果
     */
    public int insertExamExaminationCategory(ExamExaminationCategory examExaminationCategory);

    /**
     * 修改考试分类
     * 
     * @param examExaminationCategory 考试分类
     * @return 结果
     */
    public int updateExamExaminationCategory(ExamExaminationCategory examExaminationCategory);

    /**
     * 批量删除考试分类
     * 
     * @param ids 需要删除的考试分类主键集合
     * @return 结果
     */
    public int deleteExamExaminationCategoryByIds(Long[] ids);

    /**
     * 删除考试分类信息
     * 
     * @param id 考试分类主键
     * @return 结果
     */
    public int deleteExamExaminationCategoryById(Long id);
} 