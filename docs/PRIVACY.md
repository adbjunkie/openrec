# OpenRec Privacy Policy

Last updated: 2026-06-08

OpenRec is a local-only background video recorder. This policy explains what the app accesses and what it does with that data.

## Summary

- No accounts
- No analytics or tracking
- No network access
- No cloud upload
- Recordings stay on your device

## Data collected

OpenRec does **not** collect, transmit, or sell personal data.

## Data stored on your device

When you record, OpenRec saves MP4 files to app-private storage:

```
Android/data/com.openrec.recorder/files/recordings/
```

You can share or delete recordings from inside the app. Uninstalling the app removes app-private data.

## Permissions

| Permission | Why |
|------------|-----|
| Camera | Record video |
| Microphone | Optional audio recording (can be disabled in settings) |
| Notifications | Required by Android while recording in the background |
| Foreground service (camera) | Keep recording active when the app is in the background |
| Wake lock | Prevent interrupted recordings on some devices |
| Request ignore battery optimizations | Optional prompt so background recording is not killed by the OS |

OpenRec only uses permissions needed for recording. The app does not access contacts, location, SMS, or call logs.

## Stealth features

OpenRec lets you:

- Change the launcher icon and label (e.g. Calculator, Notes)
- Use a minimal recording notification

These features change how the app appears on your device. They do not send data anywhere.

## Third parties

OpenRec has no third-party SDKs, ads, or crash reporters.

## Your responsibility

Recording laws vary by country and situation. You are responsible for recording only where you have the legal right to do so.

## Open source

Source code is available under the MIT license. You can inspect exactly what the app does.

## Contact

Report issues in the project's public issue tracker (GitHub/GitLab) listed in the app repository.