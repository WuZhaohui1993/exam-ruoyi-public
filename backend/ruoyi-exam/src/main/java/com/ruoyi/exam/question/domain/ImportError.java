package com.ruoyi.exam.question.domain;

/**
 * 导入错误信息
 * 
 * @author ruoyi
 */
public class ImportError
{
    /** 行索引 */
    private int rowIndex;
    
    /** 错误消息 */
    private String errorMessage;
    
    public ImportError()
    {
    }
    
    public ImportError(int rowIndex, String errorMessage)
    {
        this.rowIndex = rowIndex;
        this.errorMessage = errorMessage;
    }
    
    public int getRowIndex()
    {
        return rowIndex;
    }
    
    public void setRowIndex(int rowIndex)
    {
        this.rowIndex = rowIndex;
    }
    
    public String getErrorMessage()
    {
        return errorMessage;
    }
    
    public void setErrorMessage(String errorMessage)
    {
        this.errorMessage = errorMessage;
    }
}