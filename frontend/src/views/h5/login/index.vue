<template>
  <div class="h5-login">
    <div class="login-header">
      <div class="logo">
        <i class="el-icon-edit-outline"></i>
      </div>
      <h1>在线考试系统</h1>
      <p>请使用身份证号登录</p>
    </div>
    
    <!-- 错误提示 -->
    <div v-if="errorMsg" class="error-toast">
      <i class="el-icon-warning"></i>
      <span>{{ errorMsg }}</span>
    </div>
    
    <!-- 成功提示 -->
    <div v-if="successMsg" class="success-toast">
      <i class="el-icon-success"></i>
      <span>{{ successMsg }}</span>
    </div>
    
    <div class="login-form">
      <div class="form-item">
        <div class="input-wrapper">
          <i class="el-icon-postcard"></i>
          <input 
            v-model="loginForm.idCard"
            type="text"
            placeholder="请输入身份证号"
            maxlength="18"
            @keyup.enter="handleLogin"
          />
        </div>
      </div>
      
      <div class="form-item">
        <div class="input-wrapper">
          <i class="el-icon-lock"></i>
          <input 
            v-model="loginForm.password"
            :type="showPassword ? 'text' : 'password'"
            placeholder="请输入密码"
            @keyup.enter="handleLogin"
          />
          <i 
            :class="showPassword ? 'el-icon-view' : 'el-icon-hide'"
            class="toggle-password"
            @click="showPassword = !showPassword"
          ></i>
        </div>
      </div>
      
      <button 
        class="login-btn"
        :class="{ 'loading': loading }"
        :disabled="loading"
        @click="handleLogin"
      >
        <span v-if="!loading">登 录</span>
        <span v-else><i class="el-icon-loading"></i> 登录中...</span>
      </button>
    </div>
    
    <div class="login-footer">
      <p>© 2024 在线考试系统</p>
    </div>
  </div>
</template>

<script>
import { mobileLogin } from '@/api/mobile/login'
import { setToken } from '@/utils/auth'

export default {
  name: 'H5Login',
  data() {
    return {
      loginForm: {
        idCard: '',
        password: ''
      },
      loading: false,
      showPassword: false,
      examId: null,
      errorMsg: '',
      successMsg: ''
    }
  },
  created() {
    // 从路由参数获取考试ID
    this.examId = this.$route.query.examId || this.$route.params.examId
  },
  methods: {
    showError(msg) {
      this.errorMsg = msg
      this.successMsg = ''
      // 3秒后自动消失
      setTimeout(() => {
        this.errorMsg = ''
      }, 3000)
    },
    showSuccess(msg) {
      this.successMsg = msg
      this.errorMsg = ''
      // 2秒后自动消失
      setTimeout(() => {
        this.successMsg = ''
      }, 2000)
    },
    handleLogin() {
      if (!this.loginForm.idCard) {
        this.showError('请输入身份证号')
        return
      }
      if (!this.loginForm.password) {
        this.showError('请输入密码')
        return
      }
      
      this.loading = true
      this.errorMsg = ''
      mobileLogin(this.loginForm.idCard, this.loginForm.password, this.examId)
        .then(res => {
          if (res.code === 200) {
            // 保存token
            setToken(res.token)
            this.showSuccess('登录成功')
            
            // 跳转到考试页面
            setTimeout(() => {
              if (this.examId) {
                this.$router.push('/h5/exam/' + this.examId)
              } else {
                this.showError('缺少考试ID，请重新扫码')
              }
            }, 500)
          } else {
            this.showError(res.msg || '登录失败')
          }
        })
        .catch(err => {
          console.error('登录失败', err)
          // 从错误对象中提取消息
          let errMsg = '登录失败，请检查身份证号和密码'
          if (err && err.message) {
            errMsg = err.message
          } else if (err && typeof err === 'string') {
            errMsg = err
          }
          this.showError(errMsg)
        })
        .finally(() => {
          this.loading = false
        })
    }
  }
}
</script>

<style scoped>
.h5-login {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 20px;
}

.login-header {
  text-align: center;
  margin-bottom: 40px;
}

.logo {
  width: 70px;
  height: 70px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 20px;
}

.logo i {
  font-size: 35px;
  color: #fff;
}

.login-header h1 {
  color: #fff;
  font-size: 24px;
  font-weight: 600;
  margin-bottom: 8px;
}

.login-header p {
  color: rgba(255, 255, 255, 0.8);
  font-size: 14px;
}

.login-form {
  background: #fff;
  border-radius: 16px;
  padding: 30px 20px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);
}

.form-item {
  margin-bottom: 20px;
}

.input-wrapper {
  display: flex;
  align-items: center;
  background: #f5f5f5;
  border-radius: 10px;
  padding: 0 15px;
  height: 50px;
  border: 2px solid transparent;
  transition: all 0.3s;
}

.input-wrapper:focus-within {
  border-color: #667eea;
  background: #fff;
}

.input-wrapper i {
  font-size: 20px;
  color: #999;
  margin-right: 12px;
}

.input-wrapper input {
  flex: 1;
  border: none;
  background: transparent;
  font-size: 16px;
  outline: none;
}

.toggle-password {
  cursor: pointer;
  margin-right: 0;
  margin-left: 12px;
}

.login-btn {
  width: 100%;
  height: 50px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  border-radius: 10px;
  color: #fff;
  font-size: 17px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s;
  margin-top: 10px;
}

.login-btn:active {
  transform: scale(0.98);
}

.login-btn.loading {
  opacity: 0.7;
  cursor: not-allowed;
}

.login-btn i {
  margin-right: 8px;
}

.login-footer {
  text-align: center;
  margin-top: 40px;
}

.login-footer p {
  color: rgba(255, 255, 255, 0.6);
  font-size: 12px;
}

/* 自定义Toast提示样式 */
.error-toast,
.success-toast {
  position: fixed;
  top: 60px;
  left: 50%;
  transform: translateX(-50%);
  padding: 12px 24px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  z-index: 9999;
  animation: slideDown 0.3s ease;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.error-toast {
  background: #fff2f0;
  border: 1px solid #ffccc7;
  color: #ff4d4f;
}

.success-toast {
  background: #f6ffed;
  border: 1px solid #b7eb8f;
  color: #52c41a;
}

.error-toast i,
.success-toast i {
  margin-right: 8px;
  font-size: 18px;
}

@keyframes slideDown {
  from {
    opacity: 0;
    transform: translateX(-50%) translateY(-20px);
  }
  to {
    opacity: 1;
    transform: translateX(-50%) translateY(0);
  }
}
</style>
