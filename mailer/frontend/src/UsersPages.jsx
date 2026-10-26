import { useEffect, useMemo, useState } from 'react'
import { apiJson } from './api.js'
import { decodeJwtPayload } from './auth.js'
import { fieldLabel } from './fieldLabel.js'

const ROLES = ['ADMIN', 'LIST_MANAGER', 'LIST_SENDER']

export function UsersPage({ token, onError, detailId, onOpen, onCloseDetail }) {
  const [rows, setRows] = useState([])
  const [detail, setDetail] = useState(null)
  const [grants, setGrants] = useState([])
  const [lists, setLists] = useState([])
  const [form, setForm] = useState({ email: '', password: '', role: 'LIST_MANAGER' })
  const [patch, setPatch] = useState({ role: 'LIST_MANAGER', enabled: true })
  const me = useMemo(() => decodeJwtPayload(token), [token])
  const isSelf = detail && me?.sub === detail.id

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
    if (!detailId) {
      setDetail(null)
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
        setPatch({ role: u.role, enabled: u.enabled })
        setGrants(g)
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
      setForm({ email: '', password: '', role: 'LIST_MANAGER' })
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
      await load()
    } catch (err) {
      onError(err.message)
    }
  }

  async function saveGrants(e) {
    e.preventDefault()
    try {
      const next = await apiJson(token, `/api/users/${detailId}/grants`, {
        method: 'PUT',
        body: JSON.stringify(grants.filter((g) => g.listId && (g.manage || g.send))),
      })
      setGrants(next)
    } catch (err) {
      onError(err.message)
    }
  }

  function toggleGrant(listId, field) {
    setGrants((prev) => {
      const existing = prev.find((g) => g.listId === listId)
      if (!existing) {
        return [...prev, { listId, manage: field === 'manage', send: field === 'send' }]
      }
      const next = { ...existing, [field]: !existing[field] }
      if (!next.manage && !next.send) return prev.filter((g) => g.listId !== listId)
      return prev.map((g) => (g.listId === listId ? next : g))
    })
  }

  if (detailId && detail) {
    return (
      <div className="card">
        <button type="button" className="btn secondary" onClick={onCloseDetail}>
          ← Users & Grants
        </button>
        <h2>{detail.email}</h2>
        <form onSubmit={savePatch}>
          <label className="field">
            {fieldLabel('role')}
            <select
              value={patch.role}
              onChange={(ev) => setPatch({ ...patch, role: ev.target.value })}
            >
              {ROLES.map((r) => (
                <option key={r} value={r}>
                  {r}
                </option>
              ))}
            </select>
          </label>
          <label className="field">
            <input
              type="checkbox"
              checked={patch.enabled}
              onChange={(ev) => setPatch({ ...patch, enabled: ev.target.checked })}
            />{' '}
            {fieldLabel('enabled')}
          </label>
          <button type="submit" className="btn">
            Save user
          </button>
        </form>

        <h3>List grants</h3>
        <form onSubmit={saveGrants}>
          <ul style={{ listStyle: 'none', padding: 0 }}>
            {lists.map((l) => {
              const g = grants.find((x) => x.listId === l.id) || { manage: false, send: false }
              return (
                <li key={l.id}>
                  <strong>{l.name}</strong>{' '}
                  <label>
                    <input
                      type="checkbox"
                      checked={!!g.manage}
                      onChange={() => toggleGrant(l.id, 'manage')}
                    />{' '}
                    Manage
                  </label>{' '}
                  <label>
                    <input
                      type="checkbox"
                      checked={!!g.send}
                      onChange={() => toggleGrant(l.id, 'send')}
                    />{' '}
                    Send
                  </label>
                </li>
              )
            })}
          </ul>
          <button type="submit" className="btn secondary">
            Save Grants
          </button>
        </form>

        <SecurityMfaPanel token={token} onError={onError} self={isSelf} />
      </div>
    )
  }

  return (
    <div className="card">
      <h2>Users & Grants</h2>
      <ul>
        {rows.map((r) => (
          <li key={r.id}>
            <button type="button" className="btn secondary" onClick={() => onOpen(r.id)}>
              {r.email}
            </button>{' '}
            {r.role} {r.enabled ? '' : '(disabled)'}
          </li>
        ))}
      </ul>
      <h3>Create</h3>
      <form onSubmit={create}>
        <label className="field">
          {fieldLabel('email')}
          <input
            value={form.email}
            onChange={(ev) => setForm({ ...form, email: ev.target.value })}
            required
          />
        </label>
        <label className="field">
          {fieldLabel('password')}
          <input
            type="password"
            value={form.password}
            onChange={(ev) => setForm({ ...form, password: ev.target.value })}
            required
          />
        </label>
        <label className="field">
          {fieldLabel('role')}
          <select value={form.role} onChange={(ev) => setForm({ ...form, role: ev.target.value })}>
            {ROLES.map((r) => (
              <option key={r} value={r}>
                {r}
              </option>
            ))}
          </select>
        </label>
        <button type="submit" className="btn">
          Create
        </button>
      </form>
    </div>
  )
}

function SecurityMfaPanel({ token, onError, self }) {
  const [otpAuthUrl, setOtpAuthUrl] = useState('')
  const [code, setCode] = useState('')
  const [backupCodes, setBackupCodes] = useState(null)
  const [unenrollCode, setUnenrollCode] = useState('')

  if (!self) {
    return (
      <div style={{ marginTop: '1.5rem' }}>
        <h3>Security</h3>
        <p className="hint">MFA enroll/unenroll is self-service via /api/me/mfa (open your own user).</p>
      </div>
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
    <div style={{ marginTop: '1.5rem' }}>
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
              <input value={code} onChange={(ev) => setCode(ev.target.value)} required />
            </label>
            <button type="submit" className="btn">
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
          />
        </label>
        <button type="submit" className="btn secondary">
          Unenroll MFA
        </button>
      </form>
    </div>
  )
}
