package com.library.servlet;

import com.library.model.BookingIndicator;

public class BookingIndicatorCalculator {

    public static BookingIndicator calculate(
            int waitDays,
            int demandCount,
            int maxDemand,
            int totalCopies,
            int availableCopies) {

        if (waitDays < 0 || demandCount < 0 || maxDemand < 0
                || totalCopies < 0 || availableCopies < 0
                || availableCopies > totalCopies
                || demandCount > maxDemand) {
            throw new IllegalArgumentException(
                    "Invalid input values for indicator calculation."
            );
        }

        double f1 = Math.min(waitDays / 30.0, 1.0) * 100.0;

        double f2 = maxDemand > 0
                ? (demandCount / (double) maxDemand) * 100.0
                : 0.0;

        double f3 = totalCopies > 0
                ? ((totalCopies - availableCopies)
                / (double) totalCopies) * 100.0
                : 0.0;

        double integralValue =
                0.40 * f1 +
                        0.35 * f2 +
                        0.25 * f3;

        BookingIndicator indicator = new BookingIndicator();

        indicator.setWaitDays(waitDays);
        indicator.setF1(f1);

        indicator.setDemandCount(demandCount);
        indicator.setF2(f2);

        indicator.setTotalCopies(totalCopies);
        indicator.setAvailableCopies(availableCopies);
        indicator.setF3(f3);

        indicator.setIntegralValue(integralValue);

        return indicator;
    }
}
