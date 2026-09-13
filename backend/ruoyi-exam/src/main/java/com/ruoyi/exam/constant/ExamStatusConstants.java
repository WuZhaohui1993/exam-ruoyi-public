package com.ruoyi.exam.constant;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * 考试业务状态常量。
 *
 * 状态值必须和 Mapper XML、SQL、前端字典保持一致。
 */
public final class ExamStatusConstants
{
    private ExamStatusConstants()
    {
    }

    /** exam_examination.status：草稿 */
    public static final String EXAM_DRAFT = "0";

    /** exam_examination.status：已发布 */
    public static final String EXAM_PUBLISHED = "1";

    /** exam_examination.status：进行中 */
    public static final String EXAM_IN_PROGRESS = "2";

    /** exam_examination.status：已结束 */
    public static final String EXAM_FINISHED = "3";

    /** exam_examination.status：已取消 */
    public static final String EXAM_CANCELLED = "4";

    /** exam_user.registration_status：已报名/待审核 */
    public static final String REGISTRATION_REGISTERED = "registered";

    /** exam_user.registration_status：审核通过 */
    public static final String REGISTRATION_APPROVED = "approved";

    /** exam_user.registration_status：审核拒绝 */
    public static final String REGISTRATION_REJECTED = "rejected";

    /** exam_user.exam_status：已登记 */
    public static final String USER_EXAM_REGISTERED = "registered";

    /** exam_user.exam_status：未开始 */
    public static final String USER_EXAM_NOT_STARTED = "not_started";

    /** exam_user.exam_status：进行中 */
    public static final String USER_EXAM_IN_PROGRESS = "in_progress";

    /** exam_user.exam_status：已提交 */
    public static final String USER_EXAM_SUBMITTED = "submitted";

    /** exam_user.exam_status：已评分 */
    public static final String USER_EXAM_GRADED = "graded";

    public static final Set<String> REUSABLE_ATTEMPT_STATUS = Collections.unmodifiableSet(new HashSet<String>(
            Arrays.asList(USER_EXAM_REGISTERED, USER_EXAM_NOT_STARTED, USER_EXAM_IN_PROGRESS)));

    public static boolean isExaminationPublishedOrInProgress(String status)
    {
        return EXAM_PUBLISHED.equals(status) || EXAM_IN_PROGRESS.equals(status);
    }

    public static boolean isExaminationFinishedOrCancelled(String status)
    {
        return EXAM_FINISHED.equals(status) || EXAM_CANCELLED.equals(status);
    }

    public static boolean isRegistrationPending(String status)
    {
        return REGISTRATION_REGISTERED.equals(status);
    }

    public static boolean isRegistrationRejected(String status)
    {
        return REGISTRATION_REJECTED.equals(status);
    }

    public static boolean isUserExamInProgress(String status)
    {
        return USER_EXAM_IN_PROGRESS.equals(status);
    }

    /**
     * 兼容旧前端查询参数，只返回当前数据库标准状态值。
     */
    public static String normalizeExaminationStatusForQuery(String status)
    {
        if (status == null)
        {
            return null;
        }
        switch (status)
        {
            case "draft":
                return EXAM_DRAFT;
            case "published":
                return EXAM_PUBLISHED;
            case "started":
                return EXAM_IN_PROGRESS;
            case "ended":
                return EXAM_FINISHED;
            case "cancelled":
                return EXAM_CANCELLED;
            default:
                return status;
        }
    }
}
