import { useEffect, useState } from 'react'

const TOKEN_KEY = 'listmailer.jwt'

function authHeaders(token) {
  return { Authorization: `Bearer ${token}`, 'Content-Type': 'application/json' }
}

function Compose({ token, onError }) {
  const [lists, setLists] = useState([])
  const [selected, setSelected] = useState([])
  const [subject, setSubject] = useState('')
  const [htmlBody, setHtmlBody] = useState('<p>Hello</p>')
  const [textBody, setTextBody] = useState('Hello')
  const [preview, setPreview] = useState('html')
  const [campaignId, setCampaignId] = useState('')
  const [status, setStatus] = useState('')
  const [attachment, setAttachment] = useState(null)
  const [testAddresses, setTestAddresses] = useState('')
  const [busy, setBusy] = useState(false)
  const [msg, setMsg] = useState('')

  useEffect(() => {
    fetch('/api/lists', { headers: { Authorization: `Bearer ${token}` } })
      .then(async (r) => {
        if (!r.ok) throw new Error(`${r.status}`)
        return r.json()
      })
      .then(setLists)
      .catch(() => setLists([]))
  }, [token])

  function toggleList(id) {
    setSelected((prev) => (prev.includes(id) ? prev.filter((x) => x !== id) : [...prev, id]))
  }

  function moveList(id, dir) {
    setSelected((prev) => {
      const i = prev.indexOf(id)
      if (i < 0) return prev
      const j = i + dir
      if (j < 0 || j >= prev.length) return prev
      const next = [...prev]
      ;[next[i], next[j]] = [next[j], next[i]]
      return next
    })
  }

  async function fileToAttachment(file) {
    const buf = await file.arrayBuffer()
    const bytes = new Uint8Array(buf)
    let binary = ''
    for (let i = 0; i < bytes.length; i++) binary += String.fromCharCode(bytes[i])
    return {
      filename: file.name,
      contentType: file.type || 'application/octet-stream',
      contentBase64: btoa(binary),
    }
  }

  async function bodyPayload() {
    const attachments = attachment ? [await fileToAttachment(attachment)] : []
    return {
      subject,
      htmlBody,
      textBody,
      listIds: selected,
      attachments,
    }
  }

  async function saveDraft() {
    onError('')
    setMsg('')
    try {
      const payload = await bodyPayload()
      const res = await fetch(campaignId ? `/api/campaigns/${campaignId}` : '/api/campaigns', {
        method: campaignId ? 'PUT' : 'POST',
        headers: authHeaders(token),
        body: JSON.stringify(payload),
      })
      if (!res.ok) {
        onError(`Save failed (${res.status})`)
        return
      }
      const body = await res.json()
      setCampaignId(body.id)
      setStatus(body.status)
      setSelected(body.listIds || [])
      if (body.subject != null) setSubject(body.subject)
      if (body.htmlBody != null) setHtmlBody(body.htmlBody)
      if (body.textBody != null) setTextBody(body.textBody)
      setMsg(`Saved Draft ${body.id}`)
    } catch (e) {
      onError(e.message || String(e))
    }
  }

  async function queueCampaign() {
    onError('')
    setMsg('')
    if (!campaignId) {
      onError('Save a Draft first')
      return
    }
    try {
      const res = await fetch(`/api/campaigns/${campaignId}/queue`, {
        method: 'POST',
        headers: { Authorization: `Bearer ${token}` },
      })
      if (!res.ok) {
        const t = await res.text()
        onError(`Queue failed (${res.status}): ${t}`)
        return
      }
      const body = await res.json()
      setStatus(body.status)
      setMsg(
        `Queued (${body.status}); processor sends under throttle — sent so far ${body.sentCount}`,
      )
    } catch (e) {
      onError(e.message || String(e))
    }
  }

  return (
    <section style={{ maxWidth: 840 }}>
      <h2>Compose</h2>
      {msg ? <p>{msg}</p> : null}
      {status ? (
        <p>
          Campaign <code>{campaignId}</code> — <strong>{status}</strong>
        </p>
      ) : null}
      <label>
        Subject
        <input value={subject} onChange={(e) => setSubject(e.target.value)} style={{ width: '100%' }} />
      </label>
      <fieldset style={{ marginTop: '1rem' }}>
        <legend>Lists (order = first Sending path)</legend>
        {lists.map((l) => (
          <div key={l.id} style={{ display: 'flex', gap: '0.5rem', alignItems: 'center' }}>
            <label>
              <input
                type="checkbox"
                checked={selected.includes(l.id)}
                onChange={() => toggleList(l.id)}
              />{' '}
              {l.name}
            </label>
            {selected.includes(l.id) ? (
              <>
                <button type="button" onClick={() => moveList(l.id, -1)}>
                  ↑
                </button>
                <button type="button" onClick={() => moveList(l.id, 1)}>
                  ↓
                </button>
              </>
            ) : null}
          </div>
        ))}
        <p style={{ fontSize: '0.85rem', color: '#555' }}>
          Selected order: {selected.map((id) => lists.find((l) => l.id === id)?.name || id).join(' → ') || '—'}
        </p>
      </fieldset>
      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem', marginTop: '1rem' }}>
        <label>
          HTML
          <textarea
            value={htmlBody}
            onChange={(e) => setHtmlBody(e.target.value)}
            rows={10}
            style={{ width: '100%', fontFamily: 'monospace' }}
          />
        </label>
        <label>
          Text
          <textarea
            value={textBody}
            onChange={(e) => setTextBody(e.target.value)}
            rows={10}
            style={{ width: '100%', fontFamily: 'monospace' }}
          />
        </label>
      </div>
      <p>
        Preview:{' '}
        <button type="button" onClick={() => setPreview('html')}>
          HTML
        </button>{' '}
        <button type="button" onClick={() => setPreview('text')}>
          Text
        </button>
      </p>
      <div
        style={{
          border: '1px solid #ccc',
          padding: '1rem',
          minHeight: 80,
          background: '#fafafa',
          whiteSpace: preview === 'text' ? 'pre-wrap' : undefined,
        }}
      >
        {preview === 'html' ? (
          <div dangerouslySetInnerHTML={{ __html: htmlBody }} />
        ) : (
          textBody
        )}
      </div>
      <p style={{ fontSize: '0.85rem', color: '#555' }}>
        Tip: reference an image URL in HTML (blob rewrite is a later story). Add one attachment below.
      </p>
      <label>
        Attachment
        <input type="file" onChange={(e) => setAttachment(e.target.files?.[0] || null)} />
      </label>
      <label style={{ display: 'block', marginTop: '1rem' }}>
        Test send addresses (comma or newline)
        <textarea
          value={testAddresses}
          onChange={(e) => setTestAddresses(e.target.value)}
          rows={3}
          style={{ width: '100%' }}
          placeholder="you@example.com"
        />
      </label>
      <p style={{ marginTop: '1rem' }}>
        <button type="button" onClick={saveDraft}>
          Save Draft
        </button>{' '}
        <button
          type="button"
          disabled={busy}
          onClick={async () => {
            if (busy) return
            onError('')
            setMsg('')
            if (!campaignId) {
              onError('Save a Draft first')
              return
            }
            setBusy(true)
            try {
              const addresses = testAddresses
                .split(/[\n,;]+/)
                .map((s) => s.trim())
                .filter(Boolean)
              const res = await fetch(`/api/campaigns/${campaignId}/test-send`, {
                method: 'POST',
                headers: authHeaders(token),
                body: JSON.stringify({ addresses }),
              })
              if (!res.ok) {
                onError(`Test send failed (${res.status}): ${await res.text()}`)
                return
              }
              const body = await res.json()
              setMsg(`Test send OK (${body.sent})`)
            } catch (e) {
              onError(e.message || String(e))
            } finally {
              setBusy(false)
            }
          }}
        >
          Test send
        </button>{' '}
        <button type="button" onClick={queueCampaign}>
          Queue
        </button>
      </p>
    </section>
  )
}

