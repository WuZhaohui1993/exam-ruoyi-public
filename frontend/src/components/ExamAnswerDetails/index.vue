<template>
  <div class="answer-details-container">
    <!-- 筛选按钮 -->
    <div v-if="showFilter" class="filter-buttons">
      <el-button-group>
        <el-button
          :type="filterType === 'all' ? 'primary' : 'default'"
          @click="handleFilterChange('all')"
        >
          全部 ({{ statistics.totalQuestions }})
        </el-button>
        <el-button
          :type="filterType === 'correct' ? 'primary' : 'default'"
          @click="handleFilterChange('correct')"
        >
          答对 ({{ statistics.correctCount }})
        </el-button>
        <el-button
          :type="filterType === 'incorrect' ? 'primary' : 'default'"
          @click="handleFilterChange('incorrect')"
        >
          答错 ({{ statistics.incorrectCount }})
        </el-button>
        <el-button
          :type="filterType === 'partial' ? 'primary' : 'default'"
          @click="handleFilterChange('partial')"
        >
          部分正确 ({{ statistics.partialCount }})
        </el-button>
        <el-button
          :type="filterType === 'unanswered' ? 'primary' : 'default'"
          @click="handleFilterChange('unanswered')"
        >
          未答 ({{ statistics.unansweredCount }})
        </el-button>
      </el-button-group>
    </div>

    <!-- 答题列表 -->
    <div class="answer-list">
      <div v-for="(item, index) in filteredAnswers" :key="item.id || item.questionId || index" class="answer-item">
        <div class="question-header">
          <div class="question-info">
            <span class="question-number">第{{ getQuestionNumber(item) }}题</span>
            <span class="question-type">{{ getQuestionTypeLabel(item.question ? item.question.questionType : item.questionType) }}</span>
            <span class="question-score">{{ getQuestionScore(item) }}分</span>
          </div>
          <div class="result-info">
            <el-tag :type="getResultType(item)" size="small">{{ getResultText(item) }}</el-tag>
            <span class="user-score">得分：{{ item.userScore || 0 }}分</span>
          </div>
        </div>

        <div class="question-content">
          <!-- 题目标题 -->
          <div v-if="item.question && item.question.questionTitle" class="answer-section">
            <div class="answer-label">题目：</div>
            <div class="answer-content question-title">
              {{ item.question.questionTitle }}
            </div>
          </div>

          <!-- 题目内容 -->
          <div v-if="item.question && item.question.questionContent" class="answer-section">
            <div class="answer-label">题目内容：</div>
            <div class="answer-content question-content-text" v-html="formatQuestionContent(item.question.questionContent)">
            </div>
          </div>

          <div v-if="item.question && item.question.mediaList && item.question.mediaList.length" class="answer-section">
            <div class="answer-label">题目媒体：</div>
            <div class="answer-content question-media-content">
              <question-media :media-list="item.question.mediaList" />
            </div>
          </div>

          <!-- 选项（单选、多选、判断题） -->
          <div v-if="item.question && shouldShowOptions(item.question)" class="answer-section">
            <div class="answer-label">选项：</div>
            <div class="answer-content question-options">
              <div v-for="(option, optionIndex) in parseOptions(item.question.questionOptions)" :key="optionIndex" class="option-item">
                <!-- 判断题不显示选项前缀，其他题型显示 -->
                <template v-if="item.question.questionType === 'judge' || item.question.questionType === 'true_false'">
                  {{ option.content }}
                </template>
                <template v-else>
                  {{ option.label }}. {{ option.content }}
                </template>
              </div>
            </div>
          </div>

          <div class="answer-section">
            <div class="answer-label">您的答案：</div>
            <div class="answer-content user-answer">
              {{ formatAnswer(item.userAnswer, item.question) || '未答题' }}
            </div>
          </div>

          <div class="answer-section">
            <div class="answer-label">正确答案：</div>
            <div class="answer-content correct-answer">
              {{ formatAnswer(getCorrectAnswer(item), item.question) || '暂无参考答案' }}
            </div>
          </div>

          <div v-if="item.gradeComment" class="answer-section">
            <div class="answer-label">评语：</div>
            <div class="answer-content grade-comment">
              {{ item.gradeComment }}
            </div>
          </div>
        </div>
      </div>
    </div>

    <div v-if="filteredAnswers.length === 0" class="empty-result">
      <el-empty description="暂无数据"></el-empty>
    </div>
  </div>
</template>

<script>
import QuestionMedia from "@/components/QuestionMedia"
import { sanitizeRichText } from "@/utils/sanitize"

