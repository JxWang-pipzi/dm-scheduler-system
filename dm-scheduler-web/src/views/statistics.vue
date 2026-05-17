<template>
  <Layout>
    <template slot="header">
      <h2>统计分析</h2>
    </template>
    <div class="statistics-container">
      <div class="statistics-cards">
        <el-card>
          <el-statistic title="总场次" :value="sessionStats.totalSessions"></el-statistic>
        </el-card>
        <el-card>
          <el-statistic title="待处理场次" :value="sessionStats.pendingSessions"></el-statistic>
        </el-card>
        <el-card>
          <el-statistic title="已完成场次" :value="sessionStats.completedSessions"></el-statistic>
        </el-card>
        <el-card>
          <el-statistic title="总DM数" :value="dmStats.totalDms"></el-statistic>
        </el-card>
      </div>
      <div class="statistics-charts">
        <el-card>
          <template slot="header">
            <div class="card-header">
              <span>场次统计</span>
            </div>
          </template>
          <div ref="sessionChart" class="chart-container"></div>
        </el-card>
        <el-card>
          <template slot="header">
            <div class="card-header">
              <span>DM工作量统计</span>
            </div>
          </template>
          <div ref="dmChart" class="chart-container"></div>
        </el-card>
        <el-card>
          <template slot="header">
            <div class="card-header">
              <span>剧本热度统计</span>
            </div>
          </template>
          <div ref="scriptChart" class="chart-container"></div>
        </el-card>
        <el-card>
          <template slot="header">
            <div class="card-header">
              <span>收入统计</span>
            </div>
          </template>
          <div ref="incomeChart" class="chart-container"></div>
        </el-card>
      </div>
    </div>
  </Layout>
</template>

<script>
import Layout from '../components/Layout.vue'
import statisticsApi from '../api/statistics'
import * as echarts from 'echarts'

