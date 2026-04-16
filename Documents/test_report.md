# OSMAN Test Report

## 1. Test Responsibles (Test Sorumluları)
- **Mesut Barbaros Köş:** Genel Kontrol, Metriklerin Belirlenmesi, Hata Takibi
- **Melih Efe Tursun:** Test Metotları, Hata Takibi
*(Not: Katkıda bulunan diğer isimler Mahmut Tosun ve Ahmet Taha Kılınç olmakla birlikte, spesifik test sorumlulukları Task Matrix'te Mesut Barbaros Köş ve Melih Efe Tursun olarak atanmıştır.)*

## 2. Test Date (Test Tarihi)
- 15/04/2026

## 3. Test Configuration (Test Konfigürasyonu)
Proje, hızı değil doğruluğu önceleyen bir stratejiyle konfigüre edilmiştir. Test konfigürasyon detayları şunlardır:
- **Çalışma Ortamı:** JVM (Java Virtual Machine) üzerinden çalışacak şekilde ayarlanmıştır. Windows ve Linux işletim sistemleri desteklenmektedir (Mac desteği bilerek eklenmemiştir).
- **Manuel Konfigürasyonlar:** `config.osman` (ayarlar) dosyası, `Images` (resim) ve `Texts` (metin) klasörleri.

## 4. Test Inputs & Test Results (Test Girdileri ve Sonuçları)
Sağlanan metin bir "Plan" olduğu için, gerçek test sonuçları bulunmamaktadır. Sistem girdileri (Inputs) ve beklenen sonuçlar plan dahilinde aşağıya çıkarılmıştır:

| Test ID | Test Adı / Türü | Test Girdileri (Inputs) | Beklenen Sonuç (Expected Results) | Gerçekleşen Sonuç (Actual Results) |
|---|---|---|---|---|
| **Test 1** | Usability | Kullanıcının kendi Content'ini ekleyip Builder.jar'ı çalıştırması. | Gerekli dosyaların Output klasöründe sorunsuz bir şekilde oluşmuş olması. | Gerek dosyaların Output klasöründe sorunsuz bir şekilde oluşmuş olması. |
| **Test 2** | Reliability | Kullanıcının kendi Content'ini ekleyip Builder.jar'ı çalıştırması. | Herhangi bir hata olmadan, gerekli dosyaların Output klasöründe sorunsuz bir şekilde oluşmuş olması. | Herhangi bir hata olmadan, gerekli dosyaların Output klasöründe sorunsuz bir şekilde oluşmuş olması. |
| **Test 3** | Compatibility | Kullanıcının Linux'tan ve Windows'tan kendi Content'ini ekleyip Builder.jar'ı çalıştırması | Hem Windows'ta hem de Linux'ta, gerekli dosyaların Output klasöründe sorunsuz birebir aynı olacak şekilde oluşmuş olması. | Hem Windows'ta hem de Linux'ta, gerekli dosyaların Output klasöründe sorunsuz birebir aynı olacak şekilde oluşmuş olması. |
| **Test 4** | Consistency | Kullanıcının kendi Content'ini ekleyip Builder.jar'ı çalıştırması. | Programın sorunsuz bir şekilde log çıktısı vermesi. | Programın sorunsuz bir şekilde log çıktısı vermesi. |
| **Test 5** | Agility | Kullanıcının kendi 10000 tane dosyayı Content olarak verip Builder.jar'ı çalıştırması  |  Written process time in the log is below 1 second. | Written process time in the log is 0.907 second. |

*(Not: "EndUserLike Test" aşamasında elde edilecek UI/UX sonuçları da henüz manuel olarak test edilip raporlanmamıştır. Ayrıca Usability %75 anket başarı hedefi sonucuna da ulaşılamamaktadır.)*

## 5. Related Deployment Diagram (İlgili Dağıtım Diyagramı)
![Example Page](https://github.com/MTBBK/OSMAN/blob/main/Documents/UML/DeploymentDiagram.png?raw=true)

