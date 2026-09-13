<template>
  <div class="h5-exam-index" v-loading="loading">
    <div class="exam-info" v-if="examination">
      <div class="exam-header">
        <h1>{{ examination.examName }}</h1>
        <div class="exam-status" :class="examStatusClass">
          {{ examStatusText }}
        </div>
      </div>
      
      <div class="exam-details">
        <div class="detail-item">
          <i class="el-icon-document"></i>
          <span>试卷：{{ examination.paperName }}</span>
        </div>
        <div class="detail-item">
          <i class="el-icon-time"></i>
          <span>时长：{{ examination.duration }}分钟</span>
        </div>
        <div class="detail-item">
          <i class="el-icon-trophy"></i>
          <span>总分：{{ examination.totalScore }}分（及格{{ examination.passScore }}分）</span>
        </div>
        <div class="detail-item">
          <i class="el-icon-date"></i>
          <span>考试时间：{{ formatDate(examination.startTime) }} - {{ formatDate(examination.endTime) }}</span>
        </div>
        <div class="detail-item" v-if="examination.maxAttempts > 1">
          <i class="el-icon-refresh"></i>
          <span>可考次数：{{ attemptCount }}/{{ examination.maxAttempts }}</span>
        </div>
      </div>
      
      <div class="exam-desc" v-if="examination.examDescription">
        <h3>考试说明</h3>
        <p>{{ examination.examDescription }}</p>
      </div>
      
      <div class="exam-tips">
        <h3><i class="el-icon-warning-outline"></i> 注意事项</h3>
        <ul>
          <li>请确保网络稳定，避免考试中断</li>
          <li>考试过程中请勿刷新或退出页面</li>
          <li>答案会自动保存，请放心作答</li>
          <li>考试时间到后系统将自动提交</li>
        </ul>
      </div>
    </div>
    
    <div class="exam-action" v-if="!hasCompletedAttempt && primaryActionVisible">
      <button class="start-btn" @click="handleStartExam" :disabled="starting">
        <span v-if="!starting">
          <i class="el-icon-video-play"></i>
          {{ buttonText }}
        </span>
        <span v-else><i class="el-icon-loading"></i> 加载中...</span>
      </button>
    </div>

    <div class="exam-unavailable" v-else-if="examination && !loading && !hasCompletedAttempt">
      {{ unavailableText }}
    </div>
    
    <div class="exam-result" v-if="hasCompletedAttempt">
      <div class="result-card">
        <div class="result-icon" :class="resultStatusClass">
          <i :class="resultIconClass"></i>
        </div>
        <div class="result-score">{{ resultScoreText }}</div>
        <div class="result-label">{{ resultScoreLabel }}</div>
        <div class="result-status" :class="resultStatusClass">
          {{ resultStatusText }}
        </div>
        <div class="result-actions">
          <button class="view-btn" @click="viewResult">查看详情</button>
          <button v-if="canRetake" class="view-btn retake-outline-btn" @click="handleRetakeExam">再考一次</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { getExamInfo, startExam } from '@/api/mobile/exam'

