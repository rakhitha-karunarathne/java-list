import assert from 'node:assert/strict'
import { test } from 'node:test'
import { membersSearchPath } from './membersSearch.js'

test('empty search uses index without q', () => {
  assert.equal(membersSearchPath(''), '/api/members')
  assert.equal(membersSearchPath('  '), '/api/members')
})

test('search encodes query', () => {
  assert.equal(membersSearchPath('a@b.com'), '/api/members?q=a%40b.com')
})
