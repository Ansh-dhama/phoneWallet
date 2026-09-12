import { useEffect, useMemo, useState } from 'react';
import {
  Activity,
  Ban,
  BellRing,
  CircleDollarSign,
  FileClock,
  RefreshCw,
  RotateCcw,
  ShieldAlert,
  ShieldCheck,
  Snowflake,
  Undo2,
  UserCog,
  UsersRound,
  WalletCards,
} from 'lucide-react';
import { useToast } from '../context/ToastContext';
import { adminApi, pageContent, pageMeta, transactionApi, walletApi } from '../services/walletApi';
import { apiErrorMessage } from '../services/api';
import { formatDate, formatMoney, generateIdempotencyKey, walletIdOf } from '../utils/format';
import {
  Button,
  EmptyState,
  Field,
  LoadingBlock,
  Modal,
  PageHeader,
  Pagination,
  SearchBox,
  SectionCard,
  SmartTable,
  StatCard,
  StatusBadge,
} from '../components/UI';

const tabs = [
  ['overview', 'Overview'],
  ['wallets', 'Wallets'],
  ['transactions', 'Transactions'],
  ['users', 'Users & roles'],
  ['audit', 'Audit logs'],
  ['notifications', 'Notifications'],
];

export default function AdminPage() {
  const { showToast } = useToast();
  const [activeTab, setActiveTab] = useState('overview');
  const [summary, setSummary] = useState(null);
  const [wallets, setWallets] = useState([]);
  const [walletMeta, setWalletMeta] = useState(null);
  const [transactions, setTransactions] = useState([]);
  const [transactionMeta, setTransactionMeta] = useState(null);
  const [users, setUsers] = useState([]);
  const [userMeta, setUserMeta] = useState(null);
  const [auditLogs, setAuditLogs] = useState([]);
  const [auditMeta, setAuditMeta] = useState(null);
  const [notifications, setNotifications] = useState([]);
  const [notificationMeta, setNotificationMeta] = useState(null);
  const [loading, setLoading] = useState(true);
  const [query, setQuery] = useState('');
  const [error, setError] = useState('');
  const [blacklistTarget, setBlacklistTarget] = useState(null);
  const [blacklistReason, setBlacklistReason] = useState('');
  const [refundTarget, setRefundTarget] = useState(null);
  const [refundForm, setRefundForm] = useState({ amount: '', currency: 'INR', reason: '' });
  const [reversalTarget, setReversalTarget] = useState(null);
  const [reversalReason, setReversalReason] = useState('');
  const [actionLoading, setActionLoading] = useState('');
  const [filterLoading, setFilterLoading] = useState(false);
  const [walletView, setWalletView] = useState('ALL');
  const [transactionView, setTransactionView] = useState('ALL');
  const [serverFilterValue, setServerFilterValue] = useState('');
  const [roleDrafts, setRoleDrafts] = useState({});

  async function loadOverview() {
    setLoading(true); setError('');
    try {
      const [summaryData, txData] = await Promise.all([adminApi.summary(), adminApi.transactions(0, 20)]);
      setSummary(summaryData);
      setTransactions(pageContent(txData));
      setTransactionMeta(pageMeta(txData));
    } catch (err) { setError(apiErrorMessage(err)); }
    finally { setLoading(false); }
  }

  useEffect(() => { loadOverview(); }, []);

  async function loadWallets(page = 0, view = walletView) {
    setFilterLoading(true);
    try {
      const data = view === 'FROZEN' ? await adminApi.frozenWallets(page, 50)
        : view === 'BLACKLISTED' ? await adminApi.blacklistedWallets(page, 50)
          : await adminApi.wallets(page, 50);
      setWallets(pageContent(data)); setWalletMeta(pageMeta(data));
    } catch (err) { showToast(apiErrorMessage(err), 'error'); }
    finally { setFilterLoading(false); }
  }

  async function loadTransactions(page = 0, view = transactionView) {
    if ((view === 'STATUS' || view === 'TYPE') && !serverFilterValue.trim()) {
      showToast(`Enter a ${view.toLowerCase()} enum value first.`, 'error'); return;
    }
    setFilterLoading(true);
    try {
      const value = serverFilterValue.trim().toUpperCase();
      const data = view === 'FAILED' ? await adminApi.failedTransactions(page, 50)
        : view === 'STATUS' ? await adminApi.transactionsByStatus(value, page, 50)
          : view === 'TYPE' ? await adminApi.transactionsByType(value, page, 50)
            : await adminApi.transactions(page, 50);
      setTransactions(pageContent(data)); setTransactionMeta(pageMeta(data));
    } catch (err) { showToast(apiErrorMessage(err), 'error'); }
    finally { setFilterLoading(false); }
  }

  async function loadUsers(page = 0) {
    setFilterLoading(true);
    try {
      const data = await adminApi.users(page, 50);
      const content = pageContent(data);
      setUsers(content); setUserMeta(pageMeta(data));
      setRoleDrafts(Object.fromEntries(content.map((u) => [u.id, u.role])));
    } catch (err) { showToast(apiErrorMessage(err), 'error'); }
    finally { setFilterLoading(false); }
  }

  async function loadAudit(page = 0) {
    setFilterLoading(true);
    try { const data = await adminApi.auditLogs(page, 50); setAuditLogs(pageContent(data)); setAuditMeta(pageMeta(data)); }
    catch (err) { showToast(apiErrorMessage(err), 'error'); }
    finally { setFilterLoading(false); }
  }

  async function loadNotifications(page = 0) {
    setFilterLoading(true);
    try { const data = await adminApi.notifications(page, 50); setNotifications(pageContent(data)); setNotificationMeta(pageMeta(data)); }
    catch (err) { showToast(apiErrorMessage(err), 'error'); }
    finally { setFilterLoading(false); }
  }

  async function selectTab(tab) {
    setActiveTab(tab); setQuery('');
    if (tab === 'overview') return loadOverview();
    if (tab === 'wallets') return loadWallets(0);
    if (tab === 'transactions') return loadTransactions(0);
    if (tab === 'users') return loadUsers(0);
    if (tab === 'audit') return loadAudit(0);
    if (tab === 'notifications') return loadNotifications(0);
  }

  async function changeWalletView(view) { setWalletView(view); setQuery(''); await loadWallets(0, view); }
  async function changeTransactionView(view) { setTransactionView(view); setServerFilterValue(''); setQuery(''); if (view === 'ALL' || view === 'FAILED') await loadTransactions(0, view); }

  const filteredWallets = useMemo(() => wallets.filter((w) => JSON.stringify(w).toLowerCase().includes(query.toLowerCase())), [wallets, query]);
  const filteredTransactions = useMemo(() => transactions.filter((t) => JSON.stringify(t).toLowerCase().includes(query.toLowerCase())), [transactions, query]);
  const filteredUsers = useMemo(() => users.filter((u) => JSON.stringify(u).toLowerCase().includes(query.toLowerCase())), [users, query]);

  async function walletAction(name, wallet, handler) {
    const id = walletIdOf(wallet) ?? wallet?.walletId;
    if (!id) return showToast('Wallet ID could not be read from this API response.', 'error');
    setActionLoading(`${name}-${id}`);
    try { await handler(id); showToast(`Wallet #${id} updated successfully.`); await loadWallets(walletMeta?.page || 0); await loadOverview(); }
    catch (err) { showToast(apiErrorMessage(err), 'error'); }
    finally { setActionLoading(''); }
  }

  async function submitBlacklist(event) {
    event.preventDefault();
    const id = walletIdOf(blacklistTarget) ?? blacklistTarget?.walletId;
    setActionLoading(`blacklist-${id}`);
    try {
      await walletApi.blacklist(id, blacklistReason);
      showToast(`Wallet #${id} blacklisted.`);
      setBlacklistTarget(null); setBlacklistReason(''); await loadWallets(walletMeta?.page || 0); await loadOverview();
    } catch (err) { showToast(apiErrorMessage(err), 'error'); }
    finally { setActionLoading(''); }
  }

  async function submitRefund(event) {
    event.preventDefault();
    const reference = refundTarget?.transactionReference ?? refundTarget?.transactionRef;
    setActionLoading('refund');
    try {
      await transactionApi.refund({ originalTransactionReference: reference, refundAmount: Number(refundForm.amount), refundReason: refundForm.reason || null, idempotencyKey: generateIdempotencyKey('refund'), currency: refundForm.currency });
      showToast('Refund processed successfully.'); setRefundTarget(null); setRefundForm({ amount: '', currency: 'INR', reason: '' }); await loadTransactions(transactionMeta?.page || 0); await loadOverview();
    } catch (err) { showToast(apiErrorMessage(err), 'error'); }
    finally { setActionLoading(''); }
  }

  async function submitReversal(event) {
    event.preventDefault();
    const reference = reversalTarget?.transactionReference ?? reversalTarget?.transactionRef;
    setActionLoading('reversal');
    try {
      await transactionApi.reversal({ originalTransactionReference: reference, reason: reversalReason, idempotencyKey: generateIdempotencyKey('reversal') });
      showToast('Transaction reversed successfully.'); setReversalTarget(null); setReversalReason(''); await loadTransactions(transactionMeta?.page || 0); await loadOverview();
    } catch (err) { showToast(apiErrorMessage(err), 'error'); }
    finally { setActionLoading(''); }
  }

  async function updateRole(user) {
    const nextRole = roleDrafts[user.id];
    if (!nextRole || nextRole === user.role) return;
    setActionLoading(`role-${user.id}`);
    try {
      const updated = await adminApi.updateUserRole(user.id, nextRole);
      setUsers((rows) => rows.map((row) => row.id === updated.id ? updated : row));
      showToast(`User ${updated.username} is now ${updated.role}.`);
    } catch (err) { showToast(apiErrorMessage(err), 'error'); }
    finally { setActionLoading(''); }
  }

  return (
    <>
      <PageHeader eyebrow="Administration" title="Wallet operations console" description="Production controls for users, wallets, transactions, audit and notifications." actions={<Button variant="secondary" onClick={() => selectTab(activeTab)} loading={loading || filterLoading}><RefreshCw size={16} /> Refresh data</Button>} />
      <div className="admin-tabs">{tabs.map(([key, label]) => <button key={key} className={activeTab === key ? 'active' : ''} onClick={() => selectTab(key)}>{label}</button>)}</div>
      {error && <div className="warning-panel"><ShieldAlert size={18} /><span>{error}</span></div>}

      {loading && activeTab === 'overview' ? <LoadingBlock label="Loading admin console…" /> : (
        <>
          {activeTab === 'overview' && <AdminOverview summary={summary} transactions={transactions} />}

          {activeTab === 'wallets' && (
            <SectionCard title="Wallet management" subtitle="Administrative freeze and blacklist controls. Blacklist actors are derived from the authenticated admin, never the client." action={<div className="admin-filter-actions"><select className="toolbar-select" value={walletView} disabled={filterLoading} onChange={(e) => changeWalletView(e.target.value)}><option value="ALL">All wallets</option><option value="FROZEN">Frozen</option><option value="BLACKLISTED">Active blacklist records</option></select><SearchBox value={query} onChange={setQuery} placeholder="Search current page…" /></div>}>
              {filterLoading && !wallets.length ? <LoadingBlock /> : filteredWallets.length ? <div className="table-card embedded"><div className="table-wrap"><table><thead><tr><th>Wallet</th><th>User / reason</th><th>Currency</th><th>Balance</th><th>Status</th><th>Security controls</th></tr></thead><tbody>{filteredWallets.map((row, index) => { const id = walletIdOf(row) ?? row.walletId; const isBlocklist = row.reason != null && row.walletId != null && row.currency == null; const isFrozen = String(row.status || '').toUpperCase() === 'FROZEN'; return <tr key={row.id ?? id ?? index}><td><strong>#{id ?? '—'}</strong></td><td>{isBlocklist ? row.reason : (row.userId ?? row.user?.id ?? '—')}</td><td>{row.currency ?? '—'}</td><td>{row.balance != null ? formatMoney(row.balance, row.currency || 'INR') : '—'}</td><td><StatusBadge value={isBlocklist ? (row.active ? 'BLACKLISTED' : 'INACTIVE') : (row.status || (isFrozen ? 'FROZEN' : 'ACTIVE'))} /></td><td>{!isBlocklist ? <div className="table-actions"><Button variant="tiny" loading={actionLoading === `freeze-${id}`} onClick={() => walletAction('freeze', row, walletApi.freeze)}><Snowflake size={14} /> Freeze</Button><Button variant="tiny" loading={actionLoading === `unfreeze-${id}`} onClick={() => walletAction('unfreeze', row, walletApi.unfreeze)}><ShieldCheck size={14} /> Unfreeze</Button><Button variant="tiny-danger" onClick={() => setBlacklistTarget(row)}><Ban size={14} /> Blacklist</Button><Button variant="tiny" loading={actionLoading === `unblacklist-${id}`} onClick={() => walletAction('unblacklist', row, walletApi.unblacklist)}><Undo2 size={14} /> Restore</Button></div> : <Button variant="tiny" loading={actionLoading === `unblacklist-${id}`} onClick={() => walletAction('unblacklist', row, walletApi.unblacklist)}><Undo2 size={14} /> Remove blacklist</Button>}</td></tr>; })}</tbody></table></div><Pagination meta={walletMeta} loading={filterLoading} onPageChange={(p) => loadWallets(p)} /></div> : <EmptyState icon={WalletCards} title="No wallets found" description="No rows match the current server view and search." />}
            </SectionCard>
          )}

          {activeTab === 'transactions' && (
            <SectionCard title="Transaction operations" subtitle="Refunds use cumulative partial-refund accounting; reversals and refunds lock the original transaction before state changes." action={<div className="admin-filter-actions admin-filter-wrap"><select className="toolbar-select" value={transactionView} disabled={filterLoading} onChange={(e) => changeTransactionView(e.target.value)}><option value="ALL">All transactions</option><option value="FAILED">Failed</option><option value="STATUS">Filter by status</option><option value="TYPE">Filter by type</option></select>{(transactionView === 'STATUS' || transactionView === 'TYPE') && <><input className="compact-filter-input" value={serverFilterValue} onChange={(e) => setServerFilterValue(e.target.value)} placeholder={transactionView === 'STATUS' ? 'e.g. SUCCESS' : 'e.g. TRANSFER'} /><Button variant="tiny" loading={filterLoading} onClick={() => loadTransactions(0)}>Apply</Button></>}<SearchBox value={query} onChange={setQuery} placeholder="Search current page…" /></div>}>
              {filterLoading && !transactions.length ? <LoadingBlock /> : filteredTransactions.length ? <div className="table-card embedded"><div className="table-wrap"><table><thead><tr><th>Reference</th><th>Type</th><th>Amount</th><th>Refunded</th><th>From → To</th><th>Status</th><th>Created</th><th>Actions</th></tr></thead><tbody>{filteredTransactions.map((tx, index) => <tr key={tx.transactionId ?? tx.id ?? index}><td><div className="table-primary"><strong>#{tx.transactionId ?? tx.id ?? '—'}</strong><span>{tx.transactionReference ?? tx.transactionRef ?? '—'}</span></div></td><td>{tx.type ?? '—'}</td><td>{formatMoney(tx.amount, tx.currency || 'INR')}</td><td>{formatMoney(tx.refundedAmount || 0, tx.currency || 'INR')}</td><td>{tx.fromWalletId ?? 'External'} → {tx.toWalletId ?? '—'}</td><td><StatusBadge value={tx.status} /></td><td>{formatDate(tx.createdAt)}</td><td><div className="table-actions"><Button variant="tiny" disabled={!['SUCCESS','PARTIALLY_REFUNDED'].includes(String(tx.status))} onClick={() => { setRefundTarget(tx); const remaining = Math.max(0, Number(tx.amount || 0) - Number(tx.refundedAmount || 0)); setRefundForm({ amount: remaining || '', currency: tx.currency || 'INR', reason: '' }); }}><RotateCcw size={14} /> Refund</Button><Button variant="tiny-danger" disabled={String(tx.status) !== 'SUCCESS'} onClick={() => setReversalTarget(tx)}><Undo2 size={14} /> Reverse</Button></div></td></tr>)}</tbody></table></div><Pagination meta={transactionMeta} loading={filterLoading} onPageChange={(p) => loadTransactions(p)} /></div> : <EmptyState icon={Activity} title="No transactions found" description="No transaction rows match the current server view." />}
            </SectionCard>
          )}

          {activeTab === 'users' && (
            <SectionCard title="User role administration" subtitle="Public signup creates USER only. Admins may promote verified accounts to MERCHANT or return them to USER; ADMIN cannot be granted through this API." action={<SearchBox value={query} onChange={setQuery} placeholder="Search current page…" />}>
              {filterLoading && !users.length ? <LoadingBlock /> : filteredUsers.length ? <div className="table-card embedded"><div className="table-wrap"><table><thead><tr><th>User</th><th>Mobile</th><th>Current role</th><th>Verified role change</th></tr></thead><tbody>{filteredUsers.map((user) => <tr key={user.id}><td><div className="table-primary"><strong>#{user.id} · {user.username}</strong></div></td><td>{user.mobile || '—'}</td><td><StatusBadge value={user.role} /></td><td>{user.role === 'ADMIN' ? <span>Bootstrap/admin role is protected</span> : <div className="table-actions"><select className="toolbar-select role-select" value={roleDrafts[user.id] || user.role} onChange={(e) => setRoleDrafts((d) => ({ ...d, [user.id]: e.target.value }))}><option value="USER">USER</option><option value="MERCHANT">MERCHANT</option></select><Button variant="tiny" loading={actionLoading === `role-${user.id}`} disabled={(roleDrafts[user.id] || user.role) === user.role} onClick={() => updateRole(user)}><UserCog size={14} /> Apply</Button></div>}</td></tr>)}</tbody></table></div><Pagination meta={userMeta} loading={filterLoading} onPageChange={loadUsers} /></div> : <EmptyState icon={UsersRound} title="No users found" description="No users were returned on this page." />}
            </SectionCard>
          )}

          {activeTab === 'audit' && <SectionCard title="Audit logs" subtitle="Server-paginated operational audit records.">{filterLoading && !auditLogs.length ? <LoadingBlock /> : auditLogs.length ? <><SmartTable rows={auditLogs} maxColumns={8} /><Pagination meta={auditMeta} loading={filterLoading} onPageChange={loadAudit} /></> : <EmptyState icon={FileClock} title="No audit logs" description="No audit log entries were returned." />}</SectionCard>}
          {activeTab === 'notifications' && <SectionCard title="Notifications" subtitle="Idempotently consumed Kafka notification records.">{filterLoading && !notifications.length ? <LoadingBlock /> : notifications.length ? <><SmartTable rows={notifications} maxColumns={8} /><Pagination meta={notificationMeta} loading={filterLoading} onPageChange={loadNotifications} /></> : <EmptyState icon={BellRing} title="No notifications" description="No notification records were returned." />}</SectionCard>}
        </>
      )}

      <Modal open={Boolean(blacklistTarget)} title={`Blacklist wallet #${walletIdOf(blacklistTarget) || blacklistTarget?.walletId || ''}`} description="The authenticated admin identity is recorded by the backend." onClose={() => setBlacklistTarget(null)}>
        <form className="stack-form" onSubmit={submitBlacklist}><Field label="Reason"><textarea rows="3" required maxLength="500" value={blacklistReason} onChange={(e) => setBlacklistReason(e.target.value)} placeholder="Reason for blacklisting" /></Field><Button type="submit" variant="danger" loading={actionLoading.startsWith('blacklist-')}><Ban size={16} /> Confirm blacklist</Button></form>
      </Modal>

      <Modal open={Boolean(refundTarget)} title="Process refund" description={refundTarget?.transactionReference ?? refundTarget?.transactionRef} onClose={() => setRefundTarget(null)}>
        <form className="stack-form" onSubmit={submitRefund}><div className="form-grid-2"><Field label="Refund amount"><input required min="0.01" step="0.01" type="number" value={refundForm.amount} onChange={(e) => setRefundForm({ ...refundForm, amount: e.target.value })} /></Field><Field label="Currency"><input required value={refundForm.currency} disabled /></Field></div><Field label="Reason"><textarea rows="3" maxLength="500" value={refundForm.reason} onChange={(e) => setRefundForm({ ...refundForm, reason: e.target.value })} placeholder="Optional refund reason" /></Field><Button type="submit" loading={actionLoading === 'refund'}><RotateCcw size={16} /> Process refund</Button></form>
      </Modal>

      <Modal open={Boolean(reversalTarget)} title="Reverse transaction" description={reversalTarget?.transactionReference ?? reversalTarget?.transactionRef} onClose={() => setReversalTarget(null)}>
        <form className="stack-form" onSubmit={submitReversal}><Field label="Reason"><textarea rows="3" required maxLength="500" value={reversalReason} onChange={(e) => setReversalReason(e.target.value)} placeholder="Why is this transaction being reversed?" /></Field><div className="warning-panel"><ShieldAlert size={18} /><span>Reversal is an administrative operation protected by original-transaction locking and idempotency.</span></div><Button type="submit" variant="danger" loading={actionLoading === 'reversal'}><Undo2 size={16} /> Confirm reversal</Button></form>
      </Modal>
    </>
  );
}

function AdminOverview({ summary, transactions }) {
  const successful = Number(summary?.successfulTransactions || 0);
  const total = Number(summary?.totalTransactions || 0);
  const successRate = total ? Math.round((successful / total) * 100) : 0;
  return (
    <>
      <div className="metric-grid admin-metrics">
        <StatCard label="Total wallets" value={summary?.totalWallets ?? 0} hint={`${summary?.activeWallets ?? 0} active`} icon={WalletCards} tone="blue" />
        <StatCard label="Transactions" value={summary?.totalTransactions ?? 0} hint={`${successRate}% successful`} icon={CircleDollarSign} tone="green" />
        <StatCard label="Frozen wallets" value={summary?.frozenWallets ?? 0} hint="Requires monitoring" icon={Snowflake} tone="orange" />
        <StatCard label="Blacklisted" value={summary?.blacklistedWallets ?? 0} hint="Active blacklist records" icon={Ban} tone="red" />
      </div>
      <div className="admin-overview-grid">
        <SectionCard title="Transaction health" subtitle="Success, failure, partial refund, refund and reversal distribution.">
          <div className="health-list"><HealthRow label="Successful" value={summary?.successfulTransactions ?? 0} total={total} tone="success" /><HealthRow label="Failed" value={summary?.failedTransactions ?? 0} total={total} tone="danger" /><HealthRow label="Partially refunded" value={summary?.partiallyRefundedTransactions ?? 0} total={total} tone="warning" /><HealthRow label="Refunded" value={summary?.refundedTransactions ?? 0} total={total} tone="warning" /><HealthRow label="Reversed" value={summary?.reversedTransactions ?? 0} total={total} tone="neutral" /></div>
        </SectionCard>
        <SectionCard title="Platform records" subtitle="Operational observability counts from the backend.">
          <div className="record-counts"><div><BellRing size={19} /><span>Notifications</span><strong>{summary?.totalNotifications ?? 0}</strong></div><div><FileClock size={19} /><span>Audit logs</span><strong>{summary?.totalAuditLogs ?? 0}</strong></div><div><UsersRound size={19} /><span>Active wallets</span><strong>{summary?.activeWallets ?? 0}</strong></div><div><ShieldAlert size={19} /><span>Failed txns</span><strong>{summary?.failedTransactions ?? 0}</strong></div></div>
        </SectionCard>
      </div>
      <SectionCard title="Latest platform transactions" subtitle="A server-paginated operational sample.">{transactions.length ? <SmartTable rows={transactions.slice(0, 10)} maxColumns={7} /> : <EmptyState icon={Activity} title="No transaction data" description="No admin transaction records were returned." />}</SectionCard>
    </>
  );
}

function HealthRow({ label, value, total, tone }) {
  const percent = total ? Math.min(100, Math.round((Number(value || 0) / total) * 100)) : 0;
  return <div className="health-row"><div><span>{label}</span><strong>{value}</strong></div><div className="progress-track"><span className={`progress-fill progress-${tone}`} style={{ width: `${percent}%` }} /></div><small>{percent}%</small></div>;
}
