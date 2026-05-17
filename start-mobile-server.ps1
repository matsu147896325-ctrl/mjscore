$root = [System.IO.Path]::GetFullPath((Join-Path $PSScriptRoot "mobile-app"))
$port = 8080

function Get-ContentType($path) {
  $ext = [System.IO.Path]::GetExtension($path).ToLowerInvariant()
  switch ($ext) {
    ".html" { "text/html; charset=utf-8" }
    ".css" { "text/css; charset=utf-8" }
    ".js" { "text/javascript; charset=utf-8" }
    ".webmanifest" { "application/manifest+json; charset=utf-8" }
    ".svg" { "image/svg+xml" }
    default { "application/octet-stream" }
  }
}

function Send-Response($client, $status, $contentType, $bytes) {
  $stream = $client.GetStream()
  $header = "HTTP/1.1 $status`r`nContent-Type: $contentType`r`nContent-Length: $($bytes.Length)`r`nConnection: close`r`n`r`n"
  $headerBytes = [System.Text.Encoding]::UTF8.GetBytes($header)
  $stream.Write($headerBytes, 0, $headerBytes.Length)
  $stream.Write($bytes, 0, $bytes.Length)
  $stream.Flush()
}

function Get-LocalIp() {
  $lines = ipconfig | Select-String "IPv4"
  foreach ($line in $lines) {
    $ip = ($line.ToString() -split ":")[-1].Trim()
    if ($ip -and $ip -notlike "127.*") {
      return $ip
    }
  }
  return $null
}

$listener = [System.Net.Sockets.TcpListener]::new([System.Net.IPAddress]::Any, $port)
try {
  $listener.Start()
} catch {
  Write-Host "Port $port is already in use."
  Write-Host "If the app is already running, open http://localhost:$port on this PC."
  Write-Host "If not, close the old server window and run this file again."
  Read-Host "Press Enter to close"
  exit 1
}

$ip = Get-LocalIp
Write-Host "MJscore mobile server"
Write-Host "PC:    http://localhost:$port"
if ($ip) {
  Write-Host "Phone: http://$($ip):$port"
}
Write-Host "Stop:  Ctrl + C"

try {
  while ($true) {
    $client = $listener.AcceptTcpClient()
    try {
      $reader = [System.IO.StreamReader]::new($client.GetStream())
      $requestLine = $reader.ReadLine()
      if (-not $requestLine) {
        $client.Close()
        continue
      }

      $parts = $requestLine.Split(" ")
      $urlPath = [System.Uri]::UnescapeDataString($parts[1].Split("?")[0])
      if ($urlPath -eq "/") {
        $urlPath = "/index.html"
      }

      $relative = $urlPath.TrimStart("/").Replace("/", [System.IO.Path]::DirectorySeparatorChar)
      $filePath = [System.IO.Path]::GetFullPath((Join-Path $root $relative))

      if (-not $filePath.StartsWith($root) -or -not [System.IO.File]::Exists($filePath)) {
        $bytes = [System.Text.Encoding]::UTF8.GetBytes("Not Found")
        Send-Response $client "404 Not Found" "text/plain; charset=utf-8" $bytes
      } else {
        $bytes = [System.IO.File]::ReadAllBytes($filePath)
        Send-Response $client "200 OK" (Get-ContentType $filePath) $bytes
      }
    } finally {
      $client.Close()
    }
  }
} finally {
  $listener.Stop()
}
