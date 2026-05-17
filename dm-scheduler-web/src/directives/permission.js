import { hasPermission } from '@/utils/permission'

export default {
  inserted(el, binding) {
    const permissionKey = binding.value
    if (hasPermission(permissionKey)) return
    el.setAttribute('disabled', 'disabled')
    el.setAttribute('aria-disabled', 'true')
    el.style.pointerEvents = 'none'
    el.style.opacity = '0.6'
  }
}

