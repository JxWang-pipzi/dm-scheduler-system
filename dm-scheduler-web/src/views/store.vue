<template>
  <Layout>
    <template slot="header">
      <h2>门店管理</h2>
    </template>
    <div class="store-container">
      <!-- 搜索工具栏 和 添加门店按钮 -->
      <div class="page-toolbar">
        <div class="toolbar-left">
          <el-input v-model="searchKeyword" placeholder="搜索门店名称/地址" clearable size="small" style="width: 250px" prefix-icon="el-icon-search" @clear="handleSearch" @keyup.enter.native="handleSearch"></el-input>
          <el-button type="primary" size="small" icon="el-icon-search" @click="handleSearch">搜索</el-button>
        </div>
        <div class="toolbar-right">
          <el-button type="primary" size="small" icon="el-icon-plus" @click="showAddDialog = true">添加门店</el-button>
        </div>
      </div>
      
      <el-card class="list-card">
        <el-table :data="pagedList" style="width: 100%">
          <el-table-column prop="id" label="ID" width="80"></el-table-column>
          <el-table-column prop="storeName" label="门店名称"></el-table-column>
          <el-table-column prop="address" label="地址" show-overflow-tooltip></el-table-column>
          <el-table-column prop="phone" label="电话"></el-table-column>
          <el-table-column prop="description" label="描述" show-overflow-tooltip></el-table-column>
          <el-table-column prop="createdAt" label="创建时间">
            <template slot-scope="scope">{{ scope.row.createdAt ? new Date(scope.row.createdAt).toLocaleString() : '-' }}</template>
          </el-table-column>
          <el-table-column label="操作" width="150" fixed="right">
            <template slot-scope="scope">
              <el-button size="small" type="primary" plain @click="editStore(scope.row)">编辑</el-button>
              <el-button size="small" type="danger" plain @click="deleteStore(scope.row.id)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>

        <!-- 分页 -->
        <div class="pagination-wrapper">
          <el-pagination @size-change="handleSizeChange" @current-change="handleCurrentChange"
            :current-page="pageNum" :page-sizes="[10, 20, 50]" :page-size="pageSize"
            layout="total, sizes, prev, pager, next, jumper" :total="total"></el-pagination>
        </div>
      </el-card>

      <!-- 添加/编辑门店对话框 -->
      <el-dialog :title="storeForm.id ? '编辑门店' : '添加门店'" :visible.sync="showAddDialog" width="500px">
        <el-form :model="storeForm" label-width="100px" ref="form">
          <el-form-item label="门店名称" required>
            <el-input v-model="storeForm.storeName" placeholder="请输入门店名称"></el-input>
          </el-form-item>
          <el-form-item label="地址" required>
            <el-input v-model="storeForm.address" placeholder="请输入门店地址"></el-input>
          </el-form-item>
          <el-form-item label="电话">
            <el-input v-model="storeForm.phone" placeholder="请输入门店电话"></el-input>
          </el-form-item>
          <el-form-item label="描述">
            <el-input type="textarea" v-model="storeForm.description" placeholder="请输入门店描述"></el-input>
          </el-form-item>
        </el-form>
        <div slot="footer" class="dialog-footer">
          <el-button @click="resetForm">取消</el-button>
          <el-button type="primary" @click="submitForm">确定</el-button>
        </div>
      </el-dialog>
    </div>
  </Layout>
</template>

<script>
import Layout from '../components/Layout.vue'

export default {
  components: {
    Layout
  },
  data() {
    return {
      showAddDialog: false,
      searchKeyword: '',
      pageNum: 1,
      pageSize: 10,
      allList: [],
      storeForm: {
        id: '',
        storeName: '',
        address: '',
        phone: '',
        description: ''
      }
    }
  },
  computed: {
    filteredList() {
      let list = this.allList
      if (this.searchKeyword) {
        const kw = this.searchKeyword.toLowerCase()
        list = list.filter(item =>
          (item.storeName && item.storeName.toLowerCase().includes(kw)) ||
          (item.address && item.address.toLowerCase().includes(kw))
        )
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
    this.loadStores()
  },
  methods: {
    loadStores() {
      this.$axios.get('/store')
        .then(response => {
          if (response.code === 200) {
            this.allList = response.data || []
          }
        })
        .catch(error => {
          console.error('获取门店列表失败:', error)
          this.allList = []
        })
    },
    submitForm() {
      if (!this.storeForm.storeName || !this.storeForm.address) {
        this.$message.warning('请填写门店名称和地址')
        return
      }
      if (this.storeForm.id) {
        this.$axios.put('/store', this.storeForm)
          .then(response => {
            if (response.code === 200) {
              this.$message.success('更新成功')
              this.loadStores()
              this.resetForm()
            } else {
              this.$message.error(response.message)
            }
          })
          .catch(error => { console.error('更新失败:', error); this.$message.error('更新失败') })
      } else {
        this.$axios.post('/store', this.storeForm)
          .then(response => {
            if (response.code === 200) {
              this.$message.success('创建成功')
              this.loadStores()
              this.resetForm()
            } else {
              this.$message.error(response.message)
            }
          })
          .catch(error => { console.error('创建失败:', error); this.$message.error('创建失败') })
      }
    },
    resetForm() {
      this.storeForm = {
        id: '',
        storeName: '',
        address: '',
        phone: '',
        description: ''
      }
      this.showAddDialog = false
    },
    editStore(row) {
      this.storeForm = JSON.parse(JSON.stringify(row))
      this.showAddDialog = true
    },
    deleteStore(id) {
      this.$confirm('确定要删除该门店吗?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.$axios.delete(`/store/${id}`)
          .then(response => {
            if (response.code === 200) {
              this.$message.success('删除成功')
              this.loadStores()
            } else {
              this.$message.error(response.message)
            }
          })
          .catch(error => { console.error('删除失败:', error); this.$message.error('删除失败') })
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
.store-container {
  padding: 20px;
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
</style>
