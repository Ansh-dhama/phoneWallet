import { useEffect, useMemo, useState } from 'react';
import { ArrowDownToLine, ArrowUpRight, Eye, ReceiptText, RefreshCw } from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { Button, EmptyState, LoadingBlock, Modal, PageHeader, Pagination, SearchBox, StatusBadge } from '../components/UI';
import { apiErrorMessage } from '../services/api';
import { pageContent, pageMeta, transactionApi } from '../services/walletApi';
import { getStoredWalletId } from '../utils/auth';
import { formatDate, formatMoney } from '../utils/format';

export default function TransactionsPage() {
  const { auth } = useAuth();
  const walletId = getStoredWalletId(auth?.userId);
  const [rows, setRows] = useState([]);
  const [meta, setMeta] = useState(null);
  const [page, setPage] = useState(0);
  const [loading, setLoading] = useState(Boolean(walletId));
  const [detailLoading, setDetailLoading] = useState(false);
  const [error, setError] = useState('');
  const [query, setQuery] = useState('');
  const [status, setStatus] = useState('ALL');
  const [selected, setSelected] = useState(null);

  async function load(targetPage = page) {
    if (!walletId) return;
    setLoading(true); setError('');
    try {
      const data = await transactionApi.byWallet(walletId, targetPage, 20);
      setRows(pageContent(data));
      setMeta(pageMeta(data));
      setPage(targetPage);
    } catch (err) { setError(apiErrorMessage(err)); }
    finally { setLoading(false); }
  }

  async function openDetails(tx) {
    setSelected(tx);
    const id = tx?.transactionId ?? tx?.id;
    if (!id) return;
    setDetailLoading(true);
    try { setSelected(await transactionApi.get(id)); } catch { /* row data remains visible */ }
    finally { setDetailLoading(false); }
  }

  useEffect(() => { setPage(0); load(0); }, [walletId]);

  const statuses = useMemo(() => ['ALL', ...new Set(rows.map((row) => String(row.status || '').toUpperCase()).filter(Boolean))], [rows]);
  const filtered = rows.filter((row) => {
    const haystack = `${row.transactionReference || ''} ${row.type || ''} ${row.status || ''} ${row.fromWalletId || ''} ${row.toWalletId || ''}`.toLowerCase();
    return haystack.includes(query.toLowerCase()) && (status === 'ALL' || String(row.status).toUpperCase() === status);
  });

  return (
    <>
      <PageHeader eyebrow="Activity" title="Transactions" description="Review money sent, received, top-ups, payments and refunds for your selected wallet." actions={<Button variant="secondary" onClick={() => load(page)} disabled={!walletId} loading={loading}><RefreshCw size={16} /> Refresh</Button>} />
      <div className="toolbar-card"><SearchBox value={query} onChange={setQuery} placeholder="Search this page…" /><select className="toolbar-select" value={status} onChange={(e) => setStatus(e.target.value)}>{statuses.map((item) => <option key={item}>{item}</option>)}</select><span className="toolbar-count">{meta?.totalElements ?? filtered.length} total</span></div>
      {!walletId ? <EmptyState icon={ReceiptText} title="No wallet selected" description="Open Wallet and select a wallet first." /> : loading ? <LoadingBlock /> : error ? <div className="error-panel">{error}</div> : filtered.length ? (
        <div className="table-card"><div className="table-wrap"><table><thead><tr><th>Transaction</th><th>Type</th><th>Direction</th><th>Amount</th><th>Refunded</th><th>Status</th><th>Date</th><th></th></tr></thead><tbody>{filtered.map((tx, index) => { const outgoing = String(tx.fromWalletId) === String(walletId); return <tr key={tx.transactionId ?? index}><td><div className="table-primary"><strong>#{tx.transactionId ?? '—'}</strong><span>{tx.transactionReference || '—'}</span></div></td><td>{tx.type || '—'}</td><td><div className="direction-cell">{outgoing ? <ArrowUpRight size={16} /> : <ArrowDownToLine size={16} />}{outgoing ? `To #${tx.toWalletId ?? '—'}` : `From #${tx.fromWalletId ?? 'External'}`}</div></td><td><strong className={outgoing ? 'amount-negative' : 'amount-positive'}>{outgoing ? '−' : '+'}{formatMoney(tx.amount, tx.currency)}</strong></td><td>{formatMoney(tx.refundedAmount || 0, tx.currency)}</td><td><StatusBadge value={tx.status} /></td><td>{formatDate(tx.createdAt)}</td><td><button className="table-icon-button" onClick={() => openDetails(tx)} aria-label="View transaction"><Eye size={17} /></button></td></tr>; })}</tbody></table></div><Pagination meta={meta} loading={loading} onPageChange={load} /></div>
      ) : <EmptyState icon={ReceiptText} title="No matching transactions" description="Try a different search or status filter." />}

      <Modal open={Boolean(selected)} title="Transaction details" description={selected?.transactionReference} onClose={() => setSelected(null)}>
        {detailLoading ? <LoadingBlock label="Loading transaction details…" /> : selected && <div className="detail-list"><div><span>Transaction ID</span><strong>{selected.transactionId ?? '—'}</strong></div><div><span>Reference</span><strong>{selected.transactionReference || '—'}</strong></div><div><span>From wallet</span><strong>{selected.fromWalletId ?? 'External'}</strong></div><div><span>To wallet</span><strong>{selected.toWalletId ?? '—'}</strong></div><div><span>Amount</span><strong>{formatMoney(selected.amount, selected.currency)}</strong></div><div><span>Refunded amount</span><strong>{formatMoney(selected.refundedAmount || 0, selected.currency)}</strong></div><div><span>Related transaction</span><strong>{selected.relatedTransactionReference || '—'}</strong></div><div><span>Provider reference</span><strong>{selected.providerReference || '—'}</strong></div><div><span>Type</span><strong>{selected.type || '—'}</strong></div><div><span>Status</span><StatusBadge value={selected.status} /></div><div><span>Failure reason</span><strong>{selected.failureReason || '—'}</strong></div><div><span>Created</span><strong>{formatDate(selected.createdAt)}</strong></div><div><span>Updated</span><strong>{formatDate(selected.updatedAt)}</strong></div></div>}
      </Modal>
    </>
  );
}
