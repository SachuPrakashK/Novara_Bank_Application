[CmdletBinding()]
param()

$ErrorActionPreference = 'Stop'
$root = Resolve-Path (Join-Path $PSScriptRoot '..\..')

function New-DisposableSecret([int]$Bytes = 48) {
    $buffer = [byte[]]::new($Bytes)
    $generator = [System.Security.Cryptography.RandomNumberGenerator]::Create()
    try {
        $generator.GetBytes($buffer)
    } finally {
        $generator.Dispose()
    }
    return [Convert]::ToBase64String($buffer).TrimEnd('=').Replace('+', '-').Replace('/', '_')
}

$secretNames = @(
    'WORKFORCE_KEYCLOAK_ADMIN',
    'WORKFORCE_KEYCLOAK_ADMIN_PASSWORD',
    'NOVARA_WORKFORCE_POC_CLIENT_SECRET',
    'NOVARA_WORKFORCE_VERIFIER_SECRET',
    'NOVARA_WORKFORCE_REAL_PROVIDER_TESTS'
)
$startupAttempted = $false

try {
    $env:WORKFORCE_KEYCLOAK_ADMIN = 'bootstrap-' + [guid]::NewGuid().ToString('N')
    $env:WORKFORCE_KEYCLOAK_ADMIN_PASSWORD = New-DisposableSecret
    $env:NOVARA_WORKFORCE_POC_CLIENT_SECRET = New-DisposableSecret
    $env:NOVARA_WORKFORCE_VERIFIER_SECRET = New-DisposableSecret
    $env:NOVARA_WORKFORCE_REAL_PROVIDER_TESTS = 'true'

    $startupAttempted = $true
    & (Join-Path $PSScriptRoot 'Start-WorkforcePoc.ps1')
    if ($LASTEXITCODE -ne 0) { throw 'Workforce Keycloak startup failed.' }

    docker compose -f (Join-Path $root 'docker-compose.workforce-poc.yml') exec -T workforce-keycloak /opt/keycloak/bin/kc.sh --version
    if ($LASTEXITCODE -ne 0) { throw 'Unable to capture the running Keycloak version.' }

    $javaHome = Resolve-Path (Join-Path $root '.tools\jdk21\jdk-21.0.12.1+1')
    $env:JAVA_HOME = $javaHome.Path
    $env:MAVEN_OPTS = '-Xms128m -Xmx512m -XX:MaxMetaspaceSize=256m'
    Push-Location (Join-Path $root 'backend')
    try {
        & .\mvnw.cmd '-Dtest=RealKeycloakOidcAcceptanceTest' test
        if ($LASTEXITCODE -ne 0) { throw 'Real Keycloak cryptographic acceptance failed.' }
    } finally {
        Pop-Location
    }
} finally {
    if ($startupAttempted) {
        try {
            & (Join-Path $PSScriptRoot 'Stop-WorkforcePoc.ps1')
        } catch {
            Write-Error 'Targeted workforce cleanup failed; inspect only the novara-workforce-poc Compose project.'
        }
    }
    foreach ($name in $secretNames) {
        Remove-Item "Env:$name" -ErrorAction SilentlyContinue
    }
}
