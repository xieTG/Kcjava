import React, { useState } from "react";
import Login from "./pages/Login.jsx";
import ApiConsole from "./pages/ApiConsole.jsx";

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
    <div style={{ maxWidth: 900, margin: "24px auto", padding: 16, fontFamily: "system-ui" }}>
      <h1>Questionnaire MVP</h1>

      {!token ? (
        <Login onLogin={onLogin} />
      ) : (
        <>
          <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center" }}>
            <div>Connecté: <b>{role}</b></div>
            <button onClick={logout}>Logout</button>
          </div>

          <hr style={{ margin: "16px 0" }} />

          <ApiConsole token={token} />
        </>
      )}
    </div>
  );
}