# Microservices E-Commerce Application

Spring Boot + React ile geliştirilmiş mikroservis tabanlı e-ticaret uygulaması. Proje; ürün listeleme, sepet yönetimi, sipariş oluşturma, RabbitMQ ile event-driven Saga akışı, Keycloak JWT authentication ve Iyzico sandbox ödeme entegrasyonunu içerir.

## Proje Özellikleri

- Product listing ve product detail ekranları
- Cart management: ürün ekleme, adet güncelleme, ürün çıkarma, sepet temizleme
- Order creation ve kullanıcıya ait siparişleri listeleme
- RabbitMQ ile event-driven Saga flow
- Stok reserve, confirm ve rollback akışı
- PaymentProvider abstraction
- MockPaymentProvider ve IyzicoPaymentProvider
- Iyzico sandbox Non-3DS ödeme entegrasyonu
- Keycloak tabanlı JWT authentication
- API Gateway seviyesinde role-based authorization
- Cart ve Order kullanıcı izolasyonu için JWT `sub` kullanımı
- React frontend içinde Keycloak login/logout/register/forgot password akışı
- Swagger/OpenAPI dokümantasyonu
- Unit ve controller testleri
- Jib ile Docker image build konfigürasyonu

## Mimari

| Servis | Port | Görev |
| --- | ---: | --- |
| discovery-server | 8761 | Eureka service discovery sağlar. Mikroservisler bu registry üzerinden birbirini bulur. |
| config-server | 8888 | Native profile ile çalışan Spring Cloud Config Server yapısıdır. |
| api-gateway | 8080 | Spring Cloud Gateway WebFlux ile backend servislerini tek giriş noktasından expose eder. JWT doğrulama ve role-based access burada yapılır. |
| product-service | 8081 | Ürün listeleme, ürün detayı, ürün oluşturma ve stok reserve/confirm/rollback işlemlerini yönetir. |
| cart-service | 8082 | Authenticated kullanıcının sepet işlemlerini yönetir. Kullanıcı kimliği JWT subject üzerinden alınır. |
| order-service | 8083 | Sipariş oluşturur, sipariş durumlarını yönetir ve OrderCreatedEvent yayınlar. Kullanıcı siparişleri JWT subject ile izole edilir. |
| payment-service | 8084 | StockReservedEvent sonrası ödeme işlemini çalıştırır ve ödeme sonucuna göre PaymentCompletedEvent veya PaymentFailedEvent yayınlar. |

## Order / Payment Saga Flow

Projede kullanılan ana event isimleri:

- `OrderCreatedEvent`
- `StockReservedEvent`
- `StockReservationFailedEvent`
- `PaymentCompletedEvent`
- `PaymentFailedEvent`

Akış:

1. Kullanıcı ürünleri listeler ve sepete ürün ekler.
2. Kullanıcı sepetten sipariş oluşturur.
3. `order-service`, siparişi `CREATED` durumunda kaydeder ve `OrderCreatedEvent` yayınlar.
4. `product-service`, `OrderCreatedEvent` dinler ve ürün stoklarını reserve etmeye çalışır.
5. Stok reserve başarılıysa `StockReservedEvent` yayınlanır.
6. `payment-service`, `StockReservedEvent` dinler ve seçili payment provider ile ödeme işlemini başlatır.
7. Ödeme başarılıysa `PaymentCompletedEvent` yayınlanır.
8. `order-service`, siparişi `PAID` durumuna alır; `product-service` reserved stock için confirm işlemi yapar.
9. Ödeme başarısızsa `PaymentFailedEvent` yayınlanır.
10. `order-service`, siparişi `PAYMENT_FAILED` durumuna alır; `product-service` reserved stock için rollback/release işlemi yapar.

## Authentication & Authorization

Keycloak konfigürasyonu kodda şu değerlerle kullanılıyor:

- Keycloak URL: `http://localhost:8089`
- Realm: `microcommerce`
- Client: `microcommerce-frontend`
- JWT issuer-uri: `http://localhost:8089/realms/microcommerce`
- JWK set URI: `http://localhost:8089/realms/microcommerce/protocol/openid-connect/certs`

API Gateway JWT Resource Server olarak çalışır. Keycloak access token içindeki `realm_access.roles` alanından roller okunur ve Spring Security authority formatına çevrilir.

Mevcut authorization kuralları:

