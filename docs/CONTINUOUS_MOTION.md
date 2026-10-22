# Motion and stationary reading

The presentation uses JavaFX animation, native shapes and shared CSS. Question text, choices, answer feedback, prize amounts and Instructions text stay stationary while decorative elements move around them.

## Ownership and lifecycle

Each retained screen owns a `MotionScope`. Navigation, detachment, minimization and modal coverage pause the covered screen's animations while preserving phase. Returning resumes that phase. Reduced motion stops registered animations and restores their static poses; disposal closes scopes and releases listeners. Refreshing answers, helps or preferences updates retained nodes without replaying screen entrances.

Screen-entry callbacks are separate from appearance/data refreshes. Menu reads saved payouts when opened; Instructions resets to Categories when entered. Changing a preference does not repeat those entry actions. Category names restart their marquee only when text or available width changes. Popup panels own and close their own scopes.

Motion availability depends on the screen being attached, visible, showing and not minimized, rather than keyboard focus alone. Audio suspension and the current music track do not freeze visual effects. Sound Off and Reduce motion are independent preferences. Hidden/minimized windows suspend audio; transient effects stop. The splash handoff also pauses while the window is hidden or minimized.

## Gameplay

The reading area is 1120×560 at (400,190) on the 1920×1080 canvas. `GameplayLayout` also aligns the native frame, side panels, ladder and credits outline. `QuestionContent` fits the question and all four choices as one block with a separator and explicit A–D labels. Question text starts at 36 px, choices at 30 px; fitting tightens spacing first, then reduces to minimums of 30 px and 26 px. Choice rows share one font size. Text/dimension changes invalidate fitting; selection and help-state changes retain it.

After a correct or terminal answer, stationary feedback replaces the reading block and shows the answer and earned credits/payout. Continue advances after a correct answer. View results opens the full-screen win after Q15 or the centered Round Over panel after a loss. Walk Away opens its centered panel after confirmation. Terminal feedback does not automatically navigate away.

Each help is once per round and only one activates per question. Second Chance is consumed on activation, even if the first answer is correct. Its retry removes the first wrong choice and permits neither another help nor Walk Away. View clue reopens a revealed hint on that question without another activation.

Cyan identifies the current potential prize; gold identifies earned progress and the credits box. Earned progress changes with correct answers, rather than selection or a retry. Checkpoint accents last 320 ms and use localized outline effects. Category and Walk Away artwork can animate while their hit regions remain stationary. A–D selects; Enter/Space retains normal focused-button activation. The gameplay Enter shortcut locks or continues only when a button does not have focus.

## Menu, Instructions and outcomes

Menu uses a centered 1320 px panel. Its total-credits card sums saved completed-round payouts on entry, displays zero for an empty history, and shows Unavailable when history cannot be read. The card accents pulse over five seconds. Preference refreshes do not reread history.

Instructions is a read-only route with Categories, Questions, Helps, Ladder and Winning pages. It uses six bundled illustrations across the two themes with fitted explanation text. Red teaching outlines identify relevant areas and pulse over three seconds; reduced motion retains static outlines. Next/Previous update existing content, Previous is disabled on the first page, and the final Next becomes Done. Home, Done and Backspace return Home. Reopening starts at Categories. Illustrations do not create rounds or write scores.

Win uses a full-screen composition with an answered ring, winnings panel, ladder and perspective platform. Small ring groups rotate independently. Gold segments travel clockwise around the winnings panel and final ladder row; their text and values stay still. Loss and Walk Away use centered panels, and Play Again opens Categories. Menu, Scores and standard Results share the centered panel wrapper. Score History retains its scrolling list.

## Ambient effects and startup

- Home has eight native hexagons with independent 13–23-second drift, rotation and pointer repulsion up to 12 px, followed by a 600 ms return. Wires follow the endpoints without intercepting input.
- Overflowing category names move at 30 design px/second with a 1.2-second hold and 48 px repeat gap. Short names stay still; reduced motion uses fitted wrapped names.
- Home and Gameplay circuits combine breathing accents and narrow traveling highlights. Popup traces travel for three seconds within a 5.5-second cycle. Reduced motion hides traveling overlays and retains structural frames.
- The A/I title contours breathe over independent six/eight-second cycles, with thin contour highlights on staggered eight/ten-second cycles. The title artwork stays still; theme changes recolor the accents in place.
- Splash displays the fixed caption `Loading...`. Its 25%-width segment bounces with a 1.2-second auto-reversing transition until dismissal, including after background loading is ready. The HUD rotates independently.
- Splash shimmer has a five-second duration and a midpoint at 2.5 seconds. The logo sound is scheduled at 1.075 seconds of shimmer time to align its 1.425-second impact with that midpoint; late FX pulses offset playback. Reveal begins at 3.1 seconds, fades for 750 ms, then holds for three seconds. Handoff waits for both the introduction and background loading.
- `SplashHandoff` dissolves the splash over an already laid-out Home tree for one second, or 180 ms with reduced motion. It blocks input until complete, then stops and removes the splash. The saved reduced-motion setting shortens the handoff; the splash introduction itself retains its current animations.

Home music also plays on Menu, Categories, Scores and Instructions. A final correct answer plays the correct cue while its feedback remains visible; the win cue plays when the win Results screen opens. See the [audio README](../src/main/resources/audio/README.txt) for the remaining cues and provenance records.

## Verification

Maven `verify` compiles and packages; this checkout has no Maven test sources. Report that result separately from GUI/content verification. The release-input check needs no graphics display or audio device and currently fails for the five empty categories.

For UI changes, check fullscreen, 1280×720 and the 960×540 minimum; both themes; all imported questions and choice permutations; complete stationary feedback; guide navigation; keyboard focus; Menu totals and unreadable history; navigation/modal phase retention; reduced motion; minimization and disposal. Use isolated settings/history directories for automated checks and record the runtime and actual checks performed. The source launch commands are in [ReadMeFirst.txt](../ReadMeFirst.txt).
