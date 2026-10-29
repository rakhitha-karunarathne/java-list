/** Short help under the label, above the control (label.field > .field-desc). */
export function FieldDesc({ children }) {
  if (children == null || children === false || children === '') return null
  return <span className="hint field-desc">{children}</span>
}
