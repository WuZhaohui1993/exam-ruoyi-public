<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="公告标题" prop="noticeTitle">
        <el-input
          v-model="queryParams.noticeTitle"
          placeholder="请输入公告标题"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="操作人员" prop="createBy">
        <el-input
          v-model="queryParams.createBy"
          placeholder="请输入操作人员"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="类型" prop="noticeType">
        <el-select v-model="queryParams.noticeType" placeholder="公告类型" clearable>
          <el-option
            v-for="dict in dict.type.sys_notice_type"
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
          v-hasPermi="['system:notice:add']"
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
          v-hasPermi="['system:notice:edit']"
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
          v-hasPermi="['system:notice:remove']"
        >删除</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="noticeList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="序号" align="center" prop="noticeId" width="100" />
      <el-table-column
        label="公告标题"
        align="center"
        prop="noticeTitle"
        :show-overflow-tooltip="true"
      />
      <el-table-column label="公告类型" align="center" prop="noticeType" width="100">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.sys_notice_type" :value="scope.row.noticeType"/>
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" prop="status" width="100">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.sys_notice_status" :value="scope.row.status"/>
        </template>
      </el-table-column>
      <el-table-column label="创建者" align="center" prop="createBy" width="100" />
      <el-table-column label="创建时间" align="center" prop="createTime" width="100">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-view"
            @click="handleView(scope.row)"
          >查看</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['system:notice:edit']"
          >修改</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['system:notice:remove']"
          >删除</el-button>
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

    <!-- 添加或修改公告对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="780px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
        <el-row>
          <el-col :span="12">
            <el-form-item label="公告标题" prop="noticeTitle">
              <el-input v-model="form.noticeTitle" placeholder="请输入公告标题" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="公告类型" prop="noticeType">
              <el-select v-model="form.noticeType" placeholder="请选择公告类型">
                <el-option
                  v-for="dict in dict.type.sys_notice_type"
                  :key="dict.value"
                  :label="dict.label"
                  :value="dict.value"
                ></el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="状态">
              <el-radio-group v-model="form.status">
                <el-radio
                  v-for="dict in dict.type.sys_notice_status"
                  :key="dict.value"
                  :label="dict.value"
                >{{dict.label}}</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="内容">
              <editor v-model="form.noticeContent" :min-height="192"/>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="接收范围">
              <el-tabs v-model="activeTab">
                <el-tab-pane label="部门" name="dept">
                  <div v-if="!deptOptions || deptOptions.length === 0" style="text-align: center; color: #999; padding: 20px;">
                    <el-button @click="getDeptTree" size="mini">重新加载部门数据</el-button>
                    <div>暂无部门数据</div>
                  </div>
                  <treeselect
                    v-else
                    ref="deptTreeSelect"
                    v-model="selectedDepts"
                    :options="deptOptions"
                    :normalizer="normalizer"
                    :show-count="true"
                    :clearable="true"
                    :searchable="true"
                    placeholder="请选择部门（支持多选）"
                    multiple
                    :disable-branch-nodes="false"
                    :flat="false"
                    :always-open="false"
                    :auto-focus="false"
                    :close-on-select="false"
                    :limit="10"
                    :limit-text="count => `还有 ${count} 个部门未显示...`"
                    no-options-text="无部门数据"
                    no-results-text="无匹配结果"
                    :z-index="9999"
                    :append-to-body="true"
                    :max-height="300"
                  />
                  <div v-if="selectedDepts && selectedDepts.length > 0" style="margin-top: 10px;">
                    <span style="font-size: 12px; color: #666;">
                      已选择 {{ selectedDepts.length }} 个部门：
                    </span>
                    <el-tag 
                      v-for="deptId in selectedDepts" 
                      :key="deptId" 
                      size="small" 
                      style="margin-left: 5px;"
                    >
                      {{ getDeptNameById(deptId) }}
                    </el-tag>
                  </div>
                </el-tab-pane>
                <el-tab-pane label="角色" name="role">
                  <el-select v-model="selectedRoles" multiple placeholder="请选择角色" style="width: 100%">
                    <el-option
                      v-for="item in roleOptions"
                      :key="item.roleId"
                      :label="item.roleName"
                      :value="item.roleId"
                    >
                    </el-option>
                  </el-select>
                </el-tab-pane>
                <el-tab-pane label="用户" name="user">
                  <user-select ref="userSelect" v-model="selectedUsers" />
                </el-tab-pane>
              </el-tabs>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>

    <!-- 查看公告详情对话框 -->
    <el-dialog :title="viewTitle" :visible.sync="viewOpen" width="800px" append-to-body>
      <div class="notice-detail">
        <div class="notice-info">
          <el-descriptions :column="2" border>
            <el-descriptions-item label="公告标题">{{ viewForm.noticeTitle }}</el-descriptions-item>
            <el-descriptions-item label="公告类型">
              <dict-tag :options="dict.type.sys_notice_type" :value="viewForm.noticeType"/>
            </el-descriptions-item>
            <el-descriptions-item label="状态">
              <dict-tag :options="dict.type.sys_notice_status" :value="viewForm.status"/>
            </el-descriptions-item>
            <el-descriptions-item label="创建者">{{ viewForm.createBy }}</el-descriptions-item>
            <el-descriptions-item label="创建时间" :span="2">{{ viewForm.createTime }}</el-descriptions-item>
          </el-descriptions>
        </div>
        <div class="notice-content" style="margin-top: 20px;">
          <h4>公告内容：</h4>
          <div class="notice-content-body" v-html="sanitizeContent(viewForm.noticeContent)" style="padding: 15px; background-color: #f5f7fa; border-radius: 4px; margin-top: 10px; min-height: 200px;"></div>
        </div>
        <div class="notice-scope" style="margin-top: 20px;" v-if="viewForm.scopeList && viewForm.scopeList.length > 0">
          <h4>接收范围：</h4>
          <div style="padding: 15px; background-color: #f5f7fa; border-radius: 4px; margin-top: 10px;">
            <el-tag v-for="scope in viewScopeText" :key="scope" style="margin-right: 10px; margin-bottom: 5px;">
              {{ scope }}
            </el-tag>
          </div>
        </div>
      </div>
      <div slot="footer" class="dialog-footer">
        <el-button @click="viewOpen = false">关闭</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listNotice, getNotice, delNotice, addNotice, updateNotice } from "@/api/system/notice"
