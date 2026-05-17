import { shallowMount } from '@vue/test-utils'
import flushPromises from 'flush-promises'
import Session from '../views/session.vue'
import { getSessionPage, addSession, updateSession, deleteSession } from '../api/session'

jest.mock('../api/session', () => ({
  getSessionPage: jest.fn(() =>
    Promise.resolve({ code: 200, data: { list: [], total: 0 } })
  ),
  addSession: jest.fn(() => Promise.resolve({ code: 200, data: { id: 301 } })),
  updateSession: jest.fn(() => Promise.resolve({ code: 200 })),
  deleteSession: jest.fn(() => Promise.resolve({ code: 200 }))
}))

jest.mock('../api/script', () => ({
  getScriptList: jest.fn(() =>
    Promise.resolve({ code: 200, data: [{ id: 1, scriptName: 'S1' }] })
  )
}))

jest.mock('../api/dm', () => ({
  __esModule: true,
  default: {
    getDmList: jest.fn(() =>
      Promise.resolve({ code: 200, data: [{ id: 2, status: 'AVAILABLE' }] })
    )
  }
}))

describe('Session view', () => {
  let wrapper
  
  beforeEach(() => {
    jest.clearAllMocks()
    wrapper = shallowMount(Session, {
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
        'el-date-picker': true,
        'el-input-number': true,
        'el-tag': true,
        'el-row': true,
        'el-col': true,
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

  test('loads dictionaries and page on mount', async () => {
    await flushPromises()
    expect(getSessionPage).toHaveBeenCalled()
  })

  test('submitForm add calls API and closes dialog', async () => {
    wrapper.vm.isEdit = false
    wrapper.vm.dialogVisible = true
    wrapper.vm.sessionForm = {
      scriptId: 1,
      dmId: 2,
      status: 'PENDING',
      maxPlayers: 6,
      startTime: new Date().toISOString()
    }
    wrapper.vm.$refs.sessionForm = {
      validate: cb => cb(true),
      clearValidate: jest.fn()
    }

    wrapper.vm.submitForm()
    await flushPromises()
    
    expect(addSession).toHaveBeenCalledWith(wrapper.vm.sessionForm)
    expect(wrapper.vm.dialogVisible).toBe(false)
  })

  test('submitForm edit calls update API', async () => {
    wrapper.vm.isEdit = true
    wrapper.vm.sessionForm = {
      id: 1,
      scriptId: 1,
      dmId: 2,
      status: 'COMPLETED',
      maxPlayers: 6,
      startTime: new Date().toISOString()
    }
    wrapper.vm.$refs.sessionForm = {
      validate: cb => cb(true),
      clearValidate: jest.fn()
    }

    wrapper.vm.submitForm()
    await flushPromises()
    
    expect(updateSession).toHaveBeenCalledWith(wrapper.vm.sessionForm)
  })

  test('handleDelete calls delete API', async () => {
    wrapper.vm.handleDelete({ id: 1 })
    await flushPromises()
    expect(deleteSession).toHaveBeenCalledWith(1)
  })
})
