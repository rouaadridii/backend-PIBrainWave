package tn.esprit.brainwaveusermanagement.Services;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import tn.esprit.brainwaveusermanagement.Entities.Person;
import tn.esprit.brainwaveusermanagement.Entities.RoleType;
import tn.esprit.brainwaveusermanagement.Entities.UserStatus;
import tn.esprit.brainwaveusermanagement.Repositories.PersonRepository;
import tn.esprit.brainwaveusermanagement.Utils.JwtUtils;
import tn.esprit.brainwaveusermanagement.Utils.PasswordEncoderUtil;
import tn.esprit.brainwaveusermanagement.dto.UpdateProfileRequest;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service

public class PersonService {

    private PersonRepository personRepository;
    private final JwtUtils jwtUtils;

    private CloudinaryService cloudinaryService;
    @Autowired
    private PasswordEncoder passwordEncoder;

    public PersonService(PersonRepository personRepository, JwtUtils jwtUtils,PasswordEncoder passwordEncoder,CloudinaryService cloudinaryService) {
        this.personRepository = personRepository;
        this.jwtUtils = jwtUtils;
        this.passwordEncoder = passwordEncoder;
        this.cloudinaryService = cloudinaryService;
    }
    public Person findUserByEmail(String email) {
        return personRepository.findByEmail(email).orElseThrow(() -> new NoSuchElementException("User not found with email: " + email));

    }

    public Person getPersonByCin(String cin) {
        return personRepository.findByCin(cin);  // Fetch person by CIN
    }

    public boolean existsByEmail(String email) {
        return personRepository.existsByEmail(email);
    }

    public boolean existsByCin(String cin) {
        return personRepository.existsByCin(cin);}

    public Person findById(Long id) {
        return personRepository.findById(id).orElse(null);
    }

    public Person findBycin(String cin){return  personRepository.findByCin(cin);}

    public void deletePerson(Person person) {
        try {
            cloudinaryService.deleteImage(person.getPicture(),person.getCv(),person.getDiploma());
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete image from Cloudinary", e);
        }
        personRepository.delete(person);
    }

    public List<Person> findByStatus(UserStatus status) {
        return personRepository.findByStatus(status);
    }

    public void savePerson(Person person) {
        personRepository.save(person);  // Save updated person entity
    }

    public Person getAuthenticatedUser(String token) {
        String email = jwtUtils.extractUsername(token);
        // Fetch user from database
        return personRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public Person updateUserProfile(String token, UpdateProfileRequest updateRequest) {
        String extractedToken = token.replace("Bearer ", "");
        Person user = getAuthenticatedUser(extractedToken);

        // If user provided a new password, verify old password first
        if (updateRequest.getOldPassword() != null && !updateRequest.getOldPassword().isEmpty()) {
            if (!passwordEncoder.matches(updateRequest.getOldPassword(), user.getPassword())) {
                throw new RuntimeException("Old password is incorrect!");
            }
            // Encrypt and update password
            user.setPassword(passwordEncoder.encode(updateRequest.getNewPassword()));
        }

        // Update other profile fields only if provided
        if (updateRequest.getName() != null) user.setName(updateRequest.getName());
        if (updateRequest.getSurname() != null) user.setSurname(updateRequest.getSurname());
        if (updateRequest.getPhoneNumber() != null) user.setPhoneNumber(updateRequest.getPhoneNumber());
        if (updateRequest.getAddress() != null) user.setAddress(updateRequest.getAddress());
        if (updateRequest.getLevel() != null) user.setLevel(updateRequest.getLevel());
        Date birthDate = null;
        if (updateRequest.getBirthDate() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            try {
                birthDate = sdf.parse(updateRequest.getBirthDate());  // Parse the date string
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        }
        if (birthDate != null) {
            user.setBirthDate(birthDate);  // Set birthDate in the Person entity
        }

        return personRepository.save(user);
    }

    public List<Person>findAllExceptAdmin(){
        return personRepository.findByRoleNot(RoleType.ADMIN);
    }

    public Person banUser(String email) {
        Optional<Person> userOptional = personRepository.findByEmail(email);
        if (userOptional.isPresent()) {
            Person user = userOptional.get();
            user.setBanned(true);
            return personRepository.save(user);
        } else {
            return null;
        }
    }

    public Person unbanUser(String email) {
        Optional<Person> userOptional = personRepository.findByEmail(email);
        if (userOptional.isPresent()) {
            Person user = userOptional.get();
            user.setBanned(false);
            return personRepository.save(user);
        } else {
            return null;
        }
    }
}

