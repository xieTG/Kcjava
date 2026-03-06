import React, { useEffect, useMemo, useState } from "react";
import {
  apiHealth,
  apiMyUploads,
  apiUploadLCQuestionnaire,
  apiCustom,
  apiListLC,
  apiCreateLC,
  apiDownloadLCQuestionnaire
} from "../api.js";

function Card({ title, children }) {
  return (
    <div style={{ border: "1px solid #ddd", borderRadius: 8, padding: 12, marginBottom: 12 }}>
      <h3 style={{ marginTop: 0 }}>{title}</h3>
      {children}
    </div>
  );
}

export default function ApiConsole({ token, setToast }) {
  const [health, setHealth] = useState(null);
  const [ListLC, setListLC] = useState([]);
  const [CreateLC, setCreateLC] = useState(null);
  const [LCDescription, setLCDescription] = useState("");
  const [LCName, setLCName] = useState("");
  const [LCYear, setLCYear] = useState("");
  const [LCid, setLCid] = useState("");
  const [file, setFile] = useState(null);
  const [submissionResult, setSubmissionResult] = useState(null);

  const [submissions, setSubmissions] = useState(null);
  const [downloadResult, setDownloadResult] = useState(null);

  const [out, setOut] = useState("");

  // Custom request
  const [method, setMethod] = useState("GET");
  const [path, setPath] = useState("/health");
  const [jsonBodyText, setJsonBodyText] = useState("");

  function pretty(x) {
    if (typeof x === "string") {
      try {
        return JSON.stringify(JSON.parse(x), null, 2);
      } catch {
        return x; // not JSON, return as-is
      }
    }
    return JSON.stringify(x, null, 2);
  }

  return (
    <div style={{ display: "grid", gap: 12 }}>

      {/* Health API to check the status of the backend */}
      <Card title="GET /health">
        <button
          onClick={async () => {
            try {
              const res = await apiHealth();
              setHealth(res);
              setOut(pretty(res));
            } catch (e) {
              setToast(e.message);
            }
          }}
        >
          Call /health
        </button>
        {health && <pre>{pretty(health)}</pre>}
      </Card>

      {/* ListLC API to get the list of LC*/}
      <Card title="GET /ListLC">
        <button
          onClick={async () => {
            try {
              const res = await apiListLC(token);
              setListLC(res)
              setOut(pretty(res));
            } catch (e) {
              setToast(e.message);
            }
          }}
        >
          Call /ListLC
        </button>
        {ListLC && <pre style={{ whiteSpace: "pre-wrap" }}>{pretty(ListLC)}</pre>}




      </Card>

      {/* CreateLC API to create a new LC */}

      <Card title="POST /CreateLC">
        <div>
          Name:{" "}
          <input
            value={LCName}
            onChange={(e) => setLCName(e.target.value)}
            placeholder="Name"
            style={{ width: "100%" }}
          />
        </div>
        <div>
          Year:{" "}
          <input
            type="number"
            value={LCYear}
            onChange={(e) => setLCYear(e.target.value)}
            placeholder="Year"
            style={{ width: "100%" }}
          />
        </div>
        <div style={{ marginTop: 8 }}>
          Description:{" "}
          <textarea
            value={LCDescription}
            onChange={(e) => setLCDescription(e.target.value)}
            placeholder="Description"
            rows={6}
            style={{ width: "100%", fontFamily: "monospace" }}
          />
        </div>
        <button
          style={{ marginTop: 12 }}
          onClick={async () => {
            try {
    const yearInt = Number.parseInt(LCYear, 10);

    if (Number.isNaN(yearInt)) {
      setToast("Year must be an integer", "error");
      return;
    }

    const res = await apiCreateLC(token, {
      name: LCName,
      year: yearInt,
      description: LCDescription,
    });

    setCreateLC(res);
    setOut(pretty(res));
    const listRes = await apiListLC(token);
    setListLC(listRes);
  } catch (e) {
    setToast(e.message, "error");
  }
          }}
        >
          Call /CreateLC
        </button>

        {CreateLC && (<pre style={{ whiteSpace: "pre-wrap" }}>{pretty(CreateLC)}</pre>)}
      </Card>


      {/* Upload LC Questionnaire API */}
      <Card title="POST /questionnaires/{ID}/submissions (upload .xlsx)">
        <div style={{ display: "grid", gap: 8 }}>
          <input type="file" accept=".xlsx" onChange={(e) => setFile(e.target.files?.[0] || null)} />


          {ListLC && (
            <select
              value={LCid}
              onChange={(e) => setLCid(e.target.value)}
            >
              <option value="">-- Select an LC --</option>
              {ListLC.map(item => (
                <option key={item.id} value={item.id}>
                  {item.name}
                </option>
              ))}
            </select>
          )}


          <button
            onClick={async () => {
              try {
                if (!LCid) throw new Error("Select a LC first");
                if (!file) throw new Error("Select an .xlsx file");
                const res = await apiUploadLCQuestionnaire(LCid, file, token);
                setSubmissionResult(res);
                setOut(pretty(res));
              } catch (e) {
                setToast(e.message);
              }
            }}
          >
            Call /questionnaires/{LCid}/submissions
          </button>
          {submissionResult && (<pre style={{ whiteSpace: "pre-wrap" }}>{pretty(submissionResult)}</pre>)}
        </div>
      </Card>

      <Card title="GET /me/submissions">
        <button
          onClick={async () => {
            try {
              const res = await apiMyUploads(token);
              setSubmissions(res);
              setOut(pretty(res));
            } catch (e) {
              setToast(e.message);
            }
          }}
        >
          Call
        </button>
        {submissions && <pre>{pretty(submissions)}</pre>}
      </Card>

      <Card title="GET /questionnaires/{lcId}/template">
        {ListLC && (
          <select
            value={LCid}
            onChange={(e) => setLCid(e.target.value)}
          >
            <option value="">-- Select an LC --</option>
            {ListLC.map(item => (
              <option key={item.id} value={item.id}>
                {item.name}
              </option>
            ))}
          </select>
        )}
        <button
          onClick={async () => {
            try {
              const { filename } = await apiDownloadLCQuestionnaire(LCid, token);
              setDownloadResult({ filename });
              setToast(`Download started: ${filename}`, "success", 2000);
              setOut(filename);
            } catch (e) {
              setOut(e.problem ? pretty(e.problem) : (e.message || String(e)));
              setToast(e.problem ? pretty(e.problem) : (e.message || String(e)), "error");
            }
          }}
        >
          Call
        </button>
        {downloadResult && <pre>{pretty(downloadResult)}</pre>}
      </Card>

      <Card title="Custom request (quick debug)">
        <div style={{ display: "grid", gap: 8 }}>
          <div style={{ display: "flex", gap: 8 }}>
            <select value={method} onChange={(e) => setMethod(e.target.value)}>
              {["GET", "POST", "PUT", "PATCH", "DELETE"].map((m) => (
                <option key={m} value={m}>{m}</option>
              ))}
            </select>
            <input
              value={path}
              onChange={(e) => setPath(e.target.value)}
              style={{ flex: 1 }}
              placeholder="/questionnaires"
            />
          </div>

          <textarea
            value={jsonBodyText}
            onChange={(e) => setJsonBodyText(e.target.value)}
            placeholder='JSON body (optional), e.g. {"email":"...","password":"..."}'
            rows={6}
            style={{ width: "100%", fontFamily: "monospace" }}
          />

          <button
            onClick={async () => {
              try {
                const jsonBody = jsonBodyText.trim() ? JSON.parse(jsonBodyText) : undefined;
                const res = await apiCustom(path, { method, token, jsonBody });
                setOut(pretty(res));
              } catch (e) {
                setOut(pretty(e.messages));
                setToast(pretty(e.message));
              }
            }}
          >
            Send
          </button>
        </div>
      </Card>

      <Card title="Last output">
        {out && <pre style={{ whiteSpace: "pre-wrap" }}>{out}</pre>}




      </Card>
    </div>
  );
}