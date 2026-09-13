package com.ruoyi.exam.examination.domain;

import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 考试部门授权表 exam_examination_dept
 */
public class ExamExaminationDept extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键 */
    private Long id;

    /** 考试ID */
    private Long examId;

    /** 部门ID */
    private Long deptId;

    /** 是否包含子部门（0否 1是） */
    private String includeChildren;

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public Long getExamId()
    {
        return examId;
    }

    public void setExamId(Long examId)
    {
        this.examId = examId;
    }

    public Long getDeptId()
    {
        return deptId;
    }

    public void setDeptId(Long deptId)
    {
        this.deptId = deptId;
    }

    public String getIncludeChildren()
    {
        return includeChildren;
    }

    public void setIncludeChildren(String includeChildren)
    {
        this.includeChildren = includeChildren;
    }
}
