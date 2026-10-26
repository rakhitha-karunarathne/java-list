import { useEffect, useState } from 'react'
import { apiJson } from './api.js'
import {
  CAMPAIGN_STATUSES,
  campaignStatusLabel,
  canStopCampaign,
  filterCampaigns,
} from './campaignStatus.js'

const BADGE = {
  DRAFT: { bg: '#e2e8f0', fg: '#0f172a' },
  QUEUED: { bg: '#dbeafe', fg: '#1e3a8a' },
  SENDING: { bg: '#fef3c7', fg: '#92400e' },
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

export function CampaignsPage({ token, onError, detailId, onOpen, onCloseDetail }) {
  const [rows, setRows] = useState([])
  const [filter, setFilter] = useState('')
  const [detail, setDetail] = useState(null)
  const [lists, setLists] = useState([])
  const [addListId, setAddListId] = useState('')
  const [msg, setMsg] = useState('')

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
  }, [token])

  useEffect(() => {
    if (!detailId) {
      setDetail(null)
      setMsg('')
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

  async function requeue() {
    try {
      setDetail(await apiJson(token, `/api/campaigns/${detailId}/requeue`, { method: 'POST' }))
      await load()
      setMsg('Requeued')
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

  if (detailId && detail) {
    return (
      <div className="card">
        <button type="button" className="btn secondary" onClick={onCloseDetail}>
          ← Campaigns
        </button>
        <h2>{detail.subject}</h2>
        <p>
          <StatusBadge status={detail.status} /> · sent {detail.sentCount ?? 0}
        </p>
        {msg ? <p>{msg}</p> : null}
        <p className="hint">Lists: {(detail.listIds || []).join(', ') || '—'}</p>
        {canStopCampaign(detail.status) ? (
          <button type="button" className="btn secondary" onClick={() => stop(detailId)}>
            Stop
          </button>
        ) : null}{' '}
        {canRequeue(detail.status) ? (
          <button type="button" className="btn" onClick={requeue}>
            Requeue
          </button>
        ) : null}
        {(detail.status || '').toUpperCase() === 'DRAFT' ||
        (detail.status || '').toUpperCase() === 'STOPPED' ||
        (detail.status || '').toUpperCase() === 'FINISHED' ? (
          <form onSubmit={addList} style={{ marginTop: '1rem' }}>
            <label className="field">
              Add list
              <select value={addListId} onChange={(ev) => setAddListId(ev.target.value)} required>
                <option value="">— select —</option>
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
      </div>
    )
  }

  return (
    <div className="card">
      <h2>Campaigns</h2>
      <label className="field">
        Status filter
        <select value={filter} onChange={(ev) => setFilter(ev.target.value)}>
          <option value="">All</option>
          {CAMPAIGN_STATUSES.map((s) => (
            <option key={s} value={s}>
              {campaignStatusLabel(s)}
            </option>
          ))}
        </select>
      </label>
      <ul>
        {visible.map((r) => (
          <li key={r.id}>
            <button type="button" className="btn secondary" onClick={() => onOpen(r.id)}>
              {r.subject || '(no subject)'}
            </button>{' '}
            <StatusBadge status={r.status} />{' '}
            {canStopCampaign(r.status) ? (
              <button type="button" className="btn secondary" onClick={() => stop(r.id)}>
                Stop
              </button>
            ) : null}
          </li>
        ))}
      </ul>
    </div>
  )
}
