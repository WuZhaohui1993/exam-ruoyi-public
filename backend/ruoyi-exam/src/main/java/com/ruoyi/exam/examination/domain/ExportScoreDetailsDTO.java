package com.ruoyi.exam.examination.domain;

/**
 * 导出成绩详情DTO
 * 
 * @author ruoyi
 */
public class ExportScoreDetailsDTO {
    
    /** 分数段筛选 */
    private String scoreRange;
    
    /** 通过状态筛选 */
    private String passStatus;
    
    /** 部门ID筛选 */
    private Long deptId;
    
    /** 搜索姓名 */
    private String searchName;
    
    /** 是否包含答题详情 */
    private Boolean includeAnswerDetails;

    /** 单个用户ID */
    private Long userId;
    
    /** 用户ID列表（用于导出选中用户） */
    private Long[] userIds;

    public String getScoreRange() {
        return scoreRange;
    }

    public void setScoreRange(String scoreRange) {
        this.scoreRange = scoreRange;
    }

    public String getPassStatus() {
        return passStatus;
    }

    public void setPassStatus(String passStatus) {
        this.passStatus = passStatus;
    }

    public Long getDeptId() {
        return deptId;
    }

    public void setDeptId(Long deptId) {
        this.deptId = deptId;
    }

    public String getSearchName() {
        return searchName;
    }

    public void setSearchName(String searchName) {
        this.searchName = searchName;
    }

    public Boolean getIncludeAnswerDetails() {
        return includeAnswerDetails;
    }

    public void setIncludeAnswerDetails(Boolean includeAnswerDetails) {
        this.includeAnswerDetails = includeAnswerDetails;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long[] getUserIds() {
        return userIds;
    }

    public void setUserIds(Long[] userIds) {
        this.userIds = userIds;
    }
}
