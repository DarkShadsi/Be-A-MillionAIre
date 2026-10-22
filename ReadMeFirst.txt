BE A MILLIONAIRE - BUILD 19 SOURCE INSTRUCTIONS

Requirements
Use Windows and JDK 21 or newer. Set JAVA_HOME to the JDK folder, or put its bin folder
on PATH. The scripts select JAVA_HOME first. Run commands from the project root.
Paths containing spaces are supported.

Build and launch
  scripts\build-source.cmd
  scripts\run-source.cmd
  scripts\run-source.cmd --cli

In PowerShell, prefix these paths with .\, for example:
  .\scripts\build-source.cmd
  .\mvnw.cmd -B -ntp verify

A Git checkout with pom.xml and .mvn uses the Maven wrapper. The build runs
verify, produces target/classes and copies runtime dependencies to target/lib.
The first build needs internet access for Maven and dependencies.

A prepared offline source folder must include lib, sources.txt, src, MCQ and
scripts. Its build uses javac --release 21, writes classes to out and copies
resources there. No network connection is needed when these inputs are present.
Keep the whole folder together; an ordinary checkout does not include that
prepared offline library folder or source list.

The run script selects the matching compiled output and forwards arguments.
Use --cli for question-bank practice or --smoke-test for the brief GUI startup
check. The selected Java runtime must be able to open a JavaFX window for the
GUI, including smoke mode. A runtime with bundled JavaFX is launched with
--add-modules javafx.controls automatically. The existing cli.cmd remains
available in a checkout and uses Maven to prepare its practice-mode classpath.

If a command reports a missing JDK or build output, fix JAVA_HOME/PATH or run
the build first. On a Windows machine that requires vendor-signed JavaFX native
libraries, select a compatible vendor-signed Java 21 runtime that bundles
JavaFX, such as BellSoft Liberica Full, then build and launch again.

Release-input check after a Maven build
  java -cp "target/classes;target/lib/*" com.beamillionaire.tools.ReleaseCheck

Use a Java 21 command for this check. It validates category pools and decodes
the required WAV resources without a graphics display, speaker or sound card.
The current bank has 54 questions and three playable categories, each with six
Easy, six Medium and six Hard questions. The other five categories have no
questions and remain the release-input gap. All nine required WAVs are present.
Maven verify compiles and packages; this checkout has no Maven test sources.
Run gameplay and visual checks separately.

How to play
Choose a playable category. A round has 5 Easy, 5 Medium and 5 Hard questions.
Read the stationary question and all four choices, select A-D, then Lock Answer.
You can change a selection before locking it. Read correct-answer feedback,
then Continue. After the final correct answer or a loss, View results opens the
outcome: a full-screen win or a centered Round Over panel. Play Again opens
Categories. Home returns Home.

Each help is once per round, with one activation per question. 50:50 removes
two wrong choices. Clue opens a hint and View clue can reopen it on that same
question. Second Chance must be activated before locking. It is consumed on
activation, even if the first answer is correct, and grants one retry if the
first answer is wrong. During a retry, the first wrong choice is removed,
another help is unavailable and Walk Away is unavailable.

Q4, Q9 and Q12 secure 1,000, 30,000 and 100,000 credits. A terminal wrong answer
pays the last secured checkpoint, or zero before Q4. Walk Away is available
before the first locked attempt on a question; confirm to bank earned credits
and open its centered payout panel. Fifteen correct answers pay 1,000,000.

Menu and keyboard
Menu offers theme, reduced motion, sound, fullscreen, Score History and
Instructions. Total credits earned is the sum of saved completed-round payouts,
refreshed when Menu opens. Empty history shows zero; unreadable history shows
Unavailable and the file is preserved.

Instructions has five pages: Categories, Questions, Helps, Ladder and Winning.
Next/Previous navigate, Done on the last page returns Home, and Home returns
Home from every page. Reopening starts at Categories. The guide is read-only.
Questions, choices, feedback and the guide do not scroll; Score History does.

A-D selects answers. Tab/Shift+Tab moves focus. Enter or Space activates the
focused button; Enter also locks or continues gameplay when no button has
focus. F11 toggles fullscreen. Escape closes a popup before leaving fullscreen.
Backspace opens the leave-round confirmation in gameplay and returns Home
from other screens. Leaving an unfinished round does not record a payout.

Saved data and resources
Settings and scores normally live in %LOCALAPPDATA%\BeAMillionaire as
settings.json and scores.json. The fallback is .be-a-millionaire in the user's
home directory. The question bank is MCQ\questions.csv, separate from saved
data. A damaged score file is reported and is not overwritten automatically.
See MCQ\README.md for question authoring and src\main\resources\audio\README.txt
for audio behavior and source-record limitations. Preserve third-party notices.

Packaging status
Build 20 source/executable packaging is planned. Its packaging script and EXE
distribution are not included here. The Maven JAR needs its runtime libraries
and an explicit classpath launch; use the source scripts above for this build.

Build 19 ownership: Person 2 implements the source-launch instructions and
scripts; Person 1 reviews. Game-rule and UI/engine APIs remain unchanged.
