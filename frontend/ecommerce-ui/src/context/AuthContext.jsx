import { createContext, useContext, useEffect, useMemo, useState } from "react";
import keycloak, { initKeycloak } from "../auth/keycloak.js";
import { setAccessTokenGetter } from "../auth/tokenProvider.js";
import { clearToken, getToken as getManualToken, saveToken } from "../utils/tokenStorage.js";

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [isLoading, setIsLoading] = useState(true);
  const [token, setToken] = useState(getManualToken);
  const [username, setUsername] = useState("");
  const [roles, setRoles] = useState([]);

  function getRealmRoles() {
    return keycloak.tokenParsed?.realm_access?.roles || [];
  }

  async function getToken() {
    if (keycloak.authenticated) {
      try {
        await keycloak.updateToken(30);
        setToken(keycloak.token || "");
        setRoles(getRealmRoles());
        return keycloak.token || null;
      } catch {
        setToken("");
        setIsAuthenticated(false);
        setRoles([]);
        return null;
      }
    }

    return getManualToken();
  }

  useEffect(() => {
    let active = true;

    setAccessTokenGetter(getToken);

    initKeycloak()
      .then((authenticated) => {
        if (!active) {
          return;
        }

        setIsAuthenticated(authenticated || Boolean(getManualToken()));
        setToken(keycloak.token || getManualToken() || "");
        setUsername(keycloak.tokenParsed?.preferred_username || (getManualToken() ? "Manual token" : ""));
        setRoles(authenticated ? getRealmRoles() : []);
      })
      .catch(() => {
        if (active) {
          setIsAuthenticated(false);
        }
      })
      .finally(() => {
        if (active) {
          setIsLoading(false);
        }
      });

    keycloak.onAuthSuccess = () => {
      setIsAuthenticated(true);
      setToken(keycloak.token || "");
      setUsername(keycloak.tokenParsed?.preferred_username || "");
      setRoles(getRealmRoles());
    };

    keycloak.onAuthLogout = () => {
      setIsAuthenticated(false);
      setToken("");
      setUsername("");
      setRoles([]);
    };

    keycloak.onTokenExpired = () => {
      getToken();
    };

    return () => {
      active = false;
    };
  }, []);

  function login() {
    keycloak.login();
  }

  function register() {
    keycloak.register();
  }

  function forgotPassword() {
    keycloak.login({ action: "UPDATE_PASSWORD" });
  }

  function goToAccount() {
    keycloak.accountManagement();
  }

  function saveManualToken(nextToken) {
    const trimmedToken = nextToken.trim();
    saveToken(trimmedToken);
    setIsAuthenticated(Boolean(trimmedToken));
    setToken(trimmedToken);
    setUsername(trimmedToken ? "Manual token" : "");
    setRoles([]);
  }

  function logout() {
    clearToken();
    if (keycloak.authenticated) {
      keycloak.logout({ redirectUri: window.location.origin });
      return;
    }

    setIsAuthenticated(false);
    setToken("");
    setUsername("");
    setRoles([]);
  }

  function hasRole(roleName) {
    return roles.includes(roleName);
  }

  const value = useMemo(() => ({
    token,
    isAuthenticated,
    isLoading,
    username,
    roles,
    isAdmin: hasRole("ADMIN"),
    hasRole,
    login,
    register,
    forgotPassword,
    goToAccount,
    logout,
    getToken,
    saveManualToken
  }), [isAuthenticated, isLoading, roles, token, username]);

  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  return useContext(AuthContext);
}
