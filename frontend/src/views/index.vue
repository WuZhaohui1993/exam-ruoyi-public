<template>
  <div class="admin-dashboard">
    <!-- 欢迎横幅 -->
    <div class="welcome-banner">
      <div class="welcome-content">
        <h1 class="welcome-title">下午好，{{ nickName }}</h1>
        <p class="welcome-desc">欢迎回来！今天又是充满活力的一天，让我们开始管理考试吧。</p>
        <div class="welcome-time">
          <i class="el-icon-date"></i>
          <span>{{ currentTime }}</span>
        </div>
      </div>
      <div class="welcome-image">
        <div class="banner-circle"></div>
      </div>
    </div>

    <!-- 顶部统计卡片 -->
    <div class="statistics-panel">
      <el-row :gutter="20">
        <el-col :xs="12" :sm="12" :lg="6" class="stat-card-col">
          <div class="stat-card gradient-blue" @click="handleNavigationWithPermission('/exam/examination', 'exam:examination:list')">
            <div class="stat-icon-wrapper">
              <i class="el-icon-document"></i>
            </div>
            <div class="stat-content">
              <div class="stat-title">考试总数</div>
              <div class="stat-number">
                <count-to :start-val="0" :end-val="statistics.examCount" :duration="2000" />
              </div>
            </div>
          </div>
        </el-col>
        <el-col :xs="12" :sm="12" :lg="6" class="stat-card-col">
          <div class="stat-card gradient-purple" @click="handleNavigationWithPermission('/exam/question', 'exam:question:list')">
            <div class="stat-icon-wrapper">
              <i class="el-icon-edit-outline"></i>
            </div>
            <div class="stat-content">
              <div class="stat-title">试题总数</div>
              <div class="stat-number">
                <count-to :start-val="0" :end-val="statistics.questionCount" :duration="2000" />
              </div>
            </div>
          </div>
        </el-col>
        <el-col :xs="12" :sm="12" :lg="6" class="stat-card-col">
          <div class="stat-card gradient-green" @click="handleNavigationWithPermission('/system/user', 'system:user:list')">
            <div class="stat-icon-wrapper">
              <i class="el-icon-user"></i>
            </div>
            <div class="stat-content">
              <div class="stat-title">用户总数</div>
              <div class="stat-number">
                <count-to :start-val="0" :end-val="statistics.userCount" :duration="2000" />
              </div>
            </div>
          </div>
        </el-col>
        <el-col :xs="12" :sm="12" :lg="6" class="stat-card-col">
          <div class="stat-card gradient-orange" @click="handleNavigationWithPermission('/monitor/online', 'monitor:online:list')">
            <div class="stat-icon-wrapper">
              <i class="el-icon-video-play"></i>
            </div>
            <div class="stat-content">
              <div class="stat-title">在线用户</div>
              <div class="stat-number">
                <count-to :start-val="0" :end-val="statistics.onlineCount" :duration="2000" />
              </div>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- 中间：快捷管理与最新通知 (对称式布局) -->
    <el-row :gutter="20" class="middle-row">
      <!-- 快捷管理 -->
      <el-col :xs="24" :sm="24" :lg="16">
        <el-card class="section-card quick-actions-card">
          <div slot="header" class="section-header">
            <div class="section-title">
              <i class="el-icon-menu"></i>
              <span>快捷管理</span>
            </div>
          </div>
          <el-row :gutter="20">
            <el-col :xs="8" :sm="6" :md="4" :lg="4" v-for="action in quickActions" :key="action.key">
              <div 
                v-if="!action.perms || checkPermission(action.perms)"
                class="quick-action-item" 
                @click="handleQuickActionClick(action)"
              >
                <div class="action-icon-box" :style="{ background: action.color }">
                  <svg-icon v-if="!action.icon.startsWith('el-icon-')" :icon-class="action.icon" />
                  <i v-else :class="action.icon"></i>
                </div>
                <div class="action-text text-ellipsis">{{ action.title }}</div>
              </div>
            </el-col>
          </el-row>
        </el-card>
      </el-col>

      <!-- 最新公告 -->
      <el-col :xs="24" :sm="24" :lg="8">
        <el-card class="section-card notice-card">
          <div slot="header" class="section-header">
            <div class="section-title">
              <i class="el-icon-bell"></i>
              <span>最新公告</span>
            </div>
            <el-button type="text" class="section-action" @click="handleNavigation('/system/notice')">
              更多
            </el-button>
          </div>
          <div class="notice-list">
            <div v-if="noticeList.length === 0" class="empty-state-mini">
              <p>暂无公告</p>
            </div>
            <div v-for="notice in noticeList" :key="notice.noticeId" class="notice-item mini">
              <div class="notice-title text-ellipsis">{{ notice.noticeTitle }}</div>
              <div class="notice-time">{{ formatDate(notice.createTime) }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 底部区域：考试安排与题型分布 -->
    <el-row :gutter="20" class="bottom-row">
      <el-col :xs="24" :sm="24" :lg="16">
        <el-card class="section-card exam-card">
          <div slot="header" class="section-header">
            <div class="section-title">
              <i class="el-icon-date"></i>
              <span>近期考试安排</span>
            </div>
            <el-button type="text" class="section-action" @click="handleNavigation('/exam/examination')">
              查看全部
            </el-button>
          </div>
          <div class="exam-list">
            <div v-if="recentExams.length === 0" class="empty-state">
              <i class="el-icon-info"></i>
              <p>暂无近期考试安排</p>
            </div>
            <div v-for="exam in recentExams" :key="exam.id" class="exam-item">
              <div class="exam-info">
                <div class="exam-title text-ellipsis" style="text-align: left;">{{ exam.examName }}</div>
                <div class="exam-meta">
                  <span><i class="el-icon-time"></i> {{ formatTime(exam.startTime) }}</span>
                  <span><i class="el-icon-user"></i> {{ exam.registeredCount || 0 }} 人报名</span>
                </div>
              </div>
              <div class="exam-status">
                <el-tag :type="getExamStatusType(exam.status)" effect="light">
                  {{ getExamStatusText(exam.status) }}
                </el-tag>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :sm="24" :lg="8">
        <el-card class="section-card chart-card">
          <div slot="header" class="section-header">
            <div class="section-title">
              <i class="el-icon-pie-chart"></i>
              <span>题库题型分布</span>
            </div>
          </div>
          <div class="chart-wrapper" style="height: 300px;">
            <pie-chart :chart-data="pieChartData" />
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 业务概览 -->
    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="24">
        <el-card class="section-card stats-overview-card">
          <div slot="header" class="section-header">
            <div class="section-title">
              <i class="el-icon-document-copy"></i>
              <span>本月业务概览</span>
            </div>
          </div>
          <el-row :gutter="20">
            <el-col :xs="24" :sm="8">
              <div class="overview-item-inline">
                <span class="label">今日新增考试</span>
                <span class="value">{{ examStats.todayExams }}</span>
              </div>
            </el-col>
            <el-col :xs="24" :sm="8">
              <div class="overview-item-inline">
                <span class="label">本周计划考试</span>
                <span class="value">{{ examStats.weekExams }}</span>
              </div>
            </el-col>
            <el-col :xs="24" :sm="8">
              <div class="overview-item-inline">
                <span class="label">本月累计考试</span>
                <span class="value">{{ examStats.monthExams }}</span>
              </div>
            </el-col>
          </el-row>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>



<script>
import { listExamination, getBusinessStatistics } from "@/api/exam/examination";
import { listNotice } from "@/api/system/notice";
import { countQuestions, getQuestionTypeStatistics } from "@/api/exam/question";
import { listUser } from "@/api/system/user";
import { list as listOnline } from "@/api/monitor/online";
import { getQuickAccessMenus } from "@/api/system/menu";

import { parseTime } from "@/utils/ruoyi";
import CountTo from 'vue-count-to';
import PieChart from './dashboard/PieChart';

export default {
  name: "AdminDashboard",
  components: {
    CountTo,
    PieChart
  },
  data() {
    return {
      currentTime: '',
      statistics: {
        examCount: 0,
        questionCount: 0,
        userCount: 0,
        onlineCount: 0
      },
      quickActions: [],
      recentExams: [],
      noticeList: [],
      pieChartData: [],
      examStats: {
        todayExams: 0,
        weekExams: 0,
        monthExams: 0
      },
      examStatusDict: {
        '0': { text: '草稿', type: 'info' },
        '1': { text: '已发布', type: 'success' },
        '2': { text: '进行中', type: 'primary' },
        '3': { text: '已结束', type: 'warning' },
        '4': { text: '已取消', type: 'danger' }
      }
    };
  },
  computed: {
    nickName() {
      return this.$store.getters.nickName;
    }
  },
  created() {
    this.updateTime();
    this.timer = setInterval(this.updateTime, 60000);
    this.loadDashboardData();
  },
  destroyed() {
    if (this.timer) {
      clearInterval(this.timer);
    }
  },

  methods: {
    async loadDashboardData() {
      try {
        await Promise.all([
          this.loadStatistics(),
          this.loadQuickActions(),
          this.loadRecentExams(),
          this.loadNotices(),
          this.loadExamStats(),
          this.loadQuestionTypeStats()
        ]);
      } catch (error) {
        console.error('加载数据失败:', error);
      }
    },
    
    updateTime() {
      const now = new Date();
      const options = { year: 'numeric', month: 'long', day: 'numeric', weekday: 'long' };
      this.currentTime = now.toLocaleDateString('zh-CN', options);
    },

    
    async loadQuickActions() {
      try {
        // 调用专门的快捷管理菜单API
        const response = await getQuickAccessMenus();
        const quickMenus = response.data || [];
        
        this.quickActions = quickMenus.map(menu => ({
          key: menu.menuId,
          title: menu.menuName,
          icon: menu.quickIcon || menu.icon || 'dashboard',
          color: menu.quickColor || 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
          path: this.buildMenuFullPath(menu),
          perms: menu.perms,
          query: menu.query,
          component: menu.component,
          isFrame: menu.isFrame,
          quickOrder: menu.quickOrder || 0
        })).sort((a, b) => a.quickOrder - b.quickOrder);
        
        // 如果没有快捷管理菜单，使用默认配置
        if (this.quickActions.length === 0) {
          this.quickActions = [
            {
              key: 'user',
              title: '用户管理',
              icon: 'user',
              color: 'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)',
              path: '/system/user',
              perms: 'system:user:list'
            },
            {
              key: 'paper',
              title: '试卷管理',
              icon: 'documentation',
              color: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
              path: '/paper-manage/exam/paper',
              perms: 'exam:paper:list'
            },
            {
              key: 'question',
              title: '试题管理',
              icon: 'edit',
              color: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)',
              path: '/exam/question',
              perms: 'exam:question:list'
            },
            {
              key: 'exam',
              title: '考试管理',
              icon: 'list',
              color: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)',
              path: '/exam/examination',
              perms: 'exam:examination:list'
            }
          ];
        }
      } catch (error) {
        console.error('加载快捷菜单失败:', error);
        // 使用默认配置
        this.quickActions = [
          {
            key: 'user',
            title: '用户管理',
            icon: 'user',
            color: 'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)',
            path: '/system/user',
            perms: 'system:user:list'
          },
          {
            key: 'paper',
            title: '试卷管理',
            icon: 'documentation',
            color: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
            path: '/paper-manage/exam/paper',
            perms: 'exam:paper:list'
          },
          {
            key: 'question',
            title: '试题管理',
            icon: 'edit',
            color: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)',
            path: '/exam/question',
            perms: 'exam:question:list'
          },
          {
            key: 'exam',
            title: '考试管理',
            icon: 'list',
            color: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)',
            path: '/exam/examination',
            perms: 'exam:examination:list'
          }
        ];
      }
    },
    

    
    // 构建完整的菜单路径，考虑父级菜单路径
    buildMenuFullPath(menu) {
      if (!menu.path) return '/';
      
      // 如果是外链，直接返回完整路径
      if (this.isHttp(menu.path)) {
        return menu.path;
      }
      
      let path = menu.path;
      
      // 如果路径已经是绝对路径，直接返回
      if (path.startsWith('/')) {
        return path;
      }
      
      // 如果有父级路径，构建完整路径：parentPath/currentPath
      if (menu.parentPath && menu.parentPath.trim() !== '') {
        // 确保路径以 / 开头
        let fullPath = '/' + menu.parentPath + '/' + path;
        // 清理多余的 /
        return fullPath.replace(/\/+/g, '/');
      }
      
      // 否则使用原来的逻辑作为后备
      return this.getMenuPath(menu);
    },

    getMenuPath(menu) {
      // 处理菜单路径，与框架菜单跳转逻辑保持一致
      if (!menu.path) return '/';
      
      // 如果是外链，直接返回完整路径
      if (this.isHttp(menu.path)) {
        return menu.path;
      }
      
      let path = menu.path;
      
      // 如果路径已经是绝对路径，直接返回
      if (path.startsWith('/')) {
        return path;
      }
      
      // 根据菜单组件路径构建完整路径
      if (menu.component) {
        // 系统管理菜单
        if (menu.component.startsWith('system/') && !path.startsWith('system/')) {
          return '/system/' + path;
        }
        // 考试管理菜单
        else if (menu.component.startsWith('exam/')) {
          // 如果路径已经包含exam前缀，直接添加/
          if (path.startsWith('exam/')) {
            return '/' + path;
          } else {
            return '/exam/' + path;
          }
        }
        // 监控管理菜单
        else if (menu.component.startsWith('monitor/') && !path.startsWith('monitor/')) {
          return '/monitor/' + path;
        }
        // 学员端菜单
        else if (menu.component.startsWith('student/') && !path.startsWith('student/')) {
          return '/student/' + path;
        }
        // 工具菜单
        else if (menu.component.startsWith('tool/') && !path.startsWith('tool/')) {
          return '/tool/' + path;
        }
      }
      
      // 默认情况下，确保路径以 / 开头
      return path.startsWith('/') ? path : '/' + path;
    },
    
    // 判断是否为外链
    isHttp(url) {
      return url && (url.indexOf('http://') !== -1 || url.indexOf('https://') !== -1);
    },
    
    async loadStatistics() {
      try {
        const [examRes, questionRes, userRes, onlineRes] = await Promise.all([
          listExamination({ pageNum: 1, pageSize: 1 }),
          countQuestions(),
          listUser({ pageNum: 1, pageSize: 1 }),
          listOnline({ pageNum: 1, pageSize: 1 })
        ]);
        
        this.statistics = {
          examCount: examRes.total || 0,
          questionCount: questionRes.data || 0,
          userCount: userRes.total || 0,
          onlineCount: onlineRes.total || 0
        };
      } catch (error) {
        console.error('加载统计数据失败:', error);
      }
    },
    
    async loadRecentExams() {
      try {
        const response = await listExamination({ 
          pageNum: 1, 
          pageSize: 5,
          orderByColumn: 'create_time',
          isAsc: 'desc'
        });
        this.recentExams = response.rows || [];
      } catch (error) {
        console.error('加载近期考试失败:', error);
      }
    },
    
    async loadNotices() {
      try {
        const response = await listNotice({ 
          pageNum: 1, 
          pageSize: 4,
          orderByColumn: 'create_time',
          isAsc: 'desc'
        });
        this.noticeList = response.rows || [];
      } catch (error) {
        console.error('加载通知公告失败:', error);
      }
    },

    
    async loadExamStats() {
      try {
        const response = await getBusinessStatistics();
        if (response.data) {
          this.examStats = {
            todayExams: response.data.todayExams || 0,
            weekExams: response.data.weekExams || 0,
            monthExams: response.data.monthExams || 0
          };
        }
      } catch (error) {
        console.error('加载考试统计失败:', error);
      }
    },
    
    async loadQuestionTypeStats() {
      try {
        const response = await getQuestionTypeStatistics();
        const data = response.data;
        const typeMap = {
          'single': '单选题',
          'multiple': '多选题',
          'judge': '判断题',
          'fill': '填空题',
          'essay': '简答题'
        };
        const pieData = Object.keys(data).map(key => ({
          name: typeMap[key] || key,
          value: data[key]
        }));
        this.pieChartData = pieData;
      } catch (error) {
        console.error('加载题型统计失败:', error);
      }
    },
    
    handleNavigation(path, action) {
      // 使用与框架菜单一致的跳转逻辑
      if (action) {
        this.handleQuickActionClick(action);
      } else {
        this.$router.push(path);
      }
    },
    
    // 带权限检查的路由跳转方法
    handleNavigationWithPermission(path, permission) {
      // 检查权限
      if (!this.checkPermission(permission)) {
        this.$message.warning('您没有访问该页面的权限');
        return;
      }
      
      // 使用与框架菜单一致的跳转逻辑
      this.$router.push(path).catch(err => {
        if (err.name === 'NavigationDuplicated') {
          // 如果是重复导航，忽略错误
          return;
        }
        console.error('路由跳转失败:', err);
        this.$message.error('页面跳转失败，请稍后重试');
      });
    },
    
    // 快捷管理项点击处理，与框架菜单跳转逻辑保持一致
    handleQuickActionClick(action) {
      // 检查权限
      if (action.perms && !this.checkPermission(action.perms)) {
        this.$message.warning('您没有访问该页面的权限');
        return;
      }
      
      const path = action.path;
      
      if (this.isHttp(path)) {
        // 外链新窗口打开
        window.open(path, '_blank');
      } else {
        // 内部路由跳转
        if (action.query) {
          let query = {};
          try {
            query = typeof action.query === 'string' ? JSON.parse(action.query) : action.query;
          } catch (e) {
            console.warn('解析query参数失败:', action.query);
          }
          this.$router.push({ path: path, query: query }).catch(err => {
            if (err.name === 'NavigationDuplicated') {
              return;
            }
            console.error('路由跳转失败:', err);
            this.$message.error('页面跳转失败，请稍后重试');
          });
        } else {
          this.$router.push({ path: path }).catch(err => {
            if (err.name === 'NavigationDuplicated') {
              return;
            }
            console.error('路由跳转失败:', err);
            this.$message.error('页面跳转失败，请稍后重试');
          });
        }
      }
    },
    
    // 获取快捷管理菜单
    getQuickAccessMenus() {
      return getQuickAccessMenus();
    },
    
    
    getExamStatusText(status) {
      return this.examStatusDict[status] ? this.examStatusDict[status].text : '未知';
    },
    
    getExamStatusType(status) {
      return this.examStatusDict[status] ? this.examStatusDict[status].type : 'info';
    },
    

    
    formatTime(time) {
      return parseTime(time, '{y}-{m}-{d} {h}:{i}');
    },
    
    formatDate(time) {
      return parseTime(time, '{y}-{m}-{d}');
    },

    checkPermission(permission) {
      if (!permission) return true;
      const permissions = this.$store.getters.permissions;
      const roles = this.$store.getters.roles;
      // 管理员角色或拥有通配符权限时直接返回 true
      if (roles.includes('admin') || permissions.includes('*:*:*')) {
        return true;
      }
      return permissions.includes(permission);
    }
  }
};
</script>

