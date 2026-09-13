package com.ruoyi.exam.enums;

/**
 * 试题类型枚举
 * 
 * @author ruoyi
 */
public enum QuestionTypeEnum
{
    /** 单选题 */
    SINGLE("single", "单选题"),
    
    /** 多选题 */
    MULTIPLE("multiple", "多选题"),
    
    /** 判断题 */
    JUDGE("judge", "判断题"),
    
    /** 填空题 */
    FILL("fill", "填空题"),
    
    /** 简答题 */
    ESSAY("essay", "简答题");

    private final String code;
    private final String desc;

    QuestionTypeEnum(String code, String desc)
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
    public static QuestionTypeEnum getByCode(String code)
    {
        for (QuestionTypeEnum type : values())
        {
            if (type.getCode().equals(code))
            {
                return type;
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