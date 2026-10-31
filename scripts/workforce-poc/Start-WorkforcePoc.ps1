[CmdletBinding()]
param()
$ErrorActionPreference = 'Stop'
$required = @('WORKFORCE_KEYCLOAK_ADMIN','WORKFORCE_KEYCLOAK_ADMIN_PASSWORD','NOVARA_WORKFORCE_POC_CLIENT_SECRET','NOVARA_WORKFORCE_VERIFIER_SECRET')
foreach ($name in $required) {
    if ([string]::IsNullOrWhiteSpace([Environment]::GetEnvironmentVariable($name))) { throw "Inject $name through the approved test-secret mechanism before startup." }
}
& (Join-Path $PSScriptRoot 'Test-WorkforcePocPreflight.ps1')
docker compose -f (Join-Path $PSScriptRoot '..\..\docker-compose.workforce-poc.yml') up -d --wait --wait-timeout 300
if ($LASTEXITCODE -ne 0) { throw 'Disposable workforce Keycloak did not become healthy within the bounded startup window.' }
Write-Output 'Disposable workforce Keycloak is healthy at http://127.0.0.1:8082/realms/novara-workforce-test'
