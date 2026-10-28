---
deferred_work_file: 'D:/dev/java-list/_bmad-output/initiative-java-list-mailer/deferred-work.md'
---

# Step 5: Present and Act

## RULES

- When `{plan_file}` is set, always write findings to it before offering action choices.
- Never change a plan's `status`. The build leaves it at `built`; only the user marks a ticket done.
- `{triage_file}` is the source for every finding below: its verdict, evidence, route, options, and rejection reason. Render from it, not from memory.
- No human can answer during this run: never ask and never HALT. The report text is the deliverable -- everything a reader must act on is in it, not only in files the run writes.

## INSTRUCTIONS

### 1. Clean review shortcut

If zero findings remain after triage (all rejected or none raised): state that. When `{plan_file}` is set, still write section 2's block, with the summary line and the `Rejected` appendix, so the run is recorded.
Then end.

### 2. Write findings to the plan

If `{plan_file}` is set, append a block to its `## Code Review` section, creating that section at the end of the file when it is absent. The block opens with `### {date}` and the summary line from section 3, then the findings below, then the `Rejected` appendix. Write all findings in this order:

1. **`decision-needed`** findings (unchecked):
   `- [ ] [Review][Decision] <Title> — <Detail> Options: <each option, one line>. Recommended: <option> — <reason>.`

2. **`patch`** findings (unchecked):
   `- [ ] [Review][Patch] <Title> [<file>:<line>]`

3. **`defer`** findings (checked off, marked deferred):
   `- [x] [Review][Defer] <Title> [<file>:<line>] — deferred: <pre-existing, or for maybe-false the evidence that would settle it>`

Also append each `defer` finding to `{deferred_work_file}` under a heading `## Deferred from: code review ({date})`. If `plan_file` is set, include its basename in the heading (e.g., `code review of story-digest-delivery-plan (2026-03-18)`). One bullet per finding with description.

### 3. Present summary

Announce what was written:

> **Code review complete.** <D> `decision-needed`, <P> `patch`, <W> `defer`, <R> rejected.

The `Rejected` appendix is one line per rejected finding: `false` with its refutation, `low` with why it was not worth fixing. Without `plan_file`, it ends the chat listing.

The listing is the review itself. List `decision-needed` entries first, then `patch`, then `defer`, then the `Rejected` appendix, highest severity first within each. Each `decision-needed` entry carries its options and the recommended one and is marked open -- nothing was decided. Each `patch` entry carries its location and the fix.

If `plan_file` is set, add: `Findings written to {plan_file}.`
Otherwise add: `Findings are listed above. No plan was provided, so nothing was persisted.`


### 5. Handle `patch` findings

Apply every `patch` finding without asking. Do not modify defer or decision-needed items. After all patches are applied, present a summary of changes made. If `plan_file` is set, check off the patch items in it (leave defer items as-is).


## On Complete

If anything appears below, follow it as the final terminal instruction before exiting; otherwise exit normally.


