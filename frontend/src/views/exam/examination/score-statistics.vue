<template>
  <div class="app-container">
    <!-- 页面头部 -->
    <el-card shadow="never" class="header-card">
      <div class="header-content">
        <h2>
          <i class="el-icon-s-data"></i>
          成绩统计
        </h2>
        <div class="header-actions">
          <el-button type="primary" icon="el-icon-download" @click="handleExportExcel">导出Excel</el-button>
          <el-button type="success" icon="el-icon-document" @click="handleExportDetail">导出明细</el-button>
        </div>
      </div>
    </el-card>

    <!-- 筛选条件 -->
    <el-card shadow="never" class="filter-card">
      <el-form :model="filterParams" ref="filterForm" :inline="true" label-width="100px">
        <el-form-item label="选择考试" prop="examId">
          <el-select 
            v-model="filterParams.examId" 
            placeholder="请选择考试" 
            style="width: 300px"
            @change="handleExamChange"
            clearable
          >
            <el-option
              v-for="exam in examList"
              :key="exam.id"
              :label="exam.examName"
              :value="exam.id"
            >
              <span style="float: left">{{ exam.examName }}</span>
              <span style="float: right; color: #8492a6; font-size: 13px">{{ exam.categoryName }}</span>
            </el-option>
          </el-select>
        </el-form-item>
        
        <el-form-item label="分数段" prop="scoreRange">
          <el-select v-model="filterParams.scoreRange" placeholder="请选择分数段" style="width: 200px" @change="handleFilter">
            <el-option label="全部" value=""></el-option>
            <el-option label="90-100分" value="90-100"></el-option>
            <el-option label="80-89分" value="80-89"></el-option>
            <el-option label="70-79分" value="70-79"></el-option>
            <el-option label="60-69分" value="60-69"></el-option>
            <el-option label="60分以下" value="0-59"></el-option>
          </el-select>
        </el-form-item>
        
        <el-form-item label="通过状态" prop="passStatus">
          <el-select v-model="filterParams.passStatus" placeholder="请选择通过状态" style="width: 150px" @change="handleFilter">
            <el-option label="全部" value=""></el-option>
            <el-option label="通过" value="1"></el-option>
            <el-option label="未通过" value="0"></el-option>
          </el-select>
        </el-form-item>
        
        <el-form-item label="部门" prop="deptId">
          <el-select v-model="filterParams.deptId" placeholder="请选择部门" style="width: 200px" @change="handleFilter" clearable>
            <el-option label="全部" value=""></el-option>
            <el-option
              v-for="dept in deptList"
              :key="dept.deptId"
              :label="dept.deptName"
              :value="dept.deptId"
            ></el-option>
          </el-select>
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" icon="el-icon-search" @click="handleFilter">筛选</el-button>
          <el-button icon="el-icon-refresh" @click="resetFilter">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 统计概览 -->
    <el-row :gutter="20" class="statistics-overview" v-if="examInfo.id">
      <el-col :span="4">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon registered">
              <i class="el-icon-user-solid"></i>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ examInfo.registeredCount || 0 }}</div>
              <div class="stat-label">参考记录</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon submitted">
              <i class="el-icon-document"></i>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ examInfo.submittedCount || 0 }}</div>
              <div class="stat-label">已交卷</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon passed">
              <i class="el-icon-trophy"></i>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ examInfo.passedCount || 0 }}</div>
              <div class="stat-label">已通过</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="4">
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
      <el-col :span="4">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon average">
              <i class="el-icon-data-line"></i>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ averageScore }}</div>
              <div class="stat-label">平均分</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon highest">
              <i class="el-icon-star-on"></i>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ highestScore }}</div>
              <div class="stat-label">最高分</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表展示 -->
    <el-row :gutter="20" class="charts-section" v-if="examInfo.id">
      <el-col :span="12">
        <el-card shadow="never">
          <div slot="header">
            <span>成绩分布</span>
          </div>
          <div id="scoreDistributionChart" style="height: 300px;"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="never">
          <div slot="header">
            <span>交卷通过情况</span>
          </div>
          <div id="passRateChart" style="height: 300px;"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 部门统计 -->
    <el-row :gutter="20" class="dept-stats" v-if="examInfo.id && deptStatistics.length > 0">
      <el-col :span="24">
        <el-card shadow="never">
          <div slot="header">
            <span>部门统计</span>
          </div>
          <el-table :data="deptStatistics" stripe style="width: 100%">
            <el-table-column prop="deptName" label="部门" min-width="200"></el-table-column>
            <el-table-column prop="totalCount" label="参考记录" min-width="120" align="center"></el-table-column>
            <el-table-column prop="submittedCount" label="已交卷" min-width="120" align="center"></el-table-column>
            <el-table-column prop="passedCount" label="已通过" min-width="120" align="center"></el-table-column>
            <el-table-column label="通过率" min-width="120" align="center">
              <template slot-scope="scope">
                <span>{{ scope.row.submittedCount > 0 ? ((scope.row.passedCount / scope.row.submittedCount) * 100).toFixed(1) : 0 }}%</span>
              </template>
            </el-table-column>
            <el-table-column prop="averageScore" label="平均分" min-width="120" align="center">
              <template slot-scope="scope">
                <span>{{ scope.row.averageScore || '-' }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="highestScore" label="最高分" min-width="120" align="center">
              <template slot-scope="scope">
                <span>{{ scope.row.highestScore || '-' }}</span>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <!-- 成绩明细 -->
    <el-card shadow="never" class="detail-card" v-if="examInfo.id">
      <div slot="header" class="card-header">
        <span>成绩明细</span>
        <div class="header-actions">
          <el-input
            v-model="searchName"
            placeholder="搜索姓名或用户名"
            size="small"
            style="width: 200px; margin-right: 10px"
            @input="handleSearch"
          >
            <i slot="prefix" class="el-input__icon el-icon-search"></i>
          </el-input>
          <el-button size="small" type="primary" icon="el-icon-download" @click="handleExportSelected">导出选中</el-button>
        </div>
      </div>
      
      <el-table 
        :data="filteredExamUsers" 
        stripe 
        @selection-change="handleSelectionChange"
        v-loading="loading"
        style="width: 100%"
      >
        <el-table-column type="selection" width="55"></el-table-column>
        <el-table-column prop="userName" label="用户名" min-width="120"></el-table-column>
        <el-table-column prop="nickName" label="姓名" min-width="120"></el-table-column>
        <el-table-column prop="deptName" label="部门" min-width="150"></el-table-column>
        <el-table-column label="状态" min-width="100">
          <template slot-scope="scope">
            <el-tag :type="getUserStatusType(scope.row)">
              {{ getUserStatusText(scope.row) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="totalScore" label="成绩" min-width="80" sortable>
          <template slot-scope="scope">
            <span v-if="scope.row.totalScore !== null && scope.row.totalScore !== undefined" 
                  :class="getScoreClass(scope.row.totalScore)">
              {{ scope.row.totalScore }}
            </span>
            <span v-else class="text-muted">-</span>
          </template>
        </el-table-column>
        <el-table-column label="等级" min-width="80">
          <template slot-scope="scope">
            <el-tag v-if="scope.row.totalScore !== null" :type="getGradeType(scope.row.totalScore)" size="small">
              {{ getGrade(scope.row.totalScore) }}
            </el-tag>
            <span v-else class="text-muted">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="submitTime" label="提交时间" min-width="180">
          <template slot-scope="scope">
            <span v-if="scope.row.submitTime">
              {{ formatDate(scope.row.submitTime) }}
            </span>
            <span v-else class="text-muted">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="duration" label="用时" min-width="100">
          <template slot-scope="scope">
            <span v-if="scope.row.startTime && scope.row.submitTime">
              {{ formatDuration(scope.row.startTime, scope.row.submitTime) }}
            </span>
            <span v-else class="text-muted">-</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" min-width="150">
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
      
      <!-- 分页 -->
      <pagination
        v-show="total > 0"
        :total="total"
        :page.sync="queryParams.pageNum"
        :limit.sync="queryParams.pageSize"
        @pagination="handlePagination"
      />
    </el-card>
  </div>
</template>

<script>
import { listExamination, getStatistics, exportScoreStatistics, exportScoreDetails } from '@/api/exam/examination'
import { listDept } from '@/api/system/dept'
import * as echarts from 'echarts'

export default {
  name: 'ScoreStatistics',
  data() {
    return {
      loading: false,
      examList: [],
      deptList: [],
      examInfo: {},
      deptStatistics: [],
      selectedUsers: [],
      searchName: '',
      total: 0,
      queryParams: {
        pageNum: 1,
        pageSize: 20
      },
      filterParams: {
        examId: null,
        scoreRange: '',
        passStatus: '',
        deptId: ''
      },
      scoreDistributionChart: null,
      passRateChart: null
    }
  },
  computed: {
    passRate() {
      if (!this.examInfo.submittedCount || this.examInfo.submittedCount === 0) {
        return 0
      }
      return Math.round((this.examInfo.passedCount / this.examInfo.submittedCount) * 100)
    },
    averageScore() {
      if (!this.examInfo.examUsers || this.examInfo.examUsers.length === 0) {
        return 0
      }
      const validScores = this.examInfo.examUsers
        .filter(user => user.totalScore !== null && user.totalScore !== undefined)
        .map(user => user.totalScore)
      
      if (validScores.length === 0) return 0
      
      const sum = validScores.reduce((acc, score) => acc + score, 0)
      return Math.round(sum / validScores.length * 10) / 10
    },
    highestScore() {
      if (!this.examInfo.examUsers || this.examInfo.examUsers.length === 0) {
        return 0
      }
      const validScores = this.examInfo.examUsers
        .filter(user => user.totalScore !== null && user.totalScore !== undefined)
        .map(user => user.totalScore)
      
      return validScores.length > 0 ? Math.max(...validScores) : 0
    },
    filteredExamUsers() {
      if (!this.examInfo.examUsers) return []
      
      let filtered = [...this.examInfo.examUsers]
      
      // 按姓名搜索
      if (this.searchName) {
        filtered = filtered.filter(user => 
          (user.userName && user.userName.toLowerCase().includes(this.searchName.toLowerCase())) ||
          (user.nickName && user.nickName.toLowerCase().includes(this.searchName.toLowerCase()))
        )
      }
      
      // 按分数段筛选
      if (this.filterParams.scoreRange) {
        const [min, max] = this.filterParams.scoreRange.split('-').map(Number)
        filtered = filtered.filter(user => {
          if (user.totalScore === null || user.totalScore === undefined) return false
          return user.totalScore >= min && user.totalScore <= max
        })
      }
      
      // 按通过状态筛选
      if (this.filterParams.passStatus !== '') {
        filtered = filtered.filter(user => {
          if (user.totalScore === null || user.totalScore === undefined) return false
          const isPassed = user.totalScore >= (this.examInfo.passScore || 60) ? '1' : '0'
          return isPassed === this.filterParams.passStatus
        })
      }
      
      // 按部门筛选
      if (this.filterParams.deptId) {
        filtered = filtered.filter(user => user.deptId == this.filterParams.deptId)
      }
      
      this.total = filtered.length
      
      // 分页
      const start = (this.queryParams.pageNum - 1) * this.queryParams.pageSize
      const end = start + this.queryParams.pageSize
      return filtered.slice(start, end)
    }
  },
  created() {
    this.loadExamList()
    this.loadDeptList()
    
    // 如果URL中有examId参数，自动加载该考试的统计数据
    const examId = this.$route.query.examId
    if (examId) {
      this.filterParams.examId = parseInt(examId)
      this.loadStatistics()
    }
  },
  mounted() {
    this.$nextTick(() => {
      this.initCharts()
    })
  },
  beforeDestroy() {
    if (this.scoreDistributionChart) {
      this.scoreDistributionChart.dispose()
    }
    if (this.passRateChart) {
      this.passRateChart.dispose()
    }
  },
  methods: {
    // 加载考试列表
    loadExamList() {
      listExamination({ pageNum: 1, pageSize: 1000 }).then(response => {
        this.examList = response.rows || []
      })
    },
    
    // 加载部门列表
    loadDeptList() {
      listDept().then(response => {
        this.deptList = response.data || []
      })
    },
    
    // 考试选择变化
    handleExamChange() {
      if (this.filterParams.examId) {
        this.loadStatistics()
      } else {
        this.examInfo = {}
        this.deptStatistics = []
      }
    },
    
    // 加载统计数据
    loadStatistics() {
      if (!this.filterParams.examId) {
        this.$message.warning('请先选择考试')
        return
      }
      
      this.loading = true
      getStatistics(this.filterParams.examId).then(response => {
        this.examInfo = response.data
        this.calculateDeptStatistics()
        this.updateCharts()
        this.loading = false
      }).catch(error => {
        console.error('加载统计数据失败:', error)
        this.loading = false
        this.$message.error('加载统计数据失败：' + (error.message || '未知错误'))
      })
    },
    
    // 计算部门统计
    calculateDeptStatistics() {
      if (!this.examInfo.examUsers) return
      
      const deptMap = new Map()
      
      this.examInfo.examUsers.forEach(user => {
        const deptId = user.deptId
        const deptName = user.deptName || '未知部门'
        
        if (!deptMap.has(deptId)) {
          deptMap.set(deptId, {
            deptId,
            deptName,
            totalCount: 0,
            submittedCount: 0,
            passedCount: 0,
            totalScore: 0,
            validScoreCount: 0,
            highestScore: 0
          })
        }
        
        const deptStat = deptMap.get(deptId)
        deptStat.totalCount++
        
        if (this.isSubmittedUser(user)) {
          deptStat.submittedCount++
        }

        if (user.totalScore !== null && user.totalScore !== undefined) {
          deptStat.totalScore += user.totalScore
          deptStat.validScoreCount++
          deptStat.highestScore = Math.max(deptStat.highestScore, user.totalScore)
          
          if (user.totalScore >= (this.examInfo.passScore || 60)) {
            deptStat.passedCount++
          }
        }
      })
      
      this.deptStatistics = Array.from(deptMap.values()).map(dept => ({
        ...dept,
        averageScore: dept.validScoreCount > 0 ? Math.round(dept.totalScore / dept.validScoreCount * 10) / 10 : null
      }))
    },
    
    // 初始化图表
    initCharts() {
      const scoreDistributionEl = document.getElementById('scoreDistributionChart')
      const passRateEl = document.getElementById('passRateChart')
      
      
      if (scoreDistributionEl) {
        this.scoreDistributionChart = echarts.init(scoreDistributionEl)
      } else {
      }
      
      if (passRateEl) {
        this.passRateChart = echarts.init(passRateEl)
      } else {
      }
      
      // 监听窗口大小变化
      window.addEventListener('resize', () => {
        this.scoreDistributionChart?.resize()
        this.passRateChart?.resize()
      })
    },
    
    // 更新图表
    updateCharts() {
      this.$nextTick(() => {
        // 确保图表容器已经渲染后再初始化图表
        if (!this.scoreDistributionChart || !this.passRateChart) {
          this.initCharts()
        }
        this.updateScoreDistributionChart()
        this.updatePassRateChart()
      })
    },
    
    // 更新成绩分布图表
    updateScoreDistributionChart() {
      
      if (!this.scoreDistributionChart || !this.examInfo.examUsers) {
        return
      }
      
      const scoreRanges = {
        '90-100': 0,
        '80-89': 0,
        '70-79': 0,
        '60-69': 0,
        '0-59': 0
      }
      
      this.examInfo.examUsers.forEach(user => {
        if (user.totalScore !== null && user.totalScore !== undefined) {
          const score = user.totalScore
          if (score >= 90) scoreRanges['90-100']++
          else if (score >= 80) scoreRanges['80-89']++
          else if (score >= 70) scoreRanges['70-79']++
          else if (score >= 60) scoreRanges['60-69']++
          else scoreRanges['0-59']++
        }
      })
      
      
      const option = {
        title: {
          text: '成绩分布',
          left: 'center'
        },
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            type: 'shadow'
          }
        },
        xAxis: {
          type: 'category',
          data: Object.keys(scoreRanges)
        },
        yAxis: {
          type: 'value'
        },
        series: [{
          data: Object.values(scoreRanges),
          type: 'bar',
          itemStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: '#83bff6' },
              { offset: 0.5, color: '#188df0' },
              { offset: 1, color: '#188df0' }
            ])
          }
        }]
      }
      
      this.scoreDistributionChart.setOption(option)
    },
    
    // 更新通过率图表
    updatePassRateChart() {
      
      if (!this.passRateChart || !this.examInfo.examUsers) {
        return
      }
      
      const passedCount = this.examInfo.passedCount || 0
      const failedCount = Math.max((this.examInfo.submittedCount || 0) - passedCount, 0)
      
      
      const option = {
        title: {
          text: '交卷通过情况',
          left: 'center'
        },
        tooltip: {
          trigger: 'item',
          formatter: '{a} <br/>{b}: {c} ({d}%)'
        },
        legend: {
          orient: 'vertical',
          left: 'left'
        },
        series: [{
          name: '交卷通过情况',
          type: 'pie',
          radius: '50%',
          data: [
            { value: passedCount, name: '通过', itemStyle: { color: '#67C23A' } },
            { value: failedCount, name: '未通过', itemStyle: { color: '#F56C6C' } }
          ],
          emphasis: {
            itemStyle: {
              shadowBlur: 10,
              shadowOffsetX: 0,
              shadowColor: 'rgba(0, 0, 0, 0.5)'
            }
          }
        }]
      }
      
      this.passRateChart.setOption(option)
    },
    
    // 筛选
    handleFilter() {
      this.queryParams.pageNum = 1
    },
    
    // 重置筛选
    resetFilter() {
      this.filterParams = {
        examId: this.filterParams.examId, // 保留考试选择
        scoreRange: '',
        passStatus: '',
        deptId: ''
      }
      this.searchName = ''
      this.queryParams.pageNum = 1
    },
    
    // 搜索
    handleSearch() {
      this.queryParams.pageNum = 1
    },
    
    // 分页
    handlePagination() {
      // 分页逻辑在computed中处理
    },
    
    // 选择变化
    handleSelectionChange(selection) {
      this.selectedUsers = selection
    },
    
    // 查看用户成绩详情
    viewUserResult(user) {
      this.$router.push({
        path: '/exam/examination/result',
        query: {
          examId: this.filterParams.examId,
          userId: user.userId,
          userName: user.userName,
          nickName: user.nickName
        }
      })
    },
    
    // 导出Excel
    handleExportExcel() {
      if (!this.filterParams.examId) {
        this.$message.warning('请先选择考试')
        return
      }
      
      this.$modal.loading('正在导出，请稍候...')
      
      const params = {
        scoreRange: this.filterParams.scoreRange,
        passStatus: this.filterParams.passStatus,
        deptId: this.filterParams.deptId,
        searchName: this.searchName
      }
      
      exportScoreStatistics(this.filterParams.examId, params).then(response => {
        this.$download.saveAs(response, `考试成绩统计_${new Date().getTime()}.xlsx`)
        this.$modal.closeLoading()
      }).catch(error => {
        this.$modal.closeLoading()
        this.$message.error('导出失败：' + (error.message || '未知错误'))
      })
    },
    
    // 导出明细
    handleExportDetail() {
      if (!this.filterParams.examId) {
        this.$message.warning('请先选择考试')
        return
      }
      
      this.$modal.loading('正在导出详细成绩，请稍候...')
      
      const params = {
        scoreRange: this.filterParams.scoreRange,
        passStatus: this.filterParams.passStatus,
        deptId: this.filterParams.deptId,
        searchName: this.searchName,
        includeAnswerDetails: true
      }
      
      exportScoreDetails(this.filterParams.examId, params).then(response => {
        this.$download.saveAs(response, `考试成绩明细_${new Date().getTime()}.xlsx`)
        this.$modal.closeLoading()
      }).catch(error => {
        this.$modal.closeLoading()
        this.$message.error('导出失败：' + (error.message || '未知错误'))
      })
    },
    
    // 导出选中
    handleExportSelected() {
      if (this.selectedUsers.length === 0) {
        this.$message.warning('请先选择要导出的用户')
        return
      }
      
      this.$modal.loading('正在导出选中用户成绩，请稍候...')
      
      const params = {
        userIds: this.selectedUsers.map(user => user.userId),
        scoreRange: this.filterParams.scoreRange,
        passStatus: this.filterParams.passStatus,
        deptId: this.filterParams.deptId
      }
      
      exportScoreStatistics(this.filterParams.examId, params).then(response => {
        this.$download.saveAs(response, `选中用户成绩_${new Date().getTime()}.xlsx`)
        this.$modal.closeLoading()
      }).catch(error => {
        this.$modal.closeLoading()
        this.$message.error('导出失败：' + (error.message || '未知错误'))
      })
    },
    
    // 获取用户状态文本
    getUserStatusText(user) {
      if (user.totalScore !== null && user.totalScore !== undefined) {
        return user.totalScore >= (this.examInfo.passScore || 60) ? '通过' : '未通过'
      } else if (user.submitTime) {
        return '已交卷'
      } else {
        return '已报名'
      }
    },
    
    // 获取用户状态类型
    getUserStatusType(user) {
      if (user.totalScore !== null && user.totalScore !== undefined) {
        return user.totalScore >= (this.examInfo.passScore || 60) ? 'success' : 'danger'
      } else if (user.submitTime) {
        return 'warning'
      } else {
        return 'info'
      }
    },

    isSubmittedUser(user) {
      return user.examStatus === 'submitted' ||
        user.examStatus === 'graded' ||
        !!user.submitTime ||
        (user.totalScore !== null && user.totalScore !== undefined)
    },
    
    // 获取分数样式类
    getScoreClass(score) {
      if (score >= 90) return 'score-excellent'
      if (score >= 80) return 'score-good'
      if (score >= 70) return 'score-fair'
      if (score >= 60) return 'score-pass'
      return 'score-fail'
    },
    
    // 获取等级
    getGrade(score) {
      if (score >= 90) return '优秀'
      if (score >= 80) return '良好'
      if (score >= 70) return '中等'
      if (score >= 60) return '及格'
      return '不及格'
    },
    
    // 获取等级类型
    getGradeType(score) {
      if (score >= 90) return 'success'
      if (score >= 80) return 'primary'
      if (score >= 70) return 'warning'
      if (score >= 60) return 'info'
      return 'danger'
    },
    
    // 格式化日期
    formatDate(date) {
      if (!date) return '-'
      return new Date(date).toLocaleString('zh-CN')
    },
    
    // 格式化时长
    formatDuration(startTime, endTime) {
      if (!startTime || !endTime) return '-'
      
      const start = new Date(startTime)
      const end = new Date(endTime)
      const duration = Math.floor((end - start) / 1000 / 60) // 分钟
      
      if (duration < 60) {
        return `${duration}分钟`
      } else {
        const hours = Math.floor(duration / 60)
        const minutes = duration % 60
        return `${hours}小时${minutes}分钟`
      }
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

.header-actions {
  display: flex;
  gap: 10px;
}

.filter-card {
  margin-bottom: 20px;
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

.stat-icon.average {
  background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);
}

.stat-icon.highest {
  background: linear-gradient(135deg, #a8edea 0%, #fed6e3 100%);
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

.charts-section {
  margin-bottom: 20px;
}

.dept-stats {
  margin-bottom: 20px;
}

.detail-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 500;
}

.text-muted {
  color: #C0C4CC;
}

/* 分数颜色 */
.score-excellent {
  color: #67C23A;
  font-weight: bold;
}

.score-good {
  color: #409EFF;
  font-weight: bold;
}

.score-fair {
  color: #E6A23C;
  font-weight: bold;
}

.score-pass {
  color: #909399;
  font-weight: bold;
}

.score-fail {
  color: #F56C6C;
  font-weight: bold;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .statistics-overview .el-col {
    margin-bottom: 10px;
  }
}

@media (max-width: 768px) {
  .header-content {
    flex-direction: column;
    gap: 15px;
  }
  
  .charts-section .el-col {
    margin-bottom: 20px;
  }
}
</style>
