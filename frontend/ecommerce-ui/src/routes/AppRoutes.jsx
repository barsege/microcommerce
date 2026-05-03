import { Navigate, Route, Routes } from "react-router-dom";
import ProductListPage from "../pages/ProductListPage.jsx";
import ProductDetailPage from "../pages/ProductDetailPage.jsx";
import CartPage from "../pages/CartPage.jsx";
import OrdersPage from "../pages/OrdersPage.jsx";
import TokenPage from "../pages/TokenPage.jsx";
import AdminPage from "../pages/AdminPage.jsx";
import AdminRoute from "./AdminRoute.jsx";

function AppRoutes() {
  return (
    <Routes>
      <Route path="/" element={<Navigate to="/products" replace />} />
      <Route path="/products" element={<ProductListPage />} />
      <Route path="/products/:id" element={<ProductDetailPage />} />
      <Route path="/cart" element={<CartPage />} />
      <Route path="/orders" element={<OrdersPage />} />
      <Route path="/admin" element={<AdminRoute><AdminPage /></AdminRoute>} />
      <Route path="/token" element={<TokenPage />} />
      <Route path="*" element={<Navigate to="/products" replace />} />
    </Routes>
  );
}

export default AppRoutes;