export default {
  components: {
    Layout
  },
  data() {
    return {
      sessionStats: {
        totalSessions: 0,
        pendingSessions: 0,
        completedSessions: 0
      },
      dmStats: {
        totalDms: 0,
        busyDms: 0,
        availableDms: 0
      },
      scriptStats: {
        totalScripts: 0,
        popularScripts: 0
      },
      incomeStats: {
        totalIncome: 0,
        monthlyIncome: 0
      },
      sessionChart: null,
      dmChart: null,
      scriptChart: null,
      incomeChart: null,
      chartData: {
        sessionLabels: [],
        sessionData: [],
        dmLabels: [],
        dmData: [],
        scriptLabels: [],
        scriptData: [],
        incomeLabels: [],
        incomeData: []
      }
    }
  },
  mounted() {
    this.loadStatistics()
  },
  beforeDestroy() {
    if (this.sessionChart) {
      this.sessionChart.dispose()
    }
    if (this.dmChart) {
      this.dmChart.dispose()
    }
    if (this.scriptChart) {
      this.scriptChart.dispose()
    }
    if (this.incomeChart) {
      this.incomeChart.dispose()
    }
  },
  methods: {
    async loadStatistics() {
      try {
        const [sessionStats, dmStats, scriptStats, incomeStats, chartStats] = await Promise.all([
          statisticsApi.getSessionStatistics(),
          statisticsApi.getDmStatistics(),
          statisticsApi.getScriptStatistics(),
          statisticsApi.getIncomeStatistics(),
          statisticsApi.getChartStatistics()
        ])
        
        if (sessionStats.code === 200) {
          this.sessionStats = sessionStats.data
        }
        if (dmStats.code === 200) {
          this.dmStats = dmStats.data
        }
        if (scriptStats.code === 200) {
          this.scriptStats = scriptStats.data
        }
        if (incomeStats.code === 200) {
          this.incomeStats = incomeStats.data
        }

        if (chartStats.code === 200) {
          this.applyChartData(chartStats.data || {})
        }
        
        // 初始化图表
        this.$nextTick(() => {
          this.initCharts()
        })
      } catch (error) {
        console.error('获取统计数据失败:', error)
      }
    },
    applyChartData(chartStats) {
      this.chartData.sessionLabels = this.toLabelArray(chartStats?.sessionTrend?.labels)
      this.chartData.sessionData = this.toNumberArray(chartStats?.sessionTrend?.values)
      this.chartData.dmLabels = this.toLabelArray(chartStats?.dmWorkload?.labels)
      this.chartData.dmData = this.toNumberArray(chartStats?.dmWorkload?.values)
      this.chartData.scriptLabels = this.toLabelArray(chartStats?.scriptHeat?.labels)
      this.chartData.scriptData = this.toNumberArray(chartStats?.scriptHeat?.values)
      this.chartData.incomeLabels = this.toLabelArray(chartStats?.incomeTrend?.labels)
      this.chartData.incomeData = this.toNumberArray(chartStats?.incomeTrend?.values)
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
    initCharts() {
      this.initSessionChart()
      this.initDmChart()
      this.initScriptChart()
      this.initIncomeChart()
    },
    initSessionChart() {
      const chartDom = this.$refs.sessionChart
      this.sessionChart = echarts.init(chartDom)
      const option = {
        title: {
          text: '场次统计',
          left: 'center'
        },
        tooltip: {
          trigger: 'axis'
        },
        xAxis: {
          type: 'category',
          data: this.chartData.sessionLabels
        },
        yAxis: {
          type: 'value'
        },
        series: [{
          data: this.chartData.sessionData,
          type: 'line',
          smooth: true,
          itemStyle: {
            color: '#1890ff'
          }
        }]
      }
      this.sessionChart.setOption(option)
    },
    initDmChart() {
      const chartDom = this.$refs.dmChart
      this.dmChart = echarts.init(chartDom)
      const option = {
        title: {
          text: 'DM工作量统计',
          left: 'center'
        },
        tooltip: {
          trigger: 'axis'
        },
        xAxis: {
          type: 'category',
          data: this.chartData.dmLabels
        },
        yAxis: {
          type: 'value'
        },
        series: [{
          data: this.chartData.dmData,
          type: 'bar',
          itemStyle: {
            color: '#52c41a'
          }
        }]
      }
      this.dmChart.setOption(option)
    },
    initScriptChart() {
      const chartDom = this.$refs.scriptChart
      this.scriptChart = echarts.init(chartDom)
      const option = {
        title: {
          text: '剧本热度统计',
          left: 'center'
        },
        tooltip: {
          trigger: 'axis'
        },
        xAxis: {
          type: 'category',
          data: this.chartData.scriptLabels
        },
        yAxis: {
          type: 'value'
        },
        series: [{
          data: this.chartData.scriptData,
          type: 'bar',
          itemStyle: {
            color: '#faad14'
          }
        }]
      }
      this.scriptChart.setOption(option)
    },
    initIncomeChart() {
      const chartDom = this.$refs.incomeChart
      this.incomeChart = echarts.init(chartDom)
      const option = {
        title: {
          text: '收入统计',
          left: 'center'
        },
        tooltip: {
          trigger: 'axis'
        },
        xAxis: {
          type: 'category',
          data: this.chartData.incomeLabels
        },
        yAxis: {
          type: 'value'
        },
        series: [{
          data: this.chartData.incomeData,
          type: 'line',
          smooth: true,
          itemStyle: {
            color: '#f5222d'
          }
        }]
      }
      this.incomeChart.setOption(option)
    }
  }
}
</script>

<style scoped>
.statistics-container {
  padding: 20px;
}

.statistics-cards {
  display: flex;
  gap: 20px;
  margin-bottom: 30px;
  flex-wrap: wrap;
}

.statistics-charts {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
}

.chart-container {
  height: 300px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

@media screen and (max-width: 768px) {
  .statistics-charts {
    grid-template-columns: 1fr;
  }
}
</style>
