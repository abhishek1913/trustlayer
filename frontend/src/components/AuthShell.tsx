import { ReactNode } from "react";
import { Link } from "react-router-dom";
import { CreditCard, KeyRound, MailCheck, ScanFace } from "lucide-react";
import Logo from "./Logo";

const STEPS = [
  { icon: MailCheck, title: "Own the email", text: "One-time link, stored only as a hash" },
  { icon: ScanFace, title: "Prove you are real", text: "ID and selfie checked by a third party" },
  { icon: CreditCard, title: "Pay with Stripe", text: "Confirmed only by a signed webhook" },
  { icon: KeyRound, title: "Get access", text: "Granted by the server, never the browser" },
];

interface Props {
  title?: string;
  subtitle?: ReactNode;
  footer?: ReactNode;
  children: ReactNode;
}

export default function AuthShell({ title, subtitle, footer, children }: Props) {
  return (
    <div className="auth">
      <aside className="auth-aside">
        <Link to="/" className="brand"><Logo />TrustLayer</Link>
        <div className="auth-pitch">
          <h2>Real people. Confirmed payments. Access you can trust.</h2>
          <p>One backend runs the full onboarding chain in a fixed order and decides access from trusted provider data only.</p>
          <ul className="auth-steps">
            {STEPS.map(({ icon: Icon, title: stepTitle, text }) => (
              <li key={stepTitle}>
                <span className="auth-step-icon"><Icon size={18} /></span>
                <div>
                  <strong>{stepTitle}</strong>
                  <span>{text}</span>
                </div>
              </li>
            ))}
          </ul>
        </div>
        <p className="auth-note">
          Demo build. Identity, email and WhatsApp run on clearly labelled mock providers unless real ones are configured.
        </p>
      </aside>
      <main className="auth-main">
        <div className="auth-card fade-in">
          <Link to="/" className="brand auth-mobile-brand"><Logo />TrustLayer</Link>
          {title && <h1>{title}</h1>}
          {subtitle && <p className="auth-sub">{subtitle}</p>}
          {children}
          {footer && <div className="auth-footer">{footer}</div>}
        </div>
      </main>
    </div>
  );
}
