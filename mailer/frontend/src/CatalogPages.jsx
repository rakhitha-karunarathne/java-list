import { useEffect, useState } from 'react'
import { apiJson } from './api.js'

function UsedByList({ usedBy }) {
  if (!usedBy?.length) return <p className="hint">Not referenced.</p>
  return (
    <ul>
      {usedBy.map((u) => (
        <li key={`${u.sourceType}-${u.sourceId}`}>
          {u.label} <span className="hint">({u.sourceType})</span>
        </li>
      ))}
    </ul>
  )
}

function DeleteBlocked({ err, onClose }) {
  if (!err) return null
  const used = err.body?.usedBy
  return (
    <div className="card" style={{ borderColor: 'var(--danger)' }}>
      <p className="error">{err.message || 'Delete blocked'}</p>
      {used ? <UsedByList usedBy={used} /> : null}
      <button type="button" className="btn secondary" onClick={onClose}>
        OK
      </button>
    </div>
  )
}

export function SendingPathsPage({ token, onError, detailId, onOpen, onCloseDetail }) {
  const [rows, setRows] = useState([])
  const [detail, setDetail] = useState(null)
  const [form, setForm] = useState({
    name: '',
    host: '127.0.0.1',
    port: 25,
    username: '',
    password: '',
    fromEmail: '',
    fromName: '',
    useTls: false,
  })
  const [delErr, setDelErr] = useState(null)

  async function load() {
    try {
      setRows(await apiJson(token, '/api/sending-paths'))
    } catch (e) {
      onError(e.message)
    }
  }

  useEffect(() => {
    load()
  }, [token])

  useEffect(() => {
    if (!detailId) {
      setDetail(null)
      return
    }
    apiJson(token, `/api/sending-paths/${detailId}`)
      .then((d) => {
        setDetail(d)
        setForm({
          name: d.name,
          host: d.host,
          port: d.port,
          username: d.username,
          password: '',
          fromEmail: d.fromEmail,
          fromName: d.fromName,
          useTls: d.useTls,
        })
      })
      .catch((e) => onError(e.message))
  }, [token, detailId])

  async function create(e) {
    e.preventDefault()
    onError('')
    try {
      const created = await apiJson(token, '/api/sending-paths', {
        method: 'POST',
        body: JSON.stringify(form),
      })
      await load()
      onOpen(created.id)
    } catch (err) {
      onError(err.message)
    }
  }

  async function save(e) {
    e.preventDefault()
    onError('')
    try {
      const body = { ...form }
      if (!body.password) body.password = null
      await apiJson(token, `/api/sending-paths/${detailId}`, {
        method: 'PUT',
        body: JSON.stringify(body),
      })
      const d = await apiJson(token, `/api/sending-paths/${detailId}`)
      setDetail(d)
    } catch (err) {
      onError(err.message)
    }
  }

  async function remove() {
    onError('')
    setDelErr(null)
    try {
      const res = await fetch(`/api/sending-paths/${detailId}`, {
        method: 'DELETE',
        headers: { Authorization: `Bearer ${token}` },
      })
      const text = await res.text()
      let body = null
      try {
        body = text ? JSON.parse(text) : null
      } catch {
        body = text
      }
      if (!res.ok) {
        setDelErr({ message: body?.message || `Delete failed (${res.status})`, body })
        return
      }
      onCloseDetail()
      load()
    } catch (err) {
      onError(err.message)
    }
  }

  if (detailId && detail) {
    return (
      <div className="card">
        <p className="hint">
          <button type="button" className="btn secondary" onClick={onCloseDetail}>
            ← Sending Paths
          </button>
        </p>
        <h2>{detail.name}</h2>
        <form onSubmit={save}>
          {['name', 'host', 'username', 'fromEmail', 'fromName'].map((k) => (
            <label className="field" key={k}>
              {k}
              <input
                value={form[k]}
                onChange={(ev) => setForm({ ...form, [k]: ev.target.value })}
                required={k !== 'fromName'}
              />
            </label>
          ))}
          <label className="field">
            port
            <input
              type="number"
              value={form.port}
              onChange={(ev) => setForm({ ...form, port: Number(ev.target.value) })}
            />
          </label>
          <label className="field">
            password (blank = keep)
            <input
              type="password"
              value={form.password}
              onChange={(ev) => setForm({ ...form, password: ev.target.value })}
              autoComplete="new-password"
            />
          </label>
          <label className="field">
            <input
              type="checkbox"
              checked={form.useTls}
              onChange={(ev) => setForm({ ...form, useTls: ev.target.checked })}
            />{' '}
            useTls
          </label>
          <p>
            <button type="submit" className="btn">
              Save
            </button>{' '}
            <button type="button" className="btn secondary" onClick={remove}>
              Delete
            </button>
          </p>
        </form>
        <h3>Used By</h3>
        <UsedByList usedBy={detail.usedBy} />
        <DeleteBlocked err={delErr} onClose={() => setDelErr(null)} />
      </div>
    )
  }

  return (
    <div className="card">
      <h2>Sending Paths</h2>
      <ul>
        {rows.map((r) => (
          <li key={r.id}>
            <button type="button" className="btn secondary" onClick={() => onOpen(r.id)}>
              {r.name}
            </button>{' '}
            {r.host}:{r.port} · {r.fromEmail}
          </li>
        ))}
      </ul>
      <h3>Create</h3>
      <form onSubmit={create}>
        {['name', 'host', 'username', 'password', 'fromEmail', 'fromName'].map((k) => (
          <label className="field" key={k}>
            {k}
            <input
              type={k === 'password' ? 'password' : 'text'}
              value={form[k]}
              onChange={(ev) => setForm({ ...form, [k]: ev.target.value })}
              required={k !== 'fromName'}
            />
          </label>
        ))}
        <label className="field">
          port
          <input
            type="number"
            value={form.port}
            onChange={(ev) => setForm({ ...form, port: Number(ev.target.value) })}
          />
        </label>
        <button type="submit" className="btn">
          Create
        </button>
      </form>
    </div>
  )
}

