<template>
  <div class="exam-card" :class="getExamCardClass(exam)">
    <!-- 卡片头部 -->
    <div class="card-header">
      <div class="exam-title-section">
        <h3 class="exam-title">{{ exam.examName }}</h3>
        <el-tag 
          class="status-tag" 
          :type="getExamStatusType(exam.status)" 
          size="small"
          effect="dark"
        >
          <i :class="getStatusIcon(exam.status)"></i>
          {{ getExamStatusText(exam.status) }}
        </el-tag>
      </div>
      <div class="exam-meta">
        <span class="paper-name">{{ exam.paperName }}</span>
      </div>
    </div>

    <!-- 卡片内容 -->
    <div class="card-content">
      <!-- 考试基本信息 -->
      <div class="exam-basic-info">
        <div class="info-row">
          <div class="info-item">
            <i class="el-icon-time icon-primary"></i>
            <span class="info-label">考试时长</span>
            <span class="info-value">{{ exam.duration }} 分钟</span>
          </div>
          <div class="info-item">
            <i class="el-icon-star-on icon-warning"></i>
            <span class="info-label">及格分数</span>
            <span class="info-value">{{ exam.passScore }} 分</span>
          </div>
        </div>
        <div class="info-row">
          <div class="info-item">
            <i class="el-icon-refresh icon-info"></i>
            <span class="info-label">考试次数</span>
            <span class="info-value">最多 {{ exam.maxAttempts }} 次</span>
          </div>
          <div class="info-item" v-if="exam.totalScore">
            <i class="el-icon-trophy icon-success"></i>
            <span class="info-label">总分</span>
            <span class="info-value">{{ exam.totalScore }} 分</span>
          </div>
        </div>
      </div>

      <!-- 时间信息 -->
      <div class="exam-time-info">
        <div class="time-item">
          <div class="time-label">
            <i class="el-icon-video-play"></i>
            开始
          </div>
          <div class="time-value">{{ parseTime(exam.startTime, '{m}-{d} {h}:{i}') }}</div>
        </div>
        <div class="time-divider">
          <i class="el-icon-right"></i>
        </div>
        <div class="time-item">
          <div class="time-label">
            <i class="el-icon-video-pause"></i>
            结束
          </div>
          <div class="time-value">{{ parseTime(exam.endTime, '{m}-{d} {h}:{i}') }}</div>
        </div>
      </div>

      <!-- 考试进度和提示 -->
      <div class="exam-progress">
        <div v-if="getTimeStatus(exam)" class="time-status" :class="getTimeStatusClass(exam)">
          <i :class="getTimeStatusIcon(exam)"></i>
          {{ getTimeStatus(exam) }}
        </div>
      </div>
    </div>

    <!-- 卡片操作 -->
    <div class="card-actions">
      <el-button 
        class="action-btn primary-btn"
        :type="buttonType" 
        size="medium"
        @click="handleExamAction"
        :disabled="!buttonEnabled"
        :loading="exam.loading"
      >
        <i :class="buttonIcon"></i>
        {{ buttonText }}
      </el-button>
      <el-button 
        class="action-btn secondary-btn"
        size="medium"
        plain
        @click="handleViewDetail"
      >
        <i class="el-icon-view"></i>
        详情
      </el-button>
    </div>

    <!-- 卡片装饰元素 -->
    <div class="card-decoration">
      <div class="decoration-circle decoration-circle-1"></div>
      <div class="decoration-circle decoration-circle-2"></div>
    </div>
  </div>
</template>

<script>
import { parseTime } from '@/utils/ruoyi'

