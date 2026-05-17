<template>
  <div id="app">
    <!-- 环境辉光层：创造深邃空间折射感 -->
    <div class="ambient-glow">
      <div class="glow glow-purple"></div>
      <div class="glow glow-gold"></div>
      <div class="glow glow-teal"></div>
    </div>
    <!-- 噪点纹理层：消除纯色的数字廉价感 -->
    <div class="noise-overlay"></div>
    <transition name="page-fade" mode="out-in">
      <router-view />
    </transition>
  </div>
</template>

<script>
export default {
  name: 'App'
}
</script>

<style>
/* ============================================================
   剧本杀全局沉浸式美学系统 v3.0
   Midnight Velvet & Warm Gold — Ultra Premium Glassmorphism
   ============================================================ */

/* 字体引入：衬线体用于标题，无衬线用于数据 */
@import url('https://fonts.googleapis.com/css2?family=Noto+Serif+SC:wght@400;600;700;900&family=Inter:wght@300;400;500;600;700&display=swap');

/* ========== 全局重置 ========== */
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
  background: #1A1922;
  color: rgba(235, 230, 225, 0.95);
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  overflow-x: hidden;
}

#app {
  position: relative;
  min-height: 100vh;
}

/* ========== 环境辉光 (Ambient Glow) — 高级感灵魂 ========== */
.ambient-glow {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: 0;
  overflow: hidden;
}

.glow {
  position: absolute;
  border-radius: 50%;
  filter: blur(120px);
  opacity: 0.18;
  animation: glowPulse 12s ease-in-out infinite alternate;
}

/* 左上暗紫辉光 */
.glow-purple {
  width: 600px;
  height: 600px;
  background: radial-gradient(circle, #2A1B38 0%, transparent 70%);
  top: -15%;
  left: -10%;
  animation-delay: 0s;
}

/* 右下暗金辉光 */
.glow-gold {
  width: 500px;
  height: 500px;
  background: radial-gradient(circle, #332815 0%, transparent 70%);
  bottom: -10%;
  right: -5%;
  animation-delay: 4s;
}

/* 中心微青辉光 */
.glow-teal {
  width: 400px;
  height: 400px;
  background: radial-gradient(circle, #0D2B2B 0%, transparent 70%);
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  animation-delay: 8s;
  opacity: 0.10;
}

@keyframes glowPulse {
  0% { transform: scale(1); opacity: 0.15; }
  50% { transform: scale(1.15); opacity: 0.22; }
  100% { transform: scale(1); opacity: 0.15; }
}

/* ========== 噪点纹理叠加层 — 消除数字感 ========== */
.noise-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: 1;
  opacity: 0.03;
  background-image: url("data:image/svg+xml,%3Csvg viewBox='0 0 256 256' xmlns='http://www.w3.org/2000/svg'%3E%3Cfilter id='noiseFilter'%3E%3CfeTurbulence type='fractalNoise' baseFrequency='0.9' numOctaves='4' stitchTiles='stitch'/%3E%3C/filter%3E%3Crect width='100%25' height='100%25' filter='url(%23noiseFilter)'/%3E%3C/svg%3E");
  background-repeat: repeat;
  background-size: 128px 128px;
}

/* ========== 标题/Serif 字体系统 ========== */
h1, h2, h3, h4, .el-dialog__title {
  font-family: 'Noto Serif SC', serif !important;
  letter-spacing: 1.5px;
  font-weight: 600;
}

/* ========== Element UI 全局暗色深金主题覆盖 ========== */

/* 下拉框与弹出菜单 */
.el-select-dropdown, .el-dropdown-menu, .el-picker-panel, .el-message-box {
  background: rgba(32, 30, 40, 0.95) !important;
  border: 1px solid rgba(255, 255, 255, 0.1) !important;
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.5), inset 0 1px 0 rgba(255, 255, 255, 0.06) !important;
  backdrop-filter: blur(20px) !important;
  border-radius: 12px !important;
}
.el-select-dropdown__item, .el-dropdown-menu__item, .el-message-box__content, .el-date-table th {
  color: rgba(157, 150, 163, 0.9) !important;
}
.el-select-dropdown__item.hover, .el-select-dropdown__item:hover, .el-dropdown-menu__item:hover {
  background: rgba(188, 154, 98, 0.12) !important;
  color: #D6B77A !important;
}
.el-select-dropdown__item.selected {
  color: #E2C082 !important;
  font-weight: 600;
}
.el-dropdown-menu__item--divided::before {
  background: rgba(255, 255, 255, 0.06) !important;
}

/* 时间选择器 */
.el-date-picker__header-label, .el-date-table td.available .el-date-table-cell__text, .el-message-box__title {
  color: #E6E1DC !important;
}
.el-picker-panel__icon-btn {
  color: #9D96A3 !important;
}
.el-date-table th {
  border-bottom: 1px solid rgba(255, 255, 255, 0.06) !important;
}
.el-date-table td.current:not(.disabled) span {
  background-color: #A3824B !important;
  color: #fff !important;
}

/* Popper 箭头修复 */
.el-popper[x-placement^="bottom"] .popper__arrow::after,
.el-popper[x-placement^="top"] .popper__arrow::after {
  border-bottom-color: rgba(28, 27, 32, 0.95) !important;
  border-top-color: rgba(28, 27, 32, 0.95) !important;
}
.el-popper[x-placement^="bottom"] .popper__arrow,
.el-popper[x-placement^="top"] .popper__arrow {
  border-bottom-color: rgba(255, 255, 255, 0.06) !important;
  border-top-color: rgba(255, 255, 255, 0.06) !important;
}

/* 通知消息框 */
.el-message {
  background: rgba(28, 27, 32, 0.95) !important;
  border: 1px solid rgba(255, 255, 255, 0.08) !important;
  backdrop-filter: blur(20px) !important;
  border-radius: 12px !important;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.4) !important;
}
.el-message__content {
  color: #E6E1DC !important;
}
.el-message .el-message__closeBtn {
  color: #9D96A3 !important;
}

