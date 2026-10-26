import { useEffect, useState } from 'react'
import { apiJson } from './api.js'

export function MtsPage({ token, onError, detailId, onOpen, onCloseDetail }) {
  const [inboxes, setInboxes] = useState([])
  const [mappings, setMappings] = useState([])
  const [detail, setDetail] = useState(null)
  const [lists, setLists] = useState([])
  const [footers, setFooters] = useState([])
  const [senderEmail, setSenderEmail] = useState('')
  const [mapForm, setMapForm] = useState({
    name: '',
    listIds: [],
    substituteFrom: '',
    footerId: '',
  })
  const [inboxForm, setInboxForm] = useState({
    name: '',
    host: '127.0.0.1',
    port: 143,
    username: '',
    password: '',
    useSsl: false,
    pollIntervalMinutes: 5,
    enabled: true,
  })

  async function load() {
    try {
      setInboxes(await apiJson(token, '/api/mts/inboxes'))
      setMappings(await apiJson(token, '/api/mts/mappings'))
    } catch (e) {
      onError(e.message)
    }
  }

  useEffect(() => {
    load()
    apiJson(token, '/api/lists')
      .then(setLists)
      .catch(() => setLists([]))
    apiJson(token, '/api/footers')
      .then(setFooters)
      .catch(() => setFooters([]))
  }, [token])

  useEffect(() => {
    if (!detailId) {
      setDetail(null)
      return
    }
    apiJson(token, `/api/mts/inboxes/${detailId}`)
      .then(setDetail)
      .catch((e) => onError(e.message))
  }, [token, detailId])

  async function createInbox(e) {
    e.preventDefault()
    try {
      const created = await apiJson(token, '/api/mts/inboxes', {
        method: 'POST',
        body: JSON.stringify(inboxForm),
      })
      await load()
      onOpen(created.id)
    } catch (err) {
      onError(err.message)
    }
  }

  async function addSender(e) {
    e.preventDefault()
    try {
      setDetail(
        await apiJson(token, `/api/mts/inboxes/${detailId}/senders`, {
          method: 'POST',
          body: JSON.stringify({ email: senderEmail }),
        }),
      )
      setSenderEmail('')
    } catch (err) {
      onError(err.message)
    }
  }

  async function removeSender(email) {
    try {
      await apiJson(token, `/api/mts/inboxes/${detailId}/senders/${encodeURIComponent(email)}`, {
        method: 'DELETE',
      })
      setDetail(await apiJson(token, `/api/mts/inboxes/${detailId}`))
    } catch (err) {
      onError(err.message)
    }
  }

  async function addMapping(e) {
    e.preventDefault()
    try {
      await apiJson(token, `/api/mts/inboxes/${detailId}/mappings`, {
        method: 'POST',
        body: JSON.stringify({
          name: mapForm.name,
          listIds: mapForm.listIds,
          substituteFrom: mapForm.substituteFrom || null,
          footerId: mapForm.footerId || null,
        }),
      })
      setMappings(await apiJson(token, '/api/mts/mappings'))
      setMapForm({ name: '', listIds: [], substituteFrom: '', footerId: '' })
    } catch (err) {
      onError(err.message)
    }
  }

  if (detailId && detail) {
    const inboxMaps = mappings.filter((m) => m.inboxId === detailId)
    return (
      <div className="card">
        <button type="button" className="btn secondary" onClick={onCloseDetail}>
          ← Inboxes & Mappings
        </button>
        <h2>{detail.name}</h2>
        <p className="hint">
          {detail.host}:{detail.port} · {detail.enabled ? 'enabled' : 'disabled'}
        </p>
        <h3>Authorized senders</h3>
        <ul>
          {(detail.authorizedSenders || []).map((email) => (
            <li key={email}>
              {email}{' '}
              <button type="button" className="btn secondary" onClick={() => removeSender(email)}>
                Remove
              </button>
            </li>
          ))}
        </ul>
        <form onSubmit={addSender}>
          <label className="field">
            sender email
            <input
              value={senderEmail}
              onChange={(ev) => setSenderEmail(ev.target.value)}
              required
            />
          </label>
          <button type="submit" className="btn secondary">
            Add sender
          </button>
        </form>

        <h3>Mappings</h3>
        <ul>
          {inboxMaps.map((m) => (
            <li key={m.id}>
              {m.name} → lists {(m.listIds || []).join(', ')}
              {m.substituteFrom ? ` · from ${m.substituteFrom}` : ''}
            </li>
          ))}
        </ul>
        <form onSubmit={addMapping}>
          <label className="field">
            mapping name
            <input
              value={mapForm.name}
              onChange={(ev) => setMapForm({ ...mapForm, name: ev.target.value })}
              required
            />
          </label>
          <fieldset>
            <legend>Lists</legend>
            {lists.map((l) => (
              <label key={l.id} style={{ display: 'block' }}>
                <input
                  type="checkbox"
                  checked={mapForm.listIds.includes(l.id)}
                  onChange={() =>
                    setMapForm((f) => ({
                      ...f,
                      listIds: f.listIds.includes(l.id)
                        ? f.listIds.filter((x) => x !== l.id)
                        : [...f.listIds, l.id],
                    }))
                  }
                />{' '}
                {l.name}
              </label>
            ))}
          </fieldset>
          <label className="field">
            substitute From email
            <input
              value={mapForm.substituteFrom}
              onChange={(ev) => setMapForm({ ...mapForm, substituteFrom: ev.target.value })}
            />
          </label>
          <label className="field">
            footer override
            <select
              value={mapForm.footerId}
              onChange={(ev) => setMapForm({ ...mapForm, footerId: ev.target.value })}
            >
              <option value="">— none —</option>
              {footers.map((f) => (
                <option key={f.id} value={f.id}>
                  {f.name}
                </option>
              ))}
            </select>
          </label>
          <button type="submit" className="btn">
            Add mapping
          </button>
        </form>
      </div>
    )
  }

  return (
    <div className="card">
      <h2>Inboxes &amp; Mappings</h2>
      <ul>
        {inboxes.map((i) => (
          <li key={i.id}>
            <button type="button" className="btn secondary" onClick={() => onOpen(i.id)}>
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
            {m.name}{' '}
            <button type="button" className="btn secondary" onClick={() => onOpen(m.inboxId)}>
              inbox
            </button>
          </li>
        ))}
      </ul>
      <h3>Create inbox</h3>
      <form onSubmit={createInbox}>
        {['name', 'host', 'username', 'password'].map((k) => (
          <label className="field" key={k}>
            {k}
            <input
              type={k === 'password' ? 'password' : 'text'}
              value={inboxForm[k]}
              onChange={(ev) => setInboxForm({ ...inboxForm, [k]: ev.target.value })}
              required
            />
          </label>
        ))}
        <button type="submit" className="btn">
          Create
        </button>
      </form>
    </div>
  )
}