export default {
  name: "ExamCard",
  props: {
    // 接收完整的考试对象作为prop
    exam: {
      type: Object,
      required: true
    }
  },
  computed: {
    // 计算属性：按钮文本
    buttonText() {
      return this.getActionButtonText(this.exam);
    },
    // 计算属性：按钮是否可操作
    buttonEnabled() {
      return this.canPerformAction(this.exam);
    },
    // 计算属性：按钮类型
    buttonType() {
      return this.getActionButtonType(this.exam);
    },
    // 计算属性：按钮图标
    buttonIcon() {
      return this.getActionButtonIcon(this.exam);
    }
  },
  watch: {
    exam: {
      handler(newVal) {
        // 监听exam数据变化，确保组件能正确响应数据更新
      },
      deep: true,
      immediate: true
    }
  },
  methods: {
    // 当用户点击主要操作按钮时，向父组件发出一个事件
    handleExamAction() {
      this.$emit('exam-action', this.exam);
    },
    // 当用户点击查看详情按钮时，向父组件发出一个事件
    handleViewDetail() {
      this.$emit('view-detail', this.exam);
    },
    // 解析时间格式
    parseTime,
    // 获取考试卡片样式类
    getExamCardClass(exam) {
      const classes = ['exam-card-item'];
      
      // 根据考试状态添加不同的样式类
      if (exam.status === '1') {
        classes.push('exam-published');
      } else if (exam.status === '2') {
        classes.push('exam-running');
      } else if (exam.status === '3') {
        classes.push('exam-ended');
      }
      
      // 根据是否可以操作添加样式类
      if (!this.canPerformAction(exam)) {
        classes.push('exam-disabled');
      }
      
      return classes.join(' ');
    },
    // 获取考试状态类型
    getExamStatusType(status) {
      const statusMap = {
        '1': 'success',  // 已发布
        '2': 'warning',  // 进行中
        '3': 'info'      // 已结束
      };
      return statusMap[status] || 'info';
    },
    // 获取考试状态文本
    getExamStatusText(status) {
      const statusMap = {
        '1': '已发布',
        '2': '进行中',
        '3': '已结束'
      };
      return statusMap[status] || '未知状态';
    },
    // 获取状态图标
    getStatusIcon(status) {
      const iconMap = {
        '1': 'el-icon-circle-check',
        '2': 'el-icon-time',
        '3': 'el-icon-circle-close'
      };
      return iconMap[status] || 'el-icon-info';
    },
    // 获取时间状态
    getTimeStatus(exam) {
      if (!exam.startTime || !exam.endTime) return '';
      
      const now = new Date();
      const startTime = new Date(exam.startTime);
      const endTime = new Date(exam.endTime);
      
      if (now < startTime) {
        const diff = startTime - now;
        const days = Math.floor(diff / (1000 * 60 * 60 * 24));
        const hours = Math.floor((diff % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60));
        const minutes = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60));
        
        if (days > 0) return `${days}天后开始`;
        if (hours > 0) return `${hours}小时后开始`;
        if (minutes > 0) return `${minutes}分钟后开始`;
        return '即将开始';
      } else if (now <= endTime) {
        const diff = endTime - now;
        const days = Math.floor(diff / (1000 * 60 * 60 * 24));
        const hours = Math.floor((diff % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60));
        const minutes = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60));
        
        if (days > 0) return `${days}天后结束`;
        if (hours > 0) return `${hours}小时后结束`;
        if (minutes > 0) return `${minutes}分钟后结束`;
        return '即将结束';
      } else {
        return '考试已结束';
      }
    },
    // 获取时间状态样式类
    getTimeStatusClass(exam) {
      if (!exam.startTime || !exam.endTime) return '';
      
      const now = new Date();
      const startTime = new Date(exam.startTime);
      const endTime = new Date(exam.endTime);
      
      if (now < startTime) {
        return 'status-waiting';
      } else if (now <= endTime) {
        const diff = endTime - now;
        const minutes = Math.floor(diff / (1000 * 60));
        if (minutes <= 30) return 'status-urgent';
        return 'status-running';
      } else {
        return 'status-ended';
      }
    },
    // 获取时间状态图标
    getTimeStatusIcon(exam) {
      if (!exam.startTime || !exam.endTime) return '';
      
      const now = new Date();
      const startTime = new Date(exam.startTime);
      const endTime = new Date(exam.endTime);
      
      if (now < startTime) {
        return 'el-icon-time';
      } else if (now <= endTime) {
        return 'el-icon-loading';
      } else {
        return 'el-icon-check';
      }
    },
    // 获取操作按钮类型
    getActionButtonType(exam) {
      const buttonText = this.getActionButtonText(exam);
      
      if (buttonText === '开始考试' || buttonText === '继续考试') {
        return 'primary';
      } else if (buttonText === '查看成绩') {
        return 'success';
      } else if (buttonText === '报名') {
        return 'warning';
      } else if (buttonText === '待审核' || buttonText === '报名被拒') {
        return 'warning';
      } else {
        return 'info';
      }
    },
    // 获取操作按钮文本
    getActionButtonText(exam) {
      const now = new Date();
      const startTime = new Date(exam.startTime);
      const endTime = new Date(exam.endTime);

      if (exam.status === '4') {
        return '已取消';
      }
      if (exam.status === '3') {
        if (exam.examUser && exam.examUser.examStatus === 'graded') {
          return '查看成绩';
        }
        return '已结束';
      }
      
      // 考试已结束
      if (now > endTime) {
        if (exam.examUser && exam.examUser.examStatus === 'graded') {
          return '查看成绩';
        }
        return '已结束';
      }
      
      // 考试未开始
      if (now < startTime) {
        if (exam.registrationRequired === '1' && !exam.examUser) {
          return '报名';
        }
        return '未开始';
      }
      
      // 考试进行中
      if (exam.examUser) {
        if (exam.examUser.registrationStatus === 'registered') {
          return '待审核';
        } else if (exam.examUser.registrationStatus === 'rejected') {
          return '报名被拒';
        } else if (exam.examUser.examStatus === 'in_progress') {
          return '继续考试';
        } else if (exam.examUser.examStatus === 'graded') {
          return '查看成绩';
        } else if (exam.examUser.attemptCount >= exam.maxAttempts) {
          return '已达上限';
        }
      }
      
      // 需要报名但未报名
      if (exam.registrationRequired === '1' && !exam.examUser) {
        return '报名';
      }
      
      return '开始考试';
    },
    // 获取操作按钮图标
    getActionButtonIcon(exam) {
      const buttonText = this.getActionButtonText(exam);
      
      if (buttonText === '开始考试') {
        return 'el-icon-video-play';
      } else if (buttonText === '继续考试') {
        return 'el-icon-refresh';
      } else if (buttonText === '查看成绩') {
        return 'el-icon-tickets';
      } else if (buttonText === '报名') {
        return 'el-icon-user-solid';
      } else if (buttonText === '待审核') {
        return 'el-icon-time';
      } else if (buttonText === '报名被拒') {
        return 'el-icon-circle-close';
      } else if (buttonText === '已取消') {
        return 'el-icon-circle-close';
      } else {
        return 'el-icon-info';
      }
    },
    // 判断是否可以执行操作
    canPerformAction(exam) {
      const buttonText = this.getActionButtonText(exam);
      
      // 展示型状态不能操作。
      if (['未开始', '已结束', '已取消', '已达上限', '待审核', '报名被拒'].includes(buttonText)) {
        return false;
      }
      
      return true;
    }
  }
}
</script>

