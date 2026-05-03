import AppRoutes from "./routes/AppRoutes.jsx";
import Header from "./components/Header.jsx";

function App() {
  return (
    <div className="app-shell">
      <Header />
      <main className="page-shell">
        <AppRoutes />
      </main>
    </div>
  );
}

export default App;
