<template>
  <div class="login-container">
    <!-- 主题切换按钮 -->
    <button class="theme-toggle-btn" @click="toggleTheme" :title="isDarkTheme ? '切换为亮色模式' : '切换为暗色模式'">
      <i :class="isDarkTheme ? 'el-icon-sunny' : 'el-icon-moon'"></i>
    </button>

    <!-- 环境辉光 —— 登录页专属加强 -->
    <div class="login-glow">
      <div class="lg-orb lg-orb-1"></div>
      <div class="lg-orb lg-orb-2"></div>
      <div class="lg-orb lg-orb-3"></div>
    </div>

    <!-- 毛玻璃登录卡片 -->
    <div class="login-card">
      <!-- 卡片顶部装饰光带 -->
      <div class="card-shine"></div>

      <div class="login-header">
        <div class="logo-wrapper">
          <div class="logo-glow"></div>
          <div class="logo-icon">◈</div>
        </div>
        <h1 class="login-title">剧本杀调度管理系统</h1>
        <p class="login-subtitle">MYSTERY SCRIPT · SCHEDULING SYSTEM</p>
      </div>

      <el-form ref="loginForm" :model="loginForm" :rules="loginRules" class="login-form">
        <el-form-item prop="username">
          <el-input
            v-model="loginForm.username"
            prefix-icon="el-icon-user"
            placeholder="请输入用户名"
            @keyup.enter.native="handleLogin"
          ></el-input>
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            prefix-icon="el-icon-lock"
            type="password"
            placeholder="请输入密码"
            show-password
            @keyup.enter.native="handleLogin"
          ></el-input>
        </el-form-item>
        <el-form-item>
          <el-button
            :loading="loading"
            class="login-btn"
            type="primary"
            @click.native.prevent="handleLogin"
          >
            <span v-if="!loading">登 录</span>
            <span v-else>登录中...</span>
          </el-button>
        </el-form-item>
      </el-form>

      <div class="login-links">
        <span class="register-link" @click="$router.push('/register')">还没有账号？<b>立即注册</b></span>
      </div>

      <div class="login-footer">
        <span>© 2026 剧本杀调度管理系统 v3.0</span>
      </div>
    </div>
  </div>
</template>

<script>
import { login } from '@/api/user'

export default {
  name: 'Login',
  data() {
    return {
      loginForm: {
        username: '',
        password: ''
      },
      loginRules: {
        username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
        password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
      },
      loading: false,
      isDarkTheme: true
    }
  },
  created() {
    this.loadTheme()
  },
  methods: {
    toggleTheme() {
      this.isDarkTheme = !this.isDarkTheme
      this.applyTheme()
      localStorage.setItem('theme', this.isDarkTheme ? 'dark' : 'light')
    },
    loadTheme() {
      const saved = localStorage.getItem('theme')
      if (saved === 'light') {
        this.isDarkTheme = false
      }
      this.applyTheme()
    },
    applyTheme() {
      if (this.isDarkTheme) {
        document.body.classList.remove('light-theme')
      } else {
        document.body.classList.add('light-theme')
      }
    },
    handleLogin() {
      this.$refs.loginForm.validate(async valid => {
        if (!valid) return
        this.loading = true
        try {
          const res = await login(this.loginForm)
          if (res.code === 200) {
            const { user, token } = res.data
            localStorage.setItem('token', token)
            localStorage.setItem('user', JSON.stringify(user))
            this.$message.success('登录成功')
            this.$router.push('/dashboard')
          }
        } catch (err) {
          console.error('登录失败', err)
        } finally {
          this.loading = false
        }
      })
    }
  }
}
</script>

<style scoped>
.login-container {
  width: 100%;
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: #1A1922;
  position: relative;
  overflow: hidden;
}

