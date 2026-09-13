package com.ruoyi.web.controller.exam;

import java.util.List;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.TreeSelect;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.exam.question.domain.ExamQuestionCategory;
import com.ruoyi.exam.question.service.IExamQuestionCategoryService;

/**
 * 试题分类信息操作处理
 * 
 * @author ruoyi
 */
@RestController
@RequestMapping("/exam/category")
public class ExamQuestionCategoryController extends BaseController
{
    @Autowired
    private IExamQuestionCategoryService categoryService;

    /**
     * 获取试题分类列表
     */
    @PreAuthorize("@ss.hasPermi('exam:category:list')")
    @GetMapping("/list")
    public AjaxResult list(ExamQuestionCategory category)
    {
        List<ExamQuestionCategory> categories = categoryService.selectCategoryList(category);
        return success(categoryService.buildCategoryTree(categories));
    }

    /**
     * 查询试题分类下拉树列表
     */
    @PreAuthorize("@ss.hasAnyPermi('exam:category:list,exam:category:query,exam:question:list,exam:question:query')")
    @GetMapping("/treeselect")
    public AjaxResult treeselect(ExamQuestionCategory category)
    {
        List<ExamQuestionCategory> categories = categoryService.selectCategoryList(category);
        return success(categoryService.buildCategoryTreeSelect(categories));
    }

    /**
     * 导出试题分类列表
     */
    @Log(title = "试题分类", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('exam:category:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, ExamQuestionCategory category)
    {
        List<ExamQuestionCategory> list = categoryService.selectCategoryList(category);
        ExcelUtil<ExamQuestionCategory> util = new ExcelUtil<ExamQuestionCategory>(ExamQuestionCategory.class);
        util.exportExcel(response, list, "试题分类数据");
    }

    /**
     * 根据分类编号获取详细信息
     */
    @PreAuthorize("@ss.hasPermi('exam:category:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable Long id)
    {
        return success(categoryService.selectCategoryById(id));
    }

    /**
     * 新增试题分类
     */
    @PreAuthorize("@ss.hasPermi('exam:category:add')")
    @Log(title = "试题分类", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody ExamQuestionCategory category)
    {
        if (!categoryService.checkCategoryNameUnique(category))
        {
            return error("新增分类'" + category.getCategoryName() + "'失败，分类名称已存在");
        }
        category.setCreateBy(getUsername());
        return toAjax(categoryService.insertCategory(category));
    }

    /**
     * 修改试题分类
     */
    @PreAuthorize("@ss.hasPermi('exam:category:edit')")
    @Log(title = "试题分类", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody ExamQuestionCategory category)
    {
        Long categoryId = category.getId();
        if (categoryService.hasChildByCategoryId(categoryId))
        {
            return warn("存在下级分类,不允许修改");
        }
        if (!categoryService.checkCategoryNameUnique(category))
        {
            return error("修改分类'" + category.getCategoryName() + "'失败，分类名称已存在");
        }
        else if (category.getParentId().equals(categoryId))
        {
            return error("修改分类'" + category.getCategoryName() + "'失败，上级分类不能是自己");
        }
        category.setUpdateBy(getUsername());
        return toAjax(categoryService.updateCategory(category));
    }

    /**
     * 删除试题分类
     */
    @PreAuthorize("@ss.hasPermi('exam:category:remove')")
    @Log(title = "试题分类", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(categoryService.deleteCategoryByIds(ids));
    }
}
