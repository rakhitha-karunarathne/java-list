import { useEffect, useState } from 'react'
import { apiJson } from './api.js'
import { CONSENT_REQUEST_HINT } from './consentCopy.js'
import { emptyHint } from './emptyHint.js'
import { btnDirtyClass, dirtyControlProps, valuesEqual } from './formDirty.js'
import { fieldLabel } from './fieldLabel.js'
import { membersSearchPath } from './membersSearch.js'
import { CREATE_ID, isCreatePage } from './createPage.js'
import { RecordTable } from './RecordTable.jsx'
import { useRegisterUnsaved } from './Unsaved.jsx'
import { useDirtyForm } from './useDirtyForm.js'

export function ListsPage({ token, onError, detailId, onOpen, onCloseDetail, onOpenMember }) {
  const creating = isCreatePage(detailId)
  const { form, setForm, saved, dirty, blank, adopt, resetToBlank } = useDirtyForm(
    {
      name: '',
      sendingPathId: '',
      footerId: '',
      spamThreshold: '5.0',
      payloadOverride: '',
      imageTtlOverrideHours: '',
    },
    creating ? 'list-create' : `list-${detailId || 'x'}`,
    creating,
  )
  const [rows, setRows] = useState([])
  const [detail, setDetail] = useState(null)
  const [paths, setPaths] = useState([])
  const [footers, setFooters] = useState([])
  const [listMembers, setListMembers] = useState([])
  const [bulkText, setBulkText] = useState('')
  const [bulkResult, setBulkResult] = useState(null)
  const bulkDirty = !!bulkText.trim()
  useRegisterUnsaved(bulkDirty, `list-bulk-${detailId || 'x'}`)

  async function load() {
    try {
      setRows(await apiJson(token, '/api/lists'))
    } catch (e) {
      onError(e.message)
    }
  }

  useEffect(() => {
    load()
    apiJson(token, '/api/sending-paths')
      .then(setPaths)
      .catch(() => setPaths([]))
    apiJson(token, '/api/footers')
      .then(setFooters)
      .catch(() => setFooters([]))
  }, [token])

  useEffect(() => {
    if (!detailId || isCreatePage(detailId)) {
      setDetail(null)
      setBulkResult(null)
      setBulkText('')
      if (isCreatePage(detailId)) resetToBlank()
      return
    }
    Promise.all([
      apiJson(token, `/api/lists/${detailId}`),
      apiJson(token, `/api/lists/${detailId}/members`).catch(() => []),
    ])
      .then(([d, ms]) => {
        setDetail(d)
        setListMembers(ms)
        adopt({
          name: d.name,
          sendingPathId: d.sendingPathId,
          footerId: d.footerId || '',
          spamThreshold: d.spamThreshold != null ? String(d.spamThreshold) : '',
          payloadOverride: d.payloadOverride || '',
          imageTtlOverrideHours:
            d.imageTtlOverrideHours != null ? String(d.imageTtlOverrideHours) : '',
        })
      })
      .catch((e) => onError(e.message))
  }, [token, detailId])

  async function create(e) {
    e.preventDefault()
    try {
      const body = {
        name: form.name,
        sendingPathId: form.sendingPathId,
        footerId: form.footerId || null,
        spamThreshold: form.spamThreshold ? Number(form.spamThreshold) : null,
        payloadOverride: form.payloadOverride || null,
        imageTtlOverrideHours: form.imageTtlOverrideHours
          ? Number(form.imageTtlOverrideHours)
          : null,
      }
      const created = await apiJson(token, '/api/lists', {
        method: 'POST',
        body: JSON.stringify(body),
      })
      await load()
      resetToBlank()
      onOpen(created.id)
    } catch (err) {
      onError(err.message)
    }
  }

  async function save(e) {
    e.preventDefault()
    try {
      const body = {
        name: form.name,
        sendingPathId: form.sendingPathId,
        footerId: form.footerId || null,
        spamThreshold: form.spamThreshold ? Number(form.spamThreshold) : null,
        payloadOverride: form.payloadOverride || null,
        imageTtlOverrideHours: form.imageTtlOverrideHours
          ? Number(form.imageTtlOverrideHours)
          : null,
      }
      const d = await apiJson(token, `/api/lists/${detailId}`, {
        method: 'PUT',
        body: JSON.stringify(body),
      })
      setDetail(d)
      adopt({
        name: d.name,
        sendingPathId: d.sendingPathId,
        footerId: d.footerId || '',
        spamThreshold: d.spamThreshold != null ? String(d.spamThreshold) : '',
        payloadOverride: d.payloadOverride || '',
        imageTtlOverrideHours:
          d.imageTtlOverrideHours != null ? String(d.imageTtlOverrideHours) : '',
      })
    } catch (err) {
      onError(err.message)
    }
  }

  async function bulkAdd(e) {
    e.preventDefault()
    setBulkResult(null)
    try {
      const result = await apiJson(token, `/api/lists/${detailId}/members/bulk`, {
        method: 'POST',
        body: JSON.stringify({ text: bulkText }),
      })
      setBulkResult(result)
      setBulkText('')
      setListMembers(await apiJson(token, `/api/lists/${detailId}/members`))
    } catch (err) {
      onError(err.message)
    }
  }

  if (creating) {
    return (
      <div className="card">
        <button type="button" className="btn secondary" onClick={onCloseDetail}>
          ← Lists
        </button>
        <h2>Create List</h2>
        <form onSubmit={create}>
          <label className="field">
            {fieldLabel('name')}
            <input
              value={form.name}
              onChange={(ev) => setForm({ ...form, name: ev.target.value })}
              required
              {...dirtyControlProps(!valuesEqual(form.name, blank.name))}
            />
          </label>
          <label className="field">
            {fieldLabel('sending path')}
            <select
              value={form.sendingPathId}
              onChange={(ev) => setForm({ ...form, sendingPathId: ev.target.value })}
              required
              {...dirtyControlProps(!valuesEqual(form.sendingPathId, blank.sendingPathId))}
            >
              <option value="">— Select —</option>
              {paths.map((p) => (
                <option key={p.id} value={p.id}>
                  {p.name}
                </option>
              ))}
            </select>
          </label>
          <button type="submit" className={btnDirtyClass(dirty)} title={dirty ? 'Unsaved changes' : undefined}>
            Create List
          </button>
        </form>
      </div>
    )
  }

  if (detailId && detail && (saved || !detail.canManage)) {
    const base = saved || form
    return (
      <div className="card">
        <button type="button" className="btn secondary" onClick={onCloseDetail}>
          ← Lists
        </button>
        <h2>{detail.name}</h2>
        {detail.canManage ? (
          <form onSubmit={save}>
            <label className="field">
              {fieldLabel('name')}
              <input
                value={form.name}
                onChange={(ev) => setForm({ ...form, name: ev.target.value })}
                {...dirtyControlProps(!valuesEqual(form.name, base.name))}
              />
            </label>
            <label className="field">
              {fieldLabel('sending path')}
              <select
                value={form.sendingPathId}
                onChange={(ev) => setForm({ ...form, sendingPathId: ev.target.value })}
                {...dirtyControlProps(!valuesEqual(form.sendingPathId, base.sendingPathId))}
              >
                {paths.map((p) => (
                  <option key={p.id} value={p.id}>
                    {p.name}
                  </option>
                ))}
              </select>
            </label>
            <label className="field">
              {fieldLabel('footer')}
              <select
                value={form.footerId}
                onChange={(ev) => setForm({ ...form, footerId: ev.target.value })}
                {...dirtyControlProps(!valuesEqual(form.footerId, base.footerId))}
              >
                <option value="">— None —</option>
                {footers.map((f) => (
                  <option key={f.id} value={f.id}>
                    {f.name}
                  </option>
                ))}
              </select>
            </label>
            <label className="field">
              {fieldLabel('spamThreshold')}
              <input
                value={form.spamThreshold}
                onChange={(ev) => setForm({ ...form, spamThreshold: ev.target.value })}
                {...dirtyControlProps(!valuesEqual(form.spamThreshold, base.spamThreshold))}
              />
            </label>
            <button type="submit" className={btnDirtyClass(dirty)} title={dirty ? 'Unsaved changes' : undefined}>
              Save
            </button>
          </form>
        ) : (
          <p className="hint">View only (no manage grant).</p>
        )}

        <h3>Members On This List</h3>
        <RecordTable
          rows={listMembers}
          rowKey={(m) => m.id}
          onOpen={onOpenMember ? (m) => onOpenMember(m.id) : undefined}
          empty="No members on this list."
          columns={[
            { key: 'email', label: 'Email' },
            {
              key: 'enabled',
              label: 'Enabled',
              render: (m) => (m.enabled ? 'Yes' : 'No'),
            },
            {
              key: 'unsubscribed',
              label: 'Unsubscribed',
              render: (m) => (m.unsubscribed ? 'Yes' : 'No'),
            },
          ]}
        />

        {detail.canManage ? (
          <>
            <h3>Bulk Add</h3>
            <form onSubmit={bulkAdd}>
              <label className="field">
                Addresses (Newline, Comma, Or Semicolon)
                <textarea
                  rows={5}
                  style={{ maxWidth: '100%' }}
                  value={bulkText}
                  onChange={(ev) => setBulkText(ev.target.value)}
                  {...dirtyControlProps(bulkDirty)}
                />
              </label>
              <button
                type="submit"
                className={btnDirtyClass(bulkDirty)}
                title={bulkDirty ? 'Unsaved changes' : undefined}
              >
                Add members
              </button>
            </form>
            {bulkResult ? (
              <ul>
                <li>added: {bulkResult.added}</li>
                <li>skipped-invalid: {bulkResult.invalid}</li>
                <li>skipped-duplicate-in-paste: {bulkResult.duplicateInPaste}</li>
                <li>skipped-already-on-list: {bulkResult.alreadyOnList}</li>
                <li>skipped-unsubscribed: {bulkResult.unsubscribed}</li>
              </ul>
            ) : null}
          </>
        ) : null}
      </div>
    )
  }

  return (
    <div className="card">
      <div className="index-actions">
        <h2>Lists</h2>
        <button type="button" className="btn" onClick={() => onOpen(CREATE_ID)}>
          Create List
        </button>
      </div>
      <RecordTable
        rows={rows}
        rowKey={(r) => r.id}
        onOpen={(r) => onOpen(r.id)}
        empty="No lists yet."
        columns={[
          { key: 'name', label: 'Name' },
          {
            key: 'sendingPathId',
            label: 'Sending Path',
            render: (r) => paths.find((p) => p.id === r.sendingPathId)?.name || r.sendingPathId,
          },
        ]}
      />
    </div>
  )
}

