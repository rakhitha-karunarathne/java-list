import { useEffect, useState } from 'react'
import { apiJson } from './api.js'
import { RecordTable } from './RecordTable.jsx'

export function ActivityLogPage({ token, onError, kind, title, hint }) {
  const [rows, setRows] = useState([])

  useEffect(() => {
    apiJson(token, `/api/activity-logs?kind=${kind}`)
      .then(setRows)
      .catch((e) => {
        setRows([])
        onError(e.message)
      })
  }, [token, kind])

  return (
    <div className="card">
      <h2>{title}</h2>
      <p className="hint">{hint}</p>
      <RecordTable
        rows={rows}
        rowKey={(r) => r.id}
        empty="No log entries yet."
        columns={[
          { key: 'createdAt', label: 'Time' },
          { key: 'event', label: 'Event' },
          { key: 'message', label: 'Message' },
        ]}
      />
    </div>
  )
}
