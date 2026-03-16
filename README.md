# DreamTeam - Soccer Manager Arcade

DreamTeam is a high-energy soccer management application for Android that turns the player-signing process into an interactive arcade experience. Instead of static menus, users engage with a physics-based roulette system to build their elite squad. The app combines real-world player data with strategic turn-based match duels, offering a complete loop of collection, management, and gameplay.

## Figma Design
[Link to Figma Project](https://www.figma.com/design/Qwg556YSTes2NXzZ9rZSb2/DreamTeam_UIDraft?node-id=0-1&t=wccJeB1fRGulvpKU-1)

## Features & Technical Implementation

### Android & Jetpack Compose Features
*   **Declarative UI:** Built entirely with **Jetpack Compose**, utilizing a single-activity architecture with state-hoisted navigation.
*   **Animations:** Physics-based spinning roulette implemented using the **Compose Animatable API** and `spring` specs.
*   **State Management:** Reactive UI updates using `mutableStateOf`, `remember`, and `LaunchedEffect` for complex state transitions.
*   **Responsive Layouts:** Dynamic UI adjustment for **Portrait and Landscape** orientations using `LocalConfiguration`.
*   **Persistence:** 
    *   **Room Database:** Used for local storage of the player collection and squad configurations.
    *   **SharedPreferences:** Managed via a `UserStorage` wrapper for player profiles and currency.
*   **Concurrency:** Heavy use of **Kotlin Coroutines** for non-blocking network calls, database operations, and animation timing.
*   **Audio:** Custom `SoundManager` utilizing **SoundPool** for low-latency SFX and **MediaPlayer** for background music.

### 3rd Party Libraries
*   **Retrofit & GSON:** For fetching and parsing real-world soccer player metadata from TheSportsDB API.
*   **Coil:** For asynchronous image loading and memory caching of player cutouts.
*   **GSON:** Also used for initial database seeding from local assets.

## Dependencies & Requirements
*   **Android SDK:** Minimum SDK 26 (Android 8.0), Target SDK 34.
*   **Device Features:** Works on both physical devices and emulators. Supports Landscape orientation primarily for the best gameplay experience.
*   **Network:** Required for fetching player images and initial metadata search.

## Above and Beyond
*   **Physics-Based UI:** The roulette isn't just a random generator, it’s a simulated wheel where stopping distance is calculated based on entry velocity.
*   **Dynamic Visual Effects:** Implemented a "Zap" overlay system that triggers visual and audio feedback based on the statistical rarity of the player drawn.
*   **Turn-Based AI:** The "Match Play" mode features a greedy AI opponent that strategically plays cards based on the specific match moment (Pace, Shooting, etc.).
*   **Immersive UX:** Complete immersive mode implementation hiding system bars to provide a full screen arcade feel.
