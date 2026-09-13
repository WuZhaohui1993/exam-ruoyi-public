<template>
  <div class="app-container">
    <!-- 搜索栏 -->
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="考试名称" prop="examName">
        <el-input
          v-model="queryParams.examName"
          placeholder="请输入考试名称"
          clearable
          style="width: 240px"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="考试状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="考试状态" clearable style="width: 240px">
          <el-option label="已发布" value="1" />
          <el-option label="进行中" value="2" />
          <el-option label="已结束" value="3" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 切换标签 -->
    <el-tabs v-model="activeTab" @tab-click="handleTabClick" class="mb8">
      <el-tab-pane label="考试列表" name="available"></el-tab-pane>
      <el-tab-pane label="我的考试记录" name="records"></el-tab-pane>
    </el-tabs>

    <!-- 考试列表 -->
    <div v-if="activeTab === 'available'">
      <!-- 加载状态 -->
      <div v-if="availableLoading" class="loading-container">
        <i class="el-icon-loading"></i>
        <span>正在加载考试列表...</span>
      </div>
      
      <!-- 数据为空提示 -->
      <div v-else-if="availableExamList.length === 0" class="empty-container">
        <i class="el-icon-document"></i>
        <p>暂无考试</p>
        <p class="text-muted">当前没有已发布的考试，请联系管理员</p>
      </div>
      
      <!-- 考试列表 -->
      <el-row :gutter="16" v-else class="exam-row">
        <el-col :xl="4" :lg="6" :md="8" :sm="12" :xs="24" v-for="exam in availableExamList" :key="exam.id" class="exam-col">
          <exam-card
            :exam="exam"
            @exam-action="handleExamAction"
            @view-detail="handleViewDetail"
          />
        </el-col>
      </el-row>
      
      <pagination
        v-show="availableTotal > 0"
        :total="availableTotal"
        :page.sync="queryParams.pageNum"
        :limit.sync="queryParams.pageSize"
        @pagination="getAvailableList"
      />
    </div>

    <!-- 我的考试记录 -->
    <div v-if="activeTab === 'records'">
      <!-- 考试记录筛选条件 -->
      <el-form :model="recordsQueryParams" ref="recordsQueryForm" size="small" :inline="true" v-show="showSearch" label-width="68px" style="margin-bottom: 16px;">
        <el-form-item label="考试状态" prop="examStatus">
          <el-select v-model="recordsQueryParams.examStatus" placeholder="考试状态" clearable style="width: 180px">
            <el-option label="已报名" value="registered" />
            <el-option label="未开始" value="not_started" />
            <el-option label="进行中" value="in_progress" />
            <el-option label="已提交" value="submitted" />
            <el-option label="已评分" value="graded" />
          </el-select>
        </el-form-item>
        <el-form-item label="是否通过" prop="isPassed">
          <el-select v-model="recordsQueryParams.isPassed" placeholder="是否通过" clearable style="width: 120px">
            <el-option label="通过" value="1" />
            <el-option label="未通过" value="0" />
          </el-select>
        </el-form-item>
        <el-form-item label="考试名称" prop="examName">
          <el-input
            v-model="recordsQueryParams.examName"
            placeholder="请输入考试名称"
            clearable
            style="width: 200px"
            @keyup.enter.native="handleRecordsQuery"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="el-icon-search" size="mini" @click="handleRecordsQuery">搜索</el-button>
          <el-button icon="el-icon-refresh" size="mini" @click="resetRecordsQuery">重置</el-button>
        </el-form-item>
      </el-form>
      
      <el-table v-loading="recordsLoading" :data="recordsList" :default-sort="recordsDefaultSort" @sort-change="handleRecordsSortChange">
        <el-table-column label="考试名称" prop="examName" />
        <el-table-column label="报名时间" align="center" width="180" prop="registrationTime" sortable="custom" :sort-orders="['descending', 'ascending']">
          <template slot-scope="scope">
            <span>{{ parseTime(scope.row.registrationTime, '{y}-{m}-{d} {h}:{i}') }}</span>
          </template>
        </el-table-column>
        <el-table-column label="考试状态" align="center" width="120">
          <template slot-scope="scope">
            <dict-tag :options="examStatusOptions" :value="scope.row.examStatus" />
          </template>
        </el-table-column>
        <el-table-column label="考试次数" align="center" width="100" prop="attemptNumber" sortable="custom" :sort-orders="['descending', 'ascending']">
          <template slot-scope="scope">
            <span>第{{ scope.row.attemptNumber || scope.row.attemptCount || 1 }}次</span>
          </template>
        </el-table-column>
        <el-table-column label="得分" align="center" width="100" prop="totalScore" sortable="custom" :sort-orders="['descending', 'ascending']">
          <template slot-scope="scope">
            <span v-if="scope.row.totalScore">{{ scope.row.totalScore }}</span>
            <span v-else>-</span>
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
        <el-table-column label="操作" align="center" width="200">
          <template slot-scope="scope">
            <el-button
              size="mini"
              type="text"
              @click="handleContinueExam(scope.row)"
              v-if="scope.row.examStatus === 'in_progress'"
            >继续考试</el-button>
            <el-button
              size="mini"
              type="text"
              @click="handleViewResult(scope.row)"
              v-if="scope.row.examStatus === 'graded'"
            >查看成绩</el-button>
            <el-button
              size="mini"
              type="text"
              @click="handleRetakeExam(scope.row)"
              v-if="canRetake(scope.row)"
            >重新考试</el-button>
          </template>
        </el-table-column>
      </el-table>

      <pagination
        v-show="recordsTotal > 0"
        :total="recordsTotal"
        :page.sync="recordsQueryParams.pageNum"
        :limit.sync="recordsQueryParams.pageSize"
        @pagination="getRecordsList"
      />
    </div>

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
import { getAvailableExaminations, getAllExaminations, getExamRecords, getExamDetail, registerExam, startExam } from "@/api/student/exam"
import ExamCard from '../components/ExamCard'

