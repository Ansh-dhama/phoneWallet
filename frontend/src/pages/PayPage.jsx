import { useEffect, useState } from 'react';
import { CheckCircle2, CreditCard, Landmark, Receipt, ShieldCheck } from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';
import { Button, Field, PageHeader, SectionCard } from '../components/UI';
import { apiErrorMessage } from '../services/api';
import { transactionApi, walletApi } from '../services/walletApi';
import { getStoredWalletId } from '../utils/auth';
import { formatMoney, generateIdempotencyKey } from '../utils/format';

export default function PayPage() {
  const { auth } = useAuth();
  const { showToast } = useToast();
  const walletId = getStoredWalletId(auth?.userId);
  const [form, setForm] = useState({ merchantWalletId: '', amount: '', currency: 'INR', merchantReference: '' });
  const [loading, setLoading] = useState(false);
  const [result, setResult] = useState(null);

  useEffect(() => {
    if (walletId) walletApi.get(walletId).then((wallet) => setForm((f) => ({ ...f, currency: wallet?.currency || f.currency }))).catch(() => {});
  }, [walletId]);

  async function submit(event) {
    event.preventDefault();
    if (!walletId) return showToast('Create or select a wallet before making a payment.', 'warning');
    setLoading(true); setResult(null);
    try {
      const data = await transactionApi.pay({
        fromWalletId: Number(walletId),
        toWalletId: Number(form.merchantWalletId),
        amount: Number(form.amount),
        currency: form.currency,
        idempotencyKey: generateIdempotencyKey('pay'),
        merchantReference: form.merchantReference || null,
      });
      setResult(data);
      showToast('Payment completed successfully.');
    } catch (err) { showToast(apiErrorMessage(err), 'error'); }
    finally { setLoading(false); }
  }

  return (
    <>
      <PageHeader eyebrow="Pay" title="Pay a business" description="Pay a verified business wallet and keep the receipt in your activity history." />
      <div className="operation-layout">
        <SectionCard title="Payment details" subtitle="Enter the business wallet number and amount.">
          <form className="stack-form" onSubmit={submit}>
            <Field label="Business wallet number"><div className="input-with-leading"><Landmark size={17} /><input required inputMode="numeric" value={form.merchantWalletId} onChange={(e) => setForm({ ...form, merchantWalletId: e.target.value })} placeholder="Enter business wallet number" /></div></Field>
            <Field label="Amount"><div className="money-input"><span>{form.currency === 'INR' ? '₹' : form.currency}</span><input required min="0.01" step="0.01" type="number" value={form.amount} onChange={(e) => setForm({ ...form, amount: e.target.value })} placeholder="0.00" /></div></Field>
            <Field label="Order or invoice reference" hint="Optional"><div className="input-with-leading"><Receipt size={17} /><input value={form.merchantReference} onChange={(e) => setForm({ ...form, merchantReference: e.target.value })} placeholder="e.g. INV-2026-001" /></div></Field>
            <div className="secure-operation-note"><ShieldCheck size={18} /><div><strong>Payment protection</strong><span>PhoneWallet checks the business wallet and protects against accidental duplicate payments.</span></div></div>
            <Button type="submit" loading={loading} disabled={!walletId}><CreditCard size={17} /> Pay now</Button>
          </form>
        </SectionCard>
        <SectionCard title="Payment receipt" subtitle="Your payment confirmation will appear here.">
          {result ? <div className="receipt-card"><div className="receipt-success"><div className="receipt-check"><CheckCircle2 size={24} /></div><span>Payment complete</span><strong>{formatMoney(result.amount, result.currency)}</strong></div><div className="receipt-rows"><div><span>Reference</span><strong>{result.transactionReference || '—'}</strong></div><div><span>Business wallet</span><strong>•••• {String(result.toWalletId || '').padStart(4, '0').slice(-4)}</strong></div><div><span>Status</span><strong>{result.status || '—'}</strong></div></div></div> : <div className="operation-preview"><div className="preview-icon"><CreditCard size={28} /></div><h3>Ready to pay</h3><p>Your confirmed amount and payment reference will appear here.</p></div>}
        </SectionCard>
      </div>
    </>
  );
}
