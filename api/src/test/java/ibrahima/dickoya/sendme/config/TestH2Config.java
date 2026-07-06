package ibrahima.dickoya.sendme.config;

import jakarta.annotation.PostConstruct;
import org.h2.tools.SimpleResultSet;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Configuration de test pour enregistrer les fonctions MySQL manquantes dans
 * H2.
 * <p>
 * H2 en mode MySQL ne supporte pas nativement {@code SUBSTRING_INDEX}.
 * Cette configuration enregistre un alias pour cette fonction afin que
 * les requêtes natives MySQL fonctionnent également avec H2.
 */
@TestConfiguration
public class TestH2Config {

    /**
     * Enregistre la fonction SUBSTRING_INDEX pour H2.
     * Cette fonction MySQL extrait une sous-chaîne entre des occurrences
     * d'un délimiteur. Exemple : SUBSTRING_INDEX('TXN-20250609-0042', '-', -1) →
     * '0042'
     */
    @Bean
    public H2FunctionRegistrar h2FunctionRegistrar(DataSource dataSource) {
        return new H2FunctionRegistrar(dataSource);
    }

    static class H2FunctionRegistrar {

        private final DataSource dataSource;

        H2FunctionRegistrar(DataSource dataSource) {
            this.dataSource = dataSource;
        }

        @PostConstruct
        public void registerFunctions() {
            try (Connection conn = dataSource.getConnection();
                    Statement stmt = conn.createStatement()) {
                // Enregistrer SUBSTRING_INDEX comme alias Java
                stmt.execute("CREATE ALIAS IF NOT EXISTS SUBSTRING_INDEX FOR \"" +
                        H2FunctionRegistrar.class.getName() + ".substringIndex\"");
            } catch (SQLException e) {
                throw new RuntimeException("Impossible d'enregistrer SUBSTRING_INDEX pour H2", e);
            }
        }

        /**
         * Implémentation Java de SUBSTRING_INDEX pour H2.
         * <p>
         * Comportement identique à MySQL :
         * <ul>
         * <li>Si count > 0 : retourne les count premières occurrences du délimiteur
         * et tout ce qui précède</li>
         * <li>Si count < 0 : retourne les |count| dernières occurrences du délimiteur
         * et tout ce qui suit</li>
         * </ul>
         *
         * @param str   la chaîne d'entrée
         * @param delim le délimiteur
         * @param count le nombre d'occurrences (positif = depuis la gauche, négatif =
         *              depuis la droite)
         * @return la sous-chaîne extraite
         */
        public static String substringIndex(String str, String delim, int count) {
            if (str == null || delim == null || delim.isEmpty()) {
                return str;
            }

            if (count > 0) {
                // Compter depuis la gauche
                int idx = -1;
                for (int i = 0; i < count; i++) {
                    idx = str.indexOf(delim, idx + 1);
                    if (idx < 0) {
                        return str;
                    }
                }
                return str.substring(0, idx);
            } else if (count < 0) {
                // Compter depuis la droite
                int idx = str.length();
                int absCount = -count;
                for (int i = 0; i < absCount; i++) {
                    idx = str.lastIndexOf(delim, idx - 1);
                    if (idx < 0) {
                        return str;
                    }
                }
                return str.substring(idx + delim.length());
            }

            return "";
        }
    }
}