export default {
  name: "StudentExamList",
  components: { ExamCard },
  data() {
    return {
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 12,
        examName: undefined,
        status: undefined
      },
      recordsQueryParams: {
        pageNum: 1,
        pageSize: 10,
        examStatus: undefined,
        isPassed: undefined,
        examName: undefined,
        sortField: '',
        sortOrder: ''
      },
      // 显示搜索条件
      showSearch: true,
      // 当前激活标签页
      activeTab: 'available',
      // 可参加考试列表
      availableExamList: [],
      availableTotal: 0,
      availableLoading: false,
      // 考试记录列表
      recordsList: [],
      recordsTotal: 0,
      recordsLoading: false,
      // 考试详情
      examDetail: null,
      examUser: null,
      paperStatistics: null,
      historyRecords: [],
      examStatistics: null,
      detailDialogVisible: false,
      detailLoading: false,
      // 考试状态选项
      examStatusOptions: [
        { label: '已报名', value: 'registered', raw: { listClass: 'info' } },
        { label: '未开始', value: 'not_started', raw: { listClass: 'info' } },
        { label: '进行中', value: 'in_progress', raw: { listClass: 'warning' } },
        { label: '已提交', value: 'submitted', raw: { listClass: 'primary' } },
        { label: '已评分', value: 'graded', raw: { listClass: 'success' } }
      ],
      // 考试记录表格默认排序
      recordsDefaultSort: {
        prop: 'registrationTime',
        order: 'descending'
      }
    };
  },
  created() {
    this.getAvailableList();
  },
  methods: {
    /** 查询考试列表 */
    getAvailableList() {
      this.availableLoading = true;
      getAllExaminations(this.queryParams).then(response => {
        const examList = response.rows || [];
        
        // 处理考试数据，提取用户报名信息
        this.availableExamList = examList.map(exam => {
          // 从 examUsers 中获取当前用户的报名信息
          const examUser = exam.examUsers && exam.examUsers.length > 0 ? exam.examUsers[0] : null;
          return {
            ...exam,
            examUser: examUser
          };
        });
        

        
        this.availableTotal = response.total || 0;
        this.availableLoading = false;
      }).catch(error => {
        console.error('获取考试列表失败:', error);
        this.$modal.msgError("获取考试列表失败：" + (error.msg || error.message || "网络错误"));
        this.availableLoading = false;
        this.availableExamList = [];
        this.availableTotal = 0;
      });
    },
    /** 查询考试记录列表 */
    getRecordsList() {
      this.recordsLoading = true;
      getExamRecords(this.recordsQueryParams).then(response => {
        
        this.recordsList = response.rows || [];
        this.recordsTotal = response.total || 0;
        
        // 数据验证和调试
        if (this.recordsList.length > 0) {
          
          // 检查每条记录是否有有效的ID
          const invalidRecords = this.recordsList.filter(record => !record.id);
          if (invalidRecords.length > 0) {
            console.warn('发现没有ID的考试记录:', invalidRecords);
          }
        }
        
        this.recordsLoading = false;
      }).catch(error => {
        console.error('获取考试记录失败:', error);
        this.$modal.msgError("获取考试记录失败：" + (error.msg || error.message || "网络错误"));
        this.recordsLoading = false;
        this.recordsList = [];
        this.recordsTotal = 0;
      });
    },
    /** 搜索按钮操作 */
    handleQuery() {
      this.queryParams.pageNum = 1;
      this.getAvailableList();
    },
    /** 重置按钮操作 */
    resetQuery() {
      this.resetForm("queryForm");
      this.handleQuery();
    },
    /** 考试记录搜索按钮操作 */
    handleRecordsQuery() {
      this.recordsQueryParams.pageNum = 1;
      this.getRecordsList();
    },
    /** 考试记录重置按钮操作 */
    resetRecordsQuery() {
      this.resetForm("recordsQueryForm");
      this.recordsQueryParams = {
        pageNum: 1,
        pageSize: 10,
        examStatus: undefined,
        isPassed: undefined,
        examName: undefined,
        sortField: '',
        sortOrder: ''
      };
      this.handleRecordsQuery();
    },
    /** 标签页切换 */
    handleTabClick(tab) {
      if (tab.name === 'records' && this.recordsList.length === 0) {
        this.getRecordsList();
      }
    },
    /** 考试操作 */
    handleExamAction(exam) {
      const buttonText = this.getActionButtonText(exam);
      
      // 根据按钮文字决定操作
      if (buttonText === '开始考试') {
        this.handleStartExam(exam);
      } else if (buttonText === '继续考试') {
        this.handleContinueExam({ examId: exam.id, id: exam.examUser && exam.examUser.id });
      } else if (buttonText === '查看成绩') {
        // 需要传递考试记录ID，从examUser中获取
        if (exam.examUser && exam.examUser.id) {
          this.handleViewResult({ id: exam.examUser.id, examStatus: 'graded' });
        } else {
          this.$modal.msgError("无法获取考试记录信息，请刷新页面重试");
        }
      } else if (buttonText === '报名') {
        this.handleRegister(exam);
      } else {
        // 不可操作的状态
        const now = new Date();
        const startTime = new Date(exam.startTime);
        const endTime = new Date(exam.endTime);
        
        if (now > endTime) {
          this.$modal.msgWarning("考试已结束");
        } else if (now < startTime) {
          this.$modal.msgWarning("考试尚未开始");
        } else if (exam.examUser && exam.examUser.attemptCount >= exam.maxAttempts) {
          this.$modal.msgWarning("已达到最大考试次数限制");
        } else {
          this.$modal.msgWarning("当前状态不可操作");
        }
      }
    },
    /** 报名考试 */
    handleRegister(exam) {
      this.$modal.confirm('确认报名参加"' + exam.examName + '"考试吗？').then(() => {
        return registerExam(exam.id);
      }).then(() => {
        this.$modal.msgSuccess("报名成功");
        this.getAvailableList();
      }).catch(() => {});
    },
    /** 开始考试 */
    handleStartExam(exam) {
      if (!exam.id) {
        this.$modal.msgError("考试ID无效，请刷新页面重试");
        return;
      }
      
      this.$modal.confirm('确认开始考试"' + exam.examName + '"吗？开始后将计算考试时间。').then(() => {
        return startExam(exam.id);
      }).then((res) => {
        this.$modal.msgSuccess("考试已开始");
        this.$router.push({
          path: '/student/exam-detail/taking/' + exam.id,
          query: res && res.examUserId ? { examUserId: res.examUserId } : {}
        });
      }).catch(() => {});
    },
    /** 查看详情 */
    handleViewDetail(exam) {
      this.detailLoading = true;
      this.detailDialogVisible = true;
      
      getExamDetail(exam.id).then(response => {
        // 后端返回数据直接在response根级别，不是response.data
          this.examUser = response.examUser;
          this.examDetail = {
            ...response.examination,
            examUser: response.examUser
          };
        this.paperStatistics = response.paperStatistics;
        this.historyRecords = response.historyRecords || [];
        this.examStatistics = response.examStatistics;
      }).catch(error => {
        console.error('获取考试详情失败:', error);
        this.$modal.msgError("获取考试详情失败：" + (error.msg || error.message || "网络错误"));
        this.detailDialogVisible = false;
      }).finally(() => {
        this.detailLoading = false;
      });
    },
    /** 从详情页开始考试 */
    handleStartFromDetail() {
      this.detailDialogVisible = false;
      this.handleExamAction({
        ...this.examDetail,
        examUser: this.examUser
      });
    },
    /** 继续考试 */
    handleContinueExam(record) {
      this.$router.push({
        path: '/student/exam-detail/taking/' + record.examId,
        query: record.id ? { examUserId: record.id } : {}
      });
    },
    /** 查看成绩 */
    handleViewResult(record) {
      
      // 数据验证
      if (!record || (!record.id && !record.examId)) {
        this.$modal.msgError("考试记录数据异常，请刷新页面重试");
        console.error('考试记录数据异常:', record);
        return;
      }
      
      // 优先使用考试记录ID，如果没有则使用考试ID
      const recordId = record.id || record.examId;
      
      if (!recordId) {
        this.$modal.msgError("无法获取考试记录ID，请刷新页面重试");
        console.error('无法获取有效的记录ID:', record);
        return;
      }
      
      // 进一步验证：确保记录状态是已评分
      if (record.examStatus !== 'graded') {
        this.$modal.msgWarning("该考试成绩尚未发布，请稍后查看");
        console.warn('考试状态不是已评分:', record.examStatus);
        return;
      }
      
      
      // 使用命名路由跳转，确保参数正确传递
      this.$router.push({ 
        name: 'StudentExamResult',
        params: { id: String(recordId) }
      }).catch(err => {
        // 如果命名路由失败，使用路径跳转
        console.warn('命名路由跳转失败，使用路径跳转:', err);
        this.$router.push({ path: `/student/exam-detail/result/${recordId}` });
      });
    },
    /** 重新考试 */
    handleRetakeExam(record) {
      this.handleStartExam({ id: record.examId, examName: record.examName });
    },
    /** 获取试卷类型文本 */
    getPaperTypeText(paperType) {
      const typeMap = {
        'fixed': '固定试卷',
        'random': '随机试卷',
        'mixed': '混合试卷'
      };
      return typeMap[paperType] || paperType;
    },
    
    /** 获取难度等级文本 */
    getDifficultyText(level) {
      const difficultyMap = {
        1: '简单',
        2: '中等',
        3: '困难'
      };
      return difficultyMap[level] || '未知';
    },
    
    /** 获取报名状态类型 */
    getRegistrationStatusType(status) {
      const statusMap = {
        'registered': 'warning',
        'approved': 'success',
        'rejected': 'danger'
      };
      return statusMap[status] || 'info';
    },
    
    /** 获取报名状态文本 */
    getRegistrationStatusText(status) {
      const statusMap = {
        'registered': '待审核',
        'approved': '已通过',
        'rejected': '已拒绝'
      };
      return statusMap[status] || status;
    },
    
    /** 获取考试状态类型（用于标签） */
    getExamStatusType(status) {
      // 统一处理不同格式的状态值
      const statusMap = {
        // 数字状态
        '0': 'info',      // 草稿
        '1': 'success',   // 已发布
        '2': 'warning',   // 进行中
        '3': 'info',      // 已结束
        '4': 'danger',    // 已取消
        // 考试记录状态
        'registered': 'info',
        'not_started': 'info',
        'in_progress': 'warning',
        'submitted': 'primary',
        'graded': 'success'
      };
      return statusMap[status] || 'info';
    },
    
    /** 获取考试状态文本 */
    getExamStatusText(status) {
      // 统一处理不同格式的状态值
      const statusMap = {
        // 数字状态
        '0': '草稿',
        '1': '已发布',
        '2': '进行中', 
        '3': '已结束',
        '4': '已取消',
        // 考试记录状态
        'registered': '已报名',
        'not_started': '未开始',
        'in_progress': '进行中',
        'submitted': '已提交',
        'graded': '已评分'
      };
      return statusMap[status] || status;
    },
    /** 获取状态图标 */
    getStatusIcon(status) {
      const iconMap = {
        '0': 'el-icon-edit',
        '1': 'el-icon-success',
        '2': 'el-icon-loading',
        '3': 'el-icon-info',
        '4': 'el-icon-close'
      };
      return iconMap[status] || 'el-icon-info';
    },
    /** 获取时间状态 */
    getTimeStatus(exam) {
      const now = new Date();
      const startTime = new Date(exam.startTime);
      const endTime = new Date(exam.endTime);
      
      if (now < startTime) {
        const diff = startTime - now;
        const hours = Math.floor(diff / (1000 * 60 * 60));
        const minutes = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60));
        if (hours > 24) {
          const days = Math.floor(hours / 24);
          return `${days}天后开始`;
        } else if (hours > 0) {
          return `${hours}小时${minutes}分钟后开始`;
        } else {
          return `${minutes}分钟后开始`;
        }
      } else if (now > endTime) {
        return '考试已结束';
      } else {
        const diff = endTime - now;
        const hours = Math.floor(diff / (1000 * 60 * 60));
        const minutes = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60));
        if (hours > 0) {
          return `还剩 ${hours}小时${minutes}分钟`;
        } else {
          return `还剩 ${minutes}分钟`;
        }
      }
    },
    /** 获取时间状态样式类 */
    getTimeStatusClass(exam) {
      const now = new Date();
      const startTime = new Date(exam.startTime);
      const endTime = new Date(exam.endTime);
      
      if (now < startTime) {
        return 'status-waiting';
      } else if (now > endTime) {
        return 'status-ended';
      } else {
        const diff = endTime - now;
        const hours = Math.floor(diff / (1000 * 60 * 60));
        if (hours < 2) {
          return 'status-urgent';
        } else {
          return 'status-running';
        }
      }
    },
    /** 获取时间状态图标 */
    getTimeStatusIcon(exam) {
      const now = new Date();
      const startTime = new Date(exam.startTime);
      const endTime = new Date(exam.endTime);
      
      if (now < startTime) {
        return 'el-icon-clock';
      } else if (now > endTime) {
        return 'el-icon-circle-close';
      } else {
        const diff = endTime - now;
        const hours = Math.floor(diff / (1000 * 60 * 60));
        if (hours < 2) {
          return 'el-icon-warning';
        } else {
          return 'el-icon-success';
        }
      }
    },
    /** 获取操作按钮文本 */
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

    /** 获取操作按钮类型 */
    getActionButtonType(exam) {
      const buttonText = this.getActionButtonText(exam);
      
      if (buttonText === '开始考试') {
        return 'primary';
      } else if (buttonText === '继续考试') {
        return 'warning';
      } else if (buttonText === '查看成绩') {
        return 'success';
      } else if (buttonText === '报名') {
        return 'info';
      } else if (buttonText === '已结束' || buttonText === '已取消' || buttonText === '已达上限') {
        return 'danger';
      } else if (buttonText === '未开始') {
        return 'info';
      } else if (buttonText === '待审核' || buttonText === '报名被拒') {
        return 'warning';
      } else {
        return 'default';
      }
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

    /** 判断是否可以执行操作 */
    canPerformAction(exam) {
      const buttonText = this.getActionButtonText(exam);
      
      // 这些状态的按钮是可以点击的
      const clickableActions = ['开始考试', '继续考试', '查看成绩', '报名'];
      return clickableActions.includes(buttonText);
    },
    /** 判断是否可以参加考试 */
    canTakeExam(exam) {
      const now = new Date();
      const startTime = new Date(exam.startTime);
      const endTime = new Date(exam.endTime);
      
      // 考试已结束
      if (now > endTime) {
        return false;
      }
      
      // 考试未开始
      if (now < startTime) {
        return false;
      }
      
      // 考试状态必须是已发布或进行中
      if (!(exam.status === '1' || exam.status === '2')) {
        return false;
      }
      
      // 如果需要报名但未报名，不能参加
      if (exam.registrationRequired === '1' && !exam.examUser) {
        return false;
      }
      
      // 检查考试次数限制
      if (exam.examUser && exam.examUser.attemptCount >= exam.maxAttempts) {
        return false;
      }
      
      return true;
    },
    /** 判断是否可以重考 */
    canRetake(record) {
      return record.examStatus === 'graded' && 
             record.attemptCount < record.maxAttempts &&
             record.isPassed !== '1';
    },
    /** 获取考试卡片样式类 */
    getExamCardClass(exam) {
      const now = new Date();
      const startTime = new Date(exam.startTime);
      const endTime = new Date(exam.endTime);
      
      if (now > endTime) {
        return 'exam-ended'; // 已结束
      } else if (now < startTime) {
        return 'exam-not-started'; // 未开始
      } else if (!this.canTakeExam(exam)) {
        return 'exam-disabled'; // 不可参加
      } else {
        return 'exam-available'; // 可参加
      }
    },
    /** 考试记录表格排序 */
    handleRecordsSortChange(sort) {
      if (sort.order === 'ascending') {
        this.recordsQueryParams.sortField = sort.prop;
        this.recordsQueryParams.sortOrder = 'asc';
      } else if (sort.order === 'descending') {
        this.recordsQueryParams.sortField = sort.prop;
        this.recordsQueryParams.sortOrder = 'desc';
      } else {
        this.recordsQueryParams.sortField = '';
        this.recordsQueryParams.sortOrder = '';
      }
      this.getRecordsList();
    }
  }
};
</script>

