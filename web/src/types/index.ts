/**
 * Types TypeScript miroir des DTOs et enums du backend SendMe.
 * Garder ce fichier synchronisé avec :
 * - ibrahima.dickoya.sendme.enums.*
 * - ibrahima.dickoya.sendme.dto.response.*
 */

// ── Enums ──────────────────────────────────────────────────────────────
export type ChannelType =
    | 'BANK_TRANSFER'
    | 'ORANGE_MONEY'
    | 'MTN_MOBILE_MONEY'
    | 'WAVE'
    | 'MOOV_MONEY'
    | 'AIRTEL_MONEY';

export type TransactionStatus =
    | 'PENDING'
    | 'PROCESSING'
    | 'COMPLETED'
    | 'FAILED'
    | 'REFUNDED';

export type Role = 'ROLE_USER' | 'ROLE_ADMIN';

export type NotificationType =
    | 'TRANSFER_INITIATED'
    | 'TRANSFER_PROCESSING'
    | 'TRANSFER_COMPLETED'
    | 'TRANSFER_FAILED'
    | 'TRANSFER_REFUNDED'
    | 'PAYMENT_CONFIRMED'
    | 'PAYMENT_FAILED'
    | 'ACCOUNT_CREATED'
    | 'ACCOUNT_DISABLED'
    | 'ACCOUNT_REACTIVATED'
    | 'SECURITY_LOGIN'
    | 'SECURITY_PASSWORD_CHANGED'
    | 'SECURITY_SUSPICIOUS_LOGIN'
    | 'INFO'
    | 'MAINTENANCE'
    | 'FEE_UPDATE';

// ── Auth ───────────────────────────────────────────────────────────────
export interface AuthResponse {
    accessToken: string;
    refreshToken: string;
    tokenType: string;
    expiresIn: number;
    email: string;
    firstName: string;
    lastName: string;
    role: Role;
}

export interface User {
    id: number;
    firstName: string;
    lastName: string;
    email: string;
    phoneNumber?: string;
    role: Role;
    isActive: boolean;
    createdAt: string;
}

// ── Bénéficiaires ──────────────────────────────────────────────────────
export interface BankDetails {
    id?: number;
    bankName: string;
    iban: string;
    bic: string;
}

export interface MobileMoneyDetails {
    id?: number;
    phoneNumber: string;
}

export interface Beneficiary {
    id: number;
    firstName: string;
    lastName: string;
    channel: ChannelType;
    bankDetails?: BankDetails;
    mobileDetails?: MobileMoneyDetails;
    createdAt: string;
}

export interface BeneficiaryFormData {
    firstName: string;
    lastName: string;
    channel: ChannelType;
    bankDetails?: BankDetails;
    mobileDetails?: MobileMoneyDetails;
}

// ── Transferts ─────────────────────────────────────────────────────────
export interface SimulateRequest {
    amount: number;
    currencyFrom: string;
    currencyTo: string;
    channel: ChannelType;
}

export interface SimulateResponse {
    amountSent: number;
    currencyFrom: string;
    fees: number;
    feeRate: number;
    totalDebited: number;
    exchangeRate: number;
    currencyTo: string;
    amountReceived: number;
}

export interface TransferRequest {
    amount: number;
    currency: string;
    beneficiaryId: number;
    paymentMethodId: string;
    idempotencyKey: string;
}

export interface Transaction {
    id: number;
    reference: string;
    amountSent: number;
    currencySent: string;
    amountReceived: number;
    currencyReceived: string;
    exchangeRate: number;
    fees: number;
    status: TransactionStatus;
    channel: ChannelType;
    stripePaymentIntentId?: string;
    stripePaymentStatus?: string;
    beneficiary?: Beneficiary;
    createdAt: string;
    updatedAt: string;
}

// ── Notifications ──────────────────────────────────────────────────────
export interface Notification {
    id: number;
    message: string;
    type: NotificationType;
    isRead: boolean;
    transactionId?: number;
    transactionReference?: string;
    createdAt: string;
}

// ── Pagination ─────────────────────────────────────────────────────────
export interface Page<T> {
    content: T[];
    totalElements: number;
    totalPages: number;
    number: number;
    size: number;
}

// ── API générique ──────────────────────────────────────────────────────
export interface ApiResponse<T> {
    success: boolean;
    message: string;
    data: T;
}

// ── Admin ──────────────────────────────────────────────────────────────
export interface UserResponse extends User {
    totalTransactions: number;
    totalAmountSent: number;
}

export interface DashboardResponse {
    totalUsers: number;
    activeUsers: number;
    inactiveUsers: number;
    totalTransactions: number;
    completedTransactions: number;
    failedTransactions: number;
    refundedTransactions: number;
    pendingTransactions: number;
    successRate: number;
    totalVolume: number;
    totalFees: number;
    todayVolume: number;
    todayTransactions: number;
    transactionsByChannel: Record<string, number>;
    activeSSEConnections: number;
}

export interface ExchangeRateDebug {
    from: string;
    to: string;
    rawRate: number | null;
    marginRate: number | null;
    cacheTimestamp: string;
}

export interface ExchangeRateDebugEntry {
    from: string;
    to: string;
    rawRate: number | null;
    marginRate: number | null;
    cacheTimestamp: string;
}
// ── Erreurs API ────────────────────────────────────────────────────────
/**
 * Format d'erreur retourné par GlobalExceptionHandler du backend.
 */
export interface ApiError {
    success: false;
    message: string;
    data?: null;
}
