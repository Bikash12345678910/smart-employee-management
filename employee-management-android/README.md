# Smart Biometric - Android Native App / Kiosk APK Project

This folder contains the complete native Android project for the **Smart Employee Attendance & Biometric Management System**.

---

## 📱 Features

- **Native Camera & Face Biometrics**: WebChromeClient automatically grants high-speed camera access for facial recognition.
- **Native GPS Verification**: Automatically requests and passes precise office geofencing coordinates.
- **Continuous Kiosk Mode**: Configured with `FLAG_KEEP_SCREEN_ON` for tablet wall mounts and reception desks.
- **Dynamic Server Switcher**: If the server IP changes, an on-screen dialog allows entering the new IP without recompiling.
- **Unique App Icon**: Pre-configured with the official high-resolution glowing biometric shield icon.

---

## 🛠️ How to Build the APK (.apk)

### Option 1: Using Android Studio (Recommended)
1. Open **Android Studio**.
2. Select **Open** and choose this folder:
   `smart-employee-management/employee-management-android`
3. Wait for Gradle sync to complete.
4. Click **Build > Build Bundle(s) / APK(s) > Build APK(s)**.
5. Android Studio will generate the APK at:
   `app/build/outputs/apk/debug/app-debug.apk`
6. Transfer `app-debug.apk` to any Android phone or tablet and tap **Install**.

---

### Option 2: Using Command Line (Gradle)
```bash
cd employee-management-android
gradle assembleDebug
```
The output APK will be placed in:
`app/build/outputs/apk/debug/app-debug.apk`

---

## 🌐 Instant Android Installation Without Building (PWA / WebAPK)

You can also run and install the app on any Android device directly:
1. Ensure the Android device is connected to the same Wi-Fi as your computer.
2. Open **Google Chrome** on the Android device and visit:
   `http://192.168.1.2:3000`
3. Tap the **three dots menu (⋮)** in Chrome and select **"Install App"** (or **"Add to Home Screen"**).
4. Android will automatically install the app with the **unique app icon**, running in fullscreen standalone mode.