<style lang="scss" scoped>
.admin-dashboard {
  padding: 24px;
  background-color: #f0f2f5;
  min-height: calc(100vh - 84px);
}

// 欢迎横幅
.welcome-banner {
  background: linear-gradient(135deg, #2b32b2 0%, #1488cc 100%);
  border-radius: 16px;
  padding: 30px 40px;
  color: white;
  display: flex;
  justify-content: space-between;
  align-items: center;
  position: relative;
  overflow: hidden;
  margin-bottom: 24px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);

  .welcome-content {
    position: relative;
    z-index: 2;
    
    .welcome-title {
      font-size: 28px;
      margin: 0 0 10px 0;
      font-weight: 600;
    }
    
    .welcome-desc {
      font-size: 16px;
      opacity: 0.9;
      margin: 0 0 20px 0;
    }

    .welcome-time {
      display: flex;
      align-items: center;
      font-size: 14px;
      background: rgba(255, 255, 255, 0.2);
      padding: 6px 12px;
      border-radius: 20px;
      width: fit-content;
      
      i {
        margin-right: 8px;
      }
    }
  }

  .welcome-image {
    position: relative;
    z-index: 2;
    
    .banner-circle {
      width: 150px;
      height: 150px;
      background: rgba(255, 255, 255, 0.1);
      border-radius: 50%;
      position: absolute;
      right: -20px;
      top: -50px;
    }
  }

  &::after {
    content: '';
    position: absolute;
    right: -5%;
    bottom: -20%;
    width: 300px;
    height: 300px;
    background: radial-gradient(circle, rgba(255,255,255,0.15) 0%, transparent 70%);
    border-radius: 50%;
  }
}