/* ===== 登录页专属环境辉光 ===== */
.login-glow {
  position: absolute;
  width: 100%;
  height: 100%;
  top: 0;
  left: 0;
  pointer-events: none;
}
.lg-orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(100px);
  animation: orbFloat 10s ease-in-out infinite alternate;
}
.lg-orb-1 {
  width: 500px;
  height: 500px;
  background: radial-gradient(circle, #2A1B38 0%, transparent 70%);
  top: -15%;
  right: -10%;
  opacity: 0.2;
  animation-delay: 0s;
}
.lg-orb-2 {
  width: 400px;
  height: 400px;
  background: radial-gradient(circle, #332815 0%, transparent 70%);
  bottom: -10%;
  left: -5%;
  opacity: 0.15;
  animation-delay: 3s;
}
.lg-orb-3 {
  width: 350px;
  height: 350px;
  background: radial-gradient(circle, #0D2B2B 0%, transparent 70%);
  top: 50%;
  left: 20%;
  opacity: 0.08;
  animation-delay: 6s;
}
@keyframes orbFloat {
  0% { transform: translate(0, 0) scale(1); }
  50% { transform: translate(20px, -15px) scale(1.1); }
  100% { transform: translate(-10px, 10px) scale(1.05); }
}

/* ===== 玻璃登录卡片 ===== */
.login-card {
  width: 440px;
  padding: 52px 44px 40px;
  background: rgba(30, 28, 38, 0.6);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 24px;
  backdrop-filter: blur(24px);
  -webkit-backdrop-filter: blur(24px);
  box-shadow:
    0 24px 80px rgba(0, 0, 0, 0.5),
    0 0 60px rgba(42, 27, 56, 0.15),
    inset 0 1px 1px rgba(255, 255, 255, 0.06);
  position: relative;
  z-index: 10;
  animation: cardReveal 0.8s cubic-bezier(0.25, 0.8, 0.25, 1);
  overflow: hidden;
}

/* 卡片顶部的装饰光带 — 模拟玻璃反光 */
.card-shine {
  position: absolute;
  top: 0;
  left: -50%;
  width: 200%;
  height: 1px;
  background: linear-gradient(90deg, transparent 0%, rgba(226, 192, 130, 0.3) 50%, transparent 100%);
}

@keyframes cardReveal {
  from {
    transform: translateY(30px) scale(0.97);
    opacity: 0;
  }
  to {
    transform: translateY(0) scale(1);
    opacity: 1;
  }
}

.login-header {
  text-align: center;
  margin-bottom: 40px;
}

/* Logo 发光环 */
.logo-wrapper {
  position: relative;
  width: 68px;
  height: 68px;
  margin: 0 auto 20px;
}
.logo-glow {
  position: absolute;
  top: -4px;
  left: -4px;
  width: 76px;
  height: 76px;
  border-radius: 20px;
  background: linear-gradient(135deg, rgba(226, 192, 130, 0.3), rgba(163, 130, 75, 0.1));
  filter: blur(8px);
  animation: logoGlowPulse 3s ease-in-out infinite alternate;
}
@keyframes logoGlowPulse {
  0% { opacity: 0.4; }
  100% { opacity: 0.7; }
}
.logo-icon {
  position: relative;
  width: 68px;
  height: 68px;
  background: rgba(30, 27, 38, 0.6);
  border: 1px solid rgba(226, 192, 130, 0.25);
  border-radius: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  background: linear-gradient(135deg, #E2C082, #A3824B);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  z-index: 1;
  backdrop-filter: blur(4px);
}

.login-title {
  font-size: 22px;
  font-weight: 700;
  background: linear-gradient(135deg, rgba(230, 225, 220, 0.95), rgba(157, 150, 163, 0.7));
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  margin-bottom: 8px;
  letter-spacing: 3px;
}
.login-subtitle {
  font-size: 10px;
  color: rgba(157, 150, 163, 0.4);
  letter-spacing: 3px;
  text-transform: uppercase;
  font-family: 'Inter', sans-serif;
  font-weight: 300;
}

/* ===== 表单样式 ===== */
.login-form >>> .el-form-item {
  margin-bottom: 22px;
}
.login-form >>> .el-input__inner {
  height: 50px;
  background: rgba(14, 13, 17, 0.5);
  border: 1px solid rgba(255, 255, 255, 0.06);
  border-radius: 14px;
  color: rgba(230, 225, 220, 0.9);
  font-size: 14px;
  padding-left: 46px;
  transition: all 0.35s;
  font-family: 'Inter', sans-serif;
}
.login-form >>> .el-input__inner:focus {
  border-color: rgba(226, 192, 130, 0.4);
  box-shadow: 0 0 0 4px rgba(226, 192, 130, 0.06);
}
.login-form >>> .el-input__inner::placeholder {
  color: rgba(157, 150, 163, 0.3);
  font-weight: 300;
}
.login-form >>> .el-input__prefix {
  left: 16px;
  font-size: 16px;
  color: rgba(226, 192, 130, 0.4);
}

.login-btn {
  width: 100%;
  height: 50px;
  font-size: 15px;
  font-weight: 600;
  border-radius: 14px;
  background: linear-gradient(135deg, rgba(163, 130, 75, 0.8), rgba(140, 115, 85, 0.6)) !important;
  border: 1px solid rgba(226, 192, 130, 0.2) !important;
  letter-spacing: 6px;
  transition: all 0.4s cubic-bezier(0.25, 0.8, 0.25, 1);
  position: relative;
  overflow: hidden;
  font-family: 'Noto Serif SC', serif;
}
.login-btn::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 200%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.06), transparent);
  transition: left 0.6s;
}
.login-btn:hover::before {
  left: 100%;
}
.login-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(226, 192, 130, 0.15);
  border-color: rgba(226, 192, 130, 0.35) !important;
}

