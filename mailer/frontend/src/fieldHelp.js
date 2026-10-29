/**
 * Short per-field help (shown under the label via FieldDesc).
 * Prefer these keys over ad-hoc copy so SMTP/IMAP forms stay consistent.
 */
export const fieldHelp = {
  // Compose / campaign body
  subject: 'Shown in the recipient’s inbox; keep it clear and specific.',
  htmlBody: 'HTML version of the message. Remote images may be fetched when the campaign is queued.',
  textBody: 'Plain-text fallback for clients that do not show HTML.',
  attachFile: 'Optional file sent with the message. Saving a new file replaces any saved attachments.',
  composeSendingPath:
    'Optional override. When empty, send uses the first included list’s sending path.',
  testAddresses: 'One or more addresses for a test send. Comma, semicolon, or newline separated.',

  // SMTP / sending path
  name: 'Display name used in lists and pickers.',
  host: 'Mail server hostname or IP.',
  port: 'SMTP or IMAP port for this connection.',
  username: 'Login name on the mail server.',
  password: 'Mail server password. Stored encrypted.',
  passwordKeep: 'Leave blank to keep the current password.',
  fromEmail: 'Envelope/from address used when sending through this path.',
  fromName: 'Display name shown next to From Email.',
  returnPathEmail:
    'Optional bounce address. Domain must match From Email when set.',
  useTls: 'STARTTLS after connect (common on port 587). Mutually exclusive with Use SSL.',
  useSsl: 'Implicit TLS from connect (common on port 465 / IMAPS 993). Mutually exclusive with Use TLS.',

  // IMAP / MTS / bounce
  pollIntervalMinutes: 'How often this mailbox is polled when enabled.',
  autoQueue: 'When on, accepted Mail-to-Send messages are queued without a manual Queue step.',
  embedImages:
    'Default for new campaigns from this inbox: embed fetched remote images as CID when size allows.',
  deleteAfterAccept: 'When on, delete the IMAP message after it is accepted into a campaign.',
  enabled: 'When off, polling and processing for this record are paused.',
  substituteFrom: 'Optional From override for this sender. Leave blank to use the sending path From.',
  sendingPathId: 'SMTP path used when sending for this record.',
  senderEmail: 'Exact From address that may use this inbox for Mail-to-Send.',

  // Lists / footers / members
  listSendingPath: 'Default SMTP path for campaigns that include this list (unless overridden).',
  footerId: 'Footer appended to messages for this list (or sender grant).',
  spamThreshold: 'Optional spam score threshold for this list; blank uses the system default.',
  footerHtml: 'HTML appended after the campaign body.',
  footerText: 'Plain-text footer appended after the campaign text body.',
  bulkAddresses: 'One address per line, or separated by commas or semicolons.',
  disableReason: 'Recorded on the member when they are bounce-disabled.',

  // Users / auth
  email: 'Login email for this operator.',
  role: 'Operator role controls which admin screens and actions are available.',
  userEnabled: 'When off, this operator cannot sign in.',
  currentPassword: 'Your current password, required to set a new one.',
  newPassword: 'Choose a strong password you have not used elsewhere.',
  confirmNewPassword: 'Re-enter the new password to confirm.',
  timeZone: 'Used for dates and times shown in the UI for your account.',
  verificationCode: 'Code from your authenticator app.',
  unenrollCode: 'Authenticator code or a one-time backup code.',
  loginEmail: 'Operator account email.',
  loginPassword: 'Operator account password.',
  authenticatorCode: 'Current code from your authenticator app, or a backup code if offered.',

  // Blocks / filters (short)
  blockSender: 'Block this exact sender address from Mail-to-Send.',
  blockDomain: 'Block all senders at this domain from Mail-to-Send.',
  filter: 'Limit which rows are shown in this list.',
  search: 'Filter the table by matching text.',
  addList: 'Choose a list to include on this campaign.',
  statusFilter: 'Show only campaigns with this status.',
}
