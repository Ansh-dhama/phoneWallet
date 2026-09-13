(function(){
'use strict';
const __modules = Object.create(null);
__modules["App"]=function(module,exports,require){
"use strict";
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
Object.defineProperty(exports, "__esModule", { value: true });
exports.default = App;
const react_router_dom_1 = require("react-router-dom");
const AppShell_1 = __importDefault(require("./components/AppShell"));
const ProtectedRoute_1 = __importDefault(require("./components/ProtectedRoute"));
const AuthContext_1 = require("./context/AuthContext");
const AdminPage_1 = __importDefault(require("./pages/AdminPage"));
const AccountPage_1 = __importDefault(require("./pages/AccountPage"));
const DashboardPage_1 = __importDefault(require("./pages/DashboardPage"));
const LoginPage_1 = __importDefault(require("./pages/LoginPage"));
const PayPage_1 = __importDefault(require("./pages/PayPage"));
const RefundPage_1 = __importDefault(require("./pages/RefundPage"));
const SignupPage_1 = __importDefault(require("./pages/SignupPage"));
const StatementsPage_1 = __importDefault(require("./pages/StatementsPage"));
const TransactionsPage_1 = __importDefault(require("./pages/TransactionsPage"));
const TransferPage_1 = __importDefault(require("./pages/TransferPage"));
const WalletPage_1 = __importDefault(require("./pages/WalletPage"));
function HomeRedirect() {
    const { isAuthenticated, role } = (0, AuthContext_1.useAuth)();
    if (!isAuthenticated)
        return React.createElement(react_router_dom_1.Navigate, { to: "/login", replace: true });
    return React.createElement(react_router_dom_1.Navigate, { to: role === 'ADMIN' ? '/admin' : '/dashboard', replace: true });
}
function App() {
    return (React.createElement(react_router_dom_1.Routes, null,
        React.createElement(react_router_dom_1.Route, { path: "/login", element: React.createElement(LoginPage_1.default, null) }),
        React.createElement(react_router_dom_1.Route, { path: "/signup", element: React.createElement(SignupPage_1.default, null) }),
        React.createElement(react_router_dom_1.Route, { element: React.createElement(ProtectedRoute_1.default, null) },
            React.createElement(react_router_dom_1.Route, { element: React.createElement(AppShell_1.default, null) },
                React.createElement(react_router_dom_1.Route, { path: "/account", element: React.createElement(AccountPage_1.default, null) }),
                React.createElement(react_router_dom_1.Route, { element: React.createElement(ProtectedRoute_1.default, { roles: ['USER', 'MERCHANT'] }) },
                    React.createElement(react_router_dom_1.Route, { path: "/dashboard", element: React.createElement(DashboardPage_1.default, null) }),
                    React.createElement(react_router_dom_1.Route, { path: "/wallet", element: React.createElement(WalletPage_1.default, null) }),
                    React.createElement(react_router_dom_1.Route, { path: "/transactions", element: React.createElement(TransactionsPage_1.default, null) }),
                    React.createElement(react_router_dom_1.Route, { path: "/statements", element: React.createElement(StatementsPage_1.default, null) })),
                React.createElement(react_router_dom_1.Route, { element: React.createElement(ProtectedRoute_1.default, { roles: ['USER'] }) },
                    React.createElement(react_router_dom_1.Route, { path: "/transfer", element: React.createElement(TransferPage_1.default, null) }),
                    React.createElement(react_router_dom_1.Route, { path: "/pay", element: React.createElement(PayPage_1.default, null) })),
                React.createElement(react_router_dom_1.Route, { element: React.createElement(ProtectedRoute_1.default, { roles: ['MERCHANT', 'ADMIN'] }) },
                    React.createElement(react_router_dom_1.Route, { path: "/refund", element: React.createElement(RefundPage_1.default, null) })),
                React.createElement(react_router_dom_1.Route, { element: React.createElement(ProtectedRoute_1.default, { roles: ['ADMIN'] }) },
                    React.createElement(react_router_dom_1.Route, { path: "/admin", element: React.createElement(AdminPage_1.default, null) })))),
        React.createElement(react_router_dom_1.Route, { path: "/", element: React.createElement(HomeRedirect, null) }),
        React.createElement(react_router_dom_1.Route, { path: "*", element: React.createElement(HomeRedirect, null) })));
}

};
__modules["components/AppShell"]=function(module,exports,require){
"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.default = AppShell;
const react_1 = require("react");
const react_router_dom_1 = require("react-router-dom");
const lucide_react_1 = require("lucide-react");
const AuthContext_1 = require("../context/AuthContext");
const consumerItems = [
    { to: '/dashboard', label: 'Home', icon: lucide_react_1.LayoutDashboard },
    { to: '/wallet', label: 'Wallet', icon: lucide_react_1.WalletCards },
    { to: '/transfer', label: 'Send money', icon: lucide_react_1.Send },
    { to: '/pay', label: 'Pay', icon: lucide_react_1.CreditCard },
    { to: '/transactions', label: 'Activity', icon: lucide_react_1.ArrowLeftRight },
    { to: '/statements', label: 'Statements', icon: lucide_react_1.ReceiptText },
    { to: '/account', label: 'Account', icon: lucide_react_1.UserRound },
];
const merchantItems = [
    { to: '/dashboard', label: 'Home', icon: lucide_react_1.LayoutDashboard },
    { to: '/wallet', label: 'Wallet', icon: lucide_react_1.WalletCards },
    { to: '/transactions', label: 'Activity', icon: lucide_react_1.ArrowLeftRight },
    { to: '/statements', label: 'Statements', icon: lucide_react_1.ReceiptText },
    { to: '/refund', label: 'Refunds', icon: lucide_react_1.RotateCcw },
    { to: '/account', label: 'Account', icon: lucide_react_1.UserRound },
];
const adminItems = [
    { to: '/admin', label: 'Operations', icon: lucide_react_1.ShieldCheck },
    { to: '/account', label: 'Account', icon: lucide_react_1.UserRound },
];
function friendlyRole(role) {
    if (role === 'ADMIN')
        return 'Administrator';
    if (role === 'MERCHANT')
        return 'Business account';
    return 'Personal account';
}
function initials(value) {
    const text = String(value || 'PW').trim();
    return text.slice(0, 2).toUpperCase();
}
function AppShell() {
    const { auth, role, logout } = (0, AuthContext_1.useAuth)();
    const navigate = (0, react_router_dom_1.useNavigate)();
    const location = (0, react_router_dom_1.useLocation)();
    const [mobileOpen, setMobileOpen] = (0, react_1.useState)(false);
    const [profileOpen, setProfileOpen] = (0, react_1.useState)(false);
    const items = (0, react_1.useMemo)(() => {
        if (role === 'ADMIN')
            return adminItems;
        if (role === 'MERCHANT')
            return merchantItems;
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
    return (React.createElement("div", { className: "app-shell" },
        React.createElement("aside", { className: `sidebar ${mobileOpen ? 'sidebar-open' : ''}` },
            React.createElement("div", { className: "brand-row" },
                React.createElement("div", { className: "brand-mark" },
                    React.createElement(lucide_react_1.WalletCards, { size: 21 })),
                React.createElement("div", { className: "brand-copy" },
                    React.createElement("strong", null, "PhoneWallet"),
                    React.createElement("span", null, "Simple. Secure. Yours.")),
                React.createElement("button", { className: "icon-button sidebar-close", onClick: () => setMobileOpen(false), "aria-label": "Close menu" },
                    React.createElement(lucide_react_1.X, { size: 20 }))),
            React.createElement("div", { className: "sidebar-account-card" },
                React.createElement("div", { className: "sidebar-avatar" }, initials(auth?.username)),
                React.createElement("div", null,
                    React.createElement("strong", null, auth?.username || 'Wallet member'),
                    React.createElement("span", null, friendlyRole(role)))),
            React.createElement("div", { className: "sidebar-label" }, "Menu"),
            React.createElement("nav", { className: "side-nav" }, items.map(({ to, label, icon: Icon }) => (React.createElement(react_router_dom_1.NavLink, { key: to, to: to, onClick: () => setMobileOpen(false), className: ({ isActive }) => (isActive ? 'nav-item active' : 'nav-item') },
                React.createElement(Icon, { size: 18 }),
                React.createElement("span", null, label))))),
            React.createElement("div", { className: "security-card" },
                React.createElement("div", { className: "security-icon" },
                    React.createElement(lucide_react_1.ShieldCheck, { size: 19 })),
                React.createElement("div", null,
                    React.createElement("strong", null, "Your account is protected"),
                    React.createElement("span", null, "Secure sign-in and protected payments.")))),
        mobileOpen && React.createElement("button", { className: "sidebar-overlay", "aria-label": "Close navigation", onClick: () => setMobileOpen(false) }),
        React.createElement("div", { className: "main-column" },
            React.createElement("header", { className: "topbar" },
                React.createElement("div", { className: "topbar-left" },
                    React.createElement("button", { className: "icon-button menu-button", onClick: () => setMobileOpen(true), "aria-label": "Open menu" },
                        React.createElement(lucide_react_1.Menu, { size: 21 })),
                    React.createElement("div", { className: "topbar-title-wrap" },
                        React.createElement("span", null, "PhoneWallet"),
                        React.createElement("strong", null, currentLabel))),
                React.createElement("div", { className: "topbar-actions" },
                    React.createElement("div", { className: "system-status" },
                        React.createElement("span", { className: "status-dot" }),
                        " Secure session"),
                    React.createElement("button", { className: "icon-button notification-button", "aria-label": "Notifications" },
                        React.createElement(lucide_react_1.Bell, { size: 19 })),
                    React.createElement("div", { className: "profile-menu-wrap" },
                        React.createElement("button", { className: "profile-button", onClick: () => setProfileOpen((value) => !value) },
                            React.createElement("div", { className: "avatar" }, initials(auth?.username)),
                            React.createElement("span", { className: "profile-copy" },
                                React.createElement("strong", null, auth?.username || 'Member'),
                                React.createElement("small", null, friendlyRole(role))),
                            React.createElement(lucide_react_1.ChevronDown, { size: 16 })),
                        profileOpen && (React.createElement("div", { className: "profile-dropdown" },
                            React.createElement("div", { className: "profile-dropdown-head" },
                                React.createElement("strong", null, auth?.username || 'Wallet member'),
                                React.createElement("span", null, friendlyRole(role))),
                            React.createElement("button", { onClick: () => { setProfileOpen(false); navigate('/account'); } },
                                React.createElement(lucide_react_1.Settings, { size: 16 }),
                                " Account & security"),
                            React.createElement("button", { className: "danger-menu-action", onClick: handleLogout },
                                React.createElement(lucide_react_1.LogOut, { size: 16 }),
                                " Sign out")))))),
            React.createElement("main", { className: "page-content" },
                React.createElement(react_router_dom_1.Outlet, null)),
            React.createElement("footer", { className: "app-footer" },
                React.createElement("span", null, "\u00A9 2026 PhoneWallet"),
                React.createElement("span", null,
                    React.createElement(lucide_react_1.FileText, { size: 14 }),
                    " Secure digital wallet"))),
        React.createElement("nav", { className: "mobile-bottom-nav", "aria-label": "Primary navigation" }, bottomItems.map(({ to, label, icon: Icon }) => (React.createElement(react_router_dom_1.NavLink, { key: to, to: to, className: ({ isActive }) => (isActive ? 'mobile-nav-item active' : 'mobile-nav-item') },
            React.createElement(Icon, { size: 20 }),
            React.createElement("span", null, label)))))));
}

};
__modules["components/ProtectedRoute"]=function(module,exports,require){
"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.default = ProtectedRoute;
const react_router_dom_1 = require("react-router-dom");
const AuthContext_1 = require("../context/AuthContext");
function ProtectedRoute({ roles }) {
    const { isAuthenticated, role } = (0, AuthContext_1.useAuth)();
    const location = (0, react_router_dom_1.useLocation)();
    if (!isAuthenticated) {
        return React.createElement(react_router_dom_1.Navigate, { to: "/login", replace: true, state: { from: location.pathname } });
    }
    if (roles?.length && !roles.includes(role)) {
        return React.createElement(react_router_dom_1.Navigate, { to: role === 'ADMIN' ? '/admin' : '/dashboard', replace: true });
    }
    return React.createElement(react_router_dom_1.Outlet, null);
}

};
__modules["components/UI"]=function(module,exports,require){
"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.PageHeader = PageHeader;
exports.StatCard = StatCard;
exports.SectionCard = SectionCard;
exports.Field = Field;
exports.Button = Button;
exports.StatusBadge = StatusBadge;
exports.EmptyState = EmptyState;
exports.LoadingBlock = LoadingBlock;
exports.SearchBox = SearchBox;
exports.Modal = Modal;
exports.SmartTable = SmartTable;
exports.Pagination = Pagination;
const lucide_react_1 = require("lucide-react");
const format_1 = require("../utils/format");
function PageHeader({ eyebrow, title, description, actions }) {
    return (React.createElement("div", { className: "page-header" },
        React.createElement("div", null,
            eyebrow && React.createElement("div", { className: "eyebrow" }, eyebrow),
            React.createElement("h1", null, title),
            description && React.createElement("p", null, description)),
        actions && React.createElement("div", { className: "page-actions" }, actions)));
}
function StatCard({ label, value, hint, icon: Icon, tone = 'neutral' }) {
    return (React.createElement("article", { className: `stat-card tone-${tone}` },
        React.createElement("div", { className: "stat-icon" }, Icon && React.createElement(Icon, { size: 21 })),
        React.createElement("div", { className: "stat-content" },
            React.createElement("span", null, label),
            React.createElement("strong", null, value),
            hint && React.createElement("small", null, hint))));
}
function SectionCard({ title, subtitle, action, children, className = '' }) {
    return (React.createElement("section", { className: `section-card ${className}` },
        (title || action) && (React.createElement("div", { className: "section-card-header" },
            React.createElement("div", null,
                title && React.createElement("h2", null, title),
                subtitle && React.createElement("p", null, subtitle)),
            action)),
        children));
}
function Field({ label, error, hint, children }) {
    return (React.createElement("label", { className: "field" },
        React.createElement("span", null, label),
        children,
        error ? React.createElement("small", { className: "field-error" }, error) : hint ? React.createElement("small", null, hint) : null));
}
function Button({ children, loading, variant = 'primary', className = '', ...props }) {
    return (React.createElement("button", { className: `button button-${variant} ${className}`, disabled: loading || props.disabled, ...props },
        loading && React.createElement(lucide_react_1.LoaderCircle, { className: "spin", size: 17 }),
        children));
}
function StatusBadge({ value }) {
    const text = String(value || 'UNKNOWN').toUpperCase();
    const positive = ['SUCCESS', 'SUCCESSFUL', 'ACTIVE', 'COMPLETED', 'SENT'].includes(text);
    const negative = ['FAILED', 'BLACKLISTED', 'FROZEN', 'REJECTED'].includes(text);
    const warning = ['PENDING', 'PROCESSING', 'REFUNDED', 'PARTIALLY_REFUNDED', 'REVERSED'].includes(text);
    const tone = positive ? 'success' : negative ? 'danger' : warning ? 'warning' : 'neutral';
    return React.createElement("span", { className: `status-badge status-${tone}` }, text);
}
function EmptyState({ icon: Icon, title, description, action }) {
    return (React.createElement("div", { className: "empty-state" },
        Icon && (React.createElement("div", { className: "empty-icon" },
            React.createElement(Icon, { size: 28 }))),
        React.createElement("h3", null, title),
        React.createElement("p", null, description),
        action));
}
function LoadingBlock({ label = 'Loading data…' }) {
    return (React.createElement("div", { className: "loading-block" },
        React.createElement(lucide_react_1.LoaderCircle, { className: "spin", size: 24 }),
        React.createElement("span", null, label)));
}
function SearchBox({ value, onChange, placeholder = 'Search…' }) {
    return (React.createElement("div", { className: "search-box" },
        React.createElement(lucide_react_1.Search, { size: 17 }),
        React.createElement("input", { value: value, onChange: (event) => onChange(event.target.value), placeholder: placeholder })));
}
function Modal({ open, title, description, onClose, children, footer }) {
    if (!open)
        return null;
    return (React.createElement("div", { className: "modal-backdrop", onMouseDown: onClose },
        React.createElement("div", { className: "modal-card", onMouseDown: (event) => event.stopPropagation() },
            React.createElement("div", { className: "modal-header" },
                React.createElement("div", null,
                    React.createElement("h3", null, title),
                    description && React.createElement("p", null, description)),
                React.createElement("button", { className: "icon-button modal-close", onClick: onClose, "aria-label": "Close" }, "\u00D7")),
            React.createElement("div", { className: "modal-body" }, children),
            footer && React.createElement("div", { className: "modal-footer" }, footer))));
}
function SmartTable({ rows = [], actions, maxColumns = 7 }) {
    if (!rows.length)
        return null;
    const ignored = new Set(['password', 'accessToken', 'refreshToken']);
    const keys = Array.from(rows.reduce((set, row) => {
        Object.keys(row || {}).forEach((key) => {
            if (!ignored.has(key) && typeof row[key] !== 'object')
                set.add(key);
        });
        return set;
    }, new Set())).slice(0, maxColumns);
    return (React.createElement("div", { className: "table-wrap" },
        React.createElement("table", null,
            React.createElement("thead", null,
                React.createElement("tr", null,
                    keys.map((key) => React.createElement("th", { key: key }, (0, format_1.humanizeKey)(key))),
                    actions && React.createElement("th", null, "Actions"))),
            React.createElement("tbody", null, rows.map((row, index) => (React.createElement("tr", { key: row?.id ?? row?.walletId ?? row?.transactionId ?? index },
                keys.map((key) => React.createElement("td", { key: key }, String(row?.[key] ?? '—'))),
                actions && React.createElement("td", { className: "table-actions" }, actions(row)))))))));
}
function Pagination({ meta, onPageChange, loading = false }) {
    if (!meta || Number(meta.totalPages || 0) <= 1)
        return null;
    return (React.createElement("div", { className: "pagination-bar" },
        React.createElement(Button, { variant: "secondary", disabled: loading || meta.first, onClick: () => onPageChange(Math.max(0, meta.page - 1)) }, "Previous"),
        React.createElement("span", null,
            "Page ",
            React.createElement("strong", null, meta.page + 1),
            " of ",
            React.createElement("strong", null, meta.totalPages),
            " \u00B7 ",
            meta.totalElements,
            " records"),
        React.createElement(Button, { variant: "secondary", disabled: loading || meta.last, onClick: () => onPageChange(meta.page + 1) }, "Next")));
}

};
__modules["context/AuthContext"]=function(module,exports,require){
"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.AuthProvider = AuthProvider;
exports.useAuth = useAuth;
const react_1 = require("react");
const walletApi_1 = require("../services/walletApi");
const auth_1 = require("../utils/auth");
const AuthContext = (0, react_1.createContext)(null);
function AuthProvider({ children }) {
    const [auth, setAuth] = (0, react_1.useState)(() => (0, auth_1.getStoredAuth)());
    (0, react_1.useEffect)(() => {
        const sync = () => setAuth((0, auth_1.getStoredAuth)());
        window.addEventListener('storage', sync);
        return () => window.removeEventListener('storage', sync);
    }, []);
    async function login(username, password) {
        const data = await walletApi_1.authApi.login({ username, password });
        const next = {
            accessToken: data.accessToken,
            refreshToken: data.refreshToken,
            userId: data.userId,
            username,
            role: (0, auth_1.extractRole)(data.accessToken),
        };
        (0, auth_1.persistAuth)(next);
        setAuth(next);
        return next;
    }
    async function logout() {
        const refreshToken = auth?.refreshToken;
        try {
            if (refreshToken)
                await walletApi_1.authApi.logout(refreshToken);
        }
        finally {
            (0, auth_1.clearStoredAuth)();
            setAuth(null);
        }
    }
    const value = (0, react_1.useMemo)(() => ({
        auth,
        isAuthenticated: Boolean(auth?.accessToken),
        role: auth?.role || 'USER',
        login,
        logout,
    }), [auth]);
    return React.createElement(AuthContext.Provider, { value: value }, children);
}
function useAuth() {
    const context = (0, react_1.useContext)(AuthContext);
    if (!context)
        throw new Error('useAuth must be used within AuthProvider');
    return context;
}

};
__modules["context/ToastContext"]=function(module,exports,require){
"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.ToastProvider = ToastProvider;
exports.useToast = useToast;
const react_1 = require("react");
const lucide_react_1 = require("lucide-react");
const ToastContext = (0, react_1.createContext)(null);
const icons = {
    success: lucide_react_1.CheckCircle2,
    error: lucide_react_1.XCircle,
    warning: lucide_react_1.CircleAlert,
    info: lucide_react_1.Info,
};
function ToastProvider({ children }) {
    const [toasts, setToasts] = (0, react_1.useState)([]);
    const dismiss = (0, react_1.useCallback)((id) => {
        setToasts((items) => items.filter((item) => item.id !== id));
    }, []);
    const showToast = (0, react_1.useCallback)((message, type = 'success') => {
        const id = Date.now() + Math.random();
        setToasts((items) => [...items, { id, message, type }]);
        window.setTimeout(() => dismiss(id), 4200);
    }, [dismiss]);
    const value = (0, react_1.useMemo)(() => ({ showToast }), [showToast]);
    return (React.createElement(ToastContext.Provider, { value: value },
        children,
        React.createElement("div", { className: "toast-stack", "aria-live": "polite" }, toasts.map((toast) => {
            const Icon = icons[toast.type] || lucide_react_1.Info;
            return (React.createElement("div", { className: `toast toast-${toast.type}`, key: toast.id },
                React.createElement(Icon, { size: 19 }),
                React.createElement("span", null, toast.message),
                React.createElement("button", { className: "icon-button", onClick: () => dismiss(toast.id), "aria-label": "Dismiss" },
                    React.createElement(lucide_react_1.X, { size: 16 }))));
        }))));
}
function useToast() {
    const context = (0, react_1.useContext)(ToastContext);
    if (!context)
        throw new Error('useToast must be used within ToastProvider');
    return context;
}

};
__modules["main"]=function(module,exports,require){
"use strict";
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
Object.defineProperty(exports, "__esModule", { value: true });
const react_1 = __importDefault(require("react"));
const client_1 = __importDefault(require("react-dom/client"));
const react_router_dom_1 = require("react-router-dom");
const App_1 = __importDefault(require("./App"));
const AuthContext_1 = require("./context/AuthContext");
const ToastContext_1 = require("./context/ToastContext");
require("./styles/global.css");
client_1.default.createRoot(document.getElementById('root')).render(react_1.default.createElement(react_1.default.StrictMode, null,
    react_1.default.createElement(react_router_dom_1.BrowserRouter, null,
        react_1.default.createElement(ToastContext_1.ToastProvider, null,
            react_1.default.createElement(AuthContext_1.AuthProvider, null,
                react_1.default.createElement(App_1.default, null))))));

};
__modules["pages/AccountPage"]=function(module,exports,require){
"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.default = AccountPage;
const lucide_react_1 = require("lucide-react");
const react_router_dom_1 = require("react-router-dom");
const AuthContext_1 = require("../context/AuthContext");
const UI_1 = require("../components/UI");
function roleName(role) {
    if (role === 'ADMIN')
        return 'Administrator';
    if (role === 'MERCHANT')
        return 'Business';
    return 'Personal';
}
function AccountPage() {
    const { auth, role, logout } = (0, AuthContext_1.useAuth)();
    const navigate = (0, react_router_dom_1.useNavigate)();
    async function signOut() {
        await logout();
        navigate('/login', { replace: true });
    }
    return (React.createElement(React.Fragment, null,
        React.createElement(UI_1.PageHeader, { eyebrow: "Profile", title: "Account & security", description: "Manage the account you use to access PhoneWallet." }),
        React.createElement("div", { className: "account-grid" },
            React.createElement(UI_1.SectionCard, { className: "account-profile-card" },
                React.createElement("div", { className: "account-profile-hero" },
                    React.createElement("div", { className: "account-avatar" },
                        React.createElement(lucide_react_1.UserRound, { size: 30 })),
                    React.createElement("div", null,
                        React.createElement("span", null,
                            roleName(role),
                            " account"),
                        React.createElement("h2", null, auth?.username || 'PhoneWallet member'),
                        React.createElement("p", null,
                            "Member ID \u00B7 ",
                            auth?.userId ?? '—'))),
                React.createElement("div", { className: "account-info-list" },
                    React.createElement("div", null,
                        React.createElement("span", null,
                            React.createElement(lucide_react_1.UserRound, { size: 17 }),
                            " Username"),
                        React.createElement("strong", null, auth?.username || '—')),
                    React.createElement("div", null,
                        React.createElement("span", null,
                            React.createElement(lucide_react_1.WalletCards, { size: 17 }),
                            " Account type"),
                        React.createElement("strong", null, roleName(role))),
                    React.createElement("div", null,
                        React.createElement("span", null,
                            React.createElement(lucide_react_1.Smartphone, { size: 17 }),
                            " Device session"),
                        React.createElement("strong", null, "Active on this browser")))),
            React.createElement(UI_1.SectionCard, { title: "Security", subtitle: "Your session and payments are protected by the wallet service." },
                React.createElement("div", { className: "security-settings-list" },
                    React.createElement("div", { className: "security-setting" },
                        React.createElement("div", { className: "setting-icon" },
                            React.createElement(lucide_react_1.ShieldCheck, { size: 20 })),
                        React.createElement("div", null,
                            React.createElement("strong", null, "Protected sign-in"),
                            React.createElement("span", null, "Your signed-in session is validated on every protected request.")),
                        React.createElement("span", { className: "setting-state" }, "On")),
                    React.createElement("div", { className: "security-setting" },
                        React.createElement("div", { className: "setting-icon" },
                            React.createElement(lucide_react_1.LockKeyhole, { size: 20 })),
                        React.createElement("div", null,
                            React.createElement("strong", null, "Payment protection"),
                            React.createElement("span", null, "Duplicate transaction protection is applied automatically.")),
                        React.createElement("span", { className: "setting-state" }, "On"))),
                React.createElement("div", { className: "account-danger-zone" },
                    React.createElement("div", null,
                        React.createElement("strong", null, "Sign out of this device"),
                        React.createElement("span", null, "You\u2019ll need your username and password to sign in again.")),
                    React.createElement(UI_1.Button, { variant: "secondary", onClick: signOut },
                        React.createElement(lucide_react_1.LogOut, { size: 16 }),
                        " Sign out"))))));
}

};
__modules["pages/AdminPage"]=function(module,exports,require){
"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.default = AdminPage;
const react_1 = require("react");
const lucide_react_1 = require("lucide-react");
const ToastContext_1 = require("../context/ToastContext");
const walletApi_1 = require("../services/walletApi");
const api_1 = require("../services/api");
const format_1 = require("../utils/format");
const UI_1 = require("../components/UI");
const tabs = [
    ['overview', 'Overview'],
    ['wallets', 'Wallets'],
    ['transactions', 'Transactions'],
    ['users', 'Users & roles'],
    ['audit', 'Audit logs'],
    ['notifications', 'Notifications'],
];
function AdminPage() {
    const { showToast } = (0, ToastContext_1.useToast)();
    const [activeTab, setActiveTab] = (0, react_1.useState)('overview');
    const [summary, setSummary] = (0, react_1.useState)(null);
    const [wallets, setWallets] = (0, react_1.useState)([]);
    const [walletMeta, setWalletMeta] = (0, react_1.useState)(null);
    const [transactions, setTransactions] = (0, react_1.useState)([]);
    const [transactionMeta, setTransactionMeta] = (0, react_1.useState)(null);
    const [users, setUsers] = (0, react_1.useState)([]);
    const [userMeta, setUserMeta] = (0, react_1.useState)(null);
    const [auditLogs, setAuditLogs] = (0, react_1.useState)([]);
    const [auditMeta, setAuditMeta] = (0, react_1.useState)(null);
    const [notifications, setNotifications] = (0, react_1.useState)([]);
    const [notificationMeta, setNotificationMeta] = (0, react_1.useState)(null);
    const [loading, setLoading] = (0, react_1.useState)(true);
    const [query, setQuery] = (0, react_1.useState)('');
    const [error, setError] = (0, react_1.useState)('');
    const [blacklistTarget, setBlacklistTarget] = (0, react_1.useState)(null);
    const [blacklistReason, setBlacklistReason] = (0, react_1.useState)('');
    const [refundTarget, setRefundTarget] = (0, react_1.useState)(null);
    const [refundForm, setRefundForm] = (0, react_1.useState)({ amount: '', currency: 'INR', reason: '' });
    const [reversalTarget, setReversalTarget] = (0, react_1.useState)(null);
    const [reversalReason, setReversalReason] = (0, react_1.useState)('');
    const [actionLoading, setActionLoading] = (0, react_1.useState)('');
    const [filterLoading, setFilterLoading] = (0, react_1.useState)(false);
    const [walletView, setWalletView] = (0, react_1.useState)('ALL');
    const [transactionView, setTransactionView] = (0, react_1.useState)('ALL');
    const [serverFilterValue, setServerFilterValue] = (0, react_1.useState)('');
    const [roleDrafts, setRoleDrafts] = (0, react_1.useState)({});
    async function loadOverview() {
        setLoading(true);
        setError('');
        try {
            const [summaryData, txData] = await Promise.all([walletApi_1.adminApi.summary(), walletApi_1.adminApi.transactions(0, 20)]);
            setSummary(summaryData);
            setTransactions((0, walletApi_1.pageContent)(txData));
            setTransactionMeta((0, walletApi_1.pageMeta)(txData));
        }
        catch (err) {
            setError((0, api_1.apiErrorMessage)(err));
        }
        finally {
            setLoading(false);
        }
    }
    (0, react_1.useEffect)(() => { loadOverview(); }, []);
    async function loadWallets(page = 0, view = walletView) {
        setFilterLoading(true);
        try {
            const data = view === 'FROZEN' ? await walletApi_1.adminApi.frozenWallets(page, 50)
                : view === 'BLACKLISTED' ? await walletApi_1.adminApi.blacklistedWallets(page, 50)
                    : await walletApi_1.adminApi.wallets(page, 50);
            setWallets((0, walletApi_1.pageContent)(data));
            setWalletMeta((0, walletApi_1.pageMeta)(data));
        }
        catch (err) {
            showToast((0, api_1.apiErrorMessage)(err), 'error');
        }
        finally {
            setFilterLoading(false);
        }
    }
    async function loadTransactions(page = 0, view = transactionView) {
        if ((view === 'STATUS' || view === 'TYPE') && !serverFilterValue.trim()) {
            showToast(`Enter a ${view.toLowerCase()} enum value first.`, 'error');
            return;
        }
        setFilterLoading(true);
        try {
            const value = serverFilterValue.trim().toUpperCase();
            const data = view === 'FAILED' ? await walletApi_1.adminApi.failedTransactions(page, 50)
                : view === 'STATUS' ? await walletApi_1.adminApi.transactionsByStatus(value, page, 50)
                    : view === 'TYPE' ? await walletApi_1.adminApi.transactionsByType(value, page, 50)
                        : await walletApi_1.adminApi.transactions(page, 50);
            setTransactions((0, walletApi_1.pageContent)(data));
            setTransactionMeta((0, walletApi_1.pageMeta)(data));
        }
        catch (err) {
            showToast((0, api_1.apiErrorMessage)(err), 'error');
        }
        finally {
            setFilterLoading(false);
        }
    }
    async function loadUsers(page = 0) {
        setFilterLoading(true);
        try {
            const data = await walletApi_1.adminApi.users(page, 50);
            const content = (0, walletApi_1.pageContent)(data);
            setUsers(content);
            setUserMeta((0, walletApi_1.pageMeta)(data));
            setRoleDrafts(Object.fromEntries(content.map((u) => [u.id, u.role])));
        }
        catch (err) {
            showToast((0, api_1.apiErrorMessage)(err), 'error');
        }
        finally {
            setFilterLoading(false);
        }
    }
    async function loadAudit(page = 0) {
        setFilterLoading(true);
        try {
            const data = await walletApi_1.adminApi.auditLogs(page, 50);
            setAuditLogs((0, walletApi_1.pageContent)(data));
            setAuditMeta((0, walletApi_1.pageMeta)(data));
        }
        catch (err) {
            showToast((0, api_1.apiErrorMessage)(err), 'error');
        }
        finally {
            setFilterLoading(false);
        }
    }
    async function loadNotifications(page = 0) {
        setFilterLoading(true);
        try {
            const data = await walletApi_1.adminApi.notifications(page, 50);
            setNotifications((0, walletApi_1.pageContent)(data));
            setNotificationMeta((0, walletApi_1.pageMeta)(data));
        }
        catch (err) {
            showToast((0, api_1.apiErrorMessage)(err), 'error');
        }
        finally {
            setFilterLoading(false);
        }
    }
    async function selectTab(tab) {
        setActiveTab(tab);
        setQuery('');
        if (tab === 'overview')
            return loadOverview();
        if (tab === 'wallets')
            return loadWallets(0);
        if (tab === 'transactions')
            return loadTransactions(0);
        if (tab === 'users')
            return loadUsers(0);
        if (tab === 'audit')
            return loadAudit(0);
        if (tab === 'notifications')
            return loadNotifications(0);
    }
    async function changeWalletView(view) { setWalletView(view); setQuery(''); await loadWallets(0, view); }
    async function changeTransactionView(view) { setTransactionView(view); setServerFilterValue(''); setQuery(''); if (view === 'ALL' || view === 'FAILED')
        await loadTransactions(0, view); }
    const filteredWallets = (0, react_1.useMemo)(() => wallets.filter((w) => JSON.stringify(w).toLowerCase().includes(query.toLowerCase())), [wallets, query]);
    const filteredTransactions = (0, react_1.useMemo)(() => transactions.filter((t) => JSON.stringify(t).toLowerCase().includes(query.toLowerCase())), [transactions, query]);
    const filteredUsers = (0, react_1.useMemo)(() => users.filter((u) => JSON.stringify(u).toLowerCase().includes(query.toLowerCase())), [users, query]);
    async function walletAction(name, wallet, handler) {
        const id = (0, format_1.walletIdOf)(wallet) ?? wallet?.walletId;
        if (!id)
            return showToast('Wallet ID could not be read from this API response.', 'error');
        setActionLoading(`${name}-${id}`);
        try {
            await handler(id);
            showToast(`Wallet #${id} updated successfully.`);
            await loadWallets(walletMeta?.page || 0);
            await loadOverview();
        }
        catch (err) {
            showToast((0, api_1.apiErrorMessage)(err), 'error');
        }
        finally {
            setActionLoading('');
        }
    }
    async function submitBlacklist(event) {
        event.preventDefault();
        const id = (0, format_1.walletIdOf)(blacklistTarget) ?? blacklistTarget?.walletId;
        setActionLoading(`blacklist-${id}`);
        try {
            await walletApi_1.walletApi.blacklist(id, blacklistReason);
            showToast(`Wallet #${id} blacklisted.`);
            setBlacklistTarget(null);
            setBlacklistReason('');
            await loadWallets(walletMeta?.page || 0);
            await loadOverview();
        }
        catch (err) {
            showToast((0, api_1.apiErrorMessage)(err), 'error');
        }
        finally {
            setActionLoading('');
        }
    }
    async function submitRefund(event) {
        event.preventDefault();
        const reference = refundTarget?.transactionReference ?? refundTarget?.transactionRef;
        setActionLoading('refund');
        try {
            await walletApi_1.transactionApi.refund({ originalTransactionReference: reference, refundAmount: Number(refundForm.amount), refundReason: refundForm.reason || null, idempotencyKey: (0, format_1.generateIdempotencyKey)('refund'), currency: refundForm.currency });
            showToast('Refund processed successfully.');
            setRefundTarget(null);
            setRefundForm({ amount: '', currency: 'INR', reason: '' });
            await loadTransactions(transactionMeta?.page || 0);
            await loadOverview();
        }
        catch (err) {
            showToast((0, api_1.apiErrorMessage)(err), 'error');
        }
        finally {
            setActionLoading('');
        }
    }
    async function submitReversal(event) {
        event.preventDefault();
        const reference = reversalTarget?.transactionReference ?? reversalTarget?.transactionRef;
        setActionLoading('reversal');
        try {
            await walletApi_1.transactionApi.reversal({ originalTransactionReference: reference, reason: reversalReason, idempotencyKey: (0, format_1.generateIdempotencyKey)('reversal') });
            showToast('Transaction reversed successfully.');
            setReversalTarget(null);
            setReversalReason('');
            await loadTransactions(transactionMeta?.page || 0);
            await loadOverview();
        }
        catch (err) {
            showToast((0, api_1.apiErrorMessage)(err), 'error');
        }
        finally {
            setActionLoading('');
        }
    }
    async function updateRole(user) {
        const nextRole = roleDrafts[user.id];
        if (!nextRole || nextRole === user.role)
            return;
        setActionLoading(`role-${user.id}`);
        try {
            const updated = await walletApi_1.adminApi.updateUserRole(user.id, nextRole);
            setUsers((rows) => rows.map((row) => row.id === updated.id ? updated : row));
            showToast(`User ${updated.username} is now ${updated.role}.`);
        }
        catch (err) {
            showToast((0, api_1.apiErrorMessage)(err), 'error');
        }
        finally {
            setActionLoading('');
        }
    }
    return (React.createElement(React.Fragment, null,
        React.createElement(UI_1.PageHeader, { eyebrow: "Administration", title: "Wallet operations console", description: "Production controls for users, wallets, transactions, audit and notifications.", actions: React.createElement(UI_1.Button, { variant: "secondary", onClick: () => selectTab(activeTab), loading: loading || filterLoading },
                React.createElement(lucide_react_1.RefreshCw, { size: 16 }),
                " Refresh data") }),
        React.createElement("div", { className: "admin-tabs" }, tabs.map(([key, label]) => React.createElement("button", { key: key, className: activeTab === key ? 'active' : '', onClick: () => selectTab(key) }, label))),
        error && React.createElement("div", { className: "warning-panel" },
            React.createElement(lucide_react_1.ShieldAlert, { size: 18 }),
            React.createElement("span", null, error)),
        loading && activeTab === 'overview' ? React.createElement(UI_1.LoadingBlock, { label: "Loading admin console\u2026" }) : (React.createElement(React.Fragment, null,
            activeTab === 'overview' && React.createElement(AdminOverview, { summary: summary, transactions: transactions }),
            activeTab === 'wallets' && (React.createElement(UI_1.SectionCard, { title: "Wallet management", subtitle: "Administrative freeze and blacklist controls. Blacklist actors are derived from the authenticated admin, never the client.", action: React.createElement("div", { className: "admin-filter-actions" },
                    React.createElement("select", { className: "toolbar-select", value: walletView, disabled: filterLoading, onChange: (e) => changeWalletView(e.target.value) },
                        React.createElement("option", { value: "ALL" }, "All wallets"),
                        React.createElement("option", { value: "FROZEN" }, "Frozen"),
                        React.createElement("option", { value: "BLACKLISTED" }, "Active blacklist records")),
                    React.createElement(UI_1.SearchBox, { value: query, onChange: setQuery, placeholder: "Search current page\u2026" })) }, filterLoading && !wallets.length ? React.createElement(UI_1.LoadingBlock, null) : filteredWallets.length ? React.createElement("div", { className: "table-card embedded" },
                React.createElement("div", { className: "table-wrap" },
                    React.createElement("table", null,
                        React.createElement("thead", null,
                            React.createElement("tr", null,
                                React.createElement("th", null, "Wallet"),
                                React.createElement("th", null, "User / reason"),
                                React.createElement("th", null, "Currency"),
                                React.createElement("th", null, "Balance"),
                                React.createElement("th", null, "Status"),
                                React.createElement("th", null, "Security controls"))),
                        React.createElement("tbody", null, filteredWallets.map((row, index) => { const id = (0, format_1.walletIdOf)(row) ?? row.walletId; const isBlocklist = row.reason != null && row.walletId != null && row.currency == null; const isFrozen = String(row.status || '').toUpperCase() === 'FROZEN'; return React.createElement("tr", { key: row.id ?? id ?? index },
                            React.createElement("td", null,
                                React.createElement("strong", null,
                                    "#",
                                    id ?? '—')),
                            React.createElement("td", null, isBlocklist ? row.reason : (row.userId ?? row.user?.id ?? '—')),
                            React.createElement("td", null, row.currency ?? '—'),
                            React.createElement("td", null, row.balance != null ? (0, format_1.formatMoney)(row.balance, row.currency || 'INR') : '—'),
                            React.createElement("td", null,
                                React.createElement(UI_1.StatusBadge, { value: isBlocklist ? (row.active ? 'BLACKLISTED' : 'INACTIVE') : (row.status || (isFrozen ? 'FROZEN' : 'ACTIVE')) })),
                            React.createElement("td", null, !isBlocklist ? React.createElement("div", { className: "table-actions" },
                                React.createElement(UI_1.Button, { variant: "tiny", loading: actionLoading === `freeze-${id}`, onClick: () => walletAction('freeze', row, walletApi_1.walletApi.freeze) },
                                    React.createElement(lucide_react_1.Snowflake, { size: 14 }),
                                    " Freeze"),
                                React.createElement(UI_1.Button, { variant: "tiny", loading: actionLoading === `unfreeze-${id}`, onClick: () => walletAction('unfreeze', row, walletApi_1.walletApi.unfreeze) },
                                    React.createElement(lucide_react_1.ShieldCheck, { size: 14 }),
                                    " Unfreeze"),
                                React.createElement(UI_1.Button, { variant: "tiny-danger", onClick: () => setBlacklistTarget(row) },
                                    React.createElement(lucide_react_1.Ban, { size: 14 }),
                                    " Blacklist"),
                                React.createElement(UI_1.Button, { variant: "tiny", loading: actionLoading === `unblacklist-${id}`, onClick: () => walletAction('unblacklist', row, walletApi_1.walletApi.unblacklist) },
                                    React.createElement(lucide_react_1.Undo2, { size: 14 }),
                                    " Restore")) : React.createElement(UI_1.Button, { variant: "tiny", loading: actionLoading === `unblacklist-${id}`, onClick: () => walletAction('unblacklist', row, walletApi_1.walletApi.unblacklist) },
                                React.createElement(lucide_react_1.Undo2, { size: 14 }),
                                " Remove blacklist"))); })))),
                React.createElement(UI_1.Pagination, { meta: walletMeta, loading: filterLoading, onPageChange: (p) => loadWallets(p) })) : React.createElement(UI_1.EmptyState, { icon: lucide_react_1.WalletCards, title: "No wallets found", description: "No rows match the current server view and search." }))),
            activeTab === 'transactions' && (React.createElement(UI_1.SectionCard, { title: "Transaction operations", subtitle: "Refunds use cumulative partial-refund accounting; reversals and refunds lock the original transaction before state changes.", action: React.createElement("div", { className: "admin-filter-actions admin-filter-wrap" },
                    React.createElement("select", { className: "toolbar-select", value: transactionView, disabled: filterLoading, onChange: (e) => changeTransactionView(e.target.value) },
                        React.createElement("option", { value: "ALL" }, "All transactions"),
                        React.createElement("option", { value: "FAILED" }, "Failed"),
                        React.createElement("option", { value: "STATUS" }, "Filter by status"),
                        React.createElement("option", { value: "TYPE" }, "Filter by type")),
                    (transactionView === 'STATUS' || transactionView === 'TYPE') && React.createElement(React.Fragment, null,
                        React.createElement("input", { className: "compact-filter-input", value: serverFilterValue, onChange: (e) => setServerFilterValue(e.target.value), placeholder: transactionView === 'STATUS' ? 'e.g. SUCCESS' : 'e.g. TRANSFER' }),
                        React.createElement(UI_1.Button, { variant: "tiny", loading: filterLoading, onClick: () => loadTransactions(0) }, "Apply")),
                    React.createElement(UI_1.SearchBox, { value: query, onChange: setQuery, placeholder: "Search current page\u2026" })) }, filterLoading && !transactions.length ? React.createElement(UI_1.LoadingBlock, null) : filteredTransactions.length ? React.createElement("div", { className: "table-card embedded" },
                React.createElement("div", { className: "table-wrap" },
                    React.createElement("table", null,
                        React.createElement("thead", null,
                            React.createElement("tr", null,
                                React.createElement("th", null, "Reference"),
                                React.createElement("th", null, "Type"),
                                React.createElement("th", null, "Amount"),
                                React.createElement("th", null, "Refunded"),
                                React.createElement("th", null, "From \u2192 To"),
                                React.createElement("th", null, "Status"),
                                React.createElement("th", null, "Created"),
                                React.createElement("th", null, "Actions"))),
                        React.createElement("tbody", null, filteredTransactions.map((tx, index) => React.createElement("tr", { key: tx.transactionId ?? tx.id ?? index },
                            React.createElement("td", null,
                                React.createElement("div", { className: "table-primary" },
                                    React.createElement("strong", null,
                                        "#",
                                        tx.transactionId ?? tx.id ?? '—'),
                                    React.createElement("span", null, tx.transactionReference ?? tx.transactionRef ?? '—'))),
                            React.createElement("td", null, tx.type ?? '—'),
                            React.createElement("td", null, (0, format_1.formatMoney)(tx.amount, tx.currency || 'INR')),
                            React.createElement("td", null, (0, format_1.formatMoney)(tx.refundedAmount || 0, tx.currency || 'INR')),
                            React.createElement("td", null,
                                tx.fromWalletId ?? 'External',
                                " \u2192 ",
                                tx.toWalletId ?? '—'),
                            React.createElement("td", null,
                                React.createElement(UI_1.StatusBadge, { value: tx.status })),
                            React.createElement("td", null, (0, format_1.formatDate)(tx.createdAt)),
                            React.createElement("td", null,
                                React.createElement("div", { className: "table-actions" },
                                    React.createElement(UI_1.Button, { variant: "tiny", disabled: !['SUCCESS', 'PARTIALLY_REFUNDED'].includes(String(tx.status)), onClick: () => { setRefundTarget(tx); const remaining = Math.max(0, Number(tx.amount || 0) - Number(tx.refundedAmount || 0)); setRefundForm({ amount: remaining || '', currency: tx.currency || 'INR', reason: '' }); } },
                                        React.createElement(lucide_react_1.RotateCcw, { size: 14 }),
                                        " Refund"),
                                    React.createElement(UI_1.Button, { variant: "tiny-danger", disabled: String(tx.status) !== 'SUCCESS', onClick: () => setReversalTarget(tx) },
                                        React.createElement(lucide_react_1.Undo2, { size: 14 }),
                                        " Reverse")))))))),
                React.createElement(UI_1.Pagination, { meta: transactionMeta, loading: filterLoading, onPageChange: (p) => loadTransactions(p) })) : React.createElement(UI_1.EmptyState, { icon: lucide_react_1.Activity, title: "No transactions found", description: "No transaction rows match the current server view." }))),
            activeTab === 'users' && (React.createElement(UI_1.SectionCard, { title: "User role administration", subtitle: "Public signup creates USER only. Admins may promote verified accounts to MERCHANT or return them to USER; ADMIN cannot be granted through this API.", action: React.createElement(UI_1.SearchBox, { value: query, onChange: setQuery, placeholder: "Search current page\u2026" }) }, filterLoading && !users.length ? React.createElement(UI_1.LoadingBlock, null) : filteredUsers.length ? React.createElement("div", { className: "table-card embedded" },
                React.createElement("div", { className: "table-wrap" },
                    React.createElement("table", null,
                        React.createElement("thead", null,
                            React.createElement("tr", null,
                                React.createElement("th", null, "User"),
                                React.createElement("th", null, "Mobile"),
                                React.createElement("th", null, "Current role"),
                                React.createElement("th", null, "Verified role change"))),
                        React.createElement("tbody", null, filteredUsers.map((user) => React.createElement("tr", { key: user.id },
                            React.createElement("td", null,
                                React.createElement("div", { className: "table-primary" },
                                    React.createElement("strong", null,
                                        "#",
                                        user.id,
                                        " \u00B7 ",
                                        user.username))),
                            React.createElement("td", null, user.mobile || '—'),
                            React.createElement("td", null,
                                React.createElement(UI_1.StatusBadge, { value: user.role })),
                            React.createElement("td", null, user.role === 'ADMIN' ? React.createElement("span", null, "Bootstrap/admin role is protected") : React.createElement("div", { className: "table-actions" },
                                React.createElement("select", { className: "toolbar-select role-select", value: roleDrafts[user.id] || user.role, onChange: (e) => setRoleDrafts((d) => ({ ...d, [user.id]: e.target.value })) },
                                    React.createElement("option", { value: "USER" }, "USER"),
                                    React.createElement("option", { value: "MERCHANT" }, "MERCHANT")),
                                React.createElement(UI_1.Button, { variant: "tiny", loading: actionLoading === `role-${user.id}`, disabled: (roleDrafts[user.id] || user.role) === user.role, onClick: () => updateRole(user) },
                                    React.createElement(lucide_react_1.UserCog, { size: 14 }),
                                    " Apply")))))))),
                React.createElement(UI_1.Pagination, { meta: userMeta, loading: filterLoading, onPageChange: loadUsers })) : React.createElement(UI_1.EmptyState, { icon: lucide_react_1.UsersRound, title: "No users found", description: "No users were returned on this page." }))),
            activeTab === 'audit' && React.createElement(UI_1.SectionCard, { title: "Audit logs", subtitle: "Server-paginated operational audit records." }, filterLoading && !auditLogs.length ? React.createElement(UI_1.LoadingBlock, null) : auditLogs.length ? React.createElement(React.Fragment, null,
                React.createElement(UI_1.SmartTable, { rows: auditLogs, maxColumns: 8 }),
                React.createElement(UI_1.Pagination, { meta: auditMeta, loading: filterLoading, onPageChange: loadAudit })) : React.createElement(UI_1.EmptyState, { icon: lucide_react_1.FileClock, title: "No audit logs", description: "No audit log entries were returned." })),
            activeTab === 'notifications' && React.createElement(UI_1.SectionCard, { title: "Notifications", subtitle: "Idempotently consumed Kafka notification records." }, filterLoading && !notifications.length ? React.createElement(UI_1.LoadingBlock, null) : notifications.length ? React.createElement(React.Fragment, null,
                React.createElement(UI_1.SmartTable, { rows: notifications, maxColumns: 8 }),
                React.createElement(UI_1.Pagination, { meta: notificationMeta, loading: filterLoading, onPageChange: loadNotifications })) : React.createElement(UI_1.EmptyState, { icon: lucide_react_1.BellRing, title: "No notifications", description: "No notification records were returned." })))),
        React.createElement(UI_1.Modal, { open: Boolean(blacklistTarget), title: `Blacklist wallet #${(0, format_1.walletIdOf)(blacklistTarget) || blacklistTarget?.walletId || ''}`, description: "The authenticated admin identity is recorded by the backend.", onClose: () => setBlacklistTarget(null) },
            React.createElement("form", { className: "stack-form", onSubmit: submitBlacklist },
                React.createElement(UI_1.Field, { label: "Reason" },
                    React.createElement("textarea", { rows: "3", required: true, maxLength: "500", value: blacklistReason, onChange: (e) => setBlacklistReason(e.target.value), placeholder: "Reason for blacklisting" })),
                React.createElement(UI_1.Button, { type: "submit", variant: "danger", loading: actionLoading.startsWith('blacklist-') },
                    React.createElement(lucide_react_1.Ban, { size: 16 }),
                    " Confirm blacklist"))),
        React.createElement(UI_1.Modal, { open: Boolean(refundTarget), title: "Process refund", description: refundTarget?.transactionReference ?? refundTarget?.transactionRef, onClose: () => setRefundTarget(null) },
            React.createElement("form", { className: "stack-form", onSubmit: submitRefund },
                React.createElement("div", { className: "form-grid-2" },
                    React.createElement(UI_1.Field, { label: "Refund amount" },
                        React.createElement("input", { required: true, min: "0.01", step: "0.01", type: "number", value: refundForm.amount, onChange: (e) => setRefundForm({ ...refundForm, amount: e.target.value }) })),
                    React.createElement(UI_1.Field, { label: "Currency" },
                        React.createElement("input", { required: true, value: refundForm.currency, disabled: true }))),
                React.createElement(UI_1.Field, { label: "Reason" },
                    React.createElement("textarea", { rows: "3", maxLength: "500", value: refundForm.reason, onChange: (e) => setRefundForm({ ...refundForm, reason: e.target.value }), placeholder: "Optional refund reason" })),
                React.createElement(UI_1.Button, { type: "submit", loading: actionLoading === 'refund' },
                    React.createElement(lucide_react_1.RotateCcw, { size: 16 }),
                    " Process refund"))),
        React.createElement(UI_1.Modal, { open: Boolean(reversalTarget), title: "Reverse transaction", description: reversalTarget?.transactionReference ?? reversalTarget?.transactionRef, onClose: () => setReversalTarget(null) },
            React.createElement("form", { className: "stack-form", onSubmit: submitReversal },
                React.createElement(UI_1.Field, { label: "Reason" },
                    React.createElement("textarea", { rows: "3", required: true, maxLength: "500", value: reversalReason, onChange: (e) => setReversalReason(e.target.value), placeholder: "Why is this transaction being reversed?" })),
                React.createElement("div", { className: "warning-panel" },
                    React.createElement(lucide_react_1.ShieldAlert, { size: 18 }),
                    React.createElement("span", null, "Reversal is an administrative operation protected by original-transaction locking and idempotency.")),
                React.createElement(UI_1.Button, { type: "submit", variant: "danger", loading: actionLoading === 'reversal' },
                    React.createElement(lucide_react_1.Undo2, { size: 16 }),
                    " Confirm reversal")))));
}
function AdminOverview({ summary, transactions }) {
    const successful = Number(summary?.successfulTransactions || 0);
    const total = Number(summary?.totalTransactions || 0);
    const successRate = total ? Math.round((successful / total) * 100) : 0;
    return (React.createElement(React.Fragment, null,
        React.createElement("div", { className: "metric-grid admin-metrics" },
            React.createElement(UI_1.StatCard, { label: "Total wallets", value: summary?.totalWallets ?? 0, hint: `${summary?.activeWallets ?? 0} active`, icon: lucide_react_1.WalletCards, tone: "blue" }),
            React.createElement(UI_1.StatCard, { label: "Transactions", value: summary?.totalTransactions ?? 0, hint: `${successRate}% successful`, icon: lucide_react_1.CircleDollarSign, tone: "green" }),
            React.createElement(UI_1.StatCard, { label: "Frozen wallets", value: summary?.frozenWallets ?? 0, hint: "Requires monitoring", icon: lucide_react_1.Snowflake, tone: "orange" }),
            React.createElement(UI_1.StatCard, { label: "Blacklisted", value: summary?.blacklistedWallets ?? 0, hint: "Active blacklist records", icon: lucide_react_1.Ban, tone: "red" })),
        React.createElement("div", { className: "admin-overview-grid" },
            React.createElement(UI_1.SectionCard, { title: "Transaction health", subtitle: "Success, failure, partial refund, refund and reversal distribution." },
                React.createElement("div", { className: "health-list" },
                    React.createElement(HealthRow, { label: "Successful", value: summary?.successfulTransactions ?? 0, total: total, tone: "success" }),
                    React.createElement(HealthRow, { label: "Failed", value: summary?.failedTransactions ?? 0, total: total, tone: "danger" }),
                    React.createElement(HealthRow, { label: "Partially refunded", value: summary?.partiallyRefundedTransactions ?? 0, total: total, tone: "warning" }),
                    React.createElement(HealthRow, { label: "Refunded", value: summary?.refundedTransactions ?? 0, total: total, tone: "warning" }),
                    React.createElement(HealthRow, { label: "Reversed", value: summary?.reversedTransactions ?? 0, total: total, tone: "neutral" }))),
            React.createElement(UI_1.SectionCard, { title: "Platform records", subtitle: "Operational observability counts from the backend." },
                React.createElement("div", { className: "record-counts" },
                    React.createElement("div", null,
                        React.createElement(lucide_react_1.BellRing, { size: 19 }),
                        React.createElement("span", null, "Notifications"),
                        React.createElement("strong", null, summary?.totalNotifications ?? 0)),
                    React.createElement("div", null,
                        React.createElement(lucide_react_1.FileClock, { size: 19 }),
                        React.createElement("span", null, "Audit logs"),
                        React.createElement("strong", null, summary?.totalAuditLogs ?? 0)),
                    React.createElement("div", null,
                        React.createElement(lucide_react_1.UsersRound, { size: 19 }),
                        React.createElement("span", null, "Active wallets"),
                        React.createElement("strong", null, summary?.activeWallets ?? 0)),
                    React.createElement("div", null,
                        React.createElement(lucide_react_1.ShieldAlert, { size: 19 }),
                        React.createElement("span", null, "Failed txns"),
                        React.createElement("strong", null, summary?.failedTransactions ?? 0))))),
        React.createElement(UI_1.SectionCard, { title: "Latest platform transactions", subtitle: "A server-paginated operational sample." }, transactions.length ? React.createElement(UI_1.SmartTable, { rows: transactions.slice(0, 10), maxColumns: 7 }) : React.createElement(UI_1.EmptyState, { icon: lucide_react_1.Activity, title: "No transaction data", description: "No admin transaction records were returned." }))));
}
function HealthRow({ label, value, total, tone }) {
    const percent = total ? Math.min(100, Math.round((Number(value || 0) / total) * 100)) : 0;
    return React.createElement("div", { className: "health-row" },
        React.createElement("div", null,
            React.createElement("span", null, label),
            React.createElement("strong", null, value)),
        React.createElement("div", { className: "progress-track" },
            React.createElement("span", { className: `progress-fill progress-${tone}`, style: { width: `${percent}%` } })),
        React.createElement("small", null,
            percent,
            "%"));
}

};
__modules["pages/DashboardPage"]=function(module,exports,require){
"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.default = DashboardPage;
const react_1 = require("react");
const lucide_react_1 = require("lucide-react");
const react_router_dom_1 = require("react-router-dom");
const AuthContext_1 = require("../context/AuthContext");
const api_1 = require("../services/api");
const walletApi_1 = require("../services/walletApi");
const format_1 = require("../utils/format");
const UI_1 = require("../components/UI");
const auth_1 = require("../utils/auth");
function greeting() {
    const hour = new Date().getHours();
    if (hour < 12)
        return 'Good morning';
    if (hour < 18)
        return 'Good afternoon';
    return 'Good evening';
}
function DashboardPage() {
    const { auth, role } = (0, AuthContext_1.useAuth)();
    const [walletId, setWalletId] = (0, react_1.useState)(() => (0, auth_1.getStoredWalletId)(auth?.userId));
    const [balance, setBalance] = (0, react_1.useState)(null);
    const [wallet, setWallet] = (0, react_1.useState)(null);
    const [transactions, setTransactions] = (0, react_1.useState)([]);
    const [loading, setLoading] = (0, react_1.useState)(true);
    const [error, setError] = (0, react_1.useState)('');
    const [showBalance, setShowBalance] = (0, react_1.useState)(true);
    (0, react_1.useEffect)(() => {
        let live = true;
        async function load() {
            setLoading(true);
            setError('');
            try {
                let selected = walletId;
                if (!selected) {
                    const mine = await walletApi_1.walletApi.mine();
                    if (mine?.length) {
                        selected = String((0, format_1.walletIdOf)(mine[0]));
                        (0, auth_1.persistWalletId)(auth?.userId, selected);
                        if (live)
                            setWalletId(selected);
                    }
                }
                if (!selected)
                    return;
                const [walletData, balanceData, txnData] = await Promise.all([
                    walletApi_1.walletApi.get(selected),
                    walletApi_1.walletApi.balance(selected),
                    walletApi_1.transactionApi.byWallet(selected, 0, 10),
                ]);
                if (!live)
                    return;
                setWallet(walletData);
                setBalance(balanceData);
                setTransactions((0, walletApi_1.pageContent)(txnData));
            }
            catch (err) {
                if (live)
                    setError((0, api_1.apiErrorMessage)(err));
            }
            finally {
                if (live)
                    setLoading(false);
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
            { to: '/wallet', label: 'Wallet', icon: lucide_react_1.WalletCards },
            { to: '/transactions', label: 'Activity', icon: lucide_react_1.ArrowLeftRight },
            { to: '/refund', label: 'Refund', icon: lucide_react_1.RotateCcw },
            { to: '/statements', label: 'Statement', icon: lucide_react_1.ReceiptText },
        ]
        : [
            { to: '/wallet', label: 'Add money', icon: lucide_react_1.ArrowDownToLine },
            { to: '/transfer', label: 'Send', icon: lucide_react_1.Send },
            { to: '/pay', label: 'Pay', icon: lucide_react_1.CreditCard },
            { to: '/statements', label: 'Statement', icon: lucide_react_1.ReceiptText },
        ];
    return (React.createElement(React.Fragment, null,
        React.createElement(UI_1.PageHeader, { eyebrow: "Overview", title: `${greeting()}, ${auth?.username || 'there'}`, description: "Here\u2019s what\u2019s happening with your wallet today." }),
        loading ? React.createElement(UI_1.LoadingBlock, { label: "Opening your wallet\u2026" }) : error ? React.createElement("div", { className: "error-panel" }, error) : !walletId ? (React.createElement(UI_1.SectionCard, { className: "connect-wallet-hero" },
            React.createElement(UI_1.EmptyState, { icon: lucide_react_1.WalletCards, title: "Create your first wallet", description: "Set up a wallet to add money, send payments and keep track of your activity.", action: React.createElement(react_router_dom_1.Link, { className: "button button-primary", to: "/wallet" },
                    React.createElement(lucide_react_1.Plus, { size: 17 }),
                    " Create wallet") }))) : (React.createElement(React.Fragment, null,
            React.createElement("section", { className: "dashboard-hero-grid" },
                React.createElement("div", { className: "wallet-hero-card premium-wallet-card" },
                    React.createElement("div", { className: "wallet-hero-main" },
                        React.createElement("div", { className: "wallet-hero-top" },
                            React.createElement("span", null, "Available balance"),
                            React.createElement("button", { className: "balance-visibility", onClick: () => setShowBalance((value) => !value), "aria-label": "Toggle balance visibility" }, showBalance ? React.createElement(lucide_react_1.Eye, { size: 18 }) : React.createElement(lucide_react_1.EyeOff, { size: 18 }))),
                        React.createElement("strong", null, showBalance ? (0, format_1.formatMoney)(balance, currency) : '••••••'),
                        React.createElement("div", { className: "wallet-card-number" },
                            "PhoneWallet \u2022\u2022\u2022\u2022 ",
                            String(walletId).padStart(4, '0').slice(-4)),
                        React.createElement("div", { className: "wallet-hero-meta" },
                            React.createElement("span", null,
                                React.createElement(lucide_react_1.ShieldCheck, { size: 14 }),
                                " ",
                                wallet?.status === 'ACTIVE' ? 'Ready to use' : wallet?.status || 'Active'),
                            React.createElement("span", null, currency)))),
                React.createElement("div", { className: "dashboard-actions-card" },
                    React.createElement("span", { className: "dashboard-section-kicker" }, "Quick actions"),
                    React.createElement("div", { className: "quick-actions" }, quickActions.map(({ to, label, icon: Icon }) => (React.createElement(react_router_dom_1.Link, { key: to, to: to, className: "quick-action" },
                        React.createElement("span", null,
                            React.createElement(Icon, { size: 20 })),
                        React.createElement("strong", null, label))))),
                    React.createElement("div", { className: "dashboard-tip" },
                        React.createElement(lucide_react_1.ShieldCheck, { size: 18 }),
                        React.createElement("div", null,
                            React.createElement("strong", null, "Protected transactions"),
                            React.createElement("span", null, "Every money action is checked before it is processed."))))),
            React.createElement("div", { className: "metric-grid compact-grid" },
                React.createElement("div", { className: "mini-metric" },
                    React.createElement("div", { className: "mini-icon incoming" },
                        React.createElement(lucide_react_1.ArrowDownToLine, { size: 18 })),
                    React.createElement("div", null,
                        React.createElement("span", null, "Money in"),
                        React.createElement("strong", null, incoming),
                        React.createElement("small", null, "recent entries"))),
                React.createElement("div", { className: "mini-metric" },
                    React.createElement("div", { className: "mini-icon outgoing" },
                        React.createElement(lucide_react_1.ArrowUpRight, { size: 18 })),
                    React.createElement("div", null,
                        React.createElement("span", null, "Money out"),
                        React.createElement("strong", null, outgoing),
                        React.createElement("small", null, "recent entries"))),
                React.createElement("div", { className: "mini-metric" },
                    React.createElement("div", { className: "mini-icon success" },
                        React.createElement(lucide_react_1.ReceiptText, { size: 18 })),
                    React.createElement("div", null,
                        React.createElement("span", null, "Activity"),
                        React.createElement("strong", null, transactions.length),
                        React.createElement("small", null, "latest records")))),
            React.createElement(UI_1.SectionCard, { title: "Recent activity", subtitle: "Your latest wallet transactions", action: React.createElement(react_router_dom_1.Link, { className: "text-link", to: "/transactions" },
                    "See all ",
                    React.createElement(lucide_react_1.ArrowRight, { size: 15 })) }, transactions.length ? (React.createElement("div", { className: "transaction-list" }, transactions.slice(0, 6).map((tx, index) => {
                const outgoingTx = String(tx.fromWalletId) === String(walletId);
                return (React.createElement("div", { className: "transaction-row", key: tx.transactionId ?? index },
                    React.createElement("div", { className: `transaction-icon ${outgoingTx ? 'outgoing' : 'incoming'}` }, outgoingTx ? React.createElement(lucide_react_1.ArrowUpRight, { size: 19 }) : React.createElement(lucide_react_1.ArrowDownToLine, { size: 19 })),
                    React.createElement("div", { className: "transaction-main" },
                        React.createElement("strong", null, String(tx.type || (outgoingTx ? 'Money sent' : 'Money received')).replaceAll('_', ' ')),
                        React.createElement("span", null,
                            (0, format_1.shortRef)(tx.transactionReference),
                            " \u00B7 ",
                            (0, format_1.formatDate)(tx.createdAt))),
                    React.createElement("div", { className: "transaction-amount" },
                        React.createElement("strong", { className: outgoingTx ? 'amount-negative' : 'amount-positive' },
                            outgoingTx ? '−' : '+',
                            (0, format_1.formatMoney)(tx.amount, tx.currency || currency)),
                        React.createElement(UI_1.StatusBadge, { value: tx.status }))));
            }))) : React.createElement(UI_1.EmptyState, { icon: lucide_react_1.ReceiptText, title: "No activity yet", description: "Your transfers, payments and top-ups will appear here." }))))));
}

};
__modules["pages/LoginPage"]=function(module,exports,require){
"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.default = LoginPage;
const react_1 = require("react");
const react_router_dom_1 = require("react-router-dom");
const lucide_react_1 = require("lucide-react");
const AuthContext_1 = require("../context/AuthContext");
const api_1 = require("../services/api");
const UI_1 = require("../components/UI");
function LoginPage() {
    const { isAuthenticated, login } = (0, AuthContext_1.useAuth)();
    const navigate = (0, react_router_dom_1.useNavigate)();
    const location = (0, react_router_dom_1.useLocation)();
    const [form, setForm] = (0, react_1.useState)({ username: '', password: '' });
    const [showPassword, setShowPassword] = (0, react_1.useState)(false);
    const [loading, setLoading] = (0, react_1.useState)(false);
    const [error, setError] = (0, react_1.useState)('');
    if (isAuthenticated)
        return React.createElement(react_router_dom_1.Navigate, { to: "/dashboard", replace: true });
    async function handleSubmit(event) {
        event.preventDefault();
        setLoading(true);
        setError('');
        try {
            const session = await login(form.username.trim(), form.password);
            const fallback = session?.role === 'ADMIN' ? '/admin' : '/dashboard';
            navigate(location.state?.from || fallback, { replace: true });
        }
        catch (err) {
            setError((0, api_1.apiErrorMessage)(err));
        }
        finally {
            setLoading(false);
        }
    }
    return (React.createElement("div", { className: "auth-page" },
        React.createElement("section", { className: "auth-visual" },
            React.createElement("div", { className: "auth-visual-inner" },
                React.createElement("div", { className: "auth-brand" },
                    React.createElement("div", { className: "brand-mark light" },
                        React.createElement(lucide_react_1.WalletCards, { size: 22 })),
                    React.createElement("strong", null, "PhoneWallet")),
                React.createElement("div", { className: "auth-hero-copy" },
                    React.createElement("span", { className: "auth-kicker" },
                        React.createElement(lucide_react_1.Sparkles, { size: 15 }),
                        " Everyday money, made simple"),
                    React.createElement("h1", null, "Move money with confidence."),
                    React.createElement("p", null, "Keep your balance, transfers, payments and transaction history together in one secure wallet.")),
                React.createElement("div", { className: "auth-benefits" },
                    React.createElement("div", null,
                        React.createElement(lucide_react_1.CheckCircle2, { size: 18 }),
                        React.createElement("span", null, "Fast wallet-to-wallet transfers")),
                    React.createElement("div", null,
                        React.createElement(lucide_react_1.CheckCircle2, { size: 18 }),
                        React.createElement("span", null, "Protected payments and transaction history")),
                    React.createElement("div", null,
                        React.createElement(lucide_react_1.CheckCircle2, { size: 18 }),
                        React.createElement("span", null, "Clear balance and statement tracking"))),
                React.createElement("div", { className: "auth-card-preview" },
                    React.createElement("div", { className: "auth-card-top" },
                        React.createElement("span", null, "PHONEWALLET"),
                        React.createElement(lucide_react_1.ShieldCheck, { size: 22 })),
                    React.createElement("div", null,
                        React.createElement("small", null, "AVAILABLE BALANCE"),
                        React.createElement("strong", null, "\u20B9 \u2022\u2022\u2022\u2022\u2022\u2022")),
                    React.createElement("div", { className: "auth-card-footer" },
                        React.createElement("span", null, "Secure wallet"),
                        React.createElement("span", null, "\u2022\u2022\u2022\u2022 8090"))))),
        React.createElement("section", { className: "auth-form-side" },
            React.createElement("div", { className: "auth-form-card" },
                React.createElement("div", { className: "mobile-auth-brand" },
                    React.createElement("div", { className: "brand-mark" },
                        React.createElement(lucide_react_1.WalletCards, { size: 20 })),
                    React.createElement("strong", null, "PhoneWallet")),
                React.createElement("span", { className: "eyebrow" }, "Welcome back"),
                React.createElement("h2", null, "Sign in to PhoneWallet"),
                React.createElement("p", { className: "auth-subtitle" }, "Access your wallet, activity and payments."),
                error && React.createElement("div", { className: "form-alert" },
                    React.createElement(lucide_react_1.LockKeyhole, { size: 18 }),
                    React.createElement("span", null, error)),
                React.createElement("form", { onSubmit: handleSubmit, className: "auth-form" },
                    React.createElement(UI_1.Field, { label: "Username" },
                        React.createElement("input", { autoFocus: true, required: true, autoComplete: "username", value: form.username, onChange: (e) => setForm({ ...form, username: e.target.value }), placeholder: "Your username" })),
                    React.createElement(UI_1.Field, { label: "Password" },
                        React.createElement("div", { className: "input-with-action" },
                            React.createElement("input", { required: true, autoComplete: "current-password", type: showPassword ? 'text' : 'password', value: form.password, onChange: (e) => setForm({ ...form, password: e.target.value }), placeholder: "Your password" }),
                            React.createElement("button", { type: "button", className: "input-action", onClick: () => setShowPassword((v) => !v), "aria-label": showPassword ? 'Hide password' : 'Show password' }, showPassword ? React.createElement(lucide_react_1.EyeOff, { size: 18 }) : React.createElement(lucide_react_1.Eye, { size: 18 })))),
                    React.createElement(UI_1.Button, { loading: loading, type: "submit", className: "full-button" },
                        "Sign in ",
                        React.createElement(lucide_react_1.ArrowRight, { size: 17 }))),
                React.createElement("div", { className: "auth-switch" },
                    "Don\u2019t have an account? ",
                    React.createElement(react_router_dom_1.Link, { to: "/signup" }, "Create one")),
                React.createElement("div", { className: "auth-security-note" },
                    React.createElement(lucide_react_1.ShieldCheck, { size: 16 }),
                    React.createElement("span", null, "Protected connection. Your sign-in details are sent securely."))))));
}

};
__modules["pages/PayPage"]=function(module,exports,require){
"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.default = PayPage;
const react_1 = require("react");
const lucide_react_1 = require("lucide-react");
const AuthContext_1 = require("../context/AuthContext");
const ToastContext_1 = require("../context/ToastContext");
const UI_1 = require("../components/UI");
const api_1 = require("../services/api");
const walletApi_1 = require("../services/walletApi");
const auth_1 = require("../utils/auth");
const format_1 = require("../utils/format");
function PayPage() {
    const { auth } = (0, AuthContext_1.useAuth)();
    const { showToast } = (0, ToastContext_1.useToast)();
    const walletId = (0, auth_1.getStoredWalletId)(auth?.userId);
    const [form, setForm] = (0, react_1.useState)({ merchantWalletId: '', amount: '', currency: 'INR', merchantReference: '' });
    const [loading, setLoading] = (0, react_1.useState)(false);
    const [result, setResult] = (0, react_1.useState)(null);
    (0, react_1.useEffect)(() => {
        if (walletId)
            walletApi_1.walletApi.get(walletId).then((wallet) => setForm((f) => ({ ...f, currency: wallet?.currency || f.currency }))).catch(() => { });
    }, [walletId]);
    async function submit(event) {
        event.preventDefault();
        if (!walletId)
            return showToast('Create or select a wallet before making a payment.', 'warning');
        setLoading(true);
        setResult(null);
        try {
            const data = await walletApi_1.transactionApi.pay({
                fromWalletId: Number(walletId),
                toWalletId: Number(form.merchantWalletId),
                amount: Number(form.amount),
                currency: form.currency,
                idempotencyKey: (0, format_1.generateIdempotencyKey)('pay'),
                merchantReference: form.merchantReference || null,
            });
            setResult(data);
            showToast('Payment completed successfully.');
        }
        catch (err) {
            showToast((0, api_1.apiErrorMessage)(err), 'error');
        }
        finally {
            setLoading(false);
        }
    }
    return (React.createElement(React.Fragment, null,
        React.createElement(UI_1.PageHeader, { eyebrow: "Pay", title: "Pay a business", description: "Pay a verified business wallet and keep the receipt in your activity history." }),
        React.createElement("div", { className: "operation-layout" },
            React.createElement(UI_1.SectionCard, { title: "Payment details", subtitle: "Enter the business wallet number and amount." },
                React.createElement("form", { className: "stack-form", onSubmit: submit },
                    React.createElement(UI_1.Field, { label: "Business wallet number" },
                        React.createElement("div", { className: "input-with-leading" },
                            React.createElement(lucide_react_1.Landmark, { size: 17 }),
                            React.createElement("input", { required: true, inputMode: "numeric", value: form.merchantWalletId, onChange: (e) => setForm({ ...form, merchantWalletId: e.target.value }), placeholder: "Enter business wallet number" }))),
                    React.createElement(UI_1.Field, { label: "Amount" },
                        React.createElement("div", { className: "money-input" },
                            React.createElement("span", null, form.currency === 'INR' ? '₹' : form.currency),
                            React.createElement("input", { required: true, min: "0.01", step: "0.01", type: "number", value: form.amount, onChange: (e) => setForm({ ...form, amount: e.target.value }), placeholder: "0.00" }))),
                    React.createElement(UI_1.Field, { label: "Order or invoice reference", hint: "Optional" },
                        React.createElement("div", { className: "input-with-leading" },
                            React.createElement(lucide_react_1.Receipt, { size: 17 }),
                            React.createElement("input", { value: form.merchantReference, onChange: (e) => setForm({ ...form, merchantReference: e.target.value }), placeholder: "e.g. INV-2026-001" }))),
                    React.createElement("div", { className: "secure-operation-note" },
                        React.createElement(lucide_react_1.ShieldCheck, { size: 18 }),
                        React.createElement("div", null,
                            React.createElement("strong", null, "Payment protection"),
                            React.createElement("span", null, "PhoneWallet checks the business wallet and protects against accidental duplicate payments."))),
                    React.createElement(UI_1.Button, { type: "submit", loading: loading, disabled: !walletId },
                        React.createElement(lucide_react_1.CreditCard, { size: 17 }),
                        " Pay now"))),
            React.createElement(UI_1.SectionCard, { title: "Payment receipt", subtitle: "Your payment confirmation will appear here." }, result ? React.createElement("div", { className: "receipt-card" },
                React.createElement("div", { className: "receipt-success" },
                    React.createElement("div", { className: "receipt-check" },
                        React.createElement(lucide_react_1.CheckCircle2, { size: 24 })),
                    React.createElement("span", null, "Payment complete"),
                    React.createElement("strong", null, (0, format_1.formatMoney)(result.amount, result.currency))),
                React.createElement("div", { className: "receipt-rows" },
                    React.createElement("div", null,
                        React.createElement("span", null, "Reference"),
                        React.createElement("strong", null, result.transactionReference || '—')),
                    React.createElement("div", null,
                        React.createElement("span", null, "Business wallet"),
                        React.createElement("strong", null,
                            "\u2022\u2022\u2022\u2022 ",
                            String(result.toWalletId || '').padStart(4, '0').slice(-4))),
                    React.createElement("div", null,
                        React.createElement("span", null, "Status"),
                        React.createElement("strong", null, result.status || '—')))) : React.createElement("div", { className: "operation-preview" },
                React.createElement("div", { className: "preview-icon" },
                    React.createElement(lucide_react_1.CreditCard, { size: 28 })),
                React.createElement("h3", null, "Ready to pay"),
                React.createElement("p", null, "Your confirmed amount and payment reference will appear here."))))));
}

};
__modules["pages/RefundPage"]=function(module,exports,require){
"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.default = RefundPage;
const react_1 = require("react");
const lucide_react_1 = require("lucide-react");
const UI_1 = require("../components/UI");
const ToastContext_1 = require("../context/ToastContext");
const walletApi_1 = require("../services/walletApi");
const api_1 = require("../services/api");
const format_1 = require("../utils/format");
function RefundPage() {
    const { showToast } = (0, ToastContext_1.useToast)();
    const [loading, setLoading] = (0, react_1.useState)(false);
    const [form, setForm] = (0, react_1.useState)({
        originalTransactionReference: '',
        refundAmount: '',
        refundReason: '',
        currency: 'INR',
    });
    async function submit(event) {
        event.preventDefault();
        setLoading(true);
        try {
            const response = await walletApi_1.transactionApi.refund({
                originalTransactionReference: form.originalTransactionReference.trim(),
                refundAmount: Number(form.refundAmount),
                refundReason: form.refundReason.trim() || null,
                idempotencyKey: (0, format_1.generateIdempotencyKey)('refund'),
                currency: form.currency.trim().toUpperCase(),
            });
            showToast(`Refund ${response?.transactionReference || ''} processed successfully.`.trim());
            setForm({ originalTransactionReference: '', refundAmount: '', refundReason: '', currency: 'INR' });
        }
        catch (error) {
            showToast((0, api_1.apiErrorMessage)(error), 'error');
        }
        finally {
            setLoading(false);
        }
    }
    return (React.createElement(React.Fragment, null,
        React.createElement(UI_1.PageHeader, { eyebrow: "Merchant operation", title: "Refund a transaction", description: "Issue an authorized refund against an existing transaction reference." }),
        React.createElement("div", { className: "single-form-page" },
            React.createElement(UI_1.SectionCard, { title: "Refund details", subtitle: "The idempotency key is generated automatically for every refund request." },
                React.createElement("form", { className: "stack-form", onSubmit: submit },
                    React.createElement(UI_1.Field, { label: "Original transaction reference" },
                        React.createElement("input", { required: true, value: form.originalTransactionReference, onChange: (e) => setForm({ ...form, originalTransactionReference: e.target.value }), placeholder: "Enter original transaction reference" })),
                    React.createElement("div", { className: "form-grid-2" },
                        React.createElement(UI_1.Field, { label: "Refund amount" },
                            React.createElement("input", { required: true, min: "0.01", step: "0.01", type: "number", value: form.refundAmount, onChange: (e) => setForm({ ...form, refundAmount: e.target.value }), placeholder: "0.00" })),
                        React.createElement(UI_1.Field, { label: "Currency" },
                            React.createElement("input", { required: true, value: form.currency, onChange: (e) => setForm({ ...form, currency: e.target.value.toUpperCase() }), placeholder: "INR" }))),
                    React.createElement(UI_1.Field, { label: "Reason", hint: "Optional" },
                        React.createElement("textarea", { rows: "4", value: form.refundReason, onChange: (e) => setForm({ ...form, refundReason: e.target.value }), placeholder: "Reason for refund" })),
                    React.createElement("div", { className: "idempotency-note" },
                        React.createElement(lucide_react_1.ShieldCheck, { size: 17 }),
                        React.createElement("span", null, "A unique idempotency key will be sent with this refund request.")),
                    React.createElement(UI_1.Button, { type: "submit", loading: loading },
                        React.createElement(lucide_react_1.RotateCcw, { size: 17 }),
                        " Process refund"))))));
}

};
__modules["pages/SignupPage"]=function(module,exports,require){
"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.default = SignupPage;
const react_1 = require("react");
const lucide_react_1 = require("lucide-react");
const react_router_dom_1 = require("react-router-dom");
const AuthContext_1 = require("../context/AuthContext");
const ToastContext_1 = require("../context/ToastContext");
const api_1 = require("../services/api");
const walletApi_1 = require("../services/walletApi");
const UI_1 = require("../components/UI");
function SignupPage() {
    const { isAuthenticated } = (0, AuthContext_1.useAuth)();
    const { showToast } = (0, ToastContext_1.useToast)();
    const navigate = (0, react_router_dom_1.useNavigate)();
    const [showPassword, setShowPassword] = (0, react_1.useState)(false);
    const [loading, setLoading] = (0, react_1.useState)(false);
    const [error, setError] = (0, react_1.useState)('');
    const [form, setForm] = (0, react_1.useState)({ username: '', password: '', mobile: '' });
    if (isAuthenticated)
        return React.createElement(react_router_dom_1.Navigate, { to: "/dashboard", replace: true });
    async function submit(event) {
        event.preventDefault();
        setLoading(true);
        setError('');
        try {
            await walletApi_1.authApi.signup({ username: form.username.trim(), password: form.password, mobile: form.mobile.trim() });
            showToast('Your PhoneWallet account is ready. Sign in to continue.');
            navigate('/login');
        }
        catch (err) {
            setError((0, api_1.apiErrorMessage)(err));
        }
        finally {
            setLoading(false);
        }
    }
    return (React.createElement("div", { className: "auth-page auth-page-signup" },
        React.createElement("section", { className: "auth-visual signup-visual" },
            React.createElement("div", { className: "auth-visual-inner" },
                React.createElement("div", { className: "auth-brand" },
                    React.createElement("div", { className: "brand-mark light" },
                        React.createElement(lucide_react_1.WalletCards, { size: 22 })),
                    React.createElement("strong", null, "PhoneWallet")),
                React.createElement("div", { className: "auth-hero-copy" },
                    React.createElement("span", { className: "auth-kicker" },
                        React.createElement(lucide_react_1.BadgeCheck, { size: 16 }),
                        " Start in a few moments"),
                    React.createElement("h1", null, "Your wallet starts here."),
                    React.createElement("p", null, "Create your personal PhoneWallet account, then add a wallet and start sending or paying securely.")),
                React.createElement("div", { className: "auth-benefits" },
                    React.createElement("div", null,
                        React.createElement(lucide_react_1.CheckCircle2, { size: 18 }),
                        React.createElement("span", null, "One clear home for your money")),
                    React.createElement("div", null,
                        React.createElement(lucide_react_1.CheckCircle2, { size: 18 }),
                        React.createElement("span", null, "Transaction history you can follow")),
                    React.createElement("div", null,
                        React.createElement(lucide_react_1.CheckCircle2, { size: 18 }),
                        React.createElement("span", null, "Built-in payment protection"))))),
        React.createElement("section", { className: "auth-form-side" },
            React.createElement("div", { className: "auth-form-card auth-signup-card" },
                React.createElement("div", { className: "mobile-auth-brand" },
                    React.createElement("div", { className: "brand-mark" },
                        React.createElement(lucide_react_1.WalletCards, { size: 20 })),
                    React.createElement("strong", null, "PhoneWallet")),
                React.createElement("span", { className: "eyebrow" }, "Create account"),
                React.createElement("h2", null, "Get started with PhoneWallet"),
                React.createElement("p", { className: "auth-subtitle" }, "Create your personal account. You can set up your first wallet after signing in."),
                error && React.createElement("div", { className: "form-alert" },
                    React.createElement(lucide_react_1.ShieldCheck, { size: 17 }),
                    React.createElement("span", null, error)),
                React.createElement("form", { onSubmit: submit, className: "auth-form" },
                    React.createElement(UI_1.Field, { label: "Username" },
                        React.createElement("input", { required: true, minLength: "3", maxLength: "64", autoComplete: "username", value: form.username, onChange: (e) => setForm({ ...form, username: e.target.value }), placeholder: "Choose a username" })),
                    React.createElement(UI_1.Field, { label: "Mobile number" },
                        React.createElement("input", { required: true, inputMode: "tel", autoComplete: "tel", pattern: "[6-9][0-9]{9}", value: form.mobile, onChange: (e) => setForm({ ...form, mobile: e.target.value }), placeholder: "9876543210" })),
                    React.createElement(UI_1.Field, { label: "Password", hint: "Use at least 8 characters" },
                        React.createElement("div", { className: "input-with-action" },
                            React.createElement("input", { required: true, minLength: "8", maxLength: "72", autoComplete: "new-password", type: showPassword ? 'text' : 'password', value: form.password, onChange: (e) => setForm({ ...form, password: e.target.value }), placeholder: "Create a strong password" }),
                            React.createElement("button", { type: "button", className: "input-action", onClick: () => setShowPassword((v) => !v), "aria-label": showPassword ? 'Hide password' : 'Show password' }, showPassword ? React.createElement(lucide_react_1.EyeOff, { size: 18 }) : React.createElement(lucide_react_1.Eye, { size: 18 })))),
                    React.createElement(UI_1.Button, { loading: loading, type: "submit", className: "full-button" },
                        "Create account ",
                        React.createElement(lucide_react_1.ArrowRight, { size: 17 }))),
                React.createElement("p", { className: "consent-copy" }, "By creating an account, you agree to use PhoneWallet for lawful wallet activity and keep your login details secure."),
                React.createElement("div", { className: "auth-switch" },
                    "Already have an account? ",
                    React.createElement(react_router_dom_1.Link, { to: "/login" }, "Sign in"))))));
}

};
__modules["pages/StatementsPage"]=function(module,exports,require){
"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.default = StatementsPage;
const react_1 = require("react");
const lucide_react_1 = require("lucide-react");
const AuthContext_1 = require("../context/AuthContext");
const UI_1 = require("../components/UI");
const api_1 = require("../services/api");
const walletApi_1 = require("../services/walletApi");
const auth_1 = require("../utils/auth");
const format_1 = require("../utils/format");
function StatementsPage() {
    const { auth } = (0, AuthContext_1.useAuth)();
    const walletId = (0, auth_1.getStoredWalletId)(auth?.userId);
    const [rows, setRows] = (0, react_1.useState)([]);
    const [meta, setMeta] = (0, react_1.useState)(null);
    const [page, setPage] = (0, react_1.useState)(0);
    const [loading, setLoading] = (0, react_1.useState)(Boolean(walletId));
    const [error, setError] = (0, react_1.useState)('');
    const [currency, setCurrency] = (0, react_1.useState)('INR');
    const [range, setRange] = (0, react_1.useState)({ start: '', end: '' });
    const [rangeApplied, setRangeApplied] = (0, react_1.useState)(false);
    async function load(targetPage = 0, useRange = rangeApplied) {
        if (!walletId)
            return;
        setLoading(true);
        setError('');
        try {
            const data = useRange
                ? await walletApi_1.statementApi.byRange(walletId, (0, format_1.normalizeDateTimeLocal)(range.start), (0, format_1.normalizeDateTimeLocal)(range.end), targetPage, 20)
                : await walletApi_1.statementApi.byWallet(walletId, targetPage, 20);
            setRows((0, walletApi_1.pageContent)(data));
            setMeta((0, walletApi_1.pageMeta)(data));
            setPage(targetPage);
        }
        catch (err) {
            setError((0, api_1.apiErrorMessage)(err));
        }
        finally {
            setLoading(false);
        }
    }
    (0, react_1.useEffect)(() => {
        setRangeApplied(false);
        setPage(0);
        load(0, false);
        if (walletId)
            walletApi_1.walletApi.get(walletId).then((data) => setCurrency(data?.currency || 'INR')).catch(() => { });
    }, [walletId]);
    async function filter(event) {
        event.preventDefault();
        if (!range.start || !range.end)
            return;
        if (new Date(range.start) > new Date(range.end))
            return setError('Start date must be before end date.');
        setRangeApplied(true);
        await load(0, true);
    }
    function downloadCsv() {
        const headers = ['transactionRef', 'walletId', 'entryType', 'amount', 'balanceAfterTransaction', 'timestamp'];
        const csv = [headers.join(','), ...rows.map((row) => headers.map((key) => JSON.stringify(row[key] ?? '')).join(','))].join('\n');
        const blob = new Blob([csv], { type: 'text/csv;charset=utf-8' });
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `wallet-${walletId}-statement-page-${page + 1}.csv`;
        a.click();
        URL.revokeObjectURL(url);
    }
    return (React.createElement(React.Fragment, null,
        React.createElement(UI_1.PageHeader, { eyebrow: "Ledger", title: "Wallet statement", description: "View your wallet statement, filter by date and export the current page when needed.", actions: React.createElement(UI_1.Button, { variant: "secondary", onClick: downloadCsv, disabled: !rows.length },
                React.createElement(lucide_react_1.Download, { size: 16 }),
                " Export CSV") }),
        React.createElement("form", { className: "statement-filter", onSubmit: filter },
            React.createElement(UI_1.Field, { label: "Start date & time" },
                React.createElement("input", { type: "datetime-local", value: range.start, onChange: (e) => setRange({ ...range, start: e.target.value }) })),
            React.createElement(UI_1.Field, { label: "End date & time" },
                React.createElement("input", { type: "datetime-local", value: range.end, onChange: (e) => setRange({ ...range, end: e.target.value }) })),
            React.createElement(UI_1.Button, { type: "submit", disabled: !walletId || !range.start || !range.end },
                React.createElement(lucide_react_1.CalendarRange, { size: 16 }),
                " Apply range"),
            React.createElement(UI_1.Button, { type: "button", variant: "secondary", onClick: () => { setRange({ start: '', end: '' }); setRangeApplied(false); load(0, false); } },
                React.createElement(lucide_react_1.RefreshCw, { size: 16 }),
                " All entries")),
        !walletId ? React.createElement(UI_1.EmptyState, { icon: lucide_react_1.ReceiptText, title: "No wallet selected", description: "Select a wallet first." }) : loading ? React.createElement(UI_1.LoadingBlock, null) : error ? React.createElement("div", { className: "error-panel" }, error) : rows.length ? React.createElement("div", { className: "table-card" },
            React.createElement("div", { className: "table-wrap" },
                React.createElement("table", null,
                    React.createElement("thead", null,
                        React.createElement("tr", null,
                            React.createElement("th", null, "Reference"),
                            React.createElement("th", null, "Entry type"),
                            React.createElement("th", null, "Amount"),
                            React.createElement("th", null, "Balance after"),
                            React.createElement("th", null, "Wallet"),
                            React.createElement("th", null, "Timestamp"))),
                    React.createElement("tbody", null, rows.map((row, index) => React.createElement("tr", { key: `${row.transactionRef}-${index}` },
                        React.createElement("td", null,
                            React.createElement("div", { className: "table-primary" },
                                React.createElement("strong", null, row.transactionRef || '—'))),
                        React.createElement("td", null,
                            React.createElement(UI_1.StatusBadge, { value: row.entryType })),
                        React.createElement("td", null,
                            React.createElement("strong", null, (0, format_1.formatMoney)(row.amount, currency))),
                        React.createElement("td", null, (0, format_1.formatMoney)(row.balanceAfterTransaction, currency)),
                        React.createElement("td", null,
                            "#",
                            row.walletId ?? walletId),
                        React.createElement("td", null, (0, format_1.formatDate)(row.timestamp))))))),
            React.createElement(UI_1.Pagination, { meta: meta, loading: loading, onPageChange: (next) => load(next, rangeApplied) })) : React.createElement(UI_1.EmptyState, { icon: lucide_react_1.ReceiptText, title: "No statement entries", description: "No statement entries were found for this wallet or date range." })));
}

};
__modules["pages/TransactionsPage"]=function(module,exports,require){
"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.default = TransactionsPage;
const react_1 = require("react");
const lucide_react_1 = require("lucide-react");
const AuthContext_1 = require("../context/AuthContext");
const UI_1 = require("../components/UI");
const api_1 = require("../services/api");
const walletApi_1 = require("../services/walletApi");
const auth_1 = require("../utils/auth");
const format_1 = require("../utils/format");
function TransactionsPage() {
    const { auth } = (0, AuthContext_1.useAuth)();
    const walletId = (0, auth_1.getStoredWalletId)(auth?.userId);
    const [rows, setRows] = (0, react_1.useState)([]);
    const [meta, setMeta] = (0, react_1.useState)(null);
    const [page, setPage] = (0, react_1.useState)(0);
    const [loading, setLoading] = (0, react_1.useState)(Boolean(walletId));
    const [detailLoading, setDetailLoading] = (0, react_1.useState)(false);
    const [error, setError] = (0, react_1.useState)('');
    const [query, setQuery] = (0, react_1.useState)('');
    const [status, setStatus] = (0, react_1.useState)('ALL');
    const [selected, setSelected] = (0, react_1.useState)(null);
    async function load(targetPage = page) {
        if (!walletId)
            return;
        setLoading(true);
        setError('');
        try {
            const data = await walletApi_1.transactionApi.byWallet(walletId, targetPage, 20);
            setRows((0, walletApi_1.pageContent)(data));
            setMeta((0, walletApi_1.pageMeta)(data));
            setPage(targetPage);
        }
        catch (err) {
            setError((0, api_1.apiErrorMessage)(err));
        }
        finally {
            setLoading(false);
        }
    }
    async function openDetails(tx) {
        setSelected(tx);
        const id = tx?.transactionId ?? tx?.id;
        if (!id)
            return;
        setDetailLoading(true);
        try {
            setSelected(await walletApi_1.transactionApi.get(id));
        }
        catch { /* row data remains visible */ }
        finally {
            setDetailLoading(false);
        }
    }
    (0, react_1.useEffect)(() => { setPage(0); load(0); }, [walletId]);
    const statuses = (0, react_1.useMemo)(() => ['ALL', ...new Set(rows.map((row) => String(row.status || '').toUpperCase()).filter(Boolean))], [rows]);
    const filtered = rows.filter((row) => {
        const haystack = `${row.transactionReference || ''} ${row.type || ''} ${row.status || ''} ${row.fromWalletId || ''} ${row.toWalletId || ''}`.toLowerCase();
        return haystack.includes(query.toLowerCase()) && (status === 'ALL' || String(row.status).toUpperCase() === status);
    });
    return (React.createElement(React.Fragment, null,
        React.createElement(UI_1.PageHeader, { eyebrow: "Activity", title: "Transactions", description: "Review money sent, received, top-ups, payments and refunds for your selected wallet.", actions: React.createElement(UI_1.Button, { variant: "secondary", onClick: () => load(page), disabled: !walletId, loading: loading },
                React.createElement(lucide_react_1.RefreshCw, { size: 16 }),
                " Refresh") }),
        React.createElement("div", { className: "toolbar-card" },
            React.createElement(UI_1.SearchBox, { value: query, onChange: setQuery, placeholder: "Search this page\u2026" }),
            React.createElement("select", { className: "toolbar-select", value: status, onChange: (e) => setStatus(e.target.value) }, statuses.map((item) => React.createElement("option", { key: item }, item))),
            React.createElement("span", { className: "toolbar-count" },
                meta?.totalElements ?? filtered.length,
                " total")),
        !walletId ? React.createElement(UI_1.EmptyState, { icon: lucide_react_1.ReceiptText, title: "No wallet selected", description: "Open Wallet and select a wallet first." }) : loading ? React.createElement(UI_1.LoadingBlock, null) : error ? React.createElement("div", { className: "error-panel" }, error) : filtered.length ? (React.createElement("div", { className: "table-card" },
            React.createElement("div", { className: "table-wrap" },
                React.createElement("table", null,
                    React.createElement("thead", null,
                        React.createElement("tr", null,
                            React.createElement("th", null, "Transaction"),
                            React.createElement("th", null, "Type"),
                            React.createElement("th", null, "Direction"),
                            React.createElement("th", null, "Amount"),
                            React.createElement("th", null, "Refunded"),
                            React.createElement("th", null, "Status"),
                            React.createElement("th", null, "Date"),
                            React.createElement("th", null))),
                    React.createElement("tbody", null, filtered.map((tx, index) => { const outgoing = String(tx.fromWalletId) === String(walletId); return React.createElement("tr", { key: tx.transactionId ?? index },
                        React.createElement("td", null,
                            React.createElement("div", { className: "table-primary" },
                                React.createElement("strong", null,
                                    "#",
                                    tx.transactionId ?? '—'),
                                React.createElement("span", null, tx.transactionReference || '—'))),
                        React.createElement("td", null, tx.type || '—'),
                        React.createElement("td", null,
                            React.createElement("div", { className: "direction-cell" },
                                outgoing ? React.createElement(lucide_react_1.ArrowUpRight, { size: 16 }) : React.createElement(lucide_react_1.ArrowDownToLine, { size: 16 }),
                                outgoing ? `To #${tx.toWalletId ?? '—'}` : `From #${tx.fromWalletId ?? 'External'}`)),
                        React.createElement("td", null,
                            React.createElement("strong", { className: outgoing ? 'amount-negative' : 'amount-positive' },
                                outgoing ? '−' : '+',
                                (0, format_1.formatMoney)(tx.amount, tx.currency))),
                        React.createElement("td", null, (0, format_1.formatMoney)(tx.refundedAmount || 0, tx.currency)),
                        React.createElement("td", null,
                            React.createElement(UI_1.StatusBadge, { value: tx.status })),
                        React.createElement("td", null, (0, format_1.formatDate)(tx.createdAt)),
                        React.createElement("td", null,
                            React.createElement("button", { className: "table-icon-button", onClick: () => openDetails(tx), "aria-label": "View transaction" },
                                React.createElement(lucide_react_1.Eye, { size: 17 })))); })))),
            React.createElement(UI_1.Pagination, { meta: meta, loading: loading, onPageChange: load }))) : React.createElement(UI_1.EmptyState, { icon: lucide_react_1.ReceiptText, title: "No matching transactions", description: "Try a different search or status filter." }),
        React.createElement(UI_1.Modal, { open: Boolean(selected), title: "Transaction details", description: selected?.transactionReference, onClose: () => setSelected(null) }, detailLoading ? React.createElement(UI_1.LoadingBlock, { label: "Loading transaction details\u2026" }) : selected && React.createElement("div", { className: "detail-list" },
            React.createElement("div", null,
                React.createElement("span", null, "Transaction ID"),
                React.createElement("strong", null, selected.transactionId ?? '—')),
            React.createElement("div", null,
                React.createElement("span", null, "Reference"),
                React.createElement("strong", null, selected.transactionReference || '—')),
            React.createElement("div", null,
                React.createElement("span", null, "From wallet"),
                React.createElement("strong", null, selected.fromWalletId ?? 'External')),
            React.createElement("div", null,
                React.createElement("span", null, "To wallet"),
                React.createElement("strong", null, selected.toWalletId ?? '—')),
            React.createElement("div", null,
                React.createElement("span", null, "Amount"),
                React.createElement("strong", null, (0, format_1.formatMoney)(selected.amount, selected.currency))),
            React.createElement("div", null,
                React.createElement("span", null, "Refunded amount"),
                React.createElement("strong", null, (0, format_1.formatMoney)(selected.refundedAmount || 0, selected.currency))),
            React.createElement("div", null,
                React.createElement("span", null, "Related transaction"),
                React.createElement("strong", null, selected.relatedTransactionReference || '—')),
            React.createElement("div", null,
                React.createElement("span", null, "Provider reference"),
                React.createElement("strong", null, selected.providerReference || '—')),
            React.createElement("div", null,
                React.createElement("span", null, "Type"),
                React.createElement("strong", null, selected.type || '—')),
            React.createElement("div", null,
                React.createElement("span", null, "Status"),
                React.createElement(UI_1.StatusBadge, { value: selected.status })),
            React.createElement("div", null,
                React.createElement("span", null, "Failure reason"),
                React.createElement("strong", null, selected.failureReason || '—')),
            React.createElement("div", null,
                React.createElement("span", null, "Created"),
                React.createElement("strong", null, (0, format_1.formatDate)(selected.createdAt))),
            React.createElement("div", null,
                React.createElement("span", null, "Updated"),
                React.createElement("strong", null, (0, format_1.formatDate)(selected.updatedAt)))))));
}

};
__modules["pages/TransferPage"]=function(module,exports,require){
"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.default = TransferPage;
const react_1 = require("react");
const lucide_react_1 = require("lucide-react");
const AuthContext_1 = require("../context/AuthContext");
const ToastContext_1 = require("../context/ToastContext");
const UI_1 = require("../components/UI");
const api_1 = require("../services/api");
const walletApi_1 = require("../services/walletApi");
const auth_1 = require("../utils/auth");
const format_1 = require("../utils/format");
function TransferPage() {
    const { auth } = (0, AuthContext_1.useAuth)();
    const { showToast } = (0, ToastContext_1.useToast)();
    const walletId = (0, auth_1.getStoredWalletId)(auth?.userId);
    const [form, setForm] = (0, react_1.useState)({ toWalletId: '', amount: '', currency: 'INR' });
    const [loading, setLoading] = (0, react_1.useState)(false);
    const [result, setResult] = (0, react_1.useState)(null);
    (0, react_1.useEffect)(() => {
        if (walletId)
            walletApi_1.walletApi.get(walletId).then((wallet) => setForm((f) => ({ ...f, currency: wallet?.currency || f.currency }))).catch(() => { });
    }, [walletId]);
    async function submit(event) {
        event.preventDefault();
        if (!walletId)
            return showToast('Create or select a wallet before sending money.', 'warning');
        setLoading(true);
        setResult(null);
        try {
            const data = await walletApi_1.transactionApi.transfer({
                fromWalletId: Number(walletId),
                toWalletId: Number(form.toWalletId),
                amount: Number(form.amount),
                currency: form.currency,
                idempotencyKey: (0, format_1.generateIdempotencyKey)('transfer'),
            });
            setResult(data);
            setForm((f) => ({ ...f, toWalletId: '', amount: '' }));
            showToast('Money sent successfully.');
        }
        catch (err) {
            showToast((0, api_1.apiErrorMessage)(err), 'error');
        }
        finally {
            setLoading(false);
        }
    }
    return (React.createElement(React.Fragment, null,
        React.createElement(UI_1.PageHeader, { eyebrow: "Send money", title: "Send to another wallet", description: "Enter the recipient\u2019s wallet number and the amount you want to send." }),
        React.createElement("div", { className: "operation-layout" },
            React.createElement(UI_1.SectionCard, { title: "Transfer details", subtitle: "Review the details carefully before sending." },
                React.createElement("form", { className: "stack-form", onSubmit: submit },
                    React.createElement("div", { className: "source-wallet-pill" },
                        React.createElement(lucide_react_1.WalletCards, { size: 18 }),
                        React.createElement("div", null,
                            React.createElement("span", null, "Sending from"),
                            React.createElement("strong", null,
                                "PhoneWallet \u2022\u2022\u2022\u2022 ",
                                String(walletId || '').padStart(4, '0').slice(-4))),
                        React.createElement("span", null, form.currency)),
                    React.createElement("div", { className: "transfer-direction" },
                        React.createElement("div", { className: "transfer-line" }),
                        React.createElement("div", { className: "transfer-arrow" },
                            React.createElement(lucide_react_1.ArrowRight, { size: 18 }))),
                    React.createElement(UI_1.Field, { label: "Recipient wallet number" },
                        React.createElement("input", { required: true, inputMode: "numeric", value: form.toWalletId, onChange: (e) => setForm({ ...form, toWalletId: e.target.value }), placeholder: "Enter wallet number" })),
                    React.createElement(UI_1.Field, { label: "Amount" },
                        React.createElement("div", { className: "money-input" },
                            React.createElement("span", null, form.currency === 'INR' ? '₹' : form.currency),
                            React.createElement("input", { required: true, min: "0.01", step: "0.01", type: "number", value: form.amount, onChange: (e) => setForm({ ...form, amount: e.target.value }), placeholder: "0.00" }))),
                    React.createElement("div", { className: "secure-operation-note" },
                        React.createElement(lucide_react_1.ShieldCheck, { size: 18 }),
                        React.createElement("div", null,
                            React.createElement("strong", null, "Protected transfer"),
                            React.createElement("span", null, "PhoneWallet checks the request before money is moved."))),
                    React.createElement(UI_1.Button, { type: "submit", loading: loading, disabled: !walletId },
                        React.createElement(lucide_react_1.Send, { size: 17 }),
                        " Send money"))),
            React.createElement(UI_1.SectionCard, { title: "Transfer receipt", subtitle: "Your confirmation will appear here after the transfer." }, result ? (React.createElement("div", { className: "receipt-card" },
                React.createElement("div", { className: "receipt-success" },
                    React.createElement("div", { className: "receipt-check" },
                        React.createElement(lucide_react_1.CheckCircle2, { size: 24 })),
                    React.createElement("span", null, "Transfer complete"),
                    React.createElement("strong", null, (0, format_1.formatMoney)(result.amount, result.currency))),
                React.createElement("div", { className: "receipt-rows" },
                    React.createElement("div", null,
                        React.createElement("span", null, "Reference"),
                        React.createElement("strong", null, result.transactionReference || '—')),
                    React.createElement("div", null,
                        React.createElement("span", null, "Recipient wallet"),
                        React.createElement("strong", null,
                            "\u2022\u2022\u2022\u2022 ",
                            String(result.toWalletId || '').padStart(4, '0').slice(-4))),
                    React.createElement("div", null,
                        React.createElement("span", null, "Status"),
                        React.createElement("strong", null, result.status || '—'))))) : (React.createElement("div", { className: "operation-preview" },
                React.createElement("div", { className: "preview-icon" },
                    React.createElement(lucide_react_1.Send, { size: 28 })),
                React.createElement("h3", null, "No transfer yet"),
                React.createElement("p", null, "Complete the form to send money. Your confirmation and reference will appear here.")))))));
}

};
__modules["pages/WalletPage"]=function(module,exports,require){
"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.default = WalletPage;
const react_1 = require("react");
const lucide_react_1 = require("lucide-react");
const AuthContext_1 = require("../context/AuthContext");
const ToastContext_1 = require("../context/ToastContext");
const api_1 = require("../services/api");
const walletApi_1 = require("../services/walletApi");
const auth_1 = require("../utils/auth");
const format_1 = require("../utils/format");
const UI_1 = require("../components/UI");
function loadRazorpayCheckout() {
    if (window.Razorpay)
        return Promise.resolve();
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
function WalletPage() {
    const { auth, role } = (0, AuthContext_1.useAuth)();
    const { showToast } = (0, ToastContext_1.useToast)();
    const [walletId, setWalletId] = (0, react_1.useState)(() => (0, auth_1.getStoredWalletId)(auth?.userId));
    const [wallets, setWallets] = (0, react_1.useState)([]);
    const [wallet, setWallet] = (0, react_1.useState)(null);
    const [balance, setBalance] = (0, react_1.useState)(null);
    const [currency, setCurrency] = (0, react_1.useState)('INR');
    const [topUpForm, setTopUpForm] = (0, react_1.useState)({ amount: '' });
    const [topUpResult, setTopUpResult] = (0, react_1.useState)(null);
    const [topUps, setTopUps] = (0, react_1.useState)([]);
    const [loading, setLoading] = (0, react_1.useState)(true);
    const [actionLoading, setActionLoading] = (0, react_1.useState)('');
    const [error, setError] = (0, react_1.useState)('');
    const selectedId = walletId ? String(walletId) : '';
    async function loadMine(preferredWalletId = walletId) {
        setLoading(true);
        setError('');
        try {
            const mine = await walletApi_1.walletApi.mine();
            const owned = Array.isArray(mine) ? mine : [];
            setWallets(owned);
            const ownedIds = new Set(owned.map((item) => String((0, format_1.walletIdOf)(item))));
            let selected = preferredWalletId && ownedIds.has(String(preferredWalletId)) ? String(preferredWalletId) : null;
            if (!selected && owned.length)
                selected = String((0, format_1.walletIdOf)(owned[0]));
            if (selected) {
                (0, auth_1.persistWalletId)(auth?.userId, selected);
                setWalletId(selected);
                await refreshSelected(selected, owned);
            }
            else {
                (0, auth_1.clearStoredWalletId)(auth?.userId);
                setWalletId(null);
                setWallet(null);
                setBalance(null);
                setTopUps([]);
            }
        }
        catch (err) {
            setError((0, api_1.apiErrorMessage)(err));
        }
        finally {
            setLoading(false);
        }
    }
    async function refreshSelected(id = walletId, knownWallets = wallets) {
        if (!id)
            return;
        const known = knownWallets.find((item) => String((0, format_1.walletIdOf)(item)) === String(id));
        try {
            const [walletData, balanceData, topUpData] = await Promise.all([
                known ? Promise.resolve(known) : walletApi_1.walletApi.get(id),
                walletApi_1.walletApi.balance(id),
                role === 'USER' ? walletApi_1.topUpApi.byWallet(id, 0, 10) : Promise.resolve({ content: [] }),
            ]);
            setWallet(walletData);
            setBalance(balanceData);
            setTopUps((0, walletApi_1.pageContent)(topUpData));
            setTopUpForm({ amount: '' });
        }
        catch (err) {
            setError((0, api_1.apiErrorMessage)(err));
            throw err;
        }
    }
    (0, react_1.useEffect)(() => { loadMine(); }, [auth?.userId]);
    async function chooseWallet(id) {
        (0, auth_1.persistWalletId)(auth?.userId, id);
        setWalletId(String(id));
        setTopUpResult(null);
        setLoading(true);
        setError('');
        try {
            await refreshSelected(String(id));
        }
        finally {
            setLoading(false);
        }
    }
    async function create(event) {
        event.preventDefault();
        setActionLoading('create');
        try {
            const data = await walletApi_1.walletApi.create({ currency });
            const id = (0, format_1.walletIdOf)(data);
            if (!id)
                throw new Error('Wallet was created, but its ID was not returned.');
            showToast('Your new wallet is ready.');
            await loadMine(id);
        }
        catch (err) {
            showToast((0, api_1.apiErrorMessage)(err), 'error');
        }
        finally {
            setActionLoading('');
        }
    }
    async function initiateTopUp(event) {
        event.preventDefault();
        if (!walletId || role !== 'USER')
            return;
        setActionLoading('topup');
        setTopUpResult(null);
        try {
            const intent = await walletApi_1.topUpApi.initiate(walletId, {
                amount: Number(topUpForm.amount),
                currency: wallet?.currency || 'INR',
                idempotencyKey: (0, format_1.generateIdempotencyKey)('topup'),
            });
            await loadRazorpayCheckout();
            const config = await walletApi_1.topUpApi.checkoutConfig();
            const payment = await openRazorpayCheckout({
                keyId: config.keyId,
                intent,
                username: auth?.username,
            });
            showToast('Payment received. Verifying with Razorpay…', 'info');
            const completed = await walletApi_1.topUpApi.verifyPayment(intent.id, {
                razorpayPaymentId: payment.razorpay_payment_id,
                razorpayOrderId: payment.razorpay_order_id,
                razorpaySignature: payment.razorpay_signature,
            });
            showToast('Payment verified. Money added successfully.');
            setTopUpResult(completed);
            setTopUpForm({ amount: '' });
            await refreshSelected(walletId);
        }
        catch (err) {
            showToast((0, api_1.apiErrorMessage)(err), 'error');
        }
        finally {
            setActionLoading('');
        }
    }
    const activeWallets = (0, react_1.useMemo)(() => wallets.map((item) => ({ ...item, _id: (0, format_1.walletIdOf)(item) })), [wallets]);
    return (React.createElement(React.Fragment, null,
        React.createElement(UI_1.PageHeader, { eyebrow: "Wallet", title: "Your money, in one place", description: "View your balance, switch wallets and add money securely.", actions: React.createElement(UI_1.Button, { variant: "secondary", onClick: () => loadMine(walletId), loading: loading },
                React.createElement(lucide_react_1.RefreshCw, { size: 16 }),
                " Refresh") }),
        loading && !wallet ? React.createElement(UI_1.LoadingBlock, { label: "Loading your wallet\u2026" }) : error && !wallets.length ? React.createElement("div", { className: "error-panel" }, error) : (React.createElement(React.Fragment, null,
            React.createElement("div", { className: "two-panel-grid wallet-setup-grid" },
                React.createElement(UI_1.SectionCard, { title: "Your wallets", subtitle: "Choose which wallet you want to use." }, activeWallets.length ? (React.createElement("div", { className: "stack-form" },
                    React.createElement(UI_1.Field, { label: "Selected wallet" },
                        React.createElement("select", { value: selectedId, onChange: (e) => chooseWallet(e.target.value) }, activeWallets.map((item) => React.createElement("option", { key: item._id, value: item._id },
                            item.currency,
                            " wallet \u00B7 \u2022\u2022\u2022\u2022 ",
                            String(item._id).padStart(4, '0').slice(-4))))),
                    React.createElement("div", { className: "secure-operation-note" },
                        React.createElement(lucide_react_1.ShieldCheck, { size: 18 }),
                        React.createElement("div", null,
                            React.createElement("strong", null, "Only your wallets appear here"),
                            React.createElement("span", null, "Wallet access is tied to your signed-in account."))))) : React.createElement(UI_1.EmptyState, { icon: lucide_react_1.WalletCards, title: "No wallet yet", description: "Create your first wallet to start adding and sending money." })),
                React.createElement(UI_1.SectionCard, { title: "Create another wallet", subtitle: "Set up a wallet in a supported currency." },
                    React.createElement("form", { className: "stack-form", onSubmit: create },
                        React.createElement(UI_1.Field, { label: "Currency" },
                            React.createElement("select", { value: currency, onChange: (e) => setCurrency(e.target.value) },
                                React.createElement("option", { value: "INR" }, "INR \u2014 Indian Rupee"),
                                React.createElement("option", { value: "USD" }, "USD \u2014 US Dollar"),
                                React.createElement("option", { value: "EUR" }, "EUR \u2014 Euro"))),
                        React.createElement(UI_1.Button, { type: "submit", loading: actionLoading === 'create' },
                            React.createElement(lucide_react_1.Plus, { size: 17 }),
                            " Create wallet")))),
            wallet && (React.createElement("div", { className: `wallet-layout ${role !== 'USER' ? 'wallet-layout-single' : ''}` },
                React.createElement("div", null,
                    React.createElement("div", { className: "wallet-detail-card premium-wallet-card" },
                        React.createElement("div", { className: "wallet-detail-head" },
                            React.createElement("div", null,
                                React.createElement("span", null, "Available balance"),
                                React.createElement("strong", null, (0, format_1.formatMoney)(balance, wallet.currency))),
                            React.createElement("div", { className: "wallet-brand-icon" },
                                React.createElement(lucide_react_1.WalletCards, { size: 28 }))),
                        React.createElement("div", { className: "wallet-number" },
                            React.createElement("span", null, "PHONEWALLET"),
                            React.createElement("strong", null,
                                "\u2022\u2022\u2022\u2022 \u2022\u2022\u2022\u2022 ",
                                String(walletId).padStart(4, '0').slice(-4))),
                        React.createElement("div", { className: "wallet-detail-foot" },
                            React.createElement("span", null,
                                React.createElement(lucide_react_1.ShieldCheck, { size: 15 }),
                                " ",
                                wallet.status === 'ACTIVE' ? 'Ready to use' : wallet.status),
                            React.createElement("span", null, wallet.currency))),
                    React.createElement(UI_1.SectionCard, { title: "Wallet details", subtitle: "Details for your selected wallet.", className: "wallet-meta-card" },
                        error && React.createElement("div", { className: "form-alert" }, error),
                        React.createElement("div", { className: "detail-grid" },
                            React.createElement("div", null,
                                React.createElement("span", null, "Wallet number"),
                                React.createElement("strong", null,
                                    "\u2022\u2022\u2022\u2022 ",
                                    String(walletId).padStart(4, '0').slice(-4))),
                            React.createElement("div", null,
                                React.createElement("span", null, "Currency"),
                                React.createElement("strong", null, wallet.currency)),
                            React.createElement("div", null,
                                React.createElement("span", null, "Status"),
                                React.createElement(UI_1.StatusBadge, { value: wallet.status || 'ACTIVE' })),
                            React.createElement("div", null,
                                React.createElement("span", null, "Member ID"),
                                React.createElement("strong", null, auth?.userId ?? '—'))),
                        React.createElement("div", { className: "inline-actions" },
                            React.createElement(UI_1.Button, { variant: "secondary", onClick: () => navigator.clipboard?.writeText(String(walletId)).then(() => showToast('Wallet number copied.')) },
                                React.createElement(lucide_react_1.Copy, { size: 16 }),
                                " Copy wallet number")))),
                role === 'USER' && (React.createElement("div", { className: "stack-form" },
                    React.createElement(UI_1.SectionCard, { title: "Add money", subtitle: "Add funds to your selected wallet." },
                        React.createElement("form", { className: "stack-form", onSubmit: initiateTopUp },
                            React.createElement(UI_1.Field, { label: "Amount" },
                                React.createElement("div", { className: "money-input" },
                                    React.createElement("span", null, wallet.currency === 'INR' ? '₹' : wallet.currency),
                                    React.createElement("input", { required: true, min: "1", max: "50000", step: "0.01", type: "number", value: topUpForm.amount, onChange: (e) => setTopUpForm({ amount: e.target.value }), placeholder: "0.00" }))),
                            React.createElement("div", { className: "amount-chips" }, [500, 1000, 2000, 5000].map((value) => React.createElement("button", { key: value, type: "button", onClick: () => setTopUpForm({ amount: String(value) }) },
                                "+",
                                wallet.currency === 'INR' ? `₹${value}` : value))),
                            React.createElement("div", { className: "idempotency-note" },
                                React.createElement(lucide_react_1.ShieldCheck, { size: 17 }),
                                React.createElement("span", null, "Your payment is checked before your wallet balance changes.")),
                            React.createElement(UI_1.Button, { type: "submit", loading: actionLoading === 'topup', disabled: wallet.status !== 'ACTIVE' },
                                React.createElement(lucide_react_1.Banknote, { size: 17 }),
                                " Add money")),
                        topUpResult && React.createElement("div", { className: "success-panel" },
                            React.createElement(lucide_react_1.ShieldCheck, { size: 18 }),
                            React.createElement("span", null,
                                React.createElement("strong", null, topUpResult.status === 'COMPLETED' ? 'Money added' : 'Top-up started'),
                                topUpResult.transactionReference ? ` · Reference ${topUpResult.transactionReference}` : ''))),
                    React.createElement(UI_1.SectionCard, { title: "Recent top-ups", subtitle: "Your latest add-money activity." }, topUps.length ? React.createElement("div", { className: "transaction-list" }, topUps.map((item) => React.createElement("div", { className: "transaction-row", key: item.id },
                        React.createElement("div", { className: "transaction-icon incoming" },
                            React.createElement(lucide_react_1.Banknote, { size: 19 })),
                        React.createElement("div", { className: "transaction-main" },
                            React.createElement("strong", null, (0, format_1.formatMoney)(item.amount, item.currency)),
                            React.createElement("span", null, (0, format_1.formatDate)(item.createdAt))),
                        React.createElement("div", { className: "transaction-amount" },
                            React.createElement(UI_1.StatusBadge, { value: item.status }))))) : React.createElement(UI_1.EmptyState, { icon: lucide_react_1.Banknote, title: "No top-ups yet", description: "Money you add to this wallet will appear here." }))))))))));
}

};
__modules["services/api"]=function(module,exports,require){
"use strict";
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
Object.defineProperty(exports, "__esModule", { value: true });
exports.API_BASE_URL = void 0;
exports.apiErrorMessage = apiErrorMessage;
const axios_1 = __importDefault(require("axios"));
const auth_1 = require("../utils/auth");
exports.API_BASE_URL = (window.__PHONEWALLET_API_BASE_URL__ || '').trim();
const api = axios_1.default.create({
    baseURL: exports.API_BASE_URL,
    headers: { 'Content-Type': 'application/json' },
    timeout: 15000,
});
let refreshPromise = null;
api.interceptors.request.use((config) => {
    const auth = (0, auth_1.getStoredAuth)();
    if (auth?.accessToken && !config.headers.Authorization) {
        config.headers.Authorization = `Bearer ${auth.accessToken}`;
    }
    return config;
});
api.interceptors.response.use((response) => response, async (error) => {
    const original = error.config;
    const isRefreshRequest = original?.url?.includes('/api/auth/refresh-token');
    if (error.response?.status !== 401 || original?._retry || isRefreshRequest) {
        return Promise.reject(error);
    }
    const auth = (0, auth_1.getStoredAuth)();
    if (!auth?.refreshToken) {
        (0, auth_1.clearStoredAuth)();
        return Promise.reject(error);
    }
    original._retry = true;
    try {
        refreshPromise ?? (refreshPromise = axios_1.default
            .post(`${exports.API_BASE_URL}/api/auth/refresh-token`, { refreshToken: auth.refreshToken })
            .then(({ data }) => {
            const nextAuth = {
                ...auth,
                accessToken: data.accessToken,
                refreshToken: data.refreshToken || auth.refreshToken,
                userId: data.userId ?? auth.userId,
                role: (0, auth_1.extractRole)(data.accessToken),
            };
            (0, auth_1.persistAuth)(nextAuth);
            window.dispatchEvent(new StorageEvent('storage', { key: auth_1.AUTH_STORAGE_KEY }));
            return nextAuth;
        })
            .finally(() => {
            refreshPromise = null;
        }));
        const nextAuth = await refreshPromise;
        original.headers.Authorization = `Bearer ${nextAuth.accessToken}`;
        return api(original);
    }
    catch (refreshError) {
        (0, auth_1.clearStoredAuth)();
        window.location.assign('/login');
        return Promise.reject(refreshError);
    }
});
function apiErrorMessage(error) {
    return (error?.response?.data?.message ||
        (typeof error?.response?.data === 'string' ? error.response.data : null) ||
        error?.message ||
        'Something went wrong. Please try again.');
}
exports.default = api;

};
__modules["services/walletApi"]=function(module,exports,require){
"use strict";
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
Object.defineProperty(exports, "__esModule", { value: true });
exports.adminApi = exports.statementApi = exports.transactionApi = exports.topUpApi = exports.walletApi = exports.authApi = void 0;
exports.pageContent = pageContent;
exports.pageMeta = pageMeta;
const api_1 = __importDefault(require("./api"));
function pageContent(data) {
    return Array.isArray(data) ? data : Array.isArray(data?.content) ? data.content : [];
}
function pageMeta(data) {
    if (!data || Array.isArray(data)) {
        const size = Array.isArray(data) ? data.length : 0;
        return { page: 0, size, totalElements: size, totalPages: size ? 1 : 0, first: true, last: true };
    }
    return {
        page: Number(data.page ?? 0),
        size: Number(data.size ?? 0),
        totalElements: Number(data.totalElements ?? pageContent(data).length),
        totalPages: Number(data.totalPages ?? 0),
        first: Boolean(data.first),
        last: Boolean(data.last),
    };
}
exports.authApi = {
    login: (payload) => api_1.default.post('/api/auth/login', payload).then((r) => r.data),
    signup: (payload) => api_1.default.post('/api/auth/signup', payload).then((r) => r.data),
    logout: (refreshToken) => api_1.default.post('/api/auth/logout', { refreshToken }).then((r) => r.data),
};
exports.walletApi = {
    create: (payload) => api_1.default.post('/api/wallets', payload).then((r) => r.data),
    mine: () => api_1.default.get('/api/wallets/mine').then((r) => r.data),
    get: (walletId) => api_1.default.get(`/api/wallets/${walletId}`).then((r) => r.data),
    balance: (walletId) => api_1.default.get(`/api/wallets/${walletId}/balance`).then((r) => r.data),
    freeze: (walletId) => api_1.default.post(`/api/wallets/${walletId}/freeze`).then((r) => r.data),
    unfreeze: (walletId) => api_1.default.post(`/api/wallets/${walletId}/unfreeze`).then((r) => r.data),
    blacklist: (walletId, reason) => api_1.default.post(`/api/wallets/${walletId}/blacklist`, { reason }).then((r) => r.data),
    unblacklist: (walletId) => api_1.default.post(`/api/wallets/${walletId}/unblacklist`).then((r) => r.data),
};
exports.topUpApi = {
    checkoutConfig: () => api_1.default.get('/api/topups/checkout-config').then((r) => r.data),
    initiate: (walletId, payload) => api_1.default.post(`/api/topups/wallet/${walletId}`, payload).then((r) => r.data),
    verifyPayment: (intentId, payload) => api_1.default.post(`/api/topups/${intentId}/verify-payment`, payload).then((r) => r.data),
    byWallet: (walletId, page = 0, size = 20) => api_1.default.get(`/api/topups/wallet/${walletId}`, { params: { page, size } }).then((r) => r.data),
};
exports.transactionApi = {
    transfer: (payload) => api_1.default.post('/api/transactions/transfer', payload).then((r) => r.data),
    pay: (payload) => api_1.default.post('/api/transactions/pay', payload).then((r) => r.data),
    refund: (payload) => api_1.default.post('/api/transactions/refund', payload).then((r) => r.data),
    reversal: (payload) => api_1.default.post('/api/transactions/reversal', payload).then((r) => r.data),
    get: (transactionId) => api_1.default.get(`/api/transactions/${transactionId}`).then((r) => r.data),
    byWallet: (walletId, page = 0, size = 20) => api_1.default.get(`/api/transactions/wallet/${walletId}`, { params: { page, size } }).then((r) => r.data),
};
exports.statementApi = {
    byWallet: (walletId, page = 0, size = 20) => api_1.default.get(`/api/statements/wallet/${walletId}`, { params: { page, size } }).then((r) => r.data),
    byRange: (walletId, startDate, endDate, page = 0, size = 20) => api_1.default
        .get(`/api/statements/wallet/${walletId}/range`, { params: { startDate, endDate, page, size } })
        .then((r) => r.data),
};
const paging = (page = 0, size = 50) => ({ page, size });
exports.adminApi = {
    summary: () => api_1.default.get('/api/admin/dashboard/summary').then((r) => r.data),
    wallets: (page = 0, size = 50) => api_1.default.get('/api/admin/wallets', { params: paging(page, size) }).then((r) => r.data),
    frozenWallets: (page = 0, size = 50) => api_1.default.get('/api/admin/wallets/frozen', { params: paging(page, size) }).then((r) => r.data),
    blacklistedWallets: (page = 0, size = 50) => api_1.default.get('/api/admin/wallets/blacklisted', { params: paging(page, size) }).then((r) => r.data),
    transactions: (page = 0, size = 50) => api_1.default.get('/api/admin/transactions', { params: paging(page, size) }).then((r) => r.data),
    failedTransactions: (page = 0, size = 50) => api_1.default.get('/api/admin/transactions/failed', { params: paging(page, size) }).then((r) => r.data),
    transactionsByStatus: (status, page = 0, size = 50) => api_1.default.get(`/api/admin/transactions/status/${status}`, { params: paging(page, size) }).then((r) => r.data),
    transactionsByType: (type, page = 0, size = 50) => api_1.default.get(`/api/admin/transactions/type/${type}`, { params: paging(page, size) }).then((r) => r.data),
    auditLogs: (page = 0, size = 50) => api_1.default.get('/api/admin/audit-logs', { params: paging(page, size) }).then((r) => r.data),
    notifications: (page = 0, size = 50) => api_1.default.get('/api/admin/notifications', { params: paging(page, size) }).then((r) => r.data),
    users: (page = 0, size = 50) => api_1.default.get('/api/admin/users', { params: paging(page, size) }).then((r) => r.data),
    updateUserRole: (userId, role) => api_1.default.patch(`/api/admin/users/${userId}/role`, { role }).then((r) => r.data),
};

};
__modules["utils/auth"]=function(module,exports,require){
"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.AUTH_STORAGE_KEY = void 0;
exports.decodeJwtPayload = decodeJwtPayload;
exports.extractRole = extractRole;
exports.getStoredAuth = getStoredAuth;
exports.persistAuth = persistAuth;
exports.clearStoredAuth = clearStoredAuth;
exports.walletStorageKey = walletStorageKey;
exports.getStoredWalletId = getStoredWalletId;
exports.persistWalletId = persistWalletId;
exports.clearStoredWalletId = clearStoredWalletId;
exports.AUTH_STORAGE_KEY = 'phonewallet.auth';
function decodeJwtPayload(token) {
    try {
        if (!token)
            return null;
        const payload = token.split('.')[1];
        if (!payload)
            return null;
        const normalized = payload.replace(/-/g, '+').replace(/_/g, '/');
        const decoded = decodeURIComponent(window
            .atob(normalized)
            .split('')
            .map((char) => `%${`00${char.charCodeAt(0).toString(16)}`.slice(-2)}`)
            .join(''));
        return JSON.parse(decoded);
    }
    catch {
        return null;
    }
}
function extractRole(token) {
    const payload = decodeJwtPayload(token);
    if (!payload)
        return 'USER';
    const raw = payload.role ??
        payload.roles ??
        payload.authorities ??
        payload.scope ??
        payload.scopes ??
        'USER';
    const values = Array.isArray(raw) ? raw : String(raw).split(/[ ,]/).filter(Boolean);
    const normalized = values.map((value) => String(value).replace(/^ROLE_/, '').toUpperCase());
    if (normalized.includes('ADMIN'))
        return 'ADMIN';
    if (normalized.includes('MERCHANT'))
        return 'MERCHANT';
    return normalized[0] || 'USER';
}
function getStoredAuth() {
    try {
        const raw = localStorage.getItem(exports.AUTH_STORAGE_KEY);
        return raw ? JSON.parse(raw) : null;
    }
    catch {
        return null;
    }
}
function persistAuth(auth) {
    localStorage.setItem(exports.AUTH_STORAGE_KEY, JSON.stringify(auth));
}
function clearStoredAuth() {
    localStorage.removeItem(exports.AUTH_STORAGE_KEY);
}
function walletStorageKey(userId) {
    return `phonewallet.wallet.${userId || 'anonymous'}`;
}
function getStoredWalletId(userId) {
    return localStorage.getItem(walletStorageKey(userId));
}
function persistWalletId(userId, walletId) {
    localStorage.setItem(walletStorageKey(userId), String(walletId));
}
function clearStoredWalletId(userId) {
    localStorage.removeItem(walletStorageKey(userId));
}

};
__modules["utils/format"]=function(module,exports,require){
"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.formatMoney = formatMoney;
exports.formatDate = formatDate;
exports.generateIdempotencyKey = generateIdempotencyKey;
exports.shortRef = shortRef;
exports.normalizeDateTimeLocal = normalizeDateTimeLocal;
exports.walletIdOf = walletIdOf;
exports.humanizeKey = humanizeKey;
function formatMoney(value, currency = 'INR') {
    const amount = Number(value ?? 0);
    try {
        return new Intl.NumberFormat('en-IN', {
            style: 'currency',
            currency: currency || 'INR',
            maximumFractionDigits: 2,
        }).format(Number.isFinite(amount) ? amount : 0);
    }
    catch {
        return `${currency || ''} ${Number.isFinite(amount) ? amount.toFixed(2) : '0.00'}`.trim();
    }
}
function formatDate(value) {
    if (!value)
        return '—';
    const date = new Date(value);
    if (Number.isNaN(date.getTime()))
        return String(value);
    return date.toLocaleString('en-IN', {
        day: '2-digit',
        month: 'short',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
    });
}
function generateIdempotencyKey(prefix = 'txn') {
    if (globalThis.crypto?.randomUUID)
        return `${prefix}-${crypto.randomUUID()}`;
    return `${prefix}-${Date.now()}-${Math.random().toString(36).slice(2, 10)}`;
}
function shortRef(value, length = 18) {
    if (!value)
        return '—';
    const text = String(value);
    return text.length > length ? `${text.slice(0, length)}…` : text;
}
function normalizeDateTimeLocal(value) {
    if (!value)
        return value;
    return value.length === 16 ? `${value}:00` : value;
}
function walletIdOf(wallet) {
    return wallet?.walletId ?? wallet?.id ?? wallet?.walletID ?? null;
}
function humanizeKey(value) {
    return String(value || '')
        .replace(/([a-z0-9])([A-Z])/g, '$1 $2')
        .replace(/_/g, ' ')
        .replace(/^./, (char) => char.toUpperCase());
}

};

const __cache = Object.create(null);
const __externals = {
  'react': window.React,
  'react-dom': window.ReactDOM,
  'react-dom/client': { createRoot: window.ReactDOM.createRoot.bind(window.ReactDOM) },
  'react-router-dom': window.ReactRouterDOM,
  'react-router': window.ReactRouter,
  '@remix-run/router': window.RemixRouter,
  'axios': window.axios,
  'lucide-react': window.LucideReact,
};
function __normalize(parts){
  const out=[];
  for(const p of parts){
    if(!p || p==='.') continue;
    if(p==='..') out.pop(); else out.push(p);
  }
  return out.join('/');
}
function __resolve(request,parent){
  if(request.endsWith('.css')) return '__css__';
  if(!request.startsWith('.')) return request;
  const base=parent.split('/'); base.pop();
  const target=__normalize(base.concat(request.split('/'))).replace(/\.(jsx?|mjs)$/,'');
  if(__modules[target]) return target;
  if(__modules[target+'/index']) return target+'/index';
  throw new Error('Cannot resolve module '+request+' from '+parent+' -> '+target);
}
function __require(request,parent='main'){
  const id=__resolve(request,parent);
  if(id==='__css__') return {};
  if(Object.prototype.hasOwnProperty.call(__externals,id)) {
    if(!__externals[id]) throw new Error('External module '+id+' is not available');
    return __externals[id];
  }
  if(__cache[id]) return __cache[id].exports;
  const fn=__modules[id];
  if(!fn) throw new Error('Unknown module '+id+' requested from '+parent);
  const module={exports:{}}; __cache[id]=module;
  fn(module,module.exports,(req)=>__require(req,id));
  return module.exports;
}
__require('main','main');
})();
