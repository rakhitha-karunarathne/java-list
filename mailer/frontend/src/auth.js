export const TOKEN_KEY = 'listmailer.jwt'

export function authHeaders(token) {
  return { Authorization: `Bearer ${token}`, 'Content-Type': 'application/json' }
}

/** Decode JWT payload (no verify — display/role gating only). */
export function decodeJwtPayload(token) {
  if (!token || typeof token !== 'string') return null
  const parts = token.split('.')
  if (parts.length < 2) return null
  try {
    const json = atob(parts[1].replace(/-/g, '+').replace(/_/g, '/'))
    return JSON.parse(json)
  } catch {
    return null
  }
}

export function roleFromToken(token) {
  const payload = decodeJwtPayload(token)
  return payload?.role || 'LIST_SENDER'
}
