import auth from '@/plugins/auth'
import router, { constantRoutes, dynamicRoutes } from '@/router'
import { getRouters } from '@/api/menu'
import Layout from '@/layout/index'
import StudentLayout from '@/layout/StudentLayout'
import ParentView from '@/components/ParentView'
import InnerLink from '@/layout/components/InnerLink'
import store from '@/store'

const permission = {
  state: {
    routes: [],
    addRoutes: [],
    defaultRoutes: [],
    topbarRouters: [],
    sidebarRouters: []
  },
  mutations: {
    SET_ROUTES: (state, routes) => {
      state.addRoutes = routes
      state.routes = constantRoutes.concat(routes)
    },
    SET_DEFAULT_ROUTES: (state, routes) => {
      state.defaultRoutes = constantRoutes.concat(routes)
    },
    SET_TOPBAR_ROUTES: (state, routes) => {
      state.topbarRouters = routes
    },
    SET_SIDEBAR_ROUTERS: (state, routes) => {
      state.sidebarRouters = routes
    },
    CLEAR_ROUTES: (state) => {
      state.addRoutes = []
      state.routes = []
      state.defaultRoutes = []
      state.topbarRouters = []
      state.sidebarRouters = []
    }
  },
  actions: {
    // 生成路由
    GenerateRoutes({ commit }) {
      return new Promise(resolve => {
        // 向后端请求路由数据
        getRouters().then(res => {
          // 根据用户角色过滤菜单
          const roles = store.getters.roles || []
          const isStudent = roles.includes('student') || roles.includes('学员')

          // 过滤学员端菜单（对于非学员角色用户）
          let filteredData = res.data
          if (!isStudent) {
            filteredData = filterStudentMenus(res.data)
          }

          const sdata = JSON.parse(JSON.stringify(filteredData))
          const rdata = JSON.parse(JSON.stringify(filteredData))
          const sidebarRoutes = filterAsyncRouter(sdata)
          const rewriteRoutes = filterAsyncRouter(rdata, false, true)
          const asyncRoutes = filterDynamicRoutes(dynamicRoutes)
          rewriteRoutes.push({ path: '*', redirect: '/404', hidden: true })
          addDynamicRoutesOnce(asyncRoutes)
          commit('SET_ROUTES', rewriteRoutes)
          commit('SET_SIDEBAR_ROUTERS', constantRoutes.concat(sidebarRoutes))
          commit('SET_DEFAULT_ROUTES', sidebarRoutes)
          commit('SET_TOPBAR_ROUTES', sidebarRoutes)
          resolve(rewriteRoutes)
        })
      })
    },
    // 清理路由
    ClearRoutes({ commit }) {
      return new Promise(resolve => {
        commit('CLEAR_ROUTES')
        // 清理Vue Router中的动态路由
        // 注意：Vue Router 3.x没有removeRoutes方法，只能通过重新创建路由实例来清理
        // 这里我们通过清理store状态来标记需要重新生成路由
        resolve()
      })
    }
  }
}

// 过滤学员端菜单（针对非学员角色用户）
function filterStudentMenus(menus) {
  return menus.filter(menu => {
    // 过滤掉路径以 /student 或 student 开头的菜单
    const path = menu.path || ''
    if (path.startsWith('/student') || path.startsWith('student')) {
      return false
    }
    // 过滤掉使用 StudentLayout 组件的菜单
    if (menu.component === 'StudentLayout') {
      return false
    }
    // 递归过滤子菜单
    if (menu.children && menu.children.length) {
      menu.children = filterStudentMenus(menu.children)
    }
    return true
  })
}

// 遍历后台传来的路由字符串，转换为组件对象
function filterAsyncRouter(asyncRouterMap, lastRouter = false, type = false) {
  return asyncRouterMap.filter(route => {
    if (type && route.children) {
      route.children = filterChildren(route.children)
    }
    if (route.component) {
      // Layout ParentView StudentLayout 组件特殊处理
      if (route.component === 'Layout') {
        route.component = Layout
      } else if (route.component === 'StudentLayout') {
        route.component = StudentLayout
      } else if (route.component === 'ParentView') {
        route.component = ParentView
      } else if (route.component === 'InnerLink') {
        route.component = InnerLink
      } else {
        route.component = loadView(route.component)
      }
    }
    if (route.children != null && route.children && route.children.length) {
      route.children = filterAsyncRouter(route.children, route, type)
    } else {
      delete route['children']
      delete route['redirect']
    }
    return true
  })
}

function filterChildren(childrenMap, lastRouter = false) {
  var children = []
  childrenMap.forEach(el => {
    el.path = lastRouter ? lastRouter.path + '/' + el.path : el.path
    if (el.children && el.children.length && el.component === 'ParentView') {
      children = children.concat(filterChildren(el.children, el))
    } else {
      children.push(el)
    }
  })
  return children
}

// 动态路由遍历，验证是否具备权限
export function filterDynamicRoutes(routes) {
  const res = []
  routes.forEach(route => {
    if (route.permissions) {
      if (auth.hasPermiOr(route.permissions)) {
        res.push(route)
      }
    } else if (route.roles) {
      if (auth.hasRoleOr(route.roles)) {
        res.push(route)
      }
    }
  })
  return res
}

function isDynamicRouteRegistered(route) {
  if (!route || !route.path) {
    return false
  }
  const registered = router.options.routes || []
  return registered.some(item => item.path === route.path)
}

function markDynamicRoutesRegistered(routes) {
  if (!routes || routes.length === 0) {
    return
  }
  router.options.routes = router.options.routes || []
  routes.forEach(route => {
    if (!isDynamicRouteRegistered(route)) {
      router.options.routes.push(route)
    }
  })
}

function addDynamicRoutesOnce(routes) {
  const pendingRoutes = routes.filter(route => !isDynamicRouteRegistered(route))
  if (pendingRoutes.length > 0) {
    router.addRoutes(pendingRoutes)
    markDynamicRoutesRegistered(pendingRoutes)
  }
}

export const loadView = (view) => {
  if (process.env.NODE_ENV === 'development') {
    return (resolve) => require([`@/views/${view}`], resolve)
  } else {
    // 使用 import 实现生产环境的路由懒加载
    return () => import(`@/views/${view}`)
  }
}

export default permission
