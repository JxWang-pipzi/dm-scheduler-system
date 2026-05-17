<template>
  <Layout>
    <div class="dashboard-container">

      <!-- ================= 管理员专属视图 (ADMIN) ================= -->
      <template v-if="userRole === 'ADMIN'">
        <!-- 统计卡片行 — Bento Box 布局 -->
        <div class="stats-grid">
          <div
            class="stat-card"
            v-for="(stat, index) in statsCards"
            :key="index"
            :style="{ '--accent': stat.color, '--accent-dim': stat.dimColor }"
          >
            <div class="stat-card-glow"></div>
            <div class="stat-icon-wrap">
              <i :class="stat.icon"></i>
            </div>
            <div class="stat-info">
              <span class="stat-value">{{ stat.value }}</span>
              <span class="stat-label">{{ stat.label }}</span>
            </div>
            <div class="stat-trend" v-if="stat.trend !== null && stat.trend !== undefined">
              <i :class="stat.trend > 0 ? 'el-icon-top' : 'el-icon-bottom'"></i>
              {{ Math.abs(stat.trend) }}%
            </div>
          </div>
        </div>

        <!-- 快捷操作 + DM 状态行 -->
        <div class="bento-row">
          <div class="bento-card bento-wide">
            <div class="card-header">
              <h3><span class="header-dot"></span>快捷操作</h3>
            </div>
            <div class="quick-actions">
              <div
                class="action-item"
                v-for="action in quickActions.admin"
                :key="action.label"
                @click="$router.push(action.route)"
              >
                <div class="action-icon" :style="{ background: action.color }">
                  <i :class="action.icon"></i>
                </div>
                <span class="action-label">{{ action.label }}</span>
              </div>
            </div>
          </div>
          <div class="bento-card bento-narrow">
            <div class="card-header">
              <h3><span class="header-dot gold"></span>DM实时状态</h3>
            </div>
            <div class="dm-status-list">
              <div class="dm-item" v-for="dm in dmStatusList" :key="dm.id">
                <div class="dm-avatar" :style="{ '--ring': dm.statusColor }">
                  <el-avatar :size="28" icon="el-icon-user-solid"></el-avatar>
                </div>
                <div class="dm-info">
                  <span class="dm-name">{{ dm.name }}</span>
                  <span class="dm-status-tag" :style="{ color: dm.statusColor }">{{ dm.statusText }}</span>
                </div>
              </div>
              <div v-if="dmStatusList.length === 0" class="empty-tip">暂无DM数据</div>
            </div>
          </div>
        </div>

        <!-- 图表行 -->
        <div class="bento-row">
          <div class="bento-card bento-half">
            <div class="card-header">
              <h3><span class="header-dot"></span>场次趋势</h3>
            </div>
            <div ref="sessionChart" class="chart-container"></div>
          </div>
          <div class="bento-card bento-half">
            <div class="card-header">
              <h3><span class="header-dot gold"></span>剧本类型分布</h3>
            </div>
            <div ref="scriptChart" class="chart-container"></div>
          </div>
        </div>
      </template>

      <!-- ================= 剧本主持人专属视图 (DM) ================= -->
      <template v-else-if="userRole === 'DM'">
        <!-- DM 个人欢迎横幅 -->
        <div class="welcome-banner dm-banner">
          <div class="welcome-content">
            <h2>工作模式已开启，辛苦了！</h2>
            <p>在这里查看您的最新排班、快速处理剧本场次与接受指派。今天也要为玩家带来精彩的体验！</p>
          </div>
        </div>

        <div class="bento-row">
          <!-- DM 快捷操作 -->
          <div class="bento-card bento-wide">
            <div class="card-header">
              <h3><span class="header-dot"></span>我的工作台</h3>
            </div>
            <div class="quick-actions">
              <div
                class="action-item"
                v-for="action in quickActions.dm"
                :key="action.label"
                @click="$router.push(action.route)"
              >
                <div class="action-icon" :style="{ background: action.color }">
                  <i :class="action.icon"></i>
                </div>
                <span class="action-label">{{ action.label }}</span>
              </div>
            </div>
          </div>
          
          <!-- 提醒 -->
          <div class="bento-card bento-narrow" style="display: flex; flex-direction: column; align-items: center; justify-content: center; opacity: 0.8;">
            <i class="el-icon-bell" style="font-size: 40px; color: rgba(163, 130, 75, 0.6); margin-bottom: 12px;"></i>
            <span style="color: rgba(235, 230, 225, 0.8); font-size: 14px;">暂无紧急任务提醒</span>
          </div>
        </div>
      </template>

      <!-- ================= 顾客专属视图 (CUSTOMER / 其他) ================= -->
      <template v-else>
        <!-- 顾客 欢迎横幅 -->
        <div class="welcome-banner customer-banner">
          <div class="welcome-content">
            <h2>探索精彩纷呈的剧本世界 🕵️‍♂️</h2>
            <p>欢迎回到剧本杀集合站！海量优质剧本等您发掘，马上开启您的下一次角色扮演之旅。</p>
          </div>
        </div>

        <div class="bento-row" style="margin-top: 20px;">
          <!-- 顾客 快捷操作 -->
          <div class="bento-card bento-wide" style="flex: 1;">
            <div class="card-header">
              <h3><span class="header-dot gold"></span>快捷服务</h3>
            </div>
            <div class="quick-actions customer-actions">
              <div
                class="action-item"
                v-for="action in quickActions.customer"
                :key="action.label"
                @click="$router.push(action.route)"
              >
                <div class="action-icon" :style="{ background: action.color, width: '48px', height: '48px', fontSize: '20px' }">
                  <i :class="action.icon"></i>
                </div>
                <span class="action-label" style="font-size: 13px; margin-top: 4px;">{{ action.label }}</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 顾客 🔥 热门剧本推荐 -->
        <div class="bento-card bento-wide hot-scripts" style="margin-top: 20px;">
          <div class="card-header">
            <h3><span class="header-dot" style="background: #e2c082; box-shadow: 0 0 10px #e2c082;"></span>🔥 热门剧本推荐</h3>
          </div>
          <div class="script-list" v-if="hotScripts && hotScripts.length > 0">
            <el-row :gutter="20">
              <el-col :span="6" v-for="script in hotScripts" :key="script.id">
                <el-card shadow="hover" class="dark-card dm-script-card">
                  <div class="script-cover-wrap">
                    <div class="script-cover-placeholder" :style="getScriptCoverStyle(script.type)">
                      <span class="script-emoji">{{ getScriptEmoji(script.type) }}</span>
                    </div>
                    <div class="script-tags">
                      <el-tag size="mini" effect="dark" type="warning">{{ script.type }}</el-tag>
                    </div>
                  </div>
                  <div class="script-info">
                    <h4 class="script-title">{{ script.scriptName }}</h4>
                    <p class="script-meta">
                      <span><i class="el-icon-user"></i> {{ script.minPlayers }}-{{ script.maxPlayers }}人</span>
                      <span><i class="el-icon-time"></i> {{ script.duration }}h</span>
                    </p>
                    <div class="script-actions">
                      <el-button type="text" style="color: #e2c082;" @click="$router.push('/reservation')">去预约</el-button>
                    </div>
                  </div>
                </el-card>
              </el-col>
            </el-row>
          </div>
          <div v-else class="empty-tip" style="padding: 40px; text-align: center; color: rgba(157, 150, 163, 0.6);">
            暂无推荐内容
          </div>
        </div>
      </template>

    </div>
  </Layout>
