import router from '@/router'
import { MessageBox, } from 'element-ui'
import { login, logout, getInfo } from '@/api/login'
import { getToken, setToken, removeToken } from '@/utils/auth'
import { isHttp, isEmpty } from "@/utils/validate"
import defAva from '@/assets/images/profile.jpg'

const user = {
  state: {
    token: getToken(),
    id: '',
    name: '',
    nickName: '',
    avatar: '',
    roles: [],
    permissions: []
  },

  mutations: {
    SET_TOKEN: (state, token) => {
      state.token = token
    },
    SET_ID: (state, id) => {
      state.id = id
    },
    SET_NAME: (state, name) => {
      state.name = name
    },
    SET_NICK_NAME: (state, nickName) => {
      state.nickName = nickName
    },
    SET_AVATAR: (state, avatar) => {
      state.avatar = avatar
    },
    SET_ROLES: (state, roles) => {
      state.roles = roles
    },
    SET_PERMISSIONS: (state, permissions) => {
      state.permissions = permissions
    }
  },

  actions: {
    // 登录
    Login({ commit, dispatch }, userInfo) {
      const username = userInfo.username.trim()
      const password = userInfo.password
      const code = userInfo.code
      const uuid = userInfo.uuid
      return new Promise((resolve, reject) => {
        login(username, password, code, uuid).then(res => {
          setToken(res.token)
          commit('SET_TOKEN', res.token)
          // 登录成功后清理之前的路由状态，确保重新生成
          dispatch('ClearRoutes').finally(() => {
            resolve()
          })
        }).catch(error => {
          reject(error)
        })
      })
    },

    // 获取用户信息
    GetInfo({ commit, state }) {
      return new Promise((resolve, reject) => {
        getInfo().then(res => {
          const user = res.user
          let avatar = user.avatar || ""
          if (!isHttp(avatar)) {
            avatar = (isEmpty(avatar)) ? defAva : process.env.VUE_APP_BASE_API + avatar
          }
          if (res.roles && res.roles.length > 0) { // 验证返回的roles是否是一个非空数组
            commit('SET_ROLES', res.roles)
            commit('SET_PERMISSIONS', res.permissions)
          } else {
            commit('SET_ROLES', ['ROLE_DEFAULT'])
          }
          commit('SET_ID', user.userId)
          commit('SET_NAME', user.userName)
          commit('SET_NICK_NAME', user.nickName)
          commit('SET_AVATAR', avatar)
          
          // 判断用户角色，确定跳转的个人中心页面
          const isStudent = res.roles && res.roles.some(role => role === 'student' || role === '学员')
          const profileRoute = isStudent
            ? { path: '/student/profile', query: { activeTab: 'resetPwd' } }
            : { name: 'Profile', params: { activeTab: 'resetPwd' } }
          
          /* 初始密码提示 */
          if(res.isDefaultModifyPwd) {
            MessageBox.confirm('您的密码还是初始密码，请修改密码！',  '安全提示', {  confirmButtonText: '确定',  cancelButtonText: '取消',  type: 'warning' }).then(() => {
              router.push(profileRoute)
            }).catch(() => {})
          }
          /* 过期密码提示 */
          if(!res.isDefaultModifyPwd && res.isPasswordExpired) {
            MessageBox.confirm('您的密码已过期，请尽快修改密码！',  '安全提示', {  confirmButtonText: '确定',  cancelButtonText: '取消',  type: 'warning' }).then(() => {
              router.push(profileRoute)
            }).catch(() => {})
          }
          resolve(res)
        }).catch(error => {
          reject(error)
        })
      })
    },

    // 退出系统
    LogOut({ commit, state, dispatch }) {
      return new Promise((resolve, reject) => {
        logout(state.token).then(() => {
          // 立即清理所有状态
          commit('SET_TOKEN', '')
          commit('SET_ROLES', [])
          commit('SET_PERMISSIONS', [])
          commit('SET_ID', '')
          commit('SET_NAME', '')
          commit('SET_NICK_NAME', '')
          commit('SET_AVATAR', '')
          removeToken()
          // 清理路由状态
          dispatch('ClearRoutes').then(() => {
            resolve()
          }).catch(() => {
            // 即使清理路由失败也要完成退出流程
            resolve()
          })
        }).catch(error => {
          // 即使后端退出失败，也要清理前端状态
          commit('SET_TOKEN', '')
          commit('SET_ROLES', [])
          commit('SET_PERMISSIONS', [])
          commit('SET_ID', '')
          commit('SET_NAME', '')
          commit('SET_NICK_NAME', '')
          commit('SET_AVATAR', '')
          removeToken()
          dispatch('ClearRoutes').finally(() => {
            reject(error)
          })
        })
      })
    },

    // 前端 登出
    FedLogOut({ commit, dispatch }) {
      return new Promise(resolve => {
        commit('SET_TOKEN', '')
        commit('SET_ROLES', [])
        commit('SET_PERMISSIONS', [])
        commit('SET_ID', '')
        commit('SET_NAME', '')
        commit('SET_NICK_NAME', '')
        commit('SET_AVATAR', '')
        removeToken()
        // 清理路由状态
        dispatch('ClearRoutes').then(() => {
          resolve()
        })
      })
    }
  }
}

export default user
