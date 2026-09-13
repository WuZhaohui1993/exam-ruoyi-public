<template>
  <div class="login-container">
    <canvas id="particle-canvas"></canvas>
    <div class="login">
      <div class="left-panel">
        <!-- You can add a logo or illustration here -->
      </div>
      <el-form ref="loginForm" :model="loginForm" :rules="loginRules" class="login-form">
        <h3 class="title">{{title}}</h3>
        <el-form-item prop="username">
          <el-input
            v-model="loginForm.username"
            type="text"
            auto-complete="off"
            placeholder="账号"
          >
            <svg-icon slot="prefix" icon-class="user" class="el-input__icon input-icon" />
          </el-input>
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            auto-complete="off"
            placeholder="密码"
            @keyup.enter.native="handleLogin"
          >
            <svg-icon slot="prefix" icon-class="password" class="el-input__icon input-icon" />
          </el-input>
        </el-form-item>
        <el-form-item prop="code" v-if="captchaEnabled">
          <el-input
            v-model="loginForm.code"
            auto-complete="off"
            placeholder="验证码"
            style="width: 63%"
            @keyup.enter.native="handleLogin"
          >
            <svg-icon slot="prefix" icon-class="validCode" class="el-input__icon input-icon" />
          </el-input>
          <div class="login-code">
            <img :src="codeUrl" @click="getCode" class="login-code-img"/>
          </div>
        </el-form-item>
        <el-checkbox v-model="loginForm.rememberMe" style="margin:0px 0px 25px 0px;">记住密码</el-checkbox>
        <el-form-item style="width:100%;">
          <el-button
            :loading="loading"
            size="medium"
            type="primary"
            style="width:100%;"
            @click.native.prevent="handleLogin"
            class="login-button"
          >
            <span v-if="!loading">登 录</span>
            <span v-else>登 录 中...</span>
          </el-button>
          <div style="float: right;" v-if="register">
            <router-link class="link-type" :to="'/register'">立即注册</router-link>
          </div>
        </el-form-item>
      </el-form>
    </div>
    <!--  底部  -->
    <div class="el-login-footer">
      <span>Copyright © 2025 考试培训系统 All Rights Reserved.</span>
    </div>
  </div>
</template>

<script>
import { getCodeImg } from "@/api/login"
import Cookies from "js-cookie"

export default {
  name: "Login",
  data() {
    return {
      title: process.env.VUE_APP_TITLE,
      codeUrl: "",
      loginForm: {
        username: "admin",
        password: "",
        rememberMe: false,
        code: "",
        uuid: ""
      },
      loginRules: {
        username: [
          { required: true, trigger: "blur", message: "请输入您的账号" }
        ],
        password: [
          { required: true, trigger: "blur", message: "请输入您的密码" }
        ],
        code: [{ required: true, trigger: "change", message: "请输入验证码" }]
      },
      loading: false,
      // 验证码开关
      captchaEnabled: true,
      // 注册开关
      register: false,
      redirect: undefined
    }
  },
  watch: {
    $route: {
      handler: function(route) {
        this.redirect = route.query && route.query.redirect
      },
      immediate: true
    }
  },
  mounted() {
    this.initParticleAnimation();
  },
  created() {
    this.getCode()
    this.getCookie()
  },
  methods: {
    getCode() {
      getCodeImg().then(res => {
        this.captchaEnabled = res.captchaEnabled === undefined ? true : res.captchaEnabled
        if (this.captchaEnabled) {
          this.codeUrl = "data:image/gif;base64," + res.img
          this.loginForm.uuid = res.uuid
        }
      })
    },
    getCookie() {
      const username = Cookies.get("username")
      const rememberMe = Cookies.get('rememberMe')
      this.loginForm = {
        username: username === undefined ? this.loginForm.username : username,
        rememberMe: rememberMe === undefined ? false : Boolean(rememberMe)
      }
    },
    handleLogin() {
      this.$refs.loginForm.validate(valid => {
        if (valid) {
          this.loading = true
          if (this.loginForm.rememberMe) {
            Cookies.set("username", this.loginForm.username, { expires: 30 })
            Cookies.set('rememberMe', this.loginForm.rememberMe, { expires: 30 })
          } else {
            Cookies.remove("username")
            Cookies.remove('rememberMe')
          }
          this.$store.dispatch("Login", this.loginForm).then(() => {
            // 登录成功后获取用户信息，根据角色跳转
            this.$store.dispatch("GetInfo").then((res) => {
              const roles = res.roles || []
              
              // 根据用户角色判断跳转页面
              if (roles.includes('student') || roles.includes('学员')) {
                // 学员角色跳转到学员端首页
                this.$router.push({ path: '/student/dashboard' }).catch(()=>{})
              } else {
                // 管理员角色跳转到管理端首页
                this.$router.push({ path: this.redirect || "/" }).catch(()=>{})
              }
            }).catch(() => {
              // 获取用户信息失败，跳转到默认页面
              this.$router.push({ path: this.redirect || "/" }).catch(()=>{})
            })
          }).catch(() => {
            this.loading = false
            if (this.captchaEnabled) {
              this.getCode()
            }
          })
        }
      })
    },
    initParticleAnimation() {
      const canvas = document.getElementById('particle-canvas');
      if (!canvas) return;
      const ctx = canvas.getContext('2d');
      let particles = [];
      const particleCount = 100; // 保持粒子数量适中

      const mouse = {
        x: null,
        y: null,
        radius: 100 // 减小鼠标影响半径，效果更细腻
      };

      window.addEventListener('mousemove', (event) => {
        mouse.x = event.x;
        mouse.y = event.y;
      });
      window.addEventListener('mouseout', () => {
        mouse.x = null;
        mouse.y = null;
      });

      canvas.width = window.innerWidth;
      canvas.height = window.innerHeight;

      class Particle {
        constructor(x, y) {
          this.x = x;
          this.y = y;
          this.size = Math.random() * 2.5 + 1; // 粒子大小
          this.speedX = Math.random() * 1.5 - 0.75; // 持续的、随机的X轴速度
          this.speedY = Math.random() * 1.5 - 0.75; // 持续的、随机的Y轴速度
          this.color = '#ffffff';
        }
        update() {
          // 持续运动
          this.x += this.speedX;
          this.y += this.speedY;

          // 边界反弹
          if (this.x < 0 || this.x > canvas.width) this.speedX *= -1;
          if (this.y < 0 || this.y > canvas.height) this.speedY *= -1;

          // 鼠标交互：施加一个推力
          if (mouse.x != null) {
            const dx = this.x - mouse.x;
            const dy = this.y - mouse.y;
            const distance = Math.sqrt(dx * dx + dy * dy);
            if (distance < mouse.radius) {
              const force = (mouse.radius - distance) / mouse.radius;
              this.x += (dx / distance) * force * 2;
              this.y += (dy / distance) * force * 2;
            }
          }
        }
        draw() {
          ctx.fillStyle = this.color;
          ctx.beginPath();
          ctx.arc(this.x, this.y, this.size, 0, Math.PI * 2);
          ctx.fill();
        }
      }

      function init() {
        particles = [];
        for (let i = 0; i < particleCount; i++) {
          const x = Math.random() * canvas.width;
          const y = Math.random() * canvas.height;
          particles.push(new Particle(x, y));
        }
      }

      function animate() {
        ctx.clearRect(0, 0, canvas.width, canvas.height);
        for (let i = 0; i < particles.length; i++) {
          particles[i].update();
          particles[i].draw();
        }
        connect();
        requestAnimationFrame(animate);
      }

      function connect() {
        let opacityValue = 1;
        for (let a = 0; a < particles.length; a++) {
          for (let b = a; b < particles.length; b++) {
            let distance = Math.hypot(particles[a].x - particles[b].x, particles[a].y - particles[b].y);
            if (distance < 150) { // 调整连接距离
              opacityValue = 1 - (distance / 150);
              ctx.strokeStyle = 'rgba(255,255,255,' + opacityValue + ')';
              ctx.lineWidth = 1;
              ctx.beginPath();
              ctx.moveTo(particles[a].x, particles[a].y);
              ctx.lineTo(particles[b].x, particles[b].y);
              ctx.stroke();
            }
          }
        }
      }

      window.addEventListener('resize', () => {
        canvas.width = window.innerWidth;
        canvas.height = window.innerHeight;
        init();
      });

      init();
      animate();
    }
  }
}
</script>

