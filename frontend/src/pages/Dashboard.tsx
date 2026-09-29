import { ReactNode, useEffect, useRef, useState } from "react";
import { Check, CreditCard, ExternalLink, KeyRound, Lock, Mail, RefreshCw, ScanFace, ShieldCheck, TriangleAlert } from "lucide-react";
import { AccessSummary, api, ApiError, Plan, VerificationView } from "../api";
import { useAuth } from "../auth";
import { formatDate, formatMoney, useAction, usePolling } from "../hooks";
import Badge from "../components/Badge";
import ErrorNotice from "../components/ErrorNotice";

const SESSION_KEY = "tl.verification";
const PLAN_FEATURES = ["Access unlocks once Stripe confirms payment", "Billed monthly in Stripe test mode", "Cancel any time"];

type StepState = "done" | "current" | "locked";

const STATUS_LABEL: Record<StepState, string> = { done: "Complete", current: "Up next", locked: "Locked" };

function Step({ index, title, subtitle, icon, state, children }: {
  index: number;
  title: string;
  subtitle: string;
  icon: ReactNode;
  state: StepState;
  children: ReactNode;
}) {
  return (
    <section className={`card step ${state}`}>
      <div className="step-head">
        <span className="step-icon">{state === "done" ? <Check size={20} strokeWidth={2.6} /> : icon}</span>
        <div>
          <span className="step-index">Step {index}</span>
          <h2>{title}</h2>
          <p className="step-sub">{subtitle}</p>
        </div>
        <span className={`step-status ${state}`}>
          {state === "locked" && <Lock size={12} />}
          {state === "done" && <Check size={13} strokeWidth={3} />}
          {STATUS_LABEL[state]}
        </span>
      </div>
      <div className="step-body">{children}</div>
    </section>
  );
}

function AccessRing({ passed }: { passed: number }) {
  const radius = 23;
  const circumference = 2 * Math.PI * radius;
  return (
    <svg width="60" height="60" viewBox="0 0 60 60" aria-hidden="true">
      <defs>
        <linearGradient id="ring-gradient" x1="0" y1="0" x2="1" y2="1">
          <stop offset="0%" stopColor="#6366f1" />
          <stop offset="100%" stopColor="#10b981" />
        </linearGradient>
      </defs>
      <circle cx="30" cy="30" r={radius} className="ring-track" />
      <circle
        cx="30"
        cy="30"
        r={radius}
        className="ring-value"
        strokeDasharray={circumference}
        strokeDashoffset={circumference * (1 - passed / 4)}
        transform="rotate(-90 30 30)"
      />
      <text x="30" y="34" textAnchor="middle" className="ring-text">{passed}/4</text>
    </svg>
  );
}

function Facts({ facts }: { facts: AccessSummary }) {
  const items = [
    { label: "Email verified", on: facts.emailVerified, icon: Mail },
    { label: "Identity verified", on: facts.identityVerified, icon: ScanFace },
    { label: "Payment succeeded", on: facts.paymentSucceeded, icon: CreditCard },
    { label: "Subscription active", on: facts.subscriptionActive, icon: KeyRound },
  ];
  return (
    <ul className="facts">
      {items.map(({ label, on, icon: Icon }) => (
        <li key={label} className={`fact ${on ? "on" : ""}`}>
          <span className="fact-icon">{on ? <Check size={16} strokeWidth={2.6} /> : <Icon size={16} />}</span>
          <span className="fact-text">
            <strong>{label}</strong>
            <span>{on ? "Confirmed by the server" : "Waiting"}</span>
          </span>
        </li>
      ))}
    </ul>
  );
}

