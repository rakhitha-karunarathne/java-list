import { useEffect, useState } from 'react'

const TOKEN_KEY = 'listmailer.jwt'

function App() {
  const [email, setEmail] = useState('admin@example.com')
  const [password, setPassword] = useState('changeme')
  const [token, setToken] = useState(() => sessionStorage.getItem(TOKEN_KEY) || '')
  const [ping, setPing] = useState('—')
  const [error, setError] = useState('')
  const [params, setParams] = useState([])
  const [view, setView] = useState('home')

  useEffect(() => {
    if (!token) {
      setPing('sign in first')
      return
    }
    fetch('/api/ping', { headers: { Authorization: `Bearer ${token}` } })
      .then(async (r) => {
        if (!r.ok) throw new Error(`${r.status}`)
        return r.json()
      })
      .then((body) => setPing(body.status ?? JSON.stringify(body)))
      .catch((err) => setPing(`error ${err}`))
  }, [token])

  useEffect(() => {
    if (!token || view !== 'parameters') return
    fetch('/api/parameters', { headers: { Authorization: `Bearer ${token}` } })
      .then(async (r) => {
        if (!r.ok) throw new Error(`${r.status}`)
        return r.json()
      })
      .then(setParams)
      .catch(() => setParams([]))
  }, [token, view])

  async function onLogin(e) {
    e.preventDefault()
    setError('')
    const res = await fetch('/api/auth/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email, password }),
    })
    if (!res.ok) {
      setError('Login failed')
      return
    }
    const body = await res.json()
    if (!body.token) {
      setError(
        body.mfaRequired
          ? 'MFA required (use API /api/auth/mfa)'
          : body.enrollmentRequired
            ? 'MFA enrollment required'
            : 'Login failed',
      )
      return
    }
    sessionStorage.setItem(TOKEN_KEY, body.token)
    setToken(body.token)
  }

  function onLogout() {
    sessionStorage.removeItem(TOKEN_KEY)
    setToken('')
    setView('home')
  }

  async function saveParam(key, value) {
    setError('')
    const res = await fetch(`/api/parameters/${encodeURIComponent(key)}`, {
      method: 'PUT',
      headers: {
        Authorization: `Bearer ${token}`,
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ value }),
    })
    if (!res.ok) {
      setError(`Save failed for ${key} (${res.status})`)
      return
    }
    const updated = await res.json()
    setParams((prev) => prev.map((p) => (p.key === key ? updated : p)))
  }

  return (
    <main style={{ fontFamily: 'system-ui', padding: '2rem', maxWidth: 720 }}>
      <h1>List Mailer</h1>
      {!token ? (
        <form onSubmit={onLogin} style={{ maxWidth: 420 }}>
          <label>
            Email
            <input value={email} onChange={(ev) => setEmail(ev.target.value)} type="email" required />
          </label>
          <label>
            Password
            <input
              value={password}
              onChange={(ev) => setPassword(ev.target.value)}
              type="password"
              required
            />
          </label>
          <button type="submit">Sign in</button>
          {error ? <p style={{ color: 'crimson' }}>{error}</p> : null}
        </form>
      ) : (
        <>
          <p>
            Signed in.{' '}
            <button type="button" onClick={onLogout}>
              Sign out
            </button>{' '}
            <button type="button" onClick={() => setView('home')}>
              Home
            </button>{' '}
            <button type="button" onClick={() => setView('parameters')}>
              Parameters
            </button>
          </p>
          {view === 'home' ? (
            <p>
              <code>/api/ping</code>: <strong>{ping}</strong>
            </p>
          ) : (
            <section>
              <h2>Parameters</h2>
              {error ? <p style={{ color: 'crimson' }}>{error}</p> : null}
              {params.map((p) => (
                <div key={p.key} style={{ marginBottom: '1.25rem', borderBottom: '1px solid #ddd', paddingBottom: '1rem' }}>
                  <label>
                    <code>{p.key}</code>
                    <input
                      defaultValue={p.value}
                      onBlur={(ev) => {
                        if (ev.target.value !== p.value) saveParam(p.key, ev.target.value)
                      }}
                    />
                  </label>
                  <p style={{ fontSize: '0.9rem', color: '#444' }}>{p.description}</p>
                </div>
              ))}
            </section>
          )}
        </>
      )}
    </main>
  )
}

export default App
