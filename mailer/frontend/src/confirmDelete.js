/** Confirm before permanent delete. Returns true if the operator proceeds. */
export function confirmDelete(what) {
  const label = what && String(what).trim() ? String(what).trim() : 'this'
  return globalThis.confirm(
    `Delete ${label}? This cannot be undone — the data will be permanently removed.`,
  )
}
