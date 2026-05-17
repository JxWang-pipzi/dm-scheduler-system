import { mount } from '@vue/test-utils'
import AddButton from '../components/AddButton.vue'

function setUser(user) {
  window.localStorage.getItem = jest.fn(() => JSON.stringify(user))
}

describe('Action Buttons', () => {
  beforeEach(() => {
    Object.defineProperty(window, 'localStorage', {
      value: {
        getItem: jest.fn(() => null),
        setItem: jest.fn(),
        removeItem: jest.fn()
      },
      writable: true
    })
  })

  test('AddButton disabled without permission', () => {
    setUser({ id: 2, username: 'u', role: 'user', permissions: [] })
    const wrapper = mount(AddButton, {
      propsData: { permission: 'user:add' },
      directives: {
        permission: () => {}
      },
      stubs: {
        'el-button': {
          template: '<button :disabled="disabled"><slot /></button>',
          props: ['disabled', 'loading']
        }
      }
    })
    expect(wrapper.find('button').attributes('disabled')).toBe('disabled')
  })

  test('AddButton enabled for admin', () => {
    setUser({ id: 1, username: 'admin', role: 'admin' })
    const wrapper = mount(AddButton, {
      propsData: { permission: 'user:add' },
      directives: {
        permission: () => {}
      },
      stubs: {
        'el-button': {
          template: '<button :disabled="disabled"><slot /></button>',
          props: ['disabled', 'loading']
        }
      }
    })
    expect(wrapper.find('button').attributes('disabled')).toBeUndefined()
  })
})

