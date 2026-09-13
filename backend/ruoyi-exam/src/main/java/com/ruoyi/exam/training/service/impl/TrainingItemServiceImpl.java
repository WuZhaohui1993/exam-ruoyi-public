package com.ruoyi.exam.training.service.impl;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.config.RuoYiConfig;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.html.HtmlSanitizer;
import com.ruoyi.exam.examination.domain.ExamExamination;
import com.ruoyi.exam.examination.mapper.ExamExaminationMapper;
import com.ruoyi.exam.training.domain.TrainingItem;
import com.ruoyi.exam.training.domain.TrainingSummary;
import com.ruoyi.exam.training.mapper.TrainingItemMapper;
import com.ruoyi.exam.training.service.ITrainingItemService;
import com.ruoyi.system.mapper.SysUserMapper;

/**
 * 培训扩展能力服务。
 */
@Service
public class TrainingItemServiceImpl implements ITrainingItemService
{
    private static final Set<String> ALLOWED_TYPES = new HashSet<String>(Arrays.asList(
            "course", "lesson", "resource", "media_resource", "qa", "certificate", "credit", "practice", "progress"));

    private static final Set<String> RICH_TEXT_TYPES = new HashSet<String>(Arrays.asList(
            "course", "lesson", "resource", "media_resource", "qa"));

    @Autowired
    private TrainingItemMapper trainingItemMapper;

    @Autowired
    private ExamExaminationMapper examExaminationMapper;

    @Autowired
    private SysUserMapper sysUserMapper;

    @Override
    public List<TrainingItem> selectTrainingItemList(TrainingItem item)
    {
        return trainingItemMapper.selectTrainingItemList(item);
    }

    @Override
    public List<TrainingItem> selectLearningLedgerList(TrainingItem item)
    {
        return trainingItemMapper.selectLearningLedgerList(item);
    }

    @Override
    public List<TrainingItem> selectPublishedLearningList(String itemType)
    {
        TrainingItem query = new TrainingItem();
        query.setItemType(itemType);
        query.setStatus("published");
        return trainingItemMapper.selectTrainingItemList(query);
    }

    @Override
    public TrainingItem selectTrainingItemById(Long id)
    {
        return trainingItemMapper.selectTrainingItemById(id);
    }

