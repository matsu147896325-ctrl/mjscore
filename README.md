# Mahjong Score Board

IntelliJ IDEAで開いて実行できる、4人麻雀のスコア集計アプリです。

## 起動方法

1. IntelliJ IDEAでこのフォルダを開きます。
2. `src/main/java/com/example/mahjong/MahjongScoreBoard.java` を開きます。
3. `main` メソッド横の実行ボタンを押します。

## 機能

- 持ち点、返し点を入力できます。
- ウマはプルダウンで選択できます。初期値は `[10-20]` です。
- 1〜7試合分の得点を入力できます。
- 各試合の計算ボタンで、返し点・順位・ウマ・オカを反映したスコアを表示します。
- TOTALには各プレイヤーの合計スコアと総合順位を表示します。

## スマホ版

`mobile-app/index.html` をブラウザで開くと、iPhone・Android向けのWebアプリ版として使えます。
スマホで使う場合は、この `mobile-app` フォルダをWebサーバーに置くか、同じWi-Fi内のPCから配信してスマホのブラウザで開いてください。

同じWi-Fi内で試す場合は、PowerShellで以下を実行します。

```powershell
powershell -ExecutionPolicy Bypass -File ".\start-mobile-server.ps1"
```

表示された `Phone:` のURLをスマホのブラウザで開いてください。

外出時にも使う場合は、`mobile-app` フォルダをGitHub PagesやNetlifyなどのHTTPSサイトとして公開し、スマホのホーム画面に追加してください。
詳しくは `mobile-app/README.md` を参照してください。
