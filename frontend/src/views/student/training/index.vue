<template>
  <div class="student-training">
    <div class="training-header">
      <div>
        <h2>学习中心</h2>
        <p>课程学习、资源查看、自主练习、问答和证书统一管理</p>
      </div>
      <el-button type="primary" icon="el-icon-plus" @click="qaOpen = true">我要提问</el-button>
    </div>

    <el-row :gutter="16" class="summary-row">
      <el-col :xs="12" :sm="8" :md="4" v-for="item in summaryCards" :key="item.key">
        <div class="summary-card">
          <div class="summary-value">{{ item.value }}</div>
          <div class="summary-label">{{ item.label }}</div>
        </div>
      </el-col>
    </el-row>

    <el-tabs v-model="activeTab" @tab-click="handleTabClick">
      <el-tab-pane label="课程学习" name="course">
        <el-row :gutter="16">
          <el-col :xs="24" :sm="12" :lg="8" v-for="course in courses" :key="course.id">
            <div class="course-card">
              <div class="course-title">{{ course.title }}</div>
              <div class="course-summary">{{ course.summary || '暂无简介' }}</div>
              <div class="course-meta">
                <span>{{ course.durationMinutes || 0 }} 分钟</span>
                <span>{{ course.credit || 0 }} 学分</span>
                <span>{{ course.completedCount || 0 }}/{{ course.learnerCount || 0 }} 完成</span>
                <span v-if="course.relatedExamId">课后考试 #{{ course.relatedExamId }}</span>
              </div>
              <div class="course-actions">
                <el-button size="mini" type="primary" @click="openCourse(course)">开始学习</el-button>
                <el-button v-if="course.relatedExamId" size="mini" type="warning" @click="startAfterCourseExam(course)">课后考试</el-button>
              </div>
            </div>
          </el-col>
        </el-row>
        <el-empty v-if="courses.length === 0" description="暂无课程" />
      </el-tab-pane>

      <el-tab-pane label="资源库" name="resource">
        <el-table :data="resources" v-loading="resourceLoading" empty-text="暂无资源">
          <el-table-column label="资源名称" prop="title" min-width="220" />
          <el-table-column label="类型" width="110">
            <template slot-scope="scope">
              <el-tag size="small" effect="plain">{{ getItemTypeLabel(scope.row) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="处理状态" width="120">
            <template slot-scope="scope">
              <el-tag size="small" :type="getProcessingStatusType(scope.row.processingStatus)">
                {{ getProcessingStatusLabel(scope.row.processingStatus) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="摘要" prop="summary" min-width="260" show-overflow-tooltip />
          <el-table-column label="操作" width="150" fixed="right">
            <template slot-scope="scope">
              <el-button size="mini" type="text" @click="openPreview(scope.row, 'resource')">查看</el-button>
              <el-button size="mini" type="text" :disabled="!canDownload(scope.row)" @click="downloadResource(scope.row)">下载</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="自主练习" name="practice">
        <div class="practice-panel">
          <div class="section-title-row">
            <div>
              <div class="section-title">记录一次练习</div>
              <div class="section-desc">用于记录线下练习、错题重做或管理员安排的自训结果。</div>
            </div>
          </div>
          <el-form :model="practiceForm" label-width="90px" inline>
            <el-form-item label="练习主题">
              <el-input v-model="practiceForm.title" placeholder="例如：高处作业错题重练" style="width: 260px" />
            </el-form-item>
            <el-form-item label="得分">
              <el-input-number v-model="practiceForm.score" :min="0" :max="100" :precision="1" />
            </el-form-item>
            <el-form-item label="用时">
              <el-input-number v-model="practiceForm.durationMinutes" :min="0" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="submitPractice">记录练习</el-button>
            </el-form-item>
          </el-form>
        </div>

        <div class="practice-section">
          <div class="section-title-row">
            <div>
              <div class="section-title">可练习考试</div>
              <div class="section-desc">管理员发布为练习类型的考试会显示在这里，可直接进入答题。</div>
            </div>
            <el-button size="mini" icon="el-icon-refresh" @click="loadPracticeExams">刷新</el-button>
          </div>
          <el-table :data="practiceExamList" v-loading="practiceExamLoading" empty-text="暂无可用练习考试">
            <el-table-column label="考试名称" prop="examName" min-width="220" show-overflow-tooltip />
            <el-table-column label="状态" width="100">
              <template slot-scope="scope">
                <el-tag size="small" :type="getExamStatusType(scope.row.status)">{{ getExamStatusText(scope.row.status) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="考试时间" min-width="260">
              <template slot-scope="scope">
                {{ parseTime(scope.row.startTime, '{y}-{m}-{d} {h}:{i}') || '-' }}
                至
                {{ parseTime(scope.row.endTime, '{y}-{m}-{d} {h}:{i}') || '-' }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="110" fixed="right">
              <template slot-scope="scope">
                <el-button size="mini" type="primary" plain @click="startPracticeExam(scope.row)">开始练习</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>

        <div class="practice-section">
          <div class="section-title-row">
            <div>
              <div class="section-title">练习记录</div>
              <div class="section-desc">提交练习后会保留历史记录，并计入学习中心统计。</div>
            </div>
          </div>
          <el-table :data="practiceRecords" v-loading="practiceLoading" empty-text="暂无练习记录">
            <el-table-column label="主题" prop="title" min-width="220" show-overflow-tooltip />
            <el-table-column label="得分" width="90">
              <template slot-scope="scope">{{ scope.row.score == null ? '-' : scope.row.score }}</template>
            </el-table-column>
            <el-table-column label="用时" width="90">
              <template slot-scope="scope">{{ scope.row.durationMinutes || 0 }} 分钟</template>
            </el-table-column>
            <el-table-column label="学分" width="90">
              <template slot-scope="scope">{{ scope.row.credit || 0 }}</template>
            </el-table-column>
            <el-table-column label="完成时间" width="160">
              <template slot-scope="scope">{{ parseTime(scope.row.finishTime || scope.row.createTime, '{y}-{m}-{d} {h}:{i}') }}</template>
            </el-table-column>
          </el-table>
        </div>
      </el-tab-pane>

      <el-tab-pane label="互动问答" name="qa">
        <el-table :data="qaList" v-loading="qaLoading" empty-text="暂无问答">
          <el-table-column label="问题" prop="title" min-width="220" />
          <el-table-column label="状态" prop="status" width="100">
            <template slot-scope="scope">
              <el-tag size="small" :type="scope.row.status === 'answered' ? 'success' : 'warning'">{{ scope.row.status === 'answered' ? '已回复' : '待回复' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="回复" prop="answerContent" min-width="280" show-overflow-tooltip />
          <el-table-column label="时间" prop="createTime" width="160">
            <template slot-scope="scope">{{ parseTime(scope.row.createTime, '{y}-{m}-{d} {h}:{i}') }}</template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="我的证书" name="certificate">
        <el-table :data="certificates" empty-text="暂无证书">
          <el-table-column label="证书名称" prop="title" min-width="220" />
          <el-table-column label="学分" prop="credit" width="100" />
          <el-table-column label="发放时间" prop="createTime" width="160">
            <template slot-scope="scope">{{ parseTime(scope.row.createTime, '{y}-{m}-{d} {h}:{i}') }}</template>
          </el-table-column>
          <el-table-column label="操作" width="150" fixed="right">
            <template slot-scope="scope">
              <el-button type="text" size="mini" @click="openPreview(scope.row, 'certificate')">查看</el-button>
              <el-button type="text" size="mini" :disabled="!canDownload(scope.row)" @click="downloadResource(scope.row)">下载</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="作答附件" name="attachment">
        <el-form :model="attachmentForm" label-width="90px" class="attachment-form">
          <el-row :gutter="16">
            <el-col :xs="24" :sm="8">
              <el-form-item label="考试ID">
                <el-input-number v-model="attachmentForm.examId" :min="0" controls-position="right" style="width: 100%" />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="8">
              <el-form-item label="考试记录">
                <el-input-number v-model="attachmentForm.examUserId" :min="0" controls-position="right" style="width: 100%" />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="8">
              <el-form-item label="来源">
                <el-select v-model="attachmentForm.uploadSource" style="width: 100%">
                  <el-option label="Web" value="web" />
                  <el-option label="H5" value="h5" />
                  <el-option label="扫码" value="scan" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="24">
              <el-form-item label="附件">
                <file-upload v-model="attachmentForm.fileUrl" :limit="1" :file-size="20" :file-type="['pdf','doc','docx','png','jpg','jpeg','mp4']" />
              </el-form-item>
            </el-col>
            <el-col :span="24">
              <el-form-item>
                <el-button type="primary" @click="submitAttachment">保存附件</el-button>
              </el-form-item>
            </el-col>
          </el-row>
        </el-form>
        <el-table :data="attachments" v-loading="attachmentLoading" empty-text="暂无附件">
          <el-table-column label="文件名" prop="fileName" min-width="220" show-overflow-tooltip />
          <el-table-column label="考试ID" prop="examId" width="100" />
          <el-table-column label="考试记录" prop="examUserId" width="100" />
          <el-table-column label="来源" prop="uploadSource" width="90" />
          <el-table-column label="时间" prop="createTime" width="160">
            <template slot-scope="scope">{{ parseTime(scope.row.createTime, '{y}-{m}-{d} {h}:{i}') }}</template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
    </el-tabs>

    <el-dialog
      :title="activeCourse ? activeCourse.title : '课程学习'"
      :visible.sync="courseDialogOpen"
      width="960px"
      top="4vh"
      append-to-body
      class="course-dialog"
      @closed="handleCourseDialogClosed"
    >
      <div v-if="activeCourse" class="course-learn">
        <div class="player-panel">
          <div class="player-main">
            <div class="player-stage">
              <el-image v-if="isImageFile(activeCourse) || (!getPrimaryUrl(activeCourse) && activeCourse.coverUrl)" :src="getResourceUrl(getPrimaryUrl(activeCourse) || activeCourse.coverUrl)" fit="contain" />
              <video v-else-if="isVideoFile(activeCourse)" :src="getResourceUrl(getPrimaryUrl(activeCourse))" controls preload="metadata" @play="startLearningTimer" @pause="pauseLearningTimer(true)" @ended="completeCourse(activeCourse)" />
              <iframe v-else-if="isPdfFile(activeCourse)" :src="getResourceUrl(getPrimaryUrl(activeCourse))" title="课程文件预览" />
              <div v-else class="player-placeholder">
                <i class="el-icon-reading"></i>
                <div>{{ hasRichContent(activeCourse.content) ? '阅读课程正文时将自动计时' : '该课程暂无可播放媒体' }}</div>
              </div>
            </div>
            <div class="player-controls">
              <el-button :type="learningTimerRunning ? 'warning' : 'primary'" size="small" :icon="learningTimerRunning ? 'el-icon-video-pause' : 'el-icon-video-play'" @click="toggleLearningTimer">
                {{ learningTimerRunning ? '暂停学习' : '继续学习' }}
              </el-button>
              <el-button type="success" size="small" icon="el-icon-check" :disabled="!courseLearningQualified" @click="completeCourse(activeCourse)">完成学习</el-button>
              <span class="timer-text">本课程已学 {{ formatLearningDuration(currentLearningSeconds) }}</span>
            </div>
          </div>
          <div class="player-side">
            <div class="detail-title">{{ activeCourse.title }}</div>
            <div class="detail-summary">{{ activeCourse.summary || '暂无简介' }}</div>
            <div class="detail-meta">
              <el-tag size="mini" effect="plain">目标 {{ activeCourse.durationMinutes || 0 }} 分钟</el-tag>
              <el-tag size="mini" effect="plain">{{ activeCourse.credit || 0 }} 学分</el-tag>
              <el-tag :type="courseLearningStatus === 'completed' ? 'success' : 'warning'" size="mini" effect="plain">{{ courseLearningStatus === 'completed' ? '已完成' : '学习中' }}</el-tag>
              <el-tag :type="courseLearningQualified ? 'success' : 'info'" size="mini" effect="plain">{{ courseLearningQualified ? '已达标' : '未达标' }}</el-tag>
            </div>
            <el-progress :percentage="learningProgressPercent" :status="courseLearningQualified ? 'success' : null" />
            <div class="progress-desc">达标要求：累计学习满目标时长。</div>
          </div>
        </div>

        <div class="content-section">
          <div class="section-title">课程内容</div>
          <div v-if="hasRichContent(activeCourse.content)" class="rich-text" v-html="sanitizeContent(activeCourse.content)" />
          <el-empty v-else description="该课程暂无正文内容" />
        </div>

        <div v-if="getPrimaryUrl(activeCourse)" class="content-section">
          <div class="section-title">课程媒体</div>
          <div class="inline-media">
            <el-image v-if="isImageFile(activeCourse)" :src="getResourceUrl(getPrimaryUrl(activeCourse))" fit="contain" :preview-src-list="[getResourceUrl(getPrimaryUrl(activeCourse))]" />
            <video v-else-if="isVideoFile(activeCourse)" :src="getResourceUrl(getPrimaryUrl(activeCourse))" controls preload="metadata" />
            <iframe v-else-if="isPdfFile(activeCourse)" :src="getResourceUrl(getPrimaryUrl(activeCourse))" title="课程文件预览" />
            <div v-else class="unsupported-file">
              <i class="el-icon-document"></i>
              <span>{{ getFileName(getPrimaryUrl(activeCourse)) }}</span>
              <el-button size="mini" type="primary" plain @click="openFile(activeCourse)">打开文件</el-button>
            </div>
          </div>
        </div>

        <div class="content-section">
          <div class="section-title-row">
            <div class="section-title">课程课件</div>
            <el-button size="mini" icon="el-icon-refresh" @click="loadCourseItems(activeCourse.id)">刷新</el-button>
          </div>
          <el-table :data="courseItems" v-loading="courseItemLoading" empty-text="暂无课程课件">
            <el-table-column label="名称" prop="title" min-width="220" show-overflow-tooltip />
            <el-table-column label="类型" width="100">
              <template slot-scope="scope">
                <el-tag size="small" effect="plain">{{ getItemTypeLabel(scope.row) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="摘要" prop="summary" min-width="220" show-overflow-tooltip />
            <el-table-column label="操作" width="150" fixed="right">
              <template slot-scope="scope">
              <el-button size="mini" type="text" @click="openPreview(scope.row, 'lesson')">查看</el-button>
              <el-button size="mini" type="text" :disabled="!canDownload(scope.row)" @click="downloadResource(scope.row)">下载</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
      <div slot="footer" class="dialog-footer">
        <el-button :type="learningTimerRunning ? 'warning' : 'primary'" @click="toggleLearningTimer">{{ learningTimerRunning ? '暂停学习' : '继续学习' }}</el-button>
        <el-button type="success" :disabled="!courseLearningQualified" @click="completeCourse(activeCourse)">完成学习</el-button>
        <el-button v-if="activeCourse && activeCourse.relatedExamId" type="warning" @click="startAfterCourseExam(activeCourse)">课后考试</el-button>
        <el-button @click="closeCourseDialog">关 闭</el-button>
      </div>
    </el-dialog>

    <el-dialog :title="previewTitle" :visible.sync="previewOpen" width="860px" top="5vh" append-to-body @closed="resetPreviewState">
      <div v-if="previewItem" class="preview-body">
        <div class="preview-meta">
          <div class="preview-name">{{ previewItem.title }}</div>
          <div class="preview-desc">{{ previewItem.summary || previewItem.remark || '暂无摘要' }}</div>
        </div>

        <div v-if="hasRichContent(previewItem.content)" class="content-section">
          <div class="section-title">正文内容</div>
          <div class="rich-text" v-html="sanitizeContent(previewItem.content)" />
        </div>

        <div v-if="getPrimaryUrl(previewItem)" class="content-section">
          <div class="section-title">文件预览</div>
          <el-alert v-if="previewLoadError" title="文件加载失败，请确认管理员已上传有效文件" type="warning" show-icon :closable="false" />
          <div class="preview-file">
            <el-image
              v-if="isImageFile(previewItem)"
              :src="getResourceUrl(getPrimaryUrl(previewItem))"
              fit="contain"
              :preview-src-list="[getResourceUrl(getPrimaryUrl(previewItem))]"
              @error="previewLoadError = true"
            />
            <video
              v-else-if="isVideoFile(previewItem)"
              :src="getResourceUrl(getPrimaryUrl(previewItem))"
              controls
              preload="metadata"
              @error="previewLoadError = true"
            />
            <iframe v-else-if="isPdfFile(previewItem)" :src="getResourceUrl(getPrimaryUrl(previewItem))" title="文件预览" />
            <div v-else class="unsupported-file">
              <i class="el-icon-document"></i>
              <span>{{ getFileName(getPrimaryUrl(previewItem)) || '附件文件' }}</span>
              <el-button size="mini" type="primary" plain @click="openFile(previewItem)">打开文件</el-button>
            </div>
          </div>
          <div v-if="isPdfFile(previewItem)" class="preview-tip">PDF 预览区域如果显示 404 或空白，说明当前样例文件未上传到服务器。</div>
        </div>

        <el-empty v-if="!hasPreviewContent(previewItem)" description="该内容暂无可查看正文或文件" />
      </div>
      <div slot="footer" class="dialog-footer">
        <el-button v-if="canDownload(previewItem)" type="primary" @click="downloadResource(previewItem)">下载文件</el-button>
        <el-button @click="previewOpen = false">关 闭</el-button>
      </div>
    </el-dialog>

    <el-dialog title="提交问题" :visible.sync="qaOpen" width="640px" append-to-body>
      <el-form ref="qaForm" :model="qaForm" :rules="qaRules" label-width="80px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="qaForm.title" placeholder="请输入问题标题" />
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input v-model="qaForm.content" type="textarea" :rows="5" placeholder="请描述你的问题" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitQa">确 定</el-button>
        <el-button @click="qaOpen = false">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import {
  getStudentTrainingSummary,
  getStudentCourses,
  getStudentCourseItems,
  getStudentCourseProgress,
  getStudentResources,
  getStudentCertificates,
  listStudentQa,
  markCourseProgress,
  recordPractice,
  listStudentPractice,
  askTrainingQuestion,
  listStudentAttachment,
  addStudentAttachment,
  startCourseExam
} from '@/api/student/training'
import { getAllExaminations, startExam } from '@/api/student/exam'
import { sanitizeRichText, stripHtml } from '@/utils/sanitize'

export default {
  name: 'StudentTrainingCenter',
  data() {
    return {
      activeTab: 'course',
      summary: {},
      courses: [],
      resources: [],
      certificates: [],
      qaList: [],
      attachments: [],
      practiceRecords: [],
      practiceExamList: [],
      resourceLoading: false,
      qaLoading: false,
      attachmentLoading: false,
      practiceLoading: false,
      practiceExamLoading: false,
      courseDialogOpen: false,
      activeCourse: null,
      courseItems: [],
      courseItemLoading: false,
      courseProgress: null,
      currentLearningSeconds: 0,
      learningTimer: null,
      learningTimerRunning: false,
      progressSyncing: false,
      previewOpen: false,
      previewTitle: '内容预览',
      previewItem: null,
      previewLoadError: false,
      qaOpen: false,
      qaForm: { title: '', content: '' },
      qaRules: {
        title: [{ required: true, message: '问题标题不能为空', trigger: 'blur' }],
        content: [{ required: true, message: '问题内容不能为空', trigger: 'blur' }]
      },
      practiceForm: {
        title: '',
        score: 100,
        durationMinutes: 10,
        credit: 0
      },
      attachmentForm: {
        examId: null,
        examUserId: null,
        fileUrl: '',
        uploadSource: 'web'
      }
    }
  },
  computed: {
    summaryCards() {
      return [
        { key: 'course', label: '可学课程', value: this.summary.courseCount || 0 },
        { key: 'resource', label: '资源', value: this.summary.resourceCount || 0 },
        { key: 'practice', label: '练习', value: this.summary.practiceCount || 0 },
        { key: 'qa', label: '问答', value: this.summary.qaCount || 0 },
        { key: 'certificate', label: '证书', value: this.summary.certificateCount || 0 },
        { key: 'completed', label: '已学课程', value: this.summary.completedCount || 0 },
        { key: 'credit', label: '学分', value: this.summary.totalCredit || 0 }
      ]
    },
    courseLearningStatus() {
      return this.courseProgress && this.courseProgress.status === 'completed' ? 'completed' : 'learning'
    },
    learningProgressPercent() {
      const targetSeconds = Math.max(0, Number((this.activeCourse && this.activeCourse.durationMinutes) || 0)) * 60
      if (this.courseLearningStatus === 'completed') return 100
      if (targetSeconds <= 0) return 0
      return Math.max(0, Math.min(100, Number((this.currentLearningSeconds * 100 / targetSeconds).toFixed(1))))
    },
    courseLearningQualified() {
      if (!this.activeCourse) return false
      if (this.courseLearningStatus === 'completed') return true
      const targetMinutes = Number(this.activeCourse.durationMinutes || 0)
      return targetMinutes <= 0 || this.currentLearningSeconds >= targetMinutes * 60
    }
  },
  created() {
    if (this.$route.query.tab) {
      this.activeTab = this.$route.query.tab
    }
    this.refreshAll()
  },
    beforeDestroy() {
      this.stopLearningTimer()
      this.syncCourseProgress(true)
    },
  methods: {
    refreshAll() {
      this.loadSummary()
      this.loadCourses()
      this.loadResources()
      this.loadCertificates()
      this.loadQa()
      this.loadAttachments()
      this.loadPracticeRecords()
      this.loadPracticeExams()
    },
    handleTabClick() {
      if (this.activeTab === 'practice') {
        this.loadPracticeRecords()
        this.loadPracticeExams()
      }
    },
    loadSummary() {
      return getStudentTrainingSummary().then(response => {
        this.summary = response.data || {}
      })
    },
    loadCourses() {
      return getStudentCourses().then(response => {
        this.courses = response.data || []
        if (this.activeCourse) {
          const current = this.courses.find(item => item.id === this.activeCourse.id)
          if (current) {
            this.activeCourse = current
          }
        }
      })
    },
    loadCourseItems(courseId) {
      if (!courseId) return
      this.courseItemLoading = true
      return getStudentCourseItems(courseId).then(response => {
        this.courseItems = response.data || []
        this.courseItemLoading = false
      }).catch(() => {
        this.courseItemLoading = false
      })
    },
    loadResources() {
      this.resourceLoading = true
      return getStudentResources({ itemType: 'resource' }).then(response => {
        const resources = response.data || []
        return getStudentResources({ itemType: 'media_resource' }).then(mediaResponse => {
          this.resources = resources.concat(mediaResponse.data || [])
          this.resourceLoading = false
        })
      }).catch(() => {
        this.resourceLoading = false
      })
    },
    loadCertificates() {
      return getStudentCertificates().then(response => {
        this.certificates = response.data || []
      })
    },
    loadQa() {
      this.qaLoading = true
      return listStudentQa({ pageNum: 1, pageSize: 50 }).then(response => {
        this.qaList = response.rows || []
        this.qaLoading = false
      }).catch(() => {
        this.qaLoading = false
      })
    },
    loadPracticeRecords() {
      this.practiceLoading = true
      return listStudentPractice({ pageNum: 1, pageSize: 50 }).then(response => {
        this.practiceRecords = response.rows || []
        this.practiceLoading = false
      }).catch(() => {
        this.practiceLoading = false
      })
    },
    loadPracticeExams() {
      this.practiceExamLoading = true
      return getAllExaminations({ examType: 'practice', pageNum: 1, pageSize: 20 }).then(response => {
        this.practiceExamList = response.rows || []
        this.practiceExamLoading = false
      }).catch(() => {
        this.practiceExamLoading = false
      })
    },
    loadAttachments() {
      this.attachmentLoading = true
      return listStudentAttachment({ pageNum: 1, pageSize: 50 }).then(response => {
        this.attachments = response.rows || []
        this.attachmentLoading = false
      }).catch(() => {
        this.attachmentLoading = false
      })
    },
    openCourse(course) {
      this.activeCourse = course
      this.courseDialogOpen = true
      this.courseItems = []
      this.courseProgress = null
      this.currentLearningSeconds = 0
      this.previewLoadError = false
      this.loadCourseItems(course.id)
      this.loadCourseProgress(course.id).then(() => {
        if (this.courseLearningStatus !== 'completed') {
          this.startLearningTimer()
        }
        this.syncCourseProgress(true)
      })
    },
    loadCourseProgress(courseId) {
      return getStudentCourseProgress(courseId).then(response => {
        this.courseProgress = response.data || null
        this.currentLearningSeconds = ((this.courseProgress && this.courseProgress.durationMinutes) || 0) * 60
      })
    },
    completeCourse(course) {
      if (!course || !course.id) return
      if (!this.courseLearningQualified) {
        this.$modal.msgWarning('学习时长未达到课程目标，暂不能完成学习')
        return
      }
      this.stopLearningTimer()
      this.saveCourseProgress(course, 'completed', false, this.getCurrentLearningMinutes())
    },
    closeCourseDialog() {
      this.syncCourseProgress(true).finally(() => {
        this.courseDialogOpen = false
      })
    },
    handleCourseDialogClosed() {
      this.stopLearningTimer()
      this.activeCourse = null
      this.courseItems = []
      this.courseProgress = null
      this.currentLearningSeconds = 0
    },
    toggleLearningTimer() {
      if (this.learningTimerRunning) {
        this.pauseLearningTimer(false)
      } else {
        this.startLearningTimer()
      }
    },
    startLearningTimer() {
      if (!this.activeCourse || this.courseLearningStatus === 'completed' || this.learningTimerRunning) return
      this.learningTimerRunning = true
      this.learningTimer = window.setInterval(() => {
        this.currentLearningSeconds += 1
        if (this.courseLearningQualified) {
          this.stopLearningTimer()
          this.saveCourseProgress(this.activeCourse, 'completed', true, this.getCurrentLearningMinutes())
          return
        }
        if (this.currentLearningSeconds % 60 === 0) {
          this.syncCourseProgress(true)
        }
      }, 1000)
    },
    pauseLearningTimer(silent) {
      this.stopLearningTimer()
      return this.syncCourseProgress(silent !== false)
    },
    stopLearningTimer() {
      if (this.learningTimer) {
        window.clearInterval(this.learningTimer)
        this.learningTimer = null
      }
      this.learningTimerRunning = false
    },
    syncCourseProgress(silent) {
      if (!this.activeCourse || this.courseLearningStatus === 'completed' || this.progressSyncing) {
        return Promise.resolve()
      }
      return this.saveCourseProgress(this.activeCourse, 'learning', silent, this.getCurrentLearningMinutes())
    },
    getCurrentLearningMinutes() {
      return Math.max(0, Math.floor(this.currentLearningSeconds / 60))
    },
    saveCourseProgress(course, status, silent, durationMinutes) {
      this.progressSyncing = true
      return markCourseProgress(course.id, {
        status,
        durationMinutes: durationMinutes || (status === 'completed' ? course.durationMinutes : this.getCurrentLearningMinutes())
      }).then(response => {
        this.courseProgress = response.data || this.courseProgress
        this.currentLearningSeconds = Math.max(this.currentLearningSeconds, ((this.courseProgress && this.courseProgress.durationMinutes) || 0) * 60)
        if (!silent) {
          this.$modal.msgSuccess(status === 'completed' ? '课程已完成' : '学习进度已保存')
        }
        this.loadSummary()
        this.loadCourses()
      }).finally(() => {
        this.progressSyncing = false
      })
    },
    startAfterCourseExam(course) {
      if (!course || !course.relatedExamId) {
        this.$modal.msgError('课程未绑定课后考试')
        return
      }
      startCourseExam(course.id).then(res => {
        const examUserId = res && res.examUserId
        this.$router.push({
          path: '/student/exam-detail/taking/' + course.relatedExamId,
          query: examUserId ? { examUserId } : {}
        })
      })
    },
    startPracticeExam(exam) {
      if (!exam || !exam.id) return
      startExam(exam.id).then(res => {
        const examUserId = res && res.examUserId
        this.$router.push({
          path: '/student/exam-detail/taking/' + exam.id,
          query: examUserId ? { examUserId } : {}
        })
      })
    },
    submitPractice() {
      const payload = Object.assign({}, this.practiceForm, {
        title: this.practiceForm.title || '自主练习记录',
        credit: this.practiceForm.score >= 60 ? 0.5 : 0
      })
      recordPractice(payload).then(() => {
        this.$modal.msgSuccess('练习记录已保存')
        this.practiceForm = { title: '', score: 100, durationMinutes: 10, credit: 0 }
        this.loadSummary()
        this.loadPracticeRecords()
      })
    },
    submitQa() {
      this.$refs.qaForm.validate(valid => {
        if (!valid) return
        askTrainingQuestion(this.qaForm).then(() => {
          this.$modal.msgSuccess('问题已提交')
          this.qaOpen = false
          this.qaForm = { title: '', content: '' }
          this.loadQa()
          this.loadSummary()
        })
      })
    },
    submitAttachment() {
      if (!this.attachmentForm.fileUrl) {
        this.$modal.msgError('请先上传附件')
        return
      }
      const fileUrl = this.attachmentForm.fileUrl.split(',')[0]
      const payload = Object.assign({}, this.attachmentForm, {
        fileUrl,
        fileName: this.getFileName(fileUrl),
        fileType: this.getFileExt(fileUrl)
      })
      addStudentAttachment(payload).then(() => {
        this.$modal.msgSuccess('附件已保存')
        this.attachmentForm.fileUrl = ''
        this.loadAttachments()
        this.loadSummary()
      })
    },
    openPreview(row, type) {
      if (!row) return
      this.previewItem = row
      this.previewLoadError = false
      this.previewTitle = type === 'certificate' ? '证书查看' : '内容预览'
      this.previewOpen = true
      if (!this.hasPreviewContent(row)) {
        this.$modal.msgWarning(type === 'certificate' ? '证书文件未上传或不可访问' : '该资源暂无可查看内容')
      }
    },
    resetPreviewState() {
      this.previewItem = null
      this.previewLoadError = false
    },
    openFile(row) {
      const url = this.getPrimaryUrl(row)
      if (!url) {
        this.$modal.msgWarning('暂无可打开的文件')
        return
      }
      window.open(this.getResourceUrl(url), '_blank')
    },
    downloadResource(row) {
      if (!this.canDownload(row)) {
        this.$modal.msgWarning('该条目暂无可下载文件')
        return
      }
      const fileUrl = row.fileUrl
      if (/^https?:\/\//i.test(fileUrl) && this.normalizeResourcePath(fileUrl) === fileUrl) {
        window.open(fileUrl, '_blank')
        return
      }
      this.$download.resource(this.normalizeResourcePath(fileUrl))
    },
    sanitizeContent(content) {
      const sanitized = sanitizeRichText(content)
      if (!sanitized) {
        return ''
      }
      const wrapper = document.createElement('div')
      wrapper.innerHTML = sanitized
      wrapper.querySelectorAll('img[src], a[href]').forEach(node => {
        const attr = node.tagName === 'IMG' ? 'src' : 'href'
        const value = node.getAttribute(attr)
        if (value && value.startsWith('/profile/')) {
          node.setAttribute(attr, this.getResourceUrl(value))
        }
      })
      return wrapper.innerHTML
    },
    hasRichContent(content) {
      const sanitized = sanitizeRichText(content)
      return !!stripHtml(sanitized).trim() || /<(img|a|ul|ol|li|blockquote|pre|code)\b/i.test(sanitized)
    },
    hasPreviewContent(row) {
      return !!row && (this.hasRichContent(row.content) || !!this.getPrimaryUrl(row))
    },
    getPrimaryUrl(row) {
      if (!row) return ''
      return row.fileUrl || row.coverUrl || row.thumbnailUrl || ''
    },
    canDownload(row) {
      return !!(row && row.fileUrl)
    },
    getResourceUrl(url) {
      if (!url) return ''
      const normalized = this.normalizeResourcePath(url)
      if (/^https?:\/\//i.test(normalized)) {
        return normalized
      }
      const base = process.env.VUE_APP_BASE_API || ''
      return normalized.startsWith('/') ? base + normalized : base + '/' + normalized
    },
    normalizeResourcePath(url) {
      if (!url) return ''
      const value = String(url).trim()
      const base = process.env.VUE_APP_BASE_API || ''
      if (base && value.startsWith(base + '/')) {
        return value.substring(base.length)
      }
      if (/^https?:\/\//i.test(value)) {
        try {
          const parsed = new URL(value)
          const profileIndex = parsed.pathname.indexOf('/profile/')
          if (profileIndex >= 0) {
            return parsed.pathname.substring(profileIndex) + parsed.search
          }
        } catch (e) {
          return value
        }
        return value
      }
      return value.startsWith('/') ? value : '/' + value
    },
    getFileName(url) {
      const value = String(url || '').split('?')[0].split('#')[0]
      return value.split('/').pop() || ''
    },
    getFileExt(url) {
      const name = this.getFileName(url)
      return name.includes('.') ? name.split('.').pop().toLowerCase() : ''
    },
    formatLearningDuration(seconds) {
      const total = Math.max(0, Number(seconds || 0))
      const minutes = Math.floor(total / 60)
      const remainSeconds = total % 60
      if (minutes <= 0) return `${remainSeconds} 秒`
      return `${minutes} 分 ${String(remainSeconds).padStart(2, '0')} 秒`
    },
    getFileType(row) {
      const resourceType = String((row && row.resourceType) || '').toLowerCase()
      if (['image', 'video', 'pdf'].includes(resourceType)) {
        return resourceType
      }
      const ext = this.getFileExt(this.getPrimaryUrl(row))
      if (['jpg', 'jpeg', 'png', 'gif', 'webp', 'bmp'].includes(ext)) return 'image'
      if (['mp4', 'webm', 'ogg', 'mov', 'm4v'].includes(ext)) return 'video'
      if (ext === 'pdf') return 'pdf'
      return ext || resourceType
    },
    isImageFile(row) {
      return this.getFileType(row) === 'image'
    },
    isVideoFile(row) {
      return this.getFileType(row) === 'video'
    },
    isPdfFile(row) {
      return this.getFileType(row) === 'pdf'
    },
    getItemTypeLabel(row) {
      const type = row && row.itemType
      const resourceType = row && row.resourceType
      const labels = {
        course: '课程',
        lesson: '课件',
        resource: '资料',
        media_resource: resourceType === 'video' ? '视频' : '媒体',
        certificate: '证书',
        practice: '练习'
      }
      return labels[type] || resourceType || '资源'
    },
    getProcessingStatusLabel(status) {
      const labels = {
        none: '无需处理',
        ready: '可用',
        skipped: '已跳过',
        failed: '处理失败'
      }
      return labels[status] || status || '无需处理'
    },
    getProcessingStatusType(status) {
      if (status === 'ready') return 'success'
      if (status === 'failed') return 'danger'
      if (status === 'skipped') return 'warning'
      return 'info'
    },
    getExamStatusText(status) {
      const labels = {
        0: '草稿',
        1: '已发布',
        2: '进行中',
        3: '已结束',
        4: '已取消'
      }
      return labels[status] || status || '-'
    },
    getExamStatusType(status) {
      if (String(status) === '2') return 'success'
      if (String(status) === '1') return 'primary'
      if (String(status) === '3') return 'info'
      if (String(status) === '4') return 'danger'
      return 'info'
    }
  }
}
</script>

<style scoped>
.student-training {
  padding: 20px;
}
.training-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  margin-bottom: 16px;
}
.training-header h2 {
  margin: 0;
  font-size: 24px;
  color: #303133;
}
.training-header p {
  margin: 8px 0 0;
  color: #909399;
}
.summary-row {
  margin-bottom: 16px;
}
.summary-card,
.course-card,
.practice-panel,
.practice-section,
.attachment-form {
  border: 1px solid #e4e7ed;
  border-radius: 6px;
  background: #fff;
  padding: 14px 16px;
  margin-bottom: 16px;
}
.summary-value {
  font-size: 24px;
  line-height: 30px;
  color: #303133;
  font-weight: 600;
}
.summary-label {
  margin-top: 6px;
  color: #909399;
  font-size: 13px;
}
.course-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 8px;
}
.course-summary {
  color: #606266;
  min-height: 42px;
  line-height: 21px;
}
.course-meta,
.detail-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  color: #909399;
  margin: 12px 0;
  font-size: 13px;
}
.course-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
.section-title-row {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
  margin-bottom: 12px;
}
.section-title {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}
.section-desc {
  margin-top: 4px;
  color: #909399;
  font-size: 13px;
}
.player-panel {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 280px;
  gap: 18px;
}
.player-main,
.player-side {
  min-width: 0;
}
.player-stage {
  min-height: 360px;
  border-radius: 6px;
  background: #111827;
  overflow: hidden;
}
.player-stage .el-image,
.player-stage video,
.player-stage iframe {
  display: block;
  width: 100%;
  height: 360px;
  border: 0;
  background: #111827;
}
.player-placeholder {
  height: 360px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  color: #d1d5db;
}
.player-placeholder i {
  font-size: 42px;
}
.player-controls {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 12px;
}
.timer-text {
  color: #606266;
  font-size: 13px;
}
.player-side {
  border: 1px solid #ebeef5;
  border-radius: 6px;
  padding: 14px;
  background: #fff;
}
.progress-desc {
  margin-top: 8px;
  color: #909399;
  font-size: 13px;
  line-height: 20px;
}
.detail-title,
.preview-name {
  font-size: 18px;
  line-height: 26px;
  font-weight: 600;
  color: #303133;
}
.detail-summary,
.preview-desc {
  margin-top: 8px;
  color: #606266;
  line-height: 22px;
}
.content-section {
  margin-top: 18px;
}
.rich-text {
  margin-top: 10px;
  padding: 14px;
  border: 1px solid #ebeef5;
  border-radius: 6px;
  background: #fff;
  color: #303133;
  line-height: 1.7;
}
.rich-text >>> img {
  max-width: 100%;
  height: auto;
}
.inline-media,
.preview-file {
  margin-top: 10px;
  border: 1px solid #ebeef5;
  border-radius: 6px;
  background: #f8fafc;
  min-height: 220px;
  overflow: hidden;
}
.inline-media .el-image,
.preview-file .el-image {
  display: block;
  width: 100%;
  min-height: 220px;
  max-height: 520px;
}
.inline-media video,
.preview-file video {
  display: block;
  width: 100%;
  max-height: 520px;
  background: #000;
}
.inline-media iframe,
.preview-file iframe {
  display: block;
  width: 100%;
  height: 520px;
  border: 0;
  background: #fff;
}
.unsupported-file {
  min-height: 220px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  color: #606266;
}
.preview-meta {
  padding-bottom: 12px;
  border-bottom: 1px solid #ebeef5;
}
.preview-tip {
  margin-top: 8px;
  color: #909399;
  font-size: 13px;
}
@media (max-width: 768px) {
  .training-header,
  .section-title-row,
  .player-panel {
    align-items: flex-start;
    flex-direction: column;
  }
  .player-panel {
    display: block;
  }
  .player-side {
    margin-top: 12px;
  }
  .player-stage,
  .player-stage .el-image,
  .player-stage video,
  .player-stage iframe,
  .player-placeholder {
    height: 240px;
    min-height: 240px;
  }
}
</style>
