[CmdletBinding()]
param([ValidateRange(5, 60)][int]$TimeoutSeconds = 15)

$ErrorActionPreference = 'Continue'

function Invoke-BoundedCommand([string]$Name, [scriptblock]$Command) {
    $job = Start-Job -ScriptBlock $Command
    try {
        if (-not (Wait-Job $job -Timeout $TimeoutSeconds)) {
            return [pscustomobject]@{ Check = $Name; Status = 'TIMEOUT'; Detail = "No response within $TimeoutSeconds seconds" }
        }
        $result = @(Receive-Job $job 2>&1) -join [Environment]::NewLine
        $status = if ($job.State -eq 'Completed' -and $result -notmatch '(?i)failed to connect|error code:') { 'RESPONDED' } else { 'FAILED' }
        return [pscustomobject]@{ Check = $Name; Status = $status; Detail = $result.Trim() }
    } finally {
        Stop-Job $job -ErrorAction SilentlyContinue
        Remove-Job $job -Force -ErrorAction SilentlyContinue
    }
}

@(
    Invoke-BoundedCommand 'docker-version' { docker version --format 'Client={{.Client.Version}} Server={{.Server.Version}}'; if ($LASTEXITCODE -ne 0) { throw "docker version exited $LASTEXITCODE" } }
    Invoke-BoundedCommand 'docker-info' { docker info --format 'Server={{.ServerVersion}} Containers={{.Containers}} Running={{.ContainersRunning}} Images={{.Images}} Memory={{.MemTotal}} Driver={{.Driver}}'; if ($LASTEXITCODE -ne 0) { throw "docker info exited $LASTEXITCODE" } }
    Invoke-BoundedCommand 'docker-context' { docker context ls; if ($LASTEXITCODE -ne 0) { throw "docker context exited $LASTEXITCODE" } }
    Invoke-BoundedCommand 'docker-containers' { docker ps --format 'table {{.Names}}\t{{.Image}}\t{{.Status}}\t{{.Ports}}'; if ($LASTEXITCODE -ne 0) { throw "docker ps exited $LASTEXITCODE" } }
    Invoke-BoundedCommand 'wsl-status' { wsl.exe --status; if ($LASTEXITCODE -ne 0) { throw "wsl status exited $LASTEXITCODE" } }
    Invoke-BoundedCommand 'wsl-distributions' { wsl.exe --list --verbose; if ($LASTEXITCODE -ne 0) { throw "wsl list exited $LASTEXITCODE" } }
)

[pscustomobject]@{
    Check = 'docker-service'
    Status = (Get-Service -Name 'com.docker.service' -ErrorAction SilentlyContinue).Status
    Detail = 'Read-only service state; this script never starts or restarts it.'
}

[pscustomobject]@{
    Check = 'keycloak-port'
    Status = if (Get-NetTCPConnection -LocalPort 8082 -State Listen -ErrorAction SilentlyContinue) { 'IN_USE' } else { 'AVAILABLE' }
    Detail = '127.0.0.1:8082 target port'
}
