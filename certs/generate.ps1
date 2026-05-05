# Generates local development certificates for swim-ed254-provider.
#
# This project is self-contained: no dependency on swim-developer-tools.
#
# Output (all files in certs/):
#   ca.crt                   CA certificate (mkcert root CA, PEM)
#   tls.crt                  Provider HTTPS server certificate (PEM)
#   tls.key                  Provider HTTPS server private key  (PEM)
#   client.crt               Provider AMQP client certificate  (PEM) -- mTLS to Artemis
#   client.key               Provider AMQP client private key  (PEM)
#   broker.p12               Artemis broker keystore (PKCS12)
#   ca-truststore.p12        Artemis CA truststore   (PKCS12)  -- verifies consumer client certs
#   keycloak-keystore.p12    Keycloak HTTPS keystore (PKCS12)
#   validator-keystore.p12   Validator AMQP client keystore (PKCS12) -- mTLS to Artemis
#   validator-truststore.p12 Validator CA truststore (PKCS12) -- trusts broker + provider HTTPS
#
# Prerequisites:
#   mkcert   https://github.com/FiloSottile/mkcert  (winget install FiloSottile.mkcert)
#   keytool  Bundled with JDK 21
#   openssl  Bundled with Git for Windows or winget install ShiningLight.OpenSSL
#
# Usage (run from the project root):
#   powershell -ExecutionPolicy Bypass -File certs/generate.ps1

$ErrorActionPreference = "Stop"

$ScriptDir  = Split-Path -Parent $MyInvocation.MyCommand.Path
$CertsDir   = $ScriptDir
$TmpDir     = Join-Path $CertsDir ".tmp"
$Password   = "changeit"

$BrokerSans = @(
    "localhost",
    "127.0.0.1",
    "ed254-provider-artemis",
    "provider-artemis",
    "artemis.127.0.0.1.nip.io",
    "ed254-provider-artemis.127.0.0.1.nip.io",
    "ed254-provider-artemis.swim.lab"
)

$KeycloakSans = @(
    "keycloak.swim.lab",
    "keycloak",
    "localhost",
    "127.0.0.1",
    "keycloak.127.0.0.1.nip.io"
)

$ProviderSans = @(
    "localhost",
    "127.0.0.1",
    "ed254-provider",
    "provider.127.0.0.1.nip.io",
    "ed254-provider.127.0.0.1.nip.io",
    "ed254-provider.swim.lab"
)

$ClientSans = @(
    "ed254-provider",
    "localhost",
    "127.0.0.1",
    "ed254-provider.127.0.0.1.nip.io",
    "ed254-provider.swim.lab"
)

$ValidatorSans = @(
    "ed254-provider-validator",
    "localhost",
    "127.0.0.1",
    "ed254-provider-validator.127.0.0.1.nip.io",
    "ed254-provider-validator.swim.lab"
)

# --- Cleanup ------------------------------------------------------------------

foreach ($f in @("ca.crt","tls.crt","tls.key","client.crt","client.key","broker.p12","ca-truststore.p12","keycloak-keystore.p12","validator-keystore.p12","validator-truststore.p12")) {
    $target = Join-Path $CertsDir $f
    if (Test-Path $target) { Remove-Item $target -Force }
}

# --- Prerequisites ------------------------------------------------------------

function Require-Command($name, $hint) {
    if (-not (Get-Command $name -ErrorAction SilentlyContinue)) {
        Write-Error "ERROR: '$name' not found. $hint"
        exit 1
    }
}

Require-Command mkcert  "Install: winget install FiloSottile.mkcert  |  https://github.com/FiloSottile/mkcert"
Require-Command keytool "Install JDK 21: https://adoptium.net"
Require-Command openssl "Install Git for Windows or winget install ShiningLight.OpenSSL"

# --- Setup --------------------------------------------------------------------

Write-Host ""
Write-Host "=== swim-ed254-provider local PKI ==="
Write-Host ""

mkcert -install

New-Item -ItemType Directory -Force -Path $TmpDir | Out-Null

