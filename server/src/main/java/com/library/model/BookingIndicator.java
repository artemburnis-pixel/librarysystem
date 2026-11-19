
package com.library.model;

public class BookingIndicator {

    private int bookingId;
    private int publicationId;
    private String publicationTitle;

    private int waitDays;
    private double f1;

    private int demandCount;
    private double f2;

    private int totalCopies;
    private int availableCopies;
    private double f3;

    private double integralValue;

    public BookingIndicator() {
    }

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

    public double getF1() {
        return f1;
    }

    public void setF1(double f1) {
        this.f1 = f1;
    }

    public int getDemandCount() {
        return demandCount;
    }

    public void setDemandCount(int demandCount) {
        this.demandCount = demandCount;
    }

    public double getF2() {
        return f2;
    }

    public void setF2(double f2) {
        this.f2 = f2;
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

    public double getF3() {
        return f3;
    }

    public void setF3(double f3) {
        this.f3 = f3;
    }

    public double getIntegralValue() {
        return integralValue;
    }

    public void setIntegralValue(double integralValue) {
        this.integralValue = integralValue;
    }
}
