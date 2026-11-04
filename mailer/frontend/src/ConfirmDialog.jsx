import { useEffect, useId, useState } from 'react'

/**
 * Modal confirm. Optional checkbox returns its checked state with onConfirm(checked).
 * Callers pass checkboxLabel to show the extra option (e.g. delete orphan recipients).
 */
export function ConfirmDialog({
  open,
  title,
  message,
  checkboxLabel,
  confirmLabel = 'Delete',
  onConfirm,
  onCancel,
}) {
  const [checked, setChecked] = useState(false)
  const titleId = useId()

  useEffect(() => {
    if (open) setChecked(false)
  }, [open])

  if (!open) return null

  return (
    <div className="modal-backdrop" role="presentation" onClick={onCancel}>
      <div
        className="modal-dialog card"
        role="dialog"
        aria-modal="true"
        aria-labelledby={titleId}
        onClick={(e) => e.stopPropagation()}
      >
        <h3 id={titleId}>{title}</h3>
        <p>{message}</p>
        {checkboxLabel ? (
          <label className="field checkbox-row">
            <input
              type="checkbox"
              checked={checked}
              onChange={(ev) => setChecked(ev.target.checked)}
            />
            <span>{checkboxLabel}</span>
          </label>
        ) : null}
        <div className="modal-actions">
          <button type="button" className="btn secondary" onClick={onCancel}>
            Cancel
          </button>
          <button type="button" className="btn danger" onClick={() => onConfirm(checked)}>
            {confirmLabel}
          </button>
        </div>
      </div>
    </div>
  )
}
