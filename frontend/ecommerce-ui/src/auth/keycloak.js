import Keycloak from "keycloak-js";

const keycloak = new Keycloak({
  url: "http://localhost:8089",
  realm: "microcommerce",
  clientId: "microcommerce-frontend"
});

let initPromise;

export function initKeycloak() {
  if (!initPromise) {
    initPromise = keycloak.init({
      // Remember Me is handled by Keycloak cookies. check-sso lets keycloak-js restore that session on app load.
      onLoad: "check-sso",
      checkLoginIframe: false,
      pkceMethod: "S256"
    });
  }

  return initPromise;
}

export default keycloak;
