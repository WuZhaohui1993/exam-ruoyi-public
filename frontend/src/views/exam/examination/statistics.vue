<template>
  <div class="app-container">
    <!-- 页面头部 -->
    <el-card shadow="never" class="header-card">
      <div class="header-content">
        <h2>
          <i class="el-icon-s-data"></i>
          考试统计
        </h2>
        <div class="exam-info">
          <span class="exam-name">{{ examInfo.examName }}</span>
          <el-tag :type="getStatusTagType(examInfo.status)">
            {{ getStatusText(examInfo.status) }}
          </el-tag>
        </div>
      </div>

    </el-card>

    <!-- 统计概览 -->
    <el-row :gutter="20" class="statistics-overview">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon registered">
              <i class="el-icon-user-solid"></i>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ examInfo.registeredCount || 0 }}</div>
              <div class="stat-label">报名人数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon submitted">
              <i class="el-icon-document"></i>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ examInfo.submittedCount || 0 }}</div>
              <div class="stat-label">已提交人数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon passed">
              <i class="el-icon-trophy"></i>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ examInfo.passedCount || 0 }}</div>
              <div class="stat-label">通过人数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon rate">
              <i class="el-icon-pie-chart"></i>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ passRate }}%</div>
              <div class="stat-label">通过率</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 参与人员列表 -->
    <el-row :gutter="20">
      <el-col :span="24">
        <el-card shadow="never">
          <div slot="header" class="card-header">
            <span>参与人员统计</span>
            <div class="header-actions">
              <el-input
                v-model="searchName"
                placeholder="搜索姓名或用户名"
                size="small"
                style="width: 200px"
              >
                <i slot="prefix" class="el-input__icon el-icon-search"></i>
              </el-input>
            </div>
          </div>
          
          <el-table :data="filteredExamUsers" stripe style="width: 100%">
            <el-table-column prop="userName" label="用户名" width="120"></el-table-column>
            <el-table-column prop="nickName" label="姓名" width="120"></el-table-column>
            <el-table-column prop="deptName" label="部门" width="150"></el-table-column>
            <el-table-column label="状态" width="100">
              <template slot-scope="scope">
                <el-tag :type="getUserStatusType(scope.row)">
                  {{ getUserStatusText(scope.row) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="totalScore" label="成绩" width="80">
              <template slot-scope="scope">
                <span v-if="scope.row.totalScore !== null && scope.row.totalScore !== undefined">
                  {{ scope.row.totalScore }}
                </span>
                <span v-else class="text-muted">-</span>
              </template>
            </el-table-column>
            <el-table-column prop="submitTime" label="提交时间" width="180">
              <template slot-scope="scope">
                <span v-if="scope.row.submitTime">
                  {{ formatDate(scope.row.submitTime) }}
                </span>
                <span v-else class="text-muted">-</span>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="150">
              <template slot-scope="scope">
                <el-button
                  size="mini"
                  type="text"
                  @click="viewUserResult(scope.row)"
                  v-if="scope.row.totalScore !== null && scope.row.totalScore !== undefined"
                >
                  查看详情
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import { getStatistics } from '@/api/exam/examination'

export default {
  name: 'ExamStatistics',
  data() {
    return {
      examInfo: {},
      searchName: '',
      loading: false
    }
  },
  computed: {
    passRate() {
      if (!this.examInfo.submittedCount || this.examInfo.submittedCount === 0) {
        return 0
      }
      return Math.round((this.examInfo.passedCount / this.examInfo.submittedCount) * 100)
    },
    filteredExamUsers() {
      if (!this.examInfo.examUsers) return []
      
      let filtered = this.examInfo.examUsers
      
      if (this.searchName) {
        filtered = filtered.filter(user => 
          (user.userName && user.userName.toLowerCase().includes(this.searchName.toLowerCase())) ||
          (user.nickName && user.nickName.toLowerCase().includes(this.searchName.toLowerCase()))
        )
      }
      
      return filtered
    }
  },
  created() {
    this.loadStatistics()
  },
  methods: {
    loadStatistics() {
      const examId = this.$route.query.examId
      if (!examId) {
        this.$message.error('缺少考试ID参数')
        return
      }
      
      this.loading = true
      getStatistics(examId).then(response => {
        this.examInfo = response.data
        this.loading = false
      }).catch(error => {
        this.loading = false
        this.$message.error('加载统计数据失败：' + (error.message || '未知错误'))
      })
    },
    
    viewUserResult(user) {
      this.$router.push({
        path: '/exam/examination/result',
        query: {
          examId: this.$route.query.examId,
          userId: user.userId,
          userName: user.userName,
          nickName: user.nickName
        }
      })
    },
    
    getUserStatusText(user) {
      if (user.totalScore !== null && user.totalScore !== undefined) {
        return user.totalScore >= this.examInfo.passScore ? '通过' : '未通过'
      } else if (user.submitTime) {
        return '已提交'
      } else {
        return '已报名'
      }
    },
    
    getUserStatusType(user) {
      if (user.totalScore !== null && user.totalScore !== undefined) {
        return user.totalScore >= this.examInfo.passScore ? 'success' : 'danger'
      } else if (user.submitTime) {
        return 'warning'
      } else {
        return 'info'
      }
    },
    
    getStatusText(status) {
      const statusMap = {
        '0': '草稿',
        '1': '已发布',
        '2': '进行中',
        '3': '已结束',
        '4': '已取消'
      }
      return statusMap[status] || '未知'
    },
    
    getStatusTagType(status) {
      const statusMap = {
        '0': 'info',
        '1': 'success',
        '2': 'warning',
        '3': 'info',
        '4': 'danger'
      }
      return statusMap[status] || 'info'
    },
    
    formatDate(date) {
      if (!date) return '-'
      return new Date(date).toLocaleString('zh-CN')
    }
  }
}
</script>

<style scoped>
.header-card {
  margin-bottom: 20px;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-content h2 {
  margin: 0;
  color: #303133;
}

.exam-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.exam-name {
  font-weight: 500;
  color: #606266;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.statistics-overview {
  margin-bottom: 20px;
}

.stat-card {
  cursor: pointer;
  transition: all 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-2px);
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 15px;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: white;
}

.stat-icon.registered {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.stat-icon.submitted {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.stat-icon.passed {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.stat-icon.rate {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
}

.stat-info {
  flex: 1;
}

.stat-number {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
  line-height: 1;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 5px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 500;
}

.header-actions {
  display: flex;
  align-items: center;
}

.text-muted {
  color: #C0C4CC;
}
</style>
