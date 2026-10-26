import { useEffect, useState } from 'react'
import { apiJson } from './api.js'
import { authHeaders } from './auth.js'
import {
  BounceMailboxesPage,
  FootersPage,
  NoticePathPage,
  ParametersPage,
  SendingPathsPage,
} from './CatalogPages.jsx'
import { CampaignsPage, StatusBadge } from './CampaignPages.jsx'
import { ListsPage, MembersPage } from './ListsMembersPages.jsx'
import {
  BounceLogPage,
  DropLogPage,
  GlobalBlocksPage,
  IssuesPage,
  MtsPage,
} from './OpsMtsPages.jsx'
import Placeholder from './Placeholder.jsx'
import { UsersPage } from './UsersPages.jsx'

export function Compose({ token, onError }) {
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
    return { subject, htmlBody, textBody, listIds: selected, attachments }
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
        onError(`Queue failed (${res.status}): ${await res.text()}`)
        return
      }
      const body = await res.json()
      setStatus(body.status)
      setMsg(`Queued (${body.status}); sent so far ${body.sentCount}`)
    } catch (e) {
      onError(e.message || String(e))
    }
  }

  return (
    <div className="card">
      <h2>Compose</h2>
      {msg ? <p>{msg}</p> : null}
      {status ? (
        <p>
          Campaign <code>{campaignId}</code> — <StatusBadge status={status} />
        </p>
      ) : null}
      <label className="field">
        Subject
        <input value={subject} onChange={(e) => setSubject(e.target.value)} />
      </label>
      <fieldset>
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
                <button type="button" className="btn secondary" onClick={() => moveList(l.id, -1)}>
                  ↑
                </button>
                <button type="button" className="btn secondary" onClick={() => moveList(l.id, 1)}>
                  ↓
                </button>
              </>
            ) : null}
          </div>
        ))}
      </fieldset>
      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem', marginTop: '1rem' }}>
        <label className="field">
          HTML
          <textarea
            value={htmlBody}
            onChange={(e) => setHtmlBody(e.target.value)}
            rows={10}
            style={{ fontFamily: 'var(--mono)', maxWidth: '100%' }}
          />
        </label>
        <label className="field">
          Text
          <textarea
            value={textBody}
            onChange={(e) => setTextBody(e.target.value)}
            rows={10}
            style={{ fontFamily: 'var(--mono)', maxWidth: '100%' }}
          />
        </label>
      </div>
      <p>
        Preview:{' '}
        <button type="button" className="btn secondary" onClick={() => setPreview('html')}>
          HTML
        </button>{' '}
        <button type="button" className="btn secondary" onClick={() => setPreview('text')}>
          Text
        </button>
      </p>
      <div className="card" style={{ background: '#f8fafc' }}>
        {preview === 'html' ? (
          <div dangerouslySetInnerHTML={{ __html: htmlBody }} />
        ) : (
          <pre style={{ whiteSpace: 'pre-wrap', margin: 0 }}>{textBody}</pre>
        )}
      </div>
      <label className="field">
        Attachment
        <input type="file" onChange={(e) => setAttachment(e.target.files?.[0] || null)} />
      </label>
      <label className="field">
        Test send addresses (comma or newline)
        <textarea
          value={testAddresses}
          onChange={(e) => setTestAddresses(e.target.value)}
          rows={3}
          style={{ maxWidth: '100%' }}
        />
      </label>
      <p className="hint">Test send ignores bounce-disable.</p>
      <p>
        <button type="button" className="btn" onClick={saveDraft}>
          Save Draft
        </button>{' '}
        <button
          type="button"
          className="btn secondary"
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
        <button type="button" className="btn" onClick={queueCampaign}>
          Queue
        </button>
      </p>
    </div>
  )
}

export function AuditLog({ token }) {
  const [rows, setRows] = useState([])
  useEffect(() => {
    fetch('/api/audit', { headers: { Authorization: `Bearer ${token}` } })
      .then(async (r) => {
        if (!r.ok) throw new Error(`${r.status}`)
        return r.json()
      })
      .then(setRows)
      .catch(() => setRows([]))
  }, [token])
  return (
    <div className="card">
      <h2>Audit</h2>
      <p className="hint">Append-only history (read-only).</p>
      <ul>
        {rows.map((r) => (
          <li key={r.id}>
            <code>{r.action}</code> {r.detail}{' '}
            <span className="hint">{r.actor}</span>
          </li>
        ))}
      </ul>
    </div>
  )
}

