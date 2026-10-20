package com.library.model;

import java.util.Date;

public class Issuance {
    private int id;
    private int readerId;
    private int publicationId;
    private Date issueDate;
    private Date dueDate;
    private Date returnDate;
    private String status;

    public Issuance(int id, int readerId, int publicationId, Date issueDate, Date dueDate, Date returnDate, String status) {
        this.id = id;
        this.readerId = readerId;
        this.publicationId = publicationId;
        this.issueDate = issueDate;
        this.dueDate = dueDate;
        this.returnDate = returnDate;
        this.status = status;
    }

    public Issuance(int readerId, int publicationId, Date issueDate, Date dueDate, Date returnDate, String status) {
        this.readerId = readerId;
        this.publicationId = publicationId;
        this.issueDate = issueDate;
        this.dueDate = dueDate;
        this.returnDate = returnDate;
        this.status = status;
    }

    public int getId() { return id; }
    public int getReaderId() { return readerId; }
    public int getPublicationId() { return publicationId; }
    public Date getIssueDate() { return issueDate; }
    public Date getDueDate() { return dueDate; }
    public Date getReturnDate() { return returnDate; }
    public String getStatus() { return status; }
}