export function IssuesPage({ token, onError }) {
  const [rows, setRows] = useState([])

  async function load() {
    try {
      setRows(await apiJson(token, '/api/mts/from-domain-issues'))
    } catch (e) {
      onError(e.message)
    }
  }

  useEffect(() => {
    load()
  }, [token])

  async function clear(id) {
    try {
      await apiJson(token, `/api/mts/from-domain-issues/${id}/clear`, { method: 'POST' })
      await load()
    } catch (e) {
      onError(e.message)
    }
  }

  return (
    <div className="card">
      <h2>From-domain Issues</h2>
      <p className="hint">
        Held mail for From-domain policy — not Drop log entries. Clearing is not the same as resolving
        a drop.
      </p>
      <ul>
        {rows.map((r) => (
          <li key={r.id}>
            <strong>{r.subject || '(no subject)'}</strong> from {r.senderEmail} · {r.status}
            <p className="hint">{r.detail}</p>
            {r.status !== 'CLEARED' ? (
              <button type="button" className="btn secondary" onClick={() => clear(r.id)}>
                Clear issue
              </button>
            ) : null}
          </li>
        ))}
      </ul>
      {!rows.length ? <p className="hint">No open From-domain issues.</p> : null}
    </div>
  )
}

export function DropLogPage({ token, onError, inboxFilter, onFilterInbox }) {
  const [rows, setRows] = useState([])

  async function load() {
    try {
      const q = inboxFilter ? `?inboxId=${encodeURIComponent(inboxFilter)}` : ''
      setRows(await apiJson(token, `/api/mts/drop-log${q}`))
    } catch (e) {
      onError(e.message)
    }
  }

  useEffect(() => {
    load()
  }, [token, inboxFilter])

  async function resolve(id) {
    try {
      await apiJson(token, `/api/mts/drop-log/${id}/resolve`, { method: 'POST' })
      await load()
    } catch (e) {
      onError(e.message)
    }
  }

  async function unblock(id) {
    try {
      await apiJson(token, `/api/mts/blocks/${id}`, { method: 'DELETE' })
      await load()
    } catch (e) {
      onError(e.message)
    }
  }

  return (
    <div className="card">
      <h2>Drop Log</h2>
      <p className="hint">Auth/spam/policy drops — distinct from From-domain Issues.</p>
      {inboxFilter ? (
        <p>
          Filtered inbox <code>{inboxFilter}</code>{' '}
          <button type="button" className="btn secondary" onClick={() => onFilterInbox?.(null)}>
            Clear filter
          </button>
        </p>
      ) : null}
      <ul>
        {rows.map((r) => (
          <li key={r.id}>
            {r.senderEmail} · {r.reason}
            {r.countedForAutoBlock ? ' · auto-block counted' : ''}
            {r.resolved ? ' · resolved' : ''}{' '}
            <button
              type="button"
              className="btn secondary"
              onClick={() => onFilterInbox?.(r.inboxId)}
            >
              inbox
            </button>{' '}
            {!r.resolved ? (
              <button type="button" className="btn secondary" onClick={() => resolve(r.id)}>
                Resolve
              </button>
            ) : null}
          </li>
        ))}
      </ul>
    </div>
  )
}

