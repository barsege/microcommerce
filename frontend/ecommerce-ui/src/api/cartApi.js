import { apiClient } from "./apiClient.js";

export function addCartItem(product, quantity = 1) {
  return apiClient("/api/carts/items", {
    method: "POST",
    body: JSON.stringify({
      productId: product.id,
      productName: product.name,
      unitPrice: product.price,
      quantity
    })
  });
}

export function getCart() {
  return apiClient("/api/carts");
}

export function updateCartItem(productId, quantity) {
  return apiClient(`/api/carts/items/${productId}`, {
    method: "PUT",
    body: JSON.stringify({ quantity })
  });
}

export function removeCartItem(productId) {
  return apiClient(`/api/carts/items/${productId}`, {
    method: "DELETE"
  });
}

export function clearCart() {
  return apiClient("/api/carts", {
    method: "DELETE"
  });
}
