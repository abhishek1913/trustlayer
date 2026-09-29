import { useCallback, useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";
import { ArrowLeft, Check, CircleX, CreditCard, KeyRound, Mail, ScanFace, ShieldCheck, TriangleAlert } from "lucide-react";
import { api, UserOverview } from "../api";
import { formatDate, formatMoney, useAction } from "../hooks";
import Avatar from "../components/Avatar";
import Badge from "../components/Badge";
import ErrorNotice from "../components/ErrorNotice";

export default function AdminUser() {
  const { id = "" } = useParams();
  const [overview, setOverview] = useState<UserOverview | null>(null);
  const load = useAction();
  const decide = useAction();
  const { run } = load;

  const refresh = useCallback(() => run(() => api.adminOverview(id).then(setOverview)), [id, run]);

  useEffect(() => {
    refresh();
  }, [refresh]);

  async function decision(value: "VERIFIED" | "REJECTED") {
    if (!overview?.verification) return;
    const result = await decide.run(() => api.mockDecision(overview.verification!.sessionId, value));
    if (result) await refresh();
  }

  const verification = overview?.verification;
  const canDecide = verification && verification.provider === "mock" && ["PENDING", "IN_PROGRESS"].includes(verification.status);
  const access = overview?.access;
  const facts = access ? [
    { label: "Email", on: access.emailVerified, value: access.emailVerified ? "VERIFIED" : "PENDING", icon: Mail },
    { label: "Identity", on: access.identityVerified, value: access.identityVerified ? "VERIFIED" : "PENDING", icon: ScanFace },
    { label: "Payment", on: access.paymentSucceeded, value: access.paymentSucceeded ? "SUCCEEDED" : "PENDING", icon: CreditCard },
    { label: "Subscription", on: access.subscriptionActive, value: access.subscriptionActive ? "ACTIVE" : "PENDING", icon: KeyRound },
  ] : [];

  return (
    <>
      <Link to="/admin" className="back"><ArrowLeft size={15} />Back to admin</Link>
      <ErrorNotice error={load.error} />
      {overview && (
        <>
          <div className="profile-head">
            <Avatar name={overview.user.fullName} seed={overview.user.id} large />
            <div>
              <h1>{overview.user.fullName}</h1>
              <p className="muted">{overview.user.email} &middot; {overview.user.role} &middot; joined {formatDate(overview.user.createdAt)}</p>
            </div>
          </div>

          <div className="grid-2">
            <section className="card">
              <div className="card-head">
                <h2>Access</h2>
                {access && <Badge value={access.state} />}
              </div>
              {access ? (
                <ul className="facts">
                  <li className={`fact ${access.state === "GRANTED" ? "on" : "off"}`}>
                    <span className="fact-icon"><ShieldCheck size={16} /></span>
                    <span className="fact-text"><strong>Current state</strong><Badge value={access.state} /></span>
                  </li>
                  {facts.map(({ label, on, value, icon: Icon }) => (
                    <li key={label} className={`fact ${on ? "on" : ""}`}>
                      <span className="fact-icon">{on ? <Check size={16} strokeWidth={2.6} /> : <Icon size={16} />}</span>
                      <span className="fact-text"><strong>{label}</strong><Badge value={value} /></span>
                    </li>
                  ))}
                </ul>
              ) : <p className="muted">No access record yet.</p>}
            </section>

            <section className="card">
              <div className="card-head">
                <h2>Identity verification</h2>
                {verification && <Badge value={verification.status} />}
              </div>
              {verification ? (
                <div className="stack">
                  <dl className="kv">
                    <div><dt>Provider</dt><dd><code>{verification.provider}</code></dd></div>
                    <div><dt>Attempts</dt><dd>{verification.attempts} of 3</dd></div>
                    <div><dt>Last update</dt><dd>{formatDate(verification.updatedAt)}</dd></div>
                  </dl>
                  {canDecide && (
                    <>
                      <div className="notice warn"><TriangleAlert size={17} /><span>MOCK provider: choose the outcome yourself. No real document or selfie check happens.</span></div>
                      <ErrorNotice error={decide.error} />
                      <div className="row">
                        <button disabled={decide.busy} onClick={() => decision("VERIFIED")}><Check size={16} />Mark verified</button>
                        <button className="danger" disabled={decide.busy} onClick={() => decision("REJECTED")}><CircleX size={16} />Mark rejected</button>
                      </div>
                    </>
                  )}
                </div>
              ) : <p className="muted">No verification started.</p>}
            </section>
          </div>

          <section className="card section-gap">
            <div className="card-head">
              <h2>Payments</h2>
              {overview.subscription
                ? <span className="row small muted">Subscription <Badge value={overview.subscription.status} /> {overview.subscription.planCode}</span>
                : <span className="small muted">No subscription</span>}
            </div>
            <div className="table-wrap">
              <table>
                <thead><tr><th>Payment</th><th>Plan</th><th>Amount</th><th>Status</th><th>Created</th></tr></thead>
                <tbody>
                  {overview.payments.map((p) => (
                    <tr key={p.id}>
                      <td className="mono">{p.id.slice(0, 8)}</td>
                      <td>{p.planCode}</td>
                      <td><strong>{formatMoney(p.amountCents, p.currency)}</strong></td>
                      <td><Badge value={p.status} /></td>
                      <td className="muted">{formatDate(p.createdAt)}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
              {overview.payments.length === 0 && <div className="empty">No payments yet.</div>}
            </div>
          </section>

          <section className="card">
            <div className="card-head">
              <h2>Recent notifications</h2>
              <span className="small muted">Last {overview.notifications.length}</span>
            </div>
            <div className="table-wrap">
              <table>
                <thead><tr><th>Event</th><th>Channel</th><th>Status</th><th>Attempts</th><th>Created</th></tr></thead>
                <tbody>
                  {overview.notifications.map((n) => (
                    <tr key={n.id}>
                      <td><strong>{n.eventType}</strong></td>
                      <td>{n.channel}</td>
                      <td><Badge value={n.status} /></td>
                      <td>{n.attempts}</td>
                      <td className="muted">{formatDate(n.createdAt)}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
              {overview.notifications.length === 0 && <div className="empty">No notifications yet.</div>}
            </div>
          </section>
        </>
      )}
    </>
  );
}
