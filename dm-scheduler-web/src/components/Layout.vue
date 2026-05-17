<template>
  <div class="layout-container" :class="{ 'sidebar-collapsed': isCollapsed }">
    <el-container>
      <!-- 顶部导航栏 — 磨砂玻璃质感 -->
      <el-header class="top-header">
        <div class="header-left">
          <button class="collapse-btn" @click="toggleSidebar">
            <i :class="isCollapsed ? 'el-icon-s-unfold' : 'el-icon-s-fold'"></i>
          </button>
          <h2 class="system-title">
            <span class="title-icon">◈</span>
            <span v-if="!isCollapsed" class="title-text">{{ systemTitle }}</span>
          </h2>
        </div>
        <div class="header-center">
          <!-- 面包屑导航 -->
          <el-breadcrumb separator="›">
            <el-breadcrumb-item :to="{ path: '/dashboard' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item v-if="currentPageTitle">{{ currentPageTitle }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <button v-if="hasRole(['DM'])" class="header-icon-btn call-front-btn" @click="showCallDialog = true" title="呼叫前台">
            <i class="el-icon-bell"></i>
          </button>
          <button class="header-icon-btn" @click="toggleFullscreen">
            <i class="el-icon-full-screen"></i>
          </button>
          <button class="header-icon-btn theme-toggle-btn" @click="toggleTheme" :title="isDarkTheme ? '切换为亮色模式' : '切换为暗色模式'">
            <i :class="isDarkTheme ? 'el-icon-sunny' : 'el-icon-moon'"></i>
          </button>
          <el-dropdown @command="handleUserCommand">
            <span class="user-info">
              <div class="user-avatar-ring">
                <el-avatar :size="30" icon="el-icon-user-solid" class="user-avatar"></el-avatar>
              </div>
              <span class="user-name">{{ user.realName || user.username }}</span>
              <i class="el-icon-arrow-down"></i>
            </span>
            <el-dropdown-menu slot="dropdown">
              <el-dropdown-item command="profile" icon="el-icon-user">个人中心</el-dropdown-item>
              <el-dropdown-item command="logout" icon="el-icon-switch-button" divided>退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>
        </div>
      </el-header>
      <el-container class="main-container">
        <!-- 侧边栏 — 毛玻璃深渊风格 -->
        <el-aside :width="isCollapsed ? '64px' : '230px'" class="sidebar">
          <el-menu
            :default-active="activeMenu"
            :default-openeds="openedMenus"
            :collapse="isCollapsed"
            class="sidebar-menu"
            background-color="transparent"
            text-color="rgba(157, 150, 163, 0.8)"
            active-text-color="#E2C082"
            @select="handleMenuSelect"
          >
            <!-- 运营中心 -->
            <el-submenu index="group-ops" v-if="hasRole(['ADMIN', 'DM', 'USER'])">
              <template slot="title">
                <i class="el-icon-s-data"></i>
                <span slot="title">运营中心</span>
              </template>
              <el-menu-item index="/dashboard">
                <i class="el-icon-s-home"></i>
                <span slot="title">首页仪表盘</span>
              </el-menu-item>
              <el-menu-item index="/statistics" v-if="hasRole(['ADMIN'])">
                <i class="el-icon-pie-chart"></i>
                <span slot="title">统计分析</span>
              </el-menu-item>
            </el-submenu>

            <!-- 剧本与场次 -->
            <el-submenu index="group-game" v-if="hasRole(['ADMIN', 'DM', 'USER'])">
              <template slot="title">
                <i class="el-icon-film"></i>
                <span slot="title">剧本与场次</span>
              </template>
              <el-menu-item index="/script" v-if="hasRole(['ADMIN', 'DM'])">
                <i class="el-icon-document"></i>
                <span slot="title">剧本管理</span>
              </el-menu-item>
              <el-menu-item index="/session" v-if="hasRole(['ADMIN', 'DM'])">
                <i class="el-icon-time"></i>
                <span slot="title">场次管理</span>
              </el-menu-item>
              <el-menu-item index="/reservation" v-if="hasRole(['ADMIN', 'USER'])">
                <i class="el-icon-tickets"></i>
                <span slot="title">预约管理</span>
              </el-menu-item>
              <el-menu-item index="/notification" v-if="hasRole(['ADMIN', 'DM'])">
                <i class="el-icon-bell"></i>
                <span slot="title">通知中心</span>
              </el-menu-item>
            </el-submenu>

            <!-- 人员管理 -->
            <el-submenu index="group-people" v-if="hasRole(['ADMIN'])">
              <template slot="title">
                <i class="el-icon-user"></i>
                <span slot="title">人员管理</span>
              </template>
              <el-menu-item index="/user">
                <i class="el-icon-user-solid"></i>
                <span slot="title">用户管理</span>
              </el-menu-item>
              <el-menu-item index="/dm">
                <i class="el-icon-s-custom"></i>
                <span slot="title">DM管理</span>
              </el-menu-item>
              <el-menu-item index="/dm-schedule">
                <i class="el-icon-date"></i>
                <span slot="title">DM排班</span>
              </el-menu-item>
            </el-submenu>

            <!-- 交易中心 -->
            <el-submenu index="group-trade" v-if="hasRole(['ADMIN', 'USER'])">
              <template slot="title">
                <i class="el-icon-shopping-bag-1"></i>
                <span slot="title">交易中心</span>
              </template>
              <el-menu-item index="/order">
                <i class="el-icon-shopping-cart-full"></i>
                <span slot="title">订单管理</span>
              </el-menu-item>
              <el-menu-item index="/evaluation">
                <i class="el-icon-star-on"></i>
                <span slot="title">评价系统</span>
              </el-menu-item>
            </el-submenu>

            <!-- 系统设置 -->
            <el-submenu index="group-system" v-if="hasRole(['ADMIN'])">
              <template slot="title">
                <i class="el-icon-setting"></i>
                <span slot="title">系统设置</span>
              </template>
              <el-menu-item index="/store">
                <i class="el-icon-office-building"></i>
                <span slot="title">门店管理</span>
              </el-menu-item>
              <el-menu-item index="/system-config">
                <i class="el-icon-s-tools"></i>
                <span slot="title">系统配置</span>
              </el-menu-item>
              <el-menu-item index="/operation-log">
                <i class="el-icon-notebook-2"></i>
                <span slot="title">操作日志</span>
              </el-menu-item>
            </el-submenu>
          </el-menu>
        </el-aside>
        <!-- 主内容区 -->
        <el-main class="main-content">
          <div class="page-content">
            <slot></slot>
          </div>
        </el-main>
      </el-container>
    </el-container>

    <!-- WebSocket 连接状态指示器 -->
    <div class="ws-status-dot" :class="{ 'ws-online': wsConnected }" :title="wsConnected ? 'WebSocket 已连接' : 'WebSocket 未连接'"></div>

    <!-- DM 呼叫前台对话框 -->
    <el-dialog title="呼叫前台" :visible.sync="showCallDialog" width="420px" custom-class="call-front-dialog">
      <el-form :model="callForm" label-width="80px">
        <el-form-item label="包厢号">
          <el-input v-model="callForm.room" placeholder="请输入包厢号" clearable></el-input>
        </el-form-item>
        <el-form-item label="需求描述">
          <el-input v-model="callForm.content" type="textarea" :rows="3" placeholder="请描述您的需求，如：需要饮用水、空调调节等"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button size="small" @click="showCallDialog = false">取消</el-button>
        <el-button type="primary" size="small" @click="handleCallFront" :loading="callLoading">发送呼叫</el-button>
      </div>
    </el-dialog>

    <!-- 管理员接收呼叫通知弹窗 -->
    <el-dialog title="DM 呼叫前台" :visible.sync="adminNotifyVisible" width="440px" custom-class="admin-notify-dialog">
      <div class="admin-notify-content">
        <div class="notify-icon"><i class="el-icon-warning-outline"></i></div>
        <p class="notify-message">{{ adminNotifyMessage }}</p>
        <p class="notify-time">{{ adminNotifyTime }}</p>
      </div>
      <div slot="footer">
        <el-button type="primary" size="small" @click="adminNotifyVisible = false">已知晓</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { connectWebSocket, disconnectWebSocket, onMessage, sendMessage } from '@/utils/websocket';

