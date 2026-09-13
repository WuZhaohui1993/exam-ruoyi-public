package com.ruoyi.exam.enums;

/**
 * 考试状态枚举
 * 
 * @author ruoyi
 */
public enum ExamStatusEnum
{
    /** 可以考试 */
    AVAILABLE("available", "可以考试"),
    
    /** 再次考试 */
    RETRY("retry", "再次考试"),
    
    /** 考试中 */
    IN_PROGRESS("in_progress", "考试中"),
    
    /** 已结束 */
    FINISHED("finished", "已结束"),
    
    /** 未报名 */
    NOT_REGISTERED("not_registered", "未报名"),
    
    /** 报名审核中 */
    UNDER_REVIEW("under_review", "报名审核中"),
    
    /** 审核未通过 */
    REVIEW_FAILED("review_failed", "审核未通过"),
    
    /** 等待人工评分 */
    WAITING_MANUAL_SCORE("waiting_manual_score", "等待人工评分");

    private final String code;
    private final String desc;

    ExamStatusEnum(String code, String desc)
    {
        this.code = code;
        this.desc = desc;
    }

    public String getCode()
    {
        return code;
    }

    public String getDesc()
    {
        return desc;
    }

    /**
     * 根据代码获取枚举
     */
    public static ExamStatusEnum getByCode(String code)
    {
        for (ExamStatusEnum status : values())
        {
            if (status.getCode().equals(code))
            {
                return status;
            }
        }
        return null;
    }

    /**
     * 判断代码是否有效
     */
    public static boolean isValidCode(String code)
    {
        return getByCode(code) != null;
    }
} 