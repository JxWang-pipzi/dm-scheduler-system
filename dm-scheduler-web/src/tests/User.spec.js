import { shallowMount } from '@vue/test-utils'
import flushPromises from 'flush-promises'
import User from '../views/user.vue'
import { getUserPage, addUser, updateUser, deleteUser } from '../api/user'

jest.mock('../api/user', () => ({
  getUserPage: jest.fn(() =>
    Promise.resolve({ code: 200, data: { list: [], total: 0 } })
  ),
  addUser: jest.fn(() => Promise.resolve({ code: 200, data: { id: 2 } })),
  updateUser: jest.fn(() => Promise.resolve({ code: 200 })),
  deleteUser: jest.fn(() => Promise.resolve({ code: 200 }))
}))

const createWrapper = () =>
  shallowMount(User, {
    stubs: {
      Layout: { template: '<div><slot name="header"></slot><slot /></div>' },
      'el-table': true,
      'el-table-column': true,
      'el-dialog': true,
      'el-form': true,
      'el-form-item': true,
      'el-input': true,
      'el-select': true,
      'el-option': true,
      'el-button': true,
      'el-tag': true,
      'el-pagination': true
    },
    directives: {
      loading: () => {}
    },
    mocks: {
      $message: { success: jest.fn(), error: jest.fn(), info: jest.fn() },
      $confirm: jest.fn(() => Promise.resolve())
    }
  })

describe('User view', () => {
  beforeEach(() => {
    jest.clearAllMocks()
  })

  test('loads page list on mount', async () => {
    createWrapper()
    await flushPromises()
    expect(getUserPage).toHaveBeenCalled()
  })

  test('submitAdd calls add API and closes dialog', async () => {
    const wrapper = createWrapper()
    wrapper.vm.addDialogVisible = true
    wrapper.vm.addForm = {
      username: 'u',
      password: '123456',
      role: 'USER',
      phone: '13800138000',
      realName: 'test'
    }
    wrapper.vm.$refs = {
      addForm: { validate: cb => cb(true), clearValidate: jest.fn() }
    }

    wrapper.vm.submitAdd()
    await flushPromises()

    expect(addUser).toHaveBeenCalledWith(wrapper.vm.addForm)
    expect(wrapper.vm.addDialogVisible).toBe(false)
  })

  test('submitEdit removes empty password before update', async () => {
    const wrapper = createWrapper()
    wrapper.vm.editForm = {
      id: 2,
      username: 'u2',
      role: 'USER',
      phone: '13800138000',
      realName: 'test',
      password: ''
    }

    await wrapper.vm.submitEdit()
    await flushPromises()

    expect(updateUser).toHaveBeenCalledWith(
      expect.objectContaining({
        id: 2,
        username: 'u2'
      })
    )
    expect(updateUser.mock.calls[0][0].password).toBeUndefined()
  })

  test('handleDelete triggers delete API', async () => {
    const wrapper = createWrapper()
    wrapper.vm.handleDelete({ id: 2 })
    await flushPromises()
    expect(deleteUser).toHaveBeenCalledWith(2)
  })
})