export default {
  name: 'Layout',
  data() {
    return {
      user: JSON.parse(localStorage.getItem('user') || '{}'),
      systemTitle: '剧本杀调度管理',
      activeMenu: '/dashboard',
      openedMenus: ['group-ops'],
      isCollapsed: false,
      isDarkTheme: true,
      // WebSocket 相关状态
      wsConnected: false,
      wsUnsubscribe: null,
      showCallDialog: false,
      callForm: { room: '', content: '' },
      callLoading: false,
      adminNotifyVisible: false,
      adminNotifyMessage: '',
      adminNotifyTime: '',
      pageTitleMap: {
        '/dashboard': '首页仪表盘',
        '/user': '用户管理',
        '/script': '剧本管理',
        '/session': '场次管理',
        '/dm': 'DM管理',
        '/dm-schedule': 'DM排班',
        '/order': '订单管理',
        '/evaluation': '评价系统',
        '/statistics': '统计分析',
        '/store': '门店管理',
        '/system-config': '系统配置',
        '/reservation': '预约管理',
        '/operation-log': '操作日志'
      }
    }
  },
  computed: {
    currentPageTitle() {
      return this.pageTitleMap[this.$route.path] || ''
    }
  },
  mounted() {
    if (!localStorage.getItem('token')) {
      this.$router.push('/login')
      return
    }
    this.loadMenuState()
    this.activeMenu = this.$route.path
    this.loadSystemTitle()
    this.loadTheme()
    // 建立 WebSocket 连接
    this.initWebSocket()
  },
  beforeDestroy() {
    this.saveMenuState()
    window.removeEventListener('beforeunload', this.handleBeforeUnload)
    // 断开 WebSocket
    if (this.wsUnsubscribe) this.wsUnsubscribe();
    disconnectWebSocket();
  },
  created() {
    window.addEventListener('beforeunload', this.handleBeforeUnload)
  },
  watch: {
    '$route.path'(newPath) {
      this.activeMenu = newPath;
      // 路由发生变化时尝试重新拉取用户信息以保持鉴权状态的新鲜度
      try {
        const u = localStorage.getItem('user');
        if (u) {
          this.user = JSON.parse(u);
        }
      } catch(e) {}
    }
  },
  methods: {
    hasRole(roles) {
      // 兼容如果没有从 storage 中解析出 user 的场景，再次尝试防御式获取
      let currentUser = this.user;
      if (!currentUser || !currentUser.role) {
        try {
          currentUser = JSON.parse(localStorage.getItem('user') || '{}');
        } catch(e) {}
      }
      if (!currentUser || !currentUser.role) return false;
      const currentUserRole = String(currentUser.role).toUpperCase();
      return roles.some(role => String(role).toUpperCase() === currentUserRole);
    },
    toggleSidebar() {
      this.isCollapsed = !this.isCollapsed
      localStorage.setItem('sidebarCollapsed', this.isCollapsed)
    },
    toggleFullscreen() {
      if (!document.fullscreenElement) {
        document.documentElement.requestFullscreen()
      } else {
        document.exitFullscreen()
      }
    },
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
    handleUserCommand(cmd) {
      if (cmd === 'logout') {
        localStorage.removeItem('token');
        localStorage.removeItem('user');
        this.$router.push('/login');
      } else if (cmd === 'profile') {
        this.$router.push('/profile')
      }
    },
    handleMenuSelect(key) {
      if (this.$route.path !== key) {
        this.$router.push(key)
      }
    },
    handleMenuOpen(key) {
      if (!this.openedMenus.includes(key)) {
        this.openedMenus.push(key)
      }
      this.saveMenuState()
    },
    handleMenuClose(key) {
      const index = this.openedMenus.indexOf(key)
      if (index > -1) {
        this.openedMenus.splice(index, 1)
      }
      this.saveMenuState()
    },
    loadMenuState() {
      const savedState = localStorage.getItem('menuState')
      if (savedState) {
        try {
          const state = JSON.parse(savedState)
          this.openedMenus = state.openedMenus || ['group-ops']
        } catch (error) {
          console.error('加载菜单状态失败:', error)
        }
      }
      this.isCollapsed = localStorage.getItem('sidebarCollapsed') === 'true'
    },
    saveMenuState() {
      localStorage.setItem('menuState', JSON.stringify({
        openedMenus: this.openedMenus
      }))
    },
    loadSystemTitle() {
      this.$axios.get('/system/config/runtime')
        .then(res => {
          if (res && res.code === 200 && res.data && res.data.system_name) {
            this.systemTitle = String(res.data.system_name)
          }
        })
        .catch(() => {})
    },
    logout() {
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      this.$router.push('/login')
    },
    handleBeforeUnload() {
      this.saveMenuState()
    },
    // ==================== WebSocket 相关方法 ====================
    initWebSocket() {
      if (!this.user || !this.user.id) return;
      const role = this.user.role ? String(this.user.role).toUpperCase() : 'UNKNOWN';
      connectWebSocket(this.user.id, role);
      this.wsConnected = true;

      // 注册全局消息监听
      this.wsUnsubscribe = onMessage((data) => {
        this.handleWsMessage(data);
      });
    },
    handleWsMessage(data) {
      if (!data || !data.type) return;

      if (data.type === 'connect') {
        this.wsConnected = true;
        console.log('[Layout] WebSocket 连接确认');
      } else if (data.type === 'CALL_SERVICE') {
        if (this.hasRole(['ADMIN'])) {
          this.adminNotifyMessage = data.content || data.message || 'DM正在呼叫前台';
          this.adminNotifyTime = data.timestamp ? new Date(data.timestamp).toLocaleString() : new Date().toLocaleString();
          this.adminNotifyVisible = true;
          this.$notify({
            title: 'DM呼叫前台',
            message: this.adminNotifyMessage,
            type: 'warning',
            duration: 0
          });
        }
      } else if (data.type === 'CALL_CONFIRM') {
        this.$message.success(data.message || '呼叫已发送，请等待前台响应');
      }
    },
    handleCallFront() {
      if (!this.callForm.content || !this.callForm.content.trim()) {
        this.$message.warning('请填写需求描述');
        return;
      }
      this.callLoading = true;
      const sent = sendMessage({
        type: 'CALL_SERVICE',
        userId: this.user.id,
        role: this.user.role,
        room: this.callForm.room,
        content: this.callForm.content
      });
      this.callLoading = false;
      if (sent) {
        this.showCallDialog = false;
        this.callForm = { room: '', content: '' };
      } else {
        this.$message.error('WebSocket未连接，无法发送呼叫');
      }
    }
  }
}
</script>