<style rel="stylesheet/scss" lang="scss">
#particle-canvas {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 1; /* Canvas在背景和表单之间 */
}

.login-container {
  position: relative; /* 作为伪元素和内部元素的定位上下文 */
  height: 100vh;
  width: 100%;
  overflow: hidden;
  background-color: #1f2937; /* 深色底色 */
}

.login-container::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-image: url('https://images.unsplash.com/photo-1531297484001-80022131f5a1?q=80&w=2020&auto=format&fit=crop');
  background-size: cover;
  background-position: center;
  z-index: 0; /* 伪元素在最底层 */
  animation: zoom-in-out 40s ease-in-out infinite;
}

/* 恢复动画定义 */
@keyframes zoom-in-out {
  0% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.2);
  }
  100% {
    transform: scale(1);
  }
}

.login {
  position: relative; /* 确保登录表单在canvas之上 */
  z-index: 2;
  display: flex;
  justify-content: flex-end;
  align-items: center;
  height: 100%;
  padding-right: 10%;
}

.title {
  margin: 0px auto 30px auto;
  text-align: center;
  color: #eee;
  font-size: 26px;
  font-weight: bold;
  text-shadow: 0 0 10px rgba(0, 0, 0, 0.5);
}

.login-form {
  border-radius: 15px;
  width: 400px;
  padding: 35px 35px 15px 35px;
  background: rgba(20, 20, 30, 0.75); /* 更深的玻璃拟态效果 */
  backdrop-filter: blur(10px) saturate(120%);
  border: 1px solid rgba(255, 255, 255, 0.1);
  box-shadow: 0 8px 32px 0 rgba(0, 0, 0, 0.37);
  z-index: 1;

  .el-input {
    input {
      background: transparent;
      border: 1px solid rgba(255, 255, 255, 0.3);
      border-radius: 5px;
      color: #fff;
      height: 42px;
      &:focus {
        border-color: #409EFF;
        box-shadow: 0 0 15px rgba(64, 158, 255, 0.5);
      }
    }
  }
  .input-icon {
    height: 42px;
    width: 16px;
    margin-left: 5px;
    color: #eee;
  }
}

.login-button {
  background: linear-gradient(90deg, #409EFF, #67C23A);
  border: none;
  transition: all 0.3s ease;
  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 10px 20px rgba(0, 0, 0, 0.2);
  }
}

.login-code {
  width: 33%;
  height: 42px;
  float: right;
  img {
    cursor: pointer;
    vertical-align: middle;
    border-radius: 5px;
  }
}
.el-login-footer {
  z-index: 2; /* 确保页脚在最上层 */
  height: 40px;
  line-height: 40px;
  position: fixed;
  bottom: 0;
  width: 100%;
  text-align: center;
  color: #fff;
  font-family: Arial;
  font-size: 12px;
  letter-spacing: 1px;
  text-shadow: 0 0 5px rgba(0, 0, 0, 0.5);
}
.login-code-img {
  height: 42px;
}

.login-form .el-checkbox__label {
  color: #fff;
}
</style>
