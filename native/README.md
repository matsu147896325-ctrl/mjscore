# MJscore Native Apps

スマホにインストールするアプリ版の置き場所です。

## Android

`native/android` を Android Studio で開いてください。

1. Android Studioを起動
2. `Open` で `native/android` を選択
3. Gradle同期
4. 実機またはエミュレーターでRun

このPCではAndroid SDK/Gradleが見つからなかったため、ここではAPK作成までは実行していません。

## iPhone

iPhoneアプリを作るにはMacとXcodeが必要です。Windows上のIntelliJ IDEAだけではiPhone向けのビルドと署名はできません。

SwiftUI版のソースは `native/ios/MJscore` に置いてあります。

Xcodeで新規iOS Appプロジェクトを作成し、`MJscoreApp.swift` と `ContentView.swift` を置き換えると、同じ仕様のiPhone版として動かせます。