// 统计面板
.stat-card-col {
  margin-bottom: 24px;
}

.stat-card {
  height: 100px;
  border-radius: 16px;
  padding: 0 24px;
  display: flex;
  align-items: center;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
  color: white;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);

  &:hover {
    transform: translateY(-5px);
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
  }

  &.gradient-blue { background: linear-gradient(135deg, #1890ff 0%, #36cfc9 100%); }
  &.gradient-purple { background: linear-gradient(135deg, #722ed1 0%, #b37feb 100%); }
  &.gradient-green { background: linear-gradient(135deg, #52c41a 0%, #95de64 100%); }
  &.gradient-orange { background: linear-gradient(135deg, #fa8c16 0%, #ffd666 100%); }

  .stat-icon-wrapper {
    width: 48px;
    height: 48px;
    background: rgba(255, 255, 255, 0.2);
    border-radius: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-right: 16px;

    i {
      font-size: 24px;
    }
  }

  .stat-content {
    .stat-title {
      font-size: 14px;
      opacity: 0.85;
      margin-bottom: 4px;
    }
    
    .stat-number {
      font-size: 24px;
      font-weight: 700;
      font-family: 'PingFang SC', 'Helvetica Neue', Arial, sans-serif;
    }
  }
}

// 图表区域
.chart-row {
  margin-bottom: 24px;
}

.chart-card {
  .chart-wrapper {
    height: 380px;
    padding: 10px;
  }
}

.middle-row {
  margin-bottom: 24px;
  display: flex;
  flex-wrap: wrap;

  .el-col {
    display: flex;
  }

  .section-card {
    flex: 1;
    display: flex;
    flex-direction: column;

    .el-card__body {
      flex: 1;
      display: flex;
      flex-direction: column;
    }
  }
}

.notice-card {
  .notice-list {
    flex: 1;
    height: 160px; // 设定一个合适的基础高度
    overflow-y: auto;
  }
}

.quick-actions-card {
  .el-card__body {
    justify-content: center;
  }
}

.text-ellipsis {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  width: 100%;
  text-align: center;
}

.notice-item.mini {
  padding: 10px 0;
  .notice-title {
    text-align: left;
    font-size: 13px;
  }
}

.bottom-row {
  margin-bottom: 24px;
  display: flex;
  flex-wrap: wrap;

  .el-col {
    display: flex;
  }

  .section-card {
    flex: 1;
    display: flex;
    flex-direction: column;

    .el-card__body {
      flex: 1;
      display: flex;
      flex-direction: column;
    }
  }
}

.exam-card {
  .exam-list {
    flex: 1;
    min-height: 300px; // 与图表高度匹配
  }
}

.empty-state-mini {
  text-align: center;
  padding: 30px 0;
  color: #999;
  font-size: 13px;
}

// 通用卡片样式重写
.section-card {
  border: none;
  border-radius: 16px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05) !important;
  
  .el-card__header {
    padding: 16px 20px;
    border-bottom: 1px solid #f0f0f0;
  }

  .section-header {
    display: flex;
    align-items: center;
    justify-content: space-between;

    .section-title {
      display: flex;
      align-items: center;
      font-size: 16px;
      font-weight: 600;
      color: #262626;

      i {
        margin-right: 8px;
        color: #1890ff;
        font-size: 18px;
      }
    }

    .section-action {
      font-size: 14px;
      font-weight: 500;
    }
  }
}

// 快捷操作
.quick-action-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 16px;
  border-radius: 12px;
  cursor: pointer;
  transition: background 0.2s;
  margin-bottom: 0;

  &:hover {
    background: #f5f5f5;
    
    .action-icon-box {
      transform: scale(1.1);
    }
  }

  .action-icon-box {
    width: 48px;
    height: 48px;
    border-radius: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-bottom: 12px;
    transition: transform 0.2s;
    box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);

    i {
      font-size: 20px;
      color: white;
    }
    
    .svg-icon {
      width: 20px;
      height: 20px;
      color: white !important;
    }
  }

  .action-text {
    font-size: 14px;
    color: #595959;
    font-weight: 500;
  }
}

// 考试列表
.exam-list {
  .exam-item {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 18px 0;
    border-bottom: 1px solid #f0f0f0;
    
    &:last-child {
      border-bottom: none;
    }
    
    .exam-info {
      .exam-title {
        font-size: 15px;
        font-weight: 500;
        color: #262626;
        margin-bottom: 8px;
      }
      
      .exam-meta {
        font-size: 12px;
        color: #8c8c8c;
        
        span {
          margin-right: 16px;
          display: inline-flex;
          align-items: center;
          
          i {
            margin-right: 4px;
          }
        }
      }
    }
  }
}

// 通知列表
.notice-list {
  .notice-item {
    padding: 14px 0;
    border-bottom: 1px solid #f0f0f0;
    
    &:last-child {
      border-bottom: none;
    }
    
    .notice-title {
      font-size: 14px;
      color: #434343;
      margin-bottom: 6px;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
      cursor: pointer;
      
      &:hover {
        color: #1890ff;
      }
    }
    
    .notice-time {
      font-size: 12px;
      color: #bfbfbf;
    }
  }
}

.overview-item-inline {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 20px;
  background: #f9f9f9;
  border-radius: 8px;
  margin-bottom: 10px;
  
  .label {
    font-size: 14px;
    color: #595959;
  }
  
  .value {
    font-size: 18px;
    font-weight: 600;
    color: #1890ff;
  }
}

// 响应式优化
@media (max-width: 992px) {
  .welcome-banner {
    flex-direction: column;
    text-align: center;
    padding: 24px;
    
    .welcome-content {
      margin-bottom: 20px;
      .welcome-time { margin: 0 auto; }
    }
  }
}
</style>
