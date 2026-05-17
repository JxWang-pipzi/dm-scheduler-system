<template>
  <div class="register-container">
    <!-- 主题切换按钮 -->
    <button class="theme-toggle-btn" @click="toggleTheme" :title="isDarkTheme ? '切换为亮色模式' : '切换为暗色模式'">
      <i :class="isDarkTheme ? 'el-icon-sunny' : 'el-icon-moon'"></i>
    </button>

    <!-- 环境辉光 -->
    <div class="register-glow">
      <div class="rg-orb rg-orb-1"></div>
      <div class="rg-orb rg-orb-2"></div>
      <div class="rg-orb rg-orb-3"></div>
    </div>

    <!-- 毛玻璃注册卡片 -->
    <div class="register-card">
      <div class="card-shine"></div>

      <div class="register-header">
        <div class="logo-wrapper">
          <div class="logo-glow"></div>
          <div class="logo-icon">◈</div>
        </div>
        <h1 class="register-title">创建新账号</h1>
        <p class="register-subtitle">MYSTERY SCRIPT · REGISTER</p>
      </div>

      <el-form ref="registerForm" :model="registerForm" :rules="rules" class="register-form" label-width="0">
        <el-form-item prop="username">
          <el-input v-model="registerForm.username" prefix-icon="el-icon-user" placeholder="请输入用户名"
            @keyup.enter.native="register"></el-input>
        </el-form-item>
        <el-form-item prop="password">
          <el-input type="password" v-model="registerForm.password" prefix-icon="el-icon-lock"
            placeholder="请输入密码" show-password @keyup.enter.native="register"></el-input>
        </el-form-item>
        <el-form-item prop="confirmPassword">
          <el-input type="password" v-model="registerForm.confirmPassword" prefix-icon="el-icon-lock"
            placeholder="请确认密码" show-password @keyup.enter.native="register"></el-input>
        </el-form-item>
        <el-form-item prop="realName">
          <el-input v-model="registerForm.realName" prefix-icon="el-icon-postcard"
            placeholder="请输入真实姓名" @keyup.enter.native="register"></el-input>
        </el-form-item>
        <el-form-item prop="phone">
          <el-input v-model="registerForm.phone" prefix-icon="el-icon-mobile-phone"
            placeholder="请输入手机号" @keyup.enter.native="register"></el-input>
        </el-form-item>
        <el-form-item prop="role">
          <el-select v-model="registerForm.role" placeholder="请选择角色" style="width: 100%">
            <el-option label="普通用户" value="USER"></el-option>
            <el-option label="DM主持人" value="DM"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button :loading="loading" class="register-btn" type="primary" @click.native.prevent="register">
            <span v-if="!loading">注 册</span>
            <span v-else>注册中...</span>
          </el-button>
        </el-form-item>
      </el-form>

      <div class="register-links">
        <span class="login-link" @click="$router.push('/login')">已有账号？<b>返回登录</b></span>
      </div>
    </div>
  </div>
</template>

<script>
import { register } from '../api/user'

