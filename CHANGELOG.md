# Changelog
All notable changes to this project are documented in this file.
The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0),

See the [changelog of the backend](https://github.com/software-challenge/backend/blob/main/CHANGELOG.md)
for details on our versioning scheme.

## 25.0.3 UI Polish - 2024-06-25
- label fields
- show carrot costs and gains with icon
- improve card move buttons
- improve spacing
- make example player a little smarter
- animate moves

## 25.0.2 Adjustments - 2024-06-19
- small UX improvements
- display owned cards
- update background and light/dark mode styling
- update window title

## 25.0.1 Prototype - 2024-06-15

## 2025 Game Hase und Igel

### 24.2.4 Game generation and ending corrections - 2024-03-21

### 24.2.2 Detailed winner explanation and stuck ship highlighting - 2024-03-14

### 24.2.1 Backend: Fix incorrect winners - 2024-04-13

### 24.2.0 - 2024-03-12
- Standardised game result messages from backend
- Better gameplay button focus
- Better handling of game speed

### 24.1.6 - 2024-02
- Fix missing pixels between current
- Expanded Interface Indicators

### 24.1.5 Proper Guidance - 2023-10-03
- Add tooltips to all buttons
- Fix player statistics (used to display current player in both)
- Fix regression: Turn skip buttons advancing by 2

### 24.1.4 Guidance - 2023-10-02
- Update instructions and merge into game creation screen

### 24.1.3 Edge Infos - 2023-09-21
- Highlight available Push target fields
- Add player stats to top screen corners
- Bump JavaFX to 17.0.8 to fix crash on macOS

### 24.1.2 Graphics Fixes - 2023-09-20
- Display goal flag also on field with current

### 24.1.1 Improve Board Layouting - 2023-09-15
- Properly cut out current section

### 24.1.0 Interface Accessibility - 2023-09-11
- Full Mouse Control
- Modern Graphics and graphical indicators
- Improve Keyboard Usage
- Further improve Human Move validation
- Reduce font size

### 24.0.7 Enhanced Visual Feedback - 2023-08-29
- Eliminate lots of edge cases with human moves
- Show current ship attributes
- Show more details on game over
- More Sizing Fixes

### 24.0.6 Human Keyboard Moves - 2023-08-24
- Fix Board width issue on long straight as well as heavily bent boards
- Enable Human Moves via Keyboard:
  + W: Advance
  + A/D: Turn
  + 0-5: Push in Direction (0 is RIGHT, then clockwise)
  + Acceleration is handled automatically
  + Confirm Move with S, Cancel with C

### 24.0.5 Simple Viewer - 2023-08-23
- Can view computer players playing (no human moves yet)
- Preliminary Graphics

## 2024 Game Mississippi Queen

### [23.0.2](https://github.com/software-challenge/backend/commits/23.0.2) Pretty Penguins - 2022-08-21
- Overhaul of the game display

### [23.0.1](https://github.com/software-challenge/backend/commits/23.0.1) Rough Penguins - 2022-08-06
- add new graphics and animations for Penguins
- handle modifier keys (SHIFT/CTRL) when jumping turns (ebf5436)
#### Minor Improvements
- use Raleway Font (#83)
- don't crash when loading an erroneous replay (13e9a28)
- rotate board depending on startTeam parameter
#### Under the hood
- use OpenJFX 17 (fafae89)
- remove kiosk mode stub
- add debug startup option for use with ScenicView

## 2023 Game Hey, Danke für den Fisch (Penguins) - 2022-08

### [22.1.0](https://github.com/software-challenge/gui/commits/22.1.0) Fancying up - 2021-11
- Ensure compatibility beyond Java 16
- Animate figures
- Persist preferences

### [22.0.3](https://github.com/software-challenge/gui/commits/22.0.3) - 2021-07-26
- Fix annoying error when striking a figure in a human vs human match
- Improve navigation & status display
- Smoothen board interaction
- Expand logging for beta version

### [22.0.2](https://github.com/software-challenge/gui/commits/22.0.2) Interface Polishing - 2021-07-16
- Fix help links & add little usage guide
- Polish game interface
- Display amber count visually
- Make example client a little smarter

### [22.0.1](https://github.com/software-challenge/gui/commits/22.0.1) - 2021-06-25
- Proper Ostseeschach figures
- Highlight possible moves on hover
- Allow human players
- Fix issues with game controls

### [22.0.0](https://github.com/software-challenge/gui/commits/22.0.0) - 2021-06-11
- Major redesign of the layout
- New Game Ostseeschach with basic animations and placeholder graphics

## 2022 Game Ostseeschach - 2021-06-11

### [21.4.0](https://github.com/software-challenge/gui/commits/21.4.0) - 2021-01-29
- Automatically save replays & implement loading replays
- Utilise TornadoFX ResourceLookup & EventStreams more extensively
- Open help links properly on Linux-based systems

### [21.3.3](https://github.com/software-challenge/gui/commits/21.3.3) - 2021-02-26
- Fix human getting wrongly colored piece ([#64](https://github.com/software-challenge/gui/pull/64))
- Stability improvements in backend

### [21.3.0](https://github.com/software-challenge/gui/commits/21.3.0) - 2021-01-29
- Unify skip and pass button ([#63](https://github.com/software-challenge/gui/pull/63))
- Improve in-game status display ([#61](https://github.com/software-challenge/gui/pull/61))
- Remove accidental disabling of an entire menubar item before a game is started ([610d722e1f5a6056ebfcdf2ec4a56ed349fd5ba0](https://github.com/software-challenge/gui/commit/610d722e1f5a6056ebfcdf2ec4a56ed349fd5ba0))
- Improve some internal algorithms

### [21.2.1](https://github.com/software-challenge/gui/commits/21.2.1) - 2020-12-18
- Cancel an existing game when starting a new one
- Make in-game status display a little more concise

### [21.2.0](https://github.com/software-challenge/gui/commits/21.2.0) - 2020-12-14
- Implement support for manually started clients
- Add loading view when game is starting
- Show winner info on game end (#58)
- Highlight shapes that can currently be placed (#55)
- Improve internal verification & publishing mechanisms

## 2021 - Game Blokus
Replaced [Electron GUI](https://github.com/software-challenge/gui-electron)
with new GUI based on [TornadoFX](https://github.com/edvin/tornadofx2).
