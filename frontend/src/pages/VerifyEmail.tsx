import { FormEvent, useEffect, useRef, useState } from "react";
import { Link, useSearchParams } from "react-router-dom";
import { CircleCheck, LoaderCircle } from "lucide-react";
import { api } from "../api";
import { useAction } from "../hooks";
import AuthShell from "../components/AuthShell";
import ErrorNotice from "../components/ErrorNotice";

export default function VerifyEmail() {
  const [params] = useSearchParams();
  const linkToken = params.get("token");
  const [token, setToken] = useState(linkToken ?? "");
  const [verified, setVerified] = useState(false);
  const { run, error, busy } = useAction();
  const attempted = useRef(false);

  async function verify(value: string) {
    const result = await run(() => api.verifyEmail(value.trim()).then(() => true));
    if (result) setVerified(true);
  }

  useEffect(() => {
    if (linkToken && !attempted.current) {
      attempted.current = true;
      verify(linkToken);
    }
  }, [linkToken]);

  function submit(e: FormEvent) {
    e.preventDefault();
    verify(token);
  }

  if (verified) {
    return (
      <AuthShell>
        <div className="auth-state">
          <span className="state-icon"><CircleCheck size={30} /></span>
          <h1>Email verified</h1>
          <p style={{ marginTop: 10 }}>Your email is confirmed. Sign in to continue with identity verification.</p>
          <div className="actions"><Link to="/login">Sign in</Link></div>
        </div>
      </AuthShell>
    );
  }

  return (
    <AuthShell title="Verify your email" subtitle="Paste the token from the link we emailed you.">
      {busy && <p className="row muted small" style={{ marginBottom: 16 }}><LoaderCircle className="spin" size={16} />Verifying...</p>}
      <form onSubmit={submit}>
        <label className="field">
          <span className="field-label">Verification token</span>
          <input value={token} onChange={(e) => setToken(e.target.value)} className="mono" required />
        </label>
        <ErrorNotice error={error} />
        <button className="block" disabled={busy}>Verify email</button>
      </form>
    </AuthShell>
  );
}
