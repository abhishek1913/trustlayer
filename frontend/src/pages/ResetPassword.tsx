import { FormEvent, useState } from "react";
import { Link, useSearchParams } from "react-router-dom";
import { CircleCheck } from "lucide-react";
import { api } from "../api";
import { useAction } from "../hooks";
import AuthShell from "../components/AuthShell";
import ErrorNotice from "../components/ErrorNotice";

export default function ResetPassword() {
  const [params] = useSearchParams();
  const [token, setToken] = useState(params.get("token") ?? "");
  const [password, setPassword] = useState("");
  const [done, setDone] = useState(false);
  const { run, error, busy } = useAction();

  async function submit(e: FormEvent) {
    e.preventDefault();
    const ok = await run(() => api.confirmReset(token.trim(), password).then(() => true));
    if (ok) setDone(true);
  }

  if (done) {
    return (
      <AuthShell>
        <div className="auth-state">
          <span className="state-icon"><CircleCheck size={30} /></span>
          <h1>Password updated</h1>
          <p style={{ marginTop: 10 }}>Every session was signed out for safety.</p>
          <div className="actions"><Link to="/login">Sign in with the new password</Link></div>
        </div>
      </AuthShell>
    );
  }

  return (
    <AuthShell title="Choose a new password" subtitle="All your other sessions will be signed out.">
      <form onSubmit={submit}>
        {!params.get("token") && (
          <label className="field">
            <span className="field-label">Reset token</span>
            <input value={token} onChange={(e) => setToken(e.target.value)} className="mono" required />
          </label>
        )}
        <label className="field">
          <span className="field-label">New password</span>
          <input type="password" value={password} onChange={(e) => setPassword(e.target.value)} autoComplete="new-password" required minLength={10} />
          <span className="hint">At least 10 characters with upper case, lower case and a digit.</span>
        </label>
        <ErrorNotice error={error} />
        <button className="block" disabled={busy}>Update password</button>
      </form>
    </AuthShell>
  );
}
