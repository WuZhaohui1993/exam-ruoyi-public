<template>
  <div class="h5-exam-result" v-loading="loading">
    <div class="error-card" v-if="loadError">
      <div class="error-icon"><i class="el-icon-warning-outline"></i></div>
      <div class="error-title">无法查看成绩</div>
      <div class="error-message">{{ loadError }}</div>
      <button class="back-btn inline-back-btn" @click="goBack">
        <i class="el-icon-back"></i> 返回考试
      </button>
    </div>

    <template v-else-if="resultLoaded">
    <!-- 成绩卡片 -->
    <div class="result-card">
      <div class="result-header">
        <div class="result-icon" :class="resultStatusClass">
          <i :class="resultIconClass"></i>
        </div>
        <div class="result-status" :class="resultStatusClass">
          {{ resultStatusText }}
        </div>
      </div>
      
      <div class="score-display">
        <div class="score-number">{{ scoreText }}</div>
        <div class="score-label">{{ scoreLabel }}</div>
      </div>
      
      <div class="score-info">
        <div class="info-item">
          <span class="label">总分</span>
          <span class="value">{{ examination.totalScore }}</span>
        </div>
        <div class="divider"></div>
        <div class="info-item">
          <span class="label">及格分</span>
          <span class="value">{{ examination.passScore }}</span>
        </div>
        <div class="divider"></div>
        <div class="info-item">
          <span class="label">得分率</span>
          <span class="value">{{ scoreRate }}%</span>
        </div>
      </div>
    </div>
    
    <!-- 考试信息 -->
    <div class="exam-info-card">
      <h3>考试信息</h3>
      <div class="info-list">
        <div class="info-row">
          <span class="label"><i class="el-icon-document"></i> 考试名称</span>
          <span class="value">{{ examination.examName }}</span>
        </div>
        <div class="info-row">
          <span class="label"><i class="el-icon-time"></i> 考试时长</span>
          <span class="value">{{ examination.duration }}分钟</span>
        </div>
        <div class="info-row" v-if="examUser.submitTime">
          <span class="label"><i class="el-icon-finished"></i> 提交时间</span>
          <span class="value">{{ formatDate(examUser.submitTime) }}</span>
        </div>
      </div>
    </div>
    
    <!-- 答题统计 -->
    <div class="stats-card" v-if="paperQuestions.length > 0 || answerRecords.length > 0">
      <h3>答题统计</h3>
      <div class="stats-grid">
        <div class="stat-item">
          <div class="stat-number total">{{ totalQuestions }}</div>
          <div class="stat-label">总题数</div>
        </div>
        <div class="stat-item">
          <div class="stat-number correct">{{ correctCount }}</div>
          <div class="stat-label">正确数</div>
        </div>
        <div class="stat-item">
          <div class="stat-number partial">{{ partialCount }}</div>
          <div class="stat-label">部分正确</div>
        </div>
        <div class="stat-item">
          <div class="stat-number wrong">{{ wrongCount }}</div>
          <div class="stat-label">错误数</div>
        </div>
        <div class="stat-item">
          <div class="stat-number unanswered">{{ unansweredCount }}</div>
          <div class="stat-label">未答数</div>
        </div>
      </div>
    </div>
    
    <!-- 操作按钮 -->
    <div class="action-buttons">
      <button class="upload-btn" @click="uploadAttachment">
        <i class="el-icon-upload2"></i> 上传附件
      </button>
      <button class="back-btn" @click="goBack">
        <i class="el-icon-back"></i> 返回
      </button>
    </div>
    </template>
  </div>
</template>

<script>
import { getExamResult } from '@/api/mobile/exam'

export default {
  name: 'H5ExamResult',
  data() {
    return {
      loading: false,
      examId: null,
      examUserId: null,
      examination: {},
      examUser: {},
      answerRecords: [],
      paperQuestions: [],
      loadError: '',
      resultLoaded: false
    }
  },
  computed: {
    isPassed() {
      return this.examUser.isPassed === '1'
    },
    resultStatusClass() {
      if (this.examUser.examStatus === 'submitted') return 'pending'
      return this.isPassed ? 'passed' : 'failed'
    },
    resultIconClass() {
      if (this.examUser.examStatus === 'submitted') return 'el-icon-time'
      return this.isPassed ? 'el-icon-circle-check' : 'el-icon-circle-close'
    },
    resultStatusText() {
      if (this.examUser.examStatus === 'submitted') return '已提交，待评分'
      return this.isPassed ? '恭喜通过' : '未通过'
    },
    scoreText() {
      if (this.examUser.examStatus === 'submitted') return '待评分'
      return this.examUser.totalScore || 0
    },
    scoreLabel() {
      return this.examUser.examStatus === 'submitted' ? '当前状态' : '您的得分'
    },
    totalQuestions() {
      return this.paperQuestions.length || this.answerRecords.length
    },
    answeredCount() {
      return this.answerRecords.filter(r => r.userAnswer && String(r.userAnswer).trim() !== '').length
    },
    scoreRate() {
      const totalScore = Number(this.examination.totalScore || 0)
      if (!totalScore) return 0
      const rate = (Number(this.examUser.totalScore || 0) / totalScore) * 100
      return Math.round(rate)
    },
    correctCount() {
      return this.answerRecords.filter(r => r.isCorrect === '1').length
    },
    partialCount() {
      return this.answerRecords.filter(r => r.isCorrect === '2').length
    },
    wrongCount() {
      return this.answerRecords.filter(r => r.isCorrect === '0' && r.userAnswer && String(r.userAnswer).trim() !== '').length
    },
    unansweredCount() {
      return Math.max(this.totalQuestions - this.answeredCount, 0)
    }
  },
  created() {
    this.examId = this.$route.params.examId
    this.examUserId = this.$route.query.examUserId
    this.loadResult()
  },
  methods: {
    loadResult() {
      this.loading = true
      this.loadError = ''
      this.resultLoaded = false
      getExamResult(this.examId, this.examUserId)
        .then(res => {
          if (res.code === 200) {
            this.examination = res.examination || {}
            this.examUser = res.examUser || {}
            this.answerRecords = res.answerRecords || []
            this.paperQuestions = res.paperQuestions || []
            this.resultLoaded = true
          } else {
            this.loadError = res.msg || '获取结果失败'
          }
        })
        .catch(err => {
          console.error('获取结果失败', err)
          this.loadError = (err && (err.msg || err.message)) || '获取结果失败'
        })
        .finally(() => {
          this.loading = false
        })
    },
    formatDate(dateStr) {
      if (!dateStr) return '-'
      const date = new Date(dateStr)
      return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
    },
    goBack() {
      this.$router.push('/h5/exam/' + this.examId)
    },
    uploadAttachment() {
      this.$router.push({
        path: '/h5/exam/' + this.examId + '/attachment',
        query: this.examUser && this.examUser.id ? { examUserId: this.examUser.id } : {}
      })
    }
  }
}
</script>

