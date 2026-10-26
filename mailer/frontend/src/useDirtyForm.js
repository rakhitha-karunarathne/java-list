import { useRef, useState } from 'react'
import { isFormDirty } from './formDirty.js'
import { useRegisterUnsaved } from './Unsaved.jsx'

/**
 * Shared create/edit dirty tracking. Call with creating=true on create page.
 * adopt(next) after load/save; resetToBlank() when entering create.
 */
export function useDirtyForm(blankShape, unsavedKey, creating) {
  const blank = useRef(blankShape)
  const [form, setForm] = useState(() => ({ ...blankShape }))
  const [saved, setSaved] = useState(null)
  const dirty = creating
    ? isFormDirty(form, blank.current)
    : !!(saved && isFormDirty(form, saved))
  useRegisterUnsaved(dirty, unsavedKey)

  function adopt(next) {
    setForm(next)
    setSaved(next)
  }

  function resetToBlank() {
    setForm({ ...blank.current })
    setSaved(null)
  }

  return { form, setForm, saved, dirty, blank: blank.current, adopt, resetToBlank, setSaved }
}
