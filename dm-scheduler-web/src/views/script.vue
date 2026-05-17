<template>
  <Layout>
    <div class="page-toolbar">
      <div class="toolbar-left">
        <el-input v-model="searchKeyword" placeholder="搜索剧本名称/标签" prefix-icon="el-icon-search" clearable size="small" style="width: 220px" @clear="loadPage" @keyup.enter.native="loadPage"></el-input>
        <el-select v-model="searchType" placeholder="类型" clearable size="small" style="width: 120px" @change="loadPage">
          <el-option label="推理" value="推理"></el-option>
          <el-option label="恐怖" value="恐怖"></el-option>
          <el-option label="情感" value="情感"></el-option>
          <el-option label="机制" value="机制"></el-option>
          <el-option label="欢乐" value="欢乐"></el-option>
        </el-select>
        <el-select v-model="searchDifficulty" placeholder="难度" clearable size="small" style="width: 100px" @change="loadPage">
          <el-option label="简单" value="简单"></el-option>
          <el-option label="中等" value="中等"></el-option>
          <el-option label="困难" value="困难"></el-option>
        </el-select>
        <el-button type="primary" size="small" icon="el-icon-search" @click="loadPage">搜索</el-button>
      </div>
      <div class="toolbar-right">
        <el-button type="primary" size="small" icon="el-icon-plus" @click="openAddDialog">添加剧本</el-button>
      </div>
    </div>

    <el-table :data="scriptList" style="width: 100%" v-loading="loading">
      <el-table-column prop="id" label="ID" width="60"></el-table-column>
      <el-table-column prop="scriptName" label="剧本名称" min-width="160" show-overflow-tooltip>
        <template slot-scope="scope"><span style="color:#E2C082;font-weight:600">{{ scope.row.scriptName }}</span></template>
      </el-table-column>
      <el-table-column prop="type" label="类型" min-width="90">
        <template slot-scope="scope">
          <el-tag size="small" effect="dark" :type="getTypeTag(scope.row.type)">{{ scope.row.type }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="difficulty" label="难度" min-width="90">
        <template slot-scope="scope">
          <el-tag size="small" :type="getDifficultyTag(scope.row.difficulty)">{{ getDifficultyLabel(scope.row.difficulty) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="玩家人数" min-width="100">
        <template slot-scope="scope">
          {{ scope.row.minPlayers || '-' }} ~ {{ scope.row.maxPlayers || '-' }}
        </template>
      </el-table-column>
      <el-table-column prop="duration" label="时长(分)" min-width="100"></el-table-column>
      <el-table-column prop="price" label="价格(元)" min-width="100">
        <template slot-scope="scope">
          <span style="color: #D6B77A; font-weight: 600">¥{{ scope.row.price || 0 }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="needDmLevel" label="DM等级" min-width="90"></el-table-column>
      <el-table-column prop="tags" label="标签" min-width="150" show-overflow-tooltip>
        <template slot-scope="scope">
          <el-tag v-for="tag in (scope.row.tags || '').split(',')" :key="tag" size="mini" style="margin: 0 2px;background:rgba(188,154,98,0.1);color:#D6B77A;border:none;" v-if="tag">{{ tag }}</el-tag>
        </template>
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

    <!-- 添加/编辑剧本对话框 -->
    <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="560px" :close-on-click-modal="false">
      <el-form ref="scriptForm" :model="scriptForm" :rules="rules" label-width="90px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="剧本名称" prop="scriptName">
              <el-input v-model="scriptForm.scriptName" placeholder="请输入"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="类型" prop="type">
              <el-select v-model="scriptForm.type" placeholder="请选择" style="width:100%">
                <el-option label="推理" value="推理"></el-option>
                <el-option label="恐怖" value="恐怖"></el-option>
                <el-option label="情感" value="情感"></el-option>
                <el-option label="机制" value="机制"></el-option>
                <el-option label="欢乐" value="欢乐"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="难度" prop="difficulty">
              <el-select v-model="scriptForm.difficulty" placeholder="请选择" style="width:100%">
                <el-option label="简单" value="EASY"></el-option>
                <el-option label="中等" value="MEDIUM"></el-option>
                <el-option label="困难" value="HARD"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="DM等级" prop="needDmLevel">
              <el-input-number v-model="scriptForm.needDmLevel" :min="1" :max="10" style="width:100%"></el-input-number>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :xs="24" :sm="12" :md="8">
            <el-form-item label="最少人数" label-width="74px" prop="minPlayers">
              <el-input-number v-model="scriptForm.minPlayers" :min="1" :max="20" style="width:100%"></el-input-number>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :md="8">
            <el-form-item label="最多人数" prop="maxPlayers" label-width="74px">
              <el-input-number v-model="scriptForm.maxPlayers" :min="1" :max="20" style="width:100%"></el-input-number>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :md="8">
            <el-form-item label="时长(分)" label-width="74px" prop="duration">
              <el-input-number v-model="scriptForm.duration" :min="30" :max="480" :step="30" style="width:100%"></el-input-number>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="价格(元)" prop="price">
          <el-input-number v-model="scriptForm.price" :min="0.01" :precision="2" :step="10" style="width: 200px"></el-input-number>
        </el-form-item>
        <el-form-item label="标签">
          <el-input v-model="scriptForm.tags" placeholder="多个标签用逗号分隔，如：推理,恐怖"></el-input>
        </el-form-item>
        <el-form-item label="描述">
          <el-input type="textarea" v-model="scriptForm.description" :rows="3" placeholder="请输入剧本描述"></el-input>
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
import { getScriptPage, addScript, updateScript, deleteScript } from '@/api/script'

export default {
  name: 'Script',
  components: { Layout },
  data() {
    const validatePositivePrice = (rule, value, callback) => {
      if (value === null || value === undefined || value === '') return callback(new Error('请输入价格'))
      if (Number(value) <= 0) return callback(new Error('价格必须大于0'))
      callback()
    }
    const validateMaxPlayers = (rule, value, callback) => {
      if (value === null || value === undefined || value === '') return callback(new Error('请输入最多人数'))
      if (Number(value) <= 0) return callback(new Error('最多人数必须大于0'))
      if (Number(value) < Number(this.scriptForm.minPlayers || 0)) return callback(new Error('最多人数不能小于最少人数'))
      callback()
    }
    return {
      loading: false,
      scriptList: [],
      total: 0,
      pageNum: 1,
      pageSize: 10,
      searchKeyword: '',
      searchType: '',
      searchDifficulty: '',
      dialogVisible: false,
      dialogTitle: '添加剧本',
      isEdit: false,
      submitting: false,
      scriptForm: {
        scriptName: '', type: '', difficulty: '', needDmLevel: 1,
        minPlayers: 4, maxPlayers: 6, duration: 120, price: null,
        tags: '', description: ''
      },
      rules: {
        scriptName: [{ required: true, message: '请输入剧本名称', trigger: 'blur' }],
        type: [{ required: true, message: '请选择类型', trigger: 'change' }],
        difficulty: [{ required: true, message: '请选择难度', trigger: 'change' }],
        needDmLevel: [{ required: true, message: '请输入DM等级', trigger: 'blur' }],
        minPlayers: [{ required: true, message: '请输入最少人数', trigger: 'blur' }],
        maxPlayers: [{ validator: validateMaxPlayers, trigger: 'blur' }],
        duration: [{ required: true, message: '请输入时长', trigger: 'blur' }],
        price: [{ validator: validatePositivePrice, trigger: 'blur' }]
      }
    }
  },
  mounted() { this.loadPage() },
  methods: {
    async loadPage() {
      this.loading = true
      try {
        const res = await getScriptPage({
          pageNum: this.pageNum, pageSize: this.pageSize,
          keyword: this.searchKeyword || undefined,
          type: this.searchType || undefined,
          difficulty: this.searchDifficulty || undefined
        })
        if (res && res.code === 200 && res.data) {
          this.scriptList = res.data.list || []
          this.total = res.data.total || 0
        }
      } catch (err) { console.error(err) } finally { this.loading = false }
    },
    handleSizeChange(val) { this.pageSize = val; this.pageNum = 1; this.loadPage() },
    handleCurrentChange(val) { this.pageNum = val; this.loadPage() },
    getTypeTag(type) {
      const map = { '推理': '', '恐怖': 'danger', '情感': 'success', '机制': 'warning', '欢乐': 'info' }
      return map[type] || 'info'
    },
    // 兼容中文旧数据和英文 ENUM 新数据
    getDifficultyTag(val) {
      const map = { 'HARD': 'danger', '困难': 'danger', 'MEDIUM': 'warning', '中等': 'warning', 'EASY': 'success', '简单': 'success' }
      return map[val] || 'info'
    },
    getDifficultyLabel(val) {
      const map = { 'EASY': '简单', 'MEDIUM': '中等', 'HARD': '困难', '简单': '简单', '中等': '中等', '困难': '困难' }
      return map[val] || val || '未设置'
    },
    openAddDialog() {
      this.isEdit = false; this.dialogTitle = '添加剧本'
      this.scriptForm = { scriptName: '', type: '', difficulty: '', needDmLevel: 1, minPlayers: 4, maxPlayers: 6, duration: 120, price: null, tags: '', description: '' }
      this.dialogVisible = true
      this.$nextTick(() => { if (this.$refs.scriptForm) this.$refs.scriptForm.clearValidate() })
    },
    openEditDialog(row) {
      this.isEdit = true; this.dialogTitle = '编辑剧本'
      this.scriptForm = { ...row }
      this.dialogVisible = true
    },
    submitForm() {
      this.$refs.scriptForm.validate(async valid => {
        if (!valid) return
        this.submitting = true
        try {
          const res = this.isEdit ? await updateScript(this.scriptForm) : await addScript(this.scriptForm)
          if (res && res.code === 200) {
            this.$message.success(this.isEdit ? '更新成功' : '添加成功')
            this.dialogVisible = false
            this.loadPage()
          }
        } catch (err) { this.$message.error(this.isEdit ? '更新失败' : '添加失败') }
        finally { this.submitting = false }
      })
    },
    handleDelete(row) {
      this.$confirm('确定要删除该剧本吗？', '提示', { type: 'warning' }).then(async () => {
        try {
          const res = await deleteScript(row.id)
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
