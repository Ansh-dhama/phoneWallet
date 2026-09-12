import { useEffect, useState } from 'react';
import { ArrowDownToLine, ArrowLeftRight, ArrowRight, ArrowUpRight, CreditCard, Eye, EyeOff, Plus, ReceiptText, RotateCcw, Send, ShieldCheck, WalletCards } from 'lucide-react';
import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { apiErrorMessage } from '../services/api';
import { pageContent, transactionApi, walletApi } from '../services/walletApi';
import { formatDate, formatMoney, shortRef, walletIdOf } from '../utils/format';
import { EmptyState, LoadingBlock, PageHeader, SectionCard, StatusBadge } from '../components/UI';
import { getStoredWalletId, persistWalletId } from '../utils/auth';

function greeting() {
  const hour = new Date().getHours();
  if (hour < 12) return 'Good morning';
  if (hour < 18) return 'Good afternoon';
  return 'Good evening';
}

export default function DashboardPage() {
  const { auth, role } = useAuth();
  const [walletId, setWalletId] = useState(() => getStoredWalletId(auth?.userId));
  const [balance, setBalance] = useState(null);
  const [wallet, setWallet] = useState(null);
  const [transactions, setTransactions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [showBalance, setShowBalance] = useState(true);

  useEffect(() => {
    let live = true;
    async function load() {
      setLoading(true);
      setError('');
      try {
        let selected = walletId;
        if (!selected) {
          const mine = await walletApi.mine();
          if (mine?.length) {
            selected = String(walletIdOf(mine[0]));
            persistWalletId(auth?.userId, selected);
            if (live) setWalletId(selected);
          }
        }
        if (!selected) return;
        const [walletData, balanceData, txnData] = await Promise.all([
          walletApi.get(selected),
          walletApi.balance(selected),
          transactionApi.byWallet(selected, 0, 10),
        ]);
        if (!live) return;
        setWallet(walletData);
        setBalance(balanceData);
        setTransactions(pageContent(txnData));
      } catch (err) {
        if (live) setError(apiErrorMessage(err));
      } finally {
        if (live) setLoading(false);
      }
    }
    load();
    return () => { live = false; };
  }, [auth?.userId, walletId]);

  const currency = wallet?.currency || transactions[0]?.currency || 'INR';
  const incoming = transactions.filter((tx) => String(tx.toWalletId) === String(walletId)).length;
  const outgoing = transactions.filter((tx) => String(tx.fromWalletId) === String(walletId)).length;

  const quickActions = role === 'MERCHANT'
    ? [
        { to: '/wallet', label: 'Wallet', icon: WalletCards },
        { to: '/transactions', label: 'Activity', icon: ArrowLeftRight },
        { to: '/refund', label: 'Refund', icon: RotateCcw },
        { to: '/statements', label: 'Statement', icon: ReceiptText },
      ]
    : [
        { to: '/wallet', label: 'Add money', icon: ArrowDownToLine },
        { to: '/transfer', label: 'Send', icon: Send },
        { to: '/pay', label: 'Pay', icon: CreditCard },
        { to: '/statements', label: 'Statement', icon: ReceiptText },
      ];

  return (
    <>
      <PageHeader eyebrow="Overview" title={`${greeting()}, ${auth?.username || 'there'}`} description="Here’s what’s happening with your wallet today." />

      {loading ? <LoadingBlock label="Opening your wallet…" /> : error ? <div className="error-panel">{error}</div> : !walletId ? (
        <SectionCard className="connect-wallet-hero">
          <EmptyState icon={WalletCards} title="Create your first wallet" description="Set up a wallet to add money, send payments and keep track of your activity." action={<Link className="button button-primary" to="/wallet"><Plus size={17} /> Create wallet</Link>} />
        </SectionCard>
      ) : (
        <>
          <section className="dashboard-hero-grid">
            <div className="wallet-hero-card premium-wallet-card">
              <div className="wallet-hero-main">
                <div className="wallet-hero-top"><span>Available balance</span><button className="balance-visibility" onClick={() => setShowBalance((value) => !value)} aria-label="Toggle balance visibility">{showBalance ? <Eye size={18} /> : <EyeOff size={18} />}</button></div>
                <strong>{showBalance ? formatMoney(balance, currency) : '••••••'}</strong>
                <div className="wallet-card-number">PhoneWallet •••• {String(walletId).padStart(4, '0').slice(-4)}</div>
                <div className="wallet-hero-meta"><span><ShieldCheck size={14} /> {wallet?.status === 'ACTIVE' ? 'Ready to use' : wallet?.status || 'Active'}</span><span>{currency}</span></div>
              </div>
            </div>

            <div className="dashboard-actions-card">
              <span className="dashboard-section-kicker">Quick actions</span>
              <div className="quick-actions">
                {quickActions.map(({ to, label, icon: Icon }) => (
                  <Link key={to} to={to} className="quick-action"><span><Icon size={20} /></span><strong>{label}</strong></Link>
                ))}
              </div>
              <div className="dashboard-tip"><ShieldCheck size={18} /><div><strong>Protected transactions</strong><span>Every money action is checked before it is processed.</span></div></div>
            </div>
          </section>

          <div className="metric-grid compact-grid">
            <div className="mini-metric"><div className="mini-icon incoming"><ArrowDownToLine size={18} /></div><div><span>Money in</span><strong>{incoming}</strong><small>recent entries</small></div></div>
            <div className="mini-metric"><div className="mini-icon outgoing"><ArrowUpRight size={18} /></div><div><span>Money out</span><strong>{outgoing}</strong><small>recent entries</small></div></div>
            <div className="mini-metric"><div className="mini-icon success"><ReceiptText size={18} /></div><div><span>Activity</span><strong>{transactions.length}</strong><small>latest records</small></div></div>
          </div>

          <SectionCard title="Recent activity" subtitle="Your latest wallet transactions" action={<Link className="text-link" to="/transactions">See all <ArrowRight size={15} /></Link>}>
            {transactions.length ? (
              <div className="transaction-list">
                {transactions.slice(0, 6).map((tx, index) => {
                  const outgoingTx = String(tx.fromWalletId) === String(walletId);
                  return (
                    <div className="transaction-row" key={tx.transactionId ?? index}>
                      <div className={`transaction-icon ${outgoingTx ? 'outgoing' : 'incoming'}`}>{outgoingTx ? <ArrowUpRight size={19} /> : <ArrowDownToLine size={19} />}</div>
                      <div className="transaction-main"><strong>{String(tx.type || (outgoingTx ? 'Money sent' : 'Money received')).replaceAll('_', ' ')}</strong><span>{shortRef(tx.transactionReference)} · {formatDate(tx.createdAt)}</span></div>
                      <div className="transaction-amount"><strong className={outgoingTx ? 'amount-negative' : 'amount-positive'}>{outgoingTx ? '−' : '+'}{formatMoney(tx.amount, tx.currency || currency)}</strong><StatusBadge value={tx.status} /></div>
                    </div>
                  );
                })}
              </div>
            ) : <EmptyState icon={ReceiptText} title="No activity yet" description="Your transfers, payments and top-ups will appear here." />}
          </SectionCard>
        </>
      )}
    </>
  );
}
