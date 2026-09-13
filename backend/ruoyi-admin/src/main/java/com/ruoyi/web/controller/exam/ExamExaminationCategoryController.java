package com.ruoyi.web.controller.exam;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.exam.examination.domain.ExamExaminationCategory;
import com.ruoyi.exam.examination.service.IExamExaminationCategoryService;

/**
 * 考试分类Controller
 * 
 * @author ruoyi
 * @date 2024-06-10
 */
@RestController
@RequestMapping("/exam/examination/category")
public class ExamExaminationCategoryController extends BaseController
{
    @Autowired
    private IExamExaminationCategoryService examExaminationCategoryService;

    /**
     * 获取考试分类列表
     */
    @PreAuthorize("@ss.hasPermi('exam:examination:category:list')")
    @GetMapping("/list")
    public AjaxResult list(ExamExaminationCategory examExaminationCategory)
    {
        List<ExamExaminationCategory> categories = examExaminationCategoryService.selectExamExaminationCategoryList(examExaminationCategory);
        return success(categories);
    }

    /**
     * 获取考试分类树列表
     */
    @PreAuthorize("@ss.hasPermi('exam:examination:category:list')")
    @GetMapping("/treeselect")
    public AjaxResult treeselect(ExamExaminationCategory examExaminationCategory)
    {
        List<ExamExaminationCategory> categories = examExaminationCategoryService.selectExamExaminationCategoryList(examExaminationCategory);
        return success(examExaminationCategoryService.buildCategoryTreeSelect(categories));
    }

    /**
     * 根据角色ID查询考试分类树信息
     */
    @PreAuthorize("@ss.hasPermi('exam:examination:category:list')")
    @GetMapping(value = "/roleExaminationCategoryTreeselect/{roleId}")
    public AjaxResult roleExaminationCategoryTreeselect(@PathVariable("roleId") Long roleId)
    {
        List<ExamExaminationCategory> categories = examExaminationCategoryService.selectExamExaminationCategoryList(new ExamExaminationCategory());
        AjaxResult ajax = success();
        ajax.put("checkedKeys", examExaminationCategoryService.selectExamExaminationCategoryListByRoleId(roleId));
        ajax.put("categories", examExaminationCategoryService.buildCategoryTreeSelect(categories));
        return ajax;
    }

    /**
     * 获取考试分类详细信息
     */
    @PreAuthorize("@ss.hasPermi('exam:examination:category:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        examExaminationCategoryService.checkCategoryDataScope(id);
        return success(examExaminationCategoryService.selectExamExaminationCategoryById(id));
    }

    /**
     * 新增考试分类
     */
    @PreAuthorize("@ss.hasPermi('exam:examination:category:add')")
    @Log(title = "考试分类管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody ExamExaminationCategory examExaminationCategory)
    {
        if ("1".equals(examExaminationCategoryService.checkCategoryNameUnique(examExaminationCategory)))
        {
            return error("新增考试分类'" + examExaminationCategory.getCategoryName() + "'失败，分类名称已存在");
        }
        return toAjax(examExaminationCategoryService.insertExamExaminationCategory(examExaminationCategory));
    }

    /**
     * 修改考试分类
     */
    @PreAuthorize("@ss.hasPermi('exam:examination:category:edit')")
    @Log(title = "考试分类管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody ExamExaminationCategory examExaminationCategory)
    {
        Long categoryId = examExaminationCategory.getId();
        examExaminationCategoryService.checkCategoryDataScope(categoryId);
        if ("1".equals(examExaminationCategoryService.checkCategoryNameUnique(examExaminationCategory)))
        {
            return error("修改考试分类'" + examExaminationCategory.getCategoryName() + "'失败，分类名称已存在");
        }
        else if (examExaminationCategory.getParentId().equals(categoryId))
        {
            return error("修改考试分类'" + examExaminationCategory.getCategoryName() + "'失败，上级分类不能是自己");
        }
        return toAjax(examExaminationCategoryService.updateExamExaminationCategory(examExaminationCategory));
    }

    /**
     * 删除考试分类
     */
    @PreAuthorize("@ss.hasPermi('exam:examination:category:remove')")
    @Log(title = "考试分类管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{id}")
    public AjaxResult remove(@PathVariable("id") Long id)
    {
        if (examExaminationCategoryService.hasChildByCategoryId(id))
        {
            return warn("存在下级分类,不允许删除");
        }
        if (examExaminationCategoryService.checkCategoryExistUser(id))
        {
            return warn("分类存在考试,不允许删除");
        }
        examExaminationCategoryService.checkCategoryDataScope(id);
        return toAjax(examExaminationCategoryService.deleteExamExaminationCategoryById(id));
    }

    /**
     * 校验考试分类名称
     */
    @PreAuthorize("@ss.hasAnyPermi('exam:examination:category:add,exam:examination:category:edit')")
    @PostMapping("/checkCategoryNameUnique")
    public AjaxResult checkCategoryNameUnique(@RequestBody ExamExaminationCategory examExaminationCategory)
    {
        String result = examExaminationCategoryService.checkCategoryNameUnique(examExaminationCategory);
        return success("1".equals(result) ? false : true);
    }
}
