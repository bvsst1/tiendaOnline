$ErrorActionPreference = 'Stop'
$raizTienda = Split-Path $PSScriptRoot -Parent
Set-Location -LiteralPath $raizTienda
if (-not (Test-Path -LiteralPath '.env')) {
    function Nueva-Clave {
        return 'Tda' + [Guid]::NewGuid().ToString('N') + '9a'
    }
    $valores = @(
        "ORACLE_PWD=$(Nueva-Clave)"
        'USUARIOS_DB_URL=jdbc:oracle:thin:@//localhost:1521/FREEPDB1'
        'USUARIOS_DB_USER=TIENDA_USUARIOS'
        "USUARIOS_DB_PASSWORD=$(Nueva-Clave)"
        'PRODUCTOS_DB_URL=jdbc:oracle:thin:@//localhost:1521/FREEPDB1'
        'PRODUCTOS_DB_USER=TIENDA_PRODUCTOS'
        "PRODUCTOS_DB_PASSWORD=$(Nueva-Clave)"
        "TIENDA_JWT_SECRET=$([Guid]::NewGuid().ToString('N'))$([Guid]::NewGuid().ToString('N'))"
    )
    Set-Content -LiteralPath '.env' -Value $valores -Encoding ascii
    Write-Host 'Configuración local creada en .env (excluida de Git y del ZIP).'
}
. "$PSScriptRoot/Cargar-Entorno.ps1"
docker compose up -d
if ($LASTEXITCODE -ne 0) { throw 'No se pudo iniciar Oracle. Comprueba Docker Desktop y el puerto 1521.' }
Write-Host 'Esperando a que Oracle esté listo...'
$listo = $false
for ($intento = 0; $intento -lt 90; $intento++) {
    $logs = docker logs tienda-semana2-oracle 2>&1 | Out-String
    if ($logs.Contains('DATABASE IS READY TO USE!')) { $listo = $true; break }
    Start-Sleep -Seconds 2
}
if (-not $listo) { throw 'Oracle todavía no está listo. Revisa docker logs tienda-semana2-oracle y vuelve a ejecutar este script.' }

# Las claves generadas contienen solo letras y números.
foreach ($clave in @($env:USUARIOS_DB_PASSWORD, $env:PRODUCTOS_DB_PASSWORD)) {
    if ($clave -notmatch '^[A-Za-z0-9]{12,64}$') {
        throw 'Para esta inicialización automática usa claves Oracle de 12 a 64 letras y números.'
    }
}
$sql = @"
WHENEVER SQLERROR EXIT SQL.SQLCODE
SET ECHO OFF FEEDBACK OFF
ALTER SESSION SET CONTAINER=FREEPDB1;
DECLARE
    cantidad NUMBER;
BEGIN
    SELECT COUNT(*) INTO cantidad FROM dba_users WHERE username='TIENDA_USUARIOS';
    IF cantidad=0 THEN
        EXECUTE IMMEDIATE 'CREATE USER tienda_usuarios IDENTIFIED BY "$env:USUARIOS_DB_PASSWORD" DEFAULT TABLESPACE users QUOTA 20M ON users';
        EXECUTE IMMEDIATE 'GRANT CREATE SESSION, CREATE TABLE, CREATE SEQUENCE TO tienda_usuarios';
    END IF;
    SELECT COUNT(*) INTO cantidad FROM dba_users WHERE username='TIENDA_PRODUCTOS';
    IF cantidad=0 THEN
        EXECUTE IMMEDIATE 'CREATE USER tienda_productos IDENTIFIED BY "$env:PRODUCTOS_DB_PASSWORD" DEFAULT TABLESPACE users QUOTA 20M ON users';
        EXECUTE IMMEDIATE 'GRANT CREATE SESSION, CREATE TABLE, CREATE SEQUENCE TO tienda_productos';
    END IF;
END;
/
EXIT
"@
$sql | docker exec -i tienda-semana2-oracle sqlplus -s / as sysdba
if ($LASTEXITCODE -ne 0) { throw 'Error al crear los esquemas Oracle.' }

foreach ($tipo in @('usuarios', 'productos')) {
    $usuario = [Environment]::GetEnvironmentVariable($tipo.ToUpper() + '_DB_USER')
    $clave = [Environment]::GetEnvironmentVariable($tipo.ToUpper() + '_DB_PASSWORD')
    $consulta = @"
WHENEVER SQLERROR EXIT SQL.SQLCODE
SET HEADING OFF FEEDBACK OFF ECHO OFF
CONNECT $usuario/"$clave"@FREEPDB1
SELECT COUNT(*) FROM user_tables WHERE table_name=UPPER('$tipo');
EXIT
"@
    $salida = $consulta | docker exec -i tienda-semana2-oracle sqlplus -s /nolog
    if ($LASTEXITCODE -ne 0) { throw "No se pudo conectar al esquema $usuario." }
    if (($salida | Out-String).Trim() -eq '0') {
        $archivo = if ($tipo -eq 'usuarios') { '01_usuarios.sql' } else { '02_productos.sql' }
        $contenido = Get-Content -LiteralPath "database/$archivo" -Raw
        $carga = "WHENEVER SQLERROR EXIT SQL.SQLCODE`nCONNECT $usuario/`"$clave`"@FREEPDB1`n$contenido`nEXIT"
        $carga | docker exec -i tienda-semana2-oracle sqlplus -s /nolog
        if ($LASTEXITCODE -ne 0) { throw "Error cargando $archivo." }
    } else {
        Write-Host "La tabla $tipo ya existe; se conservan sus datos."
    }
}
Write-Host 'Oracle listo: localhost:1521/FREEPDB1. Se crearon tres usuarios y cinco productos en una instalación nueva.'

