export function formatMoney(value, currency = 'INR') {
  const amount = Number(value ?? 0);
  try {
    return new Intl.NumberFormat('en-IN', {
      style: 'currency',
      currency: currency || 'INR',
      maximumFractionDigits: 2,
    }).format(Number.isFinite(amount) ? amount : 0);
  } catch {
    return `${currency || ''} ${Number.isFinite(amount) ? amount.toFixed(2) : '0.00'}`.trim();
  }
}

export function formatDate(value) {
  if (!value) return '—';
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return String(value);
  return date.toLocaleString('en-IN', {
    day: '2-digit',
    month: 'short',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  });
}

export function generateIdempotencyKey(prefix = 'txn') {
  if (globalThis.crypto?.randomUUID) return `${prefix}-${crypto.randomUUID()}`;
  return `${prefix}-${Date.now()}-${Math.random().toString(36).slice(2, 10)}`;
}

export function shortRef(value, length = 18) {
  if (!value) return '—';
  const text = String(value);
  return text.length > length ? `${text.slice(0, length)}…` : text;
}

export function normalizeDateTimeLocal(value) {
  if (!value) return value;
  return value.length === 16 ? `${value}:00` : value;
}

export function walletIdOf(wallet) {
  return wallet?.walletId ?? wallet?.id ?? wallet?.walletID ?? null;
}

export function humanizeKey(value) {
  return String(value || '')
    .replace(/([a-z0-9])([A-Z])/g, '$1 $2')
    .replace(/_/g, ' ')
    .replace(/^./, (char) => char.toUpperCase());
}
