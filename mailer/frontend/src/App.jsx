import { useEffect, useState } from 'react'
import './App.css'

function App() {
  const [ping, setPing] = useState('…')

  useEffect(() => {
    fetch('/api/ping')
      .then((r) => r.json())
      .then((body) => setPing(body.status ?? JSON.stringify(body)))
      .catch((err) => setPing(String(err)))
  }, [])

  return (
    <main style={{ fontFamily: 'system-ui', padding: '2rem' }}>
      <h1>List Mailer</h1>
      <p>Operator shell (platform tracer).</p>
      <p>
        <code>/api/ping</code>: <strong>{ping}</strong>
      </p>
    </main>
  )
}

export default App
