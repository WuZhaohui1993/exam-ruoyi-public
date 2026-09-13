package com.ruoyi.exam.enums;

/**
 * 试题难度等级枚举
 * 
 * @author ruoyi
 */
public enum DifficultyLevelEnum
{
    /** 简单 */
    EASY(1, "简单"),
    
    /** 中等 */
    MEDIUM(2, "中等"),
    
    /** 困难 */
    HARD(3, "困难");

    private final Integer code;
    private final String desc;

    DifficultyLevelEnum(Integer code, String desc)
    {
        this.code = code;
        this.desc = desc;
    }

    public Integer getCode()
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
    public static DifficultyLevelEnum getByCode(Integer code)
    {
        for (DifficultyLevelEnum level : values())
        {
            if (level.getCode().equals(code))
            {
                return level;
            }
        }
        return null;
    }

    /**
     * 判断代码是否有效
     */
    public static boolean isValidCode(Integer code)
    {
        return getByCode(code) != null;
    }
} 