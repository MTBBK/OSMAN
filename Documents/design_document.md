# Design Document

# Table of Contents:

- [System Overview](#system-overview)
- - [Brief Project Description](#brief-project-description)
- - [System Architecture](#system-architecture)
- - [Technology Stack](#technology-stack)
- [Implementation Details](#implementation-details)
- - [Codebase Structure](#codebase-structure)
- - [Key Implementations](#key-implementations)
- - [Component Interfaces](#component-interfaces)
- - [Visual Interfaces](#visual-interfaces)
- [Use Case Support Design](#use-case-support-design)
- - [Use Case Selection](#use-case-selection)
- - [Requirement Mapping](#requirement-mapping)
- - [Use Case Design](#use-case-design)
- [Design Decisions](#design-decisions)
- - [Technology Comparisons](#technology-comparisons)
- - [Decision Justifications](#decision-justifications)
- [Task Matrix](#task-matrix)
- [Contributors](#contributors)

<a name="system-overview"></a>
# System Overview

<a name="brief-project-description"></a>
## Brief Project Description:

OSMAN; Java ile yazılmış, hız için optimize edilmiş ve esneklik için tasarlanmış bir statik internet sitesi oluşturucudur. Geniş tema seçenekleri ve kullanıcıya uyum sağlayan özellikleriyle internet sitesi oluşturmayı kolaylaştırarak birkaç tık öteye indiren gelişmiş bir araçtır. Kullanıcılar saniyeler içinde gerek ellerindeki yazılar ile gerekse de ellerinde bulunan resimler ile statik bir internet sitesi oluşturabilmektedir.

<a name="system-architecture"></a>
## System Architecture:

Projemiz monolitik mimari üslubunda “self-contained” bir yapıda “single unit” olarak “deploy” edilerek geliştirilmiştir.
Builder.java'da Factory ve Strategy tasarım örüntüleri kodun okunabilirliğini artırmak için kullanılmıştır.
UML diyagramları tasarım örüntüleri eklenecek şekilde güncellenmiştir.

<a name="technology-stack"></a>
## Technology Stack:

- Frontend: HTML, CSS, JavaScript
- Backend: Java

<a name="implementation-details"></a>
# Implementation Details

<a name="codebase-structure"></a>
## Codebase Structure:

```
├── config.toml
├── configGenerator.html
├── Builder.java
├── Content/
│   ├── _index.md
│   ├── first.md
│   └── second.md
├── Output/
├── Templates/
│   ├── base.html
│   ├── page.html
│   ├── index.html
└── Themes/
└── ErrorLogs/
```

<a name="key-implementations"></a>
## Key Implementations:

- config.toml: Ana konfigürasyon dosyası, kullanıcıların oluşturmak istediklerini internet sitesinin; başlık, alt-başlık, tema gibi özelliklerini ayarlaması için yerleştirilmiş bir dosyadır.
- configBuilder.html: Config dosyasını oluşturmayı kolaylaştıran bir web arayüz.
- Builder.java: Kullanıcıların config.toml dosyasından gerekli ayarlamaları yaptıktan ve sayfa oluşturmak için eklemek istedikleri belgeleri yükledikten sonra çalıştıracakları ve siteyi oluşturacak olan ana modüldür.

<a name="component-interfaces"></a>
## Component Interfaces:

### Builder.java
##### Builder Sınıfı
- **private static void setStrategy(String option)**: Factory sınıfının decideStrategy metodunu kullanarak option'a göre stratejiyi belirler. Herhangi bir hata olması durumunda hatayı "throw"lar.
- **private static void performStrategy(StringBuilder file, String config)**: Belirlenmiş stratejinin makeChanges metodunu file ve config'i argüman olarak vererek çağırır. Herhangi bir hata olması durumunda hatayı "throw"lar.
- **static void writeFile(String filePath, String fileContent)**: Output klasöründe bir filePath'e göre bir dosya oluşturan ve içine fileContent'in içeriğini yazan metod. Dosyayı yazamazsa IOException "throw"lar.
- **static String readFile(String filePath)**: filePath ile verilen dosya yolundaki dosyayı okuyan ve içeriğini String olarak dönen metod. Dosyayı okuyamazsa IOException "throw"lar.
- **static String[][] parseContentFiles(String folderPath)**: folderPath'te olan klasörü readFile kullanarak analiz eden ve dosyaların içeriklerini isimleriyle birlikte iki boyutlu bir diziye koyup diziyi dönen metod. readFile'dan hata gelirse gelen hatayı "throw"lar.
- **static void stringEditor(String contentName, String newContent, StringBuilder file)**: Verilen file'ın içinde contentName'in geçtiği yeri bulup, onu silip, yerine newContent'te gelen veriyi koyan metod. Herhangi bir hata olması durumunda hatayı "throw"lar.
- **static StringBuilder makeFile(String file, String config)**: file ile aldığı veriyi config'in içeriğine göre Factory ile gerekli stratejiyi seçip düzenleyen ve yeni sonucu StringBuilder olarak dönen metod. Herhangi bir hata olması durumunda hatayı "throw"lar.
- **static void buildSite()**: "config.toml" dosyasını readFile ile okuyup içeriğine göre "Content", "Templates", "Themes" klasörlerindeki kullanılacak verileri; parseContentFiles ile alıp, makeFile ile düzenleyip, writeFile ile "Output" klasörüne siteyi hazırlayan metod. Çağırdığı metodlardan birinde hata olursa hatayı "throw"lar.
- **main()**: "buildSite()" metodunu çağırarak işlemi başlatan ana metod. Metodlarda oluşabilecek hataların çıktısını ErrorLogs klasöründeki log.txt dosyasına yazar.

##### Strategy Abstract Sınıfı
- **String option**: Kullanılacak stratejinin değiştireceği değişkeni tutan değişken.
- **abstract void makeChanges(StringBuilder file, String config)**: Strategy'i implement eden sınıflar için yapılmış taslak metod.

##### NavbarStrategy Sınıfı
- Strategy arayüzünü extend eder.
- **public void makeChanges(StringBuilder file, String config)**: config'de olan NAV_BAR_LINKS altındaki verileri uygun bir biçimde file'a ekler. Herhangi bir hata olması durumunda hatayı "throw"lar.

##### SocialLinksStrategy Sınıfı
- Strategy arayüzünü extend eder.
- **public void makeChanges(StringBuilder file, String config)**: config'de olan SOCIAL_LINKS ve SOCIAL_ICONS altındaki verileri uygun bir biçimde file'a ekler. Herhangi bir hata olması durumunda hatayı "throw"lar.

##### ThemeNameStrategy Sınıfı
- Strategy arayüzünü extend eder.
- **public void makeChanges(StringBuilder file, String config)**: config'de olan THEME_NAME altındaki veriyi uygun bir biçimde file'a ekler. Herhangi bir hata olması durumunda hatayı "throw"lar.

##### NonArrayStrategy Sınıfı
- Strategy arayüzünü extend eder.
- **public void makeChanges(StringBuilder file, String config)**: config'de olan NAV_BAR_LINKS altındaki verileri uygun bir biçimde file'a ekler. Herhangi bir hata olması durumunda hatayı "throw"lar.

##### Factory Sınıfı
- **static Strategy decideStrategy(String option)**: option'a bakarak uygun Strategy'yi seçer, Strategy'nin option'unu ayarlar ve Strategy'yi döner.

### config.toml

- Ayarlanabilir bütün değişkenlerin tutulduğu dosya formatı.

<a name="visual-interfaces"></a>
## Visual Interfaces:
![Example Page Top](http://osman.guru/images/sayfa_ust.png)
![Example Page Bottom](http://osman.guru/images/sayfa_alt.png)

<a name="use-case-support-design"></a>
# Use Case Support Design

<a name="use-case-selection"></a>
## Use Case Selection:

1. Kullanıcı, isterse config dosyasını isterse de geliştireceğimiz arayüzü kullanarak oluşturmak istediği internet sitesi için bir tema tercihinde bulunabilecek ve internet sitesinin başlık, alt başlık, açıklama gibi özelliklerini bu yollarla ayarlayabilecek.
2. Kullanıcı, pratik bir şekilde elindeki metin veya resim dosyalarını sisteme yükleyebilecek ve sistemi çalıştırdığında beklediği sonuçları alabilecek.
3. Kullanıcı internet sitesini oluşturduktan sonra her an modifiye edebilecek.
4. Kullanıcı, sistemi hızla kurup gerektiğinde hızla kaldırabilecek.


<a name="requirement-mapping"></a>
## Requirement Mapping:

1. config.toml dosyasının fonksiyonel özellikleri.
2. Builder.java dosyasının fonksiyonel özellikleri.
3. Builder.java dosyası fonksiyonel özellikleri.
4. osman.guru internet sitesinin kullanımı.

<a name="use-case-design"></a>
## Use Case Design:

Projemizde kullandığımız monolitik mimari sayesinde yazılımımızın tüm dosyaları tek bir pakette toplanmakta ve bu sayede gerekli bir çalıştırma dosyası çalıştırıldığında hızlı bir şekilde çalışır vaziyete gelebilmektedir. Program çalıştırıldığında sırası ile `config.toml -> Builder.java` akışı izlenmektedir.

<a name="design-decisions"></a>
# Design Decisions

<a name="technology-comparisons"></a>
## Technology Comparisons:

Projemizde temel "backend" dili olarak Java programlama dilini tercih ettik.
| Feature | Java   | C++ | Python |
| ------ | ------- | ------ | ------ |
| Typing | Static | Static | Dynamic |
| Syntax Complexity | Medium, strict but simpler than C++ | High, verbose & manual control | Low, very simple & readable |
| Memory Management | Automatic (Garbage Collector) | Manual + RAII | Automatic (Garbage Collector) |
| Main Use Cases | Enterprise apps android, backend | Games, simulations, system software | Data science, AI, scripting, web |
| Platform Spesification | Platform-unaffected | Platform dependent | Platform independent |

<a name="decision-justifications"></a>
## Decision Justifications

Projemiz bir internet sitesi oluşturucu olduğu için web framework kullandık.

## Task Matrix


| Görev      | Görevli                   |
| -------------------------- | ---------------------------------------- |
| Projenin Tanımı             | Mesut Barbaros Köş ve Melih Efe Tursun |
| Sistem Mimarisi            | Melih Efe Tursun |
| Backend İmplementasyonları      | Melih Efe Tursun    |
| Tema Tasarımı           | Mahmut Tosun ve Ahmet Taha Kılınç                           |
| Frontend Tasarımı             | Mesut Barbaros Köş           |
| Frontend İmplementasyonu      | Mesut Barbaros Köş              |
| Teknoloji Kıyaslamaları    | Mesut Barbaros Köş                           |
| Budget and Resources   | Mesut Barbaros Köş                           |

<a name="contributors"></a>
## Contributors
- Mesut Barbaros Köş
- Melih Efe Tursun
- Mahmut Tosun
- Ahmet Taha Kılınç

