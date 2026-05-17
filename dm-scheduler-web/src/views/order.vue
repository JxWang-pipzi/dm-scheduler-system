<template>
  <Layout>
    <div class="page-toolbar">
      <el-form :inline="true" size="small" class="dark-form">
        <el-form-item>
          <el-input v-model="searchKeyword" placeholder="搜索订单编号" clearable prefix-icon="el-icon-search"></el-input>
        </el-form-item>
        <el-form-item>
          <el-select v-model="searchStatus" placeholder="订单状态" clearable style="width: 140px;">
            <el-option label="待支付" value="PENDING"></el-option>
            <el-option label="已支付" value="PAID"></el-option>
            <el-option label="已完成" value="COMPLETED"></el-option>
            <el-option label="已取消" value="CANCELLED"></el-option>
            <el-option label="已退款" value="REFUNDED"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch" class="search-btn"><i class="el-icon-search"></i> 搜索</el-button>
          <el-button @click="resetSearch" class="reset-btn"><i class="el-icon-refresh-right"></i> 重置</el-button>
        </el-form-item>
      </el-form>
      <div class="toolbar-actions">
        <!-- 订单一般由用户端生成或预约转换，管理端支持手动添加或导出 -->
        <el-button v-if="isAdmin" type="primary" size="small" @click="handleAdd" class="add-btn"><i class="el-icon-plus"></i> 手动录入订单</el-button>
      </div>
    </div>

    <!-- 订单列表展示 -->
    <el-card class="data-card dark-card" :body-style="{ padding: '0px' }" shadow="never">
      <el-table :data="tableData" style="width: 100%" v-loading="loading">
        <el-table-column prop="id" label="ID" width="60" align="center"></el-table-column>
        <el-table-column prop="orderNo" label="订单编号" min-width="150">
          <template slot-scope="scope">
            <span class="mono-text">{{ scope.row.orderNo }}</span>
          </template>
        </el-table-column>
        <el-table-column v-if="isAdmin" label="下单用户" min-width="140">
          <template slot-scope="scope">
            <span>{{ getUserLabel(scope.row.userId) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="关联场次" min-width="220">
          <template slot-scope="scope">
            <span>{{ getSessionLabel(scope.row.sessionId) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="totalPrice" label="总价 (元)" width="100" align="right">
          <template slot-scope="scope">
            <span class="price-text">¥{{ scope.row.totalPrice }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template slot-scope="scope">
            <el-tag :type="getStatusType(scope.row.status)" size="small" effect="dark" class="status-tag">
              {{ getStatusLabel(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="payMethod" label="支付方式" width="100" align="center">
          <template slot-scope="scope">
            {{ getPayMethodLabel(scope.row.payMethod) }}
          </template>
        </el-table-column>
        <el-table-column prop="payTime" label="支付时间" min-width="160">
          <template slot-scope="scope">
            {{ formatDate(scope.row.payTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" min-width="160">
          <template slot-scope="scope">
            {{ formatDate(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template slot-scope="scope">
            <el-button v-if="isAdmin" size="mini" type="primary" plain @click="handleEdit(scope.row)" class="action-btn">编辑</el-button>
            <el-button v-if="isAdmin && scope.row.status === 'PAID'" size="mini" type="warning" plain @click="handleRefund(scope.row)" class="action-btn">退款</el-button>
            <el-button v-if="isAdmin" size="mini" type="danger" plain @click="handleDelete(scope.row)" class="action-btn danger-btn">删除</el-button>
            <el-button v-if="!isAdmin && scope.row.status === 'PENDING'" size="mini" type="success" plain @click="openPayDialog(scope.row)" class="action-btn">去支付</el-button>
            <span v-if="!isAdmin && scope.row.status !== 'PENDING'" style="color:#9D96A3;font-size:12px;">-</span>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页组件 -->
      <div class="pagination-container">
        <el-pagination
          background
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          :current-page="pageNum"
          :page-sizes="[10, 20, 50]"
          :page-size="pageSize"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total">
        </el-pagination>
      </div>
    </el-card>

    <!-- 订单录入/编辑表单弹窗 -->
    <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="550px" custom-class="dark-dialog" append-to-body>
      <el-form :model="form" ref="orderForm" :rules="rules" label-width="110px" class="dark-form">
        <el-form-item label="订单编号" prop="orderNo">
          <el-input v-model="form.orderNo" placeholder="留空则自动生成" :disabled="form.id"></el-input>
        </el-form-item>
        <el-form-item label="下单用户" prop="userId" v-if="isAdmin">
          <el-select v-model="form.userId" filterable placeholder="请选择用户" style="width: 100%">
            <el-option
              v-for="item in userOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="关联场次" prop="sessionId">
          <el-select v-model="form.sessionId" filterable placeholder="请选择场次" style="width: 100%" @change="handleOrderSessionChange">
            <el-option
              v-for="item in sessionOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="主带DM" v-if="isAdmin && form.sessionId">
          <div class="session-dm-row">
            <span>{{ selectedDmForSessionLabel || getCurrentSessionDmLabel(form.sessionId) }}</span>
            <el-button type="text" size="mini" :loading="orderRecommendLoading" @click="loadOrderRecommendedDms">智能推荐DM</el-button>
          </div>
          <div v-if="orderRecommendedDms.length" style="margin-top: 8px;">
            <el-tag
              v-for="item in orderRecommendedDms"
              :key="item.id"
              size="mini"
              effect="plain"
              style="margin-right: 6px; margin-bottom: 6px; cursor: pointer;"
              @click="selectRecommendedDmForOrder(item)">
              {{ formatDmRecommendLabel(item) }}
            </el-tag>
          </div>
        </el-form-item>
        <el-form-item label="订单总价" prop="totalPrice">
          <el-input-number v-model="form.totalPrice" :precision="2" :step="10" :min="0.01" style="width: 100%"></el-input-number>
        </el-form-item>
        <el-form-item label="订单状态" prop="status">
          <el-select v-model="form.status" style="width: 100%">
            <el-option
              v-for="item in getEditableStatusOptions()"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="支付方式" prop="payMethod">
          <el-select v-model="form.payMethod" style="width: 100%">
            <el-option label="未支付" value=""></el-option>
            <el-option label="微信支付" value="WECHAT"></el-option>
            <el-option label="支付宝" value="ALIPAY"></el-option>
            <el-option label="现金/前台支付" value="CASH"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="支付时间" prop="payTime" v-if="form.status === 'PAID' || form.status === 'COMPLETED'">
          <el-date-picker v-model="form.payTime" type="datetime" placeholder="选择支付时间" style="width: 100%"></el-date-picker>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input type="textarea" v-model="form.remark" :rows="3" placeholder="订单备注信息..."></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="submitForm" :loading="submitLoading">确 定</el-button>
      </div>
    </el-dialog>

    <el-dialog title="订单支付" :visible.sync="payDialogVisible" width="420px" custom-class="dark-dialog" append-to-body>
      <el-form :model="payForm" label-width="90px" class="dark-form">
        <el-form-item label="订单编号">
          <el-input :value="payForm.orderNo" disabled />
        </el-form-item>
        <el-form-item label="支付金额">
          <el-input :value="payAmountLabel" disabled />
        </el-form-item>
        <el-form-item label="支付方式">
          <el-select v-model="payForm.payMethod" style="width: 100%">
            <el-option label="微信支付" value="WECHAT"></el-option>
            <el-option label="支付宝" value="ALIPAY"></el-option>
            <el-option label="现金/前台支付" value="CASH"></el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="payDialogVisible = false">取 消</el-button>
        <el-button type="primary" :loading="payLoading" @click="handlePayConfirm">确 认 支 付</el-button>
      </div>
    </el-dialog>
  </Layout>
</template>

<script>
import Layout from '@/components/Layout.vue'
import { getOrderPage, addOrder, updateOrder, deleteOrder, payOrder } from '@/api/order.js'
import { getUserList } from '@/api/user'
import { getSessionList, getSessionById, updateSession as updateSessionApi } from '@/api/session'
import { getScriptList } from '@/api/script'
import { recommendDmByLevel } from '@/api/dm-schedule'
import dmApi from '@/api/dm'

export default {
  name: 'OrderManagement',
  components: { Layout },
  data() {
    const validateTotalPrice = (rule, value, callback) => {
      if (value === null || value === undefined || value === '') return callback(new Error('请填写订单金额'))
      if (Number(value) <= 0) return callback(new Error('订单金额必须大于0'))
      callback()
    }
    const validatePayMethod = (rule, value, callback) => {
      const status = (this.form.status || '').toUpperCase()
      if ((status === 'PAID' || status === 'COMPLETED') && (!value || !String(value).trim())) {
        return callback(new Error('已支付/已完成订单必须填写支付方式'))
      }
      callback()
    }
    const validatePayTime = (rule, value, callback) => {
      const status = (this.form.status || '').toUpperCase()
      if ((status === 'PAID' || status === 'COMPLETED') && !value) {
        return callback(new Error('已支付/已完成订单必须填写支付时间'))
      }
      callback()
    }
    return {
      currentUser: JSON.parse(localStorage.getItem('user') || '{}'),
      searchKeyword: '',
      searchStatus: '',
      pageNum: 1,
      pageSize: 10,
      total: 0,
      tableData: [],
      loading: false,

      dialogVisible: false,
      dialogTitle: '新增订单',
      submitLoading: false,
      payDialogVisible: false,
      payLoading: false,
      payForm: {
        id: null,
        orderNo: '',
        totalPrice: null,
        payMethod: 'WECHAT'
      },
      form: {
        id: null,
        orderNo: '',
        userId: null,
        sessionId: null,
        totalPrice: null,
        status: 'PENDING',
        originalStatus: '',
        payMethod: '',
        payTime: null,
        remark: ''
      },
      rules: {
        orderNo: [{ required: true, message: '请填写订单编号', trigger: 'blur' }],
        userId: [{ required: true, message: '请选择下单用户', trigger: 'change' }],
        sessionId: [{ required: true, message: '请选择场次', trigger: 'change' }],
        totalPrice: [{ validator: validateTotalPrice, trigger: 'blur' }],
        status: [{ required: true, message: '请选择订单状态', trigger: 'change' }],
        payMethod: [{ validator: validatePayMethod, trigger: 'change' }],
        payTime: [{ validator: validatePayTime, trigger: 'change' }]
      },
      userOptions: [],
      sessionOptions: [],
      userMap: {},
      sessionMap: {},
      scriptMap: {},
      scriptInfoMap: {},
      sessionInfoMap: {},
      dmMap: {},
      orderRecommendedDms: [],
      orderRecommendLoading: false,
      selectedDmForSession: null,
      allStatusOptions: [
        { label: '待支付', value: 'PENDING' },
        { label: '已支付', value: 'PAID' },
        { label: '已完成', value: 'COMPLETED' },
        { label: '已取消', value: 'CANCELLED' },
        { label: '已退款', value: 'REFUNDED' }
      ]
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
    payAmountLabel() {
      const amount = this.payForm && this.payForm.totalPrice != null ? this.payForm.totalPrice : 0
      return `¥${amount}`
    },
    selectedDmForSessionLabel() {
      if (!this.selectedDmForSession) return ''
      return `已选择：${this.formatDmRecommendLabel(this.selectedDmForSession)}`
    }
  },
  mounted() {
    this.loadDictionaries().then(() => this.fetchData())
  },
  methods: {
    openPayDialog(row) {
      this.payForm = {
        id: row.id,
        orderNo: row.orderNo || '',
        totalPrice: row.totalPrice,
        payMethod: 'WECHAT'
      }
      this.payDialogVisible = true
    },
    async handlePayConfirm() {
      if (!this.payForm || !this.payForm.id) return
      this.payLoading = true
      try {
        const res = await payOrder(this.payForm.id, this.payForm.payMethod)
        if (res && res.code === 200) {
          this.$message.success('支付成功')
          this.payDialogVisible = false
          this.fetchData()
        } else {
          this.$message.error((res && (res.message || res.msg)) || '支付失败')
        }
      } catch (e) {
        console.error(e)
        this.$message.error((e && e.message) || '支付失败')
      } finally {
        this.payLoading = false
      }
    },
    async loadDictionaries() {
      try {
        const tasks = [getSessionList(), getScriptList(), dmApi.getDmList()]
        if (this.isAdmin) {
          tasks.push(getUserList())
        }
        const results = await Promise.allSettled(tasks)

        const sessionRes = results[0].status === 'fulfilled' ? results[0].value : null
        const scriptRes = results[1].status === 'fulfilled' ? results[1].value : null
        const dmRes = results[2].status === 'fulfilled' ? results[2].value : null
        const userRes = this.isAdmin && results[3] && results[3].status === 'fulfilled' ? results[3].value : null

        const sessions = sessionRes && sessionRes.code === 200 ? (sessionRes.data || []) : []
        const scripts = scriptRes && scriptRes.code === 200 ? (scriptRes.data || []) : []
        const dms = dmRes && dmRes.code === 200 ? (dmRes.data || []) : []
        const users = userRes && userRes.code === 200 ? (userRes.data || []) : []

        this.userMap = {}
        this.userOptions = []
        if (this.isAdmin) {
          this.userOptions = users.map(u => {
            const base = u.realName || u.username || `用户-${u.id}`
            this.$set(this.userMap, u.id, base)
            return { value: u.id, label: `${base}（${u.username || '-'}）` }
          })
        }

        this.dmMap = {}
        dms.forEach(d => this.$set(this.dmMap, d.id, `DM-${d.id}`))

        this.scriptMap = {}
        this.scriptInfoMap = {}
        scripts.forEach(s => {
          this.$set(this.scriptMap, s.id, s.scriptName || `剧本-${s.id}`)
          this.$set(this.scriptInfoMap, s.id, s)
        })

        this.sessionMap = {}
        this.sessionInfoMap = {}
        this.sessionOptions = sessions.map(s => {
          const label = this.buildSessionLabel(s)
          this.$set(this.sessionInfoMap, s.id, s)
          this.$set(this.sessionMap, s.id, label)
          return { value: s.id, label }
        })
      } catch (e) {
        console.error('加载订单字典失败:', e)
        this.userOptions = []
        this.sessionOptions = []
        this.userMap = {}
        this.sessionMap = {}
        this.scriptMap = {}
        this.scriptInfoMap = {}
        this.sessionInfoMap = {}
        this.dmMap = {}
      }
    },
    formatDate(dateStr) {
      if (!dateStr) return '-'
      const d = new Date(dateStr)
      if (isNaN(d.getTime())) return dateStr
      const year = d.getFullYear()
      const month = String(d.getMonth() + 1).padStart(2, '0')
      const day = String(d.getDate()).padStart(2, '0')
      const hours = String(d.getHours()).padStart(2, '0')
      const minutes = String(d.getMinutes()).padStart(2, '0')
      const seconds = String(d.getSeconds()).padStart(2, '0')
      return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
    },
    getStatusType(status) {
      const map = { PENDING: 'warning', PAID: 'success', COMPLETED: 'info', CANCELLED: 'danger', REFUNDED: 'danger' }
      return map[status] || 'info'
    },
    getStatusLabel(status) {
      const map = { PENDING: '待支付', PAID: '已支付', COMPLETED: '已完成', CANCELLED: '已取消', REFUNDED: '已退款' }
      return map[status] || status || '未知'
    },
    getPayMethodLabel(method) {
      const map = { WECHAT: '微信支付', ALIPAY: '支付宝', CASH: '现金支付' }
      return map[method] || method || '-'
    },
    formatDateTimeShort(dateStr) {
      if (!dateStr) return '-'
      const d = new Date(dateStr)
      if (isNaN(d.getTime())) return dateStr
      return `${d.getMonth() + 1}/${d.getDate()} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
    },
    buildSessionLabel(session) {
      if (!session) return '-'
      const scriptName = this.scriptMap[session.scriptId] || `剧本-${session.scriptId || '-'}`
      const dmLabel = session.dmId ? (this.dmMap[session.dmId] || `DM-${session.dmId}`) : '未分配DM'
      return `场次-${session.id}｜${scriptName}｜${dmLabel}｜${this.formatDateTimeShort(session.startTime)}`
    },
    getCurrentSessionDmLabel(sessionId) {
      const session = this.sessionInfoMap[sessionId]
      if (!session || !session.dmId) return '未分配DM'
      return this.dmMap[session.dmId] || `DM-${session.dmId}`
    },
    formatDmRecommendLabel(dm) {
      if (!dm) return 'DM'
      const level = dm.dmLevel == null ? '-' : dm.dmLevel
      const rating = dm.rating == null ? '-' : dm.rating
      return `DM-${dm.id} Lv${level} 评分${rating}`
    },
    handleOrderSessionChange() {
      this.selectedDmForSession = null
      this.orderRecommendedDms = []
    },
    async loadOrderRecommendedDms() {
      if (!this.form.sessionId) return
      const session = this.sessionInfoMap[this.form.sessionId]
      if (!session || !session.scriptId) {
        this.$message.warning('当前场次缺少剧本信息，无法推荐DM')
        return
      }
      const script = this.scriptInfoMap[session.scriptId] || {}
      const needLevel = Number(script.needDmLevel || 1)
      this.orderRecommendLoading = true
      try {
        const res = await recommendDmByLevel(needLevel > 0 ? needLevel : 1)
        if (res && res.code === 200) {
          this.orderRecommendedDms = res.data || []
          if (!this.orderRecommendedDms.length) {
            this.$message.warning('当前条件下暂无可推荐DM')
          }
        } else {
          this.orderRecommendedDms = []
        }
      } catch (e) {
        console.error(e)
        this.orderRecommendedDms = []
        this.$message.error('获取推荐DM失败')
      } finally {
        this.orderRecommendLoading = false
      }
    },
    selectRecommendedDmForOrder(dm) {
      this.selectedDmForSession = dm
    },
    async syncSelectedDmToSession() {
      if (!this.isAdmin || !this.form.sessionId || !this.selectedDmForSession) return true
      const current = this.sessionInfoMap[this.form.sessionId]
      if (current && Number(current.dmId) === Number(this.selectedDmForSession.id)) return true
      try {
        const detailRes = await getSessionById(this.form.sessionId)
        const session = detailRes && detailRes.code === 200 ? detailRes.data : null
        if (!session) {
          this.$message.error('读取场次详情失败，无法更新DM')
          return false
        }
        const updateRes = await updateSessionApi({ ...session, dmId: this.selectedDmForSession.id })
        if (!updateRes || updateRes.code !== 200) {
          this.$message.error((updateRes && (updateRes.message || updateRes.msg)) || '更新场次DM失败')
          return false
        }
        await this.loadDictionaries()
        return true
      } catch (e) {
        console.error(e)
        this.$message.error('更新场次DM失败')
        return false
      }
    },
    getUserLabel(userId) {
      if (!userId) return '-'
      return this.userMap[userId] ? `${this.userMap[userId]}（ID:${userId}）` : `用户-${userId}`
    },
    getSessionLabel(sessionId) {
      if (!sessionId) return '-'
      return this.sessionMap[sessionId] || `场次-${sessionId}`
    },
    handleSearch() {
      this.pageNum = 1
      this.fetchData()
    },
    resetSearch() {
      this.searchKeyword = ''
      this.searchStatus = ''
      this.handleSearch()
    },
    handleSizeChange(val) {
      this.pageSize = val
      this.fetchData()
    },
    handleCurrentChange(val) {
      this.pageNum = val
      this.fetchData()
    },
    async fetchData() {
      this.loading = true
      try {
        const res = await getOrderPage({
          keyword: this.searchKeyword,
          status: this.searchStatus,
          pageNum: this.pageNum,
          pageSize: this.pageSize
        })
        if (res && res.code === 200 && res.data) {
          this.tableData = res.data.list || []
          this.total = res.data.total || 0
        }
      } catch (e) {
        console.error('获取订单列表失败:', e)
        this.$message.error('获取列表失败')
      } finally {
        this.loading = false
      }
    },
    async handleAdd() {
      await this.loadDictionaries()
      this.dialogTitle = this.isAdmin ? '手动录入订单' : '创建订单'
      this.form = {
        id: null, orderNo: '', userId: this.isAdmin ? null : this.currentUserId, sessionId: null,
        totalPrice: null, status: 'PENDING', originalStatus: 'PENDING', payMethod: '', payTime: null, remark: ''
      }
      this.selectedDmForSession = null
      this.orderRecommendedDms = []
      this.dialogVisible = true
      this.$nextTick(() => { this.$refs.orderForm && this.$refs.orderForm.clearValidate() })
    },
    async handleEdit(row) {
      await this.loadDictionaries()
      this.dialogTitle = '编辑订单信息'
      this.form = Object.assign({}, row, { originalStatus: row.status })
      this.selectedDmForSession = null
      this.orderRecommendedDms = []
      this.dialogVisible = true
    },
    getEditableStatusOptions() {
      if (!this.form || !this.form.id) {
        return this.allStatusOptions
      }
      const current = String(this.form.originalStatus || this.form.status || '').toUpperCase()
      const allowMap = {
        PENDING: ['PENDING', 'PAID', 'CANCELLED'],
        PAID: ['PAID', 'COMPLETED', 'REFUNDED'],
        COMPLETED: ['COMPLETED'],
        CANCELLED: ['CANCELLED'],
        REFUNDED: ['REFUNDED']
      }
      const allowed = allowMap[current] || [current]
      return this.allStatusOptions.filter(item => allowed.includes(item.value))
    },
    handleRefund(row) {
      this.$confirm('确认将其状态标记为已退款吗？(此处仅做状态变更，实际退款需结合支付接口)', '操作提示', { type: 'warning' })
        .then(async () => {
          try {
            const res = await updateOrder({ ...row, status: 'REFUNDED' })
            if (res.code === 200) {
              this.$message.success('已标记为退款状态')
              this.fetchData()
            } else {
              this.$message.error(res.msg || '操作失败')
            }
          } catch (e) { console.error(e) }
        }).catch(() => {})
    },
    handleDelete(row) {
      this.$confirm('确定要删除该订单记录吗？', '提示', { type: 'warning' }).then(async () => {
        const res = await deleteOrder(row.id)
        if (res.code === 200) {
          this.$message.success('删除成功')
          this.fetchData()
        } else {
          this.$message.error(res.msg || '删除失败')
        }
      }).catch(() => {})
    },
    submitForm() {
      // 若没有输入订单编号，前端生成一个模拟编号
      if (!this.form.orderNo) {
        this.form.orderNo = 'ORD' + new Date().getTime()
      }
      this.$refs.orderForm.validate(async (valid) => {
        if (!valid) return
        this.submitLoading = true
        try {
          const dmSynced = await this.syncSelectedDmToSession()
          if (!dmSynced) return
          const apiCall = this.form.id ? updateOrder : addOrder
          const { originalStatus, ...restForm } = this.form
          const payload = {
            ...restForm,
            userId: this.isAdmin ? this.form.userId : this.currentUserId
          }
          const res = await apiCall(payload)
          if (res.code === 200) {
            this.$message.success('保存成功')
            this.dialogVisible = false
            this.fetchData()
          } else {
            this.$message.error(res.msg || '保存失败')
          }
        } catch (e) {
          console.error(e)
          this.$message.error('请求出错')
        } finally {
          this.submitLoading = false
        }
      })
    }
  }
}
</script>

<style scoped>
.page-toolbar {
  display: flex; justify-content: space-between; align-items: flex-start;
  margin-bottom: 24px; flex-wrap: wrap; gap: 16px;
}
.mono-text {
  font-family: 'SFMono-Regular', Consolas, 'Liberation Mono', Menlo, monospace;
  color: #B4AFB9; font-size: 13px;
}
.price-text {
  color: #E2C082; font-weight: 600; font-family: 'Inter', sans-serif;
}
.status-tag { border-radius: 6px; }

/* 深度适配暗色主题的专属按钮和表单覆盖 */
.search-btn {
  background: linear-gradient(135deg, rgba(226,192,130,0.8), rgba(156,125,67,0.8)) !important; border: none !important; color: #fff;
}
.search-btn:hover { opacity: 0.9; }
.reset-btn {
  background: rgba(157,150,163,0.1) !important; color: #E6E1DC !important; border: 1px solid rgba(255,255,255,0.08) !important;
}
.reset-btn:hover { background: rgba(157,150,163,0.2) !important; }
.add-btn {
  background: linear-gradient(135deg, #E2C082, #9C7D43) !important; color: #1A1922 !important; border: none !important; font-weight: 600;
}
.add-btn:hover { transform: translateY(-1px); box-shadow: 0 4px 12px rgba(226,192,130,0.4); }

.pagination-container { margin-top: 20px; display: flex; justify-content: flex-end; padding: 16px 20px; }
.session-dm-row { display: flex; justify-content: space-between; align-items: center; }
</style>
