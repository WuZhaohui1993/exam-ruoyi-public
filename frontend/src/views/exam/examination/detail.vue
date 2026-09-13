<template>
  <div class="app-container exam-detail-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <div class="exam-title-section">
          <h1 class="exam-name">{{ examInfo.examName }}</h1>
          <div class="exam-meta">
            <el-tag 
              :type="getStatusTagType(examInfo.status)" 
              size="medium"
            >
              {{ getStatusText(examInfo.status) }}
            </el-tag>
            <span class="exam-category">
              <i class="el-icon-folder-opened"></i>
              {{ examInfo.categoryName }}
            </span>
          </div>
        </div>
        <div class="header-actions">
          <el-button @click="handleBack" icon="el-icon-back">返回</el-button>
          <el-button type="primary" @click="handleRefresh" icon="el-icon-refresh">刷新</el-button>
        </div>
      </div>
    </div>

    <el-tabs v-model="activeTab" class="exam-detail-tabs">
      <!-- 基本信息 -->
      <el-tab-pane label="基本信息" name="basic">
        <div class="detail-content">
          <el-row :gutter="20">
            <!-- 考试基本信息 -->
            <el-col :span="12">
              <div class="info-card">
                <h3><i class="el-icon-document"></i> 考试信息</h3>
                <div class="info-items">
                  <div class="info-item">
                    <span class="label">试卷名称：</span>
                    <span class="value">{{ examInfo.paperName }}</span>
                  </div>
                  <div class="info-item">
                    <span class="label">考试时长：</span>
                    <span class="value">{{ examInfo.duration }} 分钟</span>
                  </div>
                  <div class="info-item">
                    <span class="label">及格分数：</span>
                    <span class="value">{{ examInfo.passScore }} 分</span>
                  </div>
                  <div class="info-item">
                    <span class="label">最大考试次数：</span>
                    <span class="value">{{ examInfo.maxAttempts || 1 }} 次</span>
                  </div>
                  <div class="info-item">
                    <span class="label">总分：</span>
                    <span class="value">{{ displayTotalScore }}</span>
                  </div>
                </div>
              </div>
            </el-col>

            <!-- 时间安排 -->
            <el-col :span="12">
              <div class="info-card">
                <h3><i class="el-icon-time"></i> 时间安排</h3>
                <div class="info-items">
                  <div class="info-item">
                    <span class="label">开始时间：</span>
                    <span class="value">{{ parseTime(examInfo.startTime, '{y}-{m}-{d} {h}:{i}') }}</span>
                  </div>
                  <div class="info-item">
                    <span class="label">结束时间：</span>
                    <span class="value">{{ parseTime(examInfo.endTime, '{y}-{m}-{d} {h}:{i}') }}</span>
                  </div>
                  <div class="info-item">
                    <span class="label">创建时间：</span>
                    <span class="value">{{ parseTime(examInfo.createTime, '{y}-{m}-{d} {h}:{i}') }}</span>
                  </div>
                  <div class="info-item">
                    <span class="label">创建人：</span>
                    <span class="value">{{ examInfo.creatorName || examInfo.createBy }}</span>
                  </div>
                </div>
              </div>
            </el-col>
          </el-row>

          <!-- 考试设置 -->
          <div class="info-card full-width">
            <h3><i class="el-icon-setting"></i> 考试设置</h3>
            <el-row :gutter="20">
              <el-col :span="8">
                <div class="setting-item">
                  <i class="el-icon-check setting-icon" :class="{ 'active': examInfo.autoGrade === '1' }"></i>
                  <div class="setting-content">
                    <div class="setting-title">自动评分</div>
                    <div class="setting-desc">{{ examInfo.autoGrade === '1' ? '已开启' : '已关闭' }}</div>
                  </div>
                </div>
              </el-col>
              <el-col :span="8">
                <div class="setting-item">
                  <i class="el-icon-view setting-icon" :class="{ 'active': examInfo.allowReview === '1' }"></i>
                  <div class="setting-content">
                    <div class="setting-title">允许查看试卷</div>
                    <div class="setting-desc">{{ examInfo.allowReview === '1' ? '允许' : '不允许' }}</div>
                  </div>
                </div>
              </el-col>
              <el-col :span="8">
                <div class="setting-item">
                  <i class="el-icon-refresh setting-icon" :class="{ 'active': examInfo.shuffleQuestions === '1' }"></i>
                  <div class="setting-content">
                    <div class="setting-title">打乱题目顺序</div>
                    <div class="setting-desc">{{ examInfo.shuffleQuestions === '1' ? '已开启' : '已关闭' }}</div>
                  </div>
                </div>
              </el-col>
            </el-row>
          </div>

          <!-- 考试说明 -->
          <div class="info-card full-width" v-if="examInfo.examDescription">
            <h3><i class="el-icon-info"></i> 考试说明</h3>
            <div class="exam-description">
              {{ examInfo.examDescription }}
            </div>
          </div>

          <!-- 备注 -->
          <div class="info-card full-width" v-if="examInfo.remark">
            <h3><i class="el-icon-edit-outline"></i> 备注</h3>
            <div class="exam-remark">
              {{ examInfo.remark }}
            </div>
          </div>
        </div>
      </el-tab-pane>

      <!-- 授权范围 -->
      <el-tab-pane label="授权范围" name="scope">
        <div class="detail-content">
          <el-row :gutter="20">
            <el-col :span="12">
              <div class="info-card">
                <h3><i class="el-icon-user"></i> 点名参考人员</h3>
                <div class="info-items">
                  <div class="info-item">
                    <span class="label">当前记录：</span>
                    <span class="value">{{ examInfo.registeredCount || 0 }} 条</span>
                  </div>
                  <div class="info-item">
                    <span class="label">说明：</span>
                    <span class="value">点名人员适合临时补充个别考生，人员维护请从列表进入“人员”。</span>
                  </div>
                </div>
              </div>
            </el-col>
            <el-col :span="12">
              <div class="info-card">
                <h3><i class="el-icon-office-building"></i> 授权部门</h3>
                <div class="info-items">
                  <div class="info-item">
                    <span class="label">部门数量：</span>
                    <span class="value">{{ examDeptCount }} 个</span>
                  </div>
                  <div class="info-item">
                    <span class="label">子部门：</span>
                    <span class="value">{{ examInfo.includeChildDept === '0' ? '仅所选部门' : '包含子部门' }}</span>
                  </div>
                  <div class="info-item">
                    <span class="label">说明：</span>
                    <span class="value">授权部门是长期权限范围，部门后续新增人员会自动具备考试权限。</span>
                  </div>
                </div>
              </div>
            </el-col>
          </el-row>

          <div class="info-card full-width">
            <h3><i class="el-icon-s-data"></i> 参考统计</h3>
            <el-row :gutter="16" class="summary-row">
              <el-col :span="6">
                <div class="summary-item">
                  <div class="summary-number">{{ examInfo.registeredCount || 0 }}</div>
                  <div class="summary-label">参考记录</div>
                </div>
              </el-col>
              <el-col :span="6">
                <div class="summary-item">
                  <div class="summary-number">{{ examInfo.submittedCount || 0 }}</div>
                  <div class="summary-label">已交卷</div>
                </div>
              </el-col>
              <el-col :span="6">
                <div class="summary-item">
                  <div class="summary-number">{{ examInfo.passedCount || 0 }}</div>
                  <div class="summary-label">已通过</div>
                </div>
              </el-col>
              <el-col :span="6">
                <div class="summary-item">
                  <div class="summary-number">{{ passRate }}%</div>
                  <div class="summary-label">通过率</div>
                </div>
              </el-col>
            </el-row>
          </div>
        </div>
      </el-tab-pane>

      <!-- 考试配置（简化后的高级设置） -->
      <el-tab-pane label="考试配置" name="advanced" v-if="hasAdvancedSettings">
        <div class="detail-content">
          <div class="info-card">
            <h3><i class="el-icon-setting"></i> 考试配置</h3>
            <el-row :gutter="20">
              <el-col :span="12">
                <div class="config-items">
                  <div class="config-item" v-if="examInfo.examMode">
                    <span class="label">考试模式：</span>
                    <span class="value">
                      {{ examInfo.examMode === 'online' ? '在线考试' : examInfo.examMode === 'offline' ? '线下考试' : '未知' }}
                    </span>
                  </div>
                  <div class="config-item" v-if="examInfo.examType">
                    <span class="label">考试类型：</span>
                    <span class="value">
                      {{ examInfo.examType === 'normal' ? '普通考试' : examInfo.examType === 'practice' ? '练习模式' : '未知' }}
                    </span>
                  </div>
                  <div class="config-item" v-if="examInfo.registrationRequired">
                    <span class="label">需要报名：</span>
                    <span class="value">{{ examInfo.registrationRequired === '1' ? '是' : '否' }}</span>
                  </div>
                  <div class="config-item" v-if="examInfo.resultPublishType">
                    <span class="label">成绩发布方式：</span>
                    <span class="value">
                      {{ examInfo.resultPublishType === 'auto' ? '自动发布' : '手动发布' }}
                    </span>
                  </div>
                </div>
              </el-col>
              <el-col :span="12">
                <div class="config-items">
                  <div class="config-item" v-if="examInfo.monitorMode">
                    <span class="label">监控模式：</span>
                    <span class="value">
                      {{ examInfo.monitorMode === 'normal' ? '普通' : '严格' }}
                    </span>
                  </div>
                  <div class="config-item" v-if="examInfo.antiCheat">
                    <span class="label">防作弊：</span>
                    <span class="value">{{ examInfo.antiCheat === '1' ? '开启' : '关闭' }}</span>
                  </div>
                  <div class="config-item" v-if="examInfo.shuffleOptions">
                    <span class="label">打乱选项顺序：</span>
                    <span class="value">{{ examInfo.shuffleOptions === '1' ? '是' : '否' }}</span>
                  </div>
                </div>
              </el-col>
            </el-row>
            
            <!-- 时间配置 -->
            <div v-if="examInfo.registrationRequired === '1'" style="margin-top: 20px;">
              <h4>报名时间配置</h4>
              <div class="time-config">
                <div class="config-item" v-if="examInfo.registrationStartTime">
                  <span class="label">报名开始时间：</span>
                  <span class="value">{{ parseTime(examInfo.registrationStartTime, '{y}-{m}-{d} {h}:{i}:{s}') }}</span>
                </div>
                <div class="config-item" v-if="examInfo.registrationEndTime">
                  <span class="label">报名结束时间：</span>
                  <span class="value">{{ parseTime(examInfo.registrationEndTime, '{y}-{m}-{d} {h}:{i}:{s}') }}</span>
                </div>
              </div>
            </div>
            
            <div v-if="examInfo.resultPublishTime" style="margin-top: 20px;">
              <h4>成绩发布配置</h4>
              <div class="config-item">
                <span class="label">成绩发布时间：</span>
                <span class="value">{{ parseTime(examInfo.resultPublishTime, '{y}-{m}-{d} {h}:{i}:{s}') }}</span>
              </div>
            </div>
          </div>
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script>
import { getExaminationDetails } from "@/api/exam/examination";
import { checkPermi } from "@/utils/permission";

