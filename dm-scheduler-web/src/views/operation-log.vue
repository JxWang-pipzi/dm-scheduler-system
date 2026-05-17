<template>
  <Layout>
    <div class="page-toolbar">
      <div class="toolbar-left">
        <el-input v-model="searchKeyword" placeholder="搜索操作人/操作内容" prefix-icon="el-icon-search" clearable size="small" style="width: 220px" @clear="loadPage" @keyup.enter.native="loadPage"></el-input>
        <el-select v-model="searchAction" placeholder="操作类型" clearable size="small" style="width: 130px" @change="loadPage">
          <el-option label="新增" value="CREATE"></el-option>
          <el-option label="修改" value="UPDATE"></el-option>
          <el-option label="删除" value="DELETE"></el-option>
          <el-option label="登录" value="LOGIN"></el-option>
        </el-select>
        <el-button type="primary" size="small" icon="el-icon-search" @click="loadPage">搜索</el-button>
      </div>
    </div>

    <el-table :data="logList" style="width: 100%" v-loading="loading">
      <el-table-column prop="id" label="ID" width="60"></el-table-column>
      <el-table-column label="操作人" min-width="120">
        <template slot-scope="scope">
          <span style="color:#A3824B"><i class="el-icon-user"></i> {{ getUserLabel(scope.row.userId) }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="action" label="操作类型" min-width="100">
        <template slot-scope="scope">
          <el-tag size="small" effect="dark" :type="actionType(scope.row.action)">{{ actionText(scope.row.action) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="target" label="操作对象" min-width="140">
        <template slot-scope="scope">
          <span>{{ targetText(scope.row.target) }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="detail" label="操作详情" min-width="250" show-overflow-tooltip></el-table-column>
      <el-table-column prop="ip" label="IP地址" min-width="150"></el-table-column>
      <el-table-column prop="createdAt" label="操作时间" min-width="180">
        <template slot-scope="scope">{{ scope.row.createdAt ? new Date(scope.row.createdAt).toLocaleString() : '-' }}</template>
      </el-table-column>
    </el-table>

    <div class="pagination-wrapper">
      <el-pagination @size-change="handleSizeChange" @current-change="handleCurrentChange"
        :current-page="pageNum" :page-sizes="[20, 50, 100]" :page-size="pageSize"
        layout="total, sizes, prev, pager, next, jumper" :total="total"></el-pagination>
    </div>
  </Layout>
</template>

<script>
import Layout from '@/components/Layout.vue'
import { getOperationLogPage } from '@/api/operation-log'
import { getUserList } from '@/api/user'

export default {
  name: 'OperationLog',
  components: { Layout },
  data() {
    return {
      loading: false, logList: [], total: 0, pageNum: 1, pageSize: 20,
      searchKeyword: '', searchAction: '',
      userMap: {}
    }
  },
  mounted() { this.loadUserMap().then(() => this.loadPage()) },
  methods: {
    async loadUserMap() {
      try {
        const res = await getUserList()
        if (res && res.code === 200) {
          this.userMap = {}
          ;(res.data || []).forEach(u => {
            this.$set(this.userMap, u.id, u.realName || u.username || `用户-${u.id}`)
          })
        }
      } catch (err) {
        this.userMap = {}
      }
    },
    async loadPage() {
      this.loading = true
      try {
        const res = await getOperationLogPage({
          pageNum: this.pageNum, pageSize: this.pageSize,
          keyword: this.searchKeyword || undefined, action: this.searchAction || undefined
        })
        if (res && res.code === 200 && res.data) {
          this.logList = res.data.list || []; this.total = res.data.total || 0
        }
      } catch (err) { console.error(err) } finally { this.loading = false }
    },
    getUserLabel(userId) {
      if (!userId) return '系统'
      return this.userMap[userId] ? `${this.userMap[userId]}（ID:${userId}）` : `用户-${userId}`
    },
    handleSizeChange(val) { this.pageSize = val; this.pageNum = 1; this.loadPage() },
    handleCurrentChange(val) { this.pageNum = val; this.loadPage() },
    actionType(a) { return { CREATE: 'success', UPDATE: 'warning', DELETE: 'danger', LOGIN: '' }[a] || 'info' },
    actionText(a) { return { CREATE: '新增', UPDATE: '修改', DELETE: '删除', LOGIN: '登录' }[a] || a },
    targetText(target) {
      const key = String(target || '').trim().toUpperCase()
      const map = {
        USER: '用户',
        DM: 'DM',
        SCRIPT: '剧本',
        SESSION: '场次',
        RESERVATION: '预约',
        ORDER: '订单',
        EVALUATION: '评价',
        STORE: '门店',
        SYSTEM_CONFIG: '系统配置',
        OPERATION_LOG: '操作日志',
        STATISTICS: '统计分析',
        DM_SCHEDULE: 'DM排班',
        SYSTEM: '系统'
      }
      return map[key] || key || '-'
    }
  }
}
</script>

<style scoped>
.page-toolbar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; padding: 16px 20px; background: rgba(28,27,32,0.8); border-radius: 8px; border: 1px solid #36323D; }
.toolbar-left { display: flex; gap: 10px; align-items: center; }
.pagination-wrapper { display: flex; justify-content: flex-end; margin-top: 16px; padding: 12px 0; }
</style>
