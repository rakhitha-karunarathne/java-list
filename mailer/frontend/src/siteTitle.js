const DEFAULT_SITE_TITLE = 'List Mailer'

/** @returns {Promise<string>} */
export async function fetchSiteTitle() {
  try {
    const res = await fetch('/api/public/site-title')
    if (!res.ok) return DEFAULT_SITE_TITLE
    const body = await res.json()
    const title = typeof body?.siteTitle === 'string' ? body.siteTitle.trim() : ''
    return title || DEFAULT_SITE_TITLE
  } catch {
    return DEFAULT_SITE_TITLE
  }
}

export { DEFAULT_SITE_TITLE }