export default {
  name: "ExaminationDetail",
  data() {
    return {
      // 考试ID
      examId: null,
      // 考试信息
      examInfo: {},
      // 当前选中的标签页
      activeTab: 'basic',
      // 加载状态
      loading: false
    };
  },
  computed: {
    // 判断是否有高级设置内容
    hasAdvancedSettings() {
      if (!this.examInfo) return false;
      return !!(
        this.examInfo.examMode ||
        this.examInfo.examType ||
        this.examInfo.registrationRequired ||
        this.examInfo.resultPublishType ||
        this.examInfo.monitorMode ||
        this.examInfo.antiCheat ||
        this.examInfo.shuffleOptions ||
        this.examInfo.registrationStartTime ||
        this.examInfo.registrationEndTime ||
        this.examInfo.resultPublishTime
      );
    },
    examDeptCount() {
      return this.examInfo && this.examInfo.examDeptIds ? this.examInfo.examDeptIds.length : 0;
    },
    displayTotalScore() {
      const score = this.examInfo.totalScore !== null && this.examInfo.totalScore !== undefined
        ? this.examInfo.totalScore
        : this.examInfo.paperTotalScore;
      return score !== null && score !== undefined ? `${score} 分` : '未配置';
    },
    passRate() {
      if (!this.examInfo || !this.examInfo.submittedCount) {
        return 0;
      }
      return ((this.examInfo.passedCount || 0) / this.examInfo.submittedCount * 100).toFixed(1);
    }
  },
  created() {
    // 检查是否有查看详情的权限
    if (!checkPermi(['exam:examination:detail'])) {
      this.$message.error('您没有权限访问考试详情页面');
      this.$router.go(-1);
      return;
    }
    
    // 从路由参数获取考试ID
    this.examId = parseInt(this.$route.params.id);
    if (this.examId) {
      this.getExamDetails();
    } else {
      this.$modal.msgError('考试ID参数缺失');
      this.handleBack();
    }
  },
  methods: {
    /** 获取考试详情 */
    async getExamDetails() {
      this.loading = true;
      try {
        const response = await getExaminationDetails(this.examId);
        this.examInfo = response.data;
        this.loading = false;
      } catch (error) {
        console.error('获取考试详情失败:', error);
        this.$modal.msgError('获取考试详情失败: ' + (error.message || '未知错误'));
        this.loading = false;
      }
    },
    
    /** 返回上一页 */
    handleBack() {
      this.$router.go(-1);
    },
    
    /** 刷新页面数据 */
    handleRefresh() {
      this.getExamDetails();
    },
    
    /** 获取考试状态标签类型 */
    getStatusTagType(status) {
      const statusMap = {
        '0': 'info',     // 草稿
        '1': 'success',  // 已发布
        '2': 'warning',  // 进行中
        '3': 'info',     // 已结束
        '4': 'danger'    // 已取消
      };
      return statusMap[status] || 'info';
    },
    
    /** 获取考试状态文本 */
    getStatusText(status) {
      const statusMap = {
        '0': '草稿',
        '1': '已发布',
        '2': '进行中',
        '3': '已结束',
        '4': '已取消'
      };
      return statusMap[status] || '未知';
    }
  }
};
</script>

