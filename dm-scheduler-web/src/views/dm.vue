<template>
  <Layout>
    <div class="page-toolbar">
      <div class="toolbar-left">
        <el-input v-model="searchKeyword" placeholder="搜索DM姓名/擅长类型" prefix-icon="el-icon-search" clearable size="small" style="width: 220px" @clear="loadPage" @keyup.enter.native="loadPage"></el-input>
        <el-select v-model="searchStatus" placeholder="状态" clearable size="small" style="width: 120px" @change="loadPage">
          <el-option label="空闲" value="AVAILABLE"></el-option>
          <el-option label="忙碌" value="BUSY"></el-option>
          <el-option label="休息" value="REST"></el-option>
        </el-select>
        <el-button type="primary" size="small" icon="el-icon-search" @click="loadPage">搜索</el-button>
      </div>
      <div class="toolbar-right">
        <el-button type="primary" size="small" icon="el-icon-plus" @click="openAddDialog">添加DM</el-button>
      </div>
    </div>

    <el-table :data="dmList" style="width: 100%" v-loading="loading">
      <el-table-column prop="id" label="ID" width="60"></el-table-column>
      <el-table-column label="关联用户" min-width="120">
        <template slot-scope="scope">
          <span style="font-weight:600;color:#E2C082"><i class="el-icon-user"></i> {{ getUserLabel(scope.row.userId) }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="dmLevel" label="等级" min-width="80">
        <template slot-scope="scope">
          <el-tag size="small" effect="dark" style="background:#A3824B;border:none;">Lv.{{ scope.row.dmLevel }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="experience" label="经验值" min-width="90"></el-table-column>
      <el-table-column prop="rating" label="评分" min-width="140">
        <template slot-scope="scope">
          <el-rate v-model="scope.row.rating" disabled :max="5" :colors="['#8C7355', '#D6B77A', '#E2C082']" style="display: inline-flex"></el-rate>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" min-width="90">
        <template slot-scope="scope">
          <el-tag :type="scope.row.status === 'AVAILABLE' ? 'success' : scope.row.status === 'BUSY' ? 'warning' : 'info'" size="small" effect="dark">
            {{ scope.row.status === 'AVAILABLE' ? '空闲' : scope.row.status === 'BUSY' ? '带本中' : '休息' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="specialty" label="擅长类型" min-width="160">
        <template slot-scope="scope">
          <el-tag v-for="s in (scope.row.specialty || '').split(',')" :key="s" size="mini" style="margin: 0 2px" v-if="s">{{ s }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="totalSessions" label="累计场次" min-width="100"></el-table-column>
      <el-table-column prop="weeklyMaxSessions" label="周最大" min-width="90"></el-table-column>
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

    <!-- 添加/编辑DM对话框 -->
    <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="500px" :close-on-click-modal="false">
      <el-form ref="dmForm" :model="dmForm" :rules="rules" label-width="100px">
        <el-form-item label="关联用户" prop="userId">
          <el-select v-model="dmForm.userId" filterable placeholder="请选择DM用户" style="width:100%">
            <el-option
              v-for="item in userOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="DM等级">
              <el-input-number v-model="dmForm.dmLevel" :min="1" :max="10" style="width:100%"></el-input-number>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="经验值">
              <el-input-number v-model="dmForm.experience" :min="0" style="width:100%"></el-input-number>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="评分">
              <el-input-number v-model="dmForm.rating" :min="0" :max="5" :step="0.1" :precision="1" style="width:100%"></el-input-number>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="周最大场次">
              <el-input-number v-model="dmForm.weeklyMaxSessions" :min="1" :max="30" style="width:100%"></el-input-number>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="状态">
          <el-select v-model="dmForm.status" style="width:100%">
            <el-option label="空闲" value="AVAILABLE"></el-option>
            <el-option label="忙碌" value="BUSY"></el-option>
            <el-option label="休息" value="REST"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="擅长类型">
          <el-input v-model="dmForm.specialty" placeholder="多个用逗号分隔，如：推理,恐怖"></el-input>
        </el-form-item>
        <el-form-item label="自我介绍">
          <el-input type="textarea" v-model="dmForm.bio" :rows="2" placeholder="DM的自我介绍"></el-input>
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
import dmApi from '@/api/dm'
import { getUserList } from '@/api/user'

export default {
  name: 'Dm',
  components: { Layout },
  data() {
    return {
      loading: false,
      dmList: [],
      total: 0,
      pageNum: 1,
      pageSize: 10,
      searchKeyword: '',
      searchStatus: '',
      dialogVisible: false,
      dialogTitle: '添加DM',
      isEdit: false,
      submitting: false,
      dmForm: {
        userId: null, dmLevel: 1, experience: 0, rating: 5.0,
        status: 'AVAILABLE', specialty: '', weeklyMaxSessions: 10, bio: ''
      },
      rules: {
        userId: [{ required: true, message: '请选择关联用户', trigger: 'change' }]
      },
      userOptions: [],
      userMap: {}
    }
  },
  mounted() { this.loadUserOptions().then(() => this.loadPage()) },
  methods: {
    async loadUserOptions() {
      try {
        const res = await getUserList()
        if (res && res.code === 200) {
          const users = (res.data || []).filter(u => String(u.role || '').toUpperCase() === 'DM')
          this.userOptions = users.map(u => ({
            value: u.id,
            label: `${u.realName || u.username || `用户-${u.id}`}（${u.username || '-'}）`
          }))
          this.userMap = {}
          users.forEach(u => {
            this.$set(this.userMap, u.id, `${u.realName || u.username || `用户-${u.id}`}`)
          })
        }
      } catch (err) {
        console.error('加载用户字典失败', err)
        this.userOptions = []
        this.userMap = {}
      }
    },
    getUserLabel(userId) {
      if (!userId) return '-'
      return this.userMap[userId] ? `${this.userMap[userId]}（ID:${userId}）` : `用户-${userId}`
    },
    async loadPage() {
      this.loading = true
      try {
        const res = await dmApi.getDmPage({
          pageNum: this.pageNum, pageSize: this.pageSize,
          keyword: this.searchKeyword || undefined,
          status: this.searchStatus || undefined
        })
        if (res && res.code === 200 && res.data) {
          this.dmList = res.data.list || []
          this.total = res.data.total || 0
        }
      } catch (err) { console.error(err) } finally { this.loading = false }
    },
    handleSizeChange(val) { this.pageSize = val; this.pageNum = 1; this.loadPage() },
    handleCurrentChange(val) { this.pageNum = val; this.loadPage() },
    async openAddDialog() {
      await this.loadUserOptions()
      this.isEdit = false; this.dialogTitle = '添加DM'
      this.dmForm = { userId: null, dmLevel: 1, experience: 0, rating: 5.0, status: 'AVAILABLE', specialty: '', weeklyMaxSessions: 10, bio: '' }
      this.dialogVisible = true
      this.$nextTick(() => { if (this.$refs.dmForm) this.$refs.dmForm.clearValidate() })
    },
    async openEditDialog(row) {
      await this.loadUserOptions()
      this.isEdit = true; this.dialogTitle = '编辑DM'
      this.dmForm = { ...row }
      this.dialogVisible = true
    },
    submitForm() {
      this.$refs.dmForm.validate(async valid => {
        if (!valid) return
        this.submitting = true
        try {
          const res = this.isEdit ? await dmApi.updateDm(this.dmForm) : await dmApi.addDm(this.dmForm)
          if (res && res.code === 200) {
            this.$message.success(this.isEdit ? '更新成功' : '添加成功')
            this.dialogVisible = false; this.loadPage()
          }
        } catch (err) { this.$message.error(this.isEdit ? '更新失败' : '添加失败') }
        finally { this.submitting = false }
      })
    },
    handleDelete(row) {
      this.$confirm('确定要删除该DM吗？', '提示', { type: 'warning' }).then(async () => {
        try {
          const res = await dmApi.deleteDm(row.id)
          if (res && res.code === 200) { this.$message.success('删除成功'); this.loadPage() }
        } catch (err) { this.$message.error('删除失败') }
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
