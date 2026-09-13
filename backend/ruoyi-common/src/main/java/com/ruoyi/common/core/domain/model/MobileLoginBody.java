package com.ruoyi.common.core.domain.model;

/**
 * 移动端登录请求体
 * 支持身份证号登录，用于H5移动端考试系统
 * 
 * @author ruoyi
 */
public class MobileLoginBody {
    /**
     * 身份证号
     */
    private String idCard;

    /**
     * 用户密码
     */
    private String password;

    /**
     * 考试ID（可选，用于登录后自动跳转）
     */
    private Long examId;

    public String getIdCard() {
        return idCard;
    }

    public void setIdCard(String idCard) {
        this.idCard = idCard;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Long getExamId() {
        return examId;
    }

    public void setExamId(Long examId) {
        this.examId = examId;
    }
}
