<p align="center">
  <img src="screenshots/banner.png" alt="StreamFlix Banner" width="100%"/>
</p>

<h1 align="center">StreamFlix</h1>
<h3 align="center">Stream Everything, Free Forever</h3>

<p align="center">
  <img src="https://img.shields.io/badge/Platform-Android-green?style=for-the-badge&logo=android" />
  <img src="https://img.shields.io/badge/API-24%2B-blue?style=for-the-badge" />
  <img src="https://img.shields.io/badge/Kotlin-1.9.22-purple?style=for-the-badge&logo=kotlin" />
  <img src="https://img.shields.io/badge/License-MIT-orange?style=for-the-badge" />
  <img src="https://img.shields.io/github/stars/Anikesh0001/Streaming_App?style=for-the-badge" />
</p>

<p align="center">
  <b>A powerful, free, open-source IPTV player for Android</b><br/>
  Watch 80+ built-in channels including News, Sports, Entertainment, Movies, Music & more — or add your own M3U/M3U8 playlists for unlimited content.
</p>

---

## Screenshots

<p align="center">
  <img src="screenshots/home.png" width="24%" />
  <img src="screenshots/player.png" width="24%" />
  <img src="screenshots/categories.png" width="24%" />
  <img src="screenshots/settings.png" width="24%" />
</p>

## Features

### Player Engine
- **ExoPlayer/Media3** — Industry-leading adaptive streaming engine
- **HLS, DASH, SmoothStreaming, RTSP, Progressive** — All major protocols supported
- **Audio Fix** — Proper AudioAttributes with audio focus management
- **Auto Retry** — 3 automatic retries with exponential backoff on errors
- **Adaptive Quality** — Auto quality selection (1080p/720p/480p/360p)
- **Large Buffers** — 15s-50s buffer for smooth live stream playback
- **Smart User-Agent** — Chrome UA headers for maximum compatibility

### Content
- **80+ Built-in Channels** — Ready to watch out of the box:
  - **Sports (26 channels)** — Cricket Gold, Star Sports, Sony Ten, ESPN, Willow, WWE, beIN, Eurosport, Tennis Channel
  - **News (18 channels)** — Aaj Tak HD, NDTV, India Today, ABP News, India TV, Manorama
  - **Entertainment (12 channels)** — Dangal TV, ETV Telugu/Cinema, Colors Kannada/Marathi/Tamil
  - **Movies (7 channels)** — Goldmines (4 variants), B4U Movies/Kadak/Bhojpuri
  - **Music (8 channels)** — 9XM, 9X Jalwa/Jhakaas/Tashan, B4U Music, Mastiii
  - **Government (8 channels)** — DD National HD, DD News, DD Sports, DD India, DD Bharati
  - **Kids (2 channels)** — Cartoon Network, Cartoon Network HD+
- **Load More** — Add India (780+ channels), All Countries (9000+ channels), or any M3U URL
- **iptv-org Integration** — Powered by the world's largest open-source IPTV playlist

### UI/UX
- **Material Design 3** — Modern, premium look with electric blue/teal gradient theme
- **Dark Mode** — Full AMOLED dark theme support
- **Search** — Real-time search across all channels
- **Category Chips** — Quick filter by Sports, News, Entertainment, Movies, Music, Kids, Government
- **Watch History** — Track recently watched channels with watch count
- **Favorites** — Save your favorite channels for quick access
- **Swipe to Refresh** — Pull down to reload all playlists
- **Channel Count** — See total channels loaded in the header

### Advanced
- **Picture-in-Picture (PiP)** — Watch while using other apps (Android 8+)
- **Floating Player** — Overlay player with drag support
- **Fullscreen Mode** — Immersive landscape viewing
- **Share** — Share channels with friends
- **Custom Playlists** — Add unlimited M3U/M3U8 playlist URLs
- **Persistent Favorites** — Room database for reliable storage

## Tech Stack

