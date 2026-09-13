<template>
  <div>
    <!-- 用户选择显示区域 -->
    <div v-if="!hideDisplay" class="user-select-display" @click="handleOpenDialog">
      <el-tag
        v-for="user in selectedUserList"
        :key="user.userId"
        closable
        @close="handleRemoveUser(user.userId)"
        style="margin-right: 8px; margin-bottom: 4px;"
      >
        {{ user.nickName || user.userName }}
      </el-tag>
      <el-button v-if="selectedUserList.length === 0" type="text" icon="el-icon-plus">
        点击选择用户
      </el-button>
      <el-button v-else type="text" icon="el-icon-plus" style="margin-left: 8px;">
        添加更多用户
      </el-button>
    </div>

    <!-- 用户选择对话框 -->
    <el-dialog title="选择用户" :visible.sync="dialogVisible" width="1000px" append-to-body>
      <div style="display: flex; height: 500px; min-height: 500px;">
        <!-- 左侧部门树 -->
        <div style="width: 250px; border-right: 1px solid #ebeef5; padding-right: 15px; margin-right: 15px; display: flex; flex-direction: column;">
          <div style="margin-bottom: 10px; flex-shrink: 0;">
            <div style="margin-bottom: 8px;">
              <span style="font-weight: bold;">部门列表</span>
            </div>
            <div style="display: flex; gap: 8px;">
              <el-button 
                size="mini" 
                type="primary"
                @click="handleSelectDeptUsers"
                :disabled="!currentSelectedDept"
              >
                选择部门全员
              </el-button>
              <el-button 
                size="mini" 
                type="warning"
                @click="handleSelectSubDeptUsers"
                :disabled="!currentSelectedDept || !currentSelectedDept.children || currentSelectedDept.children.length === 0"
              >
                含子部门全员
              </el-button>
            </div>
          </div>
          
          <div style="flex: 1; min-height: 0; overflow: auto;">
            <el-tree
              ref="deptTree"
              :data="deptTreeData"
              :props="deptTreeProps"
              node-key="id"
              :highlight-current="true"
              :expand-on-click-node="false"
              default-expand-all
              @node-click="handleDeptNodeClick"
            >
              <span class="custom-tree-node" slot-scope="{ node, data }">
                <span>{{ node.label }}</span>
                <span style="color: #909399; font-size: 12px;">({{ data.userCount || 0 }})</span>
              </span>
            </el-tree>
          </div>
        </div>

        <!-- 右侧用户列表 -->
        <div style="flex: 1; min-width: 0; display: flex; flex-direction: column;">
          <!-- 搜索表单 -->
          <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" style="margin-bottom: 15px; flex-shrink: 0;">
            <el-form-item label="用户名称" prop="userName">
              <el-input
                v-model="queryParams.userName"
                placeholder="请输入用户名称"
                clearable
                style="width: 160px;"
                @keyup.enter.native="handleQuery"
              />
            </el-form-item>
            <el-form-item label="手机号码" prop="phonenumber">
              <el-input
                v-model="queryParams.phonenumber"
                placeholder="请输入手机号码"
                clearable
                style="width: 160px;"
                @keyup.enter.native="handleQuery"
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
              <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
            </el-form-item>
          </el-form>
          
          <!-- 当前选择的部门信息 -->
          <div v-if="currentSelectedDept" style="margin-bottom: 10px; padding: 10px; background: #f5f7fa; border-radius: 4px; flex-shrink: 0;">
            <span style="color: #606266; font-size: 14px;">
              当前部门：<strong>{{ currentSelectedDept.label }}</strong> 
              (共 {{ total }} 人)
            </span>
          </div>
          
          <!-- 排除用户提示 -->
          <div v-if="excludeUserIds && excludeUserIds.length > 0" style="margin-bottom: 10px; padding: 8px; background: #fdf6ec; border: 1px solid #faecd8; border-radius: 4px; flex-shrink: 0;">
            <span style="color: #e6a23c; font-size: 12px;">
              <i class="el-icon-info"></i>
              灰色显示的用户已是参考人员，无法重复选择
            </span>
          </div>

          <!-- 用户表格 -->
          <div style="flex: 1; min-height: 0; position: relative;">
            <!-- 加载状态覆盖层 -->
            <transition name="fade">
              <div v-if="isLoadingData" 
                   style="position: absolute; top: 0; left: 0; right: 0; bottom: 0; 
                          background: rgba(255,255,255,0.9); display: flex; 
                          align-items: center; justify-content: center; z-index: 10;
                          border-radius: 4px;">
                <div style="text-align: center;">
                  <i class="el-icon-loading" style="font-size: 24px; color: #409EFF; animation: rotate 2s linear infinite;"></i>
                  <div style="margin-top: 8px; color: #666; font-size: 14px;">正在加载用户数据...</div>
                </div>
              </div>
            </transition>
            
            <el-table 
              @row-click="clickRow" 
              ref="table" 
              :data="userList" 
              @selection-change="handleSelectionChange" 
              row-key="userId"
              height="350px"
              size="small"
              style="width: 100%;"
              :row-class-name="getRowClassName"
            >
              <el-table-column type="selection" width="45" :selectable="checkSelectable"></el-table-column>
              <el-table-column label="用户名称" prop="userName" :show-overflow-tooltip="true" min-width="90" />
              <el-table-column label="用户昵称" prop="nickName" :show-overflow-tooltip="true" min-width="90" />
              <el-table-column label="部门" prop="dept.deptName" :show-overflow-tooltip="true" min-width="110" />
              <el-table-column label="手机" prop="phonenumber" :show-overflow-tooltip="true" min-width="110" />
              <el-table-column label="状态" align="center" prop="status" width="60">
                <template slot-scope="scope">
                  <el-tag :type="scope.row.status === '0' ? 'success' : 'danger'" size="mini">
                    {{ scope.row.status === '0' ? '正常' : '停用' }}
                  </el-tag>
                </template>
              </el-table-column>
            </el-table>
          </div>
          
          <!-- 分页 -->
          <pagination
            v-show="total > 0"
            :total="total"
            :page.sync="queryParams.pageNum"
            :limit.sync="queryParams.pageSize"
            @pagination="handlePagination"
            small
            style="margin-top: 10px; flex-shrink: 0;"
          />
        </div>
      </div>
      
      <div slot="footer" class="dialog-footer">
        <div style="float: left; color: #909399; font-size: 14px; line-height: 32px;">
          已选择 {{ tempSelectedUserIds.length }} 人
        </div>
        <el-button type="primary" @click="handleConfirm">确 定</el-button>
        <el-button @click="handleCancel">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listUser } from "@/api/system/user"
