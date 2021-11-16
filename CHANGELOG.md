# Changelog
All notable changes to this project are documented in this file.
The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0),

See the [changelog of the backend](https://github.com/software-challenge/backend/blob/main/CHANGELOG.md)
for details on our versioning scheme.

## [22.1.0](https://github.com/software-challenge/gui/commits/22.1.0) Fancying up - 2021-11
- Ensure compatibility beyond Java 16
- Animate figures
- Persist preferences

## [22.0.3](https://github.com/software-challenge/gui/commits/22.0.3) - 2021-07-26
- Fix annoying error when striking a figure in a human vs human match
- Improve navigation & status display
- Smoothen board interaction
- Expand logging for beta version

## [22.0.2](https://github.com/software-challenge/gui/commits/22.0.2) Interface Polishing - 2021-07-16
- Fix help links & add little usage guide
- Polish game interface
- Display amber count visually
- Make example client a little smarter

## [22.0.1](https://github.com/software-challenge/gui/commits/22.0.1) - 2021-06-25
- Proper Ostseeschach figures
- Highlight possible moves on hover
- Allow human players
- Fix issues with game controls

## [22.0.0](https://github.com/software-challenge/gui/commits/22.0.0) - 2021-06-11
- Major redesign of the layout
- New Game Ostseeschach with basic animations and placeholder graphics

## 2022 Game Ostseeschach - 2021-06-11

## [21.4.0](https://github.com/software-challenge/gui/commits/21.4.0) - 2021-01-29
- Automatically save replays & implement loading replays
- Utilise TornadoFX ResourceLookup & EventStreams more extensively
- Open help links properly on Linux-based systems

## [21.3.3](https://github.com/software-challenge/gui/commits/21.3.3) - 2021-02-26
- Fix human getting wrongly colored piece ([#64](https://github.com/software-challenge/gui/pull/64))
- Stability improvements in backend

## [21.3.0](https://github.com/software-challenge/gui/commits/21.3.0) - 2021-01-29
- Unify skip and pass button ([#63](https://github.com/software-challenge/gui/pull/63))
- Improve in-game status display ([#61](https://github.com/software-challenge/gui/pull/61))
- Remove accidental disabling of an entire menubar item before a game is started ([610d722e1f5a6056ebfcdf2ec4a56ed349fd5ba0](https://github.com/software-challenge/gui/commit/610d722e1f5a6056ebfcdf2ec4a56ed349fd5ba0))
- Improve some internal algorithms

## [21.2.1](https://github.com/software-challenge/gui/commits/21.2.1) - 2020-12-18
- Cancel an existing game when starting a new one
- Make in-game status display a little more concise

## [21.2.0](https://github.com/software-challenge/gui/commits/21.2.0) - 2020-12-14
- Implement support for manually started clients
- Add loading view when game is starting
- Show winner info on game end (#58)
- Highlight shapes that can currently be placed (#55)
- Improve internal verification & publishing mechanisms

## 21 - Game Blokus
Replaced [Electron GUI](https://github.com/software-challenge/gui-electron)
with new GUI based on [TornadoFX](https://github.com/edvin/tornadofx2).