<style scoped>
/* 考试卡片主体样式 */
.exam-card {
  position: relative;
  background: linear-gradient(135deg, #ffffff 0%, #f8fafe 100%);
  border: 1px solid #e4e7ed;
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 16px;
  transition: all 0.4s cubic-bezier(0.25, 0.8, 0.25, 1);
  overflow: hidden;
  box-shadow: 0 2px 8px 0 rgba(0, 0, 0, 0.06);
  display: flex;
  flex-direction: column;
  min-height: 260px;
  height: 100%;
}

.exam-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 6px 24px 0 rgba(0, 0, 0, 0.12);
  border-color: #409eff;
}

/* 不可参加的考试 */
.exam-card.exam-disabled {
  opacity: 0.7;
  background: linear-gradient(135deg, #f5f7fa 0%, #e9ecef 100%);
}

.exam-card.exam-disabled:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px 0 rgba(0, 0, 0, 0.08);
}

/* 已结束的考试 */
.exam-card.exam-ended {
  opacity: 0.8;
  background: linear-gradient(135deg, #f5f7fa 0%, #e6e8eb 100%);
  border-color: #dcdfe6;
}

.exam-card.exam-ended:hover {
  transform: translateY(-4px);
  box-shadow: 0 6px 24px 0 rgba(0, 0, 0, 0.1);
}

.exam-card.exam-ended .exam-title {
  color: #606266;
}

/* 未开始的考试 */
.exam-card.exam-not-started {
  background: linear-gradient(135deg, #fefefe 0%, #f0f9ff 100%);
  border-color: #e1f5fe;
}

.exam-card.exam-not-started:hover {
  transform: translateY(-6px);
  box-shadow: 0 8px 28px 0 rgba(33, 150, 243, 0.15);
  border-color: #2196f3;
}

/* 可参加的考试 */
.exam-card.exam-available {
  background: linear-gradient(135deg, #ffffff 0%, #f8fff8 100%);
  border-color: #e8f5e8;
}

.exam-card.exam-available:hover {
  transform: translateY(-8px);
  box-shadow: 0 12px 32px 0 rgba(76, 175, 80, 0.2);
  border-color: #4caf50;
}

/* 卡片装饰元素 */
.card-decoration {
  position: absolute;
  top: 0;
  right: 0;
  pointer-events: none;
}

.decoration-circle {
  position: absolute;
  border-radius: 50%;
  opacity: 0.1;
}

.decoration-circle-1 {
  width: 40px;
  height: 40px;
  background: linear-gradient(45deg, #409eff, #67c23a);
  top: -20px;
  right: -20px;
}

.decoration-circle-2 {
  width: 70px;
  height: 70px;
  background: linear-gradient(45deg, #e6a23c, #f56c6c);
  top: -35px;
  right: 15px;
}

/* 卡片头部 */
.card-header {
  margin-bottom: 12px;
}

.exam-title-section {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 6px;
}

.exam-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  margin: 0;
  line-height: 1.3;
  flex: 1;
  margin-right: 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.status-tag {
  border-radius: 20px;
  font-weight: 500;
  white-space: nowrap;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  margin-top: 2px;
  align-self: center;
}

.status-tag i {
  margin-right: 4px;
}

.exam-meta {
  margin-top: 8px;
}

.paper-name {
  color: #909399;
  font-size: 14px;
  background: #f5f7fa;
  padding: 4px 12px;
  border-radius: 12px;
  display: inline-block;
}

/* 卡片内容 */
.card-content {
  margin-bottom: 12px;
  flex: 1;
  display: flex;
  flex-direction: column;
}

/* 基本信息 */
.exam-basic-info {
  margin-bottom: 10px;
}

.info-row {
  display: flex;
  gap: 8px;
  margin-bottom: 8px;
  flex-wrap: nowrap; /* 防止换行 */
}

.info-row:last-child {
  margin-bottom: 0;
}

.info-item {
  flex: 1;
  display: flex;
  align-items: center;
  background: #fafbfc;
  padding: 8px 10px;
  border-radius: 6px;
  border-left: 2px solid #e4e7ed;
  transition: all 0.3s ease;
  font-size: 13px;
  min-width: 0; /* 允许内容收缩 */
  flex-shrink: 1; /* 允许弹性收缩 */
}

.info-item:hover {
  background: #f0f2f5;
  border-left-color: #409eff;
}

.info-item i {
  font-size: 16px;
  margin-right: 8px;
  width: 20px;
  text-align: center;
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
  flex-shrink: 0; /* 标签不收缩 */
}

.info-value {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
  white-space: nowrap; /* 防止数值换行 */
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

/* 不同状态按钮的特定样式 */
.action-btn.el-button--primary {
  background: linear-gradient(135deg, #409eff 0%, #2593fc 100%);
  border: none;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
}

.action-btn.el-button--primary:hover {
  background: linear-gradient(135deg, #2593fc 0%, #1e7ce8 100%);
  box-shadow: 0 6px 16px rgba(64, 158, 255, 0.4);
  transform: translateY(-1px);
}

.action-btn.el-button--warning {
  background: linear-gradient(135deg, #e6a23c 0%, #f39c12 100%);
  border: none;
  color: white;
  box-shadow: 0 4px 12px rgba(230, 162, 60, 0.3);
}

.action-btn.el-button--warning:hover {
  background: linear-gradient(135deg, #cf9236 0%, #d68910 100%);
  box-shadow: 0 6px 16px rgba(230, 162, 60, 0.4);
  transform: translateY(-1px);
}

.action-btn.el-button--success {
  background: linear-gradient(135deg, #67c23a 0%, #85ce61 100%);
  border: none;
  color: white;
  box-shadow: 0 4px 12px rgba(103, 194, 58, 0.3);
}

.action-btn.el-button--success:hover {
  background: linear-gradient(135deg, #5daf34 0%, #73b557 100%);
  box-shadow: 0 6px 16px rgba(103, 194, 58, 0.4);
  transform: translateY(-1px);
}

.action-btn.el-button--info {
  background: linear-gradient(135deg, #909399 0%, #a6a9ad 100%);
  border: none;
  color: white;
  box-shadow: 0 4px 12px rgba(144, 147, 153, 0.3);
}

.action-btn.el-button--info:hover {
  background: linear-gradient(135deg, #82848a 0%, #92959a 100%);
  box-shadow: 0 6px 16px rgba(144, 147, 153, 0.4);
  transform: translateY(-1px);
}

.action-btn.el-button--danger {
  background: linear-gradient(135deg, #f56c6c 0%, #f78989 100%);
  border: none;
  color: white;
  box-shadow: 0 4px 12px rgba(245, 108, 108, 0.3);
}

.action-btn.el-button--danger:hover {
  background: linear-gradient(135deg, #dd6161 0%, #e73c3c 100%);
  box-shadow: 0 6px 16px rgba(245, 108, 108, 0.4);
  transform: translateY(-1px);
}

.action-btn.el-button--default {
  background: #f4f4f5;
  border-color: #d3d4d6;
  color: #606266;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.action-btn.el-button--default:hover {
  background: #e6e8eb;
  border-color: #b1b3b8;
  color: #303133;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  transform: translateY(-1px);
}

/* 考试行布局 */
.exam-row {
  display: flex;
  flex-wrap: wrap;
}

.exam-col {
  display: flex;
  margin-bottom: 16px;
}

/* 通用样式 */
.mb8 {
  margin-bottom: 8px;
}

/* 加载状态 */
.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 20px;
  color: #909399;
  background: #fafbfc;
  border-radius: 12px;
  border: 2px dashed #e4e7ed;
}

.loading-container i {
  font-size: 32px;
  margin-bottom: 12px;
  color: #409eff;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.loading-container span {
  font-size: 16px;
  font-weight: 500;
}

/* 空数据状态 */
.empty-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 100px 20px;
  color: #909399;
  background: #fafbfc;
  border-radius: 12px;
  border: 2px dashed #e4e7ed;
}

.empty-container i {
  font-size: 80px;
  margin-bottom: 24px;
  color: #c0c4cc;
  opacity: 0.6;
}

.empty-container p {
  margin: 8px 0;
  font-size: 16px;
}

.empty-container p:first-of-type {
  font-weight: 500;
  color: #606266;
}

.text-muted {
  color: #c0c4cc !important;
  font-size: 14px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .exam-card {
    padding: 16px;
    margin-bottom: 16px;
  }
  
  .exam-title {
    font-size: 18px;
  }
  
  .info-row {
    flex-direction: column;
    gap: 8px;
  }
  
  .exam-time-info {
    flex-direction: column;
    gap: 12px;
  }
  
  .time-divider {
    transform: rotate(90deg);
    margin: 8px 0;
  }
  
  .card-actions {
    flex-direction: column;
  }
}

@media (max-width: 480px) {
  .exam-card {
    min-height: 400px;
  }
  
  .exam-title-section {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
  
  .status-tag {
    align-self: flex-start;
  }
  
  .exam-col {
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

/* 移动端适配 */
@media (max-width: 768px) {
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
</style>
