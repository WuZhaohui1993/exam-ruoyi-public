<template>
  <div class="practice-redirect" v-loading="true" element-loading-text="正在进入学习中心">
    <span>正在进入学习中心</span>
  </div>
</template>

<script>
export default {
  name: 'StudentPractice',
  created() {
    this.$router.replace({
      path: '/student/training',
      query: { tab: 'practice' }
    })
  }
}
</script>

<style scoped>
.practice-redirect {
  min-height: 240px;
}
</style>
