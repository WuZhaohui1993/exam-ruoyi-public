<template>
  <div>
    <!-- 显示选中的试卷 -->
    <el-input
      v-model="selectedPaperName"
      :placeholder="placeholder"
      :disabled="disabled"
      readonly
      @click="!disabled && handleOpenDialog()"
      style="width: 100%; cursor: pointer;"
    >
      <el-button
        slot="append"
        icon="el-icon-search"
        @click="handleOpenDialog"
        :disabled="disabled"
      />
    </el-input>

    <!-- 试卷选择弹窗 -->
    <el-dialog
      title="选择试卷"
      :visible.sync="dialogVisible"
      width="900px"
      append-to-body
    >
      <!-- 搜索条件 -->
      <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" label-width="80px">
        <el-form-item label="试卷名称" prop="paperName">
          <el-input
            v-model="queryParams.paperName"
            placeholder="请输入试卷名称"
            clearable
            style="width: 200px"
            @keyup.enter.native="handleQuery"
          />
        </el-form-item>
        <el-form-item label="试卷分类" prop="categoryId">
          <div style="width: 150px">
            <paper-category-tree-select
              ref="paperCategoryTreeSelect"
              v-model="queryParams.categoryId"
              placeholder="请选择分类"
              clearable
            />
          </div>
        </el-form-item>
        <el-form-item label="试卷类型" prop="paperType">
          <el-select v-model="queryParams.paperType" placeholder="试卷类型" clearable style="width: 120px">
            <el-option label="固定试卷" value="fixed" />
            <el-option label="随机试卷" value="random" />
            <el-option label="混合试卷" value="mixed" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
          <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 试卷列表 -->
      <el-table
        v-loading="loading"
        :data="paperList"
        @selection-change="handleSelectionChange"
        @row-click="handleRowClick"
        highlight-current-row
        height="400px"
      >
        <el-table-column label="" width="55" align="center">
          <template slot-scope="scope">
            <div 
              class="custom-radio"
              :class="{ 'selected': selectedPaperId === scope.row.id }"
              @click="handleRadioSelect(scope.row)"
            >
              <div class="radio-circle">
                <div v-if="selectedPaperId === scope.row.id" class="radio-dot"></div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="试卷名称" prop="paperName" :show-overflow-tooltip="true" min-width="150" />
        <el-table-column label="试卷分类" prop="categoryName" width="120" />
        <el-table-column label="试卷类型" prop="paperType" width="100">
          <template slot-scope="scope">
            <el-tag
              :type="scope.row.paperType === 'fixed' ? 'success' : scope.row.paperType === 'random' ? 'warning' : 'info'"
              size="mini"
            >
              {{ scope.row.paperType === 'fixed' ? '固定' : scope.row.paperType === 'random' ? '随机' : '混合' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="题目数量" prop="questionCount" width="100" align="center">
          <template slot-scope="scope">
            {{ scope.row.questionCount || 0 }} 题
          </template>
        </el-table-column>
        <el-table-column label="总分" prop="totalScore" width="80" align="center">
          <template slot-scope="scope">
            {{ scope.row.totalScore || 0 }} 分
          </template>
        </el-table-column>
        <el-table-column label="创建时间" prop="createTime" width="160">
          <template slot-scope="scope">
            <span>{{ parseTime(scope.row.createTime, '{y}-{m}-{d} {h}:{i}') }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" width="100">
          <template slot-scope="scope">
            <el-button
              size="mini"
              type="text"
              icon="el-icon-view"
              @click="handlePreview(scope.row)"
            >预览</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <pagination
        v-show="total > 0"
        :total="total"
        :page.sync="queryParams.pageNum"
        :limit.sync="queryParams.pageSize"
        @pagination="getList"
        style="margin-top: 15px;"
      />

      <!-- 弹窗按钮 -->
      <div slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="handleConfirm" :disabled="!selectedPaper">确 定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listPaper, getPaper } from "@/api/exam/paper";
import Pagination from "@/components/Pagination";
import PaperCategoryTreeSelect from "@/components/PaperCategoryTreeSelect";

export default {
  name: "PaperSelect",
  components: {
    Pagination,
    PaperCategoryTreeSelect
  },
  props: {
    // 绑定值
    value: {
      type: [String, Number],
      default: null
    },
    // 占位符
    placeholder: {
      type: String,
      default: "请选择试卷"
    },
    // 是否可清空
    clearable: {
      type: Boolean,
      default: true
    },
    // 是否禁用
    disabled: {
      type: Boolean,
      default: false
    },
    // 是否可搜索
    filterable: {
      type: Boolean,
      default: true
    },
    // 是否远程搜索
    remote: {
      type: Boolean,
      default: false
    },
    // 分类ID过滤
    categoryId: {
      type: [String, Number],
      default: null
    },
    // 试卷类型过滤
    paperType: {
      type: String,
      default: null
    }
  },
  data() {
    return {
      // 试卷选项
      paperOptions: [],
      // 选中值
      selectedValue: this.value,
      // 加载状态
      loading: false,
      // 弹窗显示状态
      dialogVisible: false,
      // 查询参数
      queryParams: {
        paperName: '',
        categoryId: null,
        paperType: '',
        pageNum: 1,
        pageSize: 10
      },
      // 选中的试卷ID
      selectedPaperId: null,
      // 选中的试卷名称
      selectedPaperName: '',
      // 选中的试卷对象
      selectedPaper: null,
      // 试卷列表
      paperList: [],
      // 总条数
      total: 0
    };
  },
  watch: {
    value(newVal) {
      this.selectedValue = newVal;
      // 当value变化时，更新显示的试卷名称
      this.updateSelectedPaperName();
    },
    categoryId() {
      this.getPaperList();
    },
    paperType() {
      this.getPaperList();
    },
    dialogVisible(val) {
      if (val) {
        // 打开对话框时，重新加载数据
        this.queryParams.pageNum = 1;
        this.getList();
        // 设置当前选中项
        this.selectedPaperId = this.selectedValue;
        // 刷新试卷分类数据
        this.$nextTick(() => {
          if (this.$refs.paperCategoryTreeSelect && this.$refs.paperCategoryTreeSelect.refresh) {
            this.$refs.paperCategoryTreeSelect.refresh();
          }
        });
      }
    }
  },
  created() {
    this.getPaperList();
  },
  mounted() {
    // 组件挂载后，确保初始值能正确显示
    this.$nextTick(() => {
      this.updateSelectedPaperName();
    });
  },
  methods: {
    /** 获取试卷列表 */
    getPaperList() {
      this.loading = true;
      const params = {
        status: '0' // 只获取正常状态的试卷
      };

      // 如果指定了分类ID，则按分类过滤
      if (this.categoryId) {
        params.categoryId = this.categoryId;
      }

      // 如果指定了试卷类型，则按类型过滤
      if (this.paperType) {
        params.paperType = this.paperType;
      }

      listPaper(params).then(response => {
        this.paperOptions = response.rows || [];
        this.loading = false;
        // 数据加载完成后，更新试卷名称显示
        this.$nextTick(() => {
          this.updateSelectedPaperName();
        });
      }).catch(error => {
        console.error('获取试卷列表失败:', error);
        this.paperOptions = [];
        this.loading = false;
      });
    },
    /** 远程搜索 */
    remoteSearch(query) {
      if (query !== '') {
        this.loading = true;
        const params = {
          paperName: query,
          status: '0'
        };

        if (this.categoryId) {
          params.categoryId = this.categoryId;
        }

        if (this.paperType) {
          params.paperType = this.paperType;
        }

        listPaper(params).then(response => {
          this.paperOptions = response.rows || [];
          this.loading = false;
        }).catch(() => {
          this.paperOptions = [];
          this.loading = false;
        });
      } else {
        this.paperOptions = [];
      }
    },
    /** 处理选择变化 */
    handleChange(value) {
      this.selectedValue = value;
      this.$emit('input', value);
      this.$emit('change', value);

      // 触发试卷选择事件，传递完整的试卷信息
      const selectedPaper = this.paperOptions.find(paper => paper.id === value);
      this.$emit('paper-select', selectedPaper);
    },
    /** 处理清空 */
    handleClear() {
      this.selectedValue = null;
      this.$emit('input', null);
      this.$emit('change', null);
      this.$emit('paper-select', null);
    },
    /** 打开选择弹窗 */
    handleOpenDialog() {
      this.dialogVisible = true;
    },
    /** 搜索 */
    handleQuery() {
      this.queryParams.pageNum = 1;
      this.getList();
    },
    /** 重置搜索 */
    resetQuery() {
      this.queryParams = {
        paperName: '',
        categoryId: null,
        paperType: '',
        pageNum: 1,
        pageSize: 10
      };
      this.getList();
    },
    /** 获取试卷列表 */
    getList() {
      this.loading = true;
      const params = {
        ...this.queryParams,
        status: '0' // 只获取正常状态的试卷
      };

      listPaper(params).then(response => {
        this.paperList = response.rows || [];
        this.total = response.total || 0;
        this.loading = false;
      }).catch(error => {
        console.error('获取试卷列表失败:', error);
        this.paperList = [];
        this.total = 0;
        this.loading = false;
      });
    },
    /** 表格选择变化 */
    handleSelectionChange(selection) {
      // 这个方法在单选模式下不会用到
    },
    /** 行点击 */
    handleRowClick(row) {
      this.selectedPaperId = row.id;
      this.selectedPaper = row;
    },
    /** 单选按钮选择 */
    handleRadioSelect(row) {
      this.selectedPaperId = row.id;
      this.selectedPaper = row;
    },
    /** 确认选择 */
    async handleConfirm() {
      if (this.selectedPaper) {
        this.selectedValue = this.selectedPaper.id;
        this.selectedPaperName = this.selectedPaper.paperName;
        this.$emit('input', this.selectedValue);
        this.$emit('change', this.selectedValue);
        
        // 获取试卷完整详细信息
        try {
          const response = await getPaper(this.selectedPaper.id);
          if (response.data) {
            this.$emit('paper-select', response.data);
          } else {
            this.$emit('paper-select', this.selectedPaper);
          }
        } catch (error) {
          console.error('获取试卷详细信息失败:', error);
          this.$emit('paper-select', this.selectedPaper);
        }
        
        this.dialogVisible = false;
      }
    },
    /** 预览试卷 */
    handlePreview(row) {
      // 打开试卷预览页面
      const routeUrl = this.$router.resolve({
        path: `/paper-manage/exam/paper/fullscreen-preview/${row.id}`
      });
      window.open(routeUrl.href, '_blank');
    },
    /** 时间格式化 */
    parseTime(time, format) {
      if (!time) return '-';
      const date = new Date(time);
      const year = date.getFullYear();
      const month = String(date.getMonth() + 1).padStart(2, '0');
      const day = String(date.getDate()).padStart(2, '0');
      const hour = String(date.getHours()).padStart(2, '0');
      const minute = String(date.getMinutes()).padStart(2, '0');

      return format
        .replace('{y}', year)
        .replace('{m}', month)
        .replace('{d}', day)
        .replace('{h}', hour)
        .replace('{i}', minute);
    },
    /** 更新选中试卷名称 */
    updateSelectedPaperName() {
      if (this.selectedValue) {
        if (this.paperOptions.length > 0) {
          // 如果paperOptions已有数据，直接查找
          const paper = this.paperOptions.find(p => p.id === this.selectedValue);
          this.selectedPaperName = paper ? paper.paperName : '';
        } else {
          // 如果paperOptions为空，主动调用API获取试卷信息
          this.loadSelectedPaperInfo();
        }
      } else {
        this.selectedPaperName = '';
      }
    },
    /** 加载选中试卷的信息 */
    loadSelectedPaperInfo() {
      if (!this.selectedValue) return;
      
      // 使用getPaper API直接获取试卷详情
      getPaper(this.selectedValue).then(response => {
        if (response.data) {
          const paper = response.data;
          this.selectedPaperName = paper.paperName;
          // 同时将这个试卷添加到options中，避免重复请求
          if (!this.paperOptions.find(p => p.id === paper.id)) {
            this.paperOptions.unshift(paper);
          }
        }
      }).catch(error => {
        console.error('获取选中试卷信息失败:', error);
        this.selectedPaperName = '';
      });
    }
  }
};
</script>

<style scoped>
/* 自定义单选按钮样式 */
.custom-radio {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  padding: 4px;
}

.radio-circle {
  width: 14px;
  height: 14px;
  border: 1px solid #dcdfe6;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #fff;
  transition: all 0.3s;
}

.custom-radio:hover .radio-circle {
  border-color: #409eff;
}

.custom-radio.selected .radio-circle {
  border-color: #409eff;
  background-color: #409eff;
}

.radio-dot {
  width: 4px;
  height: 4px;
  border-radius: 50%;
  background-color: #fff;
}
</style>
