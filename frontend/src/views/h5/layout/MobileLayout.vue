<template>
  <div class="h5-layout">
    <div class="h5-header" v-if="showHeader">
      <div class="header-left">
        <i v-if="showBack" class="el-icon-arrow-left" @click="goBack"></i>
      </div>
      <div class="header-title">{{ title }}</div>
      <div class="header-right"></div>
    </div>
    <div class="h5-content" :class="{ 'with-header': showHeader }">
      <router-view />
    </div>
  </div>
</template>

<script>
export default {
  name: 'MobileLayout',
  computed: {
    title() {
      return this.$route.meta.title || '考试系统'
    },
    showHeader() {
      return this.$route.meta.showHeader !== false
    },
    showBack() {
      return this.$route.meta.showBack !== false
    }
  },
  methods: {
    goBack() {
      this.$router.back()
    }
  }
}
</script>

<style scoped>
.h5-layout {
  min-height: 100vh;
  background: #f5f5f5;
}

.h5-header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  height: 44px;
  background: linear-gradient(135deg, #409eff 0%, #2593fc 100%);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 12px;
  z-index: 1000;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.header-left,
.header-right {
  width: 50px;
  display: flex;
  align-items: center;
}

.header-left i {
  font-size: 20px;
  color: #fff;
  cursor: pointer;
}

.header-title {
  font-size: 17px;
  font-weight: 500;
  color: #fff;
  text-align: center;
  flex: 1;
}

.h5-content {
  min-height: 100vh;
}

.h5-content.with-header {
  padding-top: 44px;
}
</style>
