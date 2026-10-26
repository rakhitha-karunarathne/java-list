import assert from 'node:assert/strict'
import test from 'node:test'
import { formatBooleanString, isBooleanString, parseBooleanString } from './booleanValue.js'

test('isBooleanString accepts true/false only', () => {
  assert.equal(isBooleanString('true'), true)
  assert.equal(isBooleanString('FALSE'), true)
  assert.equal(isBooleanString('yes'), false)
  assert.equal(isBooleanString('1'), false)
  assert.equal(isBooleanString(''), false)
})

test('parseBooleanString and formatBooleanString', () => {
  assert.equal(parseBooleanString('true'), true)
  assert.equal(parseBooleanString('False'), false)
  assert.equal(formatBooleanString(true), 'true')
  assert.equal(formatBooleanString(false), 'false')
})