import { deptTreeSelect, getUser } from "@/api/system/user"
import { listRole } from "@/api/system/role"
import Editor from "@/components/Editor"
import Treeselect from "@riophae/vue-treeselect"
import "@riophae/vue-treeselect/dist/vue-treeselect.css"
import UserSelect from "@/components/UserSelect"
import { sanitizeRichText } from "@/utils/sanitize"

export default {
  name: "Notice",
  components: {
    Editor,
    Treeselect,
    UserSelect
  },
  dicts: ['sys_notice_status', 'sys_notice_type'],
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
      // 公告表格数据
      noticeList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 查看详情对话框
      viewOpen: false,
      viewTitle: "",
      viewForm: {},
      viewScopeText: [],
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        noticeTitle: undefined,
        createBy: undefined,
        status: undefined
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        noticeTitle: [
          { required: true, message: "公告标题不能为空", trigger: "blur" },
          { min: 2, max: 50, message: '标题长度必须介于 2 和 50 之间', trigger: 'blur' }
        ],
        noticeType: [
          { required: true, message: "公告类型不能为空", trigger: "change" }
        ]
      },
      // 接收范围相关
      activeTab: "dept",
      deptOptions: [],
      roleOptions: [],
      selectedDepts: [],
      selectedRoles: [],
      selectedUsers: [],
    }
  },
  created() {
    this.getList()
    this.getRoleOptions()
  },
  mounted() {
    // 在组件挂载后加载部门树数据
    this.$nextTick(() => {
      this.getDeptTree()
    })
  },
  methods: {
    sanitizeContent(content) {
      return sanitizeRichText(content)
    },
    /** 查询公告列表 */
    getList() {
      this.loading = true
      listNotice(this.queryParams).then(response => {
        this.noticeList = response.rows
        this.total = response.total
        this.loading = false
      })
    },
    /** 查询部门下拉树结构 */
    getDeptTree() {
      deptTreeSelect().then(response => {
        this.deptOptions = response.data
        
        if (!this.deptOptions || this.deptOptions.length === 0) {
          this.$modal.msgWarning("获取部门树数据为空，请检查数据权限")
        }
      }).catch(error => {
        console.error('获取部门树失败:', error)
        this.$modal.msgError("获取部门树失败: " + (error.message || '未知错误'))
      })
    },
    /** 查询角色列表 */
    getRoleOptions() {
      listRole().then(response => {
        this.roleOptions = response.rows
      })
    },
    /** 转换部门数据结构 */
    normalizer(node) {
      // 确保每次调用返回一致的结构
      const normalized = {
        id: node.id || node.deptId,
        label: node.label || node.deptName,
      }
      
      // 只有存在且非空的children才添加
      if (node.children && Array.isArray(node.children) && node.children.length > 0) {
        normalized.children = node.children
      }
      
      return normalized
    },
    
    /** 根据部门ID获取部门名称 */
    getDeptNameById(deptId) {
      const dept = this.findDeptById(deptId)
      return dept ? dept.label : `部门ID-${deptId}`
    },
    // 取消按钮
    cancel() {
      this.open = false
      this.reset()
    },
    // 表单重置
    reset() {
      this.form = {
        noticeId: undefined,
        noticeTitle: undefined,
        noticeType: "1",
        noticeContent: undefined,
        status: "0"
      }
      this.selectedDepts = []
      this.selectedRoles = []
      this.selectedUsers = []
      this.resetForm("form")
    },
    /** 搜索按钮操作 */
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    /** 重置按钮操作 */
    resetQuery() {
      this.resetForm("queryForm")
      this.handleQuery()
    },
    // 多选框选中数据
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.noticeId)
      this.single = selection.length!=1
      this.multiple = !selection.length
    },
    /** 新增按钮操作 */
    handleAdd() {
      this.reset()
      this.open = true
      this.title = "添加公告"
    },
    /** 查看详情按钮操作 */
    handleView(row) {
      const noticeId = row.noticeId
      getNotice(noticeId).then(response => {
        this.viewForm = response.data
        this.viewTitle = "查看公告详情"

        // 构建接收范围文本
        this.viewScopeText = []
        if (response.data.scopeList && response.data.scopeList.length > 0) {
          // 使用 Promise 数组来并行获取所有用户信息
          const userPromises = []
          const tempScopeTexts = []

          response.data.scopeList.forEach((scope, index) => {
            if (scope.scopeType === "1") {
              // 部门
              const dept = this.findDeptById(scope.targetId)
              if (dept) {
                tempScopeTexts[index] = `部门：${dept.label}`
              }
            } else if (scope.scopeType === "2") {
              // 角色
              const role = this.roleOptions.find(r => r.roleId === scope.targetId)
              if (role) {
                tempScopeTexts[index] = `角色：${role.roleName}`
              }
            } else if (scope.scopeType === "3") {
              // 用户 - 通过API获取用户信息
              const userPromise = getUser(scope.targetId).then(userResponse => {
                tempScopeTexts[index] = `用户：${userResponse.data.userName}`
              }).catch(() => {
                tempScopeTexts[index] = `用户：用户ID-${scope.targetId}`
              })
              userPromises.push(userPromise)
            }
          })

          // 等待所有用户信息获取完成
          Promise.all(userPromises).then(() => {
            this.viewScopeText = tempScopeTexts.filter(text => text)
          }).catch(() => {
            // 如果获取用户信息失败，使用已有的范围文本
            this.viewScopeText = tempScopeTexts.filter(text => text)
          })

          // 如果没有用户范围，直接设置范围文本
          if (userPromises.length === 0) {
            this.viewScopeText = tempScopeTexts.filter(text => text)
          }
        }

        this.viewOpen = true
      })
    },
    /** 根据部门ID查找部门信息 */
    findDeptById(deptId) {
      const findInTree = (nodes, id) => {
        for (let node of nodes) {
          if (node.id === id) {
            return node
          }
          if (node.children && node.children.length > 0) {
            const found = findInTree(node.children, id)
            if (found) return found
          }
        }
        return null
      }
      return findInTree(this.deptOptions, deptId)
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset()
      const noticeId = row.noticeId || this.ids
      getNotice(noticeId).then(response => {
        this.form = response.data
        // 设置接收范围
        if (response.data.scopeList) {
          response.data.scopeList.forEach(scope => {
            if (scope.scopeType === "1") {
              this.selectedDepts.push(scope.targetId)
            } else if (scope.scopeType === "2") {
              this.selectedRoles.push(scope.targetId)
            } else if (scope.scopeType === "3") {
              this.selectedUsers.push(scope.targetId)
            }
          })
        }
        this.open = true
        this.title = "修改公告"

        // 确保用户选择器更新
        this.$nextTick(() => {
          // 触发用户选择器的值变化，确保回显正确
          if (this.$refs.userSelect) {
            this.$refs.userSelect.updateSelectedUserList()
          }
        })
      })
    },
    /** 提交按钮 */
    submitForm: function() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          // 构建接收范围列表
          const scopeList = []
          this.selectedDepts.forEach(deptId => {
            scopeList.push({
              scopeType: "1",
              targetId: deptId
            })
          })
          this.selectedRoles.forEach(roleId => {
            scopeList.push({
              scopeType: "2",
              targetId: roleId
            })
          })
          this.selectedUsers.forEach(userId => {
            scopeList.push({
              scopeType: "3",
              targetId: userId
            })
          })
          this.form.scopeList = scopeList

          if (this.form.noticeId != null) {
            updateNotice(this.form).then(response => {
              this.$modal.msgSuccess("修改成功")
              this.open = false
              this.getList()
            })
          } else {
            addNotice(this.form).then(response => {
              this.$modal.msgSuccess("新增成功")
              this.open = false
              this.getList()
            })
          }
        }
      })
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      const noticeIds = row.noticeId || this.ids
      this.$modal.confirm('是否确认删除公告编号为"' + noticeIds + '"的数据项？').then(function() {
        return delNotice(noticeIds)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess("删除成功")
      }).catch(() => {})
    }
  }
}
</script>

