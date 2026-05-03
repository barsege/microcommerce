import { NavLink } from "react-router-dom";
import { useAuth } from "../context/AuthContext.jsx";

function Header() {
  const { goToAccount, isAdmin, isAuthenticated, isLoading, login, logout, register, username } = useAuth();

  return (
    <header className="site-header">
      <NavLink to="/products" className="brand">MicroCommerce</NavLink>
      <nav className="main-nav">
        <NavLink to="/products">Products</NavLink>
        <NavLink to="/cart">Cart</NavLink>
        <NavLink to="/orders">Orders</NavLink>
        {isAuthenticated && isAdmin && <NavLink to="/admin">Admin Panel</NavLink>}
      </nav>
      {!isLoading && !isAuthenticated && (
        <div className="user-actions">
          <button className="ghost-button" type="button" onClick={register}>
            Register
          </button>
          <button className="primary-button" type="button" onClick={login}>
            Login
          </button>
        </div>
      )}
      {!isLoading && isAuthenticated && (
        <div className="user-actions">
          <span className="username">{username || "User"}</span>
          <button className="ghost-button" type="button" onClick={goToAccount}>
            Hesabım
          </button>
          <button className="ghost-button" type="button" onClick={logout}>
            Logout
          </button>
        </div>
      )}
      {isLoading && (
        <button className="ghost-button" type="button" disabled>
          Loading...
        </button>
      )}
    </header>
  );
}

export default Header;
