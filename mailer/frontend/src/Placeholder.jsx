export default function Placeholder({ title, hint }) {
  return (
    <div className="card">
      <h2>{title}</h2>
      <p className="hint">
        {hint ||
          'Coming in a later frontend epic. Index only — detail pages open from rows, not the sidebar (C8).'}
      </p>
    </div>
  )
}
