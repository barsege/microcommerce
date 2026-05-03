import { useState } from "react";
import { useAuth } from "../context/AuthContext.jsx";

function TokenPage() {
  const { forgotPassword, token, saveManualToken, logout, isAuthenticated } = useAuth();
  const [draftToken, setDraftToken] = useState(token || "");
  const [message, setMessage] = useState("");

  function handleSubmit(event) {
    event.preventDefault();
    saveManualToken(draftToken);
    setMessage("Token saved.");
  }

  function handleLogout() {
    logout();
    setDraftToken("");
    setMessage("Token removed.");
  }

  return (
    <section className="token-page">
      <div className="page-heading">
        <div>
          <p className="eyebrow">JWT</p>
          <h1>Access token</h1>
        </div>
      </div>

      <form className="content-panel token-form" onSubmit={handleSubmit}>
        <div className="state-box">
          Bu sayfa manuel token testleri için bırakılmıştır. Normal kullanım için Header'daki Login butonunu kullanın.
        </div>
        <label htmlFor="jwt">Paste JWT token</label>
        <textarea
          id="jwt"
          rows="8"
          value={draftToken}
          placeholder="eyJhbGciOi..."
          onChange={(event) => setDraftToken(event.target.value)}
        />
        <div className="actions">
          <button className="primary-button" type="submit" disabled={!draftToken.trim()}>
            Save token
          </button>
          <button className="ghost-button" type="button" onClick={forgotPassword}>
            Şifremi unuttum
          </button>
          <button className="secondary-button" type="button" onClick={handleLogout} disabled={!isAuthenticated}>
            Logout
          </button>
        </div>
        {message && <div className="state-box success-box">{message}</div>}
      </form>
    </section>
  );
}

export default TokenPage;
