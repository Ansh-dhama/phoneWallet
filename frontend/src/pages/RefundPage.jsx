import { useState } from 'react';
import { RotateCcw, ShieldCheck } from 'lucide-react';
import { Button, Field, PageHeader, SectionCard } from '../components/UI';
import { useToast } from '../context/ToastContext';
import { transactionApi } from '../services/walletApi';
import { apiErrorMessage } from '../services/api';
import { generateIdempotencyKey } from '../utils/format';

export default function RefundPage() {
  const { showToast } = useToast();
  const [loading, setLoading] = useState(false);
  const [form, setForm] = useState({
    originalTransactionReference: '',
    refundAmount: '',
    refundReason: '',
    currency: 'INR',
  });

  async function submit(event) {
    event.preventDefault();
    setLoading(true);
    try {
      const response = await transactionApi.refund({
        originalTransactionReference: form.originalTransactionReference.trim(),
        refundAmount: Number(form.refundAmount),
        refundReason: form.refundReason.trim() || null,
        idempotencyKey: generateIdempotencyKey('refund'),
        currency: form.currency.trim().toUpperCase(),
      });
      showToast(`Refund ${response?.transactionReference || ''} processed successfully.`.trim());
      setForm({ originalTransactionReference: '', refundAmount: '', refundReason: '', currency: 'INR' });
    } catch (error) {
      showToast(apiErrorMessage(error), 'error');
    } finally {
      setLoading(false);
    }
  }

  return (
    <>
      <PageHeader
        eyebrow="Merchant operation"
        title="Refund a transaction"
        description="Issue an authorized refund against an existing transaction reference."
      />
      <div className="single-form-page">
        <SectionCard title="Refund details" subtitle="The idempotency key is generated automatically for every refund request.">
          <form className="stack-form" onSubmit={submit}>
            <Field label="Original transaction reference">
              <input
                required
                value={form.originalTransactionReference}
                onChange={(e) => setForm({ ...form, originalTransactionReference: e.target.value })}
                placeholder="Enter original transaction reference"
              />
            </Field>
            <div className="form-grid-2">
              <Field label="Refund amount">
                <input
                  required
                  min="0.01"
                  step="0.01"
                  type="number"
                  value={form.refundAmount}
                  onChange={(e) => setForm({ ...form, refundAmount: e.target.value })}
                  placeholder="0.00"
                />
              </Field>
              <Field label="Currency">
                <input
                  required
                  value={form.currency}
                  onChange={(e) => setForm({ ...form, currency: e.target.value.toUpperCase() })}
                  placeholder="INR"
                />
              </Field>
            </div>
            <Field label="Reason" hint="Optional">
              <textarea
                rows="4"
                value={form.refundReason}
                onChange={(e) => setForm({ ...form, refundReason: e.target.value })}
                placeholder="Reason for refund"
              />
            </Field>
            <div className="idempotency-note">
              <ShieldCheck size={17} />
              <span>A unique idempotency key will be sent with this refund request.</span>
            </div>
            <Button type="submit" loading={loading}>
              <RotateCcw size={17} /> Process refund
            </Button>
          </form>
        </SectionCard>
      </div>
    </>
  );
}
