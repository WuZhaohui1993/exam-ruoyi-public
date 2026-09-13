package com.ruoyi.exam.question.service.impl;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.ruoyi.common.constant.UserConstants;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.html.HtmlSanitizer;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.exam.question.domain.ExamQuestion;
import com.ruoyi.exam.question.domain.ExamQuestionCategory;
import com.ruoyi.exam.question.domain.QuestionOption;
import com.ruoyi.exam.question.domain.QuestionImportResult;
import com.ruoyi.exam.question.domain.ExamQuestionMedia;
import com.ruoyi.exam.question.domain.ImportError;
import com.ruoyi.exam.question.mapper.ExamQuestionMapper;
import com.ruoyi.exam.question.mapper.ExamQuestionCategoryMapper;
import com.ruoyi.exam.question.service.IExamQuestionMediaService;
import com.ruoyi.exam.question.service.IExamQuestionService;
import com.ruoyi.exam.question.utils.QuestionTypeUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 试题 服务层处理
 * 
 * @author ruoyi
 */
@Service
public class ExamQuestionServiceImpl implements IExamQuestionService {
    private static final Logger logger = LoggerFactory.getLogger(ExamQuestionServiceImpl.class);

    @Autowired
    private ExamQuestionMapper questionMapper;

    @Autowired
    private ExamQuestionCategoryMapper categoryMapper;

    @Autowired
    private IExamQuestionMediaService questionMediaService;

    /**
     * 查询试题列表
     * 
     * @param question 试题信息
     * @return 试题集合
     */
    @Override
    public List<ExamQuestion> selectQuestionList(ExamQuestion question) {
        List<ExamQuestion> list = questionMapper.selectQuestionList(question);
        fillQuestionMedia(list);
        return list;
    }

    /**
     * 根据试题ID查询试题信息
     * 
     * @param id 试题ID
     * @return 试题信息
     */
    @Override
    public ExamQuestion selectQuestionById(Long id) {
        ExamQuestion question = questionMapper.selectQuestionById(id);
        fillQuestionMedia(question);
        return question;
    }

    /**
     * 根据分类ID查询试题列表
     * 
     * @param categoryId 分类ID
     * @return 试题列表
     */
    @Override
    public List<ExamQuestion> selectQuestionsByCategoryId(Long categoryId) {
        List<ExamQuestion> list = questionMapper.selectQuestionsByCategoryId(categoryId);
        fillQuestionMedia(list);
        return list;
    }

    /**
     * 根据题型查询试题列表
     * 
     * @param questionType 题型
     * @return 试题列表
     */
    @Override
    public List<ExamQuestion> selectQuestionsByType(String questionType) {
        List<ExamQuestion> list = questionMapper.selectQuestionsByType(questionType);
        fillQuestionMedia(list);
        return list;
    }

    /**
     * 根据难度等级查询试题列表
     * 
     * @param difficultyLevel 难度等级
     * @return 试题列表
     */
    @Override
    public List<ExamQuestion> selectQuestionsByDifficulty(Integer difficultyLevel) {
        List<ExamQuestion> list = questionMapper.selectQuestionsByDifficulty(difficultyLevel);
        fillQuestionMedia(list);
        return list;
    }

    /**
     * 校验试题标题是否唯一
     * 
     * @param question 试题信息
     * @return 结果
     */
    @Override
    public boolean checkQuestionTitleUnique(ExamQuestion question) {
        Long questionId = StringUtils.isNull(question.getId()) ? -1L : question.getId();
        ExamQuestion info = questionMapper.checkQuestionTitleUnique(question.getQuestionTitle(),
                question.getCategoryId());
        if (StringUtils.isNotNull(info) && info.getId().longValue() != questionId.longValue()) {
            return UserConstants.NOT_UNIQUE;
        }
        return UserConstants.UNIQUE;
    }

    /**
     * 新增试题
     * 
     * @param question 试题信息
     * @return 结果
     */
    @Override
    @Transactional
    public int insertQuestion(ExamQuestion question) {
        // 设置默认值
        if (question.getDifficultyLevel() == null) {
            question.setDifficultyLevel(1);
        }
        if (question.getScore() == null) {
            question.setScore(new BigDecimal("1.00"));
        }
        if (StringUtils.isEmpty(question.getStatus())) {
            question.setStatus("0");
        }

        // 处理选项和答案标准化
        processQuestionOptions(question);

        // 根据选项的isCorrect标记生成correctAnswer字段
        generateCorrectAnswerFromOptions(question);

        sanitizeQuestion(question);

        // 验证试题选项的合理性
        if (!question.validateOptions()) {
            throw new ServiceException("试题选项设置不合理，请检查题型和选项配置");
        }

        int rows = questionMapper.insertQuestion(question);
        questionMediaService.replaceQuestionMedia(question.getId(), question.getMediaList(), question.getCreateBy());
        return rows;
    }

    /**
     * 修改试题
     * 
     * @param question 试题信息
     * @return 结果
     */
    @Override
    @Transactional
    public int updateQuestion(ExamQuestion question) {
        // 处理选项和答案标准化
        processQuestionOptions(question);

        // 根据选项的isCorrect标记生成correctAnswer字段
        generateCorrectAnswerFromOptions(question);

        sanitizeQuestion(question);

        // 验证试题选项的合理性
        if (!question.validateOptions()) {
            throw new ServiceException("试题选项设置不合理，请检查题型和选项配置");
        }

        int rows = questionMapper.updateQuestion(question);
        questionMediaService.replaceQuestionMedia(question.getId(), question.getMediaList(), question.getUpdateBy());
        return rows;
    }

    /**
     * 处理试题选项，根据题型进行标准化处理
     * 
     * @param question 试题信息
     */
    private void processQuestionOptions(ExamQuestion question) {
        String questionType = question.getQuestionType();

        // 如果选项列表为空，根据题型初始化标准选项
        if (question.getOptionList() == null || question.getOptionList().isEmpty()) {
            List<QuestionOption> standardOptions = QuestionTypeUtils.initStandardOptions(questionType);
            question.setOptionList(standardOptions);
        }

        // 特殊处理判断题，确保选项固定为"正确/错误"
        if ("judge".equals(questionType)) {
            List<QuestionOption> judgeOptions = QuestionTypeUtils.initStandardOptions("judge");

            // 保持原有的正确答案设置
            List<QuestionOption> currentOptions = question.getOptionList();
            if (currentOptions != null && currentOptions.size() >= 2) {
                // 检查哪个选项被标记为正确答案
                for (QuestionOption option : currentOptions) {
                    if (Boolean.TRUE.equals(option.getIsCorrect())) {
                        // 根据原选项的key来设置新选项的正确答案
                        if ("A".equals(option.getKey()) || "true".equals(option.getKey())
                                || "正确".equals(option.getValue())) {
                            judgeOptions.get(0).setIsCorrect(true);
                        } else {
                            judgeOptions.get(1).setIsCorrect(true);
                        }
                        break;
                    }
                }
            }

            question.setOptionList(judgeOptions);
        }
    }

