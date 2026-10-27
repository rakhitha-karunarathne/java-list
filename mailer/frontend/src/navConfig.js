/** Sidebar IA — indexes/tools only (C8). Mirrors approved Clearwater mock. */

export const NAV_SECTIONS = [
  {
    id: 'dashboard',
    label: 'DASHBOARD',
    items: [{ id: 'attention', label: 'Attention', hideForSender: true }],
  },
  {
    id: 'lists-members',
    label: 'LISTS & MEMBERS',
    items: [
      { id: 'lists', label: 'Lists', hideForSender: true },
      { id: 'members', label: 'Members', hideForSender: true },
    ],
  },
  {
    id: 'campaigns',
    label: 'CAMPAIGNS',
    items: [
      { id: 'campaigns', label: 'Campaigns' },
      { id: 'compose', label: 'Compose' },
    ],
  },
  {
    id: 'mts',
    label: 'MAIL-TO-SEND',
    items: [
      { id: 'mts', label: 'Inboxes', adminOnly: true },
      { id: 'drop-log', label: 'Drop Log', adminOnly: true },
      { id: 'blocks', label: 'Global Blocks', adminOnly: true },
      { id: 'issues', label: 'Issues', hideForSender: true },
      { id: 'mts-process-log', label: 'Mail-To-Send Processing Log', adminOnly: true },
    ],
  },
  {
    id: 'bounces',
    label: 'BOUNCES',
    items: [
      { id: 'bounces', label: 'Bounce Log', hideForSender: true },
      { id: 'bounce-mailboxes', label: 'Bounce Mailboxes', adminOnly: true },
      { id: 'bounce-process-log', label: 'Bounce Processing Log', adminOnly: true },
    ],
  },
  {
    id: 'catalogs',
    label: 'CATALOGS',
    items: [
      { id: 'sending-paths', label: 'Sending Paths', adminOnly: true },
      { id: 'footers', label: 'Footers', adminOnly: true },
      { id: 'notice-path', label: 'Notice Sending Path', adminOnly: true },
      { id: 'parameters', label: 'Parameters', adminOnly: true },
    ],
  },
  {
    id: 'system',
    label: 'SYSTEM',
    items: [
      { id: 'users', label: 'Users & Grants', adminOnly: true },
      { id: 'ops', label: 'Ops Logs', adminOnly: true },
      { id: 'queue-send-log', label: 'Queue Sending Log', adminOnly: true },
      { id: 'audit', label: 'Audit Log', hideForSender: true },
    ],
  },
]

/** @param {'ADMIN'|'LIST_MANAGER'|'LIST_SENDER'|string} role */
export function visibleSections(role) {
  const isAdmin = role === 'ADMIN'
  const isSender = role === 'LIST_SENDER'
  return NAV_SECTIONS.map((section) => {
    const items = section.items.filter((item) => {
      if (item.adminOnly && !isAdmin) return false
      if (item.hideForSender && isSender) return false
      return true
    })
    return { ...section, items }
  }).filter((s) => s.items.length > 0)
}
