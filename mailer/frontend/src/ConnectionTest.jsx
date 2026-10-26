import { apiJson } from './api.js'

/** Shared Test Connection result line + API helper. */
export function ConnectionTestBanner({ result }) {
  if (!result) return null
  if (result.ok) {
    return (
      <p className="hint" style={{ color: 'var(--success)' }} role="status">
        Connection succeeded.
      </p>
    )
  }
  return (
    <p className="hint" style={{ color: 'var(--danger)' }} role="alert">
      Connection failed: {result.message || 'Unknown error'}
    </p>
  )
}

export async function testSmtpConnection(token, body) {
  return apiJson(token, '/api/sending-paths/test-connection', {
    method: 'POST',
    body: JSON.stringify(body),
  })
}

export async function testImapConnection(token, body) {
  return apiJson(token, '/api/mts/inboxes/test-connection', {
    method: 'POST',
    body: JSON.stringify(body),
  })
}
