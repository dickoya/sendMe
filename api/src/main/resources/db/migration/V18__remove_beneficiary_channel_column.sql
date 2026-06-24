-- Migration V18 : Supprimer la colonne channel redondante de la table beneficiaries
-- La colonne channel était conservée pour compatibilité mais est maintenant obsolète
-- car chaque bénéficiaire peut avoir plusieurs canaux via beneficiary_channels

-- Supprimer la colonne channel qui est devenue redondante
ALTER TABLE beneficiaries DROP COLUMN channel;
