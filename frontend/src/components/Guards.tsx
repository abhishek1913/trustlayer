import { ReactNode } from "react";
import { Navigate, useLocation } from "react-router-dom";
import { LoaderCircle, ShieldAlert } from "lucide-react";
import { useAuth } from "../auth";

function Loading() {
  return <div className="loading"><LoaderCircle className="spin" size={24} /></div>;
}

export function RequireAuth({ children }: { children: ReactNode }) {
  const { user, loading } = useAuth();
  const location = useLocation();
  if (loading) return <Loading />;
  if (!user) return <Navigate to="/login" replace state={{ from: location.pathname }} />;
  return <>{children}</>;
}

export function RequireAdmin({ children }: { children: ReactNode }) {
  const { user, loading } = useAuth();
  if (loading) return <Loading />;
  if (!user) return <Navigate to="/login" replace />;
  if (user.role !== "ADMIN") {
    return (
      <div className="card result">
        <span className="state-icon brand"><ShieldAlert size={30} /></span>
        <h1>Restricted area</h1>
        <p>Admin access is required for this page.</p>
      </div>
    );
  }
  return <>{children}</>;
}
