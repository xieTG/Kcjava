const API_BASE = "/api";

export async function login(email, password) {
  const res = await fetch(`${API_BASE}/auth/login`, {
    method: "POST",
    headers: {"Content-Type": "application/json"},
    body: JSON.stringify({ email, password }),
  });
  if (!res.ok) throw new Error(await res.text());
  return res.json();
}

export async function listQuestionnaires() {
  const res = await fetch(`${API_BASE}/questionnaires`);
  if (!res.ok) throw new Error(await res.text());
  return res.json();
}

export async function uploadSubmission(qid, file, token) {
  const fd = new FormData();
  fd.append("file", file);
  const res = await fetch(`${API_BASE}/questionnaires/${qid}/submissions`, {
    method: "POST",
    headers: { Authorization: `Bearer ${token}` },
    body: fd,
  });
  if (!res.ok) throw new Error(await res.text());
  return res.json();
}

export async function mySubmissions(token) {
  const res = await fetch(`${API_BASE}/me/submissions`, {
    headers: { Authorization: `Bearer ${token}` },
  });
  if (!res.ok) throw new Error(await res.text());
  return res.json();
}

export function templateUrl(qid) {
  return `${API_BASE}/questionnaires/${qid}/template`;
}

