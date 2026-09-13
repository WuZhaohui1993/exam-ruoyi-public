<template>
  <div>
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" label-width="68px">
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
      <el-form-item label="题目类型" prop="questionType">
        <el-select v-model="queryParams.questionType" placeholder="请选择题目类型" clearable style="width: 240px">
          <el-option
            v-for="dict in dict.type.exam_question_type"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="难度等级" prop="difficultyLevel">
        <el-select v-model="queryParams.difficultyLevel" placeholder="请选择难度等级" clearable style="width: 240px">
          <el-option label="简单" :value="1" />
          <el-option label="中等" :value="2" />
          <el-option label="困难" :value="3" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row style="margin-bottom: 10px;">
      <el-col :span="12">
        <span>已选择 {{ tempSelectedQuestions.length }} 道题目</span>
      </el-col>
      <el-col :span="12" style="text-align: right;">
        <el-button @click="selectAll" size="mini" type="text">全选当页</el-button>
        <el-button @click="clearAll" size="mini" type="text">清空选择</el-button>
      </el-col>
    </el-row>

    <el-table
      ref="table"
      v-loading="loading"
      :data="questionList"
      @selection-change="handleSelectionChange"
      @select="handleSelect"
      @select-all="handleSelectAll"
      height="400"
    >
      <el-table-column type="selection" width="55" :selectable="checkSelectable"></el-table-column>
      <el-table-column label="题目标题" prop="questionTitle" :show-overflow-tooltip="true" min-width="250">
        <template slot-scope="scope">
          <span>{{ scope.row.questionTitle || scope.row.questionContent }}</span>
        </template>
      </el-table-column>
      <el-table-column label="试题分类" prop="categoryName" width="120" :show-overflow-tooltip="true">
        <template slot-scope="scope">
          <span>{{ scope.row.categoryName || '未分类' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="题目类型" prop="questionType" width="100">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.exam_question_type" :value="scope.row.questionType"/>
        </template>
      </el-table-column>
      <el-table-column label="难度等级" prop="difficultyLevel" width="100">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.exam_difficulty_level" :value="scope.row.difficultyLevel"/>
        </template>
      </el-table-column>
      <el-table-column label="分值设置" width="170" align="center" class-name="question-score-column">
        <template slot-scope="scope">
          <el-input-number
            v-if="isSelected(scope.row)"
            class="question-score-input"
            :value="getQuestionScore(scope.row)"
            :min="1"
            :max="100"
            controls-position="right"
            size="mini"
            @input="updateQuestionScore(scope.row, $event)"
          />
          <span v-else>{{ scope.row.score || 2 }}</span>
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

    <div slot="footer" class="dialog-footer" style="margin-top: 20px; text-align: right;">
      <el-button @click="handleCancel">取消</el-button>
      <el-button type="primary" @click="handleConfirm">确定</el-button>
    </div>
  </div>
</template>

<script>
import { listQuestion } from "@/api/exam/question";
import CategoryTreeSelect from "@/components/CategoryTreeSelect";

export default {
  name: "QuestionSelector",
  dicts: ['exam_question_type', 'exam_difficulty_level'],
  components: {
    CategoryTreeSelect
  },
  props: {
    selectedQuestions: {
      type: Array,
      default: () => []
    }
  },
  data() {
    return {
      // 遮罩层
      loading: true,
      // 总条数
      total: 0,
      // 题目表格数据
      questionList: [],
      // 临时选中的题目
      tempSelectedQuestions: [],
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        questionTitle: null,
        categoryId: null,
        questionType: null,
        difficultyLevel: null
      }
    };
  },
  created() {
    this.tempSelectedQuestions = [...this.selectedQuestions];
    this.getList();
  },
  methods: {

    /** 查询题目列表 */
    getList() {
      this.loading = true;
      listQuestion(this.queryParams).then(response => {
        this.questionList = response.rows;
        this.total = response.total;
        this.loading = false;
        
        // 设置已选中的题目状态
        this.$nextTick(() => {
          this.questionList.forEach(row => {
            const isSelected = this.tempSelectedQuestions.some(q => q.id === row.id);
            this.$refs.table.toggleRowSelection(row, isSelected);
          });
        });
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
    /** 检查是否可选择 */
    checkSelectable(row) {
      return true;
    },
    /** 判断题目是否已选中 */
    isSelected(row) {
      return this.tempSelectedQuestions.some(q => q.id === row.id);
    },
    /** 获取题目分值 */
    getQuestionScore(row) {
      const question = this.tempSelectedQuestions.find(q => q.id === row.id);
      return question ? question.questionScore : row.score || 2;
    },
    /** 更新题目分值 */
    updateQuestionScore(row, score) {
      const question = this.tempSelectedQuestions.find(q => q.id === row.id);
      if (question) {
        question.questionScore = score;
      }
    },
    /** 处理单个选中 */
    handleSelect(selection, row) {
      const isSelected = selection.indexOf(row) !== -1;
      if (isSelected) {
        // 添加到选中列表
        if (!this.tempSelectedQuestions.some(q => q.id === row.id)) {
          this.tempSelectedQuestions.push({
            ...row,
            questionScore: row.score || 2
          });
        }
      } else {
        // 从选中列表移除
        const index = this.tempSelectedQuestions.findIndex(q => q.id === row.id);
        if (index !== -1) {
          this.tempSelectedQuestions.splice(index, 1);
        }
      }
    },
    /** 处理全选 */
    handleSelectAll(selection) {
      if (selection.length > 0) {
        // 全选当页
        this.questionList.forEach(row => {
          if (!this.tempSelectedQuestions.some(q => q.id === row.id)) {
            this.tempSelectedQuestions.push({
              ...row,
              questionScore: row.score || 2
            });
          }
        });
      } else {
        // 取消当页选择
        this.questionList.forEach(row => {
          const index = this.tempSelectedQuestions.findIndex(q => q.id === row.id);
          if (index !== -1) {
            this.tempSelectedQuestions.splice(index, 1);
          }
        });
      }
    },
    /** 处理选择变化 */
    handleSelectionChange(selection) {
      // 这个方法主要用于更新界面显示，具体逻辑在handleSelect中处理
    },
    /** 全选当页 */
    selectAll() {
      this.questionList.forEach(row => {
        this.$refs.table.toggleRowSelection(row, true);
        if (!this.tempSelectedQuestions.some(q => q.id === row.id)) {
          this.tempSelectedQuestions.push({
            ...row,
            questionScore: row.score || 2
          });
        }
      });
    },
    /** 清空选择 */
    clearAll() {
      this.tempSelectedQuestions = [];
      this.$refs.table.clearSelection();
    },
    /** 确认选择 */
    handleConfirm() {
      this.$emit('confirm', this.tempSelectedQuestions);
    },
    /** 取消选择 */
    handleCancel() {
      this.$emit('cancel');
    }
  }
};
</script>

<style scoped>
.dialog-footer {
  border-top: 1px solid #e6e6e6;
  padding-top: 10px;
}

.question-score-input {
  width: 130px;
}
</style>