- Public: `GET /api/products/**`
- USER: `/api/carts/**`, `/api/orders/**`
- ADMIN: `POST /api/products`
- Gateway config içinde `PUT /api/products/**` ve `DELETE /api/products/**` için ADMIN kuralı vardır; mevcut product controller içinde şu anda create endpointi bulunur.

Cart ve Order servislerinde veri sahipliği frontend'den gelen `userId` ile yapılmaz. Authenticated JWT içindeki `sub` değeri alınır ve `userId` olarak saklanır. Böylece `user1` sadece kendi cart/order kayıtlarını, `user2` sadece kendi kayıtlarını görür.

Frontend tarafında `keycloak-js` kullanılır. Login, logout, register, forgot password ve account management işlemleri Keycloak'a redirect edilerek yapılır. `/token` route'u manuel token debug/fallback amacıyla korunmuştur.

## Payment Integration

`payment-service` içinde `PaymentProvider` interface'i bulunur:

- `MockPaymentProvider`: Test/demo için mock ödeme sonucu üretir. `payment.mock.force-failure=true` ile failure senaryosu denenebilir.
- `IyzicoPaymentProvider`: `iyzipay-java` SDK ile Iyzico sandbox Non-3DS create payment çağrısı yapar.

Provider seçimi application config üzerinden yapılır:

```yaml
payment:
  provider: iyzico
  currency: TRY
  payment-method: IYZICO
```

Iyzico sandbox credential değerleri environment variable olarak verilmelidir:

```powershell
set IYZICO_API_KEY=your_sandbox_api_key
set IYZICO_SECRET_KEY=your_sandbox_secret_key
```

PowerShell:

```powershell
$env:IYZICO_API_KEY="your_sandbox_api_key"
$env:IYZICO_SECRET_KEY="your_sandbox_secret_key"
```

Secret key değerleri repository içine yazılmamalıdır. Local geliştirme için config içinde dummy fallback değerler vardır.

## Tech Stack

Backend:

- Java 17
- Spring Boot 3.5.x
- Spring Cloud Gateway WebFlux
- Spring Cloud Netflix Eureka
- Spring Cloud Config
- Spring Data JPA
- PostgreSQL
- RabbitMQ
- Keycloak JWT Resource Server
- Iyzico Java SDK
- Swagger/OpenAPI
- JUnit, Mockito, Spring Security Test

Frontend:

- React
- Vite
- React Router
- Fetch tabanlı merkezi `apiClient`
- `keycloak-js`

DevOps:

- Docker Compose
- Jib Maven Plugin

## Running the Project

### Infrastructure Başlatma

```powershell
cd infrastructure
docker compose up -d
```

Bu komut PostgreSQL, RabbitMQ ve Keycloak servislerini başlatır.

Servis URL'leri:

- RabbitMQ Management: `http://localhost:15672`
- Keycloak: `http://localhost:8089`
- Keycloak admin kullanıcı bilgisi docker compose içinde `admin/admin` olarak tanımlıdır.

Keycloak kurulumu için realm, client, role ve kullanıcı ayarlarını yapmak gerekir. Detaylı adımlar için:

```text
docs/keycloak-setup.md
```

### Backend Servisleri Başlatma Sırası

Önerilen sıra:

1. discovery-server
2. config-server
3. product-service
4. cart-service
5. order-service
6. payment-service
7. api-gateway

Örnek:

```powershell
cd backend\discovery-server
.\mvnw.cmd spring-boot:run
```

```powershell
cd backend\config-server
.\mvnw.cmd spring-boot:run
```

```powershell
cd backend\payment-service
set IYZICO_API_KEY=your_sandbox_api_key
set IYZICO_SECRET_KEY=your_sandbox_secret_key
.\mvnw.cmd spring-boot:run
```

Diğer servisler için de aynı komut kendi klasörlerinde çalıştırılır:

```powershell
.\mvnw.cmd spring-boot:run
```

### Frontend Başlatma

```powershell
cd frontend\ecommerce-ui
npm install
npm run dev
```

Frontend varsayılan olarak:

```text
http://localhost:5173
```

API Gateway base URL:

```text
http://localhost:8080
```

## Swagger URLs

Her servis kendi Swagger UI endpointine sahiptir:

- Product Service: `http://localhost:8081/swagger-ui.html`
- Cart Service: `http://localhost:8082/swagger-ui.html`
- Order Service: `http://localhost:8083/swagger-ui.html`
- Payment Service: `http://localhost:8084/swagger-ui.html`