<style scoped>
.exam-detail-container {
  padding: 20px;
}

/* 页面头部 */
.page-header {
  background: #fff;
  padding: 20px 24px;
  border-radius: 8px;
  margin-bottom: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  border: 1px solid #ebeef5;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.exam-title-section {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.exam-name {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
  color: #303133;
}

.exam-meta {
  display: flex;
  align-items: center;
  gap: 12px;
}

.exam-category {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #909399;
  font-size: 14px;
}

.exam-category i {
  color: #c0c4cc;
}

.header-actions {
  display: flex;
  gap: 10px;
}

/* 标签页样式 */
.exam-detail-tabs {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  border: 1px solid #ebeef5;
}

.exam-detail-tabs :deep(.el-tabs__header) {
  margin: 0 0 20px 0;
}

.exam-detail-tabs :deep(.el-tabs__nav) {
  border: none;
}

.exam-detail-tabs :deep(.el-tabs__item) {
  height: 40px;
  line-height: 40px;
  padding: 0 20px;
  font-weight: 500;
}

.exam-detail-tabs :deep(.el-tabs__active-bar) {
  background-color: #409eff;
}

/* 内容区域 */
.detail-content {
  padding: 0;
}

/* 信息卡片 */
.info-card {
  background: white;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  border: 1px solid #ebeef5;
}

