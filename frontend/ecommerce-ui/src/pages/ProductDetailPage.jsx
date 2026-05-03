import { useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";
import ErrorState from "../components/ErrorState.jsx";
import LoadingState from "../components/LoadingState.jsx";
import { addCartItem } from "../api/cartApi.js";
import { getProductById } from "../api/productApi.js";
import { useAuth } from "../context/AuthContext.jsx";
import { formatCurrency } from "../utils/formatters.js";

function ProductDetailPage() {
  const { id } = useParams();
  const { forgotPassword, isAdmin, isAuthenticated, isLoading: authLoading, login } = useAuth();
  const [product, setProduct] = useState(null);
  const [quantity, setQuantity] = useState(1);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");
  const [showLoginPrompt, setShowLoginPrompt] = useState(false);
  const [message, setMessage] = useState("");

  useEffect(() => {
    async function loadProduct() {
      setLoading(true);
      setError("");

      try {
        setProduct(await getProductById(id));
      } catch (err) {
        setError(err.message);
      } finally {
        setLoading(false);
      }
    }

    loadProduct();
  }, [id]);

  async function handleAddToCart() {
    if (!isAuthenticated) {
      setError("Sepete ürün eklemek için giriş yapmalısınız.");
      setShowLoginPrompt(true);
      setMessage("");
      return;
    }

    setSaving(true);
    setError("");
    setShowLoginPrompt(false);
    setMessage("");

    try {
      await addCartItem(product, Number(quantity));
      setMessage("Added to cart.");
    } catch (err) {
      setError(err.message);
    } finally {
      setSaving(false);
    }
  }

  if (loading) {
    return <LoadingState text="Loading product..." />;
  }

  if (!product) {
    return <ErrorState message={error || "Product not found."} />;
  }

  return (
    <section>
      <Link className="text-link" to="/products">Back to products</Link>
      <div className="detail-layout">
        <div className="detail-image">
          {product.imageUrl ? (
            <img src={product.imageUrl} alt={product.name} />
          ) : (
            <span>{product.category || "Product"}</span>
          )}
        </div>
        <div className="detail-panel">
          <p className="eyebrow">{product.category || "General"}</p>
          <h1>{product.name}</h1>
          <p className="muted">{product.description || "No description available."}</p>
          <div className="detail-meta">
            <strong>{formatCurrency(product.price)}</strong>
            <span>{product.availableStock ?? product.stock} available</span>
          </div>

          <div className="inline-form">
            <label htmlFor="quantity">Quantity</label>
            <input
              id="quantity"
              min="1"
              type="number"
              value={quantity}
              onChange={(event) => setQuantity(event.target.value)}
            />
            <button className="primary-button" type="button" onClick={handleAddToCart} disabled={saving}>
              {saving ? "Adding..." : "Add to cart"}
            </button>
          </div>

          <ErrorState message={error} />
          {!authLoading && showLoginPrompt && (
            <div className="login-prompt-actions">
              <button className="primary-button" type="button" onClick={login}>
                Giriş yap ve devam et
              </button>
              <button className="link-button" type="button" onClick={forgotPassword}>
                Şifremi unuttum
              </button>
            </div>
          )}
          {message && <div className="state-box success-box">{message}</div>}
          {isAdmin && (
            <div className="admin-actions">
              <p className="eyebrow">Admin Actions</p>
              <strong>Ürünü Yönet</strong>
              <p className="muted">İleride ürün güncelleme ve silme işlemleri buraya eklenecek.</p>
            </div>
          )}
        </div>
      </div>
    </section>
  );
}

export default ProductDetailPage;
