package fr.uge.gitclout;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

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
				service.save(new Repository(str, str, str, str));
			}
		};
	}*/

}
