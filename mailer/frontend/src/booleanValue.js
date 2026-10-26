export function isBooleanString(value) {
  const v = String(value ?? '').trim().toLowerCase()
  return v === 'true' || v === 'false'
}

export function parseBooleanString(value) {
  return String(value ?? '').trim().toLowerCase() === 'true'
}

export function formatBooleanString(on) {
  return on ? 'true' : 'false'
}
