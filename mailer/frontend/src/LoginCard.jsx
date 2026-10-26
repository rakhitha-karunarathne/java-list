import { useState } from 'react'

/**
 * Email + password; MFA step-up when API returns mfaRequired.
 * enrollmentRequired: block with message (Security enroll on User detail later).
 */
export default function LoginCard({ onToken, onError }) {
  const [email, setEmail] = useState('admin@example.com')
  const [password, setPassword] = useState('changeme')
  const [step, setStep] = useState('password') // password | mfa
  const [mfaToken, setMfaToken] = useState('')
  const [code, setCode] = useState('')
  const [localError, setLocalError] = useState('')
  const [busy, setBusy] = useState(false)

  async function submitPassword(e) {
    e.preventDefault()
    setLocalError('')
    onError('')
    setBusy(true)
    try {
      const res = await fetch('/api/auth/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email, password }),
      })
      if (!res.ok) {
        setLocalError('Sign in failed')
        return
      }
      const body = await res.json()
      if (body.token) {
        onToken(body.token)
        return
      }
      if (body.mfaRequired && body.mfaToken) {
        setMfaToken(body.mfaToken)
        setStep('mfa')
        setCode('')
        return
      }
      if (body.enrollmentRequired) {
        setLocalError(
          'MFA enrollment required. An Admin must complete Security enroll on User detail before a full session.',
        )
        return
      }
      setLocalError('Sign in failed')
    } catch (err) {
      setLocalError(err.message || String(err))
    } finally {
      setBusy(false)
    }
  }

  async function submitMfa(e) {
    e.preventDefault()
    setLocalError('')
    onError('')
    setBusy(true)
    try {
      const res = await fetch('/api/auth/mfa', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ mfaToken, code: code.trim() }),
      })
      if (!res.ok) {
        setLocalError('Authenticator code invalid')
        return
      }
      const body = await res.json()
      if (!body.token) {
        setLocalError('Authenticator code invalid')
        return
      }
      onToken(body.token)
    } catch (err) {
      setLocalError(err.message || String(err))
    } finally {
      setBusy(false)
    }
  }

  return (
    <div className="login-wrap">
      <div className="card login-card">
        <h2 style={{ marginTop: 0 }}>List Mailer</h2>
        <p className="hint">Sign in with your email</p>
        {step === 'password' ? (
          <form onSubmit={submitPassword}>
            <label className="field">
              Email
              <input
                type="email"
                value={email}
                onChange={(ev) => setEmail(ev.target.value)}
                autoComplete="username"
                required
              />
            </label>
            <label className="field">
              Password
              <input
                type="password"
                value={password}
                onChange={(ev) => setPassword(ev.target.value)}
                autoComplete="current-password"
                required
              />
            </label>
            <p style={{ marginTop: '1rem' }}>
              <button type="submit" className="btn" disabled={busy}>
                Sign in
              </button>
            </p>
          </form>
        ) : (
          <form onSubmit={submitMfa}>
            <p className="hint">
              Enter your authenticator code. A backup code also works if your Admin provided one.
            </p>
            <label className="field">
              Authenticator code
              <input
                type="text"
                inputMode="text"
                autoComplete="one-time-code"
                value={code}
                onChange={(ev) => setCode(ev.target.value)}
                required
                aria-describedby="mfa-hint"
              />
            </label>
            <p id="mfa-hint" className="hint">
              Backup code accepted when authenticator is unavailable.
            </p>
            <p style={{ marginTop: '1rem' }}>
              <button type="submit" className="btn" disabled={busy}>
                Verify
              </button>{' '}
              <button
                type="button"
                className="btn secondary"
                onClick={() => {
                  setStep('password')
                  setMfaToken('')
                  setCode('')
                }}
              >
                Back
              </button>
            </p>
          </form>
        )}
        {localError ? <p className="error">{localError}</p> : null}
      </div>
    </div>
  )
}
