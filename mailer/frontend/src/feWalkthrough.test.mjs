import assert from 'node:assert/strict'
import { readFileSync } from 'node:fs'
import { dirname, join } from 'node:path'
import { fileURLToPath } from 'node:url'
import { test } from 'node:test'
import { visibleSections } from './navConfig.js'

const root = dirname(fileURLToPath(import.meta.url))
const feature = readFileSync(join(root, 'FeaturePages.jsx'), 'utf8')

test('walkthrough: Admin nav exposes catalogs lists campaigns mts', () => {
  const ids = visibleSections('ADMIN').flatMap((s) => s.items.map((i) => i.id))
  for (const need of [
    'attention',
    'lists',
    'members',
    'campaigns',
    'compose',
    'mts',
    'drop-log',
    'issues',
    'sending-paths',
    'users',
  ]) {
    assert.ok(ids.includes(need), `missing nav ${need}`)
  }
})

test('walkthrough: MainView wires key indexes', () => {
  for (const view of [
    "case 'lists'",
    "case 'members'",
    "case 'campaigns'",
    "case 'mts'",
    "case 'issues'",
    "case 'drop-log'",
    "case 'sending-paths'",
    "case 'users'",
  ]) {
    assert.ok(feature.includes(view), `MainView missing ${view}`)
  }
})

test('walkthrough: List sender has Compose not Attention', () => {
  const ids = visibleSections('LIST_SENDER').flatMap((s) => s.items.map((i) => i.id))
  assert.ok(ids.includes('compose'))
  assert.ok(!ids.includes('attention'))
})
