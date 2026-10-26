import { useEffect, useState } from 'react'
import { apiJson } from './api.js'
import { CREATE_ID, isCreatePage } from './createPage.js'
import { btnDirtyClass, dirtyControlProps, isFormDirty, valuesEqual } from './formDirty.js'
import { fieldLabel } from './fieldLabel.js'
import { RecordTable } from './RecordTable.jsx'
import { useRegisterUnsaved } from './Unsaved.jsx'
import { useDirtyForm } from './useDirtyForm.js'

const MAP_BLANK = { name: '', listIds: [], substituteFrom: '', footerId: '' }

export function MtsPage({ token, onError, detailId, onOpen, onCloseDetail }) {
  const creating = isCreatePage(detailId)
  const {
    form: inboxForm,
    setForm: setInboxForm,
    dirty: inboxDirty,
    blank: inboxBlank,
    resetToBlank: resetInbox,
  } = useDirtyForm(
    {
      name: '',
      host: '127.0.0.1',
      port: 143,
      username: '',
      password: '',
      useSsl: false,
      pollIntervalMinutes: 5,
      enabled: true,
    },
    creating ? 'inbox-create' : 'inbox-create-idle',
    creating,
  )
  const [inboxes, setInboxes] = useState([])
  const [mappings, setMappings] = useState([])
  const [detail, setDetail] = useState(null)
  const [lists, setLists] = useState([])
  const [footers, setFooters] = useState([])
  const [senderEmail, setSenderEmail] = useState('')
  const [mapForm, setMapForm] = useState({ ...MAP_BLANK })
  const senderDirty = !!senderEmail.trim()
  const mapDirty = isFormDirty(mapForm, MAP_BLANK)
  useRegisterUnsaved(senderDirty, `mts-sender-${detailId || 'x'}`)
  useRegisterUnsaved(mapDirty, `mts-map-${detailId || 'x'}`)

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
    if (!detailId || isCreatePage(detailId)) {
      setDetail(null)
      setSenderEmail('')
      setMapForm({ ...MAP_BLANK })
      if (isCreatePage(detailId)) resetInbox()
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
      resetInbox()
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

  if (creating) {
    return (
      <div className="card">
        <button type="button" className="btn secondary" onClick={onCloseDetail}>
          ← Inboxes & Mappings
        </button>
        <h2>Add Mail Box</h2>
        <p className="hint">
          The mailbox is created on the mail server. This only adds it to the Mail-to-Send polling list.
        </p>
        <form onSubmit={createInbox}>
          {['name', 'host', 'username', 'password'].map((k) => (
            <label className="field" key={k}>
              {fieldLabel(k)}
              <input
                type={k === 'password' ? 'password' : 'text'}
                value={inboxForm[k]}
                onChange={(ev) => setInboxForm({ ...inboxForm, [k]: ev.target.value })}
                required
                {...dirtyControlProps(!valuesEqual(inboxForm[k], inboxBlank[k]))}
              />
            </label>
          ))}
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
        <h3>Authorized Senders</h3>
        <RecordTable
          rows={(detail.authorizedSenders || []).map((email) => ({ email }))}
          rowKey={(r) => r.email}
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

        <h3>Mappings</h3>
        <RecordTable
          rows={inboxMaps}
          rowKey={(m) => m.id}
          empty="No mappings for this inbox."
          columns={[
            { key: 'name', label: 'Name' },
            {
              key: 'listIds',
              label: 'Lists',
              render: (m) => (m.listIds || []).join(', ') || '—',
            },
            {
              key: 'substituteFrom',
              label: 'Substitute From',
              render: (m) => m.substituteFrom || '—',
            },
          ]}
        />
        <form onSubmit={addMapping}>
          <label className="field">
            {fieldLabel('mapping name')}
            <input
              value={mapForm.name}
              onChange={(ev) => setMapForm({ ...mapForm, name: ev.target.value })}
              required
              {...dirtyControlProps(!valuesEqual(mapForm.name, MAP_BLANK.name))}
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
            {fieldLabel('substitute From email')}
            <input
              value={mapForm.substituteFrom}
              onChange={(ev) => setMapForm({ ...mapForm, substituteFrom: ev.target.value })}
              {...dirtyControlProps(!valuesEqual(mapForm.substituteFrom, MAP_BLANK.substituteFrom))}
            />
          </label>
          <label className="field">
            {fieldLabel('footer override')}
            <select
              value={mapForm.footerId}
              onChange={(ev) => setMapForm({ ...mapForm, footerId: ev.target.value })}
              {...dirtyControlProps(!valuesEqual(mapForm.footerId, MAP_BLANK.footerId))}
            >
              <option value="">— None —</option>
              {footers.map((f) => (
                <option key={f.id} value={f.id}>
                  {f.name}
                </option>
              ))}
            </select>
          </label>
          <button
            type="submit"
            className={btnDirtyClass(mapDirty)}
            title={mapDirty ? 'Unsaved changes' : undefined}
          >
            Add mapping
          </button>
        </form>
      </div>
    )
  }

  return (
    <div className="card">
      <div className="index-actions">
        <h2>Inboxes &amp; Mappings</h2>
        <button type="button" className="btn" onClick={() => onOpen(CREATE_ID)}>
          Add Mail Box
        </button>
      </div>
      <RecordTable
        rows={inboxes}
        rowKey={(i) => i.id}
        onOpen={(i) => onOpen(i.id)}
        empty="No inboxes yet."
        columns={[
          { key: 'name', label: 'Name' },
          {
            key: 'enabled',
            label: 'Enabled',
            render: (i) => (i.enabled ? 'Yes' : 'No'),
          },
          {
            key: 'pollIntervalMinutes',
            label: 'Poll Minutes',
          },
        ]}
      />
      <h3>All Mappings</h3>
      <RecordTable
        rows={mappings}
        rowKey={(m) => m.id}
        onOpen={(m) => onOpen(m.inboxId)}
        empty="No mappings yet."
        columns={[
          { key: 'name', label: 'Name' },
          {
            key: 'listIds',
            label: 'Lists',
            render: (m) => (m.listIds || []).join(', ') || '—',
          },
          {
            key: 'substituteFrom',
            label: 'Substitute From',
            render: (m) => m.substituteFrom || '—',
          },
        ]}
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

export function BounceLogPage() {
  return (
    <div className="card">
      <h2>Bounce Log</h2>
      <p className="hint">Correlation index ships with bounce processing ops; mailbox config is under Bounce Mailboxes.</p>
    </div>
  )
}
