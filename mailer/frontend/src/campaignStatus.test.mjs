import assert from 'node:assert/strict'
import { test } from 'node:test'
import {
  canPauseCampaign,
  canResumeCampaign,
  canStopCampaign,
  filterCampaigns,
} from './campaignStatus.js'

test('filterCampaigns keeps Finished only', () => {
  const rows = [
    { id: '1', status: 'DRAFT' },
    { id: '2', status: 'FINISHED' },
  ]
  assert.deepEqual(filterCampaigns(rows, 'FINISHED').map((r) => r.id), ['2'])
})

test('canStopCampaign allows Draft Queued Sending Paused', () => {
  assert.equal(canStopCampaign('DRAFT'), true)
  assert.equal(canStopCampaign('PAUSED'), true)
  assert.equal(canStopCampaign('FINISHED'), false)
})

test('pause only Queued or Sending; resume only Paused', () => {
  assert.equal(canPauseCampaign('QUEUED'), true)
  assert.equal(canPauseCampaign('DRAFT'), false)
  assert.equal(canResumeCampaign('PAUSED'), true)
  assert.equal(canResumeCampaign('QUEUED'), false)
})
