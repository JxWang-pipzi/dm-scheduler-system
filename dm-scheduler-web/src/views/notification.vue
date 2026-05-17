<template>
  <Layout>
    <template slot="header">
      <h2>通知中心</h2>
    </template>
    <div class="notification-container">
      <div class="notification-stats">
        <el-card>
          <el-statistic title="未读通知" :value="unreadCount"></el-statistic>
        </el-card>
      </div>

      <!-- 搜索工具栏 -->
      <div class="page-toolbar">
        <div class="toolbar-left">
          <el-input v-model="searchKeyword" placeholder="搜索标题/内容" clearable size="small" style="width: 200px" prefix-icon="el-icon-search" @clear="handleSearch" @keyup.enter.native="handleSearch"></el-input>
          <el-select v-model="searchStatus" placeholder="状态筛选" clearable size="small" style="width: 130px" @change="handleSearch">
            <el-option label="已读" :value="1"></el-option>
            <el-option label="未读" :value="0"></el-option>
          </el-select>
          <el-button type="primary" size="small" icon="el-icon-search" @click="handleSearch">搜索</el-button>
        </div>
      </div>

      <div class="notification-list" :class="{ 'list-refreshing': isRefreshing }">
        <h3>通知列表
          <span v-if="isRefreshing" class="refresh-hint">
            <i class="el-icon-refresh"></i> 刷新中...
          </span>
        </h3>
        <el-table :data="pagedList" style="width: 100%">
          <el-table-column prop="id" label="ID" width="60"></el-table-column>
          <el-table-column prop="title" label="标题" min-width="160">
            <template slot-scope="scope"><span style="color:#E2C082;font-weight:600">{{ scope.row.title }}</span></template>
          </el-table-column>
          <el-table-column prop="content" label="内容" min-width="250" show-overflow-tooltip></el-table-column>
          <el-table-column prop="type" label="类型" min-width="100">
            <template slot-scope="scope">
              <el-tag size="small" effect="dark" type="info">{{ scope.row.type || '系统' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="isRead" label="状态" min-width="90">
            <template slot-scope="scope">
              <el-tag v-if="scope.row.isRead === 1" type="success" size="small" effect="dark">已读</el-tag>
              <el-tag v-else type="danger" size="small" effect="dark" style="background:#A3824B;border:none;">未读</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createdAt" label="创建时间" min-width="160">
            <template slot-scope="scope">{{ scope.row.createdAt ? new Date(scope.row.createdAt).toLocaleString() : '-' }}</template>
          </el-table-column>
          <el-table-column label="操作" width="120" fixed="right">
            <template slot-scope="scope">
              <el-button v-if="scope.row.isRead === 0" type="primary" size="small" @click="markAsRead(scope.row.id)">标记已读</el-button>
            </template>
          </el-table-column>
        </el-table>

        <!-- 分页 -->
        <div class="pagination-wrapper">
          <el-pagination @size-change="handleSizeChange" @current-change="handleCurrentChange"
            :current-page="pageNum" :page-sizes="[10, 20, 50]" :page-size="pageSize"
            layout="total, sizes, prev, pager, next, jumper" :total="total"></el-pagination>
        </div>
      </div>
    </div>
  </Layout>
</template>

<script>
import Layout from '../components/Layout.vue'
import notificationApi from '../api/notification'
import { onMessage } from '@/utils/websocket'

export default {
  components: {
    Layout
  },
  data() {
    return {
      allList: [],
      unreadCount: 0,
      currentUser: JSON.parse(localStorage.getItem('user') || '{}'),
      searchKeyword: '',
      searchStatus: '',
      pageNum: 1,
      pageSize: 10,
      wsUnsubscribe: null,
      isRefreshing: false
    }
  },
  computed: {
    // 根据搜索条件过滤
    filteredList() {
      let list = this.allList
      if (this.searchKeyword) {
        const kw = this.searchKeyword.toLowerCase()
        list = list.filter(item =>
          (item.title && item.title.toLowerCase().includes(kw)) ||
          (item.content && item.content.toLowerCase().includes(kw))
        )
      }
      if (this.searchStatus !== '' && this.searchStatus !== null) {
        list = list.filter(item => item.isRead === this.searchStatus)
      }
      return list
    },
    // 总条数
    total() {
      return this.filteredList.length
    },
    // 当前页数据
    pagedList() {
      const start = (this.pageNum - 1) * this.pageSize
      return this.filteredList.slice(start, start + this.pageSize)
    }
  },
  mounted() {
    this.loadNotifications()
    this.loadUnreadCount()
    this.wsUnsubscribe = onMessage((data) => {
      if (data && (data.type === 'CALL_SERVICE' || data.type === 'CALL_CONFIRM' || data.type === 'SESSION_ASSIGNMENT')) {
        setTimeout(() => {
          this.loadNotifications()
          this.loadUnreadCount()
        }, 300)
      }
    })
  },
  beforeDestroy() {
    if (this.wsUnsubscribe) {
      this.wsUnsubscribe()
      this.wsUnsubscribe = null
    }
  },
  methods: {
    getCurrentUserId() {
      return this.currentUser && this.currentUser.id ? Number(this.currentUser.id) : null
    },
    loadNotifications() {
      const userId = this.getCurrentUserId()
      if (!userId) return
      this.isRefreshing = true
      notificationApi.getNotificationsByUser(userId).then(response => {
        if (response.code === 200) {
          this.allList = response.data || []
        }
      }).catch(() => {
        this.allList = []
      }).finally(() => {
        setTimeout(() => { this.isRefreshing = false }, 400)
      })
    },
    loadUnreadCount() {
      const userId = this.getCurrentUserId()
      if (!userId) return
      notificationApi.countUnreadByUser(userId).then(response => {
        if (response.code === 200) {
          this.unreadCount = response.data
        }
      }).catch(() => {
        this.unreadCount = 0
      })
    },
    markAsRead(id) {
      notificationApi.markAsRead(id).then(response => {
        if (response.code === 200) {
          this.$message.success('标记已读成功')
          this.loadNotifications()
          this.loadUnreadCount()
        }
      })
    },
    handleSearch() {
      this.pageNum = 1
    },
    handleSizeChange(val) {
      this.pageSize = val
      this.pageNum = 1
    },
    handleCurrentChange(val) {
      this.pageNum = val
    }
  }
}
</script>

<style scoped>
.notification-container {
  padding: 20px;
}

.notification-stats {
  margin-bottom: 20px;
}

.page-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding: 16px 20px;
  background: rgba(28,27,32,0.8);
  border-radius: 8px;
  border: 1px solid #36323D;
}
.toolbar-left { display: flex; gap: 10px; align-items: center; }

.notification-list {
  padding: 20px;
  background: rgba(28, 27, 32, 0.8);
  border: 1px solid #36323D;
  border-radius: 8px;
}
.notification-list h3 {
  margin-top: 0;
  margin-bottom: 20px;
  color: #D6B77A;
  font-family: 'Noto Serif SC', serif;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
  padding: 12px 0;
}

.refresh-hint {
  font-size: 12px;
  color: #A3824B;
  margin-left: 10px;
  font-weight: 400;
}

.refresh-hint i {
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.list-refreshing {
  animation: pulse-border 0.6s ease;
}

@keyframes pulse-border {
  0% { border-color: #36323D; }
  50% { border-color: #A3824B; box-shadow: 0 0 8px rgba(163, 130, 75, 0.3); }
  100% { border-color: #36323D; }
}

body.light-theme .page-toolbar {
  background: #FFFFFF !important;
  border: 1px solid #E0E0E5 !important;
}
body.light-theme .notification-list {
  background: #FFFFFF !important;
  border: 1px solid #E0E0E5 !important;
}
body.light-theme .notification-list h3 {
  color: #6C5CE7 !important;
}
body.light-theme .notification-list >>> .el-table {
  background: transparent !important;
}
body.light-theme .notification-list >>> .el-table th {
  background: #F5F6FA !important;
  color: #2D3436 !important;
}
body.light-theme .notification-list >>> .el-table tr {
  background: #FFFFFF !important;
  color: #2D3436 !important;
}
body.light-theme .notification-list >>> .el-table--enable-row-hover .el-table__body tr:hover > td {
  background: #F5F6FA !important;
}
body.light-theme .notification-list >>> .el-table td {
  border-bottom-color: #E0E0E5 !important;
}
body.light-theme .notification-list >>> .el-table th {
  border-bottom-color: #E0E0E5 !important;
}
body.light-theme .notification-list >>> .el-card {
  background: #FFFFFF !important;
  border: 1px solid #E0E0E5 !important;
}
body.light-theme .notification-list >>> .el-statistic__head {
  color: #636E72 !important;
}
body.light-theme .notification-list >>> .el-statistic__content {
  color: #2D3436 !important;
}
body.light-theme .notification-list >>> span[style*="color:#E2C082"] {
  color: #6C5CE7 !important;
}
body.light-theme .refresh-hint {
  color: #6C5CE7 !important;
}
body.light-theme .list-refreshing {
  animation: pulse-border-light 0.6s ease;
}
@keyframes pulse-border-light {
  0% { border-color: #E0E0E5; }
  50% { border-color: #6C5CE7; box-shadow: 0 0 8px rgba(108, 92, 231, 0.2); }
  100% { border-color: #E0E0E5; }
}
</style>
