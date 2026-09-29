import { ReactNode } from "react";
import { Link, NavLink, useLocation, useNavigate } from "react-router-dom";
import { LayoutDashboard, LogOut, Shield, User } from "lucide-react";
import { useAuth } from "../auth";
import Logo from "./Logo";
import Avatar from "./Avatar";

const AUTH_PATHS = ["/login", "/register", "/verify-email", "/forgot-password", "/reset-password"];

export default function Layout({ children }: { children: ReactNode }) {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const { pathname } = useLocation();

  if (AUTH_PATHS.includes(pathname)) return <>{children}</>;

  async function signOut() {
    await logout();
    navigate("/login");
  }

  return (
    <>
      <header className="topbar">
        <div className="topbar-inner">
          <Link to="/" className="brand"><Logo />TrustLayer</Link>
          {user && (
            <nav className="nav">
              <NavLink to="/dashboard"><LayoutDashboard size={16} /><span>Dashboard</span></NavLink>
              <NavLink to="/profile"><User size={16} /><span>Profile</span></NavLink>
              {user.role === "ADMIN" && <NavLink to="/admin"><Shield size={16} /><span>Admin</span></NavLink>}
            </nav>
          )}
          <div className="spacer" />
          {user ? (
            <div className="user-chip">
              <div className="user-meta">
                <strong>{user.fullName}</strong>
                <span>{user.email}</span>
              </div>
              <Avatar name={user.fullName} seed={user.id} />
              <button className="ghost" onClick={signOut}><LogOut size={15} />Sign out</button>
            </div>
          ) : (
            <div className="row">
              <Link to="/login">Sign in</Link>
            </div>
          )}
        </div>
      </header>
      <main className="container fade-in" key={pathname}>{children}</main>
    </>
  );
}
