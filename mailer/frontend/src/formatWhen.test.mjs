import assert from 'node:assert/strict'
import { formatWhen } from './formatWhen.js'

assert.equal(formatWhen(null), '—')
assert.equal(formatWhen(''), '—')
assert.equal(formatWhen('not-a-date'), '—')

const utc = formatWhen('2026-10-27T12:00:00Z', 'UTC')
assert.match(utc, /^2026-10-27 12:00:00 /)

const colombo = formatWhen('2026-10-27T12:00:00Z', 'Asia/Colombo')
assert.match(colombo, /^2026-10-27 17:30:00 /)

console.log('formatWhen.test.mjs ok')
