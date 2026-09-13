<template>
  <div class="student-dashboard">
    <!-- 顶部横幅 -->
    <div class="dashboard-banner">
      <div class="banner-container">
        <div class="banner-content">
          <h1>欢迎来到考试大厅</h1>
          <p>书山有路勤为径，学海无涯苦作舟  - 《古今贤文·劝学篇》</p>
        </div>
        <div class="banner-stats">
          <div class="stat-item">
            <div class="stat-number">{{ stats.examCount }}</div>
            <div class="stat-label">考试场数</div>
          </div>
          <div class="stat-item">
            <div class="stat-number">{{ stats.questionCount }}</div>
            <div class="stat-label">试题总数</div>
          </div>
          <div class="stat-item">
            <div class="stat-number">{{ stats.userCount }}</div>
            <div class="stat-label">用户总数</div>
          </div>
        </div>
      </div>
    </div>

    <!-- 最新公告 -->
    <div class="notices-section">
      <div class="container">
        <div class="section-header">
          <h2><i class="el-icon-bell"></i> 最新公告</h2>
          <div class="header-extra">
            <span v-if="unreadCount > 0" class="unread-badge">{{ unreadCount }}条未读</span>
            <el-button
              type="text"
              size="small"
              @click="refreshNotices"
              :loading="noticesLoading"
              icon="el-icon-refresh"
            >
              刷新
            </el-button>
          </div>
        </div>

        <div class="notices-content" v-loading="noticesLoading">
          <div v-if="notices.length > 0" class="notices-grid">
            <div
              v-for="notice in notices"
              :key="notice.noticeId"
              class="notice-card"
              :class="{ 'notice-unread': !notice.isRead }"
              @click="viewNoticeDetail(notice)"
            >
              <div class="notice-header">
                <div class="notice-type">
                  <i :class="getNoticeIcon(notice.noticeType)"
                     :style="{ color: getNoticeColor(notice.noticeType) }"></i>
                  <span class="type-text">{{ getNoticeTypeText(notice.noticeType) }}</span>
                </div>
                <div class="notice-status">
                  <span v-if="!notice.isRead" class="unread-dot"></span>
                  <span class="notice-time">{{ formatNoticeTime(notice.createTime) }}</span>
                </div>
              </div>

              <div class="notice-content">
                <div class="notice-title" :title="notice.noticeTitle">
                  {{ notice.noticeTitle }}
                </div>
                <div class="notice-preview" v-if="notice.noticeContent">
                  {{ getNoticePreview(notice.noticeContent) }}
                </div>
              </div>

              <div class="notice-footer">
                <span class="notice-author">{{ notice.createBy }}</span>
                <el-button
                  type="text"
                  size="mini"
                  @click.stop="markAsRead(notice)"
                  v-if="!notice.isRead"
                >
                  标记已读
                </el-button>
              </div>
            </div>
          </div>

          <div v-else-if="!noticesLoading" class="empty-state">
            <i class="el-icon-info"></i>
            <p>暂无公告信息</p>
          </div>
        </div>
      </div>
    </div>

    <!-- 我的考试 -->
    <div class="my-exams-section">
      <div class="container">
        <div class="section-tabs">
          <el-tabs v-model="activeTab" @tab-click="handleTabClick">
            <el-tab-pane label="考试大厅" name="available">
              <div class="exam-grid" v-if="availableExams.length > 0">
                <exam-card
                  v-for="exam in availableExams"
                  :key="exam.id"
                  :exam="exam"
                  @exam-action="handleExamAction"
                  @view-detail="handleViewDetail"
                />
              </div>

              <div v-else class="empty-state">
                <i class="el-icon-document"></i>
                <p>暂无可参加的考试</p>
                <p class="empty-desc">请联系管理员发布考试</p>
              </div>
            </el-tab-pane>

            <el-tab-pane label="我的考试" name="records">
              <div class="exam-records" v-if="examRecords.length > 0">
                <div
                  v-for="record in examRecords"
                  :key="record.id"
                  class="record-card"
                >
                  <div class="record-info">
                    <div class="record-title">{{ record.examName }}</div>
                    <div class="record-meta">
                      <span><i class="el-icon-time"></i> 提交于 {{ formatTime(record.submitTime) }}</span>
                      <span>
                        <i :class="record.isPassed === '1' ? 'el-icon-circle-check' : 'el-icon-circle-close'"></i>
                        {{ record.isPassed === '1' ? '通过' : '未通过' }}
                      </span>
                    </div>
                  </div>
                  <div class="record-status">
                    <div class="record-score" :class="getScoreClass(record)">
                      {{ record.totalScore || 0 }} 分
                    </div>
                    <el-button
                      size="small"
                      type="primary"
                      plain
                      @click="handleViewResult(record)"
                      v-if="record.examStatus === 'graded'"
                    >
                      查看解析
                    </el-button>
                  </div>
                </div>
              </div>

              <div v-else class="empty-state">
                <i class="el-icon-tickets"></i>
                <p>暂无考试记录</p>
              </div>
            </el-tab-pane>
          </el-tabs>
        </div>
      </div>
    </div>

    <!-- 公告详情弹窗 -->
    <el-dialog
      title="公告详情"
      :visible.sync="noticeDetailVisible"
      width="800px"
      :before-close="closeNoticeDetail"
    >
      <div v-if="currentNotice" class="notice-detail">
        <div class="detail-header">
          <h3>{{ currentNotice.noticeTitle }}</h3>
          <div class="detail-meta">
            <el-tag
              :type="currentNotice.noticeType === '1' ? 'primary' : 'warning'"
              size="small"
            >
              {{ getNoticeTypeText(currentNotice.noticeType) }}
            </el-tag>
            <span class="detail-author">发布者：{{ currentNotice.createBy }}</span>
            <span class="detail-time">{{ formatTime(currentNotice.createTime) }}</span>
          </div>
        </div>

        <div class="detail-content" v-html="sanitizeContent(currentNotice.noticeContent)">
        </div>
      </div>

      <span slot="footer" class="dialog-footer">
        <el-button
          @click="closeNoticeDetail"
          v-if="currentNotice && !currentNotice.isRead"
        >
          关 闭
        </el-button>
        <el-button
          type="primary"
          @click="markAsReadAndClose"
          v-if="currentNotice && currentNotice.isRead"
        >
          标记已读并关闭
        </el-button>
      </span>
    </el-dialog>

    <!-- 考试详情对话框 -->
    <el-dialog title="考试详情" :visible.sync="detailDialogVisible" width="900px" append-to-body>
      <div v-if="examDetail" class="exam-detail-container">
        <!-- 考试基本信息 -->
        <el-card shadow="never" class="detail-card">
          <div slot="header" class="detail-card-header">
            <i class="el-icon-info"></i>
            <span>考试信息</span>
          </div>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="考试名称">{{ examDetail.examName }}</el-descriptions-item>
            <el-descriptions-item label="考试分类">{{ examDetail.categoryName || '无分类' }}</el-descriptions-item>
            <el-descriptions-item label="考试类型">
              {{ examDetail.examType === 'normal' ? '正式考试' : '练习考试' }}
            </el-descriptions-item>
            <el-descriptions-item label="考试模式">
              {{ examDetail.examMode === 'online' ? '在线考试' : '线下考试' }}
            </el-descriptions-item>
            <el-descriptions-item label="考试时长">{{ examDetail.duration }} 分钟</el-descriptions-item>
            <el-descriptions-item label="最大考试次数">{{ examDetail.maxAttempts }} 次</el-descriptions-item>
            <el-descriptions-item label="开始时间" :span="2">
              {{ parseTime(examDetail.startTime, '{y}-{m}-{d} {h}:{i}:{s}') }}
            </el-descriptions-item>
            <el-descriptions-item label="结束时间" :span="2">
              {{ parseTime(examDetail.endTime, '{y}-{m}-{d} {h}:{i}:{s}') }}
            </el-descriptions-item>
          </el-descriptions>

          <div v-if="examDetail.examDescription" class="exam-description">
            <h4><i class="el-icon-document"></i> 考试说明</h4>
            <p>{{ examDetail.examDescription }}</p>
          </div>
        </el-card>

        <!-- 试卷信息 -->
        <el-card shadow="never" class="detail-card" v-if="examDetail.paper">
          <div slot="header" class="detail-card-header">
            <i class="el-icon-notebook-1"></i>
            <span>试卷信息</span>
          </div>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="试卷名称">{{ examDetail.paper.paperName }}</el-descriptions-item>
            <el-descriptions-item label="试卷类型">
              {{ getPaperTypeText(examDetail.paper.paperType) }}
            </el-descriptions-item>
            <el-descriptions-item label="总分">{{ examDetail.paper.totalScore }} 分</el-descriptions-item>
            <el-descriptions-item label="及格分数">{{ examDetail.paper.passScore }} 分</el-descriptions-item>
            <el-descriptions-item label="题目数量">{{ examDetail.paper.questionCount }} 题</el-descriptions-item>
            <el-descriptions-item label="难度等级">
              {{ getDifficultyText(examDetail.paper.difficultyLevel) }}
            </el-descriptions-item>
          </el-descriptions>

          <!-- 题型分布 -->
          <div v-if="paperStatistics && paperStatistics.typeCount" class="paper-statistics">
            <h4><i class="el-icon-pie-chart"></i> 题型分布</h4>
            <el-row :gutter="20">
              <el-col :span="12">
                <div class="stat-chart">
                  <h5>题目数量分布</h5>
                  <div class="type-stats">
                    <div v-for="(count, type) in paperStatistics.typeCount" :key="type" class="type-stat-item">
                      <span class="type-name">{{ type }}</span>
                      <el-progress :percentage="Math.round((count / paperStatistics.totalCount) * 100)" :format="() => `${count}题`"></el-progress>
                    </div>
                  </div>
                </div>
              </el-col>
              <el-col :span="12">
                <div class="stat-chart">
                  <h5>分值分布</h5>
                  <div class="type-stats">
                    <div v-for="(score, type) in paperStatistics.typeScore" :key="type" class="type-stat-item">
                      <span class="type-name">{{ type }}</span>
                      <el-progress
                        :percentage="Math.round((score / paperStatistics.totalScore) * 100)"
                        :format="() => `${score}分`"
                        color="#67c23a">
                      </el-progress>
                    </div>
                  </div>
                </div>
              </el-col>
            </el-row>
          </div>

          <div v-if="examDetail.paper.paperDescription" class="paper-description">
            <h4><i class="el-icon-document"></i> 试卷说明</h4>
            <p>{{ examDetail.paper.paperDescription }}</p>
          </div>
        </el-card>

        <!-- 报名信息 -->
        <el-card shadow="never" class="detail-card" v-if="examDetail.registrationRequired === '1'">
          <div slot="header" class="detail-card-header">
            <i class="el-icon-user"></i>
            <span>报名信息</span>
          </div>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="是否需要报名">
              {{ examDetail.registrationRequired === '1' ? '需要报名' : '无需报名' }}
            </el-descriptions-item>
            <el-descriptions-item label="我的报名状态" v-if="examUser">
              <el-tag :type="getRegistrationStatusType(examUser.registrationStatus)">
                {{ getRegistrationStatusText(examUser.registrationStatus) }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="报名开始时间" v-if="examDetail.registrationStartTime">
              {{ parseTime(examDetail.registrationStartTime, '{y}-{m}-{d} {h}:{i}:{s}') }}
            </el-descriptions-item>
            <el-descriptions-item label="报名结束时间" v-if="examDetail.registrationEndTime">
              {{ parseTime(examDetail.registrationEndTime, '{y}-{m}-{d} {h}:{i}:{s}') }}
            </el-descriptions-item>
          </el-descriptions>
        </el-card>

        <!-- 考试统计 -->
        <el-card shadow="never" class="detail-card" v-if="examStatistics">
          <div slot="header" class="detail-card-header">
            <i class="el-icon-data-analysis"></i>
            <span>参与统计</span>
          </div>
          <el-row :gutter="20" class="exam-stats">
            <el-col :span="6">
              <div class="stat-item">
                <div class="stat-number">{{ examStatistics.registeredCount || 0 }}</div>
                <div class="stat-label">报名人数</div>
              </div>
            </el-col>
            <el-col :span="6">
              <div class="stat-item">
                <div class="stat-number">{{ examStatistics.submittedCount || 0 }}</div>
                <div class="stat-label">已提交</div>
              </div>
            </el-col>
            <el-col :span="6">
              <div class="stat-item">
                <div class="stat-number">{{ examStatistics.passedCount || 0 }}</div>
                <div class="stat-label">已通过</div>
              </div>
            </el-col>
            <el-col :span="6">
              <div class="stat-item">
                <div class="stat-number">
                  {{ examStatistics.submittedCount > 0 ?
                      Math.round((examStatistics.passedCount / examStatistics.submittedCount) * 100) : 0 }}%
                </div>
                <div class="stat-label">通过率</div>
              </div>
            </el-col>
          </el-row>
        </el-card>

        <!-- 历史记录 -->
        <el-card shadow="never" class="detail-card" v-if="historyRecords && historyRecords.length > 0">
          <div slot="header" class="detail-card-header">
            <i class="el-icon-time"></i>
            <span>我的考试记录</span>
          </div>
          <el-table :data="historyRecords" stripe>
            <el-table-column label="考试次数" align="center" width="100">
              <template slot-scope="scope">
                第{{ scope.row.attemptNumber || scope.row.attemptCount || 1 }}次
              </template>
            </el-table-column>
            <el-table-column label="开始时间" align="center" width="180">
              <template slot-scope="scope">
                {{ scope.row.startTime ? parseTime(scope.row.startTime, '{y}-{m}-{d} {h}:{i}') : '-' }}
              </template>
            </el-table-column>
            <el-table-column label="提交时间" align="center" width="180">
              <template slot-scope="scope">
                {{ scope.row.submitTime ? parseTime(scope.row.submitTime, '{y}-{m}-{d} {h}:{i}') : '-' }}
              </template>
            </el-table-column>
            <el-table-column label="考试状态" align="center" width="120">
              <template slot-scope="scope">
                <el-tag :type="getExamStatusType(scope.row.examStatus)">
                  {{ getExamStatusText(scope.row.examStatus) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="得分" align="center" width="100">
              <template slot-scope="scope">
                {{ scope.row.totalScore || '-' }}
              </template>
            </el-table-column>
            <el-table-column label="是否通过" align="center" width="100">
              <template slot-scope="scope">
                <el-tag :type="scope.row.isPassed === '1' ? 'success' : 'danger'" v-if="scope.row.isPassed">
                  {{ scope.row.isPassed === '1' ? '通过' : '未通过' }}
                </el-tag>
                <span v-else>-</span>
              </template>
            </el-table-column>
          </el-table>
        </el-card>

        <!-- 考试规则 -->
        <el-card shadow="never" class="detail-card">
          <div slot="header" class="detail-card-header">
            <i class="el-icon-warning"></i>
            <span>考试规则</span>
          </div>
          <div class="exam-rules">
            <ul>
              <li><i class="el-icon-check"></i> 考试时间为 {{ examDetail.duration }} 分钟，请合理安排答题时间</li>
              <li><i class="el-icon-check"></i> 考试期间请保持网络连接稳定，避免掉线影响考试</li>
              <li><i class="el-icon-check"></i> 考试开始后不可暂停，请确保在安静的环境中进行</li>
              <li><i class="el-icon-check"></i> 每题作答后系统会自动保存，请按照题目要求认真作答</li>
              <li><i class="el-icon-check"></i> 最多可参加 {{ examDetail.maxAttempts }} 次考试，请珍惜考试机会</li>
              <li v-if="examDetail.shuffleQuestions === '1'"><i class="el-icon-check"></i> 考试题目顺序将会打乱</li>
              <li v-if="examDetail.shuffleOptions === '1'"><i class="el-icon-check"></i> 选择题选项顺序将会打乱</li>
              <li><i class="el-icon-check"></i> 考试结束后系统将自动提交答卷并进行评分</li>
            </ul>
          </div>
        </el-card>
      </div>

      <div slot="footer" class="dialog-footer">
        <el-button @click="detailDialogVisible = false">关 闭</el-button>
        <el-button
          :type="getActionButtonType(examDetail)"
          @click="handleStartFromDetail"
          v-if="examDetail && canPerformAction(examDetail)"
        >
          <i :class="getActionButtonIcon(examDetail)"></i>
          {{ getActionButtonText(examDetail) }}
        </el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getAvailableExaminations, getExamRecords, registerExam, startExam, getExamDetail } from "@/api/student/exam"
import { listStudentNotice, getNoticeDetail, markNoticeAsRead, getUnreadNoticeCount } from "@/api/student/notice"
import { getDashboardStatistics } from '@/api/student/exam'
import { parseTime } from '@/utils'
import { sanitizeRichText } from '@/utils/sanitize'
import { mapGetters } from 'vuex'
import ExamCard from '../components/ExamCard'

export default {
  name: 'StudentDashboard',
  dicts: ['exam_status'],
  components: { ExamCard },

  computed: {
    ...mapGetters(['permissions', 'roles'])
  },

  data() {
    return {
      activeTab: 'available',
      stats: {
        examCount: 0,
        questionCount: 0,
        userCount: 0
      },
      availableExams: [],
      examRecords: [],
      loading: false,
      // 通知公告相关数据
      notices: [],
      noticesLoading: false,
      unreadCount: 0,
      noticeDetailVisible: false,
      currentNotice: null,
      // 考试详情相关数据
      examDetail: null,
      examUser: null,
      paperStatistics: null,
      historyRecords: [],
      examStatistics: null,
      detailDialogVisible: false,
      detailLoading: false
    }
  },

  created() {
    this.getAvailableExams()
    this.getExamRecords()
    this.getNotices()
    this.getUnreadCount()
    this.getStats()
  },

  methods: {
    sanitizeContent(content) {
      return sanitizeRichText(content)
    },
    // 获取统计数据
    async getStats() {
      try {
        const response = await getDashboardStatistics()
        this.stats = response.data
      } catch (error) {
        console.error('获取统计数据失败：', error)
        // 使用默认值
        this.stats = {
          examCount: 0,
          questionCount: 0,
          userCount: 0
        }
      }
    },

    async getAvailableExams() {
      try {
        this.loading = true
        const response = await getAvailableExaminations({
          pageNum: 1,
          pageSize: 6
        })

        // 处理考试数据，确保与ExamCard组件兼容
        const examList = response.rows || []
        const currentUserId = this.$store.getters.id

        this.availableExams = examList.map(exam => {
          // 从 examUsers 中获取当前用户的报名信息
          let examUser = null
          if (exam.examUsers && exam.examUsers.length > 0) {
            // 查找当前用户的数据，处理可能的类型不匹配问题
            examUser = exam.examUsers.find(user => {
              // 尝试多种匹配方式确保能找到正确的用户
              return user.userId === currentUserId ||
                     user.userId == currentUserId ||
                     String(user.userId) === String(currentUserId) ||
                     Number(user.userId) === Number(currentUserId)
            })
          }

          return {
            ...exam,
            examUser: examUser
          }
        })
      } catch (error) {
        console.error('获取考试列表失败：', error)
        this.$message.error('获取考试列表失败')
      } finally {
        this.loading = false
      }
    },

    async getExamRecords() {
      try {
        const response = await getExamRecords({
          pageNum: 1,
          pageSize: 10
        })
        this.examRecords = response.rows || []
      } catch (error) {
        console.error('获取考试记录失败：', error)
      }
    },

    handleTabClick(tab) {
      if (tab.name === 'records' && this.examRecords.length === 0) {
        this.getExamRecords()
      }
    },

    /** 考试操作 */
    handleExamAction(exam) {
      const now = new Date();
      const startTime = new Date(exam.startTime);
      const endTime = new Date(exam.endTime);

      // 考试已结束
      if (now > endTime) {
        if (exam.examUser && exam.examUser.examStatus === 'graded') {
          this.handleViewResult(exam.examUser);
        } else {
          this.$message.warning("考试已结束");
        }
        return;
      }

      // 考试未开始
      if (now < startTime) {
        if (exam.registrationRequired === '1' && (!exam.examUser || exam.examUser.registrationStatus !== 'approved')) {
          this.handleRegister(exam);
        } else {
          this.$message.warning("考试尚未开始");
        }
        return;
      }

      // 考试进行中
      const currentAttemptCount = exam.examUser ? (exam.examUser.attemptCount || 0) : 0;
      const maxAttempts = exam.maxAttempts || Infinity; // 默认无限次

      if (exam.examUser) {
        if (exam.examUser.examStatus === 'in_progress') {
          this.handleContinueExam({ examId: exam.id, id: exam.examUser.id });
          return;
        } else if (exam.examUser.examStatus === 'graded') {
          this.handleViewResult(exam.examUser);
          return;
        }
      }

      // 检查是否已达到最大考试次数限制
      if (currentAttemptCount >= maxAttempts) {
        this.$message.warning("已达到最大考试次数限制");
        return;
      }

      // 如果需要报名但未报名或未通过审核
      if (exam.registrationRequired === '1' && (!exam.examUser || exam.examUser.registrationStatus !== 'approved')) {
        this.handleRegister(exam);
        return;
      }

      // 默认情况：开始考试
      this.handleStartExam(exam);
    },

    /** 报名考试 */
    handleRegister(exam) {
      this.$confirm('确认报名参加"' + exam.examName + '"考试吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        return registerExam(exam.id);
      }).then(() => {
        this.$message.success("报名成功");
        this.getAvailableExams();
      }).catch(() => {});
    },

    /** 开始考试 */
    handleStartExam(exam) {
      if (!exam.id) {
        this.$message.error("考试ID无效，请刷新页面重试");
        return;
      }

      this.$confirm('确认开始考试"' + exam.examName + '"吗？开始后将计算考试时间。', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        return startExam(exam.id);
      }).then((res) => {
        this.$message.success("考试已开始");
        this.$router.push({
          path: '/student/exam-detail/taking/' + exam.id,
          query: res && res.examUserId ? { examUserId: res.examUserId } : {}
        });
      }).catch(() => {});
    },

    /** 继续考试 */
    handleContinueExam(record) {
      const examId = record.examId || record.id;
      this.$router.push({
        path: '/student/exam-detail/taking/' + examId,
        query: record.id ? { examUserId: record.id } : {}
      });
    },

    /** 查看成绩 */
    handleViewResult(record) {
      // 传递考试记录ID（record.id）而不是考试ID
      this.$router.push(`/student/exam-detail/result/${record.id}`);
    },

    handleViewDetail(exam) {
      this.detailLoading = true;
      this.detailDialogVisible = true;

      getExamDetail(exam.id).then(response => {
        // 后端返回数据直接在response根级别，不是response.data
        this.examDetail = response.examination;
        this.examUser = response.examUser;
        this.paperStatistics = response.paperStatistics;
        this.historyRecords = response.historyRecords || [];
        this.examStatistics = response.examStatistics;
      }).catch(error => {
        console.error('获取考试详情失败:', error);
        this.$message.error("获取考试详情失败：" + (error.msg || error.message || "网络错误"));
        this.detailDialogVisible = false;
      }).finally(() => {
        this.detailLoading = false;
      });
    },

    canTakeExam(exam) {
      const now = new Date()
      const startTime = new Date(exam.startTime)
      const endTime = new Date(exam.endTime)

      // 检查时间范围
      const timeValid = now >= startTime && now <= endTime

      // 检查状态：支持已发布(1)和进行中(2)状态
      const statusValid = exam.status === '1' || exam.status === '2'

      return timeValid && statusValid
    },



    getExamButtonText(exam) {
      if (this.canTakeExam(exam)) {
        return exam.status === '2' ? '继续考试' : '开始考试'
      }
      return '查看详情'
    },

    getScoreClass(record) {
      const score = record.totalScore || 0
      const passScore = record.passScore || 60
      return score >= passScore ? 'score-pass' : 'score-fail'
    },

    formatExamTime(exam) {
      const start = this.parseTime(exam.startTime, '{m}-{d} {h}:{i}')
      const end = this.parseTime(exam.endTime, '{m}-{d} {h}:{i}')
      return `${start} ~ ${end}`
    },

    formatTime(time) {
      return this.parseTime(time, '{y}-{m}-{d} {h}:{i}')
    },

    // 通知公告相关方法
    async getNotices() {
      try {
        this.noticesLoading = true
        const response = await listStudentNotice({
          pageNum: 1,
          pageSize: 5,
          status: '0' // 只获取正常状态的公告
        })
        this.notices = response.rows || []
      } catch (error) {
        console.error('获取公告列表失败：', error)
        this.$message.error('获取公告列表失败')
      } finally {
        this.noticesLoading = false
      }
    },

    async getUnreadCount() {
      try {
        const response = await getUnreadNoticeCount()
        this.unreadCount = response.data || 0
      } catch (error) {
        console.error('获取未读数量失败：', error)
      }
    },

    async refreshNotices() {
      await this.getNotices()
      await this.getUnreadCount()
      this.$message.success('公告列表已刷新')
    },

    async viewNoticeDetail(notice) {
      try {
        const response = await getNoticeDetail(notice.noticeId)
        this.currentNotice = response.data
        this.noticeDetailVisible = true

        // 如果是未读公告，自动标记为已读
        if (!notice.isRead) {
          await this.markAsRead(notice, false)
        }
      } catch (error) {
        console.error('获取公告详情失败：', error)
        // 详细的错误处理
        let errorMessage = '获取公告详情失败'
        if (error.response) {
          if (error.response.status === 403) {
            errorMessage = '无权查看此公告'
          } else if (error.response.status === 404) {
            errorMessage = '公告不存在或已被删除'
          } else if (error.response.data && error.response.data.msg) {
            errorMessage = error.response.data.msg
          }
        } else if (error.message) {
          errorMessage = '网络请求失败，请检查网络连接'
        }
        this.$message.error(errorMessage)
      }
    },

    async markAsRead(notice, showMessage = true) {
      try {
        // 防止重复请求
        if (notice.isRead) {
          if (showMessage) {
            this.$message.info('该公告已标记为已读')
          }
          return
        }

        await markNoticeAsRead(notice.noticeId)

        // 更新本地状态
        const index = this.notices.findIndex(n => n.noticeId === notice.noticeId)
        if (index !== -1) {
          this.notices[index].isRead = true
        }

        // 更新当前查看的公告状态
        if (this.currentNotice && this.currentNotice.noticeId === notice.noticeId) {
          this.currentNotice.isRead = true
        }

        // 更新未读数量
        if (this.unreadCount > 0) {
          this.unreadCount--
        }

        if (showMessage) {
          this.$message.success('已标记为已读')
        }
      } catch (error) {
        console.error('标记已读失败：', error)
        // 详细的错误处理
        let errorMessage = '标记已读失败'
        if (error.response) {
          if (error.response.status === 403) {
            errorMessage = '无权限操作此公告'
          } else if (error.response.status === 404) {
            errorMessage = '公告不存在'
          } else if (error.response.data && error.response.data.msg) {
            errorMessage = error.response.data.msg
          }
        } else if (error.message) {
          errorMessage = '网络请求失败，请检查网络连接'
        }
        this.$message.error(errorMessage)
      }
    },

    async markAsReadAndClose() {
      if (this.currentNotice) {
        await this.markAsRead(this.currentNotice, false)
        this.closeNoticeDetail()
        this.$message.success('已标记为已读')
      }
    },

    closeNoticeDetail() {
      this.noticeDetailVisible = false
      this.currentNotice = null
    },

    getNoticeIcon(type) {
      return type === '1' ? 'el-icon-message' : 'el-icon-warning'
    },

    getNoticeColor(type) {
      return type === '1' ? '#409EFF' : '#E6A23C'
    },

    getNoticeTypeText(type) {
      return type === '1' ? '通知' : '公告'
    },

    formatNoticeTime(time) {
      const now = new Date()
      const noticeTime = new Date(time)
      const diff = now - noticeTime
      const days = Math.floor(diff / (1000 * 60 * 60 * 24))

      if (days === 0) {
        const hours = Math.floor(diff / (1000 * 60 * 60))
        if (hours === 0) {
          const minutes = Math.floor(diff / (1000 * 60))
          return `${minutes}分钟前`
        }
        return `${hours}小时前`
      } else if (days < 7) {
        return `${days}天前`
      } else {
        return this.parseTime(time, '{m}-{d}')
      }
    },

    getNoticePreview(content) {
      if (!content) return ''
      // 去除HTML标签，截取前100个字符
      const textContent = content.replace(/<[^>]*>/g, '')
      return textContent.length > 100 ? textContent.substring(0, 100) + '...' : textContent
    },

    // 考试详情相关方法
    /** 从详情页开始考试 */
    handleStartFromDetail() {
      this.detailDialogVisible = false;
      this.handleExamAction(this.examDetail);
    },

    /** 获取试卷类型文本 */
    getPaperTypeText(paperType) {
      const typeMap = {
        'fixed': '固定试卷',
        'random': '随机试卷',
        'mixed': '混合试卷'
      };
      return typeMap[paperType] || '未知类型';
    },

    /** 获取难度等级文本 */
    getDifficultyText(difficultyLevel) {
      const levelMap = {
        1: '简单',
        2: '普通',
        3: '困难'
      };
      return levelMap[difficultyLevel] || '未知难度';
    },

    /** 获取报名状态类型 */
    getRegistrationStatusType(status) {
      const typeMap = {
        'pending': 'warning',
        'approved': 'success',
        'rejected': 'danger'
      };
      return typeMap[status] || 'info';
    },

    /** 获取报名状态文本 */
    getRegistrationStatusText(status) {
      const statusMap = {
        'pending': '审核中',
        'approved': '已通过',
        'rejected': '已拒绝'
      };
      return statusMap[status] || '未知状态';
    },

    /** 获取考试状态类型 */
    getExamStatusType(status) {
      const typeMap = {
        'registered': 'info',
        'not_started': 'info',
        'in_progress': 'warning',
        'submitted': 'primary',
        'graded': 'success'
      };
      return typeMap[status] || 'info';
    },

    /** 获取考试状态文本 */
    getExamStatusText(status) {
      const statusMap = {
        'registered': '已报名',
        'not_started': '未开始',
        'in_progress': '进行中',
        'submitted': '已提交',
        'graded': '已评分'
      };
      return statusMap[status] || '未知状态';
    },

    /** 获取操作按钮类型 */
    getActionButtonType(exam) {
      const buttonText = this.getActionButtonText(exam);

      if (buttonText === '开始考试' || buttonText === '继续考试') {
        return 'primary';
      } else if (buttonText === '查看成绩') {
        return 'success';
      } else if (buttonText === '报名') {
        return 'warning';
      } else {
        return 'info';
      }
    },

    /** 获取操作按钮文本 */
    getActionButtonText(exam) {
      const now = new Date();
      const startTime = new Date(exam.startTime);
      const endTime = new Date(exam.endTime);
      const currentAttemptCount = exam.examUser ? (exam.examUser.attemptCount || 0) : 0;
      const maxAttempts = exam.maxAttempts || Infinity; // 默认无限次

      // 考试已结束
      if (now > endTime) {
        if (exam.examUser && exam.examUser.examStatus === 'graded') {
          return '查看成绩';
        }
        return '已结束';
      }

      // 考试未开始
      if (now < startTime) {
        if (exam.registrationRequired === '1' && (!exam.examUser || exam.examUser.registrationStatus !== 'approved')) {
          return '报名';
        }
        return '未开始';
      }

      // 检查是否已达到最大考试次数限制
      if (currentAttemptCount >= maxAttempts) {
        return '已达上限';
      }

      // 考试进行中
      if (exam.examUser) {
        if (exam.examUser.examStatus === 'in_progress') {
          return '继续考试';
        } else if (exam.examUser.examStatus === 'graded') {
          return '查看成绩';
        }
      }

      // 需要报名但未报名或未通过审核
      if (exam.registrationRequired === '1' && (!exam.examUser || exam.examUser.registrationStatus !== 'approved')) {
        return '报名';
      }

      return '开始考试';
    },

    /** 获取操作按钮图标 */
    getActionButtonIcon(exam) {
      const buttonText = this.getActionButtonText(exam);

      if (buttonText === '开始考试') {
        return 'el-icon-caret-right';
      } else if (buttonText === '继续考试') {
        return 'el-icon-refresh';
      } else if (buttonText === '查看成绩') {
        return 'el-icon-view';
      } else if (buttonText === '报名') {
        return 'el-icon-edit';
      } else if (buttonText === '已结束') {
        return 'el-icon-circle-close';
      } else if (buttonText === '已达上限') {
        return 'el-icon-warning';
      } else if (buttonText === '未开始') {
        return 'el-icon-time';
      } else {
        return 'el-icon-info';
      }
    },

    /** 判断是否可以执行操作 */
    canPerformAction(exam) {
      const buttonText = this.getActionButtonText(exam);

      // 这些状态的按钮是可以点击的
      const clickableActions = ['开始考试', '继续考试', '查看成绩', '报名'];
      return clickableActions.includes(buttonText);
    }
  }
}
</script>

