[CmdletBinding()]
param()
$ErrorActionPreference = 'Stop'
docker compose -f (Join-Path $PSScriptRoot '..\..\docker-compose.workforce-poc.yml') down --volumes --remove-orphans
if ($LASTEXITCODE -ne 0) { throw 'Disposable workforce cleanup did not complete. No unrelated Docker resources were targeted.' }
Write-Output 'Disposable workforce Keycloak and its temporary storage were removed.'
