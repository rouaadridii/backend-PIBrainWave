package tn.esprit.brainwaveusermanagement.dto;


//DTO CLASS

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;
import tn.esprit.brainwaveusermanagement.Entities.RoleType;

import java.time.LocalDate;
import java.util.Date;


public class SignupRequest {


    private String cin;
    @NotBlank
    private String name;
    @NotBlank
    private String surname;
    @Email
    @NotBlank
    private String email;
    @NotBlank
    @Size(min = 6, message = "Password must be at least 8 characters")
    private String password;

    private String phoneNumber;

    private String address;

    private String level;

    private String birthDate;

    private RoleType role;













    // Getters and setters
    public void setCin(String cin) {
        this.cin = cin;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public void setRole(RoleType role) {
        this.role = role;
    }

    public String getCin() {
        return cin;
    }

    public String getName() {
        return name;
    }

    public String getSurname() {
        return surname;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getAddress() {
        return address;
    }

    public String getLevel() {
        return level;
    }

    public RoleType getRole() {
        return role;
    }

    public String getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(String birthDate) {
        this.birthDate = birthDate;
    }


}
