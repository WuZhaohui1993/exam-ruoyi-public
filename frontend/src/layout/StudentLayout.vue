<template>
  <div class="student-app-wrapper">
    <!-- 顶部导航栏 -->
    <div class="student-header">
      <div class="header-container">
        <!-- Logo和品牌名 -->
        <div class="header-brand">
          <img src="@/assets/logo/logo.svg" alt="Logo" class="brand-logo">
          <span class="brand-name">学习考试</span>
        </div>

        <!-- 主导航菜单 - 基于若依权限框架 -->
        <div class="header-nav">
          <el-menu
            mode="horizontal"
            :default-active="activeMenu"
            class="nav-menu"
            background-color="#4285f4"
            text-color="#ffffff"
            active-text-color="#ffffff"
            @select="handleMenuSelect"
          >
            <!-- 根据用户权限动态渲染菜单 -->
            <template v-for="route in topMenus">
              <el-menu-item
                :key="route.path"
                :index="getMenuIndex(route)"
                v-if="!route.hidden && hasPermission(route)"
              >
                <i :class="route.meta.icon" v-if="route.meta.icon"></i>
                <span>{{ route.meta.title }}</span>
              </el-menu-item>
            </template>
          </el-menu>
        </div>

        <!-- 用户信息和操作 -->
        <div class="header-user">
          <el-dropdown @command="handleUserCommand">
            <span class="user-info">
              <el-avatar :size="32" :src="avatar" class="user-avatar">
                <i class="el-icon-user-solid"></i>
              </el-avatar>
              <span class="user-name">{{ name || nickName }}</span>
              <i class="el-icon-arrow-down"></i>
            </span>
            <el-dropdown-menu slot="dropdown">
              <el-dropdown-item command="profile">
                <i class="el-icon-user"></i> 个人中心
              </el-dropdown-item>
              <el-dropdown-item divided command="logout">
                <i class="el-icon-switch-button"></i> 退出登录
              </el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>
        </div>
      </div>
    </div>

    <!-- 主内容区域 -->
    <div class="student-main">
      <transition name="fade-transform" mode="out-in" appear>
        <keep-alive :include="cachedViews">
          <router-view :key="key" />
        </keep-alive>
      </transition>
    </div>
  </div>
</template>

<script>
import { mapGetters } from 'vuex'
import { isExternal } from '@/utils/validate'

