import { LockKeyhole, LogOut, ShieldCheck, Smartphone, UserRound, WalletCards } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { Button, PageHeader, SectionCard } from '../components/UI';

function roleName(role) {
  if (role === 'ADMIN') return 'Administrator';
  if (role === 'MERCHANT') return 'Business';
  return 'Personal';
}

export default function AccountPage() {
  const { auth, role, logout } = useAuth();
  const navigate = useNavigate();

  async function signOut() {
    await logout();
    navigate('/login', { replace: true });
  }

  return (
    <>
      <PageHeader eyebrow="Profile" title="Account & security" description="Manage the account you use to access PhoneWallet." />
      <div className="account-grid">
        <SectionCard className="account-profile-card">
          <div className="account-profile-hero">
            <div className="account-avatar"><UserRound size={30} /></div>
            <div><span>{roleName(role)} account</span><h2>{auth?.username || 'PhoneWallet member'}</h2><p>Member ID · {auth?.userId ?? '—'}</p></div>
          </div>
          <div className="account-info-list">
            <div><span><UserRound size={17} /> Username</span><strong>{auth?.username || '—'}</strong></div>
            <div><span><WalletCards size={17} /> Account type</span><strong>{roleName(role)}</strong></div>
            <div><span><Smartphone size={17} /> Device session</span><strong>Active on this browser</strong></div>
          </div>
        </SectionCard>

        <SectionCard title="Security" subtitle="Your session and payments are protected by the wallet service.">
          <div className="security-settings-list">
            <div className="security-setting"><div className="setting-icon"><ShieldCheck size={20} /></div><div><strong>Protected sign-in</strong><span>Your signed-in session is validated on every protected request.</span></div><span className="setting-state">On</span></div>
            <div className="security-setting"><div className="setting-icon"><LockKeyhole size={20} /></div><div><strong>Payment protection</strong><span>Duplicate transaction protection is applied automatically.</span></div><span className="setting-state">On</span></div>
          </div>
          <div className="account-danger-zone">
            <div><strong>Sign out of this device</strong><span>You’ll need your username and password to sign in again.</span></div>
            <Button variant="secondary" onClick={signOut}><LogOut size={16} /> Sign out</Button>
          </div>
        </SectionCard>
      </div>
    </>
  );
}
