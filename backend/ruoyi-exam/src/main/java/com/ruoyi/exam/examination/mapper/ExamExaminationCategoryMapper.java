package com.ruoyi.exam.examination.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.exam.examination.domain.ExamExaminationCategory;

/**
 * 考试分类Mapper接口
 * 
 * @author ruoyi
 */
public interface ExamExaminationCategoryMapper 
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
     * 根据角色ID查询考试分类树信息
     * 
     * @param roleId 角色ID
     * @param categoryCheckStrictly 分类遍历是否严格遵循父子节点
     * @return 选中考试分类列表
     */
    public List<Long> selectExamExaminationCategoryListByRoleId(@Param("roleId") Long roleId, @Param("categoryCheckStrictly") boolean categoryCheckStrictly);

    /**
     * 根据用户ID查询考试分类
     * 
     * @param userId 用户ID
     * @return 考试分类列表
     */
    public List<ExamExaminationCategory> selectExamExaminationCategorysByUserId(Long userId);

    /**
     * 查询考试分类是否存在用户
     * 
     * @param categoryId 考试分类ID
     * @return 结果
     */
    public int checkExamExaminationCategoryExistUser(Long categoryId);

    /**
     * 校验考试分类名称是否唯一
     * 
     * @param categoryName 考试分类名称
     * @param parentId 父考试分类ID
     * @return 结果
     */
    public ExamExaminationCategory checkCategoryNameUnique(@Param("categoryName") String categoryName, @Param("parentId") Long parentId);

    /**
     * 查询考试分类子节点数量
     * 
     * @param parentId 父考试分类ID
     * @return 结果
     */
    public int selectChildrenCategoryById(Long parentId);

    /**
     * 根据父级分类查询所有子分类（正常状态）
     * 
     * @param parentId 父级分类ID
     * @return 分类列表
     */
    public List<ExamExaminationCategory> selectChildrenCategoryByParentId(Long parentId);

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
     * 删除考试分类
     * 
     * @param id 考试分类主键
     * @return 结果
     */
    public int deleteExamExaminationCategoryById(Long id);

    /**
     * 批量删除考试分类
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteExamExaminationCategoryByIds(Long[] ids);
    
    /**
     * 检查分类下是否存在考试
     * 
     * @param categoryId 分类ID
     * @return 考试数量
     */
    public int checkCategoryExistExamination(Long categoryId);
} 