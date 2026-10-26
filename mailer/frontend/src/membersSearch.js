export function membersSearchPath(q) {
  const t = (q || '').trim()
  return t ? `/api/members?q=${encodeURIComponent(t)}` : '/api/members'
}
