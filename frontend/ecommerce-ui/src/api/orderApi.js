import { apiClient } from "./apiClient.js";

export function createOrderFromCart(cart) {
  return apiClient("/api/orders", {
    method: "POST",
    body: JSON.stringify({
      items: cart.items.map((item) => ({
        productId: item.productId,
        productName: item.productName,
        unitPrice: item.unitPrice,
        quantity: item.quantity
      }))
    })
  });
}

export function getOrdersByUser() {
  return apiClient("/api/orders");
}