<style scoped>
/* 考试卡片样式 */
.exam-card {
  position: relative;
  background: #ffffff;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
  overflow: hidden;
  border: 1px solid #f0f0f0;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.exam-card:hover {
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
  transform: translateY(-2px);
}

.exam-card-item {
  margin-bottom: 16px;
}

/* 卡片头部 */
.card-header {
  padding: 16px 16px 12px 16px;
  border-bottom: 1px solid #f5f5f5;
  background: linear-gradient(135deg, #f8f9fa 0%, #ffffff 100%);
}

.exam-title-section {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  margin-bottom: 8px;
}

.exam-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin: 0;
  flex: 1;
  margin-right: 12px;
  line-height: 1.4;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.status-tag {
  flex-shrink: 0;
  font-size: 11px;
  padding: 4px 8px;
  border-radius: 6px;
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 4px;
}

.status-tag i {
  font-size: 12px;
}

.exam-meta {
  display: flex;
  align-items: center;
  color: #606266;
  font-size: 12px;
}

.paper-name {
  background: #f0f2f5;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 11px;
  color: #606266;
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 卡片内容 */
.card-content {
  flex: 1;
  padding: 16px;
  display: flex;
  flex-direction: column;
}

/* 考试基本信息 */
.exam-basic-info {
  margin-bottom: 12px;
}

.info-row {
  display: flex;
  gap: 8px;
  margin-bottom: 8px;
}

.info-row:last-child {
  margin-bottom: 0;
}

.info-item {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  padding: 6px 8px;
  background: #f8f9fa;
  border-radius: 6px;
  border: 1px solid #e9ecef;
}

.info-item i {
  font-size: 14px;
  flex-shrink: 0;
}

.icon-primary { color: #409eff; }
.icon-success { color: #67c23a; }
.icon-warning { color: #e6a23c; }
.icon-info { color: #909399; }

.info-label {
  font-size: 12px;
  color: #909399;
  margin-right: 8px;
  min-width: 48px;
  flex-shrink: 0;
}

.info-value {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
  white-space: nowrap;
}

/* 时间信息 */
.exam-time-info {
  display: flex;
  align-items: center;
  background: linear-gradient(90deg, #f8f9fa 0%, #e9ecef 100%);
  border-radius: 8px;
  padding: 8px 12px;
  margin-bottom: 10px;
  border: 1px solid #e4e7ed;
}

.time-item {
  flex: 1;
  text-align: center;
}

.time-label {
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 11px;
  color: #909399;
  margin-bottom: 4px;
}

.time-label i {
  margin-right: 3px;
  font-size: 12px;
}

.time-value {
  font-size: 13px;
  font-weight: 500;
  color: #303133;
}

.time-divider {
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 8px;
  color: #c0c4cc;
}

/* 考试进度 */
.exam-progress {
  margin-bottom: 8px;
  flex-grow: 1;
  display: flex;
  align-items: flex-end;
}

.time-status {
  display: inline-flex;
  align-items: center;
  padding: 6px 12px;
  border-radius: 16px;
  font-size: 12px;
  font-weight: 500;
  border: 1px solid;
}

.time-status i {
  margin-right: 4px;
  font-size: 14px;
}

.status-waiting {
  background: #fff3cd;
  color: #856404;
  border-color: #ffeaa7;
}

.status-running {
  background: #d1ecf1;
  color: #0c5460;
  border-color: #bee5eb;
}

.status-urgent {
  background: #f8d7da;
  color: #721c24;
  border-color: #f5c6cb;
  animation: pulse 2s infinite;
}

.status-ended {
  background: #d4edda;
  color: #155724;
  border-color: #c3e6cb;
}

@keyframes pulse {
  0% { opacity: 1; }
  50% { opacity: 0.7; }
  100% { opacity: 1; }
}

/* 卡片操作 */
.card-actions {
  display: flex;
  gap: 8px;
  align-items: center;
  padding: 0 16px 16px 16px;
}

.action-btn {
  flex: 1;
  height: 36px;
  border-radius: 6px;
  font-weight: 500;
  font-size: 13px;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
}

.primary-btn {
  background: linear-gradient(135deg, #409eff 0%, #2593fc 100%);
  border: none;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
}

.primary-btn:hover {
  background: linear-gradient(135deg, #2593fc 0%, #1e7ce8 100%);
  box-shadow: 0 6px 16px rgba(64, 158, 255, 0.4);
  transform: translateY(-1px);
}

.primary-btn:disabled {
  background: #c0c4cc;
  box-shadow: none;
  cursor: not-allowed;
  transform: none;
}

.secondary-btn {
  border: 1px solid #dcdfe6;
  color: #606266;
  background: #ffffff;
}

.secondary-btn:hover {
  border-color: #409eff;
  color: #409eff;
  background: #ecf5ff;
}

.action-btn i {
  margin-right: 6px;
  font-size: 14px;
}

/* 卡片装饰元素 */
.card-decoration {
  position: absolute;
  top: 0;
  right: 0;
  pointer-events: none;
  z-index: 1;
}

.decoration-circle {
  position: absolute;
  border-radius: 50%;
  opacity: 0.1;
}

.decoration-circle-1 {
  width: 60px;
  height: 60px;
  background: linear-gradient(135deg, #409eff, #2593fc);
  top: -30px;
  right: -30px;
}

.decoration-circle-2 {
  width: 20px;
  height: 20px;
  background: linear-gradient(135deg, #67c23a, #85ce61);
  top: 10px;
  right: 10px;
}

/* 考试状态样式 */
.exam-published {
  border-left: 4px solid #67c23a;
}

.exam-running {
  border-left: 4px solid #e6a23c;
}

.exam-ended {
  border-left: 4px solid #909399;
}

.exam-disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.exam-disabled:hover {
  transform: none;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}
</style>
