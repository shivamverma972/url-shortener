const API_BASE = '';

// ── TOKEN HELPERS ──────────────────────────────────────────
function getToken() {
    return localStorage.getItem('token');
}

function saveToken(token) {
    localStorage.setItem('token', token);
}

function saveUser(user) {
    localStorage.setItem('user', JSON.stringify(user));
}

function getUser() {
    const u = localStorage.getItem('user');
    return u ? JSON.parse(u) : null;
}

function logout() {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    window.location.href = 'login.html';
}

function requireAuth() {
    if (!getToken()) {
        window.location.href = 'login.html';
    }
}

// ── API HELPERS ────────────────────────────────────────────
async function apiCall(endpoint, method = 'GET', body = null) {
    const headers = { 'Content-Type': 'application/json' };
    const token = getToken();
    if (token) headers['Authorization'] = `Bearer ${token}`;

    const options = { method, headers };
    if (body) options.body = JSON.stringify(body);

    const response = await fetch(`${API_BASE}${endpoint}`, options);
    return response;
}

// ── SHOW ALERT ─────────────────────────────────────────────
function showAlert(id, message, type = 'error') {
    const el = document.getElementById(id);
    if (!el) return;
    el.textContent = message;
    el.className = `alert alert-${type}`;
    el.style.display = 'block';
    setTimeout(() => el.style.display = 'none', 4000);
}

// ── AUTH FUNCTIONS ─────────────────────────────────────────
async function register(name, email, password) {
    const res = await apiCall('/api/auth/register', 'POST',
            { name, email, password });
    const data = await res.json();

    if (res.ok) {
        saveToken(data.token);
        saveUser({ name: data.name, email: data.email });
        window.location.href = 'dashboard.html';
    } else {
        showAlert('auth-alert', data.error || 'Registration failed');
    }
}

async function login(email, password) {
    const res = await apiCall('/api/auth/login', 'POST',
            { email, password });
    const data = await res.json();

    if (res.ok) {
        saveToken(data.token);
        saveUser({ name: data.name, email: data.email });
        window.location.href = 'dashboard.html';
    } else {
        showAlert('auth-alert', data.error || 'Invalid credentials');
    }
}

// ── URL FUNCTIONS ──────────────────────────────────────────
async function shortenUrl(originalUrl) {
    const res = await apiCall('/api/urls', 'POST', { originalUrl });
    const data = await res.json();

    if (res.ok) {
        return data;
    } else if (res.status === 403) {
        window.location.href = 'login.html';
    } else {
        throw new Error(data.error || 'Failed to shorten URL');
    }
}

async function getMyUrls() {
    const res = await apiCall('/api/urls/my');
    if (res.ok) return await res.json();
    if (res.status === 403) window.location.href = 'login.html';
    return [];
}

async function deleteUrl(id) {
    const res = await apiCall(`/api/urls/${id}`, 'DELETE');
    return res.ok;
}

// ── COPY TO CLIPBOARD ──────────────────────────────────────
function copyToClipboard(text, btn) {
    navigator.clipboard.writeText(text).then(() => {
        const original = btn.textContent;
        btn.textContent = 'Copied!';
        setTimeout(() => btn.textContent = original, 2000);
    });
}