</template>

<script>
import Layout from '@/components/Layout.vue'
import { getUserList } from '@/api/user'
import { getScriptList, getHotScripts } from '@/api/script'
import { getSessionList } from '@/api/session'
import dmApi from '@/api/dm'
import statisticsApi from '@/api/statistics'
import * as echarts from 'echarts'

export default {
  name: 'Dashboard',
  components: { Layout },
  data() {
    const user = JSON.parse(localStorage.getItem('user') || '{}');
    const rawRole = String(user.role || 'USER').toUpperCase();
    return {
      userRole: rawRole,
      statsCards: [
        { label: '用户总数', value: 0, icon: 'el-icon-user-solid', color: 'rgba(226, 192, 130, 0.8)', dimColor: 'rgba(226, 192, 130, 0.06)', trend: null },
        { label: '剧本数量', value: 0, icon: 'el-icon-document', color: 'rgba(157, 150, 163, 0.8)', dimColor: 'rgba(157, 150, 163, 0.06)', trend: null },
        { label: '总场次', value: 0, icon: 'el-icon-time', color: 'rgba(163, 130, 75, 0.9)', dimColor: 'rgba(163, 130, 75, 0.06)', trend: null },
        { label: 'DM人数', value: 0, icon: 'el-icon-s-custom', color: 'rgba(96, 87, 112, 0.9)', dimColor: 'rgba(96, 87, 112, 0.06)', trend: null }
      ],
      quickActions: {
        admin: [
          { label: '新建场次', icon: 'el-icon-plus', color: 'linear-gradient(135deg, rgba(163, 130, 75, 0.6), rgba(140, 115, 85, 0.4))', route: '/session' },
          { label: '排班管理', icon: 'el-icon-date', color: 'linear-gradient(135deg, rgba(96, 87, 112, 0.6), rgba(75, 70, 83, 0.4))', route: '/dm-schedule' },
          { label: '订单管理', icon: 'el-icon-shopping-cart-full', color: 'linear-gradient(135deg, rgba(214, 183, 122, 0.5), rgba(163, 130, 75, 0.3))', route: '/order' },
          { label: '统计分析', icon: 'el-icon-pie-chart', color: 'linear-gradient(135deg, rgba(88, 79, 102, 0.5), rgba(54, 50, 61, 0.3))', route: '/statistics' },
          { label: '系统配置', icon: 'el-icon-s-tools', color: 'linear-gradient(135deg, rgba(156, 125, 67, 0.5), rgba(163, 130, 75, 0.3))', route: '/system-config' }
        ],
        dm: [
          { label: '场次管理', icon: 'el-icon-time', color: 'linear-gradient(135deg, rgba(163, 130, 75, 0.6), rgba(140, 115, 85, 0.4))', route: '/session' },
          { label: '剧本查询', icon: 'el-icon-document', color: 'linear-gradient(135deg, rgba(96, 87, 112, 0.6), rgba(75, 70, 83, 0.4))', route: '/script' }
        ],
        customer: [
          { label: '立即选本', icon: 'el-icon-search', color: 'linear-gradient(135deg, rgba(226, 192, 130, 0.8), rgba(163, 130, 75, 0.6))', route: '/reservation' },
          { label: '我的订单', icon: 'el-icon-shopping-bag-1', color: 'linear-gradient(135deg, rgba(157, 150, 163, 0.8), rgba(96, 87, 112, 0.6))', route: '/order' },
          { label: '发布评价', icon: 'el-icon-star-on', color: 'linear-gradient(135deg, rgba(214, 183, 122, 0.6), rgba(163, 130, 75, 0.4))', route: '/evaluation' }
        ]
      },
      dmStatusList: [],
      sessionChart: null,
      scriptChart: null,
      hotScripts: [],
      sessionTrendLabels: [],
      sessionTrendData: [],
      scriptTypeDistribution: []
    }
  },
  async mounted() {
    if (this.userRole === 'USER') {
      await this.loadCustomerData()
    } else {
      await this.loadStats()
      this.$nextTick(() => {
        if (this.userRole === 'ADMIN') {
          this.initSessionChart()
          this.initScriptChart()
        }
      })
    }
  },
  beforeDestroy() {
    if (this.sessionChart) this.sessionChart.dispose()
    if (this.scriptChart) this.scriptChart.dispose()
  },
  methods: {
    async loadCustomerData() {
      try {
        const res = await getHotScripts(4);
        if (res.code === 200) {
          this.hotScripts = res.data || [];
        }
      } catch (err) {
        console.error('获取热门剧本失败:', err);
      }
    },
    // 根据剧本类型返回对应的 Emoji
    getScriptEmoji(type) {
      const map = { '推理': '🔍', '恐怖': '👻', '欢乐': '🎉', '情感': '💕', '机制': '⚙️' };
      return map[type] || '🎭';
    },
    // 根据剧本类型返回对应的渐变背景色
    getScriptCoverStyle(type) {
      const styles = {
        '推理': 'background: linear-gradient(135deg, #2c3e50, #3498db)',
        '恐怖': 'background: linear-gradient(135deg, #1a1a2e, #e94560)',
        '欢乐': 'background: linear-gradient(135deg, #f39c12, #e74c3c)',
        '情感': 'background: linear-gradient(135deg, #e91e63, #9c27b0)',
        '机制': 'background: linear-gradient(135deg, #00b894, #00cec9)'
      };
      return styles[type] || 'background: linear-gradient(135deg, #636e72, #b2bec3)';
    },
    async loadStats() {
      if (this.userRole !== 'ADMIN') return; // 只有 ADMIN 需要去拉取大盘敏感数据
      try {
        const [userRes, scriptRes, sessionRes, dmRes, chartRes] = await Promise.all([
          getUserList(),
          getScriptList(),
          getSessionList(),
          dmApi.getDmList(),
          statisticsApi.getChartStatistics()
        ])
        const scriptList = scriptRes.code === 200 ? (scriptRes.data || []) : []
        if (userRes.code === 200) this.statsCards[0].value = (userRes.data || []).length
        if (scriptRes.code === 200) this.statsCards[1].value = scriptList.length
        if (sessionRes.code === 200) this.statsCards[2].value = (sessionRes.data || []).length
        if (dmRes.code === 200) {
          const dms = dmRes.data || []
          this.statsCards[3].value = dms.length
          this.dmStatusList = dms.slice(0, 5).map(dm => ({
            id: dm.id,
            name: `DM-${dm.id}`,
            statusText: dm.status === 'AVAILABLE' ? '空闲' : dm.status === 'BUSY' ? '带本中' : '休息',
            statusColor: dm.status === 'AVAILABLE' ? '#A3824B' : dm.status === 'BUSY' ? '#D6B77A' : 'rgba(96, 87, 112, 0.7)'
          }))
        }
        if (chartRes.code === 200) {
          const chartData = chartRes.data || {}
          this.sessionTrendLabels = this.toLabelArray(chartData?.sessionTrend?.labels)
          this.sessionTrendData = this.toNumberArray(chartData?.sessionTrend?.values)
          this.scriptTypeDistribution = this.toPieData(chartData?.scriptTypeDistribution)
        }
        if (!this.scriptTypeDistribution.length) {
          const typeCounter = scriptList.reduce((acc, script) => {
            const type = (script && script.type ? String(script.type) : '未分类')
            acc[type] = (acc[type] || 0) + 1
            return acc
          }, {})
          this.scriptTypeDistribution = Object.entries(typeCounter).map(([name, value]) => ({ name, value }))
        }
      } catch (err) {
        console.error('加载统计数据失败:', err)
        this.$message.error('首页统计数据加载失败，请检查后端服务')
      }
    },
    toLabelArray(values) {
      return Array.isArray(values) ? values.map(v => String(v)) : []
    },
    toNumberArray(values) {
      if (!Array.isArray(values)) return []
      return values.map(v => {
        const n = Number(v)
        return Number.isFinite(n) ? n : 0
      })
    },
    toPieData(values) {
      if (!Array.isArray(values)) return []
      return values
        .map(item => {
          const name = item && item.name != null ? String(item.name) : '未分类'
          const value = Number(item && item.value)
          return { name, value: Number.isFinite(value) ? value : 0 }
        })
        .filter(item => item.value >= 0)
    },
    initSessionChart() {
      if (!this.$refs.sessionChart) return
      this.sessionChart = echarts.init(this.$refs.sessionChart)
      
      const isLight = document.body.classList.contains('light-theme')
      const textColor = isLight ? '#636E72' : 'rgba(230, 225, 220, 0.7)'
      const axisLineColor = isLight ? '#E8E8ED' : 'rgba(255, 255, 255, 0.08)'
      const tooltipBg = isLight ? 'rgba(255, 255, 255, 0.95)' : 'rgba(28, 27, 32, 0.95)'
      const tooltipBorder = isLight ? '#E8E8ED' : 'rgba(255, 255, 255, 0.08)'

      this.sessionChart.setOption({
        tooltip: {
          trigger: 'axis',
          backgroundColor: tooltipBg,
          borderColor: tooltipBorder,
          borderWidth: 1,
          padding: [10, 16],
          textStyle: { color: isLight ? '#2D3436' : 'rgba(230, 225, 220, 0.9)', fontSize: 13, fontWeight: 500 },
          extraCssText: `backdrop-filter: blur(12px); border-radius: 8px; box-shadow: 0 8px 24px ${isLight ? 'rgba(0,0,0,0.08)' : 'rgba(0,0,0,0.4)'};`
        },
        grid: { left: '3%', right: '4%', bottom: '3%', top: '8%', containLabel: true },
        xAxis: {
          type: 'category',
          boundaryGap: false,
          data: this.sessionTrendLabels,
          axisLine: { lineStyle: { color: axisLineColor } },
          axisLabel: { color: textColor, fontSize: 12, margin: 12 },
          axisTick: { show: false }
        },
        yAxis: {
          type: 'value',
          axisLine: { show: false },
          axisLabel: { color: textColor, fontSize: 12, margin: 12 },
          splitLine: { lineStyle: { color: axisLineColor, type: 'dashed' } }
        },
        series: [{
          name: '场次数',
          type: 'line',
          smooth: 0.4,
          symbol: 'circle',
          symbolSize: 8,
          showSymbol: false,
          data: this.sessionTrendData,
          lineStyle: {
            width: 4,
            color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
              { offset: 0, color: '#A29BFE' },
              { offset: 1, color: '#6C5CE7' }
            ]),
            shadowColor: 'rgba(108, 92, 231, 0.4)',
            shadowBlur: 14,
            shadowOffsetY: 6
          },
          areaStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: 'rgba(108, 92, 231, 0.35)' },
              { offset: 0.8, color: 'rgba(108, 92, 231, 0.05)' },
              { offset: 1, color: 'rgba(108, 92, 231, 0)' }
            ])
          },
          itemStyle: { color: '#fff', borderColor: '#6C5CE7', borderWidth: 2 }
        }]
      })
      window.addEventListener('resize', () => this.sessionChart?.resize())
    },
    initScriptChart() {
      if (!this.$refs.scriptChart) return
      this.scriptChart = echarts.init(this.$refs.scriptChart)

      const isLight = document.body.classList.contains('light-theme')
      const tooltipBg = isLight ? 'rgba(255, 255, 255, 0.95)' : 'rgba(28, 27, 32, 0.95)'
      const tooltipBorder = isLight ? '#E8E8ED' : 'rgba(255, 255, 255, 0.08)'

      this.scriptChart.setOption({
        tooltip: {
          trigger: 'item',
          backgroundColor: tooltipBg,
          borderColor: tooltipBorder,
          borderWidth: 1,
          padding: [10, 16],
          textStyle: { color: isLight ? '#2D3436' : 'rgba(230, 225, 220, 0.9)', fontSize: 13, fontWeight: 500 },
          extraCssText: `backdrop-filter: blur(12px); border-radius: 8px; box-shadow: 0 8px 24px ${isLight ? 'rgba(0,0,0,0.08)' : 'rgba(0,0,0,0.4)'};`
        },
        legend: {
          bottom: '10%',
          icon: 'circle',
          itemWidth: 10,
          itemHeight: 10,
          textStyle: { color: isLight ? '#636E72' : 'rgba(230,225,220,0.7)', fontSize: 12, fontFamily: 'Inter' }
        },
        series: [{
          name: '剧本类型',
          type: 'pie',
          radius: ['55%', '75%'],
          center: ['50%', '42%'],
          avoidLabelOverlap: false,
          itemStyle: {
            borderRadius: 10,
            borderColor: isLight ? '#fff' : '#1C1B20',
            borderWidth: 2
          },
          color: ['#00B894', '#0984E3', '#FDCB6E', '#E17055', '#6C5CE7'],
          label: {
            show: false,
            position: 'center'
          },
          emphasis: {
            label: {
              show: true,
              fontSize: 22,
              fontWeight: 700,
              color: isLight ? '#2D3436' : '#EBE6E1'
            },
            itemStyle: {
              shadowBlur: 20,
              shadowOffsetX: 0,
              shadowColor: 'rgba(0,0,0,0.2)'
            }
          },
          labelLine: { show: false },
          data: this.scriptTypeDistribution
        }]
      })
      window.addEventListener('resize', () => this.scriptChart?.resize())
    }
  }
}
</script>

