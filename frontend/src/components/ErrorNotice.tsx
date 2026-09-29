import { CircleAlert } from "lucide-react";
import { ApiError } from "../api";

export default function ErrorNotice({ error }: { error: ApiError | null }) {
  if (!error) return null;
  return (
    <div className="notice error fade-in" role="alert">
      <CircleAlert size={17} />
      <div>
        <strong>{error.code}</strong> {error.message}
        {error.details.length > 0 && (
          <ul>
            {error.details.map((d) => (
              <li key={d.field}>{d.field}: {d.message}</li>
            ))}
          </ul>
        )}
        {error.correlationId && <div className="meta mono">Correlation ID {error.correlationId}</div>}
      </div>
    </div>
  );
}
