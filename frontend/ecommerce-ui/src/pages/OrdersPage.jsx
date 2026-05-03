import { useEffect, useState } from "react";
import ErrorState from "../components/ErrorState.jsx";
import LoadingState from "../components/LoadingState.jsx";
import { getOrdersByUser } from "../api/orderApi.js";
import { useAuth } from "../context/AuthContext.jsx";
import { formatCurrency } from "../utils/formatters.js";

function OrdersPage() {
  const { isAuthenticated, isLoading, login } = useAuth();
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  async function loadOrders() {
    setLoading(true);
    setError("");

    try {
      setOrders(await getOrdersByUser());
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    if (isAuthenticated) {
      loadOrders();
    } else if (!isLoading) {
      setLoading(false);
    }
  }, [isAuthenticated, isLoading]);

  if (isLoading) {
    return <LoadingState text="Checking login..." />;
  }

  if (!isAuthenticated) {
    return (
      <section>
        <div className="content-panel auth-required">
          <h1>Lütfen giriş yapın</h1>
          <p className="muted">Siparişlerinizi görüntülemek için Keycloak ile giriş yapmanız gerekir.</p>
          <button className="primary-button" type="button" onClick={login}>
            Login
          </button>
        </div>
      </section>
    );
  }

  return (
    <section>
      <div className="page-heading">
        <div>
          <p className="eyebrow">My orders</p>
          <h1>Orders</h1>
        </div>
        <button className="secondary-button" type="button" onClick={loadOrders}>
          Refresh
        </button>
      </div>

      {loading && <LoadingState text="Loading orders..." />}
      <ErrorState message={error} />

      {!loading && !error && (
        <div className="orders-list">
          {orders.length === 0 && <div className="content-panel muted">No orders yet.</div>}
          {orders.map((order) => (
            <article className="content-panel" key={order.orderId}>
              <div className="order-header">
                <div>
                  <p className="eyebrow">Order #{order.orderId}</p>
                  <h2>{order.status}</h2>
                </div>
                <strong>{formatCurrency(order.totalAmount)}</strong>
              </div>
              <div className="table-list compact">
                {order.items.map((item) => (
                  <div className="order-item" key={item.id}>
                    <span>{item.productName}</span>
                    <span>{item.quantity} x {formatCurrency(item.unitPrice)}</span>
                    <strong>{formatCurrency(item.totalPrice)}</strong>
                  </div>
                ))}
              </div>
            </article>
          ))}
        </div>
      )}
    </section>
  );
}

export default OrdersPage;