<style scoped>
.dashboard-container {
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* ===== 欢迎横幅 (DM / CUSTOMER 通用) ===== */
.welcome-banner {
  padding: 30px 40px;
  border-radius: 16px;
  background-size: cover;
  background-position: center;
  position: relative;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.2), inset 0 1px 0 rgba(255, 255, 255, 0.04);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
}
.dm-banner {
  background: linear-gradient(135deg, rgba(35, 32, 45, 0.8), rgba(96, 87, 112, 0.6));
  border: 1px solid rgba(157, 150, 163, 0.2);
}
.customer-banner {
  background: linear-gradient(135deg, rgba(35, 32, 45, 0.8), rgba(163, 130, 75, 0.5));
  border: 1px solid rgba(226, 192, 130, 0.2);
}

body.light-theme .dm-banner {
  background: linear-gradient(135deg, #f3f0f7, #e4e0eb);
  border: 1px solid #d4cde0;
}
body.light-theme .customer-banner {
  background: linear-gradient(135deg, #f9f5f0, #efe5d4);
  border: 1px solid #e2c082;
}

.welcome-content {
  position: relative;
  z-index: 2;
}
.welcome-content h2 {
  font-size: 26px;
  color: rgba(235, 230, 225, 0.95);
  margin-bottom: 10px;
  font-family: 'Inter', sans-serif;
  letter-spacing: 0.5px;
}
.welcome-content p {
  font-size: 14px;
  color: rgba(180, 175, 190, 0.8);
  max-width: 60%;
  line-height: 1.5;
}

body.light-theme .welcome-content h2 { color: #2D3436; }
body.light-theme .welcome-content p { color: #636E72; }

/* 剧本封面占位符（Emoji + 渐变色） */
.script-cover-placeholder {
  width: 100%;
  height: 180px;
  border-radius: 8px 8px 0 0;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
}
.script-emoji {
  font-size: 64px;
  filter: drop-shadow(0 4px 8px rgba(0,0,0,0.3));
}
.script-cover-wrap {
  position: relative;
  overflow: hidden;
  border-radius: 8px 8px 0 0;
}
.script-tags {
  position: absolute;
  top: 8px;
  left: 8px;
}
.script-info {
  padding: 12px;
}
.script-title {
  margin: 0 0 6px 0;
  font-size: 15px;
  color: rgba(235, 230, 225, 0.95);
}
body.light-theme .script-title { color: #2D3436; }
.script-meta {
  font-size: 12px;
  color: rgba(157, 150, 163, 0.8);
  display: flex;
  gap: 12px;
}
.script-actions {
  margin-top: 8px;
}

/* 针对于顾客的特别大的快捷入口 */
.customer-actions {
  grid-template-columns: repeat(4, 1fr) !important;
  gap: 16px !important;
}

/* ===== 统计卡片网格  ===== */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.stat-card {
  position: relative;
  background: rgba(35, 32, 45, 0.55);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 16px;
  padding: 24px 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  overflow: hidden;
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.2), inset 0 1px 0 rgba(255, 255, 255, 0.04);
  transition: all 0.4s cubic-bezier(0.25, 0.8, 0.25, 1);
}
.stat-card:hover {
  transform: translateY(-4px);
  border-color: rgba(226, 192, 130, 0.15);
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.3), inset 0 1px 0 rgba(255, 255, 255, 0.06);
}

/* 卡片底部微弱色彩辉光 */
.stat-card-glow {
  position: absolute;
  bottom: -30px;
  right: -20px;
  width: 100px;
  height: 100px;
  background: var(--accent);
  border-radius: 50%;
  filter: blur(50px);
  opacity: 0.15;
}

.stat-icon-wrap {
  width: 44px;
  height: 44px;
  background: var(--accent-dim);
  border: 1px solid rgba(255, 255, 255, 0.04);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  color: var(--accent);
  flex-shrink: 0;
}

.stat-info {
  flex: 1;
  display: flex;
  flex-direction: column;
}
.stat-value {
  font-size: 26px;
  font-weight: 700;
  color: rgba(235, 230, 225, 1);
  font-family: 'Inter', sans-serif;
  letter-spacing: -0.5px;
}
.stat-label {
  font-size: 12px;
  color: rgba(180, 175, 190, 0.7);
  margin-top: 2px;
  letter-spacing: 0.5px;
}

.stat-trend {
  font-size: 11px;
  color: rgba(163, 130, 75, 0.8);
  display: flex;
  align-items: center;
  gap: 2px;
  font-weight: 500;
}

/* ===== Bento Box 卡片体系  ===== */
.bento-row {
  display: flex;
  gap: 16px;
}

.bento-card {
  background: rgba(35, 32, 45, 0.55);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 16px;
  padding: 22px;
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.2), inset 0 1px 0 rgba(255, 255, 255, 0.04);
  transition: all 0.4s cubic-bezier(0.25, 0.8, 0.25, 1);
}
.bento-card:hover {
  border-color: rgba(226, 192, 130, 0.1);
}

.bento-wide { flex: 5; }
.bento-narrow { flex: 3; }
.bento-half { flex: 1; }

.card-header {
  display: flex;
  align-items: center;
  margin-bottom: 18px;
}
.card-header h3 {
  font-size: 14px;
  color: rgba(235, 230, 225, 0.95);
  font-family: 'Inter', sans-serif;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 8px;
  letter-spacing: 0.5px;
}
.header-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: rgba(157, 150, 163, 0.4);
  display: inline-block;
}
.header-dot.gold {
  background: rgba(226, 192, 130, 0.6);
  box-shadow: 0 0 8px rgba(226, 192, 130, 0.3);
}

/* 快捷操作 */
.quick-actions {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 10px;
}
.action-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  padding: 18px 8px;
  border-radius: 14px;
  cursor: pointer;
  transition: all 0.35s cubic-bezier(0.25, 0.8, 0.25, 1);
  background: rgba(22, 20, 28, 0.4);
  border: 1px solid transparent;
}
.action-item:hover {
  background: rgba(226, 192, 130, 0.04);
  border-color: rgba(255, 255, 255, 0.06);
  transform: translateY(-3px);
}
.action-icon {
  width: 40px;
  height: 40px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 17px;
  color: rgba(255, 255, 255, 0.8);
}
.action-label {
  font-size: 11.5px;
  color: rgba(157, 150, 163, 0.5);
  font-weight: 400;
  letter-spacing: 0.5px;
}