<style scoped>
.el-tabs {
  margin-bottom: 20px;
}

/* 公告详情内容样式优化 */
.notice-detail {
  max-width: 100%;
  overflow: hidden;
}

.notice-content {
  .notice-content-body {
    word-wrap: break-word;
    overflow-wrap: break-word;
    max-width: 100%;
    overflow: hidden;
    
    /* 强制所有图片自适应容器宽度 */
    ::v-deep img {
      max-width: 100% !important;
      width: auto !important;
      height: auto !important;
      border-radius: 6px;
      margin: 10px 0;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
      display: block !important;
      object-fit: contain !important;
    }
    
    /* 确保图片在任何容器中都不会溢出 */
    ::v-deep .ql-editor img,
    ::v-deep .editor img,
    ::v-deep div img,
    ::v-deep p img {
      max-width: 100% !important;
      width: auto !important;
      height: auto !important;
    }
  }
  
  /* 确保所有内容都不会溢出 */
  ::v-deep * {
    max-width: 100% !important;
    box-sizing: border-box;
  }
  
  /* 强制表格不溢出 */
  ::v-deep table {
    max-width: 100% !important;
    table-layout: fixed !important;
    width: 100% !important;
  }
  
  /* 强制其他可能溢出的元素 */
  ::v-deep pre,
  ::v-deep code {
    max-width: 100% !important;
    overflow-x: auto !important;
    word-wrap: break-word !important;
  }
  
  ::v-deep p {
    margin-bottom: 12px;
    line-height: 1.6;
  }
  
  ::v-deep ul, ::v-deep ol {
    padding-left: 20px;
    margin-bottom: 12px;
  }
  
  ::v-deep blockquote {
    border-left: 4px solid #409EFF;
    padding-left: 15px;
    margin: 15px 0;
    color: #606266;
    background-color: #f8f9fa;
    padding: 10px 15px;
    border-radius: 4px;
  }
}

/* 部门选择器样式优化 */
.vue-treeselect {
  z-index: 9999 !important;
  min-height: 35px;
}

.vue-treeselect__control {
  min-height: 35px;
}

/* 确保下拉框在所有容器之上 */
::v-deep .vue-treeselect__menu {
  z-index: 9999 !important;
  max-height: 300px !important;
  overflow-y: auto !important;
}

/* 修复下拉框在对话框中的显示问题 */
::v-deep .el-dialog .vue-treeselect__menu {
  z-index: 2002 !important;
}

/* 确保下拉框容器不被裁剪 */
::v-deep .vue-treeselect__menu-container {
  z-index: 9999 !important;
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
    border-radius: 6px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  }
  
  /* 富文本编辑器输出的HTML内容优化 */
  .notice-content-body {
    img {
      max-width: 100% !important;
      width: auto !important;
      height: auto !important;
    }
  }
}
</style>
