import { mtsSenderDetailId, parseMtsDetailId } from './mtsNav.js'

function assert(cond, msg) {
  if (!cond) throw new Error(msg || 'assert failed')
}

assert(parseMtsDetailId(null).kind === 'index', 'null → index')
assert(parseMtsDetailId('inbox-1').kind === 'inbox', 'plain → inbox')
assert(parseMtsDetailId('inbox-1').inboxId === 'inbox-1', 'inbox id')

const sid = mtsSenderDetailId('inbox-1', 'a@b.com')
const parsed = parseMtsDetailId(sid)
assert(parsed.kind === 'sender', 'sender kind')
assert(parsed.inboxId === 'inbox-1', 'sender inbox')
assert(parsed.email === 'a@b.com', 'sender email round-trip')

console.log('mtsNav.test.js ok')
