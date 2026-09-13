<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="题目标题" prop="questionTitle">
        <el-input
          v-model="queryParams.questionTitle"
          placeholder="请输入题目标题"
          clearable
          style="width: 240px"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="试题分类" prop="categoryId">
        <div style="width: 240px">
          <category-tree-select v-model="queryParams.categoryId" placeholder="请选择试题分类" />
        </div>
      </el-form-item>
      <el-form-item label="题型" prop="questionType">
        <el-select v-model="queryParams.questionType" placeholder="试题类型" clearable style="width: 240px">
          <el-option label="单选题" value="single" />
          <el-option label="多选题" value="multiple" />
          <el-option label="判断题" value="judge" />
          <el-option label="填空题" value="fill" />
          <el-option label="简答题" value="essay" />
        </el-select>
      </el-form-item>
      <el-form-item label="难度等级" prop="difficultyLevel">
        <el-select v-model="queryParams.difficultyLevel" placeholder="难度等级" clearable style="width: 240px">
          <el-option label="简单" :value="1" />
          <el-option label="中等" :value="2" />
          <el-option label="困难" :value="3" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="试题状态" clearable style="width: 240px">
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

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="el-icon-plus"
          size="mini"
          @click="handleAdd"
          v-hasPermi="['exam:question:add']"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="el-icon-edit"
          size="mini"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['exam:question:edit']"
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
          v-hasPermi="['exam:question:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
          v-hasPermi="['exam:question:export']"
        >导出</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="info"
          plain
          icon="el-icon-upload2"
          size="mini"
          @click="handleImport"
          v-hasPermi="['exam:question:import']"
        >导入</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="questionList" @selection-change="handleSelectionChange" :default-sort="defaultSort" @sort-change="handleSortChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="题目标题" prop="questionTitle" min-width="300" :show-overflow-tooltip="true" />
      <el-table-column label="试题分类" prop="categoryName" width="120" />
      <el-table-column label="题型" prop="questionType" width="80">
        <template slot-scope="scope">
          <span v-if="scope.row.questionType === 'single'">单选题</span>
          <span v-else-if="scope.row.questionType === 'multiple'">多选题</span>
          <span v-else-if="scope.row.questionType === 'judge'">判断题</span>
          <span v-else-if="scope.row.questionType === 'fill'">填空题</span>
          <span v-else-if="scope.row.questionType === 'essay'">简答题</span>
        </template>
      </el-table-column>
      <el-table-column label="难度" prop="difficultyLevel" width="70" sortable="custom" :sort-orders="['descending', 'ascending']">
        <template slot-scope="scope">
          <el-tag v-if="scope.row.difficultyLevel === 1" type="success">简单</el-tag>
          <el-tag v-else-if="scope.row.difficultyLevel === 2" type="warning">中等</el-tag>
          <el-tag v-else-if="scope.row.difficultyLevel === 3" type="danger">困难</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="分值" prop="score" width="60" sortable="custom" :sort-orders="['descending', 'ascending']" />
      <el-table-column label="状态" prop="status" width="70">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.sys_normal_disable" :value="scope.row.status"/>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" align="center" prop="createTime" width="160" sortable="custom" :sort-orders="['descending', 'ascending']">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="120">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-view"
            @click="handlePreview(scope.row)"
            v-hasPermi="['exam:question:query']"
          >预览</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleRowUpdate(scope.row)"
            v-hasPermi="['exam:question:edit']"
          >修改</el-button>
        </template>
      </el-table-column>
    </el-table>
    
    <pagination
      v-show="total>0"
      :total="total"
      :page.sync="queryParams.pageNum"
      :limit.sync="queryParams.pageSize"
      @pagination="getList"
    />

    <!-- 添加或修改试题对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="800px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
        <el-row>
          <el-col :span="12">
            <el-form-item label="试题分类" prop="categoryId">
              <category-tree-select 
                ref="dialogCategorySelect"
                v-model="form.categoryId" 
                placeholder="请选择试题分类" 
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="题型" prop="questionType">
              <el-select v-model="form.questionType" placeholder="请选择题型" style="width: 100%" @change="handleTypeChange">
                <el-option label="单选题" value="single" />
                <el-option label="多选题" value="multiple" />
                <el-option label="判断题" value="judge" />
                <el-option label="填空题" value="fill" />
                <el-option label="简答题" value="essay" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="题目标题" prop="questionTitle">
              <el-input v-model="form.questionTitle" placeholder="请输入题目标题" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="题目内容" prop="questionContent">
              <el-input v-model="form.questionContent" 
                        type="textarea" 
                        :rows="4" 
                        placeholder="请输入题目内容" 
                        ref="questionContentInput"
                        @input="handleContentChange" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="题目媒体">
              <div class="question-media-editor">
                <div class="media-upload-block">
                  <div class="media-upload-title">题干图片</div>
                  <image-upload
                    v-model="mediaImages"
                    :limit="6"
                    :file-size="10"
                    :file-type="['png', 'jpg', 'jpeg', 'gif', 'webp']"
                  />
                </div>
                <div class="media-upload-block">
                  <div class="media-upload-title">题干视频</div>
                  <file-upload
                    v-model="mediaVideos"
                    :limit="1"
                    :file-size="100"
                    :file-type="questionVideoTypes"
                  />
                </div>
              </div>
            </el-form-item>
          </el-col>
          <!-- 选项设置：单选题、多选题、判断题 -->
          <el-col :span="24" v-if="showOptions">
            <el-form-item label="选项设置">
              <div v-for="(option, index) in options" :key="index" style="margin-bottom: 10px; display: flex; align-items: center;">
                <div style="width: 80px; margin-right: 10px;">
                  <el-input 
                    :value="getOptionKeyDisplay(option.key)" 
                    placeholder="选项" 
                    disabled 
                    style="width: 100%;" />
                </div>
                <div style="flex: 1; margin-right: 10px;">
                  <el-input 
                    v-model="option.value" 
                    placeholder="请输入选项内容" 
                    :disabled="form.questionType === 'judge'" 
                    style="width: 100%;" />
                </div>
                <div style="width: 120px; margin-right: 10px;">
                  <!-- 单选题用单选框 -->
                  <el-radio v-if="form.questionType === 'single' || form.questionType === 'judge'" 
                            v-model="singleCorrectAnswer" 
                            :label="option.key" 
                            @change="handleSingleAnswerChange">
                    正确答案
                  </el-radio>
                  <!-- 多选题用复选框 -->
                  <el-checkbox v-else-if="form.questionType === 'multiple'" 
                               v-model="option.isCorrect" 
                               @change="handleMultipleAnswerChange">
                    正确答案
                  </el-checkbox>
                </div>
                <div style="width: 60px;">
                  <el-button v-if="options.length > 2 && form.questionType !== 'judge'" 
                             type="danger" 
                             icon="el-icon-delete" 
                             size="mini" 
                             @click="removeOption(index)">
                  </el-button>
                </div>
              </div>
              <el-button v-if="form.questionType !== 'judge'" 
                         type="primary" 
                         icon="el-icon-plus" 
                         size="mini" 
                         @click="addOption">
                添加选项
              </el-button>
            </el-form-item>
          </el-col>
          
          <!-- 填空题的多空答案设置 -->
          <el-col :span="24" v-if="form.questionType === 'fill'">
            <el-form-item label="填空设置">
              <div style="margin-bottom: 15px;">
                <el-alert
                  title="填空题操作指南"
                  type="info"
                  :closable="false"
                  show-icon>
                  <div slot="default">
                    <p><strong>操作步骤：</strong>1. 输入完整题目内容 → 2. 点击"插入填空选项"按钮 → 3. 设置正确答案</p>
                    <p><strong>手动标记：</strong>题干中可使用 &#123;&#123;空&#125;&#125;、[空] 或连续 3 个以上下划线表示填空位置。</p>
                    <p><strong>智能识别：</strong>点击后会将（答案）、(答案)、【答案】、[答案] 和连续下划线识别为填空；括号内文字会自动带入对应正确答案，下划线答案需手动填写。</p>
                    <p><strong>答案规则：</strong>支持多个填空；同一空的多种正确答案用英文分号 ; 分隔，评分时忽略首尾空格和大小写。</p>
                  </div>
                </el-alert>
              </div>
              
              <!-- 填空操作工具栏 -->
              <div style="margin-bottom: 15px; padding: 15px; background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%); border-radius: 8px; border: 1px solid #dee2e6;">
                <div style="margin-bottom: 12px; font-weight: bold; color: #495057; display: flex; align-items: center;">
                  <i class="el-icon-setting" style="margin-right: 8px; color: #6c757d;"></i>
                  填空选项操作
                </div>
                <el-row :gutter="12">
                  <el-col :span="8">
                    <el-button 
                      type="primary" 
                      icon="el-icon-plus" 
                      size="small" 
                      @click="insertBlankOption"
                      :disabled="form.questionType !== 'fill'"
                      style="width: 100%;">
                      插入填空选项
                    </el-button>
                  </el-col>
                  <el-col :span="8">
                    <el-button 
                      type="success" 
                      icon="el-icon-magic-stick" 
                      size="small" 
                      @click="autoGenerateBlanks"
                      :disabled="!form.questionContent"
                      style="width: 100%;">
                      智能识别填空
                    </el-button>
                  </el-col>
                  <el-col :span="8">
                    <el-button 
                      type="warning" 
                      icon="el-icon-refresh" 
                      size="small" 
                      @click="clearAllBlanks"
                      :disabled="fillBlanks.length === 0"
                      style="width: 100%;">
                      清空所有填空
                    </el-button>
                  </el-col>
                </el-row>
                <div style="margin-top: 10px; font-size: 12px; color: #6c757d;">
                  <i class="el-icon-info"></i> 
                  提示："插入填空选项"会在光标位置添加填空标记；"智能识别填空"会把括号内容替换为填空并提取答案。
                </div>
              </div>
              
              <!-- 智能提示区域 -->
              <div v-if="possibleBlanksCount > 0 && fillBlanks.length === 0" style="margin-bottom: 15px; padding: 12px; background: linear-gradient(135deg, #e8f4fd 0%, #f0f9ff 100%); border: 1px solid #b3d8ff; border-radius: 6px;">
                <div style="display: flex; align-items: center; margin-bottom: 8px;">
                  <i class="el-icon-warning" style="color: #409eff; margin-right: 8px; font-size: 16px;"></i>
                  <span style="color: #409eff; font-weight: bold;">智能提示</span>
                </div>
                <div style="color: #606266; font-size: 13px; margin-bottom: 10px;">
                  检测到 {{ possibleBlanksCount }} 个可能的填空位置，点击"智能识别填空"按钮自动生成填空框架
                </div>
                <el-button 
                  type="primary" 
                  icon="el-icon-magic-stick" 
                  size="mini" 
                  @click="autoGenerateBlanks">
                  立即智能识别
                </el-button>
              </div>
              
              <!-- 填空答案设置区域 -->
              <div v-if="fillBlanks.length > 0">
                <div style="margin-bottom: 10px; font-weight: bold; color: #409eff;">
                  <i class="el-icon-edit"></i> 已识别到 {{ fillBlanks.length }} 个填空，请设置正确答案：
                </div>
                <div v-for="(blank, index) in fillBlanks" :key="index" style="margin-bottom: 15px; padding: 15px; border: 1px solid #e4e7ed; border-radius: 6px; background: #fafafa;">
                  <div style="margin-bottom: 10px; display: flex; justify-content: space-between; align-items: center;">
                    <span style="font-weight: bold; color: #606266;">
                      <i class="el-icon-edit-outline"></i> 第 {{ index + 1 }} 个填空
                    </span>
                    <el-button 
                      type="danger" 
                      icon="el-icon-delete" 
                      size="mini" 
                      circle
                      @click="removeBlank(index)"
                      title="删除此填空">
                    </el-button>
                  </div>
                  <el-input
                    v-model="blank.answers"
                    placeholder="请输入正确答案，多个答案用分号(;)分隔"
                    style="width: 100%;"
                    @input="updateFillAnswers">
                    <template slot="prepend">
                      <i class="el-icon-check"></i> 正确答案
                    </template>
                  </el-input>
                  <div style="margin-top: 8px; font-size: 12px; color: #909399; display: flex; justify-content: space-between;">
                    <span>示例：北京;beijing;Beijing （支持多种写法，用分号分隔）</span>
                    <span v-if="blank.answers">已设置 {{ blank.answers.split(';').filter(a => a.trim()).length }} 个答案</span>
                  </div>
                </div>
              </div>
              
              <!-- 空状态提示 -->
              <div v-else style="padding: 30px; text-align: center; color: #909399; border: 2px dashed #e4e7ed; border-radius: 6px; background: #fafbfc;">
                <i class="el-icon-info" style="font-size: 24px; margin-bottom: 10px;"></i>
                <div style="font-size: 14px; margin-bottom: 10px;">暂未识别到填空位置</div>
                <div style="font-size: 12px;">请在题目内容中输入完整题干，然后点击"智能识别填空"</div>
              </div>
            </el-form-item>
          </el-col>
          
          <!-- 简答题的文本答案 -->
          <el-col :span="24" v-if="form.questionType === 'essay'">
            <el-form-item label="参考答案" prop="correctAnswer">
              <el-input v-model="form.correctAnswer" 
                        type="textarea" 
                        :rows="4"
                        placeholder="请输入参考答案" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="题目解析">
              <el-input v-model="form.questionAnalysis" type="textarea" placeholder="请输入题目解析" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="难度等级" prop="difficultyLevel">
              <el-select v-model="form.difficultyLevel" placeholder="请选择难度等级" style="width: 100%">
                <el-option label="简单" :value="1" />
                <el-option label="中等" :value="2" />
                <el-option label="困难" :value="3" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="分值" prop="score">
              <el-input-number v-model="form.score" :precision="2" :step="0.5" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="状态">
              <el-radio-group v-model="form.status">
                <el-radio
                  v-for="dict in dict.type.sys_normal_disable"
                  :key="dict.value"
                  :label="dict.value"
                >{{dict.label}}</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="标签">
              <el-input v-model="form.tags" placeholder="请输入标签，多个标签用逗号分隔" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="备注">
              <el-input v-model="form.remark" type="textarea" placeholder="请输入内容" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>

    <!-- 试题预览对话框 -->
    <el-dialog title="试题预览" :visible.sync="previewOpen" width="800px" append-to-body>
      <question-preview v-if="previewData.id" :question="previewData" :show-answer="true" />
    </el-dialog>

    <!-- 试题导入对话框 -->
    <el-dialog :title="upload.title" :visible.sync="upload.open" width="400px" append-to-body>
      <el-upload
        ref="upload"
        :limit="1"
        accept=".xlsx, .xls"
        :headers="upload.headers"
        :action="upload.url + '?updateSupport=' + upload.updateSupport"
        :disabled="upload.isUploading"
        :before-upload="handleImportBeforeUpload"
        :on-error="handleImportUploadError"
        :on-progress="handleFileUploadProgress"
        :on-success="handleFileSuccess"
        :auto-upload="false"
        drag
      >
        <i class="el-icon-upload"></i>
        <div class="el-upload__text">将文件拖到此处，或<em>点击上传</em></div>
        <div class="el-upload__tip text-center" slot="tip">
          <div class="el-upload__tip" slot="tip">
            <el-checkbox v-model="upload.updateSupport" /> 是否更新已经存在的试题数据
          </div>
          <span>仅允许导入xls、xlsx格式文件。</span>
          <el-dropdown @command="handleTemplateDownload">
            <el-link type="primary" :underline="false" style="font-size:12px;vertical-align: baseline;">
              下载模板<i class="el-icon-arrow-down el-icon--right"></i>
            </el-link>
            <el-dropdown-menu slot="dropdown">
              <el-dropdown-item command="new">按题型分类模板</el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>
        </div>
      </el-upload>
      <div class="import-upload-progress" v-if="upload.progressVisible">
        <div class="import-upload-progress__meta">
          <span class="import-upload-progress__name">{{ upload.fileName || '导入文件上传中' }}</span>
          <span class="import-upload-progress__size">{{ upload.loadedText }}</span>
        </div>
        <el-progress :percentage="upload.percent" :status="upload.progressStatus"></el-progress>
        <div class="import-upload-progress__hint">{{ upload.statusText }}</div>
      </div>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" :loading="upload.isUploading" :disabled="upload.isUploading" @click="submitFileForm">确 定</el-button>
        <el-button :disabled="upload.isUploading" @click="upload.open = false">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listQuestion, getQuestion, delQuestion, addQuestion, updateQuestion, exportQuestion, previewQuestion } from "@/api/exam/question";
