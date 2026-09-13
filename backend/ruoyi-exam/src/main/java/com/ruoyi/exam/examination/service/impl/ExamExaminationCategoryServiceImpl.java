package com.ruoyi.exam.examination.service.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.exam.examination.domain.ExamExaminationCategory;
import com.ruoyi.exam.examination.mapper.ExamExaminationCategoryMapper;
import com.ruoyi.exam.examination.service.IExamExaminationCategoryService;

/**
 * 考试分类Service业务层处理
 * 
 * @author ruoyi
 * @date 2024-06-10
 */
@Service
public class ExamExaminationCategoryServiceImpl implements IExamExaminationCategoryService 
{
    @Autowired
    private ExamExaminationCategoryMapper examExaminationCategoryMapper;

    /**
     * 查询考试分类列表
     * 
     * @param examExaminationCategory 考试分类
     * @return 考试分类
     */
    @Override
    public List<ExamExaminationCategory> selectExamExaminationCategoryList(ExamExaminationCategory examExaminationCategory)
    {
        return examExaminationCategoryMapper.selectExamExaminationCategoryList(examExaminationCategory);
    }

    /**
     * 构建前端所需要下拉树结构
     * 
     * @param categories 考试分类列表
     * @return 下拉树结构列表
     */
    @Override
    public List<ExamExaminationCategory> buildCategoryTreeSelect(List<ExamExaminationCategory> categories)
    {
        return buildCategoryTree(categories);
    }

    /**
     * 构建前端所需要树结构
     * 
     * @param categories 考试分类列表
     * @return 树结构列表
     */
    @Override
    public List<ExamExaminationCategory> buildCategoryTree(List<ExamExaminationCategory> categories)
    {
        List<ExamExaminationCategory> returnList = new ArrayList<>();
        List<Long> tempList = new ArrayList<>();
        for (ExamExaminationCategory category : categories)
        {
            tempList.add(category.getId());
        }
        for (Iterator<ExamExaminationCategory> iterator = categories.iterator(); iterator.hasNext();)
        {
            ExamExaminationCategory category = iterator.next();
            // 如果是顶级节点, 遍历该父节点的所有子节点
            if (!tempList.contains(category.getParentId()))
            {
                recursionFn(categories, category);
                returnList.add(category);
            }
        }
        if (returnList.isEmpty())
        {
            returnList = categories;
        }
        return returnList;
    }

    /**
     * 根据考试分类ID查询信息
     * 
     * @param id 考试分类ID
     * @return 考试分类信息
     */
    @Override
    public ExamExaminationCategory selectExamExaminationCategoryById(Long id)
    {
        return examExaminationCategoryMapper.selectExamExaminationCategoryById(id);
    }

    /**
     * 根据用户ID查询考试分类列表
     * 
     * @param userId 用户ID
     * @return 考试分类列表
     */
    @Override
    public List<ExamExaminationCategory> selectExamExaminationCategorysByUserId(Long userId)
    {
        List<ExamExaminationCategory> userCategories = examExaminationCategoryMapper.selectExamExaminationCategorysByUserId(userId);
        return userCategories;
    }

    /**
     * 校验考试分类名称是否唯一
     * 
     * @param category 考试分类信息
     * @return 结果
     */
    @Override
    public String checkCategoryNameUnique(ExamExaminationCategory category)
    {
        Long categoryId = StringUtils.isNull(category.getId()) ? -1L : category.getId();
        ExamExaminationCategory info = examExaminationCategoryMapper.checkCategoryNameUnique(category.getCategoryName(), category.getParentId());
        if (StringUtils.isNotNull(info) && info.getId().longValue() != categoryId.longValue())
        {
            return "1";
        }
        return "0";
    }

    /**
     * 根据角色ID查询考试分类
     * 
     * @param roleId 角色ID
     * @return 考试分类列表
     */
    @Override
    public List<Long> selectExamExaminationCategoryListByRoleId(Long roleId)
    {
        return examExaminationCategoryMapper.selectExamExaminationCategoryListByRoleId(roleId, true);
    }

    /**
     * 校验考试分类是否有数据权限
     * 
     * @param categoryId 考试分类id
     * @return 结果
     */
    @Override
    public boolean checkCategoryDataScope(Long categoryId)
    {
        // 这里可以根据实际需求实现数据权限校验
        return true;
    }

    /**
     * 查询考试分类是否存在用户
     * 
     * @param categoryId 考试分类ID
     * @return 结果 true 存在 false 不存在
     */
    @Override
    public boolean checkCategoryExistUser(Long categoryId)
    {
        int result = examExaminationCategoryMapper.checkExamExaminationCategoryExistUser(categoryId);
        return result > 0;
    }

    /**
     * 检查考试分类是否有子分类
     * 
     * @param categoryId 考试分类ID
     * @return 结果 true 存在 false 不存在
     */
    @Override
    public boolean hasChildByCategoryId(Long categoryId)
    {
        int result = examExaminationCategoryMapper.selectChildrenCategoryById(categoryId);
        return result > 0;
    }

