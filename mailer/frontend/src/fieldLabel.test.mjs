import assert from 'node:assert/strict'
import { test } from 'node:test'
import { fieldLabel } from './fieldLabel.js'

test('fieldLabel never glues words — camelCase becomes spaced English', () => {
  assert.equal(fieldLabel('fromEmail'), 'From Email')
  assert.equal(fieldLabel('pollIntervalMinutes'), 'Poll Interval Minutes')
  assert.equal(fieldLabel('spamThreshold'), 'Spam Threshold')
  assert.doesNotMatch(fieldLabel('fromEmail'), /FromEmail/)
  assert.doesNotMatch(fieldLabel('htmlBody'), /HtmlBody|htmlBody/)
})

test('fieldLabel uses valid English acronyms', () => {
  assert.equal(fieldLabel('htmlBody'), 'HTML Body')
  assert.equal(fieldLabel('textBody'), 'Text Body')
  assert.equal(fieldLabel('useTls'), 'Use TLS')
  assert.equal(fieldLabel('useSsl'), 'Use SSL')
})

test('fieldLabel Title Cases spaced and snake keys', () => {
  assert.equal(fieldLabel('sending path'), 'Sending Path')
  assert.equal(fieldLabel('disable_reason'), 'Disable Reason')
  assert.equal(fieldLabel('name'), 'Name')
})

test('fieldLabel maps parameter keys to English phrases', () => {
  assert.equal(fieldLabel('public.base.url'), 'Public Base URL')
  assert.equal(fieldLabel('operator.mfa.mandatory'), 'Require MFA For All Operators')
  assert.doesNotMatch(fieldLabel('public.base.url'), /public\.base/)
})
