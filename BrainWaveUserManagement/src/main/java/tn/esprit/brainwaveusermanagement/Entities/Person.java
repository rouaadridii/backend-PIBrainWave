package tn.esprit.brainwaveusermanagement.Entities;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import tn.esprit.brainwaveusermanagement.Utils.FloatListToStringConverter;

import java.util.Date;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Person {
     @Id
     @GeneratedValue(strategy = GenerationType.IDENTITY)
     Long idP;


     String cin;

     String name;
     String surname;
     String email;
     String password;
     String phoneNumber;
     String address;
     @Temporal(TemporalType.DATE)
     Date birthDate;
     String picture;
     String cv;
     String diploma;
     String level;
     @Enumerated(EnumType.STRING)
     UserStatus status;
     @Column(nullable = false)
     boolean banned = false;
     @Enumerated(EnumType.STRING)
     RoleType role;
    @Column(name = "face_descriptor", columnDefinition = "float[]")
    @Convert(converter = FloatListToStringConverter.class)
    List<Float> faceDescriptor;







    public List<Float> getFaceDescriptor() {
        return faceDescriptor;
    }

    public void setFaceDescriptor(List<Float> faceDescriptor) {
        this.faceDescriptor = faceDescriptor;
    }
    public void setStatus(UserStatus status) {
        this.status = status;
    }
    public void setName(String name) {
  this.name = name;
 }

     public String getSurname() {
  return surname;
 }

    public String getCv() {
  return cv;
 }
    public String getDiploma() {
  return diploma;
 }

    public String getLevel() {
  return level;
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

    public void setBirthDate(Date birthDate) {
  this.birthDate = birthDate;
 }

    public void setLevel(String level) {
  this.level = level;
 }

    public void setRole(RoleType role) {
  this.role = role;
 }

    public RoleType getRole() {
  return role;
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

    public Date getBirthDate() {
  return birthDate;
 }

    public String getPicture() {
  return picture;
 }

    public String getName() {
  return name;
 }

    public UserStatus getStatus() {
        return status;
    }

    public void setCin(String cin) {
  this.cin = cin;
 }
    public String getCin() {
  return this.cin;
 }
    public void setPicture(String picture) {
  this.picture = picture;
 }

    public void setCv(String cv) {
  this.cv = cv;
 }

    public void setDiploma(String diploma) {
  this.diploma = diploma;
 }
    public boolean isBanned() {
        return banned;
    }

    public void setBanned(boolean banned) {
        this.banned = banned;
    }
}
