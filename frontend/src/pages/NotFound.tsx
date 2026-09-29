import { Link } from "react-router-dom";
import { Compass } from "lucide-react";

export default function NotFound() {
  return (
    <section className="card result">
      <span className="state-icon brand"><Compass size={30} /></span>
      <h1>Page not found</h1>
      <p>The page you are looking for does not exist.</p>
      <div style={{ marginTop: 28 }}><Link to="/" className="button">Go home</Link></div>
    </section>
  );
}