export function GlobalBlocksPage({ token, onError }) {
  const [blocks, setBlocks] = useState([])
  const [blockEmail, setBlockEmail] = useState('')
  const [blockDomain, setBlockDomain] = useState('')

  async function load() {
    try {
      setBlocks(await apiJson(token, '/api/mts/blocks'))
    } catch (e) {
      onError(e.message)
    }
  }

  useEffect(() => {
    load()
  }, [token])

  async function unblock(id) {
    try {
      await apiJson(token, `/api/mts/blocks/${id}`, { method: 'DELETE' })
      await load()
    } catch (e) {
      onError(e.message)
    }
  }

  return (
    <div className="card">
      <h2>Global Blocks</h2>
      <ul>
        {blocks.map((b) => (
          <li key={b.id}>
            {b.kind}: {b.value} <span className="hint">({b.source})</span>{' '}
            <button type="button" className="btn secondary" onClick={() => unblock(b.id)}>
              Unblock
            </button>
          </li>
        ))}
      </ul>
      <form
        onSubmit={async (e) => {
          e.preventDefault()
          try {
            await apiJson(token, '/api/mts/blocks/sender', {
              method: 'POST',
              body: JSON.stringify({ email: blockEmail }),
            })
            setBlockEmail('')
            await load()
          } catch (err) {
            onError(err.message)
          }
        }}
      >
        <label className="field">
          Block sender
          <input value={blockEmail} onChange={(ev) => setBlockEmail(ev.target.value)} required />
        </label>
        <button type="submit" className="btn secondary">
          Block sender
        </button>
      </form>
      <form
        onSubmit={async (e) => {
          e.preventDefault()
          try {
            await apiJson(token, '/api/mts/blocks/domain', {
              method: 'POST',
              body: JSON.stringify({ domain: blockDomain }),
            })
            setBlockDomain('')
            await load()
          } catch (err) {
            onError(err.message)
          }
        }}
      >
        <label className="field">
          Block domain
          <input value={blockDomain} onChange={(ev) => setBlockDomain(ev.target.value)} required />
        </label>
        <button type="submit" className="btn secondary">
          Block domain
        </button>
      </form>
    </div>
  )
}

export function BounceLogPage() {
  return (
    <div className="card">
      <h2>Bounce Log</h2>
      <p className="hint">Correlation index ships with bounce processing ops; mailbox config is under Bounce Mailboxes.</p>
    </div>
  )
}
