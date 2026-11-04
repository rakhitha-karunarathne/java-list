import { useEffect, useState } from 'react'
import { otpAuthQrDataUrl } from './mfaQr.js'

/** Renders a scannable QR for an MFA otpauth:// URL. */
export function MfaQr({ otpAuthUrl }) {
  const [src, setSrc] = useState('')
  const [failed, setFailed] = useState(false)

  useEffect(() => {
    let cancelled = false
    setSrc('')
    setFailed(false)
    if (!otpAuthUrl) return undefined
    otpAuthQrDataUrl(otpAuthUrl)
      .then((url) => {
        if (!cancelled) setSrc(url)
      })
      .catch(() => {
        if (!cancelled) setFailed(true)
      })
    return () => {
      cancelled = true
    }
  }, [otpAuthUrl])

  if (failed) return <p className="hint">Could not render QR — use the secret below.</p>
  if (!src) return <p className="hint">Preparing QR…</p>
  return (
    <img
      className="mfa-qr"
      src={src}
      alt="Scan with authenticator app"
      width={220}
      height={220}
    />
  )
}
