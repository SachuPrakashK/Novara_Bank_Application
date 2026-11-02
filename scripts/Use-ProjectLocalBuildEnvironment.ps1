[CmdletBinding()]
param()

$root = Resolve-Path (Join-Path $PSScriptRoot '..')
$toolingRoot = Join-Path $root 'tmp\tooling'
$npmCache = Join-Path $toolingRoot 'npm-cache'
$processTemp = Join-Path $toolingRoot 'temp'
$javaTemp = Join-Path $toolingRoot 'java-temp'

@($npmCache, $processTemp, $javaTemp) | ForEach-Object {
    New-Item -ItemType Directory -Force -Path $_ | Out-Null
}

$env:NPM_CONFIG_CACHE = $npmCache
$env:TEMP = $processTemp
$env:TMP = $processTemp

$javaTempOption = "-Djava.io.tmpdir=$javaTemp"
$existingMavenOptions = [string]$env:MAVEN_OPTS
if ($existingMavenOptions -notmatch '(?:^|\s)-Djava\.io\.tmpdir=') {
    $env:MAVEN_OPTS = ($existingMavenOptions + ' ' + $javaTempOption).Trim()
}

[pscustomobject]@{
    Scope = 'Current PowerShell process and child processes only'
    NpmCache = $env:NPM_CONFIG_CACHE
    ProcessTemp = $env:TEMP
    JavaTemp = $javaTemp
    MavenOptions = $env:MAVEN_OPTS
}
