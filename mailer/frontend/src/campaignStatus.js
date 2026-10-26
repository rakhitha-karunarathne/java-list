export const CAMPAIGN_STATUSES = ['DRAFT', 'QUEUED', 'SENDING', 'FINISHED', 'STOPPED']

export const CAMPAIGN_STATUS_LABEL = {
  DRAFT: 'Draft',
  QUEUED: 'Queued',
  SENDING: 'Sending',
  FINISHED: 'Finished',
  STOPPED: 'Stopped',
}

export function campaignStatusLabel(status) {
  const s = (status || '').toUpperCase()
  return CAMPAIGN_STATUS_LABEL[s] || status || 'Unknown'
}

export function filterCampaigns(rows, statusFilter) {
  if (!statusFilter) return rows
  const f = statusFilter.toUpperCase()
  return rows.filter((r) => (r.status || '').toUpperCase() === f)
}

export function canStopCampaign(status) {
  const s = (status || '').toUpperCase()
  return s === 'DRAFT' || s === 'QUEUED' || s === 'SENDING'
}
