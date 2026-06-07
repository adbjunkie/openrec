# OpenRec

A minimal, open-source Android background video recorder.

No analytics. No ads. No network calls. No third-party SDKs. Videos stay on your device.

## Features

- Background video recording
- Front or back camera, optional audio
- Local MP4 storage with share/delete
- Optional discrete launcher icons
- Minimal notification mode
- In-app privacy summary

## Privacy

See [docs/PRIVACY.md](docs/PRIVACY.md).

Recordings are saved locally at:

```
Android/data/com.openrec.recorder/files/recordings/
```

## Build

Requirements: Android Studio, JDK 17+, Android SDK 35.

```bash
git clone https://github.com/adbjunkie/openrec.git
cd openrec
./gradlew assembleDebug
```

## Legal

You are responsible for complying with local recording consent laws.

## Support

If you find OpenRec useful, you can [buy me a coffee](https://buymeacoffee.com/theaiinstructor).

## License

MIT — see [LICENSE](LICENSE)