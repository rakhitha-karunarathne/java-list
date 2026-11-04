import QRCode from 'qrcode'

/** Client-side PNG data URL for an otpauth:// URI (never send secrets to a third party). */
export function otpAuthQrDataUrl(otpAuthUrl) {
  return QRCode.toDataURL(String(otpAuthUrl || ''), {
    width: 220,
    margin: 2,
    errorCorrectionLevel: 'M',
  })
}
