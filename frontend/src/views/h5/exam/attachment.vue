<template>
  <div class="h5-attachment" v-loading="loading">
    <div class="attachment-card">
      <h2>上传附件</h2>
      <p>{{ examination.examName || '考试附件' }}</p>

      <div class="form-block">
        <div class="form-label">考试记录</div>
        <el-input v-model="form.examUserId" disabled />
      </div>

      <div class="form-block">
        <div class="form-label">附件文件</div>
        <file-upload
          v-model="form.fileUrl"
          :limit="1"
          :file-size="20"
          :file-type="['pdf','doc','docx','png','jpg','jpeg','mp4']"
          :drag="false"
        />
      </div>

      <el-button type="primary" class="submit-btn" :loading="submitting" @click="submitAttachment">保存附件</el-button>
      <el-button class="back-btn" @click="goBack">返回结果</el-button>
    </div>
  </div>
</template>

<script>
import { getExamInfo, addExamAttachment } from '@/api/mobile/exam'

export default {
  name: 'H5ExamAttachment',
  data() {
    return {
      loading: false,
      submitting: false,
      examId: null,
      examination: {},
      form: {
        examId: null,
        examUserId: null,
        fileUrl: '',
        uploadSource: 'h5'
      }
    }
  },
  created() {
    this.examId = this.$route.params.examId
    this.form.examId = Number(this.examId)
    this.form.examUserId = this.$route.query.examUserId ? Number(this.$route.query.examUserId) : null
    this.loadExamInfo()
  },
  methods: {
    loadExamInfo() {
      this.loading = true
      getExamInfo(this.examId).then(res => {
        if (res.code === 200) {
          this.examination = res.examination || {}
          if (!this.form.examUserId && res.examUser && res.examUser.id) {
            this.form.examUserId = res.examUser.id
          }
        }
      }).finally(() => {
        this.loading = false
      })
    },
    submitAttachment() {
      if (!this.form.examUserId) {
        this.$message.error('缺少考试记录，请从结果页重新进入')
        return
      }
      if (!this.form.fileUrl) {
        this.$message.error('请先上传附件')
        return
      }
      const fileUrl = this.form.fileUrl.split(',')[0]
      this.submitting = true
      addExamAttachment({
        examId: this.form.examId,
        examUserId: this.form.examUserId,
        fileUrl,
        fileName: this.getFileName(fileUrl),
        fileType: this.getFileExt(fileUrl),
        uploadSource: 'h5'
      }).then(() => {
        this.$message.success('附件已保存')
        this.form.fileUrl = ''
      }).finally(() => {
        this.submitting = false
      })
    },
    goBack() {
      this.$router.push({
        path: '/h5/exam/' + this.examId + '/result',
        query: this.form.examUserId ? { examUserId: this.form.examUserId } : {}
      })
    },
    getFileName(url) {
      return (url || '').split('/').pop() || '附件'
    },
    getFileExt(url) {
      const name = this.getFileName(url)
      return name.includes('.') ? name.split('.').pop() : ''
    }
  }
}
</script>

<style scoped>
.h5-attachment {
  min-height: 100vh;
  background: #f5f7fa;
  padding: 16px;
}

.attachment-card {
  background: #fff;
  border-radius: 12px;
  padding: 18px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.attachment-card h2 {
  margin: 0;
  font-size: 20px;
  color: #303133;
}

.attachment-card p {
  margin: 8px 0 20px;
  color: #909399;
  line-height: 20px;
}

.form-block {
  margin-bottom: 18px;
}

.form-label {
  margin-bottom: 8px;
  color: #606266;
  font-size: 14px;
}

.submit-btn,
.back-btn {
  width: 100%;
  margin: 8px 0 0;
}
</style>
