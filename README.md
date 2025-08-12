# Android TV VideoCall MVP

## What is included
- Kotlin Android TV app skeleton
- WebRTC integration (google-webrtc)
- Firebase Realtime Database based signaling
- D-pad friendly simple UI

## Requirements
- Android Studio Flamingo or newer
- JDK 11+
- An Android TV device or emulator (API 24+)
- A Firebase project with Realtime Database enabled

## Firebase setup
1. Create a Firebase project at https://console.firebase.google.com
2. Add an Android app and register package name `com.example.tvvideocall`
3. Download `google-services.json` and place it in the `app/` folder
4. In Firebase Console -> Realtime Database -> Create database in test mode (or set rules to allow reads/writes for initial testing). Example rules for dev (NOT for production):

```
{
  "rules": {
    ".read": true,
    ".write": true
  }
}
```

## Build
1. Open the project in Android Studio
2. Let Gradle sync and download dependencies
3. Run the app on Android TV device or emulator

## Notes & Next steps
- The WebRtcClient provided is a minimal skeleton. You should implement offer/answer creation and SDP exchange flows (comments left in methods).
- Camera support on Android TV depends on hardware — some TV devices may not have a front camera. For TV boxes with USB webcam, Camera2Enumerator may work.
- For production, secure your Firebase DB rules and consider a dedicated signaling server for scale.


## Testing instructions (offer/answer + STUN)
1. Install the APK or run the app from Android Studio on two devices (or emulator + device).
2. On each device open the app, go to Contacts screen — you'll see 'Your ID'.
3. On Device A copy Device B's ID and enter it into 'Enter peer ID to call' then press Call.
4. On Device B you'll see an 'Accept' button — press it to answer.
5. Video should start. If not, check Firebase Realtime Database -> 'signaling' node to see exchanged messages.
6. After ending the call, signaling nodes are cleaned between the two peers automatically on activity destroy.
