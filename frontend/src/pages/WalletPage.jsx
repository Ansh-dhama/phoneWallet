import { useEffect, useMemo, useState } from 'react';
import { Banknote, Copy, Plus, RefreshCw, ShieldCheck, WalletCards } from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';
import { apiErrorMessage } from '../services/api';
import { pageContent, topUpApi, walletApi } from '../services/walletApi';
import { clearStoredWalletId, getStoredWalletId, persistWalletId } from '../utils/auth';
import { formatDate, formatMoney, generateIdempotencyKey, walletIdOf } from '../utils/format';
import { Button, EmptyState, Field, LoadingBlock, PageHeader, SectionCard, StatusBadge } from '../components/UI';

function loadRazorpayCheckout() {
  if (window.Razorpay) return Promise.resolve();

  return new Promise((resolve, reject) => {
    const existing = document.querySelector('script[data-phonewallet-razorpay="true"]');
    if (existing) {
      existing.addEventListener('load', () => resolve(), { once: true });
      existing.addEventListener('error', () => reject(new Error('Unable to load Razorpay Checkout.')), { once: true });
      return;
    }

    const script = document.createElement('script');
    script.src = 'https://checkout.razorpay.com/v1/checkout.js';
    script.async = true;
    script.dataset.phonewalletRazorpay = 'true';
    script.onload = () => resolve();
    script.onerror = () => reject(new Error('Unable to load Razorpay Checkout.'));
    document.body.appendChild(script);
  });
}

function openRazorpayCheckout({ keyId, intent, username }) {
  return new Promise((resolve, reject) => {
    const amountInSubunits = Math.round(Number(intent.amount) * 100);

    const checkout = new window.Razorpay({
      key: keyId,
      amount: amountInSubunits,
      currency: intent.currency,
      name: 'PhoneWallet',
      description: 'Add money to wallet',
      order_id: intent.providerOrderId,
      prefill: username ? { name: username } : undefined,
      notes: {
        walletId: String(intent.walletId),
        topUpIntentId: String(intent.id),
      },
      retry: { enabled: true },
      handler: (response) => resolve(response),
      modal: {
        ondismiss: () => reject(new Error('Payment cancelled.')),
      },
    });

    checkout.on('payment.failed', (response) => {
      const message = response?.error?.description || 'Payment failed.';
      reject(new Error(message));
    });

    checkout.open();
  });
}

