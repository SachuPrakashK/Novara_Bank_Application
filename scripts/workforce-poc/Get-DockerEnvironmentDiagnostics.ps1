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
        $status = if ($job.State -eq 'Completed' -and $result -notmatch '(?i)failed to connect|error code:|request returned|500 internal server error|exited [1-9]') { 'RESPONDED' } else { 'FAILED' }
        return [pscustomobject]@{ Check = $Name; Status = $status; Detail = $result.Trim() }
    } finally {
        Stop-Job $job -ErrorAction SilentlyContinue
        Remove-Job $job -Force -ErrorAction SilentlyContinue
    }
}

@(
    Invoke-BoundedCommand 'docker-version' {
        $raw = docker version --format '{{json .}}' 2>&1
        if ($LASTEXITCODE -ne 0) { throw "docker version exited $LASTEXITCODE`: $raw" }
        $version = $raw | ConvertFrom-Json
        if ([string]::IsNullOrWhiteSpace($version.Server.Version)) { throw 'docker version returned no server version' }
        "Client=$($version.Client.Version) Server=$($version.Server.Version)"
    }
    Invoke-BoundedCommand 'docker-info' {
        $raw = docker info --format '{{json .}}' 2>&1
        if ($LASTEXITCODE -ne 0) { throw "docker info exited $LASTEXITCODE`: $raw" }
        $info = $raw | ConvertFrom-Json
        if ([string]::IsNullOrWhiteSpace($info.ServerVersion) -or $info.MemTotal -le 0) { throw 'docker info returned invalid server fields' }
        "Server=$($info.ServerVersion) Containers=$($info.Containers) Running=$($info.ContainersRunning) Images=$($info.Images) Memory=$($info.MemTotal) Driver=$($info.Driver)"
    }
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
