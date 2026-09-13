package com.ruoyi.exam.question.domain;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * 试题选项实体类
 * 
 * @author ruoyi
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class QuestionOption
{
    /** 选项标识 */
    private String key;
    
    /** 选项内容 */
    private String value;
    
    /** 是否为正确答案 */
    private Boolean isCorrect;
    
    /** 选项排序 */
    private Integer sort;

    public QuestionOption()
    {
    }

    public QuestionOption(String key, String value)
    {
        this.key = key;
        this.value = value;
        this.isCorrect = false;
        this.sort = 0;
    }

    public QuestionOption(String key, String value, Boolean isCorrect)
    {
        this.key = key;
        this.value = value;
        this.isCorrect = isCorrect;
        this.sort = 0;
    }

    public String getKey()
    {
        return key;
    }

    public void setKey(String key)
    {
        this.key = key;
    }

    public String getValue()
    {
        return value;
    }

    public void setValue(String value)
    {
        this.value = value;
    }

    public Boolean getIsCorrect()
    {
        return isCorrect;
    }

    public void setIsCorrect(Boolean isCorrect)
    {
        this.isCorrect = isCorrect;
    }

    public Integer getSort()
    {
        return sort;
    }

    public void setSort(Integer sort)
    {
        this.sort = sort;
    }

    @Override
    public String toString()
    {
        return "QuestionOption{" +
                "key='" + key + '\'' +
                ", value='" + value + '\'' +
                ", isCorrect=" + isCorrect +
                ", sort=" + sort +
                '}';
    }
} 