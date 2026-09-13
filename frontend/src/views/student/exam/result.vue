<template>
  <div class="exam-result-container">
    <!-- 成绩概览 -->
    <div class="result-header">
      <el-card>
        <div class="score-overview">
          <div class="exam-info">
            <h2>{{ examInfo.examName }}</h2>
            <div class="exam-meta">
              <span>考试时间：{{ examInfo.startTime }} - {{ examInfo.endTime }}</span>
              <span>考试时长：{{ examInfo.duration }}分钟</span>
            </div>
          </div>

          <div class="score-display">
            <div class="total-score">
              <div class="score-number" :class="scoreClass">{{ examUser.totalScore || 0 }}</div>
              <div class="score-label">得分</div>
            </div>

            <div class="pass-status">
              <el-tag :type="passStatusType" size="large">
                {{ examUser.isPassed === '1' ? '通过' : '未通过' }}
              </el-tag>
              <div class="pass-score">及格分：{{ examInfo.passScore }}分</div>
            </div>
          </div>
        </div>

        <div class="exam-stats">
          <el-row :gutter="16">
            <el-col :span="4">
              <div class="stat-item">
                <div class="stat-number">{{ statistics.totalQuestions }}</div>
                <div class="stat-label">总题数</div>
              </div>
            </el-col>
            <el-col :span="4">
              <div class="stat-item">
                <div class="stat-number correct-number">{{ statistics.correctCount }}</div>
                <div class="stat-label">答对题数</div>
              </div>
            </el-col>
            <el-col :span="4">
              <div class="stat-item">
                <div class="stat-number partial-number">{{ statistics.partialCount }}</div>
                <div class="stat-label">部分正确</div>
              </div>
            </el-col>
            <el-col :span="4">
              <div class="stat-item">
                <div class="stat-number incorrect-number">{{ statistics.incorrectCount }}</div>
                <div class="stat-label">答错题数</div>
              </div>
            </el-col>
            <el-col :span="4">
              <div class="stat-item">
                <div class="stat-number unanswered-number">{{ statistics.unansweredCount }}</div>
                <div class="stat-label">未答题数</div>
              </div>
            </el-col>
            <el-col :span="4">
              <div class="stat-item">
                <div class="stat-number accuracy-number">{{ statistics.scoreRate }}%</div>
                <div class="stat-label">得分率</div>
              </div>
            </el-col>
          </el-row>
        </div>
      </el-card>
    </div>

    <!-- 答题详情 -->
    <div class="result-content">
      <el-card>
        <div slot="header" class="card-header">
          <span>答题详情</span>
        </div>
        <ExamAnswerDetails 
          :answers="answers || []"
          :paper-questions="paperQuestions || []"
          :show-filter="true"
          :initial-filter-type="'all'"
          @filter-change="handleFilterChange"
        />
      </el-card>
    </div>

    <!-- 操作按钮 -->
    <div class="result-actions">
      <el-button @click="goBack">返回</el-button>
    </div>
  </div>
</template>

<script>
import { getExamResult } from "@/api/student/exam"
import ExamAnswerDetails from "@/components/ExamAnswerDetails/index.vue"

