package com.ruoyi.exam.question.service.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.constant.UserConstants;
import com.ruoyi.common.core.domain.TreeSelect;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.exam.question.domain.ExamQuestionCategory;
import com.ruoyi.exam.question.mapper.ExamQuestionCategoryMapper;
import com.ruoyi.exam.question.service.IExamQuestionCategoryService;

/**
 * 试题分类 服务层处理
 * 
 * @author ruoyi
 */
@Service
public class ExamQuestionCategoryServiceImpl implements IExamQuestionCategoryService
{
    @Autowired
    private ExamQuestionCategoryMapper categoryMapper;

    /**
     * 查询试题分类列表
     * 
     * @param category 试题分类信息
     * @return 试题分类集合
     */
    @Override
    public List<ExamQuestionCategory> selectCategoryList(ExamQuestionCategory category)
    {
        return categoryMapper.selectCategoryList(category);
    }

    /**
     * 构建前端所需要的树结构
     * 
     * @param categories 试题分类列表
     * @return 树结构列表
     */
    @Override
    public List<ExamQuestionCategory> buildCategoryTree(List<ExamQuestionCategory> categories)
    {
        List<ExamQuestionCategory> returnList = new ArrayList<ExamQuestionCategory>();
        List<Long> tempList = categories.stream().map(ExamQuestionCategory::getId).collect(Collectors.toList());
        for (ExamQuestionCategory category : categories)
        {
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
     * 构建前端所需要下拉树结构
     * 
     * @param categories 试题分类列表
     * @return 下拉树结构列表
     */
    @Override
    public List<TreeSelect> buildCategoryTreeSelect(List<ExamQuestionCategory> categories)
    {
        List<ExamQuestionCategory> categoryTrees = buildCategoryTree(categories);
        return categoryTrees.stream().map(this::convertToTreeSelect).collect(Collectors.toList());
    }

    /**
     * 转换ExamQuestionCategory为TreeSelect
     */
    private TreeSelect convertToTreeSelect(ExamQuestionCategory category)
    {
        TreeSelect treeSelect = new TreeSelect();
        treeSelect.setId(category.getId());
        treeSelect.setLabel(category.getCategoryName());
        treeSelect.setDisabled(StringUtils.equals(UserConstants.DEPT_DISABLE, category.getStatus()));
        
        // 修正children判断逻辑
        if (category.getChildren() != null && !category.getChildren().isEmpty())
        {
            treeSelect.setChildren(category.getChildren().stream().map(this::convertToTreeSelect).collect(Collectors.toList()));
        }
        
        return treeSelect;
    }

    /**
     * 根据分类ID查询信息
     * 
     * @param id 分类ID
     * @return 试题分类信息
     */
    @Override
    public ExamQuestionCategory selectCategoryById(Long id)
    {
        return categoryMapper.selectCategoryById(id);
    }

    /**
     * 校验分类名称是否唯一
     * 
     * @param category 试题分类信息
     * @return 结果
     */
    @Override
    public boolean checkCategoryNameUnique(ExamQuestionCategory category)
    {
        Long categoryId = StringUtils.isNull(category.getId()) ? -1L : category.getId();
        ExamQuestionCategory info = categoryMapper.checkCategoryNameUnique(category.getCategoryName(), category.getParentId());
        if (StringUtils.isNotNull(info) && info.getId().longValue() != categoryId.longValue())
        {
            return UserConstants.NOT_UNIQUE;
        }
        return UserConstants.UNIQUE;
    }

    /**
     * 新增试题分类
     * 
     * @param category 试题分类信息
     * @return 结果
     */
    @Override
    public int insertCategory(ExamQuestionCategory category)
    {
        ExamQuestionCategory info = categoryMapper.selectCategoryById(category.getParentId());
        if (StringUtils.isNotNull(info))
        {
            return categoryMapper.insertCategory(category);
        }
        else if (category.getParentId() == 0)
        {
            return categoryMapper.insertCategory(category);
        }
        else
        {
            throw new ServiceException("父分类不存在");
        }
    }

    /**
     * 修改试题分类
     * 
     * @param category 试题分类信息
     * @return 结果
     */
    @Override
    public int updateCategory(ExamQuestionCategory category)
    {
        ExamQuestionCategory newParentCategory = categoryMapper.selectCategoryById(category.getParentId());
        ExamQuestionCategory oldCategory = categoryMapper.selectCategoryById(category.getId());
        if (StringUtils.isNotNull(newParentCategory) && StringUtils.isNotNull(oldCategory))
        {
            if (category.getParentId().equals(category.getId()))
            {
                throw new ServiceException("修改分类'" + category.getCategoryName() + "'失败，上级分类不能是自己");
            }
        }
        else if (category.getParentId() != 0)
        {
            throw new ServiceException("父分类不存在");
        }
        return categoryMapper.updateCategory(category);
    }

    /**
     * 批量删除试题分类
     * 
     * @param ids 需要删除的试题分类ID
     * @return 结果
     */
    @Override
    public int deleteCategoryByIds(Long[] ids)
    {
        for (Long id : ids)
        {
            ExamQuestionCategory category = selectCategoryById(id);
            if (hasChildByCategoryId(id))
            {
                throw new ServiceException(String.format("分类%1$s存在子分类,不允许删除", category.getCategoryName()));
            }
            if (checkCategoryExistQuestion(id))
            {
                throw new ServiceException(String.format("分类%1$s存在试题,不允许删除", category.getCategoryName()));
            }
        }
        return categoryMapper.deleteCategoryByIds(ids);
    }

    /**
     * 删除试题分类信息
     * 
     * @param id 试题分类ID
     * @return 结果
     */
    @Override
    public int deleteCategoryById(Long id)
    {
        return categoryMapper.deleteCategoryById(id);
    }

    /**
     * 是否存在试题分类子节点
     * 
     * @param id 试题分类ID
     * @return 结果 true 存在 false 不存在
     */
    @Override
    public boolean hasChildByCategoryId(Long id)
    {
        int result = categoryMapper.selectChildrenCategoryCount(id);
        return result > 0 ? true : false;
    }

    /**
     * 查询分类是否存在试题
     * 
     * @param id 分类ID
     * @return 结果 true 存在 false 不存在
     */
    @Override
    public boolean checkCategoryExistQuestion(Long id)
    {
        int result = categoryMapper.selectQuestionCountByCategoryId(id);
        return result > 0 ? true : false;
    }

    /**
     * 根据分类名称查找分类ID
     * 
     * @param categoryName 分类名称
     * @return 分类ID
     */
    @Override
    public Long findCategoryIdByName(String categoryName)
    {
        ExamQuestionCategory category = categoryMapper.selectCategoryByName(categoryName);
        return category != null ? category.getId() : null;
    }

    /**
     * 递归列表
     */
    private void recursionFn(List<ExamQuestionCategory> list, ExamQuestionCategory t)
    {
        // 得到子节点列表
        List<ExamQuestionCategory> childList = getChildList(list, t);
        t.setChildren(childList);
        for (ExamQuestionCategory tChild : childList)
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
    private List<ExamQuestionCategory> getChildList(List<ExamQuestionCategory> list, ExamQuestionCategory t)
    {
        List<ExamQuestionCategory> tlist = new ArrayList<ExamQuestionCategory>();
        Iterator<ExamQuestionCategory> it = list.iterator();
        while (it.hasNext())
        {
            ExamQuestionCategory n = (ExamQuestionCategory) it.next();
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
    private boolean hasChild(List<ExamQuestionCategory> list, ExamQuestionCategory t)
    {
        return getChildList(list, t).size() > 0;
    }
} 