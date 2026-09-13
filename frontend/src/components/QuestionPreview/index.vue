<template>
  <div class="question-preview">
    <div class="question-header">
      <h3 class="question-title">{{ question.questionTitle }}</h3>
      <div class="question-meta">
        <el-tag v-if="question.difficultyLevel === 1" type="success" size="mini">简单</el-tag>
        <el-tag v-else-if="question.difficultyLevel === 2" type="warning" size="mini">中等</el-tag>
        <el-tag v-else-if="question.difficultyLevel === 3" type="danger" size="mini">困难</el-tag>
        <span class="score">{{ question.score }}分</span>
        <span class="type">{{ getQuestionTypeText(question.questionType) }}</span>
      </div>
    </div>
    
    <div class="question-content" v-html="displayContent"></div>
    <question-media :media-list="question.mediaList" />
    
    <div v-if="options && options.length > 0" class="question-options">
      <div v-for="option in options" :key="option.key" class="option-item" :class="{'correct-option': showAnswer && option.isCorrect}">
        <span class="option-key">{{ getOptionKey(option.key) }}.</span>
        <span class="option-value">{{ option.value }}</span>
        <el-tag v-if="showAnswer && option.isCorrect" type="success" size="mini" class="correct-tag">✓</el-tag>
      </div>
    </div>
    
    <div v-if="showAnswer" class="question-answer">
      <div class="answer-section">
        <strong>正确答案：</strong>
        <span class="answer-text">{{ getFormattedAnswer() }}</span>
      </div>
      
      <div v-if="question.questionAnalysis" class="analysis-section">
        <strong>题目解析：</strong>
        <div class="analysis-text" v-html="sanitizeContent(question.questionAnalysis)"></div>
      </div>
    </div>
    
    <div v-if="question.tags" class="question-tags">
      <strong>标签：</strong>
      <el-tag
        v-for="tag in getTags(question.tags)"
        :key="tag"
        size="mini"
        style="margin-right: 8px;"
      >{{ tag }}</el-tag>
    </div>
  </div>
</template>

<script>
import QuestionMedia from "@/components/QuestionMedia"
import { sanitizeRichText } from "@/utils/sanitize"

