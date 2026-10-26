/**
 * Form field labels must be valid English phrases:
 * - Words separated by spaces (never CamelCase or glued identifiers)
 * - Title Case; known acronyms stay uppercase (HTML, TLS, MFA, …)
 * - Prefer explicit phrase overrides for API keys that need natural wording
 */

const ACRONYMS = new Set([
  'html',
  'tls',
  'ssl',
  'mfa',
  'url',
  'imap',
  'smtp',
  'api',
  'id',
  'totp',
  'verp',
  'ops',
])

/** API / form keys → natural English label (words already spaced). */
const PHRASES = {
  htmlBody: 'HTML Body',
  textBody: 'Text Body',
  useTls: 'Use TLS',
  useSsl: 'Use SSL',
  fromEmail: 'From Email',
  fromName: 'From Name',
  pollIntervalMinutes: 'Poll Interval Minutes',
  spamThreshold: 'Spam Threshold',
  sendingPathId: 'Sending Path',
  footerId: 'Footer',
  payloadOverride: 'Payload Override',
  imageTtlOverrideHours: 'Image TTL Override Hours',
  disableReason: 'Disable Reason',
  'public.base.url': 'Public Base URL',
  'operator.mfa.mandatory': 'Require MFA For All Operators',
  'verp.enabled': 'VERP Enabled',
  'ops.log.retention.days': 'Ops Log Retention Days',
  'audit.retention.days': 'Audit Retention Days',
  'send.delay.per.email.ms': 'Delay Per Email Milliseconds',
  'send.batch.size': 'Send Batch Size',
  'send.batch.duration.minutes': 'Send Batch Duration Minutes',
  'send.messages.per.hour': 'Messages Per Hour',
  'bounce.process.log.enabled': 'Bounce Processing Log Enabled',
  'send.queue.log.enabled': 'Queue Sending Log Enabled',
  'mts.process.log.enabled': 'Mail-To-Send Processing Log Enabled',
  'campaign.image.fetch.mode': 'Campaign Image Fetch Mode',
  'campaign.payload.max.bytes': 'Campaign Payload Max Bytes',
  'campaign.image.max.bytes': 'Campaign Image Max Bytes',
  'bounce.hard.limit': 'Bounce Hard Limit',
  'bounce.soft.limit': 'Bounce Soft Limit',
  'bounce.holdback.multiplier': 'Bounce Holdback Multiplier',
}

function titleWord(word) {
  const lower = word.toLowerCase()
  if (ACRONYMS.has(lower)) return lower.toUpperCase()
  if (!lower) return word
  return lower.charAt(0).toUpperCase() + lower.slice(1)
}

/** Split camelCase / snake / dotted keys into separate English words. */
export function fieldLabel(key) {
  if (key == null || key === '') return ''
  const raw = String(key).trim()
  if (PHRASES[raw]) return PHRASES[raw]

  const spaced = raw
    .replace(/([a-z0-9])([A-Z])/g, '$1 $2')
    .replace(/([A-Z]+)([A-Z][a-z])/g, '$1 $2')
    .replace(/[_\-.]+/g, ' ')
    .trim()

  return spaced
    .split(/\s+/)
    .filter(Boolean)
    .map(titleWord)
    .join(' ')
}