try {

# CA certificate
$caRoot = (mkcert -CAROOT).Trim()
Copy-Item "$caRoot\rootCA.pem" "$CertsDir\ca.crt"
Write-Host "[CA] ca.crt  <-  $caRoot\rootCA.pem"

# --- Artemis broker server certificate ----------------------------------------

Write-Host ""
Write-Host "[broker] Generating Artemis server certificate..."
Write-Host "         SANs: $($BrokerSans -join ', ')"

& mkcert -cert-file "$TmpDir\broker.crt" -key-file "$TmpDir\broker.key" @BrokerSans

& openssl pkcs12 -export `
    -in       "$TmpDir\broker.crt" `
    -inkey    "$TmpDir\broker.key" `
    -certfile "$CertsDir\ca.crt" `
    -out      "$CertsDir\broker.p12" `
    -name     broker `
    -password "pass:$Password"

Write-Host "[broker] broker.p12  (keystore, password: $Password)"

# --- Artemis CA truststore ----------------------------------------------------

Write-Host ""
Write-Host "[artemis-truststore] Creating CA truststore for Artemis..."

& keytool -importcert -noprompt `
    -alias     swim-ca `
    -file      "$CertsDir\ca.crt" `
    -keystore  "$CertsDir\ca-truststore.p12" `
    -storetype PKCS12 `
    -storepass $Password

Write-Host "[artemis-truststore] ca-truststore.p12  (password: $Password)"

# --- Provider HTTPS server certificate ----------------------------------------

Write-Host ""
Write-Host "[provider] Generating provider HTTPS server certificate..."
Write-Host "           SANs: $($ProviderSans -join ', ')"

& mkcert -cert-file "$CertsDir\tls.crt" -key-file "$CertsDir\tls.key" @ProviderSans
Write-Host "[provider] tls.crt, tls.key  (PEM)"

# --- Keycloak HTTPS server certificate and keystore ---------------------------

Write-Host ""
Write-Host "[keycloak] Generating Keycloak HTTPS server certificate..."
Write-Host "           SANs: $($KeycloakSans -join ', ')"

& mkcert -cert-file "$TmpDir\keycloak.crt" -key-file "$TmpDir\keycloak.key" @KeycloakSans

& openssl pkcs12 -export `
    -in       "$TmpDir\keycloak.crt" `
    -inkey    "$TmpDir\keycloak.key" `
    -certfile "$CertsDir\ca.crt" `
    -out      "$CertsDir\keycloak-keystore.p12" `
    -name     keycloak `
    -password "pass:$Password"

Write-Host "[keycloak] keycloak-keystore.p12  (PKCS12, password: $Password)"

# --- Provider AMQP client certificate -----------------------------------------

Write-Host ""
Write-Host "[client] Generating provider client certificate for AMQP..."
Write-Host "         SANs: $($ClientSans -join ', ')"

& mkcert -client -cert-file "$CertsDir\client.crt" -key-file "$CertsDir\client.key" @ClientSans
Write-Host "[client] client.crt, client.key  (PEM)"

# --- Validator AMQP client certificate and keystores --------------------------

Write-Host ""
Write-Host "[validator] Generating validator client certificate for AMQP..."
Write-Host "            SANs: $($ValidatorSans -join ', ')"

& mkcert -client -cert-file "$TmpDir\validator-client.crt" -key-file "$TmpDir\validator-client.key" @ValidatorSans

& openssl pkcs12 -export `
    -in       "$TmpDir\validator-client.crt" `
    -inkey    "$TmpDir\validator-client.key" `
    -certfile "$CertsDir\ca.crt" `
    -out      "$CertsDir\validator-keystore.p12" `
    -name     validator `
    -password "pass:$Password"

Write-Host "[validator] validator-keystore.p12  (PKCS12, password: $Password)"

& keytool -importcert -noprompt `
    -alias     swim-ca `
    -file      "$CertsDir\ca.crt" `
    -keystore  "$CertsDir\validator-truststore.p12" `
    -storetype PKCS12 `
    -storepass $Password

Write-Host "[validator] validator-truststore.p12  (PKCS12, password: $Password)"

} finally {
    Remove-Item -Recurse -Force $TmpDir -ErrorAction SilentlyContinue
}

# --- Summary ------------------------------------------------------------------

Write-Host @"

=== Done ===

  certs/
  ├── ca.crt                    CA certificate (mkcert root CA)
  ├── tls.crt                   Provider HTTPS server cert      (PEM)
  ├── tls.key                   Provider HTTPS server key       (PEM)
  ├── client.crt                Provider AMQP client cert       (PEM)
  ├── client.key                Provider AMQP client key        (PEM)
  ├── broker.p12                Artemis broker keystore         (PKCS12, password: $Password)
  ├── ca-truststore.p12         Artemis CA truststore           (PKCS12, password: $Password)
  ├── keycloak-keystore.p12     Keycloak HTTPS keystore         (PKCS12, password: $Password)
  ├── validator-keystore.p12    Validator AMQP client keystore  (PKCS12, password: $Password)
  └── validator-truststore.p12  Validator CA truststore         (PKCS12, password: $Password)

  Broker SANs    : $($BrokerSans -join ', ')
  Keycloak SANs  : $($KeycloakSans -join ', ')
  Provider SANs  : $($ProviderSans -join ', ')
  Client SANs    : $($ClientSans -join ', ')
  Validator SANs : $($ValidatorSans -join ', ')

  All keystore / truststore password: $Password

"@