    private void sanitizeQuestion(ExamQuestion question) {
        if (question == null) {
            return;
        }
        question.setQuestionTitle(cleanText(question.getQuestionTitle()));
        question.setQuestionContent(cleanRichText(question.getQuestionContent()));
        question.setCorrectAnswer(cleanText(question.getCorrectAnswer()));
        question.setQuestionAnalysis(cleanRichText(question.getQuestionAnalysis()));
        question.setTags(cleanText(question.getTags()));

        List<QuestionOption> options = question.getOptionList();
        if (options != null) {
            for (QuestionOption option : options) {
                if (option != null) {
                    option.setValue(cleanText(option.getValue()));
                }
            }
            question.setOptionList(options);
        } else if (StringUtils.isNotEmpty(question.getQuestionOptions())) {
            question.setQuestionOptions(HtmlSanitizer.cleanText(question.getQuestionOptions()));
        }
    }

    private String cleanRichText(String value) {
        return HtmlSanitizer.cleanRichText(value);
    }

    private String cleanText(String value) {
        return HtmlSanitizer.cleanText(value);
    }

    /**
     * 批量删除试题
     * 
     * @param ids 需要删除的试题ID
     * @return 结果
     */
    @Override
    @Transactional
    public int deleteQuestionByIds(Long[] ids) {
        // 检查试题是否被试卷引用
        for (Long id : ids) {
            if (isQuestionReferencedByPaper(id)) {
                ExamQuestion question = questionMapper.selectQuestionById(id);
                String questionTitle = question != null ? question.getQuestionTitle() : "ID:" + id;
                throw new ServiceException(String.format("试题【%s】正在被试卷引用，不允许删除", questionTitle));
            }
        }
        questionMediaService.deleteMediaByQuestionIds(ids);
        return questionMapper.deleteQuestionByIds(ids);
    }

    /**
     * 删除试题信息
     * 
     * @param id 试题ID
     * @return 结果
     */
    @Override
    @Transactional
    public int deleteQuestionById(Long id) {
        // 检查试题是否被试卷引用
        if (isQuestionReferencedByPaper(id)) {
            ExamQuestion question = questionMapper.selectQuestionById(id);
            String questionTitle = question != null ? question.getQuestionTitle() : "ID:" + id;
            throw new ServiceException(String.format("试题【%s】正在被试卷引用，不允许删除", questionTitle));
        }
        questionMediaService.deleteMediaByQuestionId(id);
        return questionMapper.deleteQuestionById(id);
    }

    /**
     * 检查试题是否被试卷引用
     * 
     * @param questionId 试题ID
     * @return 是否被引用
     */
    private boolean isQuestionReferencedByPaper(Long questionId) {
        return questionMapper.countPapersByQuestionId(questionId) > 0;
    }

    /**
     * 根据标签查询试题
     * 
     * @param tags 标签
     * @return 试题列表
     */
    @Override
    public List<ExamQuestion> selectQuestionsByTags(String tags) {
        List<ExamQuestion> list = questionMapper.selectQuestionsByTags(tags);
        fillQuestionMedia(list);
        return list;
    }

    /**
     * 统计试题数量
     * 
     * @param question 查询条件
     * @return 试题数量
     */
    @Override
    public int countQuestions(ExamQuestion question) {
        return questionMapper.countQuestions(question);
    }

    /**
     * 预览试题
     * 
     * @param id 试题ID
     * @return 试题详细信息
     */
    @Override
    public ExamQuestion previewQuestion(Long id) {
        ExamQuestion question = questionMapper.selectQuestionById(id);
        if (question != null) {
            fillQuestionMedia(question);

            // 处理选项数据
            processQuestionOptions(question);

            // 确保选项数据格式正确，便于前端预览显示
            List<QuestionOption> optionList = question.getOptionList();
            if (optionList != null && !optionList.isEmpty()) {
                // 根据正确答案设置选项的isCorrect标记
                String correctAnswer = question.getCorrectAnswer();
                if (StringUtils.isNotEmpty(correctAnswer)) {
                    setCorrectAnswerFlags(question.getQuestionType(), optionList, correctAnswer);
                    question.setOptionList(optionList);
                }
            } else if (StringUtils.isNotEmpty(question.getQuestionOptions())) {
                // 如果optionList为空但questionOptions有数据，尝试解析
                try {
                    ObjectMapper objectMapper = new ObjectMapper();
                    List<QuestionOption> parsedOptions = objectMapper.readValue(
                            question.getQuestionOptions(),
                            new TypeReference<List<QuestionOption>>() {
                            });
                    if (parsedOptions != null && !parsedOptions.isEmpty()) {
                        String correctAnswer = question.getCorrectAnswer();
                        if (StringUtils.isNotEmpty(correctAnswer)) {
                            setCorrectAnswerFlags(question.getQuestionType(), parsedOptions, correctAnswer);
                        }
                        question.setOptionList(parsedOptions);
                    }
                } catch (Exception e) {
                    logger.error("解析试题选项失败: " + e.getMessage(), e);
                }
            }
        }
        return question;
    }

    /**
     * 根据选项的isCorrect标记生成correctAnswer字段
     * 
     * @param question 试题信息
     */
    private void generateCorrectAnswerFromOptions(ExamQuestion question) {
        if (question.getOptionList() == null || question.getOptionList().isEmpty()) {
            return;
        }

        List<QuestionOption> optionList = question.getOptionList();
        String questionType = question.getQuestionType();

        StringBuilder correctAnswer = new StringBuilder();

        switch (questionType) {
            case "single":
            case "judge":
                // 单选题和判断题只有一个正确答案
                optionList.stream()
                        .filter(option -> Boolean.TRUE.equals(option.getIsCorrect()))
                        .findFirst()
                        .ifPresent(option -> correctAnswer.append(option.getKey()));
                break;

            case "multiple":
                // 多选题可能有多个正确答案，用逗号分隔
                List<String> correctKeys = optionList.stream()
                        .filter(option -> Boolean.TRUE.equals(option.getIsCorrect()))
                        .map(QuestionOption::getKey)
                        .collect(java.util.stream.Collectors.toList());
                correctAnswer.append(String.join(",", correctKeys));
                break;

            default:
                // 填空题和简答题的正确答案通常直接设置在question.correctAnswer中
                return;
        }

        String finalAnswer = correctAnswer.toString();
        if (StringUtils.isNotEmpty(finalAnswer)) {
            question.setCorrectAnswer(finalAnswer);
        }
    }

    /**
     * 根据正确答案设置选项的isCorrect标记
     * 
     * @param questionType  题型
     * @param optionList    选项列表
     * @param correctAnswer 正确答案
     */
    private void setCorrectAnswerFlags(String questionType, List<QuestionOption> optionList, String correctAnswer) {
        // 先重置所有选项的正确答案标记
        optionList.forEach(option -> option.setIsCorrect(false));

        switch (questionType) {
            case "single":
            case "judge":
                // 单选题和判断题只有一个正确答案
                optionList.stream()
                        .filter(option -> correctAnswer.equals(option.getKey()))
                        .findFirst()
                        .ifPresent(option -> option.setIsCorrect(true));
                break;

            case "multiple":
                // 多选题可能有多个正确答案，用逗号分隔
                if (correctAnswer.contains(",")) {
                    String[] correctOptions = correctAnswer.split(",");
                    for (String correctOption : correctOptions) {
                        optionList.stream()
                                .filter(option -> correctOption.trim().equals(option.getKey()))
                                .findFirst()
                                .ifPresent(option -> option.setIsCorrect(true));
                    }
                } else {
                    // 只有一个正确答案
                    optionList.stream()
                            .filter(option -> correctAnswer.equals(option.getKey()))
                            .findFirst()
                            .ifPresent(option -> option.setIsCorrect(true));
                }
                break;

            default:
                // 填空题和简答题不需要设置选项的正确答案标记
                break;
        }
    }

