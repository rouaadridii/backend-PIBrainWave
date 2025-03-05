package tn.esprit.brainwaveusermanagement;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.password.PasswordEncoder;
import tn.esprit.brainwaveusermanagement.Entities.Person;
import tn.esprit.brainwaveusermanagement.Repositories.PersonRepository;
import tn.esprit.brainwaveusermanagement.Entities.RoleType;
@SpringBootApplication
public class BrainWaveUserManagementApplication implements CommandLineRunner {

	@Autowired
	private PersonRepository personRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	public static void main(String[] args) {
		SpringApplication.run(BrainWaveUserManagementApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		Person admin = personRepository.findByRole(RoleType.ADMIN);
		if (admin == null) {
			admin = new Person();
			admin.setEmail("dridi.roua@esprit.tn");
			admin.setRole(RoleType.ADMIN);
			admin.setPassword(passwordEncoder.encode("123456"));  // Default password
			admin.setName("Roua");
			admin.setSurname("Dridi");
			admin.setPicture("http://res.cloudinary.com/dk5nt6kia/image/upload/v1740332614/dd0wemmknj7bs6ixwrb1.jpg");
			personRepository.save(admin);
			System.out.println("Default admin created");
		} else {
			System.out.println("Admin already exists.");
		}
	}

}
