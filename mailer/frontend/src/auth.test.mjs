import assert from 'node:assert/strict'
import { test } from 'node:test'
import { roleFromToken, decodeJwtPayload } from './auth.js'

function fakeJwt(payload) {
  const json = Buffer.from(JSON.stringify(payload)).toString('base64url')
  return `hdr.${json}.sig`
}

test('roleFromToken reads ADMIN claim', () => {
  assert.equal(roleFromToken(fakeJwt({ role: 'ADMIN', email: 'a@b.com' })), 'ADMIN')
})

test('decodeJwtPayload returns email', () => {
  assert.equal(decodeJwtPayload(fakeJwt({ email: 'x@y.com', role: 'LIST_SENDER' })).email, 'x@y.com')
})

test('bad token defaults role to LIST_SENDER', () => {
  assert.equal(roleFromToken('not-a-jwt'), 'LIST_SENDER')
})
