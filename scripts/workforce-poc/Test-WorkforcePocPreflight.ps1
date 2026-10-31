[CmdletBinding()]
param(
    [int]$KeycloakPort = 8082,
    [ValidateRange(5, 60)][int]$DockerTimeoutSeconds = 15
)

$ErrorActionPreference = 'Stop'
$root = Resolve-Path (Join-Path $PSScriptRoot '..\..')
$failures = [System.Collections.Generic.List[string]]::new()

function Test-Command([string]$Name) {
    if (-not (Get-Command $Name -ErrorAction SilentlyContinue)) { $failures.Add("Missing command: $Name") }
}

Test-Command 'docker'
if (-not (Test-Path (Join-Path $root 'backend\mvnw.cmd'))) { $failures.Add('Maven wrapper is missing.') }
if (-not (Test-Path (Join-Path $root '.tools\jdk21\jdk-21.0.12.1+1\bin\java.exe'))) { $failures.Add('Repository Java 21 runtime is missing.') }

$dockerInfo = $null
$dockerExitCode = $null
$dockerJob = Start-Job -ScriptBlock {
    $raw = docker info --format '{{json .}}' 2>$null
    [pscustomobject]@{ ExitCode = $LASTEXITCODE; Raw = [string]$raw }
}
try {
    if (Wait-Job $dockerJob -Timeout $DockerTimeoutSeconds) {
        $dockerResult = Receive-Job $dockerJob
        $dockerExitCode = $dockerResult.ExitCode
        if ($dockerJob.State -eq 'Completed' -and $dockerExitCode -eq 0 -and -not [string]::IsNullOrWhiteSpace($dockerResult.Raw)) {
            $candidate = $dockerResult.Raw | ConvertFrom-Json
            if (-not [string]::IsNullOrWhiteSpace($candidate.ServerVersion) -and $candidate.MemTotal -gt 0) {
                $dockerInfo = $candidate
            }
        }
    }
} finally {
    Stop-Job $dockerJob -ErrorAction SilentlyContinue
    Remove-Job $dockerJob -Force -ErrorAction SilentlyContinue
}
if (-not $dockerInfo) { $failures.Add("Docker Desktop is unavailable or the Docker API did not return a valid server response within $DockerTimeoutSeconds seconds (CLI exit: $dockerExitCode).") }
try { Get-Content -Raw (Join-Path $root 'keycloak\novara-workforce-test-realm.json') | ConvertFrom-Json | Out-Null } catch { $failures.Add('Workforce realm JSON is invalid.') }

$listener = Get-NetTCPConnection -LocalPort $KeycloakPort -State Listen -ErrorAction SilentlyContinue
if ($listener) { $failures.Add("Port $KeycloakPort is already in use by PID $($listener.OwningProcess).") }

$chrome = Test-Path 'C:\Program Files (x86)\Google\Chrome\Application\chrome.exe'
$dockerMemory = if ($dockerInfo) { $dockerInfo.MemTotal } else { $null }

[pscustomobject]@{
    DockerAvailable = [bool]$dockerInfo
    DockerCliExitCode = $dockerExitCode
    Java21 = Test-Path (Join-Path $root '.tools\jdk21\jdk-21.0.12.1+1\bin\java.exe')
    MavenWrapper = Test-Path (Join-Path $root 'backend\mvnw.cmd')
    KeycloakPort = $KeycloakPort
    ChromeAvailable = $chrome
    DockerMemoryBytes = $dockerMemory
    SecretVariablesPresent = @(
        'WORKFORCE_KEYCLOAK_ADMIN', 'WORKFORCE_KEYCLOAK_ADMIN_PASSWORD',
        'NOVARA_WORKFORCE_POC_CLIENT_SECRET', 'NOVARA_WORKFORCE_VERIFIER_SECRET'
    ) | ForEach-Object { [bool][Environment]::GetEnvironmentVariable($_) }
    TestDatabase = 'Testcontainers PostgreSQL only; no production database configuration'
}

if ($failures.Count -gt 0) { $failures | ForEach-Object { Write-Error $_ }; exit 1 }
