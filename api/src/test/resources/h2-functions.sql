-- Enregistre la fonction MySQL SUBSTRING_INDEX pour H2
-- Cette fonction extrait une sous-chaîne entre des occurrences d'un délimiteur
-- Exemple : SUBSTRING_INDEX('TXN-20250609-0042', '-', -1) → '0042'
CREATE ALIAS IF NOT EXISTS SUBSTRING_INDEX FOR "ibrahima.dickoya.sendme.config.TestH2Config$H2FunctionRegistrar.substringIndex";
