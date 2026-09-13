<template>
  <div class="paper-preview" :class="{ fullscreen: isFullscreen }">
    <!-- 试卷信息 -->
    <div class="paper-header">
      <h2 class="paper-title">{{ paper.paperName }}</h2>
      <div class="paper-info">
        <el-row :gutter="20">
          <el-col :span="6">
            <div class="info-item">
              <label>试卷分类：</label>
              <span>{{ paper.categoryName }}</span>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="info-item">
              <label>试卷类型：</label>
              <el-tag :type="getTypeColor(paper.paperType)">{{ getTypeName(paper.paperType) }}</el-tag>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="info-item">
              <label>总分值：</label>
              <span class="score">{{ paper.totalScore }}分</span>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="info-item">
              <label>考试时长：</label>
              <span>{{ paper.duration }}分钟</span>
            </div>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="6">
            <div class="info-item">
              <label>题目数量：</label>
              <span>{{ paper.questionCount }}题</span>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="info-item">
              <label>及格分数：</label>
              <span>{{ paper.passScore }}分</span>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="info-item">
              <label>难度等级：</label>
              <el-tag :type="getDifficultyColor(paper.difficultyLevel)">{{ getDifficultyName(paper.difficultyLevel) }}</el-tag>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="info-item">
              <label>打乱题目：</label>
              <span>{{ paper.shuffleQuestions === '1' ? '是' : '否' }}</span>
            </div>
          </el-col>
        </el-row>
        <div class="paper-description" v-if="paper.paperDescription">
          <label>试卷描述：</label>
          <p>{{ paper.paperDescription }}</p>
        </div>
      </div>
    </div>

    <el-divider></el-divider>

    <!-- 题目预览 -->
    <div class="questions-preview" v-loading="questionsLoading">
      <div class="section-title">
        <h3>题目预览</h3>
        <!-- <el-button 
          v-if="paper.paperType === 'mixed'" 
          type="primary" 
          size="mini" 
          @click="generateQuestions"
        >
          重新生成题目
        </el-button> -->
      </div>

      <div v-if="questions.length === 0" class="no-questions">
        <el-empty description="暂无题目" />
      </div>

      <div v-else class="questions-list">
        <div 
          v-for="(question, index) in questions" 
          :key="question.id" 
          class="question-item"
        >
          <div class="question-header">
            <span class="question-number">{{ index + 1 }}.</span>
            <span class="question-title">{{ question.questionTitle || question.questionContent }}</span>
            <span class="question-type">{{ getQuestionTypeName(question.questionType) }}</span>
            <span class="question-score">（{{ question.score }}分）</span>
          </div>
          
          <div class="question-content">
            <div v-if="question.questionContent && question.questionTitle" v-html="formatQuestionContent(question.questionContent)"></div>
            <div v-else-if="question.questionContent" v-html="formatQuestionContent(question.questionContent)"></div>
            <div v-else-if="question.questionTitle" v-html="formatQuestionContent(question.questionTitle)"></div>
          </div>
          <question-media :media-list="question.mediaList" />

          <!-- 选择题选项 -->
          <div v-if="isChoiceQuestion(question.questionType)" class="question-options">
            <div 
              v-for="(option, optIndex) in parseOptions(question.questionOptions)" 
              :key="optIndex"
              class="option-item"
            >
              <!-- 判断题不显示选项前缀，其他题型显示 -->
              <template v-if="question.questionType === 'judge' || question.questionType === 'true_false'">
                <span class="option-value">{{ option.value }}</span>
              </template>
              <template v-else>
                <span class="option-key">{{ getOptionLabel(option, optIndex) }}.</span>
                <span class="option-value">{{ option.value }}</span>
              </template>
            </div>
          </div>

          <!-- 答案解析 -->
          <div class="question-analysis" v-if="showAnswers">
            <div class="correct-answer">
              <strong>正确答案：</strong>
              <span class="answer">{{ formatAnswer(question.correctAnswer, question.questionType) }}</span>
            </div>
            <div v-if="question.questionAnalysis" class="analysis">
              <strong>解析：</strong>
              <p v-html="sanitizeContent(question.questionAnalysis)"></p>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 操作按钮 -->
    <div class="preview-actions">
      <el-button @click="showAnswers = !showAnswers">
        {{ showAnswers ? '隐藏答案' : '显示答案' }}
      </el-button>
      <el-button type="primary" @click="printPaper">打印试卷</el-button>
    </div>
  </div>
</template>

<script>
import { getPaper, generatePaperQuestions, getPaperQuestions } from "@/api/exam/paper";
import QuestionMedia from "@/components/QuestionMedia";
import { sanitizeRichText } from "@/utils/sanitize";

