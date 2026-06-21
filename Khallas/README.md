# Khallas! 

Personal sideloaded Android app to bulk-delete media and documents.

## What it does

- **Open Gallery** tab — browse your folder tree, pick a folder, view its
  photos/videos full-screen (landscape) with **Previous / Next / Select / Delete**.
  Selecting a file auto-advances to the next one. Long-press Previous/Next for
  ~5x fast navigation.
- **Browse folders** tab — same folder tree, but lists *every* file (not just
  media). Tap a photo/video to open the same full-screen viewer; tap a checkbox
  on any file (including documents) to mark it, then hit **Delete Selected**.

Uses **All Files Access** (`MANAGE_EXTERNAL_STORAGE`) so deletes are instant —
no per-file confirmation popups, and it works on documents too, not just media.

## Project layout

```
app/src/main/java/com/parag/khallas/
  MainActivity.kt          - tabs + permission gate
  util/FileUtils.kt        - folder/file scanning helpers
  gallery/                 - "Open Gallery" tab
  browse/                  - "Browse folders" tab
  viewer/                  - full-screen Previous/Next/Select/Delete viewer
```

A fixed `app/debug.keystore` is committed so every CI build is signed with the
**same key** — you can install updated APKs over the old one without
uninstalling first.

## Building via GitHub Actions

1. Create a new GitHub repo (private is fine) and push this folder:
   ```bash
   cd Khallas
   git init
   git add .
   git commit -m "Khallas! v1"
   git branch -M main
   git remote add origin https://github.com/<your-username>/<repo-name>.git
   git push -u origin main
   ```
2. The workflow in `.github/workflows/build.yml` triggers automatically on
   push to `main` (or run it manually from the **Actions** tab → "Build
   Khallas APK" → **Run workflow**).
3. Once it finishes, open the workflow run → **Artifacts** → download
   `khallas-debug-apk` (a zip containing `app-debug.apk`).

## Installing on your phone

1. Unzip and transfer `app-debug.apk` to your phone (or download directly if
   you open the Actions artifact link from your phone's browser).
2. Allow "Install unknown apps" for whichever app you use to open the APK
   (Settings → Apps → Special access → Install unknown apps).
3. Install it. On first launch, tap **Grant Storage Access** → enable
   "Allow access to manage all files" on the system screen that opens.

## Iterating

Whenever you change the code, just `git push` again — Actions rebuilds
automatically and signs with the same debug key, so reinstalling is a clean
update, not a conflict.

## Notes / things you may want to adjust

- **Folders shown**: starts at `/storage/emulated/0` (internal shared
  storage). SD card support isn't included — say the word if you use one.
- **Fast nav speed**: currently repeats every 150ms after a 400ms hold;
  tweak `FAST_NAV_INTERVAL_MS` / `HOLD_THRESHOLD_MS` in
  `FullScreenViewerActivity.kt` to taste.
- **Video playback**: uses the built-in `VideoView` (tap to play/pause,
  loops automatically) to keep dependencies light. If you want scrubbing
  controls, ExoPlayer can be swapped in.
- **App icon**: currently uses a generic system icon placeholder so the
  build doesn't depend on custom image assets — easy to swap once you've
  test-installed it.
