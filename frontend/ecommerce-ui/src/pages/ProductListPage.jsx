import { useEffect, useState } from "react";
import ProductCard from "../components/ProductCard.jsx";
import LoadingState from "../components/LoadingState.jsx";
import ErrorState from "../components/ErrorState.jsx";
import { getProducts } from "../api/productApi.js";

const PAGE_SIZE = 10;

function ProductListPage() {
  const [products, setProducts] = useState([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    async function loadProducts() {
      setLoading(true);
      setError("");

      try {
        const data = await getProducts(page, PAGE_SIZE);
        setProducts(data.content || []);
        setTotalPages(data.totalPages || 0);
      } catch (err) {
        setError(err.message);
      } finally {
        setLoading(false);
      }
    }

    loadProducts();
  }, [page]);

  return (
    <section>
      <div className="page-heading">
        <div>
          <p className="eyebrow">Catalog</p>
          <h1>Products</h1>
        </div>
      </div>

      {loading && <LoadingState text="Loading products..." />}
      <ErrorState message={error} />

      {!loading && !error && (
        <>
          <div className="product-grid">
            {products.map((product) => (
              <ProductCard key={product.id} product={product} />
            ))}
          </div>

          <div className="pagination">
            <button
              type="button"
              className="secondary-button"
              disabled={page === 0}
              onClick={() => setPage((current) => current - 1)}
            >
              Previous
            </button>
            <span>Page {page + 1} of {Math.max(totalPages, 1)}</span>
            <button
              type="button"
              className="secondary-button"
              disabled={totalPages === 0 || page + 1 >= totalPages}
              onClick={() => setPage((current) => current + 1)}
            >
              Next
            </button>
          </div>
        </>
      )}
    </section>
  );
}

export default ProductListPage;
