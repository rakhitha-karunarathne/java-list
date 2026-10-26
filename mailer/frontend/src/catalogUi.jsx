/** Shared catalog index/detail helpers (Used By + delete conflict). */

export function UsedByList({ usedBy }) {
  if (!usedBy?.length) return <p className="hint">Not referenced.</p>
  return (
    <ul>
      {usedBy.map((u) => (
        <li key={`${u.sourceType}-${u.sourceId}`}>
          {u.label} <span className="hint">({u.sourceType})</span>
        </li>
      ))}
    </ul>
  )
}

export function DeleteBlocked({ err, onClose }) {
  if (!err) return null
  const used = err.body?.usedBy
  return (
    <div className="card" style={{ borderColor: 'var(--danger)' }}>
      <p className="error">{err.message || 'Delete blocked'}</p>
      {used ? <UsedByList usedBy={used} /> : null}
      <button type="button" className="btn secondary" onClick={onClose}>
        OK
      </button>
    </div>
  )
}
