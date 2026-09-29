import { FormEvent, useState } from "react";
import { Link } from "react-router-dom";
import { ArrowRight, MailCheck } from "lucide-react";
import { api } from "../api";
import { useAction } from "../hooks";
import AuthShell from "../components/AuthShell";
import ErrorNotice from "../components/ErrorNotice";

export default function Register() {
  const [form, setForm] = useState({ fullName: "", email: "", password: "", phoneNumber: "" });
  const [done, setDone] = useState(false);
  const { run, error, busy } = useAction();

  const set = (field: keyof typeof form) => (e: React.ChangeEvent<HTMLInputElement>) =>
    setForm({ ...form, [field]: e.target.value });

  async function submit(e: FormEvent) {
    e.preventDefault();
    const created = await run(() => api.register({ ...form, phoneNumber: form.phoneNumber.trim() || undefined }));
    if (created) setDone(true);
  }

  if (done) {
    return (
      <AuthShell>
        <div className="auth-state">
          <span className="state-icon brand"><MailCheck size={28} /></span>
          <h1>Check your email</h1>
          <p style={{ marginTop: 10 }}>
            We sent a verification link to <strong>{form.email}</strong>. In the local demo the email provider is a mock and
            prints the link in the backend log when <code>MOCK_NOTIFICATION_LOG_BODY=true</code>.
          </p>
          <div className="actions"><Link to="/login">Continue to sign in</Link></div>
        </div>
      </AuthShell>
    );
  }

  return (
    <AuthShell
      title="Create your account"
      subtitle="It takes a minute. Verification comes next."
      footer={<>Already registered? <Link to="/login">Sign in</Link></>}
    >
      <form onSubmit={submit}>
        <label className="field">
          <span className="field-label">Full name</span>
          <input value={form.fullName} onChange={set("fullName")} autoComplete="name" required />
        </label>
        <label className="field">
          <span className="field-label">Email</span>
          <input type="email" value={form.email} onChange={set("email")} autoComplete="email" required />
        </label>
        <label className="field">
          <span className="field-label">Password</span>
          <input type="password" value={form.password} onChange={set("password")} autoComplete="new-password" required minLength={10} />
          <span className="hint">At least 10 characters with upper case, lower case and a digit.</span>
        </label>
        <label className="field">
          <span className="field-label">WhatsApp number <span className="optional">(optional)</span></span>
          <input value={form.phoneNumber} onChange={set("phoneNumber")} placeholder="+919876543210" autoComplete="tel" />
        </label>
        <ErrorNotice error={error} />
        <button className="block" disabled={busy}>
          {busy ? "Creating..." : <>Create account<ArrowRight size={16} /></>}
        </button>
      </form>
    </AuthShell>
  );
}