export default {
  name: "PaperPreview",
  components: {
    QuestionMedia
  },
  props: {
    paperId: {
      type: [Number, String],
      required: true
    },
    isFullscreen: {
      type: Boolean,
      default: false
    },
    initialShowAnswers: {
      type: Boolean,
      default: true
    }
  },
  data() {
    return {
      paper: {},
      questions: [],
      questionsLoading: false,
      showAnswers: this.initialShowAnswers
    };
  },
  created() {
    this.loadPaper();
  },
  methods: {
    /** 加载试卷信息 */
    async loadPaper() {
      try {
        const response = await getPaper(this.paperId);
        this.paper = response.data;
        await this.loadQuestions();
      } catch (error) {
        this.$message.error("加载试卷信息失败");
      }
    },
    /** 加载题目 */
    async loadQuestions() {
      this.questionsLoading = true;
      try {
        // 所有类型的试卷都直接获取关联的题目（混合试卷在创建时已生成并保存了所有题目）
        const response = await getPaperQuestions(this.paperId);
        this.questions = response.data.map(item => ({
          ...item.question,
          score: item.score
        }));
      } catch (error) {
        this.$message.error("加载题目失败");
        console.error('加载题目错误:', error);
      } finally {
        this.questionsLoading = false;
      }
    },
    /** 重新生成题目（用于随机试卷） */
    generateQuestions() {
      this.loadQuestions();
    },
    /** 获取试卷类型名称 */
    getTypeName(type) {
      const typeMap = {
        'fixed': '固定试卷',
        'random': '随机试卷',
        'mixed': '混合试卷'
      };
      return typeMap[type] || type;
    },
    /** 获取试卷类型颜色 */
    getTypeColor(type) {
      const colorMap = {
        'fixed': '',
        'random': 'success',
        'mixed': 'warning'
      };
      return colorMap[type] || '';
    },
    /** 获取难度等级名称 */
    getDifficultyName(level) {
      const levelMap = {
        1: '简单',
        2: '中等',
        3: '困难'
      };
      return levelMap[level] || level;
    },
    /** 获取难度等级颜色 */
    getDifficultyColor(level) {
      const colorMap = {
        1: 'success',
        2: 'warning',
        3: 'danger'
      };
      return colorMap[level] || '';
    },
    /** 获取题型名称 */
    getQuestionTypeName(type) {
      const typeMap = {
        'single': '单选题',
        'multiple': '多选题',
        'judge': '判断题',
        'fill': '填空题',
        'essay': '简答题'
      };
      return typeMap[type] || type;
    },
    /** 判断是否为选择题 */
    isChoiceQuestion(type) {
      return ['single', 'multiple', 'judge'].includes(type);
    },
    /** 解析选项 */
    parseOptions(options) {
      if (!options) return [];
      try {
        const parsed = JSON.parse(options);
        // 如果解析结果是对象数组（包含key和value），返回完整对象
        if (Array.isArray(parsed) && parsed.length > 0 && typeof parsed[0] === 'object' && parsed[0].hasOwnProperty('value')) {
          return parsed;
        }
        // 如果解析结果是简单数组，转换为对象格式
        if (Array.isArray(parsed)) {
          return parsed.map((value, index) => ({
            key: String.fromCharCode(65 + index),
            value: value
          }));
        }
        return [];
      } catch {
        // 如果JSON解析失败，尝试按换行符分割
        const options = options.split('\n').filter(opt => opt.trim());
        return options.map((value, index) => ({
          key: String.fromCharCode(65 + index),
          value: value
        }));
      }
    },
    /** 获取选项标签 */
    getOptionLabel(option, index) {
      // 如果option是对象且有key，使用key；否则使用默认的A、B、C、D
      if (typeof option === 'object' && option.key) {
        return option.key;
      }
      return String.fromCharCode(65 + index);
    },
    /** 格式化题目内容显示 */
    formatQuestionContent(content) {
      if (!content) return '';
      // 将[空]或{{空}}替换为___
      return sanitizeRichText(content.replace(/\[空\]|\{\{空\}\}/g, '___'));
    },
    /** 清洗富文本 */
    sanitizeContent(content) {
      return sanitizeRichText(content);
    },
    /** 格式化答案显示 */
    formatAnswer(answer, questionType) {
      if (!answer) return '';
      
      if (questionType === 'fill') {
        // 填空题答案处理
        try {
          const answerArray = JSON.parse(answer);
          if (Array.isArray(answerArray)) {
            // 处理嵌套数组格式 [["answer1"], ["answer2"]]
            return answerArray.map(item => {
              if (Array.isArray(item)) {
                return item.filter(a => a && a.trim()).join('; ');
              }
              return item && item.trim() ? item : '';
            }).filter(item => item).join(' | ');
          }
        } catch {
          // 如果JSON解析失败，直接返回原答案
        }
        return answer;
      }
      
      if (questionType === 'multiple') {
        // 多选题答案可能是数组
        try {
          const answerArray = JSON.parse(answer);
          if (Array.isArray(answerArray)) {
            return answerArray.map(a => String.fromCharCode(65 + parseInt(a))).join(', ');
          }
        } catch {
          // 如果解析失败，直接返回原答案
        }
      }
      
      if (questionType === 'single') {
        // 单选题转换为字母
        const index = parseInt(answer);
        if (!isNaN(index)) {
          return String.fromCharCode(65 + index);
        }
      }
      
      if (questionType === 'judge') {
        // 判断题 - 只显示正确或错误，不显示选项前缀
        return answer === '1' || answer === 'true' ? '正确' : '错误';
      }
      
      return answer;
    },
    /** 打印试卷 */
    printPaper() {
      window.print();
    }
  }
};
</script>

