package com.ruoyi.exam.task;

import static com.ruoyi.exam.constant.ExamStatusConstants.EXAM_CANCELLED;
import static com.ruoyi.exam.constant.ExamStatusConstants.EXAM_DRAFT;
import static com.ruoyi.exam.constant.ExamStatusConstants.EXAM_FINISHED;
import static com.ruoyi.exam.constant.ExamStatusConstants.EXAM_IN_PROGRESS;
import static com.ruoyi.exam.constant.ExamStatusConstants.EXAM_PUBLISHED;

import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ruoyi.exam.examination.domain.ExamExamination;
import com.ruoyi.exam.examination.mapper.ExamExaminationMapper;
import com.ruoyi.exam.examination.mapper.ExamUserMapper;

/**
 * 考试状态自动更新任务
 * 
 * @author ruoyi
 */
@Component("examStatusTask")
public class ExamStatusTask
{
    private static final Logger log = LoggerFactory.getLogger(ExamStatusTask.class);

    @Autowired
    private ExamExaminationMapper examExaminationMapper;
    
    @Autowired
    private ExamUserMapper examUserMapper;

    /**
     * 自动开始考试（性能优化版）
     * 定时检查已发布的考试，如果到达开始时间则自动开始
     */
    public void autoStartExaminations()
    {
        long startTime = System.currentTimeMillis();
        log.info("开始执行考试自动开始任务");
        
        try
        {
            Date now = new Date();
            
            // 查询所有已发布且到达开始时间的考试
            List<ExamExamination> examinations = examExaminationMapper.selectExaminationsByStatusAndTime(EXAM_PUBLISHED, now);
            
            if (examinations.isEmpty())
            {
                log.debug("没有需要自动开始的考试");
                return;
            }
            
            int successCount = 0;
            int skipCount = 0;
            
            for (ExamExamination exam : examinations)
            {
                try
                {
                    // 再次验证考试状态（防护措施）
                    ExamExamination currentExam = examExaminationMapper.selectExamExaminationById(exam.getId());
                    if (currentExam == null || !EXAM_PUBLISHED.equals(currentExam.getStatus()))
                    {
                        log.warn("考试 [{}] 状态已变更为 {}, 跳过自动开始", exam.getExamName(), 
                                currentExam != null ? currentExam.getStatus() : "不存在");
                        skipCount++;
                        continue;
                    }
                    
                    // 检查是否有报名用户
                    int registeredCount = examUserMapper.countRegisteredUsers(exam.getId());
                    if (registeredCount > 0)
                    {
                        // 自动开始考试
                        int result = examExaminationMapper.updateExamStatus(exam.getId(), EXAM_IN_PROGRESS);
                        if (result > 0)
                        {
                            successCount++;
                            log.info("考试 [{}] 已自动开始，参考人数: {}", exam.getExamName(), registeredCount);
                        }
                        else
                        {
                            log.warn("考试 [{}] 自动开始失败，可能状态已被其他操作修改", exam.getExamName());
                            skipCount++;
                        }
                    }
                    else
                    {
                        skipCount++;
                        log.warn("考试 [{}] 没有报名用户，跳过自动开始", exam.getExamName());
                    }
                }
                catch (Exception e)
                {
                    log.error("考试 [{}] 自动开始失败", exam.getExamName(), e);
                }
            }
            
            long duration = System.currentTimeMillis() - startTime;
            log.info("考试自动开始任务执行完成，总数: {}, 成功: {}, 跳过: {}, 耗时: {}ms", 
                     examinations.size(), successCount, skipCount, duration);
        }
        catch (Exception e)
        {
            long duration = System.currentTimeMillis() - startTime;
            log.error("考试自动开始任务执行失败，耗时: {}ms", duration, e);
        }
    }

