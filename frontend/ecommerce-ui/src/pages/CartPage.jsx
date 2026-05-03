import { useEffect, useState } from "react";
import ErrorState from "../components/ErrorState.jsx";
import LoadingState from "../components/LoadingState.jsx";
import { clearCart, getCart, removeCartItem, updateCartItem } from "../api/cartApi.js";
import { createOrderFromCart } from "../api/orderApi.js";
import { useAuth } from "../context/AuthContext.jsx";
import { formatCurrency } from "../utils/formatters.js";

function CartPage() {
  const { isAuthenticated, isLoading, login } = useAuth();
  const [cart, setCart] = useState(null);
  const [order, setOrder] = useState(null);
  const [loading, setLoading] = useState(true);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState("");

  async function loadCart() {
    setLoading(true);
    setError("");

    try {
      setCart(await getCart());
    } catch (err) {
      setCart({ items: [], totalAmount: 0 });
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    if (isAuthenticated) {
      loadCart();
    } else if (!isLoading) {
      setLoading(false);
    }
  }, [isAuthenticated, isLoading]);

  async function handleQuantityChange(productId, quantity) {
    setBusy(true);
    setError("");

    try {
      const updatedCart = await updateCartItem(productId, Number(quantity));
      setCart(updatedCart);
    } catch (err) {
      setError(err.message);
    } finally {
      setBusy(false);
    }
  }

  async function handleRemove(productId) {
    setBusy(true);
    setError("");

    try {
      setCart(await removeCartItem(productId));
    } catch (err) {
      setError(err.message);
    } finally {
      setBusy(false);
    }
  }

  async function handleClearCart() {
    setBusy(true);
    setError("");

    try {
      await clearCart();
      setCart({ items: [], totalAmount: 0 });
    } catch (err) {
      setError(err.message);
    } finally {
      setBusy(false);
    }
  }

  async function handleCreateOrder() {
    setBusy(true);
    setError("");
    setOrder(null);

    try {
      const createdOrder = await createOrderFromCart(cart);
      setOrder(createdOrder);
      await handleClearCart();
    } catch (err) {
      setError(err.message);
    } finally {
      setBusy(false);
    }
  }

  const items = cart?.items || [];

  if (isLoading) {
    return <LoadingState text="Checking login..." />;
  }

  if (!isAuthenticated) {
    return (
      <section>
        <div className="content-panel auth-required">
          <h1>Lütfen giriş yapın</h1>
          <p className="muted">Sepetinizi görüntülemek için Keycloak ile giriş yapmanız gerekir.</p>
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
          <p className="eyebrow">My cart</p>
          <h1>Cart</h1>
        </div>
        <button className="secondary-button" type="button" onClick={loadCart}>
          Refresh
        </button>
      </div>

      {loading && <LoadingState text="Loading cart..." />}
      <ErrorState message={error} />

      {!loading && (
        <div className="content-panel">
          {items.length === 0 ? (
            <p className="muted">Your cart is empty.</p>
          ) : (
            <>
              <div className="table-list">
                {items.map((item) => (
                  <div className="cart-row" key={item.productId}>
                    <div>
                      <strong>{item.productName}</strong>
                      <p className="muted">{formatCurrency(item.unitPrice)} each</p>
                    </div>
                    <input
                      aria-label={`Quantity for ${item.productName}`}
                      min="1"
                      type="number"
                      value={item.quantity}
                      disabled={busy}
                      onChange={(event) => handleQuantityChange(item.productId, event.target.value)}
                    />
                    <strong>{formatCurrency(item.totalPrice)}</strong>
                    <button className="ghost-button" type="button" disabled={busy} onClick={() => handleRemove(item.productId)}>
                      Remove
                    </button>
                  </div>
                ))}
              </div>

              <div className="summary-row">
                <strong>Total: {formatCurrency(cart.totalAmount)}</strong>
                <div className="actions">
                  <button className="secondary-button" type="button" disabled={busy} onClick={handleClearCart}>
                    Clear cart
                  </button>
                  <button className="primary-button" type="button" disabled={busy} onClick={handleCreateOrder}>
                    Create order
                  </button>
                </div>
              </div>
            </>
          )}
        </div>
      )}

      {order && (
        <div className="state-box success-box">
          Order #{order.orderId} created with status {order.status}.
        </div>
      )}
    </section>
  );
}

export default CartPage;
