# BonkMobile

Personal Android fullscreen WebView wrapper for https://bonk.io/ with experimental on-screen touch controls.

## Build APK online with GitHub Actions

This repository includes `.github/workflows/build-apk.yml`. After uploading these project files to a GitHub repository on the `main` branch, GitHub Actions builds a debug APK and uploads it as the `BonkMobile-debug-apk` workflow artifact.

To run manually: open the repository's **Actions** tab, choose **Build BonkMobile APK**, select **Run workflow**, and wait for the build to finish. Download the `BonkMobile-debug-apk` artifact from the completed run and extract the APK inside.

## Notes

- This is an experimental wrapper, not an official Bonk.io app or a native/offline copy of the game.
- The game still loads from Bonk.io servers and requires internet access.
- Touch controls are experimental and need testing in a private room.