    @Override
    public int insertTrainingItem(TrainingItem item)
    {
        normalizeItem(item, true);
        int rows = trainingItemMapper.insertTrainingItem(item);
        if ("media_resource".equals(item.getItemType()))
        {
            processMediaResource(item.getId(), StringUtils.defaultIfEmpty(item.getCreateBy(), "system"));
        }
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertTrainingItems(TrainingItem item)
    {
        List<Long> userIds = resolveUserIds(item);
        if (userIds.isEmpty())
        {
            return insertTrainingItem(item);
        }
        int rows = 0;
        for (Long userId : userIds)
        {
            TrainingItem copy = copyForUser(item, userId);
            rows += insertTrainingItem(copy);
        }
        return rows;
    }

    @Override
    public int updateTrainingItem(TrainingItem item)
    {
        TrainingItem old = trainingItemMapper.selectTrainingItemById(item.getId());
        if (old == null)
        {
            throw new ServiceException("培训条目不存在");
        }
        if (StringUtils.isEmpty(item.getItemType()))
        {
            item.setItemType(old.getItemType());
        }
        normalizeItem(item, false);
        int rows = trainingItemMapper.updateTrainingItem(item);
        if ("media_resource".equals(item.getItemType()))
        {
            processMediaResource(item.getId(), StringUtils.defaultIfEmpty(item.getUpdateBy(), "system"));
        }
        return rows;
    }

    @Override
    public int deleteTrainingItemByIds(Long[] ids)
    {
        for (Long id : ids)
        {
            ensureDeletable(id);
        }
        return trainingItemMapper.deleteTrainingItemByIds(ids);
    }

    @Override
    public TrainingSummary selectAdminSummary()
    {
        return trainingItemMapper.selectAdminSummary();
    }

    @Override
    public TrainingSummary selectStudentSummary(Long userId)
    {
        return trainingItemMapper.selectStudentSummary(userId);
    }

    @Override
    public List<Map<String, Object>> selectCourseOptions(String keyword)
    {
        return trainingItemMapper.selectCourseOptions(HtmlSanitizer.cleanText(keyword));
    }

    @Override
    public List<Map<String, Object>> selectExamOptions(String keyword)
    {
        return trainingItemMapper.selectExamOptions(HtmlSanitizer.cleanText(keyword));
    }

    @Override
    public List<Map<String, Object>> selectStudentOptions(String keyword)
    {
        return trainingItemMapper.selectStudentOptions(HtmlSanitizer.cleanText(keyword));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TrainingItem markCourseProgress(Long courseId, Long userId, String username, String status, Integer durationMinutes)
    {
        TrainingItem course = trainingItemMapper.selectTrainingItemById(courseId);
        if (course == null || !"course".equals(course.getItemType()))
        {
            throw new ServiceException("课程不存在");
        }
        if (!"published".equals(course.getStatus()))
        {
            throw new ServiceException("课程未发布，暂不能学习");
        }
        TrainingItem query = new TrainingItem();
        query.setItemType("progress");
        query.setParentId(courseId);
        query.setUserId(userId);
        List<TrainingItem> exists = trainingItemMapper.selectTrainingItemList(query);
        TrainingItem progress = exists.isEmpty() ? new TrainingItem() : exists.get(0);
        progress.setItemType("progress");
        progress.setParentId(courseId);
        progress.setUserId(userId);
        progress.setTitle(course.getTitle());
        progress.setSummary("课程学习进度");
        boolean requestedComplete = "completed".equals(status);
        int newDuration = durationMinutes == null ? 0 : Math.max(0, durationMinutes);
        int oldDuration = progress.getDurationMinutes() == null ? 0 : progress.getDurationMinutes();
        int savedDuration = Math.max(oldDuration, newDuration);
        int targetDuration = course.getDurationMinutes() == null ? 0 : course.getDurationMinutes();
        boolean durationQualified = targetDuration > 0 && savedDuration >= targetDuration;
        boolean complete = durationQualified || (requestedComplete && targetDuration <= 0);
        progress.setStatus(complete ? "completed" : "learning");
        progress.setDurationMinutes(savedDuration);
        progress.setCredit("completed".equals(progress.getStatus()) ? defaultCredit(course.getCredit()) : BigDecimal.ZERO);
        progress.setStartTime(progress.getStartTime() == null ? new Date() : progress.getStartTime());
        if ("completed".equals(progress.getStatus()) && progress.getFinishTime() == null)
        {
            progress.setFinishTime(new Date());
        }
        if (!"completed".equals(progress.getStatus()))
        {
            progress.setFinishTime(null);
        }
        if (progress.getId() == null)
        {
            progress.setCreateBy(username);
            trainingItemMapper.insertTrainingItem(progress);
        }
        else
        {
            progress.setUpdateBy(username);
            trainingItemMapper.updateTrainingItem(progress);
        }
        return progress;
    }

    @Override
    public TrainingItem recordPractice(Long userId, String username, TrainingItem practiceRecord)
    {
        practiceRecord.setItemType("practice");
        practiceRecord.setUserId(userId);
        practiceRecord.setTitle(StringUtils.defaultIfEmpty(practiceRecord.getTitle(), "自主练习记录"));
        practiceRecord.setStatus("completed");
        practiceRecord.setCredit(defaultCredit(practiceRecord.getCredit()));
        practiceRecord.setFinishTime(new Date());
        practiceRecord.setCreateBy(username);
        normalizeItem(practiceRecord, true);
        trainingItemMapper.insertTrainingItem(practiceRecord);
        return practiceRecord;
    }

    @Override
    public TrainingItem askQuestion(Long userId, String username, TrainingItem question)
    {
        question.setItemType("qa");
        question.setUserId(userId);
        question.setStatus("open");
        question.setCreateBy(username);
        normalizeItem(question, true);
        trainingItemMapper.insertTrainingItem(question);
        return question;
    }

    @Override
    public int replyQuestion(Long id, String answerContent, String username)
    {
        TrainingItem item = trainingItemMapper.selectTrainingItemById(id);
        if (item == null || !"qa".equals(item.getItemType()))
        {
            throw new ServiceException("问答不存在");
        }
        item.setAnswerContent(HtmlSanitizer.cleanRichText(answerContent));
        item.setStatus("answered");
        item.setUpdateBy(username);
        return trainingItemMapper.updateTrainingItem(item);
    }

    @Override
    public TrainingItem issueCertificate(Long userId, String username, TrainingItem certificate)
    {
        validateActiveUser(userId);
        certificate.setItemType("certificate");
        certificate.setUserId(userId);
        certificate.setStatus("issued");
        certificate.setCreateBy(username);
        certificate.setCredit(defaultCredit(certificate.getCredit()));
        normalizeItem(certificate, true);
        trainingItemMapper.insertTrainingItem(certificate);
        return certificate;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int issueCertificates(TrainingItem certificate, String username)
    {
        List<Long> userIds = resolveUserIds(certificate);
        if (userIds.isEmpty())
        {
            throw new ServiceException("发证学员不能为空");
        }
        int rows = 0;
        for (Long userId : userIds)
        {
            issueCertificate(userId, username, copyForUser(certificate, userId));
            rows++;
        }
        return rows;
    }

    @Override
    public int processMediaResource(Long id, String username)
    {
        TrainingItem item = trainingItemMapper.selectTrainingItemById(id);
        if (item == null)
        {
            throw new ServiceException("媒体资源不存在");
        }
        if (!"media_resource".equals(item.getItemType()))
        {
            throw new ServiceException("只能处理媒体资源");
        }
        TrainingItem update = new TrainingItem();
        update.setId(id);
        update.setUpdateBy(username);

        if (!"video".equals(item.getResourceType()))
        {
            update.setProcessingStatus("ready");
            update.setProcessingMessage("非视频资源无需转码");
            update.setThumbnailUrl(item.getThumbnailUrl());
            return trainingItemMapper.updateProcessingResult(update);
        }
        if (StringUtils.isEmpty(item.getFileUrl()))
        {
            update.setProcessingStatus("failed");
            update.setProcessingMessage("未上传视频文件");
            return trainingItemMapper.updateProcessingResult(update);
        }
        if (!isCommandAvailable("ffmpeg"))
        {
            update.setProcessingStatus("skipped");
            update.setProcessingMessage("当前服务器未安装 ffmpeg，已保留原视频文件，待生产环境安装后可重新处理");
            update.setThumbnailUrl(item.getThumbnailUrl());
            return trainingItemMapper.updateProcessingResult(update);
        }

        try
        {
            String localPath = toLocalPath(item.getFileUrl());
            File source = new File(localPath);
            if (!source.exists())
            {
                update.setProcessingStatus("failed");
                update.setProcessingMessage("视频文件不存在：" + item.getFileUrl());
                return trainingItemMapper.updateProcessingResult(update);
            }
            File cover = new File(source.getParentFile(), source.getName() + ".jpg");
            Process process = new ProcessBuilder("ffmpeg", "-y", "-i", source.getAbsolutePath(), "-ss", "00:00:01",
                    "-vframes", "1", cover.getAbsolutePath()).redirectErrorStream(true).start();
            int exit = process.waitFor();
            update.setProcessingStatus(exit == 0 && cover.exists() ? "ready" : "failed");
            update.setProcessingMessage(exit == 0 ? "视频封面已生成，原视频文件可直接播放" : "ffmpeg 执行失败");
            update.setThumbnailUrl(exit == 0 && cover.exists() ? toResourceUrl(cover) : item.getThumbnailUrl());
        }
        catch (Exception e)
        {
            update.setProcessingStatus("failed");
            update.setProcessingMessage("视频处理异常：" + e.getMessage());
            update.setThumbnailUrl(item.getThumbnailUrl());
        }
        return trainingItemMapper.updateProcessingResult(update);
    }

    private void normalizeItem(TrainingItem item, boolean insert)
    {
        if (!ALLOWED_TYPES.contains(item.getItemType()))
        {
            throw new ServiceException("培训条目类型不支持：" + item.getItemType());
        }
        item.setTitle(HtmlSanitizer.cleanText(item.getTitle()));
        item.setSummary(HtmlSanitizer.cleanText(item.getSummary()));
        if (RICH_TEXT_TYPES.contains(item.getItemType()))
        {
            item.setContent(HtmlSanitizer.cleanRichText(item.getContent()));
        }
        else
        {
            item.setContent(HtmlSanitizer.cleanText(item.getContent()));
        }
        item.setAnswerContent(HtmlSanitizer.cleanRichText(item.getAnswerContent()));
        item.setStatus(StringUtils.defaultIfEmpty(item.getStatus(), "draft"));
        item.setProcessingStatus(StringUtils.defaultIfEmpty(item.getProcessingStatus(), "none"));
        item.setSortOrder(item.getSortOrder() == null ? 0 : item.getSortOrder());
        item.setCredit(defaultCredit(item.getCredit()));
        if (insert && item.getParentId() == null)
        {
            item.setParentId(0L);
        }
        normalizeOptionalIds(item);
        validateReferences(item);
    }

    private void normalizeOptionalIds(TrainingItem item)
    {
        if (item.getRelatedExamId() != null && item.getRelatedExamId() <= 0)
        {
            item.setRelatedExamId(null);
        }
        if (item.getRelatedPaperId() != null && item.getRelatedPaperId() <= 0)
        {
            item.setRelatedPaperId(null);
        }
        if (item.getUserId() != null && item.getUserId() <= 0)
        {
            item.setUserId(null);
        }
        if (item.getCategoryId() != null && item.getCategoryId() <= 0)
        {
            item.setCategoryId(null);
        }
    }

    private void validateReferences(TrainingItem item)
    {
        if (item.getParentId() != null && item.getParentId() > 0)
        {
            TrainingItem parent = trainingItemMapper.selectTrainingItemById(item.getParentId());
            if (parent == null)
            {
                throw new ServiceException("父级条目不存在");
            }
            if ("course".equals(item.getItemType()))
            {
                throw new ServiceException("课程不能设置父级条目");
            }
            if ("lesson".equals(item.getItemType()) && !"course".equals(parent.getItemType()))
            {
                throw new ServiceException("课件必须挂到课程下");
            }
            if ("progress".equals(item.getItemType()) && !"course".equals(parent.getItemType()))
            {
                throw new ServiceException("学习进度必须关联课程");
            }
        }
        if (item.getRelatedExamId() != null && item.getRelatedExamId() > 0)
        {
            ExamExamination exam = examExaminationMapper.selectExamExaminationById(item.getRelatedExamId());
            if (exam == null)
            {
                throw new ServiceException("关联考试不存在");
            }
            if (!"course".equals(item.getItemType()))
            {
                throw new ServiceException("只有课程可以绑定课后考试");
            }
        }
        if (item.getUserId() != null && item.getUserId() > 0)
        {
            validateActiveUser(item.getUserId());
        }
    }

    private void validateActiveUser(Long userId)
    {
        SysUser user = sysUserMapper.selectUserById(userId);
        if (user == null || "2".equals(user.getDelFlag()))
        {
            throw new ServiceException("学员不存在");
        }
        if ("1".equals(user.getStatus()))
        {
            throw new ServiceException("学员账号已停用");
        }
    }

    private List<Long> resolveUserIds(TrainingItem item)
    {
        LinkedHashSet<Long> userIdSet = new LinkedHashSet<Long>();
        if (item.getUserIds() != null)
        {
            for (Long userId : item.getUserIds())
            {
                if (userId != null && userId > 0)
                {
                    userIdSet.add(userId);
                }
            }
        }
        if (item.getUserId() != null && item.getUserId() > 0)
        {
            userIdSet.add(item.getUserId());
        }
        return new ArrayList<Long>(userIdSet);
    }

    private TrainingItem copyForUser(TrainingItem source, Long userId)
    {
        TrainingItem item = new TrainingItem();
        item.setItemType(source.getItemType());
        item.setTitle(source.getTitle());
        item.setSummary(source.getSummary());
        item.setContent(source.getContent());
        item.setParentId(source.getParentId());
        item.setCategoryId(source.getCategoryId());
        item.setCoverUrl(source.getCoverUrl());
        item.setFileUrl(source.getFileUrl());
        item.setThumbnailUrl(source.getThumbnailUrl());
        item.setResourceType(source.getResourceType());
        item.setProcessingStatus(source.getProcessingStatus());
        item.setProcessingMessage(source.getProcessingMessage());
        item.setRelatedExamId(source.getRelatedExamId());
        item.setRelatedPaperId(source.getRelatedPaperId());
        item.setUserId(userId);
        item.setAnswerContent(source.getAnswerContent());
        item.setStatus(source.getStatus());
        item.setSortOrder(source.getSortOrder());
        item.setDurationMinutes(source.getDurationMinutes());
        item.setCredit(source.getCredit());
        item.setScore(source.getScore());
        item.setStartTime(source.getStartTime());
        item.setFinishTime(source.getFinishTime());
        item.setCreateBy(source.getCreateBy());
        item.setUpdateBy(source.getUpdateBy());
        item.setRemark(source.getRemark());
        return item;
    }

    private void ensureDeletable(Long id)
    {
        TrainingItem item = trainingItemMapper.selectTrainingItemById(id);
        if (item == null)
        {
            return;
        }
        TrainingItem query = new TrainingItem();
        query.setParentId(id);
        List<TrainingItem> children = trainingItemMapper.selectTrainingItemList(query);
        if (!children.isEmpty())
        {
            throw new ServiceException("该条目存在子课件或学习记录，不能直接删除");
        }
    }

    private BigDecimal defaultCredit(BigDecimal credit)
    {
        return credit == null ? BigDecimal.ZERO : credit;
    }

    private boolean isCommandAvailable(String command)
    {
        try
        {
            Process process = new ProcessBuilder("sh", "-c", "command -v " + command).redirectErrorStream(true).start();
            return process.waitFor() == 0;
        }
        catch (Exception e)
        {
            return false;
        }
    }

    private String toLocalPath(String resourceUrl)
    {
        String relative = resourceUrl;
        if (relative.startsWith("/profile"))
        {
            relative = relative.substring("/profile".length());
        }
        return RuoYiConfig.getProfile() + relative;
    }

    private String toResourceUrl(File file)
    {
        String profile = new File(RuoYiConfig.getProfile()).getAbsolutePath();
        String path = file.getAbsolutePath();
        if (path.startsWith(profile))
        {
            return "/profile" + path.substring(profile.length()).replace(File.separatorChar, '/');
        }
        return path;
    }
}