export function MembersPage({ token, onError, detailId, onOpen, onCloseDetail }) {
  const [q, setQ] = useState('')
  const [rows, setRows] = useState([])
  const [detail, setDetail] = useState(null)
  const [disableReason, setDisableReason] = useState('')
  const [consentLists, setConsentLists] = useState([])
  const [consentMsg, setConsentMsg] = useState('')

  async function search(query) {
    try {
      setRows(await apiJson(token, membersSearchPath(query)))
    } catch (e) {
      onError(e.message)
    }
  }

  useEffect(() => {
    search('')
  }, [token])

  useEffect(() => {
    if (!detailId) {
      setDetail(null)
      setConsentMsg('')
      return
    }
    apiJson(token, `/api/members/${detailId}`)
      .then((d) => {
        setDetail(d)
        setConsentLists(d.memberships?.filter((m) => m.unsubscribed).map((m) => m.listId) || [])
      })
      .catch((e) => onError(e.message))
  }, [token, detailId])

  async function refreshDetail() {
    const d = await apiJson(token, `/api/members/${detailId}`)
    setDetail(d)
  }

  async function disable() {
    if (!window.confirm('Disable this member globally? They will not receive Campaign or MTS mail.')) {
      return
    }
    try {
      await apiJson(token, `/api/members/${detailId}`, {
        method: 'PUT',
        body: JSON.stringify({ enabled: false, disableReason }),
      })
      await refreshDetail()
    } catch (e) {
      onError(e.message)
    }
  }

  async function enable() {
    if (!window.confirm('Re-enable this member globally?')) return
    try {
      await apiJson(token, `/api/members/${detailId}`, {
        method: 'PUT',
        body: JSON.stringify({ enabled: true, confirm: true }),
      })
      await refreshDetail()
    } catch (e) {
      onError(e.message)
    }
  }

  async function requestConsent(e) {
    e.preventDefault()
    setConsentMsg('')
    try {
      await apiJson(token, `/api/members/${detailId}/consent-clear-unsub`, {
        method: 'POST',
        body: JSON.stringify({ listIds: consentLists }),
      })
      setConsentMsg(CONSENT_REQUEST_HINT)
    } catch (err) {
      onError(err.message)
    }
  }

  if (detailId && detail) {
    return (
      <div className="card">
        <button type="button" className="btn secondary" onClick={onCloseDetail}>
          ← Members
        </button>
        <h2>{detail.email}</h2>
        <p>
          {detail.enabled ? 'Enabled' : 'Disabled'}
          {!detail.enabled && detail.disableReason ? ` — ${detail.disableReason}` : ''}
        </p>
        <h3>Memberships</h3>
        <ul>
          {detail.memberships?.map((m) => (
            <li key={m.listId}>
              {m.listName} {m.unsubscribed ? '(unsubscribed)' : ''}
            </li>
          ))}
        </ul>

        <h3>Enable / disable</h3>
        {detail.enabled ? (
          <>
            <label className="field">
              {fieldLabel('disable reason')}
              <input
                value={disableReason}
                onChange={(ev) => setDisableReason(ev.target.value)}
                required
              />
            </label>
            <button type="button" className="btn secondary" onClick={disable}>
              Disable
            </button>
          </>
        ) : (
          <button type="button" className="btn" onClick={enable}>
            Re-enable
          </button>
        )}

        <h3>Consent clear-unsub</h3>
        <p className="hint">Sends notice mail only when Notice sending path is configured.</p>
        <form onSubmit={requestConsent}>
          {(detail.memberships || [])
            .filter((m) => m.unsubscribed)
            .map((m) => (
              <label key={m.listId} className="field">
                <input
                  type="checkbox"
                  checked={consentLists.includes(m.listId)}
                  onChange={(ev) => {
                    setConsentLists((prev) =>
                      ev.target.checked
                        ? [...prev, m.listId]
                        : prev.filter((id) => id !== m.listId),
                    )
                  }}
                />{' '}
                {m.listName}
              </label>
            ))}
          <button type="submit" className="btn secondary" disabled={!consentLists.length}>
            Request consent
          </button>
        </form>
        {consentMsg ? <p className="hint">{consentMsg}</p> : null}
      </div>
    )
  }

  return (
    <div className="card">
      <h2>Members</h2>
      <label className="field">
        Search
        <input
          value={q}
          onChange={(ev) => {
            setQ(ev.target.value)
            search(ev.target.value)
          }}
          placeholder="Email Contains…"
        />
      </label>
      <RecordTable
        rows={rows}
        rowKey={(r) => r.id}
        onOpen={(r) => onOpen(r.id)}
        empty={emptyHint('members')}
        columns={[
          { key: 'email', label: 'Email' },
          {
            key: 'enabled',
            label: 'Enabled',
            render: (r) => (r.enabled ? 'Yes' : 'No'),
          },
        ]}
      />
    </div>
  )
}
