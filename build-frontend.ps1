<#
.SYNOPSIS
  Compila el frontend Angular en producción y regenera el contenedor Docker
  'frontend-urbano' (compose) en un solo paso. Apuntado a despliegue local/offline.

.DESCRIPTION
  1. Ejecuta `ng build --configuration production` en Frontend_Urbano_Angular.
  2. Reconstruye y recrea el servicio `frontend` con docker compose (usa el
     Dockerfile.compose, que copia el dist local y no requiere npm install offline).
  3. Verifica que el contenedor quedó "Up" y que http://localhost/ responde 200.

.EXAMPLE
  .\build-frontend.ps1
#>
[CmdletBinding()]
param(
    [switch]$SkipBuild,

    [switch]$SkipDeploy
)

$ErrorActionPreference = 'Stop'

$ROOT     = Split-Path -Parent $PSScriptRoot            # raíz del proyecto
$FRONT    = Join-Path $ROOT 'Frontend_Urbano_Angular'
$BACKEND  = Join-Path $ROOT 'ecomovilapp-Backend'
$COMPOSE  = $BACKEND
$APP_URL  = 'http://localhost/'

Write-Host "== Construccion y despliegue del frontend ==" -ForegroundColor Cyan

if (-not (Test-Path -LiteralPath $FRONT)) { throw "No existe la carpeta del frontend: $FRONT" }
if (-not (Test-Path -LiteralPath $BACKEND)) { throw "No existe la carpeta del backend: $BACKEND" }

# ── 1) Compilar el frontend en producción ──────────────────────────────
if (-not $SkipBuild) {
    Write-Host "`n[1/3] Compilando el frontend (produccion)..." -ForegroundColor Yellow
    Push-Location $FRONT
    try {
        & npx ng build --configuration production
        if ($LASTEXITCODE -ne 0) { throw "El build de Angular fallo (codigo $LASTEXITCODE)." }
    }
    finally {
        Pop-Location
    }
    Write-Host "[1/3] Build de Angular completado." -ForegroundColor Green
} else {
    Write-Host "`n[1/3] Build omitido (-SkipBuild)." -ForegroundColor DarkGray
}

# Verificar que exista el dist que el Dockerfile.compose va a copiar
$browser = Join-Path $FRONT 'dist\gestion_ecomovil\browser'
if (-not (Test-Path -LiteralPath $browser)) {
    throw "No se encontro el build compilado: $browser. Ejecuta sin -SkipBuild."
}

# ── 2) Reconstruir y recrear el contenedor del frontend ────────────────
if (-not $SkipDeploy) {
    Write-Host "`n[2/3] Reconstruyendo y recreando el contenedor 'frontend' (compose)..." -ForegroundColor Yellow
    Push-Location $COMPOSE
    try {
        & docker compose up -d --build frontend
        if ($LASTEXITCODE -ne 0) { throw "docker compose up fallo (codigo $LASTEXITCODE)." }
    }
    finally {
        Pop-Location
    }
    Write-Host "[2/3] Contenedor reconstruido." -ForegroundColor Green
} else {
    Write-Host "`n[2/3] Despliegue omitido (-SkipDeploy)." -ForegroundColor DarkGray
}

# ── 3) Verificacion ────────────────────────────────────────────────────
Write-Host "`n[3/3] Verificando el contenedor y la respuesta HTTP..." -ForegroundColor Yellow

$status = (docker ps --filter "name=frontend-urbano" --format "{{.Names}}: {{.Status}}")
Write-Host "  $status"

Start-Sleep -Seconds 3
try {
    $resp = Invoke-WebRequest -Uri $APP_URL -UseBasicParsing -TimeoutSec 15
    Write-Host ("  {0} -> HTTP {1}" -f $APP_URL, [int]$resp.StatusCode) -ForegroundColor Green
    if ([int]$resp.StatusCode -eq 200) {
        Write-Host "`nOK: el frontend quedo desplegado y respondiendo." -ForegroundColor Green
        exit 0
    } else {
        Write-Host "`nATENCION: el frontend no devolvio 200." -ForegroundColor Yellow
        exit 1
    }
}
catch {
    Write-Host "`nERROR: no se pudo alcanzar $APP_URL : $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}
