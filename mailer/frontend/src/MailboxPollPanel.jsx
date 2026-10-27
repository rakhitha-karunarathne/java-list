import { useEffect, useState } from 'react'
import { apiJson } from './api.js'
import { formatWhen } from './formatWhen.js'
import { RecordTable } from './RecordTable.jsx'
import { useFormatWhen } from './TimeZone.jsx'

/** Poll controls and lock section. Admin-only pages. */
export function MailboxPollPanel({
  token,
  onError,
  enabled,
  detail,
  pollPath,
  breakLockPath,
  onDetail,
  onMsg,
  onPolled,
}) {
  const when = useFormatWhen()
  async function pollNow() {
    try {
      const r = await apiJson(token, pollPath, { method: 'POST' })
      onMsg?.(r.message || 'process started')
      onDetail?.(await apiJson(token, pollPath.replace(/\/poll$/, '')))
      onPolled?.()
    } catch (e) {
      onError(e.message)
    }
  }

  async function breakLock() {
    try {
      onDetail?.(await apiJson(token, breakLockPath, { method: 'POST' }))
      onMsg?.('Lock released')
    } catch (e) {
      onError(e.message)
    }
  }

  const polling = !!detail?.polling
  const updateAt = detail?.lastPollingUpdate ? Date.parse(detail.lastPollingUpdate) : 0
  const canBreak = polling && updateAt > 0 && Date.now() - updateAt >= 60000

  return (
    <>
      {enabled ? (
        <p>
          <button type="button" className="btn" onClick={pollNow}>
            Poll
          </button>
        </p>
      ) : null}
      <p className="hint">
        Last polled: {when(detail?.lastPolledAt)} · Result: {detail?.result || '—'}
      </p>
      {polling ? (
        <section className="card">
          <h3>Polling</h3>
          <p>Last Polling Update: {when(detail?.lastPollingUpdate)}</p>
          {canBreak ? (
            <button type="button" className="btn secondary" onClick={breakLock}>
              Break lock
            </button>
          ) : (
            <p className="hint">Break lock is available after 60 seconds without an update.</p>
          )}
        </section>
      ) : null}
    </>
  )
}

/** Per-mailbox activity log. Place as the last section on detail pages. */
export function MailboxProcessingLog({
  token,
  onError,
  mailboxId,
  mailboxName,
  logKind,
  reloadKey = 0,
}) {
  const when = useFormatWhen()
  const [logs, setLogs] = useState([])
  const [logOn, setLogOn] = useState(true)

  function loadLogs() {
    apiJson(token, `/api/activity-logs/enabled?kind=${logKind}`)
      .then((r) => {
        setLogOn(!!r.enabled)
        if (!r.enabled) {
          setLogs([])
          return
        }
        return apiJson(
          token,
          `/api/activity-logs?kind=${logKind}&mailboxId=${encodeURIComponent(mailboxId)}`,
        ).then(setLogs)
      })
      .catch((e) => onError(e.message))
  }

  useEffect(() => {
    if (!mailboxId) return
    loadLogs()
  }, [token, mailboxId, logKind, reloadKey])

  const namedLogs = logs.map((row) => ({
    ...row,
    message: String(row.message || '').replaceAll(
      `mailboxId=${mailboxId}`,
      mailboxName || mailboxId,
    ),
  }))

  return (
    <section className="card">
      <h3>Processing Log</h3>
      {logOn ? (
        <RecordTable
          rows={namedLogs}
          rowKey={(r) => r.id}
          empty="No log entries for this mailbox yet."
          columns={[
            { key: 'createdAt', label: 'Time', render: (r) => when(r.createdAt) },
            { key: 'event', label: 'Event' },
            { key: 'message', label: 'Message' },
          ]}
        />
      ) : (
        <p className="hint">Logging is off.</p>
      )}
    </section>
  )
}

/** @param {{ onPoll?: (row: { id: string, enabled?: boolean }) => void, when?: (v: unknown) => string }} [opts] */
export function pollIndexColumns(opts = {}) {
  const when = opts.when || ((v) => formatWhen(v, 'UTC'))
  const cols = [
    { key: 'name', label: 'Name' },
    {
      key: 'pollIntervalMinutes',
      label: 'Poll interval',
      render: (r) => r.pollIntervalMinutes,
    },
    {
      key: 'lastPolledAt',
      label: 'Last polled',
      render: (r) => when(r.lastPolledAt),
    },
    {
      key: 'result',
      label: 'Result',
      render: (r) => r.result || '—',
    },
  ]
  if (opts.onPoll) {
    cols.push({
      key: 'actions',
      label: 'Actions',
      stopRowClick: true,
      render: (r) =>
        r.enabled === false ? (
          '—'
        ) : (
          <button type="button" className="btn secondary" onClick={() => opts.onPoll(r)}>
            Poll
          </button>
        ),
    })
  }
  return cols
}
