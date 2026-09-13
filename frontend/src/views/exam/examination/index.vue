<template>
  <div class="app-container">
    <!-- 搜索条件 -->
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="考试名称" prop="examName">
        <el-input
          v-model="queryParams.examName"
          placeholder="请输入考试名称"
          clearable
          style="width: 200px"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="考试分类" prop="categoryId">
        <div style="width: 200px">
          <examination-category-tree-select v-model="queryParams.categoryId" placeholder="请选择考试分类" />
        </div>
      </el-form-item>
      <el-form-item label="考试状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="考试状态" clearable style="width: 150px">
          <el-option label="草稿" value="0" />
          <el-option label="已发布" value="1" />
          <el-option label="进行中" value="2" />
          <el-option label="已结束" value="3" />
          <el-option label="已取消" value="4" />
        </el-select>
      </el-form-item>
      <el-form-item label="启用状态" prop="delFlag">
        <el-select v-model="queryParams.delFlag" placeholder="启用状态" clearable style="width: 120px">
          <el-option
            v-for="dict in dict.type.sys_normal_disable"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 工具栏 -->
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="el-icon-plus"
          size="mini"
          @click="handleAdd"
          v-hasPermi="['exam:examination:add']"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="el-icon-edit"
          size="mini"
          :disabled="single || !canUpdateSelection"
          @click="handleUpdate"
          v-hasPermi="['exam:examination:edit']"
        >修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="el-icon-delete"
          size="mini"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['exam:examination:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
          v-hasPermi="['exam:examination:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <!-- 数据表格 -->
    <el-table v-loading="loading" :data="examinationList" @selection-change="handleSelectionChange" :default-sort="defaultSort" @sort-change="handleSortChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="考试名称" prop="examName" :show-overflow-tooltip="true" min-width="150" />
      <el-table-column label="考试分类" prop="categoryName" width="100" />
      <el-table-column label="试卷" prop="paperName" width="120" :show-overflow-tooltip="true" />
      <el-table-column label="考试时长" prop="duration" width="90" align="center" sortable="custom" :sort-orders="['descending', 'ascending']">
        <template slot-scope="scope">
          {{ scope.row.duration }} 分钟
        </template>
      </el-table-column>
      <el-table-column label="参考记录" prop="registeredCount" width="90" align="center" sortable="custom" :sort-orders="['descending', 'ascending']" />
      <el-table-column label="已交卷" prop="submittedCount" width="80" align="center" />
      <el-table-column label="已通过" prop="passedCount" width="90" align="center" />
      <el-table-column label="通过率" width="80" align="center">
        <template slot-scope="scope">
          <span v-if="scope.row.submittedCount > 0">
            {{ ((scope.row.passedCount || 0) / scope.row.submittedCount * 100).toFixed(1) }}%
          </span>
          <span v-else>0%</span>
        </template>
      </el-table-column>
      <el-table-column label="考试状态" prop="status" width="90" align="center">
        <template slot-scope="scope">
          <el-tag v-if="scope.row.status === '0'" type="info" size="small">草稿</el-tag>
          <el-tag v-else-if="scope.row.status === '1'" type="success" size="small">已发布</el-tag>
          <el-tag v-else-if="scope.row.status === '2'" type="warning" size="small">进行中</el-tag>
          <el-tag v-else-if="scope.row.status === '3'" type="info" size="small">已结束</el-tag>
          <el-tag v-else-if="scope.row.status === '4'" type="danger" size="small">已取消</el-tag>
          <el-tag v-else type="info" size="small">{{ scope.row.status }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="开始时间" align="center" prop="startTime" width="150" sortable="custom" :sort-orders="['descending', 'ascending']">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.startTime, '{y}-{m}-{d} {h}:{i}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="启用状态" prop="delFlag" width="80" align="center">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.sys_normal_disable" :value="scope.row.delFlag || '0'"/>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="360">
        <template slot-scope="scope">
          <div class="table-actions">
            <!-- 基础操作 -->
            <el-button
              size="mini"
              type="text"
              icon="el-icon-view"
              @click="handleView(scope.row)"
              v-hasPermi="['exam:examination:detail']"
            >详情</el-button>
            <el-button
              size="mini"
              type="text"
              icon="el-icon-user"
              @click="handleUsers(scope.row)"
              v-hasPermi="['exam:examination:detail']"
            >人员</el-button>
            <el-button
              size="mini"
              type="text"
              icon="el-icon-edit"
              @click="handleUpdate(scope.row)"
              v-hasPermi="['exam:examination:edit']"
              v-if="canEditExam(scope.row) || scope.row.status === '2'"
            >修改</el-button>
            <el-button
              size="mini"
              type="text"
              icon="el-icon-s-data"
              @click="handleScoreAnalysis(scope.row)"
              v-hasPermi="['exam:examination:statistics']"
            >统计</el-button>
            <el-button
              size="mini"
              type="text"
              icon="el-icon-office-building"
              @click="handleAuthScope(scope.row)"
              v-if="scope.row.status === '2'"
              v-hasPermi="['exam:examination:edit']"
            >授权</el-button>

            <!-- 生命周期主操作 -->
            <el-button
              size="mini"
              type="text"
              icon="el-icon-upload2"
              @click="handlePublishExam(scope.row.id)"
              v-if="scope.row.status === '0'"
              v-hasPermi="['exam:examination:publish']"
            >发布</el-button>
            <el-button
              size="mini"
              type="text"
              icon="el-icon-video-play"
              @click="handleStartExam(scope.row.id)"
              v-if="scope.row.status === '1'"
              v-hasPermi="['exam:examination:start', 'exam:examination:manage']"
            >开始</el-button>
            <el-button
              size="mini"
              type="text"
              icon="el-icon-video-pause"
              @click="handleEndExam(scope.row.id)"
              v-if="scope.row.status === '2'"
              v-hasPermi="['exam:examination:end', 'exam:examination:manage']"
            >结束</el-button>
            <el-button
              size="mini"
              type="text"
              icon="el-icon-refresh-right"
              @click="handleRepublishExam(scope.row.id, scope.row)"
              v-if="scope.row.status === '3' || scope.row.status === '4'"
              v-hasPermi="['exam:examination:publish']"
            >重发</el-button>

            <!-- 辅助操作下拉菜单 -->
            <el-dropdown
              v-if="hasMoreActions(scope.row)"
              size="mini"
              @command="(command) => handleCommand(command, scope.row)"
            >
              <el-button size="mini" type="text" icon="el-icon-d-arrow-right">
                其他<i class="el-icon-arrow-down el-icon--right"></i>
              </el-button>
              <el-dropdown-menu slot="dropdown">
                <!-- 二维码：仅进行中的考试显示 -->
                <el-dropdown-item command="qrcode" v-if="scope.row.status === '2'" v-hasPermi="['exam:examination:query']">
                  <i class="el-icon-full-screen"></i> 二维码
                </el-dropdown-item>

                <el-dropdown-item command="cancel" v-if="scope.row.status === '1'" v-hasPermi="['exam:examination:cancel']">
                  <i class="el-icon-circle-close"></i> 取消考试
                </el-dropdown-item>

                <el-dropdown-item command="restore" v-if="scope.row.status === '4'" v-hasPermi="['exam:examination:edit']">
                  <i class="el-icon-document-copy"></i> 恢复草稿
                </el-dropdown-item>

                <!-- 删除：仅草稿状态 -->
                <el-dropdown-item command="delete" v-if="scope.row.status === '0'" v-hasPermi="['exam:examination:remove']" divided>
                  <i class="el-icon-delete" style="color: #F56C6C;"></i> <span style="color: #F56C6C;">删除</span>
                </el-dropdown-item>
              </el-dropdown-menu>
            </el-dropdown>
          </div>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <pagination
      v-show="total>0"
      :total="total"
      :page.sync="queryParams.pageNum"
      :limit.sync="queryParams.pageSize"
      @pagination="getList"
    />

    <!-- 添加或修改考试对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="800px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-row>
          <el-col :span="12">
            <el-form-item label="考试名称" prop="examName">
              <el-input v-model="form.examName" :disabled="isRuntimeLimitedMode" placeholder="请输入考试名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="考试分类" prop="categoryId">
              <examination-category-tree-select
                v-model="form.categoryId"
                :disabled="isDisableCoreProp"
                placeholder="请选择考试分类" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="选择试卷" prop="paperId">
              <paper-select
                v-model="form.paperId"
                :disabled="isDisableCoreProp"
                placeholder="请选择试卷"
                @paper-select="onPaperSelect" />
              <div v-if="isExamInProgress" style="color: #F56C6C; font-size: 12px; margin-top: 4px;">
                考试进行中不允许更换试卷
              </div>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="考试时长" prop="duration">
              <el-input-number
                v-model="form.duration"
                :min="1"
                :max="300"
                :disabled="isDisableCoreProp"
                placeholder="分钟"
                style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="开始时间" prop="startTime">
              <el-date-picker
                v-model="form.startTime"
                type="datetime"
                placeholder="选择开始时间"
                :disabled="isRuntimeLimitedMode"
                style="width: 100%"
                value-format="yyyy-MM-dd HH:mm:ss">
              </el-date-picker>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="结束时间" prop="endTime">
              <el-date-picker
                v-model="form.endTime"
                type="datetime"
                placeholder="选择结束时间"
                :disabled="isRuntimeLimitedMode"
                style="width: 100%"
                value-format="yyyy-MM-dd HH:mm:ss">
              </el-date-picker>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="最大考试次数" prop="maxAttempts">
              <el-input-number
                v-model="form.maxAttempts"
                :min="1"
                :max="10"
                :disabled="isDisableMaxAttempts"
                placeholder="次数"
                style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="及格分数" prop="passScore">
              <el-input-number
                v-model="form.passScore"
                :min="0"
                :max="100"
                :disabled="isDisableCoreProp"
                placeholder="分数"
                style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="考试说明" prop="examDescription">
              <el-input v-model="form.examDescription" :disabled="isRuntimeLimitedMode" type="textarea" :rows="3" placeholder="请输入考试说明" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="参考人员" prop="examUserIds">
              <user-select v-model="form.examUserIds" :disabled="isRuntimeEditMode" placeholder="请选择参考人员" />
              <div style="color: #909399; font-size: 12px; margin-top: 4px;">
                按人员点名授权，适合临时补充个别考生
                <span v-if="form.examUserIds && form.examUserIds.length > 0" style="color: #409EFF;">
                  （已选择 {{ form.examUserIds.length }} 人）
                </span>
              </div>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="授权部门" prop="examDeptIds">
              <div class="dept-scope-tree" v-loading="deptLoading">
                <el-tree
                  ref="examDeptTree"
                  :data="deptOptions"
                  :props="deptTreeProps"
                  node-key="id"
                  show-checkbox
                  default-expand-all
                  :disabled="isRuntimeEditMode"
                  :expand-on-click-node="false"
                  @check="handleExamDeptCheck"
                />
              </div>
              <div class="dept-scope-footer">
                <el-switch
                  v-model="form.includeChildDept"
                  :disabled="isRuntimeEditMode"
                  active-value="1"
                  inactive-value="0"
                  active-text="包含子部门"
                  inactive-text="仅所选部门"
                />
                <span class="dept-scope-count" v-if="form.examDeptIds && form.examDeptIds.length > 0">
                  已选择 {{ form.examDeptIds.length }} 个部门
                </span>
              </div>
              <div class="dept-scope-tip">
                按部门授权后，部门内新增人员无需逐个加入参考人员；参考人员可作为额外点名范围
              </div>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="自动评分" prop="autoGrade">
              <el-switch
                v-model="form.autoGrade"
                :disabled="isDisableCoreProp"
                active-value="1"
                inactive-value="0" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="允许查看试卷" prop="allowReview">
              <el-switch v-model="form.allowReview" :disabled="isRuntimeLimitedMode" active-value="1" inactive-value="0" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="打乱题目" prop="shuffleQuestions">
              <el-switch v-model="form.shuffleQuestions" :disabled="isRuntimeLimitedMode" active-value="1" inactive-value="0" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="打乱选项" prop="shuffleOptions">
              <el-switch v-model="form.shuffleOptions" :disabled="isRuntimeLimitedMode" active-value="1" inactive-value="0" />
            </el-form-item>
          </el-col>
          <el-col :span="16">
            <el-form-item label="考试状态" prop="status" class="status-form-item">
              <el-radio-group v-model="form.status" size="small">
                <el-radio label="0" :disabled="true">草稿</el-radio>
                <el-radio label="1" :disabled="true">已发布</el-radio>
                <el-radio label="2" :disabled="true">进行中</el-radio>
                <el-radio label="3" :disabled="true">已结束</el-radio>
                <el-radio label="4" :disabled="true">已取消</el-radio>
              </el-radio-group>
              <div style="color: #909399; font-size: 12px; margin-top: 4px;">
                状态切换请使用列表中的发布、开始、结束、取消、重新发布或恢复草稿操作
              </div>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="备注" prop="remark">
              <el-input v-model="form.remark" :disabled="isRuntimeLimitedMode" type="textarea" placeholder="请输入内容" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>

    <!-- 二维码弹窗 -->
    <el-dialog
      title="考试二维码"
      :visible.sync="qrCodeDialogVisible"
      width="400px"
      append-to-body
      center
    >
      <div class="qrcode-content" v-loading="qrCodeLoading">
        <div v-if="qrCodeData.qrCodeBase64" class="qrcode-wrapper">
          <h3 class="qrcode-exam-name">{{ qrCodeData.examName }}</h3>
          <img :src="qrCodeData.qrCodeBase64" alt="考试二维码" class="qrcode-img" />
          <p class="qrcode-url">{{ qrCodeData.h5Url }}</p>
          <p class="qrcode-tip">扫描二维码参加考试</p>
        </div>
      </div>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" icon="el-icon-download" @click="downloadQrCode">下载二维码</el-button>
        <el-button @click="qrCodeDialogVisible = false">关 闭</el-button>
      </div>
    </el-dialog>

  </div>
</template>

<script>
import {
  listExamination,
  getExamination,
  addExamination,
  updateExamination,
  addExamUsers,
  updateExamUsers,
  delExamination,
  getExamQrCode,
  publishExamination,
  startExamination,
  endExamination,
  cancelExamination,
  restoreDraftExamination
} from "@/api/exam/examination";
import { deptTreeSelect } from "@/api/system/user";
import ExaminationCategoryTreeSelect from "@/components/ExaminationCategoryTreeSelect";
import PaperSelect from "@/components/PaperSelect";
import UserSelect from "@/components/UserSelect";

export default {
  name: "ExamManagement",
  dicts: ['sys_normal_disable'],
  components: {
    ExaminationCategoryTreeSelect,
    PaperSelect,
    UserSelect
  },
  data() {
    return {
      // 遮罩层
      loading: true,
      // 选中数组
      ids: [],
      // 非单个禁用
      single: true,
      // 非多个禁用
      multiple: true,
      // 显示搜索条件
      showSearch: true,
      // 总条数
      total: 0,
      // 考试表格数据
      examinationList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 表单模式：edit 普通编辑，auth 仅维护考试权限范围
      formMode: "edit",
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        examName: null,
        categoryId: null,
        status: null,
        delFlag: null
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        examName: [
          { required: true, message: "考试名称不能为空", trigger: "blur" }
        ],
        categoryId: [
          { required: true, message: "考试分类不能为空", trigger: "change" }
        ],
        paperId: [
          { required: true, message: "试卷不能为空", trigger: "change" }
        ],
        duration: [
          { required: true, message: "考试时长不能为空", trigger: "blur" }
        ],
        startTime: [
          { required: true, message: "开始时间不能为空", trigger: "change" }
        ],
        endTime: [
          { required: true, message: "结束时间不能为空", trigger: "change" }
        ],
        passScore: [
          { required: true, message: "及格分数不能为空", trigger: "blur" }
        ]
      },
      // 表格默认排序配置
      defaultSort: {
        prop: 'startTime',
        order: 'descending'
      },
      // 二维码弹窗相关
      qrCodeDialogVisible: false,
      qrCodeLoading: false,
      qrCodeData: {
        qrCodeBase64: '',
        h5Url: '',
        examName: ''
      },
      // 部门授权范围
      deptLoading: false,
      deptOptions: [],
      deptTreeProps: {
        children: 'children',
        label: 'label'
      }
    };
  },
  computed: {
    // 是否仅维护考试授权范围
    isAuthScopeMode() {
      return this.formMode === 'auth';
    },
    // 当前单选记录是否允许修改
    canUpdateSelection() {
      if (this.ids.length !== 1) {
        return false;
      }
      const selected = this.examinationList.find(item => item.id === this.ids[0]);
      return !!selected && (this.canEditExam(selected) || selected.status === '2');
    },
    // 考试是否正在进行中
    isExamInProgress() {
      return this.form.status === '2';
    },
    // 考试是否已结束或已取消
    isExamFinishedOrCancelled() {
      return this.form.status === '3' || this.form.status === '4';
    },
    // 授权维护或进行中考试修改时，仅开放明确允许的字段
    isRuntimeLimitedMode() {
      return this.isAuthScopeMode || this.isExamInProgress;
    },
    // 进行中考试的普通修改模式只允许调整最大考试次数
    isRuntimeEditMode() {
      return this.formMode === 'edit' && this.isExamInProgress;
    },
    // 是否禁用核心字段修改
    isDisableCoreProp() {
      return this.isAuthScopeMode || this.isExamInProgress || this.isExamFinishedOrCancelled;
    },
    // 进行中考试允许调整考试次数，其他已结束/已取消/授权维护场景仍禁用
    isDisableMaxAttempts() {
      return this.isAuthScopeMode || this.isExamFinishedOrCancelled;
    }
  },
  created() {
    this.getList();
    this.loadDeptTree();
  },
  methods: {
    /** 查询考试列表 */
    getList() {
      this.loading = true;
      listExamination(this.queryParams).then(response => {
        this.examinationList = response.rows;
        this.total = response.total;
        this.loading = false;
      });
    },
    // 取消按钮
    cancel() {
      this.open = false;
      this.reset();
    },
    // 表单重置
    reset() {
      this.formMode = "edit";
      this.form = {
        id: null,
        examName: null,
        categoryId: null,
        paperId: null,
        duration: 60,
        startTime: null,
        endTime: null,
        maxAttempts: 1,
        passScore: 60,
        examDescription: null,
        examUserIds: [],
        examDeptIds: [],
        includeChildDept: '1',
        examType: 'normal',
        examMode: 'online',
        registrationRequired: '0',
        autoGrade: '1',
        resultPublishType: 'auto',
        allowReview: '1',
        shuffleQuestions: '0',
        shuffleOptions: '0',
        monitorMode: 'normal',
        antiCheat: '0',
        status: "0",
        remark: null
      };
      this.resetForm("form");
      this.$nextTick(() => {
        if (this.$refs.examDeptTree) {
          this.$refs.examDeptTree.setCheckedKeys([]);
        }
      });
    },
    /** 搜索按钮操作 */
    handleQuery() {
      this.queryParams.pageNum = 1;
      this.getList();
    },
    /** 重置按钮操作 */
    resetQuery() {
      this.resetForm("queryForm");
      this.handleQuery();
    },
    // 多选框选中数据
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.id)
      this.single = selection.length!==1
      this.multiple = !selection.length
    },
    /** 新增按钮操作 */
    handleAdd() {
      this.reset();
      this.formMode = "edit";
      this.open = true;
      this.title = "添加考试";
    },
    /** 修改按钮操作 */
    handleUpdate(row, mode = 'edit') {
      this.reset();
      const selectedRow = row && row.id ? row : this.examinationList.find(item => item.id === this.ids[0]);
      if (!selectedRow) {
        this.$modal.msgError("请选择一条考试记录");
        return;
      }
      if (mode === 'edit' && !this.canEditExam(selectedRow) && selectedRow.status !== '2') {
        this.$modal.msgError("仅草稿、已发布或进行中考试允许修改");
        return;
      }
      if (mode === 'auth' && selectedRow.status !== '2') {
        this.$modal.msgError("仅进行中的考试需要单独维护授权范围");
        return;
      }
      const id = selectedRow.id
      getExamination(id).then(response => {
        this.form = response.data;
        this.formMode = mode;
        if (!this.form.examUserIds) {
          this.$set(this.form, 'examUserIds', []);
        }
        if (!this.form.examDeptIds) {
          this.$set(this.form, 'examDeptIds', []);
        }
        if (!this.form.includeChildDept) {
          this.$set(this.form, 'includeChildDept', '1');
        }
        this.open = true;
        this.title = mode === 'auth' ? "维护考试授权" : "修改考试";
        this.$nextTick(() => this.syncDeptTreeChecked());
      });
    },
    /** 维护进行中考试授权范围 */
    handleAuthScope(row) {
      this.handleUpdate(row, 'auth');
    },
    /** 查看详情 */
    handleView(row) {
      this.$router.push({
        path: `/exam/examination/detail/${row.id}`,
        query: { examName: row.examName }
      });
    },
    /** 维护参考人员 */
    handleUsers(row) {
      this.$router.push({
        path: `/exam/examination/users/${row.id}`,
        query: { examName: row.examName }
      });
    },
    /** 查看成绩统计 */
    handleScoreAnalysis(row) {
      this.$router.push({
        path: '/exam/examination/score-statistics',
        query: { examId: row.id, examName: row.examName }
      });
    },
    /** 是否允许在表单中修改考试基础配置 */
    canEditExam(row) {
      return row.status === '0' || row.status === '1';
    },
    /** 是否展示辅助操作入口 */
    hasMoreActions(row) {
      return row.status === '0' || row.status === '1' || row.status === '2' || row.status === '4';
    },
    /** 处理更多操作命令 */
    handleCommand(command, row) {
      const id = row.id;
      switch (command) {
        case 'qrcode':
          this.handleShowQrCode(row);
          break;
        case 'publish':
          this.handlePublishExam(id);
          break;
        case 'start':
          this.handleStartExam(id);
          break;
        case 'end':
          this.handleEndExam(id);
          break;
        case 'cancel':
          this.handleCancelExam(id);
          break;
        case 'republish':
          this.handleRepublishExam(id, row);
          break;
        case 'restore':
          this.handleRestoreExam(id);
          break;
        case 'delete':
          this.handleDelete(row);
          break;
      }
    },
    /** 发布考试 */
    handlePublishExam(id) {
      this.$modal.confirm('确认发布该考试？发布后考生即可报名参加。').then(() => {
        publishExamination(id).then(response => {
          this.$modal.msgSuccess("考试发布成功");
          this.getList();
        }).catch(error => {
          this.$modal.msgError("发布失败：" + (error.message || "未知错误"));
        });
      }).catch(() => {});
    },
    /** 开始考试 */
    handleStartExam(id) {
      this.$modal.confirm('确认开始该考试？开始后考生即可进入考试。').then(() => {
        startExamination(id).then(response => {
          this.$modal.msgSuccess("考试已开始");
          this.getList();
        }).catch(error => {
          this.$modal.msgError("开始失败：" + (error.message || "未知错误"));
        });
      }).catch(() => {});
    },
    /** 结束考试 */
    handleEndExam(id) {
      this.$modal.confirm('确认结束该考试？结束后将无法恢复，请谨慎操作。').then(() => {
        endExamination(id).then(response => {
          this.$modal.msgSuccess("考试已结束");
          this.getList();
        }).catch(error => {
          this.$modal.msgError("结束失败：" + (error.message || "未知错误"));
        });
      }).catch(() => {});
    },
    /** 取消考试 */
    handleCancelExam(id) {
      this.$modal.confirm('确认取消该考试？取消后考试将停止，请谨慎操作。').then(() => {
        cancelExamination(id).then(response => {
          this.$modal.msgSuccess("考试已取消");
          this.getList();
        }).catch(error => {
          this.$modal.msgError("取消失败：" + (error.message || "未知错误"));
        });
      }).catch(() => {});
    },
    /** 重新发布考试 */
    handleRepublishExam(id, row) {
      this.$modal.confirm(`确认重新发布考试"${row.examName}"？重新发布后考试状态将变为已发布。`).then(() => {
        publishExamination(id).then(response => {
          this.$modal.msgSuccess("考试重新发布成功");
          this.getList();
        }).catch(error => {
          this.$modal.msgError("重新发布失败：" + (error.message || "未知错误"));
        });
      }).catch(() => {});
    },
    /** 恢复为草稿 */
    handleRestoreExam(id) {
      this.$modal.confirm('确认将考试恢复为草稿状态？恢复后可以重新编辑考试信息。').then(() => {
        restoreDraftExamination(id).then(response => {
          this.$modal.msgSuccess("考试已恢复为草稿状态");
          this.getList();
        }).catch(error => {
          this.$modal.msgError("恢复失败：" + (error.message || "未知错误"));
        });
      }).catch(() => {});
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          this.syncFormDeptIds();
          if (!this.isRuntimeEditMode && !this.hasExamAccessScope()) {
            this.$modal.msgError("请至少选择参考人员或授权部门");
            return;
          }
          if (this.form.id != null) {
            // 修改考试
            updateExamination(this.form).then(response => {
              if (this.isRuntimeEditMode) {
                this.$modal.msgSuccess("修改考试成功");
                this.open = false;
                this.getList();
              } else {
                this.updateExamUsersHandler(this.form.id, this.form.examUserIds || []);
              }
            });
          } else {
            // 新增考试
            addExamination(this.form).then(response => {
              const examId = response.data;
              if (this.form.examUserIds && this.form.examUserIds.length > 0 && examId) {
                this.addExamUsersToExam(examId, this.form.examUserIds);
              } else {
                this.$modal.msgSuccess("新增成功");
                this.open = false;
                this.getList();
              }
            }).catch(error => {
              this.$modal.msgError("新增失败：" + (error.message || "未知错误"));
            });
          }
        }
      });
    },
    /** 加载部门树 */
    loadDeptTree() {
      this.deptLoading = true;
      deptTreeSelect().then(response => {
        this.deptOptions = response.data || [];
        this.$nextTick(() => this.syncDeptTreeChecked());
      }).catch(error => {
        console.error('获取部门树失败:', error);
        this.$modal.msgError("获取部门树失败");
      }).finally(() => {
        this.deptLoading = false;
      });
    },
    /** 部门树选择变化 */
    handleExamDeptCheck() {
      this.syncFormDeptIds();
    },
    /** 同步表单部门ID */
    syncFormDeptIds() {
      const deptIds = this.$refs.examDeptTree ? this.$refs.examDeptTree.getCheckedKeys() : (this.form.examDeptIds || []);
      this.$set(this.form, 'examDeptIds', deptIds);
    },
    /** 是否已配置考试授权范围 */
    hasExamAccessScope() {
      const userCount = this.form.examUserIds ? this.form.examUserIds.length : 0;
      const deptCount = this.form.examDeptIds ? this.form.examDeptIds.length : 0;
      return userCount > 0 || deptCount > 0;
    },
    /** 回显部门树选择 */
    syncDeptTreeChecked() {
      if (this.$refs.examDeptTree) {
        this.$refs.examDeptTree.setCheckedKeys(this.form.examDeptIds || []);
      }
    },
    /** 添加考试用户 */
    addExamUsersToExam(examId, userIds) {
      addExamUsers(examId, userIds).then(response => {
        this.$modal.msgSuccess("新增考试成功，已关联 " + userIds.length + " 名用户");
        this.open = false;
        this.getList();
      }).catch(error => {
        this.$modal.msgError("关联用户失败：" + (error.message || "未知错误"));
        this.open = false;
        this.getList();
      });
    },
    /** 更新考试用户 */
    updateExamUsersHandler(examId, userIds) {
      updateExamUsers(examId, userIds).then(response => {
        this.$modal.msgSuccess("修改考试成功，已更新用户关联");
        this.open = false;
        this.getList();
      }).catch(error => {
        this.$modal.msgError("更新用户关联失败：" + (error.message || "未知错误"));
        this.open = false;
        this.getList();
      });
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      const ids = row.id || this.ids;
      let confirmMessage = '';

      if (row && row.examName) {
        confirmMessage = '是否确认删除考试名称为"' + row.examName + '"的数据项？';
      } else {
        const selectedExams = this.examinationList.filter(item => this.ids.includes(item.id));
        if (selectedExams.length === 1) {
          confirmMessage = '是否确认删除考试名称为"' + selectedExams[0].examName + '"的数据项？';
        } else {
          const names = selectedExams.map(item => item.examName).join('、');
          confirmMessage = '是否确认删除以下考试：' + names + '？';
        }
      }

      this.$modal.confirm(confirmMessage).then(function() {
        return delExamination(ids);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("删除成功");
      }).catch(() => {});
    },
    /** 导出按钮操作 */
    handleExport() {
      this.download('exam/examination/export', {
        ...this.queryParams
      }, `examination_${new Date().getTime()}.xlsx`)
    },
    /** 表格排序变化处理 */
    handleSortChange({ prop, order }) {
      this.queryParams.orderByColumn = prop;
      this.queryParams.isAsc = order;
      this.getList();
    },
    /** 试卷选择事件处理 */
    onPaperSelect(paper) {
      if (paper && !this.isDisableCoreProp) {
        if (paper.duration) {
          this.form.duration = paper.duration;
        }
        if (paper.passScore) {
          this.form.passScore = paper.passScore;
        }
        if (paper.shuffleQuestions !== undefined) {
          this.form.shuffleQuestions = paper.shuffleQuestions;
        }
        if (paper.shuffleOptions !== undefined) {
          this.form.shuffleOptions = paper.shuffleOptions;
        }
        this.$message.success('已自动填充试卷相关配置信息');
      }
    },
    /** 显示考试二维码 */
    handleShowQrCode(row) {
      this.qrCodeDialogVisible = true;
      this.qrCodeLoading = true;
      this.qrCodeData = {
        qrCodeBase64: '',
        h5Url: '',
        examName: row.examName
      };

      // 使用浏览器当前的origin来构建URL，确保在任何环境下（localhost/局域网/公网）都正确
      const browserOrigin = window.location.origin;
      const h5LoginUrl = browserOrigin + '/h5/login?examId=' + row.id;

      // 将浏览器origin传给后端，用于生成正确的二维码
      getExamQrCode(row.id, browserOrigin).then(res => {
        if (res.code === 200) {
          // 以后端校验后返回的URL为准，保证弹窗展示和二维码实际内容一致
          this.qrCodeData = {
            qrCodeBase64: res.qrCodeBase64,
            h5Url: res.h5Url || h5LoginUrl,
            examName: res.examName || row.examName
          };
        } else {
          this.$message.error(res.msg || '获取二维码失败');
        }
      }).catch(err => {
        console.error('获取二维码失败', err);
        this.$message.error('获取二维码失败');
      }).finally(() => {
        this.qrCodeLoading = false;
      });
    },
    /** 下载二维码 */
    downloadQrCode() {
      if (!this.qrCodeData.qrCodeBase64) {
        this.$message.warning('二维码还未加载完成');
        return;
      }

      const base64 = this.qrCodeData.qrCodeBase64.split(',')[1];
      const byteCharacters = atob(base64);
      const byteNumbers = new Array(byteCharacters.length);
      for (let i = 0; i < byteCharacters.length; i++) {
        byteNumbers[i] = byteCharacters.charCodeAt(i);
      }
      const byteArray = new Uint8Array(byteNumbers);
      const blob = new Blob([byteArray], { type: 'image/png' });

      const link = document.createElement('a');
      link.href = URL.createObjectURL(blob);
      link.download = `考试二维码_${this.qrCodeData.examName}.png`;
      link.click();
      URL.revokeObjectURL(link.href);

      this.$message.success('二维码下载成功');
    }
  }
};
</script>