export default {
  name: 'Register',
  data() {
    return {
      loading: false,
      isDarkTheme: true,
      registerForm: {
        username: '',
        password: '',
        confirmPassword: '',
        role: 'USER',
        phone: '',
        realName: ''
      },
      rules: {
        username: [
          { required: true, message: '请输入用户名', trigger: 'blur' },
          { min: 3, max: 50, message: '用户名长度在 3 到 50 个字符', trigger: 'blur' }
        ],
        password: [
          { required: true, message: '请输入密码', trigger: 'blur' },
          { min: 6, message: '密码长度至少 6 个字符', trigger: 'blur' }
        ],
        confirmPassword: [
          { required: true, message: '请确认密码', trigger: 'blur' },
          {
            validator: (rule, value, callback) => {
              if (value !== this.registerForm.password) {
                callback(new Error('两次输入密码不一致'))
              } else {
                callback()
              }
            },
            trigger: 'blur'
          }
        ],
        role: [
          { required: true, message: '请选择角色', trigger: 'blur' }
        ],
        phone: [
          { required: true, message: '请输入手机号', trigger: 'blur' },
          {
            pattern: /^\d{11}$/,
            message: '手机号必须为11位数字',
            trigger: 'blur'
          }
        ],
        realName: [
          { required: true, message: '请输入真实姓名', trigger: 'blur' }
        ]
      }
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
    register() {
      this.$refs.registerForm.validate((valid) => {
        if (!valid) return
        this.loading = true
        // 移除确认密码字段，因为后端不需要
        const formData = { ...this.registerForm }
        delete formData.confirmPassword

        register(formData).then(response => {
          if (response.code === 200) {
            this.$message.success('注册成功，请登录')
            this.$router.push('/login')
          } else {
            this.$message.error(response.message || '注册失败')
          }
        }).catch((error) => {
          console.error('注册错误:', error)
          this.$message.error('注册失败，请稍后重试')
        }).finally(() => {
          this.loading = false
        })
      })
    }
  }
}
</script>

<style scoped>
.register-container {
  width: 100%;
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: #1A1922;
  position: relative;
  overflow: hidden;
}

/* ===== 环境辉光 ===== */
.register-glow {
  position: absolute;
  width: 100%;
  height: 100%;
  top: 0;
  left: 0;
  pointer-events: none;
}
.rg-orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(100px);
  animation: orbFloat 10s ease-in-out infinite alternate;
}
.rg-orb-1 {
  width: 500px; height: 500px;
  background: radial-gradient(circle, #2A1B38 0%, transparent 70%);
  top: -15%; right: -10%; opacity: 0.2;
}
.rg-orb-2 {
  width: 400px; height: 400px;
  background: radial-gradient(circle, #332815 0%, transparent 70%);
  bottom: -10%; left: -5%; opacity: 0.15; animation-delay: 3s;
}
.rg-orb-3 {
  width: 350px; height: 350px;
  background: radial-gradient(circle, #0D2B2B 0%, transparent 70%);
  top: 50%; left: 20%; opacity: 0.08; animation-delay: 6s;
}
@keyframes orbFloat {
  0% { transform: translate(0, 0) scale(1); }
  50% { transform: translate(20px, -15px) scale(1.1); }
  100% { transform: translate(-10px, 10px) scale(1.05); }
}

/* ===== 毛玻璃注册卡片 ===== */
.register-card {
  width: 440px;
  padding: 40px 44px 32px;
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
.card-shine {
  position: absolute;
  top: 0; left: -50%;
  width: 200%; height: 1px;
  background: linear-gradient(90deg, transparent 0%, rgba(226, 192, 130, 0.3) 50%, transparent 100%);
}
@keyframes cardReveal {
  from { transform: translateY(30px) scale(0.97); opacity: 0; }
  to { transform: translateY(0) scale(1); opacity: 1; }
}

.register-header {
  text-align: center;
  margin-bottom: 28px;
}
.logo-wrapper {
  position: relative;
  width: 56px; height: 56px;
  margin: 0 auto 16px;
}
.logo-glow {
  position: absolute;
  top: -4px; left: -4px;
  width: 64px; height: 64px;
  border-radius: 16px;
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
  width: 56px; height: 56px;
  border: 1px solid rgba(226, 192, 130, 0.25);
  border-radius: 16px;
  display: flex; align-items: center; justify-content: center;
  font-size: 24px;
  background: linear-gradient(135deg, #E2C082, #A3824B);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  z-index: 1;
  backdrop-filter: blur(4px);
}

.register-title {
  font-size: 20px;
  font-weight: 700;
  background: linear-gradient(135deg, rgba(230, 225, 220, 0.95), rgba(157, 150, 163, 0.7));
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  margin-bottom: 6px;
  letter-spacing: 3px;
}
.register-subtitle {
  font-size: 10px;
  color: rgba(157, 150, 163, 0.4);
  letter-spacing: 3px;
  text-transform: uppercase;
  font-family: 'Inter', sans-serif;
  font-weight: 300;
}

/* ===== 表单样式 ===== */
.register-form >>> .el-form-item {
  margin-bottom: 16px;
}
.register-form >>> .el-input__inner {
  height: 46px;
  background: rgba(14, 13, 17, 0.5);
  border: 1px solid rgba(255, 255, 255, 0.06);
  border-radius: 12px;
  color: rgba(230, 225, 220, 0.9);
  font-size: 14px;
  padding-left: 42px;
  transition: all 0.35s;
  font-family: 'Inter', sans-serif;
}
.register-form >>> .el-input__inner:focus {
  border-color: rgba(226, 192, 130, 0.4);
  box-shadow: 0 0 0 4px rgba(226, 192, 130, 0.06);
}
.register-form >>> .el-input__inner::placeholder {
  color: rgba(157, 150, 163, 0.3);
  font-weight: 300;
}
.register-form >>> .el-input__prefix {
  left: 14px;
  font-size: 15px;
  color: rgba(226, 192, 130, 0.4);
}
/* 下拉选择框样式 */
.register-form >>> .el-select .el-input__inner {
  padding-left: 16px;
}

.register-btn {
  width: 100%;
  height: 46px;
  font-size: 15px;
  font-weight: 600;
  border-radius: 12px;
  background: linear-gradient(135deg, rgba(163, 130, 75, 0.8), rgba(140, 115, 85, 0.6)) !important;
  border: 1px solid rgba(226, 192, 130, 0.2) !important;
  letter-spacing: 6px;
  transition: all 0.4s cubic-bezier(0.25, 0.8, 0.25, 1);
  position: relative;
  overflow: hidden;
  font-family: 'Noto Serif SC', serif;
}
.register-btn::before {
  content: '';
  position: absolute;
  top: 0; left: -100%;
  width: 200%; height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.06), transparent);
  transition: left 0.6s;
}
.register-btn:hover::before {
  left: 100%;
}
.register-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(226, 192, 130, 0.15);
  border-color: rgba(226, 192, 130, 0.35) !important;
}

.register-links {
  text-align: center;
  margin-top: 12px;
}
.login-link {
  color: rgba(157, 150, 163, 0.5);
  font-size: 13px;
  cursor: pointer;
  transition: color 0.3s;
  font-family: 'Inter', sans-serif;
}
.login-link:hover {
  color: #E2C082;
}
.login-link b {
  color: rgba(226, 192, 130, 0.7);
  font-weight: 500;
}
.login-link:hover b {
  color: #E2C082;
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
body.light-theme .register-container {
  background: #F5F7FA;
}
body.light-theme .register-card {
  background: rgba(255, 255, 255, 0.85);
  border: 1px solid rgba(0, 0, 0, 0.08);
  box-shadow: 0 24px 80px rgba(0, 0, 0, 0.1);
}
body.light-theme .register-title {
  background: linear-gradient(135deg, #303133, #606266);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}
body.light-theme .register-subtitle {
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
body.light-theme .register-form >>> .el-input__inner {
  background: rgba(0, 0, 0, 0.04);
  border: 1px solid rgba(0, 0, 0, 0.08);
  color: #303133;
}
body.light-theme .register-form >>> .el-input__inner:focus {
  border-color: #409EFF;
  box-shadow: 0 0 0 4px rgba(64, 158, 255, 0.1);
}
body.light-theme .register-form >>> .el-input__inner::placeholder {
  color: rgba(0, 0, 0, 0.25);
}
body.light-theme .register-form >>> .el-input__prefix {
  color: #409EFF;
}
body.light-theme .register-btn {
  background: linear-gradient(135deg, #409EFF, #66B1FF) !important;
  border-color: #409EFF !important;
}
body.light-theme .register-btn:hover {
  box-shadow: 0 8px 24px rgba(64, 158, 255, 0.25);
}
body.light-theme .login-link {
  color: rgba(0, 0, 0, 0.4);
}
body.light-theme .login-link:hover {
  color: #409EFF;
}
body.light-theme .login-link b {
  color: #409EFF;
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