## API Endpoint Özeti

API Gateway üzerinden kullanılan ana endpointler:

```text
GET    /api/products
GET    /api/products/{id}
POST   /api/products

POST   /api/carts/items
GET    /api/carts
PUT    /api/carts/items/{productId}
DELETE /api/carts/items/{productId}
DELETE /api/carts

POST   /api/orders
GET    /api/orders
GET    /api/orders/{orderId}
```

Cart ve Order endpointleri authenticated user için çalışır; request path veya body üzerinden `userId` gönderilmez.

## Test

Her backend servisinde testleri çalıştırmak için:

```powershell
cd backend\product-service
.\mvnw.cmd test
```

```powershell
cd backend\cart-service
.\mvnw.cmd test
```

```powershell
cd backend\order-service
.\mvnw.cmd test
```

```powershell
cd backend\payment-service
.\mvnw.cmd test
```

Frontend build kontrolü:

```powershell
cd frontend\ecommerce-ui
npm run build
```

## Jib

Backend servislerinde Jib Maven Plugin konfigürasyonu bulunur. Dockerfile yazmadan image oluşturmak için servis klasöründe:

```powershell
.\mvnw.cmd jib:dockerBuild
```

Tar çıktısı almak için:

```powershell
.\mvnw.cmd jib:buildTar
```

Image isimleri `microcommerce/*` formatındadır. Örnekler:

- `microcommerce/discovery-server`
- `microcommerce/config-server`
- `microcommerce/api-gateway`
- `microcommerce/product-service`
- `microcommerce/cart-service`
- `microcommerce/order-service`
- `microcommerce/payment-service`

Not: `jib:dockerBuild` local Docker daemon'a erişim gerektirir. Docker Desktop kapalıysa veya daemon erişimi yoksa build hata verebilir.

## Demo Flow

1. Infrastructure servislerini başlat.
2. Keycloak'ta `microcommerce` realm, `microcommerce-frontend` client ve `USER`/`ADMIN` rollerini hazırla.
3. Frontend'i aç: `http://localhost:5173`
4. Register veya Login ile Keycloak üzerinden giriş yap.
5. Product list ekranından ürünleri görüntüle.
6. Product detail ekranından ürünü sepete ekle.
7. Cart ekranında sepeti kontrol et ve checkout/order oluştur.
8. Saga flow başlar: stok reserve edilir, payment-service ödeme işlemini çalıştırır.
9. Iyzico sandbox success durumunda order `PAID` olur ve stok confirm edilir.
10. Payment failure durumunda order `PAYMENT_FAILED` olur ve stok rollback/release edilir.
11. Orders ekranında authenticated kullanıcının siparişleri görüntülenir.

## Known Notes / Limitations

- Iyzico sandbox ortamında gerçek kart kullanılmaz; sandbox test kartı kullanılır.
- Production ortamı için secret management gerekir. API key ve secret key repository içine yazılmamalıdır.
- Frontend'de kullanıcıdan kart bilgisi alan checkout/payment formu henüz yoktur. Mevcut Iyzico akışı config içindeki sandbox test kartı ile çalışır.
- Payment amount tarafında mevcut payment-service config içinde default amount değeri bulunur.
- Jib local Docker daemon build işlemi geliştirme ortamına bağlıdır.
- Eski numeric veya hardcoded user test dataları varsa user isolation testinden önce cart/order/payment tabloları temizlenmelidir.

## Future Improvements

- Iyzico Checkout Form veya frontend payment UI
- Admin product management ekranları
- AWS deployment
- CI/CD pipeline geliştirme
- Monitoring ve Slack notification
- Production secret management entegrasyonu

## Final Project Summary

Bu proje Spring Boot mikroservisleri ve React frontend ile geliştirilmiş uçtan uca bir e-ticaret MVP'sidir. API Gateway, Eureka, PostgreSQL, RabbitMQ, Keycloak ve Iyzico sandbox entegrasyonlarıyla gerçekçi bir mikroservis mimarisi sunar. Sipariş ve ödeme akışı RabbitMQ eventleri üzerinden Saga yaklaşımıyla ilerler. Frontend tarafında Keycloak login akışı, role-based UI ve authenticated user'a ait cart/order izolasyonu desteklenir.
