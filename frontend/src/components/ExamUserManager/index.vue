<template>
  <div class="exam-user-manager">
    <!-- 工具栏 -->
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="el-icon-plus"
          size="mini"
          @click="handleAdd"
          v-hasPermi="['exam:examination:addUsers']"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="el-icon-office-building"
          size="mini"
          @click="handleAddByDept"
          v-hasPermi="['exam:examination:addUsers']"
        >按部门添加</el-button>
      </el-col>

      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="el-icon-delete"
          size="mini"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['exam:examination:removeUsers']"
        >批量删除</el-button>
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

    <!-- 搜索栏 -->
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="用户名" prop="userName">
        <el-input
          v-model="queryParams.userName"
          placeholder="请输入用户名"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="姓名" prop="nickName">
        <el-input
          v-model="queryParams.nickName"
          placeholder="请输入姓名"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="考试状态" prop="examStatus">
        <el-select v-model="queryParams.examStatus" placeholder="考试状态" clearable>
          <el-option label="未开始" value="not_started" />
          <el-option label="已报名" value="registered" />
          <el-option label="考试中" value="in_progress" />
          <el-option label="已提交" value="submitted" />
          <el-option label="已评分" value="graded" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery" v-hasPermi="['exam:examination:detail']">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery" v-hasPermi="['exam:examination:detail']">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 数据表格 -->
    <el-table v-loading="loading" :data="examUserList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="用户名" prop="userName" min-width="120" />
      <el-table-column label="姓名" prop="nickName" min-width="120" />
      <el-table-column label="部门" prop="deptName" min-width="150" :show-overflow-tooltip="true" />
      <el-table-column label="考试状态" prop="examStatus" width="100">
        <template slot-scope="scope">
          <el-tag v-if="scope.row.examStatus === 'not_started'" type="info">未开始</el-tag>
          <el-tag v-else-if="scope.row.examStatus === 'in_progress'" type="warning">考试中</el-tag>
          <el-tag v-else-if="scope.row.examStatus === 'submitted'" type="success">已提交</el-tag>
          <el-tag v-else-if="scope.row.examStatus === 'graded'" type="primary">已评分</el-tag>
          <el-tag v-else-if="scope.row.examStatus === 'registered'" type="info">已报名</el-tag>
          <span v-else>{{ scope.row.examStatus || '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="报名时间" prop="registrationTime" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.registrationTime, '{y}-{m}-{d} {h}:{i}:{s}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="开始时间" prop="startTime" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.startTime, '{y}-{m}-{d} {h}:{i}:{s}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="考试分数" prop="totalScore" width="80">
        <template slot-scope="scope">
          <span>{{ scope.row.totalScore || '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="是否通过" prop="isPassed" width="100">
        <template slot-scope="scope">
          <el-tag v-if="scope.row.isPassed === '1'" type="success">通过</el-tag>
          <el-tag v-else-if="scope.row.isPassed === '0'" type="danger">未通过</el-tag>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="200" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-refresh"
            @click="handleResetUser(scope.row)"
            v-hasPermi="['exam:examination:reset']"
          >重置考试</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-view"
            @click="handleViewAnswer(scope.row)"
            v-hasPermi="['exam:examination:viewAnswer']"
            v-if="scope.row.examStatus === 'in_progress' || scope.row.examStatus === 'submitted' || scope.row.examStatus === 'graded'"
          >查看答题</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['exam:examination:removeUsers']"
          >删除</el-button>
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

    <!-- 用户选择器组件 -->
    <user-select 
      ref="userSelect"
      v-model="selectedUserIds" 
      :multiple="true" 
      :excludeUserIds="existingUserIds"
      :hideDisplay="true"
      @confirm="handleUserSelectConfirm"
      @cancel="handleUserSelectCancel"
    />

    <!-- 部门选择弹窗 -->
    <el-dialog title="按部门添加参考人员" :visible.sync="deptDialogVisible" width="560px" append-to-body>
      <el-form size="small" label-width="96px">
        <el-form-item label="部门范围">
          <div class="dept-add-tree" v-loading="deptLoading">
            <el-tree
              ref="deptTree"
              :data="deptOptions"
              :props="deptTreeProps"
              node-key="id"
              show-checkbox
              default-expand-all
              :expand-on-click-node="false"
            />
          </div>
        </el-form-item>
        <el-form-item label="子部门">
          <el-switch
            v-model="includeChildren"
            active-text="包含子部门"
            inactive-text="仅所选部门"
          />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" :loading="deptSubmitLoading" @click="handleDeptConfirm">确 定</el-button>
        <el-button @click="deptDialogVisible = false">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import {
  getExaminationDetails,
  addExamUsers,
  addExamUsersByDept,
  addExamUsersByDeptWithChildren,
  removeExamUsers,
  listExamUsers,
  resetUserExam
} from "@/api/exam/examination";
import { deptTreeSelect } from "@/api/system/user";
import UserSelect from "@/components/UserSelect";
import RightToolbar from "@/components/RightToolbar";
import { checkPermi } from "@/utils/permission";

export default {
  name: "ExamUserManager",
  components: {
    UserSelect,
    RightToolbar
  },
  props: {
    examId: {
      type: Number,
      required: true
    }
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
      // 考试用户表格数据
      examUserList: [],
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        userName: null,
        nickName: null,
        examStatus: null
      },
      // 用户选择器
      selectedUserIds: [],
      // 已有参考人员的用户ID列表
      existingUserIds: [],
      // 部门添加弹窗
      deptDialogVisible: false,
      deptLoading: false,
      deptSubmitLoading: false,
      includeChildren: true,
      deptOptions: [],
      deptTreeProps: {
        children: 'children',
        label: 'label'
      }
    };
  },
  watch: {
    examId: {
      handler(newVal) {
        if (newVal) {
          this.getList();
        }
      },
      immediate: true
    }
  },
  methods: {
    /** 查询考试用户列表 */
    getList() {
      if (!this.examId) return;
      
      // 检查是否有查看考试用户的权限
      if (!checkPermi(['exam:examination:detail'])) {
        this.$message.error('您没有权限查看考试人员信息');
        return;
      }
      
      this.loading = true;
      // 使用新的分页查询API
      listExamUsers(this.examId, this.queryParams).then(response => {
        this.examUserList = response.rows;
        this.total = response.total;
        this.loading = false;
        
        // 获取已有参考人员的用户ID列表
        this.updateExistingUserIds();
      }).catch(error => {
        console.error('获取考试用户列表失败:', error);
        this.loading = false;
      });
    },
    
    /** 更新已有参考人员ID列表 */
    updateExistingUserIds() {
      // 获取完整的用户列表（不分页）来更新已有用户ID
      getExaminationDetails(this.examId).then(response => {
        const allUsers = response.data.examUsers || [];
        this.existingUserIds = allUsers.map(user => user.userId);
      }).catch(error => {
        console.error('获取完整用户列表失败:', error);
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
    
    /** 多选框选中数据 */
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.userId);
      this.single = selection.length !== 1;
      this.multiple = !selection.length;
    },
    
    /** 新增按钮操作 */
    handleAdd() {
      this.selectedUserIds = [];
      // 直接打开用户选择器
      this.$refs.userSelect.handleOpenDialog();
    },

    /** 按部门添加按钮操作 */
    handleAddByDept() {
      this.deptDialogVisible = true;
      this.includeChildren = true;
      this.loadDeptTree();
    },

    /** 加载部门树 */
    loadDeptTree() {
      this.deptLoading = true;
      deptTreeSelect().then(response => {
        this.deptOptions = response.data || [];
      }).catch(error => {
        console.error('获取部门树失败:', error);
        this.$modal.msgError("获取部门树失败");
      }).finally(() => {
        this.deptLoading = false;
      });
    },

    /** 确认按部门添加 */
    handleDeptConfirm() {
      const deptIds = this.$refs.deptTree ? this.$refs.deptTree.getCheckedKeys() : [];
      if (!deptIds || deptIds.length === 0) {
        this.$modal.msgError("请选择要添加的部门");
        return;
      }

      this.deptSubmitLoading = true;
      const request = this.includeChildren ? addExamUsersByDeptWithChildren : addExamUsersByDept;
      request(this.examId, deptIds).then(response => {
        this.$modal.msgSuccess(response.msg || "按部门添加参考人员成功");
        this.deptDialogVisible = false;
        this.getList();
        this.updateExistingUserIds();
      }).catch(error => {
        this.$modal.msgError("按部门添加失败：" + (error.message || "未知错误"));
      }).finally(() => {
        this.deptSubmitLoading = false;
      });
    },
    
    /** 重置考试 */
    handleResetUser(row) {
      const userName = row.nickName || row.userName;
      this.$modal.confirm(`确认重置用户"${userName}"的本次考试记录？\n\n重置后仅删除该轮次的答题记录，并保留该用户其他历史考试轮次。`, '警告', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
        customClass: 'reset-confirm-dialog'
      }).then(() => {
        this.loading = true;
        resetUserExam(row.id).then(response => {
          this.$modal.msgSuccess(response.msg || "重置本次考试记录成功，用户可以重新开始该轮次考试");
          this.getList();
        }).catch(error => {
          this.$modal.msgError(error.response?.data?.msg || "重置考试状态失败");
        }).finally(() => {
          this.loading = false;
        });
      }).catch(() => {});
    },
    
    /** 查看答题 */
    handleViewAnswer(row) {
      // 跳转到成绩查看页面
      this.$router.push({ 
        path: '/exam/examination/result', 
        query: { 
          examId: this.examId,
          userId: row.userId,
          userName: row.userName,
          nickName: row.nickName
        } 
      });
    },
    
    /** 删除按钮操作 */
    handleDelete(row) {
      const userIds = row.userId || this.ids;
      this.$modal.confirm('是否确认删除选中的参考人员？').then(() => {
        return removeExamUsers(this.examId, [].concat(userIds));
      }).then(() => {
        this.getList();
        this.updateExistingUserIds(); // 更新已有用户ID列表
        this.$modal.msgSuccess("删除成功");
      }).catch(() => {});
    },
    
    /** 导出按钮操作 */
    handleExport() {
      this.download(`exam/examination/exportUsers/${this.examId}`, {}, `exam_users_${this.examId}_${new Date().getTime()}.xlsx`);
    },
    

    
    /** 用户选择器确认回调 */
    handleUserSelectConfirm(selectedUsers) {
      if (!selectedUsers || selectedUsers.length === 0) {
        this.$modal.msgError("请选择要添加的用户");
        return;
      }
      
      const userIds = selectedUsers.map(user => user.userId);
      addExamUsers(this.examId, userIds).then(response => {
        this.$modal.msgSuccess(`成功添加 ${selectedUsers.length} 名参考人员`);
        this.getList();
        this.updateExistingUserIds(); // 更新已有用户ID列表
      }).catch(error => {
        this.$modal.msgError("添加失败：" + (error.message || "未知错误"));
      });
    },
    
    /** 用户选择器取消回调 */
    handleUserSelectCancel() {
      // 取消时不需要特殊处理，用户选择器会自动关闭
    }
  }
};
</script>

<style scoped>
.exam-user-manager {
  background: #fff;
  padding: 20px;
  border-radius: 6px;
}

.dept-add-tree {
  max-height: 360px;
  min-height: 220px;
  overflow: auto;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  padding: 8px;
}
</style>
