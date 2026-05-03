import { getAccessToken } from "../auth/tokenProvider.js";

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080";

export async function apiClient(path, options = {}) {
  const { skipAuth = false, ...fetchOptions } = options;
  const token = skipAuth ? null : await getAccessToken();

  if (!skipAuth && !token) {
    throw new Error("Bu işlem için giriş yapmalısınız.");
  }

  const headers = {
    "Content-Type": "application/json",
    ...fetchOptions.headers
  };

  if (token && !skipAuth) {
    headers.Authorization = `Bearer ${token}`;
  }

  let response;

  try {
    response = await fetch(`${API_BASE_URL}${path}`, {
      ...fetchOptions,
      headers
    });
  } catch {
    throw new Error("Sunucuya ulaşılamadı. Lütfen servislerin çalıştığını kontrol edin.");
  }

  if (response.status === 204) {
    return null;
  }

  const contentType = response.headers.get("content-type");
  const data = contentType?.includes("application/json")
    ? await response.json()
    : await response.text();

  if (!response.ok) {
    if (response.status === 401) {
      throw new Error("Bu işlem için giriş yapmalısınız.");
    }

    const message = typeof data === "object" && data?.message
      ? data.message
      : `Request failed with status ${response.status}`;
    throw new Error(message);
  }

  return data;
}
