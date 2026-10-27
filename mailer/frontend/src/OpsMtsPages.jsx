import { useEffect, useState } from 'react'
import { apiJson } from './api.js'
import { confirmDelete } from './confirmDelete.js'
import { CREATE_ID, isCreatePage } from './createPage.js'
import { ConnectionTestBanner, testImapConnection } from './ConnectionTest.jsx'
import { btnDirtyClass, dirtyControlProps, valuesEqual } from './formDirty.js'
import { fieldLabel } from './fieldLabel.js'
import { mtsSenderDetailId, parseMtsDetailId } from './mtsNav.js'
import { RecordTable } from './RecordTable.jsx'
import { Toggle } from './Toggle.jsx'
import { useRegisterUnsaved } from './Unsaved.jsx'
import { useDirtyForm } from './useDirtyForm.js'
import {
  MailboxPollPanel,
  MailboxProcessingLog,
  pollIndexColumns,
} from './MailboxPollPanel.jsx'

const INBOX_BLANK = {
  name: '',
  host: '127.0.0.1',
  port: 143,
  username: '',
  password: '',
  useSsl: false,
  useTls: false,
  pollIntervalMinutes: 5,
  enabled: true,
}

function grantsSig(rows) {
  return JSON.stringify(
    (rows || [])
      .filter((r) => r.granted)
      .map((r) => ({
        listId: r.listId,
        substituteFrom: r.substituteFrom || '',
        footerId: r.footerId || '',
      }))
      .sort((a, b) => String(a.listId).localeCompare(String(b.listId))),
  )
}