<style scoped>
.paper-preview {
  padding: 20px;
}

.paper-preview.fullscreen {
  padding: 0;
  background: #fff;
}

.paper-preview.fullscreen .paper-header {
  display: none;
}

.paper-preview.fullscreen .preview-actions {
  display: none;
}

.paper-preview.fullscreen .questions-list {
  max-height: none;
}

.paper-preview.fullscreen .section-title {
  padding-left: 20px;
}

.paper-preview.fullscreen .question-item {
  margin-left: 20px;
  margin-right: 20px;
}

.paper-header {
  margin-bottom: 20px;
}

.paper-title {
  text-align: center;
  color: #303133;
  margin-bottom: 20px;
}

.paper-info {
  background: #f8f9fa;
  padding: 15px;
  border-radius: 4px;
}

.info-item {
  margin-bottom: 10px;
}

.info-item label {
  font-weight: bold;
  color: #606266;
}

.score {
  color: #e6a23c;
  font-weight: bold;
}

.paper-description {
  margin-top: 15px;
}

.paper-description label {
  font-weight: bold;
  color: #606266;
}

.paper-description p {
  margin: 5px 0 0 0;
  color: #606266;
  line-height: 1.6;
}

.section-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.section-title h3 {
  margin: 0;
  color: #303133;
}

.no-questions {
  text-align: center;
  padding: 40px 0;
}

.questions-list {
  max-height: 600px;
  overflow-y: auto;
}

.question-item {
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  padding: 20px;
  margin-bottom: 15px;
  background: #fff;
}

.question-header {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
  flex-wrap: wrap;
}

.question-number {
  font-weight: bold;
  margin-right: 10px;
  color: #409eff;
  min-width: 30px;
}

.question-title {
  color: #303133;
  margin-right: 10px;
  font-weight: 600;
  font-size: 16px;
  flex: 1;
  min-width: 200px;
}

.question-type {
  background: #f0f2f5;
  padding: 2px 8px;
  border-radius: 3px;
  font-size: 12px;
  margin-right: 10px;
  white-space: nowrap;
}

.question-score {
  color: #e6a23c;
  font-weight: bold;
  white-space: nowrap;
}

.question-content {
  margin-top: 10px;
}

.question-options {
  margin: 15px 0;
}

.option-item {
  margin-bottom: 8px;
  padding: 5px 0;
  display: flex;
  align-items: flex-start;
  line-height: 1.5;
}

.option-key {
  display: inline-block;
  width: 30px;
  font-weight: 600;
  color: #409eff;
  margin-right: 0;
}

.option-value {
  color: #303133;
  flex: 1;
}

.question-analysis {
  margin-top: 15px;
  padding: 10px;
  background: #f8f9fa;
  border-radius: 4px;
  border-left: 3px solid #409eff;
}

.correct-answer {
  margin-bottom: 10px;
}

.answer {
  color: #67c23a;
  font-weight: bold;
}

.analysis p {
  margin: 5px 0 0 0;
  line-height: 1.6;
}

.preview-actions {
  text-align: center;
  padding: 20px 0;
  border-top: 1px solid #e4e7ed;
  margin-top: 20px;
}

.preview-actions .el-button {
  margin: 0 10px;
}

/* 打印样式 */
@media print {
  .preview-actions {
    display: none;
  }
  
  .question-item {
    break-inside: avoid;
    margin-bottom: 20px;
  }
  
  .paper-preview {
    padding: 0;
  }
}
</style>
