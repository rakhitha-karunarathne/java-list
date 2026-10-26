import assert from 'node:assert/strict'
import { test } from 'node:test'
import { canStopCampaign, filterCampaigns } from './campaignStatus.js'

test('filterCampaigns keeps Finished only', () => {
  const rows = [
    { id: '1', status: 'DRAFT' },
    { id: '2', status: 'FINISHED' },
  ]
  assert.deepEqual(filterCampaigns(rows, 'FINISHED').map((r) => r.id), ['2'])
})

test('canStopCampaign allows Draft Queued Sending', () => {
  assert.equal(canStopCampaign('DRAFT'), true)
  assert.equal(canStopCampaign('FINISHED'), false)
})
