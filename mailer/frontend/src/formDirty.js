/** Compare form values for unsaved-change detection. */
export function valuesEqual(a, b) {
  if (typeof a === 'boolean' || typeof b === 'boolean') return Boolean(a) === Boolean(b)
  if (Array.isArray(a) || Array.isArray(b)) {
    return JSON.stringify(a ?? []) === JSON.stringify(b ?? [])
  }
  return String(a ?? '') === String(b ?? '')
}

export function isFormDirty(form, baseline) {
  if (!form || !baseline) return false
  return Object.keys(form).some((k) => !valuesEqual(form[k], baseline[k]))
}

export function dirtyClass(changed, base = '') {
  const extra = changed ? 'is-dirty' : ''
  return [base, extra].filter(Boolean).join(' ') || undefined
}

export function btnDirtyClass(changed, base = 'btn') {
  return dirtyClass(changed, base)
}

export function dirtyControlProps(changed) {
  return {
    className: dirtyClass(changed),
    title: changed ? 'Unsaved changes' : undefined,
  }
}