/* DM状态 */
.dm-status-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.dm-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 10px;
  border-radius: 10px;
  transition: background 0.3s;
}
.dm-item:hover {
  background: rgba(226, 192, 130, 0.04);
}
.dm-avatar {
  padding: 2px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--ring), transparent);
}
.dm-avatar >>> .el-avatar {
  background: rgba(60, 55, 70, 0.6) !important;
  font-size: 13px;
}
.dm-info {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex: 1;
}
.dm-name {
  font-size: 12.5px;
  color: rgba(235, 230, 225, 0.95);
  font-weight: 500;
}
.dm-status-tag {
  font-size: 11px;
  font-weight: 500;
  letter-spacing: 0.5px;
}
.empty-tip {
  text-align: center;
  color: rgba(130, 125, 140, 0.6);
  padding: 24px;
  font-size: 12px;
}

/* 图表 */
.chart-container {
  height: 280px;
  width: 100%;
}
</style>

<style>
/* Dashboard 亮色主题覆盖 — 非 scoped */

body.light-theme .stat-card {
  background: #FFFFFF !important;
  border: 1px solid #E8E8ED !important;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04) !important;
  backdrop-filter: none !important;
}
body.light-theme .stat-card:hover {
  border-color: rgba(108, 92, 231, 0.2) !important;
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.06) !important;
}
body.light-theme .stat-card-glow {
  opacity: 0 !important;
}
body.light-theme .stat-value {
  color: #2D3436 !important;
}
body.light-theme .stat-label {
  color: #A0A5AA !important;
}
body.light-theme .stat-trend {
  color: #6C5CE7 !important;
}

