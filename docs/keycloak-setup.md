# Local Keycloak Setup

This project uses Keycloak locally so the API Gateway can validate JWT access tokens for cart, order, and admin product actions.

## 1. Start Infrastructure

From the project root:

```powershell
docker compose -f infrastructure/docker-compose.yml up -d
```

This starts:

- PostgreSQL: `localhost:5432`
- RabbitMQ: `localhost:5672`, management UI at `http://localhost:15672`
- Keycloak: `http://localhost:8089`

## 2. Open Keycloak

Open:

```text
http://localhost:8089
```

Login to the admin console with:

```text
Username: admin
Password: admin
```

## 3. Create Realm

Create a realm named:

```text
microcommerce
```

The API Gateway expects this issuer:

```text
http://localhost:8089/realms/microcommerce
```

## 4. Create React Client

Create a client named:

```text
microcommerce-frontend
```

Recommended settings:

- Client type: `OpenID Connect`
- Client authentication: `Off`
- Standard flow: `On`
- Direct access grants: `On`
- Valid redirect URIs: `http://localhost:5173/*`
- Web origins: `http://localhost:5173`

Save the client.

## 5. Create Realm Roles

In the `microcommerce` realm, create these realm roles:

```text
USER
ADMIN
```

The API Gateway maps Keycloak realm roles from `realm_access.roles` to Spring Security roles:

- `USER` -> `ROLE_USER`
- `ADMIN` -> `ROLE_ADMIN`

## 6. Create Users

Create at least two users:

```text
user1
admin1
```

For each user:

1. Set a password in the Credentials tab.
2. Turn Temporary off.
3. Assign realm roles in the Role mapping tab.

Suggested test setup:

- `user1` gets `USER`
- `admin1` gets `USER` and `ADMIN`

## 7. Get Access Token

PowerShell example for a USER token:

```powershell
$userTokenResponse = Invoke-RestMethod `
  -Method Post `
  -Uri "http://localhost:8089/realms/microcommerce/protocol/openid-connect/token" `
  -ContentType "application/x-www-form-urlencoded" `
  -Body @{
    client_id = "microcommerce-frontend"
    grant_type = "password"
    username = "user1"
    password = "password"
  }

$userTokenResponse.access_token
```

PowerShell example for an ADMIN token:

```powershell
$adminTokenResponse = Invoke-RestMethod `
  -Method Post `
  -Uri "http://localhost:8089/realms/microcommerce/protocol/openid-connect/token" `
  -ContentType "application/x-www-form-urlencoded" `
  -Body @{
    client_id = "microcommerce-frontend"
    grant_type = "password"
    username = "admin1"
    password = "password"
  }

$adminTokenResponse.access_token
```

Use the password you set for each user.

## 8. Paste Token Into React

Start the frontend:

```powershell
cd frontend/ecommerce-ui
npm install
npm run dev
```

Open:

```text
http://localhost:5173/token
```

Paste the access token and save it. The frontend stores it in `localStorage` and sends it as:

```text
Authorization: Bearer <token>
```

## 9. Test End-to-End

Start backend services, including discovery server, config server if used, API Gateway, product-service, cart-service, and order-service.

Public product list should work without a token:

```text
http://localhost:5173/products
```

With a USER token:

1. Open `http://localhost:5173/token`.
2. Save the `user1` access token.
3. Open `http://localhost:5173/products`.
4. Open a product detail page.
5. Add the product to cart.
6. Open `http://localhost:5173/cart`.
7. Create an order.
8. Open `http://localhost:5173/orders`.

Expected result: cart and order requests return successfully through the API Gateway.

With an ADMIN token, test product creation through the API Gateway:

```powershell
$headers = @{
  Authorization = "Bearer $($adminTokenResponse.access_token)"
  "Content-Type" = "application/json"
}

$body = @{
  name = "Admin Test Product"
  description = "Created through API Gateway with ADMIN token"
  price = 49.99
  stock = 20
  category = "TEST"
  imageUrl = "https://placehold.co/600x400"
} | ConvertTo-Json

Invoke-RestMethod `
  -Method Post `
  -Uri "http://localhost:8080/api/products" `
  -Headers $headers `
  -Body $body
```

Expected authorization behavior:

- `GET /api/products/**`: public
- `/api/carts/**`: requires `USER`
- `/api/orders/**`: requires `USER`
- `POST /api/products`: requires `ADMIN`
- `PUT /api/products/**`: requires `ADMIN`
- `DELETE /api/products/**`: requires `ADMIN`