<style scoped>
/* ============================================================
   Layout 高级毛玻璃体系 — Midnight Velvet & Warm Gold
   ============================================================ */

.layout-container {
  height: 100vh;
  display: flex;
  flex-direction: column;
  position: relative;
  z-index: 2;
}

/* ===== 顶部导航栏 — 磨砂玻璃 ===== */
.top-header {
  background: rgba(26, 25, 34, 0.75);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  color: rgba(235, 230, 225, 0.95);
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 24px;
  height: 56px !important;
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.3);
  z-index: 100;
  position: relative;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 14px;
}

.collapse-btn {
  cursor: pointer;
  font-size: 18px;
  color: rgba(157, 150, 163, 0.7);
  background: none;
  border: none;
  padding: 6px;
  border-radius: 8px;
  transition: all 0.3s;
  outline: none;
}
.collapse-btn:hover {
  color: #E2C082;
  background: rgba(226, 192, 130, 0.08);
}

.system-title {
  margin: 0;
  font-size: 17px;
  font-weight: 700;
  letter-spacing: 2.5px;
  display: flex;
  align-items: center;
  gap: 8px;
  white-space: nowrap;
}
.title-icon {
  font-size: 20px;
  background: linear-gradient(135deg, #E2C082, #9C7D43);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  filter: drop-shadow(0 0 6px rgba(226, 192, 130, 0.3));
}
.title-text {
  background: linear-gradient(135deg, #E2C082, #9C7D43);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.header-center {
  flex: 1;
  display: flex;
  justify-content: center;
}
.header-center .el-breadcrumb {
  font-size: 13px;
  font-family: 'Inter', sans-serif;
}
.header-center >>> .el-breadcrumb__item .el-breadcrumb__inner {
  color: rgba(157, 150, 163, 0.6) !important;
  font-weight: 400;
}
.header-center >>> .el-breadcrumb__item:last-child .el-breadcrumb__inner {
  color: rgba(230, 225, 220, 0.8) !important;
  font-weight: 500;
}
.header-center >>> .el-breadcrumb__separator {
  color: rgba(75, 70, 83, 0.6) !important;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

.header-icon-btn {
  cursor: pointer;
  font-size: 17px;
  color: rgba(157, 150, 163, 0.6);
  background: none;
  border: none;
  padding: 8px;
  border-radius: 10px;
  transition: all 0.3s;
  outline: none;
}
.header-icon-btn:hover {
  color: #E2C082;
  background: rgba(226, 192, 130, 0.08);
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  color: rgba(230, 225, 220, 0.8);
  padding: 4px 8px;
  border-radius: 10px;
  transition: all 0.3s;
}
.user-info:hover {
  background: rgba(226, 192, 130, 0.06);
}
.user-avatar-ring {
  padding: 2px;
  border-radius: 50%;
  background: linear-gradient(135deg, rgba(226, 192, 130, 0.4), rgba(163, 130, 75, 0.2));
}
.user-avatar {
  background: rgba(60, 55, 70, 0.8) !important;
  font-size: 14px;
}
.user-name {
  font-size: 13px;
  font-weight: 500;
}

/* ===== 主容器 ===== */
.main-container {
  flex: 1;
  overflow: hidden;
}

/* ===== 侧边栏 — 深渊磨砂 ===== */
.sidebar {
  background: rgba(24, 22, 30, 0.8);
  backdrop-filter: blur(16px);
  -webkit-backdrop-filter: blur(16px);
  overflow-y: auto;
  overflow-x: hidden;
  transition: width 0.35s cubic-bezier(0.25, 0.8, 0.25, 1);
  border-right: 1px solid rgba(255, 255, 255, 0.04);
  position: relative;
}
/* 侧边栏右侧微弱暗金光带 */
.sidebar::after {
  content: '';
  position: absolute;
  top: 0;
  right: 0;
  width: 1px;
  height: 100%;
  background: linear-gradient(180deg, transparent 0%, rgba(226, 192, 130, 0.1) 50%, transparent 100%);
}

.sidebar::-webkit-scrollbar {
  width: 3px;
}
.sidebar::-webkit-scrollbar-thumb {
  background: rgba(157, 150, 163, 0.15);
  border-radius: 10px;
}

.sidebar-menu {
  border-right: none !important;
  padding-top: 12px;
  padding-bottom: 20px;
}

/* 菜单项样式 */
.sidebar-menu >>> .el-menu-item {
  height: 42px;
  line-height: 42px;
  border-radius: 10px;
  margin: 2px 10px;
  transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
  font-size: 13px;
  font-weight: 400;
  letter-spacing: 0.5px;
}
.sidebar-menu >>> .el-menu-item i {
  font-size: 16px;
  opacity: 0.6;
  transition: all 0.3s;
}
.sidebar-menu >>> .el-menu-item:hover {
  background: rgba(226, 192, 130, 0.06) !important;
}
.sidebar-menu >>> .el-menu-item:hover i {
  opacity: 1;
  color: #D6B77A;
}
.sidebar-menu >>> .el-menu-item.is-active {
  background: rgba(226, 192, 130, 0.12) !important;
  color: #E2C082 !important;
  font-weight: 500;
  border-left: 2px solid #A3824B;
}
.sidebar-menu >>> .el-menu-item.is-active i {
  opacity: 1;
  color: #E2C082;
}

.sidebar-menu >>> .el-submenu__title {
  height: 42px;
  line-height: 42px;
  border-radius: 10px;
  margin: 2px 10px;
  transition: all 0.3s;
  font-size: 13px;
  font-weight: 500;
  letter-spacing: 0.5px;
}
.sidebar-menu >>> .el-submenu__title:hover {
  background: rgba(226, 192, 130, 0.04) !important;
}
.sidebar-menu >>> .el-submenu__title i {
  color: rgba(157, 150, 163, 0.5);
  font-size: 16px;
}
.sidebar-menu >>> .el-submenu .el-menu {
  background: transparent !important;
}
.sidebar-menu >>> .el-submenu .el-menu-item {
  padding-left: 50px !important;
  font-size: 12.5px;
}

/* ===== 主内容区 ===== */
.main-content {
  background: transparent;
  overflow-y: auto;
  padding: 24px;
  position: relative;
}

.page-content {
  min-height: calc(100vh - 104px);
  position: relative;
  z-index: 2;
}

/* 侧边栏折叠状态 */
.sidebar-collapsed .sidebar-menu >>> .el-submenu__title span,
.sidebar-collapsed .sidebar-menu >>> .el-menu-item span {
  display: none;
}

/* ======== Element UI 全局暗色主题覆盖 ======== */

/* 卡片 — 高级毛玻璃 */
.layout-container >>> .el-card {
  background: rgba(35, 32, 45, 0.55);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 16px;
  color: rgba(230, 225, 220, 0.9);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.25), inset 0 1px 1px rgba(255, 255, 255, 0.04);
  transition: all 0.4s cubic-bezier(0.25, 0.8, 0.25, 1);
}
.layout-container >>> .el-card:hover {
  border-color: rgba(226, 192, 130, 0.12);
  transform: translateY(-2px);
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.35), inset 0 1px 1px rgba(255, 255, 255, 0.06);
}
.layout-container >>> .el-card__header {
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
  color: rgba(230, 225, 220, 0.9);
  font-family: 'Noto Serif SC', serif;
  font-size: 15px;
  font-weight: 600;
}

/* 表格 — 深渊透明 */
.layout-container >>> .el-table,
.layout-container >>> .el-table__expanded-cell {
  background-color: transparent !important;
  color: rgba(230, 225, 220, 0.85);
}
.layout-container >>> .el-table th,
.layout-container >>> .el-table tr {
  background-color: transparent !important;
}
.layout-container >>> .el-table th {
  background-color: rgba(35, 32, 45, 0.6) !important;
  color: rgba(180, 175, 185, 0.9);
  border-bottom: 1px solid rgba(255, 255, 255, 0.05) !important;
  font-family: 'Inter', sans-serif;
  font-weight: 600;
  font-size: 12px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}
.layout-container >>> .el-table td {
  border-bottom: 1px solid rgba(255, 255, 255, 0.03) !important;
}
.layout-container >>> .el-table--enable-row-hover .el-table__body tr:hover > td.el-table__cell,
.layout-container >>> .el-table__body tr.hover-row > td.el-table__cell {
  background-color: rgba(226, 192, 130, 0.05) !important;
}
.layout-container >>> .el-table::before,
.layout-container >>> .el-table--border::after {
  background-color: rgba(255, 255, 255, 0.04);
}
.layout-container >>> .el-table__empty-block {
  background: transparent;
}

/* 对话框 — 高级毛玻璃 */
.layout-container >>> .el-dialog {
  background: rgba(32, 30, 40, 0.95);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 20px;
  box-shadow: 0 24px 80px rgba(0, 0, 0, 0.6), inset 0 1px 0 rgba(255, 255, 255, 0.05);
  backdrop-filter: blur(24px);
  -webkit-backdrop-filter: blur(24px);
}
.layout-container >>> .el-dialog__title {
  color: #E2C082;
  font-weight: 600;
}
.layout-container >>> .el-dialog__header {
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
  padding: 20px 24px;
}
.layout-container >>> .el-dialog__body {
  color: rgba(200, 195, 205, 0.9);
  padding: 24px;
}
.layout-container >>> .el-dialog__headerbtn .el-dialog__close {
  color: rgba(157, 150, 163, 0.5);
}
.layout-container >>> .el-dialog__headerbtn .el-dialog__close:hover {
  color: #E2C082;
}

/* 表单输入框 */
.layout-container >>> .el-form-item__label {
  color: rgba(157, 150, 163, 0.7);
  font-size: 13px;
}
.layout-container >>> .el-input__inner,
.layout-container >>> .el-textarea__inner {
  background: rgba(20, 19, 26, 0.6);
  border: 1px solid rgba(255, 255, 255, 0.1);
  color: rgba(235, 230, 225, 0.95);
  border-radius: 10px;
  transition: all 0.3s;
}
.layout-container >>> .el-input__inner:focus,
.layout-container >>> .el-textarea__inner:focus {
  border-color: rgba(226, 192, 130, 0.4);
  box-shadow: 0 0 0 3px rgba(226, 192, 130, 0.08);
}
.layout-container >>> .el-input__inner::placeholder {
  color: rgba(157, 150, 163, 0.35);
}
.layout-container >>> .el-select .el-input__inner {
  background: rgba(14, 13, 17, 0.5);
  border: 1px solid rgba(255, 255, 255, 0.06);
  color: rgba(230, 225, 220, 0.9);
}

/* InputNumber 全局修复：
   在窄列场景下（如 3 列表单），默认按钮宽度会挤压中间输入区导致数值不可见 */
.layout-container >>> .el-input-number {
  width: 100%;
  max-width: 100%;
}
.layout-container >>> .el-input-number .el-input__inner {
  padding-left: 34px;
  padding-right: 34px;
  text-align: center;
}
.layout-container >>> .el-input-number__decrease,
.layout-container >>> .el-input-number__increase {
  width: 30px;
  color: rgba(235, 230, 225, 0.78);
  background: rgba(255, 255, 255, 0.04);
  border-color: rgba(255, 255, 255, 0.08);
}
.layout-container >>> .el-input-number__decrease:hover,
.layout-container >>> .el-input-number__increase:hover {
  color: #E2C082;
  background: rgba(226, 192, 130, 0.08);
}

/* 主按钮 — 暗金渐变 */
.layout-container >>> .el-button--primary {
  background: linear-gradient(135deg, rgba(163, 130, 75, 0.8), rgba(140, 115, 85, 0.7));
  border: 1px solid rgba(226, 192, 130, 0.2);
  color: #FFF;
  letter-spacing: 1px;
  border-radius: 10px;
  font-weight: 500;
  transition: all 0.3s;
}
.layout-container >>> .el-button--primary:hover {
  background: linear-gradient(135deg, rgba(163, 130, 75, 0.95), rgba(140, 115, 85, 0.85));
  border-color: rgba(226, 192, 130, 0.4);
  box-shadow: 0 4px 16px rgba(226, 192, 130, 0.15);
  transform: translateY(-1px);
}

/* 线框按钮 */
.layout-container >>> .el-button--default {
  background: transparent;
  border: 1px solid rgba(255, 255, 255, 0.08);
  color: rgba(157, 150, 163, 0.8);
  border-radius: 10px;
  transition: all 0.3s;
}
.layout-container >>> .el-button--default:hover {
  border-color: rgba(226, 192, 130, 0.3);
  color: #D6B77A;
  background: rgba(226, 192, 130, 0.05);
}

/* 分页组件 */
.layout-container >>> .el-pagination {
  color: rgba(157, 150, 163, 0.6);
}
.layout-container >>> .el-pagination .btn-prev,
.layout-container >>> .el-pagination .btn-next,
.layout-container >>> .el-pager li {
  background: transparent;
  color: rgba(157, 150, 163, 0.5);
  border: 1px solid rgba(255, 255, 255, 0.06);
  border-radius: 8px;
  transition: all 0.3s;
}
.layout-container >>> .el-pager li:hover {
  color: #D6B77A;
  border-color: rgba(226, 192, 130, 0.2);
}
.layout-container >>> .el-pager li.active {
  background: linear-gradient(135deg, rgba(163, 130, 75, 0.6), rgba(140, 115, 85, 0.4));
  color: white;
  border-color: transparent;
}

.layout-container >>> .el-tag {
  border-radius: 6px;
  border-color: rgba(255, 255, 255, 0.06);
  background-color: rgba(54, 50, 61, 0.3);
}

.layout-container >>> .el-statistic .head {
  color: rgba(157, 150, 163, 0.6) !important;
}
.layout-container >>> .el-statistic .con .number {
  color: rgba(230, 225, 220, 0.9) !important;
}

/* ============================================================
   亮色主题覆盖 (Light Theme)
   ============================================================ */

/* 主题切换按钮特殊样式 */
.theme-toggle-btn {
  position: relative;
}
.theme-toggle-btn i {
  transition: transform 0.4s, color 0.3s;
}
.theme-toggle-btn:hover i {
  transform: rotate(30deg);
}

/* ===== 以下通过 body.light-theme 穿透到 scoped 内部 ===== */
</style>

<style>
/* Layout 亮色覆盖 — 非 scoped 以便匹配 body.light-theme */

/* 顶栏 */
body.light-theme .top-header {
  background: rgba(255, 255, 255, 0.85) !important;
  border-bottom: 1px solid #E8E8ED !important;
  box-shadow: 0 1px 8px rgba(0, 0, 0, 0.04) !important;
  backdrop-filter: blur(16px) !important;
}
body.light-theme .collapse-btn,
body.light-theme .header-icon-btn {
  color: #636E72 !important;
}
body.light-theme .collapse-btn:hover,
body.light-theme .header-icon-btn:hover {
  color: #6C5CE7 !important;
  background: rgba(108, 92, 231, 0.06) !important;
}
body.light-theme .system-title .title-text,
body.light-theme .system-title .title-icon {
  background: linear-gradient(135deg, #6C5CE7, #4834D4) !important;
  -webkit-background-clip: text !important;
  -webkit-text-fill-color: transparent !important;
}
body.light-theme .el-breadcrumb__item .el-breadcrumb__inner {
  color: #A0A5AA !important;
}
body.light-theme .el-breadcrumb__item:last-child .el-breadcrumb__inner {
  color: #2D3436 !important;
}
body.light-theme .el-breadcrumb__separator {
  color: #DFE6E9 !important;
}
body.light-theme .user-info {
  color: #2D3436 !important;
}
body.light-theme .user-info:hover {
  background: rgba(108, 92, 231, 0.04) !important;
}
body.light-theme .user-avatar-ring {
  background: linear-gradient(135deg, rgba(108, 92, 231, 0.3), rgba(0, 210, 211, 0.2)) !important;
}
body.light-theme .user-avatar {
  background: #E8E8ED !important;
  color: #636E72 !important;
}

/* 通知角标 */
/* 侧边栏 */
body.light-theme .sidebar {
  background: rgba(255, 255, 255, 0.92) !important;
  border-right: 1px solid #E8E8ED !important;
  backdrop-filter: blur(12px) !important;
}
body.light-theme .sidebar::after {
  background: linear-gradient(180deg, transparent 0%, rgba(108, 92, 231, 0.08) 50%, transparent 100%) !important;
}
body.light-theme .sidebar-menu {
  background: transparent !important;
}
body.light-theme .sidebar-menu .el-submenu__title {
  color: #636E72 !important;
  background: transparent !important;
}
body.light-theme .sidebar-menu .el-submenu__title i {
  color: #A0A5AA !important;
}
body.light-theme .sidebar-menu .el-submenu__title:hover {
  background: rgba(108, 92, 231, 0.04) !important;
}
body.light-theme .sidebar-menu .el-menu-item {
  color: #636E72 !important;
  background: transparent !important;
}
body.light-theme .sidebar-menu .el-menu-item i {
  color: #A0A5AA !important;
}
body.light-theme .sidebar-menu .el-menu-item:hover {
  background: rgba(108, 92, 231, 0.06) !important;
  color: #6C5CE7 !important;
}
body.light-theme .sidebar-menu .el-menu-item:hover i {
  color: #6C5CE7 !important;
}
body.light-theme .sidebar-menu .el-menu-item.is-active {
  background: rgba(108, 92, 231, 0.08) !important;
  color: #6C5CE7 !important;
  border-left-color: #6C5CE7 !important;
}
body.light-theme .sidebar-menu .el-menu-item.is-active i {
  color: #6C5CE7 !important;
}
body.light-theme .sidebar-menu .el-submenu .el-menu {
  background: transparent !important;
}

/* 卡片 */
body.light-theme .el-card {
  background: #FFFFFF !important;
  border: 1px solid #E8E8ED !important;
  color: #2D3436 !important;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04) !important;
  backdrop-filter: none !important;
}
body.light-theme .el-card:hover {
  border-color: rgba(108, 92, 231, 0.2) !important;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.06) !important;
}
body.light-theme .el-card__header {
  border-bottom: 1px solid #F0F0F5 !important;
  color: #2D3436 !important;
}

/* 表格 */
body.light-theme .el-table,
body.light-theme .layout-container .el-table {
  background-color: transparent !important;
  color: #2D3436 !important;
}
body.light-theme .el-table th,
body.light-theme .el-table tr,
body.light-theme .layout-container .el-table th,
body.light-theme .layout-container .el-table tr {
  background-color: transparent !important;
}
body.light-theme .el-table th,
body.light-theme .layout-container .el-table th {
  background-color: #FAFBFC !important;
  color: #636E72 !important;
  border-bottom: 1px solid #E8E8ED !important;
}
body.light-theme .el-table td,
body.light-theme .layout-container .el-table td {
  border-bottom: 1px solid #F0F0F5 !important;
  color: #2D3436 !important;
}
body.light-theme .el-table--enable-row-hover .el-table__body tr:hover > td.el-table__cell,
body.light-theme .el-table__body tr.hover-row > td.el-table__cell {
  background-color: rgba(108, 92, 231, 0.03) !important;
}
body.light-theme .el-table::before,
body.light-theme .el-table--border::after {
  background-color: #E8E8ED !important;
}
/* 固定列 / 修复固定列的暗色底 */
body.light-theme .el-table__fixed,
body.light-theme .el-table__fixed-right {
  box-shadow: none !important;
}
body.light-theme .el-table__fixed-right-patch {
  background-color: #FAFBFC !important;
}

/* 对话框 */
body.light-theme .el-dialog {
  background: #FFFFFF !important;
  border: 1px solid #E8E8ED !important;
  box-shadow: 0 16px 48px rgba(0, 0, 0, 0.1) !important;
  backdrop-filter: none !important;
}
body.light-theme .el-dialog__title {
  color: #6C5CE7 !important;
}
body.light-theme .el-dialog__header {
  border-bottom: 1px solid #F0F0F5 !important;
}
body.light-theme .el-dialog__body {
  color: #636E72 !important;
}

/* 表单 */
body.light-theme .el-form-item__label {
  color: #636E72 !important;
}
body.light-theme .el-input__inner,
body.light-theme .el-textarea__inner {
  background: #FAFBFC !important;
  border: 1px solid #E0E0E5 !important;
  color: #2D3436 !important;
}
body.light-theme .el-input__inner:focus,
body.light-theme .el-textarea__inner:focus {
  border-color: #6C5CE7 !important;
  box-shadow: 0 0 0 3px rgba(108, 92, 231, 0.1) !important;
}
body.light-theme .el-input__inner::placeholder,
body.light-theme .el-textarea__inner::placeholder {
  color: #B0B5BA !important;
}
body.light-theme .el-select .el-input__inner {
  background: #FAFBFC !important;
  border: 1px solid #E0E0E5 !important;
  color: #2D3436 !important;
}
body.light-theme .el-input-number {
  width: 100%;
  max-width: 100%;
}
body.light-theme .el-input-number .el-input__inner {
  padding-left: 34px !important;
  padding-right: 34px !important;
  text-align: center;
}
body.light-theme .el-input-number__decrease,
body.light-theme .el-input-number__increase {
  width: 30px;
  color: #636E72 !important;
  background: #F3F5F9 !important;
  border-color: #E0E0E5 !important;
}
body.light-theme .el-input-number__decrease:hover,
body.light-theme .el-input-number__increase:hover {
  color: #6C5CE7 !important;
  background: rgba(108, 92, 231, 0.08) !important;
}

/* 主按钮 */
body.light-theme .el-button--primary {
  background: linear-gradient(135deg, #6C5CE7, #A29BFE) !important;
  border: none !important;
  color: #FFF !important;
}
body.light-theme .el-button--primary:hover {
  background: linear-gradient(135deg, #5A4BD4, #8C7EFC) !important;
  box-shadow: 0 4px 12px rgba(108, 92, 231, 0.25) !important;
}

/* 线框按钮 */
body.light-theme .el-button--default {
  background: transparent !important;
  border: 1px solid #E0E0E5 !important;
  color: #636E72 !important;
}
body.light-theme .el-button--default:hover {
  border-color: #6C5CE7 !important;
  color: #6C5CE7 !important;
  background: rgba(108, 92, 231, 0.04) !important;
}

/* 分页 */
body.light-theme .el-pagination {
  color: #636E72 !important;
}
body.light-theme .el-pagination .btn-prev,
body.light-theme .el-pagination .btn-next,
body.light-theme .el-pager li {
  background: transparent !important;
  color: #636E72 !important;
  border: 1px solid #E0E0E5 !important;
}
body.light-theme .el-pager li:hover {
  color: #6C5CE7 !important;
  border-color: rgba(108, 92, 231, 0.3) !important;
}
body.light-theme .el-pager li.active {
  background: linear-gradient(135deg, #6C5CE7, #A29BFE) !important;
  color: white !important;
  border-color: transparent !important;
}

/* Tag */
body.light-theme .el-tag {
  border-color: #E0E0E5 !important;
  background-color: #F5F6FA !important;
  color: #636E72 !important;
}

/* Statistic */
body.light-theme .el-statistic .head {
  color: #A0A5AA !important;
}
body.light-theme .el-statistic .con .number {
  color: #2D3436 !important;
}

/* WebSocket 连接状态指示灯 */
.ws-status-dot {
  position: fixed;
  bottom: 16px;
  right: 16px;
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: #ff4757;
  box-shadow: 0 0 6px rgba(255, 71, 87, 0.6);
  z-index: 2001;
  transition: all 0.3s ease;
}
.ws-status-dot.ws-online {
  background: #2ed573;
  box-shadow: 0 0 8px rgba(46, 213, 115, 0.6);
}

/* 呼叫前台按钮 */
.call-front-btn {
  position: relative;
}
.call-front-btn i {
  animation: callPulse 2s ease-in-out infinite;
}
@keyframes callPulse {
  0%, 100% { opacity: 0.6; }
  50% { opacity: 1; }
}
.call-front-btn:hover i {
  animation: none;
  color: #E2C082 !important;
}

/* 管理员通知弹窗内容 */
.admin-notify-content {
  text-align: center;
  padding: 10px 0;
}
.admin-notify-content .notify-icon {
  font-size: 48px;
  color: #E2C082;
  margin-bottom: 16px;
}
.admin-notify-content .notify-message {
  font-size: 16px;
  color: rgba(230, 225, 220, 0.9);
  margin-bottom: 8px;
  line-height: 1.6;
}
.admin-notify-content .notify-time {
  font-size: 13px;
  color: rgba(157, 150, 163, 0.6);
}

/* ==================== 呼叫前台对话框 - 暗色主题（scoped 穿透） ==================== */
.layout-container >>> .call-front-dialog .el-dialog__title,
.layout-container >>> .admin-notify-dialog .el-dialog__title {
  color: #E2C082 !important;
}
.layout-container >>> .call-front-dialog .el-form-item__label,
.layout-container >>> .admin-notify-dialog .el-form-item__label {
  color: rgba(157, 150, 163, 0.8) !important;
}
.layout-container >>> .call-front-dialog .el-input__inner,
.layout-container >>> .call-front-dialog .el-textarea__inner {
  background: rgba(20, 19, 26, 0.6) !important;
  border: 1px solid rgba(255, 255, 255, 0.1) !important;
  color: rgba(235, 230, 225, 0.95) !important;
  border-radius: 10px !important;
}
.layout-container >>> .call-front-dialog .el-input__inner:focus,
.layout-container >>> .call-front-dialog .el-textarea__inner:focus {
  border-color: rgba(226, 192, 130, 0.4) !important;
  box-shadow: 0 0 0 3px rgba(226, 192, 130, 0.08) !important;
}
.layout-container >>> .call-front-dialog .el-input__inner::placeholder,
.layout-container >>> .call-front-dialog .el-textarea__inner::placeholder {
  color: rgba(157, 150, 163, 0.35) !important;
}
.layout-container >>> .call-front-dialog .el-button--primary {
  background: linear-gradient(135deg, rgba(163, 130, 75, 0.8), rgba(140, 115, 85, 0.7)) !important;
  border: 1px solid rgba(226, 192, 130, 0.2) !important;
  border-radius: 10px !important;
  letter-spacing: 1px;
}
.layout-container >>> .call-front-dialog .el-button--primary:hover {
  background: linear-gradient(135deg, rgba(163, 130, 75, 0.95), rgba(140, 115, 85, 0.85)) !important;
  box-shadow: 0 4px 16px rgba(226, 192, 130, 0.15) !important;
}
.layout-container >>> .call-front-dialog .el-button--default {
  background: transparent !important;
  border: 1px solid rgba(255, 255, 255, 0.08) !important;
  color: rgba(157, 150, 163, 0.8) !important;
  border-radius: 10px !important;
}
.layout-container >>> .call-front-dialog .el-button--default:hover {
  border-color: rgba(226, 192, 130, 0.3) !important;
  color: #D6B77A !important;
}

/* 管理员通知弹窗 - 暗色 */
.layout-container >>> .admin-notify-dialog .admin-notify-content .notify-icon {
  color: #E2C082 !important;
}
.layout-container >>> .admin-notify-dialog .admin-notify-content .notify-message {
  color: rgba(230, 225, 220, 0.9) !important;
}
.layout-container >>> .admin-notify-dialog .admin-notify-content .notify-time {
  color: rgba(157, 150, 163, 0.6) !important;
}
.layout-container >>> .admin-notify-dialog .el-button--primary {
  background: linear-gradient(135deg, rgba(163, 130, 75, 0.8), rgba(140, 115, 85, 0.7)) !important;
  border: 1px solid rgba(226, 192, 130, 0.2) !important;
  border-radius: 10px !important;
}

/* ==================== 呼叫前台对话框 - 亮色主题 ==================== */
body.light-theme .call-front-dialog .el-dialog__title,
body.light-theme .admin-notify-dialog .el-dialog__title {
  color: #6C5CE7 !important;
}
body.light-theme .call-front-dialog .el-form-item__label,
body.light-theme .admin-notify-dialog .el-form-item__label {
  color: #636E72 !important;
}
body.light-theme .call-front-dialog .el-input__inner,
body.light-theme .call-front-dialog .el-textarea__inner {
  background: #FAFBFC !important;
  border: 1px solid #E0E0E5 !important;
  color: #2D3436 !important;
  border-radius: 10px !important;
}
body.light-theme .call-front-dialog .el-input__inner:focus,
body.light-theme .call-front-dialog .el-textarea__inner:focus {
  border-color: #6C5CE7 !important;
  box-shadow: 0 0 0 3px rgba(108, 92, 231, 0.1) !important;
}
body.light-theme .call-front-dialog .el-input__inner::placeholder,
body.light-theme .call-front-dialog .el-textarea__inner::placeholder {
  color: #B0B5BA !important;
}
body.light-theme .call-front-dialog .el-button--primary {
  background: linear-gradient(135deg, #6C5CE7, #A29BFE) !important;
  border: none !important;
  border-radius: 10px !important;
}
body.light-theme .call-front-dialog .el-button--primary:hover {
  background: linear-gradient(135deg, #5A4BD4, #8C7EFC) !important;
  box-shadow: 0 4px 12px rgba(108, 92, 231, 0.25) !important;
}
body.light-theme .call-front-dialog .el-button--default {
  background: transparent !important;
  border: 1px solid #E0E0E5 !important;
  color: #636E72 !important;
  border-radius: 10px !important;
}
body.light-theme .call-front-dialog .el-button--default:hover {
  border-color: #6C5CE7 !important;
  color: #6C5CE7 !important;
}

/* 管理员通知弹窗 - 亮色 */
body.light-theme .admin-notify-dialog .admin-notify-content .notify-icon {
  color: #6C5CE7 !important;
}
body.light-theme .admin-notify-dialog .admin-notify-content .notify-message {
  color: #2D3436 !important;
}
body.light-theme .admin-notify-dialog .admin-notify-content .notify-time {
  color: #A0A5AA !important;
}
body.light-theme .admin-notify-dialog .el-button--primary {
  background: linear-gradient(135deg, #6C5CE7, #A29BFE) !important;
  border: none !important;
  border-radius: 10px !important;
}

/* ==================== 呼叫前台/通知弹窗 - 暗色容器（全局，匹配 append-to-body） ==================== */
.call-front-dialog .el-dialog,
.admin-notify-dialog .el-dialog {
  background: rgba(32, 30, 40, 0.95) !important;
  border: 1px solid rgba(255, 255, 255, 0.1) !important;
  border-radius: 20px !important;
  box-shadow: 0 24px 80px rgba(0, 0, 0, 0.6), inset 0 1px 0 rgba(255, 255, 255, 0.05) !important;
  backdrop-filter: blur(24px) !important;
}
.call-front-dialog .el-dialog__header,
.admin-notify-dialog .el-dialog__header {
  border-bottom: 1px solid rgba(255, 255, 255, 0.05) !important;
}
.call-front-dialog .el-dialog__body,
.admin-notify-dialog .el-dialog__body {
  color: rgba(200, 195, 205, 0.9) !important;
}
.call-front-dialog .el-dialog__headerbtn .el-dialog__close,
.admin-notify-dialog .el-dialog__headerbtn .el-dialog__close {
  color: rgba(157, 150, 163, 0.5) !important;
}
.call-front-dialog .el-dialog__headerbtn .el-dialog__close:hover,
.admin-notify-dialog .el-dialog__headerbtn .el-dialog__close:hover {
  color: #E2C082 !important;
}
.call-front-dialog .el-dialog__title,
.admin-notify-dialog .el-dialog__title {
  color: #E2C082 !important;
}
.call-front-dialog .el-form-item__label,
.admin-notify-dialog .el-form-item__label {
  color: rgba(157, 150, 163, 0.8) !important;
}

</style>