| Component | Technology |
|-----------|-----------|
| Language | Kotlin 1.9.22 |
| Player | Media3 ExoPlayer 1.2.1 |
| Architecture | MVVM + Repository Pattern |
| Database | Room 2.6.1 |
| Preferences | DataStore |
| Image Loading | Coil 2.5.0 |
| Concurrency | Kotlin Coroutines 1.7.3 |
| UI | Material Design 3 + ConstraintLayout |
| Min SDK | API 24 (Android 7.0) |
| Target SDK | API 34 (Android 14) |
| Build | Gradle 8.5 + AGP 8.2.2 |

## Installation

### Download APK
1. Go to [Releases](https://github.com/Anikesh0001/Streaming_App/releases)
2. Download the latest `StreamFlix-v2.0-debug.apk`
3. Enable "Install from Unknown Sources" in your Android settings
4. Install and enjoy!

### Build from Source
```bash
git clone https://github.com/Anikesh0001/Streaming_App.git
cd Streaming_App
./gradlew assembleDebug
# APK will be at app/build/outputs/apk/debug/app-debug.apk
```

## Project Structure

```
app/src/main/
├── assets/
│   └── default_playlist.m3u          # 80+ bundled channels
├── java/com/livestreamplayer/app/
│   ├── App.kt                        # Application class
│   ├── data/
│   │   ├── AppDatabase.kt            # Room database
│   │   ├── FavoriteDao.kt            # Favorites DAO
│   │   ├── WatchHistoryDao.kt        # Watch history DAO
│   │   ├── M3UParser.kt              # M3U/M3U8 playlist parser
│   │   ├── PlaylistRepository.kt     # Playlist loading & caching
│   │   └── PrefsManager.kt           # DataStore preferences
│   ├── model/
│   │   └── Channel.kt                # Data models
│   ├── service/
│   │   └── FloatingPlayerService.kt  # Floating overlay player
│   └── ui/
│       ├── MainActivity.kt           # Bottom nav controller
│       ├── SplashActivity.kt         # Splash screen
│       ├── PlayerActivity.kt         # Video player (ExoPlayer)
│       ├── HomeFragment.kt           # Channel list + search
│       ├── CategoriesFragment.kt     # Category grid
│       ├── FavoritesFragment.kt      # Saved channels
│       ├── HistoryFragment.kt        # Watch history
│       ├── SettingsFragment.kt       # App settings
│       ├── ChannelAdapter.kt         # Channel list adapter
│       └── CategoryAdapter.kt        # Category grid adapter
└── res/
    ├── drawable/                      # 20+ vector icons & shapes
    ├── layout/                        # 13 layout XMLs
    ├── menu/                          # Bottom navigation
    ├── mipmap-*/                      # Adaptive app icons
    └── values/                        # Themes, colors, strings, dimens
```

## Adding Your Own Playlists

1. Open the app
2. Tap the **+** button (bottom right)
3. Choose:
   - **Add from URL** — Paste any M3U/M3U8 URL
   - **Load India** — 780+ Indian channels from iptv-org
   - **Load All Countries** — 9000+ channels worldwide

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

1. Fork the project
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## Roadmap

- [ ] EPG (Electronic Program Guide) with timeline view
- [ ] Catch-up / Timeshift for supported streams
- [ ] Chromecast / DLNA casting support
- [ ] Channel recording to local storage
- [ ] Parental controls with PIN
- [ ] Auto-update playlists in background
- [ ] Multi-language subtitle support
- [ ] Android TV / Leanback support
- [ ] Dual engine (ExoPlayer + FFmpeg fallback)

## Legal Notice

StreamFlix is an open-source IPTV player. It does not host, provide, or distribute any streaming content. All built-in channels are sourced from [iptv-org](https://github.com/iptv-org/iptv), a community-maintained collection of publicly available free-to-air IPTV channels.

Users are responsible for the content they add through custom playlists. We do not endorse, promote, or condone piracy or the use of this application to access unauthorized content.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Author

**Anikesh** — [@Anikesh0001](https://github.com/Anikesh0001)

---

<p align="center">
  If you found this useful, please consider giving it a ⭐<br/>
  <b>StreamFlix — Stream Everything, Free Forever</b>
</p>
