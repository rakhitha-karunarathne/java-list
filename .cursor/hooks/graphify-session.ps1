# Inject graphify orientation into every new agent session.
$ErrorActionPreference = 'SilentlyContinue'
$null = $input  # consume stdin JSON (sessionStart payload)

$root = Split-Path (Split-Path $PSScriptRoot -Parent) -Parent
if (-not $root) { $root = (Get-Location).Path }
$graph = Join-Path $root 'graphify-out\graph.json'

if (Test-Path $graph) {
  $ctx = @'
This repo has a graphify knowledge graph at graphify-out/graph.json.
Default to graphify before exploring code: run `graphify query "<question>"` (or path/explain) before the first Grep/Read/Glob for any codebase or architecture question. Put the same rule in every subagent prompt. Only read raw files for exact lines the graph already named. After code edits, run `graphify update .`.
'@
} else {
  $ctx = @'
graphify is installed, but graphify-out/graph.json is missing. Build it once with `/graphify .` (or `graphify .`) so agents can orient from the graph instead of raw search. Until then, normal search is allowed.
'@
}

@{ additional_context = $ctx.Trim() } | ConvertTo-Json -Compress
