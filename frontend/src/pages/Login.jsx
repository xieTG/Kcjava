import React, { useState } from "react";
import { login } from "../api.js";

export default function Login({ onLogin }) {
  const [email, setEmail] = useState("admin@example.com");
  const [password, setPassword] = useState("admin123");
  const [err, setErr] = useState("");

  async function submit(e) {
    e.preventDefault();
    setErr("");
    try {
      const data = await login(email, password);
      onLogin(data.access_token, data.role);
    } catch (e) {
      setErr(String(e));
    }
  }

  return (
    <form onSubmit={submit} style={{ display: "grid", gap: 10, maxWidth: 420 }}>
      <label>
        Email
        <input value={email} onChange={(e) => setEmail(e.target.value)} style={{ width: "100%" }} />
      </label>
      <label>
        Mot de passe
        <input type="password" value={password} onChange={(e) => setPassword(e.target.value)} style={{ width: "100%" }} />
      </label>
      <button type="submit">Login</button>
      {err && <pre style={{ color: "crimson", whiteSpace: "pre-wrap" }}>{err}</pre>}
      <p style={{ opacity: 0.8 }}>
        Par défaut (dev): admin@example.com / admin123 (configurable via .env)
      </p>
    </form>
  );
}
