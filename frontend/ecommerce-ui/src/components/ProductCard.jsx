import { Link } from "react-router-dom";
import { formatCurrency } from "../utils/formatters.js";

function ProductCard({ product }) {
  return (
    <article className="product-card">
      <div className="product-image">
        {product.imageUrl ? (
          <img src={product.imageUrl} alt={product.name} />
        ) : (
          <span>{product.category || "Product"}</span>
        )}
      </div>
      <div className="product-card-body">
        <p className="eyebrow">{product.category || "General"}</p>
        <h2>{product.name}</h2>
        <p className="muted line-clamp">{product.description || "No description available."}</p>
        <div className="card-footer">
          <strong>{formatCurrency(product.price)}</strong>
          <Link className="primary-button small" to={`/products/${product.id}`}>
            Details
          </Link>
        </div>
      </div>
    </article>
  );
}

export default ProductCard;
