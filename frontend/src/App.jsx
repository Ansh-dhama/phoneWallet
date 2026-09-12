import { Navigate, Route, Routes } from 'react-router-dom';
import AppShell from './components/AppShell';
import ProtectedRoute from './components/ProtectedRoute';
import { useAuth } from './context/AuthContext';
import AdminPage from './pages/AdminPage';
import AccountPage from './pages/AccountPage';
import DashboardPage from './pages/DashboardPage';
import LoginPage from './pages/LoginPage';
import PayPage from './pages/PayPage';
import RefundPage from './pages/RefundPage';
import SignupPage from './pages/SignupPage';
import StatementsPage from './pages/StatementsPage';
import TransactionsPage from './pages/TransactionsPage';
import TransferPage from './pages/TransferPage';
import WalletPage from './pages/WalletPage';

function HomeRedirect() {
  const { isAuthenticated, role } = useAuth();
  if (!isAuthenticated) return <Navigate to="/login" replace />;
  return <Navigate to={role === 'ADMIN' ? '/admin' : '/dashboard'} replace />;
}

export default function App() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route path="/signup" element={<SignupPage />} />

      <Route element={<ProtectedRoute />}>
        <Route element={<AppShell />}>
          <Route path="/account" element={<AccountPage />} />

          <Route element={<ProtectedRoute roles={['USER', 'MERCHANT']} />}>
            <Route path="/dashboard" element={<DashboardPage />} />
            <Route path="/wallet" element={<WalletPage />} />
            <Route path="/transactions" element={<TransactionsPage />} />
            <Route path="/statements" element={<StatementsPage />} />
          </Route>

          <Route element={<ProtectedRoute roles={['USER']} />}>
            <Route path="/transfer" element={<TransferPage />} />
            <Route path="/pay" element={<PayPage />} />
          </Route>

          <Route element={<ProtectedRoute roles={['MERCHANT', 'ADMIN']} />}>
            <Route path="/refund" element={<RefundPage />} />
          </Route>

          <Route element={<ProtectedRoute roles={['ADMIN']} />}>
            <Route path="/admin" element={<AdminPage />} />
          </Route>
        </Route>
      </Route>

      <Route path="/" element={<HomeRedirect />} />
      <Route path="*" element={<HomeRedirect />} />
    </Routes>
  );
}
