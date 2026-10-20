package com.library.model;

import java.util.Date;

public class Reader {
    private int id;
    private String fullName;
    private String email; // Додали поле email
    private String phoneNumber;
    private String address;
    private Date registrationDate;

    public Reader(int id, String fullName, String phoneNumber, String address, Date registrationDate) {
        this.id = id;
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.registrationDate = registrationDate;
    }

    public Reader(int id, String fullName, String email, String phoneNumber, String address, Date registrationDate) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.registrationDate = registrationDate;
    }

    public int getId() { return id; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; } // Додали гетер для email
    public String getPhoneNumber() { return phoneNumber; }
    public String getAddress() { return address; }
    public Date getRegistrationDate() { return registrationDate; }
}