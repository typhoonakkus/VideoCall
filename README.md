# Android TV VideoCall MVP (Final)

WebRTC tabanlı Android TV uygulaması — STUN (Google), offer/answer ve ICE candidate akışı hazır.
Signaling: Firebase Realtime Database.

## Gerekli ortam
- Android Studio Flamingo+
- JDK 11+
- Android TV cihazı veya emulator (API 24+)
- Firebase projesi (Realtime Database etkin)

## Kurulum
1) Projeyi Android Studio ile aç.
2) `app/` içine `google-services.json` yerleştir (bu final ZIP'te ekli).
3) Gradle senkronizasyonunu bekle.

## Derleme (APK)
- Menü: **Build → Build Bundle(s) / APK(s) → Build APK(s)**
- Derleme bittiğinde sağ altta **Locate** bağlantısıyla APK yolunu açabilirsin.

## Test Adımları
1) İki cihazda uygulamayı aç (veya cihaz + emulator).
2) **Contacts** ekranında her cihazda **Your ID** görünecek (8 karakter).
3) Cihaz A, Cihaz B'nin ID'sini girip **Call**'a basar.
4) Cihaz B'de **Accept** çıkar; basınca görüşme başlar.
5) Firebase Console → Realtime Database → `signaling` altında mesajları görebilirsin.

## Güvenlik Notu
- Test modundaki DB kurallarını prod'a geçerken kısıtla.
- TURN yoktur; STUN ile çoğu ağda çalışır, bazı NAT senaryolarında TURN gerekebilir.
