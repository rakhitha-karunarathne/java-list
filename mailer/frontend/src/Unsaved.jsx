import { createContext, useCallback, useContext, useEffect, useRef, useState } from 'react'

const UnsavedContext = createContext(null)

const LEAVE_MSG = 'You have unsaved changes. Leave without saving?'

export function UnsavedProvider({ children }) {
  const keysRef = useRef(new Set())
  const [dirty, setDirty] = useState(false)

  const setKeyDirty = useCallback((key, isDirty) => {
    if (isDirty) keysRef.current.add(key)
    else keysRef.current.delete(key)
    setDirty(keysRef.current.size > 0)
  }, [])

  const confirmLeave = useCallback(() => {
    if (keysRef.current.size === 0) return true
    return window.confirm(LEAVE_MSG)
  }, [])

  useEffect(() => {
    if (!dirty) return undefined
    function onBeforeUnload(e) {
      e.preventDefault()
      e.returnValue = ''
    }
    window.addEventListener('beforeunload', onBeforeUnload)
    return () => window.removeEventListener('beforeunload', onBeforeUnload)
  }, [dirty])

  return (
    <UnsavedContext.Provider value={{ dirty, setKeyDirty, confirmLeave }}>{children}</UnsavedContext.Provider>
  )
}

export function useUnsavedGate() {
  return useContext(UnsavedContext)
}

/** Register this form's dirty flag for leave-confirm. key must be stable per form instance. */
export function useRegisterUnsaved(dirty, key) {
  const ctx = useContext(UnsavedContext)
  useEffect(() => {
    if (!ctx) return undefined
    ctx.setKeyDirty(key, !!dirty)
    return () => ctx.setKeyDirty(key, false)
  }, [dirty, key, ctx])
}