    /**
     * 自动结束考试（性能优化版）
     * 定时检查进行中的考试，如果到达结束时间则自动结束
     */
    public void autoEndExaminations()
    {
        long startTime = System.currentTimeMillis();
        log.info("开始执行考试自动结束任务");
        
        try
        {
            Date now = new Date();
            
            // 查询所有进行中且到达结束时间的考试
            List<ExamExamination> examinations = examExaminationMapper.selectExaminationsByStatusAndEndTime(EXAM_IN_PROGRESS, now);
            
            if (examinations.isEmpty())
            {
                log.debug("没有需要自动结束的考试");
                return;
            }
            
            int successCount = 0;
            int skipCount = 0;
            int totalForceSubmit = 0;
            
            for (ExamExamination exam : examinations)
            {
                try
                {
                    // 再次验证考试状态（防护措施）
                    ExamExamination currentExam = examExaminationMapper.selectExamExaminationById(exam.getId());
                    if (currentExam == null || !EXAM_IN_PROGRESS.equals(currentExam.getStatus()))
                    {
                        log.warn("考试 [{}] 状态已变更为 {}, 跳过自动结束", exam.getExamName(), 
                                currentExam != null ? currentExam.getStatus() : "不存在");
                        skipCount++;
                        continue;
                    }
                    
                    // 强制提交所有未完成的答卷
                    int forceSubmitCount = examUserMapper.forceSubmitUnfinishedExams(exam.getId());
                    totalForceSubmit += forceSubmitCount;
                    
                    if (forceSubmitCount > 0)
                    {
                        log.info("考试 [{}] 强制提交了 {} 份未完成的答卷", exam.getExamName(), forceSubmitCount);
                    }
                    
                    // 自动结束考试
                    int result = examExaminationMapper.updateExamStatus(exam.getId(), EXAM_FINISHED);
                    if (result > 0)
                    {
                        successCount++;
                        log.info("考试 [{}] 已自动结束", exam.getExamName());
                    }
                    else
                    {
                        log.warn("考试 [{}] 自动结束失败，可能状态已被其他操作修改", exam.getExamName());
                        skipCount++;
                    }
                }
                catch (Exception e)
                {
                    log.error("考试 [{}] 自动结束失败", exam.getExamName(), e);
                }
            }
            
            long duration = System.currentTimeMillis() - startTime;
            log.info("考试自动结束任务执行完成，总数: {}, 成功: {}, 跳过: {}, 强制提交: {}, 耗时: {}ms", 
                     examinations.size(), successCount, skipCount, totalForceSubmit, duration);
        }
        catch (Exception e)
        {
            long duration = System.currentTimeMillis() - startTime;
            log.error("考试自动结束任务执行失败，耗时: {}ms", duration, e);
        }
    }

    /**
     * 考试状态监控和统计
     * 定时输出考试状态统计信息，便于监控
     */
    public void examStatusMonitor()
    {
        log.info("开始执行考试状态监控任务");
        
        try
        {
            // 统计各状态考试数量
            int draftCount = examExaminationMapper.countByStatus(EXAM_DRAFT);
            int publishedCount = examExaminationMapper.countByStatus(EXAM_PUBLISHED);
            int inProgressCount = examExaminationMapper.countByStatus(EXAM_IN_PROGRESS);
            int finishedCount = examExaminationMapper.countByStatus(EXAM_FINISHED);
            int cancelledCount = examExaminationMapper.countByStatus(EXAM_CANCELLED);
            
            log.info("考试状态统计 - 草稿: {}, 已发布: {}, 进行中: {}, 已结束: {}, 已取消: {}", 
                     draftCount, publishedCount, inProgressCount, finishedCount, cancelledCount);
        }
        catch (Exception e)
        {
            log.error("考试状态监控任务执行失败", e);
        }
    }

    /**
     * 清理过期考试数据
     * 定时清理已结束超过指定天数的考试相关数据
     */
    public void cleanExpiredExamData()
    {
        log.info("开始执行过期考试数据清理任务");
        
        try
        {
            // TODO: 实现过期数据清理逻辑
            // 例如：删除超过30天的考试答题记录、日志等
            
            log.info("过期考试数据清理任务执行完成");
        }
        catch (Exception e)
        {
            log.error("过期考试数据清理任务执行失败", e);
        }
    }
}
