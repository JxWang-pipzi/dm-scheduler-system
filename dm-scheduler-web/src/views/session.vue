<template>
  <Layout>
    <div class="page-toolbar">
      <div class="toolbar-left">
        <el-input v-model="searchKeyword" placeholder="搜索剧本名称" prefix-icon="el-icon-search" clearable size="small" style="width: 200px" @clear="loadPage" @keyup.enter.native="loadPage"></el-input>
        <el-select v-model="searchStatus" placeholder="状态" clearable size="small" style="width: 120px" @change="loadPage">
          <el-option label="等待中" value="PENDING"></el-option>
          <el-option label="进行中" value="ONGOING"></el-option>
          <el-option label="已完成" value="COMPLETED"></el-option>
          <el-option label="已取消" value="CANCELLED"></el-option>
        </el-select>
        <el-button type="primary" size="small" icon="el-icon-search" @click="loadPage">搜索</el-button>
      </div>
      <div class="toolbar-right">
        <el-button
          v-if="isAdmin"
          type="warning"
          size="small"
          icon="el-icon-s-operation"
          :loading="autoAssignLoading"
          @click="handleAutoAssignNextWeek">智能调度下周</el-button>
        <el-button type="primary" size="small" icon="el-icon-plus" @click="openAddDialog">新建场次</el-button>
      </div>
    </div>

    <el-table :data="sessionList" style="width: 100%" v-loading="loading">
      <el-table-column prop="id" label="ID" width="60"></el-table-column>
      <el-table-column label="场次剧本" min-width="180" show-overflow-tooltip>
        <template slot-scope="scope">
          <span style="font-weight:600;color:#E2C082">{{ getScriptName(scope.row.scriptId) }}</span>
          <span style="color:#9D96A3;font-size:12px;margin-left:8px">(ID:{{ scope.row.scriptId }})</span>
        </template>
      </el-table-column>
      <el-table-column label="主带DM" min-width="120">
        <template slot-scope="scope">
          <span style="color:#A3824B"><i class="el-icon-s-custom"></i> {{ getDmName(scope.row.dmId) }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="startTime" label="开始时间" min-width="160">
        <template slot-scope="scope">{{ formatDate(scope.row.startTime) }}</template>
      </el-table-column>
      <el-table-column prop="endTime" label="结束时间" min-width="160">
        <template slot-scope="scope">{{ formatDate(scope.row.endTime) }}</template>
      </el-table-column>
      <el-table-column prop="status" label="状态" min-width="100">
        <template slot-scope="scope">
          <el-tag :type="statusType(scope.row.status)" size="small" effect="dark">{{ statusText(scope.row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="玩家与进度" min-width="170">
        <template slot-scope="scope">
          <span style="font-weight:600">{{ scope.row.currentPlayers || 0 }}</span>
          <span style="color:#9D96A3"> / {{ scope.row.maxPlayers || 0 }} </span>
          <span style="color:#9D96A3;font-size:12px;margin-left:6px">开局≥{{ getScriptMinPlayers(scope.row.scriptId) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="320" fixed="right">
        <template slot-scope="scope">
          <el-button v-if="scope.row.status === 'PENDING'" type="text" size="small" icon="el-icon-video-play" style="color:#67C23A" @click="handleStart(scope.row)">开局</el-button>
          <el-button v-if="scope.row.status === 'IN_PROGRESS' || scope.row.status === 'ONGOING'" type="text" size="small" icon="el-icon-finished" style="color:#E6A23C" @click="handleComplete(scope.row)">完场</el-button>
          <el-button v-if="canEditSession(scope.row)" type="text" size="small" icon="el-icon-edit" @click="openEditDialog(scope.row)">编辑</el-button>
          <el-button v-if="scope.row.status === 'PENDING'" type="text" size="small" icon="el-icon-s-custom" @click="recommendDm(scope.row)">推荐DM</el-button>
          <el-button v-if="canDeleteSession(scope.row)" type="text" size="small" icon="el-icon-delete" style="color: #E17055" @click="handleDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-wrapper">
      <el-pagination @size-change="handleSizeChange" @current-change="handleCurrentChange"
        :current-page="pageNum" :page-sizes="[10, 20, 50]" :page-size="pageSize"
        layout="total, sizes, prev, pager, next, jumper" :total="total"></el-pagination>
    </div>

    <!-- 添加/编辑场次对话框 -->
    <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="500px" :close-on-click-modal="false">
      <el-form ref="sessionForm" :model="sessionForm" :rules="rules" label-width="90px">
        <el-form-item label="剧本" prop="scriptId">
          <el-select v-model="sessionForm.scriptId" filterable placeholder="请选择剧本" style="width:100%" @change="handleScriptChange">
            <el-option
              v-for="item in scriptOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="主带DM" prop="dmId">
          <el-select v-model="sessionForm.dmId" filterable placeholder="请选择DM" style="width:100%">
            <el-option
              v-for="item in dmOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
          <div style="margin-top: 8px;">
            <el-button type="text" size="mini" :disabled="!sessionForm.scriptId" :loading="recommendLoading" @click="loadRecommendedDmsForForm">智能推荐DM</el-button>
            <span style="color:#9D96A3;font-size:12px;margin-left:8px;" v-if="sessionForm.scriptId">依据剧本所需DM等级推荐</span>
          </div>
          <div v-if="recommendedFormDms.length" style="margin-top: 6px;">
            <el-tag
              v-for="item in recommendedFormDms"
              :key="item.id"
              size="mini"
              effect="plain"
              style="margin-right: 6px; margin-bottom: 6px; cursor: pointer;"
              @click="sessionForm.dmId = item.id">
              {{ formatDmRecommendLabel(item) }}
            </el-tag>
          </div>
        </el-form-item>
        <el-form-item label="开始时间" prop="startTime">
          <el-date-picker v-model="sessionForm.startTime" type="datetime" style="width:100%" placeholder="请选择开始时间"></el-date-picker>
        </el-form-item>
        <el-form-item label="结束时间" prop="endTime">
          <el-date-picker v-model="sessionForm.endTime" type="datetime" style="width:100%" placeholder="请选择结束时间"></el-date-picker>
        </el-form-item>
        <el-form-item label="最大玩家" prop="maxPlayers">
          <el-input-number v-model="sessionForm.maxPlayers" :min="1" :max="20" style="width:100%"></el-input-number>
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="dialogVisible = false" size="small">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitForm" size="small">确定</el-button>
      </span>
    </el-dialog>

    <!-- 推荐DM对话框 -->
    <el-dialog title="推荐DM" :visible.sync="recommendDialogVisible" width="500px">
      <div v-if="recommendedDms.length > 0">
        <el-table :data="recommendedDms" size="small">
          <el-table-column label="DM" width="90">
            <template slot-scope="scope">
              <span>DM-{{ scope.row.id }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="dmLevel" label="等级" width="60"></el-table-column>
          <el-table-column prop="rating" label="评分" width="80"></el-table-column>
          <el-table-column prop="specialty" label="擅长" min-width="100"></el-table-column>
          <el-table-column label="操作" width="80">
            <template slot-scope="scope">
              <el-button type="primary" size="mini" @click="assignDm(scope.row)">选择</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <div v-else style="text-align: center; color: #A0AEC0; padding: 20px">暂无推荐DM</div>
    </el-dialog>
  </Layout>
</template>

<script>
import Layout from '@/components/Layout.vue'
import { getSessionPage, addSession, updateSession, deleteSession, startSession, completeSession, autoAssignNextWeek } from '@/api/session'
import { getScriptList } from '@/api/script'
import dmApi from '@/api/dm'
import { recommendDmByLevel } from '@/api/dm-schedule'

export default {
  name: 'Session',
  components: { Layout },
  data() {
    const validateEndTime = (rule, value, callback) => {
      if (!value) return callback(new Error('请选择结束时间'))
      const start = this.sessionForm.startTime ? new Date(this.sessionForm.startTime).getTime() : null
      const end = new Date(value).getTime()
      if (!start) return callback(new Error('请先选择开始时间'))
      if (isNaN(end) || end <= start) return callback(new Error('结束时间必须晚于开始时间'))
      callback()
    }
    const validateMaxPlayers = (rule, value, callback) => {
      if (value === null || value === undefined || value === '') return callback(new Error('请输入最大玩家数'))
      if (Number(value) <= 0) return callback(new Error('最大玩家数必须大于0'))
      callback()
    }
    return {
      currentUser: JSON.parse(localStorage.getItem('user') || '{}'),
      autoAssignLoading: false,
      loading: false, sessionList: [], total: 0, pageNum: 1, pageSize: 10,
      searchKeyword: '', searchStatus: '',
      dialogVisible: false, dialogTitle: '新建场次', isEdit: false, submitting: false,
      sessionForm: { scriptId: null, dmId: null, startTime: null, endTime: null, maxPlayers: 6, status: 'PENDING', currentPlayers: 0 },
      rules: {
        scriptId: [{ required: true, message: '请选择剧本', trigger: 'change' }],
        dmId: [{ required: true, message: '请选择DM', trigger: 'change' }],
        startTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
        endTime: [{ validator: validateEndTime, trigger: 'change' }],
        maxPlayers: [{ validator: validateMaxPlayers, trigger: 'blur' }]
      },
      recommendDialogVisible: false, recommendedDms: [], currentSession: null,
      recommendLoading: false,
      recommendedFormDms: [],
      scriptMap: {}, scriptInfoMap: {}, dmMap: {},
      scriptOptions: [],
      dmOptions: []
    }
  },
  computed: {
    isAdmin() {
      return String((this.currentUser && this.currentUser.role) || '').toUpperCase() === 'ADMIN'
    }
  },
  mounted() { this.loadDictionaries().then(() => this.loadPage()) },
  methods: {
    handleAutoAssignNextWeek() {
      this.$confirm('确认自动生成下周场次并同步DM排班吗？', '自动排班确认', { type: 'warning' })
        .then(async () => {
          this.autoAssignLoading = true
          try {
            const res = await autoAssignNextWeek()
            if (res && res.code === 200 && res.data) {
              const alreadyGenerated = !!res.data.alreadyGenerated
              const weekStart = res.data.weekStart || ''
              const weekEnd = res.data.weekEnd || ''
              if (alreadyGenerated) {
                const existing = Number(res.data.existingSessionCount || 0)
                this.$message.warning(`下周已生成场次（${weekStart} ~ ${weekEnd}，共 ${existing} 场）`)
              } else {
                const sessionCount = Number(res.data.createdSessionCount || 0)
                const scheduleCount = Number(res.data.createdScheduleCount || 0)
                const skippedCount = Number(res.data.skippedCount || 0)
                this.$message.success(`智能调度完成：已生成场次 ${sessionCount} 场，DM排班 ${scheduleCount} 条，跳过 ${skippedCount} 场`)
              }
              this.loadPage()
            }
          } catch (err) {
            this.$message.error((err && err.message) || '智能调度失败')
          } finally {
            this.autoAssignLoading = false
          }
        })
        .catch(() => {})
    },
    async loadDictionaries() {
      try {
        const [sRes, dRes] = await Promise.all([getScriptList(), dmApi.getDmList()])
        if (sRes && sRes.code === 200) {
          this.scriptOptions = (sRes.data || []).map(s => ({
            value: s.id,
            label: `${s.scriptName || `剧本-${s.id}`}${s.type ? `（${s.type}）` : ''}`
          }))
          sRes.data.forEach(s => {
            this.$set(this.scriptMap, s.id, s.scriptName)
            this.$set(this.scriptInfoMap, s.id, s)
          })
        }
        if (dRes && dRes.code === 200) {
          this.dmOptions = (dRes.data || []).map(d => {
            const label = `DM-${d.id}（等级${d.dmLevel || 1}，${this.dmStatusText(d.status)}）`
            return { value: d.id, label }
          })
          dRes.data.forEach(d => this.$set(this.dmMap, d.id, `DM-${d.id}`))
        }
      } catch (err) {
        console.error('Failed to load dicts', err)
      }
    },
    async handleScriptChange(scriptId) {
      this.recommendedFormDms = []
      if (!scriptId) return
      await this.loadRecommendedDmsForForm()
    },
    formatDmRecommendLabel(dm) {
      if (!dm) return 'DM'
      const level = dm.dmLevel == null ? '-' : dm.dmLevel
      const rating = dm.rating == null ? '-' : dm.rating
      return `DM-${dm.id} Lv${level} 评分${rating}`
    },
    async loadRecommendedDmsForForm() {
      const script = this.scriptInfoMap[this.sessionForm.scriptId] || {}
      const needLevel = Number(script.needDmLevel || 1)
      this.recommendLoading = true
      try {
        const res = await recommendDmByLevel(needLevel > 0 ? needLevel : 1)
        if (res && res.code === 200) {
          this.recommendedFormDms = res.data || []
          // 新建场次时，若尚未选DM，自动填充首个推荐
          if (!this.sessionForm.dmId && this.recommendedFormDms.length > 0) {
            this.sessionForm.dmId = this.recommendedFormDms[0].id
          }
        }
      } catch (err) {
        this.recommendedFormDms = []
      } finally {
        this.recommendLoading = false
      }
    },
    getScriptName(id) { return this.scriptMap[id] || '未知剧本' },
    getScriptMinPlayers(id) {
      const script = this.scriptInfoMap[id] || {}
      const min = Number(script.minPlayers || 1)
      return min > 0 ? min : 1
    },
    getDmName(id) { return this.dmMap[id] || '未知DM' },
    dmStatusText(status) {
      return { AVAILABLE: '可用', BUSY: '忙碌', OFFLINE: '离线' }[status] || '未知'
    },
    async loadPage() {
      this.loading = true
      try {
        const res = await getSessionPage({
          pageNum: this.pageNum, pageSize: this.pageSize,
          keyword: this.searchKeyword || undefined, status: this.searchStatus || undefined
        })
        if (res && res.code === 200 && res.data) {
          this.sessionList = res.data.list || []; this.total = res.data.total || 0
        }
      } catch (err) { console.error(err) } finally { this.loading = false }
    },
    handleSizeChange(val) { this.pageSize = val; this.pageNum = 1; this.loadPage() },
    handleCurrentChange(val) { this.pageNum = val; this.loadPage() },
    formatDate(d) { return d ? new Date(d).toLocaleString() : '-' },
    statusType(s) { return { PENDING: 'info', ONGOING: 'warning', IN_PROGRESS: 'warning', COMPLETED: 'success', CANCELLED: 'danger' }[s] || 'info' },
    statusText(s) { return { PENDING: '等待中', ONGOING: '进行中', IN_PROGRESS: '进行中', COMPLETED: '已完成', CANCELLED: '已取消' }[s] || s },
    async openAddDialog() {
      await this.loadDictionaries()
      this.isEdit = false; this.dialogTitle = '新建场次'
      this.sessionForm = { scriptId: null, dmId: null, startTime: null, endTime: null, maxPlayers: 6, status: 'PENDING', currentPlayers: 0 }
      this.recommendedFormDms = []
      this.dialogVisible = true
      this.$nextTick(() => { if (this.$refs.sessionForm) this.$refs.sessionForm.clearValidate() })
    },
    async openEditDialog(row) {
      await this.loadDictionaries()
      this.isEdit = true
      this.dialogTitle = '编辑场次'
      this.sessionForm = { ...row, status: this.normalizeSessionStatus(row && row.status) || 'PENDING' }
      this.recommendedFormDms = []
      this.dialogVisible = true
    },
    async submitForm() {
      this.$refs.sessionForm.validate(async valid => {
        if (!valid) return; this.submitting = true
        try {
          const payload = { ...this.sessionForm }
          payload.status = this.isEdit
            ? (this.normalizeSessionStatus(payload.status) || 'PENDING')
            : 'PENDING'
          const res = this.isEdit ? await updateSession(payload) : await addSession(payload)
          if (res && res.code === 200) { this.$message.success(this.isEdit ? '更新成功' : '添加成功'); this.dialogVisible = false; this.loadPage() }
        } catch (err) { this.$message.error((err && err.message) || (this.isEdit ? '更新失败' : '添加失败')) } finally { this.submitting = false }
      })
    },
    normalizeSessionStatus(status) {
      const normalized = String(status || '').trim().toUpperCase()
      if (normalized === 'IN_PROGRESS') return 'ONGOING'
      return normalized
    },
    canEditSession(row) {
      const status = this.normalizeSessionStatus(row && row.status)
      return status === 'PENDING' || status === 'ONGOING'
    },
    canDeleteSession(row) {
      const status = this.normalizeSessionStatus(row && row.status)
      return status === 'PENDING' || status === 'CANCELLED'
    },
    handleStart(row) {
      this.$confirm('确认将该场次设置为进行中吗？', '开局确认', { type: 'warning' }).then(async () => {
        try {
          const res = await startSession(row.id)
          if (res && res.code === 200) {
            this.$message.success('开局成功')
            this.loadPage()
          }
        } catch (err) {
          this.$message.error((err && err.message) || '开局失败')
        }
      }).catch(() => {})
    },
    handleComplete(row) {
      this.$confirm('确认将该场次设置为已完成吗？', '完场确认', { type: 'warning' }).then(async () => {
        try {
          const res = await completeSession(row.id)
          if (res && res.code === 200) {
            this.$message.success('完场成功')
            this.loadPage()
          }
        } catch (err) {
          this.$message.error((err && err.message) || '完场失败')
        }
      }).catch(() => {})
    },
    handleDelete(row) {
      this.$confirm('确定要删除该场次吗？', '提示', { type: 'warning' }).then(async () => {
        try { const res = await deleteSession(row.id); if (res && res.code === 200) { this.$message.success('删除成功'); this.loadPage() } }
        catch (err) { this.$message.error('删除失败') }
      }).catch(() => {})
    },
    async recommendDm(row) {
      this.currentSession = row; this.recommendDialogVisible = true
      try {
        const script = this.scriptInfoMap[row.scriptId] || {}
        const needLevel = Number(script.needDmLevel || 1)
        const res = await recommendDmByLevel(needLevel > 0 ? needLevel : 1)
        if (res && res.code === 200) { this.recommendedDms = (res.data || []).slice(0, 10) }
      } catch (err) { this.recommendedDms = [] }
    },
    assignDm(dm) {
      if (this.currentSession) {
        this.currentSession.dmId = dm.id
        updateSession(this.currentSession).then(res => {
          if (res && res.code === 200) { this.$message.success('DM分配成功'); this.recommendDialogVisible = false; this.loadPage() }
        }).catch(() => { this.$message.error('分配失败') })
      }
    }
  }
}
</script>

<style scoped>
.page-toolbar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; padding: 16px 20px; background: rgba(28,27,32,0.8); border-radius: 8px; border: 1px solid #36323D; }
.toolbar-left { display: flex; gap: 10px; align-items: center; }
.pagination-wrapper { display: flex; justify-content: flex-end; margin-top: 16px; padding: 12px 0; }
</style>
