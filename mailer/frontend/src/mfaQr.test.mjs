import assert from 'node:assert/strict'
import { test } from 'node:test'
import { otpAuthQrDataUrl } from './mfaQr.js'

test('otpAuthQrDataUrl returns a PNG data URL for otpauth URI', async () => {
  const url = await otpAuthQrDataUrl(
    'otpauth://totp/List%20Mailer:a@example.com?secret=JBSWY3DPEHPK3PXP&issuer=List%20Mailer',
  )
  assert.ok(url.startsWith('data:image/png;base64,'), url.slice(0, 40))
  assert.ok(url.length > 200)
})