    /**
     * 批量导入试题（支持新旧两种模板格式）- 兼容旧版本
     * 
     * @param file          Excel文件
     * @param updateSupport 是否更新支持，如果已存在，则进行更新数据
     * @param operName      操作用户
     * @return 结果
     */
    @Override
    public String importQuestion(MultipartFile file, Boolean updateSupport, String operName) throws Exception {
        QuestionImportResult result = importQuestionWithErrorFile(file, updateSupport, operName);
        return result.getMessage();
    }

    /**
     * 批量导入试题（支持错误文件生成）
     * 
     * @param file          Excel文件
     * @param updateSupport 是否更新支持，如果已存在，则进行更新数据
     * @param operName      操作用户
     * @return 结果
     */
    @Override
    public QuestionImportResult importQuestionWithErrorFile(MultipartFile file, Boolean updateSupport, String operName)
            throws Exception {
        try {
            // 尝试新模板格式导入（多sheet按题型分类）
            return importQuestionFromNewTemplateWithError(file, updateSupport, operName);
        } catch (Exception e) {
            logger.warn("新模板格式导入失败，尝试使用传统模板格式: " + e.getMessage());
            // 如果新模板失败，回退到传统模板格式
            return importQuestionFromOldTemplateWithError(file, updateSupport, operName);
        }
    }

    /**
     * 使用新模板格式导入（多sheet按题型分类）- 支持错误标记
     */
    private QuestionImportResult importQuestionFromNewTemplateWithError(MultipartFile file, Boolean updateSupport,
            String operName) throws Exception {
        Map<String, List<ImportError>> sheetErrors = new HashMap<>();
        Map<String, List<ExamQuestion>> sheetQuestions = new HashMap<>();

        try (Workbook originalWorkbook = WorkbookFactory.create(file.getInputStream())) {
            // 检查是否是新模板格式（有多个题型sheet）
            String[] sheetNames = { "单选题模板", "多选题模板", "判断题模板", "填空题模板", "简答题模板" };
            boolean hasNewFormatSheets = false;

            for (String sheetName : sheetNames) {
                Sheet sheet = originalWorkbook.getSheet(sheetName);
                if (sheet != null && sheet.getLastRowNum() > 0) {
                    hasNewFormatSheets = true;
                    break;
                }
            }

            if (!hasNewFormatSheets) {
                throw new ServiceException("未发现新模板格式的sheet页");
            }

            // 处理各个题型的sheet
            for (String sheetName : sheetNames) {
                Sheet sheet = originalWorkbook.getSheet(sheetName);
                if (sheet != null && sheet.getLastRowNum() > 0) {
                    List<ImportError> errors = new ArrayList<>();
                    List<ExamQuestion> questions = parseSheetDataWithError(sheet, sheetName, errors);
                    sheetQuestions.put(sheetName, questions);
                    if (!errors.isEmpty()) {
                        sheetErrors.put(sheetName, errors);
                    }
                }
            }

            // 逐个sheet处理导入并收集数据库层面的错误
            int totalSuccessNum = 0;
            int totalFailureNum = 0;
            StringBuilder totalSuccessMsg = new StringBuilder();
            StringBuilder totalFailureMsg = new StringBuilder();

            for (String sheetName : sheetNames) {
                List<ExamQuestion> questions = sheetQuestions.get(sheetName);
                List<ImportError> sheetParseErrors = sheetErrors.get(sheetName);

                // 统计解析失败的数量
                int parseErrorCount = (sheetParseErrors != null) ? sheetParseErrors.size() : 0;

                if (questions != null && !questions.isEmpty()) {
                    QuestionImportResult sheetResult = importQuestionListWithSheetError(questions, updateSupport,
                            operName, sheetName, sheetErrors);
                    totalSuccessNum += sheetResult.getSuccessCount();
                    totalFailureNum += sheetResult.getFailureCount();
                    totalSuccessMsg.append(sheetResult.getMessage());
                }

                // 将解析失败的数量加入到总失败数量中
                totalFailureNum += parseErrorCount;

                // 如果有解析失败，添加失败消息
                if (parseErrorCount > 0) {
                    totalFailureMsg.append("<br/>").append(sheetName).append(" - 解析失败 ").append(parseErrorCount)
                            .append(" 条");
                }
            }

            // 创建最终结果
            QuestionImportResult result;
            if (totalFailureNum > 0) {
                String message = "导入完成！成功 " + totalSuccessNum + " 条，失败 " + totalFailureNum + " 条。"
                        + totalFailureMsg.toString();
                result = new QuestionImportResult(totalSuccessNum, totalFailureNum, message);
            } else {
                String message = "恭喜您，数据已全部导入成功！共 " + totalSuccessNum + " 条，数据如下：" + totalSuccessMsg.toString();
                result = new QuestionImportResult(totalSuccessNum, totalFailureNum, message);
            }

            // 如果有错误，生成错误文件
            if (!sheetErrors.isEmpty() || result.getFailureCount() > 0) {
                byte[] errorFileData = generateErrorFile(originalWorkbook, sheetErrors);
                result.setHasErrorFile(true);
                result.setErrorFileName("题目导入错误报告_" + System.currentTimeMillis() + ".xlsx");
                result.setErrorFileData(errorFileData);
            }

            return result;
        }
    }

    /**
     * 使用传统模板格式导入（单sheet）- 支持错误标记
     */
    private QuestionImportResult importQuestionFromOldTemplateWithError(MultipartFile file, Boolean updateSupport,
            String operName) throws Exception {
        try (Workbook originalWorkbook = WorkbookFactory.create(file.getInputStream());
                ByteArrayInputStream inputStream = new ByteArrayInputStream(file.getBytes())) {
            ExcelUtil<ExamQuestion> util = new ExcelUtil<ExamQuestion>(ExamQuestion.class);
            List<ExamQuestion> questionList = util.importExcel(inputStream);
            QuestionImportResult result = importQuestionListWithError(questionList, updateSupport, operName);

            // 如果有失败记录，生成错误文件
            if (result.getFailureCount() > 0) {
                // 创建错误信息映射
                Map<String, List<ImportError>> sheetErrors = new HashMap<>();
                List<ImportError> errors = new ArrayList<>();

                // 这里无法获取具体的行号，所以创建一个通用的错误信息
                ImportError error = new ImportError();
                error.setRowIndex(0);
                error.setErrorMessage("请查看导入结果消息中的详细错误信息");
                errors.add(error);

                // 假设使用第一个sheet
                Sheet firstSheet = originalWorkbook.getSheetAt(0);
                if (firstSheet != null) {
                    sheetErrors.put(firstSheet.getSheetName(), errors);
                }

                byte[] errorFileData = generateErrorFile(originalWorkbook, sheetErrors);
                result.setHasErrorFile(true);
                result.setErrorFileName("题目导入错误报告_" + System.currentTimeMillis() + ".xlsx");
                result.setErrorFileData(errorFileData);
            }

            return result;
        }
    }

