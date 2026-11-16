param([string]$Destino)
$ErrorActionPreference = 'Stop'
$raizTienda = Split-Path $PSScriptRoot -Parent
Set-Location -LiteralPath $raizTienda
if (-not $Destino) {
    $Destino = Join-Path (Split-Path $raizTienda -Parent) 'Semana2_Tienda_Codigo.zip'
}
if (Test-Path -LiteralPath $Destino) {
    $carpetaDestino = Split-Path $Destino -Parent
    $Destino = Join-Path $carpetaDestino ("Semana2_Tienda_Codigo_" + (Get-Date -Format 'yyyyMMdd-HHmmss') + '.zip')
}
$archivos = git -c core.quotepath=false ls-files --cached --others --exclude-standard
if ($LASTEXITCODE -ne 0) { throw 'No se pudo leer el repositorio Git.' }
Add-Type -AssemblyName System.IO.Compression.FileSystem
$zip = [System.IO.Compression.ZipFile]::Open($Destino, [System.IO.Compression.ZipArchiveMode]::Create)
try {
    foreach ($relativo in $archivos) {
        if ($relativo -eq '.env' -or $relativo -match '(^|/)(target|\.git|\.m2-local|\.herramientas)/' -or $relativo -match '\.(zip|rar|log)$') {
            throw "Archivo no permitido en la entrega: $relativo"
        }
        $origen = Join-Path $raizTienda $relativo
        if (Test-Path -LiteralPath $origen -PathType Leaf) {
            [System.IO.Compression.ZipFileExtensions]::CreateEntryFromFile(
                $zip, $origen, "semana-2-tienda/$relativo", [System.IO.Compression.CompressionLevel]::Optimal
            ) | Out-Null
        }
    }
} finally {
    $zip.Dispose()
}
Write-Host "ZIP creado: $Destino"