<style scoped>
.status-form-item :deep(.el-form-item__content) {
  display: flex !important;
  align-items: center !important;
}

.table-actions {
  display: flex;
  align-items: center;
  justify-content: center;
  flex-wrap: wrap;
  gap: 0 8px;
  white-space: normal;
}

.table-actions .el-button--text {
  margin-left: 0;
}

/* 二维码弹窗样式 */
.qrcode-content {
  min-height: 300px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.qrcode-wrapper {
  text-align: center;
}

.qrcode-exam-name {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 20px 0;
}

.qrcode-img {
  width: 250px;
  height: 250px;
  display: block;
  margin: 0 auto 15px;
  border: 1px solid #ebeef5;
  border-radius: 8px;
}

.qrcode-url {
  color: #909399;
  font-size: 12px;
  margin: 10px 0 5px;
  word-break: break-all;
}

.qrcode-tip {
  color: #606266;
  font-size: 14px;
  margin: 0;
}

.dept-scope-tree {
  max-height: 220px;
  overflow: auto;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  padding: 8px;
}

.dept-scope-footer {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-top: 8px;
}

.dept-scope-count {
  color: #409EFF;
  font-size: 12px;
}

.dept-scope-tip {
  color: #909399;
  font-size: 12px;
  margin-top: 4px;
}
</style>
