import { getToken as getManualToken } from "../utils/tokenStorage.js";

let accessTokenGetter = async () => getManualToken();

export function setAccessTokenGetter(getter) {
  accessTokenGetter = getter;
}

export async function getAccessToken() {
  return accessTokenGetter();
}
