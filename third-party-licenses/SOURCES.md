# Component license sources

These files apply only to the named dependencies. They do not assign a license to the game's own source code. The portable package places this directory at `licenses/components/`. Upstream texts were downloaded unmodified on 2026-09-12.

## OpenJFX 21.0.12

The official `openjdk/jfx21u` tag `21.0.12+3` resolves to commit `6ae7c978ef7cc7900281d49712cea5bf529858d0`. The distributed modules are javafx.base, javafx.controls and javafx.graphics. License files below are pinned to that commit:

| Local file | Official source |
| --- | --- |
| OpenJFX-LICENSE.txt | [LICENSE, including the Classpath Exception](https://raw.githubusercontent.com/openjdk/jfx21u/6ae7c978ef7cc7900281d49712cea5bf529858d0/LICENSE) |
| OpenJFX-ADDITIONAL_LICENSE_INFO.txt | [Additional licensing information](https://raw.githubusercontent.com/openjdk/jfx21u/6ae7c978ef7cc7900281d49712cea5bf529858d0/ADDITIONAL_LICENSE_INFO) |
| OpenJFX-ASSEMBLY_EXCEPTION.txt | [OpenJDK Assembly Exception](https://raw.githubusercontent.com/openjdk/jfx21u/6ae7c978ef7cc7900281d49712cea5bf529858d0/ASSEMBLY_EXCEPTION) |
| OpenJFX-jpeg_fx.md | [Graphics: Independent JPEG Group notice](https://raw.githubusercontent.com/openjdk/jfx21u/6ae7c978ef7cc7900281d49712cea5bf529858d0/modules/javafx.graphics/src/main/legal/jpeg_fx.md) |
| OpenJFX-mesa3d.md | [Graphics: Mesa and Khronos notices](https://raw.githubusercontent.com/openjdk/jfx21u/6ae7c978ef7cc7900281d49712cea5bf529858d0/modules/javafx.graphics/src/main/legal/mesa3d.md) |

The [corresponding OpenJFX source tree](https://github.com/openjdk/jfx21u/tree/6ae7c978ef7cc7900281d49712cea5bf529858d0) and its [source archive](https://github.com/openjdk/jfx21u/archive/6ae7c978ef7cc7900281d49712cea5bf529858d0.zip) are available from OpenJDK. No library modifications are made by this game.

## Apache License 2.0 components

`Apache-2.0.txt` is the complete license text from [Gson 2.14.0's LICENSE](https://raw.githubusercontent.com/google/gson/gson-parent-2.14.0/LICENSE). [Error Prone 2.48.0's COPYING](https://raw.githubusercontent.com/google/error-prone/v2.48.0/COPYING) supplies the same license text. Gson is copyright 2008 Google Inc.; see its [upstream release README](https://github.com/google/gson/blob/gson-parent-2.14.0/README.md).

The same Apache 2.0 terms apply to Commons CSV 1.14.1, Commons IO 2.20.0 and Commons Codec 1.19.0. Their unmodified Maven JARs retain the component-specific `META-INF/LICENSE.txt` and `META-INF/NOTICE.txt` files supplied by Apache. Keep the original JARs and these notices with the distribution.

## Other notices

Canva artwork and extracted TT Supermolot Condensed fonts are separate retained resources; this folder's dependency licenses do not cover them. See the root THIRD_PARTY_NOTICES.md for recorded provenance and permission status. The bundled runtime retains the packaging JDK's own `legal/` notices. JUnit is a test-only dependency and is not included in the portable runtime libraries.