<style lang="scss" scoped>
.student-dashboard {
  background-color: #f7f8fc;
  min-height: 100vh;
}

// 动画
@keyframes move-dots {
  0% { transform: translate(0, 0); }
  100% { transform: translate(40px, 40px); }
}

@keyframes card-hover {
  0% { transform: translateY(0); box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1); }
  100% { transform: translateY(-8px); box-shadow: 0 12px 40px rgba(0, 0, 0, 0.2); }
}

.dashboard-banner {
  background: linear-gradient(135deg, #4A90E2 0%, #50E3C2 100%);
  color: white;
  padding: 80px 0;
  position: relative;
  overflow: hidden;

  &::before {
    content: '';
    position: absolute;
    top: -50%;
    left: -50%;
    width: 200%;
    height: 200%;
    background-image: radial-gradient(circle, rgba(255, 255, 255, 0.08) 1px, transparent 1.5px);
    background-size: 40px 40px;
    animation: move-dots 20s linear infinite;
  }

  .banner-container {
    max-width: 1200px;
    margin: 0 auto;
    padding: 0 20px;
    display: flex;
    justify-content: space-between;
    align-items: center;
    position: relative;
    z-index: 1;
  }

  .banner-content {
    h1 {
      font-size: 42px;
      margin-bottom: 16px;
      font-weight: 700;
      text-shadow: 0 2px 8px rgba(0,0,0,0.15);
    }
    p {
      font-size: 18px;
      opacity: 0.9;
      max-width: 450px;
    }
  }

  .banner-stats {
    display: flex;
    gap: 60px;
    .stat-item {
      text-align: center;
      .stat-number {
        font-size: 40px;
        font-weight: 700;
        line-height: 1;
        margin-bottom: 12px;
      }
      .stat-label {
        font-size: 14px;
        opacity: 0.85;
        text-transform: uppercase;
        letter-spacing: 1px;
      }
    }
  }
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

// 通知公告区域样式
.notices-section {
  padding: 60px 0;
  background: #f7f8fc;

  .section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 30px;

    h2 {
      font-size: 28px;
      font-weight: 600;
      color: #303133;
      margin: 0;

      i {
        margin-right: 12px;
        color: #409EFF;
      }
    }

    .header-extra {
      display: flex;
      align-items: center;
      gap: 15px;

      .unread-badge {
        background: linear-gradient(135deg, #ff6b6b, #ee5a52);
        color: white;
        padding: 4px 12px;
        border-radius: 12px;
        font-size: 12px;
        font-weight: 500;
      }
    }
  }

  .notices-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
    gap: 20px;
  }

  .notice-card {
    background: white;
    border: 1px solid #e4e7ed;
    border-radius: 12px;
    padding: 20px;
    cursor: pointer;
    transition: all 0.3s ease;
    position: relative;

    &:hover {
      transform: translateY(-3px);
      box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1);
      border-color: #409EFF;
    }

    &.notice-unread {
      border-left: 4px solid #409EFF;

      .notice-title {
        font-weight: 600;
      }
    }

    .notice-header {
      display: flex;
      justify-content: space-between;
      align-items: flex-start;
      margin-bottom: 12px;

      .notice-type {
        display: flex;
        align-items: center;
        gap: 6px;

        i {
          font-size: 16px;
        }

        .type-text {
          font-size: 12px;
          font-weight: 500;
          text-transform: uppercase;
        }
      }

      .notice-status {
        display: flex;
        align-items: center;
        gap: 8px;

        .unread-dot {
          width: 8px;
          height: 8px;
          background: #F56C6C;
          border-radius: 50%;
        }

        .notice-time {
          font-size: 12px;
          color: #909399;
        }
      }
    }

    .notice-content {
      margin-bottom: 15px;

      .notice-title {
        font-size: 16px;
        color: #303133;
        margin-bottom: 8px;
        line-height: 1.4;
        display: -webkit-box;
        -webkit-box-orient: vertical;
        -webkit-line-clamp: 2;
        overflow: hidden;
      }

      .notice-preview {
        font-size: 14px;
        color: #606266;
        line-height: 1.5;
        display: -webkit-box;
        -webkit-box-orient: vertical;
        -webkit-line-clamp: 2;
        overflow: hidden;
      }
    }

    .notice-footer {
      display: flex;
      justify-content: space-between;
      align-items: center;

      .notice-author {
        font-size: 12px;
        color: #909399;
      }
    }
  }
}

