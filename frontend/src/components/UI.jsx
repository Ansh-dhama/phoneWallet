import { LoaderCircle, Search } from 'lucide-react';
import { humanizeKey } from '../utils/format';

export function PageHeader({ eyebrow, title, description, actions }) {
  return (
    <div className="page-header">
      <div>
        {eyebrow && <div className="eyebrow">{eyebrow}</div>}
        <h1>{title}</h1>
        {description && <p>{description}</p>}
      </div>
      {actions && <div className="page-actions">{actions}</div>}
    </div>
  );
}

export function StatCard({ label, value, hint, icon: Icon, tone = 'neutral' }) {
  return (
    <article className={`stat-card tone-${tone}`}>
      <div className="stat-icon">{Icon && <Icon size={21} />}</div>
      <div className="stat-content">
        <span>{label}</span>
        <strong>{value}</strong>
        {hint && <small>{hint}</small>}
      </div>
    </article>
  );
}

export function SectionCard({ title, subtitle, action, children, className = '' }) {
  return (
    <section className={`section-card ${className}`}>
      {(title || action) && (
        <div className="section-card-header">
          <div>
            {title && <h2>{title}</h2>}
            {subtitle && <p>{subtitle}</p>}
          </div>
          {action}
        </div>
      )}
      {children}
    </section>
  );
}

export function Field({ label, error, hint, children }) {
  return (
    <label className="field">
      <span>{label}</span>
      {children}
      {error ? <small className="field-error">{error}</small> : hint ? <small>{hint}</small> : null}
    </label>
  );
}

export function Button({ children, loading, variant = 'primary', className = '', ...props }) {
  return (
    <button className={`button button-${variant} ${className}`} disabled={loading || props.disabled} {...props}>
      {loading && <LoaderCircle className="spin" size={17} />}
      {children}
    </button>
  );
}

export function StatusBadge({ value }) {
  const text = String(value || 'UNKNOWN').toUpperCase();
  const positive = ['SUCCESS', 'SUCCESSFUL', 'ACTIVE', 'COMPLETED', 'SENT'].includes(text);
  const negative = ['FAILED', 'BLACKLISTED', 'FROZEN', 'REJECTED'].includes(text);
  const warning = ['PENDING', 'PROCESSING', 'REFUNDED', 'PARTIALLY_REFUNDED', 'REVERSED'].includes(text);
  const tone = positive ? 'success' : negative ? 'danger' : warning ? 'warning' : 'neutral';
  return <span className={`status-badge status-${tone}`}>{text}</span>;
}

export function EmptyState({ icon: Icon, title, description, action }) {
  return (
    <div className="empty-state">
      {Icon && (
        <div className="empty-icon">
          <Icon size={28} />
        </div>
      )}
      <h3>{title}</h3>
      <p>{description}</p>
      {action}
    </div>
  );
}

export function LoadingBlock({ label = 'Loading data…' }) {
  return (
    <div className="loading-block">
      <LoaderCircle className="spin" size={24} />
      <span>{label}</span>
    </div>
  );
}

export function SearchBox({ value, onChange, placeholder = 'Search…' }) {
  return (
    <div className="search-box">
      <Search size={17} />
      <input value={value} onChange={(event) => onChange(event.target.value)} placeholder={placeholder} />
    </div>
  );
}

export function Modal({ open, title, description, onClose, children, footer }) {
  if (!open) return null;
  return (
    <div className="modal-backdrop" onMouseDown={onClose}>
      <div className="modal-card" onMouseDown={(event) => event.stopPropagation()}>
        <div className="modal-header">
          <div>
            <h3>{title}</h3>
            {description && <p>{description}</p>}
          </div>
          <button className="icon-button modal-close" onClick={onClose} aria-label="Close">×</button>
        </div>
        <div className="modal-body">{children}</div>
        {footer && <div className="modal-footer">{footer}</div>}
      </div>
    </div>
  );
}

export function SmartTable({ rows = [], actions, maxColumns = 7 }) {
  if (!rows.length) return null;
  const ignored = new Set(['password', 'accessToken', 'refreshToken']);
  const keys = Array.from(
    rows.reduce((set, row) => {
      Object.keys(row || {}).forEach((key) => {
        if (!ignored.has(key) && typeof row[key] !== 'object') set.add(key);
      });
      return set;
    }, new Set()),
  ).slice(0, maxColumns);

  return (
    <div className="table-wrap">
      <table>
        <thead>
          <tr>
            {keys.map((key) => <th key={key}>{humanizeKey(key)}</th>)}
            {actions && <th>Actions</th>}
          </tr>
        </thead>
        <tbody>
          {rows.map((row, index) => (
            <tr key={row?.id ?? row?.walletId ?? row?.transactionId ?? index}>
              {keys.map((key) => <td key={key}>{String(row?.[key] ?? '—')}</td>)}
              {actions && <td className="table-actions">{actions(row)}</td>}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}


export function Pagination({ meta, onPageChange, loading = false }) {
  if (!meta || Number(meta.totalPages || 0) <= 1) return null;
  return (
    <div className="pagination-bar">
      <Button variant="secondary" disabled={loading || meta.first} onClick={() => onPageChange(Math.max(0, meta.page - 1))}>Previous</Button>
      <span>Page <strong>{meta.page + 1}</strong> of <strong>{meta.totalPages}</strong> · {meta.totalElements} records</span>
      <Button variant="secondary" disabled={loading || meta.last} onClick={() => onPageChange(meta.page + 1)}>Next</Button>
    </div>
  );
}
