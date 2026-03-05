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

  const text = await res.text();
  if (!res.ok) throw new Error(text || `HTTP ${res.status}`);

  // try json, fallback to text
  try {
    return text ? JSON.parse(text) : null;
  } catch {
    return text;
  }
}

export function templateUrl(questionnaireId) {
  return `${API_BASE}/questionnaires/${questionnaireId}/template`;
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
  return request("/LC", { token }); 
}

//Create a new LC with the name and description provided in the form
export function apiCreateLC(token, { name, description }) {
  return request("/LC", { method: "POST", token, body: { name, description } });
}

// Return the list of questionnaires
export function apiListQuestionnaires(token) {
  return request("/questionnaires", { token });
}

// Return the list of submissions for the logged in user
export function apiMySubmissions(token) {
  return request("/me/submissions", { token });
}

// Upload a submission file for a specific questionnaire
export function apiUploadSubmission(questionnaireId, file, token) {
  const fd = new FormData();
  fd.append("file", file);
  return request(`/questionnaires/${questionnaireId}/submissions`, {
    method: "POST",
    token,
    body: fd,
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