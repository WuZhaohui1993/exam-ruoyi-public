<template>
  <div class="app-container exam-users-container">
    <div class="page-header">
      <div>
        <h2>{{ examName || '考试人员' }}</h2>
        <div class="page-subtitle">维护点名参考人员和一次性按部门添加结果</div>
      </div>
      <div class="header-actions">
        <el-button icon="el-icon-back" @click="handleBack">返回</el-button>
      </div>
    </div>

    <exam-user-manager v-if="examId" :exam-id="examId" />
  </div>
</template>

<script>
import ExamUserManager from "@/components/ExamUserManager";
import { checkPermi } from "@/utils/permission";

export default {
  name: "ExaminationUsers",
  components: {
    ExamUserManager
  },
  data() {
    return {
      examId: null,
      examName: ''
    };
  },
  created() {
    if (!checkPermi(['exam:examination:detail'])) {
      this.$message.error('您没有权限访问考试人员页面');
      this.$router.go(-1);
      return;
    }

    this.examId = parseInt(this.$route.params.id);
    this.examName = this.$route.query.examName || '';
    if (!this.examId) {
      this.$modal.msgError('考试ID参数缺失');
      this.handleBack();
    }
  },
  methods: {
    handleBack() {
      this.$router.go(-1);
    }
  }
};
</script>

<style scoped>
.exam-users-container {
  padding: 20px;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 18px 20px;
  margin-bottom: 16px;
  background: #fff;
  border: 1px solid #ebeef5;
  border-radius: 6px;
}

.page-header h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: #303133;
}

.page-subtitle {
  margin-top: 6px;
  font-size: 13px;
  color: #909399;
}

.header-actions {
  display: flex;
  align-items: center;
}
</style>