// 公告详情弹窗样式
.notice-detail {
  max-width: 100%;
  overflow: hidden;

  .detail-header {
    margin-bottom: 20px;
    padding-bottom: 20px;
    border-bottom: 1px solid #e4e7ed;

    h3 {
      margin: 0 0 15px 0;
      font-size: 20px;
      color: #303133;
      line-height: 1.4;
    }

    .detail-meta {
      display: flex;
      align-items: center;
      gap: 15px;
      flex-wrap: wrap;

      .detail-author, .detail-time {
        font-size: 14px;
        color: #606266;
      }
    }
  }

  .detail-content {
    font-size: 15px;
    line-height: 1.6;
    color: #303133;
    word-wrap: break-word;
    overflow-wrap: break-word;
    max-width: 100%;
    overflow: hidden;

    /* 强制所有图片自适应容器宽度 */
    :deep(img) {
      max-width: 100% !important;
      width: auto !important;
      height: auto !important;
      border-radius: 8px;
      margin: 10px 0;
      display: block !important;
      object-fit: contain !important;
    }

    /* 确保图片在任何容器中都不会溢出 */
    :deep(.ql-editor img),
    :deep(.editor img),
    :deep(div img),
    :deep(p img) {
      max-width: 100% !important;
      width: auto !important;
      height: auto !important;
    }

    /* 确保所有内容都不会溢出 */
    :deep(*) {
      max-width: 100% !important;
      box-sizing: border-box;
    }

    /* 强制表格不溢出 */
    :deep(table) {
      max-width: 100% !important;
      table-layout: fixed !important;
      width: 100% !important;
    }

    /* 强制其他可能溢出的元素 */
    :deep(pre),
    :deep(code) {
      max-width: 100% !important;
      overflow-x: auto !important;
      word-wrap: break-word !important;
    }

    :deep(p) {
      margin-bottom: 15px;
    }

    :deep(ul), :deep(ol) {
      padding-left: 20px;
      margin-bottom: 15px;
    }
  }
}

