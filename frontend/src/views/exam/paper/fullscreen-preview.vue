<template>
  <div class="fullscreen-preview">
    <!-- 头部工具栏 -->
    <div class="preview-header">
      <div class="header-left">
        <h2>{{ paper.paperName }}</h2>
        <span class="paper-info">总分：{{ paper.totalScore }}分 | 时长：{{ paper.duration }}分钟</span>
      </div>
      <div class="header-right">
        <el-button @click="showAnswers = !showAnswers" :type="showAnswers ? 'warning' : 'primary'">
          {{ showAnswers ? '隐藏答案' : '显示答案' }}
        </el-button>
        <el-button @click="printPaper" icon="el-icon-printer">打印</el-button>
      </div>
    </div>

    <!-- 试卷内容 -->
    <div class="preview-content" v-loading="loading">
      <paper-preview 
        v-if="paperId" 
        :paper-id="paperId" 
        :is-fullscreen="true"
        :initial-show-answers="showAnswers"
        ref="paperPreview"
      />
    </div>
  </div>
</template>

<script>
import PaperPreview from './components/PaperPreview'
import { getPaper } from "@/api/exam/paper"

export default {
  name: "FullscreenPreview",
  components: {
    PaperPreview
  },
  data() {
    return {
      paperId: null,
      paper: {},
      showAnswers: true,
      loading: false
    }
  },
  created() {
    this.paperId = this.$route.params.id
    // 立即设置加载状态标题，避免显示之前缓存的试卷名称
    this.setLoadingTitle()
    this.loadPaper()
  },
  watch: {
    showAnswers(newVal) {
      // 同步答案显示状态到子组件
      if (this.$refs.paperPreview) {
        this.$refs.paperPreview.showAnswers = newVal
      }
    },
    // 监听paper对象变化，确保标题及时更新
    'paper.paperName'(newVal) {
      if (newVal) {
        this.updatePageTitle()
      }
    },
    // 监听路由变化，确保每次切换试卷都重新设置标题
    '$route'(to, from) {
      // 只有在仍然是试卷预览页面时才执行相关逻辑
      const previewRouteNames = ['FullscreenPreview', 'FullscreenPreviewRealPath']
      if (previewRouteNames.includes(to.name) && to.params.id !== from.params.id) {
        this.paperId = to.params.id
        // 立即设置加载状态，避免显示旧标题
        this.setLoadingTitle()
        this.loadPaper()
      }
    }
  },
  methods: {
    async loadPaper() {
      // 检查paperId是否有效
      if (!this.paperId) {
        this.$message.error("试卷ID无效")
        return
      }
      
      this.loading = true
      try {
        const response = await getPaper(this.paperId)
        this.paper = response.data
        // 数据加载完成后更新标签页标题
        this.$nextTick(() => {
          this.updatePageTitle()
        })
      } catch (error) {
        this.$message.error("加载试卷信息失败")
      } finally {
        this.loading = false
      }
    },
    setLoadingTitle() {
      // 立即设置加载状态标题，避免显示旧的缓存标题
      const title = '试卷预览 - 加载中'
      
      // 更新当前路由的meta信息
      this.$route.meta.title = title
      
      // 通知store更新已访问的视图
      this.$store.dispatch('tagsView/updateVisitedView', {
        ...this.$route,
        title: title,
        meta: { ...this.$route.meta, title: title }
      })
    },
    updatePageTitle() {
      // 动态更新标签页标题
      const title = this.paper.paperName ? `${this.paper.paperName} - 预览` : `试卷预览`
      
      // 更新当前路由的meta信息
      this.$route.meta.title = title
      
      // 通知store更新已访问的视图，确保title字段被正确设置
      this.$store.dispatch('tagsView/updateVisitedView', {
        ...this.$route,
        title: title,  // 添加title字段，这是标签页显示的关键
        meta: { ...this.$route.meta, title: title }
      })
    },
    printPaper() {
      window.print()
    }
  }
}
</script>

<style scoped>
.fullscreen-preview {
  min-height: 100vh;
  background: #f5f5f5;
}

.preview-header {
  position: sticky;
  top: 0;
  background: #fff;
  padding: 20px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-left h2 {
  margin: 0 0 5px 0;
  color: #303133;
  font-size: 24px;
}

.paper-info {
  color: #606266;
  font-size: 14px;
}

.header-right .el-button {
  margin-left: 10px;
}

.preview-content {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

/* 打印样式 */
@media print {
  .preview-header {
    display: none;
  }
  
  .preview-content {
    padding: 0;
    max-width: none;
  }
  
  .fullscreen-preview {
    background: #fff;
  }
}
</style>
