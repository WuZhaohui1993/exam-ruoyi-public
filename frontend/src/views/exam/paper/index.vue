<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="试卷名称" prop="paperName">
        <el-input
          v-model="queryParams.paperName"
          placeholder="请输入试卷名称"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="分类" prop="categoryId">
        <div style="width: 240px">
          <paper-category-tree-select v-model="queryParams.categoryId" placeholder="请选择分类" />
        </div>
      </el-form-item>
      <el-form-item label="试卷类型" prop="paperType">
        <el-select v-model="queryParams.paperType" placeholder="请选择试卷类型" clearable>
          <el-option label="固定试卷" value="fixed" />
          <el-option label="随机试卷" value="random" />
          <el-option label="混合试卷" value="mixed" />
        </el-select>
      </el-form-item>
      <el-form-item label="难度等级" prop="difficultyLevel">
        <el-select v-model="queryParams.difficultyLevel" placeholder="请选择难度等级" clearable>
          <el-option label="简单" :value="1" />
          <el-option label="中等" :value="2" />
          <el-option label="困难" :value="3" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="试卷状态" clearable>
          <el-option label="正常" value="0" />
          <el-option label="停用" value="1" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-dropdown @command="handleCreate">
          <el-button type="primary" plain size="mini">
            创建试卷<i class="el-icon-arrow-down el-icon--right"></i>
          </el-button>
          <el-dropdown-menu slot="dropdown">
            <el-dropdown-item command="fixed" v-hasPermi="['exam:paper:add']">
              <i class="el-icon-document-copy"></i> 固定试卷
            </el-dropdown-item>
            <el-dropdown-item command="random" v-hasPermi="['exam:paper:add']">
              <i class="el-icon-s-data"></i> 随机试卷
            </el-dropdown-item>
            <el-dropdown-item command="mixed" v-hasPermi="['exam:paper:add']">
              <i class="el-icon-s-grid"></i> 混合试卷
            </el-dropdown-item>
          </el-dropdown-menu>
        </el-dropdown>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="el-icon-edit"
          size="mini"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['exam:paper:edit']"
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
          v-hasPermi="['exam:paper:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
          v-hasPermi="['exam:paper:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="paperList" @selection-change="handleSelectionChange" :default-sort="defaultSort" @sort-change="handleSortChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="试卷名称" align="center" prop="paperName" min-width="250" :show-overflow-tooltip="true" />
      <el-table-column label="分类" align="center" prop="categoryName" width="100" />
      <el-table-column label="试卷类型" align="center" prop="paperType" width="90">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.exam_paper_type" :value="scope.row.paperType"/>
        </template>
      </el-table-column>
      <el-table-column label="总分值" align="center" prop="totalScore" width="70" sortable="custom" :sort-orders="['descending', 'ascending']" />
      <el-table-column label="题目数量" align="center" prop="questionCount" width="80" sortable="custom" :sort-orders="['descending', 'ascending']" />
      <el-table-column label="考试时长" align="center" prop="duration" width="90" sortable="custom" :sort-orders="['descending', 'ascending']">
        <template slot-scope="scope">
          {{ scope.row.duration }}分钟
        </template>
      </el-table-column>
      <el-table-column label="难度等级" align="center" prop="difficultyLevel" width="90" sortable="custom" :sort-orders="['descending', 'ascending']">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.exam_difficulty_level" :value="scope.row.difficultyLevel"/>
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" prop="status" width="70">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.sys_normal_disable" :value="scope.row.status"/>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" align="center" prop="createTime" width="160" sortable="custom" :sort-orders="['descending', 'ascending']">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="160">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-view"
            @click="handlePreview(scope.row)"
            v-hasPermi="['exam:paper:query']"
          >预览</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleRowUpdate(scope.row)"
            v-hasPermi="['exam:paper:edit']"
          >修改</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-document-copy"
            @click="handleCopy(scope.row)"
            v-hasPermi="['exam:paper:add']"
          >复制</el-button>
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

    <!-- 添加或修改试卷对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="600px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="试卷名称" prop="paperName">
          <el-input v-model="form.paperName" placeholder="请输入试卷名称" />
        </el-form-item>
        <el-form-item label="试卷分类" prop="categoryId">
          <paper-category-tree-select v-model="form.categoryId" placeholder="选择试卷分类" />
        </el-form-item>
        <el-form-item label="试卷描述" prop="paperDescription">
          <el-input v-model="form.paperDescription" type="textarea" placeholder="请输入试卷描述" />
        </el-form-item>
        <el-form-item label="及格分数" prop="passScore">
          <el-input-number v-model="form.passScore" :precision="2" :min="0" :max="999" />
        </el-form-item>
        <el-form-item label="考试时长" prop="duration">
          <el-input-number v-model="form.duration" :min="1" :max="999" />
          <span style="margin-left: 10px;">分钟</span>
        </el-form-item>
        <el-form-item label="难度等级" prop="difficultyLevel">
          <el-radio-group v-model="form.difficultyLevel">
            <el-radio :label="1">简单</el-radio>
            <el-radio :label="2">中等</el-radio>
            <el-radio :label="3">困难</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="打乱题目" prop="shuffleQuestions">
          <el-radio-group v-model="form.shuffleQuestions">
            <el-radio label="0">否</el-radio>
            <el-radio label="1">是</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="打乱选项" prop="shuffleOptions">
          <el-radio-group v-model="form.shuffleOptions">
            <el-radio label="0">否</el-radio>
            <el-radio label="1">是</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" placeholder="请输入内容" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer" style="text-align: center;">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>


  </div>
