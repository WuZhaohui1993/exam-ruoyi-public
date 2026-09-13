<template>
  <treeselect
    v-model="selectedValue"
    :options="categoryOptions"
    :normalizer="normalizer"
    :placeholder="placeholder"
    :clearable="clearable"
    :disabled="disabled"
    :show-count="showCount"
    :search-nested="true"
    :clear-value-text="clearValueText"
    :no-children-text="noChildrenText"
    :no-options-text="noOptionsText"
    :no-results-text="noResultsText"
    :default-expand-level="1"
    @input="handleInput"
  />
</template>

<script>
import { categoryTreeselect } from "@/api/exam/paper-category";
import Treeselect from "@riophae/vue-treeselect";
import "@riophae/vue-treeselect/dist/vue-treeselect.css";

export default {
  name: "PaperCategoryTreeSelect",
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
      default: "请选择试卷分类"
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
      // 数据加载状态
      loading: false
    };
  },
  watch: {
    value(newVal) {
      this.selectedValue = newVal;
    }
  },
  mounted() {
    // 如果创建时数据为空，在mounted时再次尝试获取
    if (!this.categoryOptions || this.categoryOptions.length === 0) {
      this.$nextTick(() => {
        this.getCategoryList();
      });
    }
  },
  created() {
    this.getCategoryList();
  },
  methods: {
    /** 获取试卷分类列表 */
    getCategoryList() {
      if (this.loading) return;
      
      this.loading = true;
      categoryTreeselect().then(response => {
        const data = response.data || [];
        this.categoryOptions = data;
        this.loading = false;
      }).catch(error => {
        console.error('获取试卷分类列表失败:', error);
        this.categoryOptions = [];
        this.loading = false;
      });
    },
    /** 转换数据结构 */
    normalizer(node) {
      if (node.children && !node.children.length) {
        delete node.children;
      }
      return {
        id: node.id,
        label: node.categoryName,
        children: node.children
      };
    },
    /** 构造树型结构数据 */
    handleTree(data, id, parentId, children, rootId) {
      id = id || 'id';
      parentId = parentId || 'parentId';
      children = children || 'children';
      rootId = rootId || Math.min(...data.map(item => { return item[parentId] })) || 0;

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
    },
    /** 处理输入值变化 */
    handleInput(value) {
      this.selectedValue = value;
      this.$emit('input', value);
      this.$emit('change', value);
    },
    /** 强制刷新数据 */
    refresh() {
      this.loading = false; // 重置loading状态
      this.getCategoryList();
    }
  }
};
</script>

<style scoped>
/* 可根据需要添加自定义样式 */
</style>
