# Jib Image Build

Backend services use the Jib Maven plugin to build Docker images without Dockerfiles.

## Services

| Service | Image |
| --- | --- |
| discovery-server | `microcommerce/discovery-server` |
| config-server | `microcommerce/config-server` |
| api-gateway | `microcommerce/api-gateway` |
| product-service | `microcommerce/product-service` |
| cart-service | `microcommerce/cart-service` |
| order-service | `microcommerce/order-service` |
| payment-service | `microcommerce/payment-service` |

## Build One Service

Run from the service directory:

```powershell
.\mvnw.cmd -q -DskipTests compile
.\mvnw.cmd jib:dockerBuild
```

Example:

```powershell
cd backend\product-service
.\mvnw.cmd -q -DskipTests compile
.\mvnw.cmd jib:dockerBuild
```

## Verify Images

```powershell
docker images microcommerce/*
```

Expected result: the built `microcommerce/*` image appears in the local Docker daemon.
