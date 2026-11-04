import { useEffect, useState } from 'react'
import { NAV_DEFAULT_OPEN, visibleSections } from './navConfig.js'

export { default as Placeholder } from './Placeholder.jsx'

const TITLES = {
  attention: 'Attention',
  lists: 'Lists',
  members: 'Members',
  campaigns: 'Campaigns',
  compose: 'Compose',
  mts: 'Inboxes',
  'drop-log': 'Drop Log',
  blocks: 'Global Blocks',
  issues: 'Issues',
  bounces: 'Bounce Messages',
  'bounce-mailboxes': 'Bounce Mailboxes',
  'sending-paths': 'Sending Paths',
  footers: 'Footers',
  'notice-path': 'Notice Sending Path',
  parameters: 'Parameters',
  users: 'Users & Grants',
  account: 'My Account',
  ops: 'Ops Logs',
  'bounce-process-log': 'Bounce Processing Log',
  'queue-send-log': 'Queue Sending Log',
  'mts-process-log': 'Mail-To-Send Processing Log',
  audit: 'Audit Log',
  home: 'Home',
}

function sectionIdForView(sections, viewId) {
  for (const section of sections) {
    if (section.items.some((item) => item.id === viewId)) return section.id
  }
  return null
}

export default function Shell({
  role,
  view,
  onNavigate,
  onLogout,
  email,
  siteTitle = 'List Mailer',
  children,
}) {
  const sections = visibleSections(role)
  const title = TITLES[view] || siteTitle
  const activeSectionId = sectionIdForView(sections, view)
  const [openIds, setOpenIds] = useState(() => {
    const initial = new Set(NAV_DEFAULT_OPEN)
    const active = sectionIdForView(visibleSections(role), view)
    if (active) initial.add(active)
    return initial
  })

  useEffect(() => {
    if (!activeSectionId) return
    setOpenIds((prev) => {
      if (prev.has(activeSectionId)) return prev
      const next = new Set(prev)
      next.add(activeSectionId)
      return next
    })
  }, [activeSectionId])

  function openNav() {
    document.body.classList.add('nav-open')
  }

  function closeNav() {
    document.body.classList.remove('nav-open')
  }

  function go(id) {
    onNavigate(id)
    closeNav()
  }

  function toggleSection(sectionId) {
    setOpenIds((prev) => {
      const next = new Set(prev)
      if (next.has(sectionId)) next.delete(sectionId)
      else next.add(sectionId)
      return next
    })
  }

  return (
    <div className="app">
      <div className="backdrop" onClick={closeNav} aria-hidden="true" />
      <aside className="sidebar">
        <div className="brand">
          {siteTitle}
          <small>Operator UI · Clearwater</small>
        </div>
        <nav className="nav" aria-label="Primary">
          {sections.map((section) => {
            const open = openIds.has(section.id)
            return (
              <div className={`nav-section${open ? '' : ' collapsed'}`} key={section.id}>
                <button
                  type="button"
                  className="cat"
                  aria-expanded={open}
                  onClick={() => toggleSection(section.id)}
                >
                  {section.label}
                </button>
                {open
                  ? section.items.map((item) => (
                      <button
                        type="button"
                        key={item.id}
                        className={`nav-item${view === item.id ? ' active' : ''}`}
                        onClick={() => go(item.id)}
                      >
                        {item.label}
                      </button>
                    ))
                  : null}
              </div>
            )
          })}
          <div className="nav-section">
            <button
              type="button"
              className={`nav-item${view === 'account' ? ' active' : ''}`}
              onClick={() => go('account')}
            >
              My Account
            </button>
          </div>
        </nav>
      </aside>
      <div className="main">
        <header className="topbar">
          <button type="button" className="menu-btn" aria-label="Open menu" onClick={openNav}>
            ☰
          </button>
          <h1>{title}</h1>
          <button type="button" className="user-chip" title="My Account" onClick={() => go('account')}>
            {email || role}
          </button>
          <span className="hint">{role}</span>
          <button type="button" className="btn secondary" onClick={onLogout}>
            Sign out
          </button>
        </header>
        <div className="content">{children}</div>
      </div>
    </div>
  )
}
