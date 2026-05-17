<template>
  <el-button
    v-permission="permission"
    type="primary"
    size="small"
    :loading="loading"
    :disabled="computedDisabled"
    v-bind="$attrs"
    v-on="$listeners"
  >
    <slot>编辑</slot>
  </el-button>
</template>

<script>
import { hasPermission } from '@/utils/permission'

export default {
  name: 'EditButton',
  inheritAttrs: false,
  props: {
    permission: {
      type: String,
      default: ''
    },
    loading: {
      type: Boolean,
      default: false
    },
    disabled: {
      type: Boolean,
      default: false
    }
  },
  computed: {
    computedDisabled() {
      return this.disabled || !hasPermission(this.permission)
    }
  }
}
</script>

