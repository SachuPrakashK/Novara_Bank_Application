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

function Get-BoundedDockerInfo {
    $dockerJob = Start-Job -ScriptBlock {
        $raw = docker info --format '{{json .}}' 2>&1
        [pscustomobject]@{ ExitCode = $LASTEXITCODE; Raw = [string]($raw -join [Environment]::NewLine) }
    }
    try {
        if (-not (Wait-Job $dockerJob -Timeout $DockerTimeoutSeconds)) {
            return [pscustomobject]@{ Valid = $false; ExitCode = $null; Info = $null; Detail = 'TIMEOUT' }
        }
        $dockerResult = Receive-Job $dockerJob
        if ($dockerJob.State -ne 'Completed' -or $dockerResult.ExitCode -ne 0 -or [string]::IsNullOrWhiteSpace($dockerResult.Raw)) {
            return [pscustomobject]@{ Valid = $false; ExitCode = $dockerResult.ExitCode; Info = $null; Detail = $dockerResult.Raw }
        }
        try { $candidate = $dockerResult.Raw | ConvertFrom-Json } catch {
            return [pscustomobject]@{ Valid = $false; ExitCode = $dockerResult.ExitCode; Info = $null; Detail = 'INVALID_JSON' }
        }
        $valid = -not [string]::IsNullOrWhiteSpace($candidate.ServerVersion) -and $candidate.MemTotal -gt 0
        return [pscustomobject]@{ Valid = $valid; ExitCode = $dockerResult.ExitCode; Info = $candidate; Detail = if ($valid) { 'OK' } else { 'MISSING_SERVER_FIELDS' } }
    } finally {
        Stop-Job $dockerJob -ErrorAction SilentlyContinue
        Remove-Job $dockerJob -Force -ErrorAction SilentlyContinue
    }
}

$dockerSamples = @()
1..3 | ForEach-Object {
    $dockerSamples += Get-BoundedDockerInfo
    if ($_ -lt 3) { Start-Sleep -Seconds 2 }
}
$dockerInfo = if (($dockerSamples | Where-Object Valid).Count -eq 3) { $dockerSamples[-1].Info } else { $null }
$dockerExitCode = $dockerSamples[-1].ExitCode
if (-not $dockerInfo) {
    $sampleSummary = ($dockerSamples | ForEach-Object { if ($_.Valid) { 'OK' } else { "FAILED($($_.Detail))" } }) -join ', '
    $failures.Add("Docker Desktop is unavailable or unstable across three bounded API samples: $sampleSummary")
}
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
    DockerStabilitySamples = $dockerSamples | ForEach-Object Valid
    SecretVariablesPresent = @(
        'WORKFORCE_KEYCLOAK_ADMIN', 'WORKFORCE_KEYCLOAK_ADMIN_PASSWORD',
        'NOVARA_WORKFORCE_POC_CLIENT_SECRET', 'NOVARA_WORKFORCE_VERIFIER_SECRET'
    ) | ForEach-Object { [bool][Environment]::GetEnvironmentVariable($_) }
    TestDatabase = 'Testcontainers PostgreSQL only; no production database configuration'
}

if ($failures.Count -gt 0) { $failures | ForEach-Object { Write-Error $_ }; exit 1 }
