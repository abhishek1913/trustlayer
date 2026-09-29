const TONES: Record<string, string> = {
  GRANTED: "good",
  VERIFIED: "good",
  SUCCEEDED: "good",
  ACTIVE: "good",
  SENT: "good",
  BLOCKED: "bad",
  REJECTED: "bad",
  FAILED: "bad",
  CANCELLED: "bad",
  EXPIRED: "warn",
  PENDING: "warn",
  IN_PROGRESS: "warn",
  RETRYING: "warn",
};

export default function Badge({ value }: { value: string }) {
  return (
    <span className={`badge ${TONES[value] ?? ""}`}>
      <span className="dot" />
      {value.replace("_", " ")}
    </span>
  );
}
