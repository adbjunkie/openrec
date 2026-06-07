# OpenRec

A minimal, open-source Android background video recorder.

No analytics. No ads. No network calls. No third-party SDKs. Videos stay on your device.

## Features

- Background video recording (foreground service + CameraX)
- Front or back camera, optional audio
- Local MP4 storage with share/delete
- Discrete launcher icons (Calculator, Notes, Weather, Clock)
- Minimal notification mode (as discreet as Android allows)
- In-app privacy summary

## Privacy

See [docs/PRIVACY.md](docs/PRIVACY.md).

OpenRec only uses permissions required for recording. It does not access the network.

Recordings are stored at:

```
Android/data/com.openrec.recorder/files/recordings/
```

## Build

Requirements: Android Studio, JDK 17+, Android SDK 35.

```bash
git clone REPO_URL
cd openrec
./gradlew assembleDebug
```

APK output:

```
app/build/outputs/apk/debug/app-debug.apk
```

Release build (used by F-Droid):

```bash
./gradlew assembleRelease
```

## F-Droid submission

### 1. Publish source code

Push this repo to GitHub or GitLab. F-Droid builds from source — do not submit APKs directly.

### 2. Tag a release

```bash
git tag 1.0.0
git push origin 1.0.0
```

Update `versionCode` / `versionName` in `app/build.gradle.kts` for each release.

### 3. Add screenshots

Capture 2–3 phone screenshots and place them in:

```
fastlane/metadata/android/en-US/images/phoneScreenshots/
```

### 4. Submit to fdroiddata

1. Fork https://gitlab.com/fdroid/fdroiddata
2. Copy `metadata/fdroid/com.openrec.recorder.yml` into `metadata/com.openrec.recorder.yml`
3. Replace every `REPO_URL` with your real repository URL
4. Open a merge request

Store listing text is already in `fastlane/metadata/android/en-US/`.

### 5. What reviewers check

- All dependencies are free software (AndroidX / CameraX only)
- No tracking or network permissions
- Privacy policy is documented (`docs/PRIVACY.md` + in-app About screen)
- App builds with `./gradlew assembleRelease`
- Target SDK meets current F-Droid policy (35)

### 6. After acceptance

F-Droid signs builds with their own key. Users install from the F-Droid client, not your APK.

## Legal note

You are responsible for complying with local recording consent laws. Only record where you have the legal right to do so.

## Support

If you find OpenRec useful, you can [buy me a coffee](https://buymeacoffee.com/theaiinstructor).

## License

MIT — see [LICENSE](LICENSE)