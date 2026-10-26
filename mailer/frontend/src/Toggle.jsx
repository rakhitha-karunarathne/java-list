import { dirtyClass } from './formDirty.js'

/** On/Off switch for scalar boolean form fields (not multi-select checkboxes). */
export function Toggle({ checked, onChange, dirty = false, disabled = false, 'aria-label': ariaLabel }) {
  return (
    <button
      type="button"
      role="switch"
      aria-checked={checked}
      aria-label={ariaLabel}
      disabled={disabled}
      className={dirtyClass(dirty, 'toggle')}
      title={dirty ? 'Unsaved changes' : undefined}
      onClick={() => onChange(!checked)}
    >
      <span className="toggle-track" aria-hidden="true">
        <span className="toggle-knob" />
      </span>
      <span className="toggle-state">{checked ? 'On' : 'Off'}</span>
    </button>
  )
}
