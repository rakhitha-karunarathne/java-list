import { useEffect, useState } from 'react'
import { apiJson } from './api.js'
import {
  CAMPAIGN_STATUSES,
  campaignStatusLabel,
  canDeleteCampaign,
  canPauseCampaign,
  canResumeCampaign,
  canStopCampaign,
  filterCampaigns,
} from './campaignStatus.js'
import { confirmDelete } from './confirmDelete.js'
import { FieldDesc } from './FieldDesc.jsx'
import { fieldHelp } from './fieldHelp.js'
import { RecordTable } from './RecordTable.jsx'
import { useFormatWhen } from './TimeZone.jsx'

const BADGE = {
  DRAFT: { bg: '#e2e8f0', fg: '#0f172a' },
  QUEUED: { bg: '#dbeafe', fg: '#1e3a8a' },
  SENDING: { bg: '#fef3c7', fg: '#92400e' },
  PAUSED: { bg: '#fce7f3', fg: '#9d174d' },
  FINISHED: { bg: '#dcfce7', fg: '#14532d' },
  STOPPED: { bg: '#fee2e2', fg: '#7f1d1d' },
}

export function StatusBadge({ status }) {
  const s = (status || '').toUpperCase()
  const c = BADGE[s] || { bg: '#f1f5f9', fg: '#334155' }
  return (
    <span
      style={{
        display: 'inline-block',
        padding: '0.1rem 0.45rem',
        borderRadius: 4,
        background: c.bg,
        color: c.fg,
        fontSize: '0.85rem',
        fontWeight: 600,
      }}
    >
      {campaignStatusLabel(status)}
    </span>
  )
}

