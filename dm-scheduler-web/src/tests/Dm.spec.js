import { shallowMount } from '@vue/test-utils'
import flushPromises from 'flush-promises'
import Dm from '../views/dm.vue'
import dmApi from '../api/dm'

jest.mock('../api/dm', () => ({
  __esModule: true,
  default: {
    getDmPage: jest.fn(() =>
      Promise.resolve({ code: 200, data: { list: [], total: 0 } })
    ),
    addDm: jest.fn(() => Promise.resolve({ code: 200, data: { id: 201 } })),
    updateDm: jest.fn(() => Promise.resolve({ code: 200 })),
    deleteDm: jest.fn(() => Promise.resolve({ code: 200 }))
  }
}))

describe('Dm view', () => {
  let wrapper
  
  beforeEach(() => {
    jest.clearAllMocks()
    wrapper = shallowMount(Dm, {
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
        'el-rate': true,
        'el-row': true,
        'el-col': true,
        'el-input-number': true,
        'el-pagination': true
      },
      directives: {
        loading: () => {}
      },
      mocks: {
        $message: { success: jest.fn(), error: jest.fn(), info: jest.fn() },
        $confirm: jest.fn(() => Promise.resolve()),
        $nextTick: (fn) => fn()
      }
    })
  })

  test('loads page list on mount', async () => {
    await flushPromises()
    expect(dmApi.getDmPage).toHaveBeenCalled()
  })

  test('submitForm add calls API and closes dialog', async () => {
    wrapper.vm.isEdit = false
    wrapper.vm.dialogVisible = true
    wrapper.vm.dmForm = {
      userId: 1001,
      dmLevel: 2,
      experience: 100,
      rating: 4.5,
      status: 'AVAILABLE',
      specialty: '',
      weeklyMaxSessions: 10,
      bio: ''
    }
    wrapper.vm.$refs.dmForm = { validate: cb => cb(true), clearValidate: jest.fn() }

    wrapper.vm.submitForm()
    await flushPromises()
    
    expect(dmApi.addDm).toHaveBeenCalledWith(wrapper.vm.dmForm)
    expect(wrapper.vm.dialogVisible).toBe(false)
  })

  test('submitForm edit calls update API', async () => {
    wrapper.vm.isEdit = true
    wrapper.vm.dmForm = {
      id: 1,
      userId: 1001,
      dmLevel: 2,
      experience: 120,
      rating: 5.0
    }
    wrapper.vm.$refs.dmForm = { validate: cb => cb(true), clearValidate: jest.fn() }

    wrapper.vm.submitForm()
    await flushPromises()
    
    expect(dmApi.updateDm).toHaveBeenCalledWith(wrapper.vm.dmForm)
  })

  test('handleDelete calls delete API', async () => {
    wrapper.vm.handleDelete({ id: 1 })
    await flushPromises()
    expect(dmApi.deleteDm).toHaveBeenCalledWith(1)
  })
})
