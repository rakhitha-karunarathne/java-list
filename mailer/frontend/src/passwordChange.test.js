import { passwordChangeProblem } from './passwordChange.js'

function check(current, next, confirm, expect) {
  const got = passwordChangeProblem(current, next, confirm)
  if (got !== expect) throw new Error(`got ${JSON.stringify(got)} want ${JSON.stringify(expect)}`)
}

check('', 'a', 'a', 'Current and new password are required.')
check('old', '   ', '   ', 'Current and new password are required.')
check('old', 'new', 'other', 'New password and confirmation do not match.')
check('old', 'old', 'old', 'New password must differ from the current password.')
check('old', 'new', 'new', '')

console.log('passwordChange.test.js ok')
