import { shallowMount } from '@vue/test-utils'
import flushPromises from 'flush-promises'
import Script from '../views/script.vue'
import { getScriptPage, addScript, updateScript, deleteScript } from '../api/script'

jest.mock('../api/script', () => ({
  getScriptPage: jest.fn(() =>
    Promise.resolve({ code: 200, data: { list: [], total: 0 } })
  ),
  addScript: jest.fn(() => Promise.resolve({ code: 200, data: { id: 101 } })),
  updateScript: jest.fn(() => Promise.resolve({ code: 200 })),
  deleteScript: jest.fn(() => Promise.resolve({ code: 200 }))
}))

describe('Script view', () => {
  let wrapper
  
  beforeEach(() => {
    jest.clearAllMocks()
    wrapper = shallowMount(Script, {
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
    expect(getScriptPage).toHaveBeenCalled()
  })

  test('submitForm add calls API and closes dialog', async () => {
    wrapper.vm.isEdit = false
    wrapper.vm.dialogVisible = true
    wrapper.vm.scriptForm = {
      scriptName: 'New Script',
      type: 'Horror',
      difficulty: 'HARD',
      needDmLevel: 5,
      minPlayers: 4,
      maxPlayers: 7
    }
    wrapper.vm.$refs.scriptForm = {
      validate: cb => cb(true),
      clearValidate: jest.fn()
    }

    wrapper.vm.submitForm()
    await flushPromises()

    expect(addScript).toHaveBeenCalledWith(wrapper.vm.scriptForm)
    expect(wrapper.vm.dialogVisible).toBe(false)
  })

  test('submitForm edit calls update API', async () => {
    wrapper.vm.isEdit = true
    wrapper.vm.scriptForm = {
      id: 1,
      scriptName: 'New Name',
      type: 'Horror',
      difficulty: 'HARD',
      needDmLevel: 5,
      minPlayers: 4,
      maxPlayers: 7
    }
    wrapper.vm.$refs.scriptForm = {
      validate: cb => cb(true),
      clearValidate: jest.fn()
    }

    wrapper.vm.submitForm()
    await flushPromises()

    expect(updateScript).toHaveBeenCalledWith(wrapper.vm.scriptForm)
  })

  test('handleDelete calls delete API', async () => {
    wrapper.vm.handleDelete({ id: 1 })
    await flushPromises()
    expect(deleteScript).toHaveBeenCalledWith(1)
  })
})
