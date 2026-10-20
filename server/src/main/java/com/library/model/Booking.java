package com.library.model;

import java.util.Date;

public class Booking {
    private int id;
    private int readerId;
    private int publicationId;
    private Date bookingDate;
    private Date expiryDate;
    private String status;

    public Booking(int id, int readerId, int publicationId, Date bookingDate, Date expiryDate, String status) {
        this.id = id;
        this.readerId = readerId;
        this.publicationId = publicationId;
        this.bookingDate = bookingDate;
        this.expiryDate = expiryDate;
        this.status = status;
    }

    public Booking(int readerId, int publicationId, Date bookingDate, Date expiryDate, String status) {
        this.readerId = readerId;
        this.publicationId = publicationId;
        this.bookingDate = bookingDate;
        this.expiryDate = expiryDate;
        this.status = status;
    }

    public int getId() { return id; }
    public int getReaderId() { return readerId; }
    public int getPublicationId() { return publicationId; }
    public Date getBookingDate() { return bookingDate; }
    public Date getExpiryDate() { return expiryDate; }
    public Date getDueDate() { return expiryDate; }
    public String getStatus() { return status; }
}