<style scoped>
.h5-exam-result {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px 15px;
}

.result-card {
  background: #fff;
  border-radius: 16px;
  padding: 30px 20px;
  text-align: center;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);
  margin-bottom: 15px;
}

.error-card {
  background: #fff;
  border-radius: 16px;
  padding: 34px 20px;
  text-align: center;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);
}

.error-icon {
  font-size: 42px;
  color: #e6a23c;
  margin-bottom: 12px;
}

.error-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 8px;
}

.error-message {
  color: #606266;
  font-size: 14px;
  line-height: 1.6;
}

.inline-back-btn {
  margin-top: 24px;
}

.result-header {
  margin-bottom: 20px;
}

.result-icon {
  width: 70px;
  height: 70px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 12px;
}

.result-icon.passed {
  background: linear-gradient(135deg, #67c23a 0%, #85ce61 100%);
}

.result-icon.failed {
  background: linear-gradient(135deg, #f56c6c 0%, #f78989 100%);
}

.result-icon.pending {
  background: linear-gradient(135deg, #e6a23c 0%, #ebb563 100%);
}

.result-icon i {
  font-size: 40px;
  color: #fff;
}

.result-status {
  font-size: 18px;
  font-weight: 600;
}

.result-status.passed {
  color: #67c23a;
}

.result-status.failed {
  color: #f56c6c;
}

.result-status.pending {
  color: #e6a23c;
}

.score-display {
  margin: 20px 0;
}

.score-number {
  font-size: 60px;
  font-weight: 700;
  color: #303133;
  line-height: 1;
}

.score-label {
  font-size: 14px;
  color: #909399;
  margin-top: 8px;
}

.score-info {
  display: flex;
  justify-content: center;
  align-items: center;
  padding-top: 20px;
  border-top: 1px solid #f0f0f0;
}

.info-item {
  text-align: center;
  padding: 0 20px;
}

.info-item .label {
  display: block;
  font-size: 12px;
  color: #909399;
  margin-bottom: 5px;
}

.info-item .value {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.divider {
  width: 1px;
  height: 30px;
  background: #e4e7ed;
}

.exam-info-card,
.stats-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 15px;
}

.exam-info-card h3,
.stats-card h3 {
  font-size: 16px;
  color: #303133;
  margin-bottom: 15px;
}

.info-row {
  display: flex;
  justify-content: space-between;
  padding: 10px 0;
  border-bottom: 1px solid #f5f5f5;
}

.info-row:last-child {
  border-bottom: none;
}

.info-row .label {
  font-size: 14px;
  color: #909399;
}

.info-row .label i {
  margin-right: 8px;
}

.info-row .value {
  font-size: 14px;
  color: #303133;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 10px;
}

.stat-item {
  text-align: center;
  background: #f5f7fa;
  border-radius: 8px;
  padding: 15px 10px;
}

.stat-number {
  font-size: 24px;
  font-weight: 600;
}

.stat-number.total {
  color: #409eff;
}

.stat-number.correct {
  color: #67c23a;
}

.stat-number.partial {
  color: #e6a23c;
}

.stat-number.wrong {
  color: #f56c6c;
}

.stat-number.unanswered {
  color: #909399;
}

.stat-label {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
}

.action-buttons {
  padding: 20px 0;
}

.back-btn,
.upload-btn {
  width: 100%;
  height: 48px;
  border: 1px solid rgba(255, 255, 255, 0.5);
  border-radius: 10px;
  font-size: 16px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
}

.back-btn {
  background: rgba(255, 255, 255, 0.2);
  color: #fff;
}

.upload-btn {
  background: #409eff;
  color: #fff;
  border-color: #409eff;
  margin-bottom: 12px;
}

.back-btn i,
.upload-btn i {
  margin-right: 8px;
}

.back-btn:active,
.upload-btn:active {
  background: rgba(255, 255, 255, 0.3);
}
</style>
