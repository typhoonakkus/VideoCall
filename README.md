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


******************************************************************
📋 Test Senaryosu — Android TV Video Call Uygulaması
1️⃣ Ortam Hazırlığı
2 cihaz hazırla:

Android TV veya Android TV Box (Uygulama yüklü)

İkinci bir Android TV, tablet veya telefon (Uygulama yüklü)

İki cihazın da internete bağlı olduğundan emin ol.

Firebase Realtime Database → Rules’ta test için yazma/okuma iznini açık tut.

2️⃣ Uygulama Açılışı
Her iki cihazda uygulamayı başlat.

Ana ekranda Your ID alanının dolu geldiğini kontrol et.

ID’nin her cihazda farklı olması gerekir.

Firebase Console → Realtime Database ekranında signaling alanına göz at: Başlangıçta boş olmalı.

3️⃣ Çağrı Başlatma (Lokal Ağ Testi)
Cihaz A’da → Remote ID alanına Cihaz B’nin Your ID’si yaz.

Call butonuna bas.

Cihaz B’de → Accept butonu çıkmalı, tıkla.

Her iki ekranda da görüntü ve ses akışı başlamalı.

📌 Kontroller:

Her iki cihazda da video akışı düzgün mü?

Ses net mi?

Firebase’de offer/answer/iceCandidate kayıtları oluşuyor mu?

4️⃣ Çağrı Sonlandırma
Her iki cihazda da Hangup butonuna bas.

Görüntü ve ses tamamen kesilmeli.

Firebase’de ilgili signaling verisi otomatik temizlenmeli.

5️⃣ Farklı Ağ Testi (STUN Kontrolü)
Cihaz B’yi farklı bir internet ağına geçir (ör. telefon hotspot).

Tekrar 3. adımdaki çağrı senaryosunu uygula.

Bağlantı kuruluyorsa STUN sunucusu (Google STUN) düzgün çalışıyor demektir.

6️⃣ Hata Durumu Testleri
Yanlış Remote ID girip çağrı başlat: Bağlantı kurulmamalı.

Accept’e basmadan bekle → Çağrı belirli süre sonra zaman aşımına uğramalı (uygulama ayarına göre).

Bir cihazda uygulamayı kapatınca diğer cihazda call ended gibi durum oluşmalı.

💡 İstersen bu testlerin ekran görüntülerini ve Firebase loglarını birlikte inceleyip sistemin stabil çalışıp çalışmadığını da doğrulayabiliriz.