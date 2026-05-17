<template>
  <Layout>
    <div class="page-toolbar">
      <div class="toolbar-left">
        <el-input v-model="searchKeyword" placeholder="搜索用户名/手机号" prefix-icon="el-icon-search" clearable size="small" style="width: 220px" @clear="loadPage" @keyup.enter.native="loadPage"></el-input>
        <el-select v-model="searchRole" placeholder="角色" clearable size="small" style="width: 120px" @change="loadPage">
          <el-option label="管理员" value="ADMIN"></el-option>
          <el-option label="用户" value="USER"></el-option>
          <el-option label="DM" value="DM"></el-option>
        </el-select>
        <el-button type="primary" size="small" icon="el-icon-search" @click="loadPage">搜索</el-button>
      </div>
      <div class="toolbar-right">
        <el-button type="primary" size="small" icon="el-icon-plus" @click="openAddDialog">添加用户</el-button>
      </div>
    </div>

    <el-table :data="userList" style="width: 100%" v-loading="loading" highlight-current-row>
      <el-table-column prop="id" label="ID" width="60"></el-table-column>
      <el-table-column prop="username" label="用户名" min-width="140"></el-table-column>
      <el-table-column prop="realName" label="姓名" min-width="120"></el-table-column>
      <el-table-column prop="phone" label="手机号" min-width="150"></el-table-column>
      <el-table-column prop="role" label="角色" min-width="100">
        <template slot-scope="scope">
          <el-tag :type="scope.row.role === 'ADMIN' ? 'danger' : scope.row.role === 'DM' ? 'warning' : 'info'" size="small" effect="dark">
            {{ scope.row.role === 'ADMIN' || scope.row.role === 'admin' ? '管理员' : scope.row.role === 'DM' || scope.row.role === 'dm' ? 'DM' : '用户' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="创建时间" min-width="180">
        <template slot-scope="scope">
          {{ scope.row.createdAt ? new Date(scope.row.createdAt).toLocaleString() : '-' }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="160" fixed="right">
        <template slot-scope="scope">
          <el-button type="text" size="small" icon="el-icon-edit" @click="openEditDialog(scope.row)">编辑</el-button>
          <el-button type="text" size="small" icon="el-icon-delete" style="color: #E17055" @click="handleDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div class="pagination-wrapper">
      <el-pagination
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        :current-page="pageNum"
        :page-sizes="[10, 20, 50]"
        :page-size="pageSize"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
      ></el-pagination>
    </div>

    <!-- 添加用户对话框 -->
    <el-dialog title="添加用户" :visible.sync="addDialogVisible" width="480px" :close-on-click-modal="false">
      <el-form ref="addForm" :model="addForm" :rules="rules" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="addForm.username" placeholder="请输入用户名"></el-input>
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input type="password" v-model="addForm.password" placeholder="请输入密码" show-password></el-input>
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="addForm.role" placeholder="请选择角色" style="width: 100%">
            <el-option label="管理员" value="ADMIN"></el-option>
            <el-option label="用户" value="USER"></el-option>
            <el-option label="DM" value="DM"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="addForm.phone" placeholder="请输入手机号"></el-input>
        </el-form-item>
        <el-form-item label="姓名">
          <el-input v-model="addForm.realName" placeholder="请输入姓名"></el-input>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="addDialogVisible = false" size="small">取消</el-button>
        <el-button type="primary" :loading="addSubmitting" @click="submitAdd" size="small">确定</el-button>
      </span>
    </el-dialog>

    <!-- 编辑用户对话框 -->
    <el-dialog title="编辑用户" :visible.sync="editDialogVisible" width="480px" :close-on-click-modal="false">
      <el-form ref="editForm" :model="editForm" label-width="80px">
        <el-form-item label="用户名">
          <el-input v-model="editForm.username" placeholder="请输入用户名"></el-input>
        </el-form-item>
        <el-form-item label="密码">
          <el-input type="password" v-model="editForm.password" placeholder="不修改请留空" show-password></el-input>
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="editForm.role" placeholder="请选择角色" style="width: 100%">
            <el-option label="管理员" value="ADMIN"></el-option>
            <el-option label="用户" value="USER"></el-option>
            <el-option label="DM" value="DM"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="editForm.phone" placeholder="请输入手机号"></el-input>
        </el-form-item>
        <el-form-item label="姓名">
          <el-input v-model="editForm.realName" placeholder="请输入姓名"></el-input>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="editDialogVisible = false" size="small">取消</el-button>
        <el-button type="primary" :loading="editSubmitting" @click="submitEdit" size="small">确定</el-button>
      </span>
    </el-dialog>
  </Layout>
</template>

<script>
import Layout from '@/components/Layout.vue'
import { getUserPage, addUser, updateUser, deleteUser } from '@/api/user'

export default {
  name: 'User',
  components: { Layout },
  data() {
    return {
      loading: false,
      userList: [],
      total: 0,
      pageNum: 1,
      pageSize: 10,
      searchKeyword: '',
      searchRole: '',
      addDialogVisible: false,
      editDialogVisible: false,
      addSubmitting: false,
      editSubmitting: false,
      addForm: {
        username: '',
        password: '',
        role: '',
        phone: '',
        realName: ''
      },
      editForm: {
        id: null,
        username: '',
        password: '',
        role: '',
        phone: '',
        realName: ''
      },
      rules: {
        username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
        password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
        role: [{ required: true, message: '请选择角色', trigger: 'change' }],
        phone: [{
          validator: (rule, value, callback) => {
            if (!value) return callback()
            const phone = String(value).trim()
            if (!/^\d{11}$/.test(phone)) {
              return callback(new Error('手机号必须为11位数字'))
            }
            callback()
          },
          trigger: 'blur'
        }]
      }
    }
  },
  mounted() {
    this.loadPage()
  },
  methods: {
    async loadPage() {
      this.loading = true
      try {
        const res = await getUserPage({
          pageNum: this.pageNum,
          pageSize: this.pageSize,
          keyword: this.searchKeyword || undefined,
          role: this.searchRole || undefined
        })
        if (res && res.code === 200 && res.data) {
          this.userList = res.data.list || []
          this.total = res.data.total || 0
        }
      } catch (err) {
        console.error('加载用户列表失败:', err)
      } finally {
        this.loading = false
      }
    },
    handleSizeChange(val) {
      this.pageSize = val
      this.pageNum = 1
      this.loadPage()
    },
    handleCurrentChange(val) {
      this.pageNum = val
      this.loadPage()
    },
    openAddDialog() {
      this.addForm = { username: '', password: '', role: '', phone: '', realName: '' }
      this.addDialogVisible = true
      this.$nextTick(() => { if (this.$refs.addForm) this.$refs.addForm.clearValidate() })
    },
    openEditDialog(row) {
      this.editForm = { ...row, password: '' }
      this.editDialogVisible = true
    },
    async submitAdd() {
      this.$refs.addForm.validate(async valid => {
        if (!valid) return
        this.addSubmitting = true
        try {
          const res = await addUser(this.addForm)
          if (res && res.code === 200) {
            this.$message.success('添加成功')
            this.addDialogVisible = false
            this.loadPage()
          }
        } catch (err) {
          this.$message.error('添加失败')
        } finally {
          this.addSubmitting = false
        }
      })
    },
    async submitEdit() {
      if (this.editForm.phone && !/^\d{11}$/.test(String(this.editForm.phone).trim())) {
        this.$message.error('手机号必须为11位数字')
        return
      }
      this.editSubmitting = true
      try {
        const data = { ...this.editForm }
        if (!data.password) delete data.password
        const res = await updateUser(data)
        if (res && res.code === 200) {
          this.$message.success('更新成功')
          this.editDialogVisible = false
          this.loadPage()
        }
      } catch (err) {
        this.$message.error('更新失败')
      } finally {
        this.editSubmitting = false
      }
    },
    handleDelete(row) {
      this.$confirm('确定要删除该用户吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          const res = await deleteUser(row.id)
          if (res && res.code === 200) {
            this.$message.success('删除成功')
            this.loadPage()
          }
        } catch (err) {
          this.$message.error('删除失败')
        }
      }).catch(() => {})
    }
  }
}
</script>

<style scoped>
.page-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding: 16px 20px;
  background: rgba(28, 27, 32, 0.8);
  border-radius: 8px;
  border: 1px solid #36323D;
}
.toolbar-left {
  display: flex;
  gap: 10px;
  align-items: center;
}
.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
  padding: 12px 0;
}
</style>
