package ibrahima.dickoya.sendme;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync //  Active l'exécution des tâches en arrière-plan (indispensable pour les e-mails)
public class SendMeApplication {

	public static void main(String[] args) {
		SpringApplication.run(SendMeApplication.class, args);
	}

}
