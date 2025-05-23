package ma.fs.hopital;

import ma.fs.hopital.entities.Patient;
import ma.fs.hopital.repository.PatientRepository;
import ma.fs.hopital.security.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;

import java.util.Date;

@SpringBootApplication
public class HopitalApplication  {
	@Autowired
	private PatientRepository patientRepository;
	public static void main(String[] args) {
		SpringApplication.run(HopitalApplication.class, args);
	}

	//@Bean
	CommandLineRunner start(PatientRepository patientRepository) {
		return args -> {
			// Il existe trois méthodes pour insérer des patients
			// 1 ere méthode
			Patient patient = new Patient();
			patient.setId(null);
			patient.setNom("Wiame");
			patient.setDateNaissance(new Date());
			patient.setMalade(false);
			patient.setScore(23);
			//patientRepository.save(patient);

			// 2 eme méthode
			Patient patient2 = new Patient(null,"Anejjar Wiame",new Date(),false, 123);
			//patientRepository.save(patient2);

			// 3 eme méthode : en utilisant builder
			Patient patient3= Patient.builder()
					.nom("Anejjar")
					.dateNaissance(new Date())
					.score(56)
					.malade(true)
					.build();
			//patientRepository.save(patient3);

			patientRepository.save(new Patient(null,"Mohamed",new Date(),false,134));
			patientRepository.save(new Patient(null,"Hanae",new Date(),false,4321));
			patientRepository.save(new Patient(null,"Imane",new Date(),true,198));
			patientRepository.findAll().forEach(p ->{
				System.out.println(p.getNom());
			});
		};
	}
	//@Bean
	CommandLineRunner commandLineRunnerUserDetails(AccountService accountService) {
		return args -> {
			accountService.addNewRole("USER");
			accountService.addNewRole("ADMIN");
			accountService.addNewUser("user1","1234","user1@gmail.com","1234");
			accountService.addNewUser("user2","1234","user1@gmail.com","1234");
			accountService.addNewUser("admin","1234","admin@gmail.com","1234");

			accountService.addRoleToUser("user1","USER");
			accountService.addRoleToUser("user2","USER");
			accountService.addRoleToUser("admin","USER");
			accountService.addRoleToUser("admin","ADMIN");

		};

	}

	@Bean
	PasswordEncoder passwordEncder(){
		return new BCryptPasswordEncoder();
	}

	//@Bean
	CommandLineRunner commandLineRunner(JdbcUserDetailsManager jdbcUserDetailsManager){
		PasswordEncoder passwordEncoder = passwordEncder();
		return args ->{

			if(!jdbcUserDetailsManager.userExists("user11")){
				jdbcUserDetailsManager.createUser(User.withUsername("user11").password(passwordEncoder.encode("1234")).roles("USER").build());
			}
			if(!jdbcUserDetailsManager.userExists("user22")){
				jdbcUserDetailsManager.createUser(User.withUsername("user22").password(passwordEncoder.encode("1234")).roles("USER").build());
			}
			if(!jdbcUserDetailsManager.userExists("admin2")){
				jdbcUserDetailsManager.createUser(User.withUsername("admin2").password(passwordEncoder.encode("1234")).roles("USER","ADMIN").build());
			}

		};
	}

}
