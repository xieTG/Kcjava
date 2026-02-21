import React, { useState } from "react";
import Login from "./pages/Login.jsx";
import Upload from "./pages/Upload.jsx";

export default function App() {
  const [token, setToken] = useState(localStorage.getItem("token") || "");
  const [role, setRole] = useState(localStorage.getItem("role") || "");

  function onLogin(t, r) {
    setToken(t);
    setRole(r);
    localStorage.setItem("token", t);
    localStorage.setItem("role", r);
  }

  function logout() {
    setToken("");
    setRole("");
    localStorage.removeItem("token");
    localStorage.removeItem("role");
  }

  return (
    <div style={{ fontFamily: "system-ui", maxWidth: 900, margin: "40px auto", padding: 16 }}>
      <h1>Questionnaire MVP</h1>
      {!token ? (
        <Login onLogin={onLogin} />
      ) : (
        <>
          <div style={{ display: "flex", gap: 12, alignItems: "center" }}>
            <div>Connecté: <b>{role}</b></div>
            <button onClick={logout}>Logout</button>
          </div>
          <hr />
          <Upload token={token} />
        </>
      )}
    </div>
  );
}
