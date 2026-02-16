#Design Document

#Table of Contents:

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
- -[Decision Justifications](#decision-justifications)

#System Overview {#system-overview}

##Brief Project Description: {#brief-project-description}
OSMAN; Java ile yazılmış, hız için optimize edilmiş ve esneklik için tasarlanmış bir statik internet sitesi oluşturucudur. Geniş tema seçenekleri ve kullanıcıya uyum sağlayan özellikleriyle internet sitesi oluşturmayı kolaylaştırarak birkaç tık öteye indiren gelişmiş bir araçtır. Kullanıcılar saniyeler içinde gerek ellerindeki yazılar ile gerekse de ellerinde bulunan resimler ile statik bir internet sitesi oluşturabilmektedir.

##System Architecture: {#system-architecture}
Projemiz monolitik mimari üslubunda “self-contained” bir yapıda “single unit” olarak “deploy” edilerek geliştirilmiştir.

##Technology Stack: {#technology-stack}
- Frontend: HTML, CSS, JavaScript
- Backend: Java


#Implementation Details {#implementation-details}

##Codebase Structure: {#codebase-structure}

`
├── config.toml
├── builder.java
├── content/
│   ├── _index.md
│   ├── first.md
│   └── second.md
├── output/
├── templates/
│   ├── base.html
│   ├── page.html
│   └── index.html
└── themes/
`

##Key Implementations: {#key-implementations}
- config.toml: Ana konfigürasyon dosyası, kullanıcıların oluşturmak istediklerini internet sitesinin; başlık, alt-başlık, tema gibi özelliklerini ayarlaması için yerleştirilmiş bir dosyadır.
- builder.java: Kullanıcıların config.toml dosyasından gerekli ayarlamaları yaptıktan ve sayfa oluşturmak için eklemek istedikleri belgeleri yükledikten sonra çalıştıracakları ve siteyi oluşturacak olan ana modüldür.

##Component Interfaces: {#component-interfaces}
### builder.java
- parse_content_files(filename): "content" klasöründeki tüm dosyaları analiz ederek bunları hafızada depolar.
- build_site(): "content", "templates", "themes" klasörlerindeki verileri, "parse_content_files(filename)" fonksiyonunu kullanarak "output" klasörüne siteyi hazırlayan fonksiyon.
- main(): "build_site()" fonksiyonunu çağırarak işlemi başlatan ana fonksiyon.

### config.toml
- Ayarlanabilir bütün değişkenlerin tutulduğu dosya formatı.

##Visual Interfaces: {#visual-interfaces}

#Use Case Support Design {#use-case-support-design}

##Use Case Selection: {#use-case-selection}
1. Kullanıcı, isterse config dosyasını isterse de geliştireceğimiz arayüzü kullanarak oluşturmak istediği internet sitesi için bir tema tercihinde bulunabilecek ve internet sitesinin başlık, alt başlık, açıklama gibi özelliklerini bu yollarla ayarlayabilecek.
2. Kullanıcı, pratik bir şekilde elindeki metin veya resim dosyalarını sisteme yükleyebilecek ve sistemi çalıştırdığında beklediği sonuçları alabilecek.
3. Kullanıcı internet sitesini oluşturduktan sonra her an modifiye edebilecek.
4. Kullanıcı, sistemi hızla kurup gerektiğinde hızla kaldırabilecek.


##Requirement Mapping: {#requirement-mapping}
> 1. config.toml dosyasının fonksiyonel özellikleri.
> 2. builder.java dosyasının fonksiyonel özellikleri.
> 3. builder.java dosyası fonksiyonel özellikleri.
> 4. osman.guru internet sitesinin kullanımı.

##Use Case Design: {#use-case-design}
Projemizde kullandığımız monolitik mimari sayesinde yazılımımızın tüm dosyaları tek bir pakette toplanmakta ve bu sayede gerekli bir çalıştırma dosyası çalıştırıldığında hızlı bir şekilde çalışır vaziyete gelebilmektedir. Program çalıştırıldığında sırası ile `config.toml -> builder.java` akışı izlenmektedir.

#Design Decisions {#design-decisions}
##Technology Comparisons: {#technology-comparisons}
Projemizde temel "backend" dili olarak Java programlama dilini tercih ettik.
| Feature | Java   | C++ | Python |
| ------ | ------- | ------ | ------ |
| Typing | Static | Static | Dynamic |
| Syntax Complexity | Medium, strict but simpler than C++ | High, verbose & manual control | Low, very simple & readable |
| Memory Management | Automatic (Garbage Collector) | Manual + RAII | Automatic (Garbage Collector) |
| Main Use Cases | Enterprise apps android, backend | Games, simulations, system software | Data science, AI, scripting, web |
| Platform Spesification | Platform-unaffected | Platform dependent | Platform independent |

##Decision Justifications {#decision-justifications}
Projemiz bir internet sitesi oluşturucu olduğu için web framework kullandık.


