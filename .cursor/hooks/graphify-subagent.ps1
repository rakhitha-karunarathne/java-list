# Remind every subagent to orient via graphify before exploring code.
$ErrorActionPreference = 'SilentlyContinue'
$null = $input

$msg = 'Use graphify first for codebase exploration: graphify query "<question>", graphify path "<A>" "<B>", or graphify explain "<concept>". Only Read/Grep after the graph names the files and lines.'
@{ permission = 'allow'; agent_message = $msg } | ConvertTo-Json -Compress
