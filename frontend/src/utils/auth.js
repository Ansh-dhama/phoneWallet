export const AUTH_STORAGE_KEY = 'phonewallet.auth';

export function decodeJwtPayload(token) {
  try {
    if (!token) return null;
    const payload = token.split('.')[1];
    if (!payload) return null;
    const normalized = payload.replace(/-/g, '+').replace(/_/g, '/');
    const decoded = decodeURIComponent(
      window
        .atob(normalized)
        .split('')
        .map((char) => `%${`00${char.charCodeAt(0).toString(16)}`.slice(-2)}`)
        .join(''),
    );
    return JSON.parse(decoded);
  } catch {
    return null;
  }
}

export function extractRole(token) {
  const payload = decodeJwtPayload(token);
  if (!payload) return 'USER';

  const raw =
    payload.role ??
    payload.roles ??
    payload.authorities ??
    payload.scope ??
    payload.scopes ??
    'USER';

  const values = Array.isArray(raw) ? raw : String(raw).split(/[ ,]/).filter(Boolean);
  const normalized = values.map((value) => String(value).replace(/^ROLE_/, '').toUpperCase());

  if (normalized.includes('ADMIN')) return 'ADMIN';
  if (normalized.includes('MERCHANT')) return 'MERCHANT';
  return normalized[0] || 'USER';
}

export function getStoredAuth() {
  try {
    const raw = localStorage.getItem(AUTH_STORAGE_KEY);
    return raw ? JSON.parse(raw) : null;
  } catch {
    return null;
  }
}

export function persistAuth(auth) {
  localStorage.setItem(AUTH_STORAGE_KEY, JSON.stringify(auth));
}

export function clearStoredAuth() {
  localStorage.removeItem(AUTH_STORAGE_KEY);
}

export function walletStorageKey(userId) {
  return `phonewallet.wallet.${userId || 'anonymous'}`;
}

export function getStoredWalletId(userId) {
  return localStorage.getItem(walletStorageKey(userId));
}

export function persistWalletId(userId, walletId) {
  localStorage.setItem(walletStorageKey(userId), String(walletId));
}

export function clearStoredWalletId(userId) {
  localStorage.removeItem(walletStorageKey(userId));
}
