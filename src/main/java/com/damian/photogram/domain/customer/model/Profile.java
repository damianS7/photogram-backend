package com.damian.photogram.domain.customer.model;

import com.damian.photogram.domain.customer.enums.CustomerGender;
import jakarta.persistence.*;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "customer_profiles")
public class Profile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", referencedColumnName = "id")
    private Customer customer;

    @Column
    private String username;

    @Column
    private String firstName;

    @Column
    private String lastName;

    @Column
    private String phone;

    @Column
    private LocalDate birthdate;

    @Column
    @Enumerated(EnumType.STRING)
    private CustomerGender gender;

    @Column(name = "image_filename")
    private String imageFilename;

    @Column(name = "about_me")
    private String aboutMe;

    @Column
    private Instant updatedAt;

    public Profile() {
    }

    public Profile(Customer customer) {
        this.customer = customer;
    }

    public static Profile create(Customer owner) {
        Profile profile = new Profile();
        profile.setOwner(owner);
        return profile;
    }

    public Customer getOwner() {
        return this.customer;
    }

    public Profile setOwner(Customer customer) {
        this.customer = customer;
        return this;
    }

    public Long getId() {
        return id;
    }

    public Profile setId(Long id) {
        this.id = id;
        return this;
    }

    public Long getCustomerId() {
        return this.customer.getId();
    }

    public CustomerGender getGender() {
        return this.gender;
    }

    public Profile setGender(CustomerGender gender) {
        this.gender = gender;
        return this;
    }

    public String getFirstName() {
        return firstName;
    }

    public Profile setFirstName(String firstName) {
        this.firstName = firstName;
        return this;
    }

    public String getLastName() {
        return lastName;
    }

    public Profile setLastName(String lastName) {
        this.lastName = lastName;
        return this;
    }

    public String getPhone() {
        return phone;
    }

    public Profile setPhone(String phone) {
        this.phone = phone;
        return this;
    }

    public LocalDate getBirthdate() {
        return birthdate;
    }

    public Profile setBirthdate(LocalDate birthdate) {
        this.birthdate = birthdate;
        return this;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public Profile setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
        return this;
    }

    public String getImageFilename() {
        return imageFilename;
    }

    public Profile setImageFilename(String imageFilename) {
        this.imageFilename = imageFilename;
        return this;
    }

    public String getUsername() {
        return username;
    }

    public Profile setUsername(String username) {
        this.username = username;
        return this;
    }

    public String getAboutMe() {
        return aboutMe;
    }

    public Profile setAboutMe(String aboutMe) {
        this.aboutMe = aboutMe;
        return this;
    }
}
