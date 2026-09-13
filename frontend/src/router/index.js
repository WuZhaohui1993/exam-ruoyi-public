import Vue from 'vue'
import Router from 'vue-router'

Vue.use(Router)

/* Layout */
import Layout from '@/layout'
import StudentLayout from '@/layout/StudentLayout'

/**
 * Note: 路由配置项
 *
 * hidden: true                     // 当设置 true 的时候该路由不会再侧边栏出现 如401，login等页面，或者如一些编辑页面/edit/1
 * alwaysShow: true                 // 当你一个路由下面的 children 声明的路由大于1个时，自动会变成嵌套的模式--如组件页面
 *                                  // 只有一个时，会将那个子路由当做根路由显示在侧边栏--如引导页面
 *                                  // 若你想不管路由下面的 children 声明的个数都显示你的根路由
 *                                  // 你可以设置 alwaysShow: true，这样它就会忽略之前定义的规则，一直显示根路由
 * redirect: noRedirect             // 当设置 noRedirect 的时候该路由在面包屑导航中不可被点击
 * name:'router-name'               // 设定路由的名字，一定要填写不然使用<keep-alive>时会出现各种问题
 * query: '{"id": 1, "name": "ry"}' // 访问路由的默认传递参数
 * roles: ['admin', 'common']       // 访问路由的角色权限
 * permissions: ['a:a:a', 'b:b:b']  // 访问路由的菜单权限
 * meta : {
    noCache: true                   // 如果设置为true，则不会被 <keep-alive> 缓存(默认 false)
    title: 'title'                  // 设置该路由在侧边栏和面包屑中展示的名字
    icon: 'svg-name'                // 设置该路由的图标，对应路径src/assets/icons/svg
    breadcrumb: false               // 如果设置为false，则不会在breadcrumb面包屑中显示
    activeMenu: '/system/user'      // 当路由设置了该属性，则会高亮相对应的侧边栏。
  }
 */

// 公共路由
export const constantRoutes = [
  {
    path: '/redirect',
    component: Layout,
    hidden: true,
    children: [
      {
        path: '/redirect/:path(.*)',
        component: () => import('@/views/redirect')
      }
    ]
  },
  {
    path: '/login',
    component: () => import('@/views/login'),
    hidden: true
  },
  {
    path: '/register',
    component: () => import('@/views/register'),
    hidden: true
  },
  {
    path: '/404',
    component: () => import('@/views/error/404'),
    hidden: true
  },
  {
    path: '/401',
    component: () => import('@/views/error/401'),
    hidden: true
  },
  {
    path: '',
    component: Layout,
    redirect: 'index',
    children: [
      {
        path: 'index',
        component: () => import('@/views/index'),
        name: 'Index',
        meta: { title: '首页', icon: 'dashboard', affix: true }
      }
    ]
  },
  {
    path: '/user',
    component: Layout,
    hidden: true,
    redirect: 'noredirect',
    children: [
      {
        path: 'profile',
        component: () => import('@/views/system/user/profile/index'),
        name: 'Profile',
        meta: { title: '个人中心', icon: 'user' }
      }
    ]
  },
  // H5移动端路由（独立于管理端，无需Layout）
  {
    path: '/h5/login',
    component: () => import('@/views/h5/login/index'),
    hidden: true,
    meta: { title: 'H5登录', showHeader: false }
  },
  {
    path: '/h5/exam/:examId',
    component: () => import('@/views/h5/exam/index'),
    hidden: true,
    meta: { title: '考试详情', showHeader: true, showBack: false }
  },
  {
    path: '/h5/exam/:examId/taking',
    component: () => import('@/views/h5/exam/taking'),
    hidden: true,
    meta: { title: '答题', showHeader: false }
  },
  {
    path: '/h5/exam/:examId/result',
    component: () => import('@/views/h5/exam/result'),
    hidden: true,
    meta: { title: '考试成绩', showHeader: false }
  },
  {
    path: '/h5/exam/:examId/attachment',
    component: () => import('@/views/h5/exam/attachment'),
    hidden: true,
    meta: { title: '上传附件', showHeader: false }
  }

]

