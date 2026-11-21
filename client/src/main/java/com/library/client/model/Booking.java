package com.library.client.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.Date;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Booking {

    private int id;
    private int readerId;
    private int publicationId;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "UTC")
    private Date bookingDate;

    @JsonAlias("dueDate")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "UTC")
    private Date expiryDate;

    private String status;

    public Booking() {
    }

    public Booking(int id, int readerId, int publicationId,
                   Date bookingDate, Date expiryDate, String status) {
        this.id = id;
        this.readerId = readerId;
        this.publicationId = publicationId;
        this.bookingDate = bookingDate;
        this.expiryDate = expiryDate;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getReaderId() {
        return readerId;
    }

    public void setReaderId(int readerId) {
        this.readerId = readerId;
    }

    public int getPublicationId() {
        return publicationId;
    }

    public void setPublicationId(int publicationId) {
        this.publicationId = publicationId;
    }

    public Date getBookingDate() {
        return bookingDate;
    }

    public void setBookingDate(Date bookingDate) {
        this.bookingDate = bookingDate;
    }

    public Date getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(Date expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getDueDate() {
        return expiryDate;
    }

    public void setDueDate(Date dueDate) {
        this.expiryDate = dueDate;
    }
}