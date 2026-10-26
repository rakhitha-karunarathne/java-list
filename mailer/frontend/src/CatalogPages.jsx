import { useEffect, useRef, useState } from 'react'
import { apiJson } from './api.js'
import { confirmDelete } from './confirmDelete.js'
import { DeleteBlocked, UsedByList } from './catalogUi.jsx'
import { CREATE_ID, isCreatePage } from './createPage.js'
import {
  formatBooleanString,
  isBooleanString,
  parseBooleanString,
} from './booleanValue.js'
import {
  ConnectionTestBanner,
  testBounceImapConnection,
  testSmtpConnection,
} from './ConnectionTest.jsx'
import { btnDirtyClass, dirtyControlProps, isFormDirty, valuesEqual } from './formDirty.js'
import { fieldLabel } from './fieldLabel.js'
import { RecordTable } from './RecordTable.jsx'
import { Toggle } from './Toggle.jsx'
import { useRegisterUnsaved } from './Unsaved.jsx'
import { useDirtyForm } from './useDirtyForm.js'

export function SendingPathsPage({ token, onError, detailId, onOpen, onCloseDetail }) {
  const blank = useRef({
    name: '',
    host: '127.0.0.1',
    port: 25,
    username: '',
    password: '',
    fromEmail: '',
    fromName: '',
    useTls: false,
    useSsl: false,
  })
  const [rows, setRows] = useState([])
  const [detail, setDetail] = useState(null)
  const [form, setForm] = useState(() => ({ ...blank.current }))
  const [saved, setSaved] = useState(null)
  const [delErr, setDelErr] = useState(null)
  const [connTest, setConnTest] = useState(null)
  const [testing, setTesting] = useState(false)
  const creating = isCreatePage(detailId)
  const dirty = creating
    ? isFormDirty(form, blank.current)
    : !!(detailId && saved && isFormDirty(form, saved))
  useRegisterUnsaved(dirty, creating ? 'sending-path-create' : `sending-path-${detailId || 'x'}`)

  async function runSmtpTest() {
    setTesting(true)
    setConnTest(null)
    onError('')
    try {
      setConnTest(
        await testSmtpConnection(token, {
          host: form.host,
          port: form.port,
          username: form.username,
          password: form.password,
          useTls: !!form.useTls,
          useSsl: !!form.useSsl,
          pathId: creating ? null : detailId,
        }),
      )
    } catch (err) {
      setConnTest({ ok: false, message: err.message })
    } finally {
      setTesting(false)
    }
  }

  async function load() {
    try {
      setRows(await apiJson(token, '/api/sending-paths'))
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
      setSaved(null)
      setConnTest(null)
      if (isCreatePage(detailId)) setForm({ ...blank.current })
      return
    }
    apiJson(token, `/api/sending-paths/${detailId}`)
      .then((d) => {
        setDetail(d)
        const next = {
          name: d.name,
          host: d.host,
          port: d.port,
          username: d.username,
          password: '',
          fromEmail: d.fromEmail,
          fromName: d.fromName,
          useTls: d.useTls,
          useSsl: !!d.useSsl,
        }
        setForm(next)
        setSaved(next)
      })
      .catch((e) => onError(e.message))
  }, [token, detailId])

  async function create(e) {
    e.preventDefault()
    onError('')
    try {
      const created = await apiJson(token, '/api/sending-paths', {
        method: 'POST',
        body: JSON.stringify(form),
      })
      await load()
      setForm({ ...blank.current })
      onOpen(created.id)
    } catch (err) {
      onError(err.message)
    }
  }

  async function save(e) {
    e.preventDefault()
    onError('')
    try {
      const body = { ...form }
      if (!body.password) body.password = null
      await apiJson(token, `/api/sending-paths/${detailId}`, {
        method: 'PUT',
        body: JSON.stringify(body),
      })
      const d = await apiJson(token, `/api/sending-paths/${detailId}`)
      setDetail(d)
      const next = {
        name: d.name,
        host: d.host,
        port: d.port,
        username: d.username,
        password: '',
        fromEmail: d.fromEmail,
        fromName: d.fromName,
        useTls: d.useTls,
        useSsl: !!d.useSsl,
      }
      setForm(next)
      setSaved(next)
    } catch (err) {
      onError(err.message)
    }
  }

  async function remove() {
    if (!confirmDelete(`sending path “${detail?.name || form.name || detailId}”`)) return
    onError('')
    setDelErr(null)
    try {
      const res = await fetch(`/api/sending-paths/${detailId}`, {
        method: 'DELETE',
        headers: { Authorization: `Bearer ${token}` },
      })
      const text = await res.text()
      let body = null
      try {
        body = text ? JSON.parse(text) : null
      } catch {
        body = text
      }
      if (!res.ok) {
        setDelErr({ message: body?.message || `Delete failed (${res.status})`, body })
        return
      }
      onCloseDetail()
      load()
    } catch (err) {
      onError(err.message)
    }
  }

  if (creating) {
    const base = blank.current
    return (
      <div className="card">
        <button type="button" className="btn secondary" onClick={onCloseDetail}>
          {'←'} Sending Paths
        </button>
        <h2>Create Sending Path</h2>
        <form onSubmit={create}>
          <label className="field">
            {fieldLabel('name')}
            <input
              value={form.name}
              onChange={(ev) => setForm({ ...form, name: ev.target.value })}
              required
              {...dirtyControlProps(!valuesEqual(form.name, base.name))}
            />
          </label>
          <label className="field">
            {fieldLabel('host')}
            <input
              value={form.host}
              onChange={(ev) => setForm({ ...form, host: ev.target.value })}
              required
              {...dirtyControlProps(!valuesEqual(form.host, base.host))}
            />
          </label>
          <label className="field">
            {fieldLabel('port')}
            <input
              type="number"
              value={form.port}
              onChange={(ev) => setForm({ ...form, port: Number(ev.target.value) })}
              {...dirtyControlProps(!valuesEqual(form.port, base.port))}
            />
          </label>
          <label className="field">
            {fieldLabel('username')}
            <input
              value={form.username}
              onChange={(ev) => setForm({ ...form, username: ev.target.value })}
              required
              {...dirtyControlProps(!valuesEqual(form.username, base.username))}
            />
          </label>
          <label className="field">
            {fieldLabel('password')}
            <input
              type="password"
              value={form.password}
              onChange={(ev) => setForm({ ...form, password: ev.target.value })}
              required
              {...dirtyControlProps(!valuesEqual(form.password, base.password))}
            />
          </label>
          <label className="field">
            {fieldLabel('fromEmail')}
            <input
              value={form.fromEmail}
              onChange={(ev) => setForm({ ...form, fromEmail: ev.target.value })}
              required
              {...dirtyControlProps(!valuesEqual(form.fromEmail, base.fromEmail))}
            />
          </label>
          <label className="field">
            {fieldLabel('fromName')}
            <input
              value={form.fromName}
              onChange={(ev) => setForm({ ...form, fromName: ev.target.value })}
              {...dirtyControlProps(!valuesEqual(form.fromName, base.fromName))}
            />
          </label>
          <label className="field">
            {fieldLabel('useTls')}
            <Toggle
              checked={!!form.useTls}
              dirty={!valuesEqual(form.useTls, base.useTls)}
              aria-label={fieldLabel('useTls')}
              onChange={(on) =>
                setForm({ ...form, useTls: on, useSsl: on ? false : !!form.useSsl })
              }
            />
          </label>
          <label className="field">
            {fieldLabel('useSsl')}
            <Toggle
              checked={!!form.useSsl}
              dirty={!valuesEqual(form.useSsl, base.useSsl)}
              aria-label={fieldLabel('useSsl')}
              onChange={(on) =>
                setForm({ ...form, useSsl: on, useTls: on ? false : !!form.useTls })
              }
            />
          </label>
          <ConnectionTestBanner result={connTest} />
          <p>
            <button
              type="button"
              className="btn secondary"
              disabled={testing}
              onClick={runSmtpTest}
            >
              {testing ? 'Testing…' : 'Test Connection'}
            </button>{' '}
            <button type="submit" className={btnDirtyClass(dirty)} title={dirty ? 'Unsaved changes' : undefined}>
              Create Sending Path
            </button>
          </p>
        </form>
      </div>
    )
  }

  if (detailId && detail && saved) {
    return (
      <div className="page-sections">
        <section className="card">
          <button type="button" className="btn secondary" onClick={onCloseDetail}>
            {'←'} Sending Paths
          </button>
          <h2>{detail.name}</h2>
          <form onSubmit={save}>
            <label className="field">
              {fieldLabel('name')}
              <input
                value={form.name}
                onChange={(ev) => setForm({ ...form, name: ev.target.value })}
                required
                {...dirtyControlProps(!valuesEqual(form.name, saved.name))}
              />
            </label>
            <label className="field">
              {fieldLabel('host')}
              <input
                value={form.host}
                onChange={(ev) => setForm({ ...form, host: ev.target.value })}
                required
                {...dirtyControlProps(!valuesEqual(form.host, saved.host))}
              />
            </label>
            <label className="field">
              {fieldLabel('port')}
              <input
                type="number"
                value={form.port}
                onChange={(ev) => setForm({ ...form, port: Number(ev.target.value) })}
                {...dirtyControlProps(!valuesEqual(form.port, saved.port))}
              />
            </label>
            <label className="field">
              {fieldLabel('username')}
              <input
                value={form.username}
                onChange={(ev) => setForm({ ...form, username: ev.target.value })}
                required
                {...dirtyControlProps(!valuesEqual(form.username, saved.username))}
              />
            </label>
            <label className="field">
              {fieldLabel('password')} (Blank = Keep)
              <input
                type="password"
                value={form.password}
                onChange={(ev) => setForm({ ...form, password: ev.target.value })}
                autoComplete="new-password"
                {...dirtyControlProps(!valuesEqual(form.password, saved.password))}
              />
            </label>
            <label className="field">
              {fieldLabel('fromEmail')}
              <input
                value={form.fromEmail}
                onChange={(ev) => setForm({ ...form, fromEmail: ev.target.value })}
                required
                {...dirtyControlProps(!valuesEqual(form.fromEmail, saved.fromEmail))}
              />
            </label>
            <label className="field">
              {fieldLabel('fromName')}
              <input
                value={form.fromName}
                onChange={(ev) => setForm({ ...form, fromName: ev.target.value })}
                {...dirtyControlProps(!valuesEqual(form.fromName, saved.fromName))}
              />
            </label>
            <label className="field">
              {fieldLabel('useTls')}
              <Toggle
                checked={!!form.useTls}
                dirty={!valuesEqual(form.useTls, saved.useTls)}
                aria-label={fieldLabel('useTls')}
                onChange={(on) =>
                  setForm({ ...form, useTls: on, useSsl: on ? false : !!form.useSsl })
                }
              />
            </label>
            <label className="field">
              {fieldLabel('useSsl')}
              <Toggle
                checked={!!form.useSsl}
                dirty={!valuesEqual(form.useSsl, saved.useSsl)}
                aria-label={fieldLabel('useSsl')}
                onChange={(on) =>
                  setForm({ ...form, useSsl: on, useTls: on ? false : !!form.useTls })
                }
              />
            </label>
            <ConnectionTestBanner result={connTest} />
            <p>
              <button
                type="button"
                className="btn secondary"
                disabled={testing}
                onClick={runSmtpTest}
              >
                {testing ? 'Testing…' : 'Test Connection'}
              </button>{' '}
              <button type="submit" className={btnDirtyClass(dirty)} title={dirty ? 'Unsaved changes' : undefined}>
                Save
              </button>{' '}
              <button type="button" className="btn secondary" onClick={remove}>
                Delete
              </button>
            </p>
          </form>
          <DeleteBlocked err={delErr} onClose={() => setDelErr(null)} />
        </section>
        <section className="card">
          <h3>Used By</h3>
          <UsedByList usedBy={detail.usedBy} />
        </section>
      </div>
    )
  }

  return (
    <div className="card">
      <div className="index-actions">
        <h2>Sending Paths</h2>
        <button type="button" className="btn" onClick={() => onOpen(CREATE_ID)}>
          Create Sending Path
        </button>
      </div>
      <RecordTable
        rows={rows}
        rowKey={(r) => r.id}
        onOpen={(r) => onOpen(r.id)}
        empty="No sending paths yet."
        columns={[
          { key: 'name', label: 'Name' },
          {
            key: 'host',
            label: 'Host',
            render: (r) => `${r.host}:${r.port}`,
          },
          { key: 'fromEmail', label: 'From Email' },
        ]}
      />
    </div>
  )
}

