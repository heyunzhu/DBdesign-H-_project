const API_BASE = 'http://localhost:8081/api'

async function request(path, options = {}) {
  const token = localStorage.getItem('library_token')
  const response = await fetch(`${API_BASE}${path}`, {
    headers: {
      'Content-Type': 'application/json',
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
      ...(options.headers || {})
    },
    ...options
  })

  const payload = await response.json().catch(() => null)
  if (!response.ok) {
    throw new Error(payload?.message || `HTTP ${response.status}`)
  }
  if (payload && payload.code !== 200) {
    throw new Error(payload.message || '请求失败')
  }
  return payload?.data
}

export const api = {
  get: (path) => request(path),
  post: (path, data) => request(path, { method: 'POST', body: JSON.stringify(data) }),
  put: (path, data) => request(path, { method: 'PUT', body: JSON.stringify(data ?? {}) }),
  delete: (path) => request(path, { method: 'DELETE' }),
  setToken: (token) => localStorage.setItem('library_token', token),
  clearToken: () => localStorage.removeItem('library_token')
}
