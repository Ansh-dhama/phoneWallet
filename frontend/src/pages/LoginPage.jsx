import { useState } from 'react';
import { Link, Navigate, useLocation, useNavigate } from 'react-router-dom';
import { ArrowRight, CheckCircle2, Eye, EyeOff, LockKeyhole, ShieldCheck, Sparkles, WalletCards } from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { apiErrorMessage } from '../services/api';
import { Button, Field } from '../components/UI';

export default function LoginPage() {
  const { isAuthenticated, login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [form, setForm] = useState({ username: '', password: '' });
  const [showPassword, setShowPassword] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  if (isAuthenticated) return <Navigate to="/dashboard" replace />;

  async function handleSubmit(event) {
    event.preventDefault();
    setLoading(true);
    setError('');
    try {
      const session = await login(form.username.trim(), form.password);
      const fallback = session?.role === 'ADMIN' ? '/admin' : '/dashboard';
      navigate(location.state?.from || fallback, { replace: true });
    } catch (err) {
      setError(apiErrorMessage(err));
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="auth-page">
      <section className="auth-visual">
        <div className="auth-visual-inner">
          <div className="auth-brand"><div className="brand-mark light"><WalletCards size={22} /></div><strong>PhoneWallet</strong></div>
          <div className="auth-hero-copy">
            <span className="auth-kicker"><Sparkles size={15} /> Everyday money, made simple</span>
            <h1>Move money with confidence.</h1>
            <p>Keep your balance, transfers, payments and transaction history together in one secure wallet.</p>
          </div>
          <div className="auth-benefits">
            <div><CheckCircle2 size={18} /><span>Fast wallet-to-wallet transfers</span></div>
            <div><CheckCircle2 size={18} /><span>Protected payments and transaction history</span></div>
            <div><CheckCircle2 size={18} /><span>Clear balance and statement tracking</span></div>
          </div>
          <div className="auth-card-preview">
            <div className="auth-card-top"><span>PHONEWALLET</span><ShieldCheck size={22} /></div>
            <div><small>AVAILABLE BALANCE</small><strong>₹ ••••••</strong></div>
            <div className="auth-card-footer"><span>Secure wallet</span><span>•••• 8090</span></div>
          </div>
        </div>
      </section>

      <section className="auth-form-side">
        <div className="auth-form-card">
          <div className="mobile-auth-brand"><div className="brand-mark"><WalletCards size={20} /></div><strong>PhoneWallet</strong></div>
          <span className="eyebrow">Welcome back</span>
          <h2>Sign in to PhoneWallet</h2>
          <p className="auth-subtitle">Access your wallet, activity and payments.</p>

          {error && <div className="form-alert"><LockKeyhole size={18} /><span>{error}</span></div>}

          <form onSubmit={handleSubmit} className="auth-form">
            <Field label="Username">
              <input autoFocus required autoComplete="username" value={form.username} onChange={(e) => setForm({ ...form, username: e.target.value })} placeholder="Your username" />
            </Field>
            <Field label="Password">
              <div className="input-with-action">
                <input required autoComplete="current-password" type={showPassword ? 'text' : 'password'} value={form.password} onChange={(e) => setForm({ ...form, password: e.target.value })} placeholder="Your password" />
                <button type="button" className="input-action" onClick={() => setShowPassword((v) => !v)} aria-label={showPassword ? 'Hide password' : 'Show password'}>{showPassword ? <EyeOff size={18} /> : <Eye size={18} />}</button>
              </div>
            </Field>
            <Button loading={loading} type="submit" className="full-button">Sign in <ArrowRight size={17} /></Button>
          </form>

          <div className="auth-switch">Don’t have an account? <Link to="/signup">Create one</Link></div>
          <div className="auth-security-note"><ShieldCheck size={16} /><span>Protected connection. Your sign-in details are sent securely.</span></div>
        </div>
      </section>
    </div>
  );
}