export default function WalletPage() {
  const { auth, role } = useAuth();
  const { showToast } = useToast();
  const [walletId, setWalletId] = useState(() => getStoredWalletId(auth?.userId));
  const [wallets, setWallets] = useState([]);
  const [wallet, setWallet] = useState(null);
  const [balance, setBalance] = useState(null);
  const [currency, setCurrency] = useState('INR');
  const [topUpForm, setTopUpForm] = useState({ amount: '' });
  const [topUpResult, setTopUpResult] = useState(null);
  const [topUps, setTopUps] = useState([]);
  const [loading, setLoading] = useState(true);
  const [actionLoading, setActionLoading] = useState('');
  const [error, setError] = useState('');

  const selectedId = walletId ? String(walletId) : '';

  async function loadMine(preferredWalletId = walletId) {
    setLoading(true);
    setError('');
    try {
      const mine = await walletApi.mine();
      const owned = Array.isArray(mine) ? mine : [];
      setWallets(owned);
      const ownedIds = new Set(owned.map((item) => String(walletIdOf(item))));
      let selected = preferredWalletId && ownedIds.has(String(preferredWalletId)) ? String(preferredWalletId) : null;
      if (!selected && owned.length) selected = String(walletIdOf(owned[0]));
      if (selected) {
        persistWalletId(auth?.userId, selected);
        setWalletId(selected);
        await refreshSelected(selected, owned);
      } else {
        clearStoredWalletId(auth?.userId);
        setWalletId(null);
        setWallet(null);
        setBalance(null);
        setTopUps([]);
      }
    } catch (err) {
      setError(apiErrorMessage(err));
    } finally {
      setLoading(false);
    }
  }

  async function refreshSelected(id = walletId, knownWallets = wallets) {
    if (!id) return;
    const known = knownWallets.find((item) => String(walletIdOf(item)) === String(id));
    try {
      const [walletData, balanceData, topUpData] = await Promise.all([
        known ? Promise.resolve(known) : walletApi.get(id),
        walletApi.balance(id),
        role === 'USER' ? topUpApi.byWallet(id, 0, 10) : Promise.resolve({ content: [] }),
      ]);
      setWallet(walletData);
      setBalance(balanceData);
      setTopUps(pageContent(topUpData));
      setTopUpForm({ amount: '' });
    } catch (err) {
      setError(apiErrorMessage(err));
      throw err;
    }
  }

  useEffect(() => { loadMine(); }, [auth?.userId]);

  async function chooseWallet(id) {
    persistWalletId(auth?.userId, id);
    setWalletId(String(id));
    setTopUpResult(null);
    setLoading(true);
    setError('');
    try { await refreshSelected(String(id)); }
    finally { setLoading(false); }
  }

  async function create(event) {
    event.preventDefault();
    setActionLoading('create');
    try {
      const data = await walletApi.create({ currency });
      const id = walletIdOf(data);
      if (!id) throw new Error('Wallet was created, but its ID was not returned.');
      showToast('Your new wallet is ready.');
      await loadMine(id);
    } catch (err) {
      showToast(apiErrorMessage(err), 'error');
    } finally {
      setActionLoading('');
    }
  }

  async function initiateTopUp(event) {
    event.preventDefault();
    if (!walletId || role !== 'USER') return;
    setActionLoading('topup');
    setTopUpResult(null);
    try {
      const intent = await topUpApi.initiate(walletId, {
        amount: Number(topUpForm.amount),
        currency: wallet?.currency || 'INR',
        idempotencyKey: generateIdempotencyKey('topup'),
      });
      await loadRazorpayCheckout();
      const config = await topUpApi.checkoutConfig();

      const payment = await openRazorpayCheckout({
        keyId: config.keyId,
        intent,
        username: auth?.username,
      });

      showToast('Payment received. Verifying with Razorpay…', 'info');

      const completed = await topUpApi.verifyPayment(intent.id, {
        razorpayPaymentId: payment.razorpay_payment_id,
        razorpayOrderId: payment.razorpay_order_id,
        razorpaySignature: payment.razorpay_signature,
      });

      showToast('Payment verified. Money added successfully.');
      setTopUpResult(completed);
      setTopUpForm({ amount: '' });
      await refreshSelected(walletId);
    } catch (err) {
      showToast(apiErrorMessage(err), 'error');
    } finally {
      setActionLoading('');
    }
  }

  const activeWallets = useMemo(() => wallets.map((item) => ({ ...item, _id: walletIdOf(item) })), [wallets]);

  return (
    <>
      <PageHeader
        eyebrow="Wallet"
        title="Your money, in one place"
        description="View your balance, switch wallets and add money securely."
        actions={<Button variant="secondary" onClick={() => loadMine(walletId)} loading={loading}><RefreshCw size={16} /> Refresh</Button>}
      />

      {loading && !wallet ? <LoadingBlock label="Loading your wallet…" /> : error && !wallets.length ? <div className="error-panel">{error}</div> : (
        <>
          <div className="two-panel-grid wallet-setup-grid">
            <SectionCard title="Your wallets" subtitle="Choose which wallet you want to use.">
              {activeWallets.length ? (
                <div className="stack-form">
                  <Field label="Selected wallet">
                    <select value={selectedId} onChange={(e) => chooseWallet(e.target.value)}>
                      {activeWallets.map((item) => <option key={item._id} value={item._id}>{item.currency} wallet · •••• {String(item._id).padStart(4, '0').slice(-4)}</option>)}
                    </select>
                  </Field>
                  <div className="secure-operation-note"><ShieldCheck size={18} /><div><strong>Only your wallets appear here</strong><span>Wallet access is tied to your signed-in account.</span></div></div>
                </div>
              ) : <EmptyState icon={WalletCards} title="No wallet yet" description="Create your first wallet to start adding and sending money." />}
            </SectionCard>

            <SectionCard title="Create another wallet" subtitle="Set up a wallet in a supported currency.">
              <form className="stack-form" onSubmit={create}>
                <Field label="Currency"><select value={currency} onChange={(e) => setCurrency(e.target.value)}><option value="INR">INR — Indian Rupee</option><option value="USD">USD — US Dollar</option><option value="EUR">EUR — Euro</option></select></Field>
                <Button type="submit" loading={actionLoading === 'create'}><Plus size={17} /> Create wallet</Button>
              </form>
            </SectionCard>
          </div>

          {wallet && (
            <div className={`wallet-layout ${role !== 'USER' ? 'wallet-layout-single' : ''}`}>
              <div>
                <div className="wallet-detail-card premium-wallet-card">
                  <div className="wallet-detail-head"><div><span>Available balance</span><strong>{formatMoney(balance, wallet.currency)}</strong></div><div className="wallet-brand-icon"><WalletCards size={28} /></div></div>
                  <div className="wallet-number"><span>PHONEWALLET</span><strong>•••• •••• {String(walletId).padStart(4, '0').slice(-4)}</strong></div>
                  <div className="wallet-detail-foot"><span><ShieldCheck size={15} /> {wallet.status === 'ACTIVE' ? 'Ready to use' : wallet.status}</span><span>{wallet.currency}</span></div>
                </div>

                <SectionCard title="Wallet details" subtitle="Details for your selected wallet." className="wallet-meta-card">
                  {error && <div className="form-alert">{error}</div>}
                  <div className="detail-grid">
                    <div><span>Wallet number</span><strong>•••• {String(walletId).padStart(4, '0').slice(-4)}</strong></div>
                    <div><span>Currency</span><strong>{wallet.currency}</strong></div>
                    <div><span>Status</span><StatusBadge value={wallet.status || 'ACTIVE'} /></div>
                    <div><span>Member ID</span><strong>{auth?.userId ?? '—'}</strong></div>
                  </div>
                  <div className="inline-actions"><Button variant="secondary" onClick={() => navigator.clipboard?.writeText(String(walletId)).then(() => showToast('Wallet number copied.'))}><Copy size={16} /> Copy wallet number</Button></div>
                </SectionCard>
              </div>

              {role === 'USER' && (
                <div className="stack-form">
                  <SectionCard title="Add money" subtitle="Add funds to your selected wallet.">
                    <form className="stack-form" onSubmit={initiateTopUp}>
                      <Field label="Amount"><div className="money-input"><span>{wallet.currency === 'INR' ? '₹' : wallet.currency}</span><input required min="1" max="50000" step="0.01" type="number" value={topUpForm.amount} onChange={(e) => setTopUpForm({ amount: e.target.value })} placeholder="0.00" /></div></Field>
                      <div className="amount-chips">
                        {[500, 1000, 2000, 5000].map((value) => <button key={value} type="button" onClick={() => setTopUpForm({ amount: String(value) })}>+{wallet.currency === 'INR' ? `₹${value}` : value}</button>)}
                      </div>
                      <div className="idempotency-note"><ShieldCheck size={17} /><span>Your payment is checked before your wallet balance changes.</span></div>
                      <Button type="submit" loading={actionLoading === 'topup'} disabled={wallet.status !== 'ACTIVE'}><Banknote size={17} /> Add money</Button>
                    </form>
                    {topUpResult && <div className="success-panel"><ShieldCheck size={18} /><span><strong>{topUpResult.status === 'COMPLETED' ? 'Money added' : 'Top-up started'}</strong>{topUpResult.transactionReference ? ` · Reference ${topUpResult.transactionReference}` : ''}</span></div>}
                  </SectionCard>

                  <SectionCard title="Recent top-ups" subtitle="Your latest add-money activity.">
                    {topUps.length ? <div className="transaction-list">{topUps.map((item) => <div className="transaction-row" key={item.id}><div className="transaction-icon incoming"><Banknote size={19} /></div><div className="transaction-main"><strong>{formatMoney(item.amount, item.currency)}</strong><span>{formatDate(item.createdAt)}</span></div><div className="transaction-amount"><StatusBadge value={item.status} /></div></div>)}</div> : <EmptyState icon={Banknote} title="No top-ups yet" description="Money you add to this wallet will appear here." />}
                  </SectionCard>
                </div>
              )}
            </div>
          )}
        </>
      )}
    </>
  );
}