export default {
  name: 'H5ExamIndex',
  data() {
    return {
      loading: false,
      starting: false,
      examId: null,
      examination: null,
      examUser: null,
      attemptCount: 0,
      canStart: false
    }
  },
  computed: {
    examStatusClass() {
      if (!this.examination) return ''
      const now = new Date()
      const startTime = new Date(this.examination.startTime)
      const endTime = new Date(this.examination.endTime)
      
      if (now < startTime) return 'waiting'
      if (now > endTime) return 'ended'
      return 'running'
    },
    examStatusText() {
      if (!this.examination) return ''
      const now = new Date()
      const startTime = new Date(this.examination.startTime)
      const endTime = new Date(this.examination.endTime)
      
      if (now < startTime) return '未开始'
      if (now > endTime) return '已结束'
      return '进行中'
    },
    buttonText() {
      if (this.examUser && this.examUser.examStatus === 'in_progress') {
        return '继续考试'
      }
      return '开始考试'
    },
    hasCompletedAttempt() {
      return this.examUser && ['submitted', 'graded'].includes(this.examUser.examStatus)
    },
    canRetake() {
      return this.hasCompletedAttempt && this.canStart
    },
    resultStatusClass() {
      if (!this.examUser || this.examUser.examStatus === 'submitted') return 'pending'
      return this.examUser.isPassed === '1' ? 'passed' : 'failed'
    },
    resultIconClass() {
      if (!this.examUser || this.examUser.examStatus === 'submitted') return 'el-icon-time'
      return this.examUser.isPassed === '1' ? 'el-icon-circle-check' : 'el-icon-circle-close'
    },
    resultStatusText() {
      if (!this.examUser) return ''
      if (this.examUser.examStatus === 'submitted') return '已提交，待评分'
      return this.examUser.isPassed === '1' ? '恭喜通过' : '未通过'
    },
    resultScoreText() {
      if (!this.examUser) return '-'
      if (this.examUser.examStatus === 'submitted') return '待评分'
      return this.examUser.totalScore
    },
    resultScoreLabel() {
      return this.examUser && this.examUser.examStatus === 'submitted' ? '当前状态' : '您的得分'
    },
    primaryActionVisible() {
      return this.canStart || (this.examUser && this.examUser.examStatus === 'in_progress')
    },
    unavailableText() {
      if (!this.examination) return ''
      const now = new Date()
      const startTime = new Date(this.examination.startTime)
      const endTime = new Date(this.examination.endTime)
      if (now < startTime) return '考试尚未开始'
      if (now > endTime) return '考试已结束'
      return '当前状态暂不能开始考试'
    }
  },
  created() {
    this.examId = this.$route.params.examId
    this.loadExamInfo()
  },
  methods: {
    loadExamInfo() {
      this.loading = true
      getExamInfo(this.examId)
        .then(res => {
          if (res.code === 200) {
            this.examination = res.examination
            this.examUser = res.examUser
            this.attemptCount = res.attemptCount || 0
            this.canStart = res.canStart
          } else {
            this.$message.error(res.msg || '获取考试信息失败')
          }
        })
        .catch(err => {
          console.error('获取考试信息失败', err)
          this.$message.error('获取考试信息失败')
        })
        .finally(() => {
          this.loading = false
        })
    },
    handleStartExam() {
      this.starting = true
      startExam(this.examId)
        .then(res => {
          if (res.code === 200) {
            // 跳转到答题页面
            this.$router.push({
              path: '/h5/exam/' + this.examId + '/taking',
              query: { examUserId: res.examUserId }
            })
          } else {
            this.$message.error(res.msg || '开始考试失败')
          }
        })
        .catch(err => {
          console.error('开始考试失败', err)
          this.$message.error(err.msg || '开始考试失败')
        })
        .finally(() => {
          this.starting = false
        })
    },
    handleRetakeExam() {
      this.$confirm('将开始新一轮考试，本次历史成绩仍会保留。是否继续？', '再考一次', {
        confirmButtonText: '继续',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.handleStartExam()
      }).catch(() => {})
    },
    viewResult() {
      this.$router.push({
        path: '/h5/exam/' + this.examId + '/result',
        query: this.examUser && this.examUser.id ? { examUserId: this.examUser.id } : {}
      })
    },
    formatDate(dateStr) {
      if (!dateStr) return '-'
      const date = new Date(dateStr)
      return `${date.getMonth() + 1}/${date.getDate()} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
    }
  }
}
</script>

<style scoped>
.h5-exam-index {
  min-height: 100vh;
  background: #f5f5f5;
  padding-bottom: 80px;
}

.exam-info {
  background: #fff;
  margin: 12px;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.exam-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;
}

.exam-header h1 {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  flex: 1;
  margin-right: 10px;
}

.exam-status {
  padding: 4px 10px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
  white-space: nowrap;
}

.exam-status.waiting {
  background: #fdf6ec;
  color: #e6a23c;
}

.exam-status.running {
  background: #ecf5ff;
  color: #409eff;
}

.exam-status.ended {
  background: #f4f4f5;
  color: #909399;
}

.exam-details {
  border-top: 1px solid #ebeef5;
  padding-top: 16px;
}

.detail-item {
  display: flex;
  align-items: center;
  padding: 8px 0;
  color: #606266;
  font-size: 14px;
}

.detail-item i {
  width: 20px;
  color: #409eff;
  margin-right: 10px;
}

.exam-desc {
  margin-top: 16px;
  padding: 16px;
  background: #f5f7fa;
  border-radius: 8px;
}

.exam-desc h3 {
  font-size: 14px;
  color: #303133;
  margin-bottom: 8px;
}

.exam-desc p {
  font-size: 13px;
  color: #606266;
  line-height: 1.6;
}

.exam-tips {
  margin-top: 16px;
}

.exam-tips h3 {
  font-size: 14px;
  color: #e6a23c;
  margin-bottom: 10px;
}

.exam-tips h3 i {
  margin-right: 5px;
}

.exam-tips ul {
  padding-left: 20px;
}

.exam-tips li {
  font-size: 13px;
  color: #909399;
  line-height: 1.8;
}

.exam-action {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 12px 20px;
  background: #fff;
  box-shadow: 0 -2px 12px rgba(0, 0, 0, 0.08);
}

.start-btn {
  width: 100%;
  height: 48px;
  background: linear-gradient(135deg, #409eff 0%, #2593fc 100%);
  border: none;
  border-radius: 10px;
  color: #fff;
  font-size: 16px;
  font-weight: 500;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
}

.start-btn:active {
  transform: scale(0.98);
}

.start-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.start-btn i {
  margin-right: 8px;
}

.exam-result {
  margin: 12px;
}

.exam-unavailable {
  margin: 12px;
  padding: 14px 16px;
  border-radius: 8px;
  background: #f4f4f5;
  color: #606266;
  text-align: center;
  font-size: 14px;
}

.result-card {
  background: #fff;
  border-radius: 12px;
  padding: 30px 20px;
  text-align: center;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.result-icon {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 16px;
}

.result-icon.passed {
  background: #e6f7e6;
}

.result-icon.failed {
  background: #fee;
}

.result-icon.pending {
  background: #fdf6ec;
}

.result-icon i {
  font-size: 35px;
}

.result-icon.passed i {
  color: #67c23a;
}

.result-icon.failed i {
  color: #f56c6c;
}

.result-icon.pending i {
  color: #e6a23c;
}

.result-score {
  font-size: 48px;
  font-weight: 600;
  color: #303133;
}

.result-label {
  font-size: 14px;
  color: #909399;
  margin-top: 5px;
}

.result-status {
  display: inline-block;
  padding: 6px 16px;
  border-radius: 20px;
  font-size: 14px;
  font-weight: 500;
  margin-top: 16px;
}

.result-status.passed {
  background: #e6f7e6;
  color: #67c23a;
}

.result-status.failed {
  background: #fee;
  color: #f56c6c;
}

.result-status.pending {
  background: #fdf6ec;
  color: #e6a23c;
}

.view-btn {
  width: 100%;
  height: 44px;
  background: #fff;
  border: 1px solid #409eff;
  border-radius: 8px;
  color: #409eff;
  font-size: 15px;
  cursor: pointer;
  margin-top: 20px;
}

.action-view-btn {
  margin-top: 0;
  background: #409eff;
  color: #fff;
}

.result-actions {
  display: flex;
  gap: 10px;
  margin-top: 20px;
}

.result-actions .view-btn {
  flex: 1;
  margin-top: 0;
}

.retake-outline-btn {
  border-color: #67c23a;
  color: #67c23a;
}
</style>
