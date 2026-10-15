Be a Millionaire audio

The six named WAV files were selected and supplied by the project team.
win.wav is converted from Kenney Music Jingles: 8-Bit jingles/jingles_NES12.ogg (CC0).
Source links, original/output hashes and conversion details are in SOURCES.json.
All bundled files are 44.1 kHz, 16-bit signed little-endian PCM WAV, mono/stereo.

logoSound: once at startup; existing startup timing is retained.
homeScreenSound: loops on Home only.
inGameSound: loops through an active round and its popups; stops on round end.
hover: enabled buttons and answer choices, pointer entry and keyboard navigation.
correct/wrong: once per accepted locked answer, including Second Chance wrong guesses.
win: replaces correct on the final winning answer. Results do not replay feedback.

The saved Sound On/Off menu preference controls every sound. Minimizing pauses
background audio and stops transient effects. Gameplay music lowers under answer feedback.

Playback gains: Home music 1.0x, gameplay music 1.6x, logo 1.35x, hover 0.35x, answer/victory effects 0.95x.
Gameplay music lowers to 20% during answer feedback.
