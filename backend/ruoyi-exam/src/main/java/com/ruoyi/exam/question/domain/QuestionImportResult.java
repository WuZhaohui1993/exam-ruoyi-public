package com.ruoyi.exam.question.domain;

/**
 * 试题导入结果
 * 
 * @author ruoyi
 */
public class QuestionImportResult
{
    /** 成功数量 */
    private int successCount;
    
    /** 失败数量 */
    private int failureCount;
    
    /** 结果消息 */
    private String message;
    
    /** 是否有错误文件 */
    private boolean hasErrorFile;
    
    /** 错误文件名 */
    private String errorFileName;
    
    /** 错误文件数据 */
    private byte[] errorFileData;
    
    public QuestionImportResult()
    {
    }
    
    public QuestionImportResult(int successCount, int failureCount, String message)
    {
        this.successCount = successCount;
        this.failureCount = failureCount;
        this.message = message;
    }
    
    public int getSuccessCount()
    {
        return successCount;
    }
    
    public void setSuccessCount(int successCount)
    {
        this.successCount = successCount;
    }
    
    public int getFailureCount()
    {
        return failureCount;
    }
    
    public void setFailureCount(int failureCount)
    {
        this.failureCount = failureCount;
    }
    
    public String getMessage()
    {
        return message;
    }
    
    public void setMessage(String message)
    {
        this.message = message;
    }
    
    public boolean isHasErrorFile()
    {
        return hasErrorFile;
    }
    
    public void setHasErrorFile(boolean hasErrorFile)
    {
        this.hasErrorFile = hasErrorFile;
    }
    
    public String getErrorFileName()
    {
        return errorFileName;
    }
    
    public void setErrorFileName(String errorFileName)
    {
        this.errorFileName = errorFileName;
    }
    
    public byte[] getErrorFileData()
    {
        return errorFileData;
    }
    
    public void setErrorFileData(byte[] errorFileData)
    {
        this.errorFileData = errorFileData;
    }
}