import { Link } from "react-router-dom";
import LoadingState from "../components/LoadingState.jsx";
import { useAuth } from "../context/AuthContext.jsx";

function AdminRoute({ children }) {
  const { isAdmin, isAuthenticated, isLoading, login } = useAuth();

  if (isLoading) {
    return <LoadingState text="Checking permissions..." />;
  }

  if (!isAuthenticated) {
    return (
      <section>
        <div className="content-panel auth-required">
          <h1>Lütfen giriş yapın</h1>
          <p className="muted">Admin panelini görüntülemek için giriş yapmanız gerekir.</p>
          <button className="primary-button" type="button" onClick={login}>
            Login
          </button>
        </div>
      </section>
    );
  }

  if (!isAdmin) {
    return (
      <section>
        <div className="content-panel auth-required">
          <h1>Bu sayfaya erişim yetkiniz yok.</h1>
          <p className="muted">Bu alan sadece ADMIN rolüne sahip kullanıcılar içindir.</p>
          <Link className="secondary-button" to="/products">
            Ana sayfaya dön
          </Link>
        </div>
      </section>
    );
  }

  return children;
}

export default AdminRoute;
