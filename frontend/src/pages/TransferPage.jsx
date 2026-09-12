import { useEffect, useState } from 'react';
import { ArrowRight, CheckCircle2, Send, ShieldCheck, WalletCards } from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';
import { Button, Field, PageHeader, SectionCard } from '../components/UI';
import { apiErrorMessage } from '../services/api';
import { transactionApi, walletApi } from '../services/walletApi';
import { getStoredWalletId } from '../utils/auth';
import { formatMoney, generateIdempotencyKey } from '../utils/format';

export default function TransferPage() {
  const { auth } = useAuth();
  const { showToast } = useToast();
  const walletId = getStoredWalletId(auth?.userId);
  const [form, setForm] = useState({ toWalletId: '', amount: '', currency: 'INR' });
  const [loading, setLoading] = useState(false);
  const [result, setResult] = useState(null);

  useEffect(() => {
    if (walletId) walletApi.get(walletId).then((wallet) => setForm((f) => ({ ...f, currency: wallet?.currency || f.currency }))).catch(() => {});
  }, [walletId]);

  async function submit(event) {
    event.preventDefault();
    if (!walletId) return showToast('Create or select a wallet before sending money.', 'warning');
    setLoading(true); setResult(null);
    try {
      const data = await transactionApi.transfer({
        fromWalletId: Number(walletId),
        toWalletId: Number(form.toWalletId),
        amount: Number(form.amount),
        currency: form.currency,
        idempotencyKey: generateIdempotencyKey('transfer'),
      });
      setResult(data);
      setForm((f) => ({ ...f, toWalletId: '', amount: '' }));
      showToast('Money sent successfully.');
    } catch (err) { showToast(apiErrorMessage(err), 'error'); }
    finally { setLoading(false); }
  }

  return (
    <>
      <PageHeader eyebrow="Send money" title="Send to another wallet" description="Enter the recipient’s wallet number and the amount you want to send." />
      <div className="operation-layout">
        <SectionCard title="Transfer details" subtitle="Review the details carefully before sending.">
          <form className="stack-form" onSubmit={submit}>
            <div className="source-wallet-pill"><WalletCards size={18} /><div><span>Sending from</span><strong>PhoneWallet •••• {String(walletId || '').padStart(4, '0').slice(-4)}</strong></div><span>{form.currency}</span></div>
            <div className="transfer-direction"><div className="transfer-line" /><div className="transfer-arrow"><ArrowRight size={18} /></div></div>
            <Field label="Recipient wallet number"><input required inputMode="numeric" value={form.toWalletId} onChange={(e) => setForm({ ...form, toWalletId: e.target.value })} placeholder="Enter wallet number" /></Field>
            <Field label="Amount"><div className="money-input"><span>{form.currency === 'INR' ? '₹' : form.currency}</span><input required min="0.01" step="0.01" type="number" value={form.amount} onChange={(e) => setForm({ ...form, amount: e.target.value })} placeholder="0.00" /></div></Field>
            <div className="secure-operation-note"><ShieldCheck size={18} /><div><strong>Protected transfer</strong><span>PhoneWallet checks the request before money is moved.</span></div></div>
            <Button type="submit" loading={loading} disabled={!walletId}><Send size={17} /> Send money</Button>
          </form>
        </SectionCard>

        <SectionCard title="Transfer receipt" subtitle="Your confirmation will appear here after the transfer.">
          {result ? (
            <div className="receipt-card">
              <div className="receipt-success"><div className="receipt-check"><CheckCircle2 size={24} /></div><span>Transfer complete</span><strong>{formatMoney(result.amount, result.currency)}</strong></div>
              <div className="receipt-rows">
                <div><span>Reference</span><strong>{result.transactionReference || '—'}</strong></div>
                <div><span>Recipient wallet</span><strong>•••• {String(result.toWalletId || '').padStart(4, '0').slice(-4)}</strong></div>
                <div><span>Status</span><strong>{result.status || '—'}</strong></div>
              </div>
            </div>
          ) : (
            <div className="operation-preview"><div className="preview-icon"><Send size={28} /></div><h3>No transfer yet</h3><p>Complete the form to send money. Your confirmation and reference will appear here.</p></div>
          )}
        </SectionCard>
      </div>
    </>
  );
}