import { getToken } from "@/utils/auth";
import QuestionPreview from "@/components/QuestionPreview/index";
import CategoryTreeSelect from "@/components/CategoryTreeSelect";
import ImageUpload from "@/components/ImageUpload";
import FileUpload from "@/components/FileUpload";

export default {
  name: "Question",
  dicts: ['sys_normal_disable'],
  components: {
    QuestionPreview,
    CategoryTreeSelect,
    ImageUpload,
    FileUpload
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
      // 试题表格数据
      questionList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 预览弹出层
      previewOpen: false,
      // 预览数据
      previewData: {},
      // 是否显示选项设置
      showOptions: false,
      // 选项数据
      options: [
        { key: 'A', value: '', isCorrect: false },
        { key: 'B', value: '', isCorrect: false }
      ],
      // 单选题正确答案
      singleCorrectAnswer: '',
      // 填空题数据
      fillBlanks: [],
      // 内容变化定时器
      contentChangeTimer: null,
      // 可能的填空数量
      possibleBlanksCount: 0,
      // 题干图片和视频上传值
      mediaImages: '',
      mediaVideos: '',
      questionVideoTypes: ['mp4', 'm4v', 'mov', 'webm', 'ogg', 'ogv'],
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        questionTitle: null,
        categoryId: null,
        questionType: null,
        difficultyLevel: null,
        status: null
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        categoryId: [
          { required: true, message: "试题分类不能为空", trigger: "change" }
        ],
        questionType: [
          { required: true, message: "题型不能为空", trigger: "change" }
        ],
        questionTitle: [
          { required: true, message: "题目标题不能为空", trigger: "blur" }
        ],
        questionContent: [
          { required: true, message: "题目内容不能为空", trigger: "blur" }
        ],
        correctAnswer: [
          { required: true, message: "正确答案不能为空", trigger: "blur" }
        ],
        difficultyLevel: [
          { required: true, message: "难度等级不能为空", trigger: "change" }
        ],
        score: [
          { required: true, message: "分值不能为空", trigger: "blur" }
        ]
      },
      // 试题导入参数
      upload: {
        // 是否显示弹出层（试题导入）
        open: false,
        // 弹出层标题（试题导入）
        title: "",
        // 是否禁用上传
        isUploading: false,
        // 是否更新已经存在的试题数据
        updateSupport: 0,
        // 设置上传的请求头部
        headers: { Authorization: "Bearer " + getToken() },
        // 上传的地址
        url: process.env.VUE_APP_BASE_API + "/exam/question/importData",
        // 上传进度
        progressVisible: false,
        percent: 0,
        fileName: "",
        loadedText: "",
        statusText: "",
        progressStatus: null,
        progressTimer: null
      },
      // 表格默认排序配置
      defaultSort: {
        prop: 'createTime',
        order: 'descending'
      }
    };
  },
  created() {
    this.getList();
  },
  beforeDestroy() {
    clearTimeout(this.upload.progressTimer);
  },
  methods: {
    /** 查询试题列表 */
    getList() {
      this.loading = true;
      listQuestion(this.queryParams).then(response => {
        this.questionList = response.rows;
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
      this.form = {
        id: null,
        categoryId: null,
        questionType: null,
        questionTitle: null,
        questionContent: null,
        questionOptions: null,
        correctAnswer: null,
        questionAnalysis: null,
        difficultyLevel: 1,
        score: 1.0,
        tags: null,
        status: "0",
        remark: null,
        mediaList: []
      };
      this.mediaImages = '';
      this.mediaVideos = '';
      this.options = [
        { key: 'A', value: '', isCorrect: false },
        { key: 'B', value: '', isCorrect: false }
      ];
      this.singleCorrectAnswer = '';
      this.showOptions = false;
      this.fillBlanks = [];
      this.possibleBlanksCount = 0;
      this.resetForm("form");
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
      this.open = true;
      this.title = "添加试题";
      
      // 确保CategoryTreeSelect组件在对话框打开后重新加载
      this.$nextTick(() => {
        if (this.$refs.dialogCategorySelect && this.$refs.dialogCategorySelect.getCategoryList) {
          this.$refs.dialogCategorySelect.getCategoryList();
        }
      });
    },
    /** 题型变化处理 */
    handleTypeChange(value, keepExisting = false) {
      // 新增或用户切换题型时重置答案；编辑回显时保留接口返回的答案状态。
      if (!keepExisting) {
        this.singleCorrectAnswer = '';
      }
      
      if (value === 'single' || value === 'multiple' || value === 'judge') {
        this.showOptions = true;
        if (value === 'judge') {
          // 判断题固定选项
          if (!keepExisting || !this.options || this.options.length === 0) {
            this.options = [
              { key: 'true', value: '正确', isCorrect: false },
              { key: 'false', value: '错误', isCorrect: false }
            ];
          }
        } else if (value === 'single') {
          // 单选题默认4个选项
          if (!keepExisting || !this.options || this.options.length === 0) {
            this.options = [
              { key: 'A', value: '', isCorrect: false },
              { key: 'B', value: '', isCorrect: false },
              { key: 'C', value: '', isCorrect: false },
              { key: 'D', value: '', isCorrect: false }
            ];
          }
        } else if (value === 'multiple') {
          // 多选题默认4个选项
          if (!keepExisting || !this.options || this.options.length === 0) {
            this.options = [
              { key: 'A', value: '', isCorrect: false },
              { key: 'B', value: '', isCorrect: false },
              { key: 'C', value: '', isCorrect: false },
              { key: 'D', value: '', isCorrect: false }
            ];
          }
        }
      } else {
        // 填空题和简答题不显示选项
        this.showOptions = false;
        if (!keepExisting) {
          this.options = [];
        }
      }
      
      // 处理填空题
      if (value === 'fill') {
        // 只有当fillBlanks为空时才重新解析，避免覆盖已恢复的数据
        if (!keepExisting || this.fillBlanks.length === 0) {
          this.parseFillBlanks(this.form.questionContent);
        }
      } else if (!keepExisting) {
        this.fillBlanks = [];
        this.possibleBlanksCount = 0;
      }
      
      // 只有在新增模式下才清空正确答案，编辑模式保持原有答案
      if (!this.form.id && !keepExisting) {
        this.form.correctAnswer = '';
      }
    },
    /** 题目内容变化处理 */
    handleContentChange() {
      if (this.form.questionType === 'fill') {
        // 延迟执行，避免频繁触发
        clearTimeout(this.contentChangeTimer);
        this.contentChangeTimer = setTimeout(() => {
          this.smartParseFillBlanks();
        }, 500);
      }
    },
    /** 智能解析填空内容 */
    smartParseFillBlanks() {
      if (!this.form.questionContent || this.form.questionType !== 'fill') {
        this.fillBlanks = [];
        return;
      }
      
      const content = this.form.questionContent;
      
      // 检查是否已经有填空标记
      const hasExistingBlanks = /(\{\{空\}\}|\[空\]|_{3,})/g.test(content);
      
      if (hasExistingBlanks) {
        // 如果已有填空标记，直接解析
        this.parseFillBlanks(content);
      } else {
        // 如果没有填空标记，进行智能预识别（不自动替换内容）
        this.previewPossibleBlanks(content);
      }
    },
    /** 预览可能的填空位置（不修改内容） */
    previewPossibleBlanks(content) {
      // 智能识别可能的填空位置
      const bracketPatterns = [
        /（([^）]+)）/g,
        /\(([^)]+)\)/g,
        /【([^】]+)】/g,
        /\[([^\]]+)\]/g
      ];
      
      const underlinePattern = /_{3,}/g;
      
      let possibleBlanks = 0;
      
      // 计算可能的填空数量
      bracketPatterns.forEach(pattern => {
        const matches = content.match(pattern);
        if (matches) {
          possibleBlanks += matches.length;
        }
      });
      
      const underlineMatches = content.match(underlinePattern);
      if (underlineMatches) {
        possibleBlanks += underlineMatches.length;
      }
      
      // 更新可能的填空数量
      this.possibleBlanksCount = possibleBlanks;
      
      // 清空当前填空设置
      this.fillBlanks = [];
    },
    /** 添加选项 */
    addOption() {
      const keys = ['A', 'B', 'C', 'D', 'E', 'F', 'G', 'H'];
      if (this.options.length < keys.length) {
        this.options.push({
          key: keys[this.options.length],
          value: '',
          isCorrect: false
        });
      }
    },
    /** 删除选项 */
    removeOption(index) {
      this.options.splice(index, 1);
      // 重新设置选项标号
      const keys = ['A', 'B', 'C', 'D', 'E', 'F', 'G', 'H'];
      this.options.forEach((option, idx) => {
        option.key = keys[idx];
      });
    },
    /** 单选题答案变化处理 */
    handleSingleAnswerChange(value) {
      // 重置所有选项的正确答案标记
      this.options.forEach(option => {
        option.isCorrect = option.key === value;
      });
    },
    /** 多选题答案变化处理 */
    handleMultipleAnswerChange() {
      // 多选题的isCorrect已经通过v-model直接绑定，无需额外处理
    },
    /** 获取选项显示文本 */
    getOptionKeyDisplay(key) {
      if (this.form.questionType === 'judge') {
        return key === 'true' ? '正确' : '错误';
      }
      return key;
    },
    /** 顶部修改按钮操作 */
    handleUpdate() {
      this.reset();
      const id = this.ids[0];
      getQuestion(id).then(response => {
        this.form = response.data;
        this.restoreMediaFields();
        
        // 处理选项数据恢复
        if (this.form.questionOptions) {
          try {
            this.options = JSON.parse(this.form.questionOptions);
            this.showOptions = true;
            
            // 设置单选题的正确答案
            if (this.form.questionType === 'single' || this.form.questionType === 'judge') {
              const correctOption = this.options.find(opt => opt.isCorrect === true);
              if (correctOption) {
                this.singleCorrectAnswer = correctOption.key;
              }
            }
          } catch (e) {
            console.error('解析选项数据失败:', e);
            this.options = [];
          }
        }
        
        // 处理填空题数据恢复 - 在题型变化处理之前先恢复数据
        if (this.form.questionType === 'fill') {
          this.restoreFillBlanks(this.form.correctAnswer, this.form.questionContent);
        }
        
        // 处理题型变化 - 移到数据恢复之后，避免清空已恢复的数据
        this.handleTypeChange(this.form.questionType, true);
        
        this.open = true;
        this.title = "修改试题";
      });
    },
    /** 行内修改按钮操作 */
    handleRowUpdate(row) {
      this.reset();
      const id = row.id;
      getQuestion(id).then(response => {
        this.form = response.data;
        this.restoreMediaFields();
        
        // 处理选项数据恢复
        if (this.form.questionOptions) {
          try {
            this.options = JSON.parse(this.form.questionOptions);
            this.showOptions = true;
            
            // 设置单选题的正确答案
            if (this.form.questionType === 'single' || this.form.questionType === 'judge') {
              const correctOption = this.options.find(opt => opt.isCorrect === true);
              if (correctOption) {
                this.singleCorrectAnswer = correctOption.key;
              }
            }
          } catch (e) {
            console.error('解析选项数据失败:', e);
            this.options = [];
          }
        }
        
        // 处理填空题数据恢复 - 在题型变化处理之前先恢复数据
        if (this.form.questionType === 'fill') {
          this.restoreFillBlanks(this.form.correctAnswer, this.form.questionContent);
        }
        
        // 处理题型变化 - 移到数据恢复之后，避免清空已恢复的数据
        this.handleTypeChange(this.form.questionType, true);
        
        this.open = true;
        this.title = "修改试题";
      });
    },
    /** 预览按钮操作 */
    handlePreview(row) {
      previewQuestion(row.id).then(response => {
        this.previewData = response.data;
        this.previewOpen = true;
      }).catch(error => {
        console.error('预览请求失败:', error);
      });
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          this.form.mediaList = this.buildMediaList();

          // 验证选项题的正确答案设置
          if (this.showOptions) {
            const hasCorrectAnswer = this.options.some(opt => opt.isCorrect === true);
            if (!hasCorrectAnswer) {
              this.$modal.msgError("请至少设置一个正确答案");
              return;
            }
            
            // 单选题和判断题只能有一个正确答案
            if ((this.form.questionType === 'single' || this.form.questionType === 'judge')) {
              const correctCount = this.options.filter(opt => opt.isCorrect === true).length;
              if (correctCount !== 1) {
                this.$modal.msgError("单选题和判断题只能设置一个正确答案");
                return;
              }
            }
            
            this.form.questionOptions = JSON.stringify(this.options);
          }
          
          // 验证填空题的答案设置
          if (this.form.questionType === 'fill') {
            if (this.fillBlanks.length === 0) {
              this.$modal.msgError("请在题目内容中添加填空标记 {{空}} 或 [空]");
              return;
            }
            
            const hasEmptyAnswer = this.fillBlanks.some(blank => !blank.answers || blank.answers.trim() === '');
            if (hasEmptyAnswer) {
              this.$modal.msgError("请为所有填空设置正确答案");
              return;
            }
            
            // 更新填空题答案到表单
            this.updateFillAnswers();
          }
          
          if (this.form.id != null) {
            updateQuestion(this.form).then(response => {
              this.$modal.msgSuccess("修改成功");
              this.open = false;
              this.reset();
              this.getList();
            });
          } else {
            addQuestion(this.form).then(response => {
              this.$modal.msgSuccess("新增成功");
              this.open = false;
              this.reset();
              this.getList();
            });
          }
        }
      });
    },
    /** 删除按钮操作 */
    handleDelete() {
      const ids = this.ids;
      let confirmMessage = '';
      
      // 批量删除时显示选中的试题标题
      const selectedQuestions = this.questionList.filter(item => this.ids.includes(item.id));
      if (selectedQuestions.length === 1) {
        confirmMessage = '是否确认删除试题标题为"' + selectedQuestions[0].questionTitle + '"的数据项？';
      } else {
        const titles = selectedQuestions.map(item => item.questionTitle).join('、');
        confirmMessage = '是否确认删除以下试题：' + titles + '？';
      }
      
      this.$modal.confirm(confirmMessage).then(function() {
        return delQuestion(ids);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("删除成功");
      }).catch(() => {});
    },
    /** 导出按钮操作 */
    handleExport() {
      this.download('exam/question/export', {
        ...this.queryParams
      }, `question_${new Date().getTime()}.xlsx`)
    },
    /** 导入按钮操作 */
    handleImport() {
      this.upload.title = "试题导入";
      this.upload.open = true;
      this.resetImportProgress();
    },
    /** 下载模板操作 */
    handleTemplateDownload(command) {
      if (command === 'new') {
        // 下载按题型分类的模板
        this.download('exam/question/importTemplate', {}, `题目导入模板_${new Date().getTime()}.xlsx`)
      }
    },
    /** 下载模板操作（保持兼容性） */
    importTemplate() {
      this.handleTemplateDownload('new')
    },
    /** 下载错误文件 */
    downloadErrorFile() {
      this.download('exam/question/downloadErrorFile', {}, '题目导入错误报告.xlsx')
    },
    // 导入文件上传前处理
    handleImportBeforeUpload(file) {
      this.startImportProgress(file);
      return true;
    },
    // 文件上传中处理
    handleFileUploadProgress(event, file, fileList) {
      this.upload.isUploading = true;
      this.updateImportProgress(event, file);
    },
    // 导入文件上传失败处理
    handleImportUploadError(err, file, fileList) {
      this.upload.isUploading = false;
      this.finishImportProgress(false, "上传失败，请检查网络后重试");
      this.$modal.msgError("导入文件上传失败，请重试");
    },
    // 文件上传成功处理
    handleFileSuccess(response, file, fileList) {
      this.upload.isUploading = false;
      if (!response || response.code !== 200) {
        this.finishImportProgress(false, (response && response.msg) || "导入失败，请检查文件后重试");
        this.$modal.msgError((response && response.msg) || "导入失败，请检查文件后重试");
        return;
      }
      this.finishImportProgress(true, "上传解析完成");
      this.upload.open = false;
      this.$refs.upload.clearFiles();
      
      // 检查导入结果
      if (response.failureCount > 0) {
        // 有失败情况，显示错误信息和下载按钮
        const h = this.$createElement;
        this.$msgbox({
          title: '导入结果',
          message: h('div', { style: 'overflow: auto;overflow-x: hidden;max-height: 60vh;padding: 10px 20px 0;' }, [
            h('div', { style: 'margin-top: 15px; padding: 10px; background-color: #fff3cd; border: 1px solid #ffeaa7; border-radius: 4px;' }, [
              h('p', { style: 'margin: 0 0 10px 0; color: #856404; font-weight: bold;' }, '检测到导入错误'),
              h('p', { style: 'margin: 0 0 10px 0; color: #856404;' }, `成功导入: ${response.successCount || 0} 条，失败: ${response.failureCount} 条`),
              h('el-button', {
                props: { type: 'warning', size: 'small' },
                on: { click: () => this.downloadErrorFile() }
              }, '下载错误详情文件')
            ])
          ]),
          showConfirmButton: true,
          confirmButtonText: '确定'
        });
      } else {
        // 全部成功导入，显示简洁的成功信息
        let successMsg = `成功导入 ${response.successCount || 0} 条题目`;
        this.$alert(successMsg, "导入结果", { 
          type: 'success',
          confirmButtonText: '确定'
        });
      }
      
      this.getList();
    },
    // 提交上传文件
    submitFileForm() {
      const files = this.$refs.upload && this.$refs.upload.uploadFiles;
      if (!files || files.length === 0) {
        this.$modal.msgWarning("请先选择要导入的文件");
        return;
      }
      this.$refs.upload.submit();
    },
    startImportProgress(file) {
      clearTimeout(this.upload.progressTimer);
      this.upload.isUploading = true;
      this.upload.progressVisible = true;
      this.upload.percent = 0;
      this.upload.fileName = file.name;
      this.upload.loadedText = `0 B / ${this.formatUploadSize(file.size)}`;
      this.upload.statusText = "准备上传，请勿关闭页面";
      this.upload.progressStatus = null;
    },
    updateImportProgress(event, file) {
      const total = event.total || file.size || 0;
      const loaded = event.loaded || Math.round(total * (event.percent || 0) / 100);
      const rawPercent = event.percent || (total ? loaded / total * 100 : 0);
      this.upload.percent = Math.min(99, Math.max(1, Math.floor(rawPercent)));
      this.upload.fileName = file.name;
      this.upload.loadedText = total
        ? `${this.formatUploadSize(loaded)} / ${this.formatUploadSize(total)}`
        : this.formatUploadSize(loaded);
      this.upload.statusText = this.upload.percent >= 99 ? "文件已上传，正在等待服务器解析" : "正在上传，请勿关闭页面";
      this.upload.progressStatus = null;
      this.upload.progressVisible = true;
    },
    finishImportProgress(success, statusText) {
      clearTimeout(this.upload.progressTimer);
      this.upload.percent = success ? 100 : this.upload.percent;
      this.upload.progressStatus = success ? "success" : "exception";
      this.upload.statusText = statusText;
      this.upload.progressVisible = true;
      this.upload.progressTimer = setTimeout(() => {
        this.resetImportProgress();
      }, success ? 1200 : 2500);
    },
    resetImportProgress() {
      clearTimeout(this.upload.progressTimer);
      this.upload.percent = 0;
      this.upload.fileName = "";
      this.upload.loadedText = "";
      this.upload.statusText = "";
      this.upload.progressStatus = null;
      this.upload.progressVisible = false;
    },
    formatUploadSize(size) {
      if (!size) {
        return "0 B";
      }
      if (size < 1024) {
        return `${size} B`;
      }
      if (size < 1024 * 1024) {
        return `${(size / 1024).toFixed(1)} KB`;
      }
      return `${(size / 1024 / 1024).toFixed(1)} MB`;
    },
    /** 表格排序变化处理 */
    handleSortChange({ prop, order }) {
      this.queryParams.orderByColumn = prop;
      this.queryParams.isAsc = order;
      this.getList();
    },
    /** 从试题媒体恢复上传组件值 */
    restoreMediaFields() {
      const mediaList = this.form.mediaList || [];
      this.mediaImages = mediaList
        .filter(item => item.mediaType === 'image' && item.fileUrl)
        .sort((a, b) => (a.sortOrder || 0) - (b.sortOrder || 0))
        .map(item => item.fileUrl)
        .join(',');
      this.mediaVideos = mediaList
        .filter(item => item.mediaType === 'video' && item.fileUrl)
        .sort((a, b) => (a.sortOrder || 0) - (b.sortOrder || 0))
        .map(item => item.fileUrl)
        .join(',');
    },
    /** 构建提交到后端的媒体列表 */
    buildMediaList() {
      const mediaList = [];
      this.splitUploadValue(this.mediaImages).forEach((url, index) => {
        mediaList.push({
          mediaType: 'image',
          fileUrl: url,
          fileName: this.getUploadFileName(url),
          sortOrder: index,
          displayPosition: 'stem',
          status: '0'
        });
      });
      this.splitUploadValue(this.mediaVideos).forEach((url, index) => {
        mediaList.push({
          mediaType: 'video',
          fileUrl: url,
          fileName: this.getUploadFileName(url),
          sortOrder: mediaList.length + index,
          displayPosition: 'stem',
          status: '0'
        });
      });
      return mediaList;
    },
    splitUploadValue(value) {
      if (!value) return [];
      if (Array.isArray(value)) {
        return value.map(item => {
          if (typeof item === 'string') return item;
          return item.url || item.fileUrl || '';
        }).filter(Boolean);
      }
      return String(value).split(',').map(item => item.trim()).filter(Boolean);
    },
    getUploadFileName(url) {
      if (!url) return '';
      const cleanUrl = url.split('?')[0];
      return cleanUrl.substring(cleanUrl.lastIndexOf('/') + 1);
    },
    /** 解析填空题内容 */
    parseFillBlanks(content) {
      if (!content || this.form.questionType !== 'fill') {
        this.fillBlanks = [];
        this.possibleBlanksCount = 0;
        return;
      }
      
      // 匹配手工标记和导入模板里的下划线空位。
      const blankPattern = /(\{\{空\}\}|\[空\]|_{3,})/g;
      const matches = content.match(blankPattern);
      
      if (matches) {
        const blankCount = matches.length;
        // 保持现有答案，只调整数量
        const currentBlanks = [...this.fillBlanks];
        this.fillBlanks = [];
        
        for (let i = 0; i < blankCount; i++) {
          this.fillBlanks.push({
            index: i + 1,
            answers: currentBlanks[i] ? currentBlanks[i].answers : ''
          });
        }
        
        // 已有填空标记时，清空可能的填空提示
        this.possibleBlanksCount = 0;
      } else {
        this.fillBlanks = [];
      }
    },
    /** 更新填空题答案到表单 */
    updateFillAnswers() {
      if (this.form.questionType === 'fill' && this.fillBlanks.length > 0) {
        // 将所有填空答案组合成JSON格式
        const answersArray = this.fillBlanks.map(blank => {
          return blank.answers ? blank.answers.split(';').map(ans => ans.trim()).filter(ans => ans) : [];
        });
        this.form.correctAnswer = JSON.stringify(answersArray);
      }
    },
    /** 从JSON答案恢复填空题数据 */
    restoreFillBlanks(correctAnswer, questionContent) {
      if (this.form.questionType !== 'fill') {
        return;
      }
      
      // 先解析题目内容中的填空数量
      this.parseFillBlanks(questionContent);
      
      // 然后恢复答案数据
      if (correctAnswer) {
        try {
          const answersArray = JSON.parse(correctAnswer);
          if (Array.isArray(answersArray)) {
            answersArray.forEach((answers, index) => {
              if (this.fillBlanks[index] && Array.isArray(answers)) {
                this.fillBlanks[index].answers = answers.join(';');
              }
            });
          }
        } catch (e) {
          // 兼容导入模板：多个填空答案用 | 分隔，题干常用 ___ 表示空位。
          const answers = String(correctAnswer).split('|').map(answer => answer.trim());
          answers.forEach((answer, index) => {
            if (this.fillBlanks[index]) {
              this.fillBlanks[index].answers = answer;
            }
          });
          if (answers.length === 1 && this.fillBlanks.length > 0) {
            this.fillBlanks[0].answers = answers[0];
          }
        }
      }
    },
    /** 智能识别填空位置 */
    autoGenerateBlanks() {
      if (!this.form.questionContent || this.form.questionType !== 'fill') {
        this.$modal.msgWarning('请先输入题目内容');
        return;
      }
      
      const content = this.form.questionContent;
      let updatedContent = content;
      let blankCount = 0;
      
      // 智能识别模式：寻找可能的填空位置
      // 1. 识别括号内容：（答案）、(答案)、【答案】、[答案]
      const bracketPatterns = [
        /（([^）]+)）/g,
        /\(([^)]+)\)/g,
        /【([^】]+)】/g,
        /\[([^\]]+)\]/g
      ];
      
      // 2. 识别下划线：____、_____等
      const underlinePattern = /_{3,}/g;
      
      // 3. 识别已有的填空标记
      const existingBlankPattern = /(\{\{空\}\}|\[空\])/g;
      
      // 保存识别到的答案
      const detectedAnswers = [];
      
      // 处理括号内容
      bracketPatterns.forEach(pattern => {
        updatedContent = updatedContent.replace(pattern, (match, answer) => {
          detectedAnswers.push(answer.trim());
          blankCount++;
          return '{{空}}';
        });
      });
      
      // 处理下划线
      updatedContent = updatedContent.replace(underlinePattern, () => {
        detectedAnswers.push('');
        blankCount++;
        return '{{空}}';
      });
      
      // 如果没有识别到任何填空，提示用户
      if (blankCount === 0 && !existingBlankPattern.test(content)) {
        this.$modal.confirm('未能自动识别到填空位置。是否手动添加一个填空？').then(() => {
          this.addBlankManually();
        }).catch(() => {});
        return;
      }
      
      // 更新题目内容
      this.form.questionContent = updatedContent;
      
      // 重新解析填空
      this.parseFillBlanks(updatedContent);
      
      // 设置识别到的答案
      detectedAnswers.forEach((answer, index) => {
        if (this.fillBlanks[index] && answer) {
          this.fillBlanks[index].answers = answer;
        }
      });
      
      // 更新表单答案
      this.updateFillAnswers();
      
      this.$modal.msgSuccess(`智能识别成功！共识别到 ${this.fillBlanks.length} 个填空位置`);
    },
    /** 插入填空选项 */
    insertBlankOption() {
      if (this.form.questionType !== 'fill') {
        this.$modal.msgWarning("请先选择填空题类型");
        return;
      }
      
      // 获取输入框的DOM元素
      const textareaElement = this.$refs.questionContentInput.$refs.textarea;
      if (!textareaElement) {
        // 如果无法获取输入框引用，则在末尾添加
        if (!this.form.questionContent) {
          this.form.questionContent = '{{空}}';
        } else {
          if (!this.form.questionContent.endsWith(' ')) {
            this.form.questionContent += ' ';
          }
          this.form.questionContent += '{{空}}';
        }
      } else {
        // 获取光标位置和选中文本
        const start = textareaElement.selectionStart;
        const end = textareaElement.selectionEnd;
        const content = this.form.questionContent || '';
        
        // 如果有选中文本，替换选中的文本；否则在光标位置插入
        const beforeCursor = content.substring(0, start);
        const afterCursor = content.substring(end);
        
        // 插入填空标记
        this.form.questionContent = beforeCursor + '{{空}}' + afterCursor;
        
        // 设置新的光标位置（在插入的填空标记之后）
        this.$nextTick(() => {
          const newCursorPos = start + '{{空}}'.length;
          textareaElement.setSelectionRange(newCursorPos, newCursorPos);
          textareaElement.focus();
        });
      }
      
      // 重新解析填空
      this.parseFillBlanks(this.form.questionContent);
      
      this.$modal.msgSuccess("填空选项已插入，请设置正确答案");
    },
    
    /** 手动添加填空 */
    addBlankManually() {
      if (this.form.questionType !== 'fill') {
        return;
      }
      
      // 在题目内容末尾添加填空标记
      if (!this.form.questionContent) {
        this.form.questionContent = '{{空}}';
      } else {
        this.form.questionContent += '{{空}}';
      }
      
      // 重新解析填空
      this.parseFillBlanks(this.form.questionContent);
      
      this.$modal.msgSuccess('已添加一个填空位置');
    },
    /** 删除指定填空 */
    removeBlank(index) {
      if (index < 0 || index >= this.fillBlanks.length) {
        return;
      }
      
      // 从题目内容中移除对应的填空标记
      let content = this.form.questionContent;
      const blankPattern = /(\{\{空\}\}|\[空\]|_{3,})/g;
      let matchCount = 0;
      
      content = content.replace(blankPattern, (match) => {
        if (matchCount === index) {
          matchCount++;
          return ''; // 删除这个填空标记
        }
        matchCount++;
        return match;
      });
      
      this.form.questionContent = content;
      
      // 重新解析填空
      this.parseFillBlanks(content);
      
      // 更新表单答案
      this.updateFillAnswers();
      
      this.$modal.msgSuccess('已删除填空位置');
    },
    /** 清空所有填空 */
    clearAllBlanks() {
      this.$modal.confirm('确定要清空所有填空设置吗？此操作不可恢复。').then(() => {
        // 清空填空数据
        this.fillBlanks = [];
        
        // 从题目内容中移除所有填空标记
        if (this.form.questionContent) {
          this.form.questionContent = this.form.questionContent
            .replace(/(\{\{空\}\}|\[空\]|_{3,})/g, '')
            .replace(/\s+/g, ' ')
            .trim();
        }
        
        // 清空表单答案
        this.form.correctAnswer = '';
        
        this.$modal.msgSuccess('已清空所有填空设置');
      }).catch(() => {});
    }
  }
};
</script>

<style scoped>
.question-media-editor {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.media-upload-title {
  margin-bottom: 8px;
  color: #606266;
  font-size: 13px;
  font-weight: 600;
}

.import-upload-progress {
  margin: 12px 0 4px;
  padding: 10px 12px;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  background: #f8fafc;
}

.import-upload-progress__meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  margin-bottom: 6px;
  color: #606266;
  font-size: 12px;
}

.import-upload-progress__name {
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.import-upload-progress__size {
  flex: none;
  color: #909399;
}

.import-upload-progress__hint {
  margin-top: 4px;
  color: #909399;
  font-size: 12px;
}
</style>
