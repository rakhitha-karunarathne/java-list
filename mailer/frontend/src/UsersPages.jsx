import { useEffect, useMemo, useState } from 'react'
import { apiJson } from './api.js'
import { decodeJwtPayload } from './auth.js'
import { confirmDelete } from './confirmDelete.js'
import { CREATE_ID, isCreatePage } from './createPage.js'
import { btnDirtyClass, dirtyControlProps, valuesEqual } from './formDirty.js'
import { fieldLabel } from './fieldLabel.js'
import { RecordTable } from './RecordTable.jsx'
import { Toggle } from './Toggle.jsx'
import { useRegisterUnsaved } from './Unsaved.jsx'
import { useDirtyForm } from './useDirtyForm.js'

const ROLES = ['ADMIN', 'LIST_MANAGER', 'LIST_SENDER']

function isGranted(g) {
  return !!(g && (g.manage || g.send))
}

/** Role decides capabilities: Managers manage+send; Senders send only. */
function flagsForRole(role) {
  if (role === 'LIST_MANAGER') return { manage: true, send: true }
  if (role === 'LIST_SENDER') return { manage: false, send: true }
  return { manage: false, send: false }
}

function grantsSig(gs) {
  return JSON.stringify(
    [...(gs || [])]
      .filter(isGranted)
      .map((g) => g.listId)
      .sort((a, b) => String(a).localeCompare(String(b))),
  )
}

