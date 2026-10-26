import assert from 'node:assert/strict'
import test from 'node:test'
import { btnDirtyClass, dirtyClass, dirtyControlProps, isFormDirty, valuesEqual } from './formDirty.js'

test('valuesEqual treats blank string and null as equal', () => {
  assert.equal(valuesEqual('', null), true)
  assert.equal(valuesEqual(undefined, ''), true)
})

test('valuesEqual compares booleans and arrays', () => {
  assert.equal(valuesEqual(true, false), false)
  assert.equal(valuesEqual([1, 2], [1, 2]), true)
  assert.equal(valuesEqual([1], [1, 2]), false)
})

test('isFormDirty detects changed keys', () => {
  assert.equal(isFormDirty({ a: '1' }, { a: '1' }), false)
  assert.equal(isFormDirty({ a: '2' }, { a: '1' }), true)
  assert.equal(isFormDirty({ a: '1' }, null), false)
})

test('dirtyClass and btnDirtyClass add is-dirty', () => {
  assert.equal(dirtyClass(true), 'is-dirty')
  assert.equal(dirtyClass(false), undefined)
  assert.equal(btnDirtyClass(true), 'btn is-dirty')
  assert.equal(btnDirtyClass(false, 'btn secondary'), 'btn secondary')
  assert.deepEqual(dirtyControlProps(true), { className: 'is-dirty', title: 'Unsaved changes' })
})
