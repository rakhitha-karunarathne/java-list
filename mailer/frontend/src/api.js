import { authHeaders } from './auth.js'

export async function apiJson(token, path, options = {}) {
  const res = await fetch(path, {
    ...options,
    headers: {
      ...(options.body ? authHeaders(token) : { Authorization: `Bearer ${token}` }),
      ...(options.headers || {}),
    },
  })
  const text = await res.text()
  let body = null
  if (text) {
    try {
      body = JSON.parse(text)
    } catch {
      body = text
    }
  }
  if (!res.ok) {
    const err = new Error(
      typeof body === 'object' && body?.message
        ? body.message
        : typeof body === 'string'
          ? body
          : `${res.status}`,
    )
    err.status = res.status
    err.body = body
    throw err
  }
  return body
}