export function MtsPage({ token, onError, detailId, onOpen, onCloseDetail }) {
  const creating = isCreatePage(detailId)
  const nav = creating ? { kind: 'create' } : parseMtsDetailId(detailId)
  const {
    form: inboxForm,
    setForm: setInboxForm,
    saved: inboxSaved,
    dirty: inboxDirty,
    blank: inboxBlank,
    resetToBlank: resetInbox,
    adopt: adoptInbox,
  } = useDirtyForm(INBOX_BLANK, creating ? 'inbox-create' : `inbox-edit-${nav.inboxId || 'x'}`, creating)

  const [inboxes, setInboxes] = useState([])
  const [pollMsg, setPollMsg] = useState('')
  const [logReload, setLogReload] = useState(0)
  const [detail, setDetail] = useState(null)
  const [senderEmail, setSenderEmail] = useState('')
  const [senderDetail, setSenderDetail] = useState(null)
  const [grants, setGrants] = useState([])
  const [savedGrants, setSavedGrants] = useState([])
  const [footers, setFooters] = useState([])
  const [connTest, setConnTest] = useState(null)
  const [testing, setTesting] = useState(false)
  const senderDirty = !!senderEmail.trim()
  const grantsDirty = grantsSig(grants) !== grantsSig(savedGrants)
  useRegisterUnsaved(senderDirty, `mts-sender-add-${nav.inboxId || 'x'}`)
  useRegisterUnsaved(nav.kind === 'sender' && grantsDirty, `mts-sender-grants-${detailId || 'x'}`)

  async function runImapTest() {
    setTesting(true)
    setConnTest(null)
    onError('')
    try {
      setConnTest(
        await testImapConnection(token, {
          host: inboxForm.host,
          port: inboxForm.port,
          username: inboxForm.username,
          password: inboxForm.password,
          useSsl: !!inboxForm.useSsl,
          useTls: !!inboxForm.useTls,
          inboxId: creating ? null : nav.inboxId,
        }),
      )
    } catch (err) {
      setConnTest({ ok: false, message: err.message })
    } finally {
      setTesting(false)
    }
  }

  async function loadInboxes() {
    try {
      setInboxes(await apiJson(token, '/api/mts/inboxes'))
    } catch (e) {
      onError(e.message)
    }
  }

  useEffect(() => {
    loadInboxes()
    apiJson(token, '/api/footers')
      .then(setFooters)
      .catch(() => setFooters([]))
  }, [token])

  useEffect(() => {
    setConnTest(null)
    setSenderEmail('')
    setSenderDetail(null)
    setGrants([])
    setSavedGrants([])
    if (creating) {
      setDetail(null)
      resetInbox()
      return
    }
    if (nav.kind === 'inbox') {
      apiJson(token, `/api/mts/inboxes/${nav.inboxId}`)
        .then((d) => {
          setDetail(d)
          adoptInbox({
            name: d.name || '',
            host: d.host || '',
            port: d.port || 143,
            username: d.username || '',
            password: '',
            useSsl: !!d.useSsl,
        useTls: !!d.useTls,
            pollIntervalMinutes: d.pollIntervalMinutes || 5,
            enabled: !!d.enabled,
          })
        })
        .catch((e) => onError(e.message))
      return
    }
    if (nav.kind === 'sender') {
      apiJson(token, `/api/mts/inboxes/${nav.inboxId}`)
        .then(setDetail)
        .catch((e) => onError(e.message))
      apiJson(token, `/api/mts/inboxes/${nav.inboxId}/senders/${encodeURIComponent(nav.email)}`)
        .then((s) => {
          setSenderDetail(s)
          const rows = (s.grants || []).map((g) => ({
            listId: g.listId,
            listName: g.listName,
            granted: !!g.granted,
            substituteFrom: g.substituteFrom || '',
            footerId: g.footerId || '',
          }))
          setGrants(rows)
          setSavedGrants(rows)
        })
        .catch((e) => onError(e.message))
    }
  }, [token, detailId])

  async function createInbox(e) {
    e.preventDefault()
    try {
      const created = await apiJson(token, '/api/mts/inboxes', {
        method: 'POST',
        body: JSON.stringify(inboxForm),
      })
      await loadInboxes()
      resetInbox()
      onOpen(created.id)
    } catch (err) {
      onError(err.message)
    }
  }

  async function saveInbox(e) {
    e.preventDefault()
    try {
      const body = { ...inboxForm }
      if (!body.password) delete body.password
      const d = await apiJson(token, `/api/mts/inboxes/${nav.inboxId}`, {
        method: 'PUT',
        body: JSON.stringify(body),
      })
      setDetail(d)
      adoptInbox({
        name: d.name || '',
        host: d.host || '',
        port: d.port || 143,
        username: d.username || '',
        password: '',
        useSsl: !!d.useSsl,
        useTls: !!d.useTls,
        pollIntervalMinutes: d.pollIntervalMinutes || 5,
        enabled: !!d.enabled,
      })
      setConnTest(null)
      await loadInboxes()
    } catch (err) {
      onError(err.message)
    }
  }

  async function addSender(e) {
    e.preventDefault()
    try {
      setDetail(
        await apiJson(token, `/api/mts/inboxes/${nav.inboxId}/senders`, {
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
    if (!confirmDelete(`authorized sender ${email}`)) return
    try {
      setDetail(
        await apiJson(token, `/api/mts/inboxes/${nav.inboxId}/senders/${encodeURIComponent(email)}`, {
          method: 'DELETE',
        }),
      )
    } catch (err) {
      onError(err.message)
    }
  }

  async function deleteInbox() {
    if (!confirmDelete(`inbox “${detail?.name || nav.inboxId}” and all connected data`)) return
    try {
      await apiJson(token, `/api/mts/inboxes/${nav.inboxId}`, { method: 'DELETE' })
      await loadInboxes()
      resetInbox()
      setDetail(null)
      onCloseDetail()
    } catch (err) {
      onError(err.message)
    }
  }

  async function saveGrants(e) {
    e.preventDefault()
    try {
      const s = await apiJson(
        token,
        `/api/mts/inboxes/${nav.inboxId}/senders/${encodeURIComponent(nav.email)}/grants`,
        {
          method: 'PUT',
          body: JSON.stringify({
            grants: grants.map((g) => ({
              listId: g.listId,
              granted: !!g.granted,
              substituteFrom: g.substituteFrom || null,
              footerId: g.footerId || null,
            })),
          }),
        },
      )
      setSenderDetail(s)
      const rows = (s.grants || []).map((g) => ({
        listId: g.listId,
        listName: g.listName,
        granted: !!g.granted,
        substituteFrom: g.substituteFrom || '',
        footerId: g.footerId || '',
      }))
      setGrants(rows)
      setSavedGrants(rows)
    } catch (err) {
      onError(err.message)
    }
  }

  function inboxBase(k) {
    if (creating) return inboxBlank[k]
    return inboxSaved ? inboxSaved[k] : inboxBlank[k]
  }

  function inboxAccountFields({ requirePassword }) {
    return (
      <>
        <label className="field">
          {fieldLabel('name')}
          <input
            value={inboxForm.name}
            onChange={(ev) => setInboxForm({ ...inboxForm, name: ev.target.value })}
            required
            {...dirtyControlProps(!valuesEqual(inboxForm.name, inboxBase('name')))}
          />
        </label>
        <label className="field">
          {fieldLabel('host')}
          <input
            value={inboxForm.host}
            onChange={(ev) => setInboxForm({ ...inboxForm, host: ev.target.value })}
            required
            {...dirtyControlProps(!valuesEqual(inboxForm.host, inboxBase('host')))}
          />
        </label>
        <label className="field">
          {fieldLabel('port')}
          <input
            type="number"
            value={inboxForm.port}
            onChange={(ev) => setInboxForm({ ...inboxForm, port: Number(ev.target.value) })}
            {...dirtyControlProps(!valuesEqual(inboxForm.port, inboxBase('port')))}
          />
        </label>
        <label className="field">
          {fieldLabel('username')}
          <input
            value={inboxForm.username}
            onChange={(ev) => setInboxForm({ ...inboxForm, username: ev.target.value })}
            required
            {...dirtyControlProps(!valuesEqual(inboxForm.username, inboxBase('username')))}
          />
        </label>
        <label className="field">
          {fieldLabel(
            requirePassword ? 'password' : 'password (leave blank to keep)',
          )}
          <input
            type="password"
            value={inboxForm.password}
            onChange={(ev) => setInboxForm({ ...inboxForm, password: ev.target.value })}
            required={requirePassword}
            {...dirtyControlProps(!valuesEqual(inboxForm.password, inboxBase('password')))}
          />
        </label>
        <label className="field">
          {fieldLabel('poll interval minutes')}
          <input
            type="number"
            value={inboxForm.pollIntervalMinutes}
            onChange={(ev) =>
              setInboxForm({ ...inboxForm, pollIntervalMinutes: Number(ev.target.value) })
            }
            {...dirtyControlProps(
              !valuesEqual(inboxForm.pollIntervalMinutes, inboxBase('pollIntervalMinutes')),
            )}
          />
        </label>
        <label className="field">
          {fieldLabel('useSsl')}
          <Toggle
            checked={!!inboxForm.useSsl}
            dirty={!valuesEqual(inboxForm.useSsl, inboxBase('useSsl'))}
            aria-label={fieldLabel('useSsl')}
            onChange={(on) =>
              setInboxForm({
                ...inboxForm,
                useSsl: on,
                useTls: on ? false : !!inboxForm.useTls,
              })
            }
          />
        </label>
        <label className="field">
          {fieldLabel('useTls')}
          <Toggle
            checked={!!inboxForm.useTls}
            dirty={!valuesEqual(inboxForm.useTls, inboxBase('useTls'))}
            aria-label={fieldLabel('useTls')}
            onChange={(on) =>
              setInboxForm({
                ...inboxForm,
                useTls: on,
                useSsl: on ? false : !!inboxForm.useSsl,
              })
            }
          />
        </label>
        {!creating ? (
          <label className="field">
            {fieldLabel('enabled')}
            <Toggle
              checked={!!inboxForm.enabled}
              dirty={!valuesEqual(inboxForm.enabled, inboxBase('enabled'))}
              aria-label={fieldLabel('enabled')}
              onChange={(on) => setInboxForm({ ...inboxForm, enabled: on })}
            />
          </label>
        ) : null}
        <ConnectionTestBanner result={connTest} />
        <p>
          <button type="button" className="btn secondary" disabled={testing} onClick={runImapTest}>
            {testing ? 'Testing…' : 'Test Connection'}
          </button>{' '}
        </p>
      </>
    )
  }

  if (creating) {
    return (
      <div className="card">
        <button type="button" className="btn secondary" onClick={onCloseDetail}>
          ← Inboxes
        </button>
        <h2>Add Mail Box</h2>
        <p className="hint">
          The mailbox is created on the mail server. This only adds it to the Mail-to-Send polling list.
        </p>
        <form onSubmit={createInbox}>
          {inboxAccountFields({ requirePassword: true })}
          <button
            type="submit"
            className={btnDirtyClass(inboxDirty)}
            title={inboxDirty ? 'Unsaved changes' : undefined}
          >
            Add Mail Box
          </button>
        </form>
      </div>
    )
  }

  if (nav.kind === 'sender') {
    if (!senderDetail) {
      return (
        <div className="card">
          <button type="button" className="btn secondary" onClick={() => onOpen(nav.inboxId)}>
            ← Inbox
          </button>
          <p className="hint">Loading sender…</p>
        </div>
      )
    }
    return (
      <div className="card">
        <button type="button" className="btn secondary" onClick={() => onOpen(nav.inboxId)}>
          ← {detail?.name || 'Inbox'}
        </button>
        <h2>{senderDetail.email}</h2>
        <p className="hint">Grant Lists for this authorized sender. Substitute From and Footer are per List.</p>
        <form onSubmit={saveGrants}>
          <RecordTable
            rows={grants}
            rowKey={(r) => r.listId}
            empty="No lists available."
            columns={[
              {
                key: 'granted',
                label: 'Granted',
                stopRowClick: true,
                render: (r) => (
                  <input
                    type="checkbox"
                    checked={!!r.granted}
                    onChange={() =>
                      setGrants((rows) =>
                        rows.map((x) =>
                          x.listId === r.listId ? { ...x, granted: !x.granted } : x,
                        ),
                      )
                    }
                  />
                ),
              },
              { key: 'listName', label: 'List' },
              {
                key: 'substituteFrom',
                label: 'Substitute From',
                stopRowClick: true,
                render: (r) => (
                  <input
                    value={r.substituteFrom}
                    disabled={!r.granted}
                    onChange={(ev) =>
                      setGrants((rows) =>
                        rows.map((x) =>
                          x.listId === r.listId
                            ? { ...x, substituteFrom: ev.target.value }
                            : x,
                        ),
                      )
                    }
                    {...dirtyControlProps(
                      r.granted &&
                        !valuesEqual(
                          r.substituteFrom,
                          savedGrants.find((s) => s.listId === r.listId)?.substituteFrom || '',
                        ),
                    )}
                  />
                ),
              },
              {
                key: 'footerId',
                label: 'Footer',
                stopRowClick: true,
                render: (r) => (
                  <select
                    value={r.footerId}
                    disabled={!r.granted}
                    onChange={(ev) =>
                      setGrants((rows) =>
                        rows.map((x) =>
                          x.listId === r.listId ? { ...x, footerId: ev.target.value } : x,
                        ),
                      )
                    }
                    {...dirtyControlProps(
                      r.granted &&
                        !valuesEqual(
                          r.footerId,
                          savedGrants.find((s) => s.listId === r.listId)?.footerId || '',
                        ),
                    )}
                  >
                    <option value="">— List default —</option>
                    {footers.map((f) => (
                      <option key={f.id} value={f.id}>
                        {f.name}
                      </option>
                    ))}
                  </select>
                ),
              },
            ]}
          />
          <p>
            <button
              type="submit"
              className={btnDirtyClass(grantsDirty)}
              title={grantsDirty ? 'Unsaved changes' : undefined}
            >
              Save Grants
            </button>
          </p>
        </form>
      </div>
    )
  }

  if (nav.kind === 'inbox') {
    if (!detail) {
      return (
        <div className="card">
          <button type="button" className="btn secondary" onClick={onCloseDetail}>
            ← Inboxes
          </button>
          <p className="hint">Loading inbox…</p>
        </div>
      )
    }
    const inboxMaps = detail.mappings || []
    return (
      <div className="page-sections">
        <section className="card">
          <button type="button" className="btn secondary" onClick={onCloseDetail}>
            ← Inboxes
          </button>
          <h2>{detail.name}</h2>
          {pollMsg ? <p>{pollMsg}</p> : null}
          <MailboxPollPanel
            token={token}
            onError={onError}
            enabled={!!detail.enabled}
            detail={detail}
            pollPath={`/api/mts/inboxes/${nav.inboxId}/poll`}
            breakLockPath={`/api/mts/inboxes/${nav.inboxId}/break-lock`}
            onDetail={setDetail}
            onMsg={setPollMsg}
            onPolled={() => setLogReload((n) => n + 1)}
          />
        </section>

        <section className="card">
          <h3>Mail Box Account</h3>
          <form onSubmit={saveInbox}>
            {inboxAccountFields({ requirePassword: false })}
            <button
              type="submit"
              className={btnDirtyClass(inboxDirty)}
              title={inboxDirty ? 'Unsaved changes' : undefined}
            >
              Save Account
            </button>{' '}
            <button type="button" className="btn secondary" onClick={deleteInbox}>
              Delete Inbox
            </button>
          </form>
        </section>

        <section className="card">
          <h3>Authorized Senders</h3>
          <RecordTable
            rows={(detail.authorizedSenders || []).map((email) => ({ email }))}
            rowKey={(r) => r.email}
            onOpen={(r) => onOpen(mtsSenderDetailId(nav.inboxId, r.email))}
            empty="No authorized senders."
            columns={[
              { key: 'email', label: 'Email' },
              {
                key: 'actions',
                label: 'Actions',
                stopRowClick: true,
                render: (r) => (
                  <button
                    type="button"
                    className="btn secondary"
                    onClick={() => removeSender(r.email)}
                  >
                    Remove
                  </button>
                ),
              },
            ]}
          />
          <form onSubmit={addSender}>
            <label className="field">
              {fieldLabel('sender email')}
              <input
                value={senderEmail}
                onChange={(ev) => setSenderEmail(ev.target.value)}
                required
                {...dirtyControlProps(senderDirty)}
              />
            </label>
            <button
              type="submit"
              className={btnDirtyClass(senderDirty, 'btn secondary')}
              title={senderDirty ? 'Unsaved changes' : undefined}
            >
              Add Sender
            </button>
          </form>
        </section>

        <section className="card">
          <h3>List Grants</h3>
          <RecordTable
            rows={inboxMaps}
            rowKey={(m) => m.id}
            onOpen={(m) => onOpen(mtsSenderDetailId(nav.inboxId, m.senderEmail))}
            empty="No list grants yet — open a sender to grant Lists."
            columns={[
              { key: 'senderEmail', label: 'Sender' },
              { key: 'listName', label: 'List' },
              {
                key: 'substituteFrom',
                label: 'Substitute From',
                render: (m) => m.substituteFrom || '—',
              },
              {
                key: 'footerId',
                label: 'Footer',
                render: (m) =>
                  footers.find((f) => f.id === m.footerId)?.name || m.footerId || '—',
              },
            ]}
          />
        </section>

        <MailboxProcessingLog
          token={token}
          onError={onError}
          mailboxId={nav.inboxId}
          mailboxName={detail.name}
          logKind="MTS"
          reloadKey={logReload}
        />
      </div>
    )
  }

  async function pollFromIndex(row) {
    try {
      const r = await apiJson(token, `/api/mts/inboxes/${row.id}/poll`, { method: 'POST' })
      setPollMsg(r.message || 'process started')
      await loadInboxes()
    } catch (e) {
      onError(e.message)
    }
  }

  return (
    <div className="card">
      <div className="index-actions">
        <h2>Inboxes</h2>
        <button type="button" className="btn" onClick={() => onOpen(CREATE_ID)}>
          Add Mail Box
        </button>
      </div>
      {pollMsg ? <p>{pollMsg}</p> : null}
      <RecordTable
        rows={inboxes}
        rowKey={(i) => i.id}
        onOpen={(i) => onOpen(i.id)}
        empty="No inboxes yet."
        columns={pollIndexColumns({ onPoll: pollFromIndex })}
      />
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
      <RecordTable
        rows={rows}
        rowKey={(r) => r.id}
        empty="No open From-domain issues."
        columns={[
          {
            key: 'subject',
            label: 'Subject',
            render: (r) => r.subject || '(no subject)',
          },
          { key: 'senderEmail', label: 'Sender' },
          { key: 'status', label: 'Status' },
          {
            key: 'detail',
            label: 'Detail',
            render: (r) => r.detail || '—',
          },
          {
            key: 'actions',
            label: 'Actions',
            stopRowClick: true,
            render: (r) =>
              r.status !== 'CLEARED' ? (
                <button type="button" className="btn secondary" onClick={() => clear(r.id)}>
                  Clear Issue
                </button>
              ) : (
                '—'
              ),
          },
        ]}
      />
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

  return (
    <div className="card">
      <h2>Drop Log</h2>
      <p className="hint">Auth/spam/policy drops — distinct from From-domain Issues.</p>
      {inboxFilter ? (
        <p>
          Filtered inbox <code>{inboxFilter}</code>{' '}
          <button type="button" className="btn secondary" onClick={() => onFilterInbox?.(null)}>
            Clear Filter
          </button>
        </p>
      ) : null}
      <RecordTable
        rows={rows}
        rowKey={(r) => r.id}
        empty="No drop log entries."
        columns={[
          { key: 'senderEmail', label: 'Sender' },
          { key: 'reason', label: 'Reason' },
          {
            key: 'countedForAutoBlock',
            label: 'Auto-Block',
            render: (r) => (r.countedForAutoBlock ? 'Yes' : 'No'),
          },
          {
            key: 'resolved',
            label: 'Resolved',
            render: (r) => (r.resolved ? 'Yes' : 'No'),
          },
          {
            key: 'actions',
            label: 'Actions',
            stopRowClick: true,
            render: (r) => (
              <>
                <button
                  type="button"
                  className="btn secondary"
                  onClick={() => onFilterInbox?.(r.inboxId)}
                >
                  Inbox
                </button>{' '}
                {!r.resolved ? (
                  <button type="button" className="btn secondary" onClick={() => resolve(r.id)}>
                    Resolve
                  </button>
                ) : null}
              </>
            ),
          },
        ]}
      />
    </div>
  )
}

export function GlobalBlocksPage({ token, onError, detailId, onOpen, onCloseDetail }) {
  const [blocks, setBlocks] = useState([])
  const [blockEmail, setBlockEmail] = useState('')
  const [blockDomain, setBlockDomain] = useState('')
  const emailDirty = !!blockEmail.trim()
  const domainDirty = !!blockDomain.trim()
  useRegisterUnsaved(emailDirty || domainDirty, 'blocks-create')

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
    if (!confirmDelete('this block')) return
    try {
      await apiJson(token, `/api/mts/blocks/${id}`, { method: 'DELETE' })
      await load()
    } catch (e) {
      onError(e.message)
    }
  }

  if (isCreatePage(detailId)) {
    return (
      <div className="card">
        <button type="button" className="btn secondary" onClick={onCloseDetail}>
          ← Global Blocks
        </button>
        <h2>Add Block</h2>
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
              onCloseDetail()
            } catch (err) {
              onError(err.message)
            }
          }}
        >
          <label className="field">
            Block Sender
            <input
              value={blockEmail}
              onChange={(ev) => setBlockEmail(ev.target.value)}
              required
              {...dirtyControlProps(emailDirty)}
            />
          </label>
          <button
            type="submit"
            className={btnDirtyClass(emailDirty, 'btn secondary')}
            title={emailDirty ? 'Unsaved changes' : undefined}
          >
            Block Sender
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
              onCloseDetail()
            } catch (err) {
              onError(err.message)
            }
          }}
        >
          <label className="field">
            Block Domain
            <input
              value={blockDomain}
              onChange={(ev) => setBlockDomain(ev.target.value)}
              required
              {...dirtyControlProps(domainDirty)}
            />
          </label>
          <button
            type="submit"
            className={btnDirtyClass(domainDirty, 'btn secondary')}
            title={domainDirty ? 'Unsaved changes' : undefined}
          >
            Block Domain
          </button>
        </form>
      </div>
    )
  }

  return (
    <div className="card">
      <div className="index-actions">
        <h2>Global Blocks</h2>
        <button type="button" className="btn" onClick={() => onOpen(CREATE_ID)}>
          Add Block
        </button>
      </div>
      <RecordTable
        rows={blocks}
        rowKey={(b) => b.id}
        empty="No global blocks."
        columns={[
          { key: 'kind', label: 'Kind' },
          { key: 'value', label: 'Value' },
          { key: 'source', label: 'Source' },
          {
            key: 'actions',
            label: 'Actions',
            stopRowClick: true,
            render: (b) => (
              <button type="button" className="btn secondary" onClick={() => unblock(b.id)}>
                Unblock
              </button>
            ),
          },
        ]}
      />
    </div>
  )
}

export function BounceLogPage({ token, onError }) {
  const [msg, setMsg] = useState('')

  async function runProcess() {
    try {
      const r = await apiJson(token, '/api/bounce-process/run', { method: 'POST' })
      setMsg(r.message || 'process started')
    } catch (e) {
      onError(e.message)
    }
  }

  return (
    <div className="card">
      <h2>Bounce Log</h2>
      <p className="hint">
        Correlation index ships with bounce processing ops; mailbox config is under Bounce Mailboxes.
      </p>
      {msg ? <p>{msg}</p> : null}
      <p>
        <button type="button" className="btn" onClick={runProcess}>
          Run bounce processing
        </button>
      </p>
    </div>
  )
}

