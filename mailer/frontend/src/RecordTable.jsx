/** Index tables for record lists — row click opens detail; never a stack of name-buttons. */

export function RecordTable({ columns, rows, rowKey, onOpen, empty = 'No records.' }) {
  if (!rows?.length) {
    return <p className="hint">{empty}</p>
  }
  return (
    <div className="table-scroll">
      <table className="data-table">
        <thead>
          <tr>
            {columns.map((c) => (
              <th key={c.key}>{c.label}</th>
            ))}
          </tr>
        </thead>
        <tbody>
          {rows.map((row) => {
            const id = rowKey(row)
            return (
              <tr
                key={id}
                className={onOpen ? 'row-link' : undefined}
                tabIndex={onOpen ? 0 : undefined}
                onClick={onOpen ? () => onOpen(row) : undefined}
                onKeyDown={
                  onOpen
                    ? (ev) => {
                        if (ev.key === 'Enter' || ev.key === ' ') {
                          ev.preventDefault()
                          onOpen(row)
                        }
                      }
                    : undefined
                }
              >
                {columns.map((c) => (
                  <td key={c.key} onClick={c.stopRowClick ? (ev) => ev.stopPropagation() : undefined}>
                    {c.render ? c.render(row) : row[c.key]}
                  </td>
                ))}
              </tr>
            )
          })}
        </tbody>
      </table>
    </div>
  )
}
