<template>
  <Layout>
    <template slot="header">
      <h2>系统配置管理</h2>
    </template>
    <div class="system-config-container">
      <el-card>
        <el-row>
          <el-col :span="12">
            <el-form :model="configForm" label-width="100px">
              <el-form-item label="配置键">
                <el-select
                  v-model="configForm.configKey"
                  filterable
                  allow-create
                  default-first-option
                  :disabled="!!configForm.id"
                  placeholder="请输入或选择配置键"
                  style="width: 100%"
                >
                  <el-option
                    v-for="item in presetConfigKeys"
                    :key="item.key"
                    :label="item.key + '（' + item.label + '）'"
                    :value="item.key"
                  />
                </el-select>
              </el-form-item>
              <el-form-item label="配置值">
                <el-input v-model="configForm.configValue" placeholder="请输入配置值"></el-input>
              </el-form-item>
              <el-form-item label="描述">
                <el-input v-model="configForm.description" placeholder="请输入配置描述"></el-input>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="submitForm">提交</el-button>
                <el-button @click="resetForm">重置</el-button>
              </el-form-item>
            </el-form>
          </el-col>
          <el-col :span="12">
            <el-alert
              title="建议维护以下关键配置：system_name、open_time、close_time、default_session_max_players、reservation_lead_hours"
              type="info"
              :closable="false"
              show-icon
            />
          </el-col>
        </el-row>
      </el-card>

      <el-card style="margin-top: 20px;">
        <div slot="header">
          <span>当前生效配置（含默认值）</span>
        </div>
        <el-table :data="effectiveConfigRows" style="width: 100%">
          <el-table-column prop="configKey" label="配置键" width="260"></el-table-column>
          <el-table-column prop="configValue" label="当前生效值"></el-table-column>
        </el-table>
      </el-card>
      
      <el-card style="margin-top: 20px;">
        <el-table :data="configList" style="width: 100%">
          <el-table-column prop="id" label="ID" width="80"></el-table-column>
          <el-table-column prop="configKey" label="配置键"></el-table-column>
          <el-table-column prop="configValue" label="配置值"></el-table-column>
          <el-table-column prop="description" label="描述"></el-table-column>
          <el-table-column prop="createdAt" label="创建时间"></el-table-column>
          <el-table-column prop="updatedAt" label="更新时间"></el-table-column>
          <el-table-column label="操作" width="150">
            <template slot-scope="scope">
              <el-button size="small" @click="editConfig(scope.row)">编辑</el-button>
              <el-button size="small" type="danger" @click="deleteConfig(scope.row.id)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
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
      configForm: {
        id: '',
        configKey: '',
        configValue: '',
        description: ''
      },
      configList: [],
      effectiveConfig: {},
      presetConfigKeys: [
        { key: 'system_name', label: '系统名称' },
        { key: 'open_time', label: '营业开始时间（HH:mm）' },
        { key: 'close_time', label: '营业结束时间（HH:mm，可填24:00）' },
        { key: 'default_session_max_players', label: '场次默认最大人数' },
        { key: 'reservation_lead_hours', label: '预约需提前小时数' }
      ]
    }
  },
  computed: {
    effectiveConfigRows() {
      return Object.keys(this.effectiveConfig || {}).map(key => ({
        configKey: key,
        configValue: this.effectiveConfig[key]
      }))
    }
  },
  mounted() {
    this.loadConfigs()
    this.loadEffectiveConfigs()
  },
  methods: {
    loadConfigs() {
      this.$axios.get('/system/config')
        .then(response => {
          if (response.code === 200) {
            this.configList = response.data
          }
        })
        .catch(error => {
          console.error('获取配置列表失败:', error)
        })
    },
    loadEffectiveConfigs() {
      this.$axios.get('/system/config/effective')
        .then(response => {
          if (response.code === 200) {
            this.effectiveConfig = response.data || {}
          }
        })
        .catch(error => {
          console.error('获取生效配置失败:', error)
          this.effectiveConfig = {}
        })
    },
    submitForm() {
      if (!this.configForm.configKey || !this.configForm.configValue) {
        this.$message.warning('配置键和配置值不能为空')
        return
      }
      if (this.configForm.id) {
        this.$axios.put('/system/config', this.configForm)
          .then(response => {
            if (response.code === 200) {
              this.$message.success('更新成功')
              this.loadConfigs()
              this.loadEffectiveConfigs()
              this.resetForm()
            } else {
              this.$message.error(response.message)
            }
          })
          .catch(error => {
            console.error('更新失败:', error)
            this.$message.error('更新失败')
          })
      } else {
        this.$axios.post('/system/config', this.configForm)
          .then(response => {
            if (response.code === 200) {
              this.$message.success('创建成功')
              this.loadConfigs()
              this.loadEffectiveConfigs()
              this.resetForm()
            } else {
              this.$message.error(response.message)
            }
          })
          .catch(error => {
            console.error('创建失败:', error)
            this.$message.error('创建失败')
          })
      }
    },
    resetForm() {
      this.configForm = {
        id: '',
        configKey: '',
        configValue: '',
        description: ''
      }
    },
    editConfig(row) {
      this.configForm = JSON.parse(JSON.stringify(row))
    },
    deleteConfig(id) {
      this.$confirm('确定要删除该配置吗?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.$axios.delete(`/system/config/${id}`)
          .then(response => {
            if (response.code === 200) {
              this.$message.success('删除成功')
              this.loadConfigs()
              this.loadEffectiveConfigs()
            } else {
              this.$message.error(response.message)
            }
          })
          .catch(error => {
            console.error('删除失败:', error)
            this.$message.error('删除失败')
          })
      })
    }
  }
}
</script>

<style scoped>
.system-config-container {
  padding: 20px;
}
</style>
