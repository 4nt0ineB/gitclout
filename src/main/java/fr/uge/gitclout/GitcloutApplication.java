package fr.uge.gitclout;

import fr.uge.gitclout.model.RepositoryModel;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@SpringBootApplication
public class GitcloutApplication {
	
	
	public static void main(String[] args) {
		SpringApplication.run(GitcloutApplication.class, args);
	}
	
	/*@Bean
	public CommandLineRunner commandLineRunner(
			RepositoryService service
	){
		return args -> {
			for(int i = 0; i < 4; i++){
				var str = "test" + i;
				service.save(new RepositoryModel(str, str, str, str));
			}
		};
	}*/

}