    /**
     * 解析sheet数据为试题对象 - 支持错误收集
     */
    private List<ExamQuestion> parseSheetDataWithError(Sheet sheet, String sheetName, List<ImportError> errors)
            throws Exception {
        List<ExamQuestion> questions = new ArrayList<>();
        String questionType = getQuestionTypeFromSheetName(sheetName);

        // 获取表头行
        Row headerRow = sheet.getRow(0);
        if (headerRow == null) {
            return questions;
        }

        // 创建列名到索引的映射
        Map<String, Integer> columnMap = new HashMap<>();
        for (int i = 0; i < headerRow.getLastCellNum(); i++) {
            Cell cell = headerRow.getCell(i);
            if (cell != null) {
                String columnName = cell.getStringCellValue();
                columnMap.put(columnName, i);
            }
        }

        // 处理数据行
        for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
            Row row = sheet.getRow(rowIndex);
            if (row == null || isRowEmpty(row)) {
                continue;
            }

            try {
                ExamQuestion question = parseRowToQuestion(row, columnMap, questionType);
                if (question != null) {
                    questions.add(question);
                }
            } catch (Exception e) {
                // 记录解析错误
                ImportError error = new ImportError();
                error.setRowIndex(rowIndex);
                error.setErrorMessage(e.getMessage());
                errors.add(error);
                logger.warn("解析第{}行数据失败: {}", rowIndex + 1, e.getMessage());
            }
        }