export default function Dashboard() {
  const { user } = useAuth();
  const access = usePolling(api.access, 3000);
  const subscription = usePolling(() => api.subscription().catch((e) => {
    if (e instanceof ApiError && e.status === 404) return null;
    throw e;
  }), 3000);
  const [plans, setPlans] = useState<Plan[]>([]);
  const [session, setSession] = useState<VerificationView | null>(null);
  const start = useAction();
  const pay = useAction();
  const evaluate = useAction();
  const keys = useRef<Record<string, string>>({});

  useEffect(() => {
    api.plans().then(setPlans).catch(() => undefined);
    const stored = sessionStorage.getItem(SESSION_KEY);
    if (stored) api.verification(stored).then(setSession).catch(() => sessionStorage.removeItem(SESSION_KEY));
  }, []);

  const facts = access.data;
  const emailDone = facts?.emailVerified ?? false;
  const identityDone = facts?.identityVerified ?? false;
  const payDone = subscription.data?.status === "ACTIVE";
  const granted = facts?.state === "GRANTED";
  const inProgress = session && (session.status === "PENDING" || session.status === "IN_PROGRESS");
  const passed = facts ? [facts.emailVerified, facts.identityVerified, facts.paymentSucceeded, facts.subscriptionActive].filter(Boolean).length : 0;

  const completion = [emailDone, identityDone, payDone, granted];
  const firstOpen = completion.indexOf(false);
  const stateOf = (i: number): StepState => (completion[i] ? "done" : i === firstOpen ? "current" : "locked");

  useEffect(() => {
    if (!session || !inProgress) return;
    const timer = setInterval(() => api.verification(session.id).then(setSession).catch(() => undefined), 3000);
    return () => clearInterval(timer);
  }, [session?.id, inProgress]);

  async function startVerification() {
    const created = await start.run(() => api.startVerification());
    if (created) {
      sessionStorage.setItem(SESSION_KEY, created.id);
      setSession(created);
    }
  }

  async function checkout(plan: Plan) {
    keys.current[plan.code] = keys.current[plan.code] ?? crypto.randomUUID();
    const result = await pay.run(() => api.checkout(plan.code, keys.current[plan.code]));
    if (result) window.location.href = result.checkoutUrl;
  }

  return (
    <>
      <div className="page-head">
        <div>
          <span className="eyebrow">Onboarding</span>
          <h1>Welcome, {user?.fullName}</h1>
          <p className="muted">Finish each step in order. Access is decided by the server from trusted provider data.</p>
        </div>
        <div className={`access-card ${granted ? "granted" : ""}`}>
          <AccessRing passed={passed} />
          <div>
            <span className="eyebrow">Access status</span>
            {facts ? <Badge value={facts.state} /> : <span className="muted">...</span>}
            <div className="small muted" style={{ marginTop: 4 }}>{passed} of 4 checks passed</div>
          </div>
        </div>
      </div>

      <div className="progress" aria-hidden="true">
        {completion.map((done, i) => <span key={i} className={done ? "on" : ""} />)}
      </div>

      <ErrorNotice error={access.error} />

      <div className="steps">
        <Step index={1} title="Verify your email" subtitle="Prove you own the address" icon={<Mail size={20} />} state={stateOf(0)}>
          {emailDone
            ? <p>Your email is verified.</p>
            : <p>Open the link we emailed you. Nothing else unlocks until this is done.</p>}
        </Step>

        <Step index={2} title="Verify your identity" subtitle="Government ID plus a selfie, checked by a third party" icon={<ScanFace size={20} />} state={stateOf(1)}>
          {identityDone && <p>Your identity is verified.</p>}
          {!identityDone && !emailDone && <p>Verify your email first.</p>}
          {!identityDone && emailDone && (
            <>
              {session?.notice && (
                <div className="notice warn"><TriangleAlert size={17} /><span>{session.notice}</span></div>
              )}
              {session && (
                <div className="session-line">
                  <span className="muted">Session</span>
                  <Badge value={session.status} />
                  <span className="muted">via</span>
                  <code>{session.provider}</code>
                  {inProgress && <span className="muted small">expires {formatDate(session.expiresAt)}</span>}
                </div>
              )}
              {session?.redirectUrl && inProgress && (
                <div><a className="button" href={session.redirectUrl} target="_blank" rel="noreferrer">Open identity check<ExternalLink size={15} /></a></div>
              )}
              {session?.mock && inProgress && (
                <p className="muted small">With the mock provider an admin decides the outcome from the Admin page.</p>
              )}
              <ErrorNotice error={start.error} />
              {!inProgress && (
                <div>
                  <button disabled={start.busy} onClick={startVerification}>
                    <ScanFace size={16} />
                    {session?.status === "REJECTED" || session?.status === "EXPIRED" ? "Try again" : "Start verification"}
                  </button>
                </div>
              )}
            </>
          )}
        </Step>

        <Step index={3} title="Choose a plan and pay" subtitle="Secure checkout hosted by Stripe" icon={<CreditCard size={20} />} state={stateOf(2)}>
          {payDone ? (
            <div className="session-line">
              <span>Subscription</span>
              <Badge value="ACTIVE" />
              <span>on <strong>{subscription.data?.planCode}</strong> since {formatDate(subscription.data?.startedAt ?? null)}</span>
            </div>
          ) : !identityDone ? (
            <p>Verify your identity first.</p>
          ) : (
            <>
              {subscription.data?.status === "CANCELLED" && (
                <div className="notice warn"><TriangleAlert size={17} /><span>Your previous subscription was cancelled. Pick a plan to resubscribe.</span></div>
              )}
              <div className="plans">
                {plans.map((plan) => (
                  <div className="plan" key={plan.code}>
                    <div>
                      <div className="plan-name">{plan.name}</div>
                      <div className="price">
                        {formatMoney(plan.amountCents, plan.currency)}
                        <span className="per"> / {plan.interval}</span>
                      </div>
                    </div>
                    <ul className="features">
                      {PLAN_FEATURES.map((f) => <li key={f}><Check size={15} strokeWidth={2.6} />{f}</li>)}
                    </ul>
                    <button disabled={pay.busy} onClick={() => checkout(plan)}><CreditCard size={16} />Pay with Stripe</button>
                  </div>
                ))}
              </div>
              <ErrorNotice error={pay.error} />
              <p className="muted small">
                Payment status changes only when Stripe calls our webhook, never from this page.
              </p>
            </>
          )}
        </Step>

        <Step index={4} title="Access" subtitle="Unlocks when all four checks pass" icon={<ShieldCheck size={20} />} state={stateOf(3)}>
          {facts && <Facts facts={facts} />}
          <ErrorNotice error={evaluate.error} />
          <div>
            <button className="ghost" disabled={evaluate.busy} onClick={() => evaluate.run(async () => { await api.evaluateAccess(); await access.refresh(); })}>
              <RefreshCw size={15} className={evaluate.busy ? "spin" : ""} />Re-evaluate access
            </button>
          </div>
        </Step>
      </div>
    </>
  );
}
