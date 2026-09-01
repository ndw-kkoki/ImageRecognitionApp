# ImageRecognitionApp

画像を選択すると、クラウドAIサービスで被写体を判定し、判定結果を日本語に翻訳して表示するAndroidアプリ。

> 本リポジトリは個人開発のポートフォリオ作品です。

---

## 概要

端末内の画像を1枚選択すると、画像認識APIへ送信して被写体を英語ラベルで判定し、
翻訳APIで日本語に変換したうえで画面に表示します。「認識」と「翻訳」という2つの外部AIサービスを
1つのアプリ内で連携させる構成になっています。

## 機能

| 機能 | 説明 |
|------|------|
| **画像選択** | 端末のストレージから画像ファイルを1枚選択（`ACTION_OPEN_DOCUMENT`） |
| **画像プレビュー** | 選択した画像をアプリ内に表示 |
| **画像判定** | 選択画像をクラウドの画像認識APIへ送信し、被写体を英語ラベルで取得 |
| **日本語翻訳** | 判定結果（英語ラベル）を翻訳APIで日本語に変換し、英語・日本語両方を表示 |

## 画面構成

```
┌─────────────────────────┐
│                         │
│      画像プレビュー       │
│                         │
├─────────────────────────┤
│      [画像を選択]        │
│                         │
│     判定結果（日本語）    │
│     判定結果（英語）      │
│                         │
│      [判定する]          │
└─────────────────────────┘
```

## アーキテクチャ

```
MainActivity
 ├─ 画像選択 (ACTION_OPEN_DOCUMENT) → ImageView にプレビュー表示
 └─ 判定ボタン押下
      └─ 非同期処理 (Kotlin Coroutines)
           ├─ 画像認識API へ画像を送信 → 被写体ラベル(英語)を取得
           └─ 翻訳API へラベルを送信 → 日本語訳を取得
                └─ 結果をTextViewへ反映
```

## セットアップ

### 必要環境

- Android Studio
- 画像認識・翻訳を行う外部AIサービスのAPIキー（各自のサービス契約に基づき取得）

### ビルド

1. Android Studioでプロジェクトを開く
2. `app/src/main/java/.../MainActivity.kt` 内のAPIキー設定箇所に、取得したキーを設定
   - ポートフォリオとして公開するにあたり、キーはソースに直書きせず `local.properties` や `BuildConfig` 経由で読み込む形に置き換えることを推奨（現状はキー未設定のプレースホルダーのみ）
3. Gradle Sync 後、実機またはエミュレータで実行

```bash
./gradlew assembleDebug
```

## 技術スタック

| レイヤー | 内容 |
|---------|------|
| 言語 | Kotlin |
| UI | Android View / ConstraintLayout |
| 非同期処理 | Kotlin Coroutines |
| 外部連携 | クラウド画像認識API・翻訳API（REST） |
| ビルド | Gradle |

## ディレクトリ構成

```
app/src/main/java/com/example/imagerecognitionapp/
  MainActivity.kt       画面制御・API呼び出しの起点
  GetPathFromUri.kt      選択した画像URIから実ファイルパスを解決するユーティリティ
app/src/main/res/
  layout/activity_main.xml   画面レイアウト
app/src/test, androidTest/   単体・計装テストの雛形
```

## 今後の改善候補

- APIキーの `local.properties` 化（直書き排除）
- 判定中のローディング表示（現状はポーリング待機のみ）
- 単体テストの拡充（現状は雛形のみ）

## ライセンス

MIT License
