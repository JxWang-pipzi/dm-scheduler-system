<template>
  <Layout>
    <div class="page-toolbar">
      <div class="toolbar-left">
        <el-date-picker v-model="selectedDate" type="date" placeholder="选择日期" size="small" style="width: 180px"
          value-format="yyyy-MM-dd" @change="loadPage"></el-date-picker>
        <el-select v-model="searchStatus" placeholder="状态" clearable size="small" style="width: 120px" @change="loadPage">
          <el-option label="空闲" value="AVAILABLE"></el-option>
          <el-option label="已排班" value="ASSIGNED"></el-option>
          <el-option label="休息" value="REST"></el-option>
        </el-select>
        <el-button type="primary" size="small" icon="el-icon-search" @click="loadPage">查询</el-button>
      </div>
      <div class="toolbar-right">
        <el-button type="primary" size="small" icon="el-icon-plus" @click="openAddDialog">添加排班</el-button>
      </div>
    </div>

    <el-table :data="scheduleList" style="width: 100%" v-loading="loading">
      <el-table-column prop="id" label="ID" width="60"></el-table-column>
      <el-table-column label="DM" min-width="120">
        <template slot-scope="scope">
          <span style="color:#A3824B"><i class="el-icon-s-custom"></i> {{ getDmLabel(scope.row.dmId) }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="scheduleDate" label="排班日期" min-width="140">
        <template slot-scope="scope">{{ scope.row.scheduleDate ? new Date(scope.row.scheduleDate).toLocaleDateString() : '-' }}</template>
      </el-table-column>
      <el-table-column prop="timeSlot" label="时间段" min-width="120">
        <template slot-scope="scope">
          <el-tag size="small" effect="dark" :type="scope.row.timeSlot === '上午' ? '' : scope.row.timeSlot === '下午' ? 'warning' : 'danger'">
            {{ scope.row.timeSlot }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="场次信息" min-width="140">
        <template slot-scope="scope">
          <span v-if="scope.row.sessionId" style="color:#E2C082">{{ getSessionLabel(scope.row.sessionId) }}</span>
          <span v-else style="color:#9D96A3">-</span>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" min-width="120">
        <template slot-scope="scope">
          <el-tag :type="scope.row.status === 'AVAILABLE' ? 'success' : scope.row.status === 'ASSIGNED' ? 'warning' : 'info'" size="small" effect="dark">
            {{ scope.row.status === 'AVAILABLE' ? '空闲' : scope.row.status === 'ASSIGNED' ? '已排班' : '休息' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="创建时间" min-width="180">
        <template slot-scope="scope">{{ scope.row.createdAt ? new Date(scope.row.createdAt).toLocaleString() : '-' }}</template>
      </el-table-column>
      <el-table-column label="操作" width="140" fixed="right">
        <template slot-scope="scope">
          <el-button type="text" size="small" icon="el-icon-edit" @click="openEditDialog(scope.row)">编辑</el-button>
          <el-button type="text" size="small" icon="el-icon-delete" style="color: #E17055" @click="handleDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-wrapper">
      <el-pagination @size-change="handleSizeChange" @current-change="handleCurrentChange"
        :current-page="pageNum" :page-sizes="[10, 20, 50]" :page-size="pageSize"
        layout="total, sizes, prev, pager, next, jumper" :total="total"></el-pagination>
    </div>

    <!-- 添加/编辑排班对话框 -->
    <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="450px" :close-on-click-modal="false">
      <el-form ref="scheduleForm" :model="scheduleForm" :rules="rules" label-width="80px">
        <el-form-item label="DM" prop="dmId">
          <el-select v-model="scheduleForm.dmId" filterable placeholder="请选择DM" style="width:100%">
            <el-option
              v-for="item in dmOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="排班日期" prop="scheduleDate">
          <el-date-picker v-model="scheduleForm.scheduleDate" type="date" style="width:100%" value-format="yyyy-MM-dd" placeholder="请选择日期"></el-date-picker>
        </el-form-item>
        <el-form-item label="时间段" prop="timeSlot">
          <el-select v-model="scheduleForm.timeSlot" style="width:100%">
            <el-option label="上午" value="上午"></el-option>
            <el-option label="下午" value="下午"></el-option>
            <el-option label="晚上" value="晚上"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="关联场次">
          <el-select v-model="scheduleForm.sessionId" clearable filterable placeholder="可选" style="width:100%">
            <el-option
              v-for="item in sessionOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="scheduleForm.status" style="width:100%">
            <el-option label="空闲" value="AVAILABLE"></el-option>
            <el-option label="已排班" value="ASSIGNED"></el-option>
            <el-option label="休息" value="REST"></el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="dialogVisible = false" size="small">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitForm" size="small">确定</el-button>
      </span>
    </el-dialog>
  </Layout>
</template>

<script>
import Layout from '@/components/Layout.vue'
import { getDmSchedulePage, addDmSchedule, updateDmSchedule, deleteDmSchedule } from '@/api/dm-schedule'
import dmApi from '@/api/dm'
import { getSessionList } from '@/api/session'
import { getScriptList } from '@/api/script'

export default {
  name: 'DmSchedule',
  components: { Layout },
  data() {
    return {
      currentUser: JSON.parse(localStorage.getItem('user') || '{}'),
      loading: false, scheduleList: [], total: 0, pageNum: 1, pageSize: 10,
      selectedDate: '', searchStatus: '',
      dialogVisible: false, dialogTitle: '添加排班', isEdit: false, submitting: false,
      scheduleForm: { dmId: null, scheduleDate: '', timeSlot: '上午', sessionId: null, status: 'AVAILABLE' },
      rules: {
        dmId: [{ required: true, message: '请选择DM', trigger: 'change' }],
        scheduleDate: [{ required: true, message: '请选择排班日期', trigger: 'change' }],
        timeSlot: [{ required: true, message: '请选择时间段', trigger: 'change' }]
      },
      dmOptions: [],
      sessionOptions: [],
      dmMap: {},
      sessionMap: {},
      scriptMap: {}
    }
  },
  computed: {
    isAdmin() {
      return String((this.currentUser && this.currentUser.role) || '').toUpperCase() === 'ADMIN'
    }
  },
  mounted() { this.loadDictionaries().then(() => this.loadPage()) },
  methods: {
    async loadDictionaries() {
      try {
        const [dmRes, sessionRes, scriptRes] = await Promise.all([
          dmApi.getDmList(),
          getSessionList(),
          getScriptList()
        ])
        const dmList = dmRes && dmRes.code === 200 ? (dmRes.data || []) : []
        const sessionList = sessionRes && sessionRes.code === 200 ? (sessionRes.data || []) : []
        const scriptList = scriptRes && scriptRes.code === 200 ? (scriptRes.data || []) : []

        this.scriptMap = {}
        scriptList.forEach(s => {
          this.$set(this.scriptMap, s.id, s.scriptName || `剧本-${s.id}`)
        })

        this.dmMap = {}
        this.dmOptions = dmList.map(d => {
          const label = `DM-${d.id}（等级${d.dmLevel || 1}，${this.dmStatusText(d.status)}）`
          this.$set(this.dmMap, d.id, label)
          return { value: d.id, label }
        })

        this.sessionMap = {}
        this.sessionOptions = sessionList.map(s => {
          const label = this.buildSessionOptionLabel(s)
          this.$set(this.sessionMap, s.id, label)
          return { value: s.id, label }
        })
      } catch (err) {
        console.error('加载排班字典失败', err)
        this.dmOptions = []
        this.sessionOptions = []
        this.dmMap = {}
        this.sessionMap = {}
        this.scriptMap = {}
      }
    },
    dmStatusText(status) {
      return { AVAILABLE: '空闲', BUSY: '忙碌', OFFLINE: '离线', REST: '休息' }[status] || '未知'
    },
    formatDateTime(dateStr) {
      if (!dateStr) return '-'
      const d = new Date(dateStr)
      if (isNaN(d.getTime())) return dateStr
      return `${d.getMonth() + 1}/${d.getDate()} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
    },
    buildSessionOptionLabel(session) {
      if (!session) return '-'
      const scriptName = this.scriptMap[session.scriptId] || `剧本-${session.scriptId || '-'}`
      const start = this.formatDateTime(session.startTime)
      return `场次-${session.id}｜${scriptName}｜${start}`
    },
    getDmLabel(dmId) {
      if (!dmId) return '-'
      return this.dmMap[dmId] || `DM-${dmId}`
    },
    getSessionLabel(sessionId) {
      if (!sessionId) return '-'
      return this.sessionMap[sessionId] || `场次-${sessionId}`
    },
    async loadPage() {
      this.loading = true
      try {
        const res = await getDmSchedulePage({
          pageNum: this.pageNum, pageSize: this.pageSize,
          date: this.selectedDate || undefined, status: this.searchStatus || undefined
        })
        if (res && res.code === 200 && res.data) {
          this.scheduleList = res.data.list || []; this.total = res.data.total || 0
        }
      } catch (err) { console.error(err) } finally { this.loading = false }
    },
    handleSizeChange(val) { this.pageSize = val; this.pageNum = 1; this.loadPage() },
    handleCurrentChange(val) { this.pageNum = val; this.loadPage() },
    async openAddDialog() {
      await this.loadDictionaries()
      this.isEdit = false; this.dialogTitle = '添加排班'
      this.scheduleForm = { dmId: null, scheduleDate: '', timeSlot: '上午', sessionId: null, status: 'AVAILABLE' }
      this.dialogVisible = true
      this.$nextTick(() => { if (this.$refs.scheduleForm) this.$refs.scheduleForm.clearValidate() })
    },
    async openEditDialog(row) {
      await this.loadDictionaries()
      this.isEdit = true
      this.dialogTitle = '编辑排班'
      this.scheduleForm = { ...row }
      this.dialogVisible = true
    },
    submitForm() {
      this.$refs.scheduleForm.validate(async valid => {
        if (!valid) return; this.submitting = true
        try {
          const res = this.isEdit ? await updateDmSchedule(this.scheduleForm) : await addDmSchedule(this.scheduleForm)
          if (res && res.code === 200) { this.$message.success(this.isEdit ? '更新成功' : '添加成功'); this.dialogVisible = false; this.loadPage() }
        } catch (err) { this.$message.error(this.isEdit ? '更新失败' : '添加失败') } finally { this.submitting = false }
      })
    },
    handleDelete(row) {
      this.$confirm('确定要删除该排班吗？', '提示', { type: 'warning' }).then(async () => {
        try { const res = await deleteDmSchedule(row.id); if (res && res.code === 200) { this.$message.success('删除成功'); this.loadPage() } }
        catch (err) { this.$message.error('删除失败') }
      }).catch(() => {})
    }
  }
}
</script>

<style scoped>
.page-toolbar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; padding: 16px 20px; background: rgba(28,27,32,0.8); border-radius: 8px; border: 1px solid #36323D; }
.toolbar-left { display: flex; gap: 10px; align-items: center; }
.pagination-wrapper { display: flex; justify-content: flex-end; margin-top: 16px; padding: 12px 0; }
</style>
