<template>
  <div class="exam-result-container">
    <div class="breadcrumb-container">
      <el-breadcrumb separator="/">
        <el-breadcrumb-item :to="{ path: '/exam/examination' }">考试管理</el-breadcrumb-item>
        <el-breadcrumb-item>考试成绩</el-breadcrumb-item>
        <el-breadcrumb-item>{{ userInfo.userName }}的考试成绩</el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <div class="page-header">
      <div class="header-content">
        <h1>{{ examInfo.examName }} - 成绩查看</h1>
        <div class="user-info">
          <span class="user-name">{{ userInfo.userName }}</span>
          <span class="user-nick">{{ userInfo.nickName }}</span>
        </div>
      </div>
    </div>

    <!-- 如果支持多次考试，显示选择器 -->
    <el-card v-if="examUsers.length > 1" class="attempt-selector">
      <div slot="header">
        <span>选择考试记录</span>
      </div>
      <el-radio-group v-model="selectedAttempt" @change="handleAttemptChange">
        <el-radio-button 
          v-for="(examUser, index) in examUsers" 
          :key="examUser.id" 
          :label="index"
        >
          第{{ index + 1 }}次考试 - {{ examUser.examStatus | statusFilter }}
          <span v-if="examUser.totalScore != null">({{ examUser.totalScore }}分)</span>
        </el-radio-button>
      </el-radio-group>
    </el-card>

    <!-- 成绩概览 -->
    <el-card class="score-overview">
      <div slot="header">
        <span>成绩概览</span>
      </div>
      
      <div class="score-content">
        <div class="score-main">
          <div class="total-score">
            <div class="score-number" :class="scoreClass">{{ currentExamUser.totalScore || 0 }}</div>
            <div class="score-label">总分</div>
          </div>

          <div class="pass-status">
            <el-tag :type="passStatusType" size="large">
              {{ currentExamUser.isPassed === '1' ? '通过' : '未通过' }}
            </el-tag>
            <div class="pass-score">及格分：{{ examInfo.passScore }}分</div>
          </div>
        </div>

        <div class="exam-info">
          <el-descriptions :column="2" border>
            <el-descriptions-item label="考试状态">
              <el-tag :type="getExamStatusType(currentExamUser.examStatus)">
                {{ getExamStatusText(currentExamUser.examStatus) }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="开始时间">
              {{ parseTime(currentExamUser.startTime, '{y}-{m}-{d} {h}:{i}:{s}') || '-' }}
            </el-descriptions-item>
            <el-descriptions-item label="提交时间">
              {{ parseTime(currentExamUser.submitTime, '{y}-{m}-{d} {h}:{i}:{s}') || '-' }}
            </el-descriptions-item>
            <el-descriptions-item label="评分时间">
              {{ parseTime(currentExamUser.gradeTime, '{y}-{m}-{d} {h}:{i}:{s}') || '-' }}
            </el-descriptions-item>
          </el-descriptions>
        </div>
      </div>
    </el-card>

    <!-- 答题详情 -->
    <el-card class="answer-details" shadow="never">
      <div slot="header" class="clearfix">
        <span class="card-title">答题详情</span>
      </div>
      <ExamAnswerDetails 
        :answers="currentAnswerRecords || []"
        :paper-questions="paperQuestions || []"
        :show-filter="true"
        :initial-filter-type="'all'"
      />
    </el-card>

    <!-- 操作按钮 -->
    <div class="action-buttons">
      <el-button @click="handleBack">返回</el-button>
      <el-button type="primary" @click="handleExport">导出成绩</el-button>
    </div>
  </div>
</template>

<script>
import { getExamResult, exportScoreDetails } from "@/api/exam/examination";
import { getPaperQuestions } from "@/api/exam/paper";
import ExamAnswerDetails from "@/components/ExamAnswerDetails/index.vue";

export default {
  name: "ExamResult",
  components: {
    ExamAnswerDetails
  },
  data() {
    return {
      examId: null,
      userId: null,
      userInfo: {
        userName: '',
        nickName: ''
      },
      examInfo: {},
      examUsers: [],
      selectedAttempt: 0,
      loading: false,
      paperQuestions: []
    };
  },
  computed: {
    currentExamUser() {
      return this.examUsers[this.selectedAttempt] || {};
    },
    // 兼容不同的答题记录字段名
    currentAnswerRecords() {
      const examUser = this.currentExamUser;
      return examUser.answerRecords || examUser.answers || examUser.records || examUser.details || [];
    },
    scoreClass() {
      const score = this.currentExamUser.totalScore || 0;
      const passScore = this.examInfo.passScore || 0;
      return score >= passScore ? 'pass' : 'fail';
    },
    passStatusType() {
      return this.currentExamUser.isPassed === '1' ? 'success' : 'danger';
    }
  },
  filters: {
    statusFilter(status) {
      const statusMap = {
        'not_started': '未开始',
        'in_progress': '进行中',
        'submitted': '已提交',
        'graded': '已评分'
      };
      return statusMap[status] || status;
    }
  },
  created() {
    this.examId = this.$route.query.examId;
    this.userId = this.$route.query.userId;
    this.userInfo.userName = this.$route.query.userName;
    this.userInfo.nickName = this.$route.query.nickName;
    
    if (this.examId && this.userId) {
      this.getExamResult();
    }
  },
  methods: {
    /** 获取考试成绩 */
    async getExamResult() {
      this.loading = true;
      try {
        const response = await getExamResult(this.examId, this.userId);
        
        // 检查响应数据结构
        if (!response) {
          throw new Error('API响应为空');
        }
        
        // 兼容不同的数据结构
        let dataSource = null;
        if (response.data) {
          // 标准的若依框架响应格式：response.data 包含实际数据
          dataSource = response.data;
        } else if (response.examination || response.examUsers) {
          // 直接在response根级别的数据格式
          dataSource = response;
        } else {
          throw new Error('未识别的API响应格式');
        }
        
        // 提取考试信息和用户列表
        if (dataSource.examination) {
          this.examInfo = dataSource.examination;
          this.examUsers = dataSource.examUsers || [];
        } else if (dataSource.examId || dataSource.examName) {
          // 如果数据直接是考试信息
          this.examInfo = dataSource;
          this.examUsers = dataSource.examUsers || dataSource.attempts || [];
        } else {
          throw new Error('响应数据中缺少考试信息');
        }
        
        // 调试：打印examUsers数据结构
        if (this.examUsers.length > 0) {
          if (this.examUsers[0].answerRecords && this.examUsers[0].answerRecords.length > 0) {
            // 打印答题记录的所有字段，帮助理解数据结构
          }
        }
        
        // 默认选择最新的考试记录
        this.selectedAttempt = 0;
        
        // 获取试卷题目信息
        if (this.examInfo && this.examInfo.paperId) {
          await this.getPaperQuestions(this.examInfo.paperId);
        }
        
        this.loading = false;
      } catch (error) {
        console.error('获取考试成绩失败:', error);
        this.$modal.msgError('获取考试成绩失败: ' + (error.message || '未知错误'));
        this.loading = false;
      }
    },
    
    /** 选择考试记录 */
    handleAttemptChange(index) {
      this.selectedAttempt = index;
    },

    /** 获取试卷题目信息 */
    async getPaperQuestions(paperId) {
      try {
        const response = await getPaperQuestions(paperId);
        if (response && response.data) {
          this.paperQuestions = response.data;
        }
      } catch (error) {
        console.error('获取试卷题目失败:', error);
        // 不显示错误提示，因为这不是关键功能
      }
    },
    
    /** 获取题目类型标签 */
    getQuestionTypeLabel(type) {
      const typeMap = {
        'single': '单选题',
        'single_choice': '单选题',
        'radio': '单选题',
        'multiple': '多选题',
        'multiple_choice': '多选题',
        'checkbox': '多选题',
        'judge': '判断题',
        'true_false': '判断题',
        'boolean': '判断题',
        'fill': '填空题',
        'blank': '填空题',
        'essay': '问答题',
        'text': '问答题',
        'short_answer': '简答题',
        'unknown': '未知类型',
        '0': '单选题',
        '1': '多选题', 
        '2': '判断题',
        '3': '填空题',
        '4': '问答题'
      };
      return typeMap[type] || `${type}类型` || '未知类型';
    },
    
    /** 获取分数样式 */
    getScoreClass(isCorrect) {
      return {
        'correct': isCorrect === '1',
        'incorrect': isCorrect === '0',
        'partial': isCorrect === '2'
      };
    },
    
    /** 获取答案样式 */
    getAnswerClass(isCorrect) {
      return {
        'correct-answer': isCorrect === '1',
        'incorrect-answer': isCorrect === '0',
        'partial-answer': isCorrect === '2'
      };
    },
    
    /** 获取考试状态文本 */
    getExamStatusText(status) {
      const statusMap = {
        'not_started': '未开始',
        'in_progress': '进行中',
        'submitted': '已提交',
        'graded': '已评分',
        'registered': '已报名'
      };
      return statusMap[status] || status || '未知状态';
    },
    
    /** 获取考试状态标签类型 */
    getExamStatusType(status) {
      const typeMap = {
        'not_started': 'info',
        'in_progress': 'warning',
        'submitted': 'primary',
        'graded': 'success',
        'registered': 'info'
      };
      return typeMap[status] || 'info';
    },
    
    /** 返回 */
    handleBack() {
      this.$router.go(-1);
    },
    
    /** 导出成绩 */
    handleExport() {
      this.$modal.loading('正在导出成绩，请稍候...');
      exportScoreDetails(this.examId, {
        includeAnswerDetails: true,
        userId: this.userId
      }).then(response => {
        this.$download.saveAs(response, `score_details_${this.examId}_${this.userId}_${new Date().getTime()}.xlsx`);
      }).catch(error => {
        this.$message.error('导出失败：' + (error.message || '未知错误'));
      }).finally(() => {
        this.$modal.closeLoading();
      });
    },

    // 注意：shouldShowOptions、parseOptions、formatAnswer 等方法已移至 ExamAnswerDetails 组件中
  }
};
</script>

<style scoped>
.exam-result-container {
  padding: 20px;
}

.breadcrumb-container {
  margin-bottom: 20px;
}

.page-header {
  background: #fff;
  padding: 20px;
  border-radius: 6px;
  margin-bottom: 20px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-content h1 {
  margin: 0;
  color: #303133;
  font-size: 24px;
}

.user-info {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
}

.user-name {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
}

.user-nick {
  font-size: 14px;
  color: #909399;
}

.attempt-selector {
  margin-bottom: 20px;
}

.score-overview {
  margin-bottom: 20px;
}

.score-content {
  display: flex;
  gap: 30px;
  align-items: flex-start;
}

.score-main {
  display: flex;
  gap: 30px;
  align-items: center;
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

.score-number.pass {
  color: #67C23A;
}

.score-number.fail {
  color: #F56C6C;
}

.score-label {
  font-size: 14px;
  color: #909399;
}

.pass-status {
  text-align: center;
}

.pass-score {
  margin-top: 10px;
  font-size: 14px;
  color: #909399;
}

.exam-info {
  flex: 1;
}

.answer-details {
  margin-bottom: 20px;
}

/* 答题详情相关样式已移至 ExamAnswerDetails 组件中 */

.action-buttons {
  display: flex;
  justify-content: center;
  gap: 15px;
  margin-top: 20px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .score-content {
    flex-direction: column;
  }
  
  .score-main {
    flex-direction: column;
    text-align: center;
  }
  
  .question-header {
    flex-wrap: wrap;
  }
  
  .answer-section {
    flex-direction: column;
  }
}
</style>
