import React, { useEffect, useState } from "react";
import Login from "./pages/Login.jsx";
import ApiConsole from "./pages/ApiConsole.jsx";

function Toast({ toast, onClose }) {
  if (!toast) return null;

  const title =
    toast.type === "error" ? "Error" : toast.type === "success" ? "Success" : "Info";

  return (
    <div
      role="status"
      aria-live="polite"
      style={{
        position: "fixed",
        bottom: 16,
        right: 16,
        zIndex: 9999,
        maxWidth: 420,
        padding: 12,
        borderRadius: 10,
        background: "white",
        boxShadow: "0 8px 24px rgba(0,0,0,0.18)",
      }}
    >
      <div style={{ display: "flex", justifyContent: "space-between", gap: 12 }}>
        <div style={{ minWidth: 0 }}>
          <div style={{ fontWeight: 700, marginBottom: 4 }}>{title}</div>
          <div style={{ wordBreak: "break-word" }}>{toast.message}</div>
        </div>
        <button
          onClick={onClose}
          style={{ border: "none", background: "transparent", cursor: "pointer", fontSize: 16 }}
          aria-label="Close"
        >
          ✕
        </button>
      </div>
    </div>
  );
}

export default function App() {
  const [token, setToken] = useState(localStorage.getItem("token") || "");
  const [role, setRole] = useState(localStorage.getItem("role") || "");

  // ✅ Option 1 toast state
  const [toast, setToastState] = useState(null);

  // ✅ The function children will call
  function setToast(input, type = "error", timeoutMs = 4000) {
  const message =
    typeof input === "string"
      ? input
      : input instanceof Error
      ? input.message
      : input?.detail || input?.message
      ? (input.detail || input.message)
      : JSON.stringify(input, null, 2);

  setToastState({ message, type, timeoutMs });
}

  // auto-hide
  useEffect(() => {
    if (!toast) return;
    const id = setTimeout(() => setToastState(null), toast.timeoutMs ?? 4000);
    return () => clearTimeout(id);
  }, [toast]);

  function onLogin(t, r) {
    setToken(t);
    setRole(r);
    localStorage.setItem("token", t);
    localStorage.setItem("role", r);
    setToast("Logged in.", "success", 2000);
  }

  function logout() {
    setToken("");
    setRole("");
    localStorage.removeItem("token");
    localStorage.removeItem("role");
    setToast("Logged out.", "info", 2000);
  }

  return (
    <div style={{ maxWidth: 900, margin: "24px auto", padding: 16, fontFamily: "system-ui" }}>
      <h1>Questionnaire MVP</h1>

      {!token ? (
        <Login onLogin={onLogin} setToast={setToast} />
      ) : (
        <>
          <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center" }}>
            <div>Connecté: <b>{role}</b></div>
            <button onClick={logout}>Logout</button>
          </div>

          <hr style={{ margin: "16px 0" }} />

          <ApiConsole token={token} setToast={setToast} />
        </>
      )}

      <Toast toast={toast} onClose={() => setToastState(null)} />
    </div>
  );
}