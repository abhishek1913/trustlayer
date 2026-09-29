import { FormEvent, useState } from "react";
import { Link } from "react-router-dom";
import { MailCheck } from "lucide-react";
import { api } from "../api";
import { useAction } from "../hooks";
import AuthShell from "../components/AuthShell";
import ErrorNotice from "../components/ErrorNotice";

export default function ForgotPassword() {
  const [email, setEmail] = useState("");
  const [sent, setSent] = useState(false);
  const { run, error, busy } = useAction();

  async function submit(e: FormEvent) {
    e.preventDefault();
    const ok = await run(() => api.requestReset(email).then(() => true));
    if (ok) setSent(true);
  }

  if (sent) {
    return (
      <AuthShell>
        <div className="auth-state">
          <span className="state-icon brand"><MailCheck size={28} /></span>
          <h1>Check your email</h1>
          <p style={{ marginTop: 10 }}>If an account exists for that address, a reset link is on its way. It expires in one hour.</p>
          <div className="actions"><Link to="/login">Back to sign in</Link></div>
        </div>
      </AuthShell>
    );
  }

  return (
    <AuthShell
      title="Reset your password"
      subtitle="We will email you a one-time link."
      footer={<Link to="/login">Back to sign in</Link>}
    >
      <form onSubmit={submit}>
        <label className="field">
          <span className="field-label">Email</span>
          <input type="email" value={email} onChange={(e) => setEmail(e.target.value)} autoComplete="email" required />
        </label>
        <ErrorNotice error={error} />
        <button className="block" disabled={busy}>Send reset link</button>
      </form>
    </AuthShell>
  );
}