        return questions;
    }

    /**
     * 导入试题列表并收集错误（支持Excel行号映射）
     */
    private QuestionImportResult importQuestionListWithSheetError(List<ExamQuestion> questionList,
            Boolean updateSupport, String operName, String sheetName, Map<String, List<ImportError>> sheetErrors) {
        if (questionList == null || questionList.isEmpty()) {
            return new QuestionImportResult(0, 0, "导入试题数据不能为空！");
        }

        int successNum = 0;
        int failureNum = 0;
        StringBuilder successMsg = new StringBuilder();
        StringBuilder failureMsg = new StringBuilder();

        // 获取当前sheet的错误列表，如果不存在就创建一个
        List<ImportError> errors = sheetErrors.computeIfAbsent(sheetName, k -> new ArrayList<>());

        for (int i = 0; i < questionList.size(); i++) {
            ExamQuestion question = questionList.get(i);
            int excelRowIndex = i + 1; // Excel行号从1开始（表头是第0行）

            try {
                // 验证是否存在这个试题
                ExamQuestion q = questionMapper.checkQuestionTitleUnique(question.getQuestionTitle(),
                        question.getCategoryId());
                if (StringUtils.isNull(q)) {
                    question.setCreateBy(operName);
                    this.insertQuestion(question);
                    successNum++;
                    successMsg.append("<br/>" + successNum + "、试题 " + question.getQuestionTitle() + " 导入成功");
                } else if (updateSupport) {
                    question.setId(q.getId());
                    question.setUpdateBy(operName);
                    this.updateQuestion(question);
                    successNum++;
                    successMsg.append("<br/>" + successNum + "、试题 " + question.getQuestionTitle() + " 更新成功");
                } else {
                    failureNum++;
                    failureMsg.append("<br/>" + failureNum + "、试题 " + question.getQuestionTitle() + " 已存在");

                    // 添加到错误列表，映射到具体的Excel行
                    ImportError error = new ImportError();
                    error.setRowIndex(excelRowIndex);
                    error.setErrorMessage("试题已存在");
                    errors.add(error);
                }
            } catch (Exception e) {
                failureNum++;
                String msg = "<br/>" + failureNum + "、试题 " + question.getQuestionTitle() + " 导入失败：";
                failureMsg.append(msg + e.getMessage());

                // 添加到错误列表，映射到具体的Excel行
                ImportError error = new ImportError();
                error.setRowIndex(excelRowIndex);
                error.setErrorMessage("导入失败：" + e.getMessage());
                errors.add(error);
            }
        }

        QuestionImportResult result;
        if (failureNum > 0) {
            String message = sheetName + " - 成功 " + successNum + " 条，失败 " + failureNum + " 条。" + failureMsg.toString();
            result = new QuestionImportResult(successNum, failureNum, message);
        } else {
            String message = sheetName + " - 全部导入成功！共 " + successNum + " 条。" + successMsg.toString();
            result = new QuestionImportResult(successNum, failureNum, message);
        }

        return result;
    }

    /**
     * 导入试题列表并收集错误
     */
    private QuestionImportResult importQuestionListWithError(List<ExamQuestion> questionList, Boolean updateSupport,
            String operName) {
        if (questionList == null || questionList.isEmpty()) {
            return new QuestionImportResult(0, 0, "导入试题数据不能为空！");
        }

        int successNum = 0;
        int failureNum = 0;
        StringBuilder successMsg = new StringBuilder();
        StringBuilder failureMsg = new StringBuilder();

        for (ExamQuestion question : questionList) {
            try {
                // 验证是否存在这个试题
                ExamQuestion q = questionMapper.checkQuestionTitleUnique(question.getQuestionTitle(),
                        question.getCategoryId());
                if (StringUtils.isNull(q)) {
                    question.setCreateBy(operName);
                    this.insertQuestion(question);
                    successNum++;
                    successMsg.append("<br/>" + successNum + "、试题 " + question.getQuestionTitle() + " 导入成功");
                } else if (updateSupport) {
                    question.setId(q.getId());
                    question.setUpdateBy(operName);
                    this.updateQuestion(question);
                    successNum++;
                    successMsg.append("<br/>" + successNum + "、试题 " + question.getQuestionTitle() + " 更新成功");
                } else {
                    failureNum++;
                    failureMsg.append("<br/>" + failureNum + "、试题 " + question.getQuestionTitle() + " 已存在");
                }
            } catch (Exception e) {
                failureNum++;
                String msg = "<br/>" + failureNum + "、试题 " + question.getQuestionTitle() + " 导入失败：";
                failureMsg.append(msg + e.getMessage());
            }
        }

        QuestionImportResult result;
        if (failureNum > 0) {
            String message = "导入完成！成功 " + successNum + " 条，失败 " + failureNum + " 条。" + failureMsg.toString();
            result = new QuestionImportResult(successNum, failureNum, message);
        } else {
            String message = "恭喜您，数据已全部导入成功！共 " + successNum + " 条，数据如下：" + successMsg.toString();
            result = new QuestionImportResult(successNum, failureNum, message);
        }

        return result;
    }

    /**
     * 生成带有错误标记的Excel文件
     */
    private byte[] generateErrorFile(Workbook originalWorkbook, Map<String, List<ImportError>> sheetErrors)
            throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try {
            // 为每个有错误的sheet添加错误列
            for (String sheetName : sheetErrors.keySet()) {
                Sheet sheet = originalWorkbook.getSheet(sheetName);
                if (sheet != null) {
                    addErrorColumnToSheet(sheet, sheetErrors.get(sheetName));
                }
            }

            originalWorkbook.write(outputStream);
            return outputStream.toByteArray();
        } finally {
            outputStream.close();
        }
    }

    /**
     * 向sheet添加错误列
     */
    private void addErrorColumnToSheet(Sheet sheet, List<ImportError> errors) {
        // 获取表头行
        Row headerRow = sheet.getRow(0);
        if (headerRow == null) {
            return;
        }

        // 查找是否已经存在"导入错误信息"列
        int errorColumnIndex = -1;
        for (int i = 0; i < headerRow.getLastCellNum(); i++) {
            Cell cell = headerRow.getCell(i);
            if (cell != null && "导入错误信息".equals(cell.getStringCellValue())) {
                errorColumnIndex = i;
                break;
            }
        }

        // 如果不存在错误列，则创建新列
        if (errorColumnIndex == -1) {
            errorColumnIndex = headerRow.getLastCellNum();

            // 创建错误列表头
            Cell errorHeaderCell = headerRow.createCell(errorColumnIndex);
            errorHeaderCell.setCellValue("导入错误信息");

            // 设置错误列表头样式
            CellStyle headerStyle = sheet.getWorkbook().createCellStyle();
            Font headerFont = sheet.getWorkbook().createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.RED.getIndex());
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            errorHeaderCell.setCellStyle(headerStyle);
        }

        // 创建错误信息样式
        CellStyle errorStyle = sheet.getWorkbook().createCellStyle();
        Font errorFont = sheet.getWorkbook().createFont();
        errorFont.setColor(IndexedColors.RED.getIndex());
        errorStyle.setFont(errorFont);
        errorStyle.setWrapText(true);

        // 清空错误列的现有内容（除了表头）
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row != null) {
                Cell cell = row.getCell(errorColumnIndex);
                if (cell != null) {
                    cell.setCellValue(""); // 清空原有错误信息
                }
            }
        }

        // 为每个错误行添加错误信息
        for (ImportError error : errors) {
            Row row = sheet.getRow(error.getRowIndex());
            if (row != null) {
                Cell errorCell = row.getCell(errorColumnIndex);
                if (errorCell == null) {
                    errorCell = row.createCell(errorColumnIndex);
                }
                errorCell.setCellValue(error.getErrorMessage());
                errorCell.setCellStyle(errorStyle);
            }
        }

        // 自动调整错误列的宽度
        sheet.autoSizeColumn(errorColumnIndex);
    }

    /**
     * 解析sheet数据为试题对象
     */
    private List<ExamQuestion> parseSheetData(Sheet sheet, String sheetName) throws Exception {
        List<ExamQuestion> questions = new ArrayList<>();
        String questionType = getQuestionTypeFromSheetName(sheetName);

        // 获取表头行
        Row headerRow = sheet.getRow(0);
        if (headerRow == null) {
            return questions;
        }

        // 创建列名到索引的映射
        Map<String, Integer> columnMap = new HashMap<>();
        for (int i = 0; i < headerRow.getLastCellNum(); i++) {
            Cell cell = headerRow.getCell(i);
            if (cell != null) {
                String columnName = cell.getStringCellValue();
                columnMap.put(columnName, i);
            }
        }

        // 处理数据行
        for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
            Row row = sheet.getRow(rowIndex);
            if (row == null || isRowEmpty(row)) {
                continue;
            }

            try {
                ExamQuestion question = parseRowToQuestion(row, columnMap, questionType);
                if (question != null) {
                    questions.add(question);
                }
            } catch (Exception e) {
                logger.warn("解析第{}行数据失败: {}", rowIndex + 1, e.getMessage());
            }
        }

        return questions;
    }

    /**
     * 根据sheet名称获取题型
     */
    private String getQuestionTypeFromSheetName(String sheetName) {
        if (sheetName.contains("单选")) {
            return "single";
        } else if (sheetName.contains("多选")) {
            return "multiple";
        } else if (sheetName.contains("判断")) {
            return "judge";
        } else if (sheetName.contains("填空")) {
            return "fill";
        } else if (sheetName.contains("简答")) {
            return "essay";
        }
        return "single"; // 默认单选题
    }

    /**
     * 解析行数据为试题对象
     */
    private ExamQuestion parseRowToQuestion(Row row, Map<String, Integer> columnMap, String questionType)
            throws Exception {
        ExamQuestion question = new ExamQuestion();
        question.setQuestionType(questionType);

        // 收集所有错误信息
        List<String> errors = new ArrayList<>();

        // 验证必填字段 - 分类名称
        String categoryName = getCellStringValue(row, columnMap.get("*分类名称"));
        if (StringUtils.isEmpty(categoryName)) {
            // 兼容旧模板格式
            categoryName = getCellStringValue(row, columnMap.get("分类名称"));
        }
        if (StringUtils.isEmpty(categoryName)) {
            errors.add("分类名称不能为空");
        } else {
            // 查找分类ID
            Long categoryId = findCategoryIdByName(categoryName);
            if (categoryId == null) {
                errors.add("分类名称'" + categoryName + "'不存在，请先创建该分类");
            } else {
                question.setCategoryId(categoryId);
            }
        }

        // 验证必填字段 - 题目标题
        String questionTitle = getCellStringValue(row, columnMap.get("*题目标题"));
        if (StringUtils.isEmpty(questionTitle)) {
            // 兼容旧模板格式
            questionTitle = getCellStringValue(row, columnMap.get("题目标题"));
        }
        if (StringUtils.isEmpty(questionTitle)) {
            errors.add("题目标题不能为空");
        } else {
            question.setQuestionTitle(questionTitle);
        }

        // 验证必填字段 - 题目内容
        String questionContent = getCellStringValue(row, columnMap.get("*题目内容"));
        if (StringUtils.isEmpty(questionContent)) {
            // 兼容旧模板格式
            questionContent = getCellStringValue(row, columnMap.get("题目内容"));
        }
        if (StringUtils.isEmpty(questionContent)) {
            errors.add("题目内容不能为空");
        } else {
            question.setQuestionContent(questionContent);
        }

        // 非必填字段 - 题目解析
        question.setQuestionAnalysis(getCellStringValue(row, columnMap.get("题目解析")));

        // 验证必填字段 - 难度等级
        String difficultyStr = getCellStringValue(row, columnMap.get("*难度等级"));
        if (StringUtils.isEmpty(difficultyStr)) {
            // 兼容旧模板格式
            difficultyStr = getCellStringValue(row, columnMap.get("难度等级"));
        }
        if (StringUtils.isEmpty(difficultyStr)) {
            errors.add("难度等级不能为空");
        } else {
            try {
                int difficulty = Integer.parseInt(difficultyStr);
                if (difficulty < 1 || difficulty > 3) {
                    errors.add("难度等级必须为1(简单)、2(中等)或3(困难)");
                } else {
                    question.setDifficultyLevel(difficulty);
                }
            } catch (NumberFormatException e) {
                errors.add("难度等级格式错误，必须为数字1、2或3");
            }
        }

        // 验证必填字段 - 分值
        String scoreStr = getCellStringValue(row, columnMap.get("*分值"));
        if (StringUtils.isEmpty(scoreStr)) {
            // 兼容旧模板格式
            scoreStr = getCellStringValue(row, columnMap.get("分值"));
        }
        if (StringUtils.isEmpty(scoreStr)) {
            errors.add("分值不能为空");
        } else {
            try {
                BigDecimal score = new BigDecimal(scoreStr);
                if (score.compareTo(BigDecimal.ZERO) <= 0) {
                    errors.add("分值必须大于0");
                } else {
                    question.setScore(score);
                }
            } catch (NumberFormatException e) {
                errors.add("分值格式错误，必须为有效数字");
            }
        }

        // 非必填字段 - 标签
        question.setTags(getCellStringValue(row, columnMap.get("标签")));
        question.setStatus("0"); // 默认启用

        // 根据题型验证必填字段和设置选项
        try {
            if ("single".equals(questionType) || "multiple".equals(questionType)) {
                setChoiceQuestionOptions(question, row, columnMap, questionType);
            } else if ("judge".equals(questionType)) {
                setJudgeQuestionOptions(question, row, columnMap);
            } else {
                // 填空题和简答题 - 验证必填字段正确答案
                String correctAnswer = getCellStringValue(row, columnMap.get("*正确答案"));
                if (StringUtils.isEmpty(correctAnswer)) {
                    correctAnswer = getCellStringValue(row, columnMap.get("*参考答案"));
                }
                if (StringUtils.isEmpty(correctAnswer)) {
                    // 兼容旧模板格式
                    correctAnswer = getCellStringValue(row, columnMap.get("正确答案"));
                    if (StringUtils.isEmpty(correctAnswer)) {
                        correctAnswer = getCellStringValue(row, columnMap.get("参考答案"));
                    }
                }
                if (StringUtils.isEmpty(correctAnswer)) {
                    if ("fill".equals(questionType)) {
                        errors.add("正确答案不能为空");
                    } else if ("essay".equals(questionType)) {
                        errors.add("参考答案不能为空");
                    }
                } else {
                    question.setCorrectAnswer(correctAnswer);
                }
            }
        } catch (Exception e) {
            errors.add(e.getMessage());
        }

        // 如果有错误，将所有错误信息用分号连接后抛出
        if (!errors.isEmpty()) {
            throw new Exception(String.join("；", errors));
        }

        return question;
    }

    /**
     * 设置选择题选项
     */
    private void setChoiceQuestionOptions(ExamQuestion question, Row row, Map<String, Integer> columnMap,
            String questionType) throws Exception {
        List<QuestionOption> options = new ArrayList<>();
        String[] optionKeys = { "A", "B", "C", "D", "E", "F" };

        // 收集选项错误信息
        List<String> errors = new ArrayList<>();

        // 收集所有选项（不强制要求A、B必填，但需要业务逻辑验证）
        for (String key : optionKeys) {
            String optionText = getCellStringValue(row, columnMap.get("选项" + key));
            if (StringUtils.isNotEmpty(optionText)) {
                options.add(new QuestionOption(key, optionText, false));
            }
        }

        // 业务逻辑验证：选择题至少需要2个选项
        if (options.size() < 2) {
            errors.add("选择题至少需要2个选项");
        }

        // 验证必填字段 - 正确答案
        String correctAnswer = getCellStringValue(row, columnMap.get("*正确答案"));
        if (StringUtils.isEmpty(correctAnswer)) {
            // 兼容旧模板格式
            correctAnswer = getCellStringValue(row, columnMap.get("正确答案"));
        }
        if (StringUtils.isEmpty(correctAnswer)) {
            errors.add("正确答案不能为空");
        } else {
            // 验证正确答案的有效性
            if ("single".equals(questionType)) {
                // 单选题 - 答案必须是有效的选项键
                boolean isValidAnswer = false;
                for (QuestionOption option : options) {
                    if (correctAnswer.equals(option.getKey())) {
                        option.setIsCorrect(true);
                        isValidAnswer = true;
                        break;
                    }
                }
                if (!isValidAnswer) {
                    errors.add("正确答案'" + correctAnswer + "'不是有效的选项");
                }
            } else if ("multiple".equals(questionType)) {
                // 多选题 - 验证每个答案都是有效的选项键
                String[] correctAnswers = correctAnswer.split(",");
                if (correctAnswers.length < 2) {
                    errors.add("多选题必须至少有2个正确答案");
                }

                for (String answer : correctAnswers) {
                    String trimAnswer = answer.trim();
                    boolean isValidAnswer = false;
                    for (QuestionOption option : options) {
                        if (trimAnswer.equals(option.getKey())) {
                            option.setIsCorrect(true);
                            isValidAnswer = true;
                            break;
                        }
                    }
                    if (!isValidAnswer) {
                        errors.add("正确答案'" + trimAnswer + "'不是有效的选项");
                    }
                }
            }
        }

        // 如果有错误，抛出包含所有错误信息的异常
        if (!errors.isEmpty()) {
            throw new Exception(String.join("；", errors));
        }

        question.setOptionList(options);
        question.setCorrectAnswer(correctAnswer);
    }

    /**
     * 设置判断题选项
     */
    private void setJudgeQuestionOptions(ExamQuestion question, Row row, Map<String, Integer> columnMap)
            throws Exception {
        List<QuestionOption> options = new ArrayList<>();

        // 收集判断题错误信息
        List<String> errors = new ArrayList<>();

        // 验证必填字段 - 正确答案
        String correctAnswer = getCellStringValue(row, columnMap.get("*正确答案"));
        if (StringUtils.isEmpty(correctAnswer)) {
            // 兼容旧模板格式
            correctAnswer = getCellStringValue(row, columnMap.get("正确答案"));
        }
        if (StringUtils.isEmpty(correctAnswer)) {
            errors.add("正确答案不能为空");
        } else {
            // 验证判断题答案的有效性
            String standardAnswer = null;
            if ("对".equals(correctAnswer) || "true".equalsIgnoreCase(correctAnswer)
                    || "T".equalsIgnoreCase(correctAnswer) || "正确".equals(correctAnswer)) {
                standardAnswer = "true";
            } else if ("错".equals(correctAnswer) || "false".equalsIgnoreCase(correctAnswer)
                    || "F".equalsIgnoreCase(correctAnswer) || "错误".equals(correctAnswer)) {
                standardAnswer = "false";
            } else {
                errors.add("判断题答案必须为：对/错、true/false、T/F、正确/错误");
            }

            // 如果答案格式正确，创建标准判断题选项
            if (standardAnswer != null) {
                options.add(new QuestionOption("true", "正确", "true".equals(standardAnswer)));
                options.add(new QuestionOption("false", "错误", "false".equals(standardAnswer)));
                question.setCorrectAnswer(standardAnswer);
            }
        }

        // 如果有错误，抛出包含所有错误信息的异常
        if (!errors.isEmpty()) {
            throw new Exception(String.join("；", errors));
        }

        question.setOptionList(options);
    }

    /**
     * 获取单元格字符串值
     */
    private String getCellStringValue(Row row, Integer columnIndex) {
        if (columnIndex == null || row == null) {
            return "";
        }

        Cell cell = row.getCell(columnIndex);
        if (cell == null) {
            return "";
        }

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                return String.valueOf((int) cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            default:
                return "";
        }
    }

    /**
     * 检查行是否为空
     */
    private boolean isRowEmpty(Row row) {
        for (int i = 0; i < row.getLastCellNum(); i++) {
            Cell cell = row.getCell(i);
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                String value = getCellStringValue(row, i);
                if (StringUtils.isNotEmpty(value)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 根据分类名称查找分类ID
     */
    private Long findCategoryIdByName(String categoryName) {
        ExamQuestionCategory category = categoryMapper.selectCategoryByName(categoryName);
        return category != null ? category.getId() : null;
    }

    /**
     * 使用传统模板格式导入（单sheet）
     */
    private String importQuestionFromOldTemplate(MultipartFile file, Boolean updateSupport, String operName)
            throws Exception {
        ExcelUtil<ExamQuestion> util = new ExcelUtil<ExamQuestion>(ExamQuestion.class);
        List<ExamQuestion> questionList = util.importExcel(file.getInputStream());
        return importQuestion(questionList, updateSupport, operName);
    }

    /**
     * 导入试题数据
     * 
     * @param questionList  试题数据列表
     * @param updateSupport 是否更新支持，如果已存在，则进行更新数据
     * @param operName      操作用户
     * @return 结果
     */
    public String importQuestion(List<ExamQuestion> questionList, Boolean updateSupport, String operName) {
        if (StringUtils.isNull(questionList) || questionList.size() == 0) {
            throw new ServiceException("导入试题数据不能为空！");
        }
        int successNum = 0;
        int failureNum = 0;
        StringBuilder successMsg = new StringBuilder();
        StringBuilder failureMsg = new StringBuilder();
        for (ExamQuestion question : questionList) {
            try {
                // 验证是否存在这个试题
                ExamQuestion q = questionMapper.checkQuestionTitleUnique(question.getQuestionTitle(),
                        question.getCategoryId());
                if (StringUtils.isNull(q)) {
                    question.setCreateBy(operName);
                    this.insertQuestion(question);
                    successNum++;
                    successMsg.append("<br/>" + successNum + "、试题 " + question.getQuestionTitle() + " 导入成功");
                } else if (updateSupport) {
                    question.setId(q.getId());
                    question.setUpdateBy(operName);
                    this.updateQuestion(question);
                    successNum++;
                    successMsg.append("<br/>" + successNum + "、试题 " + question.getQuestionTitle() + " 更新成功");
                } else {
                    failureNum++;
                    failureMsg.append("<br/>" + failureNum + "、试题 " + question.getQuestionTitle() + " 已存在");
                }
            } catch (Exception e) {
                failureNum++;
                String msg = "<br/>" + failureNum + "、试题 " + question.getQuestionTitle() + " 导入失败：";
                failureMsg.append(msg + e.getMessage());
            }
        }
        if (failureNum > 0) {
            failureMsg.insert(0, "很抱歉，导入失败！共 " + failureNum + " 条数据格式不正确，错误如下：");
            throw new ServiceException(failureMsg.toString());
        } else {
            successMsg.insert(0, "恭喜您，数据已全部导入成功！共 " + successNum + " 条，数据如下：");
        }
        return successMsg.toString();
    }

    /**
     * 导出试题数据
     * 
     * @param question 试题信息
     * @return 试题列表
     */
    @Override
    public List<ExamQuestion> exportQuestion(ExamQuestion question) {
        return questionMapper.selectQuestionList(question);
    }

    private void fillQuestionMedia(ExamQuestion question) {
        if (question == null || question.getId() == null) {
            return;
        }
        question.setMediaList(questionMediaService.selectMediaByQuestionId(question.getId()));
    }

    private void fillQuestionMedia(List<ExamQuestion> questions) {
        if (StringUtils.isEmpty(questions)) {
            return;
        }

        List<Long> questionIds = new ArrayList<>();
        for (ExamQuestion question : questions) {
            if (question != null && question.getId() != null) {
                questionIds.add(question.getId());
            }
        }
        if (questionIds.isEmpty()) {
            return;
        }

        Map<Long, List<ExamQuestionMedia>> mediaMap = questionMediaService.selectMediaMapByQuestionIds(questionIds);
        for (ExamQuestion question : questions) {
            if (question != null && question.getId() != null) {
                question.setMediaList(mediaMap.get(question.getId()));
            }
        }
    }

    /**
     * 统计试题总数
     * 
     * @return 试题总数
     */
    @Override
    public int countTotalQuestions() {
        return questionMapper.countTotalQuestions();
    }

    /**
     * 统计各题型试题数量
     * 
     * @return 统计结果
     */
    @Override
    public java.util.Map<String, Integer> selectQuestionTypeStatistics() {
        List<java.util.Map<String, Object>> stats = questionMapper.selectQuestionTypeStatistics();
        java.util.Map<String, Integer> resultMap = new java.util.HashMap<>();

        // 初始化所有题型为0，确保即便某种题型没有题也会在图表中显示(或者至少有key)
        resultMap.put("single", 0);
        resultMap.put("multiple", 0);
        resultMap.put("judge", 0);
        resultMap.put("fill", 0);
        resultMap.put("essay", 0);

        if (stats != null) {
            for (java.util.Map<String, Object> map : stats) {
                String type = (String) map.get("questionType");
                Number count = (Number) map.get("count");
                if (type != null && count != null) {
                    resultMap.put(type, count.intValue());
                }
            }
        }
        return resultMap;
    }

    /**
     * 下载按题型分sheet的导入模板
     * 
     * @param response HTTP响应
     */
    @Override
    public void downloadImportTemplate(HttpServletResponse response) {
        try {
            // 创建工作簿
            Workbook workbook = new XSSFWorkbook();

            // 创建单选题模板sheet
            createSingleChoiceSheet(workbook);

            // 创建多选题模板sheet
            createMultipleChoiceSheet(workbook);

            // 创建判断题模板sheet
            createJudgeSheet(workbook);

            // 创建填空题模板sheet
            createFillSheet(workbook);

            // 创建简答题模板sheet
            createEssaySheet(workbook);

            // 创建使用说明sheet
            createInstructionSheet(workbook);

            // 设置响应头
            response.setHeader("Content-Disposition", "attachment; filename=" +
                    URLEncoder.encode("题目导入模板.xlsx", "UTF-8"));
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

            // 输出到响应流
            workbook.write(response.getOutputStream());
            workbook.close();
        } catch (Exception e) {
            throw new ServiceException("导入模板下载失败：" + e.getMessage());
        }
    }

    /**
     * 创建单选题模板sheet
     */
    private void createSingleChoiceSheet(Workbook workbook) {
        Sheet sheet = workbook.createSheet("单选题模板");

        // 创建标题行
        Row headerRow = sheet.createRow(0);
        String[] headers = { "*分类名称", "*题目标题", "*题目内容", "选项A", "选项B", "选项C", "选项D", "*正确答案", "题目解析", "*难度等级", "*分值",
                "标签" };
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
        }

        // 创建示例数据行
        Row dataRow = sheet.createRow(1);
        dataRow.createCell(0).setCellValue("施工安全");
        dataRow.createCell(1).setCellValue("安全帽的作用是什么？");
        dataRow.createCell(2).setCellValue("安全帽主要用于保护哪个部位？");
        dataRow.createCell(3).setCellValue("保护头部");
        dataRow.createCell(4).setCellValue("保护眼部");
        dataRow.createCell(5).setCellValue("保护手部");
        dataRow.createCell(6).setCellValue("保护脚部");
        dataRow.createCell(7).setCellValue("A");
        dataRow.createCell(8).setCellValue("安全帽是用来保护头部的个人防护用品");
        dataRow.createCell(9).setCellValue(1);
        dataRow.createCell(10).setCellValue(2);
        dataRow.createCell(11).setCellValue("安全防护,个人防护");

        // 设置列宽
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    /**
     * 创建多选题模板sheet
     */
    private void createMultipleChoiceSheet(Workbook workbook) {
        Sheet sheet = workbook.createSheet("多选题模板");

        // 创建标题行
        Row headerRow = sheet.createRow(0);
        String[] headers = { "*分类名称", "*题目标题", "*题目内容", "选项A", "选项B", "选项C", "选项D", "选项E", "选项F", "*正确答案", "题目解析",
                "*难度等级", "*分值", "标签" };
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
        }

        // 创建示例数据行
        Row dataRow = sheet.createRow(1);
        dataRow.createCell(0).setCellValue("施工安全");
        dataRow.createCell(1).setCellValue("高处作业安全防护包括哪些？");
        dataRow.createCell(2).setCellValue("进行高处作业时，需要采取哪些安全防护措施？");
        dataRow.createCell(3).setCellValue("佩戴安全带");
        dataRow.createCell(4).setCellValue("设置安全网");
        dataRow.createCell(5).setCellValue("搭设脚手架");
        dataRow.createCell(6).setCellValue("随意攀爬");
        dataRow.createCell(7).setCellValue("使用安全梯");
        dataRow.createCell(8).setCellValue("");
        dataRow.createCell(9).setCellValue("A,B,C,E");
        dataRow.createCell(10).setCellValue("高处作业需要多重安全防护措施，随意攀爬是不安全的");
        dataRow.createCell(11).setCellValue(2);
        dataRow.createCell(12).setCellValue(3);
        dataRow.createCell(13).setCellValue("高处作业,安全防护");

        // 设置列宽
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    /**
     * 创建判断题模板sheet
     */
    private void createJudgeSheet(Workbook workbook) {
        Sheet sheet = workbook.createSheet("判断题模板");

        // 创建标题行
        Row headerRow = sheet.createRow(0);
        String[] headers = { "*分类名称", "*题目标题", "*题目内容", "*正确答案", "题目解析", "*难度等级", "*分值", "标签" };
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
        }

        // 创建示例数据行
        Row dataRow = sheet.createRow(1);
        dataRow.createCell(0).setCellValue("施工安全");
        dataRow.createCell(1).setCellValue("安全第一，预防为主");
        dataRow.createCell(2).setCellValue("安全生产的方针是\"安全第一，预防为主，综合治理\"");
        dataRow.createCell(3).setCellValue("对");
        dataRow.createCell(4).setCellValue("这是我国安全生产的基本方针");
        dataRow.createCell(5).setCellValue(1);
        dataRow.createCell(6).setCellValue(1);
        dataRow.createCell(7).setCellValue("安全方针");

        // 设置列宽
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    /**
     * 创建填空题模板sheet
     */
    private void createFillSheet(Workbook workbook) {
        Sheet sheet = workbook.createSheet("填空题模板");

        // 创建标题行
        Row headerRow = sheet.createRow(0);
        String[] headers = { "*分类名称", "*题目标题", "*题目内容", "*正确答案", "题目解析", "*难度等级", "*分值", "标签" };
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
        }

        // 创建示例数据行
        Row dataRow = sheet.createRow(1);
        dataRow.createCell(0).setCellValue("施工安全");
        dataRow.createCell(1).setCellValue("安全生产三原则");
        dataRow.createCell(2).setCellValue("安全生产的三个原则是：___、___、___");
        dataRow.createCell(3).setCellValue("安全第一|预防为主|综合治理");
        dataRow.createCell(4).setCellValue("安全生产的三个基本原则");
        dataRow.createCell(5).setCellValue(2);
        dataRow.createCell(6).setCellValue(3);
        dataRow.createCell(7).setCellValue("安全原则");

        // 设置列宽
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    /**
     * 创建简答题模板sheet
     */
    private void createEssaySheet(Workbook workbook) {
        Sheet sheet = workbook.createSheet("简答题模板");

        // 创建标题行
        Row headerRow = sheet.createRow(0);
        String[] headers = { "*分类名称", "*题目标题", "*题目内容", "*参考答案", "题目解析", "*难度等级", "*分值", "标签" };
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
        }

        // 创建示例数据行
        Row dataRow = sheet.createRow(1);
        dataRow.createCell(0).setCellValue("施工安全");
        dataRow.createCell(1).setCellValue("安全事故应急处理");
        dataRow.createCell(2).setCellValue("请简述发生安全事故时的应急处理步骤");
        dataRow.createCell(3).setCellValue("1.立即停止作业；2.保护事故现场；3.抢救伤员；4.及时报告；5.配合调查");
        dataRow.createCell(4).setCellValue("安全事故应急处理的标准流程");
        dataRow.createCell(5).setCellValue(3);
        dataRow.createCell(6).setCellValue(5);
        dataRow.createCell(7).setCellValue("应急处理,安全事故");

        // 设置列宽
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    /**
     * 创建使用说明sheet
     */
    private void createInstructionSheet(Workbook workbook) {
        Sheet sheet = workbook.createSheet("使用说明");

        // 创建标题行样式
        CellStyle titleStyle = workbook.createCellStyle();
        Font titleFont = workbook.createFont();
        titleFont.setBold(true);
        titleFont.setFontHeightInPoints((short) 14);
        titleStyle.setFont(titleFont);

        // 创建内容行样式
        CellStyle contentStyle = workbook.createCellStyle();
        contentStyle.setWrapText(true);

        int rowNum = 0;

        // 添加标题
        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("题目导入模板使用说明");
        titleCell.setCellStyle(titleStyle);

        rowNum++; // 空行

        // 添加说明内容
        String[] instructions = {
                "1. 模板说明：",
                "   - 本模板按题型分为5个sheet页，请根据需要填写对应的sheet",
                "   - 单选题：选择题只有一个正确答案，正确答案填写A/B/C/D",
                "   - 多选题：选择题有多个正确答案，正确答案用逗号分隔，如：A,B,C",
                "   - 判断题：正确答案填写'对'或'错'",
                "   - 填空题：多个填空答案用|分隔，如：答案1|答案2|答案3",
                "   - 简答题：填写参考答案即可",
                "",
                "2. 填写规范：",
                "   - 分类名称：必须是系统中已存在的分类名称",
                "   - 题目标题：必填，不能重复",
                "   - 题目内容：必填，支持富文本",
                "   - 难度等级：1=简单，2=中等，3=困难",
                "   - 分值：数字格式，默认5分",
                "   - 标签：多个标签用逗号分隔",
                "",
                "3. 注意事项：",
                "   - 导入前请确保分类已存在",
                "   - 每个sheet最多支持1000条数据",
                "   - 建议先导入少量数据测试",
                "   - 如有错误，系统会提示具体错误信息"
        };

        for (String instruction : instructions) {
            Row row = sheet.createRow(rowNum++);
            Cell cell = row.createCell(0);
            cell.setCellValue(instruction);
            cell.setCellStyle(contentStyle);
        }

        // 设置列宽
        sheet.setColumnWidth(0, 15000);
    }
}