.info-card.full-width {
  width: 100%;
}

.info-card h3 {
  margin: 0 0 16px 0;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  display: flex;
  align-items: center;
  gap: 8px;
}

.info-card h3 i {
  color: #409eff;
}

.info-items {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.info-item {
  display: flex;
  align-items: center;
  padding: 8px 0;
  border-bottom: 1px solid #f5f7fa;
}

.info-item:last-child {
  border-bottom: none;
}

.info-item .label {
  font-weight: 500;
  color: #606266;
  min-width: 120px;
  flex-shrink: 0;
}

.info-item .value {
  color: #303133;
  flex: 1;
}

/* 设置项样式 */
.setting-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  background: #f8f9fa;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.setting-item:hover {
  background: #e3f2fd;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.setting-icon {
  font-size: 20px;
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #e0e0e0;
  color: #757575;
  transition: all 0.3s ease;
}

.setting-icon.active {
  background: #4caf50;
  color: white;
}

.setting-content {
  flex: 1;
}

.setting-title {
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
}

.setting-desc {
  font-size: 12px;
  color: #909399;
}

.summary-row {
  margin-top: 4px;
}

.summary-item {
  padding: 16px;
  border: 1px solid #ebeef5;
  border-radius: 6px;
  background: #fafafa;
  text-align: center;
}

.summary-number {
  font-size: 26px;
  font-weight: 600;
  color: #303133;
  line-height: 1.2;
}

.summary-label {
  margin-top: 6px;
  font-size: 13px;
  color: #606266;
}

/* 配置项样式 */
.config-items {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.config-item {
  display: flex;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #f5f7fa;
}

.config-item:last-child {
  border-bottom: none;
}

.config-item .label {
  font-weight: 500;
  color: #606266;
  min-width: 140px;
  flex-shrink: 0;
}

.config-item .value {
  color: #303133;
  flex: 1;
}

/* 时间配置 */
.time-config {
  background: #f8f9fa;
  padding: 16px;
  border-radius: 8px;
  margin-top: 8px;
}

.time-config h4 {
  margin: 0 0 12px 0;
  font-size: 14px;
  color: #303133;
}

/* 考试说明和备注 */
.exam-description,
.exam-remark {
  background: #f8f9fa;
  padding: 16px;
  border-radius: 8px;
  color: #606266;
  line-height: 1.6;
  margin-top: 8px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .header-content {
    flex-direction: column;
    gap: 16px;
    align-items: flex-start;
  }
  
  .exam-meta {
    flex-wrap: wrap;
  }
  
  .summary-row {
    flex-direction: column;
  }

  .summary-row .el-col {
    margin-bottom: 16px;
  }
}
</style>