.login-links {
  text-align: center;
  margin-top: 8px;
}
.register-link {
  color: rgba(157, 150, 163, 0.5);
  font-size: 13px;
  cursor: pointer;
  transition: color 0.3s;
  font-family: 'Inter', sans-serif;
}
.register-link:hover {
  color: #E2C082;
}
.register-link b {
  color: rgba(226, 192, 130, 0.7);
  font-weight: 500;
}
.register-link:hover b {
  color: #E2C082;
}

.login-footer {
  text-align: center;
  margin-top: 28px;
  font-size: 11px;
  color: rgba(157, 150, 163, 0.25);
  letter-spacing: 0.5px;
  font-family: 'Inter', sans-serif;
}

.theme-toggle-btn {
  position: absolute;
  top: 20px;
  right: 20px;
  width: 40px;
  height: 40px;
  border: none;
  border-radius: 50%;
  background: rgba(30, 28, 38, 0.6);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s;
  z-index: 100;
}
.theme-toggle-btn i {
  font-size: 18px;
  color: rgba(226, 192, 130, 0.8);
  transition: transform 0.4s;
}
.theme-toggle-btn:hover {
  background: rgba(226, 192, 130, 0.15);
}
.theme-toggle-btn:hover i {
  transform: rotate(30deg);
}
</style>

<style>
/* 亮色主题样式 */
body.light-theme .login-container {
  background: #F5F7FA;
}
body.light-theme .login-card {
  background: rgba(255, 255, 255, 0.85);
  border: 1px solid rgba(0, 0, 0, 0.08);
  box-shadow: 0 24px 80px rgba(0, 0, 0, 0.1);
}
body.light-theme .login-title {
  background: linear-gradient(135deg, #303133, #606266);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}
body.light-theme .login-subtitle {
  color: rgba(0, 0, 0, 0.25);
}
body.light-theme .logo-glow {
  background: linear-gradient(135deg, rgba(64, 158, 255, 0.3), rgba(64, 158, 255, 0.1));
}
body.light-theme .logo-icon {
  background: linear-gradient(135deg, #409EFF, #66B1FF);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}
body.light-theme .login-form >>> .el-input__inner {
  background: rgba(0, 0, 0, 0.04);
  border: 1px solid rgba(0, 0, 0, 0.08);
  color: #303133;
}
body.light-theme .login-form >>> .el-input__inner:focus {
  border-color: #409EFF;
  box-shadow: 0 0 0 4px rgba(64, 158, 255, 0.1);
}
body.light-theme .login-form >>> .el-input__inner::placeholder {
  color: rgba(0, 0, 0, 0.25);
}
body.light-theme .login-form >>> .el-input__prefix {
  color: #409EFF;
}
body.light-theme .login-btn {
  background: linear-gradient(135deg, #409EFF, #66B1FF) !important;
  border-color: #409EFF !important;
}
body.light-theme .login-btn:hover {
  box-shadow: 0 8px 24px rgba(64, 158, 255, 0.25);
}
body.light-theme .register-link {
  color: rgba(0, 0, 0, 0.4);
}
body.light-theme .register-link:hover {
  color: #409EFF;
}
body.light-theme .register-link b {
  color: #409EFF;
}
body.light-theme .login-footer {
  color: rgba(0, 0, 0, 0.2);
}
body.light-theme .theme-toggle-btn {
  background: rgba(255, 255, 255, 0.8);
  border: 1px solid rgba(0, 0, 0, 0.08);
}
body.light-theme .theme-toggle-btn i {
  color: #F0C200;
}
body.light-theme .theme-toggle-btn:hover {
  background: rgba(64, 158, 255, 0.1);
}
</style>