/* ========== 高级滚动条 ========== */
::-webkit-scrollbar {
  width: 5px;
  height: 5px;
}
::-webkit-scrollbar-track {
  background: transparent;
}
::-webkit-scrollbar-thumb {
  background: rgba(157, 150, 163, 0.2);
  border-radius: 10px;
}
::-webkit-scrollbar-thumb:hover {
  background: rgba(214, 183, 122, 0.3);
}

/* ========== 全局页面切换动画 ========== */
.page-fade-enter-active {
  transition: opacity 0.35s ease, transform 0.35s cubic-bezier(0.25, 0.8, 0.25, 1);
}
.page-fade-leave-active {
  transition: opacity 0.2s ease;
}
.page-fade-enter {
  opacity: 0;
  transform: translateY(8px);
}
.page-fade-leave-to {
  opacity: 0;
}

/* ========== 实用工具类 ========== */

/* 暗金渐变文字 */
.text-gold {
  background: linear-gradient(135deg, #E2C082 0%, #A3824B 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

/* 柔白文字 */
.text-muted {
  color: rgba(157, 150, 163, 0.7);
  font-size: 13px;
}

/* 高级毛玻璃卡片基类 */
.glass-panel {
  background: rgba(35, 32, 45, 0.55);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 16px;
  box-shadow:
    0 8px 32px 0 rgba(0, 0, 0, 0.37),
    inset 0 1px 1px rgba(255, 255, 255, 0.05);
  transition: all 0.4s cubic-bezier(0.25, 0.8, 0.25, 1);
}

.glass-panel:hover {
  background: rgba(50, 46, 62, 0.6);
  border-color: rgba(226, 192, 130, 0.15);
  transform: translateY(-2px);
  box-shadow:
    0 12px 40px 0 rgba(0, 0, 0, 0.45),
    inset 0 1px 1px rgba(255, 255, 255, 0.08);
}

/* 暗金发光边框效果 — 可叠加给特殊区域 */
.gold-glow {
  border: 1px solid rgba(226, 192, 130, 0.2) !important;
  box-shadow: 0 0 20px rgba(226, 192, 130, 0.06) !important;
}

/* ============================================================
   亮色主题覆盖 (Light Theme)
   body.light-theme 时生效
   ============================================================ */

body.light-theme {
  background: #F5F6FA;
  color: #2D3436;
}

/* 隐藏环境辉光层和噪点层 */
body.light-theme .ambient-glow {
  opacity: 0;
}
body.light-theme .noise-overlay {
  opacity: 0;
}

/* 标题文字颜色 */
body.light-theme h1,
body.light-theme h2,
body.light-theme h3,
body.light-theme h4 {
  color: #2D3436 !important;
  -webkit-text-fill-color: #2D3436 !important;
  background: none !important;
}

/* Element UI 弹出组件 — 亮色 */
body.light-theme .el-select-dropdown,
body.light-theme .el-dropdown-menu,
body.light-theme .el-picker-panel,
body.light-theme .el-message-box {
  background: #FFFFFF !important;
  border: 1px solid #E8E8ED !important;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.08) !important;
  backdrop-filter: none !important;
}
body.light-theme .el-select-dropdown__item,
body.light-theme .el-dropdown-menu__item {
  color: #636E72 !important;
}
body.light-theme .el-select-dropdown__item.hover,
body.light-theme .el-select-dropdown__item:hover,
body.light-theme .el-dropdown-menu__item:hover {
  background: rgba(108, 92, 231, 0.06) !important;
  color: #6C5CE7 !important;
}
body.light-theme .el-select-dropdown__item.selected {
  color: #6C5CE7 !important;
}

/* 通知消息框 — 亮色 */
body.light-theme .el-message {
  background: #FFFFFF !important;
  border: 1px solid #E8E8ED !important;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08) !important;
  backdrop-filter: none !important;
}
body.light-theme .el-message__content {
  color: #2D3436 !important;
}

/* 滚动条 — 亮色 */
body.light-theme ::-webkit-scrollbar-track {
  background: #F5F6FA;
}
body.light-theme ::-webkit-scrollbar-thumb {
  background: rgba(0, 0, 0, 0.12);
}
body.light-theme ::-webkit-scrollbar-thumb:hover {
  background: rgba(0, 0, 0, 0.2);
}

/* 毛玻璃基类 — 亮色 */
body.light-theme .glass-panel {
  background: rgba(255, 255, 255, 0.85);
  border: 1px solid rgba(0, 0, 0, 0.06);
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
  backdrop-filter: none;
}
body.light-theme .glass-panel:hover {
  background: rgba(255, 255, 255, 0.95);
  border-color: rgba(108, 92, 231, 0.15);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
}

/* ========== 子页面通用暗色区域覆盖 ========== */

/* .page-toolbar — script/session/dm/dm-schedule/evaluation/operation-log 等 */
body.light-theme .page-toolbar {
  background: #FFFFFF !important;
  border: 1px solid #E8E8ED !important;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04) !important;
}

/* 评价列表、用户统计等自定义暗色容器 */
body.light-theme .evaluation-list,
body.light-theme .user-stats,
body.light-theme .order-list,
body.light-theme .log-list {
  background: #FFFFFF !important;
  border: 1px solid #E8E8ED !important;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04) !important;
}

/* 暗金色标题 → 紫色 */
body.light-theme .evaluation-list h3,
body.light-theme .page-toolbar h3 {
  color: #6C5CE7 !important;
}

/* 所有 #36323D 暗色边框 → 亮色边框 */
body.light-theme [style*="36323D"],
body.light-theme [style*="border"] {
  border-color: #E8E8ED !important;
}

/* 深底色容器通用 — 覆盖所有 rgba(28,27,32) / rgba(30,27,38) 容器 */
body.light-theme .stats-card,
body.light-theme .info-card,
body.light-theme .detail-card,
body.light-theme .form-card,
body.light-theme .filter-card,
body.light-theme .data-card,
body.light-theme .config-card,
body.light-theme .list-card,
body.light-theme .table-card {
  background: #FFFFFF !important;
  border: 1px solid #E8E8ED !important;
  color: #2D3436 !important;
}

/* 暗金色文字 → 紫色 */
body.light-theme [style*="D6B77A"] {
  color: #6C5CE7 !important;
}

/* 改善所有 el-table 在亮色下的表现 */
body.light-theme .el-table__empty-text {
  color: #A0A5AA !important;
}
body.light-theme .el-table__empty-block {
  background: transparent !important;
}
body.light-theme .el-table .cell {
  color: #2D3436 !important;
}

/* 对话框遮罩 */
body.light-theme .el-dialog__wrapper .el-overlay,
body.light-theme .v-modal {
  background: rgba(0, 0, 0, 0.25) !important;
}

/* el-tabs */
body.light-theme .el-tabs__header {
  border-bottom-color: #E8E8ED !important;
}
body.light-theme .el-tabs__item {
  color: #636E72 !important;
}
body.light-theme .el-tabs__item.is-active {
  color: #6C5CE7 !important;
}
body.light-theme .el-tabs__active-bar {
  background: #6C5CE7 !important;
}

/* el-switch */
body.light-theme .el-switch__core {
  border-color: #E0E0E5 !important;
  background: #E0E0E5 !important;
}
body.light-theme .el-switch.is-checked .el-switch__core {
  background: #6C5CE7 !important;
  border-color: #6C5CE7 !important;
}

/* el-radio / el-checkbox */
body.light-theme .el-radio__label,
body.light-theme .el-checkbox__label {
  color: #636E72 !important;
}

/* el-date-picker */
body.light-theme .el-date-editor .el-input__inner {
  background: #FAFBFC !important;
  border: 1px solid #E0E0E5 !important;
  color: #2D3436 !important;
}

/* el-tooltip */
body.light-theme .el-tooltip__popper.is-dark {
  background: #2D3436 !important;
}

/* 金色渐变icon → 紫色 */
body.light-theme .stat-icon-wrap {
  background: rgba(108, 92, 231, 0.08) !important;
}
body.light-theme .stat-icon-wrap .stat-icon {
  color: #6C5CE7 !important;
}

/* 快捷操作 action-icon */
body.light-theme .action-icon-wrap {
  background: rgba(108, 92, 231, 0.06) !important;
}
body.light-theme .action-icon-wrap .action-icon {
  color: #6C5CE7 !important;
}

/* DM 头像 */
body.light-theme .dm-status-tag {
  color: #6C5CE7 !important;
}

/* 危险/删除按钮仍保持红色 */
body.light-theme .el-button--danger {
  background: #FF6B6B !important;
  border-color: #FF6B6B !important;
  color: #FFF !important;
}
body.light-theme .el-button--danger:hover {
  background: #EE5A5A !important;
}

/* 成功/警告标签保持原色 */
body.light-theme .el-tag--success {
  background: #E8F5E9 !important;
  border-color: #C8E6C9 !important;
  color: #2E7D32 !important;
}
body.light-theme .el-tag--warning {
  background: #FFF3E0 !important;
  border-color: #FFE0B2 !important;
  color: #E65100 !important;
}
body.light-theme .el-tag--danger {
  background: #FCE4EC !important;
  border-color: #F8BBD0 !important;
  color: #C62828 !important;
}
body.light-theme .el-tag--info {
  background: #FAFBFC !important;
  border-color: #E8E8ED !important;
  color: #636E72 !important;
}

/* 主内容区背景 */
body.light-theme .el-main {
  background: transparent !important;
}

/* 搜索/过滤条件区域 */
body.light-theme .el-form--inline .el-form-item__label {
  color: #636E72 !important;
}


</style>