export function AttentionInbox({ token, onError }) {
  const [rows, setRows] = useState([])
  const [forbidden, setForbidden] = useState(false)

  async function load() {
    try {
      const res = await fetch('/api/attention', { headers: { Authorization: `Bearer ${token}` } })
      if (res.status === 403) {
        setForbidden(true)
        setRows([])
        return
      }
      if (!res.ok) throw new Error(`${res.status}`)
      setRows(await res.json())
    } catch {
      setRows([])
    }
  }

  useEffect(() => {
    load()
  }, [token])

  async function ack(id) {
    try {
      await apiJson(token, `/api/attention/${id}/ack`, { method: 'POST' })
      await load()
    } catch (e) {
      onError?.(e.message)
    }
  }

  if (forbidden) {
    return (
      <div className="card">
        <h2>Attention</h2>
        <p>Not available for this role (List senders have no Attention inbox).</p>
      </div>
    )
  }
  return (
    <div className="card">
      <h2>Attention</h2>
      <ul style={{ listStyle: 'none', padding: 0 }}>
        {rows.map((r) => (
          <li
            key={r.id}
            style={{
              background: r.opsHighlight ? '#fffbeb' : undefined,
              borderLeft: r.opsHighlight ? '4px solid var(--warning)' : undefined,
              padding: '0.35rem 0.5rem',
              marginBottom: '0.35rem',
            }}
          >
            <strong>{r.title}</strong> — {r.detail}{' '}
            <button type="button" className="btn secondary" onClick={() => ack(r.id)}>
              Ack
            </button>
          </li>
        ))}
      </ul>
    </div>
  )
}

export function OpsLogs({ token, onError }) {
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
    <div className="card">
      <h2>Ops Logs</h2>
      <p className="hint">Index — open a row for detail (C8).</p>
      <ul>
        {rows.map((r) => (
          <li key={r.id}>
            <button type="button" className="btn secondary" onClick={() => openRow(r.id)}>
              [{r.severity}] {r.category}
            </button>{' '}
            {r.message?.slice(0, 80)}
          </li>
        ))}
      </ul>
      {detail ? (
        <div className="card">
          <h2>{detail.category}</h2>
          <p>{detail.message}</p>
          <p className="hint">{detail.createdAt}</p>
          <button type="button" className="btn secondary" onClick={() => setDetail(null)}>
            Close
          </button>
        </div>
      ) : null}
    </div>
  )
}

export function MainView({
  view,
  token,
  onError,
  detailId,
  onOpenDetail,
  onCloseDetail,
  openInView,
}) {
  switch (view) {
    case 'compose':
      return <Compose token={token} onError={onError} />
    case 'campaigns':
      return (
        <CampaignsPage
          token={token}
          onError={onError}
          detailId={detailId}
          onOpen={onOpenDetail}
          onCloseDetail={onCloseDetail}
        />
      )
    case 'mts':
      return (
        <MtsPage
          token={token}
          onError={onError}
          detailId={detailId}
          onOpen={onOpenDetail}
          onCloseDetail={onCloseDetail}
        />
      )
    case 'issues':
      return <IssuesPage token={token} onError={onError} />
    case 'drop-log':
      return (
        <DropLogPage
          token={token}
          onError={onError}
          inboxFilter={detailId}
          onFilterInbox={onOpenDetail}
        />
      )
    case 'blocks':
      return <GlobalBlocksPage token={token} onError={onError} />
    case 'bounces':
      return <BounceLogPage />
    case 'ops':
      return <OpsLogs token={token} onError={onError} />
    case 'attention':
      return <AttentionInbox token={token} onError={onError} />
    case 'audit':
      return <AuditLog token={token} />
    case 'parameters':
      return <ParametersPage token={token} onError={onError} />
    case 'lists':
      return (
        <ListsPage
          token={token}
          onError={onError}
          detailId={detailId}
          onOpen={onOpenDetail}
          onCloseDetail={onCloseDetail}
          onOpenMember={(id) => openInView?.('members', id)}
        />
      )
    case 'members':
      return (
        <MembersPage
          token={token}
          onError={onError}
          detailId={detailId}
          onOpen={onOpenDetail}
          onCloseDetail={onCloseDetail}
        />
      )
    case 'sending-paths':
      return (
        <SendingPathsPage
          token={token}
          onError={onError}
          detailId={detailId}
          onOpen={onOpenDetail}
          onCloseDetail={onCloseDetail}
        />
      )
    case 'footers':
      return (
        <FootersPage
          token={token}
          onError={onError}
          detailId={detailId}
          onOpen={onOpenDetail}
          onCloseDetail={onCloseDetail}
        />
      )
    case 'bounce-mailboxes':
      return (
        <BounceMailboxesPage
          token={token}
          onError={onError}
          detailId={detailId}
          onOpen={onOpenDetail}
          onCloseDetail={onCloseDetail}
        />
      )
    case 'notice-path':
      return <NoticePathPage token={token} onError={onError} />
    case 'users':
      return (
        <UsersPage
          token={token}
          onError={onError}
          detailId={detailId}
          onOpen={onOpenDetail}
          onCloseDetail={onCloseDetail}
        />
      )
    case 'home':
      return (
        <Placeholder
          title="Home"
          hint="Use the sidebar for indexes and tools. Detail pages open from rows only (C8)."
        />
      )
    default:
      return <Placeholder title={view} />
  }
}