body.light-theme .bento-card {
  background: #FFFFFF !important;
  border: 1px solid #E8E8ED !important;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04) !important;
  backdrop-filter: none !important;
}
body.light-theme .bento-card:hover {
  border-color: rgba(108, 92, 231, 0.15) !important;
}
body.light-theme .card-header h3 {
  color: #2D3436 !important;
}
body.light-theme .header-dot {
  background: #A0A5AA !important;
  box-shadow: none !important;
}
body.light-theme .header-dot.gold {
  background: #6C5CE7 !important;
  box-shadow: 0 0 6px rgba(108, 92, 231, 0.3) !important;
}

body.light-theme .action-item {
  background: #FAFBFC !important;
}
body.light-theme .action-item:hover {
  background: rgba(108, 92, 231, 0.04) !important;
  border-color: #E8E8ED !important;
}
body.light-theme .action-label {
  color: #636E72 !important;
}

body.light-theme .dm-name {
  color: #2D3436 !important;
}
body.light-theme .dm-avatar .el-avatar {
  background: #E8E8ED !important;
  color: #636E72 !important;
}
body.light-theme .dm-item:hover {
  background: rgba(108, 92, 231, 0.03) !important;
}
body.light-theme .empty-tip {
  color: #A0A5AA !important;
}
</style>
