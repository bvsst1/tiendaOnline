param(
    [Parameter(Mandatory = $true)]
    [ValidateSet('usuarios', 'productos')]
    [string]$Servicio
)
$ErrorActionPreference = 'Stop'
. "$PSScriptRoot/Cargar-Entorno.ps1"
$raizTienda = Split-Path $PSScriptRoot -Parent
$jar = Join-Path $raizTienda "$Servicio-service/target/$Servicio-service-1.0.0.jar"
if (-not (Test-Path -LiteralPath $jar)) {
    throw 'Falta compilar. Desde la raíz ejecuta .\mvnw.cmd clean verify'
}
& java -jar $jar
if ($LASTEXITCODE -ne 0) { throw "El servicio $Servicio terminó con error." }
