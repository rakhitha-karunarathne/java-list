import { visibleSections } from './navConfig.js'

const TITLES = {
  attention: 'Attention',
  lists: 'Lists',
  members: 'Members',
  campaigns: 'Campaigns',
  compose: 'Compose',
  mts: 'Inboxes & Mappings',
  'drop-log': 'Drop Log',
  blocks: 'Global Blocks',
  issues: 'Issues',
  bounces: 'Bounce Log',
  'bounce-mailboxes': 'Bounce Mailboxes',
  'sending-paths': 'Sending Paths',
  footers: 'Footers',
  'notice-path': 'Notice Sending Path',
  parameters: 'Parameters',
  users: 'Users & Grants',
  ops: 'Ops Logs',
  audit: 'Audit Log',
  home: 'Home',
}

export function Placeholder({ title, hint }) {
  return (
    <div className="card">
      <h2>{title}</h2>
      <p className="hint">{hint || 'Coming in a later frontend epic. Index only — detail pages open from rows, not the sidebar (C8).'}</p>
    </div>
  )
}

export default function Shell({ role, view, onNavigate, onLogout, email, children }) {
  const sections = visibleSections(role)
  const title = TITLES[view] || 'List Mailer'

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

  return (
    <div className="app">
      <div className="backdrop" onClick={closeNav} aria-hidden="true" />
      <aside className="sidebar" aria-label="Primary">
        <div className="brand">
          List Mailer
          <small>Operator UI · Clearwater</small>
        </div>
        <nav className="nav">
          {sections.map((section) => (
            <div className="nav-section" key={section.id}>
              <button type="button" className="cat">
                {section.label}
              </button>
              {section.items.map((item) => (
                <button
                  type="button"
                  key={item.id}
                  className={`nav-item${view === item.id ? ' active' : ''}`}
                  onClick={() => go(item.id)}
                >
                  {item.label}
                </button>
              ))}
            </div>
          ))}
        </nav>
      </aside>
      <div className="main">
        <header className="topbar">
          <button type="button" className="menu-btn" aria-label="Open menu" onClick={openNav}>
            ☰
          </button>
          <h1>{title}</h1>
          <span className="hint">
            {email || role} · {role}
          </span>
          <button type="button" className="btn secondary" onClick={onLogout}>
            Sign out
          </button>
        </header>
        <div className="content">{children}</div>
      </div>
    </div>
  )
}
