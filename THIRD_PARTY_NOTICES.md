# Third-party notices

These notices identify dependencies and font files distributed with or used to build the game. They do not grant a license for the project's own code.

| Component | Version | License and upstream |
| --- | --- | --- |
| OpenJFX (JavaFX) | 21.0.12 | GPL v2 with Classpath Exception; [OpenJFX 21 updates](https://github.com/openjdk/jfx21u/tree/21.0.12%2B3) |
| Gson | 2.14.0 | Apache License 2.0; [Gson](https://github.com/google/gson/tree/gson-parent-2.14.0) |
| Error Prone annotations | 2.48.0 | Apache License 2.0; [Error Prone](https://github.com/google/error-prone/tree/v2.48.0) |
| Apache Commons CSV | 1.14.1 | Apache License 2.0; [Commons CSV](https://commons.apache.org/proper/commons-csv/) |
| Apache Commons IO | 2.20.0 | Apache License 2.0; [Commons IO](https://commons.apache.org/proper/commons-io/) |
| Apache Commons Codec | 1.19.0 | Apache License 2.0; [Commons Codec](https://commons.apache.org/proper/commons-codec/) |
| JUnit Jupiter | 6.1.3, test only | Eclipse Public License 2.0; [JUnit](https://junit.org/) |
| Canva artwork and source export | Retained supplied resources | Provenance recorded in the artwork manifest; redistribution permissions not verified |
| TT Supermolot Condensed Regular/Bold | Retained extracted TTF files | Source manifest records WOFF2-to-TTF extraction; font redistribution permissions not verified |

The game retains the supplied Canva resources: 48 runtime PNGs, two TT Supermolot Condensed TTF files. Mokoto Glitch lettering is baked into source artwork. Their recorded origin is not evidence of redistribution permission. Before public redistribution, the owner should confirm the applicable artwork, source-export and font permissions. None of these visual resources is labeled as SIL Open Font License content.

The unmodified dependency JARs retain any notices supplied within them, but not every JAR embeds a license file. Complete Apache 2.0 and OpenJFX GPL v2/Classpath Exception texts are supplied separately in `third-party-licenses/`, together with OpenJFX's additional licensing information, assembly exception and graphics-component notices. [License sources](third-party-licenses/SOURCES.md) records the exact upstream files. The portable build includes this directory as `third-party-licenses/`.

OpenJFX's graphics module includes Independent JPEG Group and Mesa/Khronos notices. This software is based in part on the work of the Independent JPEG Group. Preserve `OpenJFX-jpeg_fx.md` and `OpenJFX-mesa3d.md` with the other component notices.

A portable build also includes the JDK runtime's `legal` directory and its notices; the runtime vendor and version depend on the JDK used for packaging. Keep those files with the portable application. Runtime libraries accompany the source ZIP. Maven resolves build and test dependencies. These component licenses do not license the game's own source code.

JavaFX source supplies the interactive controls and live text over the retained exported artwork. The dependency licenses above do not apply to the Canva artwork or extracted font files.
