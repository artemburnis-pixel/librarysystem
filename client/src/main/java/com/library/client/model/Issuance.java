
package com.library.client.model;

import java.util.Date;

public class Issuance {

    private int id;
    private int bookingId;
    private Date issueDate;
    private Date returnDate;

    public Issuance() {
    }

    public Issuance(int id, int bookingId,
                    Date issueDate, Date returnDate) {
        this.id = id;
        this.bookingId = bookingId;
        this.issueDate = issueDate;
        this.returnDate = returnDate;
    }

    public Issuance(int bookingId, Date issueDate, Date returnDate) {
        this.bookingId = bookingId;
        this.issueDate = issueDate;
        this.returnDate = returnDate;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getBookingId() {
        return bookingId;
    }

    public void setBookingId(int bookingId) {
        this.bookingId = bookingId;
    }

    public Date getIssueDate() {
        return issueDate;
    }

    public void setIssueDate(Date issueDate) {
        this.issueDate = issueDate;
    }

    public Date getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(Date returnDate) {
        this.returnDate = returnDate;
    }
}