export function FootersPage({ token, onError, detailId, onOpen, onCloseDetail }) {
  const creating = isCreatePage(detailId)
  const { form, setForm, saved, dirty, blank, adopt, resetToBlank } = useDirtyForm(
    { name: '', htmlBody: '', textBody: '' },
    creating ? 'footer-create' : `footer-${detailId || 'x'}`,
    creating,
  )
  const [rows, setRows] = useState([])
  const [detail, setDetail] = useState(null)
  const [delErr, setDelErr] = useState(null)

  async function load() {
    try {
      setRows(await apiJson(token, '/api/footers'))
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
      if (isCreatePage(detailId)) resetToBlank()
      return
    }
    apiJson(token, `/api/footers/${detailId}`)
      .then((d) => {
        setDetail(d)
        adopt({ name: d.name, htmlBody: d.htmlBody || '', textBody: d.textBody || '' })
      })
      .catch((e) => onError(e.message))
  }, [token, detailId])

  async function create(e) {
    e.preventDefault()
    try {
      const created = await apiJson(token, '/api/footers', {
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

  async function save(e) {
    e.preventDefault()
    try {
      await apiJson(token, `/api/footers/${detailId}`, {
        method: 'PUT',
        body: JSON.stringify(form),
      })
      const d = await apiJson(token, `/api/footers/${detailId}`)
      setDetail(d)
      adopt({ name: d.name, htmlBody: d.htmlBody || '', textBody: d.textBody || '' })
    } catch (err) {
      onError(err.message)
    }
  }

  async function remove() {
    if (!confirmDelete(`footer “${detail?.name || form.name || detailId}”`)) return
    setDelErr(null)
    const res = await fetch(`/api/footers/${detailId}`, {
      method: 'DELETE',
      headers: { Authorization: `Bearer ${token}` },
    })
    const text = await res.text()
    let body = null
    try {
      body = text ? JSON.parse(text) : null
    } catch {
      body = text
    }
    if (!res.ok) {
      setDelErr({ message: body?.message || `Delete failed (${res.status})`, body })
      return
    }
    onCloseDetail()
    load()
  }

  if (creating) {
    return (
      <div className="card">
        <button type="button" className="btn secondary" onClick={onCloseDetail}>
          ← Footers
        </button>
        <h2>Create Footer</h2>
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
            {fieldLabel('htmlBody')}
            <textarea
              rows={4}
              style={{ maxWidth: '100%' }}
              value={form.htmlBody}
              onChange={(ev) => setForm({ ...form, htmlBody: ev.target.value })}
              {...dirtyControlProps(!valuesEqual(form.htmlBody, blank.htmlBody))}
            />
          </label>
          <label className="field">
            {fieldLabel('textBody')}
            <textarea
              rows={3}
              style={{ maxWidth: '100%' }}
              value={form.textBody}
              onChange={(ev) => setForm({ ...form, textBody: ev.target.value })}
              {...dirtyControlProps(!valuesEqual(form.textBody, blank.textBody))}
            />
          </label>
          <button type="submit" className={btnDirtyClass(dirty)} title={dirty ? 'Unsaved changes' : undefined}>
            Create Footer
          </button>
        </form>
      </div>
    )
  }

  if (detailId && detail && saved) {
    return (
      <div className="page-sections">
        <section className="card">
          <button type="button" className="btn secondary" onClick={onCloseDetail}>
            ← Footers
          </button>
          <h2>{detail.name}</h2>
          <form onSubmit={save}>
            <label className="field">
              {fieldLabel('name')}
              <input
                value={form.name}
                onChange={(ev) => setForm({ ...form, name: ev.target.value })}
                {...dirtyControlProps(!valuesEqual(form.name, saved.name))}
              />
            </label>
            <label className="field">
              {fieldLabel('htmlBody')}
              <textarea
                rows={6}
                style={{ maxWidth: '100%' }}
                value={form.htmlBody}
                onChange={(ev) => setForm({ ...form, htmlBody: ev.target.value })}
                {...dirtyControlProps(!valuesEqual(form.htmlBody, saved.htmlBody))}
              />
            </label>
            <label className="field">
              {fieldLabel('textBody')}
              <textarea
                rows={4}
                style={{ maxWidth: '100%' }}
                value={form.textBody}
                onChange={(ev) => setForm({ ...form, textBody: ev.target.value })}
                {...dirtyControlProps(!valuesEqual(form.textBody, saved.textBody))}
              />
            </label>
            <p>
              <button type="submit" className={btnDirtyClass(dirty)} title={dirty ? 'Unsaved changes' : undefined}>
                Save
              </button>{' '}
              <button type="button" className="btn secondary" onClick={remove}>
                Delete
              </button>
            </p>
          </form>
          <DeleteBlocked err={delErr} onClose={() => setDelErr(null)} />
        </section>
        <section className="card">
          <h3>Used By</h3>
          <UsedByList usedBy={detail.usedBy} />
        </section>
      </div>
    )
  }

  return (
    <div className="card">
      <div className="index-actions">
        <h2>Footers</h2>
        <button type="button" className="btn" onClick={() => onOpen(CREATE_ID)}>
          Create Footer
        </button>
      </div>
      <RecordTable
        rows={rows}
        rowKey={(r) => r.id}
        onOpen={(r) => onOpen(r.id)}
        empty="No footers yet."
        columns={[{ key: 'name', label: 'Name' }]}
      />
    </div>
  )
}

export function BounceMailboxesPage({ token, onError, detailId, onOpen, onCloseDetail }) {
  const creating = isCreatePage(detailId)
  const { form, setForm, saved, dirty, blank, adopt, resetToBlank } = useDirtyForm(
    {
      name: '',
      host: '127.0.0.1',
      port: 143,
      username: '',
      password: '',
      useSsl: false,
      useTls: false,
      pollIntervalMinutes: 15,
    },
    creating ? 'bounce-create' : `bounce-${detailId || 'x'}`,
    creating,
  )
  const [rows, setRows] = useState([])
  const [detail, setDetail] = useState(null)
  const [connTest, setConnTest] = useState(null)
  const [testing, setTesting] = useState(false)

  async function runBounceImapTest() {
    setTesting(true)
    setConnTest(null)
    onError('')
    try {
      setConnTest(
        await testBounceImapConnection(token, {
          host: form.host,
          port: form.port,
          username: form.username,
          password: form.password,
          useSsl: !!form.useSsl,
          useTls: !!form.useTls,
          mailboxId: creating ? null : detailId,
        }),
      )
    } catch (err) {
      setConnTest({ ok: false, message: err.message })
    } finally {
      setTesting(false)
    }
  }

  async function load() {
    try {
      setRows(await apiJson(token, '/api/bounce-mailboxes'))
    } catch (e) {
      onError(e.message)
    }
  }

  useEffect(() => {
    load()
  }, [token])

  useEffect(() => {
    setConnTest(null)
    if (!detailId || isCreatePage(detailId)) {
      setDetail(null)
      if (isCreatePage(detailId)) resetToBlank()
      return
    }
    apiJson(token, `/api/bounce-mailboxes/${detailId}`)
      .then((d) => {
        setDetail(d)
        adopt({
          name: d.name,
          host: d.host,
          port: d.port,
          username: d.username,
          password: '',
          useSsl: d.useSsl,
          useTls: !!d.useTls,
          pollIntervalMinutes: d.pollIntervalMinutes,
        })
      })
      .catch((e) => onError(e.message))
  }, [token, detailId])

  async function create(e) {
    e.preventDefault()
    try {
      const created = await apiJson(token, '/api/bounce-mailboxes', {
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

  async function save(e) {
    e.preventDefault()
    try {
      const body = { ...form }
      if (!body.password) body.password = null
      await apiJson(token, `/api/bounce-mailboxes/${detailId}`, {
        method: 'PUT',
        body: JSON.stringify(body),
      })
      const d = await apiJson(token, `/api/bounce-mailboxes/${detailId}`)
      setDetail(d)
      adopt({
        name: d.name,
        host: d.host,
        port: d.port,
        username: d.username,
        password: '',
        useSsl: d.useSsl,
        useTls: !!d.useTls,
        pollIntervalMinutes: d.pollIntervalMinutes,
      })
    } catch (err) {
      onError(err.message)
    }
  }

  async function remove() {
    if (!confirmDelete(`bounce mail box “${detail?.name || form.name || detailId}”`)) return
    try {
      await apiJson(token, `/api/bounce-mailboxes/${detailId}`, { method: 'DELETE' })
      onCloseDetail()
      load()
    } catch (err) {
      onError(err.message)
    }
  }

  if (creating) {
    return (
      <div className="card">
        <button type="button" className="btn secondary" onClick={onCloseDetail}>
          ← Bounce Mailboxes
        </button>
        <h2>Add Mail Box</h2>
        <p className="hint">
          The mailbox is created on the mail server. This only adds it to the Bounce polling list.
        </p>
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
            {fieldLabel('host')}
            <input
              value={form.host}
              onChange={(ev) => setForm({ ...form, host: ev.target.value })}
              required
              {...dirtyControlProps(!valuesEqual(form.host, blank.host))}
            />
          </label>
          <label className="field">
            {fieldLabel('port')}
            <input
              type="number"
              value={form.port}
              onChange={(ev) => setForm({ ...form, port: Number(ev.target.value) })}
              {...dirtyControlProps(!valuesEqual(form.port, blank.port))}
            />
          </label>
          <label className="field">
            {fieldLabel('username')}
            <input
              value={form.username}
              onChange={(ev) => setForm({ ...form, username: ev.target.value })}
              required
              {...dirtyControlProps(!valuesEqual(form.username, blank.username))}
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
            {fieldLabel('pollIntervalMinutes')}
            <input
              type="number"
              value={form.pollIntervalMinutes}
              onChange={(ev) => setForm({ ...form, pollIntervalMinutes: Number(ev.target.value) })}
              {...dirtyControlProps(!valuesEqual(form.pollIntervalMinutes, blank.pollIntervalMinutes))}
            />
          </label>
          <label className="field">
            {fieldLabel('useSsl')}
            <Toggle
              checked={!!form.useSsl}
              dirty={!valuesEqual(form.useSsl, blank.useSsl)}
              aria-label={fieldLabel('useSsl')}
              onChange={(on) =>
                setForm({ ...form, useSsl: on, useTls: on ? false : !!form.useTls })
              }
            />
          </label>
          <label className="field">
            {fieldLabel('useTls')}
            <Toggle
              checked={!!form.useTls}
              dirty={!valuesEqual(form.useTls, blank.useTls)}
              aria-label={fieldLabel('useTls')}
              onChange={(on) =>
                setForm({ ...form, useTls: on, useSsl: on ? false : !!form.useSsl })
              }
            />
          </label>
          <ConnectionTestBanner result={connTest} />
          <p>
            <button
              type="button"
              className="btn secondary"
              disabled={testing}
              onClick={runBounceImapTest}
            >
              {testing ? 'Testing…' : 'Test Connection'}
            </button>{' '}
            <button type="submit" className={btnDirtyClass(dirty)} title={dirty ? 'Unsaved changes' : undefined}>
              Add Mail Box
            </button>
          </p>
        </form>
      </div>
    )
  }

  if (detailId && detail && saved) {
    return (
      <div className="card">
        <button type="button" className="btn secondary" onClick={onCloseDetail}>
          ← Bounce Mailboxes
        </button>
        <h2>{detail.name}</h2>
        <form onSubmit={save}>
          <label className="field">
            {fieldLabel('name')}
            <input
              value={form.name}
              onChange={(ev) => setForm({ ...form, name: ev.target.value })}
              {...dirtyControlProps(!valuesEqual(form.name, saved.name))}
            />
          </label>
          <label className="field">
            {fieldLabel('host')}
            <input
              value={form.host}
              onChange={(ev) => setForm({ ...form, host: ev.target.value })}
              {...dirtyControlProps(!valuesEqual(form.host, saved.host))}
            />
          </label>
          <label className="field">
            {fieldLabel('port')}
            <input
              type="number"
              value={form.port}
              onChange={(ev) => setForm({ ...form, port: Number(ev.target.value) })}
              {...dirtyControlProps(!valuesEqual(form.port, saved.port))}
            />
          </label>
          <label className="field">
            {fieldLabel('username')}
            <input
              value={form.username}
              onChange={(ev) => setForm({ ...form, username: ev.target.value })}
              {...dirtyControlProps(!valuesEqual(form.username, saved.username))}
            />
          </label>
          <label className="field">
            {fieldLabel('password')} (Blank = Keep)
            <input
              type="password"
              value={form.password}
              onChange={(ev) => setForm({ ...form, password: ev.target.value })}
              autoComplete="new-password"
              {...dirtyControlProps(!valuesEqual(form.password, saved.password))}
            />
          </label>
          <label className="field">
            {fieldLabel('pollIntervalMinutes')}
            <input
              type="number"
              value={form.pollIntervalMinutes}
              onChange={(ev) => setForm({ ...form, pollIntervalMinutes: Number(ev.target.value) })}
              {...dirtyControlProps(!valuesEqual(form.pollIntervalMinutes, saved.pollIntervalMinutes))}
            />
          </label>
          <label className="field">
            {fieldLabel('useSsl')}
            <Toggle
              checked={!!form.useSsl}
              dirty={!valuesEqual(form.useSsl, saved.useSsl)}
              aria-label={fieldLabel('useSsl')}
              onChange={(on) =>
                setForm({ ...form, useSsl: on, useTls: on ? false : !!form.useTls })
              }
            />
          </label>
          <label className="field">
            {fieldLabel('useTls')}
            <Toggle
              checked={!!form.useTls}
              dirty={!valuesEqual(form.useTls, saved.useTls)}
              aria-label={fieldLabel('useTls')}
              onChange={(on) =>
                setForm({ ...form, useTls: on, useSsl: on ? false : !!form.useSsl })
              }
            />
          </label>
          <ConnectionTestBanner result={connTest} />
          <p>
            <button
              type="button"
              className="btn secondary"
              disabled={testing}
              onClick={runBounceImapTest}
            >
              {testing ? 'Testing…' : 'Test Connection'}
            </button>{' '}
            <button type="submit" className={btnDirtyClass(dirty)} title={dirty ? 'Unsaved changes' : undefined}>
              Save
            </button>{' '}
            <button type="button" className="btn secondary" onClick={remove}>
              Delete
            </button>
          </p>
        </form>
      </div>
    )
  }

  return (
    <div className="card">
      <div className="index-actions">
        <h2>Bounce Mailboxes</h2>
        <button type="button" className="btn" onClick={() => onOpen(CREATE_ID)}>
          Add Mail Box
        </button>
      </div>
      <RecordTable
        rows={rows}
        rowKey={(r) => r.id}
        onOpen={(r) => onOpen(r.id)}
        empty="No bounce mailboxes yet."
        columns={[
          { key: 'name', label: 'Name' },
          { key: 'host', label: 'Host' },
          {
            key: 'pollIntervalMinutes',
            label: 'Poll Minutes',
            render: (r) => r.pollIntervalMinutes,
          },
        ]}
      />
    </div>
  )
}

export function ParametersPage({ token, onError }) {
  const [params, setParams] = useState([])
  const [drafts, setDrafts] = useState({})
  const [forbidden, setForbidden] = useState(false)
  const anyDirty = params.some((p) => !valuesEqual(drafts[p.key], p.value))
  useRegisterUnsaved(anyDirty, 'parameters')

  useEffect(() => {
    apiJson(token, '/api/parameters')
      .then((rows) => {
        setParams(rows)
        setDrafts(Object.fromEntries(rows.map((p) => [p.key, p.value])))
        setForbidden(false)
      })
      .catch((e) => {
        if (e.status === 403) {
          setForbidden(true)
          setParams([])
          return
        }
        onError(e.message)
      })
  }, [token])

  async function save(key) {
    onError('')
    try {
      const updated = await apiJson(token, `/api/parameters/${encodeURIComponent(key)}`, {
        method: 'PUT',
        body: JSON.stringify({ value: drafts[key] ?? '' }),
      })
      setParams((prev) => prev.map((p) => (p.key === key ? updated : p)))
      setDrafts((d) => ({ ...d, [key]: updated.value }))
    } catch (e) {
      onError(e.message)
    }
  }

  if (forbidden) {
    return (
      <div className="card">
        <h2>Parameters</h2>
        <p>Not available for this role.</p>
      </div>
    )
  }

  return (
    <div className="card">
      <h2>Parameters</h2>
      <p className="hint">Admin runtime settings — no SMTP/IMAP passwords or signing secrets here.</p>
      {params.map((p) => {
        const dirty = !valuesEqual(drafts[p.key], p.value)
        const asToggle = isBooleanString(p.value) || isBooleanString(drafts[p.key])
        return (
          <div key={p.key} style={{ marginBottom: '1.5rem' }}>
            <label className="field">
              {fieldLabel(p.key)}
              <span className="hint field-desc">{p.description}</span>
              <div className="field-control-row">
                {asToggle ? (
                  <Toggle
                    checked={parseBooleanString(drafts[p.key])}
                    dirty={dirty}
                    aria-label={fieldLabel(p.key)}
                    onChange={(on) =>
                      setDrafts((d) => ({ ...d, [p.key]: formatBooleanString(on) }))
                    }
                  />
                ) : (
                  <input
                    value={drafts[p.key] ?? ''}
                    onChange={(ev) => setDrafts((d) => ({ ...d, [p.key]: ev.target.value }))}
                    {...dirtyControlProps(dirty)}
                  />
                )}
                <button
                  type="button"
                  className={btnDirtyClass(dirty, 'btn secondary')}
                  title={dirty ? 'Unsaved changes' : undefined}
                  onClick={() => save(p.key)}
                >
                  Save
                </button>
              </div>
            </label>
          </div>
        )
      })}
    </div>
  )
}

export function NoticePathPage({ token, onError }) {
  const [paths, setPaths] = useState([])
  const [pathId, setPathId] = useState('')
  const [savedPathId, setSavedPathId] = useState('')
  const [loaded, setLoaded] = useState(false)
  const dirty = loaded && !valuesEqual(pathId, savedPathId)
  useRegisterUnsaved(dirty, 'notice-path')

  useEffect(() => {
    Promise.all([
      apiJson(token, '/api/sending-paths'),
      apiJson(token, '/api/notice-sending-path'),
    ])
      .then(([p, n]) => {
        setPaths(p)
        const id = n.sendingPathId || ''
        setPathId(id)
        setSavedPathId(id)
        setLoaded(true)
      })
      .catch((e) => onError(e.message))
  }, [token])

  async function save(e) {
    e.preventDefault()
    try {
      const out = await apiJson(token, '/api/notice-sending-path', {
        method: 'PUT',
        body: JSON.stringify({ sendingPathId: pathId || null }),
      })
      const id = out.sendingPathId || ''
      setPathId(id)
      setSavedPathId(id)
    } catch (err) {
      onError(err.message)
    }
  }

  return (
    <div className="card">
      <h2>Notice Sending Path</h2>
      <p className="hint">
        Used for system/consent/admin notices — not Campaign queue. Empty = notices are skipped
        until a path is set.
      </p>
      {!loaded ? <p className="hint">Loading…</p> : null}
      <form onSubmit={save}>
        <label className="field">
          Sending Path
          <select
            value={pathId}
            onChange={(ev) => setPathId(ev.target.value)}
            {...dirtyControlProps(dirty)}
          >
            <option value="">— Unset (No Notice Mail) —</option>
            {paths.map((p) => (
              <option key={p.id} value={p.id}>
                {p.name}
              </option>
            ))}
          </select>
        </label>
        {!pathId ? <p className="hint">Notice path is unset.</p> : null}
        <button type="submit" className={btnDirtyClass(dirty)} title={dirty ? 'Unsaved changes' : undefined}>
          Save
        </button>
      </form>
    </div>
  )
}
