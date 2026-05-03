import { apiClient } from "./apiClient.js";

export function getProducts(page = 0, size = 10) {
  return apiClient(`/api/products?page=${page}&size=${size}`, { skipAuth: true });
}

export function getProductById(id) {
  return apiClient(`/api/products/${id}`, { skipAuth: true });
}
