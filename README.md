# SugarMelt

## It's Kotlin rewrite of [TwineHacker](https://github.com/lure0xaos/TwineHacker)

## Extension purpose and usage:

> To Debug(Or Cheat) Twine{SugarCube} Variables

it analyzes `SugarCube.State.active.variables` continuously and presents it conveniently as tabular data,
so you could easily inspect and change its content.

*This extension offers nothing more beyond you could do using the console.*
It it intended to debug games or make easier grinding in some games if you hate grinds or constantly dying
and just want to enjoy content.

**WARNING!**
**Use it on your own risk.**
**Note that you could easily break a game, make its behavior be strange and\or buggy,
or just spoil your own gaming joy.**

## Credits

This extension is based on similar one
from [this f95 thread](https://f95zone.to/threads/how-to-debug-or-cheat-twine-sugarcube-variables.6553/)
(but this one is borrowing just an idea, no code was stolen.)

(thanks to [@spectr3.9911](https://f95zone.to/members/spectr3.9911/#about))

compatible with Chrome and Firefox (possibly others could handle it too).

## Installation instructions

- **Chrome**: download repository and use *Developer Mode* then point
  `build/dist/js/productionExecutable` or `build/dist/js/developmentExecutable` directory
  - Also, you can run `unpacked` Gradle task and use `build/dist/package` directory
- **Firefox**: use *Firefox Developer Edition*, upload as *Temporary Extension* and point
  `build/package/SugarMelt.zip` file

## Building from the source

### Prerequisites:

JVM, Gradle. The best choice is using [JetBrains Idea](https://www.jetbrains.com/idea/download/), it has everything you
need.

### Building

- to build as packed extension run `package` Gradle task.
  Then in `build/package` you'll find `SugarMelt.zip` file containing packed extension.
- to build as unpacked extension run `unpacked` Gradle task.
  Then `build/dist/package` directory contains unpacked extension.
- to build it to debug run `jsBrowserDevelopmentWebpack` Gradle task.
  Then `build/dist/js/developmentExecutable` directory contains unpacked extension
  with source code suitable for debugging.

## Comparison with the original

Extension is completely rewritten, these are changes:

- **F12** - Extension has its own dev tools panel
- It's always open instead of being initially collapsed, so you might get scrolling
  (I hate this bug with not fully expandable panels and no possibility even to scroll)
- No keys to activate. If *TwH* has detected the game, it will show panel, otherwise empty.
- It's easy to add support to other engines (Wikifier etc)

## TODO

- ~~support more HTML games and game engines~~
  (What a pity some of them requires patching, that's not this extension is intended to do **SO, CLOSED**)
- ~~removal and creation feature~~ (Complete. If something is buggy, close and open DevTools.)
- correct sorting when new variables are discovered

## Latest changes

- 23.05.2026 21:00 - v.3.0.5.0: Some UI improvements:
  - finally! Fixed annoying bug when two fields are switching focus (got me a long time to catch this)
  - ability to change UI language
  - altered some colors for better visual distinction
  - settings are persisted across versions, no separate options page, all changes are made through main page are persisted immediately
  - more languages supported (i'm using Google Translate, please native speaking people hint me if some translations are incorrect)
  - _TODO_ make more good-looking UI theme...
- 20.05.2026 21:00 - v.3.0.4: filter fix (thanks to @[flying-hippo](https://github.com/flying-hippo)). Changed some colors. Code refucktoring.
- 15.10.2024 01:00 - v.3.0.3.5: Chrome: it's usable again! Chrome and Firefox. thanks
- 14.10.2024 00:00 - v.3.0.3.4: it's usable again! Chrome and Firefox. thanks
  for [@hikaru-shindo](https://github.com/hikaru-shindo) for fix
- 14.03.2024 23:00 - v.3.0.3.3: (experimental and failed)
- 14.03.2024 23:00 - v.3.0.3.2: (experimental) Russian translation added
- 13.03.2024 23:00 - v.3.0.3.1: Some bugs fixed (Broken images, highlighting and filtering regression)
- 04.03.2024 15:00 - v.3.0.3: README and naming updated, added LICENSE, submitted to Google Store
- 29.02.2024 23:00 - new beta: auto update structure, support of Date & Map types
- 04.04.2023 22:00 - hanging issues fixed (less computations, less cycles)

### List of tested games

- ...
