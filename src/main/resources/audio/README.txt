BE A MILLIONAIRE AUDIO

Runtime files
GameAudio.FILES requires these nine WAV resources:
  logoSound.wav        Startup logo cue, once during the splash.
  homeScreenSound.wav  Loops on Home, Menu, Categories, Scores and Instructions.
  inGameSound.wav      Loops on Gameplay, including its popups.
  hover.wav            Enabled controls on pointer entry or keyboard navigation.
  button_click.wav     Most button activations, including Start.
  categ_sound.wav      Start and category-card activation.
  correct.wav          Every accepted correct locked answer, including Q15.
  wrong.wav            Every accepted wrong locked answer, including a retry.
  win.wav              Entry to the winning Results screen.

All nine files are present. A win's stationary final-answer feedback plays
correct.wav; choosing View results then plays win.wav. Loss and Walk Away
Results have no looping background track. Gameplay music continues through
final-answer feedback until Results opens. A new round restarts that track;
questions, helps and popups retain its playback position.

Sound On/Off is saved in Menu and controls all music and effects. Minimizing or
hiding the window pauses background audio and stops transient effects. An audio
device or decoding failure leaves the game usable. Music state and audio
suspension are independent of decorative visual motion.

Playback gains: Home music 1.0x, Gameplay music 1.6x, logo 1.35x, hover 0.35x,
and other effects 0.95x. Current playback does not lower Gameplay music under
answer feedback. The logo cue is scheduled at 1.075 seconds of the five-second
shimmer so its 1.425-second impact meets the 2.5-second midpoint; late animation
pulses advance the sound offset.

Format checks
ReleaseCheck decodes every name in GameAudio.FILES, without opening a graphics
display or sound device. It accepts nonempty, complete, signed PCM WAVs with
16-bit little-endian samples, one or two channels, and 44.1 or 48 kHz sampling.
After a Maven build, run from the project root:
  java -cp "target/classes;target/lib/*" com.beamillionaire.tools.ReleaseCheck
The same check also requires complete question pools; the five empty categories
currently prevent a complete release result.

Provenance and notices
SOURCES.json retains source links, original/output hashes and conversion records
for seven named files. The existing records identify six as project-team supplied
with the original license unspecified, and attribute win.wav to Kenney Music
Jingles, Audio/8-Bit jingles/jingles_NES12.ogg (CC0 1.0). The supplied Kenney
notice is retained in third-party-licenses/kenney-music-jingles.txt.

button_click.wav and categ_sound.wav have no entries in SOURCES.json. The
current win.wav also differs from its recorded hash and duration; that older
Kenney record does not establish the replacement file's origin. The other six
recorded hashes match the current files. Preserve these records and the root
THIRD_PARTY_NOTICES.md; they do not establish a license for unrecorded or
replaced files, and the Kenney dedication does not cover other audio.
