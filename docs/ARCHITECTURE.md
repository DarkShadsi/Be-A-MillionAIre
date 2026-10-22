# Architecture

The game is one Java 21 / JavaFX Maven project. Gameplay and local persistence work offline after dependencies are available. `Launcher` dispatches `--cli` to the existing question-bank practice mode; otherwise it starts `MillionaireApplication`.

## Responsibilities

| Package | Responsibility |
| --- | --- |
| `domain` | Validated immutable questions, choices, categories, difficulties and indexed question pools. |
| `engine` | Question selection, answer evaluation, help eligibility, round status and payouts, independent of JavaFX. |
| `application` | Startup state, preferences and repository ports. Question and preference failures are reported independently. |
| `storage` | UTF-8 CSV loading and local JSON settings/history. |
| `ui` | Retained screens, navigation, input, audio and rendering of presenter state. |
| `ui.state` | Immutable gameplay/results view state and mapping from engine decisions. |
| `ui.design` | Shared controls, geometry, scaling, overlays, text fitting and motion ownership. |
| `ui.assets` | Classpath artwork/font loading and asset manifest validation. |
| `tools` | Release-input checks for complete category pools and decodable audio. |

There is no FXML layer. JavaFX nodes supply live text and controls over retained artwork. The design viewport scales a 1920×1080 canvas; the window minimum is 960×540. Styles, artwork, fonts and WAVs are classpath resources; `MCQ/questions.csv` remains an external content file.

## Startup and navigation

Startup reads the small preferences first, then loads the question bank and assets on a background task while the splash runs. Home is revealed only after both the splash introduction and loading finish. The splash-to-Home handoff preserves the already constructed Home tree.

`GamePresentation` coordinates Home, Categories, Gameplay, Menu, Results, Scores and Instructions through `ScreenRouter`. Each screen retains its nodes and motion scope. Data refresh, appearance changes and screen entry have separate callbacks. Theme or answer-state changes update existing properties; Menu reads saved payout totals on entry and Instructions resets to its first page on entry. Overlays disable the background, contain keyboard focus, and restore focus when closed.

`GameplayLayout` supplies shared frame, reading area, ladder and credits coordinates. `QuestionContent` measures the complete question and four wrapping choices together, tightens spacing, then reduces font sizes within fixed bounds. Selection/help refreshes do not repeat fitting when the text and dimensions are unchanged. Correct/terminal feedback occupies the same stationary reading area. Score History is a separate scrolling list.

## Rules and results

`GameRound` is authoritative. `QuestionSelector` selects five questions per difficulty and returns fresh questions with shuffled choices and matching A–D correct-answer IDs; it does not mutate the bank. Each help can be activated once per round, with one activation per question. Second Chance consumes its activation immediately and permits one retry; that retry permits neither another help nor Walk Away. The coordinator reopens a revealed clue without activating it again.

The engine calculates earned credits, checkpoints and final payouts. Correct and terminal answer feedback stays in Gameplay until Continue or View results. Walk Away opens Results after confirmation. Results selects a full-screen win composition or a centered loss/walk-away panel. Play Again returns to Categories.

When a GUI round finishes, its UUID is recorded once with category, outcome, correct-answer count and payout. `ScoreStore.append` also rejects a duplicate ID. Abandoning an unfinished round does not save a result. CLI practice uses the same selector and round engine but does not write GUI score history; its inspect command deliberately reveals the answer.

## Persistence and audio

`AppPaths` normally reads questions from `MCQ` and writes `settings.json` and `scores.json` to `%LOCALAPPDATA%\BeAMillionaire`, falling back to `~/.be-a-millionaire`. `millionaire.questionDataDir` and `millionaire.dataDir` can override those locations for isolated checks. Settings/history writes use a temporary file and replacement; an unreadable score file is reported and preserved.

Menu totals use a `long` sum of saved payouts, read only on entry. Empty history shows zero; an `IOException` shows Unavailable. A failed save is reported without undoing the current preferences or payout.

`GameAudio` uses the JDK's sampled-audio support. Audio failure does not prevent play. Home music is shared by Home, Menu, Categories, Scores and Instructions; round music belongs to Gameplay, and Results is silent apart from the win cue on entry. Sound and motion have separate lifecycles. See [CONTINUOUS_MOTION.md](CONTINUOUS_MOTION.md) and [audio README](../src/main/resources/audio/README.txt).

## Build and verification boundary

The source scripts support Maven checkouts (`target/classes` plus `target/lib`) and prepared offline source folders (`lib`, `sources.txt` and `out`). The Maven artifact is a thin JAR; runtime dependencies remain separate. Build 20 packaging is planned and is not supplied by this checkout.

Maven `verify` compiles and packages, with no Maven test sources in the current tree. `ReleaseCheck` loads the CSV and decodes all nine required WAVs without opening a GUI or audio device. The 54-question bank supports three categories; five empty categories prevent a complete release check. Gameplay, fitting, navigation and motion checks must be reported separately from build success. Build 19 changes documentation and source-launch support without changing the UI/engine APIs or saved-data format.