export default {
  name: "QuestionPreview",
  components: {
    QuestionMedia
  },
  props: {
    question: {
      type: Object,
      required: true,
      default: () => ({})
    },
    showAnswer: {
      type: Boolean,
      default: true
    }
  },
  computed: {
    options() {
      // 优先使用后端处理过的optionList，确保包含isCorrect信息
      if (this.question.optionList && this.question.optionList.length > 0) {
        return this.question.optionList;
      }
      
      // 兼容处理：如果没有optionList，尝试解析questionOptions
      if (!this.question.questionOptions) {
        return [];
      }
      try {
        const parsedOptions = JSON.parse(this.question.questionOptions);
        // 确保选项有isCorrect字段
        return parsedOptions.map(option => ({
          key: option.key,
          value: option.value,
          isCorrect: option.isCorrect || false
        }));
      } catch (e) {
        console.error('解析选项失败:', e);
        return [];
      }
    },
    displayContent() {
      if (!this.question.questionContent) {
        return '';
      }
      
      // 将[空]或{{空}}替换为___
      return sanitizeRichText(this.question.questionContent.replace(/\[空\]|\{\{空\}\}/g, '___'));
    }
  },
  methods: {
    sanitizeContent(content) {
      return sanitizeRichText(content);
    },
    getQuestionTypeText(type) {
      const typeMap = {
        'single': '单选题',
        'multiple': '多选题',
        'judge': '判断题',
        'fill': '填空题',
        'essay': '简答题'
      };
      return typeMap[type] || '';
    },
    getTags(tagsStr) {
      if (!tagsStr) return [];
      return tagsStr.split(',').map(tag => tag.trim()).filter(tag => tag);
    },
    getOptionKey(key) {
      // 判断题的选项key转换
      if (this.question.questionType === 'judge') {
        return key === 'true' ? 'A' : 'B';
      }
      return key;
    },
    getFormattedAnswer() {
      if (!this.question.correctAnswer) {
        return '';
      }
      
      const questionType = this.question.questionType;
      const correctAnswer = this.question.correctAnswer;
      
      // 根据题型格式化答案显示
      switch (questionType) {
        case 'single':
          // 单选题显示选项key和内容
          const singleOption = this.options.find(opt => opt.key === correctAnswer);
          return singleOption ? `${correctAnswer}. ${singleOption.value}` : correctAnswer;
          
        case 'multiple':
          // 多选题显示所有正确选项，用逗号分隔
          if (correctAnswer.includes(',')) {
            const correctKeys = correctAnswer.split(',').map(key => key.trim());
            const correctOptions = correctKeys.map(key => {
              const option = this.options.find(opt => opt.key === key);
              return option ? `${key}. ${option.value}` : key;
            });
            return correctOptions.join(', ');
          } else {
            const multipleOption = this.options.find(opt => opt.key === correctAnswer);
            return multipleOption ? `${correctAnswer}. ${multipleOption.value}` : correctAnswer;
          }
          
        case 'judge':
          // 判断题显示"正确"或"错误"
          if (correctAnswer === 'true') {
            return 'A. 正确';
          } else if (correctAnswer === 'false') {
            return 'B. 错误';  
          }
          return correctAnswer;
          
        case 'fill':
          // 填空题需要解析JSON格式的答案
          try {
            const answersArray = JSON.parse(correctAnswer);
            if (Array.isArray(answersArray)) {
              // 将每个空的答案数组转换为字符串，多个答案用分号分隔
              const formattedAnswers = answersArray.map(answers => {
                if (Array.isArray(answers)) {
                  return answers.join(';');
                }
                return answers || '';
              });
              return formattedAnswers.join(' | ');
            }
            return correctAnswer;
          } catch (e) {
            // 如果不是JSON格式，直接返回原答案
            return correctAnswer;
          }
          
        case 'essay':
          // 简答题直接显示文本答案
          return correctAnswer;
          
        default:
          return correctAnswer;
      }
    }
  }
};
</script>

<style scoped>
.question-preview {
  padding: 20px;
  border: 1px solid #e4e7ed;
  border-radius: 6px;
  background-color: #fff;
}

.question-header {
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #ebeef5;
}

.question-title {
  margin: 0 0 10px 0;
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  line-height: 1.4;
}

.question-meta {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 14px;
}

.score {
  color: #e6a23c;
  font-weight: 500;
}

.type {
  color: #606266;
}

.question-content {
  margin-bottom: 20px;
  font-size: 16px;
  line-height: 1.6;
  color: #303133;
}

.question-content >>> p {
  margin: 10px 0;
}

.question-content >>> img {
  max-width: 100%;
  height: auto;
}

.question-options {
  margin-bottom: 20px;
}

.option-item {
  margin-bottom: 10px;
  font-size: 15px;
  line-height: 1.5;
  display: flex;
  align-items: center;
  padding: 8px;
  border-radius: 4px;
  transition: background-color 0.2s;
}

.option-item.correct-option {
  background-color: #f0f9ff;
  border: 1px solid #67c23a;
}

.option-key {
  display: inline-block;
  width: 30px;
  font-weight: 600;
  color: #409eff;
  flex-shrink: 0;
}

.option-value {
  color: #303133;
  flex: 1;
}

.correct-tag {
  margin-left: 10px;
  flex-shrink: 0;
}

.question-answer {
  margin-bottom: 20px;
  padding: 15px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.answer-section {
  margin-bottom: 15px;
}

.answer-text {
  color: #67c23a;
  font-weight: 500;
  margin-left: 10px;
}

.analysis-section strong {
  color: #606266;
}

.analysis-text {
  margin-top: 8px;
  color: #606266;
  line-height: 1.6;
}

.analysis-text >>> p {
  margin: 8px 0;
}

.question-tags {
  padding-top: 15px;
  border-top: 1px solid #ebeef5;
  font-size: 14px;
}

.question-tags strong {
  color: #606266;
  margin-right: 10px;
}
</style>
