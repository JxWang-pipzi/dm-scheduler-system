export function getCurrentUser() {
  try {
    return JSON.parse(localStorage.getItem('user') || 'null')
  } catch (e) {
    return null
  }
}

export function hasPermission(permissionKey) {
  if (!permissionKey) return true
  const user = getCurrentUser()
  if (!user) return false
  const role = (user.role || '').toLowerCase()
  if (role === 'admin' || role === 'administrator') return true
  const permissions = Array.isArray(user.permissions) ? user.permissions : []
  return permissions.includes(permissionKey)
}