export function FootersPage({ token, onError, detailId, onOpen, onCloseDetail }) {
  const [rows, setRows] = useState([])
  const [detail, setDetail] = useState(null)
  const [form, setForm] = useState({ name: '', htmlBody: '', textBody: '' })
  const [delErr, setDelErr] = useState(null)

  async function load() {
    try {
      setRows(await apiJson(token, '/api/footers'))
    } catch (e) {
      onError(e.message)
    }
  }

  useEffect(() => {
    load()
  }, [token])

  useEffect(() => {
    if (!detailId) {
      setDetail(null)
      return
    }
    apiJson(token, `/api/footers/${detailId}`)
      .then((d) => {
        setDetail(d)
        setForm({ name: d.name, htmlBody: d.htmlBody || '', textBody: d.textBody || '' })
      })
      .catch((e) => onError(e.message))
  }, [token, detailId])

  async function create(e) {
    e.preventDefault()
    try {
      const created = await apiJson(token, '/api/footers', {
        method: 'POST',
        body: JSON.stringify(form),
      })
      await load()
      onOpen(created.id)
    } catch (err) {
      onError(err.message)
    }
  }

  async function save(e) {
    e.preventDefault()
    try {
      await apiJson(token, `/api/footers/${detailId}`, {
        method: 'PUT',
        body: JSON.stringify(form),
      })
      setDetail(await apiJson(token, `/api/footers/${detailId}`))
    } catch (err) {
      onError(err.message)
    }
  }

  async function remove() {
    setDelErr(null)
    const res = await fetch(`/api/footers/${detailId}`, {
      method: 'DELETE',
      headers: { Authorization: `Bearer ${token}` },
    })
    const text = await res.text()
    let body = null
    try {
      body = text ? JSON.parse(text) : null
    } catch {
      body = text
    }
    if (!res.ok) {
      setDelErr({ message: body?.message || `Delete failed (${res.status})`, body })
      return
    }
    onCloseDetail()
    load()
  }

  if (detailId && detail) {
    return (
      <div className="card">
        <button type="button" className="btn secondary" onClick={onCloseDetail}>
          ← Footers
        </button>
        <h2>{detail.name}</h2>
        <form onSubmit={save}>
          <label className="field">
            name
            <input value={form.name} onChange={(ev) => setForm({ ...form, name: ev.target.value })} />
          </label>
          <label className="field">
            htmlBody
            <textarea
              rows={6}
              style={{ maxWidth: '100%' }}
              value={form.htmlBody}
              onChange={(ev) => setForm({ ...form, htmlBody: ev.target.value })}
            />
          </label>
          <label className="field">
            textBody
            <textarea
              rows={4}
              style={{ maxWidth: '100%' }}
              value={form.textBody}
              onChange={(ev) => setForm({ ...form, textBody: ev.target.value })}
            />
          </label>
          <p>
            <button type="submit" className="btn">
              Save
            </button>{' '}
            <button type="button" className="btn secondary" onClick={remove}>
              Delete
            </button>
          </p>
        </form>
        <h3>Used By</h3>
        <UsedByList usedBy={detail.usedBy} />
        <DeleteBlocked err={delErr} onClose={() => setDelErr(null)} />
      </div>
    )
  }

  return (
    <div className="card">
      <h2>Footers</h2>
      <ul>
        {rows.map((r) => (
          <li key={r.id}>
            <button type="button" className="btn secondary" onClick={() => onOpen(r.id)}>
              {r.name}
            </button>
          </li>
        ))}
      </ul>
      <h3>Create</h3>
      <form onSubmit={create}>
        <label className="field">
          name
          <input value={form.name} onChange={(ev) => setForm({ ...form, name: ev.target.value })} required />
        </label>
        <label className="field">
          htmlBody
          <textarea
            rows={4}
            style={{ maxWidth: '100%' }}
            value={form.htmlBody}
            onChange={(ev) => setForm({ ...form, htmlBody: ev.target.value })}
          />
        </label>
        <label className="field">
          textBody
          <textarea
            rows={3}
            style={{ maxWidth: '100%' }}
            value={form.textBody}
            onChange={(ev) => setForm({ ...form, textBody: ev.target.value })}
          />
        </label>
        <button type="submit" className="btn">
          Create
        </button>
      </form>
    </div>
  )
}
