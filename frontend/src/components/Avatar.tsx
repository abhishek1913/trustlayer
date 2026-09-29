const GRADIENTS = [
  "linear-gradient(135deg, #6366f1, #8b5cf6)",
  "linear-gradient(135deg, #0ea5e9, #6366f1)",
  "linear-gradient(135deg, #10b981, #0ea5e9)",
  "linear-gradient(135deg, #f59e0b, #ef4444)",
  "linear-gradient(135deg, #ec4899, #8b5cf6)",
];

function initials(name: string): string {
  return name.split(/\s+/).filter(Boolean).slice(0, 2).map((part) => part[0]!.toUpperCase()).join("") || "?";
}

export default function Avatar({ name, seed, large = false }: { name: string; seed: string; large?: boolean }) {
  const index = [...seed].reduce((sum, ch) => sum + ch.charCodeAt(0), 0) % GRADIENTS.length;
  return (
    <span className={`avatar ${large ? "lg" : ""}`} style={{ background: GRADIENTS[index] }} aria-hidden="true">
      {initials(name)}
    </span>
  );
}
