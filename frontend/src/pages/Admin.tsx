import { ReactNode, useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { Bell, BellOff, Clock, CreditCard, Receipt, Search, Users } from "lucide-react";
import { api, DeliverySummary, Page, PaymentSummary, UserSummary } from "../api";
import { formatDate, formatMoney, useAction, useDebounced } from "../hooks";
import Avatar from "../components/Avatar";
import Badge from "../components/Badge";
import ErrorNotice from "../components/ErrorNotice";
import Pager from "../components/Pager";

type Tab = "users" | "payments" | "notifications";

function Table({ head, empty, children }: { head: string[]; empty: boolean; children: ReactNode }) {
  return (
    <div className="table-wrap">
      <table>
        <thead><tr>{head.map((h) => <th key={h}>{h}</th>)}</tr></thead>
        <tbody>{children}</tbody>
      </table>
      {empty && <div className="empty">No results match these filters.</div>}
    </div>
  );
}

function UsersTab() {
  const [email, setEmail] = useState("");
  const [verified, setVerified] = useState("");
  const [pageNo, setPageNo] = useState(0);
  const [page, setPage] = useState<Page<UserSummary> | null>(null);
  const { run, error } = useAction();
  const emailQuery = useDebounced(email, 250);

  useEffect(() => {
    let current = true;
    const query = new URLSearchParams({ page: String(pageNo), size: "10" });
    if (emailQuery) query.set("email", emailQuery);
    if (verified) query.set("emailVerified", verified);
    run(() => api.adminUsers(query.toString()).then((result) => current && setPage(result)));
    return () => { current = false; };
  }, [emailQuery, verified, pageNo, run]);

  return (
    <>
      <div className="toolbar">
        <div className="filters">
          <div className="search">
            <Search size={16} />
            <input placeholder="Search email" value={email} onChange={(e) => { setPageNo(0); setEmail(e.target.value); }} />
          </div>
          <select value={verified} onChange={(e) => { setPageNo(0); setVerified(e.target.value); }}>
            <option value="">Any email status</option>
            <option value="true">Email verified</option>
            <option value="false">Email not verified</option>
          </select>
        </div>
      </div>
      <ErrorNotice error={error} />
      <Table head={["User", "Role", "Email status", "Joined"]} empty={page?.content.length === 0}>
        {page?.content.map((u) => (
          <tr key={u.id}>
            <td>
              <div className="user-cell">
                <Avatar name={u.fullName} seed={u.id} />
                <div className="who">
                  <strong>{u.fullName}</strong>
                  <Link to={`/admin/users/${u.id}`}>{u.email}</Link>
                </div>
              </div>
            </td>
            <td>{u.role}</td>
            <td><Badge value={u.emailVerified ? "VERIFIED" : "PENDING"} /></td>
            <td className="muted">{formatDate(u.createdAt)}</td>
          </tr>
        ))}
      </Table>
      {page && <Pager page={page} onChange={setPageNo} />}
    </>
  );
}

function PaymentsTab() {
  const [status, setStatus] = useState("");
  const [pageNo, setPageNo] = useState(0);
  const [page, setPage] = useState<Page<PaymentSummary> | null>(null);
  const { run, error } = useAction();

  useEffect(() => {
    const query = new URLSearchParams({ page: String(pageNo), size: "10" });
    if (status) query.set("status", status);
    let current = true;
    run(() => api.adminPayments(query.toString()).then((result) => current && setPage(result)));
    return () => { current = false; };
  }, [status, pageNo, run]);

  return (
    <>
      <div className="toolbar">
        <div className="filters">
          <select value={status} onChange={(e) => { setPageNo(0); setStatus(e.target.value); }}>
            <option value="">Any status</option>
            {["PENDING", "SUCCEEDED", "FAILED", "EXPIRED"].map((s) => <option key={s}>{s}</option>)}
          </select>
        </div>
      </div>
      <ErrorNotice error={error} />
      <Table head={["Payment", "User", "Plan", "Amount", "Status", "Created"]} empty={page?.content.length === 0}>
        {page?.content.map((p) => (
          <tr key={p.id}>
            <td className="mono">{p.id.slice(0, 8)}</td>
            <td><Link to={`/admin/users/${p.userId}`} className="mono">{p.userId.slice(0, 8)}</Link></td>
            <td>{p.planCode}</td>
            <td><strong>{formatMoney(p.amountCents, p.currency)}</strong></td>
            <td><Badge value={p.status} /></td>
            <td className="muted">{formatDate(p.createdAt)}</td>
          </tr>
        ))}
      </Table>
      {page && <Pager page={page} onChange={setPageNo} />}
    </>
  );
}

function NotificationsTab() {
  const [status, setStatus] = useState("");
  const [channel, setChannel] = useState("");
  const [pageNo, setPageNo] = useState(0);
  const [page, setPage] = useState<Page<DeliverySummary> | null>(null);
  const { run, error } = useAction();

  useEffect(() => {
    const query = new URLSearchParams({ page: String(pageNo), size: "10" });
    if (status) query.set("status", status);
    if (channel) query.set("channel", channel);
    let current = true;
    run(() => api.adminNotifications(query.toString()).then((result) => current && setPage(result)));
    return () => { current = false; };
  }, [status, channel, pageNo, run]);

  return (
    <>
      <div className="toolbar">
        <div className="filters">
          <select value={status} onChange={(e) => { setPageNo(0); setStatus(e.target.value); }}>
            <option value="">Any status</option>
            {["PENDING", "SENT", "FAILED", "RETRYING"].map((s) => <option key={s}>{s}</option>)}
          </select>
          <select value={channel} onChange={(e) => { setPageNo(0); setChannel(e.target.value); }}>
            <option value="">Any channel</option>
            <option>EMAIL</option>
            <option>WHATSAPP</option>
          </select>
        </div>
      </div>
      <ErrorNotice error={error} />
      <Table head={["Event", "Channel", "Status", "Attempts", "Last error", "Created"]} empty={page?.content.length === 0}>
        {page?.content.map((d) => (
          <tr key={d.id}>
            <td><strong>{d.eventType}</strong></td>
            <td>{d.channel}</td>
            <td><Badge value={d.status} /></td>
            <td>{d.attempts}</td>
            <td className="mono muted">{d.lastError ?? "-"}</td>
            <td className="muted">{formatDate(d.createdAt)}</td>
          </tr>
        ))}
      </Table>
      {page && <Pager page={page} onChange={setPageNo} />}
    </>
  );
}

function Stat({ label, value, icon, tone = "" }: { label: string; value: number | null; icon: ReactNode; tone?: string }) {
  return (
    <div className="card stat">
      <div className="stat-top">
        <span className="stat-label">{label}</span>
        <span className={`stat-icon ${tone}`}>{icon}</span>
      </div>
      <div className="stat-value">{value ?? "-"}</div>
    </div>
  );
}

const TABS: { key: Tab; label: string; icon: ReactNode }[] = [
  { key: "users", label: "Users", icon: <Users size={15} /> },
  { key: "payments", label: "Payments", icon: <Receipt size={15} /> },
  { key: "notifications", label: "Notifications", icon: <Bell size={15} /> },
];

export default function Admin() {
  const [tab, setTab] = useState<Tab>("users");
  const [stats, setStats] = useState<{ users: number; paid: number; pending: number; failed: number } | null>(null);

  useEffect(() => {
    Promise.all([
      api.adminUsers("size=1"),
      api.adminPayments("status=SUCCEEDED&size=1"),
      api.adminPayments("status=PENDING&size=1"),
      api.adminNotifications("status=FAILED&size=1"),
    ]).then(([users, paid, pending, failed]) => setStats({
      users: users.totalElements,
      paid: paid.totalElements,
      pending: pending.totalElements,
      failed: failed.totalElements,
    })).catch(() => undefined);
  }, []);

  return (
    <>
      <div className="page-head">
        <div>
          <span className="eyebrow">Operations</span>
          <h1>Admin console</h1>
          <p className="muted">Read only view across users, payments and notification deliveries.</p>
        </div>
      </div>
      <div className="stats">
        <Stat label="Users" value={stats?.users ?? null} icon={<Users size={17} />} />
        <Stat label="Succeeded payments" value={stats?.paid ?? null} icon={<CreditCard size={17} />} tone="good" />
        <Stat label="Pending checkouts" value={stats?.pending ?? null} icon={<Clock size={17} />} tone="warn" />
        <Stat label="Failed deliveries" value={stats?.failed ?? null} icon={<BellOff size={17} />} tone="bad" />
      </div>
      <section className="card">
        <div className="card-head">
          <div className="segmented" role="tablist">
            {TABS.map((t) => (
              <button key={t.key} role="tab" aria-selected={tab === t.key} className={tab === t.key ? "active" : ""} onClick={() => setTab(t.key)}>
                {t.icon}{t.label}
              </button>
            ))}
          </div>
        </div>
        {tab === "users" && <UsersTab />}
        {tab === "payments" && <PaymentsTab />}
        {tab === "notifications" && <NotificationsTab />}
      </section>
    </>
  );
}