export default {
  name: "StudentExamResult",
  components: {
    ExamAnswerDetails
  },
  data() {
    return {
      loading: false,
      examId: null,
      examInfo: {},
      paperInfo: {},
      examUser: {},
      answers: [],
      paperQuestions: [] // 试卷题目列表
    };
  },
  computed: {
    scoreClass() {
      const score = this.examUser.totalScore || 0;
      const passScore = this.examInfo.passScore || 60;
      return score >= passScore ? 'pass-score' : 'fail-score';
    },
    passStatusType() {
      return this.examUser.isPassed === '1' ? 'success' : 'danger';
    },
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
      const totalScore = Number(this.paperInfo.totalScore || this.examInfo.totalScore || 0);
      const userScore = Number(this.examUser.totalScore || 0);
      const scoreRate = totalScore > 0 ? Math.round((userScore / totalScore) * 100) : 0;

      return {
        totalQuestions,
        correctCount,
        partialCount,
        incorrectCount,
        unansweredCount,
        answeredCount,
        accuracy,
        scoreRate
      };
    }
  },
  created() {
    // 修复：路由参数名应该是id，examId为兼容性备选
    this.examId = this.$route.params.id || this.$route.params.examId;
    
    // 参数验证
    if (!this.examId || this.examId === 'undefined' || this.examId === 'null') {
      console.error('result.vue接收到无效的参数ID:', this.examId);
      this.$modal.msgError("参数错误：考试记录ID不能为空");
      this.$router.push('/student/exam');
      return;
    }
    
    this.getResult();
  },
  methods: {
    /** 获取成绩 */
    async getResult(retryCount = 0) {
      this.loading = true;
      try {
        const response = await getExamResult(this.examId);

        // 修复：若依框架的响应数据直接在response中，不需要.data
        const data = response;

        // 检查是否有成绩数据
        if (!data.examUser || !data.examUser.examStatus) {
          throw new Error('成绩数据不完整');
        }

        // 如果状态是已提交但还未评分，可以重试
        if (data.examUser.examStatus === 'submitted' && retryCount < 3) {
          this.loading = false;
          setTimeout(() => {
            this.getResult(retryCount + 1);
          }, 2000);
          return;
        }

        // 如果状态是已提交但重试次数超限，显示提示
        if (data.examUser.examStatus === 'submitted') {
          this.$modal.msgWarning("成绩正在生成中，请稍后查看");
          this.examInfo = data.examination;
          this.paperInfo = data.paper || {};
          this.examUser = data.examUser;
          this.answers = [];
          this.paperQuestions = data.paperQuestions || [];
        } else {
          this.examInfo = data.examination;
          this.paperInfo = data.paper || {};
          this.examUser = data.examUser;
          this.answers = data.answers || [];
          this.paperQuestions = data.paperQuestions || [];
        }

      } catch (error) {
        console.error('获取成绩失败:', error);

        // 根据错误类型给出不同提示
        if (error.message && error.message.includes('参数错误：考试记录ID不能为空')) {
          console.error('错误类型：参数错误 - 考试记录ID为空');
          this.$modal.msgError("参数错误：考试记录ID不能为空，请从考试列表重新进入");
          this.$router.push('/student/exam');
          return;
        } else if (error.message && error.message.includes('成绩尚未发布')) {
          console.error('错误类型：成绩未发布');
          this.$modal.msgWarning("成绩还未发布，请稍后查看");
        } else if (error.message && error.message.includes('您未参加此考试')) {
          console.error('错误类型：未参加考试');
          this.$modal.msgError("未找到您的考试记录，请检查是否已参加此考试");
        } else {
          console.error('错误类型：其他错误 -', error.message);
          this.$modal.msgError("获取成绩失败：" + (error.msg || error.message || "未知错误"));
        }

        // 如果是第一次尝试且可能是时序问题，可以重试一次
        if (retryCount === 0 && !error.message.includes('参数错误')) {
          setTimeout(() => {
            this.getResult(1);
          }, 3000);
          return;
        }

        // 多次重试失败后，返回考试列表
        if (retryCount > 0) {
          this.$router.push('/student/exam');
        }
      } finally {
        this.loading = false;
      }
    },

    /** 处理过滤器变化 */
    handleFilterChange(filterType) {
      // 可以在这里处理过滤器变化的逻辑，如果需要的话
    },

    /** 返回 */
    goBack() {
      this.$router.push('/student/exam');
    },

    /** 判断是否应该显示选项 */
    shouldShowOptions(question) {
      if (!question) return false;

      // 判断题始终显示选项（兼容新旧类型）
      if (question.questionType === 'true_false' || question.questionType === 'judge') {
        return true;
      }

      // 单选、多选题需要有选项数据（兼容新旧类型）
      if (question.questionType === 'single_choice' || question.questionType === 'multiple_choice' ||
          question.questionType === 'single' || question.questionType === 'multiple') {
        return !!question.questionOptions;
      }

      return false;
    },

    /** 解析选项 */
    parseOptions(optionsStr) {
      if (!optionsStr) return [];

      try {
        // 尝试解析JSON格式
        const options = JSON.parse(optionsStr);
        if (Array.isArray(options) && options.length > 0) {
          // 检查数组元素的格式
          const firstOption = options[0];

          // 标准格式：[{key: "A", value: "选项内容"}]
          if (firstOption && typeof firstOption === 'object' && firstOption.key && firstOption.value) {
            return options.map(opt => ({
              label: opt.key,
              content: opt.value
            }));
          }

          // 兼容旧格式：[{label: "A", content: "选项内容"}]
          if (firstOption && typeof firstOption === 'object' && firstOption.label && firstOption.content) {
            return options;
          }

          // 简单数组格式：["选项1", "选项2", "选项3", "选项4"]
          if (typeof firstOption === 'string') {
            return options.map((content, index) => ({
              label: String.fromCharCode(65 + index), // A, B, C, D...
              content: content
            }));
          }
        }
      } catch (e) {
        // JSON解析失败，尝试其他格式
      }

      // 如果JSON解析失败，尝试按逗号分隔的字符串格式
      if (typeof optionsStr === 'string' && optionsStr.includes(',')) {
        const simpleOptions = optionsStr.split(',').map((item, index) => {
          const label = String.fromCharCode(65 + index); // A, B, C, D...
          return {
            label: label,
            content: item.trim()
          };
        });
        return simpleOptions;
      }

      return [];
    },

    /** 格式化答案显示 */
    formatAnswer(answer, question) {
      if (!answer || !question) return answer;

      // 判断题特殊处理 - 只显示正确或错误，不显示选项前缀
      if (question.questionType === 'true_false' || question.questionType === 'judge') {
        if (answer === 'A' || answer === '1' || answer === 'true') {
          return '正确';
        } else if (answer === 'B' || answer === '0' || answer === 'false') {
          return '错误';
        }
        return answer;
      }

      // 对于单选、多选题，将选项值转换为选项文本
      if (question.questionType === 'single_choice' || question.questionType === 'multiple_choice' ||
          question.questionType === 'single' || question.questionType === 'multiple') {
        const options = this.parseOptions(question.questionOptions);
        if (options && options.length > 0) {
          // 单选题
          if (question.questionType === 'single_choice' || question.questionType === 'single') {
            const option = options.find(opt => opt.label === answer);
            return option ? `${answer}. ${option.content}` : answer;
          }
          // 多选题
          else if (question.questionType === 'multiple_choice' || question.questionType === 'multiple') {
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
    }
  }
};
</script>

<style scoped>
.exam-result-container {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: 100vh;
}

.result-header {
  margin-bottom: 20px;
}

.score-overview {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
}

.exam-info h2 {
  margin: 0 0 10px 0;
  color: #303133;
}

.exam-meta {
  color: #909399;
  font-size: 14px;
}

.exam-meta span {
  margin-right: 20px;
}

.score-display {
  display: flex;
  align-items: center;
  gap: 30px;
}

.total-score {
  text-align: center;
}

.score-number {
  font-size: 48px;
  font-weight: bold;
  line-height: 1;
  margin-bottom: 8px;
}

.pass-score { color: #67c23a; }
.fail-score { color: #f56c6c; }

.score-label {
  color: #909399;
  font-size: 14px;
}

.pass-status {
  text-align: center;
}

.pass-score {
  margin-top: 8px;
  color: #909399;
  font-size: 14px;
}

.exam-stats {
  padding-top: 20px;
  border-top: 1px solid #e4e7ed;
}

.stat-item {
  text-align: center;
  padding: 20px;
  background-color: #f9f9f9;
  border-radius: 4px;
}

.stat-number {
  font-size: 24px;
  font-weight: bold;
  margin-bottom: 8px;
}

.correct-number { color: #67c23a; }
.partial-number { color: #e6a23c; }
.incorrect-number { color: #f56c6c; }
.unanswered-number { color: #909399; }
.accuracy-number { color: #409eff; }

.stat-label {
  color: #909399;
  font-size: 14px;
}

.result-content {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.result-actions {
  text-align: center;
  margin-top: 20px;
}

/* 注意：答题详情相关样式已移至ExamAnswerDetails组件中 */
</style>