</template>

<script>
import { listPaper, getPaper, delPaper, addPaper, updatePaper, copyPaper } from "@/api/exam/paper";
import PaperCategoryTreeSelect from "@/components/PaperCategoryTreeSelect";

export default {
  name: "Paper",
  dicts: ['sys_normal_disable', 'exam_paper_type', 'exam_difficulty_level'],
  components: {
    PaperCategoryTreeSelect
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
      // 试卷表格数据
      paperList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        paperName: null,
        categoryId: null,
        paperType: null,
        difficultyLevel: null,
        status: null
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        paperName: [
          { required: true, message: "试卷名称不能为空", trigger: "blur" }
        ],
        categoryId: [
          { required: true, message: "试卷分类不能为空", trigger: "change" }
        ],
        passScore: [
          { required: true, message: "及格分数不能为空", trigger: "blur" }
        ],
        duration: [
          { required: true, message: "考试时长不能为空", trigger: "blur" }
        ]
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
  watch: {
    '$route.query.refresh'() {
      // 监听刷新参数变化，重新加载列表
      this.getList();
    }
  },
  methods: {
    /** 查询试卷列表 */
    getList() {
      this.loading = true;
      listPaper(this.queryParams).then(response => {
        this.paperList = response.rows;
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
        paperName: null,
        categoryId: null,
        paperDescription: null,
        passScore: null,
        duration: null,
        difficultyLevel: 1,
        shuffleQuestions: "0",
        shuffleOptions: "0",
        remark: null
      };
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

    /** 创建试卷操作 */
    handleCreate(command) {
      this.$router.push("/paper-manage/exam/paper/create/" + command);
    },
        /** 顶部修改按钮操作 */
    handleUpdate() {
      this.reset();
      const id = this.ids[0];
      getPaper(id).then(response => {
        this.form = response.data;
        this.open = true;
        this.title = "修改试卷";
      });
    },
    /** 行内修改按钮操作 */
    handleRowUpdate(row) {
      this.reset();
      const id = row.id;
      getPaper(id).then(response => {
        this.form = response.data;
        this.open = true;
        this.title = "修改试卷";
      });
    },
    /** 预览按钮操作 */
    handlePreview(row) {
      this.$router.push(`/paper-manage/exam/paper/fullscreen-preview/${row.id}`);
    },
    /** 复制按钮操作 */
    handleCopy(row) {
      const id = row.id;
      copyPaper(id).then(response => {
        this.$modal.msgSuccess("复制成功");
        this.getList();
      });
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.id != null) {
            updatePaper(this.form).then(response => {
              this.$modal.msgSuccess("修改成功");
              this.open = false;
              this.getList();
            });
          } else {
            addPaper(this.form).then(response => {
              this.$modal.msgSuccess("新增成功");
              this.open = false;
              this.getList();
            });
          }
        }
      });
    },
    /** 顶部删除按钮操作 */
    handleDelete() {
      const ids = this.ids;
      let confirmMessage = '';
      
      // 批量删除时显示选中的试卷名称
      const selectedPapers = this.paperList.filter(item => this.ids.includes(item.id));
      if (selectedPapers.length === 1) {
        confirmMessage = '是否确认删除试卷名称为"' + selectedPapers[0].paperName + '"的数据项？';
      } else {
        const names = selectedPapers.map(item => item.paperName).join('、');
        confirmMessage = '是否确认删除以下试卷：' + names + '？';
      }
      
      this.$modal.confirm(confirmMessage).then(function() {
        return delPaper(ids);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("删除成功");
      }).catch(() => {});
    },

    /** 导出按钮操作 */
    handleExport() {
      this.download('exam/paper/export', {
        ...this.queryParams
      }, `paper_${new Date().getTime()}.xlsx`)
    },
    /** 表格排序变化处理 */
    handleSortChange({ prop, order }) {
      this.queryParams.orderByColumn = prop;
      this.queryParams.isAsc = order;
      this.getList();
    }
  }
};
</script>
