<template>
  <Layout>
    <template slot="header">
      <h2>预约管理</h2>
    </template>
    <div class="reservation-container">
      <div class="reservation-content">
        <div class="page-toolbar">
          <div class="toolbar-left">
            <el-input v-model="searchKeyword" placeholder="搜索用户/场次" clearable size="small" style="width: 200px" prefix-icon="el-icon-search" @clear="handleSearch" @keyup.enter.native="handleSearch"></el-input>
            <el-select v-model="searchStatus" placeholder="状态筛选" clearable size="small" style="width: 130px" @change="handleSearch">
              <el-option label="已确认" value="CONFIRMED"></el-option>
              <el-option label="已取消" value="CANCELLED"></el-option>
            </el-select>
            <el-button type="primary" size="small" icon="el-icon-search" @click="handleSearch">搜索</el-button>
          </div>
          <div class="toolbar-right">
            <el-button type="primary" size="small" icon="el-icon-plus" @click="openAddDialog">添加预约</el-button>
          </div>
        </div>
        <el-table :data="pagedList" style="width: 100%">
          <el-table-column prop="id" label="ID" width="60"></el-table-column>
          <el-table-column label="预约用户" min-width="120">
            <template slot-scope="scope">
              <span style="color:#A3824B;font-weight:600"><i class="el-icon-user"></i> {{ getUserLabel(scope.row.userId) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="关联场次" min-width="140">
            <template slot-scope="scope">
              <span style="color:#E2C082">{{ getSessionLabel(scope.row.sessionId) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="预约人数" min-width="90">
            <template slot-scope="scope">
              <span style="font-weight:600">{{ scope.row.playersCount || 1 }} 人</span>
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" min-width="100">
            <template slot-scope="scope">
              <el-tag size="small" effect="dark" :type="scope.row.status === 'CONFIRMED' ? 'success' : scope.row.status === 'CANCELLED' ? 'danger' : 'info'">{{ scope.row.status === 'CONFIRMED' ? '已确认' : scope.row.status === 'CANCELLED' ? '已取消' : scope.row.status }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createdAt" label="创建时间" min-width="160">
            <template slot-scope="scope">
              {{ formatDate(scope.row.createdAt) }}
            </template>
          </el-table-column>
	          <el-table-column label="操作" width="180" fixed="right">
	            <template slot-scope="scope">
	              <el-button
                  v-if="scope.row.status !== 'CANCELLED' && !isReservationLocked(scope.row)"
                  type="warning"
                  size="small"
                  @click="cancelReservation(scope.row.id)">取消预约</el-button>
	              <el-button
                  v-if="!isReservationLocked(scope.row)"
                  type="danger"
                  size="small"
                  @click="deleteReservation(scope.row.id)">删除</el-button>
                <span v-if="isReservationLocked(scope.row)" style="color:#909399;font-size:12px">已支付不可操作</span>
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
    
    <!-- 添加预约对话框 -->
        <el-dialog title="添加预约" :visible.sync="addDialogVisible">
      <el-form ref="addForm" :model="addForm" :rules="rules">
        <el-form-item label="用户" prop="userId" v-if="isAdmin">
          <el-select v-model="addForm.userId" filterable placeholder="请选择用户" style="width: 100%">
            <el-option
              v-for="item in userOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value">
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="场次" prop="sessionId">
          <el-select v-model="addForm.sessionId" filterable placeholder="请选择场次" style="width: 100%" @change="handleSessionChange">
            <el-option
              v-for="item in sessionOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value">
            </el-option>
          </el-select>
        </el-form-item>
        <el-alert
          v-if="sessionPayHint"
          :title="sessionPayHint"
          type="warning"
          :closable="false"
          show-icon
          style="margin-bottom: 12px;">
        </el-alert>
        <el-form-item label="人数" prop="playersCount">
          <el-input-number v-model="addForm.playersCount" :min="1" :max="maxSelectablePlayers" style="width: 100%"></el-input-number>
          <div v-if="addForm.sessionId" style="color:#909399;font-size:12px;margin-top:6px;">
            当前场次最多可预约 {{ maxSelectablePlayers }} 人
          </div>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="addDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="addReservation">确定</el-button>
      </span>
    </el-dialog>
  </Layout>
</template>

<script>
import Layout from '../components/Layout.vue'
import { getReservationList, getReservationsByUserId, addReservation, cancelReservation, deleteReservation } from '../api/reservation'
import { getSessionList } from '../api/session'
import { getUserList } from '../api/user'
import { getScriptList } from '../api/script'
import { getOrdersByUserId } from '../api/order'

export default {
  name: 'Reservation',
  components: {
    Layout
  },
  data() {
    return {
      allList: [],
      sessionOptions: [],
      userOptions: [],
      currentUser: JSON.parse(localStorage.getItem('user') || '{}'),
      searchKeyword: '',
      searchStatus: '',
      pageNum: 1,
      pageSize: 10,
      addDialogVisible: false,
      addForm: {
        userId: null,
        sessionId: null,
        playersCount: 1
      },
      rules: {
        userId: [
          { required: true, message: '请选择用户', trigger: 'change' }
        ],
        sessionId: [
          { required: true, message: '请选择场次', trigger: 'change' }
        ],
        playersCount: [
          { required: true, message: '请输入预约人数', trigger: 'change' },
          { validator: (rule, value, callback) => {
            if (!value || Number(value) < 1) return callback(new Error('预约人数必须大于0'))
            if (this.addForm && this.addForm.sessionId) {
              const remaining = this.getSessionRemaining(this.addForm.sessionId)
              if (remaining < 1) return callback(new Error('该场次已满，请选择其他场次'))
              if (Number(value) > remaining) return callback(new Error(`超过剩余名额，当前最多可预约 ${remaining} 人`))
            }
            callback()
          }, trigger: 'change' }
        ]
      },
      userMap: {},
      scriptMap: {},
      sessionMap: {},
      sessionRawMap: {},
      paidSessionMap: {},
      runtimeConfig: {
        reservationLeadHours: 0
      }
    }
  },
  computed: {
    currentUserId() {
      return this.currentUser && this.currentUser.id ? Number(this.currentUser.id) : null
    },
    role() {
      return String((this.currentUser && this.currentUser.role) || '').toUpperCase()
    },
    isAdmin() {
      return this.role === 'ADMIN'
    },
    sessionPayHint() {
      const sessionId = this.addForm && this.addForm.sessionId
      if (!sessionId) return ''
      const session = this.sessionRawMap[sessionId]
      if (!session || !session.startTime) return ''
      const startAt = new Date(session.startTime).getTime()
      if (Number.isNaN(startAt)) return ''
      const now = Date.now()
      if (startAt <= now) {
        return '该场次已到或超过开场时间，开场前必须完成支付，请选择其他场次。'
      }
      const leftMinutes = Math.floor((startAt - now) / (60 * 1000))
      if (leftMinutes <= 30) {
        return `距离开场仅剩 ${leftMinutes} 分钟，预约后请立即前往订单管理完成支付。`
      }
      const leadHours = Number(this.runtimeConfig.reservationLeadHours || 0)
      if (leadHours > 0) {
        return `预约需至少提前 ${leadHours} 小时，且必须在开场前完成支付。`
      }
      return '开场前需完成支付，建议预约后立即前往订单管理支付。'
    },
    filteredList() {
      let list = this.allList
      if (this.searchKeyword) {
        const kw = this.searchKeyword.toLowerCase()
        list = list.filter(item =>
          String(item.userId).includes(kw) ||
          String(item.sessionId).includes(kw) ||
          this.getUserLabel(item.userId).toLowerCase().includes(kw) ||
          this.getSessionLabel(item.sessionId).toLowerCase().includes(kw)
        )
      }
      if (this.searchStatus) {
        list = list.filter(item => item.status === this.searchStatus)
      }
      return list
    },
    total() {
      return this.filteredList.length
    },
    pagedList() {
      const start = (this.pageNum - 1) * this.pageSize
      return this.filteredList.slice(start, start + this.pageSize)
    },
    selectedSessionRemaining() {
      return this.getSessionRemaining(this.addForm && this.addForm.sessionId)
    },
    maxSelectablePlayers() {
      return this.selectedSessionRemaining > 0 ? this.selectedSessionRemaining : 1
    }
  },
  mounted() {
    this.getReservationList()
    this.loadDictionaries()
    this.loadRuntimeConfig()
    this.loadOrderLocks()
  },
  methods: {
    async loadOrderLocks() {
      if (this.isAdmin || !this.currentUserId) {
        this.paidSessionMap = {}
        return
      }
      try {
        const res = await getOrdersByUserId(this.currentUserId)
        const map = {}
        ;(res && res.code === 200 ? (res.data || []) : []).forEach(order => {
          const status = String((order && order.status) || '').toUpperCase()
          const sessionId = order && order.sessionId
          if (!sessionId) return
          if (status === 'PAID' || status === 'COMPLETED') {
            map[sessionId] = true
          }
        })
        this.paidSessionMap = map
      } catch (error) {
        this.paidSessionMap = {}
      }
    },
    isReservationLocked(row) {
      if (this.isAdmin || !row || !row.sessionId) return false
      return !!this.paidSessionMap[row.sessionId]
    },
    async loadRuntimeConfig() {
      try {
        const res = await this.$axios.get('/system/config/runtime')
        if (res && res.code === 200 && res.data) {
          this.runtimeConfig.reservationLeadHours = Number(res.data.reservation_lead_hours || 0)
        }
      } catch (error) {
        this.runtimeConfig.reservationLeadHours = 0
      }
    },
    async loadDictionaries() {
      try {
        const tasks = [getSessionList(), getScriptList()]
        if (this.isAdmin) {
          tasks.push(getUserList())
        }
        const results = await Promise.allSettled(tasks)

        const sessionRes = results[0].status === 'fulfilled' ? results[0].value : null
        const scriptRes = results[1].status === 'fulfilled' ? results[1].value : null
        const userRes = this.isAdmin && results[2] && results[2].status === 'fulfilled' ? results[2].value : null

        const sessions = sessionRes && sessionRes.code === 200 ? (sessionRes.data || []) : []
        const scripts = scriptRes && scriptRes.code === 200 ? (scriptRes.data || []) : []
        const users = userRes && userRes.code === 200 ? (userRes.data || []) : []

        this.userMap = {}
        this.userOptions = []
        if (this.isAdmin) {
          this.userOptions = users
            .filter(u => String(u.role || '').toUpperCase() === 'USER')
            .map(u => {
              const base = u.realName || u.username || `用户-${u.id}`
              this.$set(this.userMap, u.id, base)
              return { value: u.id, label: `${base}（${u.username || '-'}）` }
            })
        }

        this.scriptMap = {}
        scripts.forEach(s => this.$set(this.scriptMap, s.id, s.scriptName || `剧本-${s.id}`))

        this.sessionMap = {}
        this.sessionRawMap = {}
        this.sessionOptions = sessions.map(s => {
          const label = this.buildSessionOptionLabel(s)
          this.$set(this.sessionMap, s.id, label)
          this.$set(this.sessionRawMap, s.id, s)
          return { value: s.id, label }
        })
      } catch (error) {
        console.error('加载预约字典失败:', error)
        this.userOptions = []
        this.sessionOptions = []
        this.userMap = {}
        this.scriptMap = {}
        this.sessionMap = {}
        this.sessionRawMap = {}
      }
    },
    buildSessionOptionLabel(session) {
      if (!session) return '-'
      const scriptName = this.scriptMap[session.scriptId] || `剧本-${session.scriptId || '-'}`
      const remaining = this.getSessionRemaining(session.id)
      return `场次-${session.id}｜${scriptName}｜余位${remaining}｜${this.formatDate(session.startTime)}`
    },
    getSessionRemaining(sessionId) {
      if (!sessionId) return 0
      const session = this.sessionRawMap[sessionId]
      if (!session) return 0
      const current = Number(session.currentPlayers || 0)
      const max = Number(session.maxPlayers || 0)
      const remaining = Math.max(max - current, 0)
      return Number.isFinite(remaining) ? remaining : 0
    },
    getUserLabel(userId) {
      if (!userId) return '-'
      return this.userMap[userId] ? `${this.userMap[userId]}（ID:${userId}）` : `用户-${userId}`
    },
    getSessionLabel(sessionId) {
      if (!sessionId) return '-'
      return this.sessionMap[sessionId] || `场次-${sessionId}`
    },
    getReservationList() {
      const apiCall = this.isAdmin ? getReservationList() : getReservationsByUserId(this.currentUserId)
      apiCall.then(response => {
        if (response.code === 200) {
          this.allList = response.data || []
        }
      }).catch(error => {
        console.error('获取预约列表失败:', error)
        this.allList = []
      })
    },
    async openAddDialog() {
      await this.loadDictionaries()
      await this.loadOrderLocks()
      this.addForm = {
        userId: this.isAdmin ? null : this.currentUserId,
        sessionId: null,
        playersCount: 1
      }
      this.addDialogVisible = true
    },
    handleSessionChange(sessionId) {
      const remaining = this.getSessionRemaining(sessionId)
      if (remaining < 1) {
        this.addForm.playersCount = 1
        return
      }
      if (Number(this.addForm.playersCount || 1) > remaining) {
        this.addForm.playersCount = remaining
      }
    },
    addReservation() {
      this.$refs.addForm.validate((valid) => {
        if (valid) {
          const remaining = this.getSessionRemaining(this.addForm.sessionId)
          if (remaining < 1) {
            this.$message.error('该场次已满，请选择其他场次')
            return
          }
          if (Number(this.addForm.playersCount) > remaining) {
            this.$message.error(`超过剩余名额，当前最多可预约 ${remaining} 人`)
            return
          }
          const selectedSession = this.sessionRawMap[this.addForm.sessionId]
          if (selectedSession && selectedSession.startTime) {
            const startAt = new Date(selectedSession.startTime).getTime()
            if (!Number.isNaN(startAt) && startAt <= Date.now()) {
              this.$message.error('该场次已到开场时间，无法预约，请选择其他场次')
              return
            }
          }
          const payload = {
            ...this.addForm,
            userId: this.isAdmin ? this.addForm.userId : this.currentUserId
          }
          addReservation(payload).then(response => {
            if (response.code === 200) {
              this.$message.success('预约成功，已自动生成待支付订单，请前往订单管理支付')
              this.addDialogVisible = false
              this.getReservationList()
              this.loadOrderLocks()
            } else {
              this.$message.error(response.message)
            }
          }).catch(error => {
            console.error('添加预约失败:', error)
            this.$message.error((error && error.message) || '添加预约失败')
          })
        }
      })
    },
    cancelReservation(id) {
      this.$confirm('确定要取消该预约吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        cancelReservation(id).then(response => {
          if (response.code === 200) {
            this.$message.success('取消成功')
            this.getReservationList()
            this.loadOrderLocks()
          } else {
            this.$message.error(response.message)
          }
        }).catch(error => {
          console.error('取消预约失败:', error)
          this.$message.error((error && error.message) || '取消预约失败')
        })
      })
    },
    deleteReservation(id) {
      this.$confirm('确定要删除该预约吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        deleteReservation(id).then(response => {
          if (response.code === 200) {
            this.$message.success('删除成功')
            this.getReservationList()
            this.loadOrderLocks()
          } else {
            this.$message.error(response.message)
          }
        }).catch(error => {
          console.error('删除预约失败:', error)
          this.$message.error((error && error.message) || '删除预约失败')
        })
      })
    },
    formatDate(date) {
      if (!date) return ''
      return new Date(date).toLocaleString()
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
.reservation-container {
  padding: 20px;
}

.reservation-content {
  padding: 0;
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
.toolbar-left, .toolbar-right { display: flex; gap: 10px; align-items: center; }

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
  padding: 12px 0;
}

.dialog-footer {
  width: 100%;
  display: flex;
  justify-content: flex-end;
}
</style>
