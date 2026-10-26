/** detailId: inboxId | inboxId/s/email | CREATE_ID */

export function parseMtsDetailId(detailId) {
  if (!detailId) return { kind: 'index' }
  const marker = '/s/'
  const i = detailId.indexOf(marker)
  if (i > 0) {
    return {
      kind: 'sender',
      inboxId: detailId.slice(0, i),
      email: decodeURIComponent(detailId.slice(i + marker.length)),
    }
  }
  return { kind: 'inbox', inboxId: detailId }
}

export function mtsSenderDetailId(inboxId, email) {
  return `${inboxId}/s/${encodeURIComponent(email)}`
}
