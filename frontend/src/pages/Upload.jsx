import React, { useEffect, useState } from "react";
import { listQuestionnaires, uploadSubmission, mySubmissions, templateURL } from "../api.js";

export default function Upload({ token }) {
  const [questionnaires, setQuestionnaires] = useState([]);
  const [qid, setQid] = useState("");
  const [file, setFile] = useState(null);
  const [msg, setMsg] = useState("");
  const [subs, setSubs] = useState([]);

  async function refresh() {
    const qs = await listQuestionnaires();
    setQuestionnaires(qs);
    if (!qid && qs.length) setQid(qs[0].id);
    const s = await mySubmissions(token);
    setSubs(s);
  }

  useEffect(() => { refresh().catch(console.error); }, []);

  async function submit() {
    setMsg("");
    if (!qid) return setMsg("Choisis un questionnaire");
    if (!file) return setMsg("Choisis un fichier .xlsx");
    try {
      const res = await uploadSubmission(qid, file, token);
      setMsg(`OK: submission_id=${res.submission_id} status=${res.status}`);
      await refresh();
    } catch (e) {
      setMsg(String(e));
    }
  }

  return (
    <div style={{ display: "grid", gap: 12 }}>
      <h2>Upload Answers Excel</h2>
      <label>
        Questionnaire
        <select value={qid} onChange={(e) => setQid(e.target.value)} style={{ marginLeft: 8 }}>
          {questionnaires.map((q) => (
            <option key={q.id} value={q.id}>{q.name} v{q.version}</option>
          ))}
        </select>
      </label>

	  {qid ? (
  <a
    href={`/api/questionnaires/${qid}/template`}
    target="_blank"
    rel="noreferrer"
    style={{
      display: "inline-block",
      padding: "8px 12px",
      border: "1px solid #333",
      borderRadius: 8,
      textDecoration: "none",
      color: "inherit",
      width: "fit-content",
    }}
  >
    Download Excel Questionnaire Template
  </a>
) : (
  <button disabled>Download Excel Questionnaire Template</button>
)}


      <input type="file" accept=".xlsx" onChange={(e) => setFile(e.target.files?.[0] || null)} />
      <button onClick={submit}>Uploader</button>

      {msg && <pre style={{ whiteSpace: "pre-wrap" }}>{msg}</pre>}

      <h3>My Answers</h3>
      <div style={{ display: "grid", gap: 8 }}>
        {subs.map((s) => (
          <div key={s.id} style={{ border: "1px solid #ddd", padding: 10, borderRadius: 8 }}>
            <div><b>{s.id}</b></div>
            <div>Status: {s.status}</div>
            <div>Date: {s.submitted_at}</div>
            {s.error && <pre style={{ color: "crimson", whiteSpace: "pre-wrap" }}>{JSON.stringify(s.error, null, 2)}</pre>}
          </div>
        ))}
        {!subs.length && <div style={{ opacity: 0.7 }}>No Answers Yet.</div>}
      </div>

      <hr />
      <p style={{ opacity: 0.8 }}>
        ⚠️ L’Excel doit avoir un onglet <b>RESPONSES</b> avec colonnes <b>question_id</b> et <b>answer</b>.
      </p>
    </div>
  );
}
