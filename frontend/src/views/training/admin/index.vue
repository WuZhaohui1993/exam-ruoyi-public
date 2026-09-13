<template>
  <div class="app-container training-admin">
    <el-row :gutter="16" class="summary-row">
      <el-col :xs="12" :sm="8" :md="4" v-for="item in summaryCards" :key="item.key">
        <div class="summary-card">
          <div class="summary-value">{{ item.value }}</div>
          <div class="summary-label">{{ item.label }}</div>
        </div>
      </el-col>
    </el-row>

    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="类型" prop="itemType">
        <el-select v-model="queryParams.itemType" clearable placeholder="全部类型" style="width: 180px">
          <el-option v-for="item in itemTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="标题" prop="title">
        <el-input v-model="queryParams.title" placeholder="请输入标题" clearable style="width: 220px" @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" clearable placeholder="全部状态" style="width: 160px">
          <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleAdd" v-hasPermi="['training:item:add']">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain icon="el-icon-medal" size="mini" @click="handleIssueCertificate" v-hasPermi="['training:item:add']">发证</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="info" plain icon="el-icon-document" size="mini" @click="attachmentOpen = true" v-hasPermi="['training:attachment:list']">附件</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="primary" plain icon="el-icon-notebook-2" size="mini" @click="openLearningLedger" v-hasPermi="['training:item:list']">学习台账</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="el-icon-download" size="mini" @click="handleExport" v-hasPermi="['training:item:export']">导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="itemList">
      <el-table-column label="标题" prop="title" min-width="220" show-overflow-tooltip />
      <el-table-column label="所属课程" prop="parentId" width="150">
        <template slot-scope="scope">
          <span v-if="scope.row.parentId">{{ getCourseLabel(scope.row.parentId) }}</span>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="类型" prop="itemType" width="120">
        <template slot-scope="scope">{{ getTypeLabel(scope.row.itemType) }}</template>
      </el-table-column>
      <el-table-column label="状态" prop="status" width="110">
        <template slot-scope="scope">
          <el-tag size="small" :type="getStatusTag(scope.row.status)">{{ getStatusLabel(scope.row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="资源" prop="resourceType" width="100" />
      <el-table-column label="媒体处理" prop="processingStatus" width="130">
        <template slot-scope="scope">
          <el-tag v-if="scope.row.processingStatus && scope.row.processingStatus !== 'none'" size="small" :type="scope.row.processingStatus === 'ready' ? 'success' : scope.row.processingStatus === 'failed' ? 'danger' : 'warning'">
            {{ scope.row.processingStatus }}
          </el-tag>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="学分" prop="credit" width="90" />
      <el-table-column label="关联考试" prop="relatedExamId" width="100">
        <template slot-scope="scope">
          <el-tag v-if="scope.row.relatedExamId" size="small" type="warning">{{ scope.row.relatedExamId }}</el-tag>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="学习人数" prop="learnerCount" width="90" />
      <el-table-column label="完成人数" prop="completedCount" width="90" />
      <el-table-column label="创建时间" prop="createTime" width="160">
        <template slot-scope="scope">{{ parseTime(scope.row.createTime, '{y}-{m}-{d} {h}:{i}') }}</template>
      </el-table-column>
      <el-table-column label="操作" width="280" align="center" fixed="right">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)" v-hasPermi="['training:item:edit']">修改</el-button>
          <el-button v-if="scope.row.itemType === 'qa'" size="mini" type="text" icon="el-icon-chat-dot-round" @click="handleReply(scope.row)" v-hasPermi="['training:item:edit']">回复</el-button>
          <el-button v-if="scope.row.itemType === 'media_resource'" size="mini" type="text" icon="el-icon-video-play" @click="handleProcess(scope.row)" v-hasPermi="['training:item:edit']">处理</el-button>
          <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)" v-hasPermi="['training:item:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="title" :visible.sync="open" width="760px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="96px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="类型" prop="itemType">
              <el-select v-model="form.itemType" placeholder="请选择类型" style="width: 100%" @change="handleItemTypeChange">
                <el-option v-for="item in itemTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-select v-model="form.status" placeholder="请选择状态" style="width: 100%">
                <el-option v-for="item in formStatusOptions" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="标题" prop="title">
              <el-input v-model="form.title" placeholder="请输入标题" maxlength="120" show-word-limit />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="摘要" prop="summary">
              <el-input v-model="form.summary" type="textarea" :rows="2" maxlength="500" show-word-limit />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="所属课程" prop="parentId">
              <el-select v-model="form.parentId" :disabled="!needsCourseParent" clearable filterable placeholder="选择课程" style="width: 100%">
                <el-option v-for="item in courseOptions" :key="item.id" :label="item.title" :value="item.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="资源类型" prop="resourceType">
              <el-select v-model="form.resourceType" clearable placeholder="请选择资源类型" style="width: 100%">
                <el-option label="文档" value="document" />
                <el-option label="图片" value="image" />
                <el-option label="视频" value="video" />
                <el-option label="链接" value="link" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="关联考试" prop="relatedExamId">
              <div class="reference-picker">
                <el-input
                  :value="selectedExamLabel"
                  :disabled="form.itemType !== 'course'"
                  readonly
                  placeholder="请选择课后考试"
                  class="reference-picker-input"
                  @focus="openExamSelector"
                />
                <el-button type="primary" icon="el-icon-search" :disabled="form.itemType !== 'course'" @click="openExamSelector">选择</el-button>
                <el-button icon="el-icon-delete" :disabled="form.itemType !== 'course' || !form.relatedExamId" @click="clearSelectedExam">清除</el-button>
              </div>
            </el-form-item>
          </el-col>
          <el-col v-if="needsUserSelector" :span="12">
            <el-form-item label="关联学员" prop="userIds">
              <user-select ref="trainingUserSelect" v-model="form.userIds" :multiple="true" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="学分" prop="credit">
              <el-input-number v-model="form.credit" :min="0" :precision="2" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="目标时长" prop="durationMinutes">
              <el-input-number v-model="form.durationMinutes" :min="0" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="排序" prop="sortOrder">
              <el-input-number v-model="form.sortOrder" :min="0" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="文件地址" prop="fileUrl">
              <file-upload v-model="form.fileUrl" :limit="1" :file-size="50" :file-type="fileTypes" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="封面地址" prop="coverUrl">
              <image-upload v-model="form.coverUrl" :limit="1" :file-size="5" :file-type="['png','jpg','jpeg','webp']" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="内容" prop="content">
              <editor v-model="form.content" :min-height="180" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>

    <el-dialog title="回复问答" :visible.sync="replyOpen" width="620px" append-to-body>
      <el-form label-width="80px">
        <el-form-item label="问题">
          <div class="readonly-text">{{ replyRow.title }}</div>
        </el-form-item>
        <el-form-item label="回复">
          <editor v-model="replyForm.answerContent" :min-height="160" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitReply">确 定</el-button>
        <el-button @click="replyOpen = false">取 消</el-button>
      </div>
    </el-dialog>

    <el-dialog title="发放证书" :visible.sync="certificateOpen" width="620px" append-to-body>
      <el-form ref="certificateForm" :model="certificateForm" :rules="certificateRules" label-width="96px">
        <el-form-item label="发放学员" prop="userIds">
          <user-select ref="certificateUserSelect" v-model="certificateForm.userIds" :multiple="true" />
        </el-form-item>
        <el-form-item label="证书名称" prop="title">
          <el-input v-model="certificateForm.title" placeholder="请输入证书名称" />
        </el-form-item>
        <el-form-item label="证书文件">
          <file-upload v-model="certificateForm.fileUrl" :limit="1" :file-size="20" :file-type="['pdf','png','jpg','jpeg']" />
        </el-form-item>
        <el-form-item label="学分">
          <el-input-number v-model="certificateForm.credit" :min="0" :precision="2" controls-position="right" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitCertificate">确 定</el-button>
        <el-button @click="certificateOpen = false">取 消</el-button>
      </div>
    </el-dialog>

    <el-dialog title="作答附件" :visible.sync="attachmentOpen" width="980px" append-to-body @open="getAttachmentList">
      <el-table v-loading="attachmentLoading" :data="attachmentList">
        <el-table-column label="文件名" prop="fileName" min-width="220" show-overflow-tooltip />
        <el-table-column label="操作" width="120" align="center">
          <template slot-scope="scope">
            <el-button v-if="scope.row.fileUrl" type="text" size="mini" @click="openFile(scope.row.fileUrl)">查看</el-button>
            <el-button type="text" size="mini" class="danger-link" @click="handleDeleteAttachment(scope.row)" v-hasPermi="['training:attachment:remove']">删除</el-button>
          </template>
        </el-table-column>
        <el-table-column label="学员ID" prop="userId" width="90" />
        <el-table-column label="考试ID" prop="examId" width="90" />
        <el-table-column label="考试记录" prop="examUserId" width="100" />
        <el-table-column label="来源" prop="uploadSource" width="90" />
        <el-table-column label="上传时间" prop="createTime" width="160">
          <template slot-scope="scope">{{ parseTime(scope.row.createTime, '{y}-{m}-{d} {h}:{i}') }}</template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <el-dialog title="学员学习台账" :visible.sync="ledgerOpen" width="1120px" append-to-body @open="getLearningLedger">
      <el-form :model="ledgerQuery" size="small" :inline="true" label-width="80px" class="ledger-query">
        <el-form-item label="课程">
          <el-select v-model="ledgerQuery.parentId" clearable filterable placeholder="全部课程" style="width: 220px">
            <el-option v-for="item in courseOptions" :key="item.id" :label="item.title" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="学员">
          <el-input v-model="ledgerQuery.userName" clearable placeholder="账号或姓名" style="width: 180px" @keyup.enter.native="handleLedgerQuery" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="ledgerQuery.status" clearable placeholder="全部状态" style="width: 140px">
            <el-option label="学习中" value="learning" />
            <el-option label="已完成" value="completed" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="el-icon-search" size="mini" @click="handleLedgerQuery">搜索</el-button>
          <el-button icon="el-icon-refresh" size="mini" @click="resetLedgerQuery">重置</el-button>
        </el-form-item>
      </el-form>
      <el-table v-loading="ledgerLoading" :data="ledgerList" empty-text="暂无学习记录">
        <el-table-column label="学员" min-width="180" show-overflow-tooltip>
          <template slot-scope="scope">{{ scope.row.userNickName || scope.row.userName || '-' }} / #{{ scope.row.userId }}</template>
        </el-table-column>
        <el-table-column label="课程" prop="courseTitle" min-width="220" show-overflow-tooltip />
        <el-table-column label="状态" width="100">
          <template slot-scope="scope">
            <el-tag size="small" :type="scope.row.status === 'completed' ? 'success' : 'warning'">{{ getStatusLabel(scope.row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="学习时长" width="110">
          <template slot-scope="scope">{{ scope.row.durationMinutes || 0 }} 分钟</template>
        </el-table-column>
        <el-table-column label="目标时长" width="110">
          <template slot-scope="scope">{{ scope.row.targetDurationMinutes || 0 }} 分钟</template>
        </el-table-column>
        <el-table-column label="达标率" width="180">
          <template slot-scope="scope">
            <el-progress :percentage="formatProgress(scope.row.progressPercent)" :status="scope.row.status === 'completed' ? 'success' : null" />
          </template>
        </el-table-column>
        <el-table-column label="是否达标" width="100">
          <template slot-scope="scope">
            <el-tag size="small" :type="isLearningQualified(scope.row) ? 'success' : 'info'">{{ isLearningQualified(scope.row) ? '达标' : '未达标' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="学分" prop="credit" width="90" />
        <el-table-column label="开始时间" prop="startTime" width="160">
          <template slot-scope="scope">{{ parseTime(scope.row.startTime, '{y}-{m}-{d} {h}:{i}') || '-' }}</template>
        </el-table-column>
        <el-table-column label="完成时间" prop="finishTime" width="160">
          <template slot-scope="scope">{{ parseTime(scope.row.finishTime, '{y}-{m}-{d} {h}:{i}') || '-' }}</template>
        </el-table-column>
        <el-table-column label="更新时间" prop="updateTime" width="160">
          <template slot-scope="scope">{{ parseTime(scope.row.updateTime || scope.row.createTime, '{y}-{m}-{d} {h}:{i}') || '-' }}</template>
        </el-table-column>
      </el-table>
      <pagination v-show="ledgerTotal > 0" :total="ledgerTotal" :page.sync="ledgerQuery.pageNum" :limit.sync="ledgerQuery.pageSize" @pagination="getLearningLedger" />
    </el-dialog>

    <el-dialog title="选择关联考试" :visible.sync="examSelectorOpen" width="860px" append-to-body>
      <el-form :model="examSelectorQuery" size="small" :inline="true" label-width="80px">
        <el-form-item label="考试">
          <el-input v-model="examSelectorQuery.keyword" clearable placeholder="考试名称或ID" style="width: 260px" @keyup.enter.native="handleExamSelectorQuery" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="el-icon-search" size="mini" @click="handleExamSelectorQuery">搜索</el-button>
          <el-button icon="el-icon-refresh" size="mini" @click="resetExamSelectorQuery">重置</el-button>
        </el-form-item>
      </el-form>
      <el-table
        v-loading="examSelectorLoading"
        :data="examSelectorList"
        height="360"
        highlight-current-row
        empty-text="暂无可关联考试"
        @row-click="handleExamSelectorRowClick"
      >
        <el-table-column width="54" align="center">
          <template slot-scope="scope">
            <el-radio v-model="examSelectorCurrentId" :label="scope.row.id">&nbsp;</el-radio>
          </template>
        </el-table-column>
        <el-table-column label="考试名称" prop="examName" min-width="220" show-overflow-tooltip />
        <el-table-column label="状态" prop="status" width="90">
          <template slot-scope="scope">
            <el-tag size="small" :type="getExamStatusTag(scope.row.status)">{{ getExamStatusLabel(scope.row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="开始时间" prop="startTime" width="160">
          <template slot-scope="scope">{{ parseTime(scope.row.startTime, '{y}-{m}-{d} {h}:{i}') || '-' }}</template>
        </el-table-column>
        <el-table-column label="结束时间" prop="endTime" width="160">
          <template slot-scope="scope">{{ parseTime(scope.row.endTime, '{y}-{m}-{d} {h}:{i}') || '-' }}</template>
        </el-table-column>
      </el-table>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="confirmExamSelect">确 定</el-button>
        <el-button @click="examSelectorOpen = false">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import UserSelect from '@/components/UserSelect'
import {
  listTrainingItem,
  listTrainingLearningLedger,
  getTrainingItem,
  addTrainingItem,
  updateTrainingItem,
  delTrainingItem,
  getTrainingSummary,
  listTrainingCourseOptions,
  listTrainingExamOptions,
  replyTrainingQuestion,
  issueCertificate,
  processMediaResource,
  listTrainingAttachment,
  delTrainingAttachment
} from '@/api/training/manage'

export default {
  name: 'TrainingAdmin',
  components: {
    UserSelect
  },
  data() {
    return {
      loading: false,
      showSearch: true,
      itemList: [],
      total: 0,
      open: false,
      replyOpen: false,
      certificateOpen: false,
      attachmentOpen: false,
      ledgerOpen: false,
      examSelectorOpen: false,
      attachmentLoading: false,
      ledgerLoading: false,
      examSelectorLoading: false,
      title: '',
      form: {},
      replyRow: {},
      replyForm: { answerContent: '' },
      certificateForm: {},
      attachmentList: [],
      ledgerList: [],
      examSelectorList: [],
      ledgerTotal: 0,
      courseOptions: [],
      examOptions: [],
      summary: {},
      examSelectorCurrentId: null,
      examSelectorCurrentRow: null,
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        itemType: null,
        title: null,
        status: null
      },
      ledgerQuery: {
        pageNum: 1,
        pageSize: 10,
        parentId: null,
        userName: null,
        status: null
      },
      examSelectorQuery: {
        keyword: null
      },
      itemTypeOptions: [
        { label: '课程', value: 'course' },
        { label: '课件', value: 'lesson' },
        { label: '资源', value: 'resource' },
        { label: '媒体资源', value: 'media_resource' },
        { label: '互动问答', value: 'qa' },
        { label: '证书', value: 'certificate' },
        { label: '学分', value: 'credit' },
        { label: '自主练习', value: 'practice' },
        { label: '学习进度', value: 'progress' }
      ],
      statusMap: {
        course: ['draft', 'published'],
        lesson: ['draft', 'published'],
        resource: ['draft', 'published'],
        media_resource: ['draft', 'published'],
        qa: ['open', 'answered'],
        certificate: ['issued'],
        credit: ['completed'],
        practice: ['completed'],
        progress: ['learning', 'completed']
      },
      statusOptions: [
        { label: '草稿', value: 'draft' },
        { label: '已发布', value: 'published' },
        { label: '待回复', value: 'open' },
        { label: '已回复', value: 'answered' },
        { label: '已发放', value: 'issued' },
        { label: '学习中', value: 'learning' },
        { label: '已完成', value: 'completed' }
      ],
      fileTypes: ['pdf', 'doc', 'docx', 'ppt', 'pptx', 'xls', 'xlsx', 'txt', 'png', 'jpg', 'jpeg', 'mp4'],
      rules: {
        itemType: [{ required: true, message: '类型不能为空', trigger: 'change' }],
        title: [{ required: true, message: '标题不能为空', trigger: 'blur' }],
        status: [{ required: true, message: '状态不能为空', trigger: 'change' }]
      },
      certificateRules: {
        userIds: [{ required: true, type: 'array', min: 1, message: '发放学员不能为空', trigger: 'change' }],
        title: [{ required: true, message: '证书名称不能为空', trigger: 'blur' }]
      }
    }
  },
  computed: {
    needsCourseParent() {
      return ['lesson'].includes(this.form.itemType)
    },
    needsUserSelector() {
      return ['qa', 'certificate', 'credit', 'practice', 'progress'].includes(this.form.itemType)
    },
    selectedExamLabel() {
      const exam = this.examOptions.find(item => Number(item.id) === Number(this.form.relatedExamId))
      return exam ? this.formatExamLabel(exam) : (this.form.relatedExamId ? `#${this.form.relatedExamId}` : '')
    },
    formStatusOptions() {
      const allowed = this.statusMap[this.form.itemType]
      if (!allowed) {
        return this.statusOptions
      }
      return this.statusOptions.filter(item => allowed.includes(item.value))
    },
    summaryCards() {
      return [
        { key: 'course', label: '课程', value: this.summary.courseCount || 0 },
        { key: 'resource', label: '资源', value: this.summary.resourceCount || 0 },
        { key: 'qa', label: '问答', value: this.summary.qaCount || 0 },
        { key: 'certificate', label: '证书', value: this.summary.certificateCount || 0 },
        { key: 'credit', label: '学分', value: this.summary.totalCredit || 0 },
        { key: 'openQa', label: '待回复', value: this.summary.openQaCount || 0 },
        { key: 'media', label: '媒体待处理', value: (this.summary.mediaSkippedCount || 0) + (this.summary.mediaFailedCount || 0) },
        { key: 'score', label: '均分', value: this.formatNumber(this.summary.averageScore) }
      ]
    }
  },
  created() {
    this.loadReferenceOptions()
    this.getList()
    this.getSummary()
  },
  methods: {
    getList() {
      this.loading = true
      listTrainingItem(this.queryParams).then(response => {
        this.itemList = response.rows || []
        this.total = response.total || 0
        this.mergeCourseOptions(this.itemList)
        this.loading = false
      }).catch(() => {
        this.loading = false
      })
    },
    getSummary() {
      getTrainingSummary().then(response => {
        this.summary = response.data || {}
      })
    },
    getAttachmentList() {
      this.attachmentLoading = true
      listTrainingAttachment({ pageNum: 1, pageSize: 50 }).then(response => {
        this.attachmentList = response.rows || []
        this.attachmentLoading = false
      }).catch(() => {
        this.attachmentLoading = false
      })
    },
    openLearningLedger() {
      this.ledgerOpen = true
    },
    getLearningLedger() {
      this.ledgerLoading = true
      listTrainingLearningLedger(this.ledgerQuery).then(response => {
        this.ledgerList = response.rows || []
        this.ledgerTotal = response.total || 0
        this.ledgerLoading = false
      }).catch(() => {
        this.ledgerLoading = false
      })
    },
    handleLedgerQuery() {
      this.ledgerQuery.pageNum = 1
      this.getLearningLedger()
    },
    resetLedgerQuery() {
      this.ledgerQuery = {
        pageNum: 1,
        pageSize: 10,
        parentId: null,
        userName: null,
        status: null
      }
      this.getLearningLedger()
    },
    loadReferenceOptions() {
      listTrainingCourseOptions('').then(response => {
        this.courseOptions = response.data || []
      })
      this.searchExams('')
    },
    mergeCourseOptions(rows) {
      const courseMap = new Map(this.courseOptions.map(item => [item.id, item]))
      ;(rows || []).filter(item => item.itemType === 'course').forEach(item => courseMap.set(item.id, item))
      this.courseOptions = Array.from(courseMap.values())
    },
    reset() {
      this.form = {
        id: null,
        itemType: 'course',
        title: null,
        summary: null,
        content: null,
        parentId: 0,
        resourceType: null,
        status: 'draft',
        sortOrder: 0,
        durationMinutes: 0,
        credit: 0,
        fileUrl: null,
        coverUrl: null,
        relatedExamId: null,
        userId: null,
        userIds: []
      }
      this.resetForm('form')
    },
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    resetQuery() {
      this.resetForm('queryForm')
      this.handleQuery()
    },
    handleExport() {
      const { pageNum, pageSize, ...params } = this.queryParams
      this.download('training/manage/export', params, `培训运营_${new Date().getTime()}.xlsx`)
    },
    handleAdd() {
      this.reset()
      this.loadReferenceOptions()
      this.open = true
      this.title = '新增培训条目'
    },
    handleUpdate(row) {
      this.reset()
      getTrainingItem(row.id).then(response => {
        this.form = response.data || {}
        this.form.userIds = this.form.userId ? [this.form.userId] : []
        this.ensureSelectedReferences(this.form)
        this.open = true
        this.title = '修改培训条目'
      })
    },
    submitForm() {
      this.$refs.form.validate(valid => {
        if (!valid) return
        const data = this.buildSubmitPayload()
        const action = this.form.id ? updateTrainingItem : addTrainingItem
        action(data).then(() => {
          this.$modal.msgSuccess('保存成功')
          this.open = false
          this.getList()
          this.getSummary()
          this.loadReferenceOptions()
        })
      })
    },
    cancel() {
      this.open = false
      this.reset()
    },
    handleDelete(row) {
      this.$modal.confirm(`确认删除"${row.title}"吗？`).then(() => delTrainingItem(row.id)).then(() => {
        this.$modal.msgSuccess('删除成功')
        this.getList()
        this.getSummary()
      }).catch(() => {})
    },
    handleReply(row) {
      this.replyRow = row
      this.replyForm = { answerContent: row.answerContent || '' }
      this.replyOpen = true
    },
    submitReply() {
      replyTrainingQuestion(this.replyRow.id, this.replyForm).then(() => {
        this.$modal.msgSuccess('回复成功')
        this.replyOpen = false
        this.getList()
      })
    },
    handleIssueCertificate() {
      this.certificateForm = {
        userId: null,
        userIds: [],
        title: null,
        fileUrl: null,
        credit: 0
      }
      this.certificateOpen = true
    },
    submitCertificate() {
      this.$refs.certificateForm.validate(valid => {
        if (!valid) return
        issueCertificate(this.certificateForm).then(() => {
          this.$modal.msgSuccess('证书已发放')
          this.certificateOpen = false
          this.getList()
          this.getSummary()
        })
      })
    },
    handleItemTypeChange(value) {
      const allowedStatus = this.statusMap[value] || []
      if (allowedStatus.length > 0 && !allowedStatus.includes(this.form.status)) {
        this.form.status = allowedStatus[0]
      }
      if (!this.needsCourseParent) {
        this.form.parentId = 0
      }
      if (value !== 'course') {
        this.form.relatedExamId = null
      }
      if (!this.needsUserSelector) {
        this.form.userId = null
        this.form.userIds = []
      }
      if (!['lesson', 'resource', 'media_resource'].includes(value)) {
        this.form.resourceType = null
        this.form.fileUrl = null
      }
    },
    handleProcess(row) {
      if (row.itemType !== 'media_resource') {
        this.$modal.msgWarning('只有媒体资源需要处理')
        return
      }
      processMediaResource(row.id).then(() => {
        this.$modal.msgSuccess('媒体处理已执行')
        this.getList()
      })
    },
    handleDeleteAttachment(row) {
      this.$modal.confirm(`确认删除附件"${row.fileName}"吗？`).then(() => delTrainingAttachment(row.id)).then(() => {
        this.$modal.msgSuccess('删除成功')
        this.getAttachmentList()
        this.getSummary()
      }).catch(() => {})
    },
    searchExams(keyword) {
      listTrainingExamOptions(keyword || '').then(response => {
        this.examOptions = response.data || []
      })
    },
    ensureSelectedReferences(row) {
      if (row.relatedExamId && !this.examOptions.some(item => item.id === row.relatedExamId)) {
        listTrainingExamOptions(String(row.relatedExamId)).then(response => {
          this.examOptions = this.examOptions.concat(response.data || [])
        })
      }
    },
    buildSubmitPayload() {
      const data = { ...this.form }
      if (!this.needsCourseParent) {
        data.parentId = 0
      }
      if (data.itemType !== 'course') {
        data.relatedExamId = null
      }
      data.userIds = this.needsUserSelector ? (data.userIds || []) : []
      data.userId = data.userIds.length > 0 ? data.userIds[0] : data.userId
      if (!this.needsUserSelector) {
        data.userId = null
        data.userIds = []
      }
      return data
    },
    getTypeLabel(value) {
      const item = this.itemTypeOptions.find(option => option.value === value)
      return item ? item.label : value
    },
    getCourseLabel(id) {
      const item = this.courseOptions.find(option => option.id === id)
      return item ? item.title : `#${id}`
    },
    formatExamLabel(item) {
      return `#${item.id} ${item.examName || ''}`
    },
    openExamSelector() {
      if (this.form.itemType !== 'course') return
      this.examSelectorOpen = true
      this.examSelectorCurrentId = this.form.relatedExamId
      this.examSelectorCurrentRow = this.examOptions.find(item => Number(item.id) === Number(this.form.relatedExamId)) || null
      this.getExamSelectorList()
    },
    getExamSelectorList() {
      this.examSelectorLoading = true
      listTrainingExamOptions(this.examSelectorQuery.keyword || '').then(response => {
        this.examSelectorList = response.data || []
        this.mergeExamOptions(this.examSelectorList)
        this.examSelectorLoading = false
      }).catch(() => {
        this.examSelectorLoading = false
      })
    },
    mergeExamOptions(rows) {
      const examMap = new Map(this.examOptions.map(item => [Number(item.id), item]))
      ;(rows || []).forEach(item => examMap.set(Number(item.id), item))
      this.examOptions = Array.from(examMap.values())
    },
    handleExamSelectorQuery() {
      this.getExamSelectorList()
    },
    resetExamSelectorQuery() {
      this.examSelectorQuery.keyword = null
      this.getExamSelectorList()
    },
    handleExamSelectorRowClick(row) {
      this.examSelectorCurrentId = row.id
      this.examSelectorCurrentRow = row
    },
    confirmExamSelect() {
      if (!this.examSelectorCurrentId) {
        this.$modal.msgWarning('请选择关联考试')
        return
      }
      this.form.relatedExamId = this.examSelectorCurrentId
      if (this.examSelectorCurrentRow) {
        this.mergeExamOptions([this.examSelectorCurrentRow])
      }
      this.examSelectorOpen = false
    },
    clearSelectedExam() {
      this.form.relatedExamId = null
      this.examSelectorCurrentId = null
      this.examSelectorCurrentRow = null
    },
    getExamStatusLabel(value) {
      const map = {
        '0': '草稿',
        '1': '已发布',
        '2': '进行中',
        '3': '已结束',
        '4': '已取消'
      }
      return map[String(value)] || value
    },
    getExamStatusTag(value) {
      if (String(value) === '1') return 'success'
      if (String(value) === '2') return 'warning'
      if (String(value) === '3') return 'info'
      if (String(value) === '4') return 'danger'
      return 'info'
    },
    openFile(url) {
      if (!url) return
      const base = process.env.VUE_APP_BASE_API
      const fullUrl = /^https?:\/\//.test(url) ? url : base + url
      window.open(fullUrl, '_blank')
    },
    getStatusLabel(value) {
      const item = this.statusOptions.find(option => option.value === value)
      return item ? item.label : value
    },
    getStatusTag(value) {
      if (['published', 'answered', 'issued', 'completed'].includes(value)) return 'success'
      if (value === 'open' || value === 'learning') return 'warning'
      return 'info'
    },
    formatNumber(value) {
      if (value === null || value === undefined) return 0
      const number = Number(value)
      return Number.isNaN(number) ? value : number.toFixed(1)
    },
    formatProgress(value) {
      const number = Number(value || 0)
      if (Number.isNaN(number)) return 0
      return Math.max(0, Math.min(100, Number(number.toFixed(1))))
    },
    isLearningQualified(row) {
      if (!row) return false
      if (row.status === 'completed') return true
      const target = Number(row.targetDurationMinutes || 0)
      if (target <= 0) return Number(row.durationMinutes || 0) > 0
      return Number(row.durationMinutes || 0) >= target
    }
  }
}
</script>

<style scoped>
.summary-row {
  margin-bottom: 16px;
}
.summary-card {
  min-height: 76px;
  border: 1px solid #e4e7ed;
  border-radius: 6px;
  padding: 14px 16px;
  background: #fff;
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
.readonly-text {
  min-height: 36px;
  line-height: 22px;
  color: #303133;
}
.reference-picker {
  display: flex;
  align-items: center;
  gap: 8px;
}
.reference-picker-input {
  flex: 1;
  min-width: 0;
}
</style>
