package tn.esprit.brainwaveusermanagement.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.brainwaveusermanagement.Entities.Person;
import tn.esprit.brainwaveusermanagement.Entities.UserStatus;

import java.util.List;
import java.util.Optional;

public interface PersonRepository extends JpaRepository<Person, Long> {
    Person findByCin(String cin);
    boolean existsByEmail(String email);
    Optional<Person> findByEmail(String email);
    boolean existsByCin(String email);

    List<Person> findByStatus(UserStatus status);
}
