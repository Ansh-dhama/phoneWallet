import { useMemo, useState } from 'react';
import { NavLink, Outlet, useLocation, useNavigate } from 'react-router-dom';
import {
  ArrowLeftRight,
  Bell,
  ChevronDown,
  CreditCard,
  FileText,
  LayoutDashboard,
  LogOut,
  Menu,
  ReceiptText,
  RotateCcw,
  Send,
  Settings,
  ShieldCheck,
  UserRound,
  WalletCards,
  X,
} from 'lucide-react';
import { useAuth } from '../context/AuthContext';

const consumerItems = [
  { to: '/dashboard', label: 'Home', icon: LayoutDashboard },
  { to: '/wallet', label: 'Wallet', icon: WalletCards },
  { to: '/transfer', label: 'Send money', icon: Send },
  { to: '/pay', label: 'Pay', icon: CreditCard },
  { to: '/transactions', label: 'Activity', icon: ArrowLeftRight },
  { to: '/statements', label: 'Statements', icon: ReceiptText },
  { to: '/account', label: 'Account', icon: UserRound },
];

const merchantItems = [
  { to: '/dashboard', label: 'Home', icon: LayoutDashboard },
  { to: '/wallet', label: 'Wallet', icon: WalletCards },
  { to: '/transactions', label: 'Activity', icon: ArrowLeftRight },
  { to: '/statements', label: 'Statements', icon: ReceiptText },
  { to: '/refund', label: 'Refunds', icon: RotateCcw },
  { to: '/account', label: 'Account', icon: UserRound },
];

const adminItems = [
  { to: '/admin', label: 'Operations', icon: ShieldCheck },
  { to: '/account', label: 'Account', icon: UserRound },
];

function friendlyRole(role) {
  if (role === 'ADMIN') return 'Administrator';
  if (role === 'MERCHANT') return 'Business account';
  return 'Personal account';
}

function initials(value) {
  const text = String(value || 'PW').trim();
  return text.slice(0, 2).toUpperCase();
}

export default function AppShell() {
  const { auth, role, logout } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [mobileOpen, setMobileOpen] = useState(false);
  const [profileOpen, setProfileOpen] = useState(false);

  const items = useMemo(() => {
    if (role === 'ADMIN') return adminItems;
    if (role === 'MERCHANT') return merchantItems;
    return consumerItems;
  }, [role]);

  const bottomItems = role === 'USER'
    ? consumerItems.filter((item) => ['/dashboard', '/wallet', '/transfer', '/transactions', '/account'].includes(item.to))
    : items.slice(0, 5);

  async function handleLogout() {
    await logout();
    navigate('/login', { replace: true });
  }

  const currentLabel = items.find((item) => location.pathname.startsWith(item.to))?.label || 'PhoneWallet';

  return (
    <div className="app-shell">
      <aside className={`sidebar ${mobileOpen ? 'sidebar-open' : ''}`}>
        <div className="brand-row">
          <div className="brand-mark"><WalletCards size={21} /></div>
          <div className="brand-copy"><strong>PhoneWallet</strong><span>Simple. Secure. Yours.</span></div>
          <button className="icon-button sidebar-close" onClick={() => setMobileOpen(false)} aria-label="Close menu"><X size={20} /></button>
        </div>

        <div className="sidebar-account-card">
          <div className="sidebar-avatar">{initials(auth?.username)}</div>
          <div><strong>{auth?.username || 'Wallet member'}</strong><span>{friendlyRole(role)}</span></div>
        </div>

        <div className="sidebar-label">Menu</div>
        <nav className="side-nav">
          {items.map(({ to, label, icon: Icon }) => (
            <NavLink
              key={to}
              to={to}
              onClick={() => setMobileOpen(false)}
              className={({ isActive }) => (isActive ? 'nav-item active' : 'nav-item')}
            >
              <Icon size={18} />
              <span>{label}</span>
            </NavLink>
          ))}
        </nav>

        <div className="security-card">
          <div className="security-icon"><ShieldCheck size={19} /></div>
          <div><strong>Your account is protected</strong><span>Secure sign-in and protected payments.</span></div>
        </div>
      </aside>

      {mobileOpen && <button className="sidebar-overlay" aria-label="Close navigation" onClick={() => setMobileOpen(false)} />}

      <div className="main-column">
        <header className="topbar">
          <div className="topbar-left">
            <button className="icon-button menu-button" onClick={() => setMobileOpen(true)} aria-label="Open menu"><Menu size={21} /></button>
            <div className="topbar-title-wrap"><span>PhoneWallet</span><strong>{currentLabel}</strong></div>
          </div>
          <div className="topbar-actions">
            <div className="system-status"><span className="status-dot" /> Secure session</div>
            <button className="icon-button notification-button" aria-label="Notifications"><Bell size={19} /></button>
            <div className="profile-menu-wrap">
              <button className="profile-button" onClick={() => setProfileOpen((value) => !value)}>
                <div className="avatar">{initials(auth?.username)}</div>
                <span className="profile-copy"><strong>{auth?.username || 'Member'}</strong><small>{friendlyRole(role)}</small></span>
                <ChevronDown size={16} />
              </button>
              {profileOpen && (
                <div className="profile-dropdown">
                  <div className="profile-dropdown-head">
                    <strong>{auth?.username || 'Wallet member'}</strong>
                    <span>{friendlyRole(role)}</span>
                  </div>
                  <button onClick={() => { setProfileOpen(false); navigate('/account'); }}><Settings size={16} /> Account & security</button>
                  <button className="danger-menu-action" onClick={handleLogout}><LogOut size={16} /> Sign out</button>
                </div>
              )}
            </div>
          </div>
        </header>

        <main className="page-content"><Outlet /></main>
        <footer className="app-footer"><span>© 2026 PhoneWallet</span><span><FileText size={14} /> Secure digital wallet</span></footer>
      </div>

      <nav className="mobile-bottom-nav" aria-label="Primary navigation">
        {bottomItems.map(({ to, label, icon: Icon }) => (
          <NavLink key={to} to={to} className={({ isActive }) => (isActive ? 'mobile-nav-item active' : 'mobile-nav-item')}>
            <Icon size={20} /><span>{label}</span>
          </NavLink>
        ))}
      </nav>
    </div>
  );
}
