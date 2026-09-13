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
import com.ruoyi.common.constant.UserConstants;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.exam.paper.domain.ExamPaperCategory;
import com.ruoyi.exam.paper.service.IExamPaperCategoryService;

/**
 * 试卷分类信息操作处理
 * 
 * @author ruoyi
 */
@RestController
@RequestMapping("/exam/paper/category")
public class ExamPaperCategoryController extends BaseController
{
    @Autowired
    private IExamPaperCategoryService categoryService;

    /**
     * 获取试卷分类列表
     */
    @PreAuthorize("@ss.hasPermi('exam:paper:category:list')")
    @GetMapping("/list")
    public AjaxResult list(ExamPaperCategory category)
    {
        List<ExamPaperCategory> categories = categoryService.selectCategoryList(category);
        return success(categoryService.buildCategoryTree(categories));
    }

    /**
     * 查询试卷分类下拉树结构
     */
    @PreAuthorize("@ss.hasAnyPermi('exam:paper:category:list,exam:paper:category:query,exam:paper:list,exam:paper:query')")
    @GetMapping("/treeselect")
    public AjaxResult treeselect(ExamPaperCategory category)
    {
        List<ExamPaperCategory> categories = categoryService.selectCategoryList(category);
        return success(categoryService.buildCategoryTreeSelect(categories));
    }

    /**
     * 根据试卷分类编号获取详细信息
     */
    @PreAuthorize("@ss.hasPermi('exam:paper:category:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable Long id)
    {
        return success(categoryService.selectCategoryById(id));
    }

    /**
     * 新增试卷分类
     */
    @PreAuthorize("@ss.hasPermi('exam:paper:category:add')")
    @Log(title = "试卷分类管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody ExamPaperCategory category)
    {
        if (!categoryService.checkCategoryNameUnique(category))
        {
            return error("新增分类'" + category.getCategoryName() + "'失败，分类名称已存在");
        }
        category.setCreateBy(getUsername());
        return toAjax(categoryService.insertCategory(category));
    }

    /**
     * 修改试卷分类
     */
    @PreAuthorize("@ss.hasPermi('exam:paper:category:edit')")
    @Log(title = "试卷分类管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody ExamPaperCategory category)
    {
        if (!categoryService.checkCategoryNameUnique(category))
        {
            return error("修改分类'" + category.getCategoryName() + "'失败，分类名称已存在");
        }
        else if (category.getParentId().equals(category.getId()))
        {
            return error("修改分类'" + category.getCategoryName() + "'失败，上级分类不能是自己");
        }
        else if (UserConstants.EXCEPTION.equals(category.getStatus()))
        {
            return error("该分类包含未停用的子分类！");
        }
        category.setUpdateBy(getUsername());
        return toAjax(categoryService.updateCategory(category));
    }

    /**
     * 删除试卷分类
     */
    @PreAuthorize("@ss.hasPermi('exam:paper:category:remove')")
    @Log(title = "试卷分类管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(categoryService.deleteCategoryByIds(ids));
    }

    /**
     * 导出试卷分类列表
     */
    @Log(title = "试卷分类管理", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('exam:paper:category:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, ExamPaperCategory category)
    {
        List<ExamPaperCategory> list = categoryService.selectCategoryList(category);
        ExcelUtil<ExamPaperCategory> util = new ExcelUtil<ExamPaperCategory>(ExamPaperCategory.class);
        util.exportExcel(response, list, "试卷分类数据");
    }
}
