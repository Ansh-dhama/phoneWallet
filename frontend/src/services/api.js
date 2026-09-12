import axios from 'axios';
import { AUTH_STORAGE_KEY, clearStoredAuth, extractRole, getStoredAuth, persistAuth } from '../utils/auth';

export const API_BASE_URL = (import.meta.env?.VITE_API_BASE_URL ?? '').trim();

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: { 'Content-Type': 'application/json' },
  timeout: 15000,
});

let refreshPromise = null;

api.interceptors.request.use((config) => {
  const auth = getStoredAuth();
  if (auth?.accessToken && !config.headers.Authorization) {
    config.headers.Authorization = `Bearer ${auth.accessToken}`;
  }
  return config;
});

api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const original = error.config;
    const isRefreshRequest = original?.url?.includes('/api/auth/refresh-token');

    if (error.response?.status !== 401 || original?._retry || isRefreshRequest) {
      return Promise.reject(error);
    }

    const auth = getStoredAuth();
    if (!auth?.refreshToken) {
      clearStoredAuth();
      return Promise.reject(error);
    }

    original._retry = true;

    try {
      refreshPromise ??= axios
        .post(`${API_BASE_URL}/api/auth/refresh-token`, { refreshToken: auth.refreshToken })
        .then(({ data }) => {
          const nextAuth = {
            ...auth,
            accessToken: data.accessToken,
            refreshToken: data.refreshToken || auth.refreshToken,
            userId: data.userId ?? auth.userId,
            role: extractRole(data.accessToken),
          };
          persistAuth(nextAuth);
          window.dispatchEvent(new StorageEvent('storage', { key: AUTH_STORAGE_KEY }));
          return nextAuth;
        })
        .finally(() => {
          refreshPromise = null;
        });

      const nextAuth = await refreshPromise;
      original.headers.Authorization = `Bearer ${nextAuth.accessToken}`;
      return api(original);
    } catch (refreshError) {
      clearStoredAuth();
      window.location.assign('/login');
      return Promise.reject(refreshError);
    }
  },
);

export function apiErrorMessage(error) {
  return (
    error?.response?.data?.message ||
    (typeof error?.response?.data === 'string' ? error.response.data : null) ||
    error?.message ||
    'Something went wrong. Please try again.'
  );
}

export default api;