export default {
  name: 'ExamAnswerDetails',
  components: {
    QuestionMedia
  },
  props: {
    // 答题记录
    answers: {
      type: Array,
      default: () => []
    },
    // 试卷题目列表
    paperQuestions: {
      type: Array,
      default: () => []
    },
    // 是否显示筛选按钮
    showFilter: {
      type: Boolean,
      default: true
    },
    // 初始筛选类型
    initialFilterType: {
      type: String,
      default: 'all'
    }
  },
  data() {
    return {
      filterType: this.initialFilterType
    };
  },
  computed: {
    statistics() {
      // 使用试卷题目总数作为总题数
      const totalQuestions = this.paperQuestions ? this.paperQuestions.length : this.answers.length;
      let correctCount = 0;
      let partialCount = 0;
      let incorrectCount = 0;
      let answeredCount = 0;

      // 创建答题记录映射，便于查找
      const answerMap = {};
      this.answers.forEach(answer => {
        answerMap[answer.questionId] = answer;
      });

      // 遍历试卷所有题目进行统计
      if (this.paperQuestions) {
        this.paperQuestions.forEach(paperQuestion => {
          const answer = answerMap[paperQuestion.questionId];
          if (answer && answer.userAnswer && answer.userAnswer.trim() !== '') {
            // 有答案的题目
            answeredCount++;
            if (answer.isCorrect === '1') {
              correctCount++;
            } else if (answer.isCorrect === '2') {
              partialCount++;
            } else if (answer.isCorrect === '0') {
              incorrectCount++;
            }
          }
        });
      } else {
        // 兼容处理：如果没有试卷题目信息，使用原逻辑
        this.answers.forEach(answer => {
          if (answer.userAnswer && answer.userAnswer.trim() !== '') {
            answeredCount++;
            if (answer.isCorrect === '1') {
              correctCount++;
            } else if (answer.isCorrect === '2') {
              partialCount++;
            } else if (answer.isCorrect === '0') {
              incorrectCount++;
            }
          }
        });
      }

      const unansweredCount = totalQuestions - answeredCount;
      const accuracy = totalQuestions > 0 ? Math.round((correctCount / totalQuestions) * 100) : 0;

      return {
        totalQuestions,
        correctCount,
        partialCount,
        incorrectCount,
        unansweredCount,
        answeredCount,
        accuracy
      };
    },
    filteredAnswers() {
      if (!this.paperQuestions) return this.answers;

      // 创建答题记录映射
      const answerMap = {};
      this.answers.forEach(answer => {
        answerMap[answer.questionId] = answer;
      });

      if (this.filterType === 'all') {
        // 显示所有题目：包括已答和未答
        return this.paperQuestions.map(paperQuestion => {
          const answer = answerMap[paperQuestion.questionId];
          if (answer) {
            // 已答题目，返回实际答题记录
            return answer;
          } else {
            // 未答题目，创建虚拟答题记录
            return {
              questionId: paperQuestion.questionId,
              question: paperQuestion.question || {
                id: paperQuestion.questionId,
                questionTitle: paperQuestion.questionTitle || '题目信息缺失',
                questionContent: paperQuestion.questionContent || '',
                questionType: paperQuestion.questionType || 'single',
                questionOptions: paperQuestion.questionOptions || '',
                correctAnswer: paperQuestion.correctAnswer || '',
                questionAnalysis: paperQuestion.questionAnalysis || '',
                score: paperQuestion.score || 0
              },
              questionScore: paperQuestion.score || 0, // 添加题目分值
              userAnswer: '',
              isCorrect: null, // 未答题用null表示
              userScore: 0
            };
          }
        });
      } else if (this.filterType === 'correct') {
        // 只显示答对的题目
        return this.answers.filter(answer => answer.isCorrect === '1');
      } else if (this.filterType === 'incorrect') {
        // 只显示答错的题目（有答案但答错）
        return this.answers.filter(answer => {
          return answer.userAnswer && answer.userAnswer.trim() !== '' && answer.isCorrect === '0';
        });
      } else if (this.filterType === 'partial') {
        // 只显示部分正确的题目
        return this.answers.filter(answer => {
          return answer.userAnswer && answer.userAnswer.trim() !== '' && answer.isCorrect === '2';
        });
      } else if (this.filterType === 'unanswered') {
        // 显示未答题目
        return this.paperQuestions.filter(paperQuestion => {
          const answer = answerMap[paperQuestion.questionId];
          return !answer || !answer.userAnswer || answer.userAnswer.trim() === '';
        }).map(paperQuestion => {
          // 为未答题创建虚拟答题记录用于显示
          return {
            questionId: paperQuestion.questionId,
            question: paperQuestion.question || {
              id: paperQuestion.questionId,
              questionTitle: paperQuestion.questionTitle || '题目信息缺失',
              questionContent: paperQuestion.questionContent || '',
              questionType: paperQuestion.questionType || 'single',
              questionOptions: paperQuestion.questionOptions || '',
              correctAnswer: paperQuestion.correctAnswer || '',
              questionAnalysis: paperQuestion.questionAnalysis || '',
              score: paperQuestion.score || 0
            },
            questionScore: paperQuestion.score || 0, // 添加题目分值
            userAnswer: '',
            isCorrect: null, // 未答题用null表示
            userScore: 0
          };
        });
      }

      return [];
    }
  },
  methods: {
    /** 处理筛选类型变化 */
    handleFilterChange(type) {
      this.filterType = type;
      this.$emit('filter-change', type);
    },

    /** 判断是否应该显示选项 */
    shouldShowOptions(question) {
      if (!question) return false;
      const type = question.questionType;
      return type === 'single' || type === 'multiple' || type === 'judge' || type === 'true_false';
    },

    /** 解析选项 */
    parseOptions(optionsStr) {
      if (!optionsStr) return [];

      try {
        const options = JSON.parse(optionsStr);
        if (Array.isArray(options)) {
          return options.map((opt, index) => {
            if (typeof opt === 'object' && opt.key && opt.value) {
              return { label: opt.key, content: opt.value };
            } else if (typeof opt === 'object' && opt.label && opt.content) {
              return opt;
            } else if (typeof opt === 'string') {
              return {
                label: String.fromCharCode(65 + index),
                content: opt
              };
            }
            return { label: String.fromCharCode(65 + index), content: String(opt) };
          });
        }
      } catch (e) {
        // JSON解析失败，按逗号分隔处理
        if (typeof optionsStr === 'string') {
          return optionsStr.split(',').map((item, index) => ({
            label: String.fromCharCode(65 + index),
            content: item.trim()
          }));
        }
      }

      return [];
    },

    /** 格式化题目内容显示 */
    formatQuestionContent(content) {
      if (!content) return '';
      // 将[空]或{{空}}替换为___
      return sanitizeRichText(content.replace(/\[空\]|\{\{空\}\}/g, '___'));
    },

    /** 格式化答案 */
    formatAnswer(answer, question) {
      if (!answer || answer.trim() === '') return '';
      if (!question) return answer;

      const type = question.questionType;

      // 填空题特殊处理
      if (type === 'fill') {
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

      // 判断题特殊处理
      if (type === 'judge' || type === 'true_false') {
        if (answer === 'A' || answer === '1' || answer === 'true') {
          return '正确';
        } else if (answer === 'B' || answer === '0' || answer === 'false') {
          return '错误';
        }
        return answer;
      }

      // 单选、多选题转换为选项文本
      if (type === 'single' || type === 'multiple') {
        const options = this.parseOptions(question.questionOptions);
        if (options.length > 0) {
          if (type === 'single') {
            const option = options.find(opt => opt.label === answer);
            return option ? `${answer}. ${option.content}` : answer;
          } else {
            const answerLabels = answer.split(',');
            const answerTexts = answerLabels.map(label => {
              const option = options.find(opt => opt.label === label.trim());
              return option ? `${label.trim()}. ${option.content}` : label.trim();
            });
            return answerTexts.join('; ');
          }
        }
      }

      return answer;
    },

    /** 获取题目类型标签 */
    getQuestionTypeLabel(type) {
      const typeMap = {
        'single': '单选题',
        'multiple': '多选题',
        'judge': '判断题',
        'true_false': '判断题',
        'fill': '填空题',
        'essay': '问答题'
      };
      return typeMap[type] || '未知类型';
    },

    /** 获取题目序号（基于试卷题目的顺序） */
    getQuestionNumber(answer) {
      if (this.paperQuestions && this.paperQuestions.length > 0) {
        // 基于试卷题目顺序计算序号
        const index = this.paperQuestions.findIndex(item => item.questionId === answer.questionId);
        return index >= 0 ? index + 1 : 1;
      } else {
        // 兼容处理：基于答题记录顺序
        const index = this.answers.findIndex(item => item.id === answer.id);
        return index >= 0 ? index + 1 : 1;
      }
    },

    /** 获取题目分值 */
    getQuestionScore(answer) {
      // 优先从试卷题目中获取分值
      if (this.paperQuestions && answer.questionId) {
        const paperQuestion = this.paperQuestions.find(item => item.questionId === answer.questionId);
        if (paperQuestion && paperQuestion.score) {
          return paperQuestion.score;
        }
      }

      // 从答题记录的question对象中获取
      if (answer.question && answer.question.score) {
        return answer.question.score;
      }

      // 从答题记录本身获取
      if (answer.questionScore) {
        return answer.questionScore;
      }

      return 0;
    },

    /** 获取正确答案 */
    getCorrectAnswer(answer) {
      // 如果答题记录中有正确答案，直接返回
      if (answer.correctAnswer) {
        return answer.correctAnswer;
      }

      // 如果是虚拟答题记录（未答题），从question对象中获取
      if (answer.question && answer.question.correctAnswer) {
        return answer.question.correctAnswer;
      }

      // 从试卷题目中查找正确答案
      if (this.paperQuestions && answer.questionId) {
        const paperQuestion = this.paperQuestions.find(item => item.questionId === answer.questionId);
        if (paperQuestion && paperQuestion.correctAnswer) {
          return paperQuestion.correctAnswer;
        }
      }

      return '';
    },

    /** 获取结果类型 */
    getResultType(answer) {
      // 未答题：没有用户答案或isCorrect为null
      if (!answer.userAnswer || answer.userAnswer.trim() === '' || answer.isCorrect === null) {
        return 'info';
      }
      if (answer.isCorrect === '1') return 'success';
      if (answer.isCorrect === '0') return 'danger';
      return 'warning';
    },

    /** 获取结果文本 */
    getResultText(answer) {
      // 未答题：没有用户答案或isCorrect为null
      if (!answer.userAnswer || answer.userAnswer.trim() === '' || answer.isCorrect === null) {
        return '未答题';
      }
      if (answer.isCorrect === '1') return '正确';
      if (answer.isCorrect === '0') return '错误';
      return '部分正确';
    }
  }
};
</script>

<style scoped>
.answer-details-container {
  width: 100%;
}

.filter-buttons {
  margin-bottom: 20px;
  text-align: center;
}

.answer-list {
  max-height: 600px;
  overflow-y: auto;
}

.answer-item {
  border: 1px solid #E4E7ED;
  border-radius: 8px;
  margin-bottom: 16px;
  padding: 16px;
  background: #fff;
  transition: box-shadow 0.3s;
}

.answer-item:hover {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.question-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  padding-bottom: 12px;
  border-bottom: 1px solid #F5F7FA;
}

.question-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.question-number {
  font-weight: bold;
  color: #303133;
  font-size: 16px;
}

.question-type {
  background: #E1F3FF;
  color: #409EFF;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
}

.question-score {
  color: #909399;
  font-size: 14px;
}

.result-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-score {
  font-weight: bold;
  color: #606266;
  font-size: 14px;
}

.question-content {
  line-height: 1.6;
}

.answer-section {
  margin-bottom: 12px;
}

.answer-label {
  font-weight: bold;
  color: #606266;
  font-size: 14px;
  margin-bottom: 6px;
}

.answer-content {
  padding: 8px 12px;
  background-color: #F5F7FA;
  border-radius: 4px;
  font-size: 14px;
  color: #303133;
  line-height: 1.6;
}

.question-title {
  font-weight: bold;
  background-color: #FFF7E6;
  border-left: 4px solid #E6A23C;
}

.question-content-text {
  background-color: #F0F9FF;
  border-left: 4px solid #409EFF;
}

.question-media-content {
  background-color: #fff;
  border-left: 4px solid #409EFF;
}

.question-options {
  background-color: #F5F7FA;
}

.option-item {
  margin-bottom: 6px;
  padding: 4px 0;
  border-bottom: 1px dashed #E4E7ED;
}

.option-item:last-child {
  border-bottom: none;
  margin-bottom: 0;
}

.user-answer {
  background-color: #FFF2E8;
  border-left: 4px solid #E6A23C;
}

.correct-answer {
  background-color: #F0F9FF;
  border-left: 4px solid #67C23A;
}

.grade-comment {
  background-color: #F5F7FA;
  border-left: 4px solid #909399;
  font-style: italic;
}

.empty-result {
  text-align: center;
  padding: 40px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .question-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }

  .question-info {
    flex-wrap: wrap;
  }

  .result-info {
    align-self: flex-end;
  }
}
</style>
