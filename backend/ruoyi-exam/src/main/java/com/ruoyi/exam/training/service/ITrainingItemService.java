package com.ruoyi.exam.training.service;

import java.util.List;
import java.util.Map;
import com.ruoyi.exam.training.domain.TrainingItem;
import com.ruoyi.exam.training.domain.TrainingSummary;

public interface ITrainingItemService
{
    public List<TrainingItem> selectTrainingItemList(TrainingItem item);

    public List<TrainingItem> selectLearningLedgerList(TrainingItem item);

    public List<TrainingItem> selectPublishedLearningList(String itemType);

    public TrainingItem selectTrainingItemById(Long id);

    public int insertTrainingItem(TrainingItem item);

    public int insertTrainingItems(TrainingItem item);

    public int updateTrainingItem(TrainingItem item);

    public int deleteTrainingItemByIds(Long[] ids);

    public TrainingSummary selectAdminSummary();

    public TrainingSummary selectStudentSummary(Long userId);

    public List<Map<String, Object>> selectCourseOptions(String keyword);

    public List<Map<String, Object>> selectExamOptions(String keyword);

    public List<Map<String, Object>> selectStudentOptions(String keyword);

    public TrainingItem markCourseProgress(Long courseId, Long userId, String username, String status, Integer durationMinutes);

    public TrainingItem recordPractice(Long userId, String username, TrainingItem practiceRecord);

    public TrainingItem askQuestion(Long userId, String username, TrainingItem question);

    public int replyQuestion(Long id, String answerContent, String username);

    public TrainingItem issueCertificate(Long userId, String username, TrainingItem certificate);

    public int issueCertificates(TrainingItem certificate, String username);

    public int processMediaResource(Long id, String username);
}
