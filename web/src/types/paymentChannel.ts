/**
 * Types pour la gestion dynamique des canaux de paiement.
 * Miroir du DTO PaymentChannelResponse du backend.
 */
export interface PaymentChannel {
    id: number;
    channelType: string;
    displayName: string;
    icon: string | null;
    isActive: boolean;
}

export interface PaymentChannelRequest {
    channelType: string;
    displayName: string;
    icon?: string;
}
