import { confirmDelete } from './confirmDelete.js'

const g = globalThis
const original = g.confirm
let lastMsg = ''
g.confirm = (msg) => {
  lastMsg = msg
  return true
}

if (!confirmDelete('Inbox “Ops”')) throw new Error('expected true')
if (!lastMsg.includes('Inbox “Ops”')) throw new Error('label missing')
if (!lastMsg.includes('cannot be undone')) throw new Error('warning missing')

g.confirm = () => false
if (confirmDelete('x') !== false) throw new Error('expected false')

g.confirm = original
console.log('confirmDelete.test.js ok')
