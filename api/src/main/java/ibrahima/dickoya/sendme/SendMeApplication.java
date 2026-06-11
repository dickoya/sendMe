package ibrahima.dickoya.sendme;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Classe principale de l'application SendMe.
 *
 * @EnableAsync active l'exécution asynchrone des méthodes annotées
 * avec @Async dans les services.
 *
 * Pourquoi @Async pour les emails ?
 * Sans @Async : l'utilisateur attend que l'email soit envoyé
 * avant de recevoir la réponse HTTP — peut prendre 2-3 secondes.
 * Avec @Async : l'email part en arrière-plan, la réponse HTTP
 * est immédiate et l'utilisateur n'attend pas.
 */
@SpringBootApplication
@EnableAsync
public class SendMeApplication {

	public static void main(String[] args) {
		SpringApplication.run(SendMeApplication.class, args);
	}
}