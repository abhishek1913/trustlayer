import { FormEvent, useState } from "react";
import { Link, useLocation, useNavigate } from "react-router-dom";
import { ArrowRight } from "lucide-react";
import { useAuth } from "../auth";
import { useAction } from "../hooks";
import AuthShell from "../components/AuthShell";
import ErrorNotice from "../components/ErrorNotice";

export default function Login() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const { run, error, busy } = useAction();

  async function submit(e: FormEvent) {
    e.preventDefault();
    await run(async () => {
      await login(email, password);
      const from = (location.state as { from?: string } | null)?.from;
      navigate(from ?? "/dashboard", { replace: true });
    });
  }

  return (
    <AuthShell
      title="Welcome back"
      subtitle="Sign in to continue your onboarding."
      footer={<>New here? <Link to="/register">Create an account</Link></>}
    >
      <form onSubmit={submit}>
        <label className="field">
          <span className="field-label">Email</span>
          <input type="email" value={email} onChange={(e) => setEmail(e.target.value)} autoComplete="email" required />
        </label>
        <div className="field">
          <div className="label-row">
            <label className="field-label" htmlFor="login-password">Password</label>
            <Link to="/forgot-password">Forgot password?</Link>
          </div>
          <input id="login-password" type="password" value={password} onChange={(e) => setPassword(e.target.value)} autoComplete="current-password" required />
        </div>
        <ErrorNotice error={error} />
        <button className="block" disabled={busy}>
          {busy ? "Signing in..." : <>Sign in<ArrowRight size={16} /></>}
        </button>
      </form>
    </AuthShell>
  );
}
