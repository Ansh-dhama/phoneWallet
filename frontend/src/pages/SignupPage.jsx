import { useState } from 'react';
import { ArrowRight, BadgeCheck, CheckCircle2, Eye, EyeOff, ShieldCheck, WalletCards } from 'lucide-react';
import { Link, Navigate, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';
import { apiErrorMessage } from '../services/api';
import { authApi } from '../services/walletApi';
import { Button, Field } from '../components/UI';

export default function SignupPage() {
  const { isAuthenticated } = useAuth();
  const { showToast } = useToast();
  const navigate = useNavigate();
  const [showPassword, setShowPassword] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [form, setForm] = useState({ username: '', password: '', mobile: '' });

  if (isAuthenticated) return <Navigate to="/dashboard" replace />;

  async function submit(event) {
    event.preventDefault();
    setLoading(true);
    setError('');
    try {
      await authApi.signup({ username: form.username.trim(), password: form.password, mobile: form.mobile.trim() });
      showToast('Your PhoneWallet account is ready. Sign in to continue.');
      navigate('/login');
    } catch (err) {
      setError(apiErrorMessage(err));
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="auth-page auth-page-signup">
      <section className="auth-visual signup-visual">
        <div className="auth-visual-inner">
          <div className="auth-brand"><div className="brand-mark light"><WalletCards size={22} /></div><strong>PhoneWallet</strong></div>
          <div className="auth-hero-copy">
            <span className="auth-kicker"><BadgeCheck size={16} /> Start in a few moments</span>
            <h1>Your wallet starts here.</h1>
            <p>Create your personal PhoneWallet account, then add a wallet and start sending or paying securely.</p>
          </div>
          <div className="auth-benefits">
            <div><CheckCircle2 size={18} /><span>One clear home for your money</span></div>
            <div><CheckCircle2 size={18} /><span>Transaction history you can follow</span></div>
            <div><CheckCircle2 size={18} /><span>Built-in payment protection</span></div>
          </div>
        </div>
      </section>

      <section className="auth-form-side">
        <div className="auth-form-card auth-signup-card">
          <div className="mobile-auth-brand"><div className="brand-mark"><WalletCards size={20} /></div><strong>PhoneWallet</strong></div>
          <span className="eyebrow">Create account</span>
          <h2>Get started with PhoneWallet</h2>
          <p className="auth-subtitle">Create your personal account. You can set up your first wallet after signing in.</p>
          {error && <div className="form-alert"><ShieldCheck size={17} /><span>{error}</span></div>}
          <form onSubmit={submit} className="auth-form">
            <Field label="Username"><input required minLength="3" maxLength="64" autoComplete="username" value={form.username} onChange={(e) => setForm({ ...form, username: e.target.value })} placeholder="Choose a username" /></Field>
            <Field label="Mobile number"><input required inputMode="tel" autoComplete="tel" pattern="[6-9][0-9]{9}" value={form.mobile} onChange={(e) => setForm({ ...form, mobile: e.target.value })} placeholder="9876543210" /></Field>
            <Field label="Password" hint="Use at least 8 characters"><div className="input-with-action"><input required minLength="8" maxLength="72" autoComplete="new-password" type={showPassword ? 'text' : 'password'} value={form.password} onChange={(e) => setForm({ ...form, password: e.target.value })} placeholder="Create a strong password" /><button type="button" className="input-action" onClick={() => setShowPassword((v) => !v)} aria-label={showPassword ? 'Hide password' : 'Show password'}>{showPassword ? <EyeOff size={18} /> : <Eye size={18} />}</button></div></Field>
            <Button loading={loading} type="submit" className="full-button">Create account <ArrowRight size={17} /></Button>
          </form>
          <p className="consent-copy">By creating an account, you agree to use PhoneWallet for lawful wallet activity and keep your login details secure.</p>
          <div className="auth-switch">Already have an account? <Link to="/login">Sign in</Link></div>
        </div>
      </section>
    </div>
  );
}
