package com.ruoyi.exam.paper.service.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.constant.UserConstants;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.exam.paper.domain.ExamPaperCategory;
import com.ruoyi.exam.paper.mapper.ExamPaperCategoryMapper;
import com.ruoyi.exam.paper.service.IExamPaperCategoryService;

/**
 * 试卷分类 服务层处理
 * 
 * @author ruoyi
 */
@Service
public class ExamPaperCategoryServiceImpl implements IExamPaperCategoryService
{
    @Autowired
    private ExamPaperCategoryMapper categoryMapper;

    /**
     * 查询试卷分类列表
     * 
     * @param category 试卷分类信息
     * @return 试卷分类集合
     */
    @Override
    public List<ExamPaperCategory> selectCategoryList(ExamPaperCategory category)
    {
        return categoryMapper.selectCategoryList(category);
    }

    /**
     * 构建前端所需要的树结构
     * 
     * @param categories 试卷分类列表
     * @return 树结构列表
     */
    @Override
    public List<ExamPaperCategory> buildCategoryTree(List<ExamPaperCategory> categories)
    {
        List<ExamPaperCategory> returnList = new ArrayList<ExamPaperCategory>();
        List<Long> tempList = new ArrayList<Long>();
        for (ExamPaperCategory category : categories)
        {
            tempList.add(category.getId());
        }
        for (Iterator<ExamPaperCategory> iterator = categories.iterator(); iterator.hasNext();)
        {
            ExamPaperCategory category = iterator.next();
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
     * @param categories 试卷分类列表
     * @return 下拉树结构列表
     */
    @Override
    public List<ExamPaperCategory> buildCategoryTreeSelect(List<ExamPaperCategory> categories)
    {
        List<ExamPaperCategory> categoryTrees = buildCategoryTree(categories);
        return categoryTrees.stream().collect(Collectors.toList());
    }

    /**
     * 根据分类ID查询信息
     * 
     * @param id 分类ID
     * @return 试卷分类信息
     */
    @Override
    public ExamPaperCategory selectCategoryById(Long id)
    {
        return categoryMapper.selectCategoryById(id);
    }

    /**
     * 校验分类名称是否唯一
     * 
     * @param category 试卷分类信息
     * @return 结果
     */
    @Override
    public boolean checkCategoryNameUnique(ExamPaperCategory category)
    {
        Long categoryId = StringUtils.isNull(category.getId()) ? -1L : category.getId();
        ExamPaperCategory info = categoryMapper.checkCategoryNameUnique(category.getCategoryName(), category.getParentId());
        if (StringUtils.isNotNull(info) && info.getId().longValue() != categoryId.longValue())
        {
            return UserConstants.NOT_UNIQUE;
        }
        return UserConstants.UNIQUE;
    }

    /**
     * 新增试卷分类
     * 
     * @param category 试卷分类信息
     * @return 结果
     */
    @Override
    public int insertCategory(ExamPaperCategory category)
    {
        ExamPaperCategory info = categoryMapper.selectCategoryById(category.getParentId());
        // 如果父节点不为正常状态,则不允许新增子节点
        if (info != null && !UserConstants.NORMAL.equals(info.getStatus()))
        {
            throw new ServiceException("分类停用，不允许新增");
        }
        
        // 设置默认值
        if (StringUtils.isEmpty(category.getStatus()))
        {
            category.setStatus(UserConstants.NORMAL);
        }
        
        return categoryMapper.insertCategory(category);
    }

    /**
     * 修改试卷分类
     * 
     * @param category 试卷分类信息
     * @return 结果
     */
    @Override
    public int updateCategory(ExamPaperCategory category)
    {
        ExamPaperCategory newParentCategory = categoryMapper.selectCategoryById(category.getParentId());
        ExamPaperCategory oldCategory = categoryMapper.selectCategoryById(category.getId());
        if (StringUtils.isNotNull(newParentCategory) && StringUtils.isNotNull(oldCategory))
        {
            String newAncestors = newParentCategory.getId() + "," + newParentCategory.getParentId();
            String oldAncestors = oldCategory.getParentId().toString();
            category.setUpdateBy(category.getUpdateBy());
        }
        int result = categoryMapper.updateCategory(category);
        if (UserConstants.NORMAL.equals(category.getStatus()) && StringUtils.isNotEmpty(category.getChildren()))
        {
            // 如果该分类是启用状态，则启用该分类的所有上级分类
            updateParentCategoryStatusNormal(category);
        }
        return result;
    }

    /**
     * 启用所有上级分类
     * 
     * @param category 当前分类
     */
    private void updateParentCategoryStatusNormal(ExamPaperCategory category)
    {
        // 递归更新父级分类状态
        if (category.getParentId() != null && category.getParentId() != 0)
        {
            ExamPaperCategory parentCategory = categoryMapper.selectCategoryById(category.getParentId());
            if (parentCategory != null && !UserConstants.NORMAL.equals(parentCategory.getStatus()))
            {
                parentCategory.setStatus(UserConstants.NORMAL);
                categoryMapper.updateCategory(parentCategory);
                updateParentCategoryStatusNormal(parentCategory);
            }
        }
    }

    /**
     * 批量删除试卷分类
     * 
     * @param ids 需要删除的试卷分类ID
     * @return 结果
     */
    @Override
    public int deleteCategoryByIds(Long[] ids)
    {
        for (Long id : ids)
        {
            ExamPaperCategory category = selectCategoryById(id);
            if (hasChildById(id))
            {
                throw new ServiceException(String.format("%1$s存在子分类,不允许删除", category.getCategoryName()));
            }
            if (existPaper(id))
            {
                throw new ServiceException(String.format("%1$s分类存在试卷,不允许删除", category.getCategoryName()));
            }
        }
        return categoryMapper.deleteCategoryByIds(ids);
    }

    /**
     * 删除试卷分类信息
     * 
     * @param id 试卷分类ID
     * @return 结果
     */
    @Override
    public int deleteCategoryById(Long id)
    {
        return categoryMapper.deleteCategoryById(id);
    }

    /**
     * 递归列表
     */
    private void recursionFn(List<ExamPaperCategory> list, ExamPaperCategory t)
    {
        // 得到子节点列表
        List<ExamPaperCategory> childList = getChildList(list, t);
        t.setChildren(childList);
        for (ExamPaperCategory tChild : childList)
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
    private List<ExamPaperCategory> getChildList(List<ExamPaperCategory> list, ExamPaperCategory t)
    {
        List<ExamPaperCategory> tlist = new ArrayList<ExamPaperCategory>();
        Iterator<ExamPaperCategory> it = list.iterator();
        while (it.hasNext())
        {
            ExamPaperCategory n = it.next();
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
    private boolean hasChild(List<ExamPaperCategory> list, ExamPaperCategory t)
    {
        return getChildList(list, t).size() > 0 ? true : false;
    }

    /**
     * 是否存在子节点
     * 
     * @param id 分类ID
     * @return 结果
     */
    public boolean hasChildById(Long id)
    {
        int result = categoryMapper.selectChildrenCategoryCount(id);
        return result > 0 ? true : false;
    }

    /**
     * 查询分类是否存在试卷
     * 
     * @param id 分类ID
     * @return 结果 true 存在 false 不存在
     */
    public boolean existPaper(Long id)
    {
        int result = categoryMapper.selectPaperCountByCategoryId(id);
        return result > 0 ? true : false;
    }
} 