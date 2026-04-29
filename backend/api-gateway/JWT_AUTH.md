# API Gateway JWT Authentication

The API Gateway is configured as an OAuth2 Resource Server and validates JWTs before requests are routed to backend services.

## Keycloak Example

Create a Keycloak realm named `microcommerce` and a client for the frontend/API. Use realm roles named:

- `USER`
- `ADMIN`

Assign those roles to test users. Keycloak includes realm roles in the JWT under `realm_access.roles`, which the gateway maps to Spring Security authorities:

- `USER` -> `ROLE_USER`
- `ADMIN` -> `ROLE_ADMIN`

Example gateway config:

```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: http://localhost:8085/realms/microcommerce
          jwk-set-uri: http://localhost:8085/realms/microcommerce/protocol/openid-connect/certs
```

## Authorization Rules

- Public: `GET /api/products/**`
- Public Swagger/OpenAPI paths: `/swagger-ui/**`, `/v3/api-docs/**`
- `USER`: `/api/carts/**`, `/api/orders/**`
- `ADMIN`: `POST /api/products`, `PUT /api/products/**`, `DELETE /api/products/**`

## Manual Test

Call public product endpoints without a token:

```bash
curl http://localhost:8080/api/products
```

Call protected endpoints with a JWT:

```bash
curl -H "Authorization: Bearer <USER_TOKEN>" http://localhost:8080/api/carts/user-1
curl -X POST -H "Authorization: Bearer <ADMIN_TOKEN>" -H "Content-Type: application/json" http://localhost:8080/api/products -d '{"name":"Keyboard","description":"Mechanical","price":99.99,"stock":10,"category":"ACCESSORIES","imageUrl":"https://example.com/keyboard.png"}'
```

Expected responses:

- Missing token on protected paths: `401 Unauthorized`
- Valid token without required role: `403 Forbidden`
- Valid token with required role: request is routed to the target service