function OpsLogs({ token, onError }) {
  const [rows, setRows] = useState([])
  const [detail, setDetail] = useState(null)

  useEffect(() => {
    fetch('/api/ops-logs', { headers: { Authorization: `Bearer ${token}` } })
      .then(async (r) => {
        if (!r.ok) throw new Error(`${r.status}`)
        return r.json()
      })
      .then(setRows)
      .catch(() => setRows([]))
  }, [token])

  async function openRow(id) {
    onError('')
    const res = await fetch(`/api/ops-logs/${id}`, {
      headers: { Authorization: `Bearer ${token}` },
    })
    if (!res.ok) {
      onError(`ops ${res.status}`)
      return
    }
    setDetail(await res.json())
  }

  return (
    <section>
      <h2>Ops Logs</h2>
      <p style={{ color: '#555' }}>Index — open a row for detail (C8).</p>
      <ul>
        {rows.map((r) => (
          <li key={r.id}>
            <button type="button" onClick={() => openRow(r.id)}>
              [{r.severity}] {r.category}
            </button>{' '}
            {r.message?.slice(0, 80)}
          </li>
        ))}
      </ul>
      {detail ? (
        <div style={{ borderTop: '1px solid #ddd', marginTop: '1rem', paddingTop: '1rem' }}>
          <h3>{detail.category}</h3>
          <p>{detail.message}</p>
          <p style={{ fontSize: '0.85rem' }}>{detail.createdAt}</p>
          <button type="button" onClick={() => setDetail(null)}>
            Close
          </button>
        </div>
      ) : null}
    </section>
  )
}