import { listDept } from "@/api/system/dept"
import Pagination from "@/components/Pagination"

export default {
  name: "UserSelect",
  components: {
    Pagination
  },
  props: {
    value: {
      type: Array,
      default: () => []
    },
    multiple: {
      type: Boolean,
      default: true
    },
    // 需要排除的用户ID列表（已有参考人员）
    excludeUserIds: {
      type: Array,
      default: () => []
    },
    // 是否隐藏用户选择显示区域
    hideDisplay: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      dialogVisible: false,
      selectedUserIds: [],
      selectedUserList: [],
      tempSelectedUserIds: [],
      currentSelectedDept: null,
      isLoadingData: false, // 数据加载标志位，防止在加载期间处理选择变化事件
      // 部门树数据
      deptTreeData: [],
      deptTreeProps: {
        children: 'children',
        label: 'label'
      },
      // 用户列表数据
      userList: [],
      total: 0,
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        userName: undefined,
        phonenumber: undefined,
        status: '0', // 只查询正常状态的用户
        deptId: undefined // 部门ID
      }
    }
  },
  watch: {
    value: {
      handler(newVal) {
        this.selectedUserIds = newVal || []
        this.updateSelectedUserList()
        
        // 如果对话框是打开状态，需要同步临时选择列表
        if (this.dialogVisible) {
          this.tempSelectedUserIds = [...this.selectedUserIds.map(id => Number(id))]
          this.$nextTick(() => {
            this.updateTableSelection()
          })
        }
      },
      immediate: true,
      deep: true
    }
  },
  created() {
    this.updateSelectedUserList()
  },
  computed: {
    // 计算哪些用户应该被选中
    shouldSelectedUserIds() {
      return this.tempSelectedUserIds.map(id => Number(id))
    }
  },
  methods: {
    // 检查行是否可选
    checkSelectable(row) {
      // 如果用户在排除列表中，则不可选
      if (this.excludeUserIds && this.excludeUserIds.length > 0) {
        return !this.excludeUserIds.some(id => Number(id) === Number(row.userId))
      }
      return true
    },
    
    // 获取行的class名称
    getRowClassName({row, rowIndex}) {
      // 如果用户在排除列表中，添加特殊样式
      if (this.excludeUserIds && this.excludeUserIds.length > 0) {
        const isExcluded = this.excludeUserIds.some(id => Number(id) === Number(row.userId))
        if (isExcluded) {
          return 'excluded-user-row'
        }
      }
      return ''
    },
    // 打开选择对话框
    handleOpenDialog() {
      // 确保用户ID都是数字类型，并设置临时选择列表
      this.tempSelectedUserIds = [...this.selectedUserIds.map(id => Number(id))]
      this.dialogVisible = true
      this.loadDeptTree()
      
      // 重置查询参数并加载用户列表
      this.queryParams.pageNum = 1
      this.queryParams.deptId = undefined
      this.currentSelectedDept = null
      this.getList()
    },
    
    // 加载部门树
    loadDeptTree() {
      listDept().then(response => {
        this.deptTreeData = this.handleTree(response.data, "deptId", "parentId")
        // 延迟统计用户数量，确保树结构已完全构建
        this.$nextTick(() => {
          this.countDeptUsers(this.deptTreeData)
        })
      }).catch(error => {
        console.error('获取部门数据失败:', error)
        this.$message.error('获取部门数据失败')
      })
    },

    // 统计各部门用户数量
    countDeptUsers(deptList) {
      if (!deptList || deptList.length === 0) return
      
      deptList.forEach(dept => {
        // 为每个部门统计用户数量
        listUser({ 
          deptId: dept.deptId, 
          pageNum: 1, 
          pageSize: 1,
          status: '0' // 只统计正常状态的用户
        }).then(response => {
          // 直接更新部门的用户数量
          this.$set(dept, 'userCount', response.total || 0)
        }).catch(error => {
          // 失败时设置为0，避免显示错误
          this.$set(dept, 'userCount', 0)
        })
        
        // 递归统计子部门
        if (dept.children && dept.children.length > 0) {
          this.countDeptUsers(dept.children)
        }
      })
    },

    // 构建树形结构
    handleTree(data, id, parentId, children) {
      let config = {
        id: id || 'id',
        parentId: parentId || 'parentId', 
        childrenList: children || 'children'
      }

      var childrenListMap = {}
      var nodeIds = {}
      var tree = []

      for (let d of data) {
        let parentId = d[config.parentId]
        if (childrenListMap[parentId] == null) {
          childrenListMap[parentId] = []
        }
        nodeIds[d[config.id]] = d
        childrenListMap[parentId].push(d)
      }

      for (let d of data) {
        let parentId = d[config.parentId]
        if (nodeIds[parentId] == null) {
          tree.push(d)
        }
      }

      for (let t of tree) {
        adaptToChildrenList(t)
      }

      function adaptToChildrenList(o) {
        if (childrenListMap[o[config.id]] !== null) {
          o[config.childrenList] = childrenListMap[o[config.id]]
        }
        if (o[config.childrenList]) {
          for (let c of o[config.childrenList]) {
            adaptToChildrenList(c)
          }
        }
      }
      
      // 为每个节点添加label属性和id映射
      function addLabel(nodes) {
        for (let node of nodes) {
          node.label = node.deptName
          node.id = node.deptId  // 确保id字段存在
          node.userCount = 0     // 初始化用户数量
          if (node.children && node.children.length > 0) {
            addLabel(node.children)
          }
        }
      }
      addLabel(tree)
      
      return tree
    },

    // 部门节点点击
    handleDeptNodeClick(data) {
      this.currentSelectedDept = data
      this.queryParams.deptId = data.deptId
      this.queryParams.pageNum = 1
      this.queryParams.userName = undefined
      this.queryParams.phonenumber = undefined
      
      // 立即显示加载状态，提升用户体验
      this.isLoadingData = true
      this.getList()
    },

    // 选择部门全员
    handleSelectDeptUsers() {
      if (!this.currentSelectedDept) return
      
      // 获取当前部门的所有用户
      listUser({ 
        deptId: this.currentSelectedDept.deptId, 
        status: '0',
        pageNum: 1,
        pageSize: 10000 // 获取所有用户
      }).then(response => {
        const deptUserIds = response.rows.map(user => Number(user.userId))
        
        // 合并到临时选择列表中，确保数据类型一致
        const currentSelected = this.tempSelectedUserIds.map(id => Number(id))
        const newSelectedIds = [...new Set([...currentSelected, ...deptUserIds])]
        this.tempSelectedUserIds = newSelectedIds
        
        // 更新当前页面的选择状态
        this.updateTableSelection()
        
        this.$message.success(`已选择 ${this.currentSelectedDept.label} 部门的 ${deptUserIds.length} 名用户`)
      })
    },
    
    // 选择含子部门全员
    handleSelectSubDeptUsers() {
      if (!this.currentSelectedDept || !this.currentSelectedDept.children || this.currentSelectedDept.children.length === 0) return
      
      // 获取所有子部门的用户
      const allSubDeptIds = this.getAllSubDeptIds(this.currentSelectedDept.deptId)
      
      // 获取所有子部门的用户
      listUser({ 
        deptIds: allSubDeptIds, 
        status: '0',
        pageNum: 1,
        pageSize: 10000 // 获取所有用户
      }).then(response => {
        const subDeptUserIds = response.rows.map(user => Number(user.userId))
        
        // 合并到临时选择列表中，确保数据类型一致
        const currentSelected = this.tempSelectedUserIds.map(id => Number(id))
        const newSelectedIds = [...new Set([...currentSelected, ...subDeptUserIds])]
        this.tempSelectedUserIds = newSelectedIds
        
        // 更新当前页面的选择状态
        this.updateTableSelection()
        
        this.$message.success(`已选择 ${this.currentSelectedDept.label} 部门的 ${subDeptUserIds.length} 名用户`)
      })
    },
    
    // 获取所有子部门ID
    getAllSubDeptIds(deptId) {
      const allSubDeptIds = []
      const deptIds = [deptId]
      
      while (deptIds.length > 0) {
        const currentDeptId = deptIds.pop()
        allSubDeptIds.push(currentDeptId)
        
        const children = this.deptTreeData.find(dept => dept.parentId === currentDeptId)
        if (children && children.length > 0) {
          deptIds.push(...children.map(child => child.deptId))
        }
      }
      
      return allSubDeptIds
    },
    
    // 获取用户列表
    getList() {
      // 设置加载标志，防止在数据更新期间处理选择变化事件
      this.isLoadingData = true
      
      listUser(this.queryParams).then(response => {
        this.userList = response.rows
        this.total = response.total
        
        // 等待DOM更新后再恢复选择状态
        this.$nextTick(() => {
          // 延迟执行，确保表格完全渲染
          setTimeout(() => {
            this.updateTableSelection()
          }, 50) // 减少延迟时间，提升体验
        })
      }).catch(error => {
        console.error('UserSelect: 获取用户列表失败:', error)
        this.userList = []
        this.total = 0
        // 加载失败时也要清除标志位
        this.isLoadingData = false
      })
    },

    // 更新表格选择状态
    updateTableSelection() {
      if (!this.userList || this.userList.length === 0 || !this.$refs.table) {
        return
      }
      
      // 使用多重延迟确保表格完全稳定
      this.$nextTick(() => {
        setTimeout(() => {
          this.forceUpdateTableSelection()
        }, 30) // 减少延迟时间，提升体验
      })
    },
    
        // 强制更新表格选择状态
    forceUpdateTableSelection() {
      if (!this.$refs.table || !this.userList.length) {
        return
      }
      
      try {
        // 转换为数字数组进行比较
        const selectedIds = this.tempSelectedUserIds.map(id => Number(id))
        
        // 先清除选择
        this.$refs.table.clearSelection()
        
        // 等待一个tick，然后再设置选择
        this.$nextTick(() => {
          // 遍历设置选中状态
          this.userList.forEach((user, index) => {
            const userId = Number(user.userId)
            if (selectedIds.includes(userId)) {
              this.$refs.table.toggleRowSelection(user, true)
            }
          })
          
          // 恢复事件处理，允许正常的选择变化处理
          this.isLoadingData = false
          
          // 验证选择状态
          setTimeout(() => {
            this.verifyTableSelection()
          }, 30)
        })
        
      } catch (error) {
        console.error('UserSelect: 强制更新表格选择状态失败', error)
        // 出错时也要清除标志位
        this.isLoadingData = false
      }
    },
    
    // 验证表格选择状态
    verifyTableSelection() {
      if (!this.$refs.table) return
      
      try {
        const currentSelection = this.$refs.table.selection || []
        const currentSelectedIds = currentSelection.map(user => Number(user.userId))
        const expectedIds = this.tempSelectedUserIds.map(id => Number(id))
        const currentPageIds = this.userList.map(user => Number(user.userId))
        const shouldBeSelectedOnCurrentPage = expectedIds.filter(id => currentPageIds.includes(id))
        
        // 如果状态不匹配，再次尝试修复
        if (!shouldBeSelectedOnCurrentPage.every(id => currentSelectedIds.includes(id))) {
          console.log('UserSelect: 选择状态不匹配，尝试修复...')
          setTimeout(() => {
            this.forceUpdateTableSelection()
          }, 50)
        }
        
      } catch (error) {
        console.error('UserSelect: 验证表格选择状态失败', error)
      }
    },
    
    // 点击行选择
    clickRow(row) {
      this.$refs.table.toggleRowSelection(row)
    },
    
    // 表格选择变化
    handleSelectionChange(selection) {
      // 如果正在加载数据，忽略此事件，避免状态被错误重置
      if (this.isLoadingData) {
        console.log('UserSelect: 数据加载中，忽略选择变化事件', {
          selection: selection.length,
          isLoadingData: this.isLoadingData
        })
        return
      }
      
      console.log('UserSelect: 处理表格选择变化', {
        selectionCount: selection.length,
        oldTempSelectedCount: this.tempSelectedUserIds.length
      })
      
      // 确保数据类型一致性
      const currentPageUserIds = this.userList.map(user => Number(user.userId))
      const selectedOnCurrentPage = selection.map(item => Number(item.userId))
      
      // 保留其他页面的选择状态
      const selectedOnOtherPages = this.tempSelectedUserIds.filter(id => {
        const numId = Number(id)
        return !currentPageUserIds.includes(numId)
      })
      
      // 合并选择状态，确保所有ID都是数字类型
      this.tempSelectedUserIds = [...selectedOnOtherPages, ...selectedOnCurrentPage]
        .map(id => Number(id))
        .filter((id, index, arr) => arr.indexOf(id) === index) // 去重
      
      console.log('UserSelect: 选择状态更新后', {
        newTempSelectedCount: this.tempSelectedUserIds.length,
        selectedOnOtherPagesCount: selectedOnOtherPages.length,
        selectedOnCurrentPageCount: selectedOnCurrentPage.length
      })
    },
    
    // 搜索
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    
    // 重置搜索
    resetQuery() {
      this.$refs["queryForm"].resetFields()
      this.queryParams.deptId = this.currentSelectedDept ? this.currentSelectedDept.deptId : undefined
      this.handleQuery()
    },
    
    // 分页切换
    handlePagination() {
      this.getList()
    },
    
    // 确认选择
    handleConfirm() {
      // 确保数据类型一致性
      this.selectedUserIds = [...this.tempSelectedUserIds.map(id => Number(id))]
      this.updateSelectedUserList()
      this.dialogVisible = false
      this.$emit('input', this.selectedUserIds)
      this.$emit('change', this.selectedUserIds)
      // 发射确认事件，传递选中的用户对象列表
      this.$emit('confirm', this.selectedUserList)
    },
    
    // 取消选择
    handleCancel() {
      // 恢复临时选择为原始状态
      this.tempSelectedUserIds = [...this.selectedUserIds.map(id => Number(id))]
      this.dialogVisible = false
      // 发射取消事件
      this.$emit('cancel')
    },
    
    // 移除用户
    handleRemoveUser(userId) {
      this.selectedUserIds = this.selectedUserIds.filter(id => id !== userId)
      this.updateSelectedUserList()
      this.$emit('input', this.selectedUserIds)
      this.$emit('change', this.selectedUserIds)
    },
    
    // 更新已选用户列表
    updateSelectedUserList() {
      if (this.selectedUserIds.length === 0) {
        this.selectedUserList = []
        return
      }
      
      // 从已有的用户列表中查找，确保数据类型一致
      const foundUsers = []
      const notFoundUserIds = []
      
      for (let userId of this.selectedUserIds) {
        const user = this.userList.find(u => Number(u.userId) === Number(userId))
        if (user) {
          foundUsers.push(user)
        } else {
          notFoundUserIds.push(userId)
        }
      }
      
      // 如果有未找到的用户，需要查询
      if (notFoundUserIds.length > 0) {
        // 查询所有用户来获取用户信息
        listUser({ pageNum: 1, pageSize: 10000 }).then(response => {
          const allUsers = response.rows
          const selectedUsers = allUsers.filter(user => 
            this.selectedUserIds.some(id => Number(id) === Number(user.userId))
          )
          this.selectedUserList = selectedUsers
        })
      } else {
        this.selectedUserList = foundUsers
      }
    }
  }
}
</script>

<style scoped>
.user-select-display {
  min-height: 32px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  padding: 6px 12px;
  cursor: pointer;
  transition: border-color 0.2s cubic-bezier(0.645, 0.045, 0.355, 1);
}

.user-select-display:hover {
  border-color: #c0c4cc;
}

.custom-tree-node {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 14px;
  padding-right: 8px;
}

.el-tree-node__content {
  height: 32px;
}

/* 加载状态过渡动画 */
.fade-enter-active, .fade-leave-active {
  transition: opacity 0.2s;
}
.fade-enter, .fade-leave-to {
  opacity: 0;
}

/* 旋转动画 */
@keyframes rotate {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

/* 排除用户行样式 */
:deep(.excluded-user-row) {
  background-color: #f5f7fa !important;
  color: #c0c4cc !important;
}

:deep(.excluded-user-row):hover {
  background-color: #f5f7fa !important;
}

:deep(.excluded-user-row .el-table__cell) {
  color: #c0c4cc !important;
}
</style> 