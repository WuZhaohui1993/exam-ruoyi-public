# UserSelect 用户选择器组件

## 概述
用户选择器组件支持部门树形结构的用户选择功能，特别适用于大用户量场景下的用户选择操作。

## 功能特性

### 🌲 部门树形结构
- 左侧展示完整的部门树形结构
- 实时显示每个部门的用户数量
- 支持部门节点的点击筛选

### 👥 用户选择功能
- 支持单选和多选模式
- 跨页面保持选择状态
- 用户信息详细展示（用户名、昵称、部门、手机号、状态）

### ⚡ 快速选择
- 一键选择部门全员
- 智能合并选择结果
- 支持用户搜索和筛选

### ✨ 用户体验优化
- 大对话框布局（1000px宽度）
- 响应式设计
- 实时选择数量统计
- 友好的操作反馈

## 使用方法

### 基本用法
```vue
<template>
  <div>
    <user-select v-model="selectedUserIds" />
  </div>
</template>

<script>
import UserSelect from "@/components/UserSelect"

export default {
  components: {
    UserSelect
  },
  data() {
    return {
      selectedUserIds: []
    }
  }
}
</script>
```

### 在表单中使用
```vue
<template>
  <el-form :model="form" :rules="rules">
    <el-form-item label="参考人员" prop="examUserIds">
      <user-select v-model="form.examUserIds" placeholder="请选择参考人员" />
    </el-form-item>
  </el-form>
</template>

<script>
export default {
  data() {
    return {
      form: {
        examUserIds: []
      },
      rules: {
        examUserIds: [
          { required: true, message: "参考人员不能为空", trigger: "change" }
        ]
      }
    }
  }
}
</script>
```

## 属性配置

### Props
| 参数 | 说明 | 类型 | 可选值 | 默认值 |
|------|------|------|--------|--------|
| value / v-model | 绑定值，用户ID数组 | Array | — | [] |
| multiple | 是否多选 | Boolean | — | true |

### Events
| 事件名称 | 说明 | 回调参数 |
|----------|------|----------|
| input | 选择值发生变化时触发 | 当前选择的用户ID数组 |
| change | 选择值发生变化时触发 | 当前选择的用户ID数组 |

## 界面布局

```
┌─────────────────────────────────────────────────────────────────────┐
│                           选择用户                                      │
├─────────────────────────────────────────────────────────────────────┤
│  部门列表          │                用户列表                           │
│  ├ 公司总部 (5)     │  用户名称: [____] 手机号码: [____] [搜索] [重置]      │
│  │ ├ 技术部 (8)    │                                                    │
│  │ ├ 财务部 (3)    │  当前部门: 技术部 (共 8 人)                         │
│  │ └ 人事部 (2)    │                                                    │
│  └ 分公司 (12)      │  ☑ 张三    李技术  技术部      13912345678  正常     │
│  [选择部门全员]      │  ☑ 李四    王开发  技术部      13987654321  正常     │
│                    │  □ 王五    赵测试  技术部      13765432109  正常     │
│                    │                                                    │
│                    │  [分页组件]                                         │
├─────────────────────────────────────────────────────────────────────┤
│  已选择 15 人                                       [确定] [取消]        │
└─────────────────────────────────────────────────────────────────────┘
```

## 技术实现

### 核心依赖
```javascript
import { listUser } from "@/api/system/user"
import { listDept } from "@/api/system/dept"
import Pagination from "@/components/Pagination"
```

### 关键方法
- `loadDeptTree()` - 加载部门树数据
- `handleDeptNodeClick()` - 处理部门节点点击
- `handleSelectAllUsers()` - 选择部门全员
- `countDeptUsers()` - 统计部门用户数量
- `handleTree()` - 构建树形结构

### 性能优化
- 分页加载用户数据
- 异步统计部门用户数量
- 智能缓存选择状态
- 防抖处理搜索操作

## 样式定制

### CSS类名
```css
.user-select-display {
  /* 用户选择显示区域 */
}

.custom-tree-node {
  /* 自定义树节点样式 */
}
```

### 主题定制
组件使用Element UI的主题系统，支持全局主题定制。

## 注意事项

1. **数据权限**：组件会根据当前用户的数据权限显示相应的部门和用户
2. **性能考量**：在大量用户数据时，建议启用分页加载
3. **兼容性**：需要Element UI 2.x版本支持
4. **API依赖**：依赖系统用户和部门管理API

## 更新日志

### v1.1.0 (2024-12-19)
- ✅ 新增部门树形结构支持
- ✅ 支持按部门筛选用户
- ✅ 新增选择部门全员功能
- ✅ 优化大用户量下的选择体验
- ✅ 改进用户界面布局

### v1.0.0 (2024-03-20)
- ✅ 基础用户选择功能
- ✅ 多选和单选支持
- ✅ 用户搜索和分页
- ✅ 跨页选择状态保持

## 相关组件
- [DeptTreeSelect](../DeptTreeSelect/) - 部门树选择器
- [UserTreeSelect](../UserTreeSelect/) - 用户树选择器
- [Pagination](../Pagination/) - 分页组件 