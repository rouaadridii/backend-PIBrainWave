package tn.esprit.brainwaveusermanagement.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;



public class UpdateProfileRequest {

    private String name;
    private String surname;

    @Email
    private String email;

    private String phoneNumber;
    private String address;
    private String level;
    private String birthDate;

    // Password update fields (optional)
    private String oldPassword;

    @Size(min = 6, message = "Password must be at least 6 characters")
    private String newPassword;


    public void setName(String name) {
        this.name = name;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public void setEmail(String email) {
        this.email = email;
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

    public void setBirthDate(String birthDate) {
        this.birthDate = birthDate;
    }

    public void setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
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

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getAddress() {
        return address;
    }

    public String getLevel() {
        return level;
    }

    public String getBirthDate() {
        return birthDate;
    }

    public String getOldPassword() {
        return oldPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }
}
