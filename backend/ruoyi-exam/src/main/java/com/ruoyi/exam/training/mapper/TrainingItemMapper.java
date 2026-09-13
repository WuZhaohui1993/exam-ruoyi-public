package com.ruoyi.exam.training.mapper;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.exam.training.domain.TrainingItem;
import com.ruoyi.exam.training.domain.TrainingSummary;

public interface TrainingItemMapper
{
    public List<TrainingItem> selectTrainingItemList(TrainingItem item);

    public List<TrainingItem> selectLearningLedgerList(TrainingItem item);

    public TrainingItem selectTrainingItemById(Long id);

    public int insertTrainingItem(TrainingItem item);

    public int updateTrainingItem(TrainingItem item);

    public int deleteTrainingItemByIds(Long[] ids);

    public int updateProcessingResult(TrainingItem item);

    public TrainingSummary selectAdminSummary();

    public TrainingSummary selectStudentSummary(@Param("userId") Long userId);

    public List<Map<String, Object>> selectCourseOptions(@Param("keyword") String keyword);

    public List<Map<String, Object>> selectExamOptions(@Param("keyword") String keyword);

    public List<Map<String, Object>> selectStudentOptions(@Param("keyword") String keyword);
}
