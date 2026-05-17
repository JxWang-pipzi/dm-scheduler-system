<template>
  <Layout>
    <div class="page-toolbar">
      <div class="toolbar-left">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索评价内容"
          clearable
          size="small"
          style="width: 220px"
          prefix-icon="el-icon-search"
          @clear="handleSearch"
          @keyup.enter.native="handleSearch">
        </el-input>
        <el-select v-model="searchRating" placeholder="评分筛选" clearable size="small" style="width: 130px" @change="handleSearch">
          <el-option v-for="item in [1,2,3,4,5]" :key="item" :label="`${item} 星`" :value="item"></el-option>
        </el-select>
        <el-button type="primary" size="small" icon="el-icon-search" @click="handleSearch">搜索</el-button>
      </div>
      <div class="toolbar-right">
        <el-button type="primary" size="small" icon="el-icon-plus" @click="showForm = !showForm">{{ showForm ? '收起' : '添加评价' }}</el-button>
      </div>
    </div>

    <div class="eval-form-card" v-if="showForm">
      <h3><i class="el-icon-edit-outline"></i> 添加评价</h3>
      <el-form :model="evaluationForm" :rules="rules" ref="evalForm" label-width="90px" size="small">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="场次" prop="sessionId">
              <el-select v-model="evaluationForm.sessionId" filterable placeholder="请选择场次" style="width:100%">
                <el-option
                  v-for="item in sessionOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value">
                </el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="DM" prop="dmId">
              <el-select v-model="evaluationForm.dmId" filterable placeholder="请选择DM" style="width:100%">
                <el-option
                  v-for="item in dmOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value">
                </el-option>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="评分" prop="rating">
          <el-rate v-model="evaluationForm.rating" :max="5" :colors="['#8C7355', '#D6B77A', '#E2C082']"></el-rate>
        </el-form-item>
        <el-form-item label="评价内容" prop="comment">
          <el-input type="textarea" v-model="evaluationForm.comment" :rows="3" placeholder="请输入评价内容"></el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="submitting" @click="createEvaluation">提交评价</el-button>
          <el-button @click="showForm = false">取消</el-button>
        </el-form-item>
      </el-form>
    </div>

    <el-table :data="pagedList" style="width: 100%" v-loading="loading">
      <el-table-column prop="id" label="ID" width="60"></el-table-column>
      <el-table-column v-if="isAdmin" label="用户" min-width="150">
        <template slot-scope="scope">
          <span>{{ getUserLabel(scope.row.userId) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="场次" min-width="220">
        <template slot-scope="scope">
          <span>{{ getSessionLabel(scope.row.sessionId) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="DM" min-width="160">
        <template slot-scope="scope">
          <span>{{ getDmLabel(scope.row.dmId) }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="rating" label="评分" min-width="160">
        <template slot-scope="scope">
          <el-rate v-model="scope.row.rating" disabled :max="5" :colors="['#8C7355', '#D6B77A', '#E2C082']" style="display:inline-flex"></el-rate>
        </template>
      </el-table-column>
      <el-table-column prop="comment" label="评价内容" min-width="240" show-overflow-tooltip></el-table-column>
      <el-table-column prop="createdAt" label="创建时间" min-width="160">
        <template slot-scope="scope">{{ formatDate(scope.row.createdAt) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="100" fixed="right">
        <template slot-scope="scope">
          <el-button type="text" style="color: #E17055" @click="removeEvaluation(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-wrapper">
      <el-pagination
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        :current-page="pageNum"
        :page-sizes="[10, 20, 50]"
        :page-size="pageSize"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total">
      </el-pagination>
    </div>
  </Layout>
</template>

<script>
import Layout from '@/components/Layout.vue'
import { addEvaluation, deleteEvaluation, getEvaluationList, getEvaluationsByUserId } from '@/api/evaluation'
import { getSessionList } from '@/api/session'
import dmApi from '@/api/dm'
import { getScriptList } from '@/api/script'
import { getUserList } from '@/api/user'

export default {
  name: 'Evaluation',
  components: { Layout },
  data() {
    return {
      loading: false,
      showForm: false,
      submitting: false,
      searchKeyword: '',
      searchRating: null,
      pageNum: 1,
      pageSize: 10,
      allList: [],
      currentUser: JSON.parse(localStorage.getItem('user') || '{}'),
      sessionOptions: [],
      dmOptions: [],
      userMap: {},
      sessionMap: {},
      dmMap: {},
      scriptMap: {},
      evaluationForm: { sessionId: null, dmId: null, rating: 5, comment: '' },
      rules: {
        sessionId: [{ required: true, message: '请选择场次', trigger: 'change' }],
        dmId: [{ required: true, message: '请选择DM', trigger: 'change' }],
        rating: [{ required: true, message: '请给出评分', trigger: 'change' }],
        comment: [{ required: true, message: '请输入评价内容', trigger: 'blur' }]
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
    filteredList() {
      let list = this.allList
      if (this.searchKeyword) {
        const kw = this.searchKeyword.toLowerCase()
        list = list.filter(e => (e.comment || '').toLowerCase().includes(kw))
      }
      if (this.searchRating) {
        list = list.filter(e => Number(e.rating) === Number(this.searchRating))
      }
      return list
    },
    total() {
      return this.filteredList.length
    },
    pagedList() {
      const start = (this.pageNum - 1) * this.pageSize
      return this.filteredList.slice(start, start + this.pageSize)
    }
  },
  mounted() {
    this.loadOptions()
    this.loadEvaluations()
  },
  methods: {
    formatDate(value) {
      if (!value) return '-'
      const date = new Date(value)
      return Number.isNaN(date.getTime()) ? value : date.toLocaleString()
    },
    async loadOptions() {
      try {
        const tasks = [getSessionList(), dmApi.getDmList(), getScriptList()]
        if (this.isAdmin) {
          tasks.push(getUserList())
        }
        const results = await Promise.allSettled(tasks)
        const sessionRes = results[0].status === 'fulfilled' ? results[0].value : null
        const dmRes = results[1].status === 'fulfilled' ? results[1].value : null
        const scriptRes = results[2].status === 'fulfilled' ? results[2].value : null
        const userRes = this.isAdmin && results[3] && results[3].status === 'fulfilled' ? results[3].value : null

        const sessions = sessionRes && sessionRes.code === 200 ? (sessionRes.data || []) : []
        const dms = dmRes && dmRes.code === 200 ? (dmRes.data || []) : []
        const scripts = scriptRes && scriptRes.code === 200 ? (scriptRes.data || []) : []
        const users = userRes && userRes.code === 200 ? (userRes.data || []) : []

        this.scriptMap = {}
        scripts.forEach(s => this.$set(this.scriptMap, s.id, s.scriptName || `剧本-${s.id}`))

        this.sessionMap = {}
        this.sessionOptions = sessions.map(s => {
          const label = this.buildSessionLabel(s)
          this.$set(this.sessionMap, s.id, label)
          return { value: s.id, label }
        })

        this.dmMap = {}
        this.dmOptions = dms.map(d => {
          const label = `DM-${d.id}（等级${d.dmLevel || 1}）`
          this.$set(this.dmMap, d.id, label)
          return { value: d.id, label }
        })

        this.userMap = {}
        users.forEach(u => {
          this.$set(this.userMap, u.id, u.realName || u.username || `用户-${u.id}`)
        })
      } catch (err) {
        this.sessionOptions = []
        this.dmOptions = []
        this.userMap = {}
        this.sessionMap = {}
        this.dmMap = {}
        this.scriptMap = {}
      }
    },
    formatDateShort(value) {
      if (!value) return '-'
      const d = new Date(value)
      if (Number.isNaN(d.getTime())) return value
      return `${d.getMonth() + 1}/${d.getDate()} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
    },
    buildSessionLabel(session) {
      if (!session) return '-'
      const scriptName = this.scriptMap[session.scriptId] || `剧本-${session.scriptId || '-'}`
      return `场次-${session.id}｜${scriptName}｜${this.formatDateShort(session.startTime)}`
    },
    getUserLabel(userId) {
      if (!userId) return '-'
      return this.userMap[userId] ? `${this.userMap[userId]}（ID:${userId}）` : `用户-${userId}`
    },
    getSessionLabel(sessionId) {
      if (!sessionId) return '-'
      return this.sessionMap[sessionId] || `场次-${sessionId}`
    },
    getDmLabel(dmId) {
      if (!dmId) return '-'
      return this.dmMap[dmId] || `DM-${dmId}`
    },
    async loadEvaluations() {
      this.loading = true
      try {
        const res = this.isAdmin ? await getEvaluationList() : await getEvaluationsByUserId(this.currentUserId)
        if (res && res.code === 200) {
          this.allList = res.data || []
        }
      } catch (err) {
        this.allList = []
      } finally {
        this.loading = false
      }
    },
    createEvaluation() {
      this.$refs.evalForm.validate(async valid => {
        if (!valid) return
        this.submitting = true
        try {
          const payload = {
            ...this.evaluationForm,
            userId: this.currentUserId
          }
          const res = await addEvaluation(payload)
          if (res && res.code === 200) {
            this.$message.success('评价提交成功')
            this.showForm = false
            this.evaluationForm = { sessionId: null, dmId: null, rating: 5, comment: '' }
            this.loadEvaluations()
          }
        } catch (err) {
          this.$message.error('提交评价失败')
        } finally {
          this.submitting = false
        }
      })
    },
    removeEvaluation(row) {
      this.$confirm('确定要删除这条评价吗？', '提示', { type: 'warning' })
        .then(async () => {
          const res = await deleteEvaluation(row.id)
          if (res.code === 200) {
            this.$message.success('删除成功')
            this.loadEvaluations()
          }
        })
        .catch(() => {})
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
.page-toolbar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; padding: 16px 20px; background: rgba(28,27,32,0.8); border-radius: 8px; border: 1px solid #36323D; }
.toolbar-left, .toolbar-right { display: flex; gap: 10px; align-items: center; }
.eval-form-card {
  background: rgba(28, 27, 32, 0.8);
  border: 1px solid #36323D;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 16px;
}
.eval-form-card h3 {
  margin: 0 0 16px 0;
  color: #D6B77A;
  font-family: 'Noto Serif SC', serif;
  font-size: 16px;
}
.eval-form-card h3 i { color: #A3824B; margin-right: 6px; }
.pagination-wrapper { display: flex; justify-content: flex-end; margin-top: 16px; padding: 12px 0; }

/* Light Theme Overrides */
body.light-theme .page-toolbar,
body.light-theme .eval-form-card {
  background: rgba(255, 255, 255, 0.8) !important;
  border: 1px solid #E0E0E5 !important;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.05);
}
body.light-theme .eval-form-card h3 {
  color: #6C5CE7 !important;
}
body.light-theme .eval-form-card h3 i {
  color: #A29BFE !important;
}
</style>