function formatBytes(n) {
  const bytes = Number(n) || 0
  if (bytes < 1024) return `${bytes} B`
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`
  return `${(bytes / (1024 * 1024)).toFixed(1)} MB`
}

export function CampaignsPage({ token, onError, detailId, onOpen, onCloseDetail, onEdit }) {
  const when = useFormatWhen()
  const [rows, setRows] = useState([])
  const [filter, setFilter] = useState('')
  const [detail, setDetail] = useState(null)
  const [lists, setLists] = useState([])
  const [pathNames, setPathNames] = useState({})
  const [addListId, setAddListId] = useState('')
  const [msg, setMsg] = useState('')
  const [recipientQ, setRecipientQ] = useState('')

  async function load() {
    try {
      setRows(await apiJson(token, '/api/campaigns'))
    } catch (e) {
      onError(e.message)
    }
  }

  useEffect(() => {
    load()
    apiJson(token, '/api/lists')
      .then(setLists)
      .catch(() => setLists([]))
    apiJson(token, '/api/sending-paths')
      .then((paths) => {
        const map = {}
        for (const p of paths || []) map[p.id] = p.name
        setPathNames(map)
      })
      .catch(() => setPathNames({}))
  }, [token])

  useEffect(() => {
    setMsg('')
    setRecipientQ('')
    if (!detailId) {
      setDetail(null)
      return
    }
    apiJson(token, `/api/campaigns/${detailId}`)
      .then(setDetail)
      .catch((e) => onError(e.message))
  }, [token, detailId])

  const visible = filterCampaigns(rows, filter)

  async function stop(id) {
    try {
      await apiJson(token, `/api/campaigns/${id}/stop`, { method: 'POST' })
      await load()
      if (detailId === id) setDetail(await apiJson(token, `/api/campaigns/${id}`))
    } catch (e) {
      onError(e.message)
    }
  }

  async function pause(id) {
    try {
      const updated = await apiJson(token, `/api/campaigns/${id}/pause`, { method: 'POST' })
      await load()
      if (detailId === id) setDetail(updated)
    } catch (e) {
      onError(e.message)
    }
  }

  async function resume(id) {
    try {
      const updated = await apiJson(token, `/api/campaigns/${id}/resume`, { method: 'POST' })
      await load()
      if (detailId === id) setDetail(updated)
    } catch (e) {
      onError(e.message)
    }
  }

  async function queue(id) {
    try {
      const updated = await apiJson(token, `/api/campaigns/${id}/queue`, { method: 'POST' })
      await load()
      if (detailId === id) {
        setDetail(updated)
        setMsg('Queued')
      }
    } catch (e) {
      onError(e.message)
    }
  }

  async function requeue() {
    try {
      setDetail(await apiJson(token, `/api/campaigns/${detailId}/requeue`, { method: 'POST' }))
      await load()
      setMsg('Requeued')
    } catch (e) {
      onError(e.message)
    }
  }

  async function remove(id, subject) {
    if (!confirmDelete(`campaign “${subject || id}” and all of its send data`)) return
    try {
      await apiJson(token, `/api/campaigns/${id}`, { method: 'DELETE' })
      await load()
      if (detailId === id) onCloseDetail()
    } catch (e) {
      onError(e.message)
    }
  }

  async function addList(e) {
    e.preventDefault()
    try {
      setDetail(
        await apiJson(token, `/api/campaigns/${detailId}/lists`, {
          method: 'POST',
          body: JSON.stringify({ listId: addListId }),
        }),
      )
      setAddListId('')
      setMsg('List added')
    } catch (err) {
      onError(err.message)
    }
  }

  function canRequeue(status) {
    const s = (status || '').toUpperCase()
    return s === 'STOPPED' || s === 'FINISHED'
  }

  const recipients = detail?.recipients || []
  const recipientNeedle = recipientQ.trim().toLowerCase()
  const visibleRecipients = recipientNeedle
    ? recipients.filter(
        (r) =>
          String(r.email || '')
            .toLowerCase()
            .includes(recipientNeedle) ||
          String(r.status || '')
            .toLowerCase()
            .includes(recipientNeedle),
      )
    : recipients
  const hasSendFailed = recipients.some((r) => r.status === 'Send Failed')

  if (detailId && detail) {
    return (
      <div className="page-sections">
        <section className="card">
          <button type="button" className="btn secondary" onClick={onCloseDetail}>
            ← Campaigns
          </button>
          <h2>{detail.subject}</h2>
          <p>
            <StatusBadge status={detail.status} />
          </p>
          {msg ? <p>{msg}</p> : null}
          {canPauseCampaign(detail.status) ? (
            <button type="button" className="btn secondary" onClick={() => pause(detailId)}>
              Pause
            </button>
          ) : null}{' '}
          {canResumeCampaign(detail.status) ? (
            <button type="button" className="btn" onClick={() => resume(detailId)}>
              Resume
            </button>
          ) : null}{' '}
          {canStopCampaign(detail.status) ? (
            <button type="button" className="btn secondary" onClick={() => stop(detailId)}>
              Stop
            </button>
          ) : null}{' '}
          {(detail.status || '').toUpperCase() === 'DRAFT' ? (
            <>
              <button type="button" className="btn" onClick={() => onEdit(detailId)}>
                Edit
              </button>{' '}
              <button type="button" className="btn" onClick={() => queue(detailId)}>
                Queue
              </button>
            </>
          ) : null}{' '}
          {canRequeue(detail.status) ? (
            <button type="button" className="btn" onClick={requeue}>
              Requeue
            </button>
          ) : null}{' '}
          {hasSendFailed ? (
            <button
              type="button"
              className="btn"
              onClick={async () => {
                try {
                  setDetail(
                    await apiJson(token, `/api/campaigns/${detailId}/retry-failed`, {
                      method: 'POST',
                    }),
                  )
                  setMsg('Retry queued')
                } catch (e) {
                  onError(e.message)
                }
              }}
            >
              Retry failed
            </button>
          ) : null}{' '}
          {canDeleteCampaign(detail.status) ? (
            <button
              type="button"
              className="btn secondary"
              onClick={() => remove(detailId, detail.subject)}
            >
              Delete
            </button>
          ) : null}
        </section>

        <section className="card">
          <h3>Lists</h3>
          <RecordTable
            rows={detail.lists || []}
            rowKey={(l) => l.id}
            empty="No lists."
            columns={[{ key: 'name', label: 'List' }]}
          />
          {(detail.status || '').toUpperCase() === 'DRAFT' ||
          (detail.status || '').toUpperCase() === 'STOPPED' ||
          (detail.status || '').toUpperCase() === 'FINISHED' ? (
            <form onSubmit={addList}>
              <label className="field">
                Add List
                <FieldDesc>{fieldHelp.addList}</FieldDesc>
                <select value={addListId} onChange={(ev) => setAddListId(ev.target.value)} required>
                  <option value="">— Select —</option>
                  {lists.map((l) => (
                    <option key={l.id} value={l.id}>
                      {l.name}
                    </option>
                  ))}
                </select>
              </label>
              <button type="submit" className="btn secondary">
                Add list
              </button>
            </form>
          ) : null}
        </section>

        <section className="card">
          <h3>Sending Path</h3>
          <p>
            {detail.sendingPathId
              ? pathNames[detail.sendingPathId] || detail.sendingPathId
              : 'None — first included list path is used at send'}
          </p>
        </section>

        <section className="card">
          <h3>Preview</h3>
          <p className="hint">Subject: {detail.subject || '(no subject)'}</p>
          {detail.htmlBody ? (
            <iframe
              title="Campaign HTML preview"
              sandbox=""
              srcDoc={detail.htmlBody}
              style={{
                width: '100%',
                minHeight: 280,
                border: '1px solid #cbd5e1',
                background: '#fff',
              }}
            />
          ) : (
            <pre className="hint" style={{ whiteSpace: 'pre-wrap' }}>
              {detail.textBody || 'No body.'}
            </pre>
          )}
          {detail.htmlBody && detail.textBody ? (
            <details style={{ marginTop: '0.75rem' }}>
              <summary>Text body</summary>
              <pre style={{ whiteSpace: 'pre-wrap' }}>{detail.textBody}</pre>
            </details>
          ) : null}
        </section>

        <section className="card">
          <h3>Attachments</h3>
          <RecordTable
            rows={detail.attachments || []}
            rowKey={(a) => a.id}
            empty="No attachments."
            columns={[
              { key: 'filename', label: 'File' },
              {
                key: 'sizeBytes',
                label: 'Size',
                render: (a) => formatBytes(a.sizeBytes),
              },
              {
                key: 'download',
                label: '',
                stopRowClick: true,
                render: (a) => (
                  <button
                    type="button"
                    className="btn secondary"
                    onClick={async () => {
                      try {
                        const res = await fetch(
                          `/api/campaigns/${detailId}/attachments/${a.id}`,
                          { headers: { Authorization: `Bearer ${token}` } },
                        )
                        if (!res.ok) throw new Error(`Download failed (${res.status})`)
                        const blob = await res.blob()
                        const url = URL.createObjectURL(blob)
                        const link = document.createElement('a')
                        link.href = url
                        link.download = a.filename || 'attachment'
                        link.click()
                        URL.revokeObjectURL(url)
                      } catch (e) {
                        onError(e.message || String(e))
                      }
                    }}
                  >
                    Download
                  </button>
                ),
              },
            ]}
          />
        </section>

        <section className="card">
          <h3>Sending</h3>
          <dl className="stats">
            <div>
              <dt>Send Start</dt>
              <dd>{when(detail.sendStartedAt)}</dd>
            </div>
            <div>
              <dt>Send Finish</dt>
              <dd>{when(detail.sendFinishedAt)}</dd>
            </div>
            <div>
              <dt>Messages Sent</dt>
              <dd>{detail.sentCount ?? 0}</dd>
            </div>
            <div>
              <dt>Waiting</dt>
              <dd>{detail.waitingCount ?? 0}</dd>
            </div>
            <div>
              <dt>Bounces</dt>
              <dd>{detail.bounceCount ?? 0}</dd>
            </div>
            <div>
              <dt>Unique Views</dt>
              <dd>{detail.uniqueViews ?? 0}</dd>
            </div>
            <div>
              <dt>Total Views</dt>
              <dd>{detail.totalViews ?? 0}</dd>
            </div>
          </dl>
        </section>

        <section className="card">
          <h3>Recipients</h3>
          <label className="field">
            Search
            <FieldDesc>{fieldHelp.search}</FieldDesc>
            <input
              value={recipientQ}
              onChange={(ev) => setRecipientQ(ev.target.value)}
              placeholder="Email or status…"
            />
          </label>
          <RecordTable
            rows={visibleRecipients}
            rowKey={(r) => r.email}
            empty={recipients.length ? 'No recipients match this search.' : 'No recipients yet.'}
            columns={[
              { key: 'email', label: 'Email' },
              { key: 'status', label: 'Status' },
              {
                key: 'firstOpenAt',
                label: 'First Open',
                render: (r) => when(r.firstOpenAt),
              },
              { key: 'openCount', label: 'Opens' },
            ]}
          />
        </section>
      </div>
    )
  }

  return (
    <div className="card">
      <h2>Campaigns</h2>
      <label className="field">
        Status Filter
        <FieldDesc>{fieldHelp.statusFilter}</FieldDesc>
        <select value={filter} onChange={(ev) => setFilter(ev.target.value)}>
          <option value="">All</option>
          {CAMPAIGN_STATUSES.map((s) => (
            <option key={s} value={s}>
              {campaignStatusLabel(s)}
            </option>
          ))}
        </select>
      </label>
      <RecordTable
        rows={visible}
        rowKey={(r) => r.id}
        onOpen={(r) => onOpen(r.id)}
        empty="No campaigns match this filter."
        columns={[
          {
            key: 'subject',
            label: 'Subject',
            render: (r) => r.subject || '(no subject)',
          },
          {
            key: 'status',
            label: 'Status',
            render: (r) => <StatusBadge status={r.status} />,
          },
          {
            key: 'createdAt',
            label: 'Created',
            render: (r) => when(r.createdAt),
          },
          {
            key: 'queuedAt',
            label: 'Queued',
            render: (r) => when(r.queuedAt),
          },
          {
            key: 'actions',
            label: 'Actions',
            stopRowClick: true,
            render: (r) => {
              const draft = (r.status || '').toUpperCase() === 'DRAFT'
              const showPause = canPauseCampaign(r.status)
              const showResume = canResumeCampaign(r.status)
              const showStop = canStopCampaign(r.status)
              if (!draft && !showPause && !showResume && !showStop) return '—'
              return (
                <>
                  {draft ? (
                    <>
                      <button type="button" className="btn secondary" onClick={() => onEdit(r.id)}>
                        Edit
                      </button>{' '}
                      <button type="button" className="btn" onClick={() => queue(r.id)}>
                        Queue
                      </button>
                    </>
                  ) : null}{' '}
                  {showPause ? (
                    <button type="button" className="btn secondary" onClick={() => pause(r.id)}>
                      Pause
                    </button>
                  ) : null}{' '}
                  {showResume ? (
                    <button type="button" className="btn" onClick={() => resume(r.id)}>
                      Resume
                    </button>
                  ) : null}{' '}
                  {showStop ? (
                    <button type="button" className="btn secondary" onClick={() => stop(r.id)}>
                      Stop
                    </button>
                  ) : null}
                </>
              )
            },
          },
        ]}
      />
    </div>
  )
}
