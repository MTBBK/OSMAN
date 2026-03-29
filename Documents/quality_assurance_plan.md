# Quality Assurance Plan

# Table of Contents:

- [Quality Assurance Strategy](#quality-assurance-strategy)
- - [Testing Methodologies and Types](#testing-methodologies-and-types)
- [Quality Factors & Metrics](#quality-factors-and-metrics)
- [Task Matrix](#task-matrix)
- [Contributors](#contributors)

<a name="quality-assurance-strategy"></a>
## Quality Assurance Strategy:
Projemizin kalite yaklaşımını ve kalite faktörlerini belirlerken kullanıcıların yaptığımız araçtan azami verim almalarını hedefledik. Bu kapsamda hızdan önce doğruluğu amaçladık.,

<a name="testing-methodologies-and-types"></a>
### Testing Methodologies and Types:
- Unit Testing: Aracımız çalıştırıldığında yaptığı her önemli iterasyonu log dosyasına yazdırır. Daha sonra yazdığımız örnek doğru sonuç log dosyası ile oluşan çıktı karşılaştırılır ve bu şekilde istenen her fonksiyon ayrı ayrı test edilmiş olur. Otomatik olarak yapılır.
- Integration Testing: Aracımız çalıştırıldığında oluşturulan dosyaların doğruluğu test.java dosyası sayesinde kontrol edilir. Bu sayede projede yer alan `Builder`, `Strategy` ve `Factory" sınıf ve alt sınıflarının toplu testi gerçekleştirilmiş olur. Otomatik olarak yapılır.
EndUserLike Test: Aracın nihai çıktısının son kullanıcının gözünden görüyormuşçasına tüm temalar için değerlendirildiği test aşamasıdır. Manuel olarak yapılır. Ve tüm çıktılar teker teker incelenir.

<a name="quality-factors-and-metrics"></a>
## Quality Factors and Metrics:
| Quality Factor | Description   | Measurement Metric |
| ------ | ------- | ------ |
| Usability | System is easy to learn and use | User score from surveys over 75% |
| Reliability | System works without crashes or unexpected failures | No critical system crashes during test period |
| Compatibility | System works across different operating systems | Tests for Windows and Linux but no Mac support for arbitrary reasons |
| Usability | Ease of use for users | User satisfaction score from surveys |

<a name="test-plan"></a>
## Test Plan:

- 1: Kullanıcı, `config` dosyasını kullanarak oluşturmak istediği internet sitesi için bir tema tercihinde bulunabilecek. Adımlar: `config` dosyasına git -> İstenilen temanın adını yaz -> Programı çalıştır. Beklenen Sonuçlar: Temaya uygun internet sitesinin çıktı olarak gelmesi.
- 2: Kullanıcı, pratik bir şekilde elindeki metin veya resim dosyalarını sisteme yükleyebilecek ve sistemi çalıştırdığında beklediği sonuçları alabilecek. Adımlar: Dosya dizinine git -> `Images` veya `Texts` klasörüne git -> Dosyaları klasöre yükle -> Programı çalıştır. Beklenen Sonuçlar: Eklenen dosyaların program çıktısında uygun yerlerde bulunması.
- 3: Kullanıcı sisteme yüklediği metin dosyaları için ayrı ayrı oluşturulmuş sayfaları bir buton listesi hâlinde ana sayfada görüp butonlardan herhangi birine tıkladığı takdirde o sayfaya erişebilecek. Adımlar: Oluşturulan sitenin ana sayfasına gir -> Sayfa butonlarından birisine tıkla -> Sayfaya eriş. Beklenen Sonuçlar: Ana sayfadaki butonlar aracılığıyla alt sayfalara erişilebilmesi.
- 4: Sistem, işletim sisteminden bağımsız olarak JVM üzerinden çalışacak. Adımlar: Programı Çalıştır -> Çıktıyı elde et. Beklenen Sonuçlar: Programın sorunsuz çıktı vermesi.
- 5: Kullanıcının `config` dosyasında oluşturacağı internet sitesi için başlık ve açıklama belirleyebilmesi. Adımlar: Config dosyasına git -> İstenilen bilgileri yaz -> Programı çalıştır. Beklenen Sonuçlar: `config` dosyasındaki girdilere uygun internet sitesinin çıktı olarak gelmesi.

Hata takibi manuel olarak yapılacaktır.

<a name="task-matrix"></a>
## Task Matrix


| Görev      | Görevli                   |
| ------------------------------- | ------------------------------------ |
| Genel Kontrol                     | Mesut Barbaros Köş     |
| Test Metotları         | Melih Efe Tursun             |
| Metriklerin Belirlenmesi | Mesut Barbaros Köş                        |
| Hata Takibi                     | Mesut Barbaros Köş ve Melih Efe Tursun         |

<a name="contributors"></a>
## Contributors
- Mesut Barbaros Köş
- Melih Efe Tursun
- Mahmut Tosun
- Ahmet Taha Kılınç
