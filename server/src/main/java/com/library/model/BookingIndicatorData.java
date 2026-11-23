package com.library.model;

public class BookingIndicatorData {

    private int bookingId;
    private int publicationId;
    private String publicationTitle;
    private int waitDays;
    private int demandCount;
    private int maxDemand;
    private int totalCopies;
    private int availableCopies;

    public int getBookingId() {
        return bookingId;
    }

    public void setBookingId(int bookingId) {
        this.bookingId = bookingId;
    }

    public int getPublicationId() {
        return publicationId;
    }

    public void setPublicationId(int publicationId) {
        this.publicationId = publicationId;
    }

    public String getPublicationTitle() {
        return publicationTitle;
    }

    public void setPublicationTitle(String publicationTitle) {
        this.publicationTitle = publicationTitle;
    }

    public int getWaitDays() {
        return waitDays;
    }

    public void setWaitDays(int waitDays) {
        this.waitDays = waitDays;
    }

    public int getDemandCount() {
        return demandCount;
    }

    public void setDemandCount(int demandCount) {
        this.demandCount = demandCount;
    }

    public int getMaxDemand() {
        return maxDemand;
    }

    public void setMaxDemand(int maxDemand) {
        this.maxDemand = maxDemand;
    }

    public int getTotalCopies() {
        return totalCopies;
    }

    public void setTotalCopies(int totalCopies) {
        this.totalCopies = totalCopies;
    }

    public int getAvailableCopies() {
        return availableCopies;
    }

    public void setAvailableCopies(int availableCopies) {
        this.availableCopies = availableCopies;
    }
}