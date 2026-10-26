import { useEffect, useState } from 'react'
import { apiJson } from './api.js'
import { DeleteBlocked, UsedByList } from './catalogUi.jsx'
import { CREATE_ID, isCreatePage } from './createPage.js'
import { fieldLabel } from './fieldLabel.js'
import { RecordTable } from './RecordTable.jsx'

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
    if (!detailId || isCreatePage(detailId)) {
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

  if (isCreatePage(detailId)) {
    return (
      <div className="card">
        <button type="button" className="btn secondary" onClick={onCloseDetail}>
          {'←'} Sending Paths
        </button>
        <h2>Create Sending Path</h2>
        <form onSubmit={create}>
          {['name', 'host', 'username', 'password', 'fromEmail', 'fromName'].map((k) => (
            <label className="field" key={k}>
              {fieldLabel(k)}
              <input
                type={k === 'password' ? 'password' : 'text'}
                value={form[k]}
                onChange={(ev) => setForm({ ...form, [k]: ev.target.value })}
                required={k !== 'fromName'}
              />
            </label>
          ))}
          <label className="field">
            {fieldLabel('port')}
            <input
              type="number"
              value={form.port}
              onChange={(ev) => setForm({ ...form, port: Number(ev.target.value) })}
            />
          </label>
          <button type="submit" className="btn">
            Create Sending Path
          </button>
        </form>
      </div>
    )
  }

  if (detailId && detail) {
    return (
      <div className="card">
        <button type="button" className="btn secondary" onClick={onCloseDetail}>
          {'←'} Sending Paths
        </button>
        <h2>{detail.name}</h2>
        <form onSubmit={save}>
          {['name', 'host', 'username', 'fromEmail', 'fromName'].map((k) => (
            <label className="field" key={k}>
              {fieldLabel(k)}
              <input
                value={form[k]}
                onChange={(ev) => setForm({ ...form, [k]: ev.target.value })}
                required={k !== 'fromName'}
              />
            </label>
          ))}
          <label className="field">
            {fieldLabel('port')}
            <input
              type="number"
              value={form.port}
              onChange={(ev) => setForm({ ...form, port: Number(ev.target.value) })}
            />
          </label>
          <label className="field">
            {fieldLabel('password')} (Blank = Keep)
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
            {fieldLabel('useTls')}
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
      <div className="index-actions">
        <h2>Sending Paths</h2>
        <button type="button" className="btn" onClick={() => onOpen(CREATE_ID)}>
          Create Sending Path
        </button>
      </div>
      <RecordTable
        rows={rows}
        rowKey={(r) => r.id}
        onOpen={(r) => onOpen(r.id)}
        empty="No sending paths yet."
        columns={[
          { key: 'name', label: 'Name' },
          {
            key: 'host',
            label: 'Host',
            render: (r) => `${r.host}:${r.port}`,
          },
          { key: 'fromEmail', label: 'From Email' },
        ]}
      />
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
    if (!detailId || isCreatePage(detailId)) {
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

  if (isCreatePage(detailId)) {
    return (
      <div className="card">
        <button type="button" className="btn secondary" onClick={onCloseDetail}>
          ← Footers
        </button>
        <h2>Create Footer</h2>
        <form onSubmit={create}>
          <label className="field">
            {fieldLabel('name')}
            <input
              value={form.name}
              onChange={(ev) => setForm({ ...form, name: ev.target.value })}
              required
            />
          </label>
          <label className="field">
            {fieldLabel('htmlBody')}
            <textarea
              rows={4}
              style={{ maxWidth: '100%' }}
              value={form.htmlBody}
              onChange={(ev) => setForm({ ...form, htmlBody: ev.target.value })}
            />
          </label>
          <label className="field">
            {fieldLabel('textBody')}
            <textarea
              rows={3}
              style={{ maxWidth: '100%' }}
              value={form.textBody}
              onChange={(ev) => setForm({ ...form, textBody: ev.target.value })}
            />
          </label>
          <button type="submit" className="btn">
            Create Footer
          </button>
        </form>
      </div>
    )
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
            {fieldLabel('name')}
            <input value={form.name} onChange={(ev) => setForm({ ...form, name: ev.target.value })} />
          </label>
          <label className="field">
            {fieldLabel('htmlBody')}
            <textarea
              rows={6}
              style={{ maxWidth: '100%' }}
              value={form.htmlBody}
              onChange={(ev) => setForm({ ...form, htmlBody: ev.target.value })}
            />
          </label>
          <label className="field">
            {fieldLabel('textBody')}
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
      <div className="index-actions">
        <h2>Footers</h2>
        <button type="button" className="btn" onClick={() => onOpen(CREATE_ID)}>
          Create Footer
        </button>
      </div>
      <RecordTable
        rows={rows}
        rowKey={(r) => r.id}
        onOpen={(r) => onOpen(r.id)}
        empty="No footers yet."
        columns={[{ key: 'name', label: 'Name' }]}
      />
    </div>
  )
}

export function BounceMailboxesPage({ token, onError, detailId, onOpen, onCloseDetail }) {
  const [rows, setRows] = useState([])
  const [detail, setDetail] = useState(null)
  const [form, setForm] = useState({
    name: '',
    host: '127.0.0.1',
    port: 143,
    username: '',
    password: '',
    useSsl: false,
    pollIntervalMinutes: 15,
  })

  async function load() {
    try {
      setRows(await apiJson(token, '/api/bounce-mailboxes'))
    } catch (e) {
      onError(e.message)
    }
  }

  useEffect(() => {
    load()
  }, [token])

  useEffect(() => {
    if (!detailId || isCreatePage(detailId)) {
      setDetail(null)
      return
    }
    apiJson(token, `/api/bounce-mailboxes/${detailId}`)
      .then((d) => {
        setDetail(d)
        setForm({
          name: d.name,
          host: d.host,
          port: d.port,
          username: d.username,
          password: '',
          useSsl: d.useSsl,
          pollIntervalMinutes: d.pollIntervalMinutes,
        })
      })
      .catch((e) => onError(e.message))
  }, [token, detailId])

  async function create(e) {
    e.preventDefault()
    try {
      const created = await apiJson(token, '/api/bounce-mailboxes', {
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
      const body = { ...form }
      if (!body.password) body.password = null
      await apiJson(token, `/api/bounce-mailboxes/${detailId}`, {
        method: 'PUT',
        body: JSON.stringify(body),
      })
      setDetail(await apiJson(token, `/api/bounce-mailboxes/${detailId}`))
      setForm((f) => ({ ...f, password: '' }))
    } catch (err) {
      onError(err.message)
    }
  }

  async function remove() {
    try {
      await apiJson(token, `/api/bounce-mailboxes/${detailId}`, { method: 'DELETE' })
      onCloseDetail()
      load()
    } catch (err) {
      onError(err.message)
    }
  }

  if (isCreatePage(detailId)) {
    return (
      <div className="card">
        <button type="button" className="btn secondary" onClick={onCloseDetail}>
          ← Bounce Mailboxes
        </button>
        <h2>Create Bounce Mailbox</h2>
        <form onSubmit={create}>
          {['name', 'host', 'username', 'password'].map((k) => (
            <label className="field" key={k}>
              {fieldLabel(k)}
              <input
                type={k === 'password' ? 'password' : 'text'}
                value={form[k]}
                onChange={(ev) => setForm({ ...form, [k]: ev.target.value })}
                required
              />
            </label>
          ))}
          <label className="field">
            {fieldLabel('port')}
            <input
              type="number"
              value={form.port}
              onChange={(ev) => setForm({ ...form, port: Number(ev.target.value) })}
            />
          </label>
          <label className="field">
            {fieldLabel('pollIntervalMinutes')}
            <input
              type="number"
              value={form.pollIntervalMinutes}
              onChange={(ev) => setForm({ ...form, pollIntervalMinutes: Number(ev.target.value) })}
            />
          </label>
          <label className="field">
            <input
              type="checkbox"
              checked={form.useSsl}
              onChange={(ev) => setForm({ ...form, useSsl: ev.target.checked })}
            />{' '}
            {fieldLabel('useSsl')}
          </label>
          <button type="submit" className="btn">
            Create Bounce Mailbox
          </button>
        </form>
      </div>
    )
  }

  if (detailId && detail) {
    return (
      <div className="card">
        <button type="button" className="btn secondary" onClick={onCloseDetail}>
          ← Bounce Mailboxes
        </button>
        <h2>{detail.name}</h2>
        <form onSubmit={save}>
          {['name', 'host', 'username'].map((k) => (
            <label className="field" key={k}>
              {fieldLabel(k)}
              <input value={form[k]} onChange={(ev) => setForm({ ...form, [k]: ev.target.value })} />
            </label>
          ))}
          <label className="field">
            {fieldLabel('password')} (Blank = Keep)
            <input
              type="password"
              value={form.password}
              onChange={(ev) => setForm({ ...form, password: ev.target.value })}
              autoComplete="new-password"
            />
          </label>
          <label className="field">
            {fieldLabel('port')}
            <input
              type="number"
              value={form.port}
              onChange={(ev) => setForm({ ...form, port: Number(ev.target.value) })}
            />
          </label>
          <label className="field">
            {fieldLabel('pollIntervalMinutes')}
            <input
              type="number"
              value={form.pollIntervalMinutes}
              onChange={(ev) => setForm({ ...form, pollIntervalMinutes: Number(ev.target.value) })}
            />
          </label>
          <label className="field">
            <input
              type="checkbox"
              checked={form.useSsl}
              onChange={(ev) => setForm({ ...form, useSsl: ev.target.checked })}
            />{' '}
            {fieldLabel('useSsl')}
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
      </div>
    )
  }

  return (
    <div className="card">
      <div className="index-actions">
        <h2>Bounce Mailboxes</h2>
        <button type="button" className="btn" onClick={() => onOpen(CREATE_ID)}>
          Create Bounce Mailbox
        </button>
      </div>
      <RecordTable
        rows={rows}
        rowKey={(r) => r.id}
        onOpen={(r) => onOpen(r.id)}
        empty="No bounce mailboxes yet."
        columns={[
          { key: 'name', label: 'Name' },
          { key: 'host', label: 'Host' },
          {
            key: 'pollIntervalMinutes',
            label: 'Poll Minutes',
            render: (r) => r.pollIntervalMinutes,
          },
        ]}
      />
    </div>
  )
}

export function ParametersPage({ token, onError }) {
  const [params, setParams] = useState([])
  const [drafts, setDrafts] = useState({})
  const [forbidden, setForbidden] = useState(false)

  useEffect(() => {
    apiJson(token, '/api/parameters')
      .then((rows) => {
        setParams(rows)
        setDrafts(Object.fromEntries(rows.map((p) => [p.key, p.value])))
        setForbidden(false)
      })
      .catch((e) => {
        if (e.status === 403) {
          setForbidden(true)
          setParams([])
          return
        }
        onError(e.message)
      })
  }, [token])

  async function save(key) {
    onError('')
    try {
      const updated = await apiJson(token, `/api/parameters/${encodeURIComponent(key)}`, {
        method: 'PUT',
        body: JSON.stringify({ value: drafts[key] ?? '' }),
      })
      setParams((prev) => prev.map((p) => (p.key === key ? updated : p)))
      setDrafts((d) => ({ ...d, [key]: updated.value }))
    } catch (e) {
      onError(e.message)
    }
  }

  if (forbidden) {
    return (
      <div className="card">
        <h2>Parameters</h2>
        <p>Not available for this role.</p>
      </div>
    )
  }

  return (
    <div className="card">
      <h2>Parameters</h2>
      <p className="hint">Admin runtime settings — no SMTP/IMAP passwords or signing secrets here.</p>
      {params.map((p) => (
        <div key={p.key} style={{ marginBottom: '1.5rem' }}>
          <label className="field">
            {fieldLabel(p.key)}
            <input
              value={drafts[p.key] ?? ''}
              onChange={(ev) => setDrafts((d) => ({ ...d, [p.key]: ev.target.value }))}
            />
          </label>
          <p className="hint" style={{ marginTop: 0 }}>
            {p.description}
          </p>
          <button type="button" className="btn secondary" onClick={() => save(p.key)}>
            Save
          </button>
        </div>
      ))}
    </div>
  )
}

export function NoticePathPage({ token, onError }) {
  const [paths, setPaths] = useState([])
  const [pathId, setPathId] = useState('')
  const [loaded, setLoaded] = useState(false)

  useEffect(() => {
    Promise.all([
      apiJson(token, '/api/sending-paths'),
      apiJson(token, '/api/notice-sending-path'),
    ])
      .then(([p, n]) => {
        setPaths(p)
        setPathId(n.sendingPathId || '')
        setLoaded(true)
      })
      .catch((e) => onError(e.message))
  }, [token])

  async function save(e) {
    e.preventDefault()
    try {
      const out = await apiJson(token, '/api/notice-sending-path', {
        method: 'PUT',
        body: JSON.stringify({ sendingPathId: pathId || null }),
      })
      setPathId(out.sendingPathId || '')
    } catch (err) {
      onError(err.message)
    }
  }

  return (
    <div className="card">
      <h2>Notice Sending Path</h2>
      <p className="hint">
        Used for system/consent/admin notices — not Campaign queue. Empty = notices are skipped
        until a path is set.
      </p>
      {!loaded ? <p className="hint">Loading…</p> : null}
      <form onSubmit={save}>
        <label className="field">
          Sending Path
          <select value={pathId} onChange={(ev) => setPathId(ev.target.value)}>
            <option value="">— Unset (No Notice Mail) —</option>
            {paths.map((p) => (
              <option key={p.id} value={p.id}>
                {p.name}
              </option>
            ))}
          </select>
        </label>
        {!pathId ? <p className="hint">Notice path is unset.</p> : null}
        <button type="submit" className="btn">
          Save
        </button>
      </form>
    </div>
  )
}