// 动态路由，基于用户权限动态去加载
export const dynamicRoutes = [
  {
    path: '/exam/paper/index',
    redirect: '/paper-manage/exam/paper',
    hidden: true,
    permissions: ['exam:paper:list']
  },
  {
    path: '/exam/category',
    redirect: '/question-manage/question-category',
    hidden: true,
    permissions: ['exam:category:list']
  },
  {
    path: '/exam/examination/category',
    component: Layout,
    hidden: true,
    permissions: ['exam:examination:category:list'],
    children: [
      {
        path: '',
        component: () => import('@/views/exam/examination/category/index'),
        name: 'ExamExaminationCategoryCompat',
        meta: { title: '考试分类', activeMenu: '/exam/examination/category' }
      }
    ]
  },
  {
    path: '/question-manage/exam/category',
    redirect: '/question-manage/question-category',
    hidden: true,
    permissions: ['exam:category:list']
  },
  {
    path: '/monitor/operlog',
    redirect: '/monitor/log/operlog',
    hidden: true,
    permissions: ['monitor:operlog:list']
  },
  {
    path: '/monitor/logininfor',
    redirect: '/monitor/log/logininfor',
    hidden: true,
    permissions: ['monitor:logininfor:list']
  },
  {
    path: '/student/exam/taking/:examId(\\d+)',
    redirect: to => ({
      path: `/student/exam-detail/taking/${to.params.examId}`,
      query: to.query
    }),
    hidden: true,
    permissions: ['student:exam:list']
  },
  {
    path: '/student/exam/result/:id(\\d+)',
    redirect: to => ({
      path: `/student/exam-detail/result/${to.params.id}`,
      query: to.query
    }),
    hidden: true,
    permissions: ['student:exam:list']
  },
  {
    path: '/student/practice',
    component: StudentLayout,
    hidden: true,
    permissions: ['student:training:list'],
    children: [
      {
        path: '',
        component: () => import('@/views/student/practice/index'),
        name: 'StudentPracticeCompat',
        meta: { title: '自主练习', activeMenu: '/student/training' }
      }
    ]
  },
  {
    path: '/training/manage',
    component: Layout,
    hidden: true,
    permissions: ['training:item:list'],
    children: [
      {
        path: '',
        component: () => import('@/views/training/admin/index'),
        name: 'TrainingAdminCompat',
        meta: { title: '培训运营', activeMenu: '/training/manage' }
      }
    ]
  },
  {
    path: '/student/training',
    component: StudentLayout,
    hidden: true,
    permissions: ['student:training:list'],
    children: [
      {
        path: '',
        component: () => import('@/views/student/training/index'),
        name: 'StudentTrainingCenterCompat',
        meta: { title: '学习中心', activeMenu: '/student/training' }
      }
    ]
  },
  {
    path: '/monitor/log/operlog',
    component: Layout,
    hidden: true,
    permissions: ['monitor:operlog:list'],
    children: [
      {
        path: '',
        component: () => import('@/views/monitor/operlog/index'),
        name: 'OperlogCompat',
        meta: { title: '操作日志', activeMenu: '/monitor/log/operlog' }
      }
    ]
  },
  {
    path: '/monitor/log/logininfor',
    component: Layout,
    hidden: true,
    permissions: ['monitor:logininfor:list'],
    children: [
      {
        path: '',
        component: () => import('@/views/monitor/logininfor/index'),
        name: 'LogininforCompat',
        meta: { title: '登录日志', activeMenu: '/monitor/log/logininfor' }
      }
    ]
  },
  {
    path: '/exam/examination/result',
    component: Layout,
    hidden: true,
    permissions: ['exam:examination:query'],
    children: [
      {
        path: '',
        component: () => import('@/views/exam/examination/result'),
        name: 'ExamResult',
        meta: { title: '考试成绩', activeMenu: '/exam/examination' }
      }
    ]
  },
  {
    path: '/exam/examination/statistics',
    redirect: to => ({
      path: '/exam/examination/score-statistics',
      query: to.query
    }),
    hidden: true,
    permissions: ['exam:examination:statistics']
  },
  {
    path: '/exam/examination/score-statistics',
    component: Layout,
    hidden: true,
    permissions: ['exam:examination:statistics'],
    children: [
      {
        path: '',
        component: () => import('@/views/exam/examination/score-statistics'),
        name: 'ScoreStatistics',
        meta: { title: '成绩统计', activeMenu: '/exam/examination' }
      }
    ]
  },
  {
    path: '/exam/examination/detail/:id(\\d+)',
    component: Layout,
    hidden: true,
    permissions: ['exam:examination:detail'],
    children: [
      {
        path: '',
        component: () => import('@/views/exam/examination/detail'),
        name: 'ExaminationDetail',
        meta: { title: '考试详情', activeMenu: '/exam/examination' }
      }
    ]
  },
  {
    path: '/exam/examination/users/:id(\\d+)',
    component: Layout,
    hidden: true,
    permissions: ['exam:examination:detail'],
    children: [
      {
        path: '',
        component: () => import('@/views/exam/examination/users'),
        name: 'ExaminationUsers',
        meta: { title: '考试人员', activeMenu: '/exam/examination' }
      }
    ]
  },
  {
    path: '/system/user-auth',
    component: Layout,
    hidden: true,
    permissions: ['system:user:edit'],
    children: [
      {
        path: 'role/:userId(\\d+)',
        component: () => import('@/views/system/user/authRole'),
        name: 'AuthRole',
        meta: { title: '分配角色', activeMenu: '/system/user' }
      }
    ]
  },
  {
    path: '/system/role-auth',
    component: Layout,
    hidden: true,
    permissions: ['system:role:edit'],
    children: [
      {
        path: 'user/:roleId(\\d+)',
        component: () => import('@/views/system/role/authUser'),
        name: 'AuthUser',
        meta: { title: '分配用户', activeMenu: '/system/role' }
      }
    ]
  },
  {
    path: '/system/dict-data',
    component: Layout,
    hidden: true,
    permissions: ['system:dict:list'],
    children: [
      {
        path: 'index/:dictId(\\d+)',
        component: () => import('@/views/system/dict/data'),
        name: 'Data',
        meta: { title: '字典数据', activeMenu: '/system/dict' }
      }
    ]
  },
  {
    path: '/monitor/job-log',
    component: Layout,
    hidden: true,
    permissions: ['monitor:job:list'],
    children: [
      {
        path: 'index/:jobId(\\d+)',
        component: () => import('@/views/monitor/job/log'),
        name: 'JobLog',
        meta: { title: '调度日志', activeMenu: '/monitor/job' }
      }
    ]
  },
  {
    path: '/tool/gen-edit',
    component: Layout,
    hidden: true,
    permissions: ['tool:gen:edit'],
    children: [
      {
        path: 'index/:tableId(\\d+)',
        component: () => import('@/views/tool/gen/editTable'),
        name: 'GenEdit',
        meta: { title: '修改生成配置', activeMenu: '/tool/gen' }
      }
    ]
  },
  // 试卷相关详情页面动态路由
  {
    path: '/exam/paper',
    component: Layout,
    hidden: true,
    redirect: '/paper-manage/exam/paper',
    permissions: ['exam:paper:list'],
    children: [
      {
        path: 'create/:type',
        component: () => import('@/views/exam/paper/create'),
        name: 'CreatePaper',
        meta: { title: '创建试卷', activeMenu: '/paper-manage/exam/paper' },
        permissions: ['exam:paper:add']
      },
      {
        path: 'edit/:id(\\d+)',
        component: () => import('@/views/exam/paper/edit'),
        name: 'EditPaper',
        meta: { title: '编辑试卷', activeMenu: '/paper-manage/exam/paper' },
        permissions: ['exam:paper:edit']
      },
      {
        path: 'fullscreen-preview/:id(\\d+)',
        component: () => import('@/views/exam/paper/fullscreen-preview'),
        name: 'FullscreenPreview',
        meta: { title: '试卷预览', activeMenu: '/paper-manage/exam/paper' },
        permissions: ['exam:paper:query']
      }
    ]
  },
  {
    path: '/paper-manage/exam/paper/create/:type',
    component: Layout,
    hidden: true,
    permissions: ['exam:paper:add'],
    children: [
      {
        path: '',
        component: () => import('@/views/exam/paper/create'),
        name: 'CreatePaperRealPath',
        meta: { title: '创建试卷', activeMenu: '/paper-manage/exam/paper' },
        permissions: ['exam:paper:add']
      }
    ]
  },
  {
    path: '/paper-manage/exam/paper/edit/:id(\\d+)',
    component: Layout,
    hidden: true,
    permissions: ['exam:paper:edit'],
    children: [
      {
        path: '',
        component: () => import('@/views/exam/paper/edit'),
        name: 'EditPaperRealPath',
        meta: { title: '编辑试卷', activeMenu: '/paper-manage/exam/paper' },
        permissions: ['exam:paper:edit']
      }
    ]
  },
  {
    path: '/paper-manage/exam/paper/fullscreen-preview/:id(\\d+)',
    component: Layout,
    hidden: true,
    permissions: ['exam:paper:query'],
    children: [
      {
        path: '',
        component: () => import('@/views/exam/paper/fullscreen-preview'),
        name: 'FullscreenPreviewRealPath',
        meta: { title: '试卷预览', activeMenu: '/paper-manage/exam/paper' },
        permissions: ['exam:paper:query']
      }
    ]
  },
  //学员端考试相关详情页面动态路由
  {
    path: '/student/exam-detail',
    component: StudentLayout,
    hidden: true,
    redirect: 'noredirect',
    permissions: ['student:exam:list'],
    children: [
      {
        path: 'taking/:examId(\\d+)',
        component: () => import('@/views/student/exam/taking'),
        name: 'StudentExamTaking',
        meta: { title: '参加考试', activeMenu: '/student/exam' },
        permissions: ['student:exam:start']
      },
      {
        path: 'result/:id(\\d+)',
        component: () => import('@/views/student/exam/result'),
        name: 'StudentExamResult',
        meta: { title: '考试结果', activeMenu: '/student/exam' },
        permissions: ['student:exam:result']
      }
    ]
  }
]

// 防止连续点击多次路由报错
let routerPush = Router.prototype.push
let routerReplace = Router.prototype.replace
// push
Router.prototype.push = function push(location) {
  return routerPush.call(this, location).catch(err => err)
}
// replace
Router.prototype.replace = function push(location) {
  return routerReplace.call(this, location).catch(err => err)
}

export default new Router({
  mode: 'history', // 去掉url中的#
  scrollBehavior: () => ({ y: 0 }),
  routes: constantRoutes
})
