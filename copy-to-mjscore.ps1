$source = "C:\Users\matsu\Documents\Codex\2026-05-16\intellij-idea\src\main\java\com\example\mahjong\MahjongScoreBoard.java"
$destinationDir = "D:\アプリ開発\MJscore\src\com\example\mahjong"
$destination = Join-Path $destinationDir "MahjongScoreBoard.java"

New-Item -ItemType Directory -Force -Path $destinationDir | Out-Null
Copy-Item -Path $source -Destination $destination -Force

Write-Host "Copied: $destination"
Write-Host "Next: IntelliJ IDEA > Build > Rebuild Project, then run MahjongScoreBoard.main"
