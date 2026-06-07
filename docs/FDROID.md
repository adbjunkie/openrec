# F-Droid submission notes

Internal notes for submitting OpenRec to F-Droid.

## 1. Tag a release

```bash
git tag 1.0.0
git push origin 1.0.0
```

Update `versionCode` / `versionName` in `app/build.gradle.kts` for each release.

## 2. Submit to fdroiddata

1. Fork https://gitlab.com/fdroid/fdroiddata
2. Copy `metadata/fdroid/com.openrec.recorder.yml` to `metadata/com.openrec.recorder.yml`
3. Replace `REPO_URL` with `https://github.com/adbjunkie/openrec`
4. Open a merge request

## 3. Useful links

- Privacy policy: https://github.com/adbjunkie/openrec/blob/main/docs/PRIVACY.md
- Store listing text: `fastlane/metadata/android/en-US/`
- Screenshots: `fastlane/metadata/android/en-US/images/phoneScreenshots/`

## 4. Verify build

```bash
./gradlew assembleRelease
```

F-Droid builds from source and signs the APK themselves.