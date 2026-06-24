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
    referralCode?: string;
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

/**
 * Canal de réception d'un bénéficiaire.
 * Un bénéficiaire peut avoir 1 ou plusieurs canaux.
 */
export interface BeneficiaryChannel {
    id: number;
    channel: ChannelType;
    phoneNumber?: string;
    countryCode?: string;
    bankName?: string;
    /** IBAN masqué pour la sécurité */
    iban?: string;
    bic?: string;
    isPrimary: boolean;
    /** Devise déterminée par le pays (ex: GIN → GNF, SEN → XOF, FRA → EUR) */
    currency: string;
}

export interface Beneficiary {
    id: number;
    firstName: string;
    lastName: string;
    channel: ChannelType;
    channels: BeneficiaryChannel[];
    createdAt: string;
}

export interface BeneficiaryChannelFormData {
    channel: ChannelType;
    phoneNumber?: string;
    countryCode?: string;
    bankName?: string;
    iban?: string;
    bic?: string;
    isPrimary: boolean;
}

export interface BeneficiaryFormData {
    firstName: string;
    lastName: string;
    channel: ChannelType;
    channels: BeneficiaryChannelFormData[];
}

// ── Transferts ─────────────────────────────────────────────────────────
export interface SimulateRequest {
    amount: number;
    currencyFrom: string;
    currencyTo: string;
    channel: ChannelType;
    /** Si true (défaut), applique la réduction de parrainage si un coupon valide existe */
    applyDiscount?: boolean;
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
    discount?: number;
}

export interface TransferRequest {
    amount: number;
    currency: string;
    beneficiaryId: number;
    /** Identifiant du canal de réception choisi (optionnel, utilise le canal principal si non fourni) */
    channelId?: number;
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
// ── Parrainage ─────────────────────────────────────────────────────────
export interface ReferralCodeResponse {
    referralCode: string;
    shareLink: string;
    totalReferred: number;
    totalCouponsEarned: number;
    availableCoupons: number;
}

export interface ReferralCouponResponse {
    id: number;
    couponType: 'SPONSOR' | 'REFERRED';
    discountAmount: number;
    currency: string;
    isUsed: boolean;
    expiresAt: string;
    usedAt: string | null;
    referredUserName: string;
    transactionId: number | null;
    createdAt: string;
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
