<template>
  <treeselect
    v-model="selectedValue"
    :options="categoryOptions"
    :normalizer="normalizer"
    :placeholder="placeholder"
    :clearable="clearable"
    :disabled="disabled || loading"
    :show-count="showCount"
    :search-nested="true"
    :clear-value-text="clearValueText"
    :no-children-text="noChildrenText"
    :no-options-text="noOptionsText"
    :no-results-text="noResultsText"
    :loading="loading"
    :default-expand-level="1"
    @input="handleInput"
    @open="handleOpen"
  />
</template>

<script>
import { listCategory } from "@/api/exam/category";
import Treeselect from "@riophae/vue-treeselect";
import "@riophae/vue-treeselect/dist/vue-treeselect.css";

export default {
  name: "CategoryTreeSelect",
  components: {
    Treeselect
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
      default: "请选择试题分类"
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
    // 是否显示计数
    showCount: {
      type: Boolean,
      default: false
    },
    // 清空提示文本
    clearValueText: {
      type: String,
      default: "清空"
    },
    // 无子节点提示文本
    noChildrenText: {
      type: String,
      default: "暂无数据"
    },
    // 无选项提示文本
    noOptionsText: {
      type: String,
      default: "暂无数据"
    },
    // 无结果提示文本
    noResultsText: {
      type: String,
      default: "暂无数据"
    }
  },
  data() {
    return {
      // 分类选项
      categoryOptions: [],
      // 选中值
      selectedValue: this.value,
      // 加载状态
      loading: false,
      // 数据是否已加载
      dataLoaded: false
    };
  },
  watch: {
    value(newVal) {
      this.selectedValue = newVal;
    }
  },
  created() {
    this.getCategoryList();
  },
  mounted() {
    // 如果创建时数据为空，在mounted时再次尝试获取
    if (!this.dataLoaded) {
      this.$nextTick(() => {
        this.getCategoryList();
      });
    }
  },
  methods: {
    /** 获取分类列表 */
    getCategoryList() {
      // 防止重复请求
      if (this.loading) {
        return Promise.resolve();
      }
      
      this.loading = true;
      
      return listCategory().then(response => {
        const data = response.data || [];
        this.categoryOptions = this.handleTree(data, "id", "parentId");
        this.dataLoaded = true;
        this.loading = false;
        return this.categoryOptions;
      }).catch(error => {
        console.error('获取试题分类列表失败:', error);
        this.categoryOptions = [];
        this.dataLoaded = false;
        this.loading = false;
        // 不要在这里抛出错误，避免影响上层组件
        return [];
      });
    },
    
    /** 处理展开事件 */
    handleOpen() {
      // 如果数据还没加载，尝试重新加载
      if (!this.dataLoaded && !this.loading) {
        this.getCategoryList();
      }
    },
    
    /** 转换数据结构 */
    normalizer(node) {
      if (!node) return null;
      
      // 如果有子节点但为空数组，则删除children属性
      if (node.children && !node.children.length) {
        delete node.children;
      }
      
      return {
        id: node.id,
        label: node.categoryName || '未知分类',
        children: node.children
      };
    },
    
    /** 构造树型结构数据 */
    handleTree(data, id, parentId, children, rootId) {
      if (!data || !Array.isArray(data)) {
        return [];
      }
      
      id = id || 'id';
      parentId = parentId || 'parentId';
      children = children || 'children';
      rootId = rootId || Math.min(...data.map(item => { return item[parentId] })) || 0;
      
      try {
        //对源数据深度克隆
        const cloneData = JSON.parse(JSON.stringify(data));
        
        //循环所有项
        const treeData = cloneData.filter(father => {
          let branchArr = cloneData.filter(child => {
            //返回每一项的子级数组
            return father[id] === child[parentId]
          });
          branchArr.length > 0 ? father.children = branchArr : '';
          //返回第一层
          return father[parentId] === rootId;
        });
        
        return treeData !== '' ? treeData : data;
      } catch (error) {
        console.error('构建树型结构失败:', error);
        return data;
      }
    },
    
    /** 处理输入值变化 */
    handleInput(value) {
      this.selectedValue = value;
      this.$emit('input', value);
      this.$emit('change', value);
    },
    
    /** 强制刷新数据 */
    refresh() {
      this.dataLoaded = false;
      this.categoryOptions = [];
      return this.getCategoryList();
    }
  }
};
</script>

<style scoped>
/* 使用appendToBody后，下拉菜单渲染到body中，不再需要复杂的z-index设置 */
/* 基础样式保持不变 */
.vue-treeselect {
  position: relative;
}

.vue-treeselect__control {
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  min-height: 32px;
}

.vue-treeselect__placeholder,
.vue-treeselect__single-value {
  color: #606266;
  font-size: 14px;
  line-height: 32px;
}

.vue-treeselect__menu {
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  background-color: #fff;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.12), 0 0 6px rgba(0, 0, 0, 0.04);
  z-index: 3000;
}

/* 禁用状态样式 */
.vue-treeselect--disabled .vue-treeselect__control {
  background-color: #f5f7fa;
  border-color: #e4e7ed;
  color: #c0c4cc;
  cursor: not-allowed;
}
</style> 