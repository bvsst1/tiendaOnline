$ErrorActionPreference = 'Stop'
$raizTienda = Split-Path $PSScriptRoot -Parent
$archivoEntorno = Join-Path $raizTienda '.env'
if (-not (Test-Path -LiteralPath $archivoEntorno)) {
    throw 'Falta .env. Ejecuta scripts/Inicializar-Oracle.ps1 o configura .env.example.'
}
foreach ($linea in Get-Content -LiteralPath $archivoEntorno) {
    if ($linea.Trim() -eq '' -or $linea.TrimStart().StartsWith('#')) { continue }
    $par = $linea.Split('=', 2)
    if ($par.Length -eq 2) {
        [Environment]::SetEnvironmentVariable($par[0].Trim(), $par[1].Trim(), 'Process')
    }
}
if (-not $env:JAVA_HOME -and (Test-Path -LiteralPath 'C:\Program Files\Android\Android Studio\jbr\bin\java.exe')) {
    $env:JAVA_HOME = 'C:\Program Files\Android\Android Studio\jbr'
}
if ($env:JAVA_HOME) { $env:PATH = "$env:JAVA_HOME\bin;$env:PATH" }

