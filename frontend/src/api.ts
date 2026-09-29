export interface UserSummary {
  id: string;
  email: string;
  fullName: string;
  phoneNumber: string | null;
  emailVerified: boolean;
  role: "USER" | "ADMIN";
  createdAt: string;
}

export interface TokenResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresIn: number;
}

export interface VerificationView {
  id: string;
  status: "PENDING" | "IN_PROGRESS" | "VERIFIED" | "REJECTED" | "EXPIRED";
  provider: string;
  mock: boolean;
  expiresAt: string;
  redirectUrl: string | null;
  notice: string | null;
}

export interface Plan {
  code: string;
  name: string;
  amountCents: number;
  currency: string;
  interval: string;
}

export interface CheckoutResult {
  paymentId: string;
  status: string;
  checkoutUrl: string;
  planCode: string;
  replayed: boolean;
}

export interface PaymentSummary {
  id: string;
  userId: string;
  planCode: string;
  status: string;
  amountCents: number;
  currency: string;
  createdAt: string;
}

export interface SubscriptionSummary {
  id: string;
  userId: string;
  planCode: string;
  status: string;
  startedAt: string;
  cancelledAt: string | null;
}

export interface AccessSummary {
  state: "GRANTED" | "BLOCKED";
  emailVerified: boolean;
  identityVerified: boolean;
  paymentSucceeded: boolean;
  subscriptionActive: boolean;
  updatedAt: string;
}

export interface VerificationSummary {
  sessionId: string;
  status: string;
  provider: string;
  attempts: number;
  updatedAt: string;
}

export interface DeliverySummary {
  id: string;
  userId: string;
  eventType: string;
  channel: string;
  status: string;
  attempts: number;
  lastError: string | null;
  sentAt: string | null;
  createdAt: string;
}

export interface UserOverview {
  user: UserSummary;
  verification: VerificationSummary | null;
  payments: PaymentSummary[];
  subscription: SubscriptionSummary | null;
  access: AccessSummary | null;
  notifications: DeliverySummary[];
}

export interface Page<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export class ApiError extends Error {
  status: number;
  code: string;
  details: { field: string; message: string }[];
  correlationId?: string;

  constructor(status: number, code: string, message: string, details: { field: string; message: string }[] = [], correlationId?: string) {
    super(message);
    this.status = status;
    this.code = code;
    this.details = details;
    this.correlationId = correlationId;
  }
}

const ACCESS_KEY = "tl.access";
const REFRESH_KEY = "tl.refresh";

export const tokenStore = {
  access: () => sessionStorage.getItem(ACCESS_KEY),
  refresh: () => sessionStorage.getItem(REFRESH_KEY),
  save(tokens: TokenResponse) {
    sessionStorage.setItem(ACCESS_KEY, tokens.accessToken);
    sessionStorage.setItem(REFRESH_KEY, tokens.refreshToken);
  },
  clear() {
    sessionStorage.removeItem(ACCESS_KEY);
    sessionStorage.removeItem(REFRESH_KEY);
  },
};

let onSessionLost: () => void = () => undefined;
let refreshing: Promise<boolean> | null = null;

export function setSessionLostHandler(handler: () => void) {
  onSessionLost = handler;
}

async function parseError(response: Response): Promise<ApiError> {
  try {
    const body = await response.json();
    return new ApiError(response.status, body.code ?? "UNKNOWN", body.message ?? "Request failed", body.details ?? [], body.correlationId);
  } catch {
    return new ApiError(response.status, "UNKNOWN", "Request failed");
  }
}

async function refreshTokens(): Promise<boolean> {
  const refreshToken = tokenStore.refresh();
  if (!refreshToken) return false;
  const response = await fetch("/api/v1/auth/refresh", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ refreshToken }),
  });
  if (!response.ok) {
    tokenStore.clear();
    return false;
  }
  tokenStore.save(await response.json());
  return true;
}

interface RequestOptions {
  method?: string;
  body?: unknown;
  auth?: boolean;
  headers?: Record<string, string>;
}

export async function request<T>(path: string, options: RequestOptions = {}, retried = false): Promise<T> {
  const { method = "GET", body, auth = true, headers = {} } = options;
  const finalHeaders: Record<string, string> = { ...headers };
  if (body !== undefined) finalHeaders["Content-Type"] = "application/json";
  const access = tokenStore.access();
  if (auth && access) finalHeaders["Authorization"] = `Bearer ${access}`;

  const response = await fetch(`/api/v1${path}`, {
    method,
    headers: finalHeaders,
    body: body === undefined ? undefined : JSON.stringify(body),
  });

  if (response.status === 401 && auth && !retried && tokenStore.refresh()) {
    refreshing = refreshing ?? refreshTokens().finally(() => (refreshing = null));
    if (await refreshing) return request<T>(path, options, true);
    onSessionLost();
  }
  if (!response.ok) throw await parseError(response);
  if (response.status === 204) return undefined as T;
  const text = await response.text();
  return (text ? JSON.parse(text) : undefined) as T;
}

export const api = {
  register: (body: { email: string; password: string; fullName: string; phoneNumber?: string }) =>
    request<UserSummary>("/users", { method: "POST", body, auth: false }),
  login: (email: string, password: string) =>
    request<TokenResponse>("/auth/login", { method: "POST", body: { email, password }, auth: false }),
  logout: (refreshToken: string) => request<void>("/auth/logout", { method: "POST", body: { refreshToken }, auth: false }),
  verifyEmail: (token: string) => request<void>("/auth/verify-email", { method: "POST", body: { token }, auth: false }),
  requestReset: (email: string) => request<void>("/auth/password-reset/request", { method: "POST", body: { email }, auth: false }),
  confirmReset: (token: string, newPassword: string) =>
    request<void>("/auth/password-reset/confirm", { method: "POST", body: { token, newPassword }, auth: false }),
  me: () => request<UserSummary>("/users/me"),
  updateMe: (body: { fullName: string; phoneNumber?: string }) => request<UserSummary>("/users/me", { method: "PUT", body }),
  access: () => request<AccessSummary>("/access/me"),
  evaluateAccess: () => request<AccessSummary>("/access/evaluate", { method: "POST" }),
  startVerification: () => request<VerificationView>("/verifications", { method: "POST" }),
  verification: (id: string) => request<VerificationView>(`/verifications/${id}`),
  mockDecision: (id: string, decision: "VERIFIED" | "REJECTED") =>
    request<VerificationView>(`/verifications/${id}/mock-decision`, { method: "POST", body: { decision } }),
  plans: () => request<Plan[]>("/plans", { auth: false }),
  checkout: (planCode: string, idempotencyKey: string) =>
    request<CheckoutResult>("/payments/checkout", { method: "POST", body: { planCode }, headers: { "Idempotency-Key": idempotencyKey } }),
  payment: (id: string) => request<PaymentSummary>(`/payments/${id}`),
  subscription: () => request<SubscriptionSummary>("/subscriptions/me"),
  adminUsers: (query: string) => request<Page<UserSummary>>(`/admin/users?${query}`),
  adminOverview: (id: string) => request<UserOverview>(`/admin/users/${id}/overview`),
  adminPayments: (query: string) => request<Page<PaymentSummary>>(`/admin/payments?${query}`),
  adminNotifications: (query: string) => request<Page<DeliverySummary>>(`/admin/notifications?${query}`),
};
