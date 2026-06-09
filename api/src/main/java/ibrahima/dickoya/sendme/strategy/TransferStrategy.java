package ibrahima.dickoya.sendme.strategy;

import ibrahima.dickoya.sendme.entity.Beneficiary;
import ibrahima.dickoya.sendme.entity.Transaction;

/**
 * Interface définissant le contrat du Strategy Pattern pour les transferts.
 *
 * Le Strategy Pattern permet d'isoler la logique d'appel vers chaque opérateur
 * (banque ou Mobile Money) dans une classe dédiée, tout en exposant
 * une interface commune au service de transfert.
 *
 * Avantage principal (Principe Ouvert/Fermé — SOLID) :
 * Pour ajouter un nouvel opérateur, il suffit de créer une nouvelle classe
 * implémentant cette interface, SANS modifier le code existant.
 *
 * Implémentations disponibles :
 * - BankTransferStrategy      → Virement bancaire SEPA/SWIFT
 * - OrangeMoneyStrategy       → Orange Money
 * - MtnMobileMoneyStrategy    → MTN Mobile Money
 * - WaveStrategy              → Wave
 * - MoovMoneyStrategy         → Moov Money
 * - AirtelMoneyStrategy       → Airtel Money
 */
public interface TransferStrategy {

    /**
     * Exécute le transfert vers l'opérateur cible.
     *
     * Cette méthode simule l'appel vers l'API externe de l'opérateur.
     * En production, elle ferait un vrai appel HTTP vers l'API de l'opérateur.
     *
     * @param transaction  la transaction contenant le montant, la devise, etc.
     * @param beneficiary  le bénéficiaire avec ses coordonnées de réception
     * @return TransferResult le résultat de l'opération (succès ou échec)
     */
    TransferResult execute(Transaction transaction, Beneficiary beneficiary);
}