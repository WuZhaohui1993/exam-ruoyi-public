package com.ruoyi.exam.question.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.html.EscapeUtil;
import com.ruoyi.exam.question.domain.ExamQuestionMedia;
import com.ruoyi.exam.question.mapper.ExamQuestionMediaMapper;
import com.ruoyi.exam.question.service.IExamQuestionMediaService;

/**
 * 试题媒体附件 服务层处理
 *
 * @author ruoyi
 */
@Service
public class ExamQuestionMediaServiceImpl implements IExamQuestionMediaService
{
    private static final int MAX_IMAGE_COUNT = 6;
    private static final int MAX_VIDEO_COUNT = 1;
    private static final long MAX_IMAGE_SIZE = 10L * 1024 * 1024;
    private static final long MAX_VIDEO_SIZE = 100L * 1024 * 1024;
    private static final String[] ALLOWED_IMAGE_EXTENSIONS = { "png", "jpg", "jpeg", "gif", "webp", "bmp" };
    private static final String[] ALLOWED_VIDEO_EXTENSIONS = { "mp4", "m4v", "mov", "webm", "ogg", "ogv" };
    private static final String ALLOWED_IMAGE_EXTENSION_TIP = "png/jpg/jpeg/gif/webp/bmp";
    private static final String ALLOWED_VIDEO_EXTENSION_TIP = "mp4/m4v/mov/webm/ogg/ogv";

    @Autowired
    private ExamQuestionMediaMapper examQuestionMediaMapper;

    @Override
    public List<ExamQuestionMedia> selectMediaByQuestionId(Long questionId)
    {
        if (questionId == null)
        {
            return new ArrayList<>();
        }
        return examQuestionMediaMapper.selectQuestionMediaByQuestionId(questionId);
    }

    @Override
    public Map<Long, List<ExamQuestionMedia>> selectMediaMapByQuestionIds(List<Long> questionIds)
    {
        Map<Long, List<ExamQuestionMedia>> mediaMap = new HashMap<>();
        if (StringUtils.isEmpty(questionIds))
        {
            return mediaMap;
        }

        List<ExamQuestionMedia> mediaList = examQuestionMediaMapper.selectQuestionMediaByQuestionIds(questionIds);
        for (ExamQuestionMedia media : mediaList)
        {
            if (media == null || media.getQuestionId() == null)
            {
                continue;
            }
            List<ExamQuestionMedia> list = mediaMap.get(media.getQuestionId());
            if (list == null)
            {
                list = new ArrayList<>();
                mediaMap.put(media.getQuestionId(), list);
            }
            list.add(media);
        }
        return mediaMap;
    }

    @Override
    public void replaceQuestionMedia(Long questionId, List<ExamQuestionMedia> mediaList, String operName)
    {
        if (questionId == null)
        {
            return;
        }

        examQuestionMediaMapper.deleteQuestionMediaByQuestionId(questionId);

        List<ExamQuestionMedia> normalizedList = normalizeMediaList(questionId, mediaList, operName);
        if (StringUtils.isNotEmpty(normalizedList))
        {
            examQuestionMediaMapper.batchInsertQuestionMedia(normalizedList);
        }
    }

    @Override
    public int deleteMediaByQuestionId(Long questionId)
    {
        return examQuestionMediaMapper.deleteQuestionMediaByQuestionId(questionId);
    }

    @Override
    public int deleteMediaByQuestionIds(Long[] questionIds)
    {
        if (StringUtils.isEmpty(questionIds))
        {
            return 0;
        }
        return examQuestionMediaMapper.deleteQuestionMediaByQuestionIds(questionIds);
    }

