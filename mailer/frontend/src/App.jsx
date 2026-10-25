import { useEffect, useState } from 'react'

const TOKEN_KEY = 'listmailer.jwt'

function App() {
  const [email, setEmail] = useState('admin@example.com')
  const [password, setPassword] = useState('changeme')
  const [token, setToken] = useState(() => sessionStorage.getItem(TOKEN_KEY) || '')
  const [ping, setPing] = useState('—')
  const [error, setError] = useState('')

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
    sessionStorage.setItem(TOKEN_KEY, body.token)
    setToken(body.token)
  }

  function onLogout() {
    sessionStorage.removeItem(TOKEN_KEY)
    setToken('')
  }

  return (
    <main style={{ fontFamily: 'system-ui', padding: '2rem', maxWidth: 420 }}>
      <h1>List Mailer</h1>
      <p>JWT stub (sessionStorage).</p>
      {!token ? (
        <form onSubmit={onLogin}>
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
        <p>
          Signed in. <button type="button" onClick={onLogout}>Sign out</button>
        </p>
      )}
      <p>
        <code>/api/ping</code>: <strong>{ping}</strong>
      </p>
    </main>
  )
}

export default App