export function UsersPage({ token, onError, detailId, onOpen, onCloseDetail }) {
  const creating = isCreatePage(detailId)
  const { form, setForm, dirty: createDirty, blank, resetToBlank } = useDirtyForm(
    { email: '', password: '', role: 'LIST_MANAGER' },
    creating ? 'user-create' : 'user-create-idle',
    creating,
  )
  const [rows, setRows] = useState([])
  const [detail, setDetail] = useState(null)
  const [grants, setGrants] = useState([])
  const [savedGrants, setSavedGrants] = useState([])
  const [lists, setLists] = useState([])
  const [patch, setPatch] = useState({ role: 'LIST_MANAGER', enabled: true })
  const [savedPatch, setSavedPatch] = useState(null)
  const me = useMemo(() => decodeJwtPayload(token), [token])
  const isSelf = detail && me?.sub === detail.id
  const patchDirty = !!(savedPatch && !valuesEqual(patch.role, savedPatch.role)) ||
    !!(savedPatch && !valuesEqual(patch.enabled, savedPatch.enabled))
  const grantsDirty = grantsSig(grants) !== grantsSig(savedGrants)
  const showListGrants =
    patch.role === 'LIST_MANAGER' || patch.role === 'LIST_SENDER'
  useRegisterUnsaved(patchDirty, `user-patch-${detailId || 'x'}`)
  useRegisterUnsaved(showListGrants && grantsDirty, `user-grants-${detailId || 'x'}`)

  async function load() {
    try {
      setRows(await apiJson(token, '/api/users'))
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
      setSavedPatch(null)
      if (isCreatePage(detailId)) resetToBlank()
      return
    }
    Promise.all([
      apiJson(token, '/api/users').then((all) => all.find((u) => u.id === detailId)),
      apiJson(token, `/api/users/${detailId}/grants`),
      apiJson(token, '/api/lists').catch(() => []),
    ])
      .then(([u, g, ls]) => {
        if (!u) throw new Error('user not found')
        setDetail(u)
        const p = { role: u.role, enabled: u.enabled }
        setPatch(p)
        setSavedPatch(p)
        setGrants(g)
        setSavedGrants(g)
        setLists(ls)
      })
      .catch((e) => onError(e.message))
  }, [token, detailId])

  async function create(e) {
    e.preventDefault()
    try {
      const created = await apiJson(token, '/api/users', {
        method: 'POST',
        body: JSON.stringify(form),
      })
      await load()
      resetToBlank()
      onOpen(created.id)
    } catch (err) {
      onError(err.message)
    }
  }

  async function savePatch(e) {
    e.preventDefault()
    try {
      const updated = await apiJson(token, `/api/users/${detailId}`, {
        method: 'PATCH',
        body: JSON.stringify(patch),
      })
      setDetail(updated)
      const p = { role: updated.role, enabled: updated.enabled }
      setPatch(p)
      setSavedPatch(p)
      await load()
    } catch (err) {
      onError(err.message)
    }
  }

  async function saveGrants(e) {
    e.preventDefault()
    try {
      const flags = flagsForRole(patch.role)
      const body = grants
        .filter((g) => g.listId && isGranted(g))
        .map((g) => ({ listId: g.listId, ...flags }))
      const next = await apiJson(token, `/api/users/${detailId}/grants`, {
        method: 'PUT',
        body: JSON.stringify(body),
      })
      setGrants(next)
      setSavedGrants(next)
    } catch (err) {
      onError(err.message)
    }
  }

  function toggleGranted(listId) {
    setGrants((prev) => {
      const existing = prev.find((g) => g.listId === listId)
      if (isGranted(existing)) {
        return prev.filter((g) => g.listId !== listId)
      }
      return [...prev.filter((g) => g.listId !== listId), { listId, ...flagsForRole(patch.role) }]
    })
  }

  if (creating) {
    return (
      <div className="card">
        <button type="button" className="btn secondary" onClick={onCloseDetail}>
          ← Users & Grants
        </button>
        <h2>Create User</h2>
        <form onSubmit={create}>
          <label className="field">
            {fieldLabel('email')}
            <input
              value={form.email}
              onChange={(ev) => setForm({ ...form, email: ev.target.value })}
              required
              {...dirtyControlProps(!valuesEqual(form.email, blank.email))}
            />
          </label>
          <label className="field">
            {fieldLabel('password')}
            <input
              type="password"
              value={form.password}
              onChange={(ev) => setForm({ ...form, password: ev.target.value })}
              required
              {...dirtyControlProps(!valuesEqual(form.password, blank.password))}
            />
          </label>
          <label className="field">
            {fieldLabel('role')}
            <select
              value={form.role}
              onChange={(ev) => setForm({ ...form, role: ev.target.value })}
              {...dirtyControlProps(!valuesEqual(form.role, blank.role))}
            >
              {ROLES.map((r) => (
                <option key={r} value={r}>
                  {r}
                </option>
              ))}
            </select>
          </label>
          <button
            type="submit"
            className={btnDirtyClass(createDirty)}
            title={createDirty ? 'Unsaved changes' : undefined}
          >
            Create User
          </button>
        </form>
      </div>
    )
  }

  if (detailId && detail && savedPatch) {
    return (
      <div className="page-sections">
        <section className="card">
          <button type="button" className="btn secondary" onClick={onCloseDetail}>
            ← Users & Grants
          </button>
          <h2>{detail.email}</h2>
        </section>

        <section className="card">
          <h3>Account</h3>
          <form onSubmit={savePatch}>
            <label className="field">
              {fieldLabel('role')}
              <select
                value={patch.role}
                onChange={(ev) => setPatch({ ...patch, role: ev.target.value })}
                {...dirtyControlProps(!valuesEqual(patch.role, savedPatch.role))}
              >
                {ROLES.map((r) => (
                  <option key={r} value={r}>
                    {r}
                  </option>
                ))}
              </select>
            </label>
            <label className="field">
              {fieldLabel('enabled')}
              <Toggle
                checked={!!patch.enabled}
                dirty={!valuesEqual(patch.enabled, savedPatch.enabled)}
                aria-label={fieldLabel('enabled')}
                onChange={(on) => setPatch({ ...patch, enabled: on })}
              />
            </label>
            <button
              type="submit"
              className={btnDirtyClass(patchDirty)}
              title={patchDirty ? 'Unsaved changes' : undefined}
            >
              Save
            </button>
          </form>
        </section>

        <section className="card">
          <h3>List Grants</h3>
          {showListGrants ? (
            <>
              <p className="hint">
                {patch.role === 'LIST_MANAGER'
                  ? 'Granted lists: manage and send.'
                  : 'Granted lists: send only.'}
              </p>
              <form onSubmit={saveGrants}>
                <RecordTable
                  rows={lists}
                  rowKey={(l) => l.id}
                  empty="No lists yet."
                  columns={[
                    { key: 'name', label: 'List' },
                    {
                      key: 'granted',
                      label: 'Granted',
                      stopRowClick: true,
                      render: (l) => {
                        const granted = isGranted(grants.find((x) => x.listId === l.id))
                        const saved = isGranted(savedGrants.find((x) => x.listId === l.id))
                        return (
                          <input
                            type="checkbox"
                            checked={granted}
                            onChange={() => toggleGranted(l.id)}
                            aria-label={`Grant ${l.name}`}
                            {...dirtyControlProps(granted !== saved)}
                          />
                        )
                      },
                    },
                  ]}
                />
                <p>
                  <button
                    type="submit"
                    className={btnDirtyClass(grantsDirty, 'btn secondary')}
                    title={grantsDirty ? 'Unsaved changes' : undefined}
                  >
                    Save Grants
                  </button>
                </p>
              </form>
            </>
          ) : (
            <p className="hint">List grants do not apply to Admin — full access.</p>
          )}
        </section>

        <section className="card">
          <SecurityMfaPanel token={token} onError={onError} self={isSelf} />
        </section>
      </div>
    )
  }

  return (
    <div className="card">
      <div className="index-actions">
        <h2>Users & Grants</h2>
        <button type="button" className="btn" onClick={() => onOpen(CREATE_ID)}>
          Create User
        </button>
      </div>
      <RecordTable
        rows={rows}
        rowKey={(r) => r.id}
        onOpen={(r) => onOpen(r.id)}
        empty="No users yet."
        columns={[
          { key: 'email', label: 'Email' },
          { key: 'role', label: 'Role' },
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

function SecurityMfaPanel({ token, onError, self }) {
  const [otpAuthUrl, setOtpAuthUrl] = useState('')
  const [code, setCode] = useState('')
  const [backupCodes, setBackupCodes] = useState(null)
  const [unenrollCode, setUnenrollCode] = useState('')
  useRegisterUnsaved(!!code.trim() || !!unenrollCode.trim(), 'user-mfa')

  if (!self) {
    return (
      <>
        <h3>Security</h3>
        <p className="hint">MFA enroll/unenroll is self-service via /api/me/mfa (open your own user).</p>
      </>
    )
  }

  async function setup() {
    onError('')
    setBackupCodes(null)
    try {
      const out = await apiJson(token, '/api/me/mfa/setup', { method: 'POST' })
      setOtpAuthUrl(out.otpAuthUrl || '')
    } catch (e) {
      onError(e.message)
    }
  }

  async function confirm(e) {
    e.preventDefault()
    try {
      const out = await apiJson(token, '/api/me/mfa/confirm', {
        method: 'POST',
        body: JSON.stringify({ code }),
      })
      setBackupCodes(out.backupCodes || [])
      setOtpAuthUrl('')
      setCode('')
    } catch (err) {
      onError(err.message)
    }
  }

  async function unenroll(e) {
    e.preventDefault()
    if (!confirmDelete('MFA enrollment for this account')) return
    try {
      await apiJson(token, '/api/me/mfa', {
        method: 'DELETE',
        body: JSON.stringify({ code: unenrollCode }),
      })
      setUnenrollCode('')
      setBackupCodes(null)
      setOtpAuthUrl('')
    } catch (err) {
      onError(err.message)
    }
  }

  return (
    <>
      <h3>Security</h3>
      <p className="hint">MFA lives on user detail only — not a sidebar destination (C8).</p>
      <p>
        <button type="button" className="btn secondary" onClick={setup}>
          Enroll MFA
        </button>
      </p>
      {otpAuthUrl ? (
        <div>
          <p className="hint">Scan otpauth URL (QR apps accept the URI):</p>
          <code style={{ wordBreak: 'break-all' }}>{otpAuthUrl}</code>
          <form onSubmit={confirm}>
            <label className="field">
              {fieldLabel('verification code')}
              <input
                value={code}
                onChange={(ev) => setCode(ev.target.value)}
                required
                {...dirtyControlProps(!!code.trim())}
              />
            </label>
            <button
              type="submit"
              className={btnDirtyClass(!!code.trim())}
              title={code.trim() ? 'Unsaved changes' : undefined}
            >
              Confirm MFA
            </button>
          </form>
        </div>
      ) : null}
      {backupCodes ? (
        <div>
          <p>
            <strong>Backup codes (shown once)</strong>
          </p>
          <ul>
            {backupCodes.map((c) => (
              <li key={c}>
                <code>{c}</code>
              </li>
            ))}
          </ul>
        </div>
      ) : null}
      <form onSubmit={unenroll}>
        <label className="field">
          {fieldLabel('unenroll')} (TOTP Or Backup Code)
          <input
            value={unenrollCode}
            onChange={(ev) => setUnenrollCode(ev.target.value)}
            required
            {...dirtyControlProps(!!unenrollCode.trim())}
          />
        </label>
        <button
          type="submit"
          className={btnDirtyClass(!!unenrollCode.trim(), 'btn secondary')}
          title={unenrollCode.trim() ? 'Unsaved changes' : undefined}
        >
          Unenroll MFA
        </button>
      </form>
    </>
  )
}
