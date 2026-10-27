import assert from 'node:assert/strict'
import { test } from 'node:test'
import { visibleSections } from './navConfig.js'

const FORBIDDEN_SIDEBAR_LABELS = [
  'Campaign detail',
  'List detail',
  'Member detail',
  'User detail',
  'Sending path detail',
  'Bounce mailbox detail',
  'Ops log detail',
  'Inbox detail',
]

test('admin sees Attention and Parameters', () => {
  const ids = visibleSections('ADMIN').flatMap((s) => s.items.map((i) => i.id))
  assert.ok(ids.includes('attention'))
  assert.ok(ids.includes('parameters'))
  assert.ok(ids.includes('users'))
})

test('list sender has no Attention or Admin catalogs', () => {
  const ids = visibleSections('LIST_SENDER').flatMap((s) => s.items.map((i) => i.id))
  assert.ok(!ids.includes('attention'))
  assert.ok(!ids.includes('parameters'))
  assert.ok(!ids.includes('users'))
  assert.ok(ids.includes('compose'))
  assert.ok(ids.includes('campaigns'))
})

test('list manager sees Attention but not Parameters', () => {
  const ids = visibleSections('LIST_MANAGER').flatMap((s) => s.items.map((i) => i.id))
  assert.ok(ids.includes('attention'))
  assert.ok(!ids.includes('parameters'))
  assert.ok(ids.includes('lists'))
})

test('bounces nav label is Bounce Messages', () => {
  const item = visibleSections('ADMIN')
    .flatMap((s) => s.items)
    .find((i) => i.id === 'bounces')
  assert.equal(item?.label, 'Bounce Messages')
})

test('bounces section order is mailbox, messages, processing log', () => {
  const section = visibleSections('ADMIN').find((s) => s.id === 'bounces')
  assert.deepEqual(
    section?.items.map((i) => i.id),
    ['bounce-mailboxes', 'bounces', 'bounce-process-log'],
  )
})

test('parameters is under system not catalogs', () => {
  const admin = visibleSections('ADMIN')
  const system = admin.find((s) => s.id === 'system')
  const catalogs = admin.find((s) => s.id === 'catalogs')
  assert.ok(system?.items.some((i) => i.id === 'parameters'))
  assert.ok(!catalogs?.items.some((i) => i.id === 'parameters'))
})

test('C8 forbidden detail labels not used in nav config', () => {
  const labels = visibleSections('ADMIN').flatMap((s) => s.items.map((i) => i.label))
  for (const bad of FORBIDDEN_SIDEBAR_LABELS) {
    assert.ok(!labels.includes(bad), bad)
  }
})
