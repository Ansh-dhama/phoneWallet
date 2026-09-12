import api from './api';

export function pageContent(data) {
  return Array.isArray(data) ? data : Array.isArray(data?.content) ? data.content : [];
}

export function pageMeta(data) {
  if (!data || Array.isArray(data)) {
    const size = Array.isArray(data) ? data.length : 0;
    return { page: 0, size, totalElements: size, totalPages: size ? 1 : 0, first: true, last: true };
  }
  return {
    page: Number(data.page ?? 0),
    size: Number(data.size ?? 0),
    totalElements: Number(data.totalElements ?? pageContent(data).length),
    totalPages: Number(data.totalPages ?? 0),
    first: Boolean(data.first),
    last: Boolean(data.last),
  };
}

export const authApi = {
  login: (payload) => api.post('/api/auth/login', payload).then((r) => r.data),
  signup: (payload) => api.post('/api/auth/signup', payload).then((r) => r.data),
  logout: (refreshToken) => api.post('/api/auth/logout', { refreshToken }).then((r) => r.data),
};

export const walletApi = {
  create: (payload) => api.post('/api/wallets', payload).then((r) => r.data),
  mine: () => api.get('/api/wallets/mine').then((r) => r.data),
  get: (walletId) => api.get(`/api/wallets/${walletId}`).then((r) => r.data),
  balance: (walletId) => api.get(`/api/wallets/${walletId}/balance`).then((r) => r.data),
  freeze: (walletId) => api.post(`/api/wallets/${walletId}/freeze`).then((r) => r.data),
  unfreeze: (walletId) => api.post(`/api/wallets/${walletId}/unfreeze`).then((r) => r.data),
  blacklist: (walletId, reason) => api.post(`/api/wallets/${walletId}/blacklist`, { reason }).then((r) => r.data),
  unblacklist: (walletId) => api.post(`/api/wallets/${walletId}/unblacklist`).then((r) => r.data),
};

export const topUpApi = {
  initiate: (walletId, payload) => api.post(`/api/topups/wallet/${walletId}`, payload).then((r) => r.data),
  completeDemo: (intentId) => api.post(`/api/topups/${intentId}/demo-complete`).then((r) => r.data),
  byWallet: (walletId, page = 0, size = 20) => api.get(`/api/topups/wallet/${walletId}`, { params: { page, size } }).then((r) => r.data),
};

export const transactionApi = {
  transfer: (payload) => api.post('/api/transactions/transfer', payload).then((r) => r.data),
  pay: (payload) => api.post('/api/transactions/pay', payload).then((r) => r.data),
  refund: (payload) => api.post('/api/transactions/refund', payload).then((r) => r.data),
  reversal: (payload) => api.post('/api/transactions/reversal', payload).then((r) => r.data),
  get: (transactionId) => api.get(`/api/transactions/${transactionId}`).then((r) => r.data),
  byWallet: (walletId, page = 0, size = 20) => api.get(`/api/transactions/wallet/${walletId}`, { params: { page, size } }).then((r) => r.data),
};

export const statementApi = {
  byWallet: (walletId, page = 0, size = 20) => api.get(`/api/statements/wallet/${walletId}`, { params: { page, size } }).then((r) => r.data),
  byRange: (walletId, startDate, endDate, page = 0, size = 20) =>
    api
      .get(`/api/statements/wallet/${walletId}/range`, { params: { startDate, endDate, page, size } })
      .then((r) => r.data),
};

const paging = (page = 0, size = 50) => ({ page, size });

export const adminApi = {
  summary: () => api.get('/api/admin/dashboard/summary').then((r) => r.data),
  wallets: (page = 0, size = 50) => api.get('/api/admin/wallets', { params: paging(page, size) }).then((r) => r.data),
  frozenWallets: (page = 0, size = 50) => api.get('/api/admin/wallets/frozen', { params: paging(page, size) }).then((r) => r.data),
  blacklistedWallets: (page = 0, size = 50) => api.get('/api/admin/wallets/blacklisted', { params: paging(page, size) }).then((r) => r.data),
  transactions: (page = 0, size = 50) => api.get('/api/admin/transactions', { params: paging(page, size) }).then((r) => r.data),
  failedTransactions: (page = 0, size = 50) => api.get('/api/admin/transactions/failed', { params: paging(page, size) }).then((r) => r.data),
  transactionsByStatus: (status, page = 0, size = 50) => api.get(`/api/admin/transactions/status/${status}`, { params: paging(page, size) }).then((r) => r.data),
  transactionsByType: (type, page = 0, size = 50) => api.get(`/api/admin/transactions/type/${type}`, { params: paging(page, size) }).then((r) => r.data),
  auditLogs: (page = 0, size = 50) => api.get('/api/admin/audit-logs', { params: paging(page, size) }).then((r) => r.data),
  notifications: (page = 0, size = 50) => api.get('/api/admin/notifications', { params: paging(page, size) }).then((r) => r.data),
  users: (page = 0, size = 50) => api.get('/api/admin/users', { params: paging(page, size) }).then((r) => r.data),
  updateUserRole: (userId, role) => api.patch(`/api/admin/users/${userId}/role`, { role }).then((r) => r.data),
};