    private List<ExamQuestionMedia> normalizeMediaList(Long questionId, List<ExamQuestionMedia> mediaList, String operName)
    {
        List<ExamQuestionMedia> normalizedList = new ArrayList<>();
        if (StringUtils.isEmpty(mediaList))
        {
            return normalizedList;
        }

        int imageCount = 0;
        int videoCount = 0;
        int sortOrder = 0;

        for (ExamQuestionMedia media : mediaList)
        {
            if (media == null || StringUtils.isEmpty(media.getFileUrl()))
            {
                continue;
            }

            String mediaType = clean(media.getMediaType());
            if (StringUtils.isEmpty(mediaType))
            {
                mediaType = inferMediaType(media.getFileUrl());
            }
            if (!isAllowedMediaType(mediaType))
            {
                throw new ServiceException("题目媒体类型不支持：" + mediaType);
            }

            String fileUrl = cleanUrl(media.getFileUrl());
            if (StringUtils.isEmpty(fileUrl))
            {
                continue;
            }

            if ("image".equals(mediaType))
            {
                imageCount++;
                if (imageCount > MAX_IMAGE_COUNT)
                {
                    throw new ServiceException("题干图片最多上传6张");
                }
                validateFileExtension(fileUrl, "题干图片支持：" + ALLOWED_IMAGE_EXTENSION_TIP, ALLOWED_IMAGE_EXTENSIONS);
                validateFileSize(media.getFileSize(), MAX_IMAGE_SIZE, "题干图片大小不能超过10MB");
            }
            else if ("video".equals(mediaType))
            {
                videoCount++;
                if (videoCount > MAX_VIDEO_COUNT)
                {
                    throw new ServiceException("题干视频最多上传1个");
                }
                validateFileExtension(fileUrl, "题干视频支持：" + ALLOWED_VIDEO_EXTENSION_TIP, ALLOWED_VIDEO_EXTENSIONS);
                validateFileSize(media.getFileSize(), MAX_VIDEO_SIZE, "题干视频大小不能超过100MB");
            }

            ExamQuestionMedia normalized = new ExamQuestionMedia();
            normalized.setQuestionId(questionId);
            normalized.setMediaType(mediaType);
            normalized.setFileUrl(fileUrl);
            normalized.setThumbnailUrl(cleanUrl(media.getThumbnailUrl()));
            normalized.setFileName(clean(media.getFileName()));
            normalized.setFileSize(media.getFileSize());
            normalized.setDuration(media.getDuration());
            normalized.setSortOrder(media.getSortOrder() == null ? sortOrder : media.getSortOrder());
            normalized.setDisplayPosition(normalizeDisplayPosition(media.getDisplayPosition()));
            normalized.setStatus(StringUtils.defaultIfEmpty(clean(media.getStatus()), "0"));
            normalized.setCreateBy(operName);
            normalized.setRemark(clean(media.getRemark()));
            normalizedList.add(normalized);
            sortOrder++;
        }

        return normalizedList;
    }

    private String normalizeDisplayPosition(String displayPosition)
    {
        String value = clean(displayPosition);
        if ("analysis".equals(value))
        {
            return "analysis";
        }
        return "stem";
    }

    private boolean isAllowedMediaType(String mediaType)
    {
        return "image".equals(mediaType) || "video".equals(mediaType);
    }

    private String inferMediaType(String fileUrl)
    {
        String value = removeUrlQuery(StringUtils.defaultString(fileUrl).toLowerCase());
        if (hasAllowedExtension(value, ALLOWED_IMAGE_EXTENSIONS))
        {
            return "image";
        }
        if (hasAllowedExtension(value, ALLOWED_VIDEO_EXTENSIONS))
        {
            return "video";
        }
        return "file";
    }

    private void validateFileExtension(String fileUrl, String message, String... extensions)
    {
        String cleanUrl = removeUrlQuery(StringUtils.defaultString(fileUrl).toLowerCase());
        for (String extension : extensions)
        {
            if (cleanUrl.endsWith("." + extension))
            {
                return;
            }
        }
        throw new ServiceException("题目媒体文件格式不支持，" + message);
    }

    private boolean hasAllowedExtension(String fileUrl, String... extensions)
    {
        String cleanUrl = removeUrlQuery(StringUtils.defaultString(fileUrl).toLowerCase());
        for (String extension : extensions)
        {
            if (cleanUrl.endsWith("." + extension))
            {
                return true;
            }
        }
        return false;
    }

    private void validateFileSize(Long fileSize, long maxSize, String message)
    {
        if (fileSize != null && fileSize > maxSize)
        {
            throw new ServiceException(message);
        }
    }

    private String removeUrlQuery(String url)
    {
        int queryIndex = url.indexOf("?");
        if (queryIndex >= 0)
        {
            return url.substring(0, queryIndex);
        }
        return url;
    }

    private String cleanUrl(String value)
    {
        String cleaned = clean(value);
        if (StringUtils.isEmpty(cleaned))
        {
            return cleaned;
        }

        String lower = cleaned.toLowerCase();
        if (lower.startsWith("javascript:") || lower.startsWith("data:") || lower.startsWith("vbscript:"))
        {
            throw new ServiceException("题目媒体地址不合法");
        }
        if (lower.contains("\r") || lower.contains("\n"))
        {
            throw new ServiceException("题目媒体地址不合法");
        }
        return cleaned;
    }

    private String clean(String value)
    {
        return StringUtils.isEmpty(value) ? value : EscapeUtil.clean(value.trim());
    }
}
