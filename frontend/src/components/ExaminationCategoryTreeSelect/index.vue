<template>
  <treeselect
    v-model="selectedValue"
    :options="examinationCategoryOptions"
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
    :append-to-body="appendToBody"
    :flat="false"
    :multiple="false"
    :limit="100"
    @input="handleInput"
  />
</template>

<script>
import { listExaminationCategory } from "@/api/exam/examination-category";
import Treeselect from "@riophae/vue-treeselect";
import "@riophae/vue-treeselect/dist/vue-treeselect.css";

export default {
  name: "ExaminationCategoryTreeSelect",
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
      default: "请选择考试分类"
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
    },
    // 是否附加到body元素（解决容器溢出问题）
    appendToBody: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      // 考试分类选项
      examinationCategoryOptions: [],
      // 选中值
      selectedValue: this.value
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
    // 组件挂载完成
  },
  methods: {
    /** 获取考试分类列表 */
    getCategoryList() {
      // 临时使用测试数据
      const testData = [
        {
          id: 1,
          categoryName: '安全考试',
          parentId: 0,
          orderNum: 1,
          status: '0'
        },
        {
          id: 2,
          categoryName: '基础安全',
          parentId: 1,
          orderNum: 1,
          status: '0'
        },
        {
          id: 3,
          categoryName: '高空作业',
          parentId: 1,
          orderNum: 2,
          status: '0'
        },
        {
          id: 4,
          categoryName: '技能考试',
          parentId: 0,
          orderNum: 2,
          status: '0'
        },
        {
          id: 5,
          categoryName: '电工技能',
          parentId: 4,
          orderNum: 1,
          status: '0'
        }
      ];
      
      this.examinationCategoryOptions = this.handleTree(testData, "id", "parentId");
      
      // 注释掉原来的API调用，测试通过后再恢复
      /*
      listExaminationCategory().then(response => {
        this.examinationCategoryOptions = this.handleTree(response.data, "id", "parentId");
      }).catch(error => {
        console.error('获取考试分类失败:', error);
        this.examinationCategoryOptions = [];
      });
      */
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
    /** 处理输入值变化 */
    handleInput(value) {
      this.selectedValue = value;
      this.$emit('input', value);
      this.$emit('change', value);
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
          return father[id] === child[parentId];
        });
        branchArr.length > 0 ? father.children = branchArr : '';
        //返回第一层
        return father[parentId] === rootId;
      });
      
      return treeData !== '' ? treeData : data;
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
  height: 40px;
}

.vue-treeselect__placeholder,
.vue-treeselect__single-value {
  color: #606266;
  font-size: 14px;
  line-height: 40px;
}

.vue-treeselect__menu {
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  background-color: #fff;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.12), 0 0 6px rgba(0, 0, 0, 0.04);
}
</style> 