export default {
  name: 'StudentLayout',
  computed: {
    ...mapGetters(['name', 'nickName', 'avatar', 'roles', 'permissions', 'sidebar', 'cachedViews']),

    activeMenu() {
      const route = this.$route
      const { meta, path } = route

      // 如果页面配置了activeMenu，使用配置的值
      if (meta.activeMenu) {
        return meta.activeMenu
      }

      // 对于学员端路由，需要正确匹配菜单路径
      // 如果当前是根路径/student，默认选中学习大厅
      if (path === '/student' || path === '/student/') {
        return '/student/dashboard'
      }

      // 其他情况返回当前路径
      return path
    },

    key() {
      return this.$route.path
    },

    // 获取顶部菜单 - 基于若依权限框架
    topMenus() {
      // 获取当前用户的角色
      const userRoles = this.roles || []

      // 从store中获取所有路由菜单
      const allRoutes = this.$store.getters.sidebarRouters || []

             // 查找学员端父菜单（数据库中名称为"学员中心"）
       const studentParentMenu = allRoutes.find(route => {
         // 通过菜单名称或路径识别学员端父菜单
         return (route.meta && route.meta.title === '学员中心') ||
                (route.path === 'student' || route.path === '/student') ||
                (route.name === 'student')
       })

      if (!studentParentMenu || !studentParentMenu.children) {
        console.warn('未找到学员端父菜单或其子菜单')
        return []
      }

      // 提取学员端父菜单的子菜单作为顶部导航
      const studentMenus = studentParentMenu.children.filter(route => {
        // 跳过隐藏的菜单
        if (route.hidden) return false

        // 只显示菜单类型（有component的菜单）
        if (!route.component) return false

        // 检查菜单状态：只显示状态正常的菜单（status为'0'表示正常，'1'表示停用）
        if (route.meta && route.meta.status === '1') {
          return false // 过滤掉停用状态的菜单
        }

        // 检查权限：如果菜单配置了角色要求，检查用户是否有对应角色
        if (route.meta && route.meta.roles && route.meta.roles.length > 0) {
          const hasRole = userRoles.some(role => route.meta.roles.includes(role))
          if (!hasRole) return false
        }

        // 检查权限：如果菜单配置了权限要求，检查用户是否有对应权限
        if (route.meta && route.meta.permissions && route.meta.permissions.length > 0) {
          const hasPermission = this.permissions.some(permission =>
            route.meta.permissions.includes(permission)
          )
          if (!hasPermission) return false
        }

        return true
      })

      // 如果用户没有配置任何学员菜单权限，返回空数组
      if (studentMenus.length === 0) {
        console.warn('当前用户没有可访问的学员端菜单权限')
        return []
      }

      return studentMenus
    }
  },

  mounted() {
    // 检查当前路径，如果是根路径则重定向到学习大厅
    this.checkAndRedirectToDefault()
  },

  methods: {
    // 检查并重定向到默认页面（学习大厅）
    checkAndRedirectToDefault() {
      const currentPath = this.$route.path

      // 如果当前在学员端根路径，重定向到学习大厅
      if (currentPath === '/student' || currentPath === '/student/') {
        this.$nextTick(() => {
          this.$router.replace('/student/dashboard').catch(() => {
            // 如果路由不存在，可以创建一个默认页面
            console.warn('学习大厅路由不存在，请检查路由配置')
          })
        })
      }
    },

    // 获取菜单索引路径 - 用于菜单选中状态
    getMenuIndex(route) {
      // 对于学员端菜单，需要拼接完整路径
      if (route.path && !route.path.startsWith('/')) {
        // 相对路径需要拼接学员端根路径
        return `/student/${route.path}`
      }

      // 已经是完整路径直接返回
      return route.path || route.name
    },

    // 检查权限 - 基于若依权限框架
    hasPermission(route) {
      if (route.meta && route.meta.roles) {
        return this.roles.some(role => route.meta.roles.includes(role))
      }
      if (route.meta && route.meta.permissions) {
        return this.permissions.some(permission => route.meta.permissions.includes(permission))
      }
      return true
    },

    handleMenuSelect(key) {
      if (isExternal(key)) {
        window.open(key, '_blank')
      } else {
        // 确保路径正确
        let targetPath = key

        // 如果是相对路径，拼接学员端根路径
        if (key && !key.startsWith('/') && !key.startsWith('http')) {
          targetPath = `/student/${key}`
        }

        this.$router.push(targetPath).catch(err => {
          console.warn('路由跳转失败:', err)
        })
      }
    },

    handleUserCommand(command) {
      switch (command) {
        case 'profile':
          this.$router.push('/student/profile')
          break
        case 'logout':
          this.$modal.confirm('确定注销并退出系统吗？', '提示', {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
          }).then(() => {
            this.$store.dispatch('LogOut').then(() => {
              this.$router.push('/login')
            })
          })
          break
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.student-app-wrapper {
  min-height: 100vh;
  background: #f5f7fa;
}

.student-header {
  background: linear-gradient(135deg, #4285f4 0%, #34a853 100%);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  position: sticky;
  top: 0;
  z-index: 1000;
}

.header-container {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 64px;
  padding: 0 20px;
}

.header-brand {
  display: flex;
  align-items: center;
  color: white;

  .brand-logo {
    width: 40px;
    height: 40px;
    margin-right: 12px;
  }

  .brand-name {
    font-size: 20px;
    font-weight: 600;
    letter-spacing: 1px;
  }
}

.header-nav {
  flex: 1;
  display: flex;
  justify-content: center;

  .nav-menu {
    border: none;
    background: transparent !important;

    .el-menu-item {
      border-bottom: none !important;
      margin: 0 8px;
      border-radius: 8px;
      transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
      background: transparent !important;
      color: #ffffff !important;
      font-weight: 600;

      &:hover {
        background: rgba(255, 255, 255, 0.05) !important;
        color: #ffffff !important;
        transform: translateY(-1px);
      }

      &.is-active {
        background: transparent !important;
        color: #ffffff !important;
        font-weight: 700;
        transform: translateY(-2px);

        &::after {
          content: '';
          position: absolute;
          bottom: -2px;
          left: 50%;
          transform: translateX(-50%);
          width: 70%;
          height: 3px;
          background: rgba(255, 255, 255, 0.9);
          border-radius: 2px;
          box-shadow: 0 1px 3px rgba(255, 255, 255, 0.3);
        }
      }

      i {
        margin-right: 6px;
        font-size: 16px;
        color: #ffffff !important;
        font-weight: 600;
      }

      span {
        color: #ffffff !important;
        font-weight: inherit;
      }
    }
  }
}

.header-user {
  .user-info {
    display: flex;
    align-items: center;
    color: white;
    cursor: pointer;
    padding: 8px 12px;
    border-radius: 20px;
    transition: all 0.3s ease;

    &:hover {
      background-color: rgba(255, 255, 255, 0.1);
    }
  }

  .user-avatar {
    margin-right: 8px;
  }

  .user-name {
    margin-right: 8px;
    font-size: 14px;
  }
}

.student-main {
  min-height: calc(100vh - 64px);
  padding: 0;
}

// 页面过渡动画
.fade-transform-leave-active,
.fade-transform-enter-active {
  transition: all 0.3s;
}

.fade-transform-enter {
  opacity: 0;
  transform: translateX(-30px);
}

.fade-transform-leave-to {
  opacity: 0;
  transform: translateX(30px);
}

// 响应式设计
@media (max-width: 768px) {
  .header-container {
    padding: 0 15px;

    .header-nav {
      display: none;
    }

    .brand-name {
      display: none;
    }
  }
}
</style>
