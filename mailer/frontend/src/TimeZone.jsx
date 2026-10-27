import { createContext, useContext, useEffect, useMemo, useState } from 'react'
import { apiJson } from './api.js'
import { formatWhen } from './formatWhen.js'

const TimeZoneContext = createContext({
  timeZone: 'UTC',
  setTimeZone: () => {},
  formatWhen: (v) => formatWhen(v, 'UTC'),
})

export function TimeZoneProvider({ token, children }) {
  const [timeZone, setTimeZone] = useState('UTC')

  useEffect(() => {
    if (!token) {
      setTimeZone('UTC')
      return
    }
    let cancelled = false
    apiJson(token, '/api/me')
      .then((m) => {
        if (!cancelled) setTimeZone(m.timeZone || 'UTC')
      })
      .catch(() => {
        if (!cancelled) setTimeZone('UTC')
      })
    return () => {
      cancelled = true
    }
  }, [token])

  const value = useMemo(
    () => ({
      timeZone,
      setTimeZone,
      formatWhen: (v) => formatWhen(v, timeZone),
    }),
    [timeZone],
  )

  return <TimeZoneContext.Provider value={value}>{children}</TimeZoneContext.Provider>
}

export function useTimeZone() {
  return useContext(TimeZoneContext)
}

export function useFormatWhen() {
  return useTimeZone().formatWhen
}
