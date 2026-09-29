import { Link } from "react-router-dom";
import { ArrowRight, CircleCheck, CircleX, LoaderCircle } from "lucide-react";
import { api, ApiError } from "../api";
import { usePolling } from "../hooks";
import Badge from "../components/Badge";

export default function PaymentResult({ outcome }: { outcome: "success" | "cancel" }) {
  const subscription = usePolling(() => api.subscription().catch((e) => {
    if (e instanceof ApiError && e.status === 404) return null;
    throw e;
  }), 2000, outcome === "success");
  const access = usePolling(api.access, 2000, outcome === "success");
  const active = subscription.data?.status === "ACTIVE";

  if (outcome === "cancel") {
    return (
      <section className="card result">
        <span className="state-icon brand"><CircleX size={32} /></span>
        <h1>Checkout cancelled</h1>
        <p>No payment was taken. You can pick a plan again from the dashboard.</p>
        <div style={{ marginTop: 28 }}><Link to="/dashboard" className="button">Back to dashboard</Link></div>
      </section>
    );
  }

  return (
    <section className="card result">
      <span className={`state-icon ${active ? "" : "brand"}`}>
        {active ? <CircleCheck size={34} /> : <LoaderCircle size={32} className="spin" />}
      </span>
      <h1>{active ? "Payment confirmed" : "Waiting for confirmation"}</h1>
      <p>
        Returning from Stripe proves nothing on its own. TrustLayer waits for Stripe's signed webhook and only then activates
        the subscription and grants access.
      </p>
      <div className="result-rows">
        <div>
          <span className="muted">Subscription</span>
          {subscription.data ? <Badge value={subscription.data.status} /> : <span className="muted small">waiting for webhook...</span>}
        </div>
        <div>
          <span className="muted">Access</span>
          {access.data ? <Badge value={access.data.state} /> : <span className="muted small">...</span>}
        </div>
      </div>
      <Link to="/dashboard" className="button">Go to dashboard<ArrowRight size={16} /></Link>
    </section>
  );
}
