import assert from 'node:assert/strict'
import { test } from 'node:test'
import {
  canDeleteCampaign,
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

test('canDeleteCampaign allows Draft Finished Stopped only', () => {
  assert.equal(canDeleteCampaign('DRAFT'), true)
  assert.equal(canDeleteCampaign('FINISHED'), true)
  assert.equal(canDeleteCampaign('STOPPED'), true)
  assert.equal(canDeleteCampaign('PAUSED'), false)
  assert.equal(canDeleteCampaign('QUEUED'), false)
  assert.equal(canDeleteCampaign('SENDING'), false)
})
