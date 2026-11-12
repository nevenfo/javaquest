// Client de l'API locale. Le jeton de session arrive dans le fragment d'URL (#t=...) puis est conservé
// dans sessionStorage (jamais envoyé en Referer ni conservé après fermeture de l'onglet).
let token = null;

export function initToken() {
  const m = location.hash.match(/t=([0-9a-f]+)/);
  if (m) {
    token = m[1];
    try { sessionStorage.setItem('jq-token', token); } catch { /* stockage indisponible */ }
    history.replaceState(null, '', location.pathname);
  } else {
    try { token = sessionStorage.getItem('jq-token'); } catch { token = null; }
  }
  return token;
}

async function call(method, path, body) {
  const res = await fetch(path, {
    method,
    headers: { 'X-JQ-Token': token || '', ...(body !== undefined ? { 'Content-Type': 'application/json' } : {}) },
    body: body !== undefined ? JSON.stringify(body) : undefined,
  });
  let data = null;
  try { data = await res.json(); } catch { data = null; }
  if (!res.ok) {
    const err = new Error((data && data.error) || `HTTP ${res.status}`);
    err.status = res.status;
    throw err;
  }
  return data;
}

export const api = {
  ping: () => call('GET', '/api/ping'),
  quit: () => call('POST', '/api/quit', {}),
  health: () => call('GET', '/api/health'),
  content: () => call('GET', '/api/content'),
  profiles: () => call('GET', '/api/profiles'),
  createProfile: (name) => call('POST', '/api/profiles', { name }),
  deleteProfile: (id) => call('POST', '/api/profiles/delete', { id }),
  load: (id) => call('GET', `/api/save?profile=${encodeURIComponent(id)}`),
  save: (id, data) => call('PUT', `/api/save?profile=${encodeURIComponent(id)}`, data),
  saveKeepalive: (id, data) => fetch(`/api/save?profile=${encodeURIComponent(id)}`, { method: 'PUT', keepalive: true,
    headers: { 'X-JQ-Token': token || '', 'Content-Type': 'application/json' }, body: JSON.stringify(data) }),
  run: (quest, files, stdin, args) => call('POST', '/api/run', { quest, files, stdin, args }),
  check: (quest, files, extra = {}) => call('POST', '/api/check', { quest, files, ...extra }),
};
