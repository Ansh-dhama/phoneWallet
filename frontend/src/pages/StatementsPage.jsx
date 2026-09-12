import { useEffect, useState } from 'react';
import { CalendarRange, Download, ReceiptText, RefreshCw } from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { Button, EmptyState, Field, LoadingBlock, PageHeader, Pagination, StatusBadge } from '../components/UI';
import { apiErrorMessage } from '../services/api';
import { pageContent, pageMeta, statementApi, walletApi } from '../services/walletApi';
import { getStoredWalletId } from '../utils/auth';
import { formatDate, formatMoney, normalizeDateTimeLocal } from '../utils/format';

export default function StatementsPage() {
  const { auth } = useAuth();
  const walletId = getStoredWalletId(auth?.userId);
  const [rows, setRows] = useState([]);
  const [meta, setMeta] = useState(null);
  const [page, setPage] = useState(0);
  const [loading, setLoading] = useState(Boolean(walletId));
  const [error, setError] = useState('');
  const [currency, setCurrency] = useState('INR');
  const [range, setRange] = useState({ start: '', end: '' });
  const [rangeApplied, setRangeApplied] = useState(false);

  async function load(targetPage = 0, useRange = rangeApplied) {
    if (!walletId) return;
    setLoading(true); setError('');
    try {
      const data = useRange
        ? await statementApi.byRange(walletId, normalizeDateTimeLocal(range.start), normalizeDateTimeLocal(range.end), targetPage, 20)
        : await statementApi.byWallet(walletId, targetPage, 20);
      setRows(pageContent(data));
      setMeta(pageMeta(data));
      setPage(targetPage);
    } catch (err) { setError(apiErrorMessage(err)); }
    finally { setLoading(false); }
  }

  useEffect(() => {
    setRangeApplied(false);
    setPage(0);
    load(0, false);
    if (walletId) walletApi.get(walletId).then((data) => setCurrency(data?.currency || 'INR')).catch(() => {});
  }, [walletId]);

  async function filter(event) {
    event.preventDefault();
    if (!range.start || !range.end) return;
    if (new Date(range.start) > new Date(range.end)) return setError('Start date must be before end date.');
    setRangeApplied(true);
    await load(0, true);
  }

  function downloadCsv() {
    const headers = ['transactionRef','walletId','entryType','amount','balanceAfterTransaction','timestamp'];
    const csv = [headers.join(','), ...rows.map((row) => headers.map((key) => JSON.stringify(row[key] ?? '')).join(','))].join('\n');
    const blob = new Blob([csv], { type: 'text/csv;charset=utf-8' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a'); a.href = url; a.download = `wallet-${walletId}-statement-page-${page + 1}.csv`; a.click(); URL.revokeObjectURL(url);
  }

  return (
    <>
      <PageHeader eyebrow="Ledger" title="Wallet statement" description="View your wallet statement, filter by date and export the current page when needed." actions={<Button variant="secondary" onClick={downloadCsv} disabled={!rows.length}><Download size={16} /> Export CSV</Button>} />
      <form className="statement-filter" onSubmit={filter}><Field label="Start date & time"><input type="datetime-local" value={range.start} onChange={(e) => setRange({ ...range, start: e.target.value })} /></Field><Field label="End date & time"><input type="datetime-local" value={range.end} onChange={(e) => setRange({ ...range, end: e.target.value })} /></Field><Button type="submit" disabled={!walletId || !range.start || !range.end}><CalendarRange size={16} /> Apply range</Button><Button type="button" variant="secondary" onClick={() => { setRange({ start: '', end: '' }); setRangeApplied(false); load(0, false); }}><RefreshCw size={16} /> All entries</Button></form>
      {!walletId ? <EmptyState icon={ReceiptText} title="No wallet selected" description="Select a wallet first." /> : loading ? <LoadingBlock /> : error ? <div className="error-panel">{error}</div> : rows.length ? <div className="table-card"><div className="table-wrap"><table><thead><tr><th>Reference</th><th>Entry type</th><th>Amount</th><th>Balance after</th><th>Wallet</th><th>Timestamp</th></tr></thead><tbody>{rows.map((row, index) => <tr key={`${row.transactionRef}-${index}`}><td><div className="table-primary"><strong>{row.transactionRef || '—'}</strong></div></td><td><StatusBadge value={row.entryType} /></td><td><strong>{formatMoney(row.amount, currency)}</strong></td><td>{formatMoney(row.balanceAfterTransaction, currency)}</td><td>#{row.walletId ?? walletId}</td><td>{formatDate(row.timestamp)}</td></tr>)}</tbody></table></div><Pagination meta={meta} loading={loading} onPageChange={(next) => load(next, rangeApplied)} /></div> : <EmptyState icon={ReceiptText} title="No statement entries" description="No statement entries were found for this wallet or date range." />}
    </>
  );
}