function MtsInboxes({ token, onError }) {
  const [inboxes, setInboxes] = useState([])
  const [mappings, setMappings] = useState([])
  const [detail, setDetail] = useState(null)

  function load() {
    fetch('/api/mts/inboxes', { headers: { Authorization: `Bearer ${token}` } })
      .then(async (r) => {
        if (!r.ok) throw new Error(`${r.status}`)
        return r.json()
      })
      .then(setInboxes)
      .catch(() => setInboxes([]))
    fetch('/api/mts/mappings', { headers: { Authorization: `Bearer ${token}` } })
      .then(async (r) => {
        if (!r.ok) throw new Error(`${r.status}`)
        return r.json()
      })
      .then(setMappings)
      .catch(() => setMappings([]))
  }

  useEffect(() => {
    load()
  }, [token])

  async function openInbox(id) {
    onError('')
    const res = await fetch(`/api/mts/inboxes/${id}`, {
      headers: { Authorization: `Bearer ${token}` },
    })
    if (!res.ok) {
      onError(`inbox ${res.status}`)
      return
    }
    setDetail(await res.json())
  }

  return (
    <section>
      <h2>Inboxes &amp; mappings</h2>
      <p style={{ color: '#555' }}>Index only — open an inbox for senders and mappings (C8).</p>
      <ul>
        {inboxes.map((i) => (
          <li key={i.id}>
            <button type="button" onClick={() => openInbox(i.id)}>
              {i.name}
            </button>{' '}
            {i.enabled ? '' : '(disabled)'} poll {i.pollIntervalMinutes}m
          </li>
        ))}
      </ul>
      <h3>All mappings</h3>
      <ul>
        {mappings.map((m) => (
          <li key={m.id}>
            {m.name} → lists {m.listIds?.join(', ')}{' '}
            <button type="button" onClick={() => openInbox(m.inboxId)}>
              inbox
            </button>
          </li>
        ))}
      </ul>
      {detail ? (
        <div style={{ marginTop: '1rem', borderTop: '1px solid #ddd', paddingTop: '1rem' }}>
          <h3>{detail.name}</h3>
          <p>
            {detail.host}:{detail.port} user {detail.username}
          </p>
          <p>Authorized: {detail.authorizedSenders?.join(', ') || '—'}</p>
          <p>Mappings: {detail.mappings?.map((m) => m.name).join(', ') || '—'}</p>
          <button type="button" onClick={() => setDetail(null)}>
            Close detail
          </button>
        </div>
      ) : null}
    </section>
  )
}

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
      headers: authHeaders(token),
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
    <main style={{ fontFamily: 'system-ui', padding: '2rem', maxWidth: 900 }}>
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
            <button type="button" onClick={() => setView('compose')}>
              Compose
            </button>{' '}
            <button type="button" onClick={() => setView('parameters')}>
              Parameters
            </button>{' '}
            <button type="button" onClick={() => setView('mts')}>
              Inboxes &amp; mappings
            </button>{' '}
            <button type="button" onClick={() => setView('ops')}>
              Ops Logs
            </button>
          </p>
          {error ? <p style={{ color: 'crimson' }}>{error}</p> : null}
          {view === 'home' ? (
            <p>
              <code>/api/ping</code>: <strong>{ping}</strong>
            </p>
          ) : null}
          {view === 'compose' ? <Compose token={token} onError={setError} /> : null}
          {view === 'mts' ? <MtsInboxes token={token} onError={setError} /> : null}
          {view === 'ops' ? <OpsLogs token={token} onError={setError} /> : null}
          {view === 'parameters' ? (
            <section>
              <h2>Parameters</h2>
              {params.map((p) => (
                <div
                  key={p.key}
                  style={{
                    marginBottom: '1.25rem',
                    borderBottom: '1px solid #ddd',
                    paddingBottom: '1rem',
                  }}
                >
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
          ) : null}
        </>
      )}
    </main>
  )
}

export default App
