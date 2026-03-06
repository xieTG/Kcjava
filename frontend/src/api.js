const API_BASE = "/api";

async function request(path, { method = "GET", token, headers, body } = {}) {
  const res = await fetch(`${API_BASE}${path}`, {
    method,
    headers: {
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
      ...(body && !(body instanceof FormData) ? { "Content-Type": "application/json" } : {}),
      ...(headers || {}),
    },
    body: body
      ? body instanceof FormData
        ? body
        : JSON.stringify(body)
      : undefined,
  });

  const contentType = res.headers.get("content-type") || "";
  const isJson = contentType.includes("application/json") || contentType.includes("application/problem+json");

  const payload = isJson ? await res.json().catch(() => null) : await res.text().catch(() => null);

  if (!res.ok) {
    // throw structured info React can use
    const err = new Error(payload?.detail || payload || `HTTP ${res.status}`);
    err.status = res.status;
    err.code = payload?.code;
    err.params = payload?.params;
    err.problem = payload;
    throw err;
  }

  return payload;
}

// --- NEW: binary download helper (Blob) ---
function extractFilenameFromContentDisposition(contentDisposition) {
  if (!contentDisposition) return null;

  // supports: filename="x.xlsx" and filename*=UTF-8''x.xlsx
  const match = contentDisposition.match(/filename\*?=(?:UTF-8'')?["']?([^"';]+)["']?/i);
  if (!match?.[1]) return null;

  try {
    return decodeURIComponent(match[1]);
  } catch {
    return match[1];
  }
}

async function downloadAsFile(
  path,
  { token, method = "GET", headers, defaultFilename = "download.xlsx" } = {}
) {
  const res = await fetch(`${API_BASE}${path}`, {
    method,
    headers: {
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
      ...(headers || {}),
    },
  });

  // ✅ If error: read body as JSON (problem+json) or text, then throw
  if (!res.ok) {
    const contentType = res.headers.get("content-type") || "";
    const isJson =
      contentType.includes("application/json") ||
      contentType.includes("application/problem+json");

    const payload = isJson
      ? await res.json().catch(() => null)
      : await res.text().catch(() => null);

    const err = new Error(payload?.detail || payload || `HTTP ${res.status}`);
    err.status = res.status;
    err.code = payload?.code;
    err.params = payload?.params;
    err.problem = payload;
    throw err;
  }

  // ✅ Success: read as Blob ONCE
  const blob = await res.blob();

  const cd = res.headers.get("content-disposition");
  const filename = extractFilenameFromContentDisposition(cd) || defaultFilename;

  const url = window.URL.createObjectURL(blob);
  const a = document.createElement("a");
  a.href = url;
  a.download = filename;
  document.body.appendChild(a);
  a.click();
  a.remove();
  window.URL.revokeObjectURL(url);

  return { filename };
}




// Specific endpoints (nice for the console)
export function apiHealth() {
  return request("/health");
}

export function apiLogin(email, password) {
  return request("/auth/login", { method: "POST", body: { email, password } });
}

//Return the list of LC for the dropdown menu in the submission form
export function apiListLC(token) {
  return request("/lcs", { token }); 
}

//Create a new LC with the name and description provided in the form
export function apiCreateLC(token, { name, year, description }) {
  return request("/lcs", { method: "POST", token, body: { name, year, description } });
}

// Return the list of questionnaires
export function apiListQuestionnaires(token) {
  return request("/questionnaires", { token });
}

// Return the list of submissions for the logged in user
export function apiMyUploads(token) {
  return request("/submissions", { token });
}

// Upload a questionnaire file for a specific LC
export function apiUploadLCQuestionnaire(lcID, file, token) {
  const fd = new FormData();
  fd.append("file", file);
  return request(`/lcs/${lcID}/submissions`, {
    method: "POST",
    token,
    body: fd,
  });
}

// Download a questionnaire template (Excel) for a specific LC
export function apiDownloadLCQuestionnaire(lcId, token) {
  return downloadAsFile(`/lcs/${lcId}/xlsx`, {
    token,
    defaultFilename: `template-${lcId}.xlsx`,
  });
}

// For the “Custom request” box
export function apiCustom(path, { method, token, jsonBody } = {}) {
  return request(path.startsWith("/") ? path : `/${path}`, {
    method,
    token,
    body: jsonBody,
  });
}