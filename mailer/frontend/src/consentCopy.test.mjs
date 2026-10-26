import assert from 'node:assert/strict'
import { test } from 'node:test'
import { CONSENT_REQUEST_HINT } from './consentCopy.js'

test('consent hint does not claim delivery', () => {
  assert.match(CONSENT_REQUEST_HINT, /Notice sending path/i)
  assert.doesNotMatch(CONSENT_REQUEST_HINT, /delivered|sent successfully/i)
})
