import assert from 'node:assert/strict'
import { test } from 'node:test'
import { apiJson } from './api.js'

test('apiJson returns parsed body on 200', async () => {
  globalThis.fetch = async () =>
    new Response(JSON.stringify({ id: '1' }), {
      status: 200,
      headers: { 'Content-Type': 'application/json' },
    })
  const body = await apiJson('tok', '/api/x')
  assert.equal(body.id, '1')
})

test('apiJson throws with message and status on error', async () => {
  globalThis.fetch = async () =>
    new Response(JSON.stringify({ message: 'conflict' }), {
      status: 409,
      headers: { 'Content-Type': 'application/json' },
    })
  await assert.rejects(() => apiJson('tok', '/api/x'), (err) => {
    assert.equal(err.message, 'conflict')
    assert.equal(err.status, 409)
    return true
  })
})