    /**
     * 新增考试分类
     * 
     * @param examExaminationCategory 考试分类
     * @return 结果
     */
    @Override
    public int insertExamExaminationCategory(ExamExaminationCategory examExaminationCategory)
    {
        // 如果不是根分类，需要检查父分类状态
        if (examExaminationCategory.getParentId() != null && examExaminationCategory.getParentId() != 0)
        {
            ExamExaminationCategory info = examExaminationCategoryMapper.selectExamExaminationCategoryById(examExaminationCategory.getParentId());
            // 如果父节点不存在或不为正常状态,则不允许新增子节点
            if (StringUtils.isNull(info))
            {
                throw new ServiceException("父级分类不存在");
            }
            if (!"0".equals(info.getStatus()))
            {
                throw new ServiceException("考试分类停用，不允许新增");
            }
        }
        examExaminationCategory.setCreateBy(SecurityUtils.getUsername());
        return examExaminationCategoryMapper.insertExamExaminationCategory(examExaminationCategory);
    }

    /**
     * 修改考试分类
     * 
     * @param examExaminationCategory 考试分类
     * @return 结果
     */
    @Override
    public int updateExamExaminationCategory(ExamExaminationCategory examExaminationCategory)
    {
        ExamExaminationCategory oldCategory = examExaminationCategoryMapper.selectExamExaminationCategoryById(examExaminationCategory.getId());
        if (StringUtils.isNull(oldCategory))
        {
            throw new ServiceException("考试分类不存在");
        }
        
        // 检查是否修改为自己的子分类
        if (examExaminationCategory.getParentId().longValue() == examExaminationCategory.getId().longValue())
        {
            throw new ServiceException("修改考试分类'" + examExaminationCategory.getCategoryName() + "'失败，上级分类不能是自己");
        }
        
        // 如果不是根分类，需要检查父分类状态
        if (examExaminationCategory.getParentId() != null && examExaminationCategory.getParentId() != 0)
        {
            ExamExaminationCategory newParentCategory = examExaminationCategoryMapper.selectExamExaminationCategoryById(examExaminationCategory.getParentId());
            if (StringUtils.isNull(newParentCategory))
            {
                throw new ServiceException("父级分类不存在");
            }
            if (!"0".equals(newParentCategory.getStatus()))
            {
                throw new ServiceException("考试分类停用，不允许修改");
            }
        }
        
        examExaminationCategory.setUpdateBy(SecurityUtils.getUsername());
        return examExaminationCategoryMapper.updateExamExaminationCategory(examExaminationCategory);
    }

    /**
     * 批量删除考试分类
     * 
     * @param ids 需要删除的考试分类主键集合
     * @return 结果
     */
    @Override
    public int deleteExamExaminationCategoryByIds(Long[] ids)
    {
        for (Long id : ids)
        {
            ExamExaminationCategory category = selectExamExaminationCategoryById(id);
            if (category == null)
            {
                continue;
            }
            
            // 检查是否有子分类
            if (hasChildByCategoryId(id))
            {
                throw new ServiceException(String.format("分类【%s】存在子分类，不允许删除", category.getCategoryName()));
            }
            
            // 检查是否有考试在使用这个分类
            if (checkCategoryExistExamination(id))
            {
                throw new ServiceException(String.format("分类【%s】下存在考试，不允许删除", category.getCategoryName()));
            }
        }
        
        return examExaminationCategoryMapper.deleteExamExaminationCategoryByIds(ids);
    }

    /**
     * 删除考试分类管理信息
     * 
     * @param categoryId 考试分类ID
     * @return 结果
     */
    @Override
    public int deleteExamExaminationCategoryById(Long categoryId)
    {
        ExamExaminationCategory category = selectExamExaminationCategoryById(categoryId);
        if (category == null)
        {
            throw new ServiceException("考试分类不存在");
        }
        
        // 检查是否有子分类
        if (hasChildByCategoryId(categoryId))
        {
            throw new ServiceException(String.format("分类【%s】存在子分类，不允许删除", category.getCategoryName()));
        }
        
        // 检查是否有考试在使用这个分类
        if (checkCategoryExistExamination(categoryId))
        {
            throw new ServiceException(String.format("分类【%s】下存在考试，不允许删除", category.getCategoryName()));
        }
        
        return examExaminationCategoryMapper.deleteExamExaminationCategoryById(categoryId);
    }
    
    /**
     * 检查分类下是否存在考试
     * 
     * @param categoryId 分类ID
     * @return 是否存在考试
     */
    private boolean checkCategoryExistExamination(Long categoryId)
    {
        int result = examExaminationCategoryMapper.checkCategoryExistExamination(categoryId);
        return result > 0;
    }

    /**
     * 递归列表
     */
    private void recursionFn(List<ExamExaminationCategory> list, ExamExaminationCategory t)
    {
        // 得到子节点列表
        List<ExamExaminationCategory> childList = getChildList(list, t);
        t.setChildren(childList);
        for (ExamExaminationCategory tChild : childList)
        {
            if (hasChild(list, tChild))
            {
                recursionFn(list, tChild);
            }
        }
    }

    /**
     * 得到子节点列表
     */
    private List<ExamExaminationCategory> getChildList(List<ExamExaminationCategory> list, ExamExaminationCategory t)
    {
        List<ExamExaminationCategory> tlist = new ArrayList<>();
        Iterator<ExamExaminationCategory> it = list.iterator();
        while (it.hasNext())
        {
            ExamExaminationCategory n = it.next();
            if (StringUtils.isNotNull(n.getParentId()) && n.getParentId().longValue() == t.getId().longValue())
            {
                tlist.add(n);
            }
        }
        return tlist;
    }

    /**
     * 判断是否有子节点
     */
    private boolean hasChild(List<ExamExaminationCategory> list, ExamExaminationCategory t)
    {
        return getChildList(list, t).size() > 0;
    }
}
