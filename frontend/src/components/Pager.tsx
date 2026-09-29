import { ChevronLeft, ChevronRight } from "lucide-react";
import { Page } from "../api";

export default function Pager<T>({ page, onChange }: { page: Page<T>; onChange: (next: number) => void }) {
  return (
    <div className="pager">
      <span className="muted small">
        Page {page.page + 1} of {Math.max(page.totalPages, 1)} &middot; {page.totalElements} total
      </span>
      <div className="row">
        <button className="ghost" disabled={page.page <= 0} onClick={() => onChange(page.page - 1)}>
          <ChevronLeft size={16} />Previous
        </button>
        <button className="ghost" disabled={page.page + 1 >= page.totalPages} onClick={() => onChange(page.page + 1)}>
          Next<ChevronRight size={16} />
        </button>
      </div>
    </div>
  );
}
