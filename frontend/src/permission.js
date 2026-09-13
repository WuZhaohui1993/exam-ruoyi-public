import router from './router'
import store from './store'
import { Message } from 'element-ui'
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'
import { getToken } from '@/utils/auth'
import { isPathMatch } from '@/utils/validate'
import { isRelogin } from '@/utils/request'

NProgress.configure({ showSpinner: false })

const whiteList = ['/login', '/register', '/h5/login']

// H5移动端路由（独立于管理端，有独立的权限处理逻辑）
const h5Routes = ['/h5']

// 学员端路由路径
const studentRoutes = ['/student']

// 判断是否为H5移动端路由
const isH5Route = (path) => {
  return h5Routes.some(route => path.startsWith(route))
}

// 判断是否为学员端路由
const isStudentRoute = (path) => {
  return studentRoutes.some(route => path.startsWith(route))
}

const isWhiteList = (path) => {
  return whiteList.some(pattern => isPathMatch(pattern, path))
}

const isRouteRegistered = (route) => {
  if (!route || !route.path) return false
  return (router.options.routes || []).some(item => item.path === route.path)
}

const markRoutesRegistered = (routes) => {
  router.options.routes = router.options.routes || []
  routes.forEach(route => {
    if (!isRouteRegistered(route)) {
      router.options.routes.push(route)
    }
  })
}

const addRoutesOnce = (routes) => {
  const pendingRoutes = (routes || []).filter(route => !isRouteRegistered(route))
  if (pendingRoutes.length > 0) {
    router.addRoutes(pendingRoutes)
    markRoutesRegistered(pendingRoutes)
  }
}

router.beforeEach((to, from, next) => {
  NProgress.start()
  if (getToken()) {
    to.meta.title && store.dispatch('settings/setTitle', to.meta.title)
    /* has token*/
    if (to.path === '/login') {
      next({ path: '/' })
      NProgress.done()
    } else if (to.path === '/h5/login') {
      // H5扫码入口允许重新登录，避免沿用同浏览器内的管理端或其他学员 token。
      next()
      NProgress.done()
    } else if (isH5Route(to.path)) {
      // H5移动端路由，已有token直接放行，不需要获取管理端用户信息
      next()
    } else if (isWhiteList(to.path)) {
      next()
    } else {
      if (store.getters.roles.length === 0) {
        isRelogin.show = true
        // 判断当前用户是否已拉取完user_info信息
        store.dispatch('GetInfo').then(() => {
          isRelogin.show = false
          const roles = store.getters.roles || []

          // 检查角色和路由匹配
          if (isStudentRoute(to.path)) {
            // 访问学员端路由，检查是否有学员权限
            if (!roles.includes('student') && !roles.includes('学员')) {
              // 非学员角色访问学员端，重定向到管理端
              next({ path: '/' })
              return
            }
          } else {
            // 访问管理端路由，检查是否有管理权限
            if (roles.includes('student') && !roles.includes('admin') && !roles.includes('common')) {
              // 学员角色访问管理端，重定向到学员端
              next({ path: '/student/dashboard' })
              return
            }
          }

          store.dispatch('GenerateRoutes').then(accessRoutes => {
            // 根据roles权限生成可访问的路由表
            addRoutesOnce(accessRoutes) // 动态添加可访问路由表
            next({ ...to, replace: true }) // hack方法 确保addRoutes已完成
          })
        }).catch(err => {
          store.dispatch('LogOut').then(() => {
            Message.error(err)
            next({ path: '/' })
          })
        })
      } else {
        // 已有用户信息，检查角色和路由匹配
        const roles = store.getters.roles || []

        // 检查是否需要重新生成路由（解决角色切换问题）
        const currentRoutes = store.getters.permission_routes || []
        const hasRoutes = currentRoutes.length > 0
        const sidebarRouters = store.getters.sidebarRouters || []

        // 如果没有路由或者当前路由表为空，优先重新生成路由，避免刷新循环导致白屏
        if (!hasRoutes || sidebarRouters.length === 0) {
          store.dispatch('GenerateRoutes').then(accessRoutes => {
            addRoutesOnce(accessRoutes)
            next({ ...to, replace: true })
          }).catch(() => {
            // 生成路由失败，退出登录
            store.dispatch('LogOut').then(() => {
              next({ path: '/login' })
            })
          })
          return
        }

        if (isStudentRoute(to.path)) {
          // 访问学员端路由，检查是否有学员权限
          if (!roles.includes('student') && !roles.includes('学员')) {
            next({ path: '/' })
            return
          }
        } else {
          // 访问管理端路由，检查是否有管理权限
          if (roles.includes('student') && !roles.includes('admin') && !roles.includes('common')) {
            next({ path: '/student/dashboard' })
            return
          }
        }

        next()
      }
    }
  } else {
    // 没有token
    if (isWhiteList(to.path)) {
      // 在免登录白名单，直接进入
      next()
    } else if (isH5Route(to.path)) {
      // H5移动端路由，跳转到H5登录页，并携带考试ID
      const examIdMatch = to.path.match(/\/h5\/exam\/(\d+)/)
      if (examIdMatch) {
        next(`/h5/login?examId=${examIdMatch[1]}`)
      } else {
        next('/h5/login')
      }
      NProgress.done()
    } else {
      next(`/login?redirect=${encodeURIComponent(to.fullPath)}`) // 否则全部重定向到登录页
      NProgress.done()
    }
  }
})

router.afterEach(() => {
  NProgress.done()
})
