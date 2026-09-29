import { FormEvent, useEffect, useState } from "react";
import { CircleCheck } from "lucide-react";
import { api } from "../api";
import { useAuth } from "../auth";
import { formatDate, useAction } from "../hooks";
import Avatar from "../components/Avatar";
import Badge from "../components/Badge";
import ErrorNotice from "../components/ErrorNotice";

export default function Profile() {
  const { user, reload } = useAuth();
  const [fullName, setFullName] = useState("");
  const [phoneNumber, setPhoneNumber] = useState("");
  const [saved, setSaved] = useState(false);
  const { run, error, busy } = useAction();

  useEffect(() => {
    if (user) {
      setFullName(user.fullName);
      setPhoneNumber(user.phoneNumber ?? "");
    }
  }, [user]);

  async function submit(e: FormEvent) {
    e.preventDefault();
    setSaved(false);
    const updated = await run(() => api.updateMe({ fullName, phoneNumber: phoneNumber.trim() || undefined }));
    if (updated) {
      await reload();
      setSaved(true);
    }
  }

  if (!user) return null;

  return (
    <div style={{ maxWidth: 720, margin: "0 auto" }}>
      <div className="profile-head">
        <Avatar name={user.fullName} seed={user.id} large />
        <div>
          <h1>{user.fullName}</h1>
          <p className="muted">{user.email}</p>
        </div>
      </div>
      <div className="grid-2">
        <section className="card">
          <div className="card-head"><h2>Account</h2></div>
          <dl className="kv">
            <div><dt>Role</dt><dd>{user.role}</dd></div>
            <div><dt>Email</dt><dd><Badge value={user.emailVerified ? "VERIFIED" : "PENDING"} /></dd></div>
            <div><dt>Member since</dt><dd>{formatDate(user.createdAt)}</dd></div>
          </dl>
        </section>
        <section className="card">
          <div className="card-head">
            <div>
              <h2>Details</h2>
              <p className="muted small">Used for email and WhatsApp notifications.</p>
            </div>
          </div>
          <form onSubmit={submit}>
            <label className="field">
              <span className="field-label">Full name</span>
              <input value={fullName} onChange={(e) => setFullName(e.target.value)} required />
            </label>
            <label className="field">
              <span className="field-label">WhatsApp number</span>
              <input value={phoneNumber} onChange={(e) => setPhoneNumber(e.target.value)} placeholder="+919876543210" />
            </label>
            <ErrorNotice error={error} />
            {saved && <div className="notice ok fade-in"><CircleCheck size={17} />Profile saved.</div>}
            <button disabled={busy}>Save</button>
          </form>
        </section>
      </div>
    </div>
  );
}
