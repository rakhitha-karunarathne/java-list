import { useMemo, useState } from 'react'
import { TOKEN_KEY, decodeJwtPayload, roleFromToken } from './auth.js'
import LoginCard from './LoginCard.jsx'
import Shell from './Shell.jsx'
import { MainView } from './FeaturePages.jsx'

export default function App() {
  const [token, setToken] = useState(() => sessionStorage.getItem(TOKEN_KEY) || '')
  const [error, setError] = useState('')
  const [view, setView] = useState('attention')
  const [detailId, setDetailId] = useState(null)

  const role = useMemo(() => roleFromToken(token), [token])
  const email = useMemo(() => decodeJwtPayload(token)?.email || '', [token])

  function acceptToken(t) {
    sessionStorage.setItem(TOKEN_KEY, t)
    setToken(t)
    setError('')
    setDetailId(null)
    const r = roleFromToken(t)
    setView(r === 'LIST_SENDER' ? 'compose' : 'attention')
  }

  function onLogout() {
    sessionStorage.removeItem(TOKEN_KEY)
    setToken('')
    setView('attention')
    setDetailId(null)
    setError('')
    document.body.classList.remove('nav-open')
  }

  function onNavigate(id) {
    setView(id)
    setDetailId(null)
    setError('')
  }

  function openInView(nextView, id) {
    setView(nextView)
    setDetailId(id)
    setError('')
  }

  if (!token) {
    return <LoginCard onToken={acceptToken} onError={setError} />
  }

  return (
    <>
      {error ? (
        <div style={{ background: '#fef2f2', color: 'var(--danger)', padding: '8px 16px' }}>
          {error}
        </div>
      ) : null}
      <Shell
        role={role}
        view={view}
        email={email}
        onNavigate={onNavigate}
        onLogout={onLogout}
      >
        <MainView
          view={view}
          token={token}
          onError={setError}
          detailId={detailId}
          onOpenDetail={setDetailId}
          onCloseDetail={() => setDetailId(null)}
          openInView={openInView}
        />
      </Shell>
    </>
  )
}
