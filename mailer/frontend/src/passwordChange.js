/** Client check before POST /api/me/password. Empty string means ok. */
export function passwordChangeProblem(current, next, confirm) {
  const c = current || ''
  const n = next || ''
  if (!c.trim() || !n.trim()) return 'Current and new password are required.'
  if (n !== (confirm || '')) return 'New password and confirmation do not match.'
  if (c === n) return 'New password must differ from the current password.'
  return ''
}
