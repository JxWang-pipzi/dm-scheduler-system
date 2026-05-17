import Layout from '../components/Layout.vue'
import { shallowMount } from '@vue/test-utils'

jest.mock('../utils/websocket', () => ({
  connectWebSocket: jest.fn(),
  disconnectWebSocket: jest.fn(),
  onMessage: jest.fn(() => () => {})
}))

// Mock Vue Router
const mockRouter = {
  push: jest.fn(),
  path: '/dashboard'
}

// Mock localStorage
const storage = {
  token: 'mock-token',
  user: JSON.stringify({ id: 1, username: 'admin', realName: '管理员', role: 'ADMIN' }),
  menuState: null,
  sidebarCollapsed: 'false',
  theme: 'dark'
}

Object.defineProperty(window, 'localStorage', {
  value: {
    getItem: jest.fn((key) => (key in storage ? storage[key] : null)),
    setItem: jest.fn(),
    removeItem: jest.fn()
  },
  writable: true
})

describe('Layout Component', () => {
  let wrapper

  beforeEach(() => {
    jest.clearAllMocks()
    wrapper = shallowMount(Layout, {
      stubs: {
        'el-container': true,
        'el-header': true,
        'el-main': true,
        'el-aside': true,
        'el-menu': true,
        'el-submenu': true,
        'el-menu-item': true,
        'el-badge': true,
        'el-dropdown': true,
        'el-dropdown-menu': true,
        'el-dropdown-item': true,
        'el-avatar': true,
        'el-breadcrumb': true,
        'el-breadcrumb-item': true,
        'el-dialog': true,
        'el-form': true,
        'el-form-item': true,
        'el-input': true,
        'el-button': true
      },
      mocks: {
        $router: mockRouter,
        $route: { path: '/dashboard' },
        $axios: { get: jest.fn(() => Promise.resolve({ code: 200, data: 0 })) },
        $notify: jest.fn(),
        $message: { success: jest.fn(), error: jest.fn(), warning: jest.fn() }
      },
      slots: {
        default: '<div>Test content</div>',
        header: '<h2>Test Header</h2>'
      }
    })
  })

  afterEach(() => {
    wrapper.destroy()
  })

  test('should initialize with correct default values', () => {
    expect(wrapper.vm.activeMenu).toBe('/dashboard')
    expect(wrapper.vm.openedMenus).toEqual(['group-ops'])
    expect(wrapper.vm.user.username).toBe('admin')
  })

  test('should handle menu select event', () => {
    // Test with different route
    wrapper.vm.$route.path = '/dashboard'
    wrapper.vm.handleMenuSelect('/user')
    expect(mockRouter.push).toHaveBeenCalledWith('/user')

    // Test with same route (should not call push)
    mockRouter.push.mockClear()
    wrapper.vm.handleMenuSelect('/dashboard')
    expect(mockRouter.push).not.toHaveBeenCalled()
  })

  test('should handle menu open event', () => {
    wrapper.vm.handleMenuOpen('/store')
    expect(wrapper.vm.openedMenus).toContain('/store')

    // Test duplicate open (should not add duplicate)
    wrapper.vm.handleMenuOpen('/store')
    expect(wrapper.vm.openedMenus.filter(item => item === '/store').length).toBe(1)
  })

  test('should handle menu close event', () => {
    // Add menu to openedMenus
    wrapper.vm.openedMenus.push('/store')
    expect(wrapper.vm.openedMenus).toContain('/store')

    // Close menu
    wrapper.vm.handleMenuClose('/store')
    expect(wrapper.vm.openedMenus).not.toContain('/store')
  })

  test('should handle logout', () => {
    wrapper.vm.logout()
    expect(localStorage.removeItem).toHaveBeenCalledWith('token')
    expect(localStorage.removeItem).toHaveBeenCalledWith('user')
    expect(mockRouter.push).toHaveBeenCalledWith('/login')
  })

  test('should handle profile command', () => {
    wrapper.vm.handleUserCommand('profile')
    expect(mockRouter.push).toHaveBeenCalledWith('/profile')
  })
})
