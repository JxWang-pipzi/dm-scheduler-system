<template>
  <div class="profile-container">
    <el-card class="box-card dark-card">
      <div slot="header" class="clearfix">
        <span class="card-title">个人中心 (Profile)</span>
      </div>
      
      <div class="profile-content">
        <div class="user-info-section">
          <el-avatar :size="100" class="user-avatar" :src="userInfo.avatar || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'"></el-avatar>
          <div class="user-details">
            <h2 class="username">{{ userInfo.username || 'Ghost' }}</h2>
            <el-tag :type="roleTypeMap[userInfo.role] || 'info'" effect="dark" class="role-tag">
              {{ roleNameMap[userInfo.role] || '未知角色' }}
            </el-tag>
            <p class="detail-item"><i class="el-icon-mobile-phone"></i> {{ userInfo.phone || '暂未绑定' }}</p>
            <p class="detail-item"><i class="el-icon-user"></i> {{ userInfo.realName || '暂无实名' }}</p>
          </div>
        </div>

        <el-divider><i class="el-icon-lock"></i> 安全设置</el-divider>

        <div class="password-section">
          <el-form :model="pwdForm" :rules="pwdRules" ref="pwdForm" label-width="100px" class="dark-form">
            <el-form-item label="原密码" prop="oldPassword">
              <el-input 
                type="password" 
                v-model="pwdForm.oldPassword" 
                placeholder="请输入当前密码" 
                show-password>
              </el-input>
            </el-form-item>
            <el-form-item label="新密码" prop="newPassword">
              <el-input 
                type="password" 
                v-model="pwdForm.newPassword" 
                placeholder="请输入新密码" 
                show-password>
              </el-input>
            </el-form-item>
            <el-form-item label="确认密码" prop="confirmPassword">
              <el-input 
                type="password" 
                v-model="pwdForm.confirmPassword" 
                placeholder="请再次输入新密码" 
                show-password>
              </el-input>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="submitPwdChange" :loading="updating" class="custom-btn">提交修改</el-button>
              <el-button @click="resetForm('pwdForm')" class="custom-btn plain">重置内容</el-button>
            </el-form-item>
          </el-form>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script>
import { updatePassword } from '@/api/user'

export default {
  name: 'Profile',
  data() {
    const validatePass2 = (rule, value, callback) => {
      if (value === '') {
        callback(new Error('请再次输入新密码'));
      } else if (value !== this.pwdForm.newPassword) {
        callback(new Error('两次输入密码不一致!'));
      } else {
        callback();
      }
    };
    return {
      userInfo: {},
      updating: false,
      roleNameMap: {
        'ADMIN': '超级管理员',
        'DM': '剧本杀DM',
        'USER': '顾客'
      },
      roleTypeMap: {
        'ADMIN': 'danger',
        'DM': 'warning',
        'USER': 'success'
      },
      pwdForm: {
        oldPassword: '',
        newPassword: '',
        confirmPassword: ''
      },
      pwdRules: {
        oldPassword: [
          { required: true, message: '请输入原密码', trigger: 'blur' },
          { min: 6, max: 20, message: '长度在 6 到 20 个字符', trigger: 'blur' }
        ],
        newPassword: [
          { required: true, message: '请输入新密码', trigger: 'blur' },
          { min: 6, max: 20, message: '长度在 6 到 20 个字符', trigger: 'blur' }
        ],
        confirmPassword: [
          { required: true, validator: validatePass2, trigger: 'blur' }
        ]
      }
    }
  },
  created() {
    this.loadUserInfo();
  },
  methods: {
    loadUserInfo() {
      const userStr = localStorage.getItem('user');
      if (userStr) {
        try {
          this.userInfo = JSON.parse(userStr);
          if (this.userInfo.role) {
            this.userInfo.role = String(this.userInfo.role).toUpperCase();
          }
        } catch (e) {
          console.error("加载用户信息失败", e);
        }
      }
    },
    submitPwdChange() {
      this.$refs.pwdForm.validate(valid => {
        if (valid) {
          this.updating = true;
          const payload = {
            oldPassword: this.pwdForm.oldPassword,
            newPassword: this.pwdForm.newPassword
          };
          updatePassword(payload).then(res => {
            if (res.code === 200) {
              this.$message.success('密码修改成功，请重新登录！');
              localStorage.removeItem('token');
              localStorage.removeItem('user');
              this.$router.push('/login');
            } else {
              this.$message.error(res.message || '修改失败');
            }
          }).catch(() => {
            this.$message.error('网络出现异常，请稍后再试');
          }).finally(() => {
            this.updating = false;
          });
        }
      });
    },
    resetForm(formName) {
      if (this.$refs[formName]) {
        this.$refs[formName].resetFields();
      }
    }
  }
}
</script>

<style scoped>
.profile-container {
  padding: 24px;
  animation: fadeIn 0.8s ease;
  min-height: calc(100vh - 120px);
}

.dark-card {
  background: rgba(45, 42, 54, 0.6);
  border: 1px solid rgba(157, 150, 163, 0.2);
  backdrop-filter: blur(16px);
  border-radius: 16px;
  color: #ebe6e1;
}

.dark-card >>> .el-card__header {
  border-bottom: 1px solid rgba(157, 150, 163, 0.1);
}

.card-title {
  font-size: 18px;
  font-weight: 600;
  color: #e2c082;
  letter-spacing: 1px;
}

.user-info-section {
  display: flex;
  align-items: center;
  gap: 30px;
  padding: 20px 0 40px;
}

.user-avatar {
  border: 2px solid rgba(226, 192, 130, 0.5);
  box-shadow: 0 0 20px rgba(226, 192, 130, 0.2);
}

.user-details .username {
  margin: 0 0 10px 0;
  font-size: 28px;
  font-weight: 700;
  color: #fff;
}

.role-tag {
  margin-bottom: 15px;
  font-size: 12px;
  border: none;
  font-weight: 600;
}

.detail-item {
  margin: 8px 0;
  color: #9d96a3;
  font-size: 14px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.password-section {
  padding: 20px 0;
  max-width: 500px;
}

/* 覆盖原生 Element UI 输入框为磨砂黑风格 */
.dark-form >>> .el-form-item__label {
  color: #ebe6e1;
}

.dark-form >>> .el-input__inner {
  background: rgba(20, 18, 25, 0.4);
  border: 1px solid rgba(157, 150, 163, 0.3);
  color: #ebe6e1;
  border-radius: 8px;
  transition: all 0.3s;
}

.dark-form >>> .el-input__inner:focus {
  border-color: rgba(226, 192, 130, 0.6);
  box-shadow: 0 0 8px rgba(226, 192, 130, 0.2);
}

.custom-btn {
  background: linear-gradient(135deg, #a3824b, #e2c082);
  border: none;
  color: #2d2a36;
  font-weight: 600;
  border-radius: 8px;
  padding: 10px 24px;
}
.custom-btn.plain {
  background: transparent;
  border: 1px solid rgba(157, 150, 163, 0.5);
  color: #ebe6e1;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}
</style>