/* 公告详情弹窗优化 */
::v-deep .el-dialog {
  /* 弹窗内的图片全局优化 */
  img {
    max-width: 100% !important;
    width: auto !important;
    height: auto !important;
    display: block;
    margin: 10px auto;
    border-radius: 8px;
  }

  /* 富文本编辑器输出的HTML内容优化 */
  .detail-content {
    img {
      max-width: 100% !important;
      width: auto !important;
      height: auto !important;
    }
  }
}

.my-exams-section {
  padding: 80px 0;
  background: #ffffff;
}

.exam-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(360px, 1fr));
  gap: 30px;
  margin-top: 20px;
}

.exam-records {
  margin-top: 20px;
}

.record-card {
  background: white;
  border: 1px solid #e4e7ed;
  border-radius: 12px;
  padding: 20px 24px;
  margin-bottom: 16px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  transition: all 0.3s ease;

  &:hover {
    box-shadow: 0 4px 15px rgba(0,0,0,0.08);
    border-color: #dcdfe6;
  }

  .record-info {
    flex-grow: 1;
    .record-title {
      font-size: 17px;
      font-weight: 600;
      color: #303133;
      margin-bottom: 10px;
    }
    .record-meta {
      display: flex;
      gap: 24px;
      font-size: 14px;
      color: #909399;
      span {
        display: flex;
        align-items: center;
        i {
          margin-right: 6px;
        }
      }
    }
  }

  .record-status {
    text-align: right;
    margin-left: 20px;
    .record-score {
      font-size: 22px;
      font-weight: bold;
      margin-bottom: 10px;
      &.score-pass { color: #67c23a; }
      &.score-fail { color: #f56c6c; }
    }
  }
}

.empty-state {
  text-align: center;
  padding: 100px 20px;
  color: #909399;
  i {
    font-size: 72px;
    margin-bottom: 24px;
    color: #dce1e6;
  }
  p {
    font-size: 18px;
    margin: 8px 0;
    &.empty-desc {
      font-size: 14px;
      color: #c0c4cc;
    }
  }
}

// 响应式设计
@media (max-width: 992px) {
  .exam-grid {
    grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  }

  .notices-grid {
    grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  }
}

@media (max-width: 768px) {
  .dashboard-banner {
    padding: 50px 0;
    .banner-container {
      flex-direction: column;
      text-align: center;
      gap: 40px;
    }
    .banner-content h1 {
      font-size: 32px;
    }
    .banner-stats {
      gap: 20px;
      justify-content: center;
      .stat-item .stat-number {
        font-size: 30px;
      }
    }
  }

  .my-exams-section,
  .notices-section {
    padding: 50px 0;
  }

  .notices-section h2 {
    font-size: 24px;
  }

  .notices-grid {
    grid-template-columns: 1fr;
    gap: 15px;
  }

  .notices-section .section-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 15px;

    .header-extra {
      width: 100%;
      justify-content: space-between;
    }
  }

  .notice-card {
    padding: 15px;

    .notice-header {
      flex-direction: column;
      align-items: flex-start;
      gap: 8px;
    }
  }

  .exam-grid {
    grid-template-columns: 1fr;
  }

  .record-card {
    flex-direction: column;
    align-items: flex-start;
    gap: 15px;

    .record-status {
      width: 100%;
      text-align: left;
      margin-left: 0;
      display: flex;
      justify-content: space-between;
      align-items: center;
    }
  }

  .exam-detail-container {
    max-height: 60vh;
  }

  .paper-statistics .el-row {
    flex-direction: column;
  }

  .paper-statistics .el-col {
    margin-bottom: 20px;
  }

  .exam-stats .el-row {
    flex-direction: column;
  }

  .exam-stats .el-col {
    margin-bottom: 16px;
  }
}

/* 考试详情对话框样式 */
.exam-detail-container {
  max-height: 70vh;
  overflow-y: auto;
}

.detail-card {
  margin-bottom: 20px;
  border-radius: 8px;
}

.detail-card:last-child {
  margin-bottom: 0;
}

.detail-card-header {
  display: flex;
  align-items: center;
  font-weight: 600;
  color: #303133;
}

.detail-card-header i {
  margin-right: 8px;
  font-size: 16px;
  color: #409eff;
}

.exam-description,
.paper-description {
  margin-top: 20px;
  padding: 16px;
  background: #f8f9fa;
  border-radius: 6px;
  border-left: 4px solid #409eff;
}

.exam-description h4,
.paper-description h4 {
  margin: 0 0 12px 0;
  color: #303133;
  font-size: 14px;
  display: flex;
  align-items: center;
}

.exam-description h4 i,
.paper-description h4 i {
  margin-right: 6px;
  color: #409eff;
}

.exam-description p,
.paper-description p {
  margin: 0;
  color: #606266;
  line-height: 1.6;
}

.paper-statistics {
  margin-top: 20px;
}

.paper-statistics h4 {
  margin: 0 0 16px 0;
  color: #303133;
  font-size: 14px;
  display: flex;
  align-items: center;
}

.paper-statistics h4 i {
  margin-right: 6px;
  color: #409eff;
}

.stat-chart h5 {
  margin: 0 0 12px 0;
  color: #606266;
  font-size: 13px;
  font-weight: 500;
}

.type-stats {
  space-y: 12px;
}

.type-stat-item {
  margin-bottom: 12px;
}

.type-stat-item:last-child {
  margin-bottom: 0;
}

.type-name {
  display: inline-block;
  width: 60px;
  font-size: 12px;
  color: #606266;
  margin-bottom: 4px;
}

.exam-stats {
  padding: 20px 0;
}

.exam-stats .stat-item {
  text-align: center;
  padding: 20px;
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
  border-radius: 8px;
  transition: all 0.3s ease;
}

.exam-stats .stat-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.exam-stats .stat-number {
  font-size: 28px;
  font-weight: 700;
  color: #409eff;
  margin-bottom: 8px;
}

.exam-stats .stat-label {
  font-size: 14px;
  color: #606266;
  font-weight: 500;
}

.exam-rules ul {
  margin: 0;
  padding: 0;
  list-style: none;
}

.exam-rules li {
  display: flex;
  align-items: flex-start;
  margin-bottom: 12px;
  padding: 8px 0;
  color: #606266;
  line-height: 1.6;
}

.exam-rules li:last-child {
  margin-bottom: 0;
}

.exam-rules li i {
  margin-right: 8px;
  margin-top: 2px;
  color: #67c23a;
  font-size: 14px;
  flex-shrink: 0;
}

/* 对话框自定义滚动条 */
.exam-detail-container::-webkit-scrollbar {
  width: 6px;
}

.exam-detail-container::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

.exam-detail-container::-webkit-scrollbar-thumb {
  background: #c0c4cc;
  border-radius: 3px;
}

.exam-detail-container::-webkit-scrollbar-thumb:hover {
  background: #909399;
}
</style>
