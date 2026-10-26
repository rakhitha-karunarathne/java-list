import assert from 'node:assert/strict'
import { test } from 'node:test'
import { fieldLabel } from './fieldLabel.js'

test('fieldLabel Title Cases camelCase', () => {
  assert.equal(fieldLabel('fromEmail'), 'From Email')
  assert.equal(fieldLabel('pollIntervalMinutes'), 'Poll Interval Minutes')
  assert.equal(fieldLabel('htmlBody'), 'Html Body')
  assert.equal(fieldLabel('useTls'), 'Use Tls')
})

test('fieldLabel Title Cases spaced and snake', () => {
  assert.equal(fieldLabel('sending path'), 'Sending Path')
  assert.equal(fieldLabel('disable_reason'), 'Disable Reason')
  assert.equal(fieldLabel('name'), 'Name